package org.apache.batik.css.dom;

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.svg.SVGColorManager;
import org.apache.batik.css.engine.value.svg.SVGPaintManager;
import org.w3c.dom.css.CSSValue;

public class CSSOMSVGComputedStyle extends CSSOMComputedStyle {
   public CSSOMSVGComputedStyle(CSSEngine e, CSSStylableElement elt, String pseudoElt) {
      super(e, elt, pseudoElt);
   }

   protected CSSValue createCSSValue(int idx) {
      if (idx > 59) {
         if (this.cssEngine.getValueManagers()[idx] instanceof SVGPaintManager) {
            return new ComputedCSSPaintValue(idx);
         }

         if (this.cssEngine.getValueManagers()[idx] instanceof SVGColorManager) {
            return new ComputedCSSColorValue(idx);
         }
      } else {
         switch (idx) {
            case 15:
            case 45:
               return new ComputedCSSPaintValue(idx);
            case 19:
            case 33:
            case 43:
               return new ComputedCSSColorValue(idx);
         }
      }

      return super.createCSSValue(idx);
   }

   public class ComputedCSSPaintValue extends CSSOMSVGPaint implements CSSOMSVGColor.ValueProvider {
      protected int index;

      public ComputedCSSPaintValue(int idx) {
         super((CSSOMSVGColor.ValueProvider)null);
         this.valueProvider = this;
         this.index = idx;
      }

      public Value getValue() {
         return CSSOMSVGComputedStyle.this.cssEngine.getComputedStyle(CSSOMSVGComputedStyle.this.element, CSSOMSVGComputedStyle.this.pseudoElement, this.index);
      }
   }

   protected class ComputedCSSColorValue extends CSSOMSVGColor implements CSSOMSVGColor.ValueProvider {
      protected int index;

      public ComputedCSSColorValue(int idx) {
         super((CSSOMSVGColor.ValueProvider)null);
         this.valueProvider = this;
         this.index = idx;
      }

      public Value getValue() {
         return CSSOMSVGComputedStyle.this.cssEngine.getComputedStyle(CSSOMSVGComputedStyle.this.element, CSSOMSVGComputedStyle.this.pseudoElement, this.index);
      }
   }
}
