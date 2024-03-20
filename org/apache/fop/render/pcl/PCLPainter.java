package org.apache.fop.render.pcl;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import org.apache.fop.fonts.CIDFontType;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.fonts.FontType;
import org.apache.fop.fonts.LazyFont;
import org.apache.fop.fonts.MultiByteFont;
import org.apache.fop.fonts.Typeface;
import org.apache.fop.render.ImageHandlerUtil;
import org.apache.fop.render.RenderingContext;
import org.apache.fop.render.intermediate.AbstractIFPainter;
import org.apache.fop.render.intermediate.IFException;
import org.apache.fop.render.intermediate.IFState;
import org.apache.fop.render.intermediate.IFUtil;
import org.apache.fop.render.java2d.CustomFontMetricsMapper;
import org.apache.fop.render.java2d.FontMetricsMapper;
import org.apache.fop.render.java2d.Java2DPainter;
import org.apache.fop.render.pcl.fonts.PCLCharacterWriter;
import org.apache.fop.render.pcl.fonts.PCLSoftFont;
import org.apache.fop.render.pcl.fonts.PCLSoftFontManager;
import org.apache.fop.render.pcl.fonts.truetype.PCLTTFCharacterWriter;
import org.apache.fop.traits.BorderProps;
import org.apache.fop.traits.RuleStyle;
import org.apache.fop.util.CharUtilities;
import org.apache.xmlgraphics.image.loader.ImageException;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageProcessingHints;
import org.apache.xmlgraphics.image.loader.ImageSize;
import org.apache.xmlgraphics.image.loader.impl.ImageGraphics2D;
import org.apache.xmlgraphics.java2d.GraphicContext;
import org.apache.xmlgraphics.java2d.Graphics2DImagePainter;
import org.w3c.dom.Document;

public class PCLPainter extends AbstractIFPainter implements PCLConstants {
   private static final boolean DEBUG = false;
   private PCLGenerator gen;
   private PCLPageDefinition currentPageDefinition;
   private int currentPrintDirection;
   private Stack graphicContextStack = new Stack();
   private GraphicContext graphicContext = new GraphicContext();
   private PCLSoftFontManager sfManager;
   private static final double SAFETY_MARGIN_FACTOR = 0.05;

   public PCLPainter(PCLDocumentHandler parent, PCLPageDefinition pageDefinition) {
      super(parent);
      this.gen = parent.getPCLGenerator();
      this.state = IFState.create();
      this.currentPageDefinition = pageDefinition;
   }

   PCLRenderingUtil getPCLUtil() {
      return ((PCLDocumentHandler)this.getDocumentHandler()).getPCLUtil();
   }

   protected int getResolution() {
      int resolution = Math.round(this.getUserAgent().getTargetResolution());
      return resolution <= 300 ? 300 : 600;
   }

   private boolean isSpeedOptimized() {
      return this.getPCLUtil().getRenderingMode() == PCLRenderingMode.SPEED;
   }

   public void startViewport(AffineTransform transform, Dimension size, Rectangle clipRect) throws IFException {
      this.saveGraphicsState();

      try {
         this.concatenateTransformationMatrix(transform);
      } catch (IOException var5) {
         throw new IFException("I/O error in startViewport()", var5);
      }
   }

   public void endViewport() throws IFException {
      this.restoreGraphicsState();
   }

   public void startGroup(AffineTransform transform, String layer) throws IFException {
      this.saveGraphicsState();

      try {
         this.concatenateTransformationMatrix(transform);
      } catch (IOException var4) {
         throw new IFException("I/O error in startGroup()", var4);
      }
   }

   public void endGroup() throws IFException {
      this.restoreGraphicsState();
   }

   public void drawImage(String uri, Rectangle rect) throws IFException {
      this.drawImageUsingURI(uri, rect);
   }

   protected RenderingContext createRenderingContext() {
      PCLRenderingContext pdfContext = new PCLRenderingContext(this.getUserAgent(), this.gen, this.getPCLUtil()) {
         public Point2D transformedPoint(int x, int y) {
            return PCLPainter.this.transformedPoint(x, y);
         }

         public GraphicContext getGraphicContext() {
            return PCLPainter.this.graphicContext;
         }
      };
      return pdfContext;
   }

   public void drawImage(Document doc, Rectangle rect) throws IFException {
      this.drawImageUsingDocument(doc, rect);
   }

   public void clipRect(Rectangle rect) throws IFException {
   }

   public void clipBackground(Rectangle rect, BorderProps bpsBefore, BorderProps bpsAfter, BorderProps bpsStart, BorderProps bpsEnd) throws IFException {
   }

   public void fillRect(Rectangle rect, Paint fill) throws IFException {
      if (fill != null) {
         if (rect.width != 0 && rect.height != 0) {
            Color fillColor = null;
            if (fill != null) {
               if (!(fill instanceof Color)) {
                  throw new UnsupportedOperationException("Non-Color paints NYI");
               }

               fillColor = (Color)fill;

               try {
                  this.setCursorPos(rect.x, rect.y);
                  this.gen.fillRect(rect.width, rect.height, fillColor, this.getPCLUtil().isColorEnabled());
               } catch (IOException var5) {
                  throw new IFException("I/O error in fillRect()", var5);
               }
            }
         }

      }
   }

   public void drawBorderRect(final Rectangle rect, final BorderProps top, final BorderProps bottom, final BorderProps left, final BorderProps right) throws IFException {
      if (this.isSpeedOptimized()) {
         super.drawBorderRect(rect, top, bottom, left, right, (Color)null);
      } else {
         if (top != null || bottom != null || left != null || right != null) {
            final Dimension dim = rect.getSize();
            Graphics2DImagePainter painter = new Graphics2DImagePainter() {
               public void paint(Graphics2D g2d, Rectangle2D area) {
                  g2d.translate(-rect.x, -rect.y);
                  Java2DPainter painter = new Java2DPainter(g2d, PCLPainter.this.getContext(), PCLPainter.this.getFontInfo(), PCLPainter.this.state);

                  try {
                     painter.drawBorderRect(rect, top, bottom, left, right);
                  } catch (IFException var5) {
                     throw new RuntimeException("Unexpected error while painting borders", var5);
                  }
               }

               public Dimension getImageSize() {
                  return dim.getSize();
               }
            };
            this.paintMarksAsBitmap(painter, rect);
         }

      }
   }

   public void drawLine(final Point start, final Point end, final int width, final Color color, final RuleStyle style) throws IFException {
      if (this.isSpeedOptimized()) {
         super.drawLine(start, end, width, color, style);
      } else {
         final Rectangle boundingBox = this.getLineBoundingBox(start, end, width);
         final Dimension dim = boundingBox.getSize();
         Graphics2DImagePainter painter = new Graphics2DImagePainter() {
            public void paint(Graphics2D g2d, Rectangle2D area) {
               g2d.translate(-boundingBox.x, -boundingBox.y);
               Java2DPainter painter = new Java2DPainter(g2d, PCLPainter.this.getContext(), PCLPainter.this.getFontInfo(), PCLPainter.this.state);

               try {
                  painter.drawLine(start, end, width, color, style);
               } catch (IFException var5) {
                  throw new RuntimeException("Unexpected error while painting a line", var5);
               }
            }

            public Dimension getImageSize() {
               return dim.getSize();
            }
         };
         this.paintMarksAsBitmap(painter, boundingBox);
      }
   }

   private void paintMarksAsBitmap(Graphics2DImagePainter painter, Rectangle boundingBox) throws IFException {
      ImageInfo info = new ImageInfo((String)null, (String)null);
      ImageSize size = new ImageSize();
      size.setSizeInMillipoints(boundingBox.width, boundingBox.height);
      info.setSize(size);
      ImageGraphics2D img = new ImageGraphics2D(info, painter);
      Map hints = new HashMap();
      if (this.isSpeedOptimized()) {
         hints.put(ImageProcessingHints.BITMAP_TYPE_INTENT, "mono");
      } else {
         hints.put(ImageProcessingHints.BITMAP_TYPE_INTENT, "gray");
      }

      hints.put(ImageHandlerUtil.CONVERSION_MODE, "bitmap");
      PCLRenderingContext context = (PCLRenderingContext)this.createRenderingContext();
      context.setSourceTransparencyEnabled(true);

      try {
         this.drawImage(img, boundingBox, context, true, hints);
      } catch (IOException var9) {
         throw new IFException("I/O error while painting marks using a bitmap", var9);
      } catch (ImageException var10) {
         throw new IFException("Error while painting marks using a bitmap", var10);
      }
   }

   public void drawText(int x, int y, int letterSpacing, int wordSpacing, int[][] dp, String text) throws IFException {
      try {
         FontTriplet triplet = new FontTriplet(this.state.getFontFamily(), this.state.getFontStyle(), this.state.getFontWeight());
         String fontKey = this.getFontKey(triplet);
         Typeface tf = this.getTypeface(fontKey);
         boolean drawAsBitmaps = this.getPCLUtil().isAllTextAsBitmaps();
         boolean pclFont = HardcodedFonts.setFont(this.gen, fontKey, this.state.getFontSize(), text);
         if (pclFont) {
            this.drawTextNative(x, y, letterSpacing, wordSpacing, dp, text, triplet);
         } else if (!drawAsBitmaps && this.isTrueType(tf)) {
            if (this.sfManager == null) {
               this.sfManager = new PCLSoftFontManager(this.gen.fontReaderMap);
            }

            if (this.getPCLUtil().isOptimizeResources() || this.sfManager.getSoftFont(tf, text) == null) {
               char[] var12 = text.toCharArray();
               int var13 = var12.length;

               for(int var14 = 0; var14 < var13; ++var14) {
                  char c = var12[var14];
                  tf.mapChar(c);
               }

               ByteArrayOutputStream baos = this.sfManager.makeSoftFont(tf, text);
               if (baos != null) {
                  if (this.getPCLUtil().isOptimizeResources()) {
                     this.gen.addFont(this.sfManager, tf);
                  } else {
                     this.gen.writeBytes(baos.toByteArray());
                  }
               }
            }

            String formattedSize = this.gen.formatDouble2((double)this.state.getFontSize() / 1000.0);
            this.gen.writeCommand(String.format("(s%sV", formattedSize));
            List textSegments = this.sfManager.getTextSegments(text, tf);
            if (textSegments.isEmpty()) {
               textSegments.add(new PCLSoftFontManager.PCLTextSegment(this.sfManager.getSoftFontID(tf), text));
            }

            boolean first = true;
            Iterator var24 = textSegments.iterator();

            while(var24.hasNext()) {
               PCLSoftFontManager.PCLTextSegment textSegment = (PCLSoftFontManager.PCLTextSegment)var24.next();
               this.gen.writeCommand(String.format("(%dX", textSegment.getFontID()));
               PCLSoftFont softFont = this.sfManager.getSoftFontFromID(textSegment.getFontID());
               PCLCharacterWriter charWriter = new PCLTTFCharacterWriter(softFont);
               this.gen.writeBytes(this.sfManager.assignFontID(textSegment.getFontID()));
               this.gen.writeBytes(charWriter.writeCharacterDefinitions(textSegment.getText()));
               if (first) {
                  this.drawTextUsingSoftFont(x, y, letterSpacing, wordSpacing, dp, textSegment.getText(), triplet, softFont);
                  first = false;
               } else {
                  this.drawTextUsingSoftFont(-1, -1, letterSpacing, wordSpacing, dp, textSegment.getText(), triplet, softFont);
               }
            }
         } else {
            this.drawTextAsBitmap(x, y, letterSpacing, wordSpacing, dp, text, triplet);
         }

      } catch (IOException var19) {
         throw new IFException("I/O error in drawText()", var19);
      }
   }

   private boolean isTrueType(Typeface tf) {
      if (tf.getFontType().equals(FontType.TRUETYPE)) {
         return true;
      } else {
         if (tf instanceof CustomFontMetricsMapper) {
            Typeface realFont = ((CustomFontMetricsMapper)tf).getRealFont();
            if (realFont instanceof MultiByteFont) {
               return ((MultiByteFont)realFont).getCIDType().equals(CIDFontType.CIDTYPE2);
            }
         }

         return false;
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

   private void drawTextNative(int x, int y, int letterSpacing, int wordSpacing, int[][] dp, String text, FontTriplet triplet) throws IOException {
      Color textColor = this.state.getTextColor();
      if (textColor != null) {
         this.gen.setTransparencyMode(true, false);
         if (((PCLDocumentHandler)this.getDocumentHandler()).getPCLUtil().isColorEnabled()) {
            this.gen.selectColor(textColor);
         } else {
            this.gen.selectGrayscale(textColor);
         }
      }

      this.gen.setTransparencyMode(true, true);
      this.setCursorPos(x, y);
      float fontSize = (float)this.state.getFontSize() / 1000.0F;
      Font font = this.getFontInfo().getFontInstance(triplet, this.state.getFontSize());
      int l = text.length();
      StringBuffer sb = new StringBuffer(Math.max(16, l));
      if (dp != null && dp[0] != null && dp[0][0] != 0) {
         if (dp[0][0] > 0) {
            sb.append("\u001b&a+").append(this.gen.formatDouble2((double)dp[0][0] / 100.0)).append('H');
         } else {
            sb.append("\u001b&a-").append(this.gen.formatDouble2((double)(-dp[0][0]) / 100.0)).append('H');
         }
      }

      if (dp != null && dp[0] != null && dp[0][1] != 0) {
         if (dp[0][1] > 0) {
            sb.append("\u001b&a-").append(this.gen.formatDouble2((double)dp[0][1] / 100.0)).append('V');
         } else {
            sb.append("\u001b&a+").append(this.gen.formatDouble2((double)(-dp[0][1]) / 100.0)).append('V');
         }
      }

      for(int i = 0; i < l; ++i) {
         char orgChar = text.charAt(i);
         float xGlyphAdjust = 0.0F;
         float yGlyphAdjust = 0.0F;
         char ch;
         if (font.hasChar(orgChar)) {
            ch = font.mapChar(orgChar);
         } else if (CharUtilities.isFixedWidthSpace(orgChar)) {
            ch = font.mapChar(' ');
            int spaceDiff = font.getCharWidth(ch) - font.getCharWidth(orgChar);
            xGlyphAdjust = -((float)(10 * spaceDiff) / fontSize);
         } else {
            ch = font.mapChar(orgChar);
         }

         sb.append(ch);
         if (wordSpacing != 0 && CharUtilities.isAdjustableSpace(orgChar)) {
            xGlyphAdjust += (float)wordSpacing;
         }

         xGlyphAdjust += (float)letterSpacing;
         if (dp != null && i < dp.length && dp[i] != null) {
            xGlyphAdjust += (float)(dp[i][2] - dp[i][0]);
            yGlyphAdjust += (float)(dp[i][3] - dp[i][1]);
         }

         if (dp != null && i < dp.length - 1 && dp[i + 1] != null) {
            xGlyphAdjust += (float)dp[i + 1][0];
            yGlyphAdjust += (float)dp[i + 1][1];
         }

         if (xGlyphAdjust != 0.0F) {
            if (xGlyphAdjust > 0.0F) {
               sb.append("\u001b&a+").append(this.gen.formatDouble2((double)xGlyphAdjust / 100.0)).append('H');
            } else {
               sb.append("\u001b&a-").append(this.gen.formatDouble2((double)(-xGlyphAdjust) / 100.0)).append('H');
            }
         }

         if (yGlyphAdjust != 0.0F) {
            if (yGlyphAdjust > 0.0F) {
               sb.append("\u001b&a-").append(this.gen.formatDouble2((double)yGlyphAdjust / 100.0)).append('V');
            } else {
               sb.append("\u001b&a+").append(this.gen.formatDouble2((double)(-yGlyphAdjust) / 100.0)).append('V');
            }
         }
      }

      this.gen.getOutputStream().write(sb.toString().getBytes(this.gen.getTextEncoding()));
   }

   private void drawTextUsingSoftFont(int x, int y, int letterSpacing, int wordSpacing, int[][] dp, String text, FontTriplet triplet, PCLSoftFont softFont) throws IOException {
      Color textColor = this.state.getTextColor();
      if (textColor != null) {
         this.gen.setTransparencyMode(true, false);
         if (((PCLDocumentHandler)this.getDocumentHandler()).getPCLUtil().isColorEnabled()) {
            this.gen.selectColor(textColor);
         } else {
            this.gen.selectGrayscale(textColor);
         }
      }

      if (x != -1 && y != -1) {
         this.setCursorPos(x, y);
      }

      float fontSize = (float)this.state.getFontSize() / 1000.0F;
      Font font = this.getFontInfo().getFontInstance(triplet, this.state.getFontSize());
      int l = text.length();
      int[] dx = IFUtil.convertDPToDX(dp);
      int dxl = dx != null ? dx.length : 0;
      StringBuffer sb = new StringBuffer(Math.max(16, l));
      if (dx != null && dxl > 0 && dx[0] != 0) {
         sb.append("\u001b&a+").append(this.gen.formatDouble2((double)dx[0] / 100.0)).append('H');
      }

      String current = "";

      int i;
      for(i = 0; i < l; ++i) {
         char orgChar = text.charAt(i);
         float glyphAdjust = 0.0F;
         int j;
         if (!font.hasChar(orgChar) && CharUtilities.isFixedWidthSpace(orgChar)) {
            j = font.mapChar(' ');
            int spaceDiff = font.getCharWidth((char)j) - font.getCharWidth(orgChar);
            glyphAdjust = -((float)(10 * spaceDiff) / fontSize);
         }

         if (wordSpacing != 0 && CharUtilities.isAdjustableSpace(orgChar)) {
            glyphAdjust += (float)wordSpacing;
         }

         current = current + orgChar;
         glyphAdjust += (float)letterSpacing;
         if (dx != null && i < dxl - 1) {
            glyphAdjust += (float)dx[i + 1];
         }

         if (glyphAdjust != 0.0F) {
            this.gen.getOutputStream().write(sb.toString().getBytes(this.gen.getTextEncoding()));

            for(j = 0; j < current.length(); ++j) {
               this.gen.getOutputStream().write(softFont.getCharCode(current.charAt(j)));
            }

            sb = new StringBuffer();
            String command = glyphAdjust > 0.0F ? "\u001b&a+" : "\u001b&a";
            sb.append(command).append(this.gen.formatDouble2((double)glyphAdjust / 100.0)).append('H');
            current = "";
         }
      }

      if (!current.equals("")) {
         this.gen.getOutputStream().write(sb.toString().getBytes(this.gen.getTextEncoding()));

         for(i = 0; i < current.length(); ++i) {
            this.gen.getOutputStream().write(softFont.getCharCode(current.charAt(i)));
         }
      }

   }

   private Rectangle getTextBoundingBox(int x, int y, int letterSpacing, int wordSpacing, int[][] dp, String text, Font font, FontMetricsMapper metrics) {
      int maxAscent = metrics.getMaxAscent(font.getFontSize()) / 1000;
      int descent = metrics.getDescender(font.getFontSize()) / 1000;
      int safetyMargin = (int)(0.05 * (double)font.getFontSize());
      Rectangle boundingRect = new Rectangle(x, y - maxAscent - safetyMargin, 0, maxAscent - descent + 2 * safetyMargin);
      int l = text.length();
      int[] dx = IFUtil.convertDPToDX(dp);
      int dxl = dx != null ? dx.length : 0;
      if (dx != null && dxl > 0 && dx[0] != 0) {
         boundingRect.setLocation(boundingRect.x - (int)Math.ceil((double)((float)dx[0] / 10.0F)), boundingRect.y);
      }

      float width = 0.0F;

      int i;
      for(i = 0; i < l; ++i) {
         char orgChar = text.charAt(i);
         float glyphAdjust = 0.0F;
         int cw = font.getCharWidth(orgChar);
         if (wordSpacing != 0 && CharUtilities.isAdjustableSpace(orgChar)) {
            glyphAdjust += (float)wordSpacing;
         }

         glyphAdjust += (float)letterSpacing;
         if (dx != null && i < dxl - 1) {
            glyphAdjust += (float)dx[i + 1];
         }

         width += (float)cw + glyphAdjust;
      }

      i = font.getFontSize() / 3;
      boundingRect.setSize((int)Math.ceil((double)width) + i, boundingRect.height);
      return boundingRect;
   }

   private void drawTextAsBitmap(final int x, final int y, final int letterSpacing, final int wordSpacing, final int[][] dp, final String text, FontTriplet triplet) throws IFException {
      Font font = this.getFontInfo().getFontInstance(triplet, this.state.getFontSize());

      FontMetricsMapper mapper;
      try {
         mapper = (FontMetricsMapper)this.getFontInfo().getMetricsFor(font.getFontName());
      } catch (Exception var18) {
         throw new RuntimeException(var18);
      }

      final int maxAscent = mapper.getMaxAscent(font.getFontSize()) / 1000;
      final int ascent = mapper.getAscender(font.getFontSize()) / 1000;
      final int descent = mapper.getDescender(font.getFontSize()) / 1000;
      int safetyMargin = (int)(0.05 * (double)font.getFontSize());
      final int baselineOffset = maxAscent + safetyMargin;
      Rectangle boundingBox = this.getTextBoundingBox(x, y, letterSpacing, wordSpacing, dp, text, font, mapper);
      final Dimension dim = boundingBox.getSize();
      Graphics2DImagePainter painter = new Graphics2DImagePainter() {
         public void paint(Graphics2D g2d, Rectangle2D area) {
            g2d.translate(-x, -y + baselineOffset);
            Java2DPainter painter = new Java2DPainter(g2d, PCLPainter.this.getContext(), PCLPainter.this.getFontInfo(), PCLPainter.this.state);

            try {
               painter.drawText(x, y, letterSpacing, wordSpacing, dp, text);
            } catch (IFException var5) {
               throw new RuntimeException("Unexpected error while painting text", var5);
            }
         }

         public Dimension getImageSize() {
            return dim.getSize();
         }
      };
      this.paintMarksAsBitmap(painter, boundingBox);
   }

   private void saveGraphicsState() {
      this.graphicContextStack.push(this.graphicContext);
      this.graphicContext = (GraphicContext)this.graphicContext.clone();
   }

   private void restoreGraphicsState() {
      this.graphicContext = (GraphicContext)this.graphicContextStack.pop();
   }

   private void concatenateTransformationMatrix(AffineTransform transform) throws IOException {
      if (!transform.isIdentity()) {
         this.graphicContext.transform(transform);
         this.changePrintDirection();
      }

   }

   private Point2D transformedPoint(int x, int y) {
      return PCLRenderingUtil.transformedPoint(x, y, this.graphicContext.getTransform(), this.currentPageDefinition, this.currentPrintDirection);
   }

   private void changePrintDirection() throws IOException {
      AffineTransform at = this.graphicContext.getTransform();
      int newDir = PCLRenderingUtil.determinePrintDirection(at);
      if (newDir != this.currentPrintDirection) {
         this.currentPrintDirection = newDir;
         this.gen.changePrintDirection(this.currentPrintDirection);
      }

   }

   void setCursorPos(int x, int y) throws IOException {
      Point2D transPoint = this.transformedPoint(x, y);
      this.gen.setCursorPos(transPoint.getX(), transPoint.getY());
   }
}
