package org.apache.fop.afp.modca.triplets;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class AttributeValueTriplet extends AbstractTriplet {
   private String attVal;
   private int userEncoding;

   public AttributeValueTriplet(String attVal) {
      super((byte)54);
      this.userEncoding = -1;
      this.attVal = truncate(attVal, 250);
   }

   public AttributeValueTriplet(String attVal, int userEncoding) {
      this(attVal);
      this.userEncoding = userEncoding;
   }

   public void writeToStream(OutputStream os) throws IOException {
      byte[] data = super.getData();
      data[2] = 0;
      data[3] = 0;
      byte[] tleByteValue = null;

      byte[] tleByteValue;
      try {
         if (this.userEncoding != -1) {
            tleByteValue = this.attVal.getBytes("Cp" + this.userEncoding);
         } else {
            tleByteValue = this.attVal.getBytes("Cp1146");
         }
      } catch (UnsupportedEncodingException var5) {
         throw new IllegalArgumentException(this.attVal + " encoding failed");
      }

      System.arraycopy(tleByteValue, 0, data, 4, tleByteValue.length);
      os.write(data);
   }

   public int getDataLength() {
      return 4 + this.attVal.length();
   }

   public String toString() {
      return this.attVal;
   }
}
