package org.apache.fop.render.pcl.fonts.truetype;

import java.io.IOException;
import org.apache.fop.fonts.truetype.FontFileReader;

public class PCLTTFOS2FontTable extends PCLTTFTable {
   private int avgCharWidth;
   private int xHeight;
   private int widthClass;
   private int weightClass;
   private int capHeight;
   private int[] panose = new int[10];

   public PCLTTFOS2FontTable(FontFileReader in) throws IOException {
      super(in);
      int version = this.reader.readTTFUShort();
      this.avgCharWidth = this.reader.readTTFShort();
      this.weightClass = this.reader.readTTFShort();
      this.widthClass = this.reader.readTTFShort();
      this.skipShort(this.reader, 12);

      for(int i = 0; i < 10; ++i) {
         this.panose[i] = this.reader.readTTFByte();
      }

      this.skipLong(this.reader, 4);
      this.skipByte(this.reader, 4);
      this.skipShort(this.reader, 8);
      if (version >= 2) {
         this.skipLong(this.reader, 2);
         this.xHeight = this.reader.readTTFShort();
         this.capHeight = this.reader.readTTFShort();
      }

   }

   public int getAvgCharWidth() {
      return this.avgCharWidth;
   }

   public int getXHeight() {
      return this.xHeight;
   }

   public int getWidthClass() {
      return this.widthClass;
   }

   public int getWeightClass() {
      return this.weightClass;
   }

   public int getCapHeight() {
      return this.capHeight;
   }

   public int[] getPanose() {
      return this.panose;
   }
}
