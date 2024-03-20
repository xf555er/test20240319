package net.jsign.poi.util;

public class LongField {
   private long _value;
   private final int _offset;

   public LongField(int offset) throws ArrayIndexOutOfBoundsException {
      if (offset < 0) {
         throw new ArrayIndexOutOfBoundsException("Illegal offset: " + offset);
      } else {
         this._offset = offset;
      }
   }

   public LongField(int offset, long value, byte[] data) throws ArrayIndexOutOfBoundsException {
      this(offset);
      this.set(value, data);
   }

   public void set(long value, byte[] data) throws ArrayIndexOutOfBoundsException {
      this._value = value;
      this.writeToBytes(data);
   }

   public void writeToBytes(byte[] data) throws ArrayIndexOutOfBoundsException {
      LittleEndian.putLong(data, this._offset, this._value);
   }

   public String toString() {
      return String.valueOf(this._value);
   }
}
