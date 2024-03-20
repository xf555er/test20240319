package org.apache.batik.css.parser;

public class DefaultOneOfAttributeCondition extends DefaultAttributeCondition {
   public DefaultOneOfAttributeCondition(String localName, String namespaceURI, boolean specified, String value) {
      super(localName, namespaceURI, specified, value);
   }

   public short getConditionType() {
      return 7;
   }

   public String toString() {
      return "[" + this.getLocalName() + "~=\"" + this.getValue() + "\"]";
   }
}
