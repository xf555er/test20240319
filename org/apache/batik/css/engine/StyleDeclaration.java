package org.apache.batik.css.engine;

import org.apache.batik.css.engine.value.Value;

public class StyleDeclaration {
   protected static final int INITIAL_LENGTH = 8;
   protected Value[] values = new Value[8];
   protected int[] indexes = new int[8];
   protected boolean[] priorities = new boolean[8];
   protected int count;

   public int size() {
      return this.count;
   }

   public Value getValue(int idx) {
      return this.values[idx];
   }

   public int getIndex(int idx) {
      return this.indexes[idx];
   }

   public boolean getPriority(int idx) {
      return this.priorities[idx];
   }

   public void remove(int idx) {
      --this.count;
      int from = idx + 1;
      int nCopy = this.count - idx;
      System.arraycopy(this.values, from, this.values, idx, nCopy);
      System.arraycopy(this.indexes, from, this.indexes, idx, nCopy);
      System.arraycopy(this.priorities, from, this.priorities, idx, nCopy);
      this.values[this.count] = null;
      this.indexes[this.count] = 0;
      this.priorities[this.count] = false;
   }

   public void put(int idx, Value v, int i, boolean prio) {
      this.values[idx] = v;
      this.indexes[idx] = i;
      this.priorities[idx] = prio;
   }

   public void append(Value v, int idx, boolean prio) {
      if (this.values.length == this.count) {
         Value[] newval = new Value[this.count * 2];
         int[] newidx = new int[this.count * 2];
         boolean[] newprio = new boolean[this.count * 2];
         System.arraycopy(this.values, 0, newval, 0, this.count);
         System.arraycopy(this.indexes, 0, newidx, 0, this.count);
         System.arraycopy(this.priorities, 0, newprio, 0, this.count);
         this.values = newval;
         this.indexes = newidx;
         this.priorities = newprio;
      }

      for(int i = 0; i < this.count; ++i) {
         if (this.indexes[i] == idx) {
            if (prio || this.priorities[i] == prio) {
               this.values[i] = v;
               this.priorities[i] = prio;
            }

            return;
         }
      }

      this.values[this.count] = v;
      this.indexes[this.count] = idx;
      this.priorities[this.count] = prio;
      ++this.count;
   }

   public String toString(CSSEngine eng) {
      StringBuffer sb = new StringBuffer(this.count * 8);

      for(int i = 0; i < this.count; ++i) {
         sb.append(eng.getPropertyName(this.indexes[i]));
         sb.append(": ");
         sb.append(this.values[i]);
         sb.append(";\n");
      }

      return sb.toString();
   }
}
