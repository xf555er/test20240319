package org.apache.fop.fonts.truetype;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class OFDirTabEntry {
   private byte[] tag = new byte[4];
   private long checksum;
   private long offset;
   private long length;

   public OFDirTabEntry() {
   }

   public OFDirTabEntry(long offset, long length) {
      this.offset = offset;
      this.length = length;
   }

   public String read(FontFileReader in) throws IOException {
      this.tag[0] = in.readTTFByte();
      this.tag[1] = in.readTTFByte();
      this.tag[2] = in.readTTFByte();
      this.tag[3] = in.readTTFByte();
      this.checksum = (long)in.readTTFLong();
      this.offset = in.readTTFULong();
      this.length = in.readTTFULong();
      return this.getTagString();
   }

   public String toString() {
      return "Read dir tab [" + Arrays.toString(this.tag) + "] offset: " + this.offset + " length: " + this.length + " name: " + this.getTagString();
   }

   public long getChecksum() {
      return this.checksum;
   }

   public long getLength() {
      return this.length;
   }

   public long getOffset() {
      return this.offset;
   }

   public byte[] getTag() {
      return this.tag;
   }

   public String getTagString() {
      try {
         return new String(this.tag, "ISO-8859-1");
      } catch (UnsupportedEncodingException var2) {
         return this.toString();
      }
   }
}
