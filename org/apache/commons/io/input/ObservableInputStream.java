package org.apache.commons.io.input;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.io.IOUtils;

public class ObservableInputStream extends ProxyInputStream {
   private final List observers;

   public ObservableInputStream(InputStream inputStream) {
      this(inputStream, (List)(new ArrayList()));
   }

   private ObservableInputStream(InputStream inputStream, List observers) {
      super(inputStream);
      this.observers = observers;
   }

   public ObservableInputStream(InputStream inputStream, Observer... observers) {
      this(inputStream, Arrays.asList(observers));
   }

   public void add(Observer observer) {
      this.observers.add(observer);
   }

   public void close() throws IOException {
      IOException ioe = null;

      try {
         super.close();
      } catch (IOException var3) {
         ioe = var3;
      }

      if (ioe == null) {
         this.noteClosed();
      } else {
         this.noteError(ioe);
      }

   }

   public void consume() throws IOException {
      byte[] buffer = IOUtils.byteArray();

      while(this.read(buffer) != -1) {
      }

   }

   public List getObservers() {
      return this.observers;
   }

   protected void noteClosed() throws IOException {
      Iterator var1 = this.getObservers().iterator();

      while(var1.hasNext()) {
         Observer observer = (Observer)var1.next();
         observer.closed();
      }

   }

   protected void noteDataByte(int value) throws IOException {
      Iterator var2 = this.getObservers().iterator();

      while(var2.hasNext()) {
         Observer observer = (Observer)var2.next();
         observer.data(value);
      }

   }

   protected void noteDataBytes(byte[] buffer, int offset, int length) throws IOException {
      Iterator var4 = this.getObservers().iterator();

      while(var4.hasNext()) {
         Observer observer = (Observer)var4.next();
         observer.data(buffer, offset, length);
      }

   }

   protected void noteError(IOException exception) throws IOException {
      Iterator var2 = this.getObservers().iterator();

      while(var2.hasNext()) {
         Observer observer = (Observer)var2.next();
         observer.error(exception);
      }

   }

   protected void noteFinished() throws IOException {
      Iterator var1 = this.getObservers().iterator();

      while(var1.hasNext()) {
         Observer observer = (Observer)var1.next();
         observer.finished();
      }

   }

   private void notify(byte[] buffer, int offset, int result, IOException ioe) throws IOException {
      if (ioe != null) {
         this.noteError(ioe);
         throw ioe;
      } else {
         if (result == -1) {
            this.noteFinished();
         } else if (result > 0) {
            this.noteDataBytes(buffer, offset, result);
         }

      }
   }

   public int read() throws IOException {
      int result = 0;
      IOException ioe = null;

      try {
         result = super.read();
      } catch (IOException var4) {
         ioe = var4;
      }

      if (ioe != null) {
         this.noteError(ioe);
         throw ioe;
      } else {
         if (result == -1) {
            this.noteFinished();
         } else {
            this.noteDataByte(result);
         }

         return result;
      }
   }

   public int read(byte[] buffer) throws IOException {
      int result = 0;
      IOException ioe = null;

      try {
         result = super.read(buffer);
      } catch (IOException var5) {
         ioe = var5;
      }

      this.notify(buffer, 0, result, ioe);
      return result;
   }

   public int read(byte[] buffer, int offset, int length) throws IOException {
      int result = 0;
      IOException ioe = null;

      try {
         result = super.read(buffer, offset, length);
      } catch (IOException var7) {
         ioe = var7;
      }

      this.notify(buffer, offset, result, ioe);
      return result;
   }

   public void remove(Observer observer) {
      this.observers.remove(observer);
   }

   public void removeAllObservers() {
      this.observers.clear();
   }

   public abstract static class Observer {
      public void closed() throws IOException {
      }

      public void data(byte[] buffer, int offset, int length) throws IOException {
      }

      public void data(int value) throws IOException {
      }

      public void error(IOException exception) throws IOException {
         throw exception;
      }

      public void finished() throws IOException {
      }
   }
}
