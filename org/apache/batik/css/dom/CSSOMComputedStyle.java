package org.apache.batik.css.dom;

import java.util.HashMap;
import java.util.Map;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.value.Value;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSValue;

public class CSSOMComputedStyle implements CSSStyleDeclaration {
   protected CSSEngine cssEngine;
   protected CSSStylableElement element;
   protected String pseudoElement;
   protected Map values = new HashMap();

   public CSSOMComputedStyle(CSSEngine e, CSSStylableElement elt, String pseudoElt) {
      this.cssEngine = e;
      this.element = elt;
      this.pseudoElement = pseudoElt;
   }

   public String getCssText() {
      StringBuffer sb = new StringBuffer();

      for(int i = 0; i < this.cssEngine.getNumberOfProperties(); ++i) {
         sb.append(this.cssEngine.getPropertyName(i));
         sb.append(": ");
         sb.append(this.cssEngine.getComputedStyle(this.element, this.pseudoElement, i).getCssText());
         sb.append(";\n");
      }

      return sb.toString();
   }

   public void setCssText(String cssText) throws DOMException {
      throw new DOMException((short)7, "");
   }

   public String getPropertyValue(String propertyName) {
      int idx = this.cssEngine.getPropertyIndex(propertyName);
      if (idx == -1) {
         return "";
      } else {
         Value v = this.cssEngine.getComputedStyle(this.element, this.pseudoElement, idx);
         return v.getCssText();
      }
   }

   public CSSValue getPropertyCSSValue(String propertyName) {
      CSSValue result = (CSSValue)this.values.get(propertyName);
      if (result == null) {
         int idx = this.cssEngine.getPropertyIndex(propertyName);
         if (idx != -1) {
            result = this.createCSSValue(idx);
            this.values.put(propertyName, result);
         }
      }

      return result;
   }

   public String removeProperty(String propertyName) throws DOMException {
      throw new DOMException((short)7, "");
   }

   public String getPropertyPriority(String propertyName) {
      return "";
   }

   public void setProperty(String propertyName, String value, String prio) throws DOMException {
      throw new DOMException((short)7, "");
   }

   public int getLength() {
      return this.cssEngine.getNumberOfProperties();
   }

   public String item(int index) {
      return index >= 0 && index < this.cssEngine.getNumberOfProperties() ? this.cssEngine.getPropertyName(index) : "";
   }

   public CSSRule getParentRule() {
      return null;
   }

   protected CSSValue createCSSValue(int idx) {
      return new ComputedCSSValue(idx);
   }

   public class ComputedCSSValue extends CSSOMValue implements CSSOMValue.ValueProvider {
      protected int index;

      public ComputedCSSValue(int idx) {
         super((CSSOMValue.ValueProvider)null);
         this.valueProvider = this;
         this.index = idx;
      }

      public Value getValue() {
         return CSSOMComputedStyle.this.cssEngine.getComputedStyle(CSSOMComputedStyle.this.element, CSSOMComputedStyle.this.pseudoElement, this.index);
      }
   }
}
