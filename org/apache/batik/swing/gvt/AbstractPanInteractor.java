package org.apache.batik.swing.gvt;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;

public abstract class AbstractPanInteractor extends InteractorAdapter {
   public static final Cursor PAN_CURSOR = new Cursor(13);
   protected boolean finished = true;
   protected int xStart;
   protected int yStart;
   protected int xCurrent;
   protected int yCurrent;
   protected Cursor previousCursor;

   public boolean endInteraction() {
      return this.finished;
   }

   public void mousePressed(MouseEvent e) {
      if (!this.finished) {
         this.mouseExited(e);
      } else {
         this.finished = false;
         this.xStart = e.getX();
         this.yStart = e.getY();
         JGVTComponent c = (JGVTComponent)e.getSource();
         this.previousCursor = c.getCursor();
         c.setCursor(PAN_CURSOR);
      }
   }

   public void mouseReleased(MouseEvent e) {
      if (!this.finished) {
         this.finished = true;
         JGVTComponent c = (JGVTComponent)e.getSource();
         this.xCurrent = e.getX();
         this.yCurrent = e.getY();
         AffineTransform at = AffineTransform.getTranslateInstance((double)(this.xCurrent - this.xStart), (double)(this.yCurrent - this.yStart));
         AffineTransform rt = (AffineTransform)c.getRenderingTransform().clone();
         rt.preConcatenate(at);
         c.setRenderingTransform(rt);
         if (c.getCursor() == PAN_CURSOR) {
            c.setCursor(this.previousCursor);
         }

      }
   }

   public void mouseExited(MouseEvent e) {
      this.finished = true;
      JGVTComponent c = (JGVTComponent)e.getSource();
      c.setPaintingTransform((AffineTransform)null);
      if (c.getCursor() == PAN_CURSOR) {
         c.setCursor(this.previousCursor);
      }

   }

   public void mouseDragged(MouseEvent e) {
      JGVTComponent c = (JGVTComponent)e.getSource();
      this.xCurrent = e.getX();
      this.yCurrent = e.getY();
      AffineTransform at = AffineTransform.getTranslateInstance((double)(this.xCurrent - this.xStart), (double)(this.yCurrent - this.yStart));
      c.setPaintingTransform(at);
   }
}
