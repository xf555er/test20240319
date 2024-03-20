package net.jsign.poi.util;

public interface POILogger {
   void initialize(String var1);

   void _log(int var1, Object var2);

   void _log(int var1, Object var2, Throwable var3);

   boolean check(int var1);

   default void log(int level, Object... objs) {
      if (this.check(level)) {
         Throwable lastEx = null;
         String msg;
         if (objs.length == 0) {
            msg = "";
         } else if (objs.length == 1) {
            if (objs[0] instanceof Throwable) {
               lastEx = (Throwable)objs[0];
            }

            msg = objs[0].toString();
         } else {
            StringBuilder sb = new StringBuilder(32);

            for(int i = 0; i < objs.length; ++i) {
               if (i == objs.length - 1 && objs[i] instanceof Throwable) {
                  lastEx = (Throwable)objs[i];
               } else {
                  sb.append(objs[i]);
               }
            }

            msg = sb.toString();
         }

         msg = msg.replaceAll("[\r\n]+", " ");
         if (lastEx == null) {
            this._log(level, msg);
         } else {
            this._log(level, msg, lastEx);
         }

      }
   }
}
