package org.apache.fop.render.ps.svg;

import java.awt.Graphics;
import java.awt.Paint;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.util.Iterator;
import org.apache.batik.ext.awt.LinearGradientPaint;
import org.apache.batik.ext.awt.RadialGradientPaint;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.render.gradient.Function;
import org.apache.fop.render.gradient.GradientMaker;
import org.apache.fop.render.gradient.Pattern;
import org.apache.fop.render.gradient.Shading;
import org.apache.xmlgraphics.java2d.ps.PSGraphics2D;
import org.apache.xmlgraphics.ps.PSGenerator;

public class PSSVGGraphics2D extends PSGraphics2D {
   private static final Log LOG = LogFactory.getLog(PSSVGGraphics2D.class);

   public PSSVGGraphics2D(boolean textAsShapes) {
      super(textAsShapes);
   }

   public PSSVGGraphics2D(boolean textAsShapes, PSGenerator gen) {
      super(textAsShapes, gen);
   }

   public PSSVGGraphics2D(PSGraphics2D g) {
      super(g);
   }

   protected void applyPaint(Paint paint, boolean fill) {
      super.applyPaint(paint, fill);
      Pattern pattern;
      if (paint instanceof LinearGradientPaint) {
         pattern = GradientMaker.makeLinearGradient((LinearGradientPaint)paint, new AffineTransform(), new AffineTransform());

         try {
            this.gen.write(this.outputPattern(pattern));
         } catch (IOException var6) {
            this.handleIOException(var6);
         }
      } else if (paint instanceof RadialGradientPaint) {
         pattern = GradientMaker.makeRadialGradient((RadialGradientPaint)paint, new AffineTransform(), new AffineTransform());

         try {
            this.gen.write(this.outputPattern(pattern));
         } catch (IOException var5) {
            this.handleIOException(var5);
         }
      }

   }

   private String outputPattern(Pattern pattern) {
      StringBuilder p = new StringBuilder(64);
      p.append("/Pattern setcolorspace\n");
      p.append("<< \n/Type /Pattern \n");
      p.append("/PatternType " + pattern.getPatternType() + " \n");
      if (pattern.getShading() != null) {
         p.append("/Shading ");
         this.outputShading(p, pattern.getShading());
         p.append(" \n");
      }

      p.append(">> \n");
      p.append("[ ");
      Iterator var3 = pattern.getMatrix().iterator();

      while(var3.hasNext()) {
         double m = (Double)var3.next();
         p.append(this.getPSGenerator().formatDouble(m));
         p.append(" ");
      }

      p.append("] ");
      p.append("makepattern setcolor\n");
      return p.toString();
   }

   private void outputShading(StringBuilder out, Shading shading) {
      final GradientMaker.DoubleFormatter doubleFormatter = new GradientMaker.DoubleFormatter() {
         public String formatDouble(double d) {
            return PSSVGGraphics2D.this.getPSGenerator().formatDouble(d);
         }
      };
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

   public Graphics create() {
      this.preparePainting();
      return new PSSVGGraphics2D(this);
   }
}
