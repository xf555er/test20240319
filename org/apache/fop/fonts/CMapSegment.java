package org.apache.fop.fonts;

public final class CMapSegment {
   private final int unicodeStart;
   private final int unicodeEnd;
   private final int glyphStartIndex;

   public CMapSegment(int unicodeStart, int unicodeEnd, int glyphStartIndex) {
      this.unicodeStart = unicodeStart;
      this.unicodeEnd = unicodeEnd;
      this.glyphStartIndex = glyphStartIndex;
   }

   public int hashCode() {
      int hc = 17;
      hc = 31 * hc + this.unicodeStart;
      hc = 31 * hc + this.unicodeEnd;
      hc = 31 * hc + this.glyphStartIndex;
      return hc;
   }

   public boolean equals(Object o) {
      if (!(o instanceof CMapSegment)) {
         return false;
      } else {
         CMapSegment ce = (CMapSegment)o;
         return ce.unicodeStart == this.unicodeStart && ce.unicodeEnd == this.unicodeEnd && ce.glyphStartIndex == this.glyphStartIndex;
      }
   }

   public int getUnicodeStart() {
      return this.unicodeStart;
   }

   public int getUnicodeEnd() {
      return this.unicodeEnd;
   }

   public int getGlyphStartIndex() {
      return this.glyphStartIndex;
   }

   public String toString() {
      StringBuilder sb = new StringBuilder("CMapSegment: ");
      sb.append("{ UC[");
      sb.append(this.unicodeStart);
      sb.append(',');
      sb.append(this.unicodeEnd);
      sb.append("]: GC[");
      sb.append(this.glyphStartIndex);
      sb.append(',');
      sb.append(this.glyphStartIndex + (this.unicodeEnd - this.unicodeStart));
      sb.append("] }");
      return sb.toString();
   }
}
