package org.apache.batik.css.engine.sac;

import org.w3c.dom.Element;

public class CSSPseudoElementSelector extends AbstractElementSelector {
   public CSSPseudoElementSelector(String uri, String name) {
      super(uri, name);
   }

   public short getSelectorType() {
      return 9;
   }

   public boolean match(Element e, String pseudoE) {
      return this.getLocalName().equalsIgnoreCase(pseudoE);
   }

   public int getSpecificity() {
      return 0;
   }

   public String toString() {
      return ":" + this.getLocalName();
   }
}
