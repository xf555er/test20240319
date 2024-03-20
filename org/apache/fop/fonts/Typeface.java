package org.apache.fop.fonts;

import java.util.HashSet;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class Typeface implements FontMetrics {
   public static final char NOT_FOUND = '#';
   private static Log log = LogFactory.getLog(Typeface.class);
   private long charMapOps;
   protected FontEventListener eventListener;
   private Set warnedChars;

   public abstract String getEncodingName();

   public abstract char mapChar(char var1);

   protected void notifyMapOperation() {
      ++this.charMapOps;
   }

   public boolean hadMappingOperations() {
      return this.charMapOps > 0L;
   }

   public abstract boolean hasChar(char var1);

   public boolean isMultiByte() {
      return false;
   }

   public boolean isCID() {
      return this.getFontType() == FontType.TYPE1C;
   }

   public int getMaxAscent(int size) {
      return this.getAscender(size);
   }

   public boolean hasFeature(int tableType, String script, String language, String feature) {
      return false;
   }

   public void setEventListener(FontEventListener listener) {
      this.eventListener = listener;
   }

   protected void warnMissingGlyph(char c) {
      Character ch = c;
      if (this.warnedChars == null) {
         this.warnedChars = new HashSet();
      }

      if (this.warnedChars.size() < 8 && !this.warnedChars.contains(ch)) {
         this.warnedChars.add(ch);
         if (this.eventListener != null) {
            this.eventListener.glyphNotAvailable(this, c, this.getFontName());
         } else if (this.warnedChars.size() == 8) {
            log.warn("Many requested glyphs are not available in font " + this.getFontName());
         } else {
            log.warn("Glyph " + c + " (0x" + Integer.toHexString(c) + ", " + org.apache.xmlgraphics.fonts.Glyphs.charToGlyphName(c) + ") not available in font " + this.getFontName());
         }
      }

   }

   public String toString() {
      StringBuffer sbuf = new StringBuffer(super.toString());
      sbuf.append('{');
      sbuf.append(this.getFullName());
      sbuf.append('}');
      return sbuf.toString();
   }
}
