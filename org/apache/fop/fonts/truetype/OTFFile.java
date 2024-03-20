package org.apache.fop.fonts.truetype;

import java.io.IOException;
import java.util.List;
import org.apache.fontbox.cff.CFFDataInput;
import org.apache.fontbox.cff.CFFFont;
import org.apache.fontbox.cff.CFFParser;
import org.apache.fontbox.cff.CFFType1Font;

public class OTFFile extends OpenFont {
   protected CFFFont fileFont;

   public OTFFile() throws IOException {
      this(true, false);
   }

   public OTFFile(boolean useKerning, boolean useAdvanced) throws IOException {
      super(useKerning, useAdvanced);
      this.checkForFontbox();
   }

   private void checkForFontbox() throws IOException {
      try {
         Class.forName("org.apache.fontbox.cff.CFFFont");
      } catch (ClassNotFoundException var2) {
         throw new IOException("The Fontbox jar was not found in the classpath. This is required for OTF CFF ssupport.");
      }
   }

   protected void updateBBoxAndOffset() throws IOException {
      Object bbox = this.fileFont.getTopDict().get("FontBBox");
      if (bbox != null) {
         List bboxList = (List)bbox;
         int[] bboxInt = new int[4];

         for(int i = 0; i < bboxInt.length; ++i) {
            bboxInt[i] = (Integer)bboxList.get(i);
         }

         OFMtxEntry[] var8 = this.mtxTab;
         int var5 = var8.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            OFMtxEntry o = var8[var6];
            o.setBoundingBox(bboxInt);
         }
      }

   }

   protected void initializeFont(FontFileReader in) throws IOException {
      this.fontFile = in;
      this.fontFile.seekSet(0L);
      CFFParser parser = new CFFParser();
      this.fileFont = (CFFFont)parser.parse(in.getAllBytes()).get(0);
      this.embedFontName = this.fileFont.getName();
   }

   protected void readName() throws IOException {
      Object familyName = this.fileFont.getTopDict().get("FamilyName");
      if (familyName != null && !familyName.equals("")) {
         this.familyNames.add(familyName.toString());
         this.fullName = familyName.toString();
      } else {
         this.fullName = this.fileFont.getName();
         this.familyNames.add(this.fullName);
      }

   }

   public static byte[] getCFFData(FontFileReader fontFile) throws IOException {
      byte[] cff = fontFile.getAllBytes();
      CFFDataInput input = new CFFDataInput(fontFile.getAllBytes());
      input.readBytes(4);
      short numTables = input.readShort();
      input.readShort();
      input.readShort();
      input.readShort();

      for(int q = 0; q < numTables; ++q) {
         String tagName = new String(input.readBytes(4));
         readLong(input);
         long offset = readLong(input);
         long length = readLong(input);
         if (tagName.equals("CFF ")) {
            cff = new byte[(int)length];
            System.arraycopy(fontFile.getAllBytes(), (int)offset, cff, 0, cff.length);
            break;
         }
      }

      return cff;
   }

   private static long readLong(CFFDataInput input) throws IOException {
      return (long)(input.readCard16() << 16 | input.readCard16());
   }

   public boolean isType1() {
      return this.fileFont instanceof CFFType1Font;
   }

   private static class Mapping {
      private int sid;
      private String name;
      private byte[] bytes;

      public void setSID(int sid) {
         this.sid = sid;
      }

      public int getSID() {
         return this.sid;
      }

      public void setName(String name) {
         this.name = name;
      }

      public String getName() {
         return this.name;
      }

      public void setBytes(byte[] bytes) {
         this.bytes = bytes;
      }

      public byte[] getBytes() {
         return this.bytes;
      }
   }
}
