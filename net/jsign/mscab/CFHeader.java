package net.jsign.mscab;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.security.MessageDigest;

class CFHeader {
   public final byte[] signature = new byte[4];
   public long csumHeader;
   public long cbCabinet;
   public long csumFolders;
   public long coffFiles;
   public long csumFiles;
   public byte versionMinor;
   public byte versionMajor;
   public int cFolders;
   public int cFiles;
   public int flags;
   public int setID;
   public int iCabinet;
   public int cbCFHeader;
   public short cbCFFolder;
   public short cbCFData;
   public byte[] abReserved;

   public CFHeader() {
   }

   public CFHeader(CFHeader origin) throws IOException {
      ByteBuffer buffer = ByteBuffer.allocate(origin.getHeaderSize()).order(ByteOrder.LITTLE_ENDIAN);
      origin.write(buffer);
      buffer.flip();
      this.readHeaderFirst(buffer);
      this.readHeaderSecond(buffer);
      if (this.cbCFHeader > 0) {
         buffer.get(this.abReserved);
      }

   }

   public void read(SeekableByteChannel channel) throws IOException {
      if (channel.size() < 44L) {
         throw new IOException("MSCabinet file too short");
      } else {
         ByteBuffer buffer = ByteBuffer.allocate(36).order(ByteOrder.LITTLE_ENDIAN);
         channel.read(buffer);
         buffer.flip();
         this.readHeaderFirst(buffer);
         if (this.isReservePresent()) {
            buffer.clear();
            buffer.limit(4);
            channel.read(buffer);
            buffer.flip();
            this.readHeaderSecond(buffer);
            if (this.cbCFHeader > 0) {
               ByteBuffer ab = ByteBuffer.wrap(this.abReserved);
               channel.read(ab);
            }
         }

      }
   }

   private void readHeaderFirst(ByteBuffer buffer) throws IOException {
      buffer.get(this.signature);
      if (this.signature[0] == 77 && this.signature[1] == 83 && this.signature[2] == 67 && this.signature[3] == 70) {
         this.csumHeader = (long)buffer.getInt() & 4294967295L;
         this.cbCabinet = (long)buffer.getInt() & 4294967295L;
         this.csumFolders = (long)buffer.getInt() & 4294967295L;
         this.coffFiles = (long)buffer.getInt() & 4294967295L;
         this.csumFiles = (long)buffer.getInt() & 4294967295L;
         this.versionMinor = buffer.get();
         this.versionMajor = buffer.get();
         this.cFolders = buffer.getShort() & '\uffff';
         this.cFiles = buffer.getShort() & '\uffff';
         this.flags = buffer.getShort() & '\uffff';
         this.setID = buffer.getShort();
         this.iCabinet = buffer.getShort() & '\uffff';
         this.abReserved = null;
      } else {
         throw new IOException("MSCabinet header signature not found");
      }
   }

   private void readHeaderSecond(ByteBuffer buffer) {
      if (this.isReservePresent()) {
         this.cbCFHeader = buffer.getShort() & '\uffff';
         this.cbCFFolder = (short)(buffer.get() & 255);
         this.cbCFData = (short)(buffer.get() & 255);
         if (this.cbCFHeader > 0) {
            this.abReserved = new byte[this.cbCFHeader];
         } else {
            this.abReserved = null;
         }
      } else {
         this.cbCFHeader = 0;
         this.cbCFFolder = 0;
         this.cbCFData = 0;
         this.abReserved = null;
      }

   }

   public void write(ByteBuffer buffer) {
      buffer.put(this.signature);
      buffer.putInt((int)this.csumHeader);
      buffer.putInt((int)this.cbCabinet);
      buffer.putInt((int)this.csumFolders);
      buffer.putInt((int)this.coffFiles);
      buffer.putInt((int)this.csumFiles);
      buffer.put(this.versionMinor);
      buffer.put(this.versionMajor);
      buffer.putShort((short)this.cFolders);
      buffer.putShort((short)this.cFiles);
      buffer.putShort((short)this.flags);
      buffer.putShort((short)this.setID);
      buffer.putShort((short)this.iCabinet);
      if (this.isReservePresent()) {
         buffer.putShort((short)this.cbCFHeader);
         buffer.put((byte)this.cbCFFolder);
         buffer.put((byte)this.cbCFData);
         if (this.cbCFHeader > 0) {
            buffer.put(this.abReserved);
         }
      }

   }

   public int getHeaderSize() {
      return this.isReservePresent() ? 40 + this.cbCFHeader : 36;
   }

   public void headerDigestUpdate(MessageDigest digest) {
      ByteBuffer buffer = ByteBuffer.allocate(36).order(ByteOrder.LITTLE_ENDIAN);
      buffer.put(this.signature);
      buffer.putInt((int)this.cbCabinet);
      buffer.putInt((int)this.csumFolders);
      buffer.putInt((int)this.coffFiles);
      buffer.putInt((int)this.csumFiles);
      buffer.put(this.versionMinor);
      buffer.put(this.versionMajor);
      buffer.putShort((short)this.cFolders);
      buffer.putShort((short)this.cFiles);
      buffer.putShort((short)this.flags);
      buffer.putShort((short)this.setID);
      buffer.putShort((short)this.iCabinet);
      buffer.flip();
      digest.update(buffer);
      if (this.abReserved != null) {
         digest.update(this.abReserved, 0, 2);
      }

   }

   public boolean hasPreviousCabinet() {
      return (1 & this.flags) != 0;
   }

   public boolean hasNextCabinet() {
      return (2 & this.flags) != 0;
   }

   public boolean isReservePresent() {
      return (4 & this.flags) != 0;
   }

   public boolean hasSignature() {
      return this.abReserved != null;
   }

   public CABSignature getSignature() {
      return this.abReserved != null ? new CABSignature(this.abReserved) : null;
   }
}
