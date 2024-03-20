package org.apache.fop.afp.modca.triplets;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.fop.afp.util.BinaryUtils;

public class EncodingTriplet extends AbstractTriplet {
   private int encoding;

   public EncodingTriplet(int encoding) {
      super((byte)1);
      this.encoding = encoding;
   }

   public void writeToStream(OutputStream os) throws IOException {
      byte[] data = this.getData();
      byte[] encodingBytes = BinaryUtils.convert(this.encoding, 2);
      System.arraycopy(encodingBytes, 0, data, 4, encodingBytes.length);
      os.write(data);
   }

   public int getDataLength() {
      return 6;
   }
}
