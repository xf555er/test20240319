package org.apache.fop.apps;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.apps.io.ResourceResolverFactory;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.fonts.FontManager;
import org.apache.fop.layoutmgr.LayoutManagerMaker;
import org.apache.xmlgraphics.image.loader.ImageContext;
import org.apache.xmlgraphics.image.loader.ImageManager;
import org.apache.xmlgraphics.image.loader.impl.AbstractImageSessionContext;
import org.apache.xmlgraphics.io.ResourceResolver;

public final class FopFactoryBuilder {
   private final FopFactoryConfig config;
   private FopFactoryConfigBuilder fopFactoryConfigBuilder;

   public FopFactoryBuilder(URI defaultBaseURI) {
      this(defaultBaseURI, ResourceResolverFactory.createDefaultResourceResolver());
   }

   public FopFactoryBuilder(URI defaultBaseURI, ResourceResolver resourceResolver) {
      this(EnvironmentalProfileFactory.createDefault(defaultBaseURI, resourceResolver));
   }

   public FopFactoryBuilder(EnvironmentProfile enviro) {
      this.config = new FopFactoryConfigImpl(enviro);
      this.fopFactoryConfigBuilder = new ActiveFopFactoryConfigBuilder((FopFactoryConfigImpl)this.config);
   }

   /** @deprecated */
   public FopFactoryConfig buildConfig() {
      return this.buildConfiguration();
   }

   FopFactoryConfig buildConfiguration() {
      this.fopFactoryConfigBuilder = FopFactoryBuilder.CompletedFopFactoryConfigBuilder.INSTANCE;
      return this.config;
   }

   public FopFactory build() {
      return FopFactory.newInstance(this.buildConfiguration());
   }

   URI getBaseURI() {
      return this.config.getBaseURI();
   }

   public FontManager getFontManager() {
      return this.config.getFontManager();
   }

   public ImageManager getImageManager() {
      return this.config.getImageManager();
   }

   public FopFactoryBuilder setAccessibility(boolean enableAccessibility) {
      this.fopFactoryConfigBuilder.setAccessibility(enableAccessibility);
      return this;
   }

   public FopFactoryBuilder setKeepEmptyTags(boolean b) {
      this.fopFactoryConfigBuilder.setKeepEmptyTags(b);
      return this;
   }

   public FopFactoryBuilder setLayoutManagerMakerOverride(LayoutManagerMaker lmMaker) {
      this.fopFactoryConfigBuilder.setLayoutManagerMakerOverride(lmMaker);
      return this;
   }

   public FopFactoryBuilder setBaseURI(URI baseURI) {
      this.fopFactoryConfigBuilder.setBaseURI(baseURI);
      return this;
   }

   public FopFactoryBuilder setHyphenBaseResourceResolver(InternalResourceResolver hyphenationResourceResolver) {
      this.fopFactoryConfigBuilder.setHyphenationResourceResolver(hyphenationResourceResolver);
      return this;
   }

   public FopFactoryBuilder setStrictFOValidation(boolean validateStrictly) {
      this.fopFactoryConfigBuilder.setStrictFOValidation(validateStrictly);
      return this;
   }

   public FopFactoryBuilder setStrictUserConfigValidation(boolean validateStrictly) {
      this.fopFactoryConfigBuilder.setStrictUserConfigValidation(validateStrictly);
      return this;
   }

   public FopFactoryBuilder setBreakIndentInheritanceOnReferenceAreaBoundary(boolean value) {
      this.fopFactoryConfigBuilder.setBreakIndentInheritanceOnReferenceAreaBoundary(value);
      return this;
   }

   public FopFactoryBuilder setSourceResolution(float dpi) {
      this.fopFactoryConfigBuilder.setSourceResolution(dpi);
      return this;
   }

   public FopFactoryBuilder setTargetResolution(float dpi) {
      this.fopFactoryConfigBuilder.setTargetResolution(dpi);
      return this;
   }

   public FopFactoryBuilder setPageHeight(String pageHeight) {
      this.fopFactoryConfigBuilder.setPageHeight(pageHeight);
      return this;
   }

   public FopFactoryBuilder setPageWidth(String pageWidth) {
      this.fopFactoryConfigBuilder.setPageWidth(pageWidth);
      return this;
   }

   public FopFactoryBuilder ignoreNamespace(String namespaceURI) {
      this.fopFactoryConfigBuilder.ignoreNamespace(namespaceURI);
      return this;
   }

   public FopFactoryBuilder ignoreNamespaces(Collection namespaceURIs) {
      this.fopFactoryConfigBuilder.ignoreNamespaces(namespaceURIs);
      return this;
   }

   public FopFactoryBuilder setConfiguration(Configuration cfg) {
      this.fopFactoryConfigBuilder.setConfiguration(cfg);
      return this;
   }

   public FopFactoryBuilder setPreferRenderer(boolean preferRenderer) {
      this.fopFactoryConfigBuilder.setPreferRenderer(preferRenderer);
      return this;
   }

   public FopFactoryBuilder setComplexScriptFeatures(boolean csf) {
      this.fopFactoryConfigBuilder.setComplexScriptFeaturesEnabled(csf);
      return this;
   }

   public FopFactoryBuilder setHyphPatNames(Map hyphPatNames) {
      this.fopFactoryConfigBuilder.setHyphPatNames(hyphPatNames);
      return this;
   }

   public FopFactoryBuilder setTableBorderOverpaint(boolean b) {
      this.fopFactoryConfigBuilder.setTableBorderOverpaint(b);
      return this;
   }

   private static final class ActiveFopFactoryConfigBuilder implements FopFactoryConfigBuilder {
      private final FopFactoryConfigImpl config;

      private ActiveFopFactoryConfigBuilder(FopFactoryConfigImpl config) {
         this.config = config;
      }

      public void setAccessibility(boolean enableAccessibility) {
         this.config.accessibility = enableAccessibility;
      }

      public void setKeepEmptyTags(boolean b) {
         this.config.keepEmptyTags = b;
      }

      public void setLayoutManagerMakerOverride(LayoutManagerMaker lmMaker) {
         this.config.layoutManagerMaker = lmMaker;
      }

      public void setBaseURI(URI baseURI) {
         this.config.baseURI = baseURI;
      }

      public void setHyphenationResourceResolver(InternalResourceResolver hyphenationResourceResolver) {
         this.config.hyphenationResourceResolver = hyphenationResourceResolver;
      }

      public void setStrictFOValidation(boolean validateStrictly) {
         this.config.hasStrictFOValidation = validateStrictly;
      }

      public void setStrictUserConfigValidation(boolean validateStrictly) {
         this.config.hasStrictUserValidation = validateStrictly;
      }

      public void setBreakIndentInheritanceOnReferenceAreaBoundary(boolean value) {
         this.config.breakIndentInheritanceOnReferenceBoundary = value;
      }

      public void setSourceResolution(float dpi) {
         this.config.sourceResolution = dpi;
      }

      public void setTargetResolution(float dpi) {
         this.config.targetResolution = dpi;
      }

      public void setPageHeight(String pageHeight) {
         this.config.pageHeight = pageHeight;
      }

      public void setPageWidth(String pageWidth) {
         this.config.pageWidth = pageWidth;
      }

      public void ignoreNamespace(String namespaceURI) {
         this.config.ignoredNamespaces.add(namespaceURI);
      }

      public void ignoreNamespaces(Collection namespaceURIs) {
         this.config.ignoredNamespaces.addAll(namespaceURIs);
      }

      public void setConfiguration(Configuration cfg) {
         this.config.cfg = cfg;
      }

      public void setPreferRenderer(boolean preferRenderer) {
         this.config.preferRenderer = preferRenderer;
      }

      public void setComplexScriptFeaturesEnabled(boolean csf) {
         this.config.isComplexScript = csf;
      }

      public void setHyphPatNames(Map hyphPatNames) {
         this.config.hyphPatNames = hyphPatNames;
      }

      public void setTableBorderOverpaint(boolean b) {
         this.config.tableBorderOverpaint = b;
      }

      // $FF: synthetic method
      ActiveFopFactoryConfigBuilder(FopFactoryConfigImpl x0, Object x1) {
         this(x0);
      }
   }

   private static final class CompletedFopFactoryConfigBuilder implements FopFactoryConfigBuilder {
      private static final CompletedFopFactoryConfigBuilder INSTANCE = new CompletedFopFactoryConfigBuilder();

      private void throwIllegalStateException() {
         throw new IllegalStateException("The final FOP Factory configuration has already been built");
      }

      public void setAccessibility(boolean enableAccessibility) {
         this.throwIllegalStateException();
      }

      public void setKeepEmptyTags(boolean b) {
         this.throwIllegalStateException();
      }

      public void setLayoutManagerMakerOverride(LayoutManagerMaker lmMaker) {
         this.throwIllegalStateException();
      }

      public void setBaseURI(URI baseURI) {
         this.throwIllegalStateException();
      }

      public void setHyphenationResourceResolver(InternalResourceResolver hyphenationResourceResolver) {
         this.throwIllegalStateException();
      }

      public void setStrictFOValidation(boolean validateStrictly) {
         this.throwIllegalStateException();
      }

      public void setStrictUserConfigValidation(boolean validateStrictly) {
         this.throwIllegalStateException();
      }

      public void setBreakIndentInheritanceOnReferenceAreaBoundary(boolean value) {
         this.throwIllegalStateException();
      }

      public void setSourceResolution(float dpi) {
         this.throwIllegalStateException();
      }

      public void setTargetResolution(float dpi) {
         this.throwIllegalStateException();
      }

      public void setPageHeight(String pageHeight) {
         this.throwIllegalStateException();
      }

      public void setPageWidth(String pageWidth) {
         this.throwIllegalStateException();
      }

      public void ignoreNamespace(String namespaceURI) {
         this.throwIllegalStateException();
      }

      public void ignoreNamespaces(Collection namespaceURIs) {
         this.throwIllegalStateException();
      }

      public void setConfiguration(Configuration cfg) {
         this.throwIllegalStateException();
      }

      public void setPreferRenderer(boolean preferRenderer) {
         this.throwIllegalStateException();
      }

      public void setComplexScriptFeaturesEnabled(boolean csf) {
         this.throwIllegalStateException();
      }

      public void setHyphPatNames(Map hyphPatNames) {
         this.throwIllegalStateException();
      }

      public void setTableBorderOverpaint(boolean b) {
         this.throwIllegalStateException();
      }
   }

   private interface FopFactoryConfigBuilder {
      void setAccessibility(boolean var1);

      void setKeepEmptyTags(boolean var1);

      void setLayoutManagerMakerOverride(LayoutManagerMaker var1);

      void setBaseURI(URI var1);

      void setHyphenationResourceResolver(InternalResourceResolver var1);

      void setStrictFOValidation(boolean var1);

      void setStrictUserConfigValidation(boolean var1);

      void setBreakIndentInheritanceOnReferenceAreaBoundary(boolean var1);

      void setSourceResolution(float var1);

      void setTargetResolution(float var1);

      void setPageHeight(String var1);

      void setPageWidth(String var1);

      void ignoreNamespace(String var1);

      void ignoreNamespaces(Collection var1);

      void setConfiguration(Configuration var1);

      void setPreferRenderer(boolean var1);

      void setComplexScriptFeaturesEnabled(boolean var1);

      void setHyphPatNames(Map var1);

      void setTableBorderOverpaint(boolean var1);
   }

   public static class FopFactoryConfigImpl implements FopFactoryConfig {
      private final EnvironmentProfile enviro;
      private final ImageManager imageManager;
      private boolean accessibility;
      private boolean keepEmptyTags = true;
      private LayoutManagerMaker layoutManagerMaker;
      private URI baseURI;
      private InternalResourceResolver hyphenationResourceResolver;
      private boolean hasStrictFOValidation = true;
      private boolean hasStrictUserValidation = true;
      private boolean breakIndentInheritanceOnReferenceBoundary = false;
      private float sourceResolution = 72.0F;
      private float targetResolution = 72.0F;
      private String pageHeight = "11in";
      private String pageWidth = "8.26in";
      private Set ignoredNamespaces = new HashSet();
      private Configuration cfg;
      private boolean preferRenderer;
      private boolean isComplexScript = true;
      private Map hyphPatNames;
      private boolean tableBorderOverpaint;

      FopFactoryConfigImpl(EnvironmentProfile enviro) {
         this.enviro = enviro;
         this.baseURI = enviro.getDefaultBaseURI();
         this.imageManager = new ImageManager(new ImageContextImpl(this));
      }

      public boolean isAccessibilityEnabled() {
         return this.accessibility;
      }

      public boolean isKeepEmptyTags() {
         return this.keepEmptyTags;
      }

      public LayoutManagerMaker getLayoutManagerMakerOverride() {
         return this.layoutManagerMaker;
      }

      public ResourceResolver getResourceResolver() {
         return this.enviro.getResourceResolver();
      }

      public URI getBaseURI() {
         return this.baseURI;
      }

      public InternalResourceResolver getHyphenationResourceResolver() {
         return this.hyphenationResourceResolver;
      }

      public boolean validateStrictly() {
         return this.hasStrictFOValidation;
      }

      public boolean validateUserConfigStrictly() {
         return this.hasStrictUserValidation;
      }

      public boolean isBreakIndentInheritanceOnReferenceAreaBoundary() {
         return this.breakIndentInheritanceOnReferenceBoundary;
      }

      public float getSourceResolution() {
         return this.sourceResolution;
      }

      public float getTargetResolution() {
         return this.targetResolution;
      }

      public String getPageHeight() {
         return this.pageHeight;
      }

      public String getPageWidth() {
         return this.pageWidth;
      }

      public Set getIgnoredNamespaces() {
         return Collections.unmodifiableSet(this.ignoredNamespaces);
      }

      public boolean isNamespaceIgnored(String namespace) {
         return this.ignoredNamespaces.contains(namespace);
      }

      public Configuration getUserConfig() {
         return this.cfg;
      }

      public boolean preferRenderer() {
         return this.preferRenderer;
      }

      public FontManager getFontManager() {
         return this.enviro.getFontManager();
      }

      public ImageManager getImageManager() {
         return this.imageManager;
      }

      public boolean isComplexScriptFeaturesEnabled() {
         return this.isComplexScript;
      }

      public boolean isTableBorderOverpaint() {
         return this.tableBorderOverpaint;
      }

      public Map getHyphenationPatternNames() {
         return this.hyphPatNames;
      }

      public AbstractImageSessionContext.FallbackResolver getFallbackResolver() {
         return this.enviro.getFallbackResolver();
      }

      private static final class ImageContextImpl implements ImageContext {
         private final FopFactoryConfig config;

         ImageContextImpl(FopFactoryConfig config) {
            this.config = config;
         }

         public float getSourceResolution() {
            return this.config.getSourceResolution();
         }
      }
   }
}
