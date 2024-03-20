package org.apache.fop.events;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.xmlgraphics.util.Service;

public final class EventExceptionManager {
   private static final Map EXCEPTION_FACTORIES = new HashMap();

   private EventExceptionManager() {
   }

   public static void throwException(Event event, String exceptionClass) throws Throwable {
      if (exceptionClass != null) {
         ExceptionFactory factory = (ExceptionFactory)EXCEPTION_FACTORIES.get(exceptionClass);
         if (factory != null) {
            throw factory.createException(event);
         } else {
            throw new IllegalArgumentException("No such ExceptionFactory available: " + exceptionClass);
         }
      } else {
         String msg = EventFormatter.format(event);
         Throwable t = null;
         Iterator var4 = event.getParams().values().iterator();

         while(var4.hasNext()) {
            Object o = var4.next();
            if (o instanceof Throwable) {
               t = (Throwable)o;
               break;
            }
         }

         if (t != null) {
            throw new RuntimeException(msg, t);
         } else {
            throw new RuntimeException(msg);
         }
      }
   }

   static {
      Iterator iter = Service.providers(ExceptionFactory.class);

      while(iter.hasNext()) {
         ExceptionFactory factory = (ExceptionFactory)iter.next();
         EXCEPTION_FACTORIES.put(factory.getExceptionClass().getName(), factory);
      }

   }

   public interface ExceptionFactory {
      Throwable createException(Event var1);

      Class getExceptionClass();
   }
}
