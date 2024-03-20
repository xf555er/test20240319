package org.apache.batik.css.parser;

public class DefaultClassCondition extends DefaultAttributeCondition {
   public DefaultClassCondition(String namespaceURI, String value) {
      super("class", namespaceURI, true, value);
   }

   public short getConditionType() {
      return 9;
   }

   public String toString() {
      return "." + this.getValue();
   }
}
