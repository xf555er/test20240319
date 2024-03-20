package org.apache.batik.extension.svg;

import java.awt.geom.GeneralPath;
import org.apache.batik.bridge.Bridge;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.BridgeException;
import org.apache.batik.bridge.SVGDecoratedShapeElementBridge;
import org.apache.batik.bridge.SVGUtilities;
import org.apache.batik.gvt.ShapeNode;
import org.apache.batik.parser.UnitProcessor;
import org.w3c.dom.Element;

public class BatikRegularPolygonElementBridge extends SVGDecoratedShapeElementBridge implements BatikExtConstants {
   public String getNamespaceURI() {
      return "http://xml.apache.org/batik/ext";
   }

   public String getLocalName() {
      return "regularPolygon";
   }

   public Bridge getInstance() {
      return new BatikRegularPolygonElementBridge();
   }

   protected void buildShape(BridgeContext ctx, Element e, ShapeNode shapeNode) {
      UnitProcessor.Context uctx = org.apache.batik.bridge.UnitProcessor.createContext(ctx, e);
      String s = e.getAttributeNS((String)null, "cx");
      float cx = 0.0F;
      if (s.length() != 0) {
         cx = org.apache.batik.bridge.UnitProcessor.svgHorizontalCoordinateToUserSpace(s, "cx", uctx);
      }

      s = e.getAttributeNS((String)null, "cy");
      float cy = 0.0F;
      if (s.length() != 0) {
         cy = org.apache.batik.bridge.UnitProcessor.svgVerticalCoordinateToUserSpace(s, "cy", uctx);
      }

      s = e.getAttributeNS((String)null, "r");
      if (s.length() != 0) {
         float r = org.apache.batik.bridge.UnitProcessor.svgOtherLengthToUserSpace(s, "r", uctx);
         int sides = convertSides(e, "sides", 3, ctx);
         GeneralPath gp = new GeneralPath();

         for(int i = 0; i < sides; ++i) {
            double angle = ((double)i + 0.5) * (6.283185307179586 / (double)sides) - 1.5707963267948966;
            double x = (double)cx + (double)r * Math.cos(angle);
            double y = (double)cy - (double)r * Math.sin(angle);
            if (i == 0) {
               gp.moveTo((float)x, (float)y);
            } else {
               gp.lineTo((float)x, (float)y);
            }
         }

         gp.closePath();
         shapeNode.setShape(gp);
      } else {
         throw new BridgeException(ctx, e, "attribute.missing", new Object[]{"r", s});
      }
   }

   protected static int convertSides(Element filterElement, String attrName, int defaultValue, BridgeContext ctx) {
      String s = filterElement.getAttributeNS((String)null, attrName);
      if (s.length() == 0) {
         return defaultValue;
      } else {
         int ret = false;

         int ret;
         try {
            ret = SVGUtilities.convertSVGInteger(s);
         } catch (NumberFormatException var7) {
            throw new BridgeException(ctx, filterElement, var7, "attribute.malformed", new Object[]{attrName, s});
         }

         if (ret < 3) {
            throw new BridgeException(ctx, filterElement, "attribute.malformed", new Object[]{attrName, s});
         } else {
            return ret;
         }
      }
   }
}
