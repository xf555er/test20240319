package server;

import c2profile.Profile;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Iterator;
import java.util.List;

public class ProfileEdits {
   public ProfileEdits(Profile var1) {
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
