package org.apache.batik.bridge;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.batik.anim.dom.SVGOMDocument;
import org.apache.batik.dom.util.XLinkSupport;
import org.apache.batik.ext.awt.image.ConcreteComponentTransferFunction;
import org.apache.batik.ext.awt.image.renderable.ComponentTransferRable8Bit;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.gvt.AbstractGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.PatternPaint;
import org.apache.batik.gvt.RootGraphicsNode;
import org.apache.batik.util.ParsedURL;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class SVGPatternElementBridge extends AnimatableGenericSVGBridge implements PaintBridge, ErrorConstants {
   public String getLocalName() {
      return "pattern";
   }

   public Paint createPaint(BridgeContext ctx, Element patternElement, Element paintedElement, GraphicsNode paintedNode, float opacity) {
      RootGraphicsNode patternContentNode = (RootGraphicsNode)ctx.getElementData(patternElement);
      if (patternContentNode == null) {
         patternContentNode = extractPatternContent(patternElement, ctx);
         ctx.setElementData(patternElement, patternContentNode);
      }

      if (patternContentNode == null) {
         return null;
      } else {
         Rectangle2D patternRegion = SVGUtilities.convertPatternRegion(patternElement, paintedElement, paintedNode, ctx);
         String s = SVGUtilities.getChainableAttributeNS(patternElement, (String)null, "patternTransform", ctx);
         AffineTransform patternTransform;
         if (s.length() != 0) {
            patternTransform = SVGUtilities.convertTransform(patternElement, "patternTransform", s, ctx);
         } else {
            patternTransform = new AffineTransform();
         }

         boolean overflowIsHidden = CSSUtilities.convertOverflow(patternElement);
         s = SVGUtilities.getChainableAttributeNS(patternElement, (String)null, "patternContentUnits", ctx);
         short contentCoordSystem;
         if (s.length() == 0) {
            contentCoordSystem = 1;
         } else {
            contentCoordSystem = SVGUtilities.parseCoordinateSystem(patternElement, "patternContentUnits", s, ctx);
         }

         AffineTransform patternContentTransform = new AffineTransform();
         patternContentTransform.translate(patternRegion.getX(), patternRegion.getY());
         String viewBoxStr = SVGUtilities.getChainableAttributeNS(patternElement, (String)null, "viewBox", ctx);
         if (viewBoxStr.length() > 0) {
            String aspectRatioStr = SVGUtilities.getChainableAttributeNS(patternElement, (String)null, "preserveAspectRatio", ctx);
            float w = (float)patternRegion.getWidth();
            float h = (float)patternRegion.getHeight();
            AffineTransform preserveAspectRatioTransform = ViewBox.getPreserveAspectRatioTransform(patternElement, viewBoxStr, aspectRatioStr, w, h, ctx);
            patternContentTransform.concatenate(preserveAspectRatioTransform);
         } else if (contentCoordSystem == 2) {
            AffineTransform patternContentUnitsTransform = new AffineTransform();
            Rectangle2D objectBoundingBox = paintedNode.getGeometryBounds();
            patternContentUnitsTransform.translate(objectBoundingBox.getX(), objectBoundingBox.getY());
            patternContentUnitsTransform.scale(objectBoundingBox.getWidth(), objectBoundingBox.getHeight());
            patternContentTransform.concatenate(patternContentUnitsTransform);
         }

         GraphicsNode gn = new PatternGraphicsNode(patternContentNode);
         gn.setTransform(patternContentTransform);
         if (opacity != 1.0F) {
            Filter filter = gn.getGraphicsNodeRable(true);
            Filter filter = new ComponentTransferRable8Bit(filter, ConcreteComponentTransferFunction.getLinearTransfer(opacity, 0.0F), ConcreteComponentTransferFunction.getIdentityTransfer(), ConcreteComponentTransferFunction.getIdentityTransfer(), ConcreteComponentTransferFunction.getIdentityTransfer());
            gn.setFilter(filter);
         }

         return new PatternPaint(gn, patternRegion, !overflowIsHidden, patternTransform);
      }
   }

   protected static RootGraphicsNode extractPatternContent(Element patternElement, BridgeContext ctx) {
      List refs = new LinkedList();

      while(true) {
         RootGraphicsNode content = extractLocalPatternContent(patternElement, ctx);
         if (content != null) {
            return content;
         }

         String uri = XLinkSupport.getXLinkHref(patternElement);
         if (uri.length() == 0) {
            return null;
         }

         SVGOMDocument doc = (SVGOMDocument)patternElement.getOwnerDocument();
         ParsedURL purl = new ParsedURL(doc.getURL(), uri);
         if (!purl.complete()) {
            throw new BridgeException(ctx, patternElement, "uri.malformed", new Object[]{uri});
         }

         if (contains(refs, purl)) {
            throw new BridgeException(ctx, patternElement, "xlink.href.circularDependencies", new Object[]{uri});
         }

         refs.add(purl);
         patternElement = ctx.getReferencedElement(patternElement, uri);
      }
   }

   protected static RootGraphicsNode extractLocalPatternContent(Element e, BridgeContext ctx) {
      GVTBuilder builder = ctx.getGVTBuilder();
      RootGraphicsNode content = null;

      for(Node n = e.getFirstChild(); n != null; n = n.getNextSibling()) {
         if (n.getNodeType() == 1) {
            GraphicsNode gn = builder.build(ctx, (Element)n);
            if (gn != null) {
               if (content == null) {
                  content = new RootGraphicsNode();
               }

               content.getChildren().add(gn);
            }
         }
      }

      return content;
   }

   private static boolean contains(List urls, ParsedURL key) {
      Iterator var2 = urls.iterator();

      Object url;
      do {
         if (!var2.hasNext()) {
            return false;
         }

         url = var2.next();
      } while(!key.equals(url));

      return true;
   }

   public static class PatternGraphicsNode extends AbstractGraphicsNode {
      GraphicsNode pcn;
      Rectangle2D pBounds;
      Rectangle2D gBounds;
      Rectangle2D sBounds;
      Shape oShape;

      public PatternGraphicsNode(GraphicsNode gn) {
         this.pcn = gn;
      }

      public void primitivePaint(Graphics2D g2d) {
         this.pcn.paint(g2d);
      }

      public Rectangle2D getPrimitiveBounds() {
         if (this.pBounds != null) {
            return this.pBounds;
         } else {
            this.pBounds = this.pcn.getTransformedBounds(IDENTITY);
            return this.pBounds;
         }
      }

      public Rectangle2D getGeometryBounds() {
         if (this.gBounds != null) {
            return this.gBounds;
         } else {
            this.gBounds = this.pcn.getTransformedGeometryBounds(IDENTITY);
            return this.gBounds;
         }
      }

      public Rectangle2D getSensitiveBounds() {
         if (this.sBounds != null) {
            return this.sBounds;
         } else {
            this.sBounds = this.pcn.getTransformedSensitiveBounds(IDENTITY);
            return this.sBounds;
         }
      }

      public Shape getOutline() {
         if (this.oShape != null) {
            return this.oShape;
         } else {
            this.oShape = this.pcn.getOutline();
            AffineTransform tr = this.pcn.getTransform();
            if (tr != null) {
               this.oShape = tr.createTransformedShape(this.oShape);
            }

            return this.oShape;
         }
      }

      protected void invalidateGeometryCache() {
         this.pBounds = null;
         this.gBounds = null;
         this.sBounds = null;
         this.oShape = null;
         super.invalidateGeometryCache();
      }
   }
}
