package org.apache.xmlgraphics.image.codec.png;

import java.io.DataInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PNGChunk {
   int length;
   int type;
   byte[] data;
   int crc;
   String typeString;
   protected static final Log log = LogFactory.getLog(PNGChunk.class);

   public PNGChunk(int length, int type, byte[] data, int crc) {
      this.length = length;
      this.type = type;
      this.data = data;
      this.crc = crc;
      this.typeString = typeIntToString(this.type);
   }

   public int getLength() {
      return this.length;
   }

   public int getType() {
      return this.type;
   }

   public String getTypeString() {
      return this.typeString;
   }

   public byte[] getData() {
      return this.data;
   }

   public byte getByte(int offset) {
      return this.data[offset];
   }

   public int getInt1(int offset) {
      return this.data[offset] & 255;
   }

   public int getInt2(int offset) {
      return (this.data[offset] & 255) << 8 | this.data[offset + 1] & 255;
   }

   public int getInt4(int offset) {
      return (this.data[offset] & 255) << 24 | (this.data[offset + 1] & 255) << 16 | (this.data[offset + 2] & 255) << 8 | this.data[offset + 3] & 255;
   }

   public String getString4(int offset) {
      return "" + (char)this.data[offset] + (char)this.data[offset + 1] + (char)this.data[offset + 2] + (char)this.data[offset + 3];
   }

   public boolean isType(String typeName) {
      return this.typeString.equals(typeName);
   }

   public static PNGChunk readChunk(DataInputStream distream) {
      try {
         int length = distream.readInt();
         int type = distream.readInt();
         byte[] data = new byte[length];
         distream.readFully(data);
         int crc = distream.readInt();
         return new PNGChunk(length, type, data, crc);
      } catch (Exception var5) {
         var5.printStackTrace();
         return null;
      }
   }

   public static String getChunkType(DataInputStream distream) {
      try {
         distream.mark(8);
         distream.readInt();
         int type = distream.readInt();
         distream.reset();
         return typeIntToString(type);
      } catch (Exception var2) {
         var2.printStackTrace();
         return null;
      }
   }

   private static String typeIntToString(int type) {
      String typeString = "";
      typeString = typeString + (char)(type >> 24);
      typeString = typeString + (char)(type >> 16 & 255);
      typeString = typeString + (char)(type >> 8 & 255);
      typeString = typeString + (char)(type & 255);
      return typeString;
   }

   public static boolean skipChunk(DataInputStream distream) {
      try {
         int length = distream.readInt();
         distream.readInt();
         int skipped = distream.skipBytes(length);
         distream.readInt();
         if (skipped != length) {
            log.warn("Incorrect number of bytes skipped.");
            return false;
         } else {
            return true;
         }
      } catch (Exception var3) {
         log.warn(var3.getMessage());
         return false;
      }
   }

   public static enum ChunkType {
      IHDR,
      PLTE,
      IDAT,
      IEND,
      bKGD,
      cHRM,
      gAMA,
      hIST,
      iCCP,
      iTXt,
      pHYs,
      sBIT,
      sPLT,
      sRGB,
      sTER,
      tEXt,
      tIME,
      tRNS,
      zTXt;
   }
}
