package org.apache.batik.bridge.svg12;

import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.batik.bridge.Bridge;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.BridgeException;
import org.apache.batik.bridge.CSSUtilities;
import org.apache.batik.bridge.MultiResGraphicsNode;
import org.apache.batik.bridge.SVGImageElementBridge;
import org.apache.batik.bridge.SVGUtilities;
import org.apache.batik.bridge.Viewport;
import org.apache.batik.dom.AbstractNode;
import org.apache.batik.dom.util.XLinkSupport;
import org.apache.batik.ext.awt.image.renderable.ClipRable8Bit;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.ImageNode;
import org.apache.batik.parser.UnitProcessor;
import org.apache.batik.util.ParsedURL;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class SVGMultiImageElementBridge extends SVGImageElementBridge {
   public String getNamespaceURI() {
      return "http://www.w3.org/2000/svg";
   }

   public String getLocalName() {
      return "multiImage";
   }

   public Bridge getInstance() {
      return new SVGMultiImageElementBridge();
   }

   public GraphicsNode createGraphicsNode(BridgeContext ctx, Element e) {
      if (!SVGUtilities.matchUserAgent(e, ctx.getUserAgent())) {
         return null;
      } else {
         ImageNode imgNode = (ImageNode)this.instantiateGraphicsNode();
         if (imgNode == null) {
            return null;
         } else {
            this.associateSVGContext(ctx, e, imgNode);
            Rectangle2D b = getImageBounds(ctx, e);
            AffineTransform at = null;
            String s = e.getAttribute("transform");
            if (s.length() != 0) {
               at = SVGUtilities.convertTransform(e, "transform", s, ctx);
            } else {
               at = new AffineTransform();
            }

            at.translate(b.getX(), b.getY());
            imgNode.setTransform(at);
            imgNode.setVisible(CSSUtilities.convertVisibility(e));
            Rectangle2D clip = new Rectangle2D.Double(0.0, 0.0, b.getWidth(), b.getHeight());
            Filter filter = imgNode.getGraphicsNodeRable(true);
            imgNode.setClip(new ClipRable8Bit(filter, clip));
            Rectangle2D r = CSSUtilities.convertEnableBackground(e);
            if (r != null) {
               imgNode.setBackgroundEnable(r);
            }

            ctx.openViewport(e, new MultiImageElementViewport((float)b.getWidth(), (float)b.getHeight()));
            List elems = new LinkedList();
            List minDim = new LinkedList();
            List maxDim = new LinkedList();

            for(Node n = e.getFirstChild(); n != null; n = n.getNextSibling()) {
               if (n.getNodeType() == 1) {
                  Element se = (Element)n;
                  if (this.getNamespaceURI().equals(se.getNamespaceURI())) {
                     if (se.getLocalName().equals("subImage")) {
                        this.addInfo(se, elems, minDim, maxDim, b);
                     }

                     if (se.getLocalName().equals("subImageRef")) {
                        this.addRefInfo(se, elems, minDim, maxDim, b);
                     }
                  }
               }
            }

            Dimension[] mindary = new Dimension[elems.size()];
            Dimension[] maxdary = new Dimension[elems.size()];
            Element[] elemary = new Element[elems.size()];
            Iterator mindi = minDim.iterator();
            Iterator maxdi = maxDim.iterator();
            Iterator ei = elems.iterator();

            for(int n = 0; mindi.hasNext(); ++n) {
               Dimension minD = (Dimension)mindi.next();
               Dimension maxD = (Dimension)maxdi.next();
               int i = 0;
               if (minD != null) {
                  while(i < n && (mindary[i] == null || minD.width >= mindary[i].width)) {
                     ++i;
                  }
               }

               for(int j = n; j > i; --j) {
                  elemary[j] = elemary[j - 1];
                  mindary[j] = mindary[j - 1];
                  maxdary[j] = maxdary[j - 1];
               }

               elemary[i] = (Element)ei.next();
               mindary[i] = minD;
               maxdary[i] = maxD;
            }

            GraphicsNode node = new MultiResGraphicsNode(e, clip, elemary, mindary, maxdary, ctx);
            imgNode.setImage(node);
            return imgNode;
         }
      }
   }

   public boolean isComposite() {
      return false;
   }

   public void buildGraphicsNode(BridgeContext ctx, Element e, GraphicsNode node) {
      this.initializeDynamicSupport(ctx, e, node);
      ctx.closeViewport(e);
   }

   protected void initializeDynamicSupport(BridgeContext ctx, Element e, GraphicsNode node) {
      if (ctx.isInteractive()) {
         ImageNode imgNode = (ImageNode)node;
         ctx.bind(e, imgNode.getImage());
      }

   }

   public void dispose() {
      this.ctx.removeViewport(this.e);
      super.dispose();
   }

   protected static Rectangle2D getImageBounds(BridgeContext ctx, Element element) {
      UnitProcessor.Context uctx = org.apache.batik.bridge.UnitProcessor.createContext(ctx, element);
      String s = element.getAttributeNS((String)null, "x");
      float x = 0.0F;
      if (s.length() != 0) {
         x = org.apache.batik.bridge.UnitProcessor.svgHorizontalCoordinateToUserSpace(s, "x", uctx);
      }

      s = element.getAttributeNS((String)null, "y");
      float y = 0.0F;
      if (s.length() != 0) {
         y = org.apache.batik.bridge.UnitProcessor.svgVerticalCoordinateToUserSpace(s, "y", uctx);
      }

      s = element.getAttributeNS((String)null, "width");
      if (s.length() == 0) {
         throw new BridgeException(ctx, element, "attribute.missing", new Object[]{"width"});
      } else {
         float w = org.apache.batik.bridge.UnitProcessor.svgHorizontalLengthToUserSpace(s, "width", uctx);
         s = element.getAttributeNS((String)null, "height");
         if (s.length() == 0) {
            throw new BridgeException(ctx, element, "attribute.missing", new Object[]{"height"});
         } else {
            float h = org.apache.batik.bridge.UnitProcessor.svgVerticalLengthToUserSpace(s, "height", uctx);
            return new Rectangle2D.Float(x, y, w, h);
         }
      }
   }

   protected void addInfo(Element e, Collection elems, Collection minDim, Collection maxDim, Rectangle2D bounds) {
      Document doc = e.getOwnerDocument();
      Element gElem = doc.createElementNS("http://www.w3.org/2000/svg", "g");
      NamedNodeMap attrs = e.getAttributes();
      int len = attrs.getLength();

      for(int i = 0; i < len; ++i) {
         Attr attr = (Attr)attrs.item(i);
         gElem.setAttributeNS(attr.getNamespaceURI(), attr.getName(), attr.getValue());
      }

      for(Node n = e.getFirstChild(); n != null; n = e.getFirstChild()) {
         gElem.appendChild(n);
      }

      e.appendChild(gElem);
      elems.add(gElem);
      minDim.add(this.getElementMinPixel(e, bounds));
      maxDim.add(this.getElementMaxPixel(e, bounds));
   }

   protected void addRefInfo(Element e, Collection elems, Collection minDim, Collection maxDim, Rectangle2D bounds) {
      String uriStr = XLinkSupport.getXLinkHref(e);
      if (uriStr.length() == 0) {
         throw new BridgeException(this.ctx, e, "attribute.missing", new Object[]{"xlink:href"});
      } else {
         String baseURI = AbstractNode.getBaseURI(e);
         ParsedURL purl;
         if (baseURI == null) {
            purl = new ParsedURL(uriStr);
         } else {
            purl = new ParsedURL(baseURI, uriStr);
         }

         Document doc = e.getOwnerDocument();
         Element imgElem = doc.createElementNS("http://www.w3.org/2000/svg", "image");
         imgElem.setAttributeNS("http://www.w3.org/1999/xlink", "href", purl.toString());
         NamedNodeMap attrs = e.getAttributes();
         int len = attrs.getLength();

         for(int i = 0; i < len; ++i) {
            Attr attr = (Attr)attrs.item(i);
            imgElem.setAttributeNS(attr.getNamespaceURI(), attr.getName(), attr.getValue());
         }

         String s = e.getAttribute("x");
         if (s.length() == 0) {
            imgElem.setAttribute("x", "0");
         }

         s = e.getAttribute("y");
         if (s.length() == 0) {
            imgElem.setAttribute("y", "0");
         }

         s = e.getAttribute("width");
         if (s.length() == 0) {
            imgElem.setAttribute("width", "100%");
         }

         s = e.getAttribute("height");
         if (s.length() == 0) {
            imgElem.setAttribute("height", "100%");
         }

         e.appendChild(imgElem);
         elems.add(imgElem);
         minDim.add(this.getElementMinPixel(e, bounds));
         maxDim.add(this.getElementMaxPixel(e, bounds));
      }
   }

   protected Dimension getElementMinPixel(Element e, Rectangle2D bounds) {
      return this.getElementPixelSize(e, "max-pixel-size", bounds);
   }

   protected Dimension getElementMaxPixel(Element e, Rectangle2D bounds) {
      return this.getElementPixelSize(e, "min-pixel-size", bounds);
   }

   protected Dimension getElementPixelSize(Element e, String attr, Rectangle2D bounds) {
      String s = e.getAttribute(attr);
      if (s.length() == 0) {
         return null;
      } else {
         Float[] vals = SVGUtilities.convertSVGNumberOptionalNumber(e, attr, s, this.ctx);
         if (vals[0] == null) {
            return null;
         } else {
            float xPixSz = vals[0];
            float yPixSz = xPixSz;
            if (vals[1] != null) {
               yPixSz = vals[1];
            }

            return new Dimension((int)(bounds.getWidth() / (double)xPixSz + 0.5), (int)(bounds.getHeight() / (double)yPixSz + 0.5));
         }
      }
   }

   public static class MultiImageElementViewport implements Viewport {
      private float width;
      private float height;

      public MultiImageElementViewport(float w, float h) {
         this.width = w;
         this.height = h;
      }

      public float getWidth() {
         return this.width;
      }

      public float getHeight() {
         return this.height;
      }
   }
}
