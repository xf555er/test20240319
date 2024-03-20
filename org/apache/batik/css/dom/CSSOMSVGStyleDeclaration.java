package org.apache.batik.css.dom;

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.svg.SVGColorManager;
import org.apache.batik.css.engine.value.svg.SVGPaintManager;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSValue;

public class CSSOMSVGStyleDeclaration extends CSSOMStyleDeclaration {
   protected CSSEngine cssEngine;

   public CSSOMSVGStyleDeclaration(CSSOMStyleDeclaration.ValueProvider vp, CSSRule parent, CSSEngine eng) {
      super(vp, parent);
      this.cssEngine = eng;
   }

   protected CSSValue createCSSValue(String name) {
      int idx = this.cssEngine.getPropertyIndex(name);
      if (idx > 59) {
         if (this.cssEngine.getValueManagers()[idx] instanceof SVGPaintManager) {
            return new StyleDeclarationPaintValue(name);
         }

         if (this.cssEngine.getValueManagers()[idx] instanceof SVGColorManager) {
            return new StyleDeclarationColorValue(name);
         }
      } else {
         switch (idx) {
            case 15:
            case 45:
               return new StyleDeclarationPaintValue(name);
            case 19:
            case 33:
            case 43:
               return new StyleDeclarationColorValue(name);
         }
      }

      return super.createCSSValue(name);
   }

   public class StyleDeclarationPaintValue extends CSSOMSVGPaint implements CSSOMSVGColor.ValueProvider {
      protected String property;

      public StyleDeclarationPaintValue(String prop) {
         super((CSSOMSVGColor.ValueProvider)null);
         this.valueProvider = this;
         this.setModificationHandler(new CSSOMSVGPaint.AbstractModificationHandler() {
            protected Value getValue() {
               return StyleDeclarationPaintValue.this.getValue();
            }

            public void textChanged(String text) throws DOMException {
               if (StyleDeclarationPaintValue.this.handler == null) {
                  throw new DOMException((short)7, "");
               } else {
                  String prio = CSSOMSVGStyleDeclaration.this.getPropertyPriority(StyleDeclarationPaintValue.this.property);
                  CSSOMSVGStyleDeclaration.this.handler.propertyChanged(StyleDeclarationPaintValue.this.property, text, prio);
               }
            }
         });
         this.property = prop;
      }

      public Value getValue() {
         return CSSOMSVGStyleDeclaration.this.valueProvider.getValue(this.property);
      }
   }

   public class StyleDeclarationColorValue extends CSSOMSVGColor implements CSSOMSVGColor.ValueProvider {
      protected String property;

      public StyleDeclarationColorValue(String prop) {
         super((CSSOMSVGColor.ValueProvider)null);
         this.valueProvider = this;
         this.setModificationHandler(new CSSOMSVGColor.AbstractModificationHandler() {
            protected Value getValue() {
               return StyleDeclarationColorValue.this.getValue();
            }

            public void textChanged(String text) throws DOMException {
               if (StyleDeclarationColorValue.this.handler == null) {
                  throw new DOMException((short)7, "");
               } else {
                  String prio = CSSOMSVGStyleDeclaration.this.getPropertyPriority(StyleDeclarationColorValue.this.property);
                  CSSOMSVGStyleDeclaration.this.handler.propertyChanged(StyleDeclarationColorValue.this.property, text, prio);
               }
            }
         });
         this.property = prop;
      }

      public Value getValue() {
         return CSSOMSVGStyleDeclaration.this.valueProvider.getValue(this.property);
      }
   }
}
