package org.apache.batik.dom.svg;

public class SVGNumberItem extends AbstractSVGNumber implements SVGItem {
   protected AbstractSVGList parentList;

   public SVGNumberItem(float value) {
      this.value = value;
   }

   public String getValueAsString() {
      return Float.toString(this.value);
   }

   public void setParent(AbstractSVGList list) {
      this.parentList = list;
   }

   public AbstractSVGList getParent() {
      return this.parentList;
   }

   protected void reset() {
      if (this.parentList != null) {
         this.parentList.itemChanged();
      }

   }
}
