package net.jsign.mscab;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

class CABSignature {
   private final ByteBuffer buffer;
   public int header = 1048576;
   public long offset;
   public long length;
   private long filler;

   public CABSignature() {
      this.buffer = ByteBuffer.allocate(20).order(ByteOrder.LITTLE_ENDIAN);
   }

   public CABSignature(byte[] array) {
      this.buffer = ByteBuffer.wrap(array).order(ByteOrder.LITTLE_ENDIAN);
      this.load();
   }

   private void load() {
      this.buffer.rewind();
      this.header = this.buffer.getInt();
      this.offset = (long)this.buffer.getInt() & 4294967295L;
      this.length = (long)this.buffer.getInt() & 4294967295L;
      this.filler = this.buffer.getLong();
      this.buffer.flip();
   }

   private void save() {
      this.buffer.rewind();
      this.buffer.putInt(this.header);
      this.buffer.putInt((int)this.offset);
      this.buffer.putInt((int)this.length);
      this.buffer.putLong(this.filler);
      this.buffer.flip();
   }

   public byte[] array() {
      this.save();
      return this.buffer.array();
   }
}
