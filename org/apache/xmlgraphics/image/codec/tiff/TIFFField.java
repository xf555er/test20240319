package org.apache.xmlgraphics.image.codec.tiff;

import java.io.Serializable;

public class TIFFField implements Comparable, Serializable {
   public static final int TIFF_BYTE = 1;
   public static final int TIFF_ASCII = 2;
   public static final int TIFF_SHORT = 3;
   public static final int TIFF_LONG = 4;
   public static final int TIFF_RATIONAL = 5;
   public static final int TIFF_SBYTE = 6;
   public static final int TIFF_UNDEFINED = 7;
   public static final int TIFF_SSHORT = 8;
   public static final int TIFF_SLONG = 9;
   public static final int TIFF_SRATIONAL = 10;
   public static final int TIFF_FLOAT = 11;
   public static final int TIFF_DOUBLE = 12;
   private static final long serialVersionUID = 207783128222415437L;
   int tag;
   int type;
   int count;
   Object data;

   TIFFField() {
   }

   public TIFFField(int tag, int type, int count, Object data) {
      this.tag = tag;
      this.type = type;
      this.count = count;
      this.data = data;
   }

   public int getTag() {
      return this.tag;
   }

   public int getType() {
      return this.type;
   }

   public int getCount() {
      return this.count;
   }

   public byte[] getAsBytes() {
      return (byte[])((byte[])this.data);
   }

   public char[] getAsChars() {
      return (char[])((char[])this.data);
   }

   public short[] getAsShorts() {
      return (short[])((short[])this.data);
   }

   public int[] getAsInts() {
      return (int[])((int[])this.data);
   }

   public long[] getAsLongs() {
      return (long[])((long[])this.data);
   }

   public float[] getAsFloats() {
      return (float[])((float[])this.data);
   }

   public double[] getAsDoubles() {
      return (double[])((double[])this.data);
   }

   public int[][] getAsSRationals() {
      return (int[][])((int[][])this.data);
   }

   public long[][] getAsRationals() {
      return (long[][])((long[][])this.data);
   }

   public int getAsInt(int index) {
      switch (this.type) {
         case 1:
         case 7:
            return ((byte[])((byte[])this.data))[index] & 255;
         case 2:
         case 4:
         case 5:
         default:
            throw new ClassCastException();
         case 3:
            return ((char[])((char[])this.data))[index] & '\uffff';
         case 6:
            return ((byte[])((byte[])this.data))[index];
         case 8:
            return ((short[])((short[])this.data))[index];
         case 9:
            return ((int[])((int[])this.data))[index];
      }
   }

   public long getAsLong(int index) {
      switch (this.type) {
         case 1:
         case 7:
            return (long)(((byte[])((byte[])this.data))[index] & 255);
         case 2:
         case 5:
         default:
            throw new ClassCastException();
         case 3:
            return (long)(((char[])((char[])this.data))[index] & '\uffff');
         case 4:
            return ((long[])((long[])this.data))[index];
         case 6:
            return (long)((byte[])((byte[])this.data))[index];
         case 8:
            return (long)((short[])((short[])this.data))[index];
         case 9:
            return (long)((int[])((int[])this.data))[index];
      }
   }

   public float getAsFloat(int index) {
      switch (this.type) {
         case 1:
            return (float)(((byte[])((byte[])this.data))[index] & 255);
         case 2:
         case 7:
         default:
            throw new ClassCastException();
         case 3:
            return (float)(((char[])((char[])this.data))[index] & '\uffff');
         case 4:
            return (float)((long[])((long[])this.data))[index];
         case 5:
            long[] lvalue = this.getAsRational(index);
            return (float)((double)lvalue[0] / (double)lvalue[1]);
         case 6:
            return (float)((byte[])((byte[])this.data))[index];
         case 8:
            return (float)((short[])((short[])this.data))[index];
         case 9:
            return (float)((int[])((int[])this.data))[index];
         case 10:
            int[] ivalue = this.getAsSRational(index);
            return (float)((double)ivalue[0] / (double)ivalue[1]);
         case 11:
            return ((float[])((float[])this.data))[index];
         case 12:
            return (float)((double[])((double[])this.data))[index];
      }
   }

   public double getAsDouble(int index) {
      switch (this.type) {
         case 1:
            return (double)(((byte[])((byte[])this.data))[index] & 255);
         case 2:
         case 7:
         default:
            throw new ClassCastException();
         case 3:
            return (double)(((char[])((char[])this.data))[index] & '\uffff');
         case 4:
            return (double)((long[])((long[])this.data))[index];
         case 5:
            long[] lvalue = this.getAsRational(index);
            return (double)lvalue[0] / (double)lvalue[1];
         case 6:
            return (double)((byte[])((byte[])this.data))[index];
         case 8:
            return (double)((short[])((short[])this.data))[index];
         case 9:
            return (double)((int[])((int[])this.data))[index];
         case 10:
            int[] ivalue = this.getAsSRational(index);
            return (double)ivalue[0] / (double)ivalue[1];
         case 11:
            return (double)((float[])((float[])this.data))[index];
         case 12:
            return ((double[])((double[])this.data))[index];
      }
   }

   public String getAsString(int index) {
      return ((String[])((String[])this.data))[index];
   }

   public int[] getAsSRational(int index) {
      return ((int[][])((int[][])this.data))[index];
   }

   public long[] getAsRational(int index) {
      return ((long[][])((long[][])this.data))[index];
   }

   public int compareTo(Object o) {
      if (o == null) {
         throw new NullPointerException();
      } else {
         int oTag = ((TIFFField)o).getTag();
         if (this.tag < oTag) {
            return -1;
         } else {
            return this.tag > oTag ? 1 : 0;
         }
      }
   }
}
