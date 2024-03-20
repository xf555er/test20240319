package org.apache.batik.css.parser;

import org.w3c.css.sac.Condition;
import org.w3c.css.sac.ConditionalSelector;
import org.w3c.css.sac.SimpleSelector;

public class DefaultConditionalSelector implements ConditionalSelector {
   protected SimpleSelector simpleSelector;
   protected Condition condition;

   public DefaultConditionalSelector(SimpleSelector s, Condition c) {
      this.simpleSelector = s;
      this.condition = c;
   }

   public short getSelectorType() {
      return 0;
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
