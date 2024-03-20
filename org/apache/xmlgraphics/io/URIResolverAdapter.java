package org.apache.xmlgraphics.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

public class URIResolverAdapter implements ResourceResolver {
   private final URIResolver resolver;

   public URIResolverAdapter(URIResolver resolver) {
      this.resolver = resolver;
   }

   public Resource getResource(URI uri) throws IOException {
      try {
         Source src = this.resolver.resolve(uri.toASCIIString(), (String)null);
         InputStream resourceStream = XmlSourceUtil.getInputStream(src);
         if (resourceStream == null) {
            URL url = new URL(src.getSystemId());
            resourceStream = url.openStream();
         }

         return new Resource(resourceStream);
      } catch (TransformerException var5) {
         throw new IOException(var5.getMessage());
      }
   }

   public OutputStream getOutputStream(URI uri) throws IOException {
      try {
         Source src = this.resolver.resolve(uri.toASCIIString(), (String)null);
         return (new URL(src.getSystemId())).openConnection().getOutputStream();
      } catch (TransformerException var3) {
         throw new IOException(var3.getMessage());
      }
   }
}
