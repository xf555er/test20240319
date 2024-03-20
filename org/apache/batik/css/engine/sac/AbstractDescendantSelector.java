package org.apache.batik.css.engine.sac;

import org.w3c.css.sac.DescendantSelector;
import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SimpleSelector;

public abstract class AbstractDescendantSelector implements DescendantSelector, ExtendedSelector {
   protected Selector ancestorSelector;
   protected SimpleSelector simpleSelector;

   protected AbstractDescendantSelector(Selector ancestor, SimpleSelector simple) {
      this.ancestorSelector = ancestor;
      this.simpleSelector = simple;
   }

   public boolean equals(Object obj) {
      if (obj != null && obj.getClass() == this.getClass()) {
         AbstractDescendantSelector s = (AbstractDescendantSelector)obj;
         return s.simpleSelector.equals(this.simpleSelector);
      } else {
         return false;
      }
   }

   public int getSpecificity() {
      return ((ExtendedSelector)this.ancestorSelector).getSpecificity() + ((ExtendedSelector)this.simpleSelector).getSpecificity();
   }

   public Selector getAncestorSelector() {
      return this.ancestorSelector;
   }

   public SimpleSelector getSimpleSelector() {
      return this.simpleSelector;
   }
}
