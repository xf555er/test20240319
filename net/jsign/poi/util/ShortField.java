package net.jsign.poi.util;

public class ShortField {
   private short _value;
   private final int _offset;

   public ShortField(int offset) throws ArrayIndexOutOfBoundsException {
      if (offset < 0) {
         throw new ArrayIndexOutOfBoundsException("Illegal offset: " + offset);
      } else {
         this._offset = offset;
      }
   }

   public ShortField(int offset, byte[] data) throws ArrayIndexOutOfBoundsException {
      this(offset);
      this.readFromBytes(data);
   }

   public ShortField(int offset, short value, byte[] data) throws ArrayIndexOutOfBoundsException {
      this(offset);
      this.set(value, data);
   }

   public short get() {
      return this._value;
   }

   public void set(short value, byte[] data) throws ArrayIndexOutOfBoundsException {
      this._value = value;
      this.writeToBytes(data);
   }

   public void readFromBytes(byte[] data) throws ArrayIndexOutOfBoundsException {
      this._value = LittleEndian.getShort(data, this._offset);
   }

   public void writeToBytes(byte[] data) throws ArrayIndexOutOfBoundsException {
      LittleEndian.putShort(data, this._offset, this._value);
   }

   public String toString() {
      return String.valueOf(this._value);
   }
}
