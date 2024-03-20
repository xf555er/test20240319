package org.apache.batik.util.gui;

import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.batik.util.gui.resource.ActionMap;
import org.apache.batik.util.gui.resource.ButtonFactory;
import org.apache.batik.util.gui.resource.MissingListenerException;
import org.apache.batik.util.resources.ResourceManager;

public class UserStyleDialog extends JDialog implements ActionMap {
   public static final int OK_OPTION = 0;
   public static final int CANCEL_OPTION = 1;
   protected static final String RESOURCES = "org.apache.batik.util.gui.resources.UserStyleDialog";
   protected static ResourceBundle bundle = ResourceBundle.getBundle("org.apache.batik.util.gui.resources.UserStyleDialog", Locale.getDefault());
   protected static ResourceManager resources;
   protected Panel panel;
   protected String chosenPath;
   protected int returnCode;
   protected Map listeners = new HashMap();

   public UserStyleDialog(JFrame f) {
      super(f);
      this.setModal(true);
      this.setTitle(resources.getString("Dialog.title"));
      this.listeners.put("OKButtonAction", new OKButtonAction());
      this.listeners.put("CancelButtonAction", new CancelButtonAction());
      this.getContentPane().add(this.panel = new Panel());
      this.getContentPane().add(this.createButtonsPanel(), "South");
      this.pack();
   }

   public int showDialog() {
      this.pack();
      this.setVisible(true);
      return this.returnCode;
   }

   public String getPath() {
      return this.chosenPath;
   }

   public void setPath(String s) {
      this.chosenPath = s;
      this.panel.fileTextField.setText(s);
      this.panel.fileCheckBox.setSelected(true);
   }

   protected JPanel createButtonsPanel() {
      JPanel p = new JPanel(new FlowLayout(2));
      ButtonFactory bf = new ButtonFactory(bundle, this);
      p.add(bf.createJButton("OKButton"));
      p.add(bf.createJButton("CancelButton"));
      return p;
   }

   public Action getAction(String key) throws MissingListenerException {
      return (Action)this.listeners.get(key);
   }

   static {
      resources = new ResourceManager(bundle);
   }

   public static class Panel extends JPanel {
      protected JCheckBox fileCheckBox;
      protected JLabel fileLabel;
      protected JTextField fileTextField;
      protected JButton browseButton;

      public Panel() {
         super(new GridBagLayout());
         this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), UserStyleDialog.resources.getString("Panel.title")));
         ExtendedGridBagConstraints constraints = new ExtendedGridBagConstraints();
         constraints.insets = new Insets(5, 5, 5, 5);
         this.fileCheckBox = new JCheckBox(UserStyleDialog.resources.getString("PanelFileCheckBox.text"));
         this.fileCheckBox.addChangeListener(new FileCheckBoxChangeListener());
         constraints.weightx = 0.0;
         constraints.weighty = 0.0;
         constraints.fill = 2;
         constraints.setGridBounds(0, 2, 3, 1);
         this.add(this.fileCheckBox, constraints);
         this.fileLabel = new JLabel(UserStyleDialog.resources.getString("PanelFileLabel.text"));
         constraints.weightx = 0.0;
         constraints.weighty = 0.0;
         constraints.fill = 2;
         constraints.setGridBounds(0, 3, 3, 1);
         this.add(this.fileLabel, constraints);
         this.fileTextField = new JTextField(30);
         constraints.weightx = 1.0;
         constraints.weighty = 0.0;
         constraints.fill = 2;
         constraints.setGridBounds(0, 4, 2, 1);
         this.add(this.fileTextField, constraints);
         ButtonFactory bf = new ButtonFactory(UserStyleDialog.bundle, (ActionMap)null);
         constraints.weightx = 0.0;
         constraints.weighty = 0.0;
         constraints.fill = 0;
         constraints.anchor = 13;
         constraints.setGridBounds(2, 4, 1, 1);
         this.browseButton = bf.createJButton("PanelFileBrowseButton");
         this.add(this.browseButton, constraints);
         this.browseButton.addActionListener(new FileBrowseButtonAction());
         this.fileLabel.setEnabled(false);
         this.fileTextField.setEnabled(false);
         this.browseButton.setEnabled(false);
      }

      public String getPath() {
         return this.fileCheckBox.isSelected() ? this.fileTextField.getText() : null;
      }

      public void setPath(String s) {
         if (s == null) {
            this.fileTextField.setEnabled(false);
            this.fileCheckBox.setSelected(false);
         } else {
            this.fileTextField.setEnabled(true);
            this.fileTextField.setText(s);
            this.fileCheckBox.setSelected(true);
         }

      }

      protected class FileBrowseButtonAction extends AbstractAction {
         public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser(new File("."));
            fileChooser.setFileHidingEnabled(false);
            int choice = fileChooser.showOpenDialog(Panel.this);
            if (choice == 0) {
               File f = fileChooser.getSelectedFile();

               try {
                  Panel.this.fileTextField.setText(f.getCanonicalPath());
               } catch (IOException var6) {
               }
            }

         }
      }

      protected class FileCheckBoxChangeListener implements ChangeListener {
         public void stateChanged(ChangeEvent e) {
            boolean selected = Panel.this.fileCheckBox.isSelected();
            Panel.this.fileLabel.setEnabled(selected);
            Panel.this.fileTextField.setEnabled(selected);
            Panel.this.browseButton.setEnabled(selected);
         }
      }
   }

   protected class CancelButtonAction extends AbstractAction {
      public void actionPerformed(ActionEvent e) {
         UserStyleDialog.this.returnCode = 1;
         UserStyleDialog.this.dispose();
      }
   }

   protected class OKButtonAction extends AbstractAction {
      public void actionPerformed(ActionEvent e) {
         if (UserStyleDialog.this.panel.fileCheckBox.isSelected()) {
            String path = UserStyleDialog.this.panel.fileTextField.getText();
            if (path.equals("")) {
               JOptionPane.showMessageDialog(UserStyleDialog.this, UserStyleDialog.resources.getString("StyleDialogError.text"), UserStyleDialog.resources.getString("StyleDialogError.title"), 0);
               return;
            }

            File f = new File(path);
            if (f.exists()) {
               if (f.isDirectory()) {
                  path = null;
               } else {
                  path = "file:" + path;
               }
            }

            UserStyleDialog.this.chosenPath = path;
         } else {
            UserStyleDialog.this.chosenPath = null;
         }

         UserStyleDialog.this.returnCode = 0;
         UserStyleDialog.this.dispose();
      }
   }
}
