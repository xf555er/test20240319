package org.apache.fop.layoutmgr.inline;

import org.apache.fop.traits.WritingMode;

final class ScaledBaselineTable {
   private static final float HANGING_BASELINE_FACTOR = 0.8F;
   private static final float MATHEMATICAL_BASELINE_FACTOR = 0.5F;
   private final int altitude;
   private final int depth;
   private final int xHeight;
   private final int dominantBaselineIdentifier;
   private final WritingMode writingMode;
   private final int dominantBaselineOffset;
   private int beforeEdgeOffset;
   private int afterEdgeOffset;

   ScaledBaselineTable(int altitude, int depth, int xHeight, int dominantBaselineIdentifier, WritingMode writingMode) {
      this.altitude = altitude;
      this.depth = depth;
      this.xHeight = xHeight;
      this.dominantBaselineIdentifier = dominantBaselineIdentifier;
      this.writingMode = writingMode;
      this.dominantBaselineOffset = this.getBaselineDefaultOffset(this.dominantBaselineIdentifier);
      this.beforeEdgeOffset = altitude - this.dominantBaselineOffset;
      this.afterEdgeOffset = depth - this.dominantBaselineOffset;
   }

   int getDominantBaselineIdentifier() {
      return this.dominantBaselineIdentifier;
   }

   WritingMode getWritingMode() {
      return this.writingMode;
   }

   int getBaseline(int baselineIdentifier) {
      int offset = false;
      if (!this.isHorizontalWritingMode()) {
         switch (baselineIdentifier) {
            case 20:
            case 143:
            case 144:
            case 145:
               throw new IllegalArgumentException("Baseline " + baselineIdentifier + " only supported for horizontal writing modes");
         }
      }

      int offset;
      switch (baselineIdentifier) {
         case 4:
         case 20:
            offset = this.afterEdgeOffset;
            break;
         case 6:
         case 24:
         case 56:
         case 59:
         case 82:
         case 84:
         case 141:
         case 142:
         case 143:
         case 144:
            offset = this.getBaselineDefaultOffset(baselineIdentifier) - this.dominantBaselineOffset;
            break;
         case 14:
         case 145:
            offset = this.beforeEdgeOffset;
            break;
         default:
            throw new IllegalArgumentException(String.valueOf(baselineIdentifier));
      }

      return offset;
   }

   private boolean isHorizontalWritingMode() {
      return this.writingMode.isHorizontal();
   }

   private int getBaselineDefaultOffset(int baselineIdentifier) {
      int offset = false;
      int offset;
      switch (baselineIdentifier) {
         case 6:
            offset = 0;
            break;
         case 24:
            offset = (this.altitude - this.depth) / 2 + this.depth;
            break;
         case 56:
            offset = Math.round((float)this.altitude * 0.8F);
            break;
         case 59:
         case 141:
            offset = this.depth;
            break;
         case 82:
            offset = Math.round((float)this.altitude * 0.5F);
            break;
         case 84:
            offset = this.xHeight / 2;
            break;
         case 142:
            offset = this.altitude;
            break;
         default:
            throw new IllegalArgumentException(String.valueOf(baselineIdentifier));
      }

      return offset;
   }

   void setBeforeAndAfterBaselines(int beforeBaseline, int afterBaseline) {
      this.beforeEdgeOffset = beforeBaseline;
      this.afterEdgeOffset = afterBaseline;
   }

   ScaledBaselineTable deriveScaledBaselineTable(int baselineIdentifier) {
      ScaledBaselineTable bac = new ScaledBaselineTable(this.altitude, this.depth, this.xHeight, baselineIdentifier, this.writingMode);
      return bac;
   }
}
