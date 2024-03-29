package org.apache.commons.io.monitor;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadFactory;

public final class FileAlterationMonitor implements Runnable {
   private static final FileAlterationObserver[] EMPTY_ARRAY = new FileAlterationObserver[0];
   private final long interval;
   private final List observers;
   private Thread thread;
   private ThreadFactory threadFactory;
   private volatile boolean running;

   public FileAlterationMonitor() {
      this(10000L);
   }

   public FileAlterationMonitor(long interval) {
      this.observers = new CopyOnWriteArrayList();
      this.interval = interval;
   }

   public FileAlterationMonitor(long interval, Collection observers) {
      this(interval, (FileAlterationObserver[])((Collection)Optional.ofNullable(observers).orElse(Collections.emptyList())).toArray(EMPTY_ARRAY));
   }

   public FileAlterationMonitor(long interval, FileAlterationObserver... observers) {
      this(interval);
      if (observers != null) {
         FileAlterationObserver[] var4 = observers;
         int var5 = observers.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            FileAlterationObserver observer = var4[var6];
            this.addObserver(observer);
         }
      }

   }

   public long getInterval() {
      return this.interval;
   }

   public synchronized void setThreadFactory(ThreadFactory threadFactory) {
      this.threadFactory = threadFactory;
   }

   public void addObserver(FileAlterationObserver observer) {
      if (observer != null) {
         this.observers.add(observer);
      }

   }

   public void removeObserver(FileAlterationObserver observer) {
      if (observer != null) {
         while(true) {
            if (this.observers.remove(observer)) {
               continue;
            }
         }
      }

   }

   public Iterable getObservers() {
      return this.observers;
   }

   public synchronized void start() throws Exception {
      if (this.running) {
         throw new IllegalStateException("Monitor is already running");
      } else {
         Iterator var1 = this.observers.iterator();

         while(var1.hasNext()) {
            FileAlterationObserver observer = (FileAlterationObserver)var1.next();
            observer.initialize();
         }

         this.running = true;
         if (this.threadFactory != null) {
            this.thread = this.threadFactory.newThread(this);
         } else {
            this.thread = new Thread(this);
         }

         this.thread.start();
      }
   }

   public synchronized void stop() throws Exception {
      this.stop(this.interval);
   }

   public synchronized void stop(long stopInterval) throws Exception {
      if (!this.running) {
         throw new IllegalStateException("Monitor is not running");
      } else {
         this.running = false;

         try {
            this.thread.interrupt();
            this.thread.join(stopInterval);
         } catch (InterruptedException var5) {
            Thread.currentThread().interrupt();
         }

         Iterator var3 = this.observers.iterator();

         while(var3.hasNext()) {
            FileAlterationObserver observer = (FileAlterationObserver)var3.next();
            observer.destroy();
         }

      }
   }

   public void run() {
      while(true) {
         if (this.running) {
            Iterator var1 = this.observers.iterator();

            while(var1.hasNext()) {
               FileAlterationObserver observer = (FileAlterationObserver)var1.next();
               observer.checkAndNotify();
            }

            if (this.running) {
               try {
                  Thread.sleep(this.interval);
               } catch (InterruptedException var3) {
               }
               continue;
            }
         }

         return;
      }
   }
}
