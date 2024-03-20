package org.apache.batik.dom.util;

import java.util.List;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ListNodeList implements NodeList {
   protected List list;

   public ListNodeList(List list) {
      this.list = list;
   }

   public Node item(int index) {
      return index >= 0 && index <= this.list.size() ? (Node)this.list.get(index) : null;
   }

   public int getLength() {
      return this.list.size();
   }
}
