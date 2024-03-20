package org.apache.batik.transcoder.wmf.tosvg;

import java.util.ArrayList;
import java.util.List;

public class MetaRecord {
   public int functionId;
   public int numPoints;
   private final List ptVector = new ArrayList();

   public void EnsureCapacity(int cc) {
   }

   public void AddElement(Object obj) {
      this.ptVector.add(obj);
   }

   public final void addElement(int iValue) {
      this.ptVector.add(iValue);
   }

   public Integer ElementAt(int offset) {
      return (Integer)this.ptVector.get(offset);
   }

   public final int elementAt(int offset) {
      return (Integer)this.ptVector.get(offset);
   }

   public static class StringRecord extends MetaRecord {
      public final String text;

      public StringRecord(String newText) {
         this.text = newText;
      }
   }

   public static class ByteRecord extends MetaRecord {
      public final byte[] bstr;

      public ByteRecord(byte[] bstr) {
         this.bstr = bstr;
      }
   }
}
