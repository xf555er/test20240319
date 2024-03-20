package org.apache.batik.css.parser;

public class DefaultAttributeCondition extends AbstractAttributeCondition {
   protected String localName;
   protected String namespaceURI;
   protected boolean specified;

   public DefaultAttributeCondition(String localName, String namespaceURI, boolean specified, String value) {
      super(value);
      this.localName = localName;
      this.namespaceURI = namespaceURI;
      this.specified = specified;
   }

   public short getConditionType() {
      return 4;
   }

   public String getNamespaceURI() {
      return this.namespaceURI;
   }

   public String getLocalName() {
      return this.localName;
   }

   public boolean getSpecified() {
      return this.specified;
   }

   public String toString() {
      return this.value == null ? "[" + this.localName + "]" : "[" + this.localName + "=\"" + this.value + "\"]";
   }
}
