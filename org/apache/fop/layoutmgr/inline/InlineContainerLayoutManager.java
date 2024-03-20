package org.apache.fop.layoutmgr.inline;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.fop.area.Area;
import org.apache.fop.area.Trait;
import org.apache.fop.area.inline.Container;
import org.apache.fop.area.inline.InlineViewport;
import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.PercentBaseContext;
import org.apache.fop.datatypes.SimplePercentBaseContext;
import org.apache.fop.fo.flow.InlineContainer;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.LengthRangeProperty;
import org.apache.fop.fo.properties.Property;
import org.apache.fop.layoutmgr.AbstractLayoutManager;
import org.apache.fop.layoutmgr.AreaAdditionUtil;
import org.apache.fop.layoutmgr.BlockLevelEventProducer;
import org.apache.fop.layoutmgr.ElementListUtils;
import org.apache.fop.layoutmgr.InlineKnuthSequence;
import org.apache.fop.layoutmgr.KnuthPossPosIter;
import org.apache.fop.layoutmgr.KnuthSequence;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.layoutmgr.ListElement;
import org.apache.fop.layoutmgr.NonLeafPosition;
import org.apache.fop.layoutmgr.Position;
import org.apache.fop.layoutmgr.PositionIterator;
import org.apache.fop.layoutmgr.SpaceResolver;
import org.apache.fop.layoutmgr.TraitSetter;

public class InlineContainerLayoutManager extends AbstractLayoutManager implements InlineLevelLayoutManager {
   private CommonBorderPaddingBackground borderProps;
   private int contentAreaIPD;
   private int contentAreaBPD;
   private List childElements;
   private int ipdOverflow;
   private AlignmentContext alignmentContext;
   private InlineViewport currentViewport;
   private Container referenceArea;

   public InlineContainerLayoutManager(InlineContainer node) {
      super(node);
      this.setGeneratesReferenceArea(true);
   }

   public void initialize() {
      InlineContainer node = (InlineContainer)this.fobj;
      this.borderProps = node.getCommonBorderPaddingBackground();
   }

   private InlineContainer getInlineContainer() {
      assert this.fobj instanceof InlineContainer;

      return (InlineContainer)this.fobj;
   }

   public List getNextKnuthElements(LayoutContext context, int alignment) {
      this.determineIPD(context);
      this.childElements = this.getChildKnuthElements(context, alignment);
      this.determineBPD();
      this.alignmentContext = this.makeAlignmentContext(context);
      Position position = new Position(this, 0);
      KnuthSequence knuthSequence = new InlineKnuthSequence();
      knuthSequence.add(new KnuthInlineBox(this.contentAreaIPD, this.alignmentContext, position, false));
      List knuthElements = new ArrayList(1);
      knuthElements.add(knuthSequence);
      this.setFinished(true);
      return knuthElements;
   }

   private void determineIPD(LayoutContext layoutContext) {
      LengthRangeProperty ipd = this.getInlineContainer().getInlineProgressionDimension();
      Property optimum = ipd.getOptimum(this);
      if (optimum.isAuto()) {
         this.contentAreaIPD = layoutContext.getRefIPD();
         InlineLevelEventProducer eventProducer = InlineLevelEventProducer.Provider.get(this.fobj.getUserAgent().getEventBroadcaster());
         eventProducer.inlineContainerAutoIPDNotSupported(this, (float)this.contentAreaIPD / 1000.0F);
      } else {
         this.contentAreaIPD = optimum.getLength().getValue(this);
      }

   }

   private List getChildKnuthElements(LayoutContext layoutContext, int alignment) {
      List allChildElements = new LinkedList();

      LayoutManager childLM;
      while((childLM = this.getChildLM()) != null) {
         LayoutContext childLC = LayoutContext.offspringOf(layoutContext);
         childLC.setRefIPD(this.contentAreaIPD);
         List childElements = childLM.getNextKnuthElements(childLC, alignment);
         allChildElements.addAll(childElements);
      }

      this.handleIPDOverflow();
      this.wrapPositions(allChildElements);
      SpaceResolver.resolveElementList(allChildElements);
      SpaceResolver.performConditionalsNotification(allChildElements, 0, allChildElements.size() - 1, -1);
      return allChildElements;
   }

   private void determineBPD() {
      LengthRangeProperty bpd = this.getInlineContainer().getBlockProgressionDimension();
      Property optimum = bpd.getOptimum(this);
      int actualBPD = ElementListUtils.calcContentLength(this.childElements);
      if (optimum.isAuto()) {
         this.contentAreaBPD = actualBPD;
      } else {
         double bpdValue = optimum.getLength().getNumericValue(this);
         if (bpdValue < 0.0) {
            this.contentAreaBPD = actualBPD;
         } else {
            this.contentAreaBPD = (int)Math.round(bpdValue);
            if (this.contentAreaBPD < actualBPD) {
               BlockLevelEventProducer eventProducer = this.getBlockLevelEventProducer();
               eventProducer.viewportBPDOverflow(this, this.fobj.getName(), actualBPD - this.contentAreaBPD, this.needClip(), this.canRecoverFromOverflow(), this.fobj.getLocator());
            }
         }
      }

   }

   protected AlignmentContext makeAlignmentContext(LayoutContext context) {
      InlineContainer ic = (InlineContainer)this.fobj;
      AlignmentContext ac = new AlignmentContext(this.contentAreaBPD, ic.getAlignmentAdjust(), ic.getAlignmentBaseline(), ic.getBaselineShift(), ic.getDominantBaseline(), context.getAlignmentContext());
      int baselineOffset = this.getAlignmentPoint(ac.getDominantBaselineIdentifier());
      ac.resizeLine(this.contentAreaBPD, baselineOffset);
      return ac;
   }

   private void handleIPDOverflow() {
      if (this.ipdOverflow > 0) {
         BlockLevelEventProducer eventProducer = this.getBlockLevelEventProducer();
         eventProducer.viewportIPDOverflow(this, this.fobj.getName(), this.ipdOverflow, this.needClip(), this.canRecoverFromOverflow(), this.fobj.getLocator());
      }

   }

   private void wrapPositions(List elements) {
      Iterator var2 = elements.iterator();

      while(var2.hasNext()) {
         ListElement element = (ListElement)var2.next();
         Position position = new NonLeafPosition(this, element.getPosition());
         this.notifyPos(position);
         element.setPosition(position);
      }

   }

   private BlockLevelEventProducer getBlockLevelEventProducer() {
      return BlockLevelEventProducer.Provider.get(this.fobj.getUserAgent().getEventBroadcaster());
   }

   private boolean canRecoverFromOverflow() {
      return this.getInlineContainer().getOverflow() != 42;
   }

   private int getAlignmentPoint(int dominantBaseline) {
      Length alignmentAdjust = this.getInlineContainer().getAlignmentAdjust();
      int baseline = alignmentAdjust.getEnum();
      if (baseline == 9) {
         return this.getInlineContainerBaselineOffset(this.getInlineContainer().getAlignmentBaseline());
      } else if (baseline == 12) {
         return this.getInlineContainerBaselineOffset(dominantBaseline);
      } else if (baseline != 0) {
         return this.getInlineContainerBaselineOffset(baseline);
      } else {
         int baselineOffset = this.getInlineContainerBaselineOffset(dominantBaseline);
         int lineHeight = this.getInlineContainer().getLineHeight().getOptimum(this).getLength().getValue(this);
         int adjust = alignmentAdjust.getValue(new SimplePercentBaseContext((PercentBaseContext)null, 12, lineHeight));
         return baselineOffset + adjust;
      }
   }

   private int getInlineContainerBaselineOffset(int property) {
      switch (property) {
         case 4:
         case 141:
            return this.contentAreaBPD;
         case 6:
            return this.contentAreaBPD * 6 / 10;
         case 9:
         case 12:
            return this.hasLineAreaDescendant() ? this.getBaselineOffset() : this.contentAreaBPD;
         case 14:
         case 142:
            return 0;
         case 24:
         case 82:
         case 84:
            return this.contentAreaBPD / 2;
         case 56:
            return this.contentAreaBPD * 2 / 10;
         case 59:
            return this.contentAreaBPD * 7 / 10;
         default:
            throw new AssertionError("Unknown baseline value: " + property);
      }
   }

   public void addAreas(PositionIterator posIter, LayoutContext context) {
      Position inlineContainerPosition = null;

      do {
         if (!posIter.hasNext()) {
            assert inlineContainerPosition != null;

            KnuthPossPosIter childPosIter = new KnuthPossPosIter(this.childElements);
            AreaAdditionUtil.addAreas(this, childPosIter, context);
            return;
         }

         assert inlineContainerPosition == null;

         inlineContainerPosition = posIter.next();
      } while($assertionsDisabled || inlineContainerPosition.getLM() == this);

      throw new AssertionError();
   }

   public Area getParentArea(Area childArea) {
      if (this.referenceArea == null) {
         this.referenceArea = new Container();
         this.referenceArea.addTrait(Trait.IS_REFERENCE_AREA, Boolean.TRUE);
         TraitSetter.setProducerID(this.referenceArea, this.fobj.getId());
         this.referenceArea.setIPD(this.contentAreaIPD);
         this.currentViewport = new InlineViewport(this.referenceArea);
         this.currentViewport.addTrait(Trait.IS_VIEWPORT_AREA, Boolean.TRUE);
         TraitSetter.setProducerID(this.currentViewport, this.fobj.getId());
         this.currentViewport.setBlockProgressionOffset(this.alignmentContext.getOffset());
         this.currentViewport.setIPD(this.getContentAreaIPD());
         this.currentViewport.setBPD(this.getContentAreaBPD());
         TraitSetter.addBackground(this.currentViewport, this.borderProps, this);
         this.currentViewport.setClip(this.needClip());
         this.currentViewport.setContentPosition(new Rectangle2D.Float(0.0F, 0.0F, (float)this.getContentAreaIPD(), (float)this.getContentAreaBPD()));
         this.getParent().addChildArea(this.currentViewport);
      }

      return this.referenceArea;
   }

   public int getContentAreaIPD() {
      return this.contentAreaIPD;
   }

   public int getContentAreaBPD() {
      return this.contentAreaBPD;
   }

   public void addChildArea(Area childArea) {
      this.referenceArea.addChildArea(childArea);
   }

   private boolean needClip() {
      int overflow = this.getInlineContainer().getOverflow();
      return overflow == 57 || overflow == 42;
   }

   public boolean handleOverflow(int milliPoints) {
      this.ipdOverflow = Math.max(this.ipdOverflow, milliPoints);
      return true;
   }

   public List addALetterSpaceTo(List oldList) {
      return oldList;
   }

   public List addALetterSpaceTo(List oldList, int depth) {
      return oldList;
   }

   public String getWordChars(Position pos) {
      return "";
   }

   public void hyphenate(Position pos, HyphContext hyphContext) {
   }

   public boolean applyChanges(List oldList) {
      return false;
   }

   public boolean applyChanges(List oldList, int depth) {
      return false;
   }

   public List getChangedKnuthElements(List oldList, int alignment, int depth) {
      return oldList;
   }
}
