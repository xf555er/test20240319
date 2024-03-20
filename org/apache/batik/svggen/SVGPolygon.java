package org.apache.batik.svggen;

import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import org.w3c.dom.Element;

public class SVGPolygon extends SVGGraphicObjectConverter {
   public SVGPolygon(SVGGeneratorContext generatorContext) {
      super(generatorContext);
   }

   public Element toSVG(Polygon polygon) {
      Element svgPolygon = this.generatorContext.domFactory.createElementNS("http://www.w3.org/2000/svg", "polygon");
      StringBuffer points = new StringBuffer(" ");
      PathIterator pi = polygon.getPathIterator((AffineTransform)null);

      for(float[] seg = new float[6]; !pi.isDone(); pi.next()) {
         int segType = pi.currentSegment(seg);
         switch (segType) {
            case 0:
               this.appendPoint(points, seg[0], seg[1]);
               break;
            case 1:
               this.appendPoint(points, seg[0], seg[1]);
               break;
            case 2:
            case 3:
            default:
               throw new RuntimeException("invalid segmentType:" + segType);
            case 4:
         }
      }

      svgPolygon.setAttributeNS((String)null, "points", points.substring(0, points.length() - 1));
      return svgPolygon;
   }

   private void appendPoint(StringBuffer points, float x, float y) {
      points.append(this.doubleString((double)x));
      points.append(" ");
      points.append(this.doubleString((double)y));
      points.append(" ");
   }
}
