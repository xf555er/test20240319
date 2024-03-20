package org.apache.batik.svggen.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;

public class NameRecord {
   private short platformId;
   private short encodingId;
   private short languageId;
   private short nameId;
   private short stringLength;
   private short stringOffset;
   private String record;

   protected NameRecord(RandomAccessFile raf) throws IOException {
      this.platformId = raf.readShort();
      this.encodingId = raf.readShort();
      this.languageId = raf.readShort();
      this.nameId = raf.readShort();
      this.stringLength = raf.readShort();
      this.stringOffset = raf.readShort();
   }

   public short getEncodingId() {
      return this.encodingId;
   }

   public short getLanguageId() {
      return this.languageId;
   }

   public short getNameId() {
      return this.nameId;
   }

   public short getPlatformId() {
      return this.platformId;
   }

   public String getRecordString() {
      return this.record;
   }

   protected void loadString(RandomAccessFile raf, int stringStorageOffset) throws IOException {
      StringBuffer sb = new StringBuffer();
      raf.seek((long)(stringStorageOffset + this.stringOffset));
      int i;
      if (this.platformId == 0) {
         for(i = 0; i < this.stringLength / 2; ++i) {
            sb.append(raf.readChar());
         }
      } else if (this.platformId == 1) {
         for(i = 0; i < this.stringLength; ++i) {
            sb.append((char)raf.readByte());
         }
      } else if (this.platformId == 2) {
         for(i = 0; i < this.stringLength; ++i) {
            sb.append((char)raf.readByte());
         }
      } else if (this.platformId == 3) {
         for(int i = 0; i < this.stringLength / 2; ++i) {
            char c = raf.readChar();
            sb.append(c);
         }
      }

      this.record = sb.toString();
   }
}
