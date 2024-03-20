package org.apache.batik.gvt;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import org.apache.batik.ext.awt.geom.ExtendedGeneralPath;
import org.apache.batik.ext.awt.geom.ExtendedPathIterator;
import org.apache.batik.ext.awt.geom.ExtendedShape;
import org.apache.batik.ext.awt.geom.ShapeExtender;

public class MarkerShapePainter implements ShapePainter {
   protected ExtendedShape extShape;
   protected Marker startMarker;
   protected Marker middleMarker;
   protected Marker endMarker;
   private ProxyGraphicsNode startMarkerProxy;
   private ProxyGraphicsNode[] middleMarkerProxies;
   private ProxyGraphicsNode endMarkerProxy;
   private CompositeGraphicsNode markerGroup;
   private Rectangle2D dPrimitiveBounds;
   private Rectangle2D dGeometryBounds;

   public MarkerShapePainter(Shape shape) {
      if (shape == null) {
         throw new IllegalArgumentException();
      } else {
         if (shape instanceof ExtendedShape) {
            this.extShape = (ExtendedShape)shape;
         } else {
            this.extShape = new ShapeExtender(shape);
         }

      }
   }

   public void paint(Graphics2D g2d) {
      if (this.markerGroup == null) {
         this.buildMarkerGroup();
      }

      if (this.markerGroup.getChildren().size() > 0) {
         this.markerGroup.paint(g2d);
      }

   }

   public Shape getPaintedArea() {
      if (this.markerGroup == null) {
         this.buildMarkerGroup();
      }

      return this.markerGroup.getOutline();
   }

   public Rectangle2D getPaintedBounds2D() {
      if (this.markerGroup == null) {
         this.buildMarkerGroup();
      }

      return this.markerGroup.getPrimitiveBounds();
   }

   public boolean inPaintedArea(Point2D pt) {
      if (this.markerGroup == null) {
         this.buildMarkerGroup();
      }

      GraphicsNode gn = this.markerGroup.nodeHitAt(pt);
      return gn != null;
   }

   public Shape getSensitiveArea() {
      return null;
   }

   public Rectangle2D getSensitiveBounds2D() {
      return null;
   }

   public boolean inSensitiveArea(Point2D pt) {
      return false;
   }

   public void setShape(Shape shape) {
      if (shape == null) {
         throw new IllegalArgumentException();
      } else {
         if (shape instanceof ExtendedShape) {
            this.extShape = (ExtendedShape)shape;
         } else {
            this.extShape = new ShapeExtender(shape);
         }

         this.startMarkerProxy = null;
         this.middleMarkerProxies = null;
         this.endMarkerProxy = null;
         this.markerGroup = null;
      }
   }

   public ExtendedShape getExtShape() {
      return this.extShape;
   }

   public Shape getShape() {
      return this.extShape;
   }

   public Marker getStartMarker() {
      return this.startMarker;
   }

   public void setStartMarker(Marker startMarker) {
      this.startMarker = startMarker;
      this.startMarkerProxy = null;
      this.markerGroup = null;
   }

   public Marker getMiddleMarker() {
      return this.middleMarker;
   }

   public void setMiddleMarker(Marker middleMarker) {
      this.middleMarker = middleMarker;
      this.middleMarkerProxies = null;
      this.markerGroup = null;
   }

   public Marker getEndMarker() {
      return this.endMarker;
   }

   public void setEndMarker(Marker endMarker) {
      this.endMarker = endMarker;
      this.endMarkerProxy = null;
      this.markerGroup = null;
   }

   protected void buildMarkerGroup() {
      if (this.startMarker != null && this.startMarkerProxy == null) {
         this.startMarkerProxy = this.buildStartMarkerProxy();
      }

      if (this.middleMarker != null && this.middleMarkerProxies == null) {
         this.middleMarkerProxies = this.buildMiddleMarkerProxies();
      }

      if (this.endMarker != null && this.endMarkerProxy == null) {
         this.endMarkerProxy = this.buildEndMarkerProxy();
      }

      CompositeGraphicsNode group = new CompositeGraphicsNode();
      List children = group.getChildren();
      if (this.startMarkerProxy != null) {
         children.add(this.startMarkerProxy);
      }

      if (this.middleMarkerProxies != null) {
         ProxyGraphicsNode[] var3 = this.middleMarkerProxies;
         int var4 = var3.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            ProxyGraphicsNode middleMarkerProxy = var3[var5];
            children.add(middleMarkerProxy);
         }
      }

      if (this.endMarkerProxy != null) {
         children.add(this.endMarkerProxy);
      }

      this.markerGroup = group;
   }

   protected ProxyGraphicsNode buildStartMarkerProxy() {
      ExtendedPathIterator iter = this.getExtShape().getExtendedPathIterator();
      double[] coords = new double[7];
      int segType = false;
      if (iter.isDone()) {
         return null;
      } else {
         int segType = iter.currentSegment(coords);
         if (segType != 0) {
            return null;
         } else {
            iter.next();
            Point2D markerPosition = new Point2D.Double(coords[0], coords[1]);
            double rotation = this.startMarker.getOrient();
            if (Double.isNaN(rotation) && !iter.isDone()) {
               double[] next = new double[7];
               int nextSegType = false;
               int nextSegType = iter.currentSegment(next);
               if (nextSegType == 4) {
                  nextSegType = 1;
                  next[0] = coords[0];
                  next[1] = coords[1];
               }

               rotation = this.computeRotation((double[])null, 0, coords, segType, next, nextSegType);
            }

            AffineTransform markerTxf = this.computeMarkerTransform(this.startMarker, markerPosition, rotation);
            ProxyGraphicsNode gn = new ProxyGraphicsNode();
            gn.setSource(this.startMarker.getMarkerNode());
            gn.setTransform(markerTxf);
            return gn;
         }
      }
   }

   protected ProxyGraphicsNode buildEndMarkerProxy() {
      ExtendedPathIterator iter = this.getExtShape().getExtendedPathIterator();
      int nPoints = 0;
      if (iter.isDone()) {
         return null;
      } else {
         double[] coords = new double[7];
         double[] moveTo = new double[2];
         int segType = false;
         int segType = iter.currentSegment(coords);
         if (segType != 0) {
            return null;
         } else {
            ++nPoints;
            moveTo[0] = coords[0];
            moveTo[1] = coords[1];
            iter.next();
            double[] lastButOne = new double[7];
            double[] last = new double[]{coords[0], coords[1], coords[2], coords[3], coords[4], coords[5], coords[6]};
            double[] tmp = null;
            int lastSegType = segType;

            int lastButOneSegType;
            for(lastButOneSegType = 0; !iter.isDone(); ++nPoints) {
               double[] tmp = lastButOne;
               lastButOne = last;
               last = tmp;
               lastButOneSegType = lastSegType;
               lastSegType = iter.currentSegment(tmp);
               if (lastSegType == 0) {
                  moveTo[0] = tmp[0];
                  moveTo[1] = tmp[1];
               } else if (lastSegType == 4) {
                  lastSegType = 1;
                  tmp[0] = moveTo[0];
                  tmp[1] = moveTo[1];
               }

               iter.next();
            }

            if (nPoints < 2) {
               return null;
            } else {
               Point2D markerPosition = this.getSegmentTerminatingPoint(last, lastSegType);
               double rotation = this.endMarker.getOrient();
               if (Double.isNaN(rotation)) {
                  rotation = this.computeRotation(lastButOne, lastButOneSegType, last, lastSegType, (double[])null, 0);
               }

               AffineTransform markerTxf = this.computeMarkerTransform(this.endMarker, markerPosition, rotation);
               ProxyGraphicsNode gn = new ProxyGraphicsNode();
               gn.setSource(this.endMarker.getMarkerNode());
               gn.setTransform(markerTxf);
               return gn;
            }
         }
      }
   }

   protected ProxyGraphicsNode[] buildMiddleMarkerProxies() {
      ExtendedPathIterator iter = this.getExtShape().getExtendedPathIterator();
      double[] prev = new double[7];
      double[] curr = new double[7];
      double[] next = new double[7];
      double[] tmp = null;
      int prevSegType = false;
      int currSegType = false;
      int nextSegType = false;
      if (iter.isDone()) {
         return null;
      } else {
         int prevSegType = iter.currentSegment(prev);
         double[] moveTo = new double[2];
         if (prevSegType != 0) {
            return null;
         } else {
            moveTo[0] = prev[0];
            moveTo[1] = prev[1];
            iter.next();
            if (iter.isDone()) {
               return null;
            } else {
               int currSegType = iter.currentSegment(curr);
               if (currSegType == 0) {
                  moveTo[0] = curr[0];
                  moveTo[1] = curr[1];
               } else if (currSegType == 4) {
                  currSegType = 1;
                  curr[0] = moveTo[0];
                  curr[1] = moveTo[1];
               }

               iter.next();
               List proxies = new ArrayList();

               while(!iter.isDone()) {
                  int nextSegType = iter.currentSegment(next);
                  if (nextSegType == 0) {
                     moveTo[0] = next[0];
                     moveTo[1] = next[1];
                  } else if (nextSegType == 4) {
                     nextSegType = 1;
                     next[0] = moveTo[0];
                     next[1] = moveTo[1];
                  }

                  proxies.add(this.createMiddleMarker(prev, prevSegType, curr, currSegType, next, nextSegType));
                  double[] tmp = prev;
                  prev = curr;
                  prevSegType = currSegType;
                  curr = next;
                  currSegType = nextSegType;
                  next = tmp;
                  iter.next();
               }

               ProxyGraphicsNode[] gn = new ProxyGraphicsNode[proxies.size()];
               proxies.toArray(gn);
               return gn;
            }
         }
      }
   }

   private ProxyGraphicsNode createMiddleMarker(double[] prev, int prevSegType, double[] curr, int currSegType, double[] next, int nextSegType) {
      Point2D markerPosition = this.getSegmentTerminatingPoint(curr, currSegType);
      double rotation = this.middleMarker.getOrient();
      if (Double.isNaN(rotation)) {
         rotation = this.computeRotation(prev, prevSegType, curr, currSegType, next, nextSegType);
      }

      AffineTransform markerTxf = this.computeMarkerTransform(this.middleMarker, markerPosition, rotation);
      ProxyGraphicsNode gn = new ProxyGraphicsNode();
      gn.setSource(this.middleMarker.getMarkerNode());
      gn.setTransform(markerTxf);
      return gn;
   }

   private double computeRotation(double[] prev, int prevSegType, double[] curr, int currSegType, double[] next, int nextSegType) {
      double[] inSlope = this.computeInSlope(prev, prevSegType, curr, currSegType);
      double[] outSlope = this.computeOutSlope(curr, currSegType, next, nextSegType);
      if (inSlope == null) {
         inSlope = outSlope;
      }

      if (outSlope == null) {
         outSlope = inSlope;
      }

      if (inSlope == null) {
         return 0.0;
      } else {
         double dx = inSlope[0] + outSlope[0];
         double dy = inSlope[1] + outSlope[1];
         return dx == 0.0 && dy == 0.0 ? Math.toDegrees(Math.atan2(inSlope[1], inSlope[0])) + 90.0 : Math.toDegrees(Math.atan2(dy, dx));
      }
   }

   private double[] computeInSlope(double[] prev, int prevSegType, double[] curr, int currSegType) {
      Point2D currEndPoint = this.getSegmentTerminatingPoint(curr, currSegType);
      double dx = 0.0;
      double dy = 0.0;
      Point2D prevEndPoint;
      switch (currSegType) {
         case 0:
         default:
            return null;
         case 1:
            prevEndPoint = this.getSegmentTerminatingPoint(prev, prevSegType);
            dx = currEndPoint.getX() - prevEndPoint.getX();
            dy = currEndPoint.getY() - prevEndPoint.getY();
            break;
         case 2:
            dx = currEndPoint.getX() - curr[0];
            dy = currEndPoint.getY() - curr[1];
            break;
         case 3:
            dx = currEndPoint.getX() - curr[2];
            dy = currEndPoint.getY() - curr[3];
            break;
         case 4:
            throw new RuntimeException("should not have SEG_CLOSE here");
         case 4321:
            prevEndPoint = this.getSegmentTerminatingPoint(prev, prevSegType);
            boolean large = curr[3] != 0.0;
            boolean goLeft = curr[4] != 0.0;
            Arc2D arc = ExtendedGeneralPath.computeArc(prevEndPoint.getX(), prevEndPoint.getY(), curr[0], curr[1], curr[2], large, goLeft, curr[5], curr[6]);
            double theta = arc.getAngleStart() + arc.getAngleExtent();
            theta = Math.toRadians(theta);
            dx = -arc.getWidth() / 2.0 * Math.sin(theta);
            dy = arc.getHeight() / 2.0 * Math.cos(theta);
            if (curr[2] != 0.0) {
               double ang = Math.toRadians(-curr[2]);
               double sinA = Math.sin(ang);
               double cosA = Math.cos(ang);
               double tdx = dx * cosA - dy * sinA;
               double tdy = dx * sinA + dy * cosA;
               dx = tdx;
               dy = tdy;
            }

            if (goLeft) {
               dx = -dx;
            } else {
               dy = -dy;
            }
      }

      return dx == 0.0 && dy == 0.0 ? null : this.normalize(new double[]{dx, dy});
   }

   private double[] computeOutSlope(double[] curr, int currSegType, double[] next, int nextSegType) {
      Point2D currEndPoint = this.getSegmentTerminatingPoint(curr, currSegType);
      double dx = 0.0;
      double dy = 0.0;
      switch (nextSegType) {
         case 0:
         default:
            return null;
         case 1:
         case 2:
         case 3:
            dx = next[0] - currEndPoint.getX();
            dy = next[1] - currEndPoint.getY();
         case 4:
            break;
         case 4321:
            boolean large = next[3] != 0.0;
            boolean goLeft = next[4] != 0.0;
            Arc2D arc = ExtendedGeneralPath.computeArc(currEndPoint.getX(), currEndPoint.getY(), next[0], next[1], next[2], large, goLeft, next[5], next[6]);
            double theta = arc.getAngleStart();
            theta = Math.toRadians(theta);
            dx = -arc.getWidth() / 2.0 * Math.sin(theta);
            dy = arc.getHeight() / 2.0 * Math.cos(theta);
            if (next[2] != 0.0) {
               double ang = Math.toRadians(-next[2]);
               double sinA = Math.sin(ang);
               double cosA = Math.cos(ang);
               double tdx = dx * cosA - dy * sinA;
               double tdy = dx * sinA + dy * cosA;
               dx = tdx;
               dy = tdy;
            }

            if (goLeft) {
               dx = -dx;
            } else {
               dy = -dy;
            }
      }

      return dx == 0.0 && dy == 0.0 ? null : this.normalize(new double[]{dx, dy});
   }

   public double[] normalize(double[] v) {
      double n = Math.sqrt(v[0] * v[0] + v[1] * v[1]);
      v[0] /= n;
      v[1] /= n;
      return v;
   }

   private AffineTransform computeMarkerTransform(Marker marker, Point2D markerPosition, double rotation) {
      Point2D ref = marker.getRef();
      AffineTransform txf = new AffineTransform();
      txf.translate(markerPosition.getX() - ref.getX(), markerPosition.getY() - ref.getY());
      if (!Double.isNaN(rotation)) {
         txf.rotate(Math.toRadians(rotation), ref.getX(), ref.getY());
      }

      return txf;
   }

   protected Point2D getSegmentTerminatingPoint(double[] coords, int segType) {
      switch (segType) {
         case 0:
            return new Point2D.Double(coords[0], coords[1]);
         case 1:
            return new Point2D.Double(coords[0], coords[1]);
         case 2:
            return new Point2D.Double(coords[2], coords[3]);
         case 3:
            return new Point2D.Double(coords[4], coords[5]);
         case 4:
         default:
            throw new RuntimeException("invalid segmentType:" + segType);
         case 4321:
            return new Point2D.Double(coords[5], coords[6]);
      }
   }
}
