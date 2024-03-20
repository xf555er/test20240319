package aggressor.windows;

import common.CommandParser;
import console.Console;
import console.ConsolePopup;
import cortana.ConsoleInterface;
import cortana.Cortana;
import cortana.CortanaPipe;
import cortana.CortanaTabCompletion;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import javax.swing.JTextField;

public class CortanaConsole implements CortanaPipe.CortanaPipeListener, ActionListener, ConsolePopup {
   protected Console console = null;
   protected Cortana engine = null;
   protected ConsoleInterface myinterface = null;

   public CortanaConsole(Cortana var1) {
      RuntimeMXBean var2 = ManagementFactory.getRuntimeMXBean();
      List var3 = var2.getInputArguments();
      Iterator var4 = var3.iterator();

      while(var4.hasNext()) {
         String var5 = (String)var4.next();
         if (var5 != null && var5.toLowerCase().contains("-javaagent:")) {
            System.exit(0);
         }
      }

      this.console = new Console();
      this.console.updatePrompt("\u001faggressor\u000f> ");
      var1.addTextListener(this);
      this.console.getInput().addActionListener(this);
      this.engine = var1;
      this.myinterface = var1.getConsoleInterface();
      new CortanaTabCompletion(this.console, var1);
      this.console.setPopupMenu(this);
   }

   public Console getConsole() {
      return this.console;
   }

   public void showPopup(String var1, MouseEvent var2) {
      this.engine.getMenuBuilder().installMenu(var2, "aggressor", new Stack());
   }

   public void actionPerformed(ActionEvent var1) {
      String var2 = var1.getActionCommand();
      ((JTextField)var1.getSource()).setText("");

      try {
         String var3 = this.console.processHistoryBang(var2);
         if (!var3.isEmpty()) {
            this.console.append("\u001faggressor\u000f> " + var2 + "\n");
            var2 = var3;
         }
      } catch (Exception var5) {
         this.console.append("\u001faggressor\u000f> " + var2 + "\n");
         this.console.append(var5.getMessage() + "\n");
         return;
      }

      this.console.addCommandToHistory(var2);
      this.console.append("\u001faggressor\u000f> " + var2 + "\n");
      if (!"".equals(var2)) {
         CommandParser var6 = new CommandParser(var2);
         if (var6.is("history")) {
            int var4 = var6.popHistoryArgument();
            if (var6.hasError()) {
               this.console.append(var6.error() + "\n");
            } else {
               this.getConsole().showHistory(var4);
            }

            return;
         }

         this.myinterface.processCommand(var2);
      }

   }

   public void read(String var1) {
      this.console.append(var1 + "\n");
   }
}
