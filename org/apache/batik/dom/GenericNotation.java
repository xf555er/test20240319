package org.apache.batik.dom;

import org.w3c.dom.Node;

public class GenericNotation extends AbstractNotation {
   protected boolean readonly;

   protected GenericNotation() {
   }

   public GenericNotation(String name, String pubId, String sysId, AbstractDocument owner) {
      this.ownerDocument = owner;
      this.setNodeName(name);
      this.setPublicId(pubId);
      this.setSystemId(sysId);
   }

   public boolean isReadonly() {
      return this.readonly;
   }

   public void setReadonly(boolean v) {
      this.readonly = v;
   }

   protected Node newNode() {
      return new GenericNotation();
   }
}
