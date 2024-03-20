package org.apache.batik.swing.gvt;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;

public class AbstractZoomInteractor extends InteractorAdapter {
   protected boolean finished = true;
   protected int xStart;
   protected int yStart;
   protected int xCurrent;
   protected int yCurrent;
   protected Line2D markerTop;
   protected Line2D markerLeft;
   protected Line2D markerBottom;
   protected Line2D markerRight;
   protected Overlay overlay = new ZoomOverlay();
   protected BasicStroke markerStroke = new BasicStroke(1.0F, 2, 0, 10.0F, new float[]{4.0F, 4.0F}, 0.0F);

   public boolean endInteraction() {
      return this.finished;
   }

   public void mousePressed(MouseEvent e) {
      if (!this.finished) {
         this.mouseExited(e);
      } else {
         this.finished = false;
         this.markerTop = null;
         this.markerLeft = null;
         this.markerBottom = null;
         this.markerRight = null;
         this.xStart = e.getX();
         this.yStart = e.getY();
         JGVTComponent c = (JGVTComponent)e.getSource();
         c.getOverlays().add(this.overlay);
      }
   }

   public void mouseReleased(MouseEvent e) {
      this.finished = true;
      JGVTComponent c = (JGVTComponent)e.getSource();
      c.getOverlays().remove(this.overlay);
      this.overlay.paint(c.getGraphics());
      this.xCurrent = e.getX();
      this.yCurrent = e.getY();
      if (this.xCurrent - this.xStart != 0 && this.yCurrent - this.yStart != 0) {
         int dx = this.xCurrent - this.xStart;
         int dy = this.yCurrent - this.yStart;
         if (dx < 0) {
            dx = -dx;
            this.xStart = this.xCurrent;
         }

         if (dy < 0) {
            dy = -dy;
            this.yStart = this.yCurrent;
         }

         Dimension size = c.getSize();
         float scaleX = (float)size.width / (float)dx;
         float scaleY = (float)size.height / (float)dy;
         float scale = scaleX < scaleY ? scaleX : scaleY;
         AffineTransform at = new AffineTransform();
         at.scale((double)scale, (double)scale);
         at.translate((double)(-this.xStart), (double)(-this.yStart));
         at.concatenate(c.getRenderingTransform());
         c.setRenderingTransform(at);
      }

   }

   public void mouseExited(MouseEvent e) {
      this.finished = true;
      JGVTComponent c = (JGVTComponent)e.getSource();
      c.getOverlays().remove(this.overlay);
      this.overlay.paint(c.getGraphics());
   }

   public void mouseDragged(MouseEvent e) {
      JGVTComponent c = (JGVTComponent)e.getSource();
      this.overlay.paint(c.getGraphics());
      this.xCurrent = e.getX();
      this.yCurrent = e.getY();
      float xMin;
      float width;
      if (this.xStart < this.xCurrent) {
         xMin = (float)this.xStart;
         width = (float)(this.xCurrent - this.xStart);
      } else {
         xMin = (float)this.xCurrent;
         width = (float)(this.xStart - this.xCurrent);
      }

      float yMin;
      float height;
      if (this.yStart < this.yCurrent) {
         yMin = (float)this.yStart;
         height = (float)(this.yCurrent - this.yStart);
      } else {
         yMin = (float)this.yCurrent;
         height = (float)(this.yStart - this.yCurrent);
      }

      Dimension d = c.getSize();
      float compAR = (float)d.width / (float)d.height;
      if (compAR > width / height) {
         width = compAR * height;
      } else {
         height = width / compAR;
      }

      this.markerTop = new Line2D.Float(xMin, yMin, xMin + width, yMin);
      this.markerLeft = new Line2D.Float(xMin, yMin, xMin, yMin + height);
      this.markerBottom = new Line2D.Float(xMin, yMin + height, xMin + width, yMin + height);
      this.markerRight = new Line2D.Float(xMin + width, yMin, xMin + width, yMin + height);
      this.overlay.paint(c.getGraphics());
   }

   protected class ZoomOverlay implements Overlay {
      public void paint(Graphics g) {
         if (AbstractZoomInteractor.this.markerTop != null) {
            Graphics2D g2d = (Graphics2D)g;
            g2d.setXORMode(Color.white);
            g2d.setColor(Color.black);
            g2d.setStroke(AbstractZoomInteractor.this.markerStroke);
            g2d.draw(AbstractZoomInteractor.this.markerTop);
            g2d.draw(AbstractZoomInteractor.this.markerLeft);
            g2d.draw(AbstractZoomInteractor.this.markerBottom);
            g2d.draw(AbstractZoomInteractor.this.markerRight);
         }

      }
   }
}
