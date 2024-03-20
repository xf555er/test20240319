package org.apache.batik.util.gui;

import java.awt.GridBagConstraints;

public class ExtendedGridBagConstraints extends GridBagConstraints {
   public void setGridBounds(int x, int y, int width, int height) {
      this.gridx = x;
      this.gridy = y;
      this.gridwidth = width;
      this.gridheight = height;
   }

   public void setWeight(double weightx, double weighty) {
      this.weightx = weightx;
      this.weighty = weighty;
   }
}
