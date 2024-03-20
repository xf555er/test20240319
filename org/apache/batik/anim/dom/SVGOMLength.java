package org.apache.batik.anim.dom;

public class SVGOMLength extends AbstractSVGLength {
   protected AbstractElement element;

   public SVGOMLength(AbstractElement elt) {
      super((short)0);
      this.element = elt;
   }

   protected SVGOMElement getAssociatedElement() {
      return (SVGOMElement)this.element;
   }
}
