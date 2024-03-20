package org.apache.batik.bridge;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.font.GVTFontFace;
import org.apache.batik.gvt.font.Glyph;
import org.apache.batik.gvt.text.TextPaintInfo;
import org.apache.batik.parser.AWTPathProducer;
import org.apache.batik.parser.ParseException;
import org.apache.batik.parser.PathParser;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SVGGlyphElementBridge extends AbstractSVGBridge implements ErrorConstants {
   protected SVGGlyphElementBridge() {
   }

   public String getLocalName() {
      return "glyph";
   }

   public Glyph createGlyph(BridgeContext ctx, Element glyphElement, Element textElement, int glyphCode, float fontSize, GVTFontFace fontFace, TextPaintInfo tpi) {
      float fontHeight = fontFace.getUnitsPerEm();
      float scale = fontSize / fontHeight;
      AffineTransform scaleTransform = AffineTransform.getScaleInstance((double)scale, (double)(-scale));
      String d = glyphElement.getAttributeNS((String)null, "d");
      Shape dShape = null;
      if (d.length() != 0) {
         AWTPathProducer app = new AWTPathProducer();
         app.setWindingRule(CSSUtilities.convertFillRule(textElement));
         boolean var42 = false;

         try {
            var42 = true;
            PathParser pathParser = new PathParser();
            pathParser.setPathHandler(app);
            pathParser.parse(d);
            var42 = false;
         } catch (ParseException var49) {
            throw new BridgeException(ctx, glyphElement, var49, "attribute.malformed", new Object[]{"d"});
         } finally {
            if (var42) {
               Shape shape = app.getShape();
               Shape var18 = scaleTransform.createTransformedShape(shape);
            }
         }

         Shape shape = app.getShape();
         Shape transformedShape = scaleTransform.createTransformedShape(shape);
         dShape = transformedShape;
      }

      NodeList glyphChildren = glyphElement.getChildNodes();
      int numChildren = glyphChildren.getLength();
      int numGlyphChildren = 0;

      for(int i = 0; i < numChildren; ++i) {
         Node childNode = glyphChildren.item(i);
         if (childNode.getNodeType() == 1) {
            ++numGlyphChildren;
         }
      }

      CompositeGraphicsNode glyphContentNode = null;
      if (numGlyphChildren > 0) {
         GVTBuilder builder = ctx.getGVTBuilder();
         glyphContentNode = new CompositeGraphicsNode();
         Element fontElementClone = (Element)glyphElement.getParentNode().cloneNode(false);
         NamedNodeMap fontAttributes = glyphElement.getParentNode().getAttributes();
         int numAttributes = fontAttributes.getLength();

         for(int i = 0; i < numAttributes; ++i) {
            fontElementClone.setAttributeNode((Attr)fontAttributes.item(i));
         }

         Element clonedGlyphElement = (Element)glyphElement.cloneNode(true);
         fontElementClone.appendChild(clonedGlyphElement);
         textElement.appendChild(fontElementClone);
         CompositeGraphicsNode glyphChildrenNode = new CompositeGraphicsNode();
         glyphChildrenNode.setTransform(scaleTransform);
         NodeList clonedGlyphChildren = clonedGlyphElement.getChildNodes();
         int numClonedChildren = clonedGlyphChildren.getLength();

         for(int i = 0; i < numClonedChildren; ++i) {
            Node childNode = clonedGlyphChildren.item(i);
            if (childNode.getNodeType() == 1) {
               Element childElement = (Element)childNode;
               GraphicsNode childGraphicsNode = builder.build(ctx, childElement);
               glyphChildrenNode.add(childGraphicsNode);
            }
         }

         glyphContentNode.add(glyphChildrenNode);
         textElement.removeChild(fontElementClone);
      }

      String unicode = glyphElement.getAttributeNS((String)null, "unicode");
      String nameList = glyphElement.getAttributeNS((String)null, "glyph-name");
      List names = new ArrayList();
      StringTokenizer st = new StringTokenizer(nameList, " ,");

      while(st.hasMoreTokens()) {
         names.add(st.nextToken());
      }

      String orientation = glyphElement.getAttributeNS((String)null, "orientation");
      String arabicForm = glyphElement.getAttributeNS((String)null, "arabic-form");
      String lang = glyphElement.getAttributeNS((String)null, "lang");
      Element parentFontElement = (Element)glyphElement.getParentNode();
      String s = glyphElement.getAttributeNS((String)null, "horiz-adv-x");
      if (s.length() == 0) {
         s = parentFontElement.getAttributeNS((String)null, "horiz-adv-x");
         if (s.length() == 0) {
            throw new BridgeException(ctx, parentFontElement, "attribute.missing", new Object[]{"horiz-adv-x"});
         }
      }

      float horizAdvX;
      try {
         horizAdvX = SVGUtilities.convertSVGNumber(s) * scale;
      } catch (NumberFormatException var48) {
         throw new BridgeException(ctx, glyphElement, var48, "attribute.malformed", new Object[]{"horiz-adv-x", s});
      }

      s = glyphElement.getAttributeNS((String)null, "vert-adv-y");
      if (s.length() == 0) {
         s = parentFontElement.getAttributeNS((String)null, "vert-adv-y");
         if (s.length() == 0) {
            s = String.valueOf(fontFace.getUnitsPerEm());
         }
      }

      float vertAdvY;
      try {
         vertAdvY = SVGUtilities.convertSVGNumber(s) * scale;
      } catch (NumberFormatException var47) {
         throw new BridgeException(ctx, glyphElement, var47, "attribute.malformed", new Object[]{"vert-adv-y", s});
      }

      s = glyphElement.getAttributeNS((String)null, "vert-origin-x");
      if (s.length() == 0) {
         s = parentFontElement.getAttributeNS((String)null, "vert-origin-x");
         if (s.length() == 0) {
            s = Float.toString(horizAdvX / 2.0F);
         }
      }

      float vertOriginX;
      try {
         vertOriginX = SVGUtilities.convertSVGNumber(s) * scale;
      } catch (NumberFormatException var46) {
         throw new BridgeException(ctx, glyphElement, var46, "attribute.malformed", new Object[]{"vert-origin-x", s});
      }

      s = glyphElement.getAttributeNS((String)null, "vert-origin-y");
      if (s.length() == 0) {
         s = parentFontElement.getAttributeNS((String)null, "vert-origin-y");
         if (s.length() == 0) {
            s = String.valueOf(fontFace.getAscent());
         }
      }

      float vertOriginY;
      try {
         vertOriginY = SVGUtilities.convertSVGNumber(s) * -scale;
      } catch (NumberFormatException var45) {
         throw new BridgeException(ctx, glyphElement, var45, "attribute.malformed", new Object[]{"vert-origin-y", s});
      }

      Point2D vertOrigin = new Point2D.Float(vertOriginX, vertOriginY);
      s = parentFontElement.getAttributeNS((String)null, "horiz-origin-x");
      if (s.length() == 0) {
         s = "0";
      }

      float horizOriginX;
      try {
         horizOriginX = SVGUtilities.convertSVGNumber(s) * scale;
      } catch (NumberFormatException var44) {
         throw new BridgeException(ctx, parentFontElement, var44, "attribute.malformed", new Object[]{"horiz-origin-x", s});
      }

      s = parentFontElement.getAttributeNS((String)null, "horiz-origin-y");
      if (s.length() == 0) {
         s = "0";
      }

      float horizOriginY;
      try {
         horizOriginY = SVGUtilities.convertSVGNumber(s) * -scale;
      } catch (NumberFormatException var43) {
         throw new BridgeException(ctx, glyphElement, var43, "attribute.malformed", new Object[]{"horiz-origin-y", s});
      }

      Point2D horizOrigin = new Point2D.Float(horizOriginX, horizOriginY);
      return new Glyph(unicode, names, orientation, arabicForm, lang, horizOrigin, vertOrigin, horizAdvX, vertAdvY, glyphCode, tpi, dShape, glyphContentNode);
   }
}
