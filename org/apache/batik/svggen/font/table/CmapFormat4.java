package org.apache.batik.svggen.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;

public class CmapFormat4 extends CmapFormat {
   public int language;
   private int segCountX2;
   private int searchRange;
   private int entrySelector;
   private int rangeShift;
   private int[] endCode;
   private int[] startCode;
   private int[] idDelta;
   private int[] idRangeOffset;
   private int[] glyphIdArray;
   private int segCount;
   private int first;
   private int last;

   protected CmapFormat4(RandomAccessFile raf) throws IOException {
      super(raf);
      this.format = 4;
      this.segCountX2 = raf.readUnsignedShort();
      this.segCount = this.segCountX2 / 2;
      this.endCode = new int[this.segCount];
      this.startCode = new int[this.segCount];
      this.idDelta = new int[this.segCount];
      this.idRangeOffset = new int[this.segCount];
      this.searchRange = raf.readUnsignedShort();
      this.entrySelector = raf.readUnsignedShort();
      this.rangeShift = raf.readUnsignedShort();
      this.last = -1;

      int count;
      for(count = 0; count < this.segCount; ++count) {
         this.endCode[count] = raf.readUnsignedShort();
         if (this.endCode[count] > this.last) {
            this.last = this.endCode[count];
         }
      }

      raf.readUnsignedShort();

      for(count = 0; count < this.segCount; ++count) {
         this.startCode[count] = raf.readUnsignedShort();
         if (count == 0 || this.startCode[count] < this.first) {
            this.first = this.startCode[count];
         }
      }

      for(count = 0; count < this.segCount; ++count) {
         this.idDelta[count] = raf.readUnsignedShort();
      }

      for(count = 0; count < this.segCount; ++count) {
         this.idRangeOffset[count] = raf.readUnsignedShort();
      }

      count = (this.length - 16 - this.segCount * 8) / 2;
      this.glyphIdArray = new int[count];

      for(int i = 0; i < count; ++i) {
         this.glyphIdArray[i] = raf.readUnsignedShort();
      }

   }

   public int getFirst() {
      return this.first;
   }

   public int getLast() {
      return this.last;
   }

   public int mapCharCode(int charCode) {
      try {
         if (charCode < 0 || charCode >= 65534) {
            return 0;
         }

         for(int i = 0; i < this.segCount; ++i) {
            if (this.endCode[i] >= charCode) {
               if (this.startCode[i] <= charCode) {
                  if (this.idRangeOffset[i] > 0) {
                     return this.glyphIdArray[this.idRangeOffset[i] / 2 + (charCode - this.startCode[i]) - (this.segCount - i)];
                  }

                  return (this.idDelta[i] + charCode) % 65536;
               }
               break;
            }
         }
      } catch (ArrayIndexOutOfBoundsException var3) {
         System.err.println("error: Array out of bounds - " + var3.getMessage());
      }

      return 0;
   }

   public String toString() {
      return (new StringBuffer(80)).append(super.toString()).append(", segCountX2: ").append(this.segCountX2).append(", searchRange: ").append(this.searchRange).append(", entrySelector: ").append(this.entrySelector).append(", rangeShift: ").append(this.rangeShift).append(", endCode: ").append(intToStr(this.endCode)).append(", startCode: ").append(intToStr(this.startCode)).append(", idDelta: ").append(intToStr(this.idDelta)).append(", idRangeOffset: ").append(intToStr(this.idRangeOffset)).toString();
   }

   private static String intToStr(int[] array) {
      int nSlots = array.length;
      StringBuffer workBuff = new StringBuffer(nSlots * 8);
      workBuff.append('[');

      for(int i = 0; i < nSlots; ++i) {
         workBuff.append(array[i]);
         if (i < nSlots - 1) {
            workBuff.append(',');
         }
      }

      workBuff.append(']');
      return workBuff.toString();
   }
}
