package org.apache.batik.css.parser;

public class DefaultElementSelector extends AbstractElementSelector {
   public DefaultElementSelector(String uri, String name) {
      super(uri, name);
   }

   public short getSelectorType() {
      return 4;
   }

   public String toString() {
      String name = this.getLocalName();
      return name == null ? "*" : name;
   }
}
