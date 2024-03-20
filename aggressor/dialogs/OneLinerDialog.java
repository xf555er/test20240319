package aggressor.dialogs;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import beacon.TaskBeacon;
import common.BeaconEntry;
import common.ListenerUtils;
import common.ScListener;
import dialog.DialogListener;
import dialog.DialogManager;
import dialog.DialogUtils;
import java.awt.event.ActionEvent;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JFrame;

public class OneLinerDialog implements DialogListener {
   protected AggressorClient client;
   protected String[] bids;
   protected JFrame dialog = null;

   public OneLinerDialog(AggressorClient var1, String[] var2) {
      this.client = var1;
      this.bids = var2;
   }

   public void dialogAction(ActionEvent var1, Map var2) {
      String var3 = DialogUtils.string(var2, "listener");
      String var4 = DialogUtils.bool(var2, "x64") ? "x64" : "x86";
      ScListener var5 = ListenerUtils.getListener(this.client, var3);
      if (var5 == null) {
         this.dialog.setVisible(true);
         DialogUtils.showError("A listener was not selected");
      } else {
         TaskBeacon var6 = new TaskBeacon(this.client, this.bids);
         var6.input("oneliner " + var4 + " " + var3);

         for(int var7 = 0; var7 < this.bids.length; ++var7) {
            BeaconEntry var8 = DataUtils.getBeacon(this.client.getData(), this.bids[var7]);
            if (var8 != null) {
               var6.log_task(this.bids[var7], "Created PowerShell one-liner to run " + var3 + " (" + var4 + ")", "T1086");
               String var9 = var6.SetupPayloadDownloadCradle(this.bids[var7], var4, var5);
               DialogUtils.startedWebService(var8.title("One-liner for"), var9);
            }
         }

      }
   }

   public void show() {
      this.dialog = DialogUtils.dialog("PowerShell One-liner", 640, 480);
      DialogManager var1 = new DialogManager(this.dialog);
      var1.addDialogListener(this);
      var1.sc_listener_all("listener", "Listener:", this.client);
      var1.set("x64", "true");
      var1.checkbox_add("x64", "x64:", "Use x64 payload");
      JButton var2 = var1.action("Launch");
      JButton var3 = var1.help("https://www.cobaltstrike.com/help-oneliner");
      this.dialog.add(DialogUtils.description("Generate a single use one-liner that runs payload within this session."), "North");
      this.dialog.add(var1.layout(), "Center");
      this.dialog.add(DialogUtils.center(var2, var3), "South");
      this.dialog.pack();
      this.dialog.setVisible(true);
   }
}
