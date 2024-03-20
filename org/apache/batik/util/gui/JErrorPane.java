package org.apache.batik.util.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import org.apache.batik.util.gui.resource.ActionMap;
import org.apache.batik.util.gui.resource.ButtonFactory;
import org.apache.batik.util.gui.resource.MissingListenerException;
import org.apache.batik.util.resources.ResourceManager;

public class JErrorPane extends JPanel implements ActionMap {
   protected static final String RESOURCES = "org.apache.batik.util.gui.resources.JErrorPane";
   protected static ResourceBundle bundle = ResourceBundle.getBundle("org.apache.batik.util.gui.resources.JErrorPane", Locale.getDefault());
   protected static ResourceManager resources;
   protected String msg;
   protected String stacktrace;
   protected ButtonFactory bf;
   protected JComponent detailsArea;
   protected JButton showDetailButton;
   protected boolean isDetailShown;
   protected JPanel subpanel;
   protected JButton okButton;
   protected Map listeners;

   public JErrorPane(Throwable th, int type) {
      super(new GridBagLayout());
      this.bf = new ButtonFactory(bundle, this);
      this.isDetailShown = false;
      this.listeners = new HashMap();
      this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
      this.listeners.put("ShowDetailButtonAction", new ShowDetailButtonAction());
      this.listeners.put("OKButtonAction", new OKButtonAction());
      this.msg = bundle.getString("Heading.text") + "\n\n" + th.getMessage();
      StringWriter writer = new StringWriter();
      th.printStackTrace(new PrintWriter(writer));
      writer.flush();
      this.stacktrace = writer.toString();
      ExtendedGridBagConstraints constraints = new ExtendedGridBagConstraints();
      JTextArea msgArea = new JTextArea();
      msgArea.setText(this.msg);
      msgArea.setColumns(50);
      msgArea.setFont((new JLabel()).getFont());
      msgArea.setForeground((new JLabel()).getForeground());
      msgArea.setOpaque(false);
      msgArea.setEditable(false);
      msgArea.setLineWrap(true);
      constraints.setWeight(0.0, 0.0);
      constraints.anchor = 17;
      constraints.fill = 0;
      constraints.setGridBounds(0, 0, 1, 1);
      this.add(msgArea, constraints);
      constraints.setWeight(1.0, 0.0);
      constraints.anchor = 10;
      constraints.fill = 2;
      constraints.setGridBounds(0, 1, 1, 1);
      this.add(this.createButtonsPanel(), constraints);
      JTextArea details = new JTextArea();
      msgArea.setColumns(50);
      details.setText(this.stacktrace);
      details.setEditable(false);
      this.detailsArea = new JPanel(new BorderLayout(0, 10));
      this.detailsArea.add(new JSeparator(), "North");
      this.detailsArea.add(new JScrollPane(details), "Center");
      this.subpanel = new JPanel(new BorderLayout());
      constraints.insets = new Insets(10, 4, 4, 4);
      constraints.setWeight(1.0, 1.0);
      constraints.anchor = 10;
      constraints.fill = 1;
      constraints.setGridBounds(0, 2, 1, 1);
      this.add(this.subpanel, constraints);
   }

   public JDialog createDialog(Component owner, String title) {
      JDialog dialog = new JDialog(JOptionPane.getFrameForComponent(owner), title);
      dialog.getContentPane().add(this, "Center");
      dialog.pack();
      dialog.getRootPane().setDefaultButton(this.okButton);
      return dialog;
   }

   protected JPanel createButtonsPanel() {
      JPanel panel = new JPanel(new FlowLayout(2));
      this.showDetailButton = this.bf.createJButton("ShowDetailButton");
      panel.add(this.showDetailButton);
      this.okButton = this.bf.createJButton("OKButton");
      panel.add(this.okButton);
      return panel;
   }

   public Action getAction(String key) throws MissingListenerException {
      return (Action)this.listeners.get(key);
   }

   static {
      resources = new ResourceManager(bundle);
   }

   protected class ShowDetailButtonAction extends AbstractAction {
      public void actionPerformed(ActionEvent evt) {
         if (JErrorPane.this.isDetailShown) {
            JErrorPane.this.subpanel.remove(JErrorPane.this.detailsArea);
            JErrorPane.this.isDetailShown = false;
            JErrorPane.this.showDetailButton.setText(JErrorPane.resources.getString("ShowDetailButton.text"));
         } else {
            JErrorPane.this.subpanel.add(JErrorPane.this.detailsArea, "Center");
            JErrorPane.this.showDetailButton.setText(JErrorPane.resources.getString("ShowDetailButton.text2"));
            JErrorPane.this.isDetailShown = true;
         }

         ((JDialog)JErrorPane.this.getTopLevelAncestor()).pack();
      }
   }

   protected class OKButtonAction extends AbstractAction {
      public void actionPerformed(ActionEvent evt) {
         ((JDialog)JErrorPane.this.getTopLevelAncestor()).dispose();
      }
   }
}
