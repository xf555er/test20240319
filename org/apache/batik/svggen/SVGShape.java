package org.apache.batik.svggen;

import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import org.w3c.dom.Element;

public class SVGShape extends SVGGraphicObjectConverter {
   private SVGArc svgArc;
   private SVGEllipse svgEllipse;
   private SVGLine svgLine;
   private SVGPath svgPath;
   private SVGPolygon svgPolygon;
   private SVGRectangle svgRectangle;

   public SVGShape(SVGGeneratorContext generatorContext) {
      super(generatorContext);
      this.svgArc = new SVGArc(generatorContext);
      this.svgEllipse = new SVGEllipse(generatorContext);
      this.svgLine = new SVGLine(generatorContext);
      this.svgPath = new SVGPath(generatorContext);
      this.svgPolygon = new SVGPolygon(generatorContext);
      this.svgRectangle = new SVGRectangle(generatorContext);
   }

   public Element toSVG(Shape shape) {
      if (shape instanceof Polygon) {
         return this.svgPolygon.toSVG((Polygon)shape);
      } else if (shape instanceof Rectangle2D) {
         return this.svgRectangle.toSVG((Rectangle2D)shape);
      } else if (shape instanceof RoundRectangle2D) {
         return this.svgRectangle.toSVG((RoundRectangle2D)shape);
      } else if (shape instanceof Ellipse2D) {
         return this.svgEllipse.toSVG((Ellipse2D)shape);
      } else if (shape instanceof Line2D) {
         return this.svgLine.toSVG((Line2D)shape);
      } else {
         return shape instanceof Arc2D ? this.svgArc.toSVG((Arc2D)shape) : this.svgPath.toSVG(shape);
      }
   }
}
