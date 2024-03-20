package org.apache.batik.css.engine.value.svg12;

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.value.LengthManager;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.svg.SVGValueConstants;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;

public class MarginLengthManager extends LengthManager {
   protected String prop;

   public MarginLengthManager(String prop) {
      this.prop = prop;
   }

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
      return this.prop;
   }

   public Value getDefaultValue() {
      return SVGValueConstants.NUMBER_0;
   }

   public Value createValue(LexicalUnit lu, CSSEngine engine) throws DOMException {
      return lu.getLexicalUnitType() == 12 ? SVGValueConstants.INHERIT_VALUE : super.createValue(lu, engine);
   }

   protected int getOrientation() {
      return 0;
   }
}
