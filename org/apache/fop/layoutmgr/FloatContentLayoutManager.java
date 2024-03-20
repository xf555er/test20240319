package org.apache.fop.layoutmgr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import org.apache.fop.area.Area;
import org.apache.fop.area.SideFloat;
import org.apache.fop.fo.flow.Float;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.layoutmgr.inline.FloatLayoutManager;
import org.apache.fop.layoutmgr.inline.KnuthInlineBox;
import org.apache.fop.layoutmgr.table.TableLayoutManager;

public class FloatContentLayoutManager extends SpacedBorderedPaddedBlockLayoutManager {
   private SideFloat floatContentArea;
   private int side;
   private int yOffset;

   public FloatContentLayoutManager(Float node) {
      super(node);
      this.generatesReferenceArea = true;
      this.side = node.getFloat();
   }

   public Keep getKeepTogether() {
      return this.getParentKeepTogether();
   }

   public Keep getKeepWithNext() {
      return Keep.KEEP_AUTO;
   }

   public Keep getKeepWithPrevious() {
      return Keep.KEEP_ALWAYS;
   }

   public void addAreas(PositionIterator parentIter, LayoutContext layoutContext) {
      this.floatContentArea = new SideFloat();
      AreaAdditionUtil.addAreas(this, parentIter, layoutContext);
      this.flush();
   }

   public void addChildArea(Area childArea) {
      this.floatContentArea.addChildArea(childArea);
      this.floatContentArea.setBPD(childArea.getAllocBPD());
      int effectiveContentIPD = this.getContentAreaIPD(this.childLMs, childArea);
      int contentIPD = childArea.getIPD();
      int xOffset = childArea.getBorderAndPaddingWidthStart();
      this.floatContentArea.setIPD(effectiveContentIPD);
      childArea.activateEffectiveIPD();
      if (this.side != 39 && this.side != 120) {
         if (this.side == 135 || this.side == 73) {
            this.floatContentArea.setXOffset(xOffset);
         }
      } else {
         xOffset += this.getStartIndent();
         this.floatContentArea.setXOffset(xOffset + contentIPD - effectiveContentIPD);
      }

      LayoutManager lm;
      for(lm = this.parentLayoutManager; !lm.getGeneratesReferenceArea(); lm = lm.getParent()) {
      }

      this.yOffset = lm.getParentArea(this.floatContentArea).getBPD();
      lm.addChildArea(this.floatContentArea);
      if (this.side != 39 && this.side != 120) {
         if (this.side == 135 || this.side == 73) {
            lm.getPSLM().setStartIntrusionAdjustment(effectiveContentIPD);
         }
      } else {
         lm.getPSLM().setEndIntrusionAdjustment(effectiveContentIPD);
      }

   }

   private int getContentAreaIPD(List childLMs, Area childArea) {
      int ipd = this.getContentAreaIPD(childLMs);
      return ipd == 0 ? childArea.getEffectiveAllocIPD() : ipd;
   }

   private int getContentAreaIPD(List childLMs) {
      int ipd = 0;
      Iterator var3 = childLMs.iterator();

      while(var3.hasNext()) {
         LayoutManager childLM = (LayoutManager)var3.next();
         if (childLM instanceof TableLayoutManager) {
            ipd += childLM.getContentAreaIPD();
         } else {
            ipd += this.getContentAreaIPD(childLM.getChildLMs());
         }
      }

      return ipd;
   }

   public static List checkForFloats(List elemenList, int startIndex, int endIndex) {
      ListIterator iter = elemenList.listIterator(startIndex);
      List floats = new ArrayList();

      while(true) {
         while(iter.nextIndex() <= endIndex) {
            ListElement element = (ListElement)iter.next();
            if (element instanceof KnuthInlineBox && ((KnuthInlineBox)element).isFloatAnchor()) {
               floats.add(((KnuthInlineBox)element).getFloatContentLM());
            } else if (element instanceof KnuthBlockBox && ((KnuthBlockBox)element).hasFloatAnchors()) {
               floats.addAll(((KnuthBlockBox)element).getFloatContentLMs());
            }
         }

         if (floats.isEmpty()) {
            return Collections.emptyList();
         }

         return floats;
      }
   }

   protected CommonBorderPaddingBackground getCommonBorderPaddingBackground() {
      return null;
   }

   public void processAreas(LayoutContext layoutContext) {
      if (this.getParent() instanceof FloatLayoutManager) {
         FloatLayoutManager flm = (FloatLayoutManager)this.getParent();
         flm.processAreas(layoutContext);
      }

   }

   public int getFloatHeight() {
      return this.floatContentArea.getAllocBPD();
   }

   public int getFloatYOffset() {
      return this.yOffset;
   }

   private int getStartIndent() {
      LayoutManager lm;
      for(lm = this.getParent(); !(lm instanceof BlockLayoutManager); lm = lm.getParent()) {
      }

      int startIndent = ((BlockLayoutManager)lm).startIndent;
      return startIndent;
   }
}
