package org.apache.fop.afp.modca;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import org.apache.commons.io.IOUtils;
import org.apache.fop.afp.util.AFPResourceAccessor;
import org.apache.fop.afp.util.AFPResourceUtil;

public class IncludedResourceObject extends AbstractNamedAFPObject {
   private final AFPResourceAccessor resourceAccessor;
   private URI uri;

   public IncludedResourceObject(String name, AFPResourceAccessor resourceAccessor, URI uri) {
      super(name);
      this.resourceAccessor = resourceAccessor;
      this.uri = uri;
   }

   public void writeToStream(OutputStream os) throws IOException {
      InputStream in = this.resourceAccessor.createInputStream(this.uri);

      try {
         AFPResourceUtil.copyResourceFile(in, os);
      } finally {
         IOUtils.closeQuietly(in);
      }

   }
}
