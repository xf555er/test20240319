package org.apache.fop.afp;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.afp.modca.ResourceGroup;
import org.apache.fop.afp.modca.StreamedResourceGroup;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.xmlgraphics.io.TempResourceURIGenerator;

public class AFPStreamer implements Streamable {
   private static final Log LOG = LogFactory.getLog(AFPStreamer.class);
   private static final String DEFAULT_EXTERNAL_RESOURCE_FILENAME = "resources.afp";
   private static final TempResourceURIGenerator TEMP_URI_GENERATOR = new TempResourceURIGenerator("AFPDataStream_");
   private final Factory factory;
   private final InternalResourceResolver resourceResolver;
   private final Map pathResourceGroupMap = new HashMap();
   private StreamedResourceGroup printFileResourceGroup;
   private URI defaultResourceGroupUri;
   private final URI tempUri;
   private OutputStream tempOutputStream;
   private OutputStream outputStream;
   private DataStream dataStream;

   public AFPStreamer(Factory factory, InternalResourceResolver resourceResolver) {
      this.factory = factory;
      this.resourceResolver = resourceResolver;
      this.tempUri = TEMP_URI_GENERATOR.generate();
      this.defaultResourceGroupUri = URI.create("resources.afp");
   }

   public DataStream createDataStream(AFPPaintingState paintingState) throws IOException {
      this.tempOutputStream = new BufferedOutputStream(this.resourceResolver.getOutputStream(this.tempUri));
      this.dataStream = this.factory.createDataStream(paintingState, this.tempOutputStream);
      return this.dataStream;
   }

   public void setDefaultResourceGroupUri(URI uri) {
      this.defaultResourceGroupUri = uri;
   }

   public ResourceGroup getResourceGroup(AFPResourceLevel level) {
      ResourceGroup resourceGroup = null;
      if (level.isInline()) {
         return null;
      } else {
         if (level.isExternal()) {
            URI uri = level.getExternalURI();
            if (uri == null) {
               LOG.warn("No file path provided for external resource, using default.");
               uri = this.defaultResourceGroupUri;
            }

            resourceGroup = (ResourceGroup)this.pathResourceGroupMap.get(uri);
            if (resourceGroup == null) {
               OutputStream os = null;

               try {
                  os = new BufferedOutputStream(this.resourceResolver.getOutputStream(uri));
               } catch (IOException var9) {
                  LOG.error("Failed to create/open external resource group for uri '" + uri + "'");
               } finally {
                  if (os != null) {
                     resourceGroup = this.factory.createStreamedResourceGroup(os);
                     this.pathResourceGroupMap.put(uri, resourceGroup);
                  }

               }
            }
         } else if (level.isPrintFile()) {
            if (this.printFileResourceGroup == null) {
               this.printFileResourceGroup = this.factory.createStreamedResourceGroup(this.outputStream);
            }

            resourceGroup = this.printFileResourceGroup;
         } else {
            resourceGroup = this.dataStream.getResourceGroup(level);
         }

         return (ResourceGroup)resourceGroup;
      }
   }

   public void close() throws IOException {
      Iterator var1 = this.pathResourceGroupMap.values().iterator();

      while(var1.hasNext()) {
         ResourceGroup resourceGroup = (ResourceGroup)var1.next();

         assert resourceGroup instanceof StreamedResourceGroup;

         ((StreamedResourceGroup)resourceGroup).close();
      }

      if (this.printFileResourceGroup != null) {
         this.printFileResourceGroup.close();
      }

      this.writeToStream(this.outputStream);
      this.outputStream.close();
   }

   public void setOutputStream(OutputStream outputStream) {
      this.outputStream = outputStream;
   }

   public void writeToStream(OutputStream os) throws IOException {
      this.tempOutputStream.close();
      InputStream tempInputStream = this.resourceResolver.getResource(this.tempUri);
      IOUtils.copy((InputStream)tempInputStream, (OutputStream)os);
      tempInputStream.close();
      os.flush();
   }
}
