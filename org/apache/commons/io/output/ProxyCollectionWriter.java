package org.apache.commons.io.output;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import org.apache.commons.io.IOUtils;

public class ProxyCollectionWriter extends FilterCollectionWriter {
   public ProxyCollectionWriter(Collection writers) {
      super(writers);
   }

   public ProxyCollectionWriter(Writer... writers) {
      super(writers);
   }

   protected void afterWrite(int n) throws IOException {
   }

   public Writer append(char c) throws IOException {
      try {
         this.beforeWrite(1);
         super.append(c);
         this.afterWrite(1);
      } catch (IOException var3) {
         this.handleIOException(var3);
      }

      return this;
   }

   public Writer append(CharSequence csq) throws IOException {
      try {
         int len = IOUtils.length(csq);
         this.beforeWrite(len);
         super.append(csq);
         this.afterWrite(len);
      } catch (IOException var3) {
         this.handleIOException(var3);
      }

      return this;
   }

   public Writer append(CharSequence csq, int start, int end) throws IOException {
      try {
         this.beforeWrite(end - start);
         super.append(csq, start, end);
         this.afterWrite(end - start);
      } catch (IOException var5) {
         this.handleIOException(var5);
      }

      return this;
   }

   protected void beforeWrite(int n) throws IOException {
   }

   public void close() throws IOException {
      try {
         super.close();
      } catch (IOException var2) {
         this.handleIOException(var2);
      }

   }

   public void flush() throws IOException {
      try {
         super.flush();
      } catch (IOException var2) {
         this.handleIOException(var2);
      }

   }

   protected void handleIOException(IOException e) throws IOException {
      throw e;
   }

   public void write(char[] cbuf) throws IOException {
      try {
         int len = IOUtils.length(cbuf);
         this.beforeWrite(len);
         super.write(cbuf);
         this.afterWrite(len);
      } catch (IOException var3) {
         this.handleIOException(var3);
      }

   }

   public void write(char[] cbuf, int off, int len) throws IOException {
      try {
         this.beforeWrite(len);
         super.write(cbuf, off, len);
         this.afterWrite(len);
      } catch (IOException var5) {
         this.handleIOException(var5);
      }

   }

   public void write(int c) throws IOException {
      try {
         this.beforeWrite(1);
         super.write(c);
         this.afterWrite(1);
      } catch (IOException var3) {
         this.handleIOException(var3);
      }

   }

   public void write(String str) throws IOException {
      try {
         int len = IOUtils.length((CharSequence)str);
         this.beforeWrite(len);
         super.write(str);
         this.afterWrite(len);
      } catch (IOException var3) {
         this.handleIOException(var3);
      }

   }

   public void write(String str, int off, int len) throws IOException {
      try {
         this.beforeWrite(len);
         super.write(str, off, len);
         this.afterWrite(len);
      } catch (IOException var5) {
         this.handleIOException(var5);
      }

   }
}
