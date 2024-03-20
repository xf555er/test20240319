package org.apache.batik.css.parser;

import org.w3c.css.sac.LangCondition;

public class DefaultLangCondition implements LangCondition {
   protected String lang;

   public DefaultLangCondition(String lang) {
      this.lang = lang;
   }

   public short getConditionType() {
      return 6;
   }

   public String getLang() {
      return this.lang;
   }

   public String toString() {
      return ":lang(" + this.lang + ")";
   }
}
