package org.apache.fop.image.loader.batik;

import java.awt.geom.AffineTransform;
import org.apache.batik.anim.dom.SVGOMDocument;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.UserAgent;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.svg.AbstractFOPBridgeContext;
import org.apache.fop.svg.SVGUserAgent;
import org.apache.xmlgraphics.image.loader.ImageManager;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;

class GenericFOPBridgeContext extends AbstractFOPBridgeContext {
   public GenericFOPBridgeContext(UserAgent userAgent, DocumentLoader documentLoader, FontInfo fontInfo, ImageManager imageManager, ImageSessionContext imageSessionContext, AffineTransform linkTransform) {
      super(userAgent, documentLoader, fontInfo, imageManager, imageSessionContext, linkTransform);
   }

   public GenericFOPBridgeContext(UserAgent userAgent, FontInfo fontInfo, ImageManager imageManager, ImageSessionContext imageSessionContext) {
      super(userAgent, fontInfo, imageManager, imageSessionContext);
   }

   public GenericFOPBridgeContext(SVGUserAgent userAgent, FontInfo fontInfo, ImageManager imageManager, ImageSessionContext imageSessionContext, AffineTransform linkTransform) {
      super(userAgent, fontInfo, imageManager, imageSessionContext, linkTransform);
   }

   public void registerSVGBridges() {
      super.registerSVGBridges();
      this.putBridge(new GenericFOPImageElementBridge());
   }

   public BridgeContext createBridgeContext(SVGOMDocument doc) {
      return this.createBridgeContext();
   }

   public BridgeContext createBridgeContext() {
      return new GenericFOPBridgeContext(this.getUserAgent(), this.getDocumentLoader(), this.fontInfo, this.getImageManager(), this.getImageSessionContext(), this.linkTransform);
   }
}
