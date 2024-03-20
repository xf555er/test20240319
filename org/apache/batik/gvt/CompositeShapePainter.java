package org.apache.batik.gvt;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class CompositeShapePainter implements ShapePainter {
   protected Shape shape;
   protected ShapePainter[] painters;
   protected int count;

   public CompositeShapePainter(Shape shape) {
      if (shape == null) {
         throw new IllegalArgumentException();
      } else {
         this.shape = shape;
      }
   }

   public void addShapePainter(ShapePainter shapePainter) {
      if (shapePainter != null) {
         if (this.shape != shapePainter.getShape()) {
            shapePainter.setShape(this.shape);
         }

         if (this.painters == null) {
            this.painters = new ShapePainter[2];
         }

         if (this.count == this.painters.length) {
            ShapePainter[] newPainters = new ShapePainter[this.count + this.count / 2 + 1];
            System.arraycopy(this.painters, 0, newPainters, 0, this.count);
            this.painters = newPainters;
         }

         this.painters[this.count++] = shapePainter;
      }
   }

   public ShapePainter getShapePainter(int index) {
      return this.painters[index];
   }

   public int getShapePainterCount() {
      return this.count;
   }

   public void paint(Graphics2D g2d) {
      if (this.painters != null) {
         for(int i = 0; i < this.count; ++i) {
            this.painters[i].paint(g2d);
         }
      }

   }

   public Shape getPaintedArea() {
      if (this.painters == null) {
         return null;
      } else {
         Area paintedArea = new Area();

         for(int i = 0; i < this.count; ++i) {
            Shape s = this.painters[i].getPaintedArea();
            if (s != null) {
               paintedArea.add(new Area(s));
            }
         }

         return paintedArea;
      }
   }

   public Rectangle2D getPaintedBounds2D() {
      if (this.painters == null) {
         return null;
      } else {
         Rectangle2D bounds = null;

         for(int i = 0; i < this.count; ++i) {
            Rectangle2D pb = this.painters[i].getPaintedBounds2D();
            if (pb != null) {
               if (bounds == null) {
                  bounds = (Rectangle2D)pb.clone();
               } else {
                  bounds.add(pb);
               }
            }
         }

         return bounds;
      }
   }

   public boolean inPaintedArea(Point2D pt) {
      if (this.painters == null) {
         return false;
      } else {
         for(int i = 0; i < this.count; ++i) {
            if (this.painters[i].inPaintedArea(pt)) {
               return true;
            }
         }

         return false;
      }
   }

   public Shape getSensitiveArea() {
      if (this.painters == null) {
         return null;
      } else {
         Area paintedArea = new Area();

         for(int i = 0; i < this.count; ++i) {
            Shape s = this.painters[i].getSensitiveArea();
            if (s != null) {
               paintedArea.add(new Area(s));
            }
         }

         return paintedArea;
      }
   }

   public Rectangle2D getSensitiveBounds2D() {
      if (this.painters == null) {
         return null;
      } else {
         Rectangle2D bounds = null;

         for(int i = 0; i < this.count; ++i) {
            Rectangle2D pb = this.painters[i].getSensitiveBounds2D();
            if (pb != null) {
               if (bounds == null) {
                  bounds = (Rectangle2D)pb.clone();
               } else {
                  bounds.add(pb);
               }
            }
         }

         return bounds;
      }
   }

   public boolean inSensitiveArea(Point2D pt) {
      if (this.painters == null) {
         return false;
      } else {
         for(int i = 0; i < this.count; ++i) {
            if (this.painters[i].inSensitiveArea(pt)) {
               return true;
            }
         }

         return false;
      }
   }

   public void setShape(Shape shape) {
      if (shape == null) {
         throw new IllegalArgumentException();
      } else {
         if (this.painters != null) {
            for(int i = 0; i < this.count; ++i) {
               this.painters[i].setShape(shape);
            }
         }

         this.shape = shape;
      }
   }

   public Shape getShape() {
      return this.shape;
   }
}
