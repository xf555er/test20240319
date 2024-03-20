package org.apache.batik.css.parser;

import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SimpleSelector;

public class DefaultDescendantSelector extends AbstractDescendantSelector {
   public DefaultDescendantSelector(Selector ancestor, SimpleSelector simple) {
      super(ancestor, simple);
   }

   public short getSelectorType() {
      return 10;
   }

   public String toString() {
      return this.getAncestorSelector() + " " + this.getSimpleSelector();
   }
}
