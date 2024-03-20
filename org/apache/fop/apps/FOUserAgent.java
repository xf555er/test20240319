package org.apache.fop.apps;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.Version;
import org.apache.fop.accessibility.DummyStructureTreeEventHandler;
import org.apache.fop.accessibility.StructureTreeEventHandler;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.configuration.ConfigurationException;
import org.apache.fop.events.DefaultEventBroadcaster;
import org.apache.fop.events.Event;
import org.apache.fop.events.EventBroadcaster;
import org.apache.fop.events.EventListener;
import org.apache.fop.events.FOPEventListenerProxy;
import org.apache.fop.events.LoggingEventListener;
import org.apache.fop.fo.ElementMappingRegistry;
import org.apache.fop.fo.FOEventHandler;
import org.apache.fop.fonts.FontManager;
import org.apache.fop.hyphenation.HyphenationTreeCache;
import org.apache.fop.layoutmgr.LayoutManagerMaker;
import org.apache.fop.render.ImageHandlerRegistry;
import org.apache.fop.render.Renderer;
import org.apache.fop.render.RendererConfig;
import org.apache.fop.render.RendererConfigOption;
import org.apache.fop.render.RendererFactory;
import org.apache.fop.render.XMLHandlerRegistry;
import org.apache.fop.render.intermediate.IFDocumentHandler;
import org.apache.fop.util.ColorSpaceCache;
import org.apache.fop.util.ContentHandlerFactoryRegistry;
import org.apache.xmlgraphics.image.loader.ImageContext;
import org.apache.xmlgraphics.image.loader.ImageManager;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;
import org.apache.xmlgraphics.image.loader.impl.AbstractImageSessionContext;
import org.apache.xmlgraphics.image.loader.util.SoftMapCache;

public class FOUserAgent {
   private static Log log = LogFactory.getLog("FOP");
   private final FopFactory factory;
   private final InternalResourceResolver resourceResolver;
   private float targetResolution = 72.0F;
   private Map rendererOptions = new HashMap();
   private File outputFile;
   private IFDocumentHandler documentHandlerOverride;
   private Renderer rendererOverride;
   private FOEventHandler foEventHandlerOverride;
   private boolean locatorEnabled = true;
   private boolean conserveMemoryPolicy;
   private EventBroadcaster eventBroadcaster = new FOPEventBroadcaster();
   private StructureTreeEventHandler structureTreeEventHandler;
   private boolean pdfUAEnabled;
   protected String producer;
   protected String creator;
   protected Date creationDate;
   protected String author;
   protected String title;
   protected String subject;
   protected String keywords;
   private final ImageSessionContext imageSessionContext;
   private final SoftMapCache pdfObjectCache;

   FOUserAgent(final FopFactory factory, InternalResourceResolver resourceResolver) {
      this.structureTreeEventHandler = DummyStructureTreeEventHandler.INSTANCE;
      this.producer = "Apache FOP Version " + Version.getVersion();
      this.pdfObjectCache = new SoftMapCache(true);
      this.factory = factory;
      this.resourceResolver = resourceResolver;
      this.setTargetResolution(factory.getTargetResolution());
      this.setAccessibility(factory.isAccessibilityEnabled());
      this.setKeepEmptyTags(factory.isKeepEmptyTags());
      this.imageSessionContext = new AbstractImageSessionContext(factory.getFallbackResolver()) {
         public ImageContext getParentContext() {
            return factory;
         }

         public float getTargetResolution() {
            return FOUserAgent.this.getTargetResolution();
         }

         public Source resolveURI(String uri) {
            return FOUserAgent.this.resolveURI(uri);
         }
      };
   }

   public Fop newFop(String outputFormat, OutputStream stream) throws FOPException {
      return new Fop(outputFormat, this, stream);
   }

   public Fop newFop(String outputFormat) throws FOPException {
      return this.newFop(outputFormat, (OutputStream)null);
   }

   public InternalResourceResolver getResourceResolver() {
      return this.resourceResolver;
   }

   public void setDocumentHandlerOverride(IFDocumentHandler documentHandler) {
      if (this.isAccessibilityEnabled()) {
         this.setStructureTreeEventHandler(documentHandler.getStructureTreeEventHandler());
      }

      this.documentHandlerOverride = documentHandler;
   }

   public IFDocumentHandler getDocumentHandlerOverride() {
      return this.documentHandlerOverride;
   }

   public void setRendererOverride(Renderer renderer) {
      this.rendererOverride = renderer;
   }

   public Renderer getRendererOverride() {
      return this.rendererOverride;
   }

   public void setFOEventHandlerOverride(FOEventHandler handler) {
      this.foEventHandlerOverride = handler;
   }

   public FOEventHandler getFOEventHandlerOverride() {
      return this.foEventHandlerOverride;
   }

   public void setProducer(String producer) {
      this.producer = producer;
   }

   public String getProducer() {
      return this.producer;
   }

   public void setCreator(String creator) {
      this.creator = creator;
   }

   public String getCreator() {
      return this.creator;
   }

   public void setCreationDate(Date creationDate) {
      this.creationDate = creationDate;
   }

   public Date getCreationDate() {
      return this.creationDate;
   }

   public void setAuthor(String author) {
      this.author = author;
   }

   public String getAuthor() {
      return this.author;
   }

   public void setTitle(String title) {
      this.title = title;
   }

   public String getTitle() {
      return this.title;
   }

   public void setSubject(String subject) {
      this.subject = subject;
   }

   public String getSubject() {
      return this.subject;
   }

   public void setKeywords(String keywords) {
      this.keywords = keywords;
   }

   public String getKeywords() {
      return this.keywords;
   }

   public Map getRendererOptions() {
      return this.rendererOptions;
   }

   public Object getRendererOption(RendererConfigOption option) {
      return this.rendererOptions.get(option.getName());
   }

   public StreamSource resolveURI(String uri) {
      try {
         StreamSource src = new StreamSource(this.resourceResolver.getResource(uri));
         src.setSystemId(this.getResourceResolver().getBaseURI().toASCIIString());
         return src;
      } catch (URISyntaxException var3) {
         return null;
      } catch (IOException var4) {
         return null;
      }
   }

   public void setOutputFile(File f) {
      this.outputFile = f;
   }

   public File getOutputFile() {
      return this.outputFile;
   }

   public float getTargetPixelUnitToMillimeter() {
      return 25.4F / this.targetResolution;
   }

   public float getTargetResolution() {
      return this.targetResolution;
   }

   public void setTargetResolution(float dpi) {
      this.targetResolution = dpi;
      if (log.isDebugEnabled()) {
         log.debug("target-resolution set to: " + this.targetResolution + "dpi (px2mm=" + this.getTargetPixelUnitToMillimeter() + ")");
      }

   }

   public void setTargetResolution(int dpi) {
      this.setTargetResolution((float)dpi);
   }

   public ImageSessionContext getImageSessionContext() {
      return this.imageSessionContext;
   }

   public float getSourcePixelUnitToMillimeter() {
      return this.factory.getSourcePixelUnitToMillimeter();
   }

   public float getSourceResolution() {
      return this.factory.getSourceResolution();
   }

   public String getPageHeight() {
      return this.factory.getPageHeight();
   }

   public String getPageWidth() {
      return this.factory.getPageWidth();
   }

   public boolean validateStrictly() {
      return this.factory.validateStrictly();
   }

   public boolean isBreakIndentInheritanceOnReferenceAreaBoundary() {
      return this.factory.isBreakIndentInheritanceOnReferenceAreaBoundary();
   }

   public RendererFactory getRendererFactory() {
      return this.factory.getRendererFactory();
   }

   public XMLHandlerRegistry getXMLHandlerRegistry() {
      return this.factory.getXMLHandlerRegistry();
   }

   public void setLocatorEnabled(boolean enableLocator) {
      this.locatorEnabled = enableLocator;
   }

   public boolean isLocatorEnabled() {
      return this.locatorEnabled;
   }

   public EventBroadcaster getEventBroadcaster() {
      return this.eventBroadcaster;
   }

   public boolean isPdfUAEnabled() {
      return this.pdfUAEnabled;
   }

   public void setPdfUAEnabled(boolean pdfUAEnabled) {
      this.pdfUAEnabled = pdfUAEnabled;
   }

   public boolean isConserveMemoryPolicyEnabled() {
      return this.conserveMemoryPolicy;
   }

   public void setConserveMemoryPolicy(boolean conserveMemoryPolicy) {
      this.conserveMemoryPolicy = conserveMemoryPolicy;
   }

   public boolean isComplexScriptFeaturesEnabled() {
      return this.factory.isComplexScriptFeaturesEnabled();
   }

   public RendererConfig getRendererConfig(String mimeType, RendererConfig.RendererConfigParser configCreator) throws FOPException {
      return this.factory.getRendererConfig(this, this.getRendererConfiguration(mimeType), configCreator);
   }

   public Configuration getRendererConfiguration(String mimeType) {
      Configuration cfg = this.getUserConfig();
      String type = "renderer";
      String mime = "mime";
      if (cfg == null) {
         if (log.isDebugEnabled()) {
            log.debug("userconfig is null");
         }

         return null;
      } else {
         Configuration userConfig = null;
         Configuration[] cfgs = cfg.getChild(type + "s").getChildren(type);
         Configuration[] var7 = cfgs;
         int var8 = cfgs.length;

         for(int var9 = 0; var9 < var8; ++var9) {
            Configuration child = var7[var9];

            try {
               if (child.getAttribute(mime).equals(mimeType)) {
                  userConfig = child;
                  break;
               }
            } catch (ConfigurationException var12) {
            }
         }

         log.debug((userConfig == null ? "No u" : "U") + "ser configuration found for MIME type " + mimeType);
         return userConfig;
      }
   }

   public void setAccessibility(boolean accessibility) {
      if (accessibility) {
         this.getRendererOptions().put("accessibility", Boolean.TRUE);
      }

   }

   public boolean isAccessibilityEnabled() {
      Boolean enabled = (Boolean)this.getRendererOptions().get("accessibility");
      return enabled != null ? enabled : false;
   }

   public void setStructureTreeEventHandler(StructureTreeEventHandler structureTreeEventHandler) {
      this.structureTreeEventHandler = structureTreeEventHandler;
   }

   public StructureTreeEventHandler getStructureTreeEventHandler() {
      return this.structureTreeEventHandler;
   }

   public LayoutManagerMaker getLayoutManagerMakerOverride() {
      return this.factory.getLayoutManagerMakerOverride();
   }

   public ContentHandlerFactoryRegistry getContentHandlerFactoryRegistry() {
      return this.factory.getContentHandlerFactoryRegistry();
   }

   public ImageManager getImageManager() {
      return this.factory.getImageManager();
   }

   public ElementMappingRegistry getElementMappingRegistry() {
      return this.factory.getElementMappingRegistry();
   }

   public FontManager getFontManager() {
      return this.factory.getFontManager();
   }

   public boolean isNamespaceIgnored(String namespaceURI) {
      return this.factory.isNamespaceIgnored(namespaceURI);
   }

   public boolean validateUserConfigStrictly() {
      return this.factory.validateUserConfigStrictly();
   }

   public Configuration getUserConfig() {
      return this.factory.getUserConfig();
   }

   public ImageHandlerRegistry getImageHandlerRegistry() {
      return this.factory.getImageHandlerRegistry();
   }

   public ColorSpaceCache getColorSpaceCache() {
      return this.factory.getColorSpaceCache();
   }

   public Map getHyphenationPatternNames() {
      return this.factory.getHyphenationPatternNames();
   }

   public InternalResourceResolver getHyphenationResourceResolver() {
      return this.factory.getHyphenationResourceResolver();
   }

   public SoftMapCache getPDFObjectCache() {
      return this.pdfObjectCache;
   }

   public HyphenationTreeCache getHyphenationTreeCache() {
      return this.factory.getHyphenationTreeCache();
   }

   public void setKeepEmptyTags(boolean b) {
      this.getRendererOptions().put("keep-empty-tags", b);
   }

   public boolean isKeepEmptyTags() {
      Boolean enabled = (Boolean)this.getRendererOptions().get("keep-empty-tags");
      return enabled != null ? enabled : true;
   }

   public boolean isTableBorderOverpaint() {
      return this.factory.isTableBorderOverpaint();
   }

   private class FOPEventBroadcaster extends DefaultEventBroadcaster {
      private EventListener rootListener = new EventListener() {
         public void processEvent(Event event) {
            if (!FOPEventBroadcaster.this.listeners.hasEventListeners()) {
               FOPEventBroadcaster.this.addEventListener(new LoggingEventListener(LogFactory.getLog(FOUserAgent.class)));
            }

            FOPEventBroadcaster.this.rootListener = new FOPEventListenerProxy(FOPEventBroadcaster.this.listeners, FOUserAgent.this);
            FOPEventBroadcaster.this.rootListener.processEvent(event);
         }
      };

      public FOPEventBroadcaster() {
      }

      public void broadcastEvent(Event event) {
         this.rootListener.processEvent(event);
      }
   }
}
