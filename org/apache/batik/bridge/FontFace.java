package org.apache.batik.bridge;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.batik.dom.AbstractNode;
import org.apache.batik.gvt.font.GVTFontFace;
import org.apache.batik.gvt.font.GVTFontFamily;
import org.apache.batik.util.ParsedURL;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGDocument;

public abstract class FontFace extends GVTFontFace implements ErrorConstants {
   List srcs;

   public FontFace(List srcs, String familyName, float unitsPerEm, String fontWeight, String fontStyle, String fontVariant, String fontStretch, float slope, String panose1, float ascent, float descent, float strikethroughPosition, float strikethroughThickness, float underlinePosition, float underlineThickness, float overlinePosition, float overlineThickness) {
      super(familyName, unitsPerEm, fontWeight, fontStyle, fontVariant, fontStretch, slope, panose1, ascent, descent, strikethroughPosition, strikethroughThickness, underlinePosition, underlineThickness, overlinePosition, overlineThickness);
      this.srcs = srcs;
   }

   protected FontFace(String familyName) {
      super(familyName);
   }

   public static CSSFontFace createFontFace(String familyName, FontFace src) {
      return new CSSFontFace(new LinkedList(src.srcs), familyName, src.unitsPerEm, src.fontWeight, src.fontStyle, src.fontVariant, src.fontStretch, src.slope, src.panose1, src.ascent, src.descent, src.strikethroughPosition, src.strikethroughThickness, src.underlinePosition, src.underlineThickness, src.overlinePosition, src.overlineThickness);
   }

   public GVTFontFamily getFontFamily(BridgeContext ctx) {
      FontFamilyResolver fontFamilyResolver = ctx.getFontFamilyResolver();
      GVTFontFamily family = fontFamilyResolver.resolve(this.familyName, this);
      if (family != null) {
         return family;
      } else {
         Iterator var4 = this.srcs.iterator();

         label43:
         do {
            while(var4.hasNext()) {
               Object o = var4.next();
               if (o instanceof String) {
                  family = fontFamilyResolver.resolve((String)o, this);
                  continue label43;
               }

               if (o instanceof ParsedURL) {
                  try {
                     GVTFontFamily ff = this.getFontFamily(ctx, (ParsedURL)o);
                     if (ff != null) {
                        return ff;
                     }
                  } catch (SecurityException var7) {
                     ctx.getUserAgent().displayError(var7);
                  } catch (BridgeException var8) {
                     if ("uri.unsecure".equals(var8.getCode())) {
                        ctx.getUserAgent().displayError(var8);
                     }
                  } catch (Exception var9) {
                  }
               }
            }

            return null;
         } while(family == null);

         return family;
      }
   }

   protected GVTFontFamily getFontFamily(BridgeContext ctx, ParsedURL purl) {
      String purlStr = purl.toString();
      Element e = this.getBaseElement(ctx);
      SVGDocument svgDoc = (SVGDocument)e.getOwnerDocument();
      String docURL = svgDoc.getURL();
      ParsedURL pDocURL = null;
      if (docURL != null) {
         pDocURL = new ParsedURL(docURL);
      }

      String baseURI = AbstractNode.getBaseURI(e);
      purl = new ParsedURL(baseURI, purlStr);
      UserAgent userAgent = ctx.getUserAgent();

      try {
         userAgent.checkLoadExternalResource(purl, pDocURL);
      } catch (SecurityException var18) {
         userAgent.displayError(var18);
         return null;
      }

      if (purl.getRef() == null) {
         try {
            return ctx.getFontFamilyResolver().loadFont(purl.openStream(), this);
         } catch (Exception var17) {
            return null;
         }
      } else {
         Element ref = ctx.getReferencedElement(e, purlStr);
         if (ref.getNamespaceURI().equals("http://www.w3.org/2000/svg") && ref.getLocalName().equals("font")) {
            SVGDocument doc = (SVGDocument)e.getOwnerDocument();
            SVGDocument rdoc = (SVGDocument)ref.getOwnerDocument();
            Element fontElt = ref;
            if (doc != rdoc) {
               fontElt = (Element)doc.importNode(ref, true);
               String base = AbstractNode.getBaseURI(ref);
               Element g = doc.createElementNS("http://www.w3.org/2000/svg", "g");
               g.appendChild(fontElt);
               g.setAttributeNS("http://www.w3.org/XML/1998/namespace", "xml:base", base);
               CSSUtilities.computeStyleAndURIs(ref, fontElt, purlStr);
            }

            Element fontFaceElt = null;

            for(Node n = fontElt.getFirstChild(); n != null; n = n.getNextSibling()) {
               if (n.getNodeType() == 1 && n.getNamespaceURI().equals("http://www.w3.org/2000/svg") && n.getLocalName().equals("font-face")) {
                  fontFaceElt = (Element)n;
                  break;
               }
            }

            SVGFontFaceElementBridge fontFaceBridge = (SVGFontFaceElementBridge)ctx.getBridge("http://www.w3.org/2000/svg", "font-face");
            GVTFontFace gff = fontFaceBridge.createFontFace(ctx, fontFaceElt);
            return new SVGFontFamily(gff, fontElt, ctx);
         } else {
            return null;
         }
      }
   }

   protected Element getBaseElement(BridgeContext ctx) {
      SVGDocument d = (SVGDocument)ctx.getDocument();
      return d.getRootElement();
   }
}
