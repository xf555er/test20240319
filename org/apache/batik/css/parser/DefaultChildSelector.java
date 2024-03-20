package org.apache.batik.css.parser;

import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SimpleSelector;

public class DefaultChildSelector extends AbstractDescendantSelector {
   public DefaultChildSelector(Selector ancestor, SimpleSelector simple) {
      super(ancestor, simple);
   }

   public short getSelectorType() {
      return 11;
   }

   public String toString() {
      SimpleSelector s = this.getSimpleSelector();
      return s.getSelectorType() == 9 ? String.valueOf(this.getAncestorSelector()) + s : this.getAncestorSelector() + " > " + s;
   }
}
