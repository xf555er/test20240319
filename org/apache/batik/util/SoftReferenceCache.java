package org.apache.batik.util;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

public class SoftReferenceCache {
   protected final Map map;
   private final boolean synchronous;

   protected SoftReferenceCache() {
      this(false);
   }

   protected SoftReferenceCache(boolean synchronous) {
      this.map = new HashMap();
      this.synchronous = synchronous;
   }

   public synchronized void flush() {
      this.map.clear();
      this.notifyAll();
   }

   protected final synchronized boolean isPresentImpl(Object key) {
      if (!this.map.containsKey(key)) {
         return false;
      } else {
         Object o = this.map.get(key);
         if (o == null) {
            return true;
         } else {
            SoftReference sr = (SoftReference)o;
            o = sr.get();
            if (o != null) {
               return true;
            } else {
               this.clearImpl(key);
               return false;
            }
         }
      }
   }

   protected final synchronized boolean isDoneImpl(Object key) {
      Object o = this.map.get(key);
      if (o == null) {
         return false;
      } else {
         SoftReference sr = (SoftReference)o;
         o = sr.get();
         if (o != null) {
            return true;
         } else {
            this.clearImpl(key);
            return false;
         }
      }
   }

   protected final synchronized Object requestImpl(Object key) {
      if (this.map.containsKey(key)) {
         Object o = this.map.get(key);

         while(true) {
            if (o == null) {
               if (this.synchronous) {
                  return null;
               }

               try {
                  this.wait();
               } catch (InterruptedException var4) {
               }

               if (this.map.containsKey(key)) {
                  o = this.map.get(key);
                  continue;
               }
            }

            if (o != null) {
               SoftReference sr = (SoftReference)o;
               o = sr.get();
               if (o != null) {
                  return o;
               }
            }
            break;
         }
      }

      this.map.put(key, (Object)null);
      return null;
   }

   protected final synchronized void clearImpl(Object key) {
      this.map.remove(key);
      this.notifyAll();
   }

   protected final synchronized void putImpl(Object key, Object object) {
      if (this.map.containsKey(key)) {
         SoftReference ref = new SoftRefKey(object, key);
         this.map.put(key, ref);
         this.notifyAll();
      }

   }

   class SoftRefKey extends CleanerThread.SoftReferenceCleared {
      Object key;

      public SoftRefKey(Object o, Object key) {
         super(o);
         this.key = key;
      }

      public void cleared() {
         SoftReferenceCache cache = SoftReferenceCache.this;
         if (cache != null) {
            synchronized(cache) {
               if (cache.map.containsKey(this.key)) {
                  Object o = cache.map.remove(this.key);
                  if (this == o) {
                     cache.notifyAll();
                  } else {
                     cache.map.put(this.key, o);
                  }

               }
            }
         }
      }
   }
}
