package org.apache.commons.io.output;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;
import org.apache.commons.io.IOUtils;

public class ProxyWriter extends FilterWriter {
   public ProxyWriter(Writer proxy) {
      super(proxy);
   }

   public Writer append(char c) throws IOException {
      try {
         this.beforeWrite(1);
         this.out.append(c);
         this.afterWrite(1);
      } catch (IOException var3) {
         this.handleIOException(var3);
      }

      return this;
   }

   public Writer append(CharSequence csq, int start, int end) throws IOException {
      try {
         this.beforeWrite(end - start);
         this.out.append(csq, start, end);
         this.afterWrite(end - start);
      } catch (IOException var5) {
         this.handleIOException(var5);
      }

      return this;
   }

   public Writer append(CharSequence csq) throws IOException {
      try {
         int len = IOUtils.length(csq);
         this.beforeWrite(len);
         this.out.append(csq);
         this.afterWrite(len);
      } catch (IOException var3) {
         this.handleIOException(var3);
      }

      return this;
   }

   public void write(int c) throws IOException {
      try {
         this.beforeWrite(1);
         this.out.write(c);
         this.afterWrite(1);
      } catch (IOException var3) {
         this.handleIOException(var3);
      }

   }

   public void write(char[] cbuf) throws IOException {
      try {
         int len = IOUtils.length(cbuf);
         this.beforeWrite(len);
         this.out.write(cbuf);
         this.afterWrite(len);
      } catch (IOException var3) {
         this.handleIOException(var3);
      }

   }

   public void write(char[] cbuf, int off, int len) throws IOException {
      try {
         this.beforeWrite(len);
         this.out.write(cbuf, off, len);
         this.afterWrite(len);
      } catch (IOException var5) {
         this.handleIOException(var5);
      }

   }

   public void write(String str) throws IOException {
      try {
         int len = IOUtils.length((CharSequence)str);
         this.beforeWrite(len);
         this.out.write(str);
         this.afterWrite(len);
      } catch (IOException var3) {
         this.handleIOException(var3);
      }

   }

   public void write(String str, int off, int len) throws IOException {
      try {
         this.beforeWrite(len);
         this.out.write(str, off, len);
         this.afterWrite(len);
      } catch (IOException var5) {
         this.handleIOException(var5);
      }

   }

   public void flush() throws IOException {
      try {
         this.out.flush();
      } catch (IOException var2) {
         this.handleIOException(var2);
      }

   }

   public void close() throws IOException {
      IOUtils.close(this.out, this::handleIOException);
   }

   protected void beforeWrite(int n) throws IOException {
   }

   protected void afterWrite(int n) throws IOException {
   }

   protected void handleIOException(IOException e) throws IOException {
      throw e;
   }
}
