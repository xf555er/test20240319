package net.jsign.poi.poifs.filesystem;

import net.jsign.poi.util.LittleEndian;
import net.jsign.poi.util.LocaleUtil;

public enum FileMagic {
   OLE2(-2226271756974174256L),
   OOXML(new int[]{80, 75, 3, 4}),
   XML(new int[]{60, 63, 120, 109, 108}),
   BIFF2(new int[]{9, 0, 4, 0, 0, 0, 63, 0}),
   BIFF3(new int[]{9, 2, 6, 0, 0, 0, 63, 0}),
   BIFF4(new byte[][]{{9, 4, 6, 0, 0, 0, 63, 0}, {9, 4, 6, 0, 0, 0, 0, 1}}),
   MSWRITE(new byte[][]{{49, -66, 0, 0}, {50, -66, 0, 0}}),
   RTF(new String[]{"{\\rtf"}),
   PDF(new String[]{"%PDF"}),
   HTML(new String[]{"<!DOCTYP", "<html", "\n\r<html", "\r\n<html", "\r<html", "\n<html", "<HTML", "\r\n<HTML", "\n\r<HTML", "\r<HTML", "\n<HTML"}),
   WORD2(new int[]{219, 165, 45, 0}),
   JPEG(new byte[][]{{-1, -40, -1, -37}, {-1, -40, -1, -32, 63, 63, 74, 70, 73, 70, 0, 1}, {-1, -40, -1, -18}, {-1, -40, -1, -31, 63, 63, 69, 120, 105, 102, 0, 0}}),
   GIF(new String[]{"GIF87a", "GIF89a"}),
   PNG(new int[]{137, 80, 78, 71, 13, 10, 26, 10}),
   TIFF(new String[]{"II*\u0000", "MM\u0000*"}),
   WMF(new int[]{215, 205, 198, 154}),
   EMF(new int[]{1, 0, 0, 0, 63, 63, 63, 63, 63, 63, 63, 63, 63, 63, 63, 63, 63, 63, 63, 63, 63, 63, 63, 63, 63, 63, 63, 63, 63, 63, 63, 63, 63, 63, 63, 63, 63, 63, 63, 63, 32, 69, 77, 70}),
   BMP(new int[]{66, 77}),
   UNKNOWN(new byte[][]{new byte[0]});

   final byte[][] magic;

   private FileMagic(long magic) {
      this.magic = new byte[1][8];
      LittleEndian.putLong(this.magic[0], 0, magic);
   }

   private FileMagic(int... magic) {
      byte[] one = new byte[magic.length];

      for(int i = 0; i < magic.length; ++i) {
         one[i] = (byte)(magic[i] & 255);
      }

      this.magic = new byte[][]{one};
   }

   private FileMagic(byte[]... magic) {
      this.magic = magic;
   }

   private FileMagic(String... magic) {
      this.magic = new byte[magic.length][];
      int i = 0;
      String[] var5 = magic;
      int var6 = magic.length;

      for(int var7 = 0; var7 < var6; ++var7) {
         String s = var5[var7];
         this.magic[i++] = s.getBytes(LocaleUtil.CHARSET_1252);
      }

   }

   public static FileMagic valueOf(byte[] magic) {
      FileMagic[] var1 = values();
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         FileMagic fm = var1[var3];
         byte[][] var5 = fm.magic;
         int var6 = var5.length;

         for(int var7 = 0; var7 < var6; ++var7) {
            byte[] ma = var5[var7];
            if (magic.length >= ma.length && findMagic(ma, magic)) {
               return fm;
            }
         }
      }

      return UNKNOWN;
   }

   private static boolean findMagic(byte[] expected, byte[] actual) {
      int i = 0;
      byte[] var3 = expected;
      int var4 = expected.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         byte expectedByte = var3[var5];
         if (actual[i++] != expectedByte && expectedByte != 63) {
            return false;
         }
      }

      return true;
   }
}
