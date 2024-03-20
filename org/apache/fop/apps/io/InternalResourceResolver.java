package org.apache.fop.apps.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import org.apache.xmlgraphics.io.Resource;
import org.apache.xmlgraphics.io.ResourceResolver;
import org.apache.xmlgraphics.util.uri.DataURIResolver;

public class InternalResourceResolver {
   private final URI baseUri;
   private final ResourceResolver resourceResolver;
   private final DataURIResolver dataSchemeResolver = new DataURIResolver();

   InternalResourceResolver(URI baseUri, ResourceResolver resourceResolver) {
      this.baseUri = baseUri;
      this.resourceResolver = resourceResolver;
   }

   public URI getBaseURI() {
      return this.baseUri;
   }

   public Resource getResource(String stringUri) throws IOException, URISyntaxException {
      return stringUri.startsWith("data:") ? new Resource(this.resolveDataURI(stringUri)) : this.getResource(cleanURI(stringUri));
   }

   public Resource getResource(URI uri) throws IOException {
      return uri.getScheme() != null && uri.getScheme().startsWith("data") ? new Resource(this.resolveDataURI(uri.toASCIIString())) : this.resourceResolver.getResource(this.resolveFromBase(uri));
   }

   public OutputStream getOutputStream(URI uri) throws IOException {
      return this.resourceResolver.getOutputStream(this.resolveFromBase(uri));
   }

   public URI resolveFromBase(URI uri) {
      return this.baseUri.resolve(uri);
   }

   public static URI cleanURI(String uriStr) throws URISyntaxException {
      if (uriStr == null) {
         return null;
      } else {
         String fixedUri = uriStr.replace('\\', '/');
         fixedUri = fixedUri.replace(" ", "%20");
         URI baseURI = new URI(fixedUri);
         return baseURI;
      }
   }

   public static URI getBaseURI(String base) throws URISyntaxException {
      String path = base + (base.endsWith("/") ? "" : "/");
      return cleanURI(path);
   }

   private InputStream resolveDataURI(String dataURI) {
      try {
         Source src = this.dataSchemeResolver.resolve(dataURI, "");
         return src == null ? null : ((StreamSource)src).getInputStream();
      } catch (TransformerException var3) {
         throw new RuntimeException(var3);
      }
   }
}
