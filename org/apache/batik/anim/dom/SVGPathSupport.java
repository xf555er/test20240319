package org.apache.batik.anim.dom;

import java.awt.geom.Point2D;
import org.apache.batik.dom.svg.SVGPathContext;
import org.w3c.dom.DOMException;
import org.w3c.dom.svg.SVGMatrix;
import org.w3c.dom.svg.SVGPoint;

public class SVGPathSupport {
   public static float getTotalLength(SVGOMPathElement path) {
      SVGPathContext pathCtx = (SVGPathContext)path.getSVGContext();
      return pathCtx.getTotalLength();
   }

   public static int getPathSegAtLength(SVGOMPathElement path, float x) {
      SVGPathContext pathCtx = (SVGPathContext)path.getSVGContext();
      return pathCtx.getPathSegAtLength(x);
   }

   public static SVGPoint getPointAtLength(final SVGOMPathElement path, final float distance) {
      final SVGPathContext pathCtx = (SVGPathContext)path.getSVGContext();
      return pathCtx == null ? null : new SVGPoint() {
         public float getX() {
            Point2D pt = pathCtx.getPointAtLength(distance);
            return (float)pt.getX();
         }

         public float getY() {
            Point2D pt = pathCtx.getPointAtLength(distance);
            return (float)pt.getY();
         }

         public void setX(float x) throws DOMException {
            throw path.createDOMException((short)7, "readonly.point", (Object[])null);
         }

         public void setY(float y) throws DOMException {
            throw path.createDOMException((short)7, "readonly.point", (Object[])null);
         }

         public SVGPoint matrixTransform(SVGMatrix matrix) {
            throw path.createDOMException((short)7, "readonly.point", (Object[])null);
         }
      };
   }
}
