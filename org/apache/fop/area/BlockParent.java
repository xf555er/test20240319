package org.apache.fop.area;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BlockParent extends Area {
   private static final long serialVersionUID = 7076916890348533805L;
   protected int xOffset;
   protected int yOffset;
   protected List children;

   public void addChildArea(Area childArea) {
      if (this.children == null) {
         this.children = new ArrayList();
      }

      this.children.add(childArea);
   }

   public void addBlock(Block block) {
      this.addChildArea(block);
   }

   public List getChildAreas() {
      return this.children;
   }

   public boolean isEmpty() {
      return this.children == null || this.children.size() == 0;
   }

   public void setXOffset(int off) {
      this.xOffset = off;
   }

   public void setYOffset(int off) {
      this.yOffset = off;
   }

   public int getXOffset() {
      return this.xOffset;
   }

   public int getYOffset() {
      return this.yOffset;
   }

   public int getEffectiveIPD() {
      int maxIPD = 0;
      if (this.children != null) {
         Iterator var2 = this.children.iterator();

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

   public void activateEffectiveIPD() {
      if (this.children != null) {
         Iterator var1 = this.children.iterator();

         while(var1.hasNext()) {
            Area area = (Area)var1.next();
            area.activateEffectiveIPD();
         }
      }

   }
}
