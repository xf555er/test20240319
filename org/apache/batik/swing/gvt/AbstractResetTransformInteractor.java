package org.apache.batik.swing.gvt;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public abstract class AbstractResetTransformInteractor implements Interactor {
   protected boolean finished = true;

   public boolean endInteraction() {
      return this.finished;
   }

   public void keyTyped(KeyEvent e) {
      this.resetTransform(e);
   }

   public void keyPressed(KeyEvent e) {
      this.resetTransform(e);
   }

   public void keyReleased(KeyEvent e) {
      this.resetTransform(e);
   }

   public void mouseClicked(MouseEvent e) {
      this.resetTransform(e);
   }

   public void mousePressed(MouseEvent e) {
      this.resetTransform(e);
   }

   public void mouseReleased(MouseEvent e) {
      this.resetTransform(e);
   }

   public void mouseEntered(MouseEvent e) {
      this.resetTransform(e);
   }

   public void mouseExited(MouseEvent e) {
      this.resetTransform(e);
   }

   public void mouseDragged(MouseEvent e) {
      this.resetTransform(e);
   }

   public void mouseMoved(MouseEvent e) {
      this.resetTransform(e);
   }

   protected void resetTransform(InputEvent e) {
      JGVTComponent c = (JGVTComponent)e.getSource();
      c.resetRenderingTransform();
      this.finished = true;
   }
}
