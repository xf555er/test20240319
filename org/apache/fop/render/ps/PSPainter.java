package org.apache.fop.render.ps;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.fonts.LazyFont;
import org.apache.fop.fonts.MultiByteFont;
import org.apache.fop.fonts.SingleByteFont;
import org.apache.fop.fonts.Typeface;
import org.apache.fop.render.RenderingContext;
import org.apache.fop.render.intermediate.AbstractIFPainter;
import org.apache.fop.render.intermediate.BorderPainter;
import org.apache.fop.render.intermediate.GraphicsPainter;
import org.apache.fop.render.intermediate.IFException;
import org.apache.fop.render.intermediate.IFState;
import org.apache.fop.traits.BorderProps;
import org.apache.fop.traits.RuleStyle;
import org.apache.fop.util.CharUtilities;
import org.apache.fop.util.HexEncoder;
import org.apache.xmlgraphics.image.loader.ImageException;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageProcessingHints;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;
import org.apache.xmlgraphics.ps.PSGenerator;
import org.apache.xmlgraphics.ps.PSResource;
import org.w3c.dom.Document;

public class PSPainter extends AbstractIFPainter {
   private static Log log = LogFactory.getLog(PSPainter.class);
   private final GraphicsPainter graphicsPainter;
   private BorderPainter borderPainter;
   private boolean inTextMode;

   public PSPainter(PSDocumentHandler documentHandler) {
      this(documentHandler, IFState.create());
   }

   protected PSPainter(PSDocumentHandler documentHandler, IFState state) {
      super(documentHandler);
      this.graphicsPainter = new PSGraphicsPainter(this.getGenerator());
      this.borderPainter = new BorderPainter(this.graphicsPainter);
      this.state = state;
   }

   private PSGenerator getGenerator() {
      return ((PSDocumentHandler)this.getDocumentHandler()).getGenerator();
   }

   public void startViewport(AffineTransform transform, Dimension size, Rectangle clipRect) throws IFException {
      try {
         PSGenerator generator = this.getGenerator();
         this.saveGraphicsState();
         generator.concatMatrix(toPoints(transform));
      } catch (IOException var5) {
         throw new IFException("I/O error in startViewport()", var5);
      }

      if (clipRect != null) {
         this.clipRect(clipRect);
      }

   }

   public void endViewport() throws IFException {
      try {
         this.restoreGraphicsState();
      } catch (IOException var2) {
         throw new IFException("I/O error in endViewport()", var2);
      }
   }

   public void startGroup(AffineTransform transform, String layer) throws IFException {
      try {
         PSGenerator generator = this.getGenerator();
         this.saveGraphicsState();
         generator.concatMatrix(toPoints(transform));
      } catch (IOException var4) {
         throw new IFException("I/O error in startGroup()", var4);
      }
   }

   public void endGroup() throws IFException {
      try {
         this.restoreGraphicsState();
      } catch (IOException var2) {
         throw new IFException("I/O error in endGroup()", var2);
      }
   }

   protected Map createDefaultImageProcessingHints(ImageSessionContext sessionContext) {
      Map hints = super.createDefaultImageProcessingHints(sessionContext);
      hints.put(ImageProcessingHints.TRANSPARENCY_INTENT, "ignore");
      return hints;
   }

   protected RenderingContext createRenderingContext() {
      PSRenderingContext psContext = new PSRenderingContext(this.getUserAgent(), this.getGenerator(), this.getFontInfo());
      return psContext;
   }

   protected void drawImageUsingImageHandler(ImageInfo info, Rectangle rect) throws ImageException, IOException {
      if (((PSDocumentHandler)this.getDocumentHandler()).getPSUtil().isOptimizeResources() && !PSImageUtils.isImageInlined(info, (PSRenderingContext)this.createRenderingContext())) {
         if (log.isDebugEnabled()) {
            log.debug("Image " + info + " is embedded as a form later");
         }

         PSResource form = ((PSDocumentHandler)this.getDocumentHandler()).getFormForImage(info.getOriginalURI());
         PSImageUtils.drawForm(form, info, rect, this.getGenerator());
      } else {
         super.drawImageUsingImageHandler(info, rect);
      }

   }

   public void drawImage(String uri, Rectangle rect) throws IFException {
      try {
         this.endTextObject();
      } catch (IOException var4) {
         throw new IFException("I/O error in drawImage()", var4);
      }

      this.drawImageUsingURI(uri, rect);
   }

   public void drawImage(Document doc, Rectangle rect) throws IFException {
      try {
         this.endTextObject();
      } catch (IOException var4) {
         throw new IFException("I/O error in drawImage()", var4);
      }

      this.drawImageUsingDocument(doc, rect);
   }

   public void clipRect(Rectangle rect) throws IFException {
      try {
         PSGenerator generator = this.getGenerator();
         this.endTextObject();
         generator.defineRect((double)rect.x / 1000.0, (double)rect.y / 1000.0, (double)rect.width / 1000.0, (double)rect.height / 1000.0);
         generator.writeln(generator.mapCommand("clip") + " " + generator.mapCommand("newpath"));
      } catch (IOException var3) {
         throw new IFException("I/O error in clipRect()", var3);
      }
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
            try {
               this.endTextObject();
               PSGenerator generator = this.getGenerator();
               if (fill != null) {
                  if (!(fill instanceof Color)) {
                     throw new UnsupportedOperationException("Non-Color paints NYI");
                  }

                  generator.useColor((Color)fill);
               }

               generator.defineRect((double)rect.x / 1000.0, (double)rect.y / 1000.0, (double)rect.width / 1000.0, (double)rect.height / 1000.0);
               generator.writeln(generator.mapCommand("fill"));
            } catch (IOException var4) {
               throw new IFException("I/O error in fillRect()", var4);
            }
         }

      }
   }

   public void drawBorderRect(Rectangle rect, BorderProps top, BorderProps bottom, BorderProps left, BorderProps right, Color innerBackgroundColor) throws IFException {
      if (top != null || bottom != null || left != null || right != null) {
         try {
            this.endTextObject();
            if (((PSDocumentHandler)this.getDocumentHandler()).getPSUtil().getRenderingMode() == PSRenderingMode.SIZE && this.hasOnlySolidBorders(top, bottom, left, right)) {
               super.drawBorderRect(rect, top, bottom, left, right, innerBackgroundColor);
            } else {
               this.borderPainter.drawBorders(rect, top, bottom, left, right, innerBackgroundColor);
            }
         } catch (IOException var8) {
            throw new IFException("I/O error in drawBorderRect()", var8);
         }
      }

   }

   public void drawLine(Point start, Point end, int width, Color color, RuleStyle style) throws IFException {
      try {
         this.endTextObject();
         this.graphicsPainter.drawLine(start, end, width, color, style);
      } catch (IOException var7) {
         throw new IFException("I/O error in drawLine()", var7);
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

   protected void saveGraphicsState() throws IOException {
      this.endTextObject();
      this.getGenerator().saveGraphicsState();
   }

   protected void restoreGraphicsState() throws IOException {
      this.endTextObject();
      this.getGenerator().restoreGraphicsState();
   }

   protected void beginTextObject() throws IOException {
      if (!this.inTextMode) {
         PSGenerator generator = this.getGenerator();
         generator.saveGraphicsState();
         generator.writeln("BT");
         this.inTextMode = true;
      }

   }

   protected void endTextObject() throws IOException {
      if (this.inTextMode) {
         this.inTextMode = false;
         PSGenerator generator = this.getGenerator();
         generator.writeln("ET");
         generator.restoreGraphicsState();
      }

   }

   private String formatMptAsPt(PSGenerator gen, int value) {
      return gen.formatDouble((double)value / 1000.0);
   }

   public void drawText(int x, int y, int letterSpacing, int wordSpacing, int[][] dp, String text) throws IFException {
      try {
         if (this.state.getFontSize() != 0) {
            PSGenerator generator = this.getGenerator();
            generator.useColor(this.state.getTextColor());
            FontTriplet triplet = new FontTriplet(this.state.getFontFamily(), this.state.getFontStyle(), this.state.getFontWeight());
            String fontKey = this.getFontKey(triplet);
            Typeface typeface = this.getTypeface(fontKey);
            if (typeface instanceof MultiByteFont && ((MultiByteFont)typeface).hasSVG()) {
               this.drawSVGText((MultiByteFont)typeface, triplet, x, y, text, this.state);
            } else {
               this.beginTextObject();
               int sizeMillipoints = this.state.getFontSize();
               SingleByteFont singleByteFont = null;
               if (typeface instanceof SingleByteFont) {
                  singleByteFont = (SingleByteFont)typeface;
               }

               Font font = this.getFontInfo().getFontInstance(triplet, sizeMillipoints);
               PSFontResource res = ((PSDocumentHandler)this.getDocumentHandler()).getPSResourceForFontKey(fontKey);
               boolean isOpenTypeFont = typeface instanceof MultiByteFont && ((MultiByteFont)typeface).isOTFFile();
               this.useFont(fontKey, sizeMillipoints, isOpenTypeFont);
               if (dp != null && dp[0] != null) {
                  x += dp[0][0];
                  y -= dp[0][1];
               }

               generator.writeln("1 0 0 -1 " + this.formatMptAsPt(generator, x) + " " + this.formatMptAsPt(generator, y) + " Tm");
               int textLen = text.length();
               int start = 0;
               int currentEncoding;
               int i;
               char orgChar;
               int encoding;
               if (singleByteFont != null) {
                  currentEncoding = -1;

                  for(i = 0; i < textLen; ++i) {
                     orgChar = text.charAt(i);
                     char mapped = typeface.mapChar(orgChar);
                     encoding = mapped / 256;
                     if (currentEncoding != encoding) {
                        if (i > 0) {
                           this.writeText(text, start, i - start, letterSpacing, wordSpacing, dp, font, typeface, false);
                        }

                        if (encoding == 0) {
                           this.useFont(fontKey, sizeMillipoints, false);
                        } else {
                           this.useFont(fontKey + "_" + Integer.toString(encoding), sizeMillipoints, false);
                        }

                        currentEncoding = encoding;
                        start = i;
                     }
                  }
               } else if (typeface instanceof MultiByteFont && ((MultiByteFont)typeface).isOTFFile()) {
                  currentEncoding = 0;

                  for(i = start; i < textLen; ++i) {
                     orgChar = text.charAt(i);
                     MultiByteFont mbFont = (MultiByteFont)typeface;
                     mbFont.mapChar(orgChar);
                     encoding = mbFont.findGlyphIndex(orgChar);
                     int newGlyphIdx = (Integer)mbFont.getUsedGlyphs().get(encoding);
                     int encoding = newGlyphIdx / 256;
                     if (encoding != currentEncoding) {
                        if (i != 0) {
                           this.writeText(text, start, i - start, letterSpacing, wordSpacing, dp, font, typeface, true);
                           start = i;
                        }

                        generator.useFont("/" + res.getName() + "." + encoding, (float)sizeMillipoints / 1000.0F);
                        currentEncoding = encoding;
                     }
                  }
               } else {
                  this.useFont(fontKey, sizeMillipoints, false);
               }

               this.writeText(text, start, textLen - start, letterSpacing, wordSpacing, dp, font, typeface, typeface instanceof MultiByteFont);
            }
         }
      } catch (IOException var25) {
         throw new IFException("I/O error in drawText()", var25);
      }
   }

   private void writeText(String text, int start, int len, int letterSpacing, int wordSpacing, int[][] dp, Font font, Typeface tf, boolean multiByte) throws IOException {
      PSGenerator generator = this.getGenerator();
      int end = start + len;
      int initialSize = len + len / 2;
      boolean hasLetterSpacing = letterSpacing != 0;
      boolean needTJ = false;
      int lineStart = 0;
      StringBuffer accText = new StringBuffer(initialSize);
      StringBuffer sb = new StringBuffer(initialSize);
      boolean isOTF = multiByte && ((MultiByteFont)tf).isOTFFile();

      for(int i = start; i < end; ++i) {
         int orgChar = text.charAt(i);
         int xGlyphAdjust = 0;
         int yGlyphAdjust = 0;
         int ch;
         if (CharUtilities.isFixedWidthSpace(orgChar)) {
            ch = font.mapChar(' ');
            int cw = font.getCharWidth(orgChar);
            xGlyphAdjust = font.getCharWidth(ch) - cw;
         } else {
            if (wordSpacing != 0 && CharUtilities.isAdjustableSpace(orgChar)) {
               xGlyphAdjust -= wordSpacing;
            }

            if (CharUtilities.containsSurrogatePairAt(text, i)) {
               char var10000 = (char)orgChar;
               ++i;
               orgChar = Character.toCodePoint(var10000, text.charAt(i));
            }

            ch = font.mapCodePoint(orgChar);
         }

         if (dp != null && i < dp.length && dp[i] != null) {
            xGlyphAdjust -= dp[i][2] - dp[i][0];
            yGlyphAdjust += dp[i][3] - dp[i][1];
         }

         if (dp != null && i < dp.length - 1 && dp[i + 1] != null) {
            xGlyphAdjust -= dp[i + 1][0];
            yGlyphAdjust += dp[i + 1][1];
         }

         if (multiByte && !isOTF) {
            accText.append(HexEncoder.encode(ch));
         } else {
            char codepoint = (char)(ch % 256);
            if (isOTF) {
               accText.append(HexEncoder.encode(codepoint, 2));
            } else {
               PSGenerator.escapeChar(codepoint, accText);
            }
         }

         if (xGlyphAdjust != 0 || yGlyphAdjust != 0) {
            needTJ = true;
            if (sb.length() == 0) {
               sb.append('[');
            }

            if (accText.length() > 0) {
               if (sb.length() - lineStart + accText.length() > 200) {
                  sb.append('\n');
                  lineStart = sb.length();
               }

               lineStart = this.writePostScriptString(sb, accText, multiByte, lineStart);
               sb.append(' ');
               accText.setLength(0);
            }

            if (yGlyphAdjust == 0) {
               sb.append(Integer.toString(xGlyphAdjust)).append(' ');
            } else {
               sb.append('[');
               sb.append(Integer.toString(yGlyphAdjust)).append(' ');
               sb.append(Integer.toString(xGlyphAdjust)).append(']').append(' ');
            }
         }
      }

      if (needTJ) {
         if (accText.length() > 0) {
            if (sb.length() - lineStart + accText.length() > 200) {
               sb.append('\n');
            }

            this.writePostScriptString(sb, accText, multiByte);
         }

         if (hasLetterSpacing) {
            sb.append("] " + this.formatMptAsPt(generator, letterSpacing) + " ATJ");
         } else {
            sb.append("] TJ");
         }
      } else {
         this.writePostScriptString(sb, accText, multiByte);
         if (hasLetterSpacing) {
            StringBuffer spb = new StringBuffer();
            spb.append(this.formatMptAsPt(generator, letterSpacing)).append(" 0 ");
            sb.insert(0, spb.toString());
            sb.append(" " + generator.mapCommand("ashow"));
         } else {
            sb.append(" " + generator.mapCommand("show"));
         }
      }

      generator.writeln(sb.toString());
   }

   private void writePostScriptString(StringBuffer buffer, StringBuffer string, boolean multiByte) {
      this.writePostScriptString(buffer, string, multiByte, 0);
   }

   private int writePostScriptString(StringBuffer buffer, StringBuffer string, boolean multiByte, int lineStart) {
      buffer.append((char)(multiByte ? '<' : '('));
      int l = string.length();
      int index = 0;
      int maxCol = 200;
      buffer.append(string.substring(index, Math.min(index + maxCol, l)));

      for(index += maxCol; index < l; index += maxCol) {
         if (!multiByte) {
            buffer.append('\\');
         }

         buffer.append('\n');
         lineStart = buffer.length();
         buffer.append(string.substring(index, Math.min(index + maxCol, l)));
      }

      buffer.append((char)(multiByte ? '>' : ')'));
      return lineStart;
   }

   private void useFont(String key, int size, boolean otf) throws IOException {
      PSFontResource res = ((PSDocumentHandler)this.getDocumentHandler()).getPSResourceForFontKey(key);
      PSGenerator generator = this.getGenerator();
      String name = "/" + res.getName();
      if (otf) {
         name = name + ".0";
      }

      generator.useFont(name, (float)size / 1000.0F);
      res.notifyResourceUsageOnPage(generator.getResourceTracker());
   }
}
