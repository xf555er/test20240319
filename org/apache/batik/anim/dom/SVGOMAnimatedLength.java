package org.apache.batik.anim.dom;

public class SVGOMAnimatedLength extends AbstractSVGAnimatedLength {
   protected String defaultValue;

   public SVGOMAnimatedLength(AbstractElement elt, String ns, String ln, String def, short dir, boolean nonneg) {
      super(elt, ns, ln, dir, nonneg);
      this.defaultValue = def;
   }

   protected String getDefaultValue() {
      return this.defaultValue;
   }
}
