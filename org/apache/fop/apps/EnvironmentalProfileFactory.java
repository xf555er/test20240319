package org.apache.fop.apps;

import java.net.URI;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.apps.io.ResourceResolverFactory;
import org.apache.fop.fonts.FontCacheManager;
import org.apache.fop.fonts.FontCacheManagerFactory;
import org.apache.fop.fonts.FontDetector;
import org.apache.fop.fonts.FontDetectorFactory;
import org.apache.fop.fonts.FontManager;
import org.apache.xmlgraphics.image.loader.impl.AbstractImageSessionContext;
import org.apache.xmlgraphics.io.ResourceResolver;

public final class EnvironmentalProfileFactory {
   private EnvironmentalProfileFactory() {
   }

   public static EnvironmentProfile createDefault(URI defaultBaseUri, ResourceResolver resourceResolver) {
      return new Profile(defaultBaseUri, resourceResolver, createFontManager(defaultBaseUri, resourceResolver, FontDetectorFactory.createDefault(), FontCacheManagerFactory.createDefault()), new AbstractImageSessionContext.UnrestrictedFallbackResolver());
   }

   public static EnvironmentProfile createRestrictedIO(URI defaultBaseUri, ResourceResolver resourceResolver) {
      return new Profile(defaultBaseUri, resourceResolver, createFontManager(defaultBaseUri, resourceResolver, FontDetectorFactory.createDisabled(), FontCacheManagerFactory.createDisabled()), new AbstractImageSessionContext.RestrictedFallbackResolver());
   }

   private static FontManager createFontManager(URI defaultBaseUri, ResourceResolver resourceResolver, FontDetector fontDetector, FontCacheManager fontCacheManager) {
      InternalResourceResolver internalResolver = ResourceResolverFactory.createInternalResourceResolver(defaultBaseUri, resourceResolver);
      return new FontManager(internalResolver, fontDetector, fontCacheManager);
   }

   private static final class Profile implements EnvironmentProfile {
      private final ResourceResolver resourceResolver;
      private final FontManager fontManager;
      private final URI defaultBaseURI;
      private final AbstractImageSessionContext.FallbackResolver fallbackResolver;

      private Profile(URI defaultBaseURI, ResourceResolver resourceResolver, FontManager fontManager, AbstractImageSessionContext.FallbackResolver fallbackResolver) {
         if (defaultBaseURI == null) {
            throw new IllegalArgumentException("Default base URI must not be null");
         } else if (resourceResolver == null) {
            throw new IllegalArgumentException("ResourceResolver must not be null");
         } else if (fontManager == null) {
            throw new IllegalArgumentException("The FontManager must not be null");
         } else {
            this.defaultBaseURI = defaultBaseURI;
            this.resourceResolver = resourceResolver;
            this.fontManager = fontManager;
            this.fallbackResolver = fallbackResolver;
         }
      }

      public ResourceResolver getResourceResolver() {
         return this.resourceResolver;
      }

      public FontManager getFontManager() {
         return this.fontManager;
      }

      public URI getDefaultBaseURI() {
         return this.defaultBaseURI;
      }

      public AbstractImageSessionContext.FallbackResolver getFallbackResolver() {
         return this.fallbackResolver;
      }

      // $FF: synthetic method
      Profile(URI x0, ResourceResolver x1, FontManager x2, AbstractImageSessionContext.FallbackResolver x3, Object x4) {
         this(x0, x1, x2, x3);
      }
   }
}
