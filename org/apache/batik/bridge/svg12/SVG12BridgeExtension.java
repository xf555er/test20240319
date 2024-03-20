package org.apache.batik.bridge.svg12;

import java.util.Collections;
import java.util.Iterator;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.SVGBridgeExtension;
import org.w3c.dom.Element;

public class SVG12BridgeExtension extends SVGBridgeExtension {
   public float getPriority() {
      return 0.0F;
   }

   public Iterator getImplementedExtensions() {
      return Collections.EMPTY_LIST.iterator();
   }

   public String getAuthor() {
      return "The Apache Batik Team.";
   }

   public String getContactAddress() {
      return "batik-dev@xmlgraphics.apache.org";
   }

   public String getURL() {
      return "http://xml.apache.org/batik";
   }

   public String getDescription() {
      return "The required SVG 1.2 tags";
   }

   public void registerTags(BridgeContext ctx) {
      super.registerTags(ctx);
      ctx.putBridge(new SVGFlowRootElementBridge());
      ctx.putBridge(new SVGMultiImageElementBridge());
      ctx.putBridge(new SVGSolidColorElementBridge());
      ctx.putBridge(new SVG12TextElementBridge());
      ctx.putBridge(new XBLShadowTreeElementBridge());
      ctx.putBridge(new XBLContentElementBridge());
      ctx.setDefaultBridge(new BindableElementBridge());
      ctx.putReservedNamespaceURI((String)null);
      ctx.putReservedNamespaceURI("http://www.w3.org/2000/svg");
      ctx.putReservedNamespaceURI("http://www.w3.org/2004/xbl");
   }

   public boolean isDynamicElement(Element e) {
      String ns = e.getNamespaceURI();
      if ("http://www.w3.org/2004/xbl".equals(ns)) {
         return true;
      } else if (!"http://www.w3.org/2000/svg".equals(ns)) {
         return false;
      } else {
         String ln = e.getLocalName();
         return ln.equals("script") || ln.equals("handler") || ln.startsWith("animate") || ln.equals("set");
      }
   }
}
