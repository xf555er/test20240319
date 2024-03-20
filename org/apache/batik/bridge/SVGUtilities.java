package org.apache.batik.bridge;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.dom.util.XLinkSupport;
import org.apache.batik.dom.util.XMLSupport;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.parser.AWTTransformProducer;
import org.apache.batik.parser.ClockHandler;
import org.apache.batik.parser.ClockParser;
import org.apache.batik.parser.ParseException;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.svg.SVGLangSpace;
import org.w3c.dom.svg.SVGNumberList;

public abstract class SVGUtilities implements SVGConstants, ErrorConstants {
   public static final short USER_SPACE_ON_USE = 1;
   public static final short OBJECT_BOUNDING_BOX = 2;
   public static final short STROKE_WIDTH = 3;

   protected SVGUtilities() {
   }

   public static Element getParentElement(Element elt) {
      Node n;
      for(n = CSSEngine.getCSSParentNode(elt); n != null && n.getNodeType() != 1; n = CSSEngine.getCSSParentNode(n)) {
      }

      return (Element)n;
   }

   public static float[] convertSVGNumberList(SVGNumberList l) {
      int n = l.getNumberOfItems();
      if (n == 0) {
         return null;
      } else {
         float[] fl = new float[n];

         for(int i = 0; i < n; ++i) {
            fl[i] = l.getItem(i).getValue();
         }

         return fl;
      }
   }

   public static float convertSVGNumber(String s) {
      return Float.parseFloat(s);
   }

   public static int convertSVGInteger(String s) {
      return Integer.parseInt(s);
   }

   public static float convertRatio(String v) {
      float d = 1.0F;
      if (v.endsWith("%")) {
         v = v.substring(0, v.length() - 1);
         d = 100.0F;
      }

      float r = Float.parseFloat(v) / d;
      if (r < 0.0F) {
         r = 0.0F;
      } else if (r > 1.0F) {
         r = 1.0F;
      }

      return r;
   }

   public static String getDescription(SVGElement elt) {
      String result = "";
      boolean preserve = false;
      Node n = elt.getFirstChild();
      if (n != null && n.getNodeType() == 1) {
         String name = n.getPrefix() == null ? n.getNodeName() : n.getLocalName();
         if (name.equals("desc")) {
            preserve = ((SVGLangSpace)n).getXMLspace().equals("preserve");

            for(n = n.getFirstChild(); n != null; n = n.getNextSibling()) {
               if (n.getNodeType() == 3) {
                  result = result + n.getNodeValue();
               }
            }
         }
      }

      return preserve ? XMLSupport.preserveXMLSpace(result) : XMLSupport.defaultXMLSpace(result);
   }

   public static boolean matchUserAgent(Element elt, UserAgent ua) {
      String re;
      StringTokenizer st;
      String s;
      if (elt.hasAttributeNS((String)null, "systemLanguage")) {
         re = elt.getAttributeNS((String)null, "systemLanguage");
         if (re.length() == 0) {
            return false;
         }

         st = new StringTokenizer(re, ", ");

         do {
            if (!st.hasMoreTokens()) {
               return false;
            }

            s = st.nextToken();
         } while(!matchUserLanguage(s, ua.getLanguages()));
      }

      if (elt.hasAttributeNS((String)null, "requiredFeatures")) {
         re = elt.getAttributeNS((String)null, "requiredFeatures");
         if (re.length() == 0) {
            return false;
         }

         st = new StringTokenizer(re, " ");

         while(st.hasMoreTokens()) {
            s = st.nextToken();
            if (!ua.hasFeature(s)) {
               return false;
            }
         }
      }

      if (elt.hasAttributeNS((String)null, "requiredExtensions")) {
         re = elt.getAttributeNS((String)null, "requiredExtensions");
         if (re.length() == 0) {
            return false;
         }

         st = new StringTokenizer(re, " ");

         while(st.hasMoreTokens()) {
            s = st.nextToken();
            if (!ua.supportExtension(s)) {
               return false;
            }
         }
      }

      return true;
   }

   protected static boolean matchUserLanguage(String s, String userLanguages) {
      StringTokenizer st = new StringTokenizer(userLanguages, ", ");

      String t;
      do {
         if (!st.hasMoreTokens()) {
            return false;
         }

         t = st.nextToken();
      } while(!s.startsWith(t));

      if (s.length() > t.length()) {
         return s.charAt(t.length()) == '-';
      } else {
         return true;
      }
   }

   public static String getChainableAttributeNS(Element element, String namespaceURI, String attrName, BridgeContext ctx) {
      DocumentLoader loader = ctx.getDocumentLoader();
      Element e = element;
      List refs = new LinkedList();

      while(true) {
         String v = e.getAttributeNS(namespaceURI, attrName);
         if (v.length() > 0) {
            return v;
         }

         String uriStr = XLinkSupport.getXLinkHref(e);
         if (uriStr.length() == 0) {
            return "";
         }

         String baseURI = e.getBaseURI();
         ParsedURL purl = new ParsedURL(baseURI, uriStr);
         Iterator var11 = refs.iterator();

         while(var11.hasNext()) {
            Object ref = var11.next();
            if (purl.equals(ref)) {
               throw new BridgeException(ctx, e, "xlink.href.circularDependencies", new Object[]{uriStr});
            }
         }

         try {
            SVGDocument svgDoc = (SVGDocument)e.getOwnerDocument();
            URIResolver resolver = ctx.createURIResolver(svgDoc, loader);
            e = resolver.getElement(purl.toString(), e);
            refs.add(purl);
         } catch (IOException var13) {
            throw new BridgeException(ctx, e, var13, "uri.io", new Object[]{uriStr});
         } catch (SecurityException var14) {
            throw new BridgeException(ctx, e, var14, "uri.unsecure", new Object[]{uriStr});
         }
      }
   }

   public static Point2D convertPoint(String xStr, String xAttr, String yStr, String yAttr, short unitsType, org.apache.batik.parser.UnitProcessor.Context uctx) {
      float x;
      float y;
      switch (unitsType) {
         case 1:
            x = UnitProcessor.svgHorizontalCoordinateToUserSpace(xStr, xAttr, uctx);
            y = UnitProcessor.svgVerticalCoordinateToUserSpace(yStr, yAttr, uctx);
            break;
         case 2:
            x = UnitProcessor.svgHorizontalCoordinateToObjectBoundingBox(xStr, xAttr, uctx);
            y = UnitProcessor.svgVerticalCoordinateToObjectBoundingBox(yStr, yAttr, uctx);
            break;
         default:
            throw new IllegalArgumentException("Invalid unit type");
      }

      return new Point2D.Float(x, y);
   }

   public static float convertLength(String length, String attr, short unitsType, org.apache.batik.parser.UnitProcessor.Context uctx) {
      switch (unitsType) {
         case 1:
            return UnitProcessor.svgOtherLengthToUserSpace(length, attr, uctx);
         case 2:
            return UnitProcessor.svgOtherLengthToObjectBoundingBox(length, attr, uctx);
         default:
            throw new IllegalArgumentException("Invalid unit type");
      }
   }

   public static Rectangle2D convertMaskRegion(Element maskElement, Element maskedElement, GraphicsNode maskedNode, BridgeContext ctx) {
      String xStr = maskElement.getAttributeNS((String)null, "x");
      if (xStr.length() == 0) {
         xStr = "-10%";
      }

      String yStr = maskElement.getAttributeNS((String)null, "y");
      if (yStr.length() == 0) {
         yStr = "-10%";
      }

      String wStr = maskElement.getAttributeNS((String)null, "width");
      if (wStr.length() == 0) {
         wStr = "120%";
      }

      String hStr = maskElement.getAttributeNS((String)null, "height");
      if (hStr.length() == 0) {
         hStr = "120%";
      }

      String units = maskElement.getAttributeNS((String)null, "maskUnits");
      short unitsType;
      if (units.length() == 0) {
         unitsType = 2;
      } else {
         unitsType = parseCoordinateSystem(maskElement, "maskUnits", units, ctx);
      }

      org.apache.batik.parser.UnitProcessor.Context uctx = UnitProcessor.createContext(ctx, maskedElement);
      return convertRegion(xStr, yStr, wStr, hStr, unitsType, maskedNode, uctx);
   }

   public static Rectangle2D convertPatternRegion(Element patternElement, Element paintedElement, GraphicsNode paintedNode, BridgeContext ctx) {
      String xStr = getChainableAttributeNS(patternElement, (String)null, "x", ctx);
      if (xStr.length() == 0) {
         xStr = "0";
      }

      String yStr = getChainableAttributeNS(patternElement, (String)null, "y", ctx);
      if (yStr.length() == 0) {
         yStr = "0";
      }

      String wStr = getChainableAttributeNS(patternElement, (String)null, "width", ctx);
      if (wStr.length() == 0) {
         throw new BridgeException(ctx, patternElement, "attribute.missing", new Object[]{"width"});
      } else {
         String hStr = getChainableAttributeNS(patternElement, (String)null, "height", ctx);
         if (hStr.length() == 0) {
            throw new BridgeException(ctx, patternElement, "attribute.missing", new Object[]{"height"});
         } else {
            String units = getChainableAttributeNS(patternElement, (String)null, "patternUnits", ctx);
            short unitsType;
            if (units.length() == 0) {
               unitsType = 2;
            } else {
               unitsType = parseCoordinateSystem(patternElement, "patternUnits", units, ctx);
            }

            org.apache.batik.parser.UnitProcessor.Context uctx = UnitProcessor.createContext(ctx, paintedElement);
            return convertRegion(xStr, yStr, wStr, hStr, unitsType, paintedNode, uctx);
         }
      }
   }

   public static float[] convertFilterRes(Element filterElement, BridgeContext ctx) {
      float[] filterRes = new float[2];
      String s = getChainableAttributeNS(filterElement, (String)null, "filterRes", ctx);
      Float[] vals = convertSVGNumberOptionalNumber(filterElement, "filterRes", s, ctx);
      if (!(filterRes[0] < 0.0F) && !(filterRes[1] < 0.0F)) {
         if (vals[0] == null) {
            filterRes[0] = -1.0F;
         } else {
            filterRes[0] = vals[0];
            if (filterRes[0] < 0.0F) {
               throw new BridgeException(ctx, filterElement, "attribute.malformed", new Object[]{"filterRes", s});
            }
         }

         if (vals[1] == null) {
            filterRes[1] = filterRes[0];
         } else {
            filterRes[1] = vals[1];
            if (filterRes[1] < 0.0F) {
               throw new BridgeException(ctx, filterElement, "attribute.malformed", new Object[]{"filterRes", s});
            }
         }

         return filterRes;
      } else {
         throw new BridgeException(ctx, filterElement, "attribute.malformed", new Object[]{"filterRes", s});
      }
   }

   public static Float[] convertSVGNumberOptionalNumber(Element elem, String attrName, String attrValue, BridgeContext ctx) {
      Float[] ret = new Float[2];
      if (attrValue.length() == 0) {
         return ret;
      } else {
         try {
            StringTokenizer tokens = new StringTokenizer(attrValue, " ");
            ret[0] = Float.parseFloat(tokens.nextToken());
            if (tokens.hasMoreTokens()) {
               ret[1] = Float.parseFloat(tokens.nextToken());
            }

            if (tokens.hasMoreTokens()) {
               throw new BridgeException(ctx, elem, "attribute.malformed", new Object[]{attrName, attrValue});
            } else {
               return ret;
            }
         } catch (NumberFormatException var6) {
            throw new BridgeException(ctx, elem, var6, "attribute.malformed", new Object[]{attrName, attrValue, var6});
         }
      }
   }

   public static Rectangle2D convertFilterChainRegion(Element filterElement, Element filteredElement, GraphicsNode filteredNode, BridgeContext ctx) {
      String xStr = getChainableAttributeNS(filterElement, (String)null, "x", ctx);
      if (xStr.length() == 0) {
         xStr = "-10%";
      }

      String yStr = getChainableAttributeNS(filterElement, (String)null, "y", ctx);
      if (yStr.length() == 0) {
         yStr = "-10%";
      }

      String wStr = getChainableAttributeNS(filterElement, (String)null, "width", ctx);
      if (wStr.length() == 0) {
         wStr = "120%";
      }

      String hStr = getChainableAttributeNS(filterElement, (String)null, "height", ctx);
      if (hStr.length() == 0) {
         hStr = "120%";
      }

      String units = getChainableAttributeNS(filterElement, (String)null, "filterUnits", ctx);
      short unitsType;
      if (units.length() == 0) {
         unitsType = 2;
      } else {
         unitsType = parseCoordinateSystem(filterElement, "filterUnits", units, ctx);
      }

      org.apache.batik.parser.UnitProcessor.Context uctx = UnitProcessor.createContext(ctx, filteredElement);
      Rectangle2D region = convertRegion(xStr, yStr, wStr, hStr, unitsType, filteredNode, uctx);
      units = getChainableAttributeNS(filterElement, (String)null, "filterMarginsUnits", ctx);
      if (units.length() == 0) {
         unitsType = 1;
      } else {
         unitsType = parseCoordinateSystem(filterElement, "filterMarginsUnits", units, ctx);
      }

      String dxStr = filterElement.getAttributeNS((String)null, "mx");
      if (dxStr.length() == 0) {
         dxStr = "0";
      }

      String dyStr = filterElement.getAttributeNS((String)null, "my");
      if (dyStr.length() == 0) {
         dyStr = "0";
      }

      String dwStr = filterElement.getAttributeNS((String)null, "mw");
      if (dwStr.length() == 0) {
         dwStr = "0";
      }

      String dhStr = filterElement.getAttributeNS((String)null, "mh");
      if (dhStr.length() == 0) {
         dhStr = "0";
      }

      return extendRegion(dxStr, dyStr, dwStr, dhStr, unitsType, filteredNode, region, uctx);
   }

   protected static Rectangle2D extendRegion(String dxStr, String dyStr, String dwStr, String dhStr, short unitsType, GraphicsNode filteredNode, Rectangle2D region, org.apache.batik.parser.UnitProcessor.Context uctx) {
      float dx;
      float dy;
      float dw;
      float dh;
      switch (unitsType) {
         case 1:
            dx = UnitProcessor.svgHorizontalCoordinateToUserSpace(dxStr, "mx", uctx);
            dy = UnitProcessor.svgVerticalCoordinateToUserSpace(dyStr, "my", uctx);
            dw = UnitProcessor.svgHorizontalCoordinateToUserSpace(dwStr, "mw", uctx);
            dh = UnitProcessor.svgVerticalCoordinateToUserSpace(dhStr, "mh", uctx);
            break;
         case 2:
            Rectangle2D bounds = filteredNode.getGeometryBounds();
            if (bounds == null) {
               dh = 0.0F;
               dw = 0.0F;
               dy = 0.0F;
               dx = 0.0F;
            } else {
               dx = UnitProcessor.svgHorizontalCoordinateToObjectBoundingBox(dxStr, "mx", uctx);
               dx = (float)((double)dx * bounds.getWidth());
               dy = UnitProcessor.svgVerticalCoordinateToObjectBoundingBox(dyStr, "my", uctx);
               dy = (float)((double)dy * bounds.getHeight());
               dw = UnitProcessor.svgHorizontalCoordinateToObjectBoundingBox(dwStr, "mw", uctx);
               dw = (float)((double)dw * bounds.getWidth());
               dh = UnitProcessor.svgVerticalCoordinateToObjectBoundingBox(dhStr, "mh", uctx);
               dh = (float)((double)dh * bounds.getHeight());
            }
            break;
         default:
            throw new IllegalArgumentException("Invalid unit type");
      }

      region.setRect(region.getX() + (double)dx, region.getY() + (double)dy, region.getWidth() + (double)dw, region.getHeight() + (double)dh);
      return region;
   }

   public static Rectangle2D getBaseFilterPrimitiveRegion(Element filterPrimitiveElement, Element filteredElement, GraphicsNode filteredNode, Rectangle2D defaultRegion, BridgeContext ctx) {
      org.apache.batik.parser.UnitProcessor.Context uctx = UnitProcessor.createContext(ctx, filteredElement);
      double x = defaultRegion.getX();
      String s = filterPrimitiveElement.getAttributeNS((String)null, "x");
      if (s.length() != 0) {
         x = (double)UnitProcessor.svgHorizontalCoordinateToUserSpace(s, "x", uctx);
      }

      double y = defaultRegion.getY();
      s = filterPrimitiveElement.getAttributeNS((String)null, "y");
      if (s.length() != 0) {
         y = (double)UnitProcessor.svgVerticalCoordinateToUserSpace(s, "y", uctx);
      }

      double w = defaultRegion.getWidth();
      s = filterPrimitiveElement.getAttributeNS((String)null, "width");
      if (s.length() != 0) {
         w = (double)UnitProcessor.svgHorizontalLengthToUserSpace(s, "width", uctx);
      }

      double h = defaultRegion.getHeight();
      s = filterPrimitiveElement.getAttributeNS((String)null, "height");
      if (s.length() != 0) {
         h = (double)UnitProcessor.svgVerticalLengthToUserSpace(s, "height", uctx);
      }

      return new Rectangle2D.Double(x, y, w, h);
   }

   public static Rectangle2D convertFilterPrimitiveRegion(Element filterPrimitiveElement, Element filterElement, Element filteredElement, GraphicsNode filteredNode, Rectangle2D defaultRegion, Rectangle2D filterRegion, BridgeContext ctx) {
      String units = "";
      if (filterElement != null) {
         units = getChainableAttributeNS(filterElement, (String)null, "primitiveUnits", ctx);
      }

      short unitsType;
      if (units.length() == 0) {
         unitsType = 1;
      } else {
         unitsType = parseCoordinateSystem(filterElement, "filterUnits", units, ctx);
      }

      String xStr = "";
      String yStr = "";
      String wStr = "";
      String hStr = "";
      if (filterPrimitiveElement != null) {
         xStr = filterPrimitiveElement.getAttributeNS((String)null, "x");
         yStr = filterPrimitiveElement.getAttributeNS((String)null, "y");
         wStr = filterPrimitiveElement.getAttributeNS((String)null, "width");
         hStr = filterPrimitiveElement.getAttributeNS((String)null, "height");
      }

      double x = defaultRegion.getX();
      double y = defaultRegion.getY();
      double w = defaultRegion.getWidth();
      double h = defaultRegion.getHeight();
      org.apache.batik.parser.UnitProcessor.Context uctx = UnitProcessor.createContext(ctx, filteredElement);
      Rectangle2D region;
      switch (unitsType) {
         case 1:
            if (xStr.length() != 0) {
               x = (double)UnitProcessor.svgHorizontalCoordinateToUserSpace(xStr, "x", uctx);
            }

            if (yStr.length() != 0) {
               y = (double)UnitProcessor.svgVerticalCoordinateToUserSpace(yStr, "y", uctx);
            }

            if (wStr.length() != 0) {
               w = (double)UnitProcessor.svgHorizontalLengthToUserSpace(wStr, "width", uctx);
            }

            if (hStr.length() != 0) {
               h = (double)UnitProcessor.svgVerticalLengthToUserSpace(hStr, "height", uctx);
            }
            break;
         case 2:
            region = filteredNode.getGeometryBounds();
            if (region != null) {
               if (xStr.length() != 0) {
                  x = (double)UnitProcessor.svgHorizontalCoordinateToObjectBoundingBox(xStr, "x", uctx);
                  x = region.getX() + x * region.getWidth();
               }

               if (yStr.length() != 0) {
                  y = (double)UnitProcessor.svgVerticalCoordinateToObjectBoundingBox(yStr, "y", uctx);
                  y = region.getY() + y * region.getHeight();
               }

               if (wStr.length() != 0) {
                  w = (double)UnitProcessor.svgHorizontalLengthToObjectBoundingBox(wStr, "width", uctx);
                  w *= region.getWidth();
               }

               if (hStr.length() != 0) {
                  h = (double)UnitProcessor.svgVerticalLengthToObjectBoundingBox(hStr, "height", uctx);
                  h *= region.getHeight();
               }
            }
            break;
         default:
            throw new RuntimeException("invalid unitsType:" + unitsType);
      }

      Rectangle2D region = new Rectangle2D.Double(x, y, w, h);
      units = "";
      if (filterElement != null) {
         units = getChainableAttributeNS(filterElement, (String)null, "filterPrimitiveMarginsUnits", ctx);
      }

      if (units.length() == 0) {
         unitsType = 1;
      } else {
         unitsType = parseCoordinateSystem(filterElement, "filterPrimitiveMarginsUnits", units, ctx);
      }

      String dxStr = "";
      String dyStr = "";
      String dwStr = "";
      String dhStr = "";
      if (filterPrimitiveElement != null) {
         dxStr = filterPrimitiveElement.getAttributeNS((String)null, "mx");
         dyStr = filterPrimitiveElement.getAttributeNS((String)null, "my");
         dwStr = filterPrimitiveElement.getAttributeNS((String)null, "mw");
         dhStr = filterPrimitiveElement.getAttributeNS((String)null, "mh");
      }

      if (dxStr.length() == 0) {
         dxStr = "0";
      }

      if (dyStr.length() == 0) {
         dyStr = "0";
      }

      if (dwStr.length() == 0) {
         dwStr = "0";
      }

      if (dhStr.length() == 0) {
         dhStr = "0";
      }

      region = extendRegion(dxStr, dyStr, dwStr, dhStr, unitsType, filteredNode, region, uctx);
      Rectangle2D.intersect(region, filterRegion, region);
      return region;
   }

   public static Rectangle2D convertFilterPrimitiveRegion(Element filterPrimitiveElement, Element filteredElement, GraphicsNode filteredNode, Rectangle2D defaultRegion, Rectangle2D filterRegion, BridgeContext ctx) {
      Node parentNode = filterPrimitiveElement.getParentNode();
      Element filterElement = null;
      if (parentNode != null && parentNode.getNodeType() == 1) {
         filterElement = (Element)parentNode;
      }

      return convertFilterPrimitiveRegion(filterPrimitiveElement, filterElement, filteredElement, filteredNode, defaultRegion, filterRegion, ctx);
   }

   public static short parseCoordinateSystem(Element e, String attr, String coordinateSystem, BridgeContext ctx) {
      if ("userSpaceOnUse".equals(coordinateSystem)) {
         return 1;
      } else if ("objectBoundingBox".equals(coordinateSystem)) {
         return 2;
      } else {
         throw new BridgeException(ctx, e, "attribute.malformed", new Object[]{attr, coordinateSystem});
      }
   }

   public static short parseMarkerCoordinateSystem(Element e, String attr, String coordinateSystem, BridgeContext ctx) {
      if ("userSpaceOnUse".equals(coordinateSystem)) {
         return 1;
      } else if ("strokeWidth".equals(coordinateSystem)) {
         return 3;
      } else {
         throw new BridgeException(ctx, e, "attribute.malformed", new Object[]{attr, coordinateSystem});
      }
   }

   protected static Rectangle2D convertRegion(String xStr, String yStr, String wStr, String hStr, short unitsType, GraphicsNode targetNode, org.apache.batik.parser.UnitProcessor.Context uctx) {
      double x;
      double y;
      double w;
      double h;
      switch (unitsType) {
         case 1:
            x = (double)UnitProcessor.svgHorizontalCoordinateToUserSpace(xStr, "x", uctx);
            y = (double)UnitProcessor.svgVerticalCoordinateToUserSpace(yStr, "y", uctx);
            w = (double)UnitProcessor.svgHorizontalLengthToUserSpace(wStr, "width", uctx);
            h = (double)UnitProcessor.svgVerticalLengthToUserSpace(hStr, "height", uctx);
            break;
         case 2:
            x = (double)UnitProcessor.svgHorizontalCoordinateToObjectBoundingBox(xStr, "x", uctx);
            y = (double)UnitProcessor.svgVerticalCoordinateToObjectBoundingBox(yStr, "y", uctx);
            w = (double)UnitProcessor.svgHorizontalLengthToObjectBoundingBox(wStr, "width", uctx);
            h = (double)UnitProcessor.svgVerticalLengthToObjectBoundingBox(hStr, "height", uctx);
            Rectangle2D bounds = targetNode.getGeometryBounds();
            if (bounds != null) {
               x = bounds.getX() + x * bounds.getWidth();
               y = bounds.getY() + y * bounds.getHeight();
               w *= bounds.getWidth();
               h *= bounds.getHeight();
            } else {
               h = 0.0;
               w = 0.0;
               y = 0.0;
               x = 0.0;
            }
            break;
         default:
            throw new RuntimeException("invalid unitsType:" + unitsType);
      }

      return new Rectangle2D.Double(x, y, w, h);
   }

   public static AffineTransform convertTransform(Element e, String attr, String transform, BridgeContext ctx) {
      try {
         return AWTTransformProducer.createAffineTransform(transform);
      } catch (ParseException var5) {
         throw new BridgeException(ctx, e, var5, "attribute.malformed", new Object[]{attr, transform, var5});
      }
   }

   public static AffineTransform toObjectBBox(AffineTransform Tx, GraphicsNode node) {
      AffineTransform Mx = new AffineTransform();
      Rectangle2D bounds = node.getGeometryBounds();
      if (bounds != null) {
         Mx.translate(bounds.getX(), bounds.getY());
         Mx.scale(bounds.getWidth(), bounds.getHeight());
      }

      Mx.concatenate(Tx);
      return Mx;
   }

   public static Rectangle2D toObjectBBox(Rectangle2D r, GraphicsNode node) {
      Rectangle2D bounds = node.getGeometryBounds();
      return bounds != null ? new Rectangle2D.Double(bounds.getX() + r.getX() * bounds.getWidth(), bounds.getY() + r.getY() * bounds.getHeight(), r.getWidth() * bounds.getWidth(), r.getHeight() * bounds.getHeight()) : new Rectangle2D.Double();
   }

   public static float convertSnapshotTime(Element e, BridgeContext ctx) {
      if (!e.hasAttributeNS((String)null, "snapshotTime")) {
         return 0.0F;
      } else {
         String t = e.getAttributeNS((String)null, "snapshotTime");
         if (t.equals("none")) {
            return 0.0F;
         } else {
            ClockParser p = new ClockParser(false);

            class Handler implements ClockHandler {
               float time;

               public void clockValue(float t) {
                  this.time = t;
               }
            }

            Handler h = new Handler();
            p.setClockHandler(h);

            try {
               p.parse(t);
            } catch (ParseException var6) {
               throw new BridgeException((BridgeContext)null, e, var6, "attribute.malformed", new Object[]{"snapshotTime", t, var6});
            }

            return h.time;
         }
      }
   }
}
