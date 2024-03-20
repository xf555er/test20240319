package org.apache.fop.afp.modca;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.fop.afp.Completable;
import org.apache.fop.afp.util.BinaryUtils;

public class NoOperation extends AbstractAFPObject implements Completable {
   private static final int MAX_DATA_LEN = 32759;
   private String content;

   public NoOperation(String content) {
      this.content = content;
   }

   public void writeToStream(OutputStream os) throws IOException {
      byte[] contentData = this.content.getBytes("Cp1146");
      int contentLen = contentData.length;
      if (contentLen > 32759) {
         contentLen = 32759;
      }

      byte[] data = new byte[9 + contentLen];
      data[0] = 90;
      byte[] rl1 = BinaryUtils.convert(8 + contentLen, 2);
      data[1] = rl1[0];
      data[2] = rl1[1];
      data[3] = -45;
      data[4] = -18;
      data[5] = -18;
      data[6] = 0;
      data[7] = 0;
      data[8] = 0;
      int pos = 9;

      for(int i = 0; i < contentLen; ++i) {
         data[pos++] = contentData[i];
      }

      os.write(data);
   }

   public boolean isComplete() {
      return true;
   }

   public void setComplete(boolean complete) {
   }

   public String toString() {
      return "NOP: " + this.content.substring(0, Math.min(64, this.content.length()));
   }
}
