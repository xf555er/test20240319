package org.apache.batik.svggen;

import java.awt.BasicStroke;
import org.apache.batik.ext.awt.g2d.GraphicContext;

public class SVGBasicStroke extends AbstractSVGConverter {
   public SVGBasicStroke(SVGGeneratorContext generatorContext) {
      super(generatorContext);
   }

   public SVGDescriptor toSVG(GraphicContext gc) {
      return gc.getStroke() instanceof BasicStroke ? this.toSVG((BasicStroke)gc.getStroke()) : null;
   }

   public final SVGStrokeDescriptor toSVG(BasicStroke stroke) {
      String strokeWidth = this.doubleString((double)stroke.getLineWidth());
      String capStyle = endCapToSVG(stroke.getEndCap());
      String joinStyle = joinToSVG(stroke.getLineJoin());
      String miterLimit = this.doubleString((double)stroke.getMiterLimit());
      float[] array = stroke.getDashArray();
      String dashArray = null;
      if (array != null) {
         dashArray = this.dashArrayToSVG(array);
      } else {
         dashArray = "none";
      }

      String dashOffset = this.doubleString((double)stroke.getDashPhase());
      return new SVGStrokeDescriptor(strokeWidth, capStyle, joinStyle, miterLimit, dashArray, dashOffset);
   }

   private final String dashArrayToSVG(float[] dashArray) {
      StringBuffer dashArrayBuf = new StringBuffer(dashArray.length * 8);
      if (dashArray.length > 0) {
         dashArrayBuf.append(this.doubleString((double)dashArray[0]));
      }

      for(int i = 1; i < dashArray.length; ++i) {
         dashArrayBuf.append(",");
         dashArrayBuf.append(this.doubleString((double)dashArray[i]));
      }

      return dashArrayBuf.toString();
   }

   private static String joinToSVG(int lineJoin) {
      switch (lineJoin) {
         case 0:
         default:
            return "miter";
         case 1:
            return "round";
         case 2:
            return "bevel";
      }
   }

   private static String endCapToSVG(int endCap) {
      switch (endCap) {
         case 0:
            return "butt";
         case 1:
            return "round";
         case 2:
         default:
            return "square";
      }
   }
}
