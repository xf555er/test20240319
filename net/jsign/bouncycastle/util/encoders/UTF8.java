package net.jsign.bouncycastle.util.encoders;

public class UTF8 {
   private static final short[] firstUnitTable = new short[128];
   private static final byte[] transitionTable = new byte[112];

   private static void fill(byte[] var0, int var1, int var2, byte var3) {
      for(int var4 = var1; var4 <= var2; ++var4) {
         var0[var4] = var3;
      }

   }

   public static int transcodeToUTF16(byte[] var0, char[] var1) {
      int var2 = 0;
      int var3 = 0;

      while(true) {
         while(var2 < var0.length) {
            byte var4 = var0[var2++];
            if (var4 >= 0) {
               if (var3 >= var1.length) {
                  return -1;
               }

               var1[var3++] = (char)var4;
            } else {
               short var5 = firstUnitTable[var4 & 127];
               int var6 = var5 >>> 8;

               byte var7;
               for(var7 = (byte)var5; var7 >= 0; var7 = transitionTable[var7 + ((var4 & 255) >>> 4)]) {
                  if (var2 >= var0.length) {
                     return -1;
                  }

                  var4 = var0[var2++];
                  var6 = var6 << 6 | var4 & 63;
               }

               if (var7 == -2) {
                  return -1;
               }

               if (var6 <= 65535) {
                  if (var3 >= var1.length) {
                     return -1;
                  }

                  var1[var3++] = (char)var6;
               } else {
                  if (var3 >= var1.length - 1) {
                     return -1;
                  }

                  var1[var3++] = (char)('ퟀ' + (var6 >>> 10));
                  var1[var3++] = (char)('\udc00' | var6 & 1023);
               }
            }
         }

         return var3;
      }
   }

   static {
      byte[] var0 = new byte[128];
      fill(var0, 0, 15, (byte)1);
      fill(var0, 16, 31, (byte)2);
      fill(var0, 32, 63, (byte)3);
      fill(var0, 64, 65, (byte)0);
      fill(var0, 66, 95, (byte)4);
      fill(var0, 96, 96, (byte)5);
      fill(var0, 97, 108, (byte)6);
      fill(var0, 109, 109, (byte)7);
      fill(var0, 110, 111, (byte)6);
      fill(var0, 112, 112, (byte)8);
      fill(var0, 113, 115, (byte)9);
      fill(var0, 116, 116, (byte)10);
      fill(var0, 117, 127, (byte)0);
      fill(transitionTable, 0, transitionTable.length - 1, (byte)-2);
      fill(transitionTable, 8, 11, (byte)-1);
      fill(transitionTable, 24, 27, (byte)0);
      fill(transitionTable, 40, 43, (byte)16);
      fill(transitionTable, 58, 59, (byte)0);
      fill(transitionTable, 72, 73, (byte)0);
      fill(transitionTable, 89, 91, (byte)16);
      fill(transitionTable, 104, 104, (byte)16);
      byte[] var1 = new byte[]{0, 0, 0, 0, 31, 15, 15, 15, 7, 7, 7};
      byte[] var2 = new byte[]{-2, -2, -2, -2, 0, 48, 16, 64, 80, 32, 96};

      for(int var3 = 0; var3 < 128; ++var3) {
         byte var4 = var0[var3];
         int var5 = var3 & var1[var4];
         byte var6 = var2[var4];
         firstUnitTable[var3] = (short)(var5 << 8 | var6);
      }

   }
}
