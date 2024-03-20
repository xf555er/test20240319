package org.apache.batik.ext.awt.color;

import org.apache.batik.util.SoftReferenceCache;
import org.apache.xmlgraphics.java2d.color.ICCColorSpaceWithIntent;

public class NamedProfileCache extends SoftReferenceCache {
   static NamedProfileCache theCache = new NamedProfileCache();

   public static NamedProfileCache getDefaultCache() {
      return theCache;
   }

   public NamedProfileCache() {
      super(true);
   }

   public synchronized boolean isPresent(String profileName) {
      return super.isPresentImpl(profileName);
   }

   public synchronized boolean isDone(String profileName) {
      return super.isDoneImpl(profileName);
   }

   public synchronized ICCColorSpaceWithIntent request(String profileName) {
      return (ICCColorSpaceWithIntent)super.requestImpl(profileName);
   }

   public synchronized void clear(String profileName) {
      super.clearImpl(profileName);
   }

   public synchronized void put(String profileName, ICCColorSpaceWithIntent bi) {
      super.putImpl(profileName, bi);
   }
}
