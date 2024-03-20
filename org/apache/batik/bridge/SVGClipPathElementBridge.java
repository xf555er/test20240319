package org.apache.batik.bridge;

import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import org.apache.batik.anim.dom.SVGOMUseElement;
import org.apache.batik.ext.awt.image.renderable.ClipRable;
import org.apache.batik.ext.awt.image.renderable.ClipRable8Bit;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.ShapeNode;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class SVGClipPathElementBridge extends AnimatableGenericSVGBridge implements ClipBridge {
   public String getLocalName() {
      return "clipPath";
   }

   public ClipRable createClip(BridgeContext ctx, Element clipElement, Element clipedElement, GraphicsNode clipedNode) {
      String s = clipElement.getAttributeNS((String)null, "transform");
      AffineTransform Tx;
      if (s.length() != 0) {
         Tx = SVGUtilities.convertTransform(clipElement, "transform", s, ctx);
      } else {
         Tx = new AffineTransform();
      }

      s = clipElement.getAttributeNS((String)null, "clipPathUnits");
      short coordSystemType;
      if (s.length() == 0) {
         coordSystemType = 1;
      } else {
         coordSystemType = SVGUtilities.parseCoordinateSystem(clipElement, "clipPathUnits", s, ctx);
      }

      if (coordSystemType == 2) {
         Tx = SVGUtilities.toObjectBBox(Tx, clipedNode);
      }

      Area clipPath = new Area();
      GVTBuilder builder = ctx.getGVTBuilder();
      boolean hasChildren = false;

      for(Node node = clipElement.getFirstChild(); node != null; node = node.getNextSibling()) {
         if (node.getNodeType() == 1) {
            Element child = (Element)node;
            GraphicsNode clipNode = builder.build(ctx, child);
            if (clipNode != null) {
               hasChildren = true;
               if (child instanceof SVGOMUseElement) {
                  Node shadowChild = ((SVGOMUseElement)child).getCSSFirstChild();
                  if (shadowChild != null && shadowChild.getNodeType() == 1) {
                     child = (Element)shadowChild;
                  }
               }

               int wr = CSSUtilities.convertClipRule(child);
               GeneralPath path = new GeneralPath(clipNode.getOutline());
               path.setWindingRule(wr);
               AffineTransform at = clipNode.getTransform();
               if (at == null) {
                  at = Tx;
               } else {
                  at.preConcatenate(Tx);
               }

               Shape outline = at.createTransformedShape(path);
               ShapeNode outlineNode = new ShapeNode();
               outlineNode.setShape((Shape)outline);
               ClipRable clip = CSSUtilities.convertClipPath(child, outlineNode, ctx);
               if (clip != null) {
                  Area area = new Area((Shape)outline);
                  area.subtract(new Area(clip.getClipPath()));
                  outline = area;
               }

               clipPath.add(new Area((Shape)outline));
            }
         }
      }

      if (!hasChildren) {
         return null;
      } else {
         ShapeNode clipPathNode = new ShapeNode();
         clipPathNode.setShape(clipPath);
         ClipRable clipElementClipPath = CSSUtilities.convertClipPath(clipElement, clipPathNode, ctx);
         if (clipElementClipPath != null) {
            clipPath.subtract(new Area(clipElementClipPath.getClipPath()));
         }

         Filter filter = clipedNode.getFilter();
         if (filter == null) {
            filter = clipedNode.getGraphicsNodeRable(true);
         }

         boolean useAA = false;
         RenderingHints hints = CSSUtilities.convertShapeRendering(clipElement, (RenderingHints)null);
         if (hints != null) {
            Object o = hints.get(RenderingHints.KEY_ANTIALIASING);
            useAA = o == RenderingHints.VALUE_ANTIALIAS_ON;
         }

         return new ClipRable8Bit(filter, clipPath, useAA);
      }
   }
}
