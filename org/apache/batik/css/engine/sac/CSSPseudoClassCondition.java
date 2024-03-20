package org.apache.batik.css.engine.sac;

import java.util.Set;
import org.apache.batik.css.engine.CSSStylableElement;
import org.w3c.dom.Element;

public class CSSPseudoClassCondition extends AbstractAttributeCondition {
   protected String namespaceURI;

   public CSSPseudoClassCondition(String namespaceURI, String value) {
      super(value);
      this.namespaceURI = namespaceURI;
   }

   public boolean equals(Object obj) {
      if (!super.equals(obj)) {
         return false;
      } else {
         CSSPseudoClassCondition c = (CSSPseudoClassCondition)obj;
         return c.namespaceURI.equals(this.namespaceURI);
      }
   }

   public int hashCode() {
      return this.namespaceURI.hashCode();
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

   public boolean match(Element e, String pseudoE) {
      return e instanceof CSSStylableElement ? ((CSSStylableElement)e).isPseudoInstanceOf(this.getValue()) : false;
   }

   public void fillAttributeSet(Set attrSet) {
   }

   public String toString() {
      return ":" + this.getValue();
   }
}
