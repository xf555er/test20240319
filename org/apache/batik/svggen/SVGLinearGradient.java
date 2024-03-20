package org.apache.batik.svggen;

import java.awt.GradientPaint;
import java.awt.Paint;
import java.awt.geom.Point2D;
import org.apache.batik.ext.awt.g2d.GraphicContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SVGLinearGradient extends AbstractSVGConverter {
   public SVGLinearGradient(SVGGeneratorContext generatorContext) {
      super(generatorContext);
   }

   public SVGDescriptor toSVG(GraphicContext gc) {
      Paint paint = gc.getPaint();
      return this.toSVG((GradientPaint)paint);
   }

   public SVGPaintDescriptor toSVG(GradientPaint gradient) {
      SVGPaintDescriptor gradientDesc = (SVGPaintDescriptor)this.descMap.get(gradient);
      Document domFactory = this.generatorContext.domFactory;
      if (gradientDesc == null) {
         Element gradientDef = domFactory.createElementNS("http://www.w3.org/2000/svg", "linearGradient");
         gradientDef.setAttributeNS((String)null, "gradientUnits", "userSpaceOnUse");
         Point2D p1 = gradient.getPoint1();
         Point2D p2 = gradient.getPoint2();
         gradientDef.setAttributeNS((String)null, "x1", this.doubleString(p1.getX()));
         gradientDef.setAttributeNS((String)null, "y1", this.doubleString(p1.getY()));
         gradientDef.setAttributeNS((String)null, "x2", this.doubleString(p2.getX()));
         gradientDef.setAttributeNS((String)null, "y2", this.doubleString(p2.getY()));
         String spreadMethod = "pad";
         if (gradient.isCyclic()) {
            spreadMethod = "reflect";
         }

         gradientDef.setAttributeNS((String)null, "spreadMethod", spreadMethod);
         Element gradientStop = domFactory.createElementNS("http://www.w3.org/2000/svg", "stop");
         gradientStop.setAttributeNS((String)null, "offset", "0%");
         SVGPaintDescriptor colorDesc = SVGColor.toSVG(gradient.getColor1(), this.generatorContext);
         gradientStop.setAttributeNS((String)null, "stop-color", colorDesc.getPaintValue());
         gradientStop.setAttributeNS((String)null, "stop-opacity", colorDesc.getOpacityValue());
         gradientDef.appendChild(gradientStop);
         gradientStop = domFactory.createElementNS("http://www.w3.org/2000/svg", "stop");
         gradientStop.setAttributeNS((String)null, "offset", "100%");
         colorDesc = SVGColor.toSVG(gradient.getColor2(), this.generatorContext);
         gradientStop.setAttributeNS((String)null, "stop-color", colorDesc.getPaintValue());
         gradientStop.setAttributeNS((String)null, "stop-opacity", colorDesc.getOpacityValue());
         gradientDef.appendChild(gradientStop);
         gradientDef.setAttributeNS((String)null, "id", this.generatorContext.idGenerator.generateID("linearGradient"));
         StringBuffer paintAttrBuf = new StringBuffer("url(");
         paintAttrBuf.append("#");
         paintAttrBuf.append(gradientDef.getAttributeNS((String)null, "id"));
         paintAttrBuf.append(")");
         gradientDesc = new SVGPaintDescriptor(paintAttrBuf.toString(), "1", gradientDef);
         this.descMap.put(gradient, gradientDesc);
         this.defSet.add(gradientDef);
      }

      return gradientDesc;
   }
}
