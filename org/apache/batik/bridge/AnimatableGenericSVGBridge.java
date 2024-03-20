package org.apache.batik.bridge;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import org.apache.batik.anim.dom.AnimatedLiveAttributeValue;
import org.apache.batik.anim.dom.SVGOMElement;
import org.apache.batik.css.engine.CSSEngineEvent;
import org.apache.batik.dom.svg.SVGContext;
import org.w3c.dom.Element;
import org.w3c.dom.events.MutationEvent;

public abstract class AnimatableGenericSVGBridge extends AnimatableSVGBridge implements GenericBridge, BridgeUpdateHandler, SVGContext {
   public void handleElement(BridgeContext ctx, Element e) {
      if (ctx.isDynamic()) {
         this.e = e;
         this.ctx = ctx;
         ((SVGOMElement)e).setSVGContext(this);
      }

   }

   public float getPixelUnitToMillimeter() {
      return this.ctx.getUserAgent().getPixelUnitToMillimeter();
   }

   public float getPixelToMM() {
      return this.getPixelUnitToMillimeter();
   }

   public Rectangle2D getBBox() {
      return null;
   }

   public AffineTransform getScreenTransform() {
      return this.ctx.getUserAgent().getTransform();
   }

   public void setScreenTransform(AffineTransform at) {
      this.ctx.getUserAgent().setTransform(at);
   }

   public AffineTransform getCTM() {
      return null;
   }

   public AffineTransform getGlobalTransform() {
      return null;
   }

   public float getViewportWidth() {
      return 0.0F;
   }

   public float getViewportHeight() {
      return 0.0F;
   }

   public float getFontSize() {
      return 0.0F;
   }

   public void dispose() {
      ((SVGOMElement)this.e).setSVGContext((SVGContext)null);
   }

   public void handleDOMNodeInsertedEvent(MutationEvent evt) {
   }

   public void handleDOMCharacterDataModified(MutationEvent evt) {
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
}
