package org.apache.batik.css.engine.sac;

import java.util.Set;
import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SimpleSelector;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class CSSDirectAdjacentSelector extends AbstractSiblingSelector {
   public CSSDirectAdjacentSelector(short type, Selector parent, SimpleSelector simple) {
      super(type, parent, simple);
   }

   public short getSelectorType() {
      return 12;
   }

   public boolean match(Element e, String pseudoE) {
      Node n = e;
      if (!((ExtendedSelector)this.getSiblingSelector()).match(e, pseudoE)) {
         return false;
      } else {
         while((n = ((Node)n).getPreviousSibling()) != null && ((Node)n).getNodeType() != 1) {
         }

         return n == null ? false : ((ExtendedSelector)this.getSelector()).match((Element)n, (String)null);
      }
   }

   public void fillAttributeSet(Set attrSet) {
      ((ExtendedSelector)this.getSelector()).fillAttributeSet(attrSet);
      ((ExtendedSelector)this.getSiblingSelector()).fillAttributeSet(attrSet);
   }

   public String toString() {
      return this.getSelector() + " + " + this.getSiblingSelector();
   }
}
