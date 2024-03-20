package org.apache.fop.afp.ioca;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.fop.afp.modca.AbstractAFPObject;
import org.apache.fop.afp.util.BinaryUtils;

public class TileSize extends AbstractAFPObject {
   private int hSize;
   private int vSize;

   public TileSize(int hsize, int vsize, int hresol, int vresol) {
      this.hSize = hsize;
      this.vSize = vsize;
   }

   public void writeToStream(OutputStream os) throws IOException {
      byte[] data = new byte[]{-74, 9, 0, 0, 0, 0, 0, 0, 0, 0, 1};
      byte[] w = BinaryUtils.convert(this.hSize, 4);
      data[2] = w[0];
      data[3] = w[1];
      data[4] = w[2];
      data[5] = w[3];
      byte[] h = BinaryUtils.convert(this.vSize, 4);
      data[6] = h[0];
      data[7] = h[1];
      data[8] = h[2];
      data[9] = h[3];
      os.write(data);
   }
}
