package org.apache.batik.bridge.svg12;

import org.apache.batik.anim.dom.XBLOMContentElement;
import org.apache.batik.bridge.AbstractGraphicsNodeBridge;
import org.apache.batik.bridge.Bridge;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XBLContentElementBridge extends AbstractGraphicsNodeBridge {
   protected ContentChangedListener contentChangedListener;
   protected ContentManager contentManager;

   public String getLocalName() {
      return "content";
   }

   public String getNamespaceURI() {
      return "http://www.w3.org/2004/xbl";
   }

   public Bridge getInstance() {
      return new XBLContentElementBridge();
   }

   public GraphicsNode createGraphicsNode(BridgeContext ctx, Element e) {
      CompositeGraphicsNode gn = this.buildCompositeGraphicsNode(ctx, e, (CompositeGraphicsNode)null);
      return gn;
   }

   public CompositeGraphicsNode buildCompositeGraphicsNode(BridgeContext ctx, Element e, CompositeGraphicsNode cgn) {
      XBLOMContentElement content = (XBLOMContentElement)e;
      AbstractDocument doc = (AbstractDocument)e.getOwnerDocument();
      DefaultXBLManager xm = (DefaultXBLManager)doc.getXBLManager();
      this.contentManager = xm.getContentManager(e);
      if (cgn == null) {
         cgn = new CompositeGraphicsNode();
         this.associateSVGContext(ctx, e, cgn);
      } else {
         int s = cgn.size();

         for(int i = 0; i < s; ++i) {
            cgn.remove(0);
         }
      }

      GVTBuilder builder = ctx.getGVTBuilder();
      NodeList nl = this.contentManager.getSelectedContent(content);
      if (nl != null) {
         for(int i = 0; i < nl.getLength(); ++i) {
            Node n = nl.item(i);
            if (n.getNodeType() == 1) {
               GraphicsNode gn = builder.build(ctx, (Element)n);
               if (gn != null) {
                  cgn.add(gn);
               }
            }
         }
      }

      if (ctx.isDynamic() && this.contentChangedListener == null) {
         this.contentChangedListener = new ContentChangedListener();
         this.contentManager.addContentSelectionChangedListener(content, this.contentChangedListener);
      }

      return cgn;
   }

   protected GraphicsNode instantiateGraphicsNode() {
      return null;
   }

   public void buildGraphicsNode(BridgeContext ctx, Element e, GraphicsNode node) {
      this.initializeDynamicSupport(ctx, e, node);
   }

   public boolean getDisplay(Element e) {
      return true;
   }

   public boolean isComposite() {
      return false;
   }

   public void dispose() {
      super.dispose();
      if (this.contentChangedListener != null) {
         this.contentManager.removeContentSelectionChangedListener((XBLOMContentElement)this.e, this.contentChangedListener);
      }

   }

   protected class ContentChangedListener implements ContentSelectionChangedListener {
      public void contentSelectionChanged(ContentSelectionChangedEvent csce) {
         XBLContentElementBridge.this.buildCompositeGraphicsNode(XBLContentElementBridge.this.ctx, XBLContentElementBridge.this.e, (CompositeGraphicsNode)XBLContentElementBridge.this.node);
      }
   }
}
