package org.apache.fop.svg;

import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.Typeface;

public abstract class PDFTextUtil extends org.apache.fop.pdf.PDFTextUtil {
   private FontInfo fontInfo;
   private Font font;
   private int encoding;

   public PDFTextUtil(FontInfo fontInfo) {
      this.fontInfo = fontInfo;
   }

   protected void initValues() {
      super.initValues();
      this.font = null;
   }

   public Font getCurrentFont() {
      return this.font;
   }

   public int getCurrentEncoding() {
      return this.encoding;
   }

   public void setCurrentFont(Font f) {
      this.font = f;
   }

   public void setCurrentEncoding(int encoding) {
      this.encoding = encoding;
   }

   protected boolean isMultiByteFont(String name) {
      Typeface f = (Typeface)this.fontInfo.getFonts().get(name);
      return f.isMultiByte();
   }

   protected boolean isCIDFont(String name) {
      Typeface f = (Typeface)this.fontInfo.getFonts().get(name);
      return f.isCID();
   }

   public void writeTf(Font f) {
      String fontName = f.getFontName();
      float fontSize = (float)f.getFontSize() / 1000.0F;
      boolean isMultiByte = this.isMultiByteFont(fontName);
      boolean isCid = this.isCIDFont(fontName);
      if (!isMultiByte && this.encoding != 0) {
         this.updateTf(fontName + "_" + Integer.toString(this.encoding), (double)fontSize, isMultiByte, isCid);
      } else {
         this.updateTf(fontName, (double)fontSize, isMultiByte, isCid);
      }

   }
}
