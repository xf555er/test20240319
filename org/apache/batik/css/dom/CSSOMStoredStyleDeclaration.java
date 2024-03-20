package org.apache.batik.css.dom;

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.StyleDeclaration;
import org.apache.batik.css.engine.StyleDeclarationProvider;
import org.apache.batik.css.engine.value.Value;
import org.w3c.dom.css.CSSRule;

public abstract class CSSOMStoredStyleDeclaration extends CSSOMSVGStyleDeclaration implements CSSOMStyleDeclaration.ValueProvider, CSSOMStyleDeclaration.ModificationHandler, StyleDeclarationProvider {
   protected StyleDeclaration declaration;

   public CSSOMStoredStyleDeclaration(CSSEngine eng) {
      super((CSSOMStyleDeclaration.ValueProvider)null, (CSSRule)null, eng);
      this.valueProvider = this;
      this.setModificationHandler(this);
   }

   public StyleDeclaration getStyleDeclaration() {
      return this.declaration;
   }

   public void setStyleDeclaration(StyleDeclaration sd) {
      this.declaration = sd;
   }

   public Value getValue(String name) {
      int idx = this.cssEngine.getPropertyIndex(name);

      for(int i = 0; i < this.declaration.size(); ++i) {
         if (idx == this.declaration.getIndex(i)) {
            return this.declaration.getValue(i);
         }
      }

      return null;
   }

   public boolean isImportant(String name) {
      int idx = this.cssEngine.getPropertyIndex(name);

      for(int i = 0; i < this.declaration.size(); ++i) {
         if (idx == this.declaration.getIndex(i)) {
            return this.declaration.getPriority(i);
         }
      }

      return false;
   }

   public String getText() {
      return this.declaration.toString(this.cssEngine);
   }

   public int getLength() {
      return this.declaration.size();
   }

   public String item(int idx) {
      return this.cssEngine.getPropertyName(this.declaration.getIndex(idx));
   }
}
