package org.apache.fop.fo.flow;

import java.util.Stack;
import org.apache.fop.accessibility.StructureTreeElement;
import org.apache.fop.apps.FOPException;
import org.apache.fop.complexscripts.bidi.DelimitedTextRange;
import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.GraphicsProperties;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.properties.CommonAccessibility;
import org.apache.fop.fo.properties.CommonAccessibilityHolder;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.KeepProperty;
import org.apache.fop.fo.properties.LengthRangeProperty;
import org.apache.fop.fo.properties.SpaceProperty;
import org.apache.fop.fo.properties.StructureTreeElementHolder;

public abstract class AbstractGraphics extends FObj implements GraphicsProperties, StructureTreeElementHolder, CommonAccessibilityHolder {
   private CommonAccessibility commonAccessibility;
   private CommonBorderPaddingBackground commonBorderPaddingBackground;
   private Length alignmentAdjust;
   private int alignmentBaseline;
   private Length baselineShift;
   private LengthRangeProperty blockProgressionDimension;
   private Length contentHeight;
   private Length contentWidth;
   private int displayAlign;
   private int dominantBaseline;
   private Length height;
   private LengthRangeProperty inlineProgressionDimension;
   private KeepProperty keepWithNext;
   private KeepProperty keepWithPrevious;
   private SpaceProperty lineHeight;
   private int overflow;
   private int scaling;
   private int textAlign;
   private Length width;
   private String altText;
   private StructureTreeElement structureTreeElement;

   public AbstractGraphics(FONode parent) {
      super(parent);
   }

   public void bind(PropertyList pList) throws FOPException {
      super.bind(pList);
      this.commonAccessibility = CommonAccessibility.getInstance(pList);
      this.commonBorderPaddingBackground = pList.getBorderPaddingBackgroundProps();
      this.alignmentAdjust = pList.get(3).getLength();
      this.alignmentBaseline = pList.get(4).getEnum();
      this.baselineShift = pList.get(15).getLength();
      this.blockProgressionDimension = pList.get(17).getLengthRange();
      this.contentHeight = pList.get(78).getLength();
      this.contentWidth = pList.get(80).getLength();
      this.displayAlign = pList.get(87).getEnum();
      this.dominantBaseline = pList.get(88).getEnum();
      this.height = pList.get(115).getLength();
      this.inlineProgressionDimension = pList.get(127).getLengthRange();
      this.keepWithNext = pList.get(132).getKeep();
      this.keepWithPrevious = pList.get(133).getKeep();
      this.lineHeight = pList.get(144).getSpace();
      this.overflow = pList.get(169).getEnum();
      this.scaling = pList.get(215).getEnum();
      this.textAlign = pList.get(245).getEnum();
      this.width = pList.get(264).getLength();
      if (this.getUserAgent().isAccessibilityEnabled()) {
         this.altText = pList.get(273).getString();
         if (this.altText.equals("")) {
            this.getFOValidationEventProducer().altTextMissing(this, this.getLocalName(), this.getLocator());
         }
      }

   }

   public CommonAccessibility getCommonAccessibility() {
      return this.commonAccessibility;
   }

   public CommonBorderPaddingBackground getCommonBorderPaddingBackground() {
      return this.commonBorderPaddingBackground;
   }

   public SpaceProperty getLineHeight() {
      return this.lineHeight;
   }

   public LengthRangeProperty getInlineProgressionDimension() {
      return this.inlineProgressionDimension;
   }

   public LengthRangeProperty getBlockProgressionDimension() {
      return this.blockProgressionDimension;
   }

   public Length getHeight() {
      return this.height;
   }

   public Length getWidth() {
      return this.width;
   }

   public Length getContentHeight() {
      return this.contentHeight;
   }

   public Length getContentWidth() {
      return this.contentWidth;
   }

   public int getScaling() {
      return this.scaling;
   }

   public int getOverflow() {
      return this.overflow;
   }

   public int getDisplayAlign() {
      return this.displayAlign;
   }

   public int getTextAlign() {
      return this.textAlign;
   }

   public Length getAlignmentAdjust() {
      if (this.alignmentAdjust.getEnum() == 9) {
         Length intrinsicAlignmentAdjust = this.getIntrinsicAlignmentAdjust();
         if (intrinsicAlignmentAdjust != null) {
            return intrinsicAlignmentAdjust;
         }
      }

      return this.alignmentAdjust;
   }

   public int getAlignmentBaseline() {
      return this.alignmentBaseline;
   }

   public Length getBaselineShift() {
      return this.baselineShift;
   }

   public int getDominantBaseline() {
      return this.dominantBaseline;
   }

   public KeepProperty getKeepWithNext() {
      return this.keepWithNext;
   }

   public KeepProperty getKeepWithPrevious() {
      return this.keepWithPrevious;
   }

   public void setStructureTreeElement(StructureTreeElement structureTreeElement) {
      this.structureTreeElement = structureTreeElement;
   }

   public StructureTreeElement getStructureTreeElement() {
      return this.structureTreeElement;
   }

   public String getAltText() {
      return this.altText;
   }

   public abstract int getIntrinsicWidth();

   public abstract int getIntrinsicHeight();

   public abstract Length getIntrinsicAlignmentAdjust();

   public boolean isDelimitedTextRangeBoundary(int boundary) {
      return false;
   }

   protected Stack collectDelimitedTextRanges(Stack ranges, DelimitedTextRange currentRange) {
      if (currentRange != null) {
         currentRange.append('￼', this);
      }

      return ranges;
   }
}
