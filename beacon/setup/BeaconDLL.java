package beacon.setup;

public class BeaconDLL {
   public String fileName = "";
   public byte[] originalDLL;
   public byte[] peProcessedDLL;
   public int customLoaderSize = 5;
   public String customFileName = "";
   public byte[] customDLL;

   public BeaconDLL() {
   }

   public BeaconDLL(String var1) {
      this.fileName = var1;
   }

   public void setCustomLoaderSize(int var1) {
      switch (var1) {
         case 0:
         case 5:
         case 100:
            this.customLoaderSize = var1;
            return;
         default:
            throw new IllegalArgumentException("Custom Loader Size is invalid: " + this.customLoaderSize);
      }
   }

   public boolean usesCustomLoaderSize() {
      return this.customLoaderSize > 0;
   }

   public String getCustomLoaderExtension() {
      switch (this.customLoaderSize) {
         case 0:
         case 5:
            return "";
         case 100:
            return ".rl100k";
         default:
            throw new IllegalStateException("Custom Loader Size is undefined: " + this.customLoaderSize);
      }
   }
}
