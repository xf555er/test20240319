package org.apache.batik.util.gui.resource;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JComponent;

public class JToolbarSeparator extends JComponent {
   public JToolbarSeparator() {
      this.setMaximumSize(new Dimension(15, Integer.MAX_VALUE));
   }

   protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      Dimension size = this.getSize();
      int pos = size.width / 2;
      g.setColor(Color.gray);
      g.drawLine(pos, 3, pos, size.height - 5);
      g.drawLine(pos, 2, pos + 1, 2);
      g.setColor(Color.white);
      g.drawLine(pos + 1, 3, pos + 1, size.height - 5);
      g.drawLine(pos, size.height - 4, pos + 1, size.height - 4);
   }
}
