package beacon;

import common.CommonUtils;

public class BeaconMessages {
   public static String toString(int var0, int var1, int var2, String var3) {
      switch (var0) {
         case 0:
            return "Debug: " + var3;
         case 1:
            return var3;
         default:
            CommonUtils.print_error("Unknown message toString(" + var0 + ", " + var1 + ", " + var2 + ", '" + var3 + "') BEACON_ERROR");
            return "Unknown message: " + var0;
      }
   }
}
