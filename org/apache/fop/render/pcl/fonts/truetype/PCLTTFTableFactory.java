package org.apache.fop.render.pcl.fonts.truetype;

import java.io.IOException;
import org.apache.fop.fonts.truetype.FontFileReader;
import org.apache.fop.fonts.truetype.OFTableName;

public final class PCLTTFTableFactory {
   private FontFileReader reader;

   private PCLTTFTableFactory(FontFileReader reader) {
      this.reader = reader;
   }

   public static PCLTTFTableFactory getInstance(FontFileReader reader) {
      return new PCLTTFTableFactory(reader);
   }

   public PCLTTFTable newInstance(OFTableName tableName) throws IOException {
      if (tableName == OFTableName.PCLT) {
         return new PCLTTFPCLTFontTable(this.reader);
      } else if (tableName == OFTableName.OS2) {
         return new PCLTTFOS2FontTable(this.reader);
      } else {
         return tableName == OFTableName.POST ? new PCLTTFPOSTFontTable(this.reader) : null;
      }
   }
}
