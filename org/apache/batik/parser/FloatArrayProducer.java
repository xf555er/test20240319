package org.apache.batik.parser;

import java.util.Iterator;
import java.util.LinkedList;

public class FloatArrayProducer extends DefaultNumberListHandler implements PointsHandler {
   protected LinkedList as;
   protected float[] a;
   protected int index;
   protected int count;

   public float[] getFloatArray() {
      return this.a;
   }

   public void startNumberList() throws ParseException {
      this.as = new LinkedList();
      this.a = new float[11];
      this.count = 0;
      this.index = 0;
   }

   public void numberValue(float v) throws ParseException {
      if (this.index == this.a.length) {
         this.as.add(this.a);
         this.a = new float[this.a.length * 2 + 1];
         this.index = 0;
      }

      this.a[this.index++] = v;
      ++this.count;
   }

   public void endNumberList() throws ParseException {
      float[] all = new float[this.count];
      int pos = 0;

      float[] b;
      for(Iterator var3 = this.as.iterator(); var3.hasNext(); pos += b.length) {
         Object a1 = var3.next();
         b = (float[])((float[])a1);
         System.arraycopy(b, 0, all, pos, b.length);
      }

      System.arraycopy(this.a, 0, all, pos, this.index);
      this.as.clear();
      this.a = all;
   }

   public void startPoints() throws ParseException {
      this.startNumberList();
   }

   public void point(float x, float y) throws ParseException {
      this.numberValue(x);
      this.numberValue(y);
   }

   public void endPoints() throws ParseException {
      this.endNumberList();
   }
}
