package org.apache.batik.swing.gvt;

import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;

public class AbstractImageZoomInteractor extends InteractorAdapter {
   protected boolean finished = true;
   protected int xStart;
   protected int yStart;
   protected int xCurrent;
   protected int yCurrent;

   public boolean endInteraction() {
      return this.finished;
   }

   public void mousePressed(MouseEvent e) {
      if (!this.finished) {
         JGVTComponent c = (JGVTComponent)e.getSource();
         c.setPaintingTransform((AffineTransform)null);
      } else {
         this.finished = false;
         this.xStart = e.getX();
         this.yStart = e.getY();
      }
   }

   public void mouseReleased(MouseEvent e) {
      this.finished = true;
      JGVTComponent c = (JGVTComponent)e.getSource();
      AffineTransform pt = c.getPaintingTransform();
      if (pt != null) {
         AffineTransform rt = (AffineTransform)c.getRenderingTransform().clone();
         rt.preConcatenate(pt);
         c.setRenderingTransform(rt);
      }

   }

   public void mouseDragged(MouseEvent e) {
      JGVTComponent c = (JGVTComponent)e.getSource();
      this.xCurrent = e.getX();
      this.yCurrent = e.getY();
      AffineTransform at = AffineTransform.getTranslateInstance((double)this.xStart, (double)this.yStart);
      int dy = this.yCurrent - this.yStart;
      double s;
      if (dy < 0) {
         dy -= 10;
         s = dy > -15 ? 1.0 : -15.0 / (double)dy;
      } else {
         dy += 10;
         s = dy < 15 ? 1.0 : (double)dy / 15.0;
      }

      at.scale(s, s);
      at.translate((double)(-this.xStart), (double)(-this.yStart));
      c.setPaintingTransform(at);
   }
}
