package org.apache.batik.parser;

import java.util.Iterator;
import java.util.LinkedList;

public class LengthArrayProducer extends DefaultLengthListHandler {
   protected LinkedList vs;
   protected float[] v;
   protected LinkedList us;
   protected short[] u;
   protected int index;
   protected int count;
   protected short currentUnit;

   public short[] getLengthTypeArray() {
      return this.u;
   }

   public float[] getLengthValueArray() {
      return this.v;
   }

   public void startLengthList() throws ParseException {
      this.us = new LinkedList();
      this.u = new short[11];
      this.vs = new LinkedList();
      this.v = new float[11];
      this.count = 0;
      this.index = 0;
   }

   public void numberValue(float v) throws ParseException {
   }

   public void lengthValue(float val) throws ParseException {
      if (this.index == this.v.length) {
         this.vs.add(this.v);
         this.v = new float[this.v.length * 2 + 1];
         this.us.add(this.u);
         this.u = new short[this.u.length * 2 + 1];
         this.index = 0;
      }

      this.v[this.index] = val;
   }

   public void startLength() throws ParseException {
      this.currentUnit = 1;
   }

   public void endLength() throws ParseException {
      this.u[this.index++] = this.currentUnit;
      ++this.count;
   }

   public void em() throws ParseException {
      this.currentUnit = 3;
   }

   public void ex() throws ParseException {
      this.currentUnit = 4;
   }

   public void in() throws ParseException {
      this.currentUnit = 8;
   }

   public void cm() throws ParseException {
      this.currentUnit = 6;
   }

   public void mm() throws ParseException {
      this.currentUnit = 7;
   }

   public void pc() throws ParseException {
      this.currentUnit = 10;
   }

   public void pt() throws ParseException {
      this.currentUnit = 9;
   }

   public void px() throws ParseException {
      this.currentUnit = 5;
   }

   public void percentage() throws ParseException {
      this.currentUnit = 2;
   }

   public void endLengthList() throws ParseException {
      float[] allValues = new float[this.count];
      int pos = 0;

      Iterator it;
      float[] a;
      for(it = this.vs.iterator(); it.hasNext(); pos += a.length) {
         a = (float[])((float[])it.next());
         System.arraycopy(a, 0, allValues, pos, a.length);
      }

      System.arraycopy(this.v, 0, allValues, pos, this.index);
      this.vs.clear();
      this.v = allValues;
      short[] allUnits = new short[this.count];
      pos = 0;

      short[] a;
      for(it = this.us.iterator(); it.hasNext(); pos += a.length) {
         a = (short[])((short[])it.next());
         System.arraycopy(a, 0, allUnits, pos, a.length);
      }

      System.arraycopy(this.u, 0, allUnits, pos, this.index);
      this.us.clear();
      this.u = allUnits;
   }
}
