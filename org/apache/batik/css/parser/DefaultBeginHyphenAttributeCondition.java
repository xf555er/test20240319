package org.apache.batik.css.parser;

public class DefaultBeginHyphenAttributeCondition extends DefaultAttributeCondition {
   public DefaultBeginHyphenAttributeCondition(String localName, String namespaceURI, boolean specified, String value) {
      super(localName, namespaceURI, specified, value);
   }

   public short getConditionType() {
      return 8;
   }

   public String toString() {
      return "[" + this.getLocalName() + "|=\"" + this.getValue() + "\"]";
   }
}
