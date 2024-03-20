package org.apache.fop.afp.ioca;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.fop.afp.modca.AbstractAFPObject;

public class TilePosition extends AbstractAFPObject {
   public void writeToStream(OutputStream os) throws IOException {
      byte[] startData = new byte[]{-75, 8, 0, 0, 0, 0, 0, 0, 0, 0};
      os.write(startData);
   }
}
