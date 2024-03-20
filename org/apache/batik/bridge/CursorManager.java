package org.apache.batik.bridge;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.dom.AbstractNode;
import org.apache.batik.dom.util.XLinkSupport;
import org.apache.batik.ext.awt.image.PadMode;
import org.apache.batik.ext.awt.image.renderable.AffineRable8Bit;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.ext.awt.image.renderable.PadRable8Bit;
import org.apache.batik.ext.awt.image.spi.BrokenLinkProvider;
import org.apache.batik.ext.awt.image.spi.ImageTagRegistry;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.util.Platform;
import org.apache.batik.util.SVGConstants;
import org.apache.batik.util.SoftReferenceCache;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGDocument;

public class CursorManager implements SVGConstants, ErrorConstants {
   protected static Map cursorMap;
   public static final Cursor DEFAULT_CURSOR = Cursor.getPredefinedCursor(0);
   public static final Cursor ANCHOR_CURSOR = Cursor.getPredefinedCursor(12);
   public static final Cursor TEXT_CURSOR = Cursor.getPredefinedCursor(2);
   public static final int DEFAULT_PREFERRED_WIDTH = 32;
   public static final int DEFAULT_PREFERRED_HEIGHT = 32;
   protected BridgeContext ctx;
   protected CursorCache cursorCache = new CursorCache();

   public CursorManager(BridgeContext ctx) {
      this.ctx = ctx;
   }

   public static Cursor getPredefinedCursor(String cursorName) {
      return (Cursor)cursorMap.get(cursorName);
   }

   public Cursor convertCursor(Element e) {
      Value cursorValue = CSSUtilities.getComputedStyle(e, 10);
      String cursorStr = "auto";
      if (cursorValue != null) {
         if (cursorValue.getCssValueType() == 1 && cursorValue.getPrimitiveType() == 21) {
            cursorStr = cursorValue.getStringValue();
            return this.convertBuiltInCursor(e, cursorStr);
         }

         if (cursorValue.getCssValueType() == 2) {
            int nValues = cursorValue.getLength();
            if (nValues == 1) {
               cursorValue = cursorValue.item(0);
               if (cursorValue.getPrimitiveType() == 21) {
                  cursorStr = cursorValue.getStringValue();
                  return this.convertBuiltInCursor(e, cursorStr);
               }
            } else if (nValues > 1) {
               return this.convertSVGCursor(e, cursorValue);
            }
         }
      }

      return this.convertBuiltInCursor(e, cursorStr);
   }

   public Cursor convertBuiltInCursor(Element e, String cursorStr) {
      Cursor cursor = null;
      if (cursorStr.charAt(0) == 'a') {
         String nameSpaceURI = e.getNamespaceURI();
         if ("http://www.w3.org/2000/svg".equals(nameSpaceURI)) {
            String tag = e.getLocalName();
            if ("a".equals(tag)) {
               cursor = ANCHOR_CURSOR;
            } else if (!"text".equals(tag) && !"tspan".equals(tag) && !"tref".equals(tag)) {
               if ("image".equals(tag)) {
                  return null;
               }

               cursor = DEFAULT_CURSOR;
            } else {
               cursor = TEXT_CURSOR;
            }
         } else {
            cursor = DEFAULT_CURSOR;
         }
      } else {
         cursor = getPredefinedCursor(cursorStr);
      }

      return cursor;
   }

   public Cursor convertSVGCursor(Element e, Value l) {
      int nValues = l.getLength();
      Element cursorElement = null;

      for(int i = 0; i < nValues - 1; ++i) {
         Value cursorValue = l.item(i);
         if (cursorValue.getPrimitiveType() == 20) {
            String uri = cursorValue.getStringValue();

            try {
               cursorElement = this.ctx.getReferencedElement(e, uri);
            } catch (BridgeException var10) {
               if (!"uri.badTarget".equals(var10.getCode())) {
                  throw var10;
               }
            }

            if (cursorElement != null) {
               String cursorNS = cursorElement.getNamespaceURI();
               if ("http://www.w3.org/2000/svg".equals(cursorNS) && "cursor".equals(cursorElement.getLocalName())) {
                  Cursor c = this.convertSVGCursorElement(cursorElement);
                  if (c != null) {
                     return c;
                  }
               }
            }
         }
      }

      Value cursorValue = l.item(nValues - 1);
      String cursorStr = "auto";
      if (cursorValue.getPrimitiveType() == 21) {
         cursorStr = cursorValue.getStringValue();
      }

      return this.convertBuiltInCursor(e, cursorStr);
   }

   public Cursor convertSVGCursorElement(Element cursorElement) {
      String uriStr = XLinkSupport.getXLinkHref(cursorElement);
      if (uriStr.length() == 0) {
         throw new BridgeException(this.ctx, cursorElement, "attribute.missing", new Object[]{"xlink:href"});
      } else {
         String baseURI = AbstractNode.getBaseURI(cursorElement);
         ParsedURL purl;
         if (baseURI == null) {
            purl = new ParsedURL(uriStr);
         } else {
            purl = new ParsedURL(baseURI, uriStr);
         }

         org.apache.batik.parser.UnitProcessor.Context uctx = UnitProcessor.createContext(this.ctx, cursorElement);
         String s = cursorElement.getAttributeNS((String)null, "x");
         float x = 0.0F;
         if (s.length() != 0) {
            x = UnitProcessor.svgHorizontalCoordinateToUserSpace(s, "x", uctx);
         }

         s = cursorElement.getAttributeNS((String)null, "y");
         float y = 0.0F;
         if (s.length() != 0) {
            y = UnitProcessor.svgVerticalCoordinateToUserSpace(s, "y", uctx);
         }

         CursorDescriptor desc = new CursorDescriptor(purl, x, y);
         Cursor cachedCursor = this.cursorCache.getCursor(desc);
         if (cachedCursor != null) {
            return cachedCursor;
         } else {
            Point2D.Float hotSpot = new Point2D.Float(x, y);
            Filter f = this.cursorHrefToFilter(cursorElement, purl, hotSpot);
            if (f == null) {
               this.cursorCache.clearCursor(desc);
               return null;
            } else {
               Rectangle cursorSize = f.getBounds2D().getBounds();
               RenderedImage ri = f.createScaledRendering(cursorSize.width, cursorSize.height, (RenderingHints)null);
               Image img = null;
               if (ri instanceof Image) {
                  img = (Image)ri;
               } else {
                  img = this.renderedImageToImage(ri);
               }

               hotSpot.x = hotSpot.x < 0.0F ? 0.0F : hotSpot.x;
               hotSpot.y = hotSpot.y < 0.0F ? 0.0F : hotSpot.y;
               hotSpot.x = hotSpot.x > (float)(cursorSize.width - 1) ? (float)(cursorSize.width - 1) : hotSpot.x;
               hotSpot.y = hotSpot.y > (float)(cursorSize.height - 1) ? (float)(cursorSize.height - 1) : hotSpot.y;
               Cursor c = Toolkit.getDefaultToolkit().createCustomCursor(img, new Point(Math.round(hotSpot.x), Math.round(hotSpot.y)), purl.toString());
               this.cursorCache.putCursor(desc, c);
               return c;
            }
         }
      }
   }

   protected Filter cursorHrefToFilter(Element cursorElement, ParsedURL purl, Point2D hotSpot) {
      AffineRable8Bit f = null;
      String uriStr = purl.toString();
      Dimension cursorSize = null;
      DocumentLoader loader = this.ctx.getDocumentLoader();
      SVGDocument svgDoc = (SVGDocument)cursorElement.getOwnerDocument();
      URIResolver resolver = this.ctx.createURIResolver(svgDoc, loader);

      try {
         Element rootElement = null;
         Node n = resolver.getNode(uriStr, cursorElement);
         if (n.getNodeType() != 9) {
            throw new BridgeException(this.ctx, cursorElement, "uri.image.invalid", new Object[]{uriStr});
         }

         SVGDocument doc = (SVGDocument)n;
         this.ctx.initializeDocument(doc);
         rootElement = doc.getRootElement();
         GraphicsNode node = this.ctx.getGVTBuilder().build(this.ctx, (Element)rootElement);
         float width = 32.0F;
         float height = 32.0F;
         org.apache.batik.parser.UnitProcessor.Context uctx = UnitProcessor.createContext(this.ctx, rootElement);
         String s = rootElement.getAttribute("width");
         if (s.length() != 0) {
            width = UnitProcessor.svgHorizontalLengthToUserSpace(s, "width", uctx);
         }

         s = rootElement.getAttribute("height");
         if (s.length() != 0) {
            height = UnitProcessor.svgVerticalLengthToUserSpace(s, "height", uctx);
         }

         cursorSize = Toolkit.getDefaultToolkit().getBestCursorSize(Math.round(width), Math.round(height));
         AffineTransform at = ViewBox.getPreserveAspectRatioTransform(rootElement, (float)cursorSize.width, (float)cursorSize.height, this.ctx);
         Filter filter = node.getGraphicsNodeRable(true);
         f = new AffineRable8Bit(filter, at);
      } catch (BridgeException var19) {
         throw var19;
      } catch (SecurityException var20) {
         throw new BridgeException(this.ctx, cursorElement, var20, "uri.unsecure", new Object[]{uriStr});
      } catch (Exception var21) {
      }

      if (f == null) {
         ImageTagRegistry reg = ImageTagRegistry.getRegistry();
         Filter filter = reg.readURL(purl);
         if (filter == null) {
            return null;
         }

         if (BrokenLinkProvider.hasBrokenLinkProperty(filter)) {
            return null;
         }

         Rectangle preferredSize = filter.getBounds2D().getBounds();
         cursorSize = Toolkit.getDefaultToolkit().getBestCursorSize(preferredSize.width, preferredSize.height);
         if (preferredSize == null || preferredSize.width <= 0 || preferredSize.height <= 0) {
            return null;
         }

         AffineTransform at = new AffineTransform();
         if (preferredSize.width > cursorSize.width || preferredSize.height > cursorSize.height) {
            at = ViewBox.getPreserveAspectRatioTransform(new float[]{0.0F, 0.0F, (float)preferredSize.width, (float)preferredSize.height}, (short)2, true, (float)cursorSize.width, (float)cursorSize.height);
         }

         f = new AffineRable8Bit(filter, at);
      }

      AffineTransform at = f.getAffine();
      at.transform(hotSpot, hotSpot);
      Rectangle cursorViewport = new Rectangle(0, 0, cursorSize.width, cursorSize.height);
      PadRable8Bit cursorImage = new PadRable8Bit(f, cursorViewport, PadMode.ZERO_PAD);
      return cursorImage;
   }

   protected Image renderedImageToImage(RenderedImage ri) {
      int x = ri.getMinX();
      int y = ri.getMinY();
      SampleModel sm = ri.getSampleModel();
      ColorModel cm = ri.getColorModel();
      WritableRaster wr = Raster.createWritableRaster(sm, new Point(x, y));
      ri.copyData(wr);
      return new BufferedImage(cm, wr, cm.isAlphaPremultiplied(), (Hashtable)null);
   }

   static {
      Toolkit toolkit = Toolkit.getDefaultToolkit();
      cursorMap = new HashMap();
      cursorMap.put("crosshair", Cursor.getPredefinedCursor(1));
      cursorMap.put("default", Cursor.getPredefinedCursor(0));
      cursorMap.put("pointer", Cursor.getPredefinedCursor(12));
      cursorMap.put("e-resize", Cursor.getPredefinedCursor(11));
      cursorMap.put("ne-resize", Cursor.getPredefinedCursor(7));
      cursorMap.put("nw-resize", Cursor.getPredefinedCursor(6));
      cursorMap.put("n-resize", Cursor.getPredefinedCursor(8));
      cursorMap.put("se-resize", Cursor.getPredefinedCursor(5));
      cursorMap.put("sw-resize", Cursor.getPredefinedCursor(4));
      cursorMap.put("s-resize", Cursor.getPredefinedCursor(9));
      cursorMap.put("w-resize", Cursor.getPredefinedCursor(10));
      cursorMap.put("text", Cursor.getPredefinedCursor(2));
      cursorMap.put("wait", Cursor.getPredefinedCursor(3));
      Cursor moveCursor = Cursor.getPredefinedCursor(13);
      if (Platform.isOSX) {
         try {
            Image img = toolkit.createImage(CursorManager.class.getResource("resources/move.gif"));
            moveCursor = toolkit.createCustomCursor(img, new Point(11, 11), "move");
         } catch (Exception var5) {
         }
      }

      cursorMap.put("move", moveCursor);

      Cursor helpCursor;
      try {
         Image img = toolkit.createImage(CursorManager.class.getResource("resources/help.gif"));
         helpCursor = toolkit.createCustomCursor(img, new Point(1, 3), "help");
      } catch (Exception var4) {
         helpCursor = Cursor.getPredefinedCursor(12);
      }

      cursorMap.put("help", helpCursor);
   }

   static class CursorCache extends SoftReferenceCache {
      public CursorCache() {
      }

      public Cursor getCursor(CursorDescriptor desc) {
         return (Cursor)this.requestImpl(desc);
      }

      public void putCursor(CursorDescriptor desc, Cursor cursor) {
         this.putImpl(desc, cursor);
      }

      public void clearCursor(CursorDescriptor desc) {
         this.clearImpl(desc);
      }
   }

   static class CursorDescriptor {
      ParsedURL purl;
      float x;
      float y;
      String desc;

      public CursorDescriptor(ParsedURL purl, float x, float y) {
         if (purl == null) {
            throw new IllegalArgumentException();
         } else {
            this.purl = purl;
            this.x = x;
            this.y = y;
            this.desc = this.getClass().getName() + "\n\t:[" + this.purl + "]\n\t:[" + x + "]:[" + y + "]";
         }
      }

      public boolean equals(Object obj) {
         if (obj != null && obj instanceof CursorDescriptor) {
            CursorDescriptor desc = (CursorDescriptor)obj;
            boolean isEqual = this.purl.equals(desc.purl) && this.x == desc.x && this.y == desc.y;
            return isEqual;
         } else {
            return false;
         }
      }

      public String toString() {
         return this.desc;
      }

      public int hashCode() {
         return this.desc.hashCode();
      }
   }
}
