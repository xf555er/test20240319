package org.apache.fop.pdf;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PDFStream extends AbstractPDFStream {
   protected StreamCache data;
   private transient Writer streamWriter;
   private transient char[] charBuffer;

   public PDFStream() {
      this.setUp();
   }

   public PDFStream(PDFDictionary dictionary) {
      super(dictionary);
      this.setUp();
   }

   public PDFStream(PDFDictionary dictionary, boolean encodeOnTheFly) {
      super(dictionary, encodeOnTheFly);
      this.setUp();
   }

   public PDFStream(boolean encodeOnTheFly) {
      super(encodeOnTheFly);
      this.setUp();
   }

   private void setUp() {
      try {
         this.data = StreamCacheFactory.getInstance().createStreamCache();
         this.streamWriter = new OutputStreamWriter(this.getBufferOutputStream(), "ISO-8859-1");
         this.streamWriter = new BufferedWriter(this.streamWriter);
      } catch (IOException var2) {
         throw new RuntimeException(var2);
      }
   }

   public void add(String s) {
      try {
         this.streamWriter.write(s);
      } catch (IOException var3) {
         var3.printStackTrace();
      }

   }

   public void add(StringBuffer sb) {
      try {
         int nHave = sb.length();
         if (this.charBuffer == null) {
            this.charBuffer = new char[nHave * 2];
         } else {
            int nAvail = this.charBuffer.length;
            if (nAvail < nHave) {
               int nAlloc = nAvail;

               while(true) {
                  if (nAlloc >= nHave) {
                     this.charBuffer = new char[nAlloc];
                     break;
                  }

                  nAlloc *= 2;
               }
            }
         }

         sb.getChars(0, nHave, this.charBuffer, 0);
         this.streamWriter.write(this.charBuffer, 0, nHave);
      } catch (IOException var5) {
         var5.printStackTrace();
      }

   }

   private void flush() throws IOException {
      this.streamWriter.flush();
   }

   public Writer getBufferWriter() {
      return this.streamWriter;
   }

   public OutputStream getBufferOutputStream() throws IOException {
      if (this.streamWriter != null) {
         this.flush();
      }

      return this.data.getOutputStream();
   }

   public void setData(byte[] data) throws IOException {
      this.data.clear();
      this.data.write(data);
   }

   public int getDataLength() {
      try {
         this.flush();
         return this.data.getSize();
      } catch (Exception var2) {
         var2.printStackTrace();
         return 0;
      }
   }

   protected int getSizeHint() throws IOException {
      this.flush();
      return this.data.getSize();
   }

   protected void outputRawStreamData(OutputStream out) throws IOException {
      this.flush();
      this.data.outputContents(out);
   }

   public int output(OutputStream stream) throws IOException {
      int len = super.output(stream);
      return len;
   }

   public String streamHashCode() throws IOException {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      this.outputRawStreamData(bos);

      try {
         MessageDigest md = MessageDigest.getInstance("MD5");
         byte[] thedigest = md.digest(bos.toByteArray());
         StringBuilder hex = new StringBuilder();
         byte[] var5 = thedigest;
         int var6 = thedigest.length;

         for(int var7 = 0; var7 < var6; ++var7) {
            byte b = var5[var7];
            hex.append(String.format("%02x", b));
         }

         return hex.toString();
      } catch (NoSuchAlgorithmException var9) {
         throw new IOException(var9);
      }
   }
}
