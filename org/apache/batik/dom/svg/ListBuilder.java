package org.apache.batik.dom.svg;

import java.util.ArrayList;
import java.util.List;

public class ListBuilder implements ListHandler {
   private final AbstractSVGList abstractSVGList;
   protected List list;

   public ListBuilder(AbstractSVGList abstractSVGList) {
      this.abstractSVGList = abstractSVGList;
   }

   public List getList() {
      return this.list;
   }

   public void startList() {
      this.list = new ArrayList();
   }

   public void item(SVGItem item) {
      item.setParent(this.abstractSVGList);
      this.list.add(item);
   }

   public void endList() {
   }
}
