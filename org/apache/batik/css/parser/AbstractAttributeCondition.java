package org.apache.batik.css.parser;

import org.w3c.css.sac.AttributeCondition;

public abstract class AbstractAttributeCondition implements AttributeCondition {
   protected String value;

   protected AbstractAttributeCondition(String value) {
      this.value = value;
   }

   public String getValue() {
      return this.value;
   }
}
