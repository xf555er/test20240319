package org.apache.batik.bridge;

import java.awt.color.ICC_Profile;
import java.io.IOException;
import org.apache.batik.dom.util.XLinkSupport;
import org.apache.batik.ext.awt.color.NamedProfileCache;
import org.apache.batik.util.ParsedURL;
import org.apache.xmlgraphics.java2d.color.ICCColorSpaceWithIntent;
import org.apache.xmlgraphics.java2d.color.RenderingIntent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SVGColorProfileElementBridge extends AbstractSVGBridge implements ErrorConstants {
   public NamedProfileCache cache = new NamedProfileCache();

   public String getLocalName() {
      return "color-profile";
   }

   public ICCColorSpaceWithIntent createICCColorSpaceWithIntent(BridgeContext ctx, Element paintedElement, String iccProfileName) {
      ICCColorSpaceWithIntent cs = this.cache.request(iccProfileName.toLowerCase());
      if (cs != null) {
         return cs;
      } else {
         Document doc = paintedElement.getOwnerDocument();
         NodeList list = doc.getElementsByTagNameNS("http://www.w3.org/2000/svg", "color-profile");
         int n = list.getLength();
         Element profile = null;

         for(int i = 0; i < n; ++i) {
            Node node = list.item(i);
            if (node.getNodeType() == 1) {
               Element profileNode = (Element)node;
               String nameAttr = profileNode.getAttributeNS((String)null, "name");
               if (iccProfileName.equalsIgnoreCase(nameAttr)) {
                  profile = profileNode;
               }
            }
         }

         if (profile == null) {
            return null;
         } else {
            String href = XLinkSupport.getXLinkHref(profile);
            ICC_Profile p = null;
            if (href != null) {
               String baseURI = profile.getBaseURI();
               ParsedURL pDocURL = null;
               if (baseURI != null) {
                  pDocURL = new ParsedURL(baseURI);
               }

               ParsedURL purl = new ParsedURL(pDocURL, href);
               if (!purl.complete()) {
                  BridgeException be = new BridgeException(ctx, paintedElement, "uri.malformed", new Object[]{href});
                  ctx.getUserAgent().displayError(be);
                  return null;
               }

               BridgeException be;
               try {
                  ctx.getUserAgent().checkLoadExternalResource(purl, pDocURL);
                  p = ICC_Profile.getInstance(purl.openStream());
               } catch (IOException var16) {
                  be = new BridgeException(ctx, paintedElement, var16, "uri.io", new Object[]{href});
                  ctx.getUserAgent().displayError(be);
                  return null;
               } catch (SecurityException var17) {
                  be = new BridgeException(ctx, paintedElement, var17, "uri.unsecure", new Object[]{href});
                  ctx.getUserAgent().displayError(be);
                  return null;
               }
            }

            if (p == null) {
               return null;
            } else {
               RenderingIntent intent = convertIntent(profile, ctx);
               cs = new ICCColorSpaceWithIntent(p, intent, href, iccProfileName);
               this.cache.put(iccProfileName.toLowerCase(), cs);
               return cs;
            }
         }
      }
   }

   private static RenderingIntent convertIntent(Element profile, BridgeContext ctx) {
      String intent = profile.getAttributeNS((String)null, "rendering-intent");
      if (intent.length() == 0) {
         return RenderingIntent.AUTO;
      } else if ("perceptual".equals(intent)) {
         return RenderingIntent.PERCEPTUAL;
      } else if ("auto".equals(intent)) {
         return RenderingIntent.AUTO;
      } else if ("relative-colorimetric".equals(intent)) {
         return RenderingIntent.RELATIVE_COLORIMETRIC;
      } else if ("absolute-colorimetric".equals(intent)) {
         return RenderingIntent.ABSOLUTE_COLORIMETRIC;
      } else if ("saturation".equals(intent)) {
         return RenderingIntent.SATURATION;
      } else {
         throw new BridgeException(ctx, profile, "attribute.malformed", new Object[]{"rendering-intent", intent});
      }
   }
}
