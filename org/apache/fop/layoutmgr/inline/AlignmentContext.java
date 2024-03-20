package org.apache.fop.layoutmgr.inline;

import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.PercentBaseContext;
import org.apache.fop.datatypes.SimplePercentBaseContext;
import org.apache.fop.fo.Constants;
import org.apache.fop.fonts.Font;
import org.apache.fop.traits.WritingMode;

public class AlignmentContext implements Constants {
   private int areaHeight;
   private int lineHeight;
   private int alignmentPoint;
   private int baselineShiftValue;
   private int alignmentBaselineIdentifier;
   private int xHeight;
   private ScaledBaselineTable scaledBaselineTable;
   private ScaledBaselineTable actualBaselineTable;
   private AlignmentContext parentAlignmentContext;

   AlignmentContext(int height, Length alignmentAdjust, int alignmentBaseline, Length baselineShift, int dominantBaseline, AlignmentContext parentAlignmentContext) {
      this(height, 0, height, height, alignmentAdjust, alignmentBaseline, baselineShift, dominantBaseline, parentAlignmentContext);
   }

   AlignmentContext(Font font, int lineHeight, Length alignmentAdjust, int alignmentBaseline, Length baselineShift, int dominantBaseline, AlignmentContext parentAlignmentContext) {
      this(font.getAscender(), font.getDescender(), lineHeight, font.getXHeight(), alignmentAdjust, alignmentBaseline, baselineShift, dominantBaseline, parentAlignmentContext);
   }

   private AlignmentContext(int altitude, int depth, int lineHeight, int xHeight, Length alignmentAdjust, int alignmentBaseline, Length baselineShift, int dominantBaseline, AlignmentContext parentAlignmentContext) {
      this.areaHeight = altitude - depth;
      this.lineHeight = lineHeight;
      this.xHeight = xHeight;
      this.parentAlignmentContext = parentAlignmentContext;
      this.scaledBaselineTable = parentAlignmentContext.getScaledBaselineTable();
      this.setAlignmentBaselineIdentifier(alignmentBaseline, parentAlignmentContext.getDominantBaselineIdentifier());
      this.setBaselineShift(baselineShift);
      int dominantBaselineIdentifier = parentAlignmentContext.getDominantBaselineIdentifier();
      boolean newScaledBaselineTableRequired = false;
      if (this.baselineShiftValue != 0) {
         newScaledBaselineTableRequired = true;
      }

      switch (dominantBaseline) {
         case 9:
            newScaledBaselineTableRequired = this.baselineShiftValue != 0;
         case 87:
         case 157:
            break;
         case 116:
            newScaledBaselineTableRequired = true;
            break;
         default:
            newScaledBaselineTableRequired = true;
            dominantBaselineIdentifier = dominantBaseline;
      }

      this.actualBaselineTable = new ScaledBaselineTable(altitude, depth, xHeight, dominantBaselineIdentifier, this.scaledBaselineTable.getWritingMode());
      if (newScaledBaselineTableRequired) {
         this.scaledBaselineTable = new ScaledBaselineTable(altitude, depth, xHeight, dominantBaselineIdentifier, this.scaledBaselineTable.getWritingMode());
      }

      this.setAlignmentAdjust(alignmentAdjust);
   }

   AlignmentContext(Font font, int lineHeight, WritingMode writingMode) {
      this.areaHeight = font.getAscender() - font.getDescender();
      this.lineHeight = lineHeight;
      this.xHeight = font.getXHeight();
      this.scaledBaselineTable = new ScaledBaselineTable(font.getAscender(), font.getDescender(), font.getXHeight(), 6, writingMode);
      this.actualBaselineTable = this.scaledBaselineTable;
      this.alignmentBaselineIdentifier = this.getDominantBaselineIdentifier();
      this.alignmentPoint = font.getAscender();
      this.baselineShiftValue = 0;
   }

   public int getAlignmentPoint() {
      return this.alignmentPoint;
   }

   public int getBaselineShiftValue() {
      return this.baselineShiftValue;
   }

   public int getAlignmentBaselineIdentifier() {
      return this.alignmentBaselineIdentifier;
   }

   private void setAlignmentBaselineIdentifier(int alignmentBaseline, int parentDominantBaselineIdentifier) {
      switch (alignmentBaseline) {
         case 4:
         case 6:
         case 14:
         case 24:
         case 56:
         case 59:
         case 82:
         case 84:
         case 141:
         case 142:
            this.alignmentBaselineIdentifier = alignmentBaseline;
            break;
         case 9:
         case 12:
            this.alignmentBaselineIdentifier = parentDominantBaselineIdentifier;
            break;
         default:
            throw new IllegalArgumentException(String.valueOf(alignmentBaseline));
      }

   }

   private void setAlignmentAdjust(Length alignmentAdjust) {
      int beforeEdge = this.actualBaselineTable.getBaseline(14);
      switch (alignmentAdjust.getEnum()) {
         case 4:
         case 6:
         case 14:
         case 24:
         case 56:
         case 59:
         case 82:
         case 84:
         case 141:
         case 142:
            this.alignmentPoint = beforeEdge - this.actualBaselineTable.getBaseline(alignmentAdjust.getEnum());
            break;
         case 9:
            this.alignmentPoint = beforeEdge - this.actualBaselineTable.getBaseline(this.alignmentBaselineIdentifier);
            break;
         case 12:
            this.alignmentPoint = beforeEdge;
            break;
         default:
            this.alignmentPoint = beforeEdge + alignmentAdjust.getValue(new SimplePercentBaseContext((PercentBaseContext)null, 12, this.lineHeight));
      }

   }

   private ScaledBaselineTable getScaledBaselineTable() {
      return this.scaledBaselineTable;
   }

   public int getDominantBaselineIdentifier() {
      return this.actualBaselineTable.getDominantBaselineIdentifier();
   }

   private void setBaselineShift(Length baselineShift) {
      this.baselineShiftValue = 0;
      switch (baselineShift.getEnum()) {
         case 0:
            this.baselineShiftValue = baselineShift.getValue(new SimplePercentBaseContext((PercentBaseContext)null, 0, this.parentAlignmentContext.getLineHeight()));
         case 12:
            break;
         case 137:
            this.baselineShiftValue = Math.round(-((float)this.xHeight / 2.0F) + (float)this.parentAlignmentContext.getActualBaselineOffset(6));
            break;
         case 138:
            this.baselineShiftValue = Math.round((float)this.parentAlignmentContext.getXHeight() + (float)this.parentAlignmentContext.getActualBaselineOffset(6));
            break;
         default:
            throw new IllegalArgumentException(String.valueOf(baselineShift.getEnum()));
      }

   }

   public AlignmentContext getParentAlignmentContext() {
      return this.parentAlignmentContext;
   }

   private int getBaselineOffset() {
      return this.parentAlignmentContext == null ? 0 : this.parentAlignmentContext.getScaledBaselineTable().getBaseline(this.alignmentBaselineIdentifier) - this.scaledBaselineTable.deriveScaledBaselineTable(this.parentAlignmentContext.getDominantBaselineIdentifier()).getBaseline(this.alignmentBaselineIdentifier) - this.scaledBaselineTable.getBaseline(this.parentAlignmentContext.getDominantBaselineIdentifier()) + this.baselineShiftValue;
   }

   private int getTotalBaselineOffset() {
      int offset = 0;
      if (this.parentAlignmentContext != null) {
         offset = this.getBaselineOffset() + this.parentAlignmentContext.getTotalBaselineOffset();
      }

      return offset;
   }

   public int getTotalAlignmentBaselineOffset() {
      return this.getTotalAlignmentBaselineOffset(this.alignmentBaselineIdentifier);
   }

   private int getTotalAlignmentBaselineOffset(int alignmentBaselineId) {
      int offset = this.baselineShiftValue;
      if (this.parentAlignmentContext != null) {
         offset = this.parentAlignmentContext.getTotalBaselineOffset() + this.parentAlignmentContext.getScaledBaselineTable().getBaseline(alignmentBaselineId) + this.baselineShiftValue;
      }

      return offset;
   }

   private int getActualBaselineOffset(int baselineIdentifier) {
      int offset = this.getTotalAlignmentBaselineOffset() - this.getTotalBaselineOffset();
      offset += this.actualBaselineTable.deriveScaledBaselineTable(this.alignmentBaselineIdentifier).getBaseline(baselineIdentifier);
      return offset;
   }

   private int getTotalTopOffset() {
      int offset = this.getTotalAlignmentBaselineOffset() + this.getAltitude();
      return offset;
   }

   public int getHeight() {
      return this.areaHeight;
   }

   private int getLineHeight() {
      return this.lineHeight;
   }

   public int getAltitude() {
      return this.alignmentPoint;
   }

   public int getDepth() {
      return this.getHeight() - this.alignmentPoint;
   }

   private int getXHeight() {
      return this.xHeight;
   }

   public void resizeLine(int newLineHeight, int newAlignmentPoint) {
      this.areaHeight = newLineHeight;
      this.alignmentPoint = newAlignmentPoint;
      this.scaledBaselineTable.setBeforeAndAfterBaselines(this.alignmentPoint, this.alignmentPoint - this.areaHeight);
   }

   public int getOffset() {
      int offset = false;
      int offset;
      if (this.parentAlignmentContext != null) {
         offset = this.parentAlignmentContext.getTotalTopOffset() - this.getTotalTopOffset();
      } else {
         offset = this.getAltitude() - this.scaledBaselineTable.getBaseline(142);
      }

      return offset;
   }

   public boolean usesInitialBaselineTable() {
      return this.parentAlignmentContext == null || this.scaledBaselineTable == this.parentAlignmentContext.getScaledBaselineTable() && this.parentAlignmentContext.usesInitialBaselineTable();
   }

   public String toString() {
      StringBuffer sb = new StringBuffer(64);
      sb.append("areaHeight=").append(this.areaHeight);
      sb.append(" lineHeight=").append(this.lineHeight);
      sb.append(" alignmentPoint=").append(this.alignmentPoint);
      sb.append(" alignmentBaselineID=").append(this.alignmentBaselineIdentifier);
      sb.append(" baselineShift=").append(this.baselineShiftValue);
      return sb.toString();
   }
}
