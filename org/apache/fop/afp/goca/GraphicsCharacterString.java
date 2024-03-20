package org.apache.fop.afp.goca;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.CharacterCodingException;
import org.apache.fop.afp.fonts.CharacterSet;

public class GraphicsCharacterString extends AbstractGraphicsCoord {
   protected static final int MAX_STR_LEN = 255;
   private final String str;
   private final CharacterSet charSet;

   public GraphicsCharacterString(String str, int x, int y, CharacterSet charSet) {
      super(x, y);
      this.str = truncate(str, 255);
      this.charSet = charSet;
   }

   byte getOrderCode() {
      return (byte)(this.isRelative() ? -125 : -61);
   }

   public int getDataLength() {
      try {
         return super.getDataLength() + this.getStringAsBytes().length;
      } catch (IOException var2) {
         throw new RuntimeException(var2);
      }
   }

   public void writeToStream(OutputStream os) throws IOException {
      byte[] data = this.getData();
      byte[] strData = this.getStringAsBytes();
      System.arraycopy(strData, 0, data, 6, strData.length);
      os.write(data);
   }

   private byte[] getStringAsBytes() throws UnsupportedEncodingException, CharacterCodingException {
      return this.charSet.encodeChars(this.str).getBytes();
   }

   public String toString() {
      return "GraphicsCharacterString{" + (this.coords != null ? "x=" + this.coords[0] + ", y=" + this.coords[1] : "") + "str='" + this.str + "'}";
   }
}
