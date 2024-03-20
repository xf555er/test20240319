package org.apache.batik.ext.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.io.Serializable;

class AffineTransformTracker implements ActionListener, Serializable {
   JAffineTransformChooser chooser;
   AffineTransform txf;

   public AffineTransformTracker(JAffineTransformChooser c) {
      this.chooser = c;
   }

   public void actionPerformed(ActionEvent e) {
      this.txf = this.chooser.getAffineTransform();
   }

   public AffineTransform getAffineTransform() {
      return this.txf;
   }

   public void reset() {
      this.txf = null;
   }
}
