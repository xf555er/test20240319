package org.apache.batik.css.parser;

public class DefaultPseudoElementSelector extends AbstractElementSelector {
   public DefaultPseudoElementSelector(String uri, String name) {
      super(uri, name);
   }

   public short getSelectorType() {
      return 9;
   }

   public String toString() {
      return ":" + this.getLocalName();
   }
}
