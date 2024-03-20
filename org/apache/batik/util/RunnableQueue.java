package org.apache.batik.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class RunnableQueue implements Runnable {
   public static final RunnableQueueState RUNNING = new RunnableQueueState("Running");
   public static final RunnableQueueState SUSPENDING = new RunnableQueueState("Suspending");
   public static final RunnableQueueState SUSPENDED = new RunnableQueueState("Suspended");
   protected volatile RunnableQueueState state;
   protected final Object stateLock = new Object();
   protected boolean wasResumed;
   private final DoublyLinkedList list = new DoublyLinkedList();
   protected int preemptCount;
   protected RunHandler runHandler;
   protected volatile HaltingThread runnableQueueThread;
   private IdleRunnable idleRunnable;
   private long idleRunnableWaitTime;
   private static volatile int threadCount;

   public static RunnableQueue createRunnableQueue() {
      RunnableQueue result = new RunnableQueue();
      synchronized(result) {
         HaltingThread ht = new HaltingThread(result, "RunnableQueue-" + threadCount++);
         ht.setDaemon(true);
         ht.start();

         while(result.getThread() == null) {
            try {
               result.wait();
            } catch (InterruptedException var5) {
            }
         }

         return result;
      }
   }

   public void run() {
      synchronized(this) {
         this.runnableQueueThread = (HaltingThread)Thread.currentThread();
         this.notify();
      }

      Link l;
      while(true) {
         boolean var30 = false;

         try {
            var30 = true;
            if (HaltingThread.hasBeenHalted()) {
               var30 = false;
               break;
            }

            boolean callSuspended = false;
            boolean callResumed = false;
            synchronized(this.stateLock) {
               if (this.state != RUNNING) {
                  this.state = SUSPENDED;
                  callSuspended = true;
               }
            }

            if (callSuspended) {
               this.executionSuspended();
            }

            synchronized(this.stateLock) {
               while(this.state != RUNNING) {
                  this.state = SUSPENDED;
                  this.stateLock.notifyAll();

                  try {
                     this.stateLock.wait();
                  } catch (InterruptedException var39) {
                  }
               }

               if (this.wasResumed) {
                  this.wasResumed = false;
                  callResumed = true;
               }
            }

            if (callResumed) {
               this.executionResumed();
            }

            Object rable;
            synchronized(this.list) {
               if (this.state == SUSPENDING) {
                  continue;
               }

               l = (Link)this.list.pop();
               if (this.preemptCount != 0) {
                  --this.preemptCount;
               }

               if (l == null) {
                  if (this.idleRunnable == null || (this.idleRunnableWaitTime = this.idleRunnable.getWaitTime()) >= System.currentTimeMillis()) {
                     try {
                        if (this.idleRunnable != null && this.idleRunnableWaitTime != Long.MAX_VALUE) {
                           long t = this.idleRunnableWaitTime - System.currentTimeMillis();
                           if (t > 0L) {
                              this.list.wait(t);
                           }
                        } else {
                           this.list.wait();
                        }
                     } catch (InterruptedException var41) {
                     }
                     continue;
                  }

                  rable = this.idleRunnable;
               } else {
                  rable = l.runnable;
               }
            }

            try {
               this.runnableStart((Runnable)rable);
               ((Runnable)rable).run();
            } catch (ThreadDeath var37) {
               throw var37;
            } catch (Throwable var38) {
               var38.printStackTrace();
            }

            if (l != null) {
               l.unlock();
            }

            try {
               this.runnableInvoked((Runnable)rable);
            } catch (ThreadDeath var35) {
               throw var35;
            } catch (Throwable var36) {
               var36.printStackTrace();
            }
         } finally {
            if (var30) {
               while(true) {
                  synchronized(this.list) {
                     l = (Link)this.list.pop();
                  }

                  if (l == null) {
                     synchronized(this) {
                        this.runnableQueueThread = null;
                     }
                  }

                  l.unlock();
               }
            }
         }
      }

      while(true) {
         synchronized(this.list) {
            l = (Link)this.list.pop();
         }

         if (l == null) {
            synchronized(this) {
               this.runnableQueueThread = null;
               return;
            }
         }

         l.unlock();
      }
   }

   public HaltingThread getThread() {
      return this.runnableQueueThread;
   }

   public void invokeLater(Runnable r) {
      if (this.runnableQueueThread == null) {
         throw new IllegalStateException("RunnableQueue not started or has exited");
      } else {
         synchronized(this.list) {
            this.list.push(new Link(r));
            this.list.notify();
         }
      }
   }

   public void invokeAndWait(Runnable r) throws InterruptedException {
      if (this.runnableQueueThread == null) {
         throw new IllegalStateException("RunnableQueue not started or has exited");
      } else if (this.runnableQueueThread == Thread.currentThread()) {
         throw new IllegalStateException("Cannot be called from the RunnableQueue thread");
      } else {
         LockableLink l = new LockableLink(r);
         synchronized(this.list) {
            this.list.push(l);
            this.list.notify();
         }

         l.lock();
      }
   }

   public void preemptLater(Runnable r) {
      if (this.runnableQueueThread == null) {
         throw new IllegalStateException("RunnableQueue not started or has exited");
      } else {
         synchronized(this.list) {
            this.list.add(this.preemptCount, new Link(r));
            ++this.preemptCount;
            this.list.notify();
         }
      }
   }

   public void preemptAndWait(Runnable r) throws InterruptedException {
      if (this.runnableQueueThread == null) {
         throw new IllegalStateException("RunnableQueue not started or has exited");
      } else if (this.runnableQueueThread == Thread.currentThread()) {
         throw new IllegalStateException("Cannot be called from the RunnableQueue thread");
      } else {
         LockableLink l = new LockableLink(r);
         synchronized(this.list) {
            this.list.add(this.preemptCount, l);
            ++this.preemptCount;
            this.list.notify();
         }

         l.lock();
      }
   }

   public RunnableQueueState getQueueState() {
      synchronized(this.stateLock) {
         return this.state;
      }
   }

   public void suspendExecution(boolean waitTillSuspended) {
      if (this.runnableQueueThread == null) {
         throw new IllegalStateException("RunnableQueue not started or has exited");
      } else {
         synchronized(this.stateLock) {
            this.wasResumed = false;
            if (this.state == SUSPENDED) {
               this.stateLock.notifyAll();
            } else {
               if (this.state == RUNNING) {
                  this.state = SUSPENDING;
                  synchronized(this.list) {
                     this.list.notify();
                  }
               }

               if (waitTillSuspended) {
                  while(this.state == SUSPENDING) {
                     try {
                        this.stateLock.wait();
                     } catch (InterruptedException var6) {
                     }
                  }
               }

            }
         }
      }
   }

   public void resumeExecution() {
      if (this.runnableQueueThread == null) {
         throw new IllegalStateException("RunnableQueue not started or has exited");
      } else {
         synchronized(this.stateLock) {
            this.wasResumed = true;
            if (this.state != RUNNING) {
               this.state = RUNNING;
               this.stateLock.notifyAll();
            }

         }
      }
   }

   public Object getIteratorLock() {
      return this.list;
   }

   public Iterator iterator() {
      return new Iterator() {
         Link head;
         Link link;

         {
            this.head = (Link)RunnableQueue.this.list.getHead();
         }

         public boolean hasNext() {
            if (this.head == null) {
               return false;
            } else if (this.link == null) {
               return true;
            } else {
               return this.link != this.head;
            }
         }

         public Object next() {
            if (this.head != null && this.head != this.link) {
               if (this.link == null) {
                  this.link = (Link)this.head.getNext();
                  return this.head.runnable;
               } else {
                  Object result = this.link.runnable;
                  this.link = (Link)this.link.getNext();
                  return result;
               }
            } else {
               throw new NoSuchElementException();
            }
         }

         public void remove() {
            throw new UnsupportedOperationException();
         }
      };
   }

   public synchronized void setRunHandler(RunHandler rh) {
      this.runHandler = rh;
   }

   public synchronized RunHandler getRunHandler() {
      return this.runHandler;
   }

   public void setIdleRunnable(IdleRunnable r) {
      synchronized(this.list) {
         this.idleRunnable = r;
         this.idleRunnableWaitTime = 0L;
         this.list.notify();
      }
   }

   protected synchronized void executionSuspended() {
      if (this.runHandler != null) {
         this.runHandler.executionSuspended(this);
      }

   }

   protected synchronized void executionResumed() {
      if (this.runHandler != null) {
         this.runHandler.executionResumed(this);
      }

   }

   protected synchronized void runnableStart(Runnable rable) {
      if (this.runHandler != null) {
         this.runHandler.runnableStart(this, rable);
      }

   }

   protected synchronized void runnableInvoked(Runnable rable) {
      if (this.runHandler != null) {
         this.runHandler.runnableInvoked(this, rable);
      }

   }

   protected static class LockableLink extends Link {
      private volatile boolean locked;

      public LockableLink(Runnable r) {
         super(r);
      }

      public boolean isLocked() {
         return this.locked;
      }

      public synchronized void lock() throws InterruptedException {
         this.locked = true;
         this.notify();
         this.wait();
      }

      public synchronized void unlock() {
         while(!this.locked) {
            try {
               this.wait();
            } catch (InterruptedException var2) {
            }
         }

         this.locked = false;
         this.notify();
      }
   }

   protected static class Link extends DoublyLinkedList.Node {
      private final Runnable runnable;

      public Link(Runnable r) {
         this.runnable = r;
      }

      public void unlock() {
      }
   }

   public static class RunHandlerAdapter implements RunHandler {
      public void runnableStart(RunnableQueue rq, Runnable r) {
      }

      public void runnableInvoked(RunnableQueue rq, Runnable r) {
      }

      public void executionSuspended(RunnableQueue rq) {
      }

      public void executionResumed(RunnableQueue rq) {
      }
   }

   public interface RunHandler {
      void runnableStart(RunnableQueue var1, Runnable var2);

      void runnableInvoked(RunnableQueue var1, Runnable var2);

      void executionSuspended(RunnableQueue var1);

      void executionResumed(RunnableQueue var1);
   }

   public interface IdleRunnable extends Runnable {
      long getWaitTime();
   }

   public static final class RunnableQueueState {
      private final String value;

      private RunnableQueueState(String value) {
         this.value = value;
      }

      public String getValue() {
         return this.value;
      }

      public String toString() {
         return "[RunnableQueueState: " + this.value + ']';
      }

      // $FF: synthetic method
      RunnableQueueState(String x0, Object x1) {
         this(x0);
      }
   }
}
