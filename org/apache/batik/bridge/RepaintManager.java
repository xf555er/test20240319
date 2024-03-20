package org.apache.batik.bridge;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.apache.batik.ext.awt.geom.RectListManager;
import org.apache.batik.gvt.renderer.ImageRenderer;

public class RepaintManager {
   static final int COPY_OVERHEAD = 10000;
   static final int COPY_LINE_OVERHEAD = 10;
   protected ImageRenderer renderer;

   public RepaintManager(ImageRenderer r) {
      this.renderer = r;
   }

   public Collection updateRendering(Collection areas) throws InterruptedException {
      this.renderer.flush(areas);
      List rects = new ArrayList(areas.size());
      AffineTransform at = this.renderer.getTransform();
      Iterator var4 = areas.iterator();

      while(var4.hasNext()) {
         Object area = var4.next();
         Shape s = (Shape)area;
         s = at.createTransformedShape(s);
         Rectangle2D r2d = s.getBounds2D();
         int x0 = (int)Math.floor(r2d.getX());
         int y0 = (int)Math.floor(r2d.getY());
         int x1 = (int)Math.ceil(r2d.getX() + r2d.getWidth());
         int y1 = (int)Math.ceil(r2d.getY() + r2d.getHeight());
         Rectangle r = new Rectangle(x0 - 1, y0 - 1, x1 - x0 + 3, y1 - y0 + 3);
         rects.add(r);
      }

      RectListManager devRLM = null;

      try {
         devRLM = new RectListManager(rects);
         devRLM.mergeRects(10000, 10);
      } catch (Exception var13) {
         var13.printStackTrace();
      }

      this.renderer.repaint(devRLM);
      return devRLM;
   }

   public void setupRenderer(AffineTransform u2d, boolean dbr, Shape aoi, int width, int height) {
      this.renderer.setTransform(u2d);
      this.renderer.setDoubleBuffered(dbr);
      this.renderer.updateOffScreen(width, height);
      this.renderer.clearOffScreen();
   }

   public BufferedImage getOffScreen() {
      return this.renderer.getOffScreen();
   }
}
