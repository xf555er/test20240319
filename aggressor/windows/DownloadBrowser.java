package aggressor.windows;

import aggressor.AggressorClient;
import aggressor.TranscriptEditor;
import common.AObject;
import common.AdjustData;
import common.Download;
import common.DownloadFiles;
import common.ScriptUtils;
import dialog.ActivityPanel;
import dialog.DialogUtils;
import dialog.SafeDialogCallback;
import dialog.SafeDialogs;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;
import javax.swing.JButton;
import javax.swing.JComponent;
import ui.ATable;
import ui.GenericTableModel;
import ui.TablePopup;

public class DownloadBrowser extends AObject implements AdjustData, ActionListener, SafeDialogCallback, TablePopup {
   protected AggressorClient client = null;
   protected ActivityPanel dialog = null;
   protected TranscriptEditor editor = new TranscriptEditor();
   protected GenericTableModel model = null;
   protected ATable table = null;
   protected String[] cols = new String[]{"host", "name", "path", "size", "date"};

   public DownloadBrowser(AggressorClient var1) {
      this.client = var1;
      var1.getData().populateAndSubscribe("accents", this);
   }

   public ActionListener cleanup() {
      return this.client.getData().unsubOnClose("downloads, accents", this);
   }

   public void actionPerformed(ActionEvent var1) {
      if (!this.model.hasSelectedRows(this.table)) {
         DialogUtils.showError(DialogUtils.MessageID.ROWS_MUST_BE_SELECTED);
      } else {
         SafeDialogs.openFile("Sync downloads to?", (String)null, (String)null, false, true, this);
      }
   }

   public void dialogResult(String var1) {
      if (var1 != null) {
         (new DownloadFiles(this.client.getConnection(), this.model.getSelectedRows(this.table), new File(var1))).startNextDownload();
      }
   }

   public JComponent getContent() {
      LinkedList var1 = this.client.getData().populateAndSubscribe("downloads", this);
      this.model = DialogUtils.setupModel("lpath", this.cols, var1);
      this.dialog = new ActivityPanel();
      this.dialog.setLayout(new BorderLayout());
      this.table = DialogUtils.setupTable(this.model, this.cols, true);
      this.table.setPopupMenu(this);
      this.editor.setTable(this.table, this.model);
      DialogUtils.setupDateRenderer(this.table, "date");
      DialogUtils.setupSizeRenderer(this.table, "size");
      JButton var2 = new JButton("Sync Files");
      JButton var3 = new JButton("Help");
      var2.addActionListener(this);
      var3.addActionListener(DialogUtils.gotoURL("https://www.cobaltstrike.com/help-manage-downloads"));
      this.dialog.add(DialogUtils.FilterAndScroll(this.table), "Center");
      this.dialog.add(DialogUtils.center(var2, var3), "South");
      return this.dialog;
   }

   public Map format(String var1, Object var2) {
      if (this.editor.processTranscriptFormat(var1, var2)) {
         return null;
      } else {
         Download var3 = (Download)var2;
         return this.editor.decorate((Download)var2, var3.toMap());
      }
   }

   public void result(String var1, Object var2) {
      if (!this.editor.processTranscriptResult(var1, var2)) {
         DialogUtils.addToTable(this.table, this.model, this.format(var1, var2));
         this.dialog.touch();
      }
   }

   public void showPopup(MouseEvent var1) {
      Stack var2 = new Stack();
      var2.push(ScriptUtils.convertAll(this.model.getSelectedRows(this.table)));
      this.client.getScriptEngine().getMenuBuilder().installMenu(var1, "downloads", var2);
   }
}
