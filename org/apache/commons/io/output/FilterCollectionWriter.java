package org.apache.commons.io.output;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.io.IOExceptionList;
import org.apache.commons.io.IOIndexedException;

public class FilterCollectionWriter extends Writer {
   protected final Collection EMPTY_WRITERS = Collections.emptyList();
   protected final Collection writers;

   protected FilterCollectionWriter(Collection writers) {
      this.writers = writers == null ? this.EMPTY_WRITERS : writers;
   }

   protected FilterCollectionWriter(Writer... writers) {
      this.writers = (Collection)(writers == null ? this.EMPTY_WRITERS : Arrays.asList(writers));
   }

   private List add(List causeList, int i, IOException e) {
      if (causeList == null) {
         causeList = new ArrayList();
      }

      ((List)causeList).add(new IOIndexedException(i, e));
      return (List)causeList;
   }

   public Writer append(char c) throws IOException {
      List causeList = null;
      int i = 0;

      for(Iterator var4 = this.writers.iterator(); var4.hasNext(); ++i) {
         Writer w = (Writer)var4.next();
         if (w != null) {
            try {
               w.append(c);
            } catch (IOException var7) {
               causeList = this.add(causeList, i, var7);
            }
         }
      }

      if (this.notEmpty(causeList)) {
         throw new IOExceptionList("append", causeList);
      } else {
         return this;
      }
   }

   public Writer append(CharSequence csq) throws IOException {
      List causeList = null;
      int i = 0;

      for(Iterator var4 = this.writers.iterator(); var4.hasNext(); ++i) {
         Writer w = (Writer)var4.next();
         if (w != null) {
            try {
               w.append(csq);
            } catch (IOException var7) {
               causeList = this.add(causeList, i, var7);
            }
         }
      }

      if (this.notEmpty(causeList)) {
         throw new IOExceptionList("append", causeList);
      } else {
         return this;
      }
   }

   public Writer append(CharSequence csq, int start, int end) throws IOException {
      List causeList = null;
      int i = 0;

      for(Iterator var6 = this.writers.iterator(); var6.hasNext(); ++i) {
         Writer w = (Writer)var6.next();
         if (w != null) {
            try {
               w.append(csq, start, end);
            } catch (IOException var9) {
               causeList = this.add(causeList, i, var9);
            }
         }
      }

      if (this.notEmpty(causeList)) {
         throw new IOExceptionList("append", causeList);
      } else {
         return this;
      }
   }

   public void close() throws IOException {
      List causeList = null;
      int i = 0;

      for(Iterator var3 = this.writers.iterator(); var3.hasNext(); ++i) {
         Writer w = (Writer)var3.next();
         if (w != null) {
            try {
               w.close();
            } catch (IOException var6) {
               causeList = this.add(causeList, i, var6);
            }
         }
      }

      if (this.notEmpty(causeList)) {
         throw new IOExceptionList("close", causeList);
      }
   }

   public void flush() throws IOException {
      List causeList = null;
      int i = 0;

      for(Iterator var3 = this.writers.iterator(); var3.hasNext(); ++i) {
         Writer w = (Writer)var3.next();
         if (w != null) {
            try {
               w.flush();
            } catch (IOException var6) {
               causeList = this.add(causeList, i, var6);
            }
         }
      }

      if (this.notEmpty(causeList)) {
         throw new IOExceptionList("flush", causeList);
      }
   }

   private boolean notEmpty(List causeList) {
      return causeList != null && !causeList.isEmpty();
   }

   public void write(char[] cbuf) throws IOException {
      List causeList = null;
      int i = 0;

      for(Iterator var4 = this.writers.iterator(); var4.hasNext(); ++i) {
         Writer w = (Writer)var4.next();
         if (w != null) {
            try {
               w.write(cbuf);
            } catch (IOException var7) {
               causeList = this.add(causeList, i, var7);
            }
         }
      }

      if (this.notEmpty(causeList)) {
         throw new IOExceptionList("write", causeList);
      }
   }

   public void write(char[] cbuf, int off, int len) throws IOException {
      List causeList = null;
      int i = 0;

      for(Iterator var6 = this.writers.iterator(); var6.hasNext(); ++i) {
         Writer w = (Writer)var6.next();
         if (w != null) {
            try {
               w.write(cbuf, off, len);
            } catch (IOException var9) {
               causeList = this.add(causeList, i, var9);
            }
         }
      }

      if (this.notEmpty(causeList)) {
         throw new IOExceptionList("write", causeList);
      }
   }

   public void write(int c) throws IOException {
      List causeList = null;
      int i = 0;

      for(Iterator var4 = this.writers.iterator(); var4.hasNext(); ++i) {
         Writer w = (Writer)var4.next();
         if (w != null) {
            try {
               w.write(c);
            } catch (IOException var7) {
               causeList = this.add(causeList, i, var7);
            }
         }
      }

      if (this.notEmpty(causeList)) {
         throw new IOExceptionList("write", causeList);
      }
   }

   public void write(String str) throws IOException {
      List causeList = null;
      int i = 0;

      for(Iterator var4 = this.writers.iterator(); var4.hasNext(); ++i) {
         Writer w = (Writer)var4.next();
         if (w != null) {
            try {
               w.write(str);
            } catch (IOException var7) {
               causeList = this.add(causeList, i, var7);
            }
         }
      }

      if (this.notEmpty(causeList)) {
         throw new IOExceptionList("write", causeList);
      }
   }

   public void write(String str, int off, int len) throws IOException {
      List causeList = null;
      int i = 0;

      for(Iterator var6 = this.writers.iterator(); var6.hasNext(); ++i) {
         Writer w = (Writer)var6.next();
         if (w != null) {
            try {
               w.write(str, off, len);
            } catch (IOException var9) {
               causeList = this.add(causeList, i, var9);
            }
         }
      }

      if (this.notEmpty(causeList)) {
         throw new IOExceptionList("write", causeList);
      }
   }
}
