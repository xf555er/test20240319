package org.apache.batik.dom;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

public class GenericAttr extends AbstractAttr {
   protected boolean readonly;

   protected GenericAttr() {
   }

   public GenericAttr(String name, AbstractDocument owner) throws DOMException {
      super(name, owner);
      this.setNodeName(name);
   }

   public boolean isReadonly() {
      return this.readonly;
   }

   public void setReadonly(boolean v) {
      this.readonly = v;
   }

   protected Node newNode() {
      return new GenericAttr();
   }
}
