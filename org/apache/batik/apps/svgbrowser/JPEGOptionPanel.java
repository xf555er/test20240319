package org.apache.batik.apps.svgbrowser;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Hashtable;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JSlider;
import org.apache.batik.util.gui.ExtendedGridBagConstraints;

public class JPEGOptionPanel extends OptionPanel {
   protected JSlider quality;

   public JPEGOptionPanel() {
      super(new GridBagLayout());
      ExtendedGridBagConstraints constraints = new ExtendedGridBagConstraints();
      constraints.insets = new Insets(5, 5, 5, 5);
      constraints.weightx = 0.0;
      constraints.weighty = 0.0;
      constraints.fill = 0;
      constraints.setGridBounds(0, 0, 1, 1);
      this.add(new JLabel(resources.getString("JPEGOptionPanel.label")), constraints);
      this.quality = new JSlider();
      this.quality.setMinimum(0);
      this.quality.setMaximum(100);
      this.quality.setMajorTickSpacing(10);
      this.quality.setMinorTickSpacing(5);
      this.quality.setPaintTicks(true);
      this.quality.setPaintLabels(true);
      this.quality.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
      Hashtable labels = new Hashtable();

      for(int i = 0; i < 100; i += 10) {
         labels.put(i, new JLabel("0." + i / 10));
      }

      labels.put(100, new JLabel("1"));
      this.quality.setLabelTable(labels);
      Dimension dim = this.quality.getPreferredSize();
      this.quality.setPreferredSize(new Dimension(350, dim.height));
      constraints.weightx = 1.0;
      constraints.fill = 2;
      constraints.setGridBounds(1, 0, 1, 1);
      this.add(this.quality, constraints);
   }

   public float getQuality() {
      return (float)this.quality.getValue() / 100.0F;
   }

   public static float showDialog(Component parent) {
      String title = resources.getString("JPEGOptionPanel.dialog.title");
      JPEGOptionPanel panel = new JPEGOptionPanel();
      OptionPanel.Dialog dialog = new OptionPanel.Dialog(parent, title, panel);
      dialog.pack();
      dialog.setVisible(true);
      return panel.getQuality();
   }
}
