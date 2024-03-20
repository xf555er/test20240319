package org.apache.fop.fo.flow;

import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.CommonMarginInline;
import org.apache.fop.fo.properties.KeepProperty;
import org.apache.fop.fo.properties.LengthRangeProperty;
import org.apache.fop.fo.properties.SpaceProperty;
import org.apache.fop.traits.Direction;
import org.apache.fop.traits.WritingMode;
import org.apache.fop.traits.WritingModeTraits;
import org.xml.sax.Locator;

public class InlineContainer extends FObj {
   private LengthRangeProperty inlineProgressionDimension;
   private LengthRangeProperty blockProgressionDimension;
   private int overflow;
   private CommonBorderPaddingBackground commonBorderPaddingBackground;
   private CommonMarginInline commonMarginInline;
   private Numeric referenceOrientation;
   private int displayAlign;
   private KeepProperty keepTogether;
   private KeepProperty keepWithNext;
   private KeepProperty keepWithPrevious;
   private SpaceProperty lineHeight;
   private Length alignmentAdjust;
   private int alignmentBaseline;
   private Length baselineShift;
   private int dominantBaseline;
   private WritingModeTraits writingModeTraits;
   private boolean blockItemFound;

   public InlineContainer(FONode parent) {
      super(parent);
   }

   public void bind(PropertyList pList) throws FOPException {
      super.bind(pList);
      this.alignmentAdjust = pList.get(3).getLength();
      this.alignmentBaseline = pList.get(4).getEnum();
      this.baselineShift = pList.get(15).getLength();
      this.blockProgressionDimension = pList.get(17).getLengthRange();
      this.commonBorderPaddingBackground = pList.getBorderPaddingBackgroundProps();
      this.commonMarginInline = pList.getMarginInlineProps();
      this.displayAlign = pList.get(87).getEnum();
      this.dominantBaseline = pList.get(88).getEnum();
      this.inlineProgressionDimension = pList.get(127).getLengthRange();
      this.keepTogether = pList.get(131).getKeep();
      this.keepWithNext = pList.get(132).getKeep();
      this.keepWithPrevious = pList.get(133).getKeep();
      this.lineHeight = pList.get(144).getSpace();
      this.overflow = pList.get(169).getEnum();
      this.referenceOrientation = pList.get(197).getNumeric();
      this.writingModeTraits = new WritingModeTraits(WritingMode.valueOf(pList.get(267).getEnum()), pList.getExplicit(267) != null);
   }

   protected void validateChildNode(Locator loc, String nsURI, String localName) throws ValidationException {
      if ("http://www.w3.org/1999/XSL/Format".equals(nsURI)) {
         if (localName.equals("marker")) {
            if (this.blockItemFound) {
               this.nodesOutOfOrderError(loc, "fo:marker", "(%block;)+");
            }
         } else if (!this.isBlockItem(nsURI, localName)) {
            this.invalidChildError(loc, nsURI, localName);
         } else {
            this.blockItemFound = true;
         }
      }

   }

   public void endOfNode() throws FOPException {
      if (!this.blockItemFound) {
         this.missingChildElementError("marker* (%block;)+");
      }

   }

   public String getLocalName() {
      return "inline-container";
   }

   public int getNameId() {
      return 36;
   }

   public LengthRangeProperty getInlineProgressionDimension() {
      return this.inlineProgressionDimension;
   }

   public LengthRangeProperty getBlockProgressionDimension() {
      return this.blockProgressionDimension;
   }

   public int getOverflow() {
      return this.overflow;
   }

   public CommonBorderPaddingBackground getCommonBorderPaddingBackground() {
      return this.commonBorderPaddingBackground;
   }

   public CommonMarginInline getCommonMarginInline() {
      return this.commonMarginInline;
   }

   public int getReferenceOrientation() {
      return this.referenceOrientation.getValue();
   }

   public int getDisplayAlign() {
      return this.displayAlign;
   }

   public KeepProperty getKeepWithPrevious() {
      return this.keepWithPrevious;
   }

   public KeepProperty getKeepTogether() {
      return this.keepTogether;
   }

   public KeepProperty getKeepWithNext() {
      return this.keepWithNext;
   }

   public SpaceProperty getLineHeight() {
      return this.lineHeight;
   }

   public Length getAlignmentAdjust() {
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

   public WritingMode getWritingMode() {
      return this.writingModeTraits.getWritingMode();
   }

   public boolean getExplicitWritingMode() {
      return this.writingModeTraits.getExplicitWritingMode();
   }

   public Direction getInlineProgressionDirection() {
      return this.writingModeTraits.getInlineProgressionDirection();
   }

   public Direction getBlockProgressionDirection() {
      return this.writingModeTraits.getBlockProgressionDirection();
   }

   public Direction getColumnProgressionDirection() {
      return this.writingModeTraits.getColumnProgressionDirection();
   }

   public Direction getRowProgressionDirection() {
      return this.writingModeTraits.getRowProgressionDirection();
   }

   public Direction getShiftDirection() {
      return this.writingModeTraits.getShiftDirection();
   }

   public boolean isDelimitedTextRangeBoundary(int boundary) {
      return false;
   }

   public boolean generatesReferenceAreas() {
      return true;
   }

   protected boolean isBidiBoundary(boolean propagate) {
      return this.getExplicitWritingMode();
   }
}
