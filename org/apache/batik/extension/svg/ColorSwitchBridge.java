package org.apache.batik.extension.svg;

import java.awt.Color;
import java.awt.Paint;
import org.apache.batik.bridge.AbstractSVGBridge;
import org.apache.batik.bridge.Bridge;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.PaintBridge;
import org.apache.batik.bridge.SVGUtilities;
import org.apache.batik.gvt.GraphicsNode;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ColorSwitchBridge extends AbstractSVGBridge implements PaintBridge, BatikExtConstants {
   public String getNamespaceURI() {
      return "http://xml.apache.org/batik/ext";
   }

   public String getLocalName() {
      return "colorSwitch";
   }

   public Paint createPaint(BridgeContext ctx, Element paintElement, Element paintedElement, GraphicsNode paintedNode, float opacity) {
      Element clrDef = null;

      for(Node n = paintElement.getFirstChild(); n != null; n = n.getNextSibling()) {
         if (n.getNodeType() == 1) {
            Element ref = (Element)n;
            if (SVGUtilities.matchUserAgent(ref, ctx.getUserAgent())) {
               clrDef = ref;
               break;
            }
         }
      }

      if (clrDef == null) {
         return Color.black;
      } else {
         Bridge bridge = ctx.getBridge(clrDef);
         return (Paint)(bridge != null && bridge instanceof PaintBridge ? ((PaintBridge)bridge).createPaint(ctx, clrDef, paintedElement, paintedNode, opacity) : Color.black);
      }
   }
}
