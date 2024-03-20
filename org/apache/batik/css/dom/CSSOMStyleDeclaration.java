package org.apache.batik.css.dom;

import java.util.HashMap;
import java.util.Map;
import org.apache.batik.css.engine.value.Value;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSValue;

public class CSSOMStyleDeclaration implements CSSStyleDeclaration {
   protected ValueProvider valueProvider;
   protected ModificationHandler handler;
   protected CSSRule parentRule;
   protected Map values;

   public CSSOMStyleDeclaration(ValueProvider vp, CSSRule parent) {
      this.valueProvider = vp;
      this.parentRule = parent;
   }

   public void setModificationHandler(ModificationHandler h) {
      this.handler = h;
   }

   public String getCssText() {
      return this.valueProvider.getText();
   }

   public void setCssText(String cssText) throws DOMException {
      if (this.handler == null) {
         throw new DOMException((short)7, "");
      } else {
         this.values = null;
         this.handler.textChanged(cssText);
      }
   }

   public String getPropertyValue(String propertyName) {
      Value value = this.valueProvider.getValue(propertyName);
      return value == null ? "" : value.getCssText();
   }

   public CSSValue getPropertyCSSValue(String propertyName) {
      Value value = this.valueProvider.getValue(propertyName);
      return value == null ? null : this.getCSSValue(propertyName);
   }

   public String removeProperty(String propertyName) throws DOMException {
      String result = this.getPropertyValue(propertyName);
      if (result.length() > 0) {
         if (this.handler == null) {
            throw new DOMException((short)7, "");
         }

         if (this.values != null) {
            this.values.remove(propertyName);
         }

         this.handler.propertyRemoved(propertyName);
      }

      return result;
   }

   public String getPropertyPriority(String propertyName) {
      return this.valueProvider.isImportant(propertyName) ? "important" : "";
   }

   public void setProperty(String propertyName, String value, String prio) throws DOMException {
      if (this.handler == null) {
         throw new DOMException((short)7, "");
      } else {
         this.handler.propertyChanged(propertyName, value, prio);
      }
   }

   public int getLength() {
      return this.valueProvider.getLength();
   }

   public String item(int index) {
      return this.valueProvider.item(index);
   }

   public CSSRule getParentRule() {
      return this.parentRule;
   }

   protected CSSValue getCSSValue(String name) {
      CSSValue result = null;
      if (this.values != null) {
         result = (CSSValue)this.values.get(name);
      }

      if (result == null) {
         result = this.createCSSValue(name);
         if (this.values == null) {
            this.values = new HashMap(11);
         }

         this.values.put(name, result);
      }

      return result;
   }

   protected CSSValue createCSSValue(String name) {
      return new StyleDeclarationValue(name);
   }

   public class StyleDeclarationValue extends CSSOMValue implements CSSOMValue.ValueProvider {
      protected String property;

      public StyleDeclarationValue(String prop) {
         super((CSSOMValue.ValueProvider)null);
         this.valueProvider = this;
         this.setModificationHandler(new CSSOMValue.AbstractModificationHandler() {
            protected Value getValue() {
               return StyleDeclarationValue.this.getValue();
            }

            public void textChanged(String text) throws DOMException {
               if (CSSOMStyleDeclaration.this.values != null && CSSOMStyleDeclaration.this.values.get(this) != null && StyleDeclarationValue.this.handler != null) {
                  String prio = CSSOMStyleDeclaration.this.getPropertyPriority(StyleDeclarationValue.this.property);
                  CSSOMStyleDeclaration.this.handler.propertyChanged(StyleDeclarationValue.this.property, text, prio);
               } else {
                  throw new DOMException((short)7, "");
               }
            }
         });
         this.property = prop;
      }

      public Value getValue() {
         return CSSOMStyleDeclaration.this.valueProvider.getValue(this.property);
      }
   }

   public interface ModificationHandler {
      void textChanged(String var1) throws DOMException;

      void propertyRemoved(String var1) throws DOMException;

      void propertyChanged(String var1, String var2, String var3) throws DOMException;
   }

   public interface ValueProvider {
      Value getValue(String var1);

      boolean isImportant(String var1);

      String getText();

      int getLength();

      String item(int var1);
   }
}
