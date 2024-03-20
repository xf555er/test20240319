package org.apache.batik.extension;

import org.apache.batik.anim.dom.SVGOMElement;
import org.apache.batik.dom.AbstractDocument;

public abstract class ExtensionElement extends SVGOMElement {
   protected ExtensionElement() {
   }

   protected ExtensionElement(String name, AbstractDocument owner) {
      super(name, owner);
   }

   public boolean isReadonly() {
      return false;
   }

   public void setReadonly(boolean v) {
   }
}
