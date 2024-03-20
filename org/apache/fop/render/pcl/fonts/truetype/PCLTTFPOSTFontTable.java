package org.apache.fop.render.pcl.fonts.truetype;

import java.io.IOException;
import org.apache.fop.fonts.truetype.FontFileReader;

public class PCLTTFPOSTFontTable extends PCLTTFTable {
   private int underlinePosition;
   private int underlineThickness;
   private int isFixedPitch;

   public PCLTTFPOSTFontTable(FontFileReader in) throws IOException {
      super(in);
      this.reader.readTTFLong();
      this.reader.readTTFLong();
      this.underlinePosition = this.reader.readTTFShort();
      this.underlineThickness = this.reader.readTTFShort();
      this.isFixedPitch = (int)this.reader.readTTFULong();
   }

   public int getUnderlinePosition() {
      return this.underlinePosition;
   }

   public int getUnderlineThickness() {
      return this.underlineThickness;
   }

   public int getIsFixedPitch() {
      return this.isFixedPitch;
   }
}
