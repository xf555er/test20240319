package org.apache.batik.util.io;

import java.io.IOException;

public class StringNormalizingReader extends NormalizingReader {
   protected String string;
   protected int length;
   protected int next;
   protected int line = 1;
   protected int column;

   public StringNormalizingReader(String s) {
      this.string = s;
      this.length = s.length();
   }

   public int read() throws IOException {
      int result = this.length == this.next ? -1 : this.string.charAt(this.next++);
      if (result <= 13) {
         switch (result) {
            case 10:
               this.column = 0;
               ++this.line;
               break;
            case 13:
               this.column = 0;
               ++this.line;
               int c = this.length == this.next ? -1 : this.string.charAt(this.next);
               if (c == 10) {
                  ++this.next;
               }

               return 10;
         }
      }

      return result;
   }

   public int getLine() {
      return this.line;
   }

   public int getColumn() {
      return this.column;
   }

   public void close() throws IOException {
      this.string = null;
   }
}
