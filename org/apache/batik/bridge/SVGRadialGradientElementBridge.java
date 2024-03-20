package org.apache.batik.bridge;

import java.awt.Color;
import java.awt.Paint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.apache.batik.dom.svg.SVGContext;
import org.apache.batik.ext.awt.MultipleGradientPaint;
import org.apache.batik.ext.awt.RadialGradientPaint;
import org.apache.batik.gvt.GraphicsNode;
import org.w3c.dom.Element;

public class SVGRadialGradientElementBridge extends AbstractSVGGradientElementBridge {
   public String getLocalName() {
      return "radialGradient";
   }

   protected Paint buildGradient(Element paintElement, Element paintedElement, GraphicsNode paintedNode, MultipleGradientPaint.CycleMethodEnum spreadMethod, MultipleGradientPaint.ColorSpaceEnum colorSpace, AffineTransform transform, Color[] colors, float[] offsets, BridgeContext ctx) {
      String cxStr = SVGUtilities.getChainableAttributeNS(paintElement, (String)null, "cx", ctx);
      if (cxStr.length() == 0) {
         cxStr = "50%";
      }

      String cyStr = SVGUtilities.getChainableAttributeNS(paintElement, (String)null, "cy", ctx);
      if (cyStr.length() == 0) {
         cyStr = "50%";
      }

      String rStr = SVGUtilities.getChainableAttributeNS(paintElement, (String)null, "r", ctx);
      if (rStr.length() == 0) {
         rStr = "50%";
      }

      String fxStr = SVGUtilities.getChainableAttributeNS(paintElement, (String)null, "fx", ctx);
      if (fxStr.length() == 0) {
         fxStr = cxStr;
      }

      String fyStr = SVGUtilities.getChainableAttributeNS(paintElement, (String)null, "fy", ctx);
      if (fyStr.length() == 0) {
         fyStr = cyStr;
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
      float r = SVGUtilities.convertLength(rStr, "r", coordSystemType, uctx);
      if (r == 0.0F) {
         return colors[colors.length - 1];
      } else {
         Point2D c = SVGUtilities.convertPoint(cxStr, "cx", cyStr, "cy", coordSystemType, uctx);
         Point2D f = SVGUtilities.convertPoint(fxStr, "fx", fyStr, "fy", coordSystemType, uctx);
         return new RadialGradientPaint(c, r, f, offsets, colors, spreadMethod, RadialGradientPaint.SRGB, transform);
      }
   }
}
