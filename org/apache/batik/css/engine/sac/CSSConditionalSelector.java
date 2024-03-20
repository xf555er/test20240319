package org.apache.batik.css.engine.sac;

import java.util.Set;
import org.w3c.css.sac.Condition;
import org.w3c.css.sac.ConditionalSelector;
import org.w3c.css.sac.SimpleSelector;
import org.w3c.dom.Element;

public class CSSConditionalSelector implements ConditionalSelector, ExtendedSelector {
   protected SimpleSelector simpleSelector;
   protected Condition condition;

   public CSSConditionalSelector(SimpleSelector s, Condition c) {
      this.simpleSelector = s;
      this.condition = c;
   }

   public boolean equals(Object obj) {
      if (obj != null && obj.getClass() == this.getClass()) {
         CSSConditionalSelector s = (CSSConditionalSelector)obj;
         return s.simpleSelector.equals(this.simpleSelector) && s.condition.equals(this.condition);
      } else {
         return false;
      }
   }

   public short getSelectorType() {
      return 0;
   }

   public boolean match(Element e, String pseudoE) {
      return ((ExtendedSelector)this.getSimpleSelector()).match(e, pseudoE) && ((ExtendedCondition)this.getCondition()).match(e, pseudoE);
   }

   public void fillAttributeSet(Set attrSet) {
      ((ExtendedSelector)this.getSimpleSelector()).fillAttributeSet(attrSet);
      ((ExtendedCondition)this.getCondition()).fillAttributeSet(attrSet);
   }

   public int getSpecificity() {
      return ((ExtendedSelector)this.getSimpleSelector()).getSpecificity() + ((ExtendedCondition)this.getCondition()).getSpecificity();
   }

   public SimpleSelector getSimpleSelector() {
      return this.simpleSelector;
   }

   public Condition getCondition() {
      return this.condition;
   }

   public String toString() {
      return String.valueOf(this.simpleSelector) + this.condition;
   }
}
