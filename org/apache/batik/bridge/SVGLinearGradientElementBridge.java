package org.apache.batik.bridge;

import java.awt.Color;
import java.awt.Paint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.apache.batik.dom.svg.SVGContext;
import org.apache.batik.ext.awt.LinearGradientPaint;
import org.apache.batik.ext.awt.MultipleGradientPaint;
import org.apache.batik.gvt.GraphicsNode;
import org.w3c.dom.Element;

public class SVGLinearGradientElementBridge extends AbstractSVGGradientElementBridge {
   public String getLocalName() {
      return "linearGradient";
   }

   protected Paint buildGradient(Element paintElement, Element paintedElement, GraphicsNode paintedNode, MultipleGradientPaint.CycleMethodEnum spreadMethod, MultipleGradientPaint.ColorSpaceEnum colorSpace, AffineTransform transform, Color[] colors, float[] offsets, BridgeContext ctx) {
      String x1Str = SVGUtilities.getChainableAttributeNS(paintElement, (String)null, "x1", ctx);
      if (x1Str.length() == 0) {
         x1Str = "0%";
      }

      String y1Str = SVGUtilities.getChainableAttributeNS(paintElement, (String)null, "y1", ctx);
      if (y1Str.length() == 0) {
         y1Str = "0%";
      }

      String x2Str = SVGUtilities.getChainableAttributeNS(paintElement, (String)null, "x2", ctx);
      if (x2Str.length() == 0) {
         x2Str = "100%";
      }

      String y2Str = SVGUtilities.getChainableAttributeNS(paintElement, (String)null, "y2", ctx);
      if (y2Str.length() == 0) {
         y2Str = "0%";
      }

      String s = SVGUtilities.getChainableAttributeNS(paintElement, (String)null, "gradientUnits", ctx);
      short coordSystemType;
      if (s.length() == 0) {
         coordSystemType = 2;
      } else {
         coordSystemType = SVGUtilities.parseCoordinateSystem(paintElement, "gradientUnits", s, ctx);
      }

      SVGContext bridge = BridgeContext.getSVGContext(paintedElement);
      if (coordSystemType == 2 && bridge instanceof AbstractGraphicsNodeBridge) {
         Rectangle2D bbox = bridge.getBBox();
         if (bbox != null && (bbox.getWidth() == 0.0 || bbox.getHeight() == 0.0)) {
            return null;
         }
      }

      if (coordSystemType == 2) {
         transform = SVGUtilities.toObjectBBox(transform, paintedNode);
      }

      org.apache.batik.parser.UnitProcessor.Context uctx = UnitProcessor.createContext(ctx, paintElement);
      Point2D p1 = SVGUtilities.convertPoint(x1Str, "x1", y1Str, "y1", coordSystemType, uctx);
      Point2D p2 = SVGUtilities.convertPoint(x2Str, "x2", y2Str, "y2", coordSystemType, uctx);
      if (p1.getX() == p2.getX() && p1.getY() == p2.getY()) {
         return colors[colors.length - 1];
      } else {
         return new LinearGradientPaint(p1, p2, offsets, colors, spreadMethod, colorSpace, transform);
      }
   }
}
