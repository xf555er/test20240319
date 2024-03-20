package org.apache.fop.pdf;

import java.io.IOException;

public final class StreamCacheFactory {
   private static StreamCacheFactory memoryInstance = new StreamCacheFactory();

   public static StreamCacheFactory getInstance() {
      return memoryInstance;
   }

   private StreamCacheFactory() {
   }

   public StreamCache createStreamCache() throws IOException {
      return new InMemoryStreamCache();
   }

   public StreamCache createStreamCache(int hintSize) throws IOException {
      return new InMemoryStreamCache(hintSize);
   }
}
