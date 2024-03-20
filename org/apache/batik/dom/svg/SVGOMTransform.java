package org.apache.batik.dom.svg;

import java.awt.geom.AffineTransform;
import org.w3c.dom.DOMException;
import org.w3c.dom.svg.SVGMatrix;

public class SVGOMTransform extends AbstractSVGTransform {
   public SVGOMTransform() {
      this.affineTransform = new AffineTransform();
   }

   protected SVGMatrix createMatrix() {
      return new AbstractSVGMatrix() {
         protected AffineTransform getAffineTransform() {
            return SVGOMTransform.this.affineTransform;
         }

         public void setA(float a) throws DOMException {
            SVGOMTransform.this.setType((short)1);
            super.setA(a);
         }

         public void setB(float b) throws DOMException {
            SVGOMTransform.this.setType((short)1);
            super.setB(b);
         }

         public void setC(float c) throws DOMException {
            SVGOMTransform.this.setType((short)1);
            super.setC(c);
         }

         public void setD(float d) throws DOMException {
            SVGOMTransform.this.setType((short)1);
            super.setD(d);
         }

         public void setE(float e) throws DOMException {
            SVGOMTransform.this.setType((short)1);
            super.setE(e);
         }

         public void setF(float f) throws DOMException {
            SVGOMTransform.this.setType((short)1);
            super.setF(f);
         }
      };
   }
}
