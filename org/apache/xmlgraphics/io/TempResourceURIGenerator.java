package org.apache.xmlgraphics.io;

import java.net.URI;
import java.util.concurrent.atomic.AtomicLong;

public final class TempResourceURIGenerator {
   public static final String TMP_SCHEME = "tmp";
   private final String tempURIPrefix;
   private final AtomicLong counter = new AtomicLong();

   public TempResourceURIGenerator(String uriPrefix) {
      this.tempURIPrefix = URI.create("tmp:///" + uriPrefix).toASCIIString();
   }

   public URI generate() {
      return URI.create(this.tempURIPrefix + this.getUniqueId());
   }

   private String getUniqueId() {
      return Long.toHexString(this.counter.getAndIncrement());
   }

   public static boolean isTempURI(URI uri) {
      return "tmp".equals(uri.getScheme());
   }
}
