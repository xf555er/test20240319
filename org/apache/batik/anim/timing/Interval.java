package org.apache.batik.anim.timing;

import java.util.Iterator;
import java.util.LinkedList;

public class Interval {
   protected float begin;
   protected float end;
   protected InstanceTime beginInstanceTime;
   protected InstanceTime endInstanceTime;
   protected LinkedList beginDependents = new LinkedList();
   protected LinkedList endDependents = new LinkedList();

   public Interval(float begin, float end, InstanceTime beginInstanceTime, InstanceTime endInstanceTime) {
      this.begin = begin;
      this.end = end;
      this.beginInstanceTime = beginInstanceTime;
      this.endInstanceTime = endInstanceTime;
   }

   public String toString() {
      return TimedElement.toString(this.begin) + ".." + TimedElement.toString(this.end);
   }

   public float getBegin() {
      return this.begin;
   }

   public float getEnd() {
      return this.end;
   }

   public InstanceTime getBeginInstanceTime() {
      return this.beginInstanceTime;
   }

   public InstanceTime getEndInstanceTime() {
      return this.endInstanceTime;
   }

   void addDependent(InstanceTime dependent, boolean forBegin) {
      if (forBegin) {
         this.beginDependents.add(dependent);
      } else {
         this.endDependents.add(dependent);
      }

   }

   void removeDependent(InstanceTime dependent, boolean forBegin) {
      if (forBegin) {
         this.beginDependents.remove(dependent);
      } else {
         this.endDependents.remove(dependent);
      }

   }

   float setBegin(float begin) {
      float minTime = Float.POSITIVE_INFINITY;
      this.begin = begin;
      Iterator var3 = this.beginDependents.iterator();

      while(var3.hasNext()) {
         Object beginDependent = var3.next();
         InstanceTime it = (InstanceTime)beginDependent;
         float t = it.dependentUpdate(begin);
         if (t < minTime) {
            minTime = t;
         }
      }

      return minTime;
   }

   float setEnd(float end, InstanceTime endInstanceTime) {
      float minTime = Float.POSITIVE_INFINITY;
      this.end = end;
      this.endInstanceTime = endInstanceTime;
      Iterator var4 = this.endDependents.iterator();

      while(var4.hasNext()) {
         Object endDependent = var4.next();
         InstanceTime it = (InstanceTime)endDependent;
         float t = it.dependentUpdate(end);
         if (t < minTime) {
            minTime = t;
         }
      }

      return minTime;
   }
}
