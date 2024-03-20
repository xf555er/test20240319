package org.apache.batik.bridge;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.lang.ref.SoftReference;
import org.apache.batik.anim.dom.AnimatedLiveAttributeValue;
import org.apache.batik.anim.dom.SVGOMAnimatedTransformList;
import org.apache.batik.anim.dom.SVGOMElement;
import org.apache.batik.css.engine.CSSEngineEvent;
import org.apache.batik.css.engine.SVGCSSEngine;
import org.apache.batik.dom.events.AbstractEvent;
import org.apache.batik.dom.svg.AbstractSVGTransformList;
import org.apache.batik.dom.svg.LiveAttributeException;
import org.apache.batik.dom.svg.SVGContext;
import org.apache.batik.dom.svg.SVGMotionAnimatableElement;
import org.apache.batik.ext.awt.geom.SegmentList;
import org.apache.batik.gvt.CanvasGraphicsNode;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.events.DocumentEvent;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.events.MutationEvent;
import org.w3c.dom.svg.SVGFitToViewBox;
import org.w3c.dom.svg.SVGTransformable;

public abstract class AbstractGraphicsNodeBridge extends AnimatableSVGBridge implements SVGContext, BridgeUpdateHandler, GraphicsNodeBridge, ErrorConstants {
   protected GraphicsNode node;
   protected boolean isSVG12;
   protected org.apache.batik.parser.UnitProcessor.Context unitContext;
   protected SoftReference bboxShape = null;
   protected Rectangle2D bbox = null;

   protected AbstractGraphicsNodeBridge() {
   }

   public GraphicsNode createGraphicsNode(BridgeContext ctx, Element e) {
      if (!SVGUtilities.matchUserAgent(e, ctx.getUserAgent())) {
         return null;
      } else {
         GraphicsNode node = this.instantiateGraphicsNode();
         this.setTransform(node, e, ctx);
         node.setVisible(CSSUtilities.convertVisibility(e));
         this.associateSVGContext(ctx, e, node);
         return node;
      }
   }

   protected abstract GraphicsNode instantiateGraphicsNode();

   public void buildGraphicsNode(BridgeContext ctx, Element e, GraphicsNode node) {
      node.setComposite(CSSUtilities.convertOpacity(e));
      node.setFilter(CSSUtilities.convertFilter(e, node, ctx));
      node.setMask(CSSUtilities.convertMask(e, node, ctx));
      node.setClip(CSSUtilities.convertClipPath(e, node, ctx));
      node.setPointerEventType(CSSUtilities.convertPointerEvents(e));
      this.initializeDynamicSupport(ctx, e, node);
   }

   public boolean getDisplay(Element e) {
      return CSSUtilities.convertDisplay(e);
   }

   protected AffineTransform computeTransform(SVGTransformable te, BridgeContext ctx) {
      try {
         AffineTransform at = new AffineTransform();
         SVGOMAnimatedTransformList atl = (SVGOMAnimatedTransformList)te.getTransform();
         if (atl.isSpecified()) {
            atl.check();
            AbstractSVGTransformList tl = (AbstractSVGTransformList)te.getTransform().getAnimVal();
            at.concatenate(tl.getAffineTransform());
         }

         if (this.e instanceof SVGMotionAnimatableElement) {
            SVGMotionAnimatableElement mae = (SVGMotionAnimatableElement)this.e;
            AffineTransform mat = mae.getMotionTransform();
            if (mat != null) {
               at.concatenate(mat);
            }
         }

         return at;
      } catch (LiveAttributeException var7) {
         throw new BridgeException(ctx, var7);
      }
   }

   protected void setTransform(GraphicsNode n, Element e, BridgeContext ctx) {
      n.setTransform(this.computeTransform((SVGTransformable)e, ctx));
   }

   protected void associateSVGContext(BridgeContext ctx, Element e, GraphicsNode node) {
      this.e = e;
      this.node = node;
      this.ctx = ctx;
      this.unitContext = UnitProcessor.createContext(ctx, e);
      this.isSVG12 = ctx.isSVG12();
      ((SVGOMElement)e).setSVGContext(this);
   }

   protected void initializeDynamicSupport(BridgeContext ctx, Element e, GraphicsNode node) {
      if (ctx.isInteractive()) {
         ctx.bind(e, node);
      }

   }

   public void handleDOMAttrModifiedEvent(MutationEvent evt) {
   }

   protected void handleGeometryChanged() {
      this.node.setFilter(CSSUtilities.convertFilter(this.e, this.node, this.ctx));
      this.node.setMask(CSSUtilities.convertMask(this.e, this.node, this.ctx));
      this.node.setClip(CSSUtilities.convertClipPath(this.e, this.node, this.ctx));
      if (this.isSVG12) {
         if (!"use".equals(this.e.getLocalName())) {
            this.fireShapeChangeEvent();
         }

         this.fireBBoxChangeEvent();
      }

   }

   protected void fireShapeChangeEvent() {
      DocumentEvent d = (DocumentEvent)this.e.getOwnerDocument();
      AbstractEvent evt = (AbstractEvent)d.createEvent("SVGEvents");
      evt.initEventNS("http://www.w3.org/2000/svg", "shapechange", true, false);

      try {
         ((EventTarget)this.e).dispatchEvent(evt);
      } catch (RuntimeException var4) {
         this.ctx.getUserAgent().displayError(var4);
      }

   }

   public void handleDOMNodeInsertedEvent(MutationEvent evt) {
      if (evt.getTarget() instanceof Element) {
         Element e2 = (Element)evt.getTarget();
         Bridge b = this.ctx.getBridge(e2);
         if (b instanceof GenericBridge) {
            ((GenericBridge)b).handleElement(this.ctx, e2);
         }
      }

   }

   public void handleDOMNodeRemovedEvent(MutationEvent evt) {
      Node parent = this.e.getParentNode();
      if (parent instanceof SVGOMElement) {
         SVGContext bridge = ((SVGOMElement)parent).getSVGContext();
         if (bridge instanceof SVGSwitchElementBridge) {
            ((SVGSwitchElementBridge)bridge).handleChildElementRemoved(this.e);
            return;
         }
      }

      CompositeGraphicsNode gn = this.node.getParent();
      gn.remove(this.node);
      disposeTree(this.e);
   }

   public void handleDOMCharacterDataModified(MutationEvent evt) {
   }

   public void dispose() {
      SVGOMElement elt = (SVGOMElement)this.e;
      elt.setSVGContext((SVGContext)null);
      this.ctx.unbind(this.e);
      this.bboxShape = null;
   }

   protected static void disposeTree(Node node) {
      disposeTree(node, true);
   }

   protected static void disposeTree(Node node, boolean removeContext) {
      if (node instanceof SVGOMElement) {
         SVGOMElement elt = (SVGOMElement)node;
         SVGContext ctx = elt.getSVGContext();
         if (ctx instanceof BridgeUpdateHandler) {
            BridgeUpdateHandler h = (BridgeUpdateHandler)ctx;
            if (removeContext) {
               elt.setSVGContext((SVGContext)null);
            }

            h.dispose();
         }
      }

      for(Node n = node.getFirstChild(); n != null; n = n.getNextSibling()) {
         disposeTree(n, removeContext);
      }

   }

   public void handleCSSEngineEvent(CSSEngineEvent evt) {
      try {
         SVGCSSEngine eng = (SVGCSSEngine)evt.getSource();
         int[] properties = evt.getProperties();
         int[] var4 = properties;
         int var5 = properties.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            int idx = var4[var6];
            this.handleCSSPropertyChanged(idx);
            String pn = eng.getPropertyName(idx);
            this.fireBaseAttributeListeners(pn);
         }
      } catch (Exception var9) {
         this.ctx.getUserAgent().displayError(var9);
      }

   }

   protected void handleCSSPropertyChanged(int property) {
      switch (property) {
         case 3:
            this.node.setClip(CSSUtilities.convertClipPath(this.e, this.node, this.ctx));
            break;
         case 12:
            if (!this.getDisplay(this.e)) {
               CompositeGraphicsNode parent = this.node.getParent();
               parent.remove(this.node);
               disposeTree(this.e, false);
            }
            break;
         case 18:
            this.node.setFilter(CSSUtilities.convertFilter(this.e, this.node, this.ctx));
            break;
         case 37:
            this.node.setMask(CSSUtilities.convertMask(this.e, this.node, this.ctx));
            break;
         case 38:
            this.node.setComposite(CSSUtilities.convertOpacity(this.e));
            break;
         case 40:
            this.node.setPointerEventType(CSSUtilities.convertPointerEvents(this.e));
            break;
         case 57:
            this.node.setVisible(CSSUtilities.convertVisibility(this.e));
      }

   }

   public void handleAnimatedAttributeChanged(AnimatedLiveAttributeValue alav) {
      if (alav.getNamespaceURI() == null && alav.getLocalName().equals("transform")) {
         this.setTransform(this.node, this.e, this.ctx);
         this.handleGeometryChanged();
      }

   }

   public void handleOtherAnimationChanged(String type) {
      if (type.equals("motion")) {
         this.setTransform(this.node, this.e, this.ctx);
         this.handleGeometryChanged();
      }

   }

   protected void checkBBoxChange() {
      if (this.e != null) {
         this.fireBBoxChangeEvent();
      }

   }

   protected void fireBBoxChangeEvent() {
      DocumentEvent d = (DocumentEvent)this.e.getOwnerDocument();
      AbstractEvent evt = (AbstractEvent)d.createEvent("SVGEvents");
      evt.initEventNS("http://www.w3.org/2000/svg", "RenderedBBoxChange", true, false);

      try {
         ((EventTarget)this.e).dispatchEvent(evt);
      } catch (RuntimeException var4) {
         this.ctx.getUserAgent().displayError(var4);
      }

   }

   public float getPixelUnitToMillimeter() {
      return this.ctx.getUserAgent().getPixelUnitToMillimeter();
   }

   public float getPixelToMM() {
      return this.getPixelUnitToMillimeter();
   }

   public Rectangle2D getBBox() {
      if (this.node == null) {
         return null;
      } else {
         Shape s = this.node.getOutline();
         if (this.bboxShape != null && s == this.bboxShape.get()) {
            return this.bbox;
         } else {
            this.bboxShape = new SoftReference(s);
            this.bbox = null;
            if (s == null) {
               return this.bbox;
            } else {
               SegmentList sl = new SegmentList(s);
               this.bbox = sl.getBounds2D();
               return this.bbox;
            }
         }
      }
   }

   public AffineTransform getCTM() {
      GraphicsNode gn = this.node;
      AffineTransform ctm = new AffineTransform();

      for(Element elt = this.e; elt != null; gn = ((GraphicsNode)gn).getParent()) {
         AffineTransform at;
         if (elt instanceof SVGFitToViewBox) {
            if (gn instanceof CanvasGraphicsNode) {
               at = ((CanvasGraphicsNode)gn).getViewingTransform();
            } else {
               at = ((GraphicsNode)gn).getTransform();
            }

            if (at != null) {
               ctm.preConcatenate(at);
            }
            break;
         }

         at = ((GraphicsNode)gn).getTransform();
         if (at != null) {
            ctm.preConcatenate(at);
         }

         elt = SVGCSSEngine.getParentCSSStylableElement((Element)elt);
      }

      return ctm;
   }

   public AffineTransform getScreenTransform() {
      return this.ctx.getUserAgent().getTransform();
   }

   public void setScreenTransform(AffineTransform at) {
      this.ctx.getUserAgent().setTransform(at);
   }

   public AffineTransform getGlobalTransform() {
      return this.node.getGlobalTransform();
   }

   public float getViewportWidth() {
      return this.ctx.getBlockWidth(this.e);
   }

   public float getViewportHeight() {
      return this.ctx.getBlockHeight(this.e);
   }

   public float getFontSize() {
      return CSSUtilities.getComputedStyle(this.e, 22).getFloatValue();
   }
}
