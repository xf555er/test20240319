package cortana.gui;

import java.util.Stack;
import sleep.bridges.BridgeUtilities;
import sleep.bridges.SleepClosure;
import sleep.engine.Block;
import sleep.interfaces.Environment;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;
import ui.KeyHandler;

public class KeyBridge implements Loadable, Function, Environment {
   protected ScriptableApplication application;

   public KeyBridge(ScriptableApplication var1) {
      this.application = var1;
   }

   protected void registerKey(String var1, SleepClosure var2) {
      _A var3 = new _A(var2);
      this.application.bindKey(var1, var3);
   }

   public void bindFunction(ScriptInstance var1, String var2, String var3, Block var4) {
      SleepClosure var5 = new SleepClosure(var1, var4);
      this.registerKey(var3, var5);
   }

   public Scalar evaluate(String var1, ScriptInstance var2, Stack var3) {
      String var4 = BridgeUtilities.getString(var3, "");
      if ("&bind".equals(var1)) {
         SleepClosure var5 = BridgeUtilities.getFunction(var3, var2);
         this.registerKey(var4, var5);
      } else if ("&unbind".equals(var1)) {
         this.application.bindKey(var4, (KeyHandler)null);
      }

      return SleepUtils.getEmptyScalar();
   }

   public void scriptLoaded(ScriptInstance var1) {
      var1.getScriptEnvironment().getEnvironment().put("bind", this);
      var1.getScriptEnvironment().getEnvironment().put("&bind", this);
      var1.getScriptEnvironment().getEnvironment().put("&unbind", this);
   }

   public void scriptUnloaded(ScriptInstance var1) {
   }

   private static class _A implements KeyHandler {
      protected SleepClosure L;

      public _A(SleepClosure var1) {
         this.L = var1;
      }

      public void key_pressed(String var1) {
         if (this.L != null && this.L.getOwner().isLoaded()) {
            SleepUtils.runCode((SleepClosure)this.L, var1, (ScriptInstance)null, new Stack());
         } else {
            this.L = null;
         }

      }
   }
}
