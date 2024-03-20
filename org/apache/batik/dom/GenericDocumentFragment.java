package org.apache.batik.dom;

import org.w3c.dom.Node;

public class GenericDocumentFragment extends AbstractDocumentFragment {
   protected boolean readonly;

   protected GenericDocumentFragment() {
   }

   public GenericDocumentFragment(AbstractDocument owner) {
      this.ownerDocument = owner;
   }

   public boolean isReadonly() {
      return this.readonly;
   }

   public void setReadonly(boolean v) {
      this.readonly = v;
   }

   protected Node newNode() {
      return new GenericDocumentFragment();
   }
}
