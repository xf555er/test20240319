package org.apache.fop.apps.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.xmlgraphics.io.Resource;
import org.apache.xmlgraphics.io.ResourceResolver;
import org.apache.xmlgraphics.io.TempResourceResolver;
import org.apache.xmlgraphics.io.TempResourceURIGenerator;

public final class ResourceResolverFactory {
   private ResourceResolverFactory() {
   }

   public static ResourceResolver createDefaultResourceResolver() {
      return ResourceResolverFactory.DefaultResourceResolver.INSTANCE;
   }

   public static InternalResourceResolver createDefaultInternalResourceResolver(URI baseURI) {
      return new InternalResourceResolver(baseURI, createDefaultResourceResolver());
   }

   public static InternalResourceResolver createInternalResourceResolver(URI baseURI, ResourceResolver resolver) {
      return new InternalResourceResolver(baseURI, resolver);
   }

   public static ResourceResolver createTempAwareResourceResolver(TempResourceResolver tempResourceResolver, ResourceResolver defaultResourceResolver) {
      return new TempAwareResourceResolver(tempResourceResolver, defaultResourceResolver);
   }

   public static SchemeAwareResourceResolverBuilder createSchemeAwareResourceResolverBuilder(ResourceResolver defaultResolver) {
      return new SchemeAwareResourceResolverBuilderImpl(defaultResolver);
   }

   private static final class SchemeAwareResourceResolverBuilderImpl implements SchemeAwareResourceResolverBuilder {
      private SchemeAwareResourceResolverBuilder delegate;

      private SchemeAwareResourceResolverBuilderImpl(ResourceResolver defaultResolver) {
         this.delegate = new ActiveSchemeAwareResourceResolverBuilder(defaultResolver);
      }

      public void registerResourceResolverForScheme(String scheme, ResourceResolver resourceResolver) {
         this.delegate.registerResourceResolverForScheme(scheme, resourceResolver);
      }

      public ResourceResolver build() {
         ResourceResolver resourceResolver = this.delegate.build();
         this.delegate = ResourceResolverFactory.CompletedSchemeAwareResourceResolverBuilder.INSTANCE;
         return resourceResolver;
      }

      // $FF: synthetic method
      SchemeAwareResourceResolverBuilderImpl(ResourceResolver x0, Object x1) {
         this(x0);
      }
   }

   private static final class ActiveSchemeAwareResourceResolverBuilder implements SchemeAwareResourceResolverBuilder {
      private final Map schemeHandlingResourceResolvers;
      private final ResourceResolver defaultResolver;

      private ActiveSchemeAwareResourceResolverBuilder(ResourceResolver defaultResolver) {
         this.schemeHandlingResourceResolvers = new HashMap();
         this.defaultResolver = defaultResolver;
      }

      public void registerResourceResolverForScheme(String scheme, ResourceResolver resourceResolver) {
         this.schemeHandlingResourceResolvers.put(scheme, resourceResolver);
      }

      public ResourceResolver build() {
         return new SchemeAwareResourceResolver(Collections.unmodifiableMap(this.schemeHandlingResourceResolvers), this.defaultResolver);
      }

      // $FF: synthetic method
      ActiveSchemeAwareResourceResolverBuilder(ResourceResolver x0, Object x1) {
         this(x0);
      }
   }

   private static final class CompletedSchemeAwareResourceResolverBuilder implements SchemeAwareResourceResolverBuilder {
      private static final SchemeAwareResourceResolverBuilder INSTANCE = new CompletedSchemeAwareResourceResolverBuilder();

      public ResourceResolver build() {
         throw new IllegalStateException("Resource resolver already built");
      }

      public void registerResourceResolverForScheme(String scheme, ResourceResolver resourceResolver) {
         throw new IllegalStateException("Resource resolver already built");
      }
   }

   public interface SchemeAwareResourceResolverBuilder {
      void registerResourceResolverForScheme(String var1, ResourceResolver var2);

      ResourceResolver build();
   }

   private static final class SchemeAwareResourceResolver implements ResourceResolver {
      private final Map schemeHandlingResourceResolvers;
      private final ResourceResolver defaultResolver;

      private SchemeAwareResourceResolver(Map schemEHandlingResourceResolvers, ResourceResolver defaultResolver) {
         this.schemeHandlingResourceResolvers = schemEHandlingResourceResolvers;
         this.defaultResolver = defaultResolver;
      }

      private ResourceResolver getResourceResolverForScheme(URI uri) {
         String scheme = uri.getScheme();
         return this.schemeHandlingResourceResolvers.containsKey(scheme) ? (ResourceResolver)this.schemeHandlingResourceResolvers.get(scheme) : this.defaultResolver;
      }

      public Resource getResource(URI uri) throws IOException {
         return this.getResourceResolverForScheme(uri).getResource(uri);
      }

      public OutputStream getOutputStream(URI uri) throws IOException {
         return this.getResourceResolverForScheme(uri).getOutputStream(uri);
      }

      // $FF: synthetic method
      SchemeAwareResourceResolver(Map x0, ResourceResolver x1, Object x2) {
         this(x0, x1);
      }
   }

   private static class NormalResourceResolver implements ResourceResolver {
      private NormalResourceResolver() {
      }

      public Resource getResource(URI uri) throws IOException {
         return new Resource(uri.toURL().openStream());
      }

      public OutputStream getOutputStream(URI uri) throws IOException {
         return new FileOutputStream(new File(uri));
      }

      // $FF: synthetic method
      NormalResourceResolver(Object x0) {
         this();
      }
   }

   private static class FileDeletingInputStream extends FilterInputStream {
      private final File file;

      protected FileDeletingInputStream(File file) throws MalformedURLException, IOException {
         super(file.toURI().toURL().openStream());
         this.file = file;
      }

      public void close() throws IOException {
         try {
            super.close();
         } finally {
            this.file.delete();
         }

      }
   }

   private static class DefaultTempResourceResolver implements TempResourceResolver {
      private final ConcurrentHashMap tempFiles;

      private DefaultTempResourceResolver() {
         this.tempFiles = new ConcurrentHashMap();
      }

      private File getTempFile(String uri) throws IllegalStateException {
         File tempFile = (File)this.tempFiles.remove(uri);
         if (tempFile == null) {
            throw new IllegalStateException(uri + " was never created or has been deleted");
         } else {
            return tempFile;
         }
      }

      private File createTempFile(String path) throws IOException {
         File tempFile = File.createTempFile(path, ".fop.tmp");
         File oldFile = (File)this.tempFiles.put(path, tempFile);
         if (oldFile != null) {
            String errorMsg = oldFile.getAbsolutePath() + " has been already created for " + path;
            boolean newTempDeleted = tempFile.delete();
            if (!newTempDeleted) {
               errorMsg = errorMsg + ". " + tempFile.getAbsolutePath() + " was not deleted.";
            }

            throw new IOException(errorMsg);
         } else {
            return tempFile;
         }
      }

      public Resource getResource(String id) throws IOException {
         return new Resource(new FileDeletingInputStream(this.getTempFile(id)));
      }

      public OutputStream getOutputStream(String id) throws IOException {
         return new FileOutputStream(this.createTempFile(id));
      }

      // $FF: synthetic method
      DefaultTempResourceResolver(Object x0) {
         this();
      }
   }

   private static final class TempAwareResourceResolver implements ResourceResolver {
      private final TempResourceResolver tempResourceResolver;
      private final ResourceResolver defaultResourceResolver;

      public TempAwareResourceResolver(TempResourceResolver tempResourceHandler, ResourceResolver defaultResourceResolver) {
         this.tempResourceResolver = tempResourceHandler;
         this.defaultResourceResolver = defaultResourceResolver;
      }

      private static boolean isTempURI(URI uri) {
         return TempResourceURIGenerator.isTempURI(uri);
      }

      public Resource getResource(URI uri) throws IOException {
         return isTempURI(uri) ? this.tempResourceResolver.getResource(uri.getPath()) : this.defaultResourceResolver.getResource(uri);
      }

      public OutputStream getOutputStream(URI uri) throws IOException {
         return isTempURI(uri) ? this.tempResourceResolver.getOutputStream(uri.getPath()) : this.defaultResourceResolver.getOutputStream(uri);
      }
   }

   private static final class DefaultResourceResolver implements ResourceResolver {
      private static final ResourceResolver INSTANCE = new DefaultResourceResolver();
      private final TempAwareResourceResolver delegate = new TempAwareResourceResolver(new DefaultTempResourceResolver(), new NormalResourceResolver());

      public Resource getResource(URI uri) throws IOException {
         return this.delegate.getResource(uri);
      }

      public OutputStream getOutputStream(URI uri) throws IOException {
         return this.delegate.getOutputStream(uri);
      }
   }
}
