package org.apache.batik.svggen;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import org.w3c.dom.Element;

public class SVGEllipse extends SVGGraphicObjectConverter {
   private SVGLine svgLine;

   public SVGEllipse(SVGGeneratorContext generatorContext) {
      super(generatorContext);
   }

   public Element toSVG(Ellipse2D ellipse) {
      if (!(ellipse.getWidth() < 0.0) && !(ellipse.getHeight() < 0.0)) {
         return ellipse.getWidth() == ellipse.getHeight() ? this.toSVGCircle(ellipse) : this.toSVGEllipse(ellipse);
      } else {
         return null;
      }
   }

   private Element toSVGCircle(Ellipse2D ellipse) {
      Element svgCircle = this.generatorContext.domFactory.createElementNS("http://www.w3.org/2000/svg", "circle");
      svgCircle.setAttributeNS((String)null, "cx", this.doubleString(ellipse.getX() + ellipse.getWidth() / 2.0));
      svgCircle.setAttributeNS((String)null, "cy", this.doubleString(ellipse.getY() + ellipse.getHeight() / 2.0));
      svgCircle.setAttributeNS((String)null, "r", this.doubleString(ellipse.getWidth() / 2.0));
      return svgCircle;
   }

   private Element toSVGEllipse(Ellipse2D ellipse) {
      if (ellipse.getWidth() > 0.0 && ellipse.getHeight() > 0.0) {
         Element svgCircle = this.generatorContext.domFactory.createElementNS("http://www.w3.org/2000/svg", "ellipse");
         svgCircle.setAttributeNS((String)null, "cx", this.doubleString(ellipse.getX() + ellipse.getWidth() / 2.0));
         svgCircle.setAttributeNS((String)null, "cy", this.doubleString(ellipse.getY() + ellipse.getHeight() / 2.0));
         svgCircle.setAttributeNS((String)null, "rx", this.doubleString(ellipse.getWidth() / 2.0));
         svgCircle.setAttributeNS((String)null, "ry", this.doubleString(ellipse.getHeight() / 2.0));
         return svgCircle;
      } else {
         Line2D.Double line;
         if (ellipse.getWidth() == 0.0 && ellipse.getHeight() > 0.0) {
            line = new Line2D.Double(ellipse.getX(), ellipse.getY(), ellipse.getX(), ellipse.getY() + ellipse.getHeight());
            if (this.svgLine == null) {
               this.svgLine = new SVGLine(this.generatorContext);
            }

            return this.svgLine.toSVG(line);
         } else if (ellipse.getWidth() > 0.0 && ellipse.getHeight() == 0.0) {
            line = new Line2D.Double(ellipse.getX(), ellipse.getY(), ellipse.getX() + ellipse.getWidth(), ellipse.getY());
            if (this.svgLine == null) {
               this.svgLine = new SVGLine(this.generatorContext);
            }

            return this.svgLine.toSVG(line);
         } else {
            return null;
         }
      }
   }
}
