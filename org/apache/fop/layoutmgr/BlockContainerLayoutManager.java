package org.apache.fop.layoutmgr;

import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.area.Area;
import org.apache.fop.area.Block;
import org.apache.fop.area.BlockViewport;
import org.apache.fop.area.CTM;
import org.apache.fop.area.Trait;
import org.apache.fop.datatypes.FODimension;
import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.flow.BlockContainer;
import org.apache.fop.fo.properties.CommonAbsolutePosition;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.KeepProperty;
import org.apache.fop.traits.MinOptMax;
import org.apache.fop.traits.SpaceVal;

public class BlockContainerLayoutManager extends SpacedBorderedPaddedBlockLayoutManager implements BreakOpportunity {
   private static Log log = LogFactory.getLog(BlockContainerLayoutManager.class);
   private BlockViewport viewportBlockArea;
   private Block referenceArea;
   private CommonAbsolutePosition abProps;
   private FODimension relDims;
   private CTM absoluteCTM;
   private Length width;
   private Length height;
   private int vpContentBPD;
   private boolean autoHeight = true;
   private boolean inlineElementList;
   private MinOptMax foBlockSpaceBefore;
   private MinOptMax foBlockSpaceAfter;
   private int horizontalOverflow;
   private double contentRectOffsetX;
   private double contentRectOffsetY;

   public BlockContainerLayoutManager(BlockContainer node) {
      super(node);
      this.setGeneratesBlockArea(true);
   }

   public void initialize() {
      this.abProps = this.getBlockContainerFO().getCommonAbsolutePosition();
      this.foBlockSpaceBefore = (new SpaceVal(this.getBlockContainerFO().getCommonMarginBlock().spaceBefore, this)).getSpace();
      this.foBlockSpaceAfter = (new SpaceVal(this.getBlockContainerFO().getCommonMarginBlock().spaceAfter, this)).getSpace();
      this.startIndent = this.getBlockContainerFO().getCommonMarginBlock().startIndent.getValue(this);
      this.endIndent = this.getBlockContainerFO().getCommonMarginBlock().endIndent.getValue(this);
      if (this.blockProgressionDirectionChanges()) {
         this.height = this.getBlockContainerFO().getInlineProgressionDimension().getOptimum(this).getLength();
         this.width = this.getBlockContainerFO().getBlockProgressionDimension().getOptimum(this).getLength();
      } else {
         this.height = this.getBlockContainerFO().getBlockProgressionDimension().getOptimum(this).getLength();
         this.width = this.getBlockContainerFO().getInlineProgressionDimension().getOptimum(this).getLength();
      }

      this.adjustedSpaceBefore = this.getBlockContainerFO().getCommonMarginBlock().spaceBefore.getSpace().getOptimum(this).getLength().getValue(this);
      this.adjustedSpaceAfter = this.getBlockContainerFO().getCommonMarginBlock().spaceAfter.getSpace().getOptimum(this).getLength().getValue(this);
   }

   protected CommonBorderPaddingBackground getCommonBorderPaddingBackground() {
      return this.getBlockContainerFO().getCommonBorderPaddingBackground();
   }

   private void resetSpaces() {
      this.discardBorderBefore = false;
      this.discardBorderAfter = false;
      this.discardPaddingBefore = false;
      this.discardPaddingAfter = false;
      this.effSpaceBefore = null;
      this.effSpaceAfter = null;
   }

   protected int getRotatedIPD() {
      return this.getBlockContainerFO().getInlineProgressionDimension().getOptimum(this).getLength().getValue(this);
   }

   private boolean needClip() {
      int overflow = this.getBlockContainerFO().getOverflow();
      return overflow == 57 || overflow == 42;
   }

   private int getBPIndents() {
      int indents = 0;
      indents += this.getBlockContainerFO().getCommonBorderPaddingBackground().getBPPaddingAndBorder(false, this);
      return indents;
   }

   protected boolean isAbsoluteOrFixed() {
      return this.abProps.absolutePosition == 1 || this.abProps.absolutePosition == 51;
   }

   private boolean isFixed() {
      return this.abProps.absolutePosition == 51;
   }

   public int getContentAreaBPD() {
      return this.autoHeight ? -1 : this.vpContentBPD;
   }

   public List getNextKnuthElements(LayoutContext context, int alignment) {
      return this.getNextKnuthElements(context, alignment, (Stack)null, (Position)null, (LayoutManager)null);
   }

   protected LayoutContext makeChildLayoutContext(LayoutContext context) {
      LayoutContext childLC = LayoutContext.newInstance();
      childLC.setStackLimitBP(context.getStackLimitBP().minus(MinOptMax.getInstance(this.relDims.bpd)));
      childLC.setRefIPD(this.relDims.ipd);
      childLC.setWritingMode(this.getBlockContainerFO().getWritingMode());
      return childLC;
   }

   public List getNextKnuthElements(LayoutContext context, int alignment, Stack lmStack, Position restartPosition, LayoutManager restartAtLM) {
      this.resetSpaces();
      if (this.isAbsoluteOrFixed()) {
         return this.getNextKnuthElementsAbsolute(context);
      } else {
         boolean isRestart = lmStack != null;
         boolean emptyStack = !isRestart || lmStack.isEmpty();
         this.setupAreaDimensions(context);
         List contentList = new LinkedList();
         List returnList = new LinkedList();
         if (!this.breakBeforeServed(context, returnList)) {
            return returnList;
         } else {
            this.addFirstVisibleMarks(returnList, context, alignment);
            if (this.autoHeight && this.inlineElementList) {
               LayoutManager prevLM = null;
               LayoutManager curLM;
               if (!isRestart) {
                  curLM = this.getChildLM();
               } else {
                  if (emptyStack) {
                     assert restartAtLM != null && restartAtLM.getParent() == this;

                     curLM = restartAtLM;
                  } else {
                     curLM = (LayoutManager)lmStack.pop();
                  }

                  this.setCurrentChildLM(curLM);
               }

               while(true) {
                  if (curLM == null) {
                     this.wrapPositionElements(contentList, returnList);
                     break;
                  }

                  LayoutContext childLC = this.makeChildLayoutContext(context);
                  List returnedList;
                  if (isRestart && !emptyStack) {
                     returnedList = this.getNextChildElements(curLM, context, childLC, alignment, lmStack, restartPosition, restartAtLM);
                     emptyStack = true;
                  } else {
                     if (isRestart) {
                        curLM.reset();
                     }

                     returnedList = this.getNextChildElements(curLM, context, childLC, alignment, (Stack)null, (Position)null, (LayoutManager)null);
                  }

                  if (contentList.isEmpty() && childLC.isKeepWithPreviousPending()) {
                     context.updateKeepWithPreviousPending(childLC.getKeepWithPreviousPending());
                     childLC.clearKeepWithPreviousPending();
                  }

                  if (returnedList.size() == 1 && ElementListUtils.startsWithForcedBreak(returnedList)) {
                     contentList.addAll(returnedList);
                     this.wrapPositionElements(contentList, returnList);
                     return returnList;
                  }

                  if (prevLM != null) {
                     this.addInBetweenBreak(contentList, context, childLC);
                  }

                  contentList.addAll(returnedList);
                  if (!returnedList.isEmpty()) {
                     if (ElementListUtils.endsWithForcedBreak(returnedList)) {
                        if (curLM.isFinished() && !this.hasNextChildLM()) {
                           this.setFinished(true);
                        }

                        this.wrapPositionElements(contentList, returnList);
                        return returnList;
                     }

                     context.updateKeepWithNextPending(childLC.getKeepWithNextPending());
                     childLC.clearKeepsPending();
                     prevLM = curLM;
                     curLM = this.getChildLM();
                  }
               }
            } else {
               returnList.add(this.generateNonInlinedBox());
            }

            this.addLastVisibleMarks(returnList, context, alignment);
            this.addKnuthElementsForBreakAfter(returnList, context);
            context.updateKeepWithNextPending(this.getKeepWithNext());
            this.setFinished(true);
            return returnList;
         }
      }
   }

   private void setupAreaDimensions(LayoutContext context) {
      this.autoHeight = false;
      int maxbpd = context.getStackLimitBP().getOpt();
      BlockContainer fo = this.getBlockContainerFO();
      int allocBPD;
      if (this.height.getEnum() == 9 || !this.height.isAbsolute() && this.getAncestorBlockAreaBPD() <= 0) {
         allocBPD = maxbpd;
         this.autoHeight = true;
         this.inlineElementList = fo.getReferenceOrientation() == 0;
      } else {
         allocBPD = this.height.getValue(this);
         allocBPD += this.getBPIndents();
      }

      this.vpContentBPD = allocBPD - this.getBPIndents();
      this.referenceIPD = context.getRefIPD();
      int level;
      if (this.width.getEnum() == 9) {
         this.updateContentAreaIPDwithOverconstrainedAdjust();
      } else {
         level = this.width.getValue(this);
         this.updateContentAreaIPDwithOverconstrainedAdjust(level);
      }

      this.contentRectOffsetX = 0.0;
      this.contentRectOffsetY = 0.0;
      level = fo.getBidiLevel();
      if (level >= 0 && (level & 1) != 0) {
         this.contentRectOffsetX += (double)fo.getCommonMarginBlock().endIndent.getValue(this);
      } else {
         this.contentRectOffsetX += (double)fo.getCommonMarginBlock().startIndent.getValue(this);
      }

      this.contentRectOffsetY += (double)fo.getCommonBorderPaddingBackground().getBorderBeforeWidth(false);
      this.contentRectOffsetY += (double)fo.getCommonBorderPaddingBackground().getPaddingBefore(false, this);
      this.updateRelDims();
      int availableIPD = this.referenceIPD - this.getIPIndents();
      if (this.getContentAreaIPD() > availableIPD) {
         BlockLevelEventProducer eventProducer = BlockLevelEventProducer.Provider.get(fo.getUserAgent().getEventBroadcaster());
         eventProducer.objectTooWide(this, fo.getName(), this.getContentAreaIPD(), context.getRefIPD(), fo.getLocator());
      }

   }

   private KnuthBox generateNonInlinedBox() {
      MinOptMax range = MinOptMax.getInstance(this.relDims.ipd);
      BlockContainerBreaker breaker = new BlockContainerBreaker(this, range);
      breaker.doLayout(this.relDims.bpd, this.autoHeight);
      boolean contentOverflows = breaker.isOverflow();
      if (this.autoHeight) {
         int newHeight = breaker.deferredAlg.totalWidth;
         if (this.blockProgressionDirectionChanges()) {
            this.setContentAreaIPD(newHeight);
         } else {
            this.vpContentBPD = newHeight;
         }

         this.updateRelDims();
      }

      Position bcPosition = new BlockContainerPosition(this, breaker);
      KnuthBox knuthBox = new KnuthBox(this.vpContentBPD, this.notifyPos(bcPosition), false);
      if (contentOverflows) {
         BlockLevelEventProducer eventProducer = BlockLevelEventProducer.Provider.get(this.getBlockContainerFO().getUserAgent().getEventBroadcaster());
         boolean canRecover = this.getBlockContainerFO().getOverflow() != 42;
         eventProducer.viewportBPDOverflow(this, this.getBlockContainerFO().getName(), breaker.getOverflowAmount(), this.needClip(), canRecover, this.getBlockContainerFO().getLocator());
      }

      return knuthBox;
   }

   private boolean blockProgressionDirectionChanges() {
      return this.getBlockContainerFO().getReferenceOrientation() % 180 != 0;
   }

   public boolean isRestartable() {
      return true;
   }

   private List getNextKnuthElementsAbsolute(LayoutContext context) {
      this.autoHeight = false;
      boolean bpDirectionChanges = this.blockProgressionDirectionChanges();
      Point offset = this.getAbsOffset();
      int allocBPD;
      int availWidth;
      if (this.height.getEnum() == 9 || !this.height.isAbsolute() && this.getAncestorBlockAreaBPD() <= 0) {
         int allocBPD = false;
         if (this.abProps.bottom.getEnum() != 9) {
            if (this.isFixed()) {
               availWidth = (int)this.getCurrentPV().getViewArea().getHeight();
            } else {
               availWidth = context.getStackLimitBP().getOpt();
            }

            allocBPD = availWidth - offset.y;
            if (this.abProps.bottom.getEnum() != 9) {
               allocBPD -= this.abProps.bottom.getValue(this);
               if (allocBPD < 0) {
                  allocBPD = 0;
               }
            } else if (allocBPD < 0) {
               allocBPD = 0;
            }
         } else {
            allocBPD = context.getStackLimitBP().getOpt();
            if (!bpDirectionChanges) {
               this.autoHeight = true;
            }
         }
      } else {
         allocBPD = this.height.getValue(this);
         allocBPD += this.getBPIndents();
      }

      int allocIPD;
      if (this.width.getEnum() == 9) {
         if (this.isFixed()) {
            availWidth = (int)this.getCurrentPV().getViewArea().getWidth();
         } else {
            availWidth = context.getRefIPD();
         }

         allocIPD = availWidth;
         if (this.abProps.left.getEnum() != 9) {
            allocIPD = availWidth - this.abProps.left.getValue(this);
         }

         if (this.abProps.right.getEnum() != 9) {
            allocIPD -= this.abProps.right.getValue(this);
            if (allocIPD < 0) {
               allocIPD = 0;
            }
         } else {
            if (allocIPD < 0) {
               allocIPD = 0;
            }

            if (bpDirectionChanges) {
               this.autoHeight = true;
            }
         }
      } else {
         allocIPD = this.width.getValue(this);
         allocIPD += this.getIPIndents();
      }

      this.vpContentBPD = allocBPD - this.getBPIndents();
      this.setContentAreaIPD(allocIPD - this.getIPIndents());
      this.contentRectOffsetX = 0.0;
      this.contentRectOffsetY = 0.0;
      this.updateRelDims();
      MinOptMax range = MinOptMax.getInstance(this.relDims.ipd);
      BlockContainerBreaker breaker = new BlockContainerBreaker(this, range);
      breaker.doLayout(this.autoHeight ? 0 : this.relDims.bpd, this.autoHeight);
      boolean contentOverflows = breaker.isOverflow();
      if (this.autoHeight) {
         int newHeight = breaker.deferredAlg.totalWidth;
         if (bpDirectionChanges) {
            this.setContentAreaIPD(newHeight);
         } else {
            this.vpContentBPD = newHeight;
         }

         this.updateRelDims();
      }

      List returnList = new LinkedList();
      if (!breaker.isEmpty()) {
         Position bcPosition = new BlockContainerPosition(this, breaker);
         returnList.add(new KnuthBox(0, this.notifyPos(bcPosition), false));
         BlockLevelEventProducer eventProducer;
         boolean canRecover;
         if (!this.autoHeight & contentOverflows) {
            eventProducer = BlockLevelEventProducer.Provider.get(this.getBlockContainerFO().getUserAgent().getEventBroadcaster());
            canRecover = this.getBlockContainerFO().getOverflow() != 42;
            eventProducer.viewportBPDOverflow(this, this.getBlockContainerFO().getName(), breaker.getOverflowAmount(), this.needClip(), canRecover, this.getBlockContainerFO().getLocator());
         }

         if (this.horizontalOverflow > 0) {
            eventProducer = BlockLevelEventProducer.Provider.get(this.getBlockContainerFO().getUserAgent().getEventBroadcaster());
            canRecover = this.getBlockContainerFO().getOverflow() != 42;
            eventProducer.viewportIPDOverflow(this, this.getBlockContainerFO().getName(), this.horizontalOverflow, this.needClip(), canRecover, this.getBlockContainerFO().getLocator());
         }
      }

      this.setFinished(true);
      return returnList;
   }

   private void updateRelDims() {
      Rectangle2D rect = new Rectangle2D.Double(this.contentRectOffsetX, this.contentRectOffsetY, (double)this.getContentAreaIPD(), (double)this.vpContentBPD);
      this.relDims = new FODimension(0, 0);
      this.absoluteCTM = CTM.getCTMandRelDims(this.getBlockContainerFO().getReferenceOrientation(), this.getBlockContainerFO().getWritingMode(), rect, this.relDims);
   }

   private Point getAbsOffset() {
      int x = 0;
      int y = 0;
      if (this.abProps.left.getEnum() != 9) {
         x = this.abProps.left.getValue(this);
      } else if (this.abProps.right.getEnum() != 9 && this.width.getEnum() != 9) {
         x = this.getReferenceAreaIPD() - this.abProps.right.getValue(this) - this.width.getValue(this);
      }

      if (this.abProps.top.getEnum() != 9) {
         y = this.abProps.top.getValue(this);
      } else if (this.abProps.bottom.getEnum() != 9 && this.height.getEnum() != 9) {
         y = this.getReferenceAreaBPD() - this.abProps.bottom.getValue(this) - this.height.getValue(this);
      }

      return new Point(x, y);
   }

   public void addAreas(PositionIterator parentIter, LayoutContext layoutContext) {
      this.getParentArea((Area)null);
      if (layoutContext.getSpaceBefore() > 0) {
         this.addBlockSpacing(0.0, MinOptMax.getInstance(layoutContext.getSpaceBefore()));
      }

      LayoutManager lastLM = null;
      LayoutContext lc = LayoutContext.offspringOf(layoutContext);
      lc.setSpaceAdjust(layoutContext.getSpaceAdjust());
      if (layoutContext.getSpaceAfter() > 0) {
         lc.setSpaceAfter(layoutContext.getSpaceAfter());
      }

      BlockContainerPosition bcpos = null;
      List positionList = new LinkedList();
      Position firstPos = null;
      Position lastPos = null;

      while(true) {
         while(parentIter.hasNext()) {
            Position pos = parentIter.next();
            if (pos.getIndex() >= 0) {
               if (firstPos == null) {
                  firstPos = pos;
               }

               lastPos = pos;
            }

            Position innerPosition = pos;
            if (pos instanceof NonLeafPosition) {
               innerPosition = pos.getPosition();
            }

            if (pos instanceof BlockContainerPosition) {
               if (bcpos != null) {
                  throw new IllegalStateException("Only one BlockContainerPosition allowed");
               }

               bcpos = (BlockContainerPosition)pos;
            } else if (innerPosition != null && (innerPosition.getLM() != this || innerPosition instanceof BlockStackingLayoutManager.MappingPosition)) {
               positionList.add(innerPosition);
               lastLM = innerPosition.getLM();
            }
         }

         this.addId();
         this.registerMarkers(true, this.isFirst(firstPos), this.isLast(lastPos));
         if (bcpos == null) {
            PositionIterator childPosIter = new PositionIterator(positionList.listIterator());

            LayoutManager childLM;
            while((childLM = childPosIter.getNextChildLM()) != null) {
               lc.setFlags(8, layoutContext.isLastArea() && childLM == lastLM);
               lc.setStackLimitBP(layoutContext.getStackLimitBP());
               childLM.addAreas(childPosIter, lc);
            }
         } else {
            bcpos.getBreaker().addContainedAreas(layoutContext);
         }

         this.registerMarkers(false, this.isFirst(firstPos), this.isLast(lastPos));
         TraitSetter.addSpaceBeforeAfter(this.viewportBlockArea, layoutContext.getSpaceAdjust(), this.effSpaceBefore, this.effSpaceAfter);
         this.flush();
         this.viewportBlockArea = null;
         this.referenceArea = null;
         this.resetSpaces();
         this.notifyEndOfLayout();
         return;
      }
   }

   public Area getParentArea(Area childArea) {
      if (this.referenceArea == null) {
         boolean switchedProgressionDirection = this.blockProgressionDirectionChanges();
         boolean allowBPDUpdate = this.autoHeight && !switchedProgressionDirection;
         int level = this.getBlockContainerFO().getBidiLevel();
         this.viewportBlockArea = new BlockViewport(allowBPDUpdate);
         this.viewportBlockArea.setChangeBarList(this.getChangeBarList());
         this.viewportBlockArea.addTrait(Trait.IS_VIEWPORT_AREA, Boolean.TRUE);
         if (level >= 0) {
            this.viewportBlockArea.setBidiLevel(level);
         }

         this.viewportBlockArea.setIPD(this.getContentAreaIPD());
         if (allowBPDUpdate) {
            this.viewportBlockArea.setBPD(0);
         } else {
            this.viewportBlockArea.setBPD(this.vpContentBPD);
         }

         this.transferForeignAttributes(this.viewportBlockArea);
         TraitSetter.setProducerID(this.viewportBlockArea, this.getBlockContainerFO().getId());
         TraitSetter.setLayer(this.viewportBlockArea, this.getBlockContainerFO().getLayer());
         TraitSetter.addBorders(this.viewportBlockArea, this.getBlockContainerFO().getCommonBorderPaddingBackground(), this.discardBorderBefore, this.discardBorderAfter, false, false, this);
         TraitSetter.addPadding(this.viewportBlockArea, this.getBlockContainerFO().getCommonBorderPaddingBackground(), this.discardPaddingBefore, this.discardPaddingAfter, false, false, this);
         TraitSetter.addMargins(this.viewportBlockArea, this.getBlockContainerFO().getCommonBorderPaddingBackground(), this.startIndent, this.endIndent, this);
         this.viewportBlockArea.setCTM(this.absoluteCTM);
         this.viewportBlockArea.setClip(this.needClip());
         if (this.abProps.absolutePosition == 1 || this.abProps.absolutePosition == 51) {
            Point offset = this.getAbsOffset();
            this.viewportBlockArea.setXOffset(offset.x);
            this.viewportBlockArea.setYOffset(offset.y);
         }

         this.referenceArea = new Block();
         this.referenceArea.setChangeBarList(this.getChangeBarList());
         this.referenceArea.addTrait(Trait.IS_REFERENCE_AREA, Boolean.TRUE);
         if (level >= 0) {
            this.referenceArea.setBidiLevel(level);
         }

         TraitSetter.setProducerID(this.referenceArea, this.getBlockContainerFO().getId());
         if (this.abProps.absolutePosition == 1) {
            this.viewportBlockArea.setPositioning(2);
         } else if (this.abProps.absolutePosition == 51) {
            this.viewportBlockArea.setPositioning(3);
         }

         this.parentLayoutManager.getParentArea(this.referenceArea);
         this.referenceArea.setIPD(this.relDims.ipd);
         this.setCurrentArea(this.viewportBlockArea);
      }

      return this.referenceArea;
   }

   public void addChildArea(Area childArea) {
      if (this.referenceArea != null) {
         this.referenceArea.addBlock((Block)childArea);
      }

   }

   protected void flush() {
      this.viewportBlockArea.addBlock(this.referenceArea, this.autoHeight);
      TraitSetter.addBackground(this.viewportBlockArea, this.getBlockContainerFO().getCommonBorderPaddingBackground(), this);
      super.flush();
   }

   public int negotiateBPDAdjustment(int adj, KnuthElement lastElement) {
      return 0;
   }

   public void discardSpace(KnuthGlue spaceGlue) {
   }

   public KeepProperty getKeepTogetherProperty() {
      return this.getBlockContainerFO().getKeepTogether();
   }

   public KeepProperty getKeepWithPreviousProperty() {
      return this.getBlockContainerFO().getKeepWithPrevious();
   }

   public KeepProperty getKeepWithNextProperty() {
      return this.getBlockContainerFO().getKeepWithNext();
   }

   protected BlockContainer getBlockContainerFO() {
      return (BlockContainer)this.fobj;
   }

   public boolean getGeneratesReferenceArea() {
      return true;
   }

   public boolean getGeneratesBlockArea() {
      return true;
   }

   public boolean handleOverflow(int milliPoints) {
      if (milliPoints > this.horizontalOverflow) {
         this.horizontalOverflow = milliPoints;
      }

      return true;
   }

   private class BlockContainerBreaker extends AbstractBreaker {
      private BlockContainerLayoutManager bclm;
      private MinOptMax ipd;
      private PageBreakingAlgorithm deferredAlg;
      private AbstractBreaker.BlockSequence deferredOriginalList;
      private AbstractBreaker.BlockSequence deferredEffectiveList;

      public BlockContainerBreaker(BlockContainerLayoutManager bclm, MinOptMax ipd) {
         this.bclm = bclm;
         this.ipd = ipd;
      }

      protected void observeElementList(List elementList) {
         ElementListObserver.observe(elementList, "block-container", this.bclm.getBlockContainerFO().getId());
      }

      protected boolean isPartOverflowRecoveryActivated() {
         return false;
      }

      protected boolean isSinglePartFavored() {
         return true;
      }

      public int getDifferenceOfFirstPart() {
         AbstractBreaker.PageBreakPosition pbp = (AbstractBreaker.PageBreakPosition)this.deferredAlg.getPageBreaks().getFirst();
         return pbp.difference;
      }

      public boolean isOverflow() {
         return !this.isEmpty() && (this.deferredAlg.getPageBreaks().size() > 1 || this.deferredAlg.totalWidth - this.deferredAlg.totalShrink > this.deferredAlg.getLineWidth());
      }

      public int getOverflowAmount() {
         return this.deferredAlg.totalWidth - this.deferredAlg.totalShrink - this.deferredAlg.getLineWidth();
      }

      protected LayoutManager getTopLevelLM() {
         return this.bclm;
      }

      protected LayoutContext createLayoutContext() {
         LayoutContext lc = super.createLayoutContext();
         lc.setRefIPD(this.ipd.getOpt());
         lc.setWritingMode(BlockContainerLayoutManager.this.getBlockContainerFO().getWritingMode());
         return lc;
      }

      protected List getNextKnuthElements(LayoutContext context, int alignment) {
         List returnList = new LinkedList();

         LayoutManager curLM;
         while((curLM = BlockContainerLayoutManager.this.getChildLM()) != null) {
            LayoutContext childLC = BlockContainerLayoutManager.this.makeChildLayoutContext(context);
            List returnedList = null;
            if (!curLM.isFinished()) {
               returnedList = curLM.getNextKnuthElements(childLC, alignment);
            }

            if (returnedList != null) {
               this.bclm.wrapPositionElements(returnedList, returnList);
            }
         }

         SpaceResolver.resolveElementList(returnList);
         BlockContainerLayoutManager.this.setFinished(true);
         return returnList;
      }

      protected int getCurrentDisplayAlign() {
         return BlockContainerLayoutManager.this.getBlockContainerFO().getDisplayAlign();
      }

      protected boolean hasMoreContent() {
         return !BlockContainerLayoutManager.this.isFinished();
      }

      protected void addAreas(PositionIterator posIter, LayoutContext context) {
         AreaAdditionUtil.addAreas(this.bclm, posIter, context);
      }

      protected void doPhase3(PageBreakingAlgorithm alg, int partCount, AbstractBreaker.BlockSequence originalList, AbstractBreaker.BlockSequence effectiveList) {
         this.deferredAlg = alg;
         this.deferredOriginalList = originalList;
         this.deferredEffectiveList = effectiveList;
      }

      protected void finishPart(PageBreakingAlgorithm alg, AbstractBreaker.PageBreakPosition pbp) {
      }

      protected LayoutManager getCurrentChildLM() {
         return BlockContainerLayoutManager.this.curChildLM;
      }

      public void addContainedAreas(LayoutContext layoutContext) {
         if (!this.isEmpty()) {
            this.deferredAlg.removeAllPageBreaks();
            this.addAreas(this.deferredAlg, 0, this.deferredAlg.getPageBreaks().size(), this.deferredOriginalList, this.deferredEffectiveList, LayoutContext.offspringOf(layoutContext));
         }
      }
   }

   private class BlockContainerPosition extends NonLeafPosition {
      private BlockContainerBreaker breaker;

      public BlockContainerPosition(LayoutManager lm, BlockContainerBreaker breaker) {
         super(lm, (Position)null);
         this.breaker = breaker;
      }

      public BlockContainerBreaker getBreaker() {
         return this.breaker;
      }
   }
}
