package javax.xml.transform.stream;

import java.io.File;
import java.io.OutputStream;
import java.io.Writer;
import javax.xml.transform.Result;

public class StreamResult implements Result {
   public static final String FEATURE = "http://javax.xml.transform.stream.StreamResult/feature";
   private String systemId;
   private OutputStream outputStream;
   private Writer writer;

   public StreamResult() {
   }

   public StreamResult(OutputStream var1) {
      this.setOutputStream(var1);
   }

   public StreamResult(Writer var1) {
      this.setWriter(var1);
   }

   public StreamResult(String var1) {
      this.systemId = var1;
   }

   public StreamResult(File var1) {
      this.setSystemId(var1);
   }

   public void setOutputStream(OutputStream var1) {
      this.outputStream = var1;
   }

   public OutputStream getOutputStream() {
      return this.outputStream;
   }

   public void setWriter(Writer var1) {
      this.writer = var1;
   }

   public Writer getWriter() {
      return this.writer;
   }

   public void setSystemId(String var1) {
      this.systemId = var1;
   }

   public void setSystemId(File var1) {
      String var2 = var1.getAbsolutePath();
      if (File.separatorChar != '/') {
         var2 = var2.replace(File.separatorChar, '/');
      }

      if (var2.startsWith("/")) {
         this.systemId = "file://" + var2;
      } else {
         this.systemId = "file:///" + var2;
      }

   }

   public String getSystemId() {
      return this.systemId;
   }
}
