package org.apache.batik.css.engine.value.svg;

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.value.LengthManager;
import org.apache.batik.css.engine.value.Value;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;

public class StrokeDashoffsetManager extends LengthManager {
   public boolean isInheritedProperty() {
      return true;
   }

   public boolean isAnimatableProperty() {
      return true;
   }

   public boolean isAdditiveProperty() {
      return true;
   }

   public int getPropertyType() {
      return 17;
   }

   public String getPropertyName() {
      return "stroke-dashoffset";
   }

   public Value getDefaultValue() {
      return SVGValueConstants.NUMBER_0;
   }

   public Value createValue(LexicalUnit lu, CSSEngine engine) throws DOMException {
      return lu.getLexicalUnitType() == 12 ? SVGValueConstants.INHERIT_VALUE : super.createValue(lu, engine);
   }

   protected int getOrientation() {
      return 2;
   }
}
