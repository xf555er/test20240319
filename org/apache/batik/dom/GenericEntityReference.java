package org.apache.batik.dom;

import org.w3c.dom.Node;

public class GenericEntityReference extends AbstractEntityReference {
   protected boolean readonly;

   protected GenericEntityReference() {
   }

   public GenericEntityReference(String name, AbstractDocument owner) {
      super(name, owner);
   }

   public boolean isReadonly() {
      return this.readonly;
   }

   public void setReadonly(boolean v) {
      this.readonly = v;
   }

   protected Node newNode() {
      return new GenericEntityReference();
   }
}
