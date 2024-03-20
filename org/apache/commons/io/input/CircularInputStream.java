package org.apache.commons.io.input;

import java.io.InputStream;
import java.util.Objects;

public class CircularInputStream extends InputStream {
   private long byteCount;
   private int position = -1;
   private final byte[] repeatedContent;
   private final long targetByteCount;

   private static byte[] validate(byte[] repeatContent) {
      Objects.requireNonNull(repeatContent, "repeatContent");
      byte[] var1 = repeatContent;
      int var2 = repeatContent.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         byte b = var1[var3];
         if (b == -1) {
            throw new IllegalArgumentException("repeatContent contains the end-of-stream marker -1");
         }
      }

      return repeatContent;
   }

   public CircularInputStream(byte[] repeatContent, long targetByteCount) {
      this.repeatedContent = validate(repeatContent);
      if (repeatContent.length == 0) {
         throw new IllegalArgumentException("repeatContent is empty.");
      } else {
         this.targetByteCount = targetByteCount;
      }
   }

   public int read() {
      if (this.targetByteCount >= 0L) {
         if (this.byteCount == this.targetByteCount) {
            return -1;
         }

         ++this.byteCount;
      }

      this.position = (this.position + 1) % this.repeatedContent.length;
      return this.repeatedContent[this.position] & 255;
   }
}
