package org.apache.batik.bridge.svg12;

import org.apache.batik.bridge.AbstractGraphicsNodeBridge;
import org.apache.batik.bridge.Bridge;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.SVGUtilities;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.events.MutationEvent;

public class XBLShadowTreeElementBridge extends AbstractGraphicsNodeBridge {
   public String getLocalName() {
      return "shadowTree";
   }

   public String getNamespaceURI() {
      return "http://www.w3.org/2004/xbl";
   }

   public Bridge getInstance() {
      return new XBLShadowTreeElementBridge();
   }

   public GraphicsNode createGraphicsNode(BridgeContext ctx, Element e) {
      if (!SVGUtilities.matchUserAgent(e, ctx.getUserAgent())) {
         return null;
      } else {
         CompositeGraphicsNode cgn = new CompositeGraphicsNode();
         this.associateSVGContext(ctx, e, cgn);
         return cgn;
      }
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
      return true;
   }

   public void handleDOMNodeInsertedEvent(MutationEvent evt) {
      if (evt.getTarget() instanceof Element) {
         this.handleElementAdded((CompositeGraphicsNode)this.node, this.e, (Element)evt.getTarget());
      }

   }

   public void handleElementAdded(CompositeGraphicsNode gn, Node parent, Element childElt) {
      GVTBuilder builder = this.ctx.getGVTBuilder();
      GraphicsNode childNode = builder.build(this.ctx, childElt);
      if (childNode != null) {
         int idx = -1;

         for(Node ps = childElt.getPreviousSibling(); ps != null; ps = ps.getPreviousSibling()) {
            if (ps.getNodeType() == 1) {
               Element pse = (Element)ps;

               Object psgn;
               for(psgn = this.ctx.getGraphicsNode(pse); psgn != null && ((GraphicsNode)psgn).getParent() != gn; psgn = ((GraphicsNode)psgn).getParent()) {
               }

               if (psgn != null) {
                  idx = gn.indexOf(psgn);
                  if (idx != -1) {
                     break;
                  }
               }
            }
         }

         ++idx;
         gn.add(idx, childNode);
      }
   }
}
