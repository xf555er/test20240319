package org.apache.batik.swing.gvt;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;

public class AbstractRotateInteractor extends InteractorAdapter {
   protected boolean finished;
   protected double initialRotation;

   public boolean endInteraction() {
      return this.finished;
   }

   public void mousePressed(MouseEvent e) {
      this.finished = false;
      JGVTComponent c = (JGVTComponent)e.getSource();
      Dimension d = c.getSize();
      double dx = (double)(e.getX() - d.width / 2);
      double dy = (double)(e.getY() - d.height / 2);
      double cos = -dy / Math.sqrt(dx * dx + dy * dy);
      this.initialRotation = dx > 0.0 ? Math.acos(cos) : -Math.acos(cos);
   }

   public void mouseReleased(MouseEvent e) {
      this.finished = true;
      JGVTComponent c = (JGVTComponent)e.getSource();
      AffineTransform at = this.rotateTransform(c.getSize(), e.getX(), e.getY());
      at.concatenate(c.getRenderingTransform());
      c.setRenderingTransform(at);
   }

   public void mouseExited(MouseEvent e) {
      this.finished = true;
      JGVTComponent c = (JGVTComponent)e.getSource();
      c.setPaintingTransform((AffineTransform)null);
   }

   public void mouseDragged(MouseEvent e) {
      JGVTComponent c = (JGVTComponent)e.getSource();
      c.setPaintingTransform(this.rotateTransform(c.getSize(), e.getX(), e.getY()));
   }

   protected AffineTransform rotateTransform(Dimension d, int x, int y) {
      double dx = (double)(x - d.width / 2);
      double dy = (double)(y - d.height / 2);
      double cos = -dy / Math.sqrt(dx * dx + dy * dy);
      double angle = dx > 0.0 ? Math.acos(cos) : -Math.acos(cos);
      angle -= this.initialRotation;
      return AffineTransform.getRotateInstance(angle, (double)(d.width / 2), (double)(d.height / 2));
   }
}
