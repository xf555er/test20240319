package org.apache.batik.css.engine.sac;

import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SiblingSelector;
import org.w3c.css.sac.SimpleSelector;

public abstract class AbstractSiblingSelector implements SiblingSelector, ExtendedSelector {
   protected short nodeType;
   protected Selector selector;
   protected SimpleSelector simpleSelector;

   protected AbstractSiblingSelector(short type, Selector sel, SimpleSelector simple) {
      this.nodeType = type;
      this.selector = sel;
      this.simpleSelector = simple;
   }

   public short getNodeType() {
      return this.nodeType;
   }

   public boolean equals(Object obj) {
      if (obj != null && obj.getClass() == this.getClass()) {
         AbstractSiblingSelector s = (AbstractSiblingSelector)obj;
         return s.simpleSelector.equals(this.simpleSelector);
      } else {
         return false;
      }
   }

   public int getSpecificity() {
      return ((ExtendedSelector)this.selector).getSpecificity() + ((ExtendedSelector)this.simpleSelector).getSpecificity();
   }

   public Selector getSelector() {
      return this.selector;
   }

   public SimpleSelector getSiblingSelector() {
      return this.simpleSelector;
   }
}
