package net.jsign.poi.util;

import java.util.HashMap;
import java.util.Map;

public final class POILogFactory {
   private static final Map _loggers = new HashMap();
   private static final POILogger _nullLogger = new NullLogger();
   static String _loggerClassName;

   public static POILogger getLogger(Class theclass) {
      return getLogger(theclass.getName());
   }

   public static POILogger getLogger(String cat) {
      if (_loggerClassName == null) {
         try {
            _loggerClassName = System.getProperty("net.jsign.poi.util.POILogger");
         } catch (Exception var4) {
         }

         if (_loggerClassName == null) {
            _loggerClassName = _nullLogger.getClass().getName();
         }
      }

      if (_loggerClassName.equals(_nullLogger.getClass().getName())) {
         return _nullLogger;
      } else {
         POILogger logger = (POILogger)_loggers.get(cat);
         if (logger == null) {
            try {
               Class loggerClass = Class.forName(_loggerClassName);
               logger = (POILogger)loggerClass.getConstructor().newInstance();
               logger.initialize(cat);
            } catch (Exception var3) {
               logger = _nullLogger;
               _loggerClassName = _nullLogger.getClass().getName();
            }

            _loggers.put(cat, logger);
         }

         return logger;
      }
   }
}
