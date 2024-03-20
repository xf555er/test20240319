package org.apache.fop.afp.modca;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.fop.afp.modca.triplets.MappingOptionTriplet;
import org.apache.fop.afp.util.BinaryUtils;

public class MapImageObject extends AbstractTripletStructuredObject {
   public MapImageObject(byte mappingOption) {
      this.addTriplet(new MappingOptionTriplet(mappingOption));
   }

   public void writeToStream(OutputStream os) throws IOException {
      byte[] data = new byte[11];
      this.copySF(data, (byte)-85, (byte)-5);
      int tripletLen = this.getTripletDataLength();
      byte[] len = BinaryUtils.convert(10 + tripletLen, 2);
      data[1] = len[0];
      data[2] = len[1];
      len = BinaryUtils.convert(2 + tripletLen, 2);
      data[9] = len[0];
      data[10] = len[1];
      os.write(data);
      this.writeTriplets(os);
   }
}
