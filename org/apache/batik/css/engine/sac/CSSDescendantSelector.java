package org.apache.batik.css.engine.sac;

import java.util.Set;
import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SimpleSelector;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class CSSDescendantSelector extends AbstractDescendantSelector {
   public CSSDescendantSelector(Selector ancestor, SimpleSelector simple) {
      super(ancestor, simple);
   }

   public short getSelectorType() {
      return 10;
   }

   public boolean match(Element e, String pseudoE) {
      ExtendedSelector p = (ExtendedSelector)this.getAncestorSelector();
      if (!((ExtendedSelector)this.getSimpleSelector()).match(e, pseudoE)) {
         return false;
      } else {
         for(Node n = e.getParentNode(); n != null; n = n.getParentNode()) {
            if (n.getNodeType() == 1 && p.match((Element)n, (String)null)) {
               return true;
            }
         }

         return false;
      }
   }

   public void fillAttributeSet(Set attrSet) {
      ((ExtendedSelector)this.getSimpleSelector()).fillAttributeSet(attrSet);
   }

   public String toString() {
      return this.getAncestorSelector() + " " + this.getSimpleSelector();
   }
}
