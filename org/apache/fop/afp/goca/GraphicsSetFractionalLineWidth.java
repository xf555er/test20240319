package org.apache.fop.afp.goca;

import java.io.IOException;
import java.io.OutputStream;

public class GraphicsSetFractionalLineWidth extends AbstractGraphicsDrawingOrder {
   private final float multiplier;

   public GraphicsSetFractionalLineWidth(float multiplier) {
      this.multiplier = multiplier;
   }

   public int getDataLength() {
      return 4;
   }

   public void writeToStream(OutputStream os) throws IOException {
      int integral = (int)this.multiplier;
      int fractional = (int)((this.multiplier - (float)integral) * 256.0F);
      byte[] data = new byte[]{this.getOrderCode(), 2, (byte)integral, (byte)fractional};
      os.write(data);
   }

   public String toString() {
      return "GraphicsSetFractionalLineWidth{multiplier=" + this.multiplier + "}";
   }

   byte getOrderCode() {
      return 17;
   }
}
