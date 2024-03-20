package org.apache.batik.bridge;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.filter.Mask;
import org.apache.batik.gvt.filter.MaskRable8Bit;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class SVGMaskElementBridge extends AnimatableGenericSVGBridge implements MaskBridge {
   public String getLocalName() {
      return "mask";
   }

   public Mask createMask(BridgeContext ctx, Element maskElement, Element maskedElement, GraphicsNode maskedNode) {
      Rectangle2D maskRegion = SVGUtilities.convertMaskRegion(maskElement, maskedElement, maskedNode, ctx);
      GVTBuilder builder = ctx.getGVTBuilder();
      CompositeGraphicsNode maskNode = new CompositeGraphicsNode();
      CompositeGraphicsNode maskNodeContent = new CompositeGraphicsNode();
      maskNode.getChildren().add(maskNodeContent);
      boolean hasChildren = false;

      for(Node node = maskElement.getFirstChild(); node != null; node = node.getNextSibling()) {
         if (node.getNodeType() == 1) {
            Element child = (Element)node;
            GraphicsNode gn = builder.build(ctx, child);
            if (gn != null) {
               hasChildren = true;
               maskNodeContent.getChildren().add(gn);
            }
         }
      }

      if (!hasChildren) {
         return null;
      } else {
         String s = maskElement.getAttributeNS((String)null, "transform");
         AffineTransform Tx;
         if (s.length() != 0) {
            Tx = SVGUtilities.convertTransform(maskElement, "transform", s, ctx);
         } else {
            Tx = new AffineTransform();
         }

         s = maskElement.getAttributeNS((String)null, "maskContentUnits");
         short coordSystemType;
         if (s.length() == 0) {
            coordSystemType = 1;
         } else {
            coordSystemType = SVGUtilities.parseCoordinateSystem(maskElement, "maskContentUnits", s, ctx);
         }

         if (coordSystemType == 2) {
            Tx = SVGUtilities.toObjectBBox(Tx, maskedNode);
         }

         maskNodeContent.setTransform(Tx);
         Filter filter = maskedNode.getFilter();
         if (filter == null) {
            filter = maskedNode.getGraphicsNodeRable(true);
         }

         return new MaskRable8Bit(filter, maskNode, maskRegion);
      }
   }
}
