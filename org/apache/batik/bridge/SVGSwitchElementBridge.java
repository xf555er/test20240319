package org.apache.batik.bridge;

import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGTests;

public class SVGSwitchElementBridge extends SVGGElementBridge {
   protected Element selectedChild;

   public String getLocalName() {
      return "switch";
   }

   public Bridge getInstance() {
      return new SVGSwitchElementBridge();
   }

   public GraphicsNode createGraphicsNode(BridgeContext ctx, Element e) {
      GraphicsNode refNode = null;
      GVTBuilder builder = ctx.getGVTBuilder();
      this.selectedChild = null;

      for(Node n = e.getFirstChild(); n != null; n = n.getNextSibling()) {
         if (n.getNodeType() == 1) {
            Element ref = (Element)n;
            if (n instanceof SVGTests && SVGUtilities.matchUserAgent(ref, ctx.getUserAgent())) {
               this.selectedChild = ref;
               refNode = builder.build(ctx, ref);
               break;
            }
         }
      }

      if (refNode == null) {
         return null;
      } else {
         CompositeGraphicsNode group = (CompositeGraphicsNode)super.createGraphicsNode(ctx, e);
         if (group == null) {
            return null;
         } else {
            group.add(refNode);
            return group;
         }
      }
   }

   public boolean isComposite() {
      return false;
   }

   public void dispose() {
      this.selectedChild = null;
      super.dispose();
   }

   protected void handleElementAdded(CompositeGraphicsNode gn, Node parent, Element childElt) {
      for(Node n = childElt.getPreviousSibling(); n != null; n = n.getPreviousSibling()) {
         if (n == childElt) {
            return;
         }
      }

      if (childElt instanceof SVGTests && SVGUtilities.matchUserAgent(childElt, this.ctx.getUserAgent())) {
         if (this.selectedChild != null) {
            gn.remove(0);
            disposeTree(this.selectedChild);
         }

         this.selectedChild = childElt;
         GVTBuilder builder = this.ctx.getGVTBuilder();
         GraphicsNode refNode = builder.build(this.ctx, childElt);
         if (refNode != null) {
            gn.add(refNode);
         }
      }

   }

   protected void handleChildElementRemoved(Element e) {
      CompositeGraphicsNode gn = (CompositeGraphicsNode)this.node;
      if (this.selectedChild == e) {
         gn.remove(0);
         disposeTree(this.selectedChild);
         this.selectedChild = null;
         GraphicsNode refNode = null;
         GVTBuilder builder = this.ctx.getGVTBuilder();

         for(Node n = e.getNextSibling(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == 1) {
               Element ref = (Element)n;
               if (n instanceof SVGTests && SVGUtilities.matchUserAgent(ref, this.ctx.getUserAgent())) {
                  refNode = builder.build(this.ctx, ref);
                  this.selectedChild = ref;
                  break;
               }
            }
         }

         if (refNode != null) {
            gn.add(refNode);
         }
      }

   }
}
