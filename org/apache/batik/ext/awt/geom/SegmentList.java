package org.apache.batik.ext.awt.geom;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class SegmentList {
   List segments = new LinkedList();

   public SegmentList() {
   }

   public SegmentList(Shape s) {
      PathIterator pi = s.getPathIterator((AffineTransform)null);
      float[] pts = new float[6];
      Point2D.Double loc = null;

      for(Point2D.Double openLoc = null; !pi.isDone(); pi.next()) {
         int type = pi.currentSegment(pts);
         Point2D.Double p0;
         Point2D.Double p1;
         switch (type) {
            case 0:
               openLoc = loc = new Point2D.Double((double)pts[0], (double)pts[1]);
               break;
            case 1:
               p0 = new Point2D.Double((double)pts[0], (double)pts[1]);
               this.segments.add(new Linear(loc, p0));
               loc = p0;
               break;
            case 2:
               p0 = new Point2D.Double((double)pts[0], (double)pts[1]);
               p1 = new Point2D.Double((double)pts[2], (double)pts[3]);
               this.segments.add(new Quadradic(loc, p0, p1));
               loc = p1;
               break;
            case 3:
               p0 = new Point2D.Double((double)pts[0], (double)pts[1]);
               p1 = new Point2D.Double((double)pts[2], (double)pts[3]);
               Point2D.Double p2 = new Point2D.Double((double)pts[4], (double)pts[5]);
               this.segments.add(new Cubic(loc, p0, p1, p2));
               loc = p2;
               break;
            case 4:
               this.segments.add(new Linear(loc, openLoc));
               loc = openLoc;
         }
      }

   }

   public Rectangle2D getBounds2D() {
      Iterator iter = this.iterator();
      if (!iter.hasNext()) {
         return null;
      } else {
         Rectangle2D ret = (Rectangle2D)((Segment)iter.next()).getBounds2D().clone();

         while(iter.hasNext()) {
            Segment seg = (Segment)iter.next();
            Rectangle2D segB = seg.getBounds2D();
            Rectangle2D.union(segB, ret, ret);
         }

         return ret;
      }
   }

   public void add(Segment s) {
      this.segments.add(s);
   }

   public Iterator iterator() {
      return this.segments.iterator();
   }

   public int size() {
      return this.segments.size();
   }

   public SplitResults split(double y) {
      Iterator iter = this.segments.iterator();
      SegmentList above = new SegmentList();
      SegmentList below = new SegmentList();

      while(true) {
         while(iter.hasNext()) {
            Segment seg = (Segment)iter.next();
            Segment.SplitResults results = seg.split(y);
            if (results == null) {
               Rectangle2D bounds = seg.getBounds2D();
               if (bounds.getY() > y) {
                  below.add(seg);
               } else if (bounds.getY() == y) {
                  if (bounds.getHeight() != 0.0) {
                     below.add(seg);
                  }
               } else {
                  above.add(seg);
               }
            } else {
               Segment[] resAbove = results.getAbove();
               Segment[] resBelow = resAbove;
               int var10 = resAbove.length;

               int var11;
               for(var11 = 0; var11 < var10; ++var11) {
                  Segment aResAbove = resBelow[var11];
                  above.add(aResAbove);
               }

               resBelow = results.getBelow();
               Segment[] var15 = resBelow;
               var11 = resBelow.length;

               for(int var16 = 0; var16 < var11; ++var16) {
                  Segment aResBelow = var15[var16];
                  below.add(aResBelow);
               }
            }
         }

         return new SplitResults(above, below);
      }
   }

   public static class SplitResults {
      final SegmentList above;
      final SegmentList below;

      public SplitResults(SegmentList above, SegmentList below) {
         if (above != null && above.size() > 0) {
            this.above = above;
         } else {
            this.above = null;
         }

         if (below != null && below.size() > 0) {
            this.below = below;
         } else {
            this.below = null;
         }

      }

      public SegmentList getAbove() {
         return this.above;
      }

      public SegmentList getBelow() {
         return this.below;
      }
   }
}
