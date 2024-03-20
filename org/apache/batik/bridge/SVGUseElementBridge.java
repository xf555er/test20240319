package org.apache.batik.bridge;

import java.awt.Cursor;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import org.apache.batik.anim.dom.AbstractSVGAnimatedLength;
import org.apache.batik.anim.dom.AnimatedLiveAttributeValue;
import org.apache.batik.anim.dom.SVGOMAnimatedLength;
import org.apache.batik.anim.dom.SVGOMDocument;
import org.apache.batik.anim.dom.SVGOMUseElement;
import org.apache.batik.dom.events.NodeEventTarget;
import org.apache.batik.dom.svg.LiveAttributeException;
import org.apache.batik.dom.svg.SVGOMUseShadowRoot;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.svg.SVGTransformable;
import org.w3c.dom.svg.SVGUseElement;

public class SVGUseElementBridge extends AbstractGraphicsNodeBridge {
   protected ReferencedElementMutationListener l;
   protected BridgeContext subCtx;

   public String getLocalName() {
      return "use";
   }

   public Bridge getInstance() {
      return new SVGUseElementBridge();
   }

   public GraphicsNode createGraphicsNode(BridgeContext ctx, Element e) {
      if (!SVGUtilities.matchUserAgent(e, ctx.getUserAgent())) {
         return null;
      } else {
         CompositeGraphicsNode gn = this.buildCompositeGraphicsNode(ctx, e, (CompositeGraphicsNode)null);
         this.associateSVGContext(ctx, e, gn);
         return gn;
      }
   }

   public CompositeGraphicsNode buildCompositeGraphicsNode(BridgeContext ctx, Element e, CompositeGraphicsNode gn) {
      SVGOMUseElement ue = (SVGOMUseElement)e;
      String uri = ue.getHref().getAnimVal();
      if (uri.length() == 0) {
         throw new BridgeException(ctx, e, "attribute.missing", new Object[]{"xlink:href"});
      } else {
         Element refElement = ctx.getReferencedElement(e, uri);
         SVGOMDocument document = (SVGOMDocument)e.getOwnerDocument();
         SVGOMDocument refDocument = (SVGOMDocument)refElement.getOwnerDocument();
         boolean isLocal = refDocument == document;
         BridgeContext theCtx = ctx;
         this.subCtx = null;
         if (!isLocal) {
            this.subCtx = (BridgeContext)refDocument.getCSSEngine().getCSSContext();
            theCtx = this.subCtx;
         }

         Element localRefElement = (Element)document.importNode(refElement, true, true);
         int i;
         if ("symbol".equals(localRefElement.getLocalName())) {
            Element svgElement = document.createElementNS("http://www.w3.org/2000/svg", "svg");
            NamedNodeMap attrs = localRefElement.getAttributes();
            i = attrs.getLength();

            for(int i = 0; i < i; ++i) {
               Attr attr = (Attr)attrs.item(i);
               svgElement.setAttributeNS(attr.getNamespaceURI(), attr.getName(), attr.getValue());
            }

            for(Node n = localRefElement.getFirstChild(); n != null; n = localRefElement.getFirstChild()) {
               svgElement.appendChild(n);
            }

            localRefElement = svgElement;
         }

         if ("svg".equals(localRefElement.getLocalName())) {
            try {
               SVGOMAnimatedLength al = (SVGOMAnimatedLength)ue.getWidth();
               if (al.isSpecified()) {
                  localRefElement.setAttributeNS((String)null, "width", al.getAnimVal().getValueAsString());
               }

               al = (SVGOMAnimatedLength)ue.getHeight();
               if (al.isSpecified()) {
                  localRefElement.setAttributeNS((String)null, "height", al.getAnimVal().getValueAsString());
               }
            } catch (LiveAttributeException var20) {
               throw new BridgeException(ctx, var20);
            }
         }

         SVGOMUseShadowRoot root = new SVGOMUseShadowRoot(document, e, isLocal);
         root.appendChild(localRefElement);
         if (gn == null) {
            gn = new CompositeGraphicsNode();
            this.associateSVGContext(ctx, e, this.node);
         } else {
            int s = gn.size();

            for(i = 0; i < s; ++i) {
               gn.remove(0);
            }
         }

         Node oldRoot = ue.getCSSFirstChild();
         if (oldRoot != null) {
            disposeTree(oldRoot);
         }

         ue.setUseShadowTree(root);
         CSSUtilities.computeStyleAndURIs(refElement, localRefElement, uri);
         GVTBuilder builder = ctx.getGVTBuilder();
         GraphicsNode refNode = builder.build(ctx, localRefElement);
         gn.getChildren().add(refNode);
         gn.setTransform(this.computeTransform((SVGTransformable)e, ctx));
         gn.setVisible(CSSUtilities.convertVisibility(e));
         RenderingHints hints = null;
         hints = CSSUtilities.convertColorRendering(e, hints);
         if (hints != null) {
            gn.setRenderingHints(hints);
         }

         Rectangle2D r = CSSUtilities.convertEnableBackground(e);
         if (r != null) {
            gn.setBackgroundEnable(r);
         }

         NodeEventTarget target;
         if (this.l != null) {
            target = this.l.target;
            target.removeEventListenerNS("http://www.w3.org/2001/xml-events", "DOMAttrModified", this.l, true);
            target.removeEventListenerNS("http://www.w3.org/2001/xml-events", "DOMNodeInserted", this.l, true);
            target.removeEventListenerNS("http://www.w3.org/2001/xml-events", "DOMNodeRemoved", this.l, true);
            target.removeEventListenerNS("http://www.w3.org/2001/xml-events", "DOMCharacterDataModified", this.l, true);
            this.l = null;
         }

         if (isLocal && ctx.isDynamic()) {
            this.l = new ReferencedElementMutationListener();
            target = (NodeEventTarget)refElement;
            this.l.target = target;
            target.addEventListenerNS("http://www.w3.org/2001/xml-events", "DOMAttrModified", this.l, true, (Object)null);
            theCtx.storeEventListenerNS(target, "http://www.w3.org/2001/xml-events", "DOMAttrModified", this.l, true);
            target.addEventListenerNS("http://www.w3.org/2001/xml-events", "DOMNodeInserted", this.l, true, (Object)null);
            theCtx.storeEventListenerNS(target, "http://www.w3.org/2001/xml-events", "DOMNodeInserted", this.l, true);
            target.addEventListenerNS("http://www.w3.org/2001/xml-events", "DOMNodeRemoved", this.l, true, (Object)null);
            theCtx.storeEventListenerNS(target, "http://www.w3.org/2001/xml-events", "DOMNodeRemoved", this.l, true);
            target.addEventListenerNS("http://www.w3.org/2001/xml-events", "DOMCharacterDataModified", this.l, true, (Object)null);
            theCtx.storeEventListenerNS(target, "http://www.w3.org/2001/xml-events", "DOMCharacterDataModified", this.l, true);
         }

         return gn;
      }
   }

   public void dispose() {
      if (this.l != null) {
         NodeEventTarget target = this.l.target;
         target.removeEventListenerNS("http://www.w3.org/2001/xml-events", "DOMAttrModified", this.l, true);
         target.removeEventListenerNS("http://www.w3.org/2001/xml-events", "DOMNodeInserted", this.l, true);
         target.removeEventListenerNS("http://www.w3.org/2001/xml-events", "DOMNodeRemoved", this.l, true);
         target.removeEventListenerNS("http://www.w3.org/2001/xml-events", "DOMCharacterDataModified", this.l, true);
         this.l = null;
      }

      SVGOMUseElement ue = (SVGOMUseElement)this.e;
      if (ue != null && ue.getCSSFirstChild() != null) {
         disposeTree(ue.getCSSFirstChild());
      }

      super.dispose();
      this.subCtx = null;
   }

   protected AffineTransform computeTransform(SVGTransformable e, BridgeContext ctx) {
      AffineTransform at = super.computeTransform(e, ctx);
      SVGUseElement ue = (SVGUseElement)e;

      try {
         AbstractSVGAnimatedLength _x = (AbstractSVGAnimatedLength)ue.getX();
         float x = _x.getCheckedValue();
         AbstractSVGAnimatedLength _y = (AbstractSVGAnimatedLength)ue.getY();
         float y = _y.getCheckedValue();
         AffineTransform xy = AffineTransform.getTranslateInstance((double)x, (double)y);
         xy.preConcatenate(at);
         return xy;
      } catch (LiveAttributeException var10) {
         throw new BridgeException(ctx, var10);
      }
   }

   protected GraphicsNode instantiateGraphicsNode() {
      return null;
   }

   public boolean isComposite() {
      return false;
   }

   public void buildGraphicsNode(BridgeContext ctx, Element e, GraphicsNode node) {
      super.buildGraphicsNode(ctx, e, node);
      if (ctx.isInteractive()) {
         NodeEventTarget target = (NodeEventTarget)e;
         EventListener l = new CursorMouseOverListener(ctx);
         target.addEventListenerNS("http://www.w3.org/2001/xml-events", "mouseover", l, false, (Object)null);
         ctx.storeEventListenerNS(target, "http://www.w3.org/2001/xml-events", "mouseover", l, false);
      }

   }

   public void handleAnimatedAttributeChanged(AnimatedLiveAttributeValue alav) {
      try {
         String ns = alav.getNamespaceURI();
         String ln = alav.getLocalName();
         if (ns == null) {
            if (!ln.equals("x") && !ln.equals("y") && !ln.equals("transform")) {
               if (ln.equals("width") || ln.equals("height")) {
                  this.buildCompositeGraphicsNode(this.ctx, this.e, (CompositeGraphicsNode)this.node);
               }
            } else {
               this.node.setTransform(this.computeTransform((SVGTransformable)this.e, this.ctx));
               this.handleGeometryChanged();
            }
         } else if (ns.equals("http://www.w3.org/1999/xlink") && ln.equals("href")) {
            this.buildCompositeGraphicsNode(this.ctx, this.e, (CompositeGraphicsNode)this.node);
         }
      } catch (LiveAttributeException var4) {
         throw new BridgeException(this.ctx, var4);
      }

      super.handleAnimatedAttributeChanged(alav);
   }

   protected class ReferencedElementMutationListener implements EventListener {
      protected NodeEventTarget target;

      public void handleEvent(Event evt) {
         SVGUseElementBridge.this.buildCompositeGraphicsNode(SVGUseElementBridge.this.ctx, SVGUseElementBridge.this.e, (CompositeGraphicsNode)SVGUseElementBridge.this.node);
      }
   }

   public static class CursorMouseOverListener implements EventListener {
      protected BridgeContext ctx;

      public CursorMouseOverListener(BridgeContext ctx) {
         this.ctx = ctx;
      }

      public void handleEvent(Event evt) {
         Element currentTarget = (Element)evt.getCurrentTarget();
         if (!CSSUtilities.isAutoCursor(currentTarget)) {
            Cursor cursor = CSSUtilities.convertCursor(currentTarget, this.ctx);
            if (cursor != null) {
               this.ctx.getUserAgent().setSVGCursor(cursor);
            }
         }

      }
   }
}
