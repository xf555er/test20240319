package org.apache.batik.svggen.font.table;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public abstract class Program {
   private short[] instructions;

   public short[] getInstructions() {
      return this.instructions;
   }

   protected void readInstructions(RandomAccessFile raf, int count) throws IOException {
      this.instructions = new short[count];

      for(int i = 0; i < count; ++i) {
         this.instructions[i] = (short)raf.readUnsignedByte();
      }

   }

   protected void readInstructions(ByteArrayInputStream bais, int count) {
      this.instructions = new short[count];

      for(int i = 0; i < count; ++i) {
         this.instructions[i] = (short)bais.read();
      }

   }
}
