package org.apache.commons.io;

import java.time.Duration;

class ThreadMonitor implements Runnable {
   private final Thread thread;
   private final Duration timeout;

   static Thread start(Duration timeout) {
      return start(Thread.currentThread(), timeout);
   }

   static Thread start(Thread thread, Duration timeout) {
      if (!timeout.isZero() && !timeout.isNegative()) {
         ThreadMonitor timout = new ThreadMonitor(thread, timeout);
         Thread monitor = new Thread(timout, ThreadMonitor.class.getSimpleName());
         monitor.setDaemon(true);
         monitor.start();
         return monitor;
      } else {
         return null;
      }
   }

   static void stop(Thread thread) {
      if (thread != null) {
         thread.interrupt();
      }

   }

   private ThreadMonitor(Thread thread, Duration timeout) {
      this.thread = thread;
      this.timeout = timeout;
   }

   public void run() {
      try {
         sleep(this.timeout);
         this.thread.interrupt();
      } catch (InterruptedException var2) {
      }

   }

   private static void sleep(Duration duration) throws InterruptedException {
      long millis = duration.toMillis();
      long finishAtMillis = System.currentTimeMillis() + millis;
      long remainingMillis = millis;

      do {
         Thread.sleep(remainingMillis);
         remainingMillis = finishAtMillis - System.currentTimeMillis();
      } while(remainingMillis > 0L);

   }
}
