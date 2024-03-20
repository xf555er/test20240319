package org.apache.batik.dom;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

public class GenericAttrNS extends AbstractAttrNS {
   protected boolean readonly;

   protected GenericAttrNS() {
   }

   public GenericAttrNS(String nsURI, String qname, AbstractDocument owner) throws DOMException {
      super(nsURI, qname, owner);
      this.setNodeName(qname);
   }

   public boolean isReadonly() {
      return this.readonly;
   }

   public void setReadonly(boolean v) {
      this.readonly = v;
   }

   protected Node newNode() {
      return new GenericAttrNS();
   }
}
