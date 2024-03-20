package org.apache.batik.gvt.flow;

import java.awt.Shape;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.apache.batik.ext.awt.geom.Segment;
import org.apache.batik.ext.awt.geom.SegmentList;

public class FlowRegions {
   Shape flowShape;
   SegmentList sl;
   SegmentList.SplitResults sr;
   List validRanges;
   int currentRange;
   double currentY;
   double lineHeight;

   public FlowRegions(Shape s) {
      this(s, s.getBounds2D().getY());
   }

   public FlowRegions(Shape s, double startY) {
      this.flowShape = s;
      this.sl = new SegmentList(s);
      this.currentY = startY - 1.0;
      this.gotoY(startY);
   }

   public double getCurrentY() {
      return this.currentY;
   }

   public double getLineHeight() {
      return this.lineHeight;
   }

   public boolean gotoY(double y) {
      if (y < this.currentY) {
         throw new IllegalArgumentException("New Y can not be lower than old Y\nOld Y: " + this.currentY + " New Y: " + y);
      } else if (y == this.currentY) {
         return false;
      } else {
         this.sr = this.sl.split(y);
         this.sl = this.sr.getBelow();
         this.sr = null;
         this.currentY = y;
         if (this.sl == null) {
            return true;
         } else {
            this.newLineHeight(this.lineHeight);
            return false;
         }
      }
   }

   public void newLineHeight(double lineHeight) {
      this.lineHeight = lineHeight;
      this.sr = this.sl.split(this.currentY + lineHeight);
      if (this.sr.getAbove() != null) {
         this.sortRow(this.sr.getAbove());
      }

      this.currentRange = 0;
   }

   public int getNumRangeOnLine() {
      return this.validRanges == null ? 0 : this.validRanges.size();
   }

   public void resetRange() {
      this.currentRange = 0;
   }

   public double[] nextRange() {
      return this.currentRange >= this.validRanges.size() ? null : (double[])((double[])this.validRanges.get(this.currentRange++));
   }

   public void endLine() {
      this.sl = this.sr.getBelow();
      this.sr = null;
      this.currentY += this.lineHeight;
   }

   public boolean newLine() {
      return this.newLine(this.lineHeight);
   }

   public boolean newLine(double lineHeight) {
      if (this.sr != null) {
         this.sl = this.sr.getBelow();
      }

      this.sr = null;
      if (this.sl == null) {
         return false;
      } else {
         this.currentY += this.lineHeight;
         this.newLineHeight(lineHeight);
         return true;
      }
   }

   public boolean newLineAt(double y, double lineHeight) {
      if (this.sr != null) {
         this.sl = this.sr.getBelow();
      }

      this.sr = null;
      if (this.sl == null) {
         return false;
      } else {
         this.currentY = y;
         this.newLineHeight(lineHeight);
         return true;
      }
   }

   public boolean done() {
      return this.sl == null;
   }

   public void sortRow(SegmentList sl) {
      Transition[] segs = new Transition[sl.size() * 2];
      Iterator iter = sl.iterator();

      int i;
      Segment seg;
      for(i = 0; iter.hasNext(); segs[i++] = new Transition(seg.maxX(), false)) {
         seg = (Segment)iter.next();
         segs[i++] = new Transition(seg.minX(), true);
      }

      Arrays.sort(segs, FlowRegions.TransitionComp.COMP);
      this.validRanges = new ArrayList();
      int count = 1;
      double openStart = 0.0;

      for(i = 1; i < segs.length; ++i) {
         Transition t = segs[i];
         if (t.up) {
            if (count == 0) {
               double cx = (openStart + t.loc) / 2.0;
               double cy = this.currentY + this.lineHeight / 2.0;
               if (this.flowShape.contains(cx, cy)) {
                  this.validRanges.add(new double[]{openStart, t.loc});
               }
            }

            ++count;
         } else {
            --count;
            if (count == 0) {
               openStart = t.loc;
            }
         }
      }

   }

   static class TransitionComp implements Comparator {
      public static Comparator COMP = new TransitionComp();

      public int compare(Object o1, Object o2) {
         Transition t1 = (Transition)o1;
         Transition t2 = (Transition)o2;
         if (t1.loc < t2.loc) {
            return -1;
         } else if (t1.loc > t2.loc) {
            return 1;
         } else if (t1.up) {
            return t2.up ? 0 : -1;
         } else {
            return t2.up ? 1 : 0;
         }
      }

      public boolean equals(Object comp) {
         return this == comp;
      }
   }

   static class Transition {
      public double loc;
      public boolean up;

      public Transition(double loc, boolean up) {
         this.loc = loc;
         this.up = up;
      }
   }
}
