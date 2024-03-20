package org.apache.batik.css.engine.sac;

import org.w3c.dom.Element;

public class CSSElementSelector extends AbstractElementSelector {
   public CSSElementSelector(String uri, String name) {
      super(uri, name);
   }

   public short getSelectorType() {
      return 4;
   }

   public boolean match(Element e, String pseudoE) {
      String name = this.getLocalName();
      if (name == null) {
         return true;
      } else {
         String eName;
         if (e.getPrefix() == null) {
            eName = e.getNodeName();
         } else {
            eName = e.getLocalName();
         }

         return eName.equals(name);
      }
   }

   public int getSpecificity() {
      return this.getLocalName() == null ? 0 : 1;
   }

   public String toString() {
      String name = this.getLocalName();
      return name == null ? "*" : name;
   }
}
