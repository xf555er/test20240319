package org.apache.fop.render.pdf;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.apache.fop.ResourceEventProducer;
import org.apache.fop.fonts.CustomFont;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.fonts.LazyFont;
import org.apache.fop.fonts.MultiByteFont;
import org.apache.fop.fonts.SingleByteFont;
import org.apache.fop.fonts.Typeface;
import org.apache.fop.pdf.PDFArray;
import org.apache.fop.pdf.PDFDictionary;
import org.apache.fop.pdf.PDFName;
import org.apache.fop.pdf.PDFNumber;
import org.apache.fop.pdf.PDFStructElem;
import org.apache.fop.pdf.PDFTextUtil;
import org.apache.fop.pdf.PDFXObject;
import org.apache.fop.render.RenderingContext;
import org.apache.fop.render.intermediate.AbstractIFPainter;
import org.apache.fop.render.intermediate.BorderPainter;
import org.apache.fop.render.intermediate.GraphicsPainter;
import org.apache.fop.render.intermediate.IFContext;
import org.apache.fop.render.intermediate.IFException;
import org.apache.fop.render.intermediate.IFState;
import org.apache.fop.render.intermediate.IFUtil;
import org.apache.fop.traits.BorderProps;
import org.apache.fop.traits.Direction;
import org.apache.fop.traits.RuleStyle;
import org.apache.fop.util.CharUtilities;
import org.apache.xmlgraphics.image.loader.ImageException;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageManager;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;
import org.w3c.dom.Document;
import org.xml.sax.Locator;

public class PDFPainter extends AbstractIFPainter {
   protected PDFContentGenerator generator;
   private final GraphicsPainter graphicsPainter;
   private final BorderPainter borderPainter;
   private boolean accessEnabled;
   private PDFLogicalStructureHandler.MarkedContentInfo imageMCI;
   private PDFLogicalStructureHandler logicalStructureHandler;
   private final LanguageAvailabilityChecker languageAvailabilityChecker;
   private static int[] paZero = new int[4];

   public PDFPainter(PDFDocumentHandler documentHandler, PDFLogicalStructureHandler logicalStructureHandler) {
      super(documentHandler);
      this.logicalStructureHandler = logicalStructureHandler;
      this.generator = documentHandler.getGenerator();
      this.graphicsPainter = new PDFGraphicsPainter(this.generator);
      this.borderPainter = new BorderPainter(this.graphicsPainter);
      this.state = IFState.create();
      this.accessEnabled = this.getUserAgent().isAccessibilityEnabled();
      this.languageAvailabilityChecker = this.accessEnabled ? new LanguageAvailabilityChecker(documentHandler.getContext()) : null;
   }

   public void startViewport(AffineTransform transform, Dimension size, Rectangle clipRect) throws IFException {
      this.generator.saveGraphicsState();
      this.generator.concatenate(toPoints(transform));
      if (clipRect != null) {
         this.clipRect(clipRect);
      }

   }

   public void endViewport() throws IFException {
      this.generator.restoreGraphicsState();
   }

   public void startGroup(AffineTransform transform, String layer) throws IFException {
      this.generator.saveGraphicsState(layer);
      this.generator.concatenate(toPoints(transform));
   }

   public void endGroup() throws IFException {
      this.generator.restoreGraphicsState();
   }

   public void drawImage(String uri, Rectangle rect) throws IFException {
      PDFXObject xobject = ((PDFDocumentHandler)this.getDocumentHandler()).getPDFDocument().getXObject(uri);
      this.addStructTreeBBox(rect);
      if (xobject != null) {
         if (this.accessEnabled) {
            PDFStructElem structElem = (PDFStructElem)this.getContext().getStructureTreeElement();
            this.prepareImageMCID(structElem);
            this.placeImageAccess(rect, xobject);
         } else {
            this.placeImage(rect, xobject);
         }
      } else {
         this.drawImageUsingURI(uri, rect);
         if (!((PDFDocumentHandler)this.getDocumentHandler()).getPDFDocument().isLinearizationEnabled()) {
            this.flushPDFDoc();
         }
      }

   }

   private void addStructTreeBBox(Rectangle rect) {
      if (this.accessEnabled && ((PDFDocumentHandler)this.getDocumentHandler()).getPDFDocument().getProfile().getPDFUAMode().isEnabled()) {
         PDFStructElem structElem = (PDFStructElem)this.getContext().getStructureTreeElement();
         if (structElem != null) {
            PDFDictionary d = new PDFDictionary();
            int x = rect.x / 1000;
            int y = rect.y / 1000;
            int w = rect.width / 1000;
            int h = rect.height / 1000;
            d.put("BBox", new PDFArray(new Object[]{x, y, w, h}));
            d.put("O", new PDFName("Layout"));
            structElem.put("A", d);
         }
      }

   }

   protected void drawImageUsingURI(String uri, Rectangle rect) {
      ImageManager manager = this.getUserAgent().getImageManager();
      ImageInfo info = null;

      ResourceEventProducer eventProducer;
      try {
         ImageSessionContext sessionContext = this.getUserAgent().getImageSessionContext();
         info = manager.getImageInfo(uri, sessionContext);
         if (this.accessEnabled) {
            PDFStructElem structElem = (PDFStructElem)this.getContext().getStructureTreeElement();
            String mimeType = info.getMimeType();
            if (!mimeType.equalsIgnoreCase("application/pdf")) {
               this.prepareImageMCID(structElem);
            }
         }

         this.drawImageUsingImageHandler(info, rect);
      } catch (ImageException var8) {
         eventProducer = ResourceEventProducer.Provider.get(this.getUserAgent().getEventBroadcaster());
         eventProducer.imageError(this, info != null ? info.toString() : uri, var8, (Locator)null);
      } catch (FileNotFoundException var9) {
         eventProducer = ResourceEventProducer.Provider.get(this.getUserAgent().getEventBroadcaster());
         eventProducer.imageNotFound(this, info != null ? info.toString() : uri, var9, (Locator)null);
      } catch (IOException var10) {
         eventProducer = ResourceEventProducer.Provider.get(this.getUserAgent().getEventBroadcaster());
         eventProducer.imageIOError(this, info != null ? info.toString() : uri, var10, (Locator)null);
      }

   }

   private void prepareImageMCID(PDFStructElem structElem) {
      this.imageMCI = this.logicalStructureHandler.addImageContentItem(structElem);
      if (structElem != null) {
         this.languageAvailabilityChecker.checkLanguageAvailability((String)structElem.get("Alt"));
      }

   }

   protected RenderingContext createRenderingContext() {
      PDFRenderingContext pdfContext = new PDFRenderingContext(this.getUserAgent(), this.generator, ((PDFDocumentHandler)this.getDocumentHandler()).getCurrentPage(), this.getFontInfo());
      pdfContext.setMarkedContentInfo(this.imageMCI);
      pdfContext.setPageNumbers(((PDFDocumentHandler)this.getDocumentHandler()).getPageNumbers());
      pdfContext.setPdfLogicalStructureHandler(this.logicalStructureHandler);
      pdfContext.setCurrentSessionStructElem((PDFStructElem)this.getContext().getStructureTreeElement());
      return pdfContext;
   }

   private void placeImage(Rectangle rect, PDFXObject xobj) {
      this.generator.saveGraphicsState();
      this.generator.add(format(rect.width) + " 0 0 " + format(-rect.height) + " " + format(rect.x) + " " + format(rect.y + rect.height) + " cm " + xobj.getName() + " Do\n");
      this.generator.restoreGraphicsState();
   }

   private void placeImageAccess(Rectangle rect, PDFXObject xobj) {
      this.generator.saveGraphicsState(this.imageMCI.tag, this.imageMCI.mcid);
      this.generator.add(format(rect.width) + " 0 0 " + format(-rect.height) + " " + format(rect.x) + " " + format(rect.y + rect.height) + " cm " + xobj.getName() + " Do\n");
      this.generator.restoreGraphicsStateAccess();
   }

   public void drawImage(Document doc, Rectangle rect) throws IFException {
      if (this.accessEnabled) {
         PDFStructElem structElem = (PDFStructElem)this.getContext().getStructureTreeElement();
         this.prepareImageMCID(structElem);
         this.addStructTreeBBox(rect);
      }

      this.drawImageUsingDocument(doc, rect);
      if (!((PDFDocumentHandler)this.getDocumentHandler()).getPDFDocument().isLinearizationEnabled()) {
         this.flushPDFDoc();
      }

   }

   private void flushPDFDoc() throws IFException {
      try {
         this.generator.flushPDFDoc();
      } catch (IOException var2) {
         throw new IFException("I/O error flushing the PDF document", var2);
      }
   }

   protected static String format(int value) {
      return PDFNumber.doubleOut((double)((float)value / 1000.0F));
   }

   public void clipRect(Rectangle rect) throws IFException {
      this.generator.endTextObject();
      this.generator.clipRect(rect);
   }

   public void clipBackground(Rectangle rect, BorderProps bpsBefore, BorderProps bpsAfter, BorderProps bpsStart, BorderProps bpsEnd) throws IFException {
      try {
         this.borderPainter.clipBackground(rect, bpsBefore, bpsAfter, bpsStart, bpsEnd);
      } catch (IOException var7) {
         throw new IFException("I/O error while clipping background", var7);
      }
   }

   public void fillRect(Rectangle rect, Paint fill) throws IFException {
      if (fill != null) {
         if (rect.width != 0 && rect.height != 0) {
            this.generator.endTextObject();
            if (this.accessEnabled && this.getUserAgent().isPdfUAEnabled()) {
               this.generator.beginMarkedContentSequence((String)null, 0, (String)null);
            }

            if (fill != null) {
               if (!(fill instanceof Color)) {
                  throw new UnsupportedOperationException("Non-Color paints NYI");
               }

               this.generator.updateColor((Color)fill, true, (StringBuffer)null);
            }

            StringBuffer sb = new StringBuffer();
            sb.append(format(rect.x)).append(' ');
            sb.append(format(rect.y)).append(' ');
            sb.append(format(rect.width)).append(' ');
            sb.append(format(rect.height)).append(" re");
            if (fill != null) {
               sb.append(" f");
            }

            sb.append('\n');
            this.generator.add(sb.toString());
            if (this.accessEnabled && this.getUserAgent().isPdfUAEnabled()) {
               this.generator.endMarkedContentSequence();
            }
         }

      }
   }

   public void drawBorderRect(Rectangle rect, BorderProps top, BorderProps bottom, BorderProps left, BorderProps right, Color innerBackgroundColor) throws IFException {
      if (top != null || bottom != null || left != null || right != null) {
         this.generator.endTextObject();
         if (this.accessEnabled && this.getUserAgent().isPdfUAEnabled()) {
            this.generator.beginMarkedContentSequence((String)null, 0, (String)null);
         }

         this.borderPainter.drawBorders(rect, top, bottom, left, right, innerBackgroundColor);
         if (this.accessEnabled && this.getUserAgent().isPdfUAEnabled()) {
            this.generator.endMarkedContentSequence();
         }
      }

   }

   public void drawLine(Point start, Point end, int width, Color color, RuleStyle style) throws IFException {
      this.generator.endTextObject();
      if (this.accessEnabled && this.getUserAgent().isPdfUAEnabled()) {
         this.generator.beginMarkedContentSequence((String)null, 0, (String)null);
      }

      try {
         this.graphicsPainter.drawLine(start, end, width, color, style);
      } catch (IOException var7) {
         throw new IFException("Cannot draw line", var7);
      }

      if (this.accessEnabled && this.getUserAgent().isPdfUAEnabled()) {
         this.generator.endMarkedContentSequence();
      }

   }

   private Typeface getTypeface(String fontName) {
      if (fontName == null) {
         throw new NullPointerException("fontName must not be null");
      } else {
         Typeface tf = (Typeface)this.getFontInfo().getFonts().get(fontName);
         if (tf instanceof LazyFont) {
            tf = ((LazyFont)tf).getRealFont();
         }

         return tf;
      }
   }

   public void drawText(int x, int y, int letterSpacing, int wordSpacing, int[][] dp, String text) throws IFException {
      if (this.accessEnabled) {
         PDFStructElem structElem = (PDFStructElem)this.getContext().getStructureTreeElement();
         this.languageAvailabilityChecker.checkLanguageAvailability(text);
         PDFLogicalStructureHandler.MarkedContentInfo mci = this.logicalStructureHandler.addTextContentItem(structElem);
         String actualText = this.getContext().isHyphenated() ? text.substring(0, text.length() - 1) : null;
         this.generator.endTextObject();
         this.generator.updateColor(this.state.getTextColor(), true, (StringBuffer)null);
         this.generator.beginTextObject(mci.tag, mci.mcid, actualText);
      } else {
         this.generator.updateColor(this.state.getTextColor(), true, (StringBuffer)null);
         this.generator.beginTextObject();
      }

      FontTriplet triplet = new FontTriplet(this.state.getFontFamily(), this.state.getFontStyle(), this.state.getFontWeight());
      String fontKey = this.getFontInfo().getInternalFontKey(triplet);
      Typeface typeface = this.getTypeface(fontKey);
      if (typeface instanceof MultiByteFont && ((MultiByteFont)typeface).hasSVG()) {
         this.drawSVGText((MultiByteFont)typeface, triplet, x, y, text, this.state);
      } else if (dp != null && !IFUtil.isDPOnlyDX(dp)) {
         this.drawTextWithDP(x, y, text, triplet, letterSpacing, wordSpacing, dp);
      } else {
         this.drawTextWithDX(x, y, text, triplet, letterSpacing, wordSpacing, IFUtil.convertDPToDX(dp));
      }

   }

   public void drawText(int x, int y, int letterSpacing, int wordSpacing, int[][] dp, String text, boolean nextIsSpace) throws IFException {
      if (this.accessEnabled && nextIsSpace) {
         text = text + ' ';
      }

      this.drawText(x, y, letterSpacing, wordSpacing, dp, text);
   }

   private void drawTextWithDX(int x, int y, String text, FontTriplet triplet, int letterSpacing, int wordSpacing, int[] dx) throws IFException {
      String fontKey = this.getFontKey(triplet);
      int sizeMillipoints = this.state.getFontSize();
      float fontSize = (float)sizeMillipoints / 1000.0F;
      Typeface tf = this.getTypeface(fontKey);
      Font font = this.getFontInfo().getFontInstance(triplet, sizeMillipoints);
      String fontName = font.getFontName();
      PDFTextUtil textutil = this.generator.getTextUtil();
      textutil.updateTf(fontKey, (double)fontSize, tf.isMultiByte(), tf.isCID());
      double shear = 0.0;
      boolean simulateStyle = tf instanceof CustomFont && ((CustomFont)tf).getSimulateStyle();
      if (simulateStyle) {
         if (triplet.getWeight() == 700) {
            this.generator.add("q\n");
            this.generator.add("2 Tr 0.31543 w\n");
         }

         if (triplet.getStyle().equals("italic")) {
            shear = 0.3333;
         }
      }

      this.generator.updateCharacterSpacing((float)letterSpacing / 1000.0F);
      textutil.writeTextMatrix(new AffineTransform(1.0, 0.0, shear, -1.0, (double)((float)x / 1000.0F), (double)((float)y / 1000.0F)));
      int l = text.length();
      int dxl = dx != null ? dx.length : 0;
      if (dx != null && dxl > 0 && dx[0] != 0) {
         textutil.adjustGlyphTJ((double)((float)(-dx[0]) / fontSize));
      }

      for(int i = 0; i < l; ++i) {
         int orgChar = text.charAt(i);
         if (CharUtilities.containsSurrogatePairAt(text, i)) {
            char var10000 = (char)orgChar;
            ++i;
            orgChar = Character.toCodePoint(var10000, text.charAt(i));
         }

         float glyphAdjust = 0.0F;
         int ch;
         if (font.hasCodePoint(orgChar)) {
            ch = font.mapCodePoint(orgChar);
            ch = this.selectAndMapSingleByteFont(tf, fontName, fontSize, textutil, ch);
            if (wordSpacing != 0 && CharUtilities.isAdjustableSpace(orgChar)) {
               glyphAdjust += (float)wordSpacing;
            }
         } else {
            if (CharUtilities.isFixedWidthSpace(orgChar)) {
               ch = font.mapChar(' ');
               int spaceDiff = font.getCharWidth(' ') - font.getCharWidth(orgChar);
               glyphAdjust = (float)(-spaceDiff);
            } else {
               ch = font.mapCodePoint(orgChar);
               if (wordSpacing != 0 && CharUtilities.isAdjustableSpace(orgChar)) {
                  glyphAdjust += (float)wordSpacing;
               }
            }

            ch = this.selectAndMapSingleByteFont(tf, fontName, fontSize, textutil, ch);
         }

         textutil.writeTJMappedCodePoint(ch);
         if (dx != null && i < dxl - 1) {
            glyphAdjust += (float)dx[i + 1];
         }

         if (glyphAdjust != 0.0F) {
            textutil.adjustGlyphTJ((double)(-glyphAdjust / fontSize));
         }
      }

      textutil.writeTJ();
      if (simulateStyle && triplet.getWeight() == 700) {
         this.generator.add("Q\n");
      }

   }

   private void drawTextWithDP(int x, int y, String text, FontTriplet triplet, int letterSpacing, int wordSpacing, int[][] dp) {
      assert text != null;

      assert triplet != null;

      assert dp != null;

      String fk = this.getFontInfo().getInternalFontKey(triplet);
      Typeface tf = this.getTypeface(fk);
      if (tf.isMultiByte() || tf.isCID()) {
         int fs = this.state.getFontSize();
         float fsPoints = (float)fs / 1000.0F;
         Font f = this.getFontInfo().getFontInstance(triplet, fs);
         PDFTextUtil tu = this.generator.getTextUtil();
         double xc = 0.0;
         double yc = 0.0;
         double xoLast = 0.0;
         double yoLast = 0.0;
         double wox = (double)wordSpacing;
         boolean simulateStyle = tf instanceof CustomFont && ((CustomFont)tf).getSimulateStyle();
         double shear = 0.0;
         if (simulateStyle) {
            if (triplet.getWeight() == 700) {
               this.generator.add("q\n");
               this.generator.add("2 Tr 0.31543 w\n");
            }

            if (triplet.getStyle().equals("italic")) {
               shear = 0.3333;
            }
         }

         tu.writeTextMatrix(new AffineTransform(1.0, 0.0, shear, -1.0, (double)((float)x / 1000.0F), (double)((float)y / 1000.0F)));
         tu.updateTf(fk, (double)fsPoints, tf.isMultiByte(), true);
         this.generator.updateCharacterSpacing((float)letterSpacing / 1000.0F);
         int i = 0;

         for(int n = text.length(); i < n; ++i) {
            char ch = text.charAt(i);
            int[] pa = i < dp.length && dp[i] != null ? dp[i] : paZero;
            double xo = xc + (double)pa[0];
            double yo = yc + (double)pa[1];
            double xa = (double)f.getCharWidth(ch) + this.maybeWordOffsetX(wox, ch, (Direction)null);
            double ya = 0.0;
            double xd = (xo - xoLast) / 1000.0;
            double yd = (yo - yoLast) / 1000.0;
            tu.writeTd(xd, yd);
            tu.writeTj(f.mapChar(ch), tf.isMultiByte(), true);
            xc += xa + (double)pa[2];
            yc += ya + (double)pa[3];
            xoLast = xo;
            yoLast = yo;
         }
      }

   }

   private double maybeWordOffsetX(double wox, char ch, Direction dir) {
      return wox == 0.0 || !CharUtilities.isAdjustableSpace(ch) || dir != null && !dir.isHorizontal() ? 0.0 : wox;
   }

   private int selectAndMapSingleByteFont(Typeface tf, String fontName, float fontSize, PDFTextUtil textutil, int ch) {
      if (tf instanceof SingleByteFont && ((SingleByteFont)tf).hasAdditionalEncodings() || tf.isCID()) {
         int encoding = ch / 256;
         if (encoding == 0) {
            textutil.updateTf(fontName, (double)fontSize, tf.isMultiByte(), tf.isCID());
         } else {
            textutil.updateTf(fontName + "_" + Integer.toString(encoding), (double)fontSize, tf.isMultiByte(), tf.isCID());
            ch = (char)(ch % 256);
         }
      }

      return ch;
   }

   private static class LanguageAvailabilityChecker {
      private final IFContext context;
      private final Set reportedLocations = new HashSet();

      LanguageAvailabilityChecker(IFContext context) {
         this.context = context;
      }

      private void checkLanguageAvailability(String text) {
         Locale locale = this.context.getLanguage();
         if (locale == null && this.containsLettersOrDigits(text)) {
            String location = this.context.getLocation();
            if (!this.reportedLocations.contains(location)) {
               PDFEventProducer.Provider.get(this.context.getUserAgent().getEventBroadcaster()).unknownLanguage(this, location);
               this.reportedLocations.add(location);
            }
         }

      }

      private boolean containsLettersOrDigits(String text) {
         for(int i = 0; i < text.length(); ++i) {
            if (Character.isLetterOrDigit(text.charAt(i))) {
               return true;
            }
         }

         return false;
      }
   }
}
