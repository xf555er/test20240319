package org.apache.batik.dom;

import org.w3c.dom.Node;

public class GenericComment extends AbstractComment {
   protected boolean readonly;

   public GenericComment() {
   }

   public GenericComment(String value, AbstractDocument owner) {
      this.ownerDocument = owner;
      this.setNodeValue(value);
   }

   public boolean isReadonly() {
      return this.readonly;
   }

   public void setReadonly(boolean v) {
      this.readonly = v;
   }

   protected Node newNode() {
      return new GenericComment();
   }
}
