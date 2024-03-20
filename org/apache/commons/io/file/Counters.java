package org.apache.commons.io.file;

import java.math.BigInteger;
import java.util.Objects;

public class Counters {
   public static Counter bigIntegerCounter() {
      return new BigIntegerCounter();
   }

   public static PathCounters bigIntegerPathCounters() {
      return new BigIntegerPathCounters();
   }

   public static Counter longCounter() {
      return new LongCounter();
   }

   public static PathCounters longPathCounters() {
      return new LongPathCounters();
   }

   public static Counter noopCounter() {
      return Counters.NoopCounter.INSTANCE;
   }

   public static PathCounters noopPathCounters() {
      return Counters.NoopPathCounters.INSTANCE;
   }

   public interface PathCounters {
      Counter getByteCounter();

      Counter getDirectoryCounter();

      Counter getFileCounter();

      default void reset() {
      }
   }

   private static final class NoopPathCounters extends AbstractPathCounters {
      static final NoopPathCounters INSTANCE = new NoopPathCounters();

      private NoopPathCounters() {
         super(Counters.noopCounter(), Counters.noopCounter(), Counters.noopCounter());
      }
   }

   private static final class NoopCounter implements Counter {
      static final NoopCounter INSTANCE = new NoopCounter();

      public void add(long add) {
      }

      public long get() {
         return 0L;
      }

      public BigInteger getBigInteger() {
         return BigInteger.ZERO;
      }

      public Long getLong() {
         return 0L;
      }

      public void increment() {
      }
   }

   private static final class LongPathCounters extends AbstractPathCounters {
      protected LongPathCounters() {
         super(Counters.longCounter(), Counters.longCounter(), Counters.longCounter());
      }
   }

   private static final class LongCounter implements Counter {
      private long value;

      private LongCounter() {
      }

      public void add(long add) {
         this.value += add;
      }

      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         } else if (!(obj instanceof Counter)) {
            return false;
         } else {
            Counter other = (Counter)obj;
            return this.value == other.get();
         }
      }

      public long get() {
         return this.value;
      }

      public BigInteger getBigInteger() {
         return BigInteger.valueOf(this.value);
      }

      public Long getLong() {
         return this.value;
      }

      public int hashCode() {
         return Objects.hash(new Object[]{this.value});
      }

      public void increment() {
         ++this.value;
      }

      public String toString() {
         return Long.toString(this.value);
      }

      public void reset() {
         this.value = 0L;
      }

      // $FF: synthetic method
      LongCounter(Object x0) {
         this();
      }
   }

   public interface Counter {
      void add(long var1);

      long get();

      BigInteger getBigInteger();

      Long getLong();

      void increment();

      default void reset() {
      }
   }

   private static final class BigIntegerPathCounters extends AbstractPathCounters {
      protected BigIntegerPathCounters() {
         super(Counters.bigIntegerCounter(), Counters.bigIntegerCounter(), Counters.bigIntegerCounter());
      }
   }

   private static final class BigIntegerCounter implements Counter {
      private BigInteger value;

      private BigIntegerCounter() {
         this.value = BigInteger.ZERO;
      }

      public void add(long val) {
         this.value = this.value.add(BigInteger.valueOf(val));
      }

      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         } else if (!(obj instanceof Counter)) {
            return false;
         } else {
            Counter other = (Counter)obj;
            return Objects.equals(this.value, other.getBigInteger());
         }
      }

      public long get() {
         return this.value.longValueExact();
      }

      public BigInteger getBigInteger() {
         return this.value;
      }

      public Long getLong() {
         return this.value.longValueExact();
      }

      public int hashCode() {
         return Objects.hash(new Object[]{this.value});
      }

      public void increment() {
         this.value = this.value.add(BigInteger.ONE);
      }

      public String toString() {
         return this.value.toString();
      }

      public void reset() {
         this.value = BigInteger.ZERO;
      }

      // $FF: synthetic method
      BigIntegerCounter(Object x0) {
         this();
      }
   }

   private static class AbstractPathCounters implements PathCounters {
      private final Counter byteCounter;
      private final Counter directoryCounter;
      private final Counter fileCounter;

      protected AbstractPathCounters(Counter byteCounter, Counter directoryCounter, Counter fileCounter) {
         this.byteCounter = byteCounter;
         this.directoryCounter = directoryCounter;
         this.fileCounter = fileCounter;
      }

      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         } else if (!(obj instanceof AbstractPathCounters)) {
            return false;
         } else {
            AbstractPathCounters other = (AbstractPathCounters)obj;
            return Objects.equals(this.byteCounter, other.byteCounter) && Objects.equals(this.directoryCounter, other.directoryCounter) && Objects.equals(this.fileCounter, other.fileCounter);
         }
      }

      public Counter getByteCounter() {
         return this.byteCounter;
      }

      public Counter getDirectoryCounter() {
         return this.directoryCounter;
      }

      public Counter getFileCounter() {
         return this.fileCounter;
      }

      public int hashCode() {
         return Objects.hash(new Object[]{this.byteCounter, this.directoryCounter, this.fileCounter});
      }

      public void reset() {
         this.byteCounter.reset();
         this.directoryCounter.reset();
         this.fileCounter.reset();
      }

      public String toString() {
         return String.format("%,d files, %,d directories, %,d bytes", this.fileCounter.get(), this.directoryCounter.get(), this.byteCounter.get());
      }
   }
}
