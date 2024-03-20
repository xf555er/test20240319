package org.apache.fop.area;

public class NormalFlow extends BlockParent {
   private static final long serialVersionUID = -3753538631016929004L;

   public NormalFlow(int ipd) {
      this.addTrait(Trait.IS_REFERENCE_AREA, Boolean.TRUE);
      this.setIPD(ipd);
   }

   public void addBlock(Block block) {
      super.addBlock(block);
      if (block.isStacked()) {
         this.bpd += block.getAllocBPD();
      }

   }
}
