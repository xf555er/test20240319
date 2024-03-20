package org.apache.fop.render.ps;

import java.util.Iterator;
import java.util.List;
import org.apache.fop.render.gradient.Function;
import org.apache.fop.render.gradient.GradientMaker;
import org.apache.fop.render.gradient.Pattern;
import org.apache.fop.render.gradient.Shading;

public final class Gradient {
   private Gradient() {
   }

   public static String outputPattern(Pattern pattern, GradientMaker.DoubleFormatter doubleFormatter) {
      StringBuilder p = new StringBuilder(64);
      p.append("/Pattern setcolorspace\n");
      p.append("<< \n/Type /Pattern \n");
      p.append("/PatternType " + pattern.getPatternType() + " \n");
      if (pattern.getShading() != null) {
         p.append("/Shading ");
         outputShading(p, pattern.getShading(), doubleFormatter);
         p.append(" \n");
      }

      p.append(">> \n");
      List matrix = pattern.getMatrix();
      if (matrix == null) {
         p.append("matrix ");
      } else {
         p.append("[ ");
         Iterator var4 = pattern.getMatrix().iterator();

         while(var4.hasNext()) {
            double m = (Double)var4.next();
            p.append(doubleFormatter.formatDouble(m));
            p.append(" ");
         }

         p.append("] ");
      }

      p.append("makepattern setcolor\n");
      return p.toString();
   }

   private static void outputShading(StringBuilder out, Shading shading, final GradientMaker.DoubleFormatter doubleFormatter) {
      final Function function = shading.getFunction();
      Shading.FunctionRenderer functionRenderer = new Shading.FunctionRenderer() {
         public void outputFunction(StringBuilder out) {
            Function.SubFunctionRenderer subFunctionRenderer = new Function.SubFunctionRenderer() {
               public void outputFunction(StringBuilder out, int functionIndex) {
                  Function subFunction = (Function)function.getFunctions().get(functionIndex);

                  assert subFunction.getFunctions().isEmpty();

                  subFunction.output(out, doubleFormatter, (Function.SubFunctionRenderer)null);
               }
            };
            function.output(out, doubleFormatter, subFunctionRenderer);
         }
      };
      shading.output(out, doubleFormatter, functionRenderer);
   }
}
