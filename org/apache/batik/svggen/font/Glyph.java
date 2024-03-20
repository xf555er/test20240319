package org.apache.batik.svggen.font;

import org.apache.batik.svggen.font.table.GlyphDescription;

public class Glyph {
   protected short leftSideBearing;
   protected int advanceWidth;
   private Point[] points;

   public Glyph(GlyphDescription gd, short lsb, int advance) {
      this.leftSideBearing = lsb;
      this.advanceWidth = advance;
      this.describe(gd);
   }

   public int getAdvanceWidth() {
      return this.advanceWidth;
   }

   public short getLeftSideBearing() {
      return this.leftSideBearing;
   }

   public Point getPoint(int i) {
      return this.points[i];
   }

   public int getPointCount() {
      return this.points.length;
   }

   public void reset() {
   }

   public void scale(int factor) {
      Point[] var2 = this.points;
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Point point = var2[var4];
         point.x = (point.x << 10) * factor >> 26;
         point.y = (point.y << 10) * factor >> 26;
      }

      this.leftSideBearing = (short)(this.leftSideBearing * factor >> 6);
      this.advanceWidth = this.advanceWidth * factor >> 6;
   }

   private void describe(GlyphDescription gd) {
      int endPtIndex = 0;
      this.points = new Point[gd.getPointCount() + 2];

      for(int i = 0; i < gd.getPointCount(); ++i) {
         boolean endPt = gd.getEndPtOfContours(endPtIndex) == i;
         if (endPt) {
            ++endPtIndex;
         }

         this.points[i] = new Point(gd.getXCoordinate(i), gd.getYCoordinate(i), (gd.getFlags(i) & 1) != 0, endPt);
      }

      this.points[gd.getPointCount()] = new Point(0, 0, true, true);
      this.points[gd.getPointCount() + 1] = new Point(this.advanceWidth, 0, true, true);
   }
}
