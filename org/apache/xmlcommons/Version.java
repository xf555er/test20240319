package org.apache.xmlcommons;

public class Version {
   public static String getVersion() {
      return getProduct() + " " + getVersionNum();
   }

   public static String getProduct() {
      return "XmlCommons";
   }

   public static String getVersionNum() {
      return "1.0";
   }

   public static void main(String[] var0) {
      System.out.println(getVersion());
   }
}
