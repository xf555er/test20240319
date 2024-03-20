package org.apache.batik.css.engine.sac;

import org.w3c.css.sac.AttributeCondition;

public abstract class AbstractAttributeCondition implements AttributeCondition, ExtendedCondition {
   protected String value;

   protected AbstractAttributeCondition(String value) {
      this.value = value;
   }

   public boolean equals(Object obj) {
      if (obj != null && obj.getClass() == this.getClass()) {
         AbstractAttributeCondition c = (AbstractAttributeCondition)obj;
         return c.value.equals(this.value);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.value == null ? -1 : this.value.hashCode();
   }

   public int getSpecificity() {
      return 256;
   }

   public String getValue() {
      return this.value;
   }
}
