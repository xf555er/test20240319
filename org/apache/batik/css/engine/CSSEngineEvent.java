package org.apache.batik.css.engine;

import java.util.EventObject;
import org.w3c.dom.Element;

public class CSSEngineEvent extends EventObject {
   protected Element element;
   protected int[] properties;

   public CSSEngineEvent(CSSEngine source, Element elt, int[] props) {
      super(source);
      this.element = elt;
      this.properties = props;
   }

   public Element getElement() {
      return this.element;
   }

   public int[] getProperties() {
      return this.properties;
   }
}
