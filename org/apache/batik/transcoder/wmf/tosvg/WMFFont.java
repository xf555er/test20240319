package org.apache.batik.transcoder.wmf.tosvg;

import java.awt.Font;

public class WMFFont {
   public Font font;
   public int charset;
   public int underline = 0;
   public int strikeOut = 0;
   public int italic = 0;
   public int weight = 0;
   public int orientation = 0;
   public int escape = 0;

   public WMFFont(Font font, int charset) {
      this.font = font;
      this.charset = charset;
   }

   public WMFFont(Font font, int charset, int underline, int strikeOut, int italic, int weight, int orient, int escape) {
      this.font = font;
      this.charset = charset;
      this.underline = underline;
      this.strikeOut = strikeOut;
      this.italic = italic;
      this.weight = weight;
      this.orientation = orient;
      this.escape = escape;
   }
}
