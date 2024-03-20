package org.apache.fop.afp.svg;

import java.awt.geom.AffineTransform;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DefaultFontFamilyResolver;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.FontFamilyResolver;
import org.apache.batik.bridge.TextPainter;
import org.apache.batik.bridge.UserAgent;
import org.apache.fop.afp.AFPGraphics2D;
import org.apache.fop.events.EventBroadcaster;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.svg.AbstractFOPBridgeContext;
import org.apache.fop.svg.font.AggregatingFontFamilyResolver;
import org.apache.xmlgraphics.image.loader.ImageManager;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;

public class AFPBridgeContext extends AbstractFOPBridgeContext {
   private final AFPGraphics2D g2d;
   private final EventBroadcaster eventBroadCaster;

   public AFPBridgeContext(UserAgent userAgent, FontInfo fontInfo, ImageManager imageManager, ImageSessionContext imageSessionContext, AffineTransform linkTransform, AFPGraphics2D g2d, EventBroadcaster eventBroadCaster) {
      super(userAgent, fontInfo, imageManager, imageSessionContext, linkTransform);
      this.g2d = g2d;
      this.eventBroadCaster = eventBroadCaster;
   }

   private AFPBridgeContext(UserAgent userAgent, DocumentLoader documentLoader, FontInfo fontInfo, ImageManager imageManager, ImageSessionContext imageSessionContext, AffineTransform linkTransform, AFPGraphics2D g2d, EventBroadcaster eventBroadCaster) {
      super(userAgent, documentLoader, fontInfo, imageManager, imageSessionContext, linkTransform);
      this.g2d = g2d;
      this.eventBroadCaster = eventBroadCaster;
   }

   public void registerSVGBridges() {
      super.registerSVGBridges();
      if (this.fontInfo != null) {
         AFPTextHandler textHandler = new AFPTextHandler(this.fontInfo, this.g2d.getResourceManager());
         this.g2d.setCustomTextHandler(textHandler);
         FontFamilyResolver fontFamilyResolver = new AggregatingFontFamilyResolver(new FontFamilyResolver[]{new AFPFontFamilyResolver(this.fontInfo, this.eventBroadCaster), DefaultFontFamilyResolver.SINGLETON});
         TextPainter textPainter = new AFPTextPainter(textHandler, fontFamilyResolver);
         this.setTextPainter(new AFPTextPainter(textHandler, fontFamilyResolver));
         this.putBridge(new AFPTextElementBridge(textPainter));
      }

      this.putBridge(new AFPImageElementBridge());
   }

   public BridgeContext createBridgeContext() {
      return new AFPBridgeContext(this.getUserAgent(), this.getDocumentLoader(), this.fontInfo, this.getImageManager(), this.getImageSessionContext(), this.linkTransform, this.g2d, this.eventBroadCaster);
   }
}
