package org.apache.batik.css.engine.sac;

import java.util.Set;
import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SimpleSelector;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class CSSChildSelector extends AbstractDescendantSelector {
   public CSSChildSelector(Selector ancestor, SimpleSelector simple) {
      super(ancestor, simple);
   }

   public short getSelectorType() {
      return 11;
   }

   public boolean match(Element e, String pseudoE) {
      Node n = e.getParentNode();
      if (n != null && n.getNodeType() == 1) {
         return ((ExtendedSelector)this.getAncestorSelector()).match((Element)n, (String)null) && ((ExtendedSelector)this.getSimpleSelector()).match(e, pseudoE);
      } else {
         return false;
      }
   }

   public void fillAttributeSet(Set attrSet) {
      ((ExtendedSelector)this.getAncestorSelector()).fillAttributeSet(attrSet);
      ((ExtendedSelector)this.getSimpleSelector()).fillAttributeSet(attrSet);
   }

   public String toString() {
      SimpleSelector s = this.getSimpleSelector();
      return s.getSelectorType() == 9 ? String.valueOf(this.getAncestorSelector()) + s : this.getAncestorSelector() + " > " + s;
   }
}
