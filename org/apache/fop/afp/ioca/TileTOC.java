package org.apache.fop.afp.ioca;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.fop.afp.modca.AbstractAFPObject;

public class TileTOC extends AbstractAFPObject {
   public void writeToStream(OutputStream os) throws IOException {
      byte[] data = new byte[]{-2, -69, 0, 2, 0, 0};
      os.write(data);
   }
}
