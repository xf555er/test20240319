package org.apache.batik.gvt;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.apache.batik.util.HaltingThread;

public class ShapeNode extends AbstractGraphicsNode {
   protected Shape shape;
   protected ShapePainter shapePainter;
   private Rectangle2D primitiveBounds;
   private Rectangle2D geometryBounds;
   private Rectangle2D sensitiveBounds;
   private Shape paintedArea;
   private Shape sensitiveArea;

   public void setShape(Shape newShape) {
      this.fireGraphicsNodeChangeStarted();
      this.invalidateGeometryCache();
      this.shape = newShape;
      if (this.shapePainter != null) {
         if (newShape != null) {
            this.shapePainter.setShape(newShape);
         } else {
            this.shapePainter = null;
         }
      }

      this.fireGraphicsNodeChangeCompleted();
   }

   public Shape getShape() {
      return this.shape;
   }

   public void setShapePainter(ShapePainter newShapePainter) {
      if (this.shape != null) {
         this.fireGraphicsNodeChangeStarted();
         this.invalidateGeometryCache();
         this.shapePainter = newShapePainter;
         if (this.shapePainter != null && this.shape != this.shapePainter.getShape()) {
            this.shapePainter.setShape(this.shape);
         }

         this.fireGraphicsNodeChangeCompleted();
      }
   }

   public ShapePainter getShapePainter() {
      return this.shapePainter;
   }

   public void paint(Graphics2D g2d) {
      if (this.isVisible) {
         super.paint(g2d);
      }

   }

   public void primitivePaint(Graphics2D g2d) {
      if (this.shapePainter != null) {
         this.shapePainter.paint(g2d);
      }

   }

   protected void invalidateGeometryCache() {
      super.invalidateGeometryCache();
      this.primitiveBounds = null;
      this.geometryBounds = null;
      this.sensitiveBounds = null;
      this.paintedArea = null;
      this.sensitiveArea = null;
   }

   public void setPointerEventType(int pointerEventType) {
      super.setPointerEventType(pointerEventType);
      this.sensitiveBounds = null;
      this.sensitiveArea = null;
   }

   public boolean contains(Point2D p) {
      switch (this.pointerEventType) {
         case 0:
         case 1:
         case 2:
         case 3:
            if (!this.isVisible) {
               return false;
            }
         case 4:
         case 5:
         case 6:
         case 7:
            Rectangle2D b = this.getSensitiveBounds();
            if (b != null && b.contains(p)) {
               return this.inSensitiveArea(p);
            }

            return false;
         case 8:
         default:
            return false;
      }
   }

   public boolean intersects(Rectangle2D r) {
      Rectangle2D b = this.getBounds();
      if (b == null) {
         return false;
      } else {
         return b.intersects(r) && this.paintedArea != null && this.paintedArea.intersects(r);
      }
   }

   public Rectangle2D getPrimitiveBounds() {
      if (!this.isVisible) {
         return null;
      } else if (this.shape == null) {
         return null;
      } else if (this.primitiveBounds != null) {
         return this.primitiveBounds;
      } else {
         if (this.shapePainter == null) {
            this.primitiveBounds = this.shape.getBounds2D();
         } else {
            this.primitiveBounds = this.shapePainter.getPaintedBounds2D();
         }

         if (HaltingThread.hasBeenHalted()) {
            this.invalidateGeometryCache();
         }

         return this.primitiveBounds;
      }
   }

   public boolean inSensitiveArea(Point2D pt) {
      if (this.shapePainter == null) {
         return false;
      } else {
         ShapePainter strokeShapePainter = null;
         ShapePainter fillShapePainter = null;
         if (this.shapePainter instanceof StrokeShapePainter) {
            strokeShapePainter = this.shapePainter;
         } else if (this.shapePainter instanceof FillShapePainter) {
            fillShapePainter = this.shapePainter;
         } else {
            if (!(this.shapePainter instanceof CompositeShapePainter)) {
               return false;
            }

            CompositeShapePainter cp = (CompositeShapePainter)this.shapePainter;

            for(int i = 0; i < cp.getShapePainterCount(); ++i) {
               ShapePainter sp = cp.getShapePainter(i);
               if (sp instanceof StrokeShapePainter) {
                  strokeShapePainter = sp;
               } else if (sp instanceof FillShapePainter) {
                  fillShapePainter = sp;
               }
            }
         }

         switch (this.pointerEventType) {
            case 0:
            case 4:
               return this.shapePainter.inPaintedArea(pt);
            case 1:
            case 5:
               if (fillShapePainter != null) {
                  return fillShapePainter.inSensitiveArea(pt);
               }
               break;
            case 2:
            case 6:
               if (strokeShapePainter != null) {
                  return strokeShapePainter.inSensitiveArea(pt);
               }
               break;
            case 3:
            case 7:
               return this.shapePainter.inSensitiveArea(pt);
            case 8:
         }

         return false;
      }
   }

   public Rectangle2D getSensitiveBounds() {
      if (this.sensitiveBounds != null) {
         return this.sensitiveBounds;
      } else if (this.shapePainter == null) {
         return null;
      } else {
         ShapePainter strokeShapePainter = null;
         ShapePainter fillShapePainter = null;
         if (this.shapePainter instanceof StrokeShapePainter) {
            strokeShapePainter = this.shapePainter;
         } else if (this.shapePainter instanceof FillShapePainter) {
            fillShapePainter = this.shapePainter;
         } else {
            if (!(this.shapePainter instanceof CompositeShapePainter)) {
               return null;
            }

            CompositeShapePainter cp = (CompositeShapePainter)this.shapePainter;

            for(int i = 0; i < cp.getShapePainterCount(); ++i) {
               ShapePainter sp = cp.getShapePainter(i);
               if (sp instanceof StrokeShapePainter) {
                  strokeShapePainter = sp;
               } else if (sp instanceof FillShapePainter) {
                  fillShapePainter = sp;
               }
            }
         }

         switch (this.pointerEventType) {
            case 0:
            case 4:
               this.sensitiveBounds = this.shapePainter.getPaintedBounds2D();
               break;
            case 1:
            case 5:
               if (fillShapePainter != null) {
                  this.sensitiveBounds = fillShapePainter.getSensitiveBounds2D();
               }
               break;
            case 2:
            case 6:
               if (strokeShapePainter != null) {
                  this.sensitiveBounds = strokeShapePainter.getSensitiveBounds2D();
               }
               break;
            case 3:
            case 7:
               this.sensitiveBounds = this.shapePainter.getSensitiveBounds2D();
            case 8:
         }

         return this.sensitiveBounds;
      }
   }

   public Shape getSensitiveArea() {
      if (this.sensitiveArea != null) {
         return this.sensitiveArea;
      } else if (this.shapePainter == null) {
         return null;
      } else {
         ShapePainter strokeShapePainter = null;
         ShapePainter fillShapePainter = null;
         if (this.shapePainter instanceof StrokeShapePainter) {
            strokeShapePainter = this.shapePainter;
         } else if (this.shapePainter instanceof FillShapePainter) {
            fillShapePainter = this.shapePainter;
         } else {
            if (!(this.shapePainter instanceof CompositeShapePainter)) {
               return null;
            }

            CompositeShapePainter cp = (CompositeShapePainter)this.shapePainter;

            for(int i = 0; i < cp.getShapePainterCount(); ++i) {
               ShapePainter sp = cp.getShapePainter(i);
               if (sp instanceof StrokeShapePainter) {
                  strokeShapePainter = sp;
               } else if (sp instanceof FillShapePainter) {
                  fillShapePainter = sp;
               }
            }
         }

         switch (this.pointerEventType) {
            case 0:
            case 4:
               this.sensitiveArea = this.shapePainter.getPaintedArea();
               break;
            case 1:
            case 5:
               if (fillShapePainter != null) {
                  this.sensitiveArea = fillShapePainter.getSensitiveArea();
               }
               break;
            case 2:
            case 6:
               if (strokeShapePainter != null) {
                  this.sensitiveArea = strokeShapePainter.getSensitiveArea();
               }
               break;
            case 3:
            case 7:
               this.sensitiveArea = this.shapePainter.getSensitiveArea();
            case 8:
         }

         return this.sensitiveArea;
      }
   }

   public Rectangle2D getGeometryBounds() {
      if (this.geometryBounds == null) {
         if (this.shape == null) {
            return null;
         }

         this.geometryBounds = this.normalizeRectangle(this.shape.getBounds2D());
      }

      return this.geometryBounds;
   }

   public Shape getOutline() {
      return this.shape;
   }
}
