package org.apache.batik.bridge;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Calendar;
import org.apache.batik.anim.AbstractAnimation;
import org.apache.batik.anim.dom.AnimatableElement;
import org.apache.batik.anim.dom.AnimatedLiveAttributeValue;
import org.apache.batik.anim.dom.AnimationTarget;
import org.apache.batik.anim.dom.AnimationTargetListener;
import org.apache.batik.anim.dom.SVGOMElement;
import org.apache.batik.anim.timing.TimedElement;
import org.apache.batik.anim.values.AnimatableValue;
import org.apache.batik.css.engine.CSSEngineEvent;
import org.apache.batik.dom.svg.SVGAnimationContext;
import org.apache.batik.dom.svg.SVGContext;
import org.apache.batik.dom.util.XLinkSupport;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.events.MutationEvent;
import org.w3c.dom.svg.SVGElement;

public abstract class SVGAnimationElementBridge extends AbstractSVGBridge implements GenericBridge, BridgeUpdateHandler, SVGAnimationContext, AnimatableElement {
   protected SVGOMElement element;
   protected BridgeContext ctx;
   protected SVGAnimationEngine eng;
   protected TimedElement timedElement;
   protected AbstractAnimation animation;
   protected String attributeNamespaceURI;
   protected String attributeLocalName;
   protected short animationType;
   protected SVGOMElement targetElement;
   protected AnimationTarget animationTarget;

   public TimedElement getTimedElement() {
      return this.timedElement;
   }

   public AnimatableValue getUnderlyingValue() {
      return this.animationType == 0 ? this.animationTarget.getUnderlyingValue(this.attributeNamespaceURI, this.attributeLocalName) : this.eng.getUnderlyingCSSValue(this.element, this.animationTarget, this.attributeLocalName);
   }

   public void handleElement(BridgeContext ctx, Element e) {
      if (ctx.isDynamic() && BridgeContext.getSVGContext(e) == null) {
         SVGAnimationElementBridge b = (SVGAnimationElementBridge)this.getInstance();
         b.element = (SVGOMElement)e;
         b.ctx = ctx;
         b.eng = ctx.getAnimationEngine();
         b.element.setSVGContext(b);
         if (b.eng.hasStarted()) {
            b.initializeAnimation();
            b.initializeTimedElement();
         } else {
            b.eng.addInitialBridge(b);
         }
      }

   }

   protected void initializeAnimation() {
      String uri = XLinkSupport.getXLinkHref(this.element);
      Object t;
      if (uri.length() == 0) {
         t = this.element.getParentNode();
      } else {
         t = this.ctx.getReferencedElement(this.element, uri);
         if (((Node)t).getOwnerDocument() != this.element.getOwnerDocument()) {
            throw new BridgeException(this.ctx, this.element, "uri.badTarget", new Object[]{uri});
         }
      }

      this.animationTarget = null;
      if (t instanceof SVGOMElement) {
         this.targetElement = (SVGOMElement)t;
         this.animationTarget = this.targetElement;
      }

      if (this.animationTarget == null) {
         throw new BridgeException(this.ctx, this.element, "uri.badTarget", new Object[]{uri});
      } else {
         String an = this.element.getAttributeNS((String)null, "attributeName");
         int ci = an.indexOf(58);
         if (ci == -1) {
            if (this.element.hasProperty(an)) {
               this.animationType = 1;
               this.attributeLocalName = an;
            } else {
               this.animationType = 0;
               this.attributeLocalName = an;
            }
         } else {
            this.animationType = 0;
            String prefix = an.substring(0, ci);
            this.attributeNamespaceURI = this.element.lookupNamespaceURI(prefix);
            this.attributeLocalName = an.substring(ci + 1);
         }

         if ((this.animationType != 1 || this.targetElement.isPropertyAnimatable(this.attributeLocalName)) && (this.animationType != 0 || this.targetElement.isAttributeAnimatable(this.attributeNamespaceURI, this.attributeLocalName))) {
            int type;
            if (this.animationType == 1) {
               type = this.targetElement.getPropertyType(this.attributeLocalName);
            } else {
               type = this.targetElement.getAttributeType(this.attributeNamespaceURI, this.attributeLocalName);
            }

            if (!this.canAnimateType(type)) {
               throw new BridgeException(this.ctx, this.element, "type.not.animatable", new Object[]{this.targetElement.getNodeName(), an, this.element.getNodeName()});
            } else {
               this.timedElement = this.createTimedElement();
               this.animation = this.createAnimation(this.animationTarget);
               this.eng.addAnimation(this.animationTarget, this.animationType, this.attributeNamespaceURI, this.attributeLocalName, this.animation);
            }
         } else {
            throw new BridgeException(this.ctx, this.element, "attribute.not.animatable", new Object[]{this.targetElement.getNodeName(), an});
         }
      }
   }

   protected abstract boolean canAnimateType(int var1);

   protected boolean checkValueType(AnimatableValue v) {
      return true;
   }

   protected void initializeTimedElement() {
      this.initializeTimedElement(this.timedElement);
      this.timedElement.initialize();
   }

   protected TimedElement createTimedElement() {
      return new SVGTimedElement();
   }

   protected abstract AbstractAnimation createAnimation(AnimationTarget var1);

   protected AnimatableValue parseAnimatableValue(String an) {
      if (!this.element.hasAttributeNS((String)null, an)) {
         return null;
      } else {
         String s = this.element.getAttributeNS((String)null, an);
         AnimatableValue val = this.eng.parseAnimatableValue(this.element, this.animationTarget, this.attributeNamespaceURI, this.attributeLocalName, this.animationType == 1, s);
         if (!this.checkValueType(val)) {
            throw new BridgeException(this.ctx, this.element, "attribute.malformed", new Object[]{an, s});
         } else {
            return val;
         }
      }
   }

   protected void initializeTimedElement(TimedElement timedElement) {
      timedElement.parseAttributes(this.element.getAttributeNS((String)null, "begin"), this.element.getAttributeNS((String)null, "dur"), this.element.getAttributeNS((String)null, "end"), this.element.getAttributeNS((String)null, "min"), this.element.getAttributeNS((String)null, "max"), this.element.getAttributeNS((String)null, "repeatCount"), this.element.getAttributeNS((String)null, "repeatDur"), this.element.getAttributeNS((String)null, "fill"), this.element.getAttributeNS((String)null, "restart"));
   }

   public void handleDOMAttrModifiedEvent(MutationEvent evt) {
   }

   public void handleDOMNodeInsertedEvent(MutationEvent evt) {
   }

   public void handleDOMNodeRemovedEvent(MutationEvent evt) {
      this.element.setSVGContext((SVGContext)null);
      this.dispose();
   }

   public void handleDOMCharacterDataModified(MutationEvent evt) {
   }

   public void handleCSSEngineEvent(CSSEngineEvent evt) {
   }

   public void handleAnimatedAttributeChanged(AnimatedLiveAttributeValue alav) {
   }

   public void handleOtherAnimationChanged(String type) {
   }

   public void dispose() {
      if (this.element.getSVGContext() == null) {
         this.eng.removeAnimation(this.animation);
         this.timedElement.deinitialize();
         this.timedElement = null;
         this.element = null;
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
      return this.ctx.getBlockWidth(this.element);
   }

   public float getViewportHeight() {
      return this.ctx.getBlockHeight(this.element);
   }

   public float getFontSize() {
      return 0.0F;
   }

   public float svgToUserSpace(float v, int type, int pcInterp) {
      return 0.0F;
   }

   public void addTargetListener(String pn, AnimationTargetListener l) {
   }

   public void removeTargetListener(String pn, AnimationTargetListener l) {
   }

   public SVGElement getTargetElement() {
      return this.targetElement;
   }

   public float getStartTime() {
      return this.timedElement.getCurrentBeginTime();
   }

   public float getCurrentTime() {
      return this.timedElement.getLastSampleTime();
   }

   public float getSimpleDuration() {
      return this.timedElement.getSimpleDur();
   }

   public float getHyperlinkBeginTime() {
      return this.timedElement.getHyperlinkBeginTime();
   }

   public boolean beginElement() throws DOMException {
      this.timedElement.beginElement();
      return this.timedElement.canBegin();
   }

   public boolean beginElementAt(float offset) throws DOMException {
      this.timedElement.beginElement(offset);
      return true;
   }

   public boolean endElement() throws DOMException {
      this.timedElement.endElement();
      return this.timedElement.canEnd();
   }

   public boolean endElementAt(float offset) throws DOMException {
      this.timedElement.endElement(offset);
      return true;
   }

   protected boolean isConstantAnimation() {
      return false;
   }

   protected class SVGTimedElement extends TimedElement {
      public Element getElement() {
         return SVGAnimationElementBridge.this.element;
      }

      protected void fireTimeEvent(String eventType, Calendar time, int detail) {
         AnimationSupport.fireTimeEvent(SVGAnimationElementBridge.this.element, eventType, time, detail);
      }

      protected void toActive(float begin) {
         SVGAnimationElementBridge.this.eng.toActive(SVGAnimationElementBridge.this.animation, begin);
      }

      protected void toInactive(boolean stillActive, boolean isFrozen) {
         SVGAnimationElementBridge.this.eng.toInactive(SVGAnimationElementBridge.this.animation, isFrozen);
      }

      protected void removeFill() {
         SVGAnimationElementBridge.this.eng.removeFill(SVGAnimationElementBridge.this.animation);
      }

      protected void sampledAt(float simpleTime, float simpleDur, int repeatIteration) {
         SVGAnimationElementBridge.this.eng.sampledAt(SVGAnimationElementBridge.this.animation, simpleTime, simpleDur, repeatIteration);
      }

      protected void sampledLastValue(int repeatIteration) {
         SVGAnimationElementBridge.this.eng.sampledLastValue(SVGAnimationElementBridge.this.animation, repeatIteration);
      }

      protected TimedElement getTimedElementById(String id) {
         return AnimationSupport.getTimedElementById(id, SVGAnimationElementBridge.this.element);
      }

      protected EventTarget getEventTargetById(String id) {
         return AnimationSupport.getEventTargetById(id, SVGAnimationElementBridge.this.element);
      }

      protected EventTarget getRootEventTarget() {
         return (EventTarget)SVGAnimationElementBridge.this.element.getOwnerDocument();
      }

      protected EventTarget getAnimationEventTarget() {
         return SVGAnimationElementBridge.this.targetElement;
      }

      public boolean isBefore(TimedElement other) {
         Element e = other.getElement();
         int pos = SVGAnimationElementBridge.this.element.compareDocumentPosition(e);
         return (pos & 2) != 0;
      }

      public String toString() {
         if (SVGAnimationElementBridge.this.element != null) {
            String id = SVGAnimationElementBridge.this.element.getAttributeNS((String)null, "id");
            if (id.length() != 0) {
               return id;
            }
         }

         return super.toString();
      }

      protected boolean isConstantAnimation() {
         return SVGAnimationElementBridge.this.isConstantAnimation();
      }
   }
}
