package org.apache.batik.anim.timing;

public class InstanceTime implements Comparable {
   protected float time;
   protected TimingSpecifier creator;
   protected boolean clearOnReset;

   public InstanceTime(TimingSpecifier creator, float time, boolean clearOnReset) {
      this.creator = creator;
      this.time = time;
      this.clearOnReset = clearOnReset;
   }

   public boolean getClearOnReset() {
      return this.clearOnReset;
   }

   public float getTime() {
      return this.time;
   }

   float dependentUpdate(float newTime) {
      this.time = newTime;
      return this.creator != null ? this.creator.handleTimebaseUpdate(this, this.time) : Float.POSITIVE_INFINITY;
   }

   public String toString() {
      return Float.toString(this.time);
   }

   public int compareTo(Object o) {
      InstanceTime it = (InstanceTime)o;
      if (this.time == it.time) {
         return 0;
      } else {
         return this.time > it.time ? 1 : -1;
      }
   }
}
