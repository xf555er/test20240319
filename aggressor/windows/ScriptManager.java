package aggressor.windows;

import aggressor.Aggressor;
import aggressor.AggressorClient;
import aggressor.Prefs;
import common.AObject;
import common.CommonUtils;
import common.MudgeSanity;
import cortana.Cortana;
import dialog.DialogUtils;
import dialog.SafeDialogCallback;
import dialog.SafeDialogs;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import sleep.error.YourCodeSucksException;
import ui.ATable;
import ui.GenericTableModel;

public class ScriptManager extends AObject implements ActionListener, SafeDialogCallback {
   protected GenericTableModel model = null;
   protected ATable table = null;
   protected String[] cols = new String[]{"path", "ready"};
   protected AggressorClient client = null;

   public ScriptManager(AggressorClient var1) {
      this.client = var1;
      this.model = DialogUtils.setupModel("path", this.cols, this.toModel());
   }

   public void actionPerformed(ActionEvent var1) {
      if ("Load".equals(var1.getActionCommand())) {
         RuntimeMXBean var2 = ManagementFactory.getRuntimeMXBean();
         List var3 = var2.getInputArguments();
         Iterator var4 = var3.iterator();

         while(var4.hasNext()) {
            String var5 = (String)var4.next();
            if (var5 != null && var5.toLowerCase().contains("-javaagent:")) {
               System.exit(0);
            }
         }

         SafeDialogs.openFile("Load a script", (String)null, (String)null, false, false, this);
      } else {
         String var9;
         Iterator var10;
         Cortana var11;
         if ("Unload".equals(var1.getActionCommand())) {
            if (!this.model.hasSelectedRows(this.table)) {
               DialogUtils.showError(DialogUtils.MessageID.A_ROW_MUST_BE_SELECTED);
               return;
            }

            var9 = this.model.getSelectedValue(this.table) + "";
            var10 = Aggressor.getFrame().getScriptEngines().iterator();

            while(var10.hasNext()) {
               var11 = (Cortana)var10.next();
               var11.unloadScript(var9);
            }

            List var12 = Prefs.getPreferences().getList("cortana.scripts");
            var12.remove(var9);
            Prefs.getPreferences().setList("cortana.scripts", var12);
            Prefs.getPreferences().save();
            this.refresh();
         } else if ("Reload".equals(var1.getActionCommand())) {
            if (!this.model.hasSelectedRows(this.table)) {
               DialogUtils.showError(DialogUtils.MessageID.A_ROW_MUST_BE_SELECTED);
               return;
            }

            var9 = this.model.getSelectedValue(this.table) + "";

            try {
               this.client.getScriptEngine().unloadScript(var9);
               this.client.getScriptEngine().loadScript(var9);
               DialogUtils.showInfo("Reloaded " + var9);
            } catch (YourCodeSucksException var6) {
               MudgeSanity.logException("Load " + var9, var6, true);
               DialogUtils.showError("Could not load " + var9 + ":\n\n" + var6.formatErrors());
            } catch (Exception var7) {
               MudgeSanity.logException("Load " + var9, var7, false);
               DialogUtils.showError("Could not load " + var9 + "\n" + var7.getMessage());
            }

            try {
               var10 = Aggressor.getFrame().getOtherScriptEngines(this.client).iterator();

               while(var10.hasNext()) {
                  var11 = (Cortana)var10.next();
                  var11.unloadScript(var9);
                  var11.loadScript(var9);
               }
            } catch (Exception var8) {
               MudgeSanity.logException("Load " + var9, var8, false);
            }

            this.refresh();
         }
      }

   }

   public void dialogResult(String var1) {
      try {
         this.client.getScriptEngine().loadScript(var1);
         Iterator var2 = Aggressor.getFrame().getOtherScriptEngines(this.client).iterator();

         while(var2.hasNext()) {
            Cortana var3 = (Cortana)var2.next();
            var3.loadScript(var1);
         }

         List var6 = Prefs.getPreferences().getList("cortana.scripts");
         var6.add(var1);
         Prefs.getPreferences().setList("cortana.scripts", var6);
         Prefs.getPreferences().save();
         this.refresh();
      } catch (YourCodeSucksException var4) {
         MudgeSanity.logException("Load " + var1, var4, true);
         DialogUtils.showError("Could not load " + var1 + ":\n\n" + var4.formatErrors());
      } catch (Exception var5) {
         MudgeSanity.logException("Load " + var1, var5, false);
         DialogUtils.showError("Could not load " + var1 + "\n" + var5.getMessage());
      }

   }

   public void refresh() {
      DialogUtils.setTable(this.table, this.model, this.toModel());
   }

   public LinkedList toModel() {
      HashSet var1 = new HashSet(this.client.getScriptEngine().getScripts());
      Iterator var2 = Prefs.getPreferences().getList("cortana.scripts").iterator();
      LinkedList var3 = new LinkedList();

      while(var2.hasNext()) {
         String var4 = (String)var2.next();
         if (var1.contains((new File(var4)).getName())) {
            var3.add(CommonUtils.toMap("path", var4, "ready", "✓"));
         } else {
            var3.add(CommonUtils.toMap("path", var4, "ready", ""));
         }
      }

      return var3;
   }

   public JComponent getContent() {
      JPanel var1 = new JPanel();
      var1.setLayout(new BorderLayout());
      this.table = DialogUtils.setupTable(this.model, this.cols, true);
      DialogUtils.setTableColumnWidths(this.table, DialogUtils.toMap("path: 240, ready: 64"));
      this.table.getColumn("ready").setPreferredWidth(64);
      this.table.getColumn("ready").setMaxWidth(64);
      JButton var2 = new JButton("Load");
      JButton var3 = new JButton("Unload");
      JButton var4 = new JButton("Reload");
      JButton var5 = new JButton("Help");
      var2.addActionListener(this);
      var3.addActionListener(this);
      var4.addActionListener(this);
      var5.addActionListener(DialogUtils.gotoURL("https://www.cobaltstrike.com/help-scripting"));
      var1.add(new JScrollPane(this.table), "Center");
      var1.add(DialogUtils.center(var2, var3, var4, var5), "South");
      return var1;
   }
}
