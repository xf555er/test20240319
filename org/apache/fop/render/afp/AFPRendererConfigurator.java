package org.apache.fop.render.afp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.afp.AFPEventProducer;
import org.apache.fop.afp.AFPResourceLevelDefaults;
import org.apache.fop.afp.fonts.AFPFontCollection;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.fonts.FontCollection;
import org.apache.fop.render.PrintRendererConfigurator;
import org.apache.fop.render.RendererConfig;
import org.apache.fop.render.intermediate.IFDocumentHandler;
import org.apache.fop.util.LogUtil;

public class AFPRendererConfigurator extends PrintRendererConfigurator {
   private static Log log = LogFactory.getLog(AFPRendererConfigurator.class);
   private final AFPEventProducer eventProducer;

   public AFPRendererConfigurator(FOUserAgent userAgent, RendererConfig.RendererConfigParser rendererConfigParser) {
      super(userAgent, rendererConfigParser);
      this.eventProducer = AFPEventProducer.Provider.get(userAgent.getEventBroadcaster());
   }

   public void configure(IFDocumentHandler documentHandler) throws FOPException {
      AFPRendererConfig config = (AFPRendererConfig)this.getRendererConfig(documentHandler);
      if (config != null) {
         AFPDocumentHandler afpDocumentHandler = (AFPDocumentHandler)documentHandler;
         this.configure(afpDocumentHandler, config);
      }

   }

   private void configure(AFPDocumentHandler documentHandler, AFPRendererConfig config) {
      Boolean colorImages = config.isColorImages();
      if (colorImages != null) {
         documentHandler.setColorImages(colorImages);
         if (colorImages) {
            documentHandler.setCMYKImagesSupported(config.isCmykImagesSupported());
         } else {
            documentHandler.setBitsPerPixel(config.getBitsPerPixel());
         }
      }

      if (config.getDitheringQuality() != null) {
         documentHandler.setDitheringQuality(config.getDitheringQuality());
      }

      if (config.isNativeImagesSupported() != null) {
         documentHandler.setNativeImagesSupported(config.isNativeImagesSupported());
      }

      if (config.getShadingMode() != null) {
         documentHandler.setShadingMode(config.getShadingMode());
      }

      if (config.getResolution() != null) {
         documentHandler.setResolution(config.getResolution());
      }

      if (config.isWrapPseg() != null) {
         documentHandler.setWrapPSeg(config.isWrapPseg());
      }

      if (config.isGocaWrapPseg() != null) {
         documentHandler.setWrapGocaPSeg(config.isGocaWrapPseg());
      }

      if (config.isFs45() != null) {
         documentHandler.setFS45(config.isFs45());
      }

      if (config.allowJpegEmbedding() != null) {
         documentHandler.canEmbedJpeg(config.allowJpegEmbedding());
      }

      if (config.getBitmapEncodingQuality() != null) {
         documentHandler.setBitmapEncodingQuality(config.getBitmapEncodingQuality());
      }

      if (config.getLineWidthCorrection() != null) {
         documentHandler.setLineWidthCorrection(config.getLineWidthCorrection());
      }

      if (config.isGocaEnabled() != null) {
         documentHandler.setGOCAEnabled(config.isGocaEnabled());
      }

      if (config.isStrokeGocaText() != null) {
         documentHandler.setStrokeGOCAText(config.isStrokeGocaText());
      }

      if (config.getDefaultResourceGroupUri() != null) {
         documentHandler.setDefaultResourceGroupUri(config.getDefaultResourceGroupUri());
      }

      AFPResourceLevelDefaults resourceLevelDefaults = config.getResourceLevelDefaults();
      if (resourceLevelDefaults != null) {
         documentHandler.setResourceLevelDefaults(resourceLevelDefaults);
      }

   }

   protected List getDefaultFontCollection() {
      return new ArrayList();
   }

   protected FontCollection getCustomFontCollection(InternalResourceResolver uriResolverWrapper, String mimeType) throws FOPException {
      AFPRendererConfig config = (AFPRendererConfig)this.getRendererConfig(mimeType);
      if (config != null) {
         try {
            return new AFPFontCollection(this.userAgent.getEventBroadcaster(), this.createFontsList(config.getFontInfoConfig(), mimeType));
         } catch (IOException var5) {
            this.eventProducer.invalidConfiguration(this, var5);
            LogUtil.handleException(log, var5, this.userAgent.validateUserConfigStrictly());
         } catch (IllegalArgumentException var6) {
            this.eventProducer.invalidConfiguration(this, var6);
            LogUtil.handleException(log, var6, this.userAgent.validateUserConfigStrictly());
         }
      }

      return new AFPFontCollection(this.userAgent.getEventBroadcaster(), (List)null);
   }

   private List createFontsList(AFPFontConfig fontConfig, String mimeType) throws FOPException, IOException {
      List afpFonts = new ArrayList();
      Iterator var4 = fontConfig.getFontConfig().iterator();

      while(var4.hasNext()) {
         AFPFontConfig.AFPFontConfigData config = (AFPFontConfig.AFPFontConfigData)var4.next();
         afpFonts.add(config.getFontInfo(this.userAgent.getFontManager().getResourceResolver(), this.eventProducer));
      }

      return afpFonts;
   }
}
