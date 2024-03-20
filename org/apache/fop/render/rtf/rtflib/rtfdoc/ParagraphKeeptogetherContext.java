package org.apache.fop.render.rtf.rtflib.rtfdoc;

public final class ParagraphKeeptogetherContext {
   private static int paraKeepTogetherOpen;
   private static boolean paraResetProperties;

   private ParagraphKeeptogetherContext() {
   }

   public static int getKeepTogetherOpenValue() {
      return paraKeepTogetherOpen;
   }

   public static void keepTogetherOpen() {
      ++paraKeepTogetherOpen;
   }

   public static void keepTogetherClose() {
      if (paraKeepTogetherOpen > 0) {
         --paraKeepTogetherOpen;
         paraResetProperties = paraKeepTogetherOpen == 0;
      }

   }

   public static boolean paragraphResetProperties() {
      return paraResetProperties;
   }

   public static void setParagraphResetPropertiesUsed() {
      paraResetProperties = false;
   }
}
