package org.apache.batik.svggen.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;

public class FeatureList {
   private int featureCount;
   private FeatureRecord[] featureRecords;
   private Feature[] features;

   public FeatureList(RandomAccessFile raf, int offset) throws IOException {
      raf.seek((long)offset);
      this.featureCount = raf.readUnsignedShort();
      this.featureRecords = new FeatureRecord[this.featureCount];
      this.features = new Feature[this.featureCount];

      int i;
      for(i = 0; i < this.featureCount; ++i) {
         this.featureRecords[i] = new FeatureRecord(raf);
      }

      for(i = 0; i < this.featureCount; ++i) {
         this.features[i] = new Feature(raf, offset + this.featureRecords[i].getOffset());
      }

   }

   public Feature findFeature(LangSys langSys, String tag) {
      if (tag.length() != 4) {
         return null;
      } else {
         int tagVal = tag.charAt(0) << 24 | tag.charAt(1) << 16 | tag.charAt(2) << 8 | tag.charAt(3);

         for(int i = 0; i < this.featureCount; ++i) {
            if (this.featureRecords[i].getTag() == tagVal && langSys.isFeatureIndexed(i)) {
               return this.features[i];
            }
         }

         return null;
      }
   }
}
