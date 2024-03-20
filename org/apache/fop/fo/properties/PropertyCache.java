package org.apache.fop.fo.properties;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class PropertyCache {
   private static final Log LOG = LogFactory.getLog(PropertyCache.class);
   private final boolean useCache;
   private final ConcurrentMap map;
   private final AtomicInteger putCounter;
   private final Lock cleanupLock;
   private final AtomicInteger hashCodeCollisionCounter;

   public PropertyCache() {
      boolean useCache;
      try {
         useCache = Boolean.valueOf(System.getProperty("org.apache.fop.fo.properties.use-cache", "true"));
      } catch (SecurityException var3) {
         useCache = true;
         LOG.info("Unable to access org.apache.fop.fo.properties.use-cache due to security restriction; defaulting to 'true'.");
      }

      if (useCache) {
         this.map = new ConcurrentHashMap();
         this.putCounter = new AtomicInteger();
         this.cleanupLock = new ReentrantLock();
         this.hashCodeCollisionCounter = new AtomicInteger();
      } else {
         this.map = null;
         this.putCounter = null;
         this.cleanupLock = null;
         this.hashCodeCollisionCounter = null;
      }

      this.useCache = useCache;
   }

   public Object fetch(Object obj) {
      if (!this.useCache) {
         return obj;
      } else if (obj == null) {
         return null;
      } else {
         Integer hashCode = obj.hashCode();
         WeakReference weakRef = (WeakReference)this.map.get(hashCode);
         if (weakRef == null) {
            weakRef = (WeakReference)this.map.putIfAbsent(hashCode, new WeakReference(obj));
            this.attemptCleanup();
            if (weakRef == null) {
               return obj;
            }
         }

         Object cached = weakRef.get();
         if (cached != null) {
            if (this.eq(cached, obj)) {
               return cached;
            }

            if (this.hashCodeCollisionCounter.incrementAndGet() % 10 == 0) {
               LOG.info(this.hashCodeCollisionCounter.get() + " hashCode() collisions for " + obj.getClass().getName());
            }
         }

         this.map.put(hashCode, new WeakReference(obj));
         this.attemptCleanup();
         return obj;
      }
   }

   private void attemptCleanup() {
      if (this.putCounter.incrementAndGet() % 10000 == 0) {
         if (this.cleanupLock.tryLock()) {
            try {
               this.cleanReclaimedMapEntries();
            } finally {
               this.cleanupLock.unlock();
            }
         }

      }
   }

   private void cleanReclaimedMapEntries() {
      Iterator iterator = this.map.entrySet().iterator();

      while(iterator.hasNext()) {
         Map.Entry entry = (Map.Entry)iterator.next();
         WeakReference weakRef = (WeakReference)entry.getValue();
         Object r = weakRef.get();
         if (r == null) {
            iterator.remove();
         }
      }

   }

   private boolean eq(Object p, Object q) {
      return p == q || p.equals(q);
   }
}
