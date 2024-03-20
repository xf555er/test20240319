package net.jsign.pe;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DataDirectory {
   private final PEFile peFile;
   private final int index;

   DataDirectory(PEFile peFile, int index) {
      this.peFile = peFile;
      this.index = index;
   }

   public long getVirtualAddress() {
      return this.peFile.readDWord((long)this.peFile.getDataDirectoryOffset(), this.index * 8);
   }

   public int getSize() {
      return (int)this.peFile.readDWord((long)this.peFile.getDataDirectoryOffset(), this.index * 8 + 4);
   }

   public boolean exists() {
      return this.getVirtualAddress() != 0L && this.getSize() != 0;
   }

   public void erase() {
      this.peFile.write(this.getVirtualAddress(), new byte[this.getSize()]);
   }

   public boolean isTrailing() throws IOException {
      return this.getVirtualAddress() + (long)this.getSize() == this.peFile.channel.size();
   }

   public void write(long virtualAddress, int size) {
      ByteBuffer buffer = ByteBuffer.allocate(8);
      buffer.order(ByteOrder.LITTLE_ENDIAN);
      buffer.putInt((int)virtualAddress);
      buffer.putInt(size);
      this.peFile.write((long)(this.peFile.getDataDirectoryOffset() + this.index * 8), buffer.array());
   }
}
