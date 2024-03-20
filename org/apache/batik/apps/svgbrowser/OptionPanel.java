package org.apache.batik.apps.svgbrowser;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.apache.batik.util.resources.ResourceManager;

public class OptionPanel extends JPanel {
   public static final String RESOURCES = "org.apache.batik.apps.svgbrowser.resources.GUI";
   protected static ResourceBundle bundle = ResourceBundle.getBundle("org.apache.batik.apps.svgbrowser.resources.GUI", Locale.getDefault());
   protected static ResourceManager resources;

   public OptionPanel(LayoutManager layout) {
      super(layout);
   }

   static {
      resources = new ResourceManager(bundle);
   }

   public static class Dialog extends JDialog {
      protected JButton ok;
      protected JPanel panel;

      public Dialog(Component parent, String title, JPanel panel) {
         super(JOptionPane.getFrameForComponent(parent), title);
         this.setModal(true);
         this.panel = panel;
         this.getContentPane().add(panel, "Center");
         this.getContentPane().add(this.createButtonPanel(), "South");
      }

      protected JPanel createButtonPanel() {
         JPanel panel = new JPanel(new FlowLayout());
         this.ok = new JButton(OptionPanel.resources.getString("OKButton.text"));
         this.ok.addActionListener(new OKButtonAction());
         panel.add(this.ok);
         return panel;
      }

      protected class OKButtonAction extends AbstractAction {
         public void actionPerformed(ActionEvent evt) {
            Dialog.this.dispose();
         }
      }
   }
}
