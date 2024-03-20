package org.apache.batik.svggen;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import org.w3c.dom.Element;

public class SVGPath extends SVGGraphicObjectConverter {
   public SVGPath(SVGGeneratorContext generatorContext) {
      super(generatorContext);
   }

   public Element toSVG(Shape path) {
      String dAttr = toSVGPathData(path, this.generatorContext);
      if (dAttr != null && dAttr.length() != 0) {
         Element svgPath = this.generatorContext.domFactory.createElementNS("http://www.w3.org/2000/svg", "path");
         svgPath.setAttributeNS((String)null, "d", dAttr);
         if (path.getPathIterator((AffineTransform)null).getWindingRule() == 0) {
            svgPath.setAttributeNS((String)null, "fill-rule", "evenodd");
         }

         return svgPath;
      } else {
         return null;
      }
   }

   public static String toSVGPathData(Shape path, SVGGeneratorContext gc) {
      StringBuffer d = new StringBuffer(40);
      PathIterator pi = path.getPathIterator((AffineTransform)null);
      float[] seg = new float[6];

      for(int segType = false; !pi.isDone(); pi.next()) {
         int segType = pi.currentSegment(seg);
         switch (segType) {
            case 0:
               d.append("M");
               appendPoint(d, seg[0], seg[1], gc);
               break;
            case 1:
               d.append("L");
               appendPoint(d, seg[0], seg[1], gc);
               break;
            case 2:
               d.append("Q");
               appendPoint(d, seg[0], seg[1], gc);
               appendPoint(d, seg[2], seg[3], gc);
               break;
            case 3:
               d.append("C");
               appendPoint(d, seg[0], seg[1], gc);
               appendPoint(d, seg[2], seg[3], gc);
               appendPoint(d, seg[4], seg[5], gc);
               break;
            case 4:
               d.append("Z");
               break;
            default:
               throw new RuntimeException("invalid segmentType:" + segType);
         }
      }

      if (d.length() > 0) {
         return d.toString().trim();
      } else {
         return "";
      }
   }

   private static void appendPoint(StringBuffer d, float x, float y, SVGGeneratorContext gc) {
      d.append(gc.doubleString((double)x));
      d.append(" ");
      d.append(gc.doubleString((double)y));
      d.append(" ");
   }
}
