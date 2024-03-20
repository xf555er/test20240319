package net.jsign.mscab;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.security.MessageDigest;

class CFFolder {
   private final ByteBuffer buffer;
   public long coffCabStart;
   public int cCFData;
   public int typeCompress;

   CFFolder() {
      this.buffer = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
   }

   public static CFFolder read(SeekableByteChannel channel) throws IOException {
      CFFolder folder = new CFFolder();
      int length = channel.read(folder.buffer);
      if (length < 8) {
         throw new IOException("Couldn't read CFFOLDER");
      } else {
         folder.load();
         return folder;
      }
   }

   private void load() {
      this.buffer.rewind();
      this.coffCabStart = (long)this.buffer.getInt() & 4294967295L;
      this.cCFData = this.buffer.getShort() & '\uffff';
      this.typeCompress = this.buffer.getShort() & '\uffff';
      this.buffer.flip();
   }

   private void save() {
      this.buffer.rewind();
      this.buffer.putInt((int)this.coffCabStart);
      this.buffer.putShort((short)this.cCFData);
      this.buffer.putShort((short)this.typeCompress);
      this.buffer.flip();
   }

   public void write(SeekableByteChannel channel) throws IOException {
      this.save();
      channel.write(this.buffer);
   }

   public void digest(MessageDigest digest) {
      this.save();
      digest.update(this.buffer.array());
   }
}
