package org.apache.fop.afp.ioca;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.fop.afp.modca.AbstractAFPObject;

public class BandImage extends AbstractAFPObject {
   public void writeToStream(OutputStream os) throws IOException {
      byte[] startData = new byte[]{-104, 5, 4, 8, 8, 8, 8};
      os.write(startData);
   }
}
