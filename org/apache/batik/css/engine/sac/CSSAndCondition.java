package org.apache.batik.css.engine.sac;

import java.util.Set;
import org.w3c.css.sac.Condition;
import org.w3c.dom.Element;

public class CSSAndCondition extends AbstractCombinatorCondition {
   public CSSAndCondition(Condition c1, Condition c2) {
      super(c1, c2);
   }

   public short getConditionType() {
      return 0;
   }

   public boolean match(Element e, String pseudoE) {
      return ((ExtendedCondition)this.getFirstCondition()).match(e, pseudoE) && ((ExtendedCondition)this.getSecondCondition()).match(e, pseudoE);
   }

   public void fillAttributeSet(Set attrSet) {
      ((ExtendedCondition)this.getFirstCondition()).fillAttributeSet(attrSet);
      ((ExtendedCondition)this.getSecondCondition()).fillAttributeSet(attrSet);
   }

   public String toString() {
      return String.valueOf(this.getFirstCondition()) + this.getSecondCondition();
   }
}
