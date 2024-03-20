package org.apache.batik.bridge;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import org.apache.batik.anim.dom.AnimatedLiveAttributeValue;
import org.apache.batik.anim.dom.SVGOMDocument;
import org.apache.batik.css.engine.CSSEngineEvent;
import org.apache.batik.dom.svg.SVGContext;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.RootGraphicsNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.events.MutationEvent;

public class SVGDocumentBridge implements DocumentBridge, BridgeUpdateHandler, SVGContext {
   protected Document document;
   protected RootGraphicsNode node;
   protected BridgeContext ctx;

   public String getNamespaceURI() {
      return null;
   }

   public String getLocalName() {
      return null;
   }

   public Bridge getInstance() {
      return new SVGDocumentBridge();
   }

   public RootGraphicsNode createGraphicsNode(BridgeContext ctx, Document doc) {
      RootGraphicsNode gn = new RootGraphicsNode();
      this.document = doc;
      this.node = gn;
      this.ctx = ctx;
      ((SVGOMDocument)doc).setSVGContext(this);
      return gn;
   }

   public void buildGraphicsNode(BridgeContext ctx, Document doc, RootGraphicsNode node) {
      if (ctx.isDynamic()) {
         ctx.bind(doc, node);
      }

   }

   public void handleDOMAttrModifiedEvent(MutationEvent evt) {
   }

   public void handleDOMNodeInsertedEvent(MutationEvent evt) {
      if (evt.getTarget() instanceof Element) {
         Element childElt = (Element)evt.getTarget();
         GVTBuilder builder = this.ctx.getGVTBuilder();
         GraphicsNode childNode = builder.build(this.ctx, childElt);
         if (childNode == null) {
            return;
         }

         this.node.add(childNode);
      }

   }

   public void handleDOMNodeRemovedEvent(MutationEvent evt) {
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
      ((SVGOMDocument)this.document).setSVGContext((SVGContext)null);
      this.ctx.unbind(this.document);
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
}
