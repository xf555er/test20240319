package org.apache.fop.afp.apps;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import org.apache.commons.io.HexDump;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.fop.afp.parser.MODCAParser;
import org.apache.fop.afp.parser.UnparsedStructuredField;

public class FontPatternExtractor {
   private PrintStream printStream;

   public FontPatternExtractor() {
      this.printStream = System.out;
   }

   public void extract(File file, File targetDir) throws IOException {
      InputStream in = new FileInputStream(file);

      try {
         MODCAParser parser = new MODCAParser(in);
         ByteArrayOutputStream baout = new ByteArrayOutputStream();

         UnparsedStructuredField strucField;
         while((strucField = parser.readNextStructuredField()) != null) {
            if (strucField.getSfTypeID() == 13889161) {
               byte[] sfData = strucField.getData();
               this.println(strucField.toString());
               HexDump.dump(sfData, 0L, this.printStream, 0);
               baout.write(sfData);
            }
         }

         ByteArrayInputStream bin = new ByteArrayInputStream(baout.toByteArray());
         IOUtils.closeQuietly((OutputStream)baout);
         DataInputStream din = new DataInputStream(bin);
         long len = (long)din.readInt() & 4294967295L;
         this.println("Length: " + len);
         if (din.skip(4L) != 4L) {
            throw new IOException("premature EOF when skipping checksum");
         }

         int tidLen = din.readUnsignedShort() - 2;
         byte[] tid = new byte[tidLen];
         din.readFully(tid);
         String filename = new String(tid, "ISO-8859-1");
         int asciiCount1 = this.countUSAsciiCharacters(filename);
         String filenameEBCDIC = new String(tid, "Cp1146");
         int asciiCount2 = this.countUSAsciiCharacters(filenameEBCDIC);
         this.println("TID: " + filename + " " + filenameEBCDIC);
         if (asciiCount2 > asciiCount1) {
            filename = filenameEBCDIC;
         }

         if (!filename.toLowerCase().endsWith(".pfb")) {
            filename = filename + ".pfb";
         }

         this.println("Output filename: " + filename);
         File out = new File(targetDir, filename);
         OutputStream fout = new FileOutputStream(out);

         try {
            IOUtils.copyLarge((InputStream)din, (OutputStream)fout);
         } finally {
            IOUtils.closeQuietly((OutputStream)fout);
         }
      } finally {
         IOUtils.closeQuietly((InputStream)in);
      }

   }

   private void println(String s) {
      this.printStream.println(s);
   }

   private void println() {
      this.printStream.println();
   }

   private int countUSAsciiCharacters(String filename) {
      int count = 0;
      int i = 0;

      for(int c = filename.length(); i < c; ++i) {
         if (filename.charAt(i) < 128) {
            ++count;
         }
      }

      return count;
   }

   public static void main(String[] args) {
      try {
         FontPatternExtractor app = new FontPatternExtractor();
         app.println("Font Pattern Extractor");
         app.println();
         if (args.length > 0) {
            String filename = args[0];
            File file = new File(filename);
            File targetDir = file.getParentFile();
            if (args.length > 1) {
               targetDir = new File(args[1]);
               targetDir.mkdirs();
            }

            app.extract(file, targetDir);
         } else {
            app.println("This tool tries to extract the PFB file from an AFP outline font.");
            app.println();
            app.println("Usage: Java -cp ... " + FontPatternExtractor.class.getName() + " <afp-font-file> [<target-dir>]");
            System.exit(-1);
         }
      } catch (Exception var5) {
         var5.printStackTrace();
         System.exit(-1);
      }

   }
}
