package org.apache.xmlgraphics.io;

import java.io.FilterInputStream;
import java.io.InputStream;

public class Resource extends FilterInputStream {
   private final String type;

   public Resource(String type, InputStream inputStream) {
      super(inputStream);
      this.type = type;
   }

   public Resource(InputStream inputStream) {
      this("unknown", inputStream);
   }

   public String getType() {
      return this.type;
   }
}
