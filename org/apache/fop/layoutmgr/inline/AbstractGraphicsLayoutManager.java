package org.apache.fop.layoutmgr.inline;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.List;
import org.apache.fop.area.Area;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.area.inline.InlineViewport;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.flow.AbstractGraphics;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.TraitSetter;

public abstract class AbstractGraphicsLayoutManager extends LeafNodeLayoutManager {
   public AbstractGraphicsLayoutManager(AbstractGraphics node) {
      super(node);
   }

   private InlineViewport getInlineArea() {
      AbstractGraphics fobj = (AbstractGraphics)this.fobj;
      Dimension intrinsicSize = new Dimension(fobj.getIntrinsicWidth(), fobj.getIntrinsicHeight());
      int bidiLevel = fobj.getBidiLevel();
      ImageLayout imageLayout = new ImageLayout(fobj, this, intrinsicSize);
      Rectangle placement = imageLayout.getPlacement();
      CommonBorderPaddingBackground borderProps = fobj.getCommonBorderPaddingBackground();
      this.setCommonBorderPaddingBackground(borderProps);
      int beforeBPD = borderProps.getPadding(0, false, this);
      beforeBPD += borderProps.getBorderWidth(0, false);
      placement.y += beforeBPD;
      int startIPD;
      if (bidiLevel != -1 && (bidiLevel & 1) != 0) {
         startIPD = borderProps.getPadding(3, false, this);
         startIPD += borderProps.getBorderWidth(3, false);
         placement.x += startIPD;
      } else {
         startIPD = borderProps.getPadding(2, false, this);
         startIPD += borderProps.getBorderWidth(2, false);
         placement.x += startIPD;
      }

      Area viewportArea = this.getChildArea();
      TraitSetter.setProducerID(viewportArea, fobj.getId());
      this.transferForeignAttributes(viewportArea);
      InlineViewport vp = new InlineViewport(viewportArea, bidiLevel);
      TraitSetter.setProducerID(vp, fobj.getId());
      vp.setIPD(imageLayout.getViewportSize().width);
      vp.setBPD(imageLayout.getViewportSize().height);
      vp.setContentPosition(placement);
      vp.setClip(imageLayout.isClipped());
      vp.setBlockProgressionOffset(0);
      TraitSetter.addBorders(vp, borderProps, false, false, false, false, this);
      TraitSetter.addPadding(vp, borderProps, false, false, false, false, this);
      TraitSetter.addBackground(vp, borderProps, this);
      return vp;
   }

   public List getNextKnuthElements(LayoutContext context, int alignment) {
      InlineViewport areaCurrent = this.getInlineArea();
      this.setCurrentArea(areaCurrent);
      return super.getNextKnuthElements(context, alignment);
   }

   protected InlineArea getEffectiveArea(LayoutContext layoutContext) {
      InlineArea area = this.curArea != null ? this.curArea : this.getInlineArea();
      this.curArea = null;
      if (!layoutContext.treatAsArtifact()) {
         TraitSetter.addStructureTreeElement((Area)area, ((AbstractGraphics)this.fobj).getStructureTreeElement());
      }

      return (InlineArea)area;
   }

   protected AlignmentContext makeAlignmentContext(LayoutContext context) {
      AbstractGraphics fobj = (AbstractGraphics)this.fobj;
      return new AlignmentContext(this.get(context).getAllocBPD(), fobj.getAlignmentAdjust(), fobj.getAlignmentBaseline(), fobj.getBaselineShift(), fobj.getDominantBaseline(), context.getAlignmentContext());
   }

   protected abstract Area getChildArea();

   public int getBaseLength(int lengthBase, FObj fobj) {
      switch (lengthBase) {
         case 7:
            return ((AbstractGraphics)fobj).getIntrinsicWidth();
         case 8:
            return ((AbstractGraphics)fobj).getIntrinsicHeight();
         case 12:
            return this.get((LayoutContext)null).getBPD();
         default:
            return super.getBaseLength(lengthBase, fobj);
      }
   }
}
