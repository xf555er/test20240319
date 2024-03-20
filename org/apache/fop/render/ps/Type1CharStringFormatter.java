package org.apache.fop.render.ps;

import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import java.util.List;
import org.apache.fontbox.cff.CharStringCommand;

public class Type1CharStringFormatter {
   private ByteArrayOutputStream output;

   public byte[] format(List sequence) {
      this.output = new ByteArrayOutputStream();
      Iterator var2 = sequence.iterator();

      while(var2.hasNext()) {
         Object object = var2.next();
         if (object instanceof CharStringCommand) {
            this.writeCommand((CharStringCommand)object);
         } else {
            if (!(object instanceof Number)) {
               throw new IllegalArgumentException();
            }

            this.writeNumber(((Number)object).intValue());
         }
      }

      return this.output.toByteArray();
   }

   private void writeCommand(CharStringCommand command) {
      int[] value = command.getKey().getValue();
      int[] var3 = value;
      int var4 = value.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         int aValue = var3[var5];
         this.output.write(aValue);
      }

   }

   private void writeNumber(Integer number) {
      int value = number;
      if (value >= -107 && value <= 107) {
         this.output.write(value + 139);
      } else {
         int b1;
         int b2;
         if (value >= 108 && value <= 1131) {
            b1 = (value - 108) % 256;
            b2 = (value - 108 - b1) / 256 + 247;
            this.output.write(b2);
            this.output.write(b1);
         } else if (value >= -1131 && value <= -108) {
            b1 = -((value + 108) % 256);
            b2 = -((value + 108 + b1) / 256 - 251);
            this.output.write(b2);
            this.output.write(b1);
         } else {
            b1 = value >>> 24 & 255;
            b2 = value >>> 16 & 255;
            int b3 = value >>> 8 & 255;
            int b4 = value >>> 0 & 255;
            this.output.write(255);
            this.output.write(b1);
            this.output.write(b2);
            this.output.write(b3);
            this.output.write(b4);
         }
      }

   }
}
