package org.apache.fop.render.ps.fonts;

import java.io.IOException;
import org.apache.xmlgraphics.ps.PSGenerator;
import org.apache.xmlgraphics.util.io.ASCIIHexOutputStream;

public class PSTTFGenerator {
   private PSGenerator gen;
   private ASCIIHexOutputStream hexOut;
   public static final int MAX_BUFFER_SIZE = 32764;

   public PSTTFGenerator(PSGenerator gen) {
      this.gen = gen;
      this.hexOut = new ASCIIHexOutputStream(gen.getOutputStream());
   }

   public void startString() throws IOException {
      this.hexOut = new ASCIIHexOutputStream(this.gen.getOutputStream());
      this.gen.writeln("<");
   }

   public void write(String cmd) throws IOException {
      this.gen.write(cmd);
   }

   public void writeln(String cmd) throws IOException {
      this.gen.writeln(cmd);
   }

   public void streamBytes(byte[] byteArray, int offset, int length) throws IOException {
      if (length > 32764) {
         throw new UnsupportedOperationException("Attempting to write a string to a PostScript file that is greater than the buffer size.");
      } else {
         this.hexOut.write(byteArray, offset, length);
      }
   }

   public void endString() throws IOException {
      this.gen.write("00\n> ");
   }
}
