package org.apache.batik.bridge;

import java.text.AttributedCharacterIterator;
import org.apache.batik.anim.dom.SVGOMDocument;
import org.apache.batik.dom.AbstractNode;
import org.apache.batik.dom.util.XLinkSupport;
import org.apache.batik.gvt.font.Glyph;
import org.apache.batik.gvt.text.GVTAttributedCharacterIterator;
import org.apache.batik.gvt.text.TextPaintInfo;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SVGAltGlyphElementBridge extends AbstractSVGBridge implements ErrorConstants {
   public static final AttributedCharacterIterator.Attribute PAINT_INFO;

   public String getLocalName() {
      return "altGlyph";
   }

   public Glyph[] createAltGlyphArray(BridgeContext ctx, Element altGlyphElement, float fontSize, AttributedCharacterIterator aci) {
      String uri = XLinkSupport.getXLinkHref(altGlyphElement);
      Element refElement = null;

      try {
         refElement = ctx.getReferencedElement(altGlyphElement, uri);
      } catch (BridgeException var26) {
         if ("uri.unsecure".equals(var26.getCode())) {
            ctx.getUserAgent().displayError(var26);
         }
      }

      if (refElement == null) {
         return null;
      } else if (!"http://www.w3.org/2000/svg".equals(refElement.getNamespaceURI())) {
         return null;
      } else if (refElement.getLocalName().equals("glyph")) {
         Glyph glyph = this.getGlyph(ctx, uri, altGlyphElement, fontSize, aci);
         if (glyph == null) {
            return null;
         } else {
            Glyph[] glyphArray = new Glyph[]{glyph};
            return glyphArray;
         }
      } else {
         if (refElement.getLocalName().equals("altGlyphDef")) {
            SVGOMDocument document = (SVGOMDocument)altGlyphElement.getOwnerDocument();
            SVGOMDocument refDocument = (SVGOMDocument)refElement.getOwnerDocument();
            boolean isLocal = refDocument == document;
            Element localRefElement = isLocal ? refElement : (Element)document.importNode(refElement, true);
            if (!isLocal) {
               String base = AbstractNode.getBaseURI(altGlyphElement);
               Element g = document.createElementNS("http://www.w3.org/2000/svg", "g");
               g.appendChild(localRefElement);
               g.setAttributeNS("http://www.w3.org/XML/1998/namespace", "xml:base", base);
               CSSUtilities.computeStyleAndURIs(refElement, localRefElement, uri);
            }

            NodeList altGlyphDefChildren = localRefElement.getChildNodes();
            boolean containsGlyphRefNodes = false;
            int numAltGlyphDefChildren = altGlyphDefChildren.getLength();

            for(int i = 0; i < numAltGlyphDefChildren; ++i) {
               Node altGlyphChild = altGlyphDefChildren.item(i);
               if (altGlyphChild.getNodeType() == 1) {
                  Element agc = (Element)altGlyphChild;
                  if ("http://www.w3.org/2000/svg".equals(agc.getNamespaceURI()) && "glyphRef".equals(agc.getLocalName())) {
                     containsGlyphRefNodes = true;
                     break;
                  }
               }
            }

            NodeList altGlyphItemNodes;
            int numAltGlyphItemNodes;
            if (containsGlyphRefNodes) {
               altGlyphItemNodes = localRefElement.getElementsByTagNameNS("http://www.w3.org/2000/svg", "glyphRef");
               numAltGlyphItemNodes = altGlyphItemNodes.getLength();
               Glyph[] glyphArray = new Glyph[numAltGlyphItemNodes];

               for(int i = 0; i < numAltGlyphItemNodes; ++i) {
                  Element glyphRefElement = (Element)altGlyphItemNodes.item(i);
                  String glyphUri = XLinkSupport.getXLinkHref(glyphRefElement);
                  Glyph glyph = this.getGlyph(ctx, glyphUri, glyphRefElement, fontSize, aci);
                  if (glyph == null) {
                     return null;
                  }

                  glyphArray[i] = glyph;
               }

               return glyphArray;
            }

            altGlyphItemNodes = localRefElement.getElementsByTagNameNS("http://www.w3.org/2000/svg", "altGlyphItem");
            numAltGlyphItemNodes = altGlyphItemNodes.getLength();
            if (numAltGlyphItemNodes > 0) {
               boolean foundMatchingGlyph = false;
               Glyph[] glyphArray = null;

               for(int i = 0; i < numAltGlyphItemNodes && !foundMatchingGlyph; ++i) {
                  Element altGlyphItemElement = (Element)altGlyphItemNodes.item(i);
                  NodeList altGlyphRefNodes = altGlyphItemElement.getElementsByTagNameNS("http://www.w3.org/2000/svg", "glyphRef");
                  int numAltGlyphRefNodes = altGlyphRefNodes.getLength();
                  glyphArray = new Glyph[numAltGlyphRefNodes];
                  foundMatchingGlyph = true;

                  for(int j = 0; j < numAltGlyphRefNodes; ++j) {
                     Element glyphRefElement = (Element)altGlyphRefNodes.item(j);
                     String glyphUri = XLinkSupport.getXLinkHref(glyphRefElement);
                     Glyph glyph = this.getGlyph(ctx, glyphUri, glyphRefElement, fontSize, aci);
                     if (glyph == null) {
                        foundMatchingGlyph = false;
                        break;
                     }

                     glyphArray[j] = glyph;
                  }
               }

               if (!foundMatchingGlyph) {
                  return null;
               }

               return glyphArray;
            }
         }

         return null;
      }
   }

   private Glyph getGlyph(BridgeContext ctx, String glyphUri, Element altGlyphElement, float fontSize, AttributedCharacterIterator aci) {
      Element refGlyphElement = null;

      try {
         refGlyphElement = ctx.getReferencedElement(altGlyphElement, glyphUri);
      } catch (BridgeException var19) {
         if ("uri.unsecure".equals(var19.getCode())) {
            ctx.getUserAgent().displayError(var19);
         }
      }

      if (refGlyphElement != null && "http://www.w3.org/2000/svg".equals(refGlyphElement.getNamespaceURI()) && "glyph".equals(refGlyphElement.getLocalName())) {
         SVGOMDocument document = (SVGOMDocument)altGlyphElement.getOwnerDocument();
         SVGOMDocument refDocument = (SVGOMDocument)refGlyphElement.getOwnerDocument();
         boolean isLocal = refDocument == document;
         Element localGlyphElement = null;
         Element localFontFaceElement = null;
         Element localFontElement = null;
         if (isLocal) {
            localGlyphElement = refGlyphElement;
            localFontElement = (Element)refGlyphElement.getParentNode();
            NodeList fontFaceElements = localFontElement.getElementsByTagNameNS("http://www.w3.org/2000/svg", "font-face");
            if (fontFaceElements.getLength() > 0) {
               localFontFaceElement = (Element)fontFaceElements.item(0);
            }
         } else {
            localFontElement = (Element)document.importNode(refGlyphElement.getParentNode(), true);
            String base = AbstractNode.getBaseURI(altGlyphElement);
            Element g = document.createElementNS("http://www.w3.org/2000/svg", "g");
            g.appendChild(localFontElement);
            g.setAttributeNS("http://www.w3.org/XML/1998/namespace", "xml:base", base);
            CSSUtilities.computeStyleAndURIs((Element)refGlyphElement.getParentNode(), localFontElement, glyphUri);
            String glyphId = refGlyphElement.getAttributeNS((String)null, "id");
            NodeList glyphElements = localFontElement.getElementsByTagNameNS("http://www.w3.org/2000/svg", "glyph");

            for(int i = 0; i < glyphElements.getLength(); ++i) {
               Element glyphElem = (Element)glyphElements.item(i);
               if (glyphElem.getAttributeNS((String)null, "id").equals(glyphId)) {
                  localGlyphElement = glyphElem;
                  break;
               }
            }

            NodeList fontFaceElements = localFontElement.getElementsByTagNameNS("http://www.w3.org/2000/svg", "font-face");
            if (fontFaceElements.getLength() > 0) {
               localFontFaceElement = (Element)fontFaceElements.item(0);
            }
         }

         if (localGlyphElement != null && localFontFaceElement != null) {
            SVGFontFaceElementBridge fontFaceBridge = (SVGFontFaceElementBridge)ctx.getBridge(localFontFaceElement);
            SVGFontFace fontFace = fontFaceBridge.createFontFace(ctx, localFontFaceElement);
            SVGGlyphElementBridge glyphBridge = (SVGGlyphElementBridge)ctx.getBridge(localGlyphElement);
            aci.first();
            TextPaintInfo tpi = (TextPaintInfo)aci.getAttribute(PAINT_INFO);
            return glyphBridge.createGlyph(ctx, localGlyphElement, altGlyphElement, -1, fontSize, fontFace, tpi);
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   static {
      PAINT_INFO = GVTAttributedCharacterIterator.TextAttribute.PAINT_INFO;
   }
}
