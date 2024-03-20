package org.apache.batik.css.parser;

public class DefaultPseudoClassCondition extends AbstractAttributeCondition {
   protected String namespaceURI;

   public DefaultPseudoClassCondition(String namespaceURI, String value) {
      super(value);
      this.namespaceURI = namespaceURI;
   }

   public short getConditionType() {
      return 10;
   }

   public String getNamespaceURI() {
      return this.namespaceURI;
   }

   public String getLocalName() {
      return null;
   }

   public boolean getSpecified() {
      return false;
   }

   public String toString() {
      return ":" + this.getValue();
   }
}
