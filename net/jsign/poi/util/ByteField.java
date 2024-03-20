package net.jsign.poi.util;

public class ByteField {
   private byte _value;
   private final int _offset;

   public ByteField(int offset) throws ArrayIndexOutOfBoundsException {
      this(offset, (byte)0);
   }

   public ByteField(int offset, byte value) throws ArrayIndexOutOfBoundsException {
      if (offset < 0) {
         throw new ArrayIndexOutOfBoundsException("offset cannot be negative");
      } else {
         this._offset = offset;
         this.set(value);
      }
   }

   public ByteField(int offset, byte[] data) throws ArrayIndexOutOfBoundsException {
      this(offset);
      this.readFromBytes(data);
   }

   public void set(byte value) {
      this._value = value;
   }

   public void set(byte value, byte[] data) throws ArrayIndexOutOfBoundsException {
      this.set(value);
      this.writeToBytes(data);
   }

   public void readFromBytes(byte[] data) throws ArrayIndexOutOfBoundsException {
      this._value = data[this._offset];
   }

   public void writeToBytes(byte[] data) throws ArrayIndexOutOfBoundsException {
      data[this._offset] = this._value;
   }

   public String toString() {
      return String.valueOf(this._value);
   }
}
