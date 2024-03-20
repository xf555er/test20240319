package org.apache.batik.transcoder.print;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.ext.awt.RenderingHintsKeyExt;
import org.apache.batik.transcoder.SVGAbstractTranscoder;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.keys.BooleanKey;
import org.apache.batik.transcoder.keys.LengthKey;
import org.apache.batik.transcoder.keys.StringKey;
import org.w3c.dom.Document;

public class PrintTranscoder extends SVGAbstractTranscoder implements Printable {
   public static final String KEY_AOI_STR = "aoi";
   public static final String KEY_HEIGHT_STR = "height";
   public static final String KEY_LANGUAGE_STR = "language";
   public static final String KEY_MARGIN_BOTTOM_STR = "marginBottom";
   public static final String KEY_MARGIN_LEFT_STR = "marginLeft";
   public static final String KEY_MARGIN_RIGHT_STR = "marginRight";
   public static final String KEY_MARGIN_TOP_STR = "marginTop";
   public static final String KEY_PAGE_HEIGHT_STR = "pageHeight";
   public static final String KEY_PAGE_ORIENTATION_STR = "pageOrientation";
   public static final String KEY_PAGE_WIDTH_STR = "pageWidth";
   public static final String KEY_PIXEL_TO_MM_STR = "pixelToMm";
   public static final String KEY_SCALE_TO_PAGE_STR = "scaleToPage";
   public static final String KEY_SHOW_PAGE_DIALOG_STR = "showPageDialog";
   public static final String KEY_SHOW_PRINTER_DIALOG_STR = "showPrinterDialog";
   public static final String KEY_USER_STYLESHEET_URI_STR = "userStylesheet";
   public static final String KEY_WIDTH_STR = "width";
   public static final String KEY_XML_PARSER_CLASSNAME_STR = "xmlParserClassName";
   public static final String VALUE_MEDIA_PRINT = "print";
   public static final String VALUE_PAGE_ORIENTATION_LANDSCAPE = "landscape";
   public static final String VALUE_PAGE_ORIENTATION_PORTRAIT = "portrait";
   public static final String VALUE_PAGE_ORIENTATION_REVERSE_LANDSCAPE = "reverseLandscape";
   private List inputs = new ArrayList();
   private List printedInputs = null;
   private int curIndex = -1;
   private BridgeContext theCtx;
   public static final TranscodingHints.Key KEY_SHOW_PAGE_DIALOG = new BooleanKey();
   public static final TranscodingHints.Key KEY_SHOW_PRINTER_DIALOG = new BooleanKey();
   public static final TranscodingHints.Key KEY_PAGE_WIDTH = new LengthKey();
   public static final TranscodingHints.Key KEY_PAGE_HEIGHT = new LengthKey();
   public static final TranscodingHints.Key KEY_MARGIN_TOP = new LengthKey();
   public static final TranscodingHints.Key KEY_MARGIN_RIGHT = new LengthKey();
   public static final TranscodingHints.Key KEY_MARGIN_BOTTOM = new LengthKey();
   public static final TranscodingHints.Key KEY_MARGIN_LEFT = new LengthKey();
   public static final TranscodingHints.Key KEY_PAGE_ORIENTATION = new StringKey();
   public static final TranscodingHints.Key KEY_SCALE_TO_PAGE = new BooleanKey();
   public static final String USAGE = "java org.apache.batik.transcoder.print.PrintTranscoder <svgFileToPrint>";

   public PrintTranscoder() {
      this.hints.put(KEY_MEDIA, "print");
   }

   public void transcode(TranscoderInput in, TranscoderOutput out) {
      if (in != null) {
         this.inputs.add(in);
      }

   }

   protected void transcode(Document document, String uri, TranscoderOutput output) throws TranscoderException {
      super.transcode(document, uri, output);
      this.theCtx = this.ctx;
      this.ctx = null;
   }

   public void print() throws PrinterException {
      PrinterJob printerJob = PrinterJob.getPrinterJob();
      PageFormat pageFormat = printerJob.defaultPage();
      Paper paper = pageFormat.getPaper();
      Float pageWidth = (Float)this.hints.get(KEY_PAGE_WIDTH);
      Float pageHeight = (Float)this.hints.get(KEY_PAGE_HEIGHT);
      if (pageWidth != null) {
         paper.setSize((double)pageWidth, paper.getHeight());
      }

      if (pageHeight != null) {
         paper.setSize(paper.getWidth(), (double)pageHeight);
      }

      float x = 0.0F;
      float y = 0.0F;
      float width = (float)paper.getWidth();
      float height = (float)paper.getHeight();
      Float leftMargin = (Float)this.hints.get(KEY_MARGIN_LEFT);
      Float topMargin = (Float)this.hints.get(KEY_MARGIN_TOP);
      Float rightMargin = (Float)this.hints.get(KEY_MARGIN_RIGHT);
      Float bottomMargin = (Float)this.hints.get(KEY_MARGIN_BOTTOM);
      if (leftMargin != null) {
         x = leftMargin;
         width -= leftMargin;
      }

      if (topMargin != null) {
         y = topMargin;
         height -= topMargin;
      }

      if (rightMargin != null) {
         width -= rightMargin;
      }

      if (bottomMargin != null) {
         height -= bottomMargin;
      }

      paper.setImageableArea((double)x, (double)y, (double)width, (double)height);
      String pageOrientation = (String)this.hints.get(KEY_PAGE_ORIENTATION);
      if ("portrait".equalsIgnoreCase(pageOrientation)) {
         pageFormat.setOrientation(1);
      } else if ("landscape".equalsIgnoreCase(pageOrientation)) {
         pageFormat.setOrientation(0);
      } else if ("reverseLandscape".equalsIgnoreCase(pageOrientation)) {
         pageFormat.setOrientation(2);
      }

      pageFormat.setPaper(paper);
      pageFormat = printerJob.validatePage(pageFormat);
      Boolean showPageFormat = (Boolean)this.hints.get(KEY_SHOW_PAGE_DIALOG);
      if (showPageFormat != null && showPageFormat) {
         PageFormat tmpPageFormat = printerJob.pageDialog(pageFormat);
         if (tmpPageFormat == pageFormat) {
            return;
         }

         pageFormat = tmpPageFormat;
      }

      printerJob.setPrintable(this, pageFormat);
      Boolean showPrinterDialog = (Boolean)this.hints.get(KEY_SHOW_PRINTER_DIALOG);
      if (showPrinterDialog == null || !showPrinterDialog || printerJob.printDialog()) {
         printerJob.print();
      }
   }

   public int print(Graphics _g, PageFormat pageFormat, int pageIndex) {
      if (this.printedInputs == null) {
         this.printedInputs = new ArrayList(this.inputs);
      }

      if (pageIndex >= this.printedInputs.size()) {
         this.curIndex = -1;
         if (this.theCtx != null) {
            this.theCtx.dispose();
         }

         this.userAgent.displayMessage("Done");
         return 1;
      } else {
         if (this.curIndex != pageIndex) {
            if (this.theCtx != null) {
               this.theCtx.dispose();
            }

            try {
               this.width = (float)((int)pageFormat.getImageableWidth());
               this.height = (float)((int)pageFormat.getImageableHeight());
               super.transcode((TranscoderInput)this.printedInputs.get(pageIndex), (TranscoderOutput)null);
               this.curIndex = pageIndex;
            } catch (TranscoderException var9) {
               this.drawError(_g, var9);
               return 0;
            }
         }

         Graphics2D g = (Graphics2D)_g;
         g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
         g.setRenderingHint(RenderingHintsKeyExt.KEY_TRANSCODING, "Printing");
         AffineTransform t = g.getTransform();
         Shape clip = g.getClip();
         g.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
         g.transform(this.curTxf);

         try {
            this.root.paint(g);
         } catch (Exception var8) {
            g.setTransform(t);
            g.setClip(clip);
            this.drawError(_g, var8);
         }

         g.setTransform(t);
         g.setClip(clip);
         return 0;
      }
   }

   protected void setImageSize(float docWidth, float docHeight) {
      Boolean scaleToPage = (Boolean)this.hints.get(KEY_SCALE_TO_PAGE);
      if (scaleToPage != null && !scaleToPage) {
         float w = docWidth;
         float h = docHeight;
         if (this.hints.containsKey(KEY_AOI)) {
            Rectangle2D aoi = (Rectangle2D)this.hints.get(KEY_AOI);
            w = (float)aoi.getWidth();
            h = (float)aoi.getHeight();
         }

         super.setImageSize(w, h);
      }

   }

   private void drawError(Graphics g, Exception e) {
      this.userAgent.displayError(e);
   }

   public static void main(String[] args) throws Exception {
      if (args.length < 1) {
         System.err.println("java org.apache.batik.transcoder.print.PrintTranscoder <svgFileToPrint>");
         System.exit(0);
      }

      PrintTranscoder transcoder = new PrintTranscoder();
      setTranscoderFloatHint(transcoder, "language", KEY_LANGUAGE);
      setTranscoderFloatHint(transcoder, "userStylesheet", KEY_USER_STYLESHEET_URI);
      setTranscoderStringHint(transcoder, "xmlParserClassName", KEY_XML_PARSER_CLASSNAME);
      setTranscoderBooleanHint(transcoder, "scaleToPage", KEY_SCALE_TO_PAGE);
      setTranscoderRectangleHint(transcoder, "aoi", KEY_AOI);
      setTranscoderFloatHint(transcoder, "width", KEY_WIDTH);
      setTranscoderFloatHint(transcoder, "height", KEY_HEIGHT);
      setTranscoderFloatHint(transcoder, "pixelToMm", KEY_PIXEL_UNIT_TO_MILLIMETER);
      setTranscoderStringHint(transcoder, "pageOrientation", KEY_PAGE_ORIENTATION);
      setTranscoderFloatHint(transcoder, "pageWidth", KEY_PAGE_WIDTH);
      setTranscoderFloatHint(transcoder, "pageHeight", KEY_PAGE_HEIGHT);
      setTranscoderFloatHint(transcoder, "marginTop", KEY_MARGIN_TOP);
      setTranscoderFloatHint(transcoder, "marginRight", KEY_MARGIN_RIGHT);
      setTranscoderFloatHint(transcoder, "marginBottom", KEY_MARGIN_BOTTOM);
      setTranscoderFloatHint(transcoder, "marginLeft", KEY_MARGIN_LEFT);
      setTranscoderBooleanHint(transcoder, "showPageDialog", KEY_SHOW_PAGE_DIALOG);
      setTranscoderBooleanHint(transcoder, "showPrinterDialog", KEY_SHOW_PRINTER_DIALOG);
      String[] var2 = args;
      int var3 = args.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         String arg = var2[var4];
         transcoder.transcode(new TranscoderInput((new File(arg)).toURI().toURL().toString()), (TranscoderOutput)null);
      }

      transcoder.print();
      System.exit(0);
   }

   public static void setTranscoderFloatHint(Transcoder transcoder, String property, TranscodingHints.Key key) {
      String str = System.getProperty(property);
      if (str != null) {
         try {
            Float value = Float.parseFloat(str);
            transcoder.addTranscodingHint(key, value);
         } catch (NumberFormatException var5) {
            handleValueError(property, str);
         }
      }

   }

   public static void setTranscoderRectangleHint(Transcoder transcoder, String property, TranscodingHints.Key key) {
      String str = System.getProperty(property);
      if (str != null) {
         StringTokenizer st = new StringTokenizer(str, " ,");
         if (st.countTokens() != 4) {
            handleValueError(property, str);
         }

         try {
            String x = st.nextToken();
            String y = st.nextToken();
            String width = st.nextToken();
            String height = st.nextToken();
            Rectangle2D r = new Rectangle2D.Float(Float.parseFloat(x), Float.parseFloat(y), Float.parseFloat(width), Float.parseFloat(height));
            transcoder.addTranscodingHint(key, r);
         } catch (NumberFormatException var10) {
            handleValueError(property, str);
         }
      }

   }

   public static void setTranscoderBooleanHint(Transcoder transcoder, String property, TranscodingHints.Key key) {
      String str = System.getProperty(property);
      if (str != null) {
         Boolean value = "true".equalsIgnoreCase(str) ? Boolean.TRUE : Boolean.FALSE;
         transcoder.addTranscodingHint(key, value);
      }

   }

   public static void setTranscoderStringHint(Transcoder transcoder, String property, TranscodingHints.Key key) {
      String str = System.getProperty(property);
      if (str != null) {
         transcoder.addTranscodingHint(key, str);
      }

   }

   public static void handleValueError(String property, String value) {
      System.err.println("Invalid " + property + " value : " + value);
      System.exit(1);
   }
}
