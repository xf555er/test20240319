package org.apache.batik.ext.awt.geom;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

public class Polygon2D implements Shape, Cloneable, Serializable {
   public int npoints;
   public float[] xpoints;
   public float[] ypoints;
   protected Rectangle2D bounds;
   private GeneralPath path;
   private GeneralPath closedPath;

   public Polygon2D() {
      this.xpoints = new float[4];
      this.ypoints = new float[4];
   }

   public Polygon2D(Rectangle2D rec) {
      if (rec == null) {
         throw new IndexOutOfBoundsException("null Rectangle");
      } else {
         this.npoints = 4;
         this.xpoints = new float[4];
         this.ypoints = new float[4];
         this.xpoints[0] = (float)rec.getMinX();
         this.ypoints[0] = (float)rec.getMinY();
         this.xpoints[1] = (float)rec.getMaxX();
         this.ypoints[1] = (float)rec.getMinY();
         this.xpoints[2] = (float)rec.getMaxX();
         this.ypoints[2] = (float)rec.getMaxY();
         this.xpoints[3] = (float)rec.getMinX();
         this.ypoints[3] = (float)rec.getMaxY();
         this.calculatePath();
      }
   }

   public Polygon2D(Polygon pol) {
      if (pol == null) {
         throw new IndexOutOfBoundsException("null Polygon");
      } else {
         this.npoints = pol.npoints;
         this.xpoints = new float[pol.npoints];
         this.ypoints = new float[pol.npoints];

         for(int i = 0; i < pol.npoints; ++i) {
            this.xpoints[i] = (float)pol.xpoints[i];
            this.ypoints[i] = (float)pol.ypoints[i];
         }

         this.calculatePath();
      }
   }

   public Polygon2D(float[] xpoints, float[] ypoints, int npoints) {
      if (npoints <= xpoints.length && npoints <= ypoints.length) {
         this.npoints = npoints;
         this.xpoints = new float[npoints];
         this.ypoints = new float[npoints];
         System.arraycopy(xpoints, 0, this.xpoints, 0, npoints);
         System.arraycopy(ypoints, 0, this.ypoints, 0, npoints);
         this.calculatePath();
      } else {
         throw new IndexOutOfBoundsException("npoints > xpoints.length || npoints > ypoints.length");
      }
   }

   public Polygon2D(int[] xpoints, int[] ypoints, int npoints) {
      if (npoints <= xpoints.length && npoints <= ypoints.length) {
         this.npoints = npoints;
         this.xpoints = new float[npoints];
         this.ypoints = new float[npoints];

         for(int i = 0; i < npoints; ++i) {
            this.xpoints[i] = (float)xpoints[i];
            this.ypoints[i] = (float)ypoints[i];
         }

         this.calculatePath();
      } else {
         throw new IndexOutOfBoundsException("npoints > xpoints.length || npoints > ypoints.length");
      }
   }

   public void reset() {
      this.npoints = 0;
      this.bounds = null;
      this.path = new GeneralPath();
      this.closedPath = null;
   }

   public Object clone() {
      Polygon2D pol = new Polygon2D();

      for(int i = 0; i < this.npoints; ++i) {
         pol.addPoint(this.xpoints[i], this.ypoints[i]);
      }

      return pol;
   }

   private void calculatePath() {
      this.path = new GeneralPath();
      this.path.moveTo(this.xpoints[0], this.ypoints[0]);

      for(int i = 1; i < this.npoints; ++i) {
         this.path.lineTo(this.xpoints[i], this.ypoints[i]);
      }

      this.bounds = this.path.getBounds2D();
      this.closedPath = null;
   }

   private void updatePath(float x, float y) {
      this.closedPath = null;
      if (this.path == null) {
         this.path = new GeneralPath(0);
         this.path.moveTo(x, y);
         this.bounds = new Rectangle2D.Float(x, y, 0.0F, 0.0F);
      } else {
         this.path.lineTo(x, y);
         float _xmax = (float)this.bounds.getMaxX();
         float _ymax = (float)this.bounds.getMaxY();
         float _xmin = (float)this.bounds.getMinX();
         float _ymin = (float)this.bounds.getMinY();
         if (x < _xmin) {
            _xmin = x;
         } else if (x > _xmax) {
            _xmax = x;
         }

         if (y < _ymin) {
            _ymin = y;
         } else if (y > _ymax) {
            _ymax = y;
         }

         this.bounds = new Rectangle2D.Float(_xmin, _ymin, _xmax - _xmin, _ymax - _ymin);
      }

   }

   public Polyline2D getPolyline2D() {
      Polyline2D pol = new Polyline2D(this.xpoints, this.ypoints, this.npoints);
      pol.addPoint(this.xpoints[0], this.ypoints[0]);
      return pol;
   }

   public Polygon getPolygon() {
      int[] _xpoints = new int[this.npoints];
      int[] _ypoints = new int[this.npoints];

      for(int i = 0; i < this.npoints; ++i) {
         _xpoints[i] = (int)this.xpoints[i];
         _ypoints[i] = (int)this.ypoints[i];
      }

      return new Polygon(_xpoints, _ypoints, this.npoints);
   }

   public void addPoint(Point2D p) {
      this.addPoint((float)p.getX(), (float)p.getY());
   }

   public void addPoint(float x, float y) {
      if (this.npoints == this.xpoints.length) {
         float[] tmp = new float[this.npoints * 2];
         System.arraycopy(this.xpoints, 0, tmp, 0, this.npoints);
         this.xpoints = tmp;
         tmp = new float[this.npoints * 2];
         System.arraycopy(this.ypoints, 0, tmp, 0, this.npoints);
         this.ypoints = tmp;
      }

      this.xpoints[this.npoints] = x;
      this.ypoints[this.npoints] = y;
      ++this.npoints;
      this.updatePath(x, y);
   }

   public boolean contains(Point p) {
      return this.contains(p.x, p.y);
   }

   public boolean contains(int x, int y) {
      return this.contains((double)x, (double)y);
   }

   public Rectangle2D getBounds2D() {
      return this.bounds;
   }

   public Rectangle getBounds() {
      return this.bounds == null ? null : this.bounds.getBounds();
   }

   public boolean contains(double x, double y) {
      if (this.npoints > 2 && this.bounds.contains(x, y)) {
         this.updateComputingPath();
         return this.closedPath.contains(x, y);
      } else {
         return false;
      }
   }

   private void updateComputingPath() {
      if (this.npoints >= 1 && this.closedPath == null) {
         this.closedPath = (GeneralPath)this.path.clone();
         this.closedPath.closePath();
      }

   }

   public boolean contains(Point2D p) {
      return this.contains(p.getX(), p.getY());
   }

   public boolean intersects(double x, double y, double w, double h) {
      if (this.npoints > 0 && this.bounds.intersects(x, y, w, h)) {
         this.updateComputingPath();
         return this.closedPath.intersects(x, y, w, h);
      } else {
         return false;
      }
   }

   public boolean intersects(Rectangle2D r) {
      return this.intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight());
   }

   public boolean contains(double x, double y, double w, double h) {
      if (this.npoints > 0 && this.bounds.intersects(x, y, w, h)) {
         this.updateComputingPath();
         return this.closedPath.contains(x, y, w, h);
      } else {
         return false;
      }
   }

   public boolean contains(Rectangle2D r) {
      return this.contains(r.getX(), r.getY(), r.getWidth(), r.getHeight());
   }

   public PathIterator getPathIterator(AffineTransform at) {
      this.updateComputingPath();
      return this.closedPath == null ? null : this.closedPath.getPathIterator(at);
   }

   public PathIterator getPathIterator(AffineTransform at, double flatness) {
      return this.getPathIterator(at);
   }
}
