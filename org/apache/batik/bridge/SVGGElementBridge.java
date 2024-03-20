package org.apache.batik.bridge;

import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.events.MutationEvent;

public class SVGGElementBridge extends AbstractGraphicsNodeBridge {
   public String getLocalName() {
      return "g";
   }

   public Bridge getInstance() {
      return new SVGGElementBridge();
   }

   public GraphicsNode createGraphicsNode(BridgeContext ctx, Element e) {
      CompositeGraphicsNode gn = (CompositeGraphicsNode)super.createGraphicsNode(ctx, e);
      if (gn == null) {
         return null;
      } else {
         this.associateSVGContext(ctx, e, gn);
         RenderingHints hints = null;
         hints = CSSUtilities.convertColorRendering(e, hints);
         if (hints != null) {
            gn.setRenderingHints(hints);
         }

         Rectangle2D r = CSSUtilities.convertEnableBackground(e);
         if (r != null) {
            gn.setBackgroundEnable(r);
         }

         return gn;
      }
   }

   protected GraphicsNode instantiateGraphicsNode() {
      return new CompositeGraphicsNode();
   }

   public boolean isComposite() {
      return true;
   }

   public void handleDOMNodeInsertedEvent(MutationEvent evt) {
      if (evt.getTarget() instanceof Element) {
         this.handleElementAdded((CompositeGraphicsNode)this.node, this.e, (Element)evt.getTarget());
      } else {
         super.handleDOMNodeInsertedEvent(evt);
      }

   }

   protected void handleElementAdded(CompositeGraphicsNode gn, Node parent, Element childElt) {
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
