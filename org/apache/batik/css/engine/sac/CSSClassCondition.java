package org.apache.batik.css.engine.sac;

import org.apache.batik.css.engine.CSSStylableElement;
import org.w3c.dom.Element;

public class CSSClassCondition extends CSSAttributeCondition {
   public CSSClassCondition(String localName, String namespaceURI, String value) {
      super(localName, namespaceURI, true, value);
   }

   public short getConditionType() {
      return 9;
   }

   public boolean match(Element e, String pseudoE) {
      if (!(e instanceof CSSStylableElement)) {
         return false;
      } else {
         String attr = ((CSSStylableElement)e).getCSSClass();
         String val = this.getValue();
         int attrLen = attr.length();
         int valLen = val.length();

         for(int i = attr.indexOf(val); i != -1; i = attr.indexOf(val, i + valLen)) {
            if ((i == 0 || Character.isSpaceChar(attr.charAt(i - 1))) && (i + valLen == attrLen || Character.isSpaceChar(attr.charAt(i + valLen)))) {
               return true;
            }
         }

         return false;
      }
   }

   public String toString() {
      return '.' + this.getValue();
   }
}
