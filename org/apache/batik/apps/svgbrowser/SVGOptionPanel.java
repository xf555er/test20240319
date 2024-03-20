package org.apache.batik.apps.svgbrowser;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JCheckBox;
import javax.swing.JLabel;

public class SVGOptionPanel extends OptionPanel {
   protected JCheckBox xmlbaseCB;
   protected JCheckBox prettyPrintCB;

   public SVGOptionPanel() {
      super(new BorderLayout());
      this.add(new JLabel(resources.getString("SVGOptionPanel.label")), "North");
      this.xmlbaseCB = new JCheckBox(resources.getString("SVGOptionPanel.UseXMLBase"));
      this.xmlbaseCB.setSelected(resources.getBoolean("SVGOptionPanel.UseXMLBaseDefault"));
      this.add(this.xmlbaseCB, "Center");
      this.prettyPrintCB = new JCheckBox(resources.getString("SVGOptionPanel.PrettyPrint"));
      this.prettyPrintCB.setSelected(resources.getBoolean("SVGOptionPanel.PrettyPrintDefault"));
      this.add(this.prettyPrintCB, "South");
   }

   public boolean getUseXMLBase() {
      return this.xmlbaseCB.isSelected();
   }

   public boolean getPrettyPrint() {
      return this.prettyPrintCB.isSelected();
   }

   public static SVGOptionPanel showDialog(Component parent) {
      String title = resources.getString("SVGOptionPanel.dialog.title");
      SVGOptionPanel panel = new SVGOptionPanel();
      OptionPanel.Dialog dialog = new OptionPanel.Dialog(parent, title, panel);
      dialog.pack();
      dialog.setVisible(true);
      return panel;
   }
}
