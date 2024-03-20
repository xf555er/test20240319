package org.apache.fop.render.gradient;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.batik.ext.awt.LinearGradientPaint;
import org.apache.batik.ext.awt.MultipleGradientPaint;
import org.apache.batik.ext.awt.RadialGradientPaint;
import org.apache.fop.pdf.PDFDeviceColorSpace;
import org.apache.xmlgraphics.java2d.color.ColorUtil;

public final class GradientMaker {
   private GradientMaker() {
   }

   public static Pattern makeLinearGradient(LinearGradientPaint gp, AffineTransform baseTransform, AffineTransform transform) {
      Point2D startPoint = gp.getStartPoint();
      Point2D endPoint = gp.getEndPoint();
      List coords = new ArrayList(4);
      coords.add(startPoint.getX());
      coords.add(startPoint.getY());
      coords.add(endPoint.getX());
      coords.add(endPoint.getY());
      return makeGradient(gp, coords, baseTransform, transform);
   }

   public static Pattern makeRadialGradient(RadialGradientPaint gradient, AffineTransform baseTransform, AffineTransform transform) {
      double radius = (double)gradient.getRadius();
      Point2D center = gradient.getCenterPoint();
      Point2D focus = gradient.getFocusPoint();
      double dx = focus.getX() - center.getX();
      double dy = focus.getY() - center.getY();
      double d = Math.sqrt(dx * dx + dy * dy);
      if (d > radius) {
         double scale = radius * 0.9999 / d;
         dx *= scale;
         dy *= scale;
      }

      List coords = new ArrayList(6);
      coords.add(center.getX() + dx);
      coords.add(center.getY() + dy);
      coords.add(0.0);
      coords.add(center.getX());
      coords.add(center.getY());
      coords.add(radius);
      return makeGradient(gradient, coords, baseTransform, transform);
   }

   private static Pattern makeGradient(MultipleGradientPaint gradient, List coords, AffineTransform baseTransform, AffineTransform transform) {
      List matrix = makeTransform(gradient, baseTransform, transform);
      List bounds = makeBounds(gradient);
      List functions = makeFunctions(gradient);
      PDFDeviceColorSpace colorSpace = new PDFDeviceColorSpace(2);
      Function function = new Function((List)null, (List)null, functions, bounds, (List)null);
      int shadingType = gradient instanceof LinearGradientPaint ? 2 : 3;
      Shading shading = new Shading(shadingType, colorSpace, coords, function);
      return new Pattern(2, shading, matrix);
   }

   private static List makeTransform(MultipleGradientPaint gradient, AffineTransform baseTransform, AffineTransform transform) {
      AffineTransform gradientTransform = new AffineTransform(baseTransform);
      gradientTransform.concatenate(transform);
      gradientTransform.concatenate(gradient.getTransform());
      List matrix = new ArrayList(6);
      double[] m = new double[6];
      gradientTransform.getMatrix(m);
      double[] var6 = m;
      int var7 = m.length;

      for(int var8 = 0; var8 < var7; ++var8) {
         double d = var6[var8];
         matrix.add(d);
      }

      return matrix;
   }

   private static Color getsRGBColor(Color c) {
      return c.getColorSpace().isCS_sRGB() ? c : ColorUtil.toSRGBColor(c);
   }

   private static List makeBounds(MultipleGradientPaint gradient) {
      float[] fractions = gradient.getFractions();
      List bounds = new ArrayList(fractions.length);
      float[] var3 = fractions;
      int var4 = fractions.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         float offset = var3[var5];
         if (0.0F < offset) {
            bounds.add(offset);
         }
      }

      float last = (Float)bounds.get(bounds.size() - 1);
      if (last == 1.0F) {
         bounds.remove(bounds.size() - 1);
      }

      return bounds;
   }

   private static List makeFunctions(MultipleGradientPaint gradient) {
      List colors = makeColors(gradient);
      List functions = new ArrayList();
      int currentPosition = 0;

      for(int lastPosition = colors.size() - 1; currentPosition < lastPosition; ++currentPosition) {
         Color currentColor = (Color)colors.get(currentPosition);
         Color nextColor = (Color)colors.get(currentPosition + 1);
         float[] c0 = currentColor.getColorComponents((float[])null);
         float[] c1 = nextColor.getColorComponents((float[])null);
         Function function = new Function((List)null, (List)null, c0, c1, 1.0);
         functions.add(function);
      }

      return functions;
   }

   private static List makeColors(MultipleGradientPaint gradient) {
      Color[] svgColors = gradient.getColors();
      List gradientColors = new ArrayList(svgColors.length + 2);
      float[] fractions = gradient.getFractions();
      if (fractions[0] > 0.0F) {
         gradientColors.add(getsRGBColor(svgColors[0]));
      }

      Color[] var4 = svgColors;
      int var5 = svgColors.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         Color c = var4[var6];
         gradientColors.add(getsRGBColor(c));
      }

      if (fractions[fractions.length - 1] < 1.0F) {
         gradientColors.add(getsRGBColor(svgColors[svgColors.length - 1]));
      }

      return gradientColors;
   }

   static void outputDoubles(StringBuilder out, DoubleFormatter doubleFormatter, List numbers) {
      out.append("[ ");
      Iterator var3 = numbers.iterator();

      while(var3.hasNext()) {
         Number n = (Number)var3.next();
         out.append(doubleFormatter.formatDouble(n.doubleValue()));
         out.append(" ");
      }

      out.append("]");
   }

   public interface DoubleFormatter {
      String formatDouble(double var1);
   }
}
