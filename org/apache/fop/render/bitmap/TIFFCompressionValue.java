package org.apache.fop.render.bitmap;

public enum TIFFCompressionValue {
   NONE("NONE"),
   JPEG("JPEG"),
   PACKBITS("PackBits"),
   DEFLATE("Deflate"),
   LZW("LZW"),
   ZLIB("ZLib"),
   CCITT_T4("CCITT T.4", 12, true),
   CCITT_T6("CCITT T.6", 12, true);

   private final String name;
   private final int imageType;
   private boolean isCcitt;

   private TIFFCompressionValue(String name, int imageType, boolean isCcitt) {
      this.name = name;
      this.imageType = imageType;
      this.isCcitt = isCcitt;
   }

   private TIFFCompressionValue(String name) {
      this(name, 2, false);
   }

   String getName() {
      return this.name;
   }

   int getImageType() {
      return this.imageType;
   }

   boolean hasCCITTCompression() {
      return this.isCcitt;
   }

   static TIFFCompressionValue getType(String name) {
      TIFFCompressionValue[] var1 = values();
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         TIFFCompressionValue tiffConst = var1[var3];
         if (tiffConst.name.equalsIgnoreCase(name)) {
            return tiffConst;
         }
      }

      return null;
   }
}
