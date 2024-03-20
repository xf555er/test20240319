package server;

import common.CommonUtils;
import common.MudgeSanity;
import common.Reply;
import common.Request;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ServerBus implements Runnable {
   protected LinkedList requests = new LinkedList();
   protected Map calls;

   protected _A grabRequest() {
      synchronized(this) {
         return (_A)this.requests.pollFirst();
      }
   }

   protected void addRequest(ManageUser var1, Request var2) {
      synchronized(this) {
         while(this.requests.size() > 100000) {
            this.requests.removeFirst();
         }

         this.requests.add(new _A(var1, var2));
      }
   }

   public ServerBus(Map var1) {
      this.calls = var1;
      (new Thread(this, "server call bus")).start();
   }

   public void run() {
      RuntimeMXBean var1 = ManagementFactory.getRuntimeMXBean();
      List var2 = var1.getInputArguments();
      Iterator var3 = var2.iterator();

      while(var3.hasNext()) {
         String var4 = (String)var3.next();
         if (var4 != null && var4.toLowerCase().contains("-javaagent:")) {
            return;
         }
      }

      try {
         while(true) {
            while(true) {
               _A var7 = this.grabRequest();
               if (var7 != null) {
                  Request var8 = var7.B;
                  if (this.calls.containsKey(var8.getCall())) {
                     ServerHook var5 = (ServerHook)this.calls.get(var8.getCall());
                     var5.call(var8, var7.A);
                  } else if (var7.A != null) {
                     var7.A.write(new Reply("server_error", 0L, var8 + ": unknown call [or bad arguments]"));
                  } else {
                     CommonUtils.print_error("server_error " + var7 + ": unknown call " + var8.getCall() + " [or bad arguments]");
                  }

                  Thread.yield();
               } else {
                  Thread.sleep(25L);
               }
            }
         }
      } catch (Exception var6) {
         MudgeSanity.logException("server call bus loop", var6, false);
      }
   }

   private static class _A {
      public ManageUser A;
      public Request B;

      public _A(ManageUser var1, Request var2) {
         this.A = var1;
         this.B = var2;
      }
   }
}
