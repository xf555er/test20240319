package org.apache.fop.area;

public class BeforeFloat extends BlockParent {
   private static final long serialVersionUID = 4101415711488333380L;
   private Block separator;

   public void setSeparator(Block sep) {
      this.separator = sep;
   }

   public Block getSeparator() {
      return this.separator;
   }

   public int getBPD() {
      int h = super.getBPD();
      if (this.separator != null) {
         h += this.separator.getBPD();
      }

      return h;
   }

   public boolean isEmpty() {
      return true;
   }
}
