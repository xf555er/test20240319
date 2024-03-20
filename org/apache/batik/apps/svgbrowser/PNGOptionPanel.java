package org.apache.batik.apps.svgbrowser;

import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import org.apache.batik.util.gui.ExtendedGridBagConstraints;

public class PNGOptionPanel extends OptionPanel {
   protected JCheckBox check;

   public PNGOptionPanel() {
      super(new GridBagLayout());
      ExtendedGridBagConstraints constraints = new ExtendedGridBagConstraints();
      constraints.insets = new Insets(5, 5, 5, 5);
      constraints.weightx = 0.0;
      constraints.weighty = 0.0;
      constraints.fill = 0;
      constraints.setGridBounds(0, 0, 1, 1);
      this.add(new JLabel(resources.getString("PNGOptionPanel.label")), constraints);
      this.check = new JCheckBox();
      constraints.weightx = 1.0;
      constraints.fill = 2;
      constraints.setGridBounds(1, 0, 1, 1);
      this.add(this.check, constraints);
   }

   public boolean isIndexed() {
      return this.check.isSelected();
   }

   public static boolean showDialog(Component parent) {
      String title = resources.getString("PNGOptionPanel.dialog.title");
      PNGOptionPanel panel = new PNGOptionPanel();
      OptionPanel.Dialog dialog = new OptionPanel.Dialog(parent, title, panel);
      dialog.pack();
      dialog.setVisible(true);
      return panel.isIndexed();
   }
}
