package org.apache.fop.apps;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.apps.io.ResourceResolverFactory;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.fo.ElementMapping;
import org.apache.fop.fo.ElementMappingRegistry;
import org.apache.fop.fonts.FontManager;
import org.apache.fop.hyphenation.HyphenationTreeCache;
import org.apache.fop.layoutmgr.LayoutManagerMaker;
import org.apache.fop.render.ImageHandlerRegistry;
import org.apache.fop.render.RendererConfig;
import org.apache.fop.render.RendererFactory;
import org.apache.fop.render.XMLHandlerRegistry;
import org.apache.fop.util.ColorSpaceCache;
import org.apache.fop.util.ContentHandlerFactoryRegistry;
import org.apache.xmlgraphics.image.loader.ImageContext;
import org.apache.xmlgraphics.image.loader.ImageManager;
import org.apache.xmlgraphics.image.loader.impl.AbstractImageSessionContext;
import org.xml.sax.SAXException;

public final class FopFactory implements ImageContext {
   private static Log log = LogFactory.getLog(FopFactory.class);
   private final RendererFactory rendererFactory;
   private final XMLHandlerRegistry xmlHandlers;
   private final ImageHandlerRegistry imageHandlers;
   private final ElementMappingRegistry elementMappingRegistry;
   private final ContentHandlerFactoryRegistry contentHandlerFactoryRegistry = new ContentHandlerFactoryRegistry();
   private final ColorSpaceCache colorSpaceCache;
   private final FopFactoryConfig config;
   private final InternalResourceResolver resolver;
   private final Map rendererConfig;
   private HyphenationTreeCache hyphenationTreeCache;
   private Map hyphPatNames;

   private FopFactory(FopFactoryConfig config) {
      this.config = config;
      this.resolver = ResourceResolverFactory.createInternalResourceResolver(config.getBaseURI(), config.getResourceResolver());
      this.elementMappingRegistry = new ElementMappingRegistry(this);
      this.colorSpaceCache = new ColorSpaceCache(this.resolver);
      this.rendererFactory = new RendererFactory(config.preferRenderer());
      this.xmlHandlers = new XMLHandlerRegistry();
      this.imageHandlers = new ImageHandlerRegistry();
      this.rendererConfig = new HashMap();
   }

   public static FopFactory newInstance(FopFactoryConfig config) {
      return new FopFactory(config);
   }

   public static FopFactory newInstance(File fopConf) throws SAXException, IOException {
      return (new FopConfParser(fopConf)).getFopFactoryBuilder().build();
   }

   public static FopFactory newInstance(URI baseURI) {
      return (new FopFactoryBuilder(baseURI)).build();
   }

   public static FopFactory newInstance(URI baseURI, InputStream confStream) throws SAXException, IOException {
      return (new FopConfParser(confStream, baseURI)).getFopFactoryBuilder().build();
   }

   public FOUserAgent newFOUserAgent() {
      FOUserAgent userAgent = new FOUserAgent(this, this.resolver);
      return userAgent;
   }

   boolean isComplexScriptFeaturesEnabled() {
      return this.config.isComplexScriptFeaturesEnabled();
   }

   public Fop newFop(String outputFormat) throws FOPException {
      return this.newFOUserAgent().newFop(outputFormat);
   }

   public Fop newFop(String outputFormat, FOUserAgent userAgent) throws FOPException {
      return userAgent.newFop(outputFormat, (OutputStream)null);
   }

   boolean isTableBorderOverpaint() {
      return this.config.isTableBorderOverpaint();
   }

   public Fop newFop(String outputFormat, OutputStream stream) throws FOPException {
      return this.newFOUserAgent().newFop(outputFormat, stream);
   }

   public Fop newFop(String outputFormat, FOUserAgent userAgent, OutputStream stream) throws FOPException {
      return userAgent.newFop(outputFormat, stream);
   }

   public Fop newFop(FOUserAgent userAgent) throws FOPException {
      if (userAgent.getRendererOverride() == null && userAgent.getFOEventHandlerOverride() == null && userAgent.getDocumentHandlerOverride() == null) {
         throw new IllegalStateException("An overriding renderer, FOEventHandler or IFDocumentHandler must be set on the user agent when this factory method is used!");
      } else {
         return this.newFop((String)null, (FOUserAgent)userAgent);
      }
   }

   public RendererFactory getRendererFactory() {
      return this.rendererFactory;
   }

   public XMLHandlerRegistry getXMLHandlerRegistry() {
      return this.xmlHandlers;
   }

   public ImageHandlerRegistry getImageHandlerRegistry() {
      return this.imageHandlers;
   }

   public ElementMappingRegistry getElementMappingRegistry() {
      return this.elementMappingRegistry;
   }

   public ContentHandlerFactoryRegistry getContentHandlerFactoryRegistry() {
      return this.contentHandlerFactoryRegistry;
   }

   synchronized RendererConfig getRendererConfig(FOUserAgent userAgent, Configuration cfg, RendererConfig.RendererConfigParser configCreator) throws FOPException {
      RendererConfig config = (RendererConfig)this.rendererConfig.get(configCreator.getMimeType());
      if (config == null) {
         try {
            config = configCreator.build(userAgent, cfg);
            this.rendererConfig.put(configCreator.getMimeType(), config);
         } catch (Exception var6) {
            throw new FOPException(var6);
         }
      }

      return config;
   }

   public void addElementMapping(ElementMapping elementMapping) {
      this.elementMappingRegistry.addElementMapping(elementMapping);
   }

   boolean isAccessibilityEnabled() {
      return this.config.isAccessibilityEnabled();
   }

   boolean isKeepEmptyTags() {
      return this.config.isKeepEmptyTags();
   }

   public ImageManager getImageManager() {
      return this.config.getImageManager();
   }

   public LayoutManagerMaker getLayoutManagerMakerOverride() {
      return this.config.getLayoutManagerMakerOverride();
   }

   public Map getHyphenationPatternNames() {
      return this.config.getHyphenationPatternNames();
   }

   public boolean validateStrictly() {
      return this.config.validateStrictly();
   }

   public boolean isBreakIndentInheritanceOnReferenceAreaBoundary() {
      return this.config.isBreakIndentInheritanceOnReferenceAreaBoundary();
   }

   public float getSourceResolution() {
      return this.config.getSourceResolution();
   }

   public float getTargetResolution() {
      return this.config.getTargetResolution();
   }

   public InternalResourceResolver getHyphenationResourceResolver() {
      return this.config.getHyphenationResourceResolver();
   }

   public float getSourcePixelUnitToMillimeter() {
      return 25.4F / this.getSourceResolution();
   }

   public float getTargetPixelUnitToMillimeter() {
      return 25.4F / this.getTargetResolution();
   }

   public String getPageHeight() {
      return this.config.getPageHeight();
   }

   public String getPageWidth() {
      return this.config.getPageWidth();
   }

   public boolean isNamespaceIgnored(String namespaceURI) {
      return this.config.isNamespaceIgnored(namespaceURI);
   }

   public Set getIgnoredNamespace() {
      return this.config.getIgnoredNamespaces();
   }

   public Configuration getUserConfig() {
      return this.config.getUserConfig();
   }

   public boolean validateUserConfigStrictly() {
      return this.config.validateUserConfigStrictly();
   }

   public FontManager getFontManager() {
      return this.config.getFontManager();
   }

   AbstractImageSessionContext.FallbackResolver getFallbackResolver() {
      return this.config.getFallbackResolver();
   }

   public ColorSpaceCache getColorSpaceCache() {
      return this.colorSpaceCache;
   }

   public HyphenationTreeCache getHyphenationTreeCache() {
      if (this.hyphenationTreeCache == null) {
         this.hyphenationTreeCache = new HyphenationTreeCache();
      }

      return this.hyphenationTreeCache;
   }
}
