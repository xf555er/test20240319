package org.apache.fop.layoutmgr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.traits.MinOptMax;

public abstract class SpacedBorderedPaddedBlockLayoutManager extends BlockStackingLayoutManager implements ConditionalElementListener {
   private static final Log LOG = LogFactory.getLog(BlockLayoutManager.class);
   protected MinOptMax effSpaceBefore;
   protected MinOptMax effSpaceAfter;
   protected boolean discardBorderBefore;
   protected boolean discardBorderAfter;
   protected boolean discardPaddingBefore;
   protected boolean discardPaddingAfter;

   public SpacedBorderedPaddedBlockLayoutManager(FObj node) {
      super(node);
   }

   public void notifySpace(RelSide side, MinOptMax effectiveLength) {
      if (RelSide.BEFORE == side) {
         if (LOG.isDebugEnabled()) {
            LOG.debug(this + ": Space " + side + ", " + this.effSpaceBefore + "-> " + effectiveLength);
         }

         this.effSpaceBefore = effectiveLength;
      } else {
         if (LOG.isDebugEnabled()) {
            LOG.debug(this + ": Space " + side + ", " + this.effSpaceAfter + "-> " + effectiveLength);
         }

         this.effSpaceAfter = effectiveLength;
      }

   }

   public void notifyBorder(RelSide side, MinOptMax effectiveLength) {
      if (effectiveLength == null) {
         if (RelSide.BEFORE == side) {
            this.discardBorderBefore = true;
         } else {
            this.discardBorderAfter = true;
         }
      }

      if (LOG.isDebugEnabled()) {
         LOG.debug(this + ": Border " + side + " -> " + effectiveLength);
      }

   }

   public void notifyPadding(RelSide side, MinOptMax effectiveLength) {
      if (effectiveLength == null) {
         if (RelSide.BEFORE == side) {
            this.discardPaddingBefore = true;
         } else {
            this.discardPaddingAfter = true;
         }
      }

      if (LOG.isDebugEnabled()) {
         LOG.debug(this + ": Padding " + side + " -> " + effectiveLength);
      }

   }

   public int getBaselineOffset() {
      int baselineOffset = super.getBaselineOffset();
      if (this.effSpaceBefore != null) {
         baselineOffset += this.effSpaceBefore.getOpt();
      }

      if (!this.discardBorderBefore) {
         baselineOffset += this.getCommonBorderPaddingBackground().getBorderBeforeWidth(false);
      }

      if (!this.discardPaddingBefore) {
         baselineOffset += this.getCommonBorderPaddingBackground().getPaddingBefore(false, this);
      }

      return baselineOffset;
   }

   protected abstract CommonBorderPaddingBackground getCommonBorderPaddingBackground();
}
