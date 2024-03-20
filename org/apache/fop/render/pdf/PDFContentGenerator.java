package org.apache.fop.render.pdf;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.fop.pdf.PDFColorHandler;
import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.pdf.PDFLinearization;
import org.apache.fop.pdf.PDFNumber;
import org.apache.fop.pdf.PDFPaintingState;
import org.apache.fop.pdf.PDFReference;
import org.apache.fop.pdf.PDFResourceContext;
import org.apache.fop.pdf.PDFStream;
import org.apache.fop.pdf.PDFText;
import org.apache.fop.pdf.PDFTextUtil;
import org.apache.fop.pdf.PDFXObject;
import org.apache.fop.render.intermediate.IFContext;

public class PDFContentGenerator {
   protected static final boolean WRITE_COMMENTS = true;
   private PDFDocument document;
   private OutputStream outputStream;
   private PDFResourceContext resourceContext;
   private PDFStream currentStream;
   private PDFColorHandler colorHandler;
   protected PDFPaintingState currentState;
   protected PDFTextUtil textutil;
   private boolean inMarkedContentSequence;
   private boolean inArtifactMode;
   private AffineTransform transform;
   private IFContext context;
   private int ocNameIndex;

   public PDFContentGenerator(PDFDocument document, OutputStream out, PDFResourceContext resourceContext) {
      this(document, out, resourceContext, (IFContext)null);
   }

   public PDFContentGenerator(PDFDocument document, OutputStream out, PDFResourceContext resourceContext, IFContext context) {
      this.document = document;
      this.outputStream = out;
      this.resourceContext = resourceContext;
      this.currentStream = document.getFactory().makeStream("content", false);
      this.textutil = new PDFTextUtil() {
         protected void write(String code) {
            PDFContentGenerator.this.currentStream.add(code);
         }

         protected void write(StringBuffer code) {
            PDFContentGenerator.this.currentStream.add(code);
         }
      };
      this.currentState = new PDFPaintingState();
      this.colorHandler = new PDFColorHandler(document.getResources());
      this.context = context;
   }

   public AffineTransform getAffineTransform() {
      return this.transform;
   }

   public PDFDocument getDocument() {
      return this.document;
   }

   public OutputStream getOutputStream() {
      return this.outputStream;
   }

   public PDFResourceContext getResourceContext() {
      return this.resourceContext;
   }

   public PDFStream getStream() {
      return this.currentStream;
   }

   public PDFPaintingState getState() {
      return this.currentState;
   }

   public PDFTextUtil getTextUtil() {
      return this.textutil;
   }

   public void flushPDFDoc() throws IOException {
      if (this.document.isLinearizationEnabled()) {
         (new PDFLinearization(this.document)).outputPages(this.outputStream);
      }

      this.document.output(this.outputStream);
   }

   protected void comment(String text) {
      this.getStream().add("% " + text + "\n");
   }

   protected void saveGraphicsState() {
      this.endTextObject();
      this.getState().save();
      this.getStream().add("q\n");
   }

   protected void saveGraphicsState(String layer) {
      this.endTextObject();
      this.getState().save();
      this.maybeBeginLayer(layer);
      this.getStream().add("q\n");
   }

   protected void saveGraphicsState(String structElemType, int sequenceNum) {
      this.endTextObject();
      this.getState().save();
      this.beginMarkedContentSequence(structElemType, sequenceNum);
      this.getStream().add("q\n");
   }

   protected void beginMarkedContentSequence(String structElemType, int mcid) {
      this.beginMarkedContentSequence(structElemType, mcid, (String)null);
   }

   protected void beginMarkedContentSequence(String structElemType, int mcid, String actualText) {
      assert !this.inMarkedContentSequence;

      assert !this.inArtifactMode;

      if (structElemType != null) {
         String actualTextProperty = actualText == null ? "" : " /ActualText " + PDFText.escapeText(actualText);
         this.getStream().add(structElemType + " <</MCID " + mcid + actualTextProperty + ">>\nBDC\n");
      } else {
         if (this.context != null && this.context.getRegionType() != null) {
            this.getStream().add("/Artifact\n<</Type /Pagination\n/Subtype /" + this.context.getRegionType() + ">>\nBDC\n");
         } else {
            this.getStream().add("/Artifact\nBMC\n");
         }

         this.inArtifactMode = true;
      }

      this.inMarkedContentSequence = true;
   }

   void endMarkedContentSequence() {
      this.getStream().add("EMC\n");
      this.inMarkedContentSequence = false;
      this.inArtifactMode = false;
   }

   protected void restoreGraphicsState(boolean popState) {
      this.endTextObject();
      this.getStream().add("Q\n");
      this.maybeEndLayer();
      if (popState) {
         this.getState().restore();
      }

   }

   protected void restoreGraphicsState() {
      this.restoreGraphicsState(true);
   }

   protected void restoreGraphicsStateAccess() {
      this.endTextObject();
      this.getStream().add("Q\n");
      if (this.inMarkedContentSequence) {
         this.endMarkedContentSequence();
      }

      this.getState().restore();
   }

   private void maybeBeginLayer(String layer) {
      if (layer != null && layer.length() > 0) {
         this.getState().setLayer(layer);
         this.beginOptionalContent(layer);
      }

   }

   private void maybeEndLayer() {
      if (this.getState().getLayerChanged()) {
         this.endOptionalContent();
      }

   }

   private void beginOptionalContent(String layerId) {
      PDFReference layer = this.document.resolveExtensionReference(layerId);
      String name;
      if (layer != null) {
         name = "oc" + ++this.ocNameIndex;
         this.document.getResources().addProperty(name, layer);
      } else {
         name = "unknown";
      }

      this.getStream().add("/OC /" + name + " BDC\n");
   }

   private void endOptionalContent() {
      this.getStream().add("EMC\n");
   }

   protected void beginTextObject() {
      if (!this.textutil.isInTextObject()) {
         this.textutil.beginTextObject();
      }

   }

   protected void beginTextObject(String structElemType, int mcid) {
      this.beginTextObject(structElemType, mcid, (String)null);
   }

   protected void beginTextObject(String structElemType, int mcid, String actualText) {
      if (!this.textutil.isInTextObject()) {
         this.beginMarkedContentSequence(structElemType, mcid, actualText);
         this.textutil.beginTextObject();
      }

   }

   protected void endTextObject() {
      if (this.textutil.isInTextObject()) {
         this.textutil.endTextObject();
         if (this.inMarkedContentSequence) {
            this.endMarkedContentSequence();
         }
      }

   }

   public void concatenate(AffineTransform transform) {
      this.transform = transform;
      if (!transform.isIdentity()) {
         this.getState().concatenate(transform);
         this.getStream().add(CTMHelper.toPDFString(transform, false) + " cm\n");
      }

   }

   public void clipRect(Rectangle rect) {
      StringBuffer sb = new StringBuffer();
      sb.append(format((float)rect.x / 1000.0F)).append(' ');
      sb.append(format((float)rect.y / 1000.0F)).append(' ');
      sb.append(format((float)rect.width / 1000.0F)).append(' ');
      sb.append(format((float)rect.height / 1000.0F)).append(" re W n\n");
      this.add(sb.toString());
   }

   public void add(String content) {
      this.getStream().add(content);
   }

   public static final String format(float value) {
      return PDFNumber.doubleOut((double)value);
   }

   public void updateLineWidth(float width) {
      if (this.getState().setLineWidth(width)) {
         this.getStream().add(format(width) + " w\n");
      }

   }

   public void updateCharacterSpacing(float value) {
      if (this.getState().setCharacterSpacing(value)) {
         this.getStream().add(format(value) + " Tc\n");
      }

   }

   public void setColor(Color col, boolean fill, PDFStream stream) {
      assert stream != null;

      StringBuffer sb = new StringBuffer();
      this.setColor(col, fill, sb);
      stream.add(sb.toString());
   }

   public void setColor(Color col, boolean fill) {
      this.setColor(col, fill, this.getStream());
   }

   protected void setColor(Color col, boolean fill, StringBuffer pdf) {
      if (pdf != null) {
         this.colorHandler.establishColor(pdf, col, fill);
      } else {
         this.setColor(col, fill, this.getStream());
      }

   }

   public void updateColor(Color col, boolean fill, StringBuffer pdf) {
      if (col != null) {
         boolean update = false;
         if (fill) {
            update = this.getState().setBackColor(col);
         } else {
            update = this.getState().setColor(col);
         }

         if (update) {
            this.setColor(col, fill, pdf);
         }

      }
   }

   public void placeImage(float x, float y, float w, float h, PDFXObject xobj) {
      this.saveGraphicsState();
      this.add(format(w) + " 0 0 " + format(-h) + " " + format(x) + " " + format(y + h) + " cm\n" + xobj.getName() + " Do\n");
      this.restoreGraphicsState();
   }

   public void placeImage(AffineTransform at, String stream) {
      this.saveGraphicsState();
      this.concatenate(at);
      this.add(stream);
      this.restoreGraphicsState();
   }

   public void placeImage(float x, float y, float w, float h, PDFXObject xobj, String structElemType, int mcid) {
      this.saveGraphicsState(structElemType, mcid);
      this.add(format(w) + " 0 0 " + format(-h) + " " + format(x) + " " + format(y + h) + " cm\n" + xobj.getName() + " Do\n");
      this.restoreGraphicsStateAccess();
   }
}
