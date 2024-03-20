package org.apache.fop.area;

public class SideFloat extends Block {
   private static final long serialVersionUID = 2058594336594375047L;

   public SideFloat() {
      this.setAreaClass(5);
      this.addTrait(Trait.IS_REFERENCE_AREA, Boolean.TRUE);
      this.setPositioning(2);
   }
}
