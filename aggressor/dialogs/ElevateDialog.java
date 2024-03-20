package aggressor.dialogs;

import aggressor.AggressorClient;
import beacon.TaskBeacon;
import common.ListenerUtils;
import common.ScListener;
import dialog.DialogListener;
import dialog.DialogManager;
import dialog.DialogUtils;
import java.awt.event.ActionEvent;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JFrame;

public class ElevateDialog implements DialogListener {
   protected AggressorClient client;
   protected String[] bids;
   protected JFrame dialog = null;

   public ElevateDialog(AggressorClient var1, String[] var2) {
      this.client = var1;
      this.bids = var2;
   }

   public void dialogAction(ActionEvent var1, Map var2) {
      String var3 = DialogUtils.string(var2, "exploit");
      String var4 = DialogUtils.string(var2, "listener");
      ScListener var5 = ListenerUtils.getListener(this.client, var4);
      if (var5 == null) {
         this.dialog.setVisible(true);
         DialogUtils.showError("A listener was not selected");
      } else {
         TaskBeacon var6 = new TaskBeacon(this.client, this.bids);
         if (this.bids.length == 1) {
            DialogUtils.openOrActivate(this.client, this.bids[0]);
         }

         var6.input("elevate " + var3 + " " + var4);
         var6.Elevate(var3, var4);
      }
   }

   public void show() {
      this.dialog = DialogUtils.dialog("Elevate", 640, 480);
      DialogManager var1 = new DialogManager(this.dialog);
      var1.addDialogListener(this);
      var1.sc_listener_all("listener", "Listener:", this.client);
      var1.exploits("exploit", "Exploit:", this.client);
      JButton var2 = var1.action("Launch");
      JButton var3 = var1.help("https://www.cobaltstrike.com/help-elevate");
      this.dialog.add(DialogUtils.description("Attempt to execute a listener in an elevated context."), "North");
      this.dialog.add(var1.layout(), "Center");
      this.dialog.add(DialogUtils.center(var2, var3), "South");
      this.dialog.pack();
      this.dialog.setVisible(true);
   }
}
