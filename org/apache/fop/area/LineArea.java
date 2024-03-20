package org.apache.fop.area;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.fop.area.inline.InlineArea;

public class LineArea extends Area {
   private static final long serialVersionUID = 7670235908329290684L;
   private LineAdjustingInfo adjustingInfo;
   private List inlineAreas = new ArrayList();

   public LineArea() {
   }

   public LineArea(int alignment, int diff, int stretch, int shrink) {
      this.adjustingInfo = new LineAdjustingInfo(alignment, diff, stretch, shrink);
   }

   public void addChildArea(Area childArea) {
      if (childArea instanceof InlineArea) {
         this.addInlineArea((InlineArea)childArea);
         ((InlineArea)childArea).setParentArea(this);
      }

   }

   public void addInlineArea(InlineArea area) {
      this.inlineAreas.add(area);
   }

   public void setInlineAreas(List inlineAreas) {
      Iterator var2 = inlineAreas.iterator();

      while(var2.hasNext()) {
         InlineArea ia = (InlineArea)var2.next();
         Area pa = ia.getParentArea();
         if (pa == null) {
            ia.setParentArea(this);
         } else {
            assert pa == this;
         }
      }

      this.inlineAreas = inlineAreas;
   }

   public List getInlineAreas() {
      return this.inlineAreas;
   }

   public int getStartIndent() {
      return this.hasTrait(Trait.START_INDENT) ? this.getTraitAsInteger(Trait.START_INDENT) : 0;
   }

   public int getEndIndent() {
      return this.hasTrait(Trait.END_INDENT) ? this.getTraitAsInteger(Trait.END_INDENT) : 0;
   }

   public void updateExtentsFromChildren() {
      int ipd = 0;
      int bpd = 0;

      InlineArea inlineArea;
      for(Iterator var3 = this.inlineAreas.iterator(); var3.hasNext(); bpd += inlineArea.getAllocBPD()) {
         inlineArea = (InlineArea)var3.next();
         ipd = Math.max(ipd, inlineArea.getAllocIPD());
      }

      this.setIPD(ipd);
      this.setBPD(bpd);
   }

   public void handleIPDVariation(int var1) {
      // $FF: Couldn't be decompiled
   }

   public void finish() {
      if (this.adjustingInfo.lineAlignment == 70) {
         if (log.isTraceEnabled()) {
            log.trace("Applying variation factor to justified line: " + this.adjustingInfo);
         }

         boolean bUnresolvedAreasPresent = false;

         InlineArea inlineArea;
         for(Iterator var2 = this.inlineAreas.iterator(); var2.hasNext(); bUnresolvedAreasPresent |= inlineArea.applyVariationFactor(this.adjustingInfo.variationFactor, this.adjustingInfo.availableStretch, this.adjustingInfo.availableShrink)) {
            inlineArea = (InlineArea)var2.next();
         }

         if (!bUnresolvedAreasPresent) {
            this.adjustingInfo = null;
         } else {
            if (!this.adjustingInfo.bAddedToAreaTree) {
               this.adjustingInfo.bAddedToAreaTree = true;
            }

            this.adjustingInfo.variationFactor = 1.0;
         }
      }

   }

   public int getEffectiveIPD() {
      int maxIPD = 0;
      if (this.inlineAreas != null) {
         Iterator var2 = this.inlineAreas.iterator();

         while(var2.hasNext()) {
            Area area = (Area)var2.next();
            int effectiveIPD = area.getEffectiveIPD();
            if (effectiveIPD > maxIPD) {
               maxIPD = effectiveIPD;
            }
         }
      }

      return maxIPD;
   }

   private final class LineAdjustingInfo implements Serializable {
      private static final long serialVersionUID = -6103629976229458273L;
      private int lineAlignment;
      private int difference;
      private int availableStretch;
      private int availableShrink;
      private double variationFactor;
      private boolean bAddedToAreaTree;

      private LineAdjustingInfo(int alignment, int diff, int stretch, int shrink) {
         this.lineAlignment = alignment;
         this.difference = diff;
         this.availableStretch = stretch;
         this.availableShrink = shrink;
         this.variationFactor = 1.0;
         this.bAddedToAreaTree = false;
      }

      public String toString() {
         return this.getClass().getSimpleName() + ": diff=" + this.difference + ", variation=" + this.variationFactor + ", stretch=" + this.availableStretch + ", shrink=" + this.availableShrink;
      }

      // $FF: synthetic method
      LineAdjustingInfo(int x1, int x2, int x3, int x4, Object x5) {
         this(x1, x2, x3, x4);
      }

      // $FF: synthetic method
      static int access$300(LineAdjustingInfo x0) {
         return x0.difference;
      }

      // $FF: synthetic method
      static int access$302(LineAdjustingInfo x0, int x1) {
         return x0.difference = x1;
      }
   }
}
