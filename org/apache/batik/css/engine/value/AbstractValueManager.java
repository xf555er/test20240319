package org.apache.batik.css.engine.value;

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.StyleMap;
import org.w3c.dom.DOMException;

public abstract class AbstractValueManager extends AbstractValueFactory implements ValueManager {
   public Value createFloatValue(short unitType, float floatValue) throws DOMException {
      throw this.createDOMException();
   }

   public Value createStringValue(short type, String value, CSSEngine engine) throws DOMException {
      throw this.createDOMException();
   }

   public Value computeValue(CSSStylableElement elt, String pseudo, CSSEngine engine, int idx, StyleMap sm, Value value) {
      return (Value)(value.getCssValueType() == 1 && value.getPrimitiveType() == 20 ? new URIValue(value.getStringValue(), value.getStringValue()) : value);
   }
}
