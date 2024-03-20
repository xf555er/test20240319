package aggressor.dialogs;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import common.ArtifactUtils;
import common.CommonUtils;
import common.DevLog;
import common.ListenerUtils;
import common.MudgeSanity;
import common.ResourceUtils;
import common.ScListener;
import dialog.DialogListener;
import dialog.DialogManager;
import dialog.DialogUtils;
import dialog.SafeDialogCallback;
import dialog.SafeDialogs;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JFrame;

public class WindowsExecutableStageDialog implements DialogListener, SafeDialogCallback {
   protected JFrame dialog = null;
   protected AggressorClient client;
   protected Map options = null;
   protected byte[] stager;

   public WindowsExecutableStageDialog(AggressorClient var1) {
      this.client = var1;
   }

   public void dialogAction(ActionEvent var1, Map var2) {
      this.options = var2;
      String var3 = DialogUtils.string(var2, "stage");
      String var4 = DialogUtils.bool(var2, "x64") ? "x64" : "x86";
      ScListener var5 = ListenerUtils.getListener(this.client, var3);
      if (var5 == null) {
         this.dialog.setVisible(true);
         DialogUtils.showError("A listener was not selected");
      } else {
         DevLog.log(DevLog.STORY.CS0215_TEST_EXPORT, this.getClass(), "dialogAction", "001");
         this.stager = var5.export(this.client, var4);
         if (this.stager.length == 0) {
            this.dialog.setVisible(true);
            DialogUtils.showError("Could not generate " + var4 + " payload for " + var3);
         } else {
            String var6 = var2.get("output") + "";
            String var7 = "";
            if (var6.indexOf("PowerShell") > -1) {
               var7 = "beacon.ps1";
            } else if (var6.indexOf("Raw") > -1) {
               var7 = "beacon.bin";
            } else if (var6.indexOf("EXE") > -1) {
               var7 = "beacon.exe";
            } else if (var6.indexOf("DLL") > -1) {
               var7 = "beacon.dll";
            }

            SafeDialogs.saveFile((JFrame)null, var7, this);
         }
      }
   }

   public void dialogResult(String var1) {
      String var2 = this.options.get("output") + "";
      boolean var3 = DialogUtils.bool(this.options, "x64");
      boolean var4 = DialogUtils.bool(this.options, "sign");
      if (var3) {
         if (var2.equals("Windows EXE")) {
            (new ArtifactUtils(this.client)).patchArtifact(this.stager, "artifact64big.exe", var1);
         } else if (var2.equals("Windows Service EXE")) {
            (new ArtifactUtils(this.client)).patchArtifact(this.stager, "artifact64svcbig.exe", var1);
         } else if (var2.equals("Windows DLL")) {
            (new ArtifactUtils(this.client)).patchArtifact(this.stager, "artifact64big.x64.dll", var1);
         } else if (var2.equals("PowerShell")) {
            (new ResourceUtils(this.client)).buildPowerShell(this.stager, var1, true);
         } else {
            CommonUtils.writeToFile(new File(var1), this.stager);
         }
      } else if (var2.equals("Windows EXE")) {
         (new ArtifactUtils(this.client)).patchArtifact(this.stager, "artifact32big.exe", var1);
      } else if (var2.equals("Windows Service EXE")) {
         (new ArtifactUtils(this.client)).patchArtifact(this.stager, "artifact32svcbig.exe", var1);
      } else if (var2.equals("Windows DLL")) {
         (new ArtifactUtils(this.client)).patchArtifact(this.stager, "artifact32big.dll", var1);
      } else if (var2.equals("PowerShell")) {
         (new ResourceUtils(this.client)).buildPowerShell(this.stager, var1);
      } else {
         CommonUtils.writeToFile(new File(var1), this.stager);
      }

      if (var4) {
         if (!var1.toLowerCase().endsWith(".exe") && !var1.toLowerCase().endsWith(".dll")) {
            DialogUtils.showError("Can only sign EXE and DLL files\nSaved unsigned " + var2 + " to\n" + var1);
            return;
         }

         try {
            DataUtils.getSigner(this.client.getData()).sign(new File(var1));
         } catch (Exception var6) {
            MudgeSanity.logException("Could not sign '" + var1 + "'", var6, false);
            DialogUtils.showError("Could not sign the file\nSaved unsigned " + var2 + " to\n" + var1);
            return;
         }
      }

      DialogUtils.showInfo("Saved " + var2 + " to\n" + var1);
   }

   public void show() {
      this.dialog = DialogUtils.dialog("Windows Executable (Stageless)", 640, 480);
      DialogManager var1 = new DialogManager(this.dialog);
      var1.addDialogListener(this);
      var1.set("output", "Windows EXE");
      var1.sc_listener_all("stage", "Listener:", this.client);
      var1.combobox("output", "Output:", CommonUtils.toArray("PowerShell, Raw, Windows EXE, Windows Service EXE, Windows DLL"));
      var1.set("x64", "true");
      var1.checkbox_add("x64", "x64:", "Use x64 payload");
      var1.checkbox_add("sign", "sign:", "Sign executable file", DataUtils.getSigner(this.client.getData()).available());
      JButton var2 = var1.action("Generate");
      JButton var3 = var1.help("https://www.cobaltstrike.com/help-staged-exe");
      this.dialog.add(DialogUtils.description("Export a stageless Beacon as a Windows executable. Use Cobalt Strike Arsenal scripts (Help -> Arsenal) to customize this process."), "North");
      this.dialog.add(var1.layout(), "Center");
      this.dialog.add(DialogUtils.center(var2, var3), "South");
      this.dialog.pack();
      this.dialog.setVisible(true);
   }
}
