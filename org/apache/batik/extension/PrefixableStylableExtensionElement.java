package org.apache.batik.extension;

import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.util.DOMUtilities;
import org.w3c.dom.DOMException;

public abstract class PrefixableStylableExtensionElement extends StylableExtensionElement {
   protected String prefix = null;

   protected PrefixableStylableExtensionElement() {
   }

   public PrefixableStylableExtensionElement(String prefix, AbstractDocument owner) {
      super(prefix, owner);
      this.setPrefix(prefix);
   }

   public String getNodeName() {
      return this.prefix != null && !this.prefix.equals("") ? this.prefix + ':' + this.getLocalName() : this.getLocalName();
   }

   public void setPrefix(String prefix) throws DOMException {
      if (this.isReadonly()) {
         throw this.createDOMException((short)7, "readonly.node", new Object[]{Integer.valueOf(this.getNodeType()), this.getNodeName()});
      } else if (prefix != null && !prefix.equals("") && !DOMUtilities.isValidName(prefix)) {
         throw this.createDOMException((short)5, "prefix", new Object[]{Integer.valueOf(this.getNodeType()), this.getNodeName(), prefix});
      } else {
         this.prefix = prefix;
      }
   }
}
