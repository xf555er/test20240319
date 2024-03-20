package org.apache.fop.area;

public class Footnote extends BlockParent {
   private static final long serialVersionUID = -7907428219886367161L;
   private Block separator;
   private int top;

   public void setSeparator(Block sep) {
      this.separator = sep;
   }

   public Block getSeparator() {
      return this.separator;
   }

   public void setTop(int top) {
      this.top = top;
   }

   public int getTop() {
      return this.top;
   }

   public void addBlock(Block child) {
      this.addChildArea(child);
      this.setBPD(this.getBPD() + child.getAllocBPD());
   }
}
