package org.apache.batik.svggen;

import java.awt.geom.Line2D;
import org.w3c.dom.Element;

public class SVGLine extends SVGGraphicObjectConverter {
   public SVGLine(SVGGeneratorContext generatorContext) {
      super(generatorContext);
   }

   public Element toSVG(Line2D line) {
      Element svgLine = this.generatorContext.domFactory.createElementNS("http://www.w3.org/2000/svg", "line");
      svgLine.setAttributeNS((String)null, "x1", this.doubleString(line.getX1()));
      svgLine.setAttributeNS((String)null, "y1", this.doubleString(line.getY1()));
      svgLine.setAttributeNS((String)null, "x2", this.doubleString(line.getX2()));
      svgLine.setAttributeNS((String)null, "y2", this.doubleString(line.getY2()));
      return svgLine;
   }
}
