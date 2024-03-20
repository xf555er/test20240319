package org.apache.batik.util;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

public class CleanerThread extends Thread {
   static volatile ReferenceQueue queue = null;
   static CleanerThread thread = null;

   public static ReferenceQueue getReferenceQueue() {
      if (queue == null) {
         Class var0 = CleanerThread.class;
         synchronized(CleanerThread.class) {
            queue = new ReferenceQueue();
            thread = new CleanerThread();
         }
      }

      return queue;
   }

   protected CleanerThread() {
      super("Batik CleanerThread");
      this.setDaemon(true);
      this.start();
   }

   public void run() {
      while(true) {
         while(true) {
            try {
               Reference ref;
               try {
                  ref = queue.remove();
               } catch (InterruptedException var3) {
                  continue;
               }

               if (ref instanceof ReferenceCleared) {
                  ReferenceCleared rc = (ReferenceCleared)ref;
                  rc.cleared();
               }
            } catch (ThreadDeath var4) {
               throw var4;
            } catch (Throwable var5) {
               var5.printStackTrace();
            }
         }
      }
   }

   public abstract static class PhantomReferenceCleared extends PhantomReference implements ReferenceCleared {
      public PhantomReferenceCleared(Object o) {
         super(o, CleanerThread.getReferenceQueue());
      }
   }

   public abstract static class WeakReferenceCleared extends WeakReference implements ReferenceCleared {
      public WeakReferenceCleared(Object o) {
         super(o, CleanerThread.getReferenceQueue());
      }
   }

   public abstract static class SoftReferenceCleared extends SoftReference implements ReferenceCleared {
      public SoftReferenceCleared(Object o) {
         super(o, CleanerThread.getReferenceQueue());
      }
   }

   public interface ReferenceCleared {
      void cleared();
   }
}
