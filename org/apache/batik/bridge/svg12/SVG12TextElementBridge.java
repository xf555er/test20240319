package org.apache.batik.bridge.svg12;

import org.apache.batik.anim.dom.XBLEventSupport;
import org.apache.batik.bridge.Bridge;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.SVGTextElementBridge;
import org.apache.batik.dom.AbstractNode;
import org.apache.batik.dom.events.EventSupport;
import org.apache.batik.dom.events.NodeEventTarget;
import org.apache.batik.dom.xbl.NodeXBL;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.MutationEvent;

public class SVG12TextElementBridge extends SVGTextElementBridge implements SVG12BridgeUpdateHandler {
   public Bridge getInstance() {
      return new SVG12TextElementBridge();
   }

   protected void addTextEventListeners(BridgeContext ctx, NodeEventTarget e) {
      if (this.childNodeRemovedEventListener == null) {
         this.childNodeRemovedEventListener = new DOMChildNodeRemovedEventListener();
      }

      if (this.subtreeModifiedEventListener == null) {
         this.subtreeModifiedEventListener = new DOMSubtreeModifiedEventListener();
      }

      SVG12BridgeContext ctx12 = (SVG12BridgeContext)ctx;
      AbstractNode n = (AbstractNode)e;
      XBLEventSupport evtSupport = (XBLEventSupport)n.initializeEventSupport();
      evtSupport.addImplementationEventListenerNS("http://www.w3.org/2001/xml-events", "DOMNodeRemoved", this.childNodeRemovedEventListener, true);
      ctx12.storeImplementationEventListenerNS(e, "http://www.w3.org/2001/xml-events", "DOMNodeRemoved", this.childNodeRemovedEventListener, true);
      evtSupport.addImplementationEventListenerNS("http://www.w3.org/2001/xml-events", "DOMSubtreeModified", this.subtreeModifiedEventListener, false);
      ctx12.storeImplementationEventListenerNS(e, "http://www.w3.org/2001/xml-events", "DOMSubtreeModified", this.subtreeModifiedEventListener, false);
   }

   protected void removeTextEventListeners(BridgeContext ctx, NodeEventTarget e) {
      AbstractNode n = (AbstractNode)e;
      XBLEventSupport evtSupport = (XBLEventSupport)n.initializeEventSupport();
      evtSupport.removeImplementationEventListenerNS("http://www.w3.org/2001/xml-events", "DOMNodeRemoved", this.childNodeRemovedEventListener, true);
      evtSupport.removeImplementationEventListenerNS("http://www.w3.org/2001/xml-events", "DOMSubtreeModified", this.subtreeModifiedEventListener, false);
   }

   protected Node getFirstChild(Node n) {
      return ((NodeXBL)n).getXblFirstChild();
   }

   protected Node getNextSibling(Node n) {
      return ((NodeXBL)n).getXblNextSibling();
   }

   protected Node getParentNode(Node n) {
      return ((NodeXBL)n).getXblParentNode();
   }

   public void handleDOMCharacterDataModified(MutationEvent evt) {
      Node childNode = (Node)evt.getTarget();
      if (this.isParentDisplayed(childNode)) {
         if (this.getParentNode(childNode) != childNode.getParentNode()) {
            this.computeLaidoutText(this.ctx, this.e, this.node);
         } else {
            this.laidoutText = null;
         }
      }

   }

   public void handleBindingEvent(Element bindableElement, Element shadowTree) {
   }

   public void handleContentSelectionChangedEvent(ContentSelectionChangedEvent csce) {
      this.computeLaidoutText(this.ctx, this.e, this.node);
   }

   protected class DOMSubtreeModifiedEventListener extends SVGTextElementBridge.DOMSubtreeModifiedEventListener {
      protected DOMSubtreeModifiedEventListener() {
         super();
      }

      public void handleEvent(Event evt) {
         super.handleEvent(EventSupport.getUltimateOriginalEvent(evt));
      }
   }

   protected class DOMChildNodeRemovedEventListener extends SVGTextElementBridge.DOMChildNodeRemovedEventListener {
      protected DOMChildNodeRemovedEventListener() {
         super();
      }

      public void handleEvent(Event evt) {
         super.handleEvent(EventSupport.getUltimateOriginalEvent(evt));
      }
   }
}
