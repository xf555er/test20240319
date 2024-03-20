package org.apache.batik.css.engine.value.svg;

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.value.AbstractValueFactory;
import org.apache.batik.css.engine.value.ShorthandManager;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;

public class MarkerShorthandManager extends AbstractValueFactory implements ShorthandManager {
   public String getPropertyName() {
      return "marker";
   }

   public boolean isAnimatableProperty() {
      return true;
   }

   public boolean isAdditiveProperty() {
      return false;
   }

   public void setValues(CSSEngine eng, ShorthandManager.PropertyHandler ph, LexicalUnit lu, boolean imp) throws DOMException {
      ph.property("marker-end", lu, imp);
      ph.property("marker-mid", lu, imp);
      ph.property("marker-start", lu, imp);
   }
}
