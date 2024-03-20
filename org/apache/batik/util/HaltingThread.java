package org.apache.batik.util;

public class HaltingThread extends Thread {
   protected boolean beenHalted = false;

   public HaltingThread() {
   }

   public HaltingThread(Runnable r) {
      super(r);
   }

   public HaltingThread(String name) {
      super(name);
   }

   public HaltingThread(Runnable r, String name) {
      super(r, name);
   }

   public boolean isHalted() {
      synchronized(this) {
         return this.beenHalted;
      }
   }

   public void halt() {
      synchronized(this) {
         this.beenHalted = true;
      }
   }

   public void clearHalted() {
      synchronized(this) {
         this.beenHalted = false;
      }
   }

   public static void haltThread() {
      haltThread(Thread.currentThread());
   }

   public static void haltThread(Thread t) {
      if (t instanceof HaltingThread) {
         ((HaltingThread)t).halt();
      }

   }

   public static boolean hasBeenHalted() {
      return hasBeenHalted(Thread.currentThread());
   }

   public static boolean hasBeenHalted(Thread t) {
      return t instanceof HaltingThread ? ((HaltingThread)t).isHalted() : false;
   }
}
