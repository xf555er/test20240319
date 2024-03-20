package org.apache.fop.fonts;

public final class FontUtil {
   private static final String[] ITALIC_WORDS = new String[]{"italic", "oblique", "inclined"};
   private static final String[] LIGHT_WORDS = new String[]{"light"};
   private static final String[] MEDIUM_WORDS = new String[]{"medium"};
   private static final String[] DEMI_WORDS = new String[]{"demi", "semi"};
   private static final String[] BOLD_WORDS = new String[]{"bold"};
   private static final String[] EXTRA_BOLD_WORDS = new String[]{"extrabold", "extra bold", "black", "heavy", "ultra", "super"};

   private FontUtil() {
   }

   public static int parseCSS2FontWeight(String text) {
      int weight = true;

      int weight;
      try {
         weight = Integer.parseInt(text);
         weight = weight / 100 * 100;
         weight = Math.max(weight, 100);
         weight = Math.min(weight, 900);
      } catch (NumberFormatException var3) {
         if (text.equals("normal")) {
            weight = 400;
         } else {
            if (!text.equals("bold")) {
               throw new IllegalArgumentException("Illegal value for font weight: '" + text + "'. Use one of: 100, 200, 300, 400, 500, 600, 700, 800, 900, normal (=400), bold (=700)");
            }

            weight = 700;
         }
      }

      return weight;
   }

   public static String stripWhiteSpace(String str) {
      if (str != null) {
         StringBuffer stringBuffer = new StringBuffer(str.length());
         int i = 0;

         for(int strLen = str.length(); i < strLen; ++i) {
            char ch = str.charAt(i);
            if (ch != ' ' && ch != '\r' && ch != '\n' && ch != '\t') {
               stringBuffer.append(ch);
            }
         }

         return stringBuffer.toString();
      } else {
         return str;
      }
   }

   public static String guessStyle(String fontName) {
      if (fontName != null) {
         String[] var1 = ITALIC_WORDS;
         int var2 = var1.length;

         for(int var3 = 0; var3 < var2; ++var3) {
            String word = var1[var3];
            if (fontName.indexOf(word) != -1) {
               return "italic";
            }
         }
      }

      return "normal";
   }

   public static int guessWeight(String fontName) {
      int weight = 400;
      String[] var2 = BOLD_WORDS;
      int var3 = var2.length;

      int var4;
      String word;
      for(var4 = 0; var4 < var3; ++var4) {
         word = var2[var4];
         if (fontName.indexOf(word) != -1) {
            weight = 700;
            break;
         }
      }

      var2 = MEDIUM_WORDS;
      var3 = var2.length;

      for(var4 = 0; var4 < var3; ++var4) {
         word = var2[var4];
         if (fontName.indexOf(word) != -1) {
            weight = 500;
            break;
         }
      }

      var2 = DEMI_WORDS;
      var3 = var2.length;

      for(var4 = 0; var4 < var3; ++var4) {
         word = var2[var4];
         if (fontName.indexOf(word) != -1) {
            weight = 600;
            break;
         }
      }

      var2 = EXTRA_BOLD_WORDS;
      var3 = var2.length;

      for(var4 = 0; var4 < var3; ++var4) {
         word = var2[var4];
         if (fontName.indexOf(word) != -1) {
            weight = 800;
            break;
         }
      }

      var2 = LIGHT_WORDS;
      var3 = var2.length;

      for(var4 = 0; var4 < var3; ++var4) {
         word = var2[var4];
         if (fontName.indexOf(word) != -1) {
            weight = 200;
            break;
         }
      }

      return weight;
   }
}
