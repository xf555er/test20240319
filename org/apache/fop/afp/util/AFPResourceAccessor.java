package org.apache.fop.afp.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.io.InternalResourceResolver;

public final class AFPResourceAccessor {
   private static final Log log = LogFactory.getLog(AFPResourceAccessor.class);
   private final InternalResourceResolver resourceResolver;
   private final URI baseURI;
   private final URIResolver uriResolver;

   public AFPResourceAccessor(InternalResourceResolver resourceResolver, String baseURI) {
      this.resourceResolver = resourceResolver;
      URI actualBaseURI = null;
      Object uriResolver;
      if (baseURI == null) {
         actualBaseURI = null;
         uriResolver = new NullBaseURIResolver();
      } else {
         try {
            actualBaseURI = InternalResourceResolver.getBaseURI(baseURI);
            uriResolver = new BaseURIResolver();
         } catch (URISyntaxException var6) {
            log.error("The URI given \"" + baseURI + "\" is invalid: " + var6.getMessage());
            actualBaseURI = null;
            uriResolver = new NullBaseURIResolver();
         }
      }

      this.baseURI = actualBaseURI;
      this.uriResolver = (URIResolver)uriResolver;
   }

   public AFPResourceAccessor(InternalResourceResolver resourceResolver) {
      this(resourceResolver, (String)null);
   }

   public InputStream createInputStream(URI uri) throws IOException {
      return this.resourceResolver.getResource(this.uriResolver.resolveURI(uri));
   }

   public URI resolveURI(String uri) {
      return this.uriResolver.resolveURI(uri);
   }

   private final class BaseURIResolver implements URIResolver {
      private BaseURIResolver() {
      }

      public URI resolveURI(URI uri) {
         return AFPResourceAccessor.this.baseURI.resolve(uri);
      }

      public URI resolveURI(String uri) {
         return AFPResourceAccessor.this.baseURI.resolve(uri.trim());
      }

      // $FF: synthetic method
      BaseURIResolver(Object x1) {
         this();
      }
   }

   private static final class NullBaseURIResolver implements URIResolver {
      private NullBaseURIResolver() {
      }

      public URI resolveURI(URI uri) {
         return uri;
      }

      public URI resolveURI(String uri) {
         return URI.create("./" + uri.trim());
      }

      // $FF: synthetic method
      NullBaseURIResolver(Object x0) {
         this();
      }
   }

   private interface URIResolver {
      URI resolveURI(URI var1);

      URI resolveURI(String var1);
   }
}
