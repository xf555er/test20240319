package aggressor.browsers;

import aggressor.Aggressor;
import common.Callback;
import common.CommonUtils;
import common.TeamQueue;
import common.TeamSocket;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JLabel;
import ssl.ArmitageTrustListener;
import ssl.SecureSocket;

public class TestConnection implements Runnable, Callback, ArmitageTrustListener {
   private volatile boolean ć = true;
   private final String Č;
   private final String Ć;
   private final String Ċ;
   private final String Ĉ;
   private final JLabel ĉ;
   private final JButton ą;
   private final int ă = 10;
   private int ċ = 10;
   private TeamQueue Ą = null;

   public TestConnection(Map var1, JLabel var2, JButton var3) {
      this.Č = (String)var1.get("host");
      this.Ć = (String)var1.get("port");
      this.Ċ = (String)var1.get("pass");
      this.Ĉ = (String)var1.get("user");
      this.ĉ = var2;
      this.ą = var3;
   }

   public void run() {
      while(this.ć) {
         try {
            this.ĉ.setText("Retrying");
            this.ĉ.getParent().validate();
            SecureSocket var1 = new SecureSocket(this.Č, Integer.parseInt(this.Ć), this);
            this.ĉ.setText("Reconnecting");
            this.ĉ.getParent().validate();
            var1.authenticate(this.Ċ);
            this.Ą = new TeamQueue(new TeamSocket(var1.getSocket()));
            this.Ą.call("aggressor.authenticate", CommonUtils.args(this.Ĉ, this.Ċ, Aggressor.VERSION), this);
            return;
         } catch (Exception var3) {
            for(; this.ċ > 0; --this.ċ) {
               try {
                  this.ĉ.setText("Server unavailable, retry in " + this.ċ + " seconds");
                  this.ĉ.getParent().validate();
                  Thread.sleep(1000L);
               } catch (InterruptedException var2) {
               }
            }

            this.ċ = 10;
         }
      }

   }

   public void stop() {
      this.ć = false;
   }

   public boolean trust(String var1) {
      return true;
   }

   public void result(String var1, Object var2) {
      this.Ą.close();
      this.stop();
      CommonUtils.runSafe(new Runnable() {
         public void run() {
            TestConnection.this.ą.doClick();
         }
      });
   }
}
