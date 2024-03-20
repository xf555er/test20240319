package org.apache.fop.render.ps;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontSetup;
import org.apache.xmlgraphics.java2d.ps.PSGraphics2D;
import org.apache.xmlgraphics.java2d.ps.PSTextHandler;
import org.apache.xmlgraphics.ps.PSGenerator;

public class NativeTextHandler implements PSTextHandler {
   private PSGraphics2D rootG2D;
   protected FontInfo fontInfo;
   protected Font font;
   protected Font overrideFont;
   protected String currentFontName;
   protected int currentFontSize;

   public NativeTextHandler(PSGraphics2D g2d, FontInfo fontInfo) {
      this.rootG2D = g2d;
      if (fontInfo != null) {
         this.fontInfo = fontInfo;
      } else {
         this.setupFontInfo();
      }

   }

   private void setupFontInfo() {
      this.fontInfo = new FontInfo();
      boolean base14Kerning = false;
      FontSetup.setup(this.fontInfo, base14Kerning);
   }

   public FontInfo getFontInfo() {
      return this.fontInfo;
   }

   private PSGenerator getPSGenerator() {
      return this.rootG2D.getPSGenerator();
   }

   public void writeSetup() throws IOException {
      if (this.fontInfo != null) {
         PSFontUtils.writeFontDict(this.getPSGenerator(), this.fontInfo);
      }

   }

   public void writePageSetup() throws IOException {
   }

   public void drawString(Graphics2D g, String s, float x, float y) throws IOException {
      PSGraphics2D g2d = (PSGraphics2D)g;
      g2d.preparePainting();
      if (this.overrideFont == null) {
         java.awt.Font awtFont = g2d.getFont();
         this.font = this.createFont(awtFont);
      } else {
         this.font = this.overrideFont;
         this.overrideFont = null;
      }

      g2d.establishColor(g2d.getColor());
      this.establishCurrentFont();
      PSGenerator gen = this.getPSGenerator();
      gen.saveGraphicsState();
      Shape imclip = g2d.getClip();
      g2d.writeClip(imclip);
      AffineTransform trans = g2d.getTransform();
      gen.concatMatrix(trans);
      gen.writeln(gen.formatDouble((double)x) + " " + gen.formatDouble((double)y) + " moveto ");
      gen.writeln("1 -1 scale");
      StringBuffer sb = new StringBuffer("(");
      this.escapeText(s, sb);
      sb.append(") t ");
      gen.writeln(sb.toString());
      gen.restoreGraphicsState();
   }

   private void escapeText(String text, StringBuffer target) {
      int l = text.length();

      for(int i = 0; i < l; ++i) {
         char ch = text.charAt(i);
         char mch = this.font.mapChar(ch);
         PSGenerator.escapeChar(mch, target);
      }

   }

   private Font createFont(java.awt.Font f) {
      return this.fontInfo.getFontInstanceForAWTFont(f);
   }

   private void establishCurrentFont() throws IOException {
      if (!this.currentFontName.equals(this.font.getFontName()) || this.currentFontSize != this.font.getFontSize()) {
         PSGenerator gen = this.getPSGenerator();
         gen.writeln("/" + this.font.getFontTriplet().getName() + " " + gen.formatDouble((double)((float)this.font.getFontSize() / 1000.0F)) + " F");
         this.currentFontName = this.font.getFontName();
         this.currentFontSize = this.font.getFontSize();
      }

   }

   public void setOverrideFont(Font override) {
      this.overrideFont = override;
   }
}
