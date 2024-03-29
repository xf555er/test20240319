package org.apache.fop.render.java2d;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.LineMetrics;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class Java2DFontMetrics {
   public static final int FONT_SIZE = 1;
   public static final int FONT_FACTOR = 1000000;
   private int[] width;
   private int xHeight;
   private int ascender;
   private int descender;
   private Font f1;
   private String family = "";
   private int style;
   private float size;
   private FontMetrics fmt;
   private LineMetrics lineMetrics;
   private final Graphics2D graphics = createFontMetricsGraphics2D();

   private static Graphics2D createFontMetricsGraphics2D() {
      BufferedImage fontImage = new BufferedImage(100, 100, 1);
      Graphics2D graphics2D = fontImage.createGraphics();
      graphics2D.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
      return graphics2D;
   }

   public int getMaxAscent(String family, int style, int size) {
      this.setFont(family, style, size);
      return Math.round(this.lineMetrics.getAscent() * 1000000.0F);
   }

   public int getAscender(String family, int style, int size) {
      this.setFont(family, style, size);
      return this.ascender * 1000;
   }

   public int getCapHeight(String family, int style, int size) {
      return this.getAscender(family, style, size);
   }

   public int getDescender(String family, int style, int size) {
      this.setFont(family, style, size);
      return this.descender * 1000;
   }

   public int getXHeight(String family, int style, int size) {
      this.setFont(family, style, size);
      return this.xHeight * 1000;
   }

   public int getUnderlinePosition(String family, int style, int size) {
      this.setFont(family, style, size);
      return -Math.round(this.lineMetrics.getUnderlineOffset());
   }

   public int getUnderlineThickness(String family, int style, int size) {
      this.setFont(family, style, size);
      return Math.round(this.lineMetrics.getUnderlineThickness());
   }

   public int getStrikeoutPosition(String family, int style, int size) {
      this.setFont(family, style, size);
      return -Math.round(this.lineMetrics.getStrikethroughOffset());
   }

   public int getStrikeoutThickness(String family, int style, int size) {
      this.setFont(family, style, size);
      return Math.round(this.lineMetrics.getStrikethroughThickness());
   }

   public int width(int i, String family, int style, int size) {
      this.setFont(family, style, size);
      int w = this.internalCharWidth(i) * 1000;
      return w;
   }

   private int internalCharWidth(int i) {
      char[] ch = new char[]{(char)i};
      Rectangle2D rect = this.fmt.getStringBounds(ch, 0, 1, this.graphics);
      return (int)Math.round(rect.getWidth() * 1000.0);
   }

   public int[] getWidths(String family, int style, int size) {
      if (this.width == null) {
         this.width = new int[256];
      }

      this.setFont(family, style, size);

      for(int i = 0; i < 256; ++i) {
         this.width[i] = 1000 * this.internalCharWidth(i);
      }

      return this.width;
   }

   private Font getBaseFont(String family, int style, float size) {
      Map atts = new HashMap();
      atts.put(TextAttribute.FAMILY, family);
      if ((style & 1) != 0) {
         atts.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
      }

      if ((style & 2) != 0) {
         atts.put(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
      }

      atts.put(TextAttribute.SIZE, size);
      return new Font(atts);
   }

   private boolean setFont(String family, int style, int size) {
      boolean changed = false;
      float s = (float)size / 1000.0F;
      if (this.f1 == null) {
         this.f1 = this.getBaseFont(family, style, s);
         this.fmt = this.graphics.getFontMetrics(this.f1);
         changed = true;
      } else if (this.style != style || !this.family.equals(family) || this.size != s) {
         if (family.equals(this.family)) {
            this.f1 = this.f1.deriveFont(style, s);
         } else {
            this.f1 = this.getBaseFont(family, style, s);
         }

         this.fmt = this.graphics.getFontMetrics(this.f1);
         changed = true;
      }

      if (changed) {
         TextLayout layout = new TextLayout("x", this.f1, this.graphics.getFontRenderContext());
         Rectangle2D rect = layout.getBounds();
         this.xHeight = (int)Math.round(-rect.getY() * 1000.0);
         layout = new TextLayout("d", this.f1, this.graphics.getFontRenderContext());
         rect = layout.getBounds();
         this.ascender = (int)Math.round(-rect.getY() * 1000.0);
         layout = new TextLayout("p", this.f1, this.graphics.getFontRenderContext());
         rect = layout.getBounds();
         this.descender = (int)Math.round((rect.getY() + rect.getHeight()) * -1000.0);
         this.lineMetrics = this.f1.getLineMetrics("", this.graphics.getFontRenderContext());
      }

      this.family = family;
      this.style = style;
      this.size = s;
      return changed;
   }

   public Font getFont(String family, int style, int size) {
      this.setFont(family, style, size);
      return this.f1;
   }

   public boolean hasChar(String family, int style, int size, char c) {
      this.setFont(family, style, size);
      return this.f1.canDisplay(c);
   }
}
