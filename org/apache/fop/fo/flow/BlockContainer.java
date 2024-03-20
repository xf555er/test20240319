package org.apache.fop.fo.flow;

import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.properties.BreakPropertySet;
import org.apache.fop.fo.properties.CommonAbsolutePosition;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.CommonMarginBlock;
import org.apache.fop.fo.properties.KeepProperty;
import org.apache.fop.fo.properties.LengthRangeProperty;
import org.apache.fop.traits.Direction;
import org.apache.fop.traits.WritingMode;
import org.apache.fop.traits.WritingModeTraits;
import org.apache.fop.traits.WritingModeTraitsGetter;
import org.xml.sax.Locator;

public class BlockContainer extends FObj implements BreakPropertySet, WritingModeTraitsGetter {
   private CommonAbsolutePosition commonAbsolutePosition;
   private CommonBorderPaddingBackground commonBorderPaddingBackground;
   private CommonMarginBlock commonMarginBlock;
   private LengthRangeProperty blockProgressionDimension;
   private int breakAfter;
   private int breakBefore;
   private int displayAlign;
   private LengthRangeProperty inlineProgressionDimension;
   private KeepProperty keepTogether;
   private KeepProperty keepWithNext;
   private KeepProperty keepWithPrevious;
   private int overflow;
   private Numeric referenceOrientation;
   private int span;
   private int disableColumnBalancing;
   private WritingModeTraits writingModeTraits;
   private boolean blockItemFound;

   public BlockContainer(FONode parent) {
      super(parent);
   }

   public void bind(PropertyList pList) throws FOPException {
      super.bind(pList);
      this.commonAbsolutePosition = pList.getAbsolutePositionProps();
      this.commonBorderPaddingBackground = pList.getBorderPaddingBackgroundProps();
      this.commonMarginBlock = pList.getMarginBlockProps();
      this.blockProgressionDimension = pList.get(17).getLengthRange();
      this.breakAfter = pList.get(58).getEnum();
      this.breakBefore = pList.get(59).getEnum();
      this.displayAlign = pList.get(87).getEnum();
      this.inlineProgressionDimension = pList.get(127).getLengthRange();
      this.keepTogether = pList.get(131).getKeep();
      this.keepWithNext = pList.get(132).getKeep();
      this.keepWithPrevious = pList.get(133).getKeep();
      this.overflow = pList.get(169).getEnum();
      this.referenceOrientation = pList.get(197).getNumeric();
      this.span = pList.get(226).getEnum();
      this.writingModeTraits = new WritingModeTraits(WritingMode.valueOf(pList.get(267).getEnum()), pList.getExplicit(267) != null);
      this.disableColumnBalancing = pList.get(272).getEnum();
   }

   public void startOfNode() throws FOPException {
      super.startOfNode();
      this.getFOEventHandler().startBlockContainer(this);
   }

   protected void validateChildNode(Locator loc, String nsURI, String localName) throws ValidationException {
      if ("http://www.w3.org/1999/XSL/Format".equals(nsURI)) {
         if ("marker".equals(localName)) {
            if (this.commonAbsolutePosition.absolutePosition == 1 || this.commonAbsolutePosition.absolutePosition == 51) {
               this.getFOValidationEventProducer().markerBlockContainerAbsolutePosition(this, this.locator);
            }

            if (this.blockItemFound) {
               this.nodesOutOfOrderError(loc, "fo:marker", "(%block;)");
            }
         } else if (!this.isBlockItem("http://www.w3.org/1999/XSL/Format", localName)) {
            this.invalidChildError(loc, "http://www.w3.org/1999/XSL/Format", localName);
         } else {
            this.blockItemFound = true;
         }
      }

   }

   public void endOfNode() throws FOPException {
      if (!this.blockItemFound) {
         this.missingChildElementError("marker* (%block;)+");
      }

      this.getFOEventHandler().endBlockContainer(this);
   }

   public boolean generatesReferenceAreas() {
      return true;
   }

   public CommonAbsolutePosition getCommonAbsolutePosition() {
      return this.commonAbsolutePosition;
   }

   public CommonMarginBlock getCommonMarginBlock() {
      return this.commonMarginBlock;
   }

   public CommonBorderPaddingBackground getCommonBorderPaddingBackground() {
      return this.commonBorderPaddingBackground;
   }

   public LengthRangeProperty getBlockProgressionDimension() {
      return this.blockProgressionDimension;
   }

   public int getDisplayAlign() {
      return this.displayAlign;
   }

   public int getBreakAfter() {
      return this.breakAfter;
   }

   public int getBreakBefore() {
      return this.breakBefore;
   }

   public KeepProperty getKeepWithNext() {
      return this.keepWithNext;
   }

   public KeepProperty getKeepWithPrevious() {
      return this.keepWithPrevious;
   }

   public KeepProperty getKeepTogether() {
      return this.keepTogether;
   }

   public LengthRangeProperty getInlineProgressionDimension() {
      return this.inlineProgressionDimension;
   }

   public int getOverflow() {
      return this.overflow;
   }

   public int getReferenceOrientation() {
      return this.referenceOrientation.getValue();
   }

   public int getSpan() {
      return this.span;
   }

   public int getDisableColumnBalancing() {
      return this.disableColumnBalancing;
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

   public WritingMode getWritingMode() {
      return this.writingModeTraits.getWritingMode();
   }

   public boolean getExplicitWritingMode() {
      return this.writingModeTraits.getExplicitWritingMode();
   }

   public String getLocalName() {
      return "block-container";
   }

   public int getNameId() {
      return 4;
   }

   protected boolean isBidiBoundary(boolean propagate) {
      return this.getExplicitWritingMode();
   }
}
