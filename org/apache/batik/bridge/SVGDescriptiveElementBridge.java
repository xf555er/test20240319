package org.apache.batik.bridge;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import org.apache.batik.anim.dom.AnimatedLiveAttributeValue;
import org.apache.batik.anim.dom.SVGOMElement;
import org.apache.batik.css.engine.CSSEngineEvent;
import org.apache.batik.dom.svg.SVGContext;
import org.w3c.dom.Element;
import org.w3c.dom.events.MutationEvent;

public abstract class SVGDescriptiveElementBridge extends AbstractSVGBridge implements GenericBridge, BridgeUpdateHandler, SVGContext {
   Element theElt;
   Element parent;
   BridgeContext theCtx;

   public void handleElement(BridgeContext ctx, Element e) {
      UserAgent ua = ctx.getUserAgent();
      ua.handleElement(e, Boolean.TRUE);
      if (ctx.isDynamic()) {
         SVGDescriptiveElementBridge b = (SVGDescriptiveElementBridge)this.getInstance();
         b.theElt = e;
         b.parent = (Element)e.getParentNode();
         b.theCtx = ctx;
         ((SVGOMElement)e).setSVGContext(b);
      }

   }

   public void dispose() {
      UserAgent ua = this.theCtx.getUserAgent();
      ((SVGOMElement)this.theElt).setSVGContext((SVGContext)null);
      ua.handleElement(this.theElt, this.parent);
      this.theElt = null;
      this.parent = null;
   }

   public void handleDOMNodeInsertedEvent(MutationEvent evt) {
      UserAgent ua = this.theCtx.getUserAgent();
      ua.handleElement(this.theElt, Boolean.TRUE);
   }

   public void handleDOMCharacterDataModified(MutationEvent evt) {
      UserAgent ua = this.theCtx.getUserAgent();
      ua.handleElement(this.theElt, Boolean.TRUE);
   }

   public void handleDOMNodeRemovedEvent(MutationEvent evt) {
      this.dispose();
   }

   public void handleDOMAttrModifiedEvent(MutationEvent evt) {
   }

   public void handleCSSEngineEvent(CSSEngineEvent evt) {
   }

   public void handleAnimatedAttributeChanged(AnimatedLiveAttributeValue alav) {
   }

   public void handleOtherAnimationChanged(String type) {
   }

   public float getPixelUnitToMillimeter() {
      return this.theCtx.getUserAgent().getPixelUnitToMillimeter();
   }

   public float getPixelToMM() {
      return this.getPixelUnitToMillimeter();
   }

   public Rectangle2D getBBox() {
      return null;
   }

   public AffineTransform getScreenTransform() {
      return this.theCtx.getUserAgent().getTransform();
   }

   public void setScreenTransform(AffineTransform at) {
      this.theCtx.getUserAgent().setTransform(at);
   }

   public AffineTransform getCTM() {
      return null;
   }

   public AffineTransform getGlobalTransform() {
      return null;
   }

   public float getViewportWidth() {
      return this.theCtx.getBlockWidth(this.theElt);
   }

   public float getViewportHeight() {
      return this.theCtx.getBlockHeight(this.theElt);
   }

   public float getFontSize() {
      return 0.0F;
   }
}
