package org.apache.batik.css.engine.sac;

import java.util.Set;
import org.w3c.dom.Element;

public class CSSAttributeCondition extends AbstractAttributeCondition {
   protected String localName;
   protected String namespaceURI;
   protected boolean specified;

   public CSSAttributeCondition(String localName, String namespaceURI, boolean specified, String value) {
      super(value);
      this.localName = localName;
      this.namespaceURI = namespaceURI;
      this.specified = specified;
   }

   public boolean equals(Object obj) {
      if (!super.equals(obj)) {
         return false;
      } else {
         CSSAttributeCondition c = (CSSAttributeCondition)obj;
         return c.namespaceURI.equals(this.namespaceURI) && c.localName.equals(this.localName) && c.specified == this.specified;
      }
   }

   public int hashCode() {
      return this.namespaceURI.hashCode() ^ this.localName.hashCode() ^ (this.specified ? -1 : 0);
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

   public boolean match(Element e, String pseudoE) {
      String val = this.getValue();
      if (val == null) {
         return !e.getAttribute(this.getLocalName()).equals("");
      } else {
         return e.getAttribute(this.getLocalName()).equals(val);
      }
   }

   public void fillAttributeSet(Set attrSet) {
      attrSet.add(this.localName);
   }

   public String toString() {
      return this.value == null ? '[' + this.localName + ']' : '[' + this.localName + "=\"" + this.value + "\"]";
   }
}
