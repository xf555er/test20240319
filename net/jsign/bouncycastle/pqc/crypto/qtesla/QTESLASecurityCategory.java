package net.jsign.bouncycastle.pqc.crypto.qtesla;

public class QTESLASecurityCategory {
   static int getPrivateSize(int var0) {
      switch (var0) {
         case 5:
            return 5224;
         case 6:
            return 12392;
         default:
            throw new IllegalArgumentException("unknown security category: " + var0);
      }
   }

   static int getPublicSize(int var0) {
      switch (var0) {
         case 5:
            return 14880;
         case 6:
            return 38432;
         default:
            throw new IllegalArgumentException("unknown security category: " + var0);
      }
   }

   public static String getName(int var0) {
      switch (var0) {
         case 5:
            return "qTESLA-p-I";
         case 6:
            return "qTESLA-p-III";
         default:
            throw new IllegalArgumentException("unknown security category: " + var0);
      }
   }
}
