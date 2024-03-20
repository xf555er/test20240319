package org.apache.batik.svggen.font.table;

import java.io.ByteArrayInputStream;

public class GlyfSimpleDescript extends GlyfDescript {
   private int[] endPtsOfContours;
   private byte[] flags;
   private short[] xCoordinates;
   private short[] yCoordinates;
   private int count;

   public GlyfSimpleDescript(GlyfTable parentTable, short numberOfContours, ByteArrayInputStream bais) {
      super(parentTable, numberOfContours, bais);
      this.endPtsOfContours = new int[numberOfContours];

      int instructionCount;
      for(instructionCount = 0; instructionCount < numberOfContours; ++instructionCount) {
         this.endPtsOfContours[instructionCount] = bais.read() << 8 | bais.read();
      }

      this.count = this.endPtsOfContours[numberOfContours - 1] + 1;
      this.flags = new byte[this.count];
      this.xCoordinates = new short[this.count];
      this.yCoordinates = new short[this.count];
      instructionCount = bais.read() << 8 | bais.read();
      this.readInstructions(bais, instructionCount);
      this.readFlags(this.count, bais);
      this.readCoords(this.count, bais);
   }

   public int getEndPtOfContours(int i) {
      return this.endPtsOfContours[i];
   }

   public byte getFlags(int i) {
      return this.flags[i];
   }

   public short getXCoordinate(int i) {
      return this.xCoordinates[i];
   }

   public short getYCoordinate(int i) {
      return this.yCoordinates[i];
   }

   public boolean isComposite() {
      return false;
   }

   public int getPointCount() {
      return this.count;
   }

   public int getContourCount() {
      return this.getNumberOfContours();
   }

   private void readCoords(int count, ByteArrayInputStream bais) {
      short x = 0;
      short y = 0;

      int i;
      for(i = 0; i < count; ++i) {
         if ((this.flags[i] & 16) != 0) {
            if ((this.flags[i] & 2) != 0) {
               x += (short)bais.read();
            }
         } else if ((this.flags[i] & 2) != 0) {
            x += (short)(-((short)bais.read()));
         } else {
            x += (short)(bais.read() << 8 | bais.read());
         }

         this.xCoordinates[i] = x;
      }

      for(i = 0; i < count; ++i) {
         if ((this.flags[i] & 32) != 0) {
            if ((this.flags[i] & 4) != 0) {
               y += (short)bais.read();
            }
         } else if ((this.flags[i] & 4) != 0) {
            y += (short)(-((short)bais.read()));
         } else {
            y += (short)(bais.read() << 8 | bais.read());
         }

         this.yCoordinates[i] = y;
      }

   }

   private void readFlags(int flagCount, ByteArrayInputStream bais) {
      try {
         for(int index = 0; index < flagCount; ++index) {
            this.flags[index] = (byte)bais.read();
            if ((this.flags[index] & 8) != 0) {
               int repeats = bais.read();

               for(int i = 1; i <= repeats; ++i) {
                  this.flags[index + i] = this.flags[index];
               }

               index += repeats;
            }
         }
      } catch (ArrayIndexOutOfBoundsException var6) {
         System.out.println("error: array index out of bounds");
      }

   }
}
