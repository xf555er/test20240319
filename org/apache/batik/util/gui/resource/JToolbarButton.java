package org.apache.batik.util.gui.resource;

import java.awt.Color;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;

public class JToolbarButton extends JButton {
   public JToolbarButton() {
      this.initialize();
   }

   public JToolbarButton(String txt) {
      super(txt);
      this.initialize();
   }

   protected void initialize() {
      if (!System.getProperty("java.version").startsWith("1.3")) {
         this.setOpaque(false);
         this.setBackground(new Color(0, 0, 0, 0));
      }

      this.setBorderPainted(false);
      this.setMargin(new Insets(2, 2, 2, 2));
      this.addMouseListener(new MouseListener());
   }

   protected class MouseListener extends MouseAdapter {
      public void mouseEntered(MouseEvent ev) {
         JToolbarButton.this.setBorderPainted(true);
      }

      public void mouseExited(MouseEvent ev) {
         JToolbarButton.this.setBorderPainted(false);
      }
   }
}
