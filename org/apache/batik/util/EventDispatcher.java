package org.apache.batik.util;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class EventDispatcher {
   public static void fireEvent(final Dispatcher dispatcher, final List listeners, final Object evt, final boolean useEventQueue) {
      if (useEventQueue && !EventQueue.isDispatchThread()) {
         Runnable r = new Runnable() {
            public void run() {
               EventDispatcher.fireEvent(dispatcher, listeners, evt, useEventQueue);
            }
         };

         try {
            EventQueue.invokeAndWait(r);
         } catch (InvocationTargetException var9) {
            var9.printStackTrace();
         } catch (InterruptedException var10) {
         } catch (ThreadDeath var11) {
            throw var11;
         } catch (Throwable var12) {
            var12.printStackTrace();
         }

      } else {
         Object[] ll = null;
         Throwable err = null;
         int retryCount = 10;

         while(true) {
            --retryCount;
            if (retryCount == 0) {
               break;
            }

            try {
               synchronized(listeners) {
                  if (listeners.size() == 0) {
                     return;
                  }

                  ll = listeners.toArray();
                  break;
               }
            } catch (Throwable var14) {
               err = var14;
            }
         }

         if (ll == null) {
            if (err != null) {
               err.printStackTrace();
            }

         } else {
            dispatchEvent(dispatcher, ll, evt);
         }
      }
   }

   protected static void dispatchEvent(Dispatcher dispatcher, Object[] ll, Object evt) {
      ThreadDeath td = null;

      try {
         for(int i = 0; i < ll.length; ++i) {
            try {
               Object l;
               synchronized(ll) {
                  l = ll[i];
                  if (l == null) {
                     continue;
                  }

                  ll[i] = null;
               }

               dispatcher.dispatch(l, evt);
            } catch (ThreadDeath var9) {
               td = var9;
            } catch (Throwable var10) {
               var10.printStackTrace();
            }
         }
      } catch (ThreadDeath var11) {
         td = var11;
      } catch (Throwable var12) {
         if (ll[ll.length - 1] != null) {
            dispatchEvent(dispatcher, ll, evt);
         }

         var12.printStackTrace();
      }

      if (td != null) {
         throw td;
      }
   }

   public interface Dispatcher {
      void dispatch(Object var1, Object var2);
   }
}
