package net.jsign.poi.util;

public class NullLogger implements POILogger {
   public void initialize(String cat) {
   }

   public void _log(int level, Object obj1) {
   }

   public void _log(int level, Object obj1, Throwable exception) {
   }

   public void log(int level, Object... objs) {
   }

   public boolean check(int level) {
      return false;
   }
}
