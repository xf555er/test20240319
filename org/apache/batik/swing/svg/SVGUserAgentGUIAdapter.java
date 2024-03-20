package org.apache.batik.swing.svg;

import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import org.apache.batik.util.gui.JErrorPane;

public class SVGUserAgentGUIAdapter extends SVGUserAgentAdapter {
   public Component parentComponent;

   public SVGUserAgentGUIAdapter(Component parentComponent) {
      this.parentComponent = parentComponent;
   }

   public void displayError(String message) {
      JOptionPane pane = new JOptionPane(message, 0);
      JDialog dialog = pane.createDialog(this.parentComponent, "ERROR");
      dialog.setModal(false);
      dialog.setVisible(true);
   }

   public void displayError(Exception ex) {
      JErrorPane pane = new JErrorPane(ex, 0);
      JDialog dialog = pane.createDialog(this.parentComponent, "ERROR");
      dialog.setModal(false);
      dialog.setVisible(true);
   }

   public void displayMessage(String message) {
   }

   public void showAlert(String message) {
      String str = "Script alert:\n" + message;
      JOptionPane.showMessageDialog(this.parentComponent, str);
   }

   public String showPrompt(String message) {
      String str = "Script prompt:\n" + message;
      return JOptionPane.showInputDialog(this.parentComponent, str);
   }

   public String showPrompt(String message, String defaultValue) {
      String str = "Script prompt:\n" + message;
      return (String)JOptionPane.showInputDialog(this.parentComponent, str, (String)null, -1, (Icon)null, (Object[])null, defaultValue);
   }

   public boolean showConfirm(String message) {
      String str = "Script confirm:\n" + message;
      return JOptionPane.showConfirmDialog(this.parentComponent, str, "Confirm", 0) == 0;
   }
}
