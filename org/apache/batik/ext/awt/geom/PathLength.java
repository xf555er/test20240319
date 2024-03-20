package org.apache.batik.ext.awt.geom;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class PathLength {
   protected Shape path;
   protected List segments;
   protected int[] segmentIndexes;
   protected float pathLength;
   protected boolean initialised;

   public PathLength(Shape path) {
      this.setPath(path);
   }

   public Shape getPath() {
      return this.path;
   }

   public void setPath(Shape v) {
      this.path = v;
      this.initialised = false;
   }

   public float lengthOfPath() {
      if (!this.initialised) {
         this.initialise();
      }

      return this.pathLength;
   }

   protected void initialise() {
      this.pathLength = 0.0F;
      PathIterator pi = this.path.getPathIterator(new AffineTransform());
      SingleSegmentPathIterator sspi = new SingleSegmentPathIterator();
      this.segments = new ArrayList(20);
      List indexes = new ArrayList(20);
      int index = 0;
      int origIndex = -1;
      float lastMoveX = 0.0F;
      float lastMoveY = 0.0F;
      float currentX = 0.0F;
      float currentY = 0.0F;
      float[] seg = new float[6];
      this.segments.add(new PathSegment(0, 0.0F, 0.0F, 0.0F, origIndex));

      while(true) {
         label38:
         while(!pi.isDone()) {
            ++origIndex;
            indexes.add(index);
            int segType = pi.currentSegment(seg);
            switch (segType) {
               case 0:
                  this.segments.add(new PathSegment(segType, seg[0], seg[1], this.pathLength, origIndex));
                  currentX = seg[0];
                  currentY = seg[1];
                  lastMoveX = currentX;
                  lastMoveY = currentY;
                  ++index;
                  pi.next();
                  break;
               case 1:
                  this.pathLength = (float)((double)this.pathLength + Point2D.distance((double)currentX, (double)currentY, (double)seg[0], (double)seg[1]));
                  this.segments.add(new PathSegment(segType, seg[0], seg[1], this.pathLength, origIndex));
                  currentX = seg[0];
                  currentY = seg[1];
                  ++index;
                  pi.next();
                  break;
               case 2:
               case 3:
               default:
                  sspi.setPathIterator(pi, (double)currentX, (double)currentY);
                  FlatteningPathIterator fpi = new FlatteningPathIterator(sspi, 0.009999999776482582);

                  while(true) {
                     if (fpi.isDone()) {
                        continue label38;
                     }

                     segType = fpi.currentSegment(seg);
                     if (segType == 1) {
                        this.pathLength = (float)((double)this.pathLength + Point2D.distance((double)currentX, (double)currentY, (double)seg[0], (double)seg[1]));
                        this.segments.add(new PathSegment(segType, seg[0], seg[1], this.pathLength, origIndex));
                        currentX = seg[0];
                        currentY = seg[1];
                        ++index;
                     }

                     fpi.next();
                  }
               case 4:
                  this.pathLength = (float)((double)this.pathLength + Point2D.distance((double)currentX, (double)currentY, (double)lastMoveX, (double)lastMoveY));
                  this.segments.add(new PathSegment(1, lastMoveX, lastMoveY, this.pathLength, origIndex));
                  currentX = lastMoveX;
                  currentY = lastMoveY;
                  ++index;
                  pi.next();
            }
         }

         this.segmentIndexes = new int[indexes.size()];

         for(int i = 0; i < this.segmentIndexes.length; ++i) {
            this.segmentIndexes[i] = (Integer)indexes.get(i);
         }

         this.initialised = true;
         return;
      }
   }

   public int getNumberOfSegments() {
      if (!this.initialised) {
         this.initialise();
      }

      return this.segmentIndexes.length;
   }

   public float getLengthAtSegment(int index) {
      if (!this.initialised) {
         this.initialise();
      }

      if (index <= 0) {
         return 0.0F;
      } else if (index >= this.segmentIndexes.length) {
         return this.pathLength;
      } else {
         PathSegment seg = (PathSegment)this.segments.get(this.segmentIndexes[index]);
         return seg.getLength();
      }
   }

   public int segmentAtLength(float length) {
      int upperIndex = this.findUpperIndex(length);
      if (upperIndex == -1) {
         return -1;
      } else {
         PathSegment lower;
         if (upperIndex == 0) {
            lower = (PathSegment)this.segments.get(upperIndex);
            return lower.getIndex();
         } else {
            lower = (PathSegment)this.segments.get(upperIndex - 1);
            return lower.getIndex();
         }
      }
   }

   public Point2D pointAtLength(int index, float proportion) {
      if (!this.initialised) {
         this.initialise();
      }

      if (index >= 0 && index < this.segmentIndexes.length) {
         PathSegment seg = (PathSegment)this.segments.get(this.segmentIndexes[index]);
         float start = seg.getLength();
         float end;
         if (index == this.segmentIndexes.length - 1) {
            end = this.pathLength;
         } else {
            seg = (PathSegment)this.segments.get(this.segmentIndexes[index + 1]);
            end = seg.getLength();
         }

         return this.pointAtLength(start + (end - start) * proportion);
      } else {
         return null;
      }
   }

   public Point2D pointAtLength(float length) {
      int upperIndex = this.findUpperIndex(length);
      if (upperIndex == -1) {
         return null;
      } else {
         PathSegment upper = (PathSegment)this.segments.get(upperIndex);
         if (upperIndex == 0) {
            return new Point2D.Float(upper.getX(), upper.getY());
         } else {
            PathSegment lower = (PathSegment)this.segments.get(upperIndex - 1);
            float offset = length - lower.getLength();
            double theta = Math.atan2((double)(upper.getY() - lower.getY()), (double)(upper.getX() - lower.getX()));
            float xPoint = (float)((double)lower.getX() + (double)offset * Math.cos(theta));
            float yPoint = (float)((double)lower.getY() + (double)offset * Math.sin(theta));
            return new Point2D.Float(xPoint, yPoint);
         }
      }
   }

   public float angleAtLength(int index, float proportion) {
      if (!this.initialised) {
         this.initialise();
      }

      if (index >= 0 && index < this.segmentIndexes.length) {
         PathSegment seg = (PathSegment)this.segments.get(this.segmentIndexes[index]);
         float start = seg.getLength();
         float end;
         if (index == this.segmentIndexes.length - 1) {
            end = this.pathLength;
         } else {
            seg = (PathSegment)this.segments.get(this.segmentIndexes[index + 1]);
            end = seg.getLength();
         }

         return this.angleAtLength(start + (end - start) * proportion);
      } else {
         return 0.0F;
      }
   }

   public float angleAtLength(float length) {
      int upperIndex = this.findUpperIndex(length);
      if (upperIndex == -1) {
         return 0.0F;
      } else {
         PathSegment upper = (PathSegment)this.segments.get(upperIndex);
         if (upperIndex == 0) {
            upperIndex = 1;
         }

         PathSegment lower = (PathSegment)this.segments.get(upperIndex - 1);
         return (float)Math.atan2((double)(upper.getY() - lower.getY()), (double)(upper.getX() - lower.getX()));
      }
   }

   public int findUpperIndex(float length) {
      if (!this.initialised) {
         this.initialise();
      }

      if (!(length < 0.0F) && !(length > this.pathLength)) {
         int lb = 0;
         int ub = this.segments.size() - 1;

         int upperIndex;
         while(lb != ub) {
            upperIndex = lb + ub >> 1;
            PathSegment ps = (PathSegment)this.segments.get(upperIndex);
            if (ps.getLength() >= length) {
               ub = upperIndex;
            } else {
               lb = upperIndex + 1;
            }
         }

         while(true) {
            PathSegment ps = (PathSegment)this.segments.get(ub);
            if (ps.getSegType() != 0 || ub == this.segments.size() - 1) {
               upperIndex = -1;
               int currentIndex = 0;

               for(int numSegments = this.segments.size(); upperIndex <= 0 && currentIndex < numSegments; ++currentIndex) {
                  PathSegment ps = (PathSegment)this.segments.get(currentIndex);
                  if (ps.getLength() >= length && ps.getSegType() != 0) {
                     upperIndex = currentIndex;
                  }
               }

               return upperIndex;
            }

            ++ub;
         }
      } else {
         return -1;
      }
   }

   protected static class PathSegment {
      protected final int segType;
      protected float x;
      protected float y;
      protected float length;
      protected int index;

      PathSegment(int segType, float x, float y, float len, int idx) {
         this.segType = segType;
         this.x = x;
         this.y = y;
         this.length = len;
         this.index = idx;
      }

      public int getSegType() {
         return this.segType;
      }

      public float getX() {
         return this.x;
      }

      public void setX(float v) {
         this.x = v;
      }

      public float getY() {
         return this.y;
      }

      public void setY(float v) {
         this.y = v;
      }

      public float getLength() {
         return this.length;
      }

      public void setLength(float v) {
         this.length = v;
      }

      public int getIndex() {
         return this.index;
      }

      public void setIndex(int v) {
         this.index = v;
      }
   }

   protected static class SingleSegmentPathIterator implements PathIterator {
      protected PathIterator it;
      protected boolean done;
      protected boolean moveDone;
      protected double x;
      protected double y;

      public void setPathIterator(PathIterator it, double x, double y) {
         this.it = it;
         this.x = x;
         this.y = y;
         this.done = false;
         this.moveDone = false;
      }

      public int currentSegment(double[] coords) {
         int type = this.it.currentSegment(coords);
         if (!this.moveDone) {
            coords[0] = this.x;
            coords[1] = this.y;
            return 0;
         } else {
            return type;
         }
      }

      public int currentSegment(float[] coords) {
         int type = this.it.currentSegment(coords);
         if (!this.moveDone) {
            coords[0] = (float)this.x;
            coords[1] = (float)this.y;
            return 0;
         } else {
            return type;
         }
      }

      public int getWindingRule() {
         return this.it.getWindingRule();
      }

      public boolean isDone() {
         return this.done || this.it.isDone();
      }

      public void next() {
         if (!this.done) {
            if (!this.moveDone) {
               this.moveDone = true;
            } else {
               this.it.next();
               this.done = true;
            }
         }

      }
   }
}
