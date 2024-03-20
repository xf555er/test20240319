package org.apache.batik.css.dom;

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStylableElement;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSStyleDeclaration;

public class CSSOMSVGViewCSS extends CSSOMViewCSS {
   public CSSOMSVGViewCSS(CSSEngine engine) {
      super(engine);
   }

   public CSSStyleDeclaration getComputedStyle(Element elt, String pseudoElt) {
      return elt instanceof CSSStylableElement ? new CSSOMSVGComputedStyle(this.cssEngine, (CSSStylableElement)elt, pseudoElt) : null;
   }
}
