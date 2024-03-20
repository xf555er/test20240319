package org.apache.batik.ext.awt.image;

import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.util.SoftReferenceCache;

public class URLImageCache extends SoftReferenceCache {
   static URLImageCache theCache = new URLImageCache();

   public static URLImageCache getDefaultCache() {
      return theCache;
   }

   public synchronized boolean isPresent(ParsedURL purl) {
      return super.isPresentImpl(purl);
   }

   public synchronized boolean isDone(ParsedURL purl) {
      return super.isDoneImpl(purl);
   }

   public synchronized Filter request(ParsedURL purl) {
      return (Filter)super.requestImpl(purl);
   }

   public synchronized void clear(ParsedURL purl) {
      super.clearImpl(purl);
   }

   public synchronized void put(ParsedURL purl, Filter filt) {
      super.putImpl(purl, filt);
   }
}
