package org.apache.fop.render.afp;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.EnumMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.afp.AFPEventProducer;
import org.apache.fop.afp.AFPResourceLevel;
import org.apache.fop.afp.AFPResourceLevelDefaults;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.configuration.ConfigurationException;
import org.apache.fop.fonts.FontManager;
import org.apache.fop.render.RendererConfig;
import org.apache.fop.util.LogUtil;

public final class AFPRendererConfig implements RendererConfig {
   private final EnumMap params;
   private final EnumMap imageModeParams;
   private final AFPFontConfig fontConfig;

   private AFPRendererConfig(AFPFontConfig fontConfig) {
      this.params = new EnumMap(AFPRendererOption.class);
      this.imageModeParams = new EnumMap(ImagesModeOptions.class);
      this.fontConfig = fontConfig;
   }

   public AFPFontConfig getFontInfoConfig() {
      return this.fontConfig;
   }

   public Boolean isColorImages() {
      return (Boolean)this.getParam(AFPRendererOption.IMAGES_MODE, Boolean.class);
   }

   public Boolean isCmykImagesSupported() {
      if (!this.isColorImages()) {
         throw new IllegalStateException();
      } else {
         return (Boolean)Boolean.class.cast(this.imageModeParams.get(AFPRendererConfig.ImagesModeOptions.MODE_COLOR));
      }
   }

   public Integer getBitsPerPixel() {
      if (this.isColorImages()) {
         throw new IllegalStateException();
      } else {
         return (Integer)Integer.class.cast(this.imageModeParams.get(AFPRendererConfig.ImagesModeOptions.MODE_GRAYSCALE));
      }
   }

   public Float getDitheringQuality() {
      return (Float)this.getParam(AFPRendererOption.IMAGES_DITHERING_QUALITY, Float.class);
   }

   public Boolean isNativeImagesSupported() {
      return (Boolean)this.getParam(AFPRendererOption.IMAGES_NATIVE, Boolean.class);
   }

   public AFPShadingMode getShadingMode() {
      return (AFPShadingMode)this.getParam(AFPRendererOption.SHADING, AFPShadingMode.class);
   }

   public Integer getResolution() {
      return (Integer)this.getParam(AFPRendererOption.RENDERER_RESOLUTION, Integer.class);
   }

   public URI getDefaultResourceGroupUri() {
      return (URI)this.getParam(AFPRendererOption.RESOURCE_GROUP_URI, URI.class);
   }

   public AFPResourceLevelDefaults getResourceLevelDefaults() {
      return (AFPResourceLevelDefaults)this.getParam(AFPRendererOption.DEFAULT_RESOURCE_LEVELS, AFPResourceLevelDefaults.class);
   }

   public Boolean isWrapPseg() {
      return (Boolean)this.getParam(AFPRendererOption.IMAGES_WRAP_PSEG, Boolean.class);
   }

   public Boolean isGocaWrapPseg() {
      return (Boolean)this.getParam(AFPRendererOption.GOCA_WRAP_PSEG, Boolean.class);
   }

   public Boolean isFs45() {
      return (Boolean)this.getParam(AFPRendererOption.IMAGES_FS45, Boolean.class);
   }

   public Boolean allowJpegEmbedding() {
      return (Boolean)this.getParam(AFPRendererOption.JPEG_ALLOW_JPEG_EMBEDDING, Boolean.class);
   }

   public Float getBitmapEncodingQuality() {
      return (Float)this.getParam(AFPRendererOption.JPEG_BITMAP_ENCODING_QUALITY, Float.class);
   }

   public Float getLineWidthCorrection() {
      return (Float)this.getParam(AFPRendererOption.LINE_WIDTH_CORRECTION, Float.class);
   }

   public Boolean isGocaEnabled() {
      return (Boolean)this.getParam(AFPRendererOption.GOCA, Boolean.class);
   }

   public Boolean isStrokeGocaText() {
      return (Boolean)this.getParam(AFPRendererOption.GOCA_TEXT, Boolean.class);
   }

   private Object getParam(AFPRendererOption options, Class type) {
      assert options.getType().equals(type);

      return type.cast(this.params.get(options));
   }

   private void setParam(AFPRendererOption option, Object value) {
      assert option.getType().isInstance(value);

      this.params.put(option, value);
   }

   // $FF: synthetic method
   AFPRendererConfig(AFPFontConfig x0, Object x1) {
      this(x0);
   }

   private static final class ParserHelper {
      private static final Log LOG = LogFactory.getLog(ParserHelper.class);
      private final AFPRendererConfig config;
      private final boolean strict;
      private final Configuration cfg;

      private ParserHelper(Configuration cfg, FontManager fontManager, boolean strict, AFPEventProducer eventProducer) throws ConfigurationException, FOPException {
         this.cfg = cfg;
         this.strict = strict;
         if (cfg != null) {
            this.config = new AFPRendererConfig((new AFPFontConfig.AFPFontInfoConfigParser()).parse(cfg, fontManager, strict, eventProducer));
            this.configure();
         } else {
            this.config = new AFPRendererConfig((new AFPFontConfig.AFPFontInfoConfigParser()).getEmptyConfig());
         }

      }

      private void configure() throws ConfigurationException, FOPException {
         this.configureImages();
         this.configureGOCA();
         this.setParam(AFPRendererOption.SHADING, AFPShadingMode.getValueOf(this.cfg.getChild(AFPRendererOption.SHADING.getName()).getValue(AFPShadingMode.COLOR.getName())));
         Configuration rendererResolutionCfg = this.cfg.getChild(AFPRendererOption.RENDERER_RESOLUTION.getName(), false);
         this.setParam(AFPRendererOption.RENDERER_RESOLUTION, rendererResolutionCfg == null ? 240 : rendererResolutionCfg.getValueAsInteger(240));
         Configuration lineWidthCorrectionCfg = this.cfg.getChild(AFPRendererOption.LINE_WIDTH_CORRECTION.getName(), false);
         this.setParam(AFPRendererOption.LINE_WIDTH_CORRECTION, lineWidthCorrectionCfg != null ? lineWidthCorrectionCfg.getValueAsFloat() : 2.5F);
         Configuration gocaCfg = this.cfg.getChild(AFPRendererOption.GOCA.getName());
         boolean gocaEnabled = gocaCfg.getAttributeAsBoolean("enabled", true);
         this.setParam(AFPRendererOption.GOCA, gocaEnabled);
         String strokeGocaText = gocaCfg.getAttribute(AFPRendererOption.GOCA_TEXT.getName(), "default");
         this.setParam(AFPRendererOption.GOCA_TEXT, "stroke".equalsIgnoreCase(strokeGocaText) || "shapes".equalsIgnoreCase(strokeGocaText));
         this.createResourceGroupFile();
         this.createResourceLevel();
      }

      private void setParam(AFPRendererOption option, Object value) {
         this.config.setParam(option, value);
      }

      private void configureImages() throws ConfigurationException, FOPException {
         Configuration imagesCfg = this.cfg.getChild(AFPRendererOption.IMAGES.getName());
         ImagesModeOptions imagesMode = AFPRendererConfig.ImagesModeOptions.forName(imagesCfg.getAttribute(AFPRendererOption.IMAGES_MODE.getName(), AFPRendererConfig.ImagesModeOptions.MODE_GRAYSCALE.getName()));
         boolean colorImages = AFPRendererConfig.ImagesModeOptions.MODE_COLOR == imagesMode;
         this.setParam(AFPRendererOption.IMAGES_MODE, colorImages);
         if (colorImages) {
            this.config.imageModeParams.put(AFPRendererConfig.ImagesModeOptions.MODE_COLOR, imagesCfg.getAttributeAsBoolean(imagesMode.getModeAttribute(), false));
         } else {
            this.config.imageModeParams.put(AFPRendererConfig.ImagesModeOptions.MODE_GRAYSCALE, imagesCfg.getAttributeAsInteger(imagesMode.getModeAttribute(), 8));
         }

         String dithering = imagesCfg.getAttribute(AFPRendererOption.IMAGES_DITHERING_QUALITY.getName(), "medium");
         float dq;
         if (dithering.startsWith("min")) {
            dq = 0.0F;
         } else if (dithering.startsWith("max")) {
            dq = 1.0F;
         } else {
            try {
               dq = Float.parseFloat(dithering);
            } catch (NumberFormatException var7) {
               dq = 0.5F;
            }
         }

         this.setParam(AFPRendererOption.IMAGES_DITHERING_QUALITY, dq);
         this.setParam(AFPRendererOption.IMAGES_NATIVE, imagesCfg.getAttributeAsBoolean(AFPRendererOption.IMAGES_NATIVE.getName(), false));
         this.setParam(AFPRendererOption.IMAGES_WRAP_PSEG, imagesCfg.getAttributeAsBoolean(AFPRendererOption.IMAGES_WRAP_PSEG.getName(), false));
         this.setParam(AFPRendererOption.IMAGES_FS45, imagesCfg.getAttributeAsBoolean(AFPRendererOption.IMAGES_FS45.getName(), false));
         if ("scale-to-fit".equals(imagesCfg.getAttribute(AFPRendererOption.IMAGES_MAPPING_OPTION.getName(), (String)null))) {
            this.setParam(AFPRendererOption.IMAGES_MAPPING_OPTION, (byte)96);
         } else {
            this.setParam(AFPRendererOption.IMAGES_MAPPING_OPTION, (byte)0);
         }

         this.configureJpegImages(imagesCfg);
      }

      private void configureGOCA() {
         Configuration gocaCfg = this.cfg.getChild(AFPRendererOption.GOCA.getName());
         this.setParam(AFPRendererOption.GOCA_WRAP_PSEG, gocaCfg.getAttributeAsBoolean(AFPRendererOption.GOCA_WRAP_PSEG.getName(), false));
      }

      private void configureJpegImages(Configuration imagesCfg) {
         Configuration jpegConfig = imagesCfg.getChild(AFPRendererOption.IMAGES_JPEG.getName());
         float bitmapEncodingQuality = 1.0F;
         boolean allowJpegEmbedding = false;
         if (jpegConfig != null) {
            allowJpegEmbedding = jpegConfig.getAttributeAsBoolean(AFPRendererOption.JPEG_ALLOW_JPEG_EMBEDDING.getName(), false);
            String bitmapEncodingQualityStr = jpegConfig.getAttribute(AFPRendererOption.JPEG_BITMAP_ENCODING_QUALITY.getName(), (String)null);
            if (bitmapEncodingQualityStr != null) {
               try {
                  bitmapEncodingQuality = Float.parseFloat(bitmapEncodingQualityStr);
               } catch (NumberFormatException var7) {
               }
            }
         }

         this.setParam(AFPRendererOption.JPEG_BITMAP_ENCODING_QUALITY, bitmapEncodingQuality);
         this.setParam(AFPRendererOption.JPEG_ALLOW_JPEG_EMBEDDING, allowJpegEmbedding);
      }

      private void createResourceGroupFile() throws FOPException {
         try {
            Configuration resourceGroupUriCfg = this.cfg.getChild(AFPRendererOption.RESOURCE_GROUP_URI.getName(), false);
            if (resourceGroupUriCfg != null) {
               URI resourceGroupUri = InternalResourceResolver.cleanURI(resourceGroupUriCfg.getValue());
               this.setParam(AFPRendererOption.RESOURCE_GROUP_URI, resourceGroupUri);
            }
         } catch (ConfigurationException var3) {
            LogUtil.handleException(LOG, var3, this.strict);
         } catch (URISyntaxException var4) {
            LogUtil.handleException(LOG, var4, this.strict);
         }

      }

      private void createResourceLevel() throws FOPException {
         Configuration defaultResourceLevelCfg = this.cfg.getChild(AFPRendererOption.DEFAULT_RESOURCE_LEVELS.getName(), false);
         if (defaultResourceLevelCfg != null) {
            AFPResourceLevelDefaults defaults = new AFPResourceLevelDefaults();
            String[] types = defaultResourceLevelCfg.getAttributeNames();
            String[] var4 = types;
            int var5 = types.length;

            for(int var6 = 0; var6 < var5; ++var6) {
               String type = var4[var6];

               try {
                  String level = defaultResourceLevelCfg.getAttribute(type);
                  defaults.setDefaultResourceLevel(type, AFPResourceLevel.valueOf(level));
               } catch (IllegalArgumentException var9) {
                  LogUtil.handleException(LOG, var9, this.strict);
               } catch (ConfigurationException var10) {
                  LogUtil.handleException(LOG, var10, this.strict);
               }
            }

            this.setParam(AFPRendererOption.DEFAULT_RESOURCE_LEVELS, defaults);
         }

      }

      // $FF: synthetic method
      ParserHelper(Configuration x0, FontManager x1, boolean x2, AFPEventProducer x3, Object x4) throws ConfigurationException, FOPException {
         this(x0, x1, x2, x3);
      }
   }

   public static final class AFPRendererConfigParser implements RendererConfig.RendererConfigParser {
      private static final Log LOG = LogFactory.getLog(AFPRendererConfigParser.class);

      public AFPRendererConfig build(FOUserAgent userAgent, Configuration cfg) throws FOPException {
         boolean strict = userAgent.validateUserConfigStrictly();
         AFPRendererConfig config = null;
         AFPEventProducer eventProducer = AFPEventProducer.Provider.get(userAgent.getEventBroadcaster());

         try {
            config = (new ParserHelper(cfg, userAgent.getFontManager(), strict, eventProducer)).config;
         } catch (ConfigurationException var7) {
            LogUtil.handleException(LOG, var7, strict);
         }

         return config;
      }

      public String getMimeType() {
         return "application/x-afp";
      }
   }

   public static enum ImagesModeOptions {
      MODE_GRAYSCALE("b+w", "bits-per-pixel"),
      MODE_COLOR("color", "cmyk");

      private final String name;
      private final String modeAttribute;

      private ImagesModeOptions(String name, String modeAttribute) {
         this.name = name;
         this.modeAttribute = modeAttribute;
      }

      public String getName() {
         return this.name;
      }

      public String getModeAttribute() {
         return this.modeAttribute;
      }

      public static ImagesModeOptions forName(String name) {
         ImagesModeOptions[] var1 = values();
         int var2 = var1.length;

         for(int var3 = 0; var3 < var2; ++var3) {
            ImagesModeOptions option = var1[var3];
            if (option.name.equals(name)) {
               return option;
            }
         }

         throw new IllegalArgumentException(name);
      }
   }
}
