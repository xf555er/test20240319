package org.apache.batik.bridge;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import org.apache.batik.dom.util.XLinkSupport;
import org.apache.batik.gvt.text.TextPath;
import org.apache.batik.parser.AWTPathProducer;
import org.apache.batik.parser.ParseException;
import org.apache.batik.parser.PathParser;
import org.w3c.dom.Element;

public class SVGTextPathElementBridge extends AnimatableGenericSVGBridge implements ErrorConstants {
   public String getLocalName() {
      return "textPath";
   }

   public void handleElement(BridgeContext ctx, Element e) {
   }

   public TextPath createTextPath(BridgeContext ctx, Element textPathElement) {
      String uri = XLinkSupport.getXLinkHref(textPathElement);
      Element pathElement = ctx.getReferencedElement(textPathElement, uri);
      if (pathElement != null && "http://www.w3.org/2000/svg".equals(pathElement.getNamespaceURI()) && pathElement.getLocalName().equals("path")) {
         String s = pathElement.getAttributeNS((String)null, "d");
         Shape pathShape = null;
         if (s.length() != 0) {
            AWTPathProducer app = new AWTPathProducer();
            app.setWindingRule(CSSUtilities.convertFillRule(pathElement));

            try {
               PathParser pathParser = new PathParser();
               pathParser.setPathHandler(app);
               pathParser.parse(s);
            } catch (ParseException var18) {
               throw new BridgeException(ctx, pathElement, var18, "attribute.malformed", new Object[]{"d"});
            } finally {
               pathShape = app.getShape();
            }

            s = pathElement.getAttributeNS((String)null, "transform");
            if (s.length() != 0) {
               AffineTransform tr = SVGUtilities.convertTransform(pathElement, "transform", s, ctx);
               pathShape = tr.createTransformedShape(pathShape);
            }

            TextPath textPath = new TextPath(new GeneralPath(pathShape));
            s = textPathElement.getAttributeNS((String)null, "startOffset");
            if (s.length() > 0) {
               float startOffset = 0.0F;
               int percentIndex = s.indexOf(37);
               if (percentIndex != -1) {
                  float pathLength = textPath.lengthOfPath();
                  String percentString = s.substring(0, percentIndex);
                  float startOffsetPercent = 0.0F;

                  try {
                     startOffsetPercent = SVGUtilities.convertSVGNumber(percentString);
                  } catch (NumberFormatException var17) {
                     throw new BridgeException(ctx, textPathElement, "attribute.malformed", new Object[]{"startOffset", s});
                  }

                  startOffset = (float)((double)(startOffsetPercent * pathLength) / 100.0);
               } else {
                  org.apache.batik.parser.UnitProcessor.Context uctx = UnitProcessor.createContext(ctx, textPathElement);
                  startOffset = UnitProcessor.svgOtherLengthToUserSpace(s, "startOffset", uctx);
               }

               textPath.setStartOffset(startOffset);
            }

            return textPath;
         } else {
            throw new BridgeException(ctx, pathElement, "attribute.missing", new Object[]{"d"});
         }
      } else {
         throw new BridgeException(ctx, textPathElement, "uri.badTarget", new Object[]{uri});
      }
   }
}
