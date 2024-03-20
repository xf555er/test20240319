package org.apache.fop.util;

import java.util.Locale;

public final class LanguageTags {
   private LanguageTags() {
   }

   public static String toLanguageTag(Locale locale) {
      StringBuffer sb = new StringBuffer(5);
      sb.append(locale.getLanguage());
      String country = locale.getCountry();
      if (country.length() > 0) {
         sb.append('-');
         sb.append(country);
      }

      return sb.toString();
   }

   public static Locale toLocale(String languageTag) {
      String[] parts = languageTag.split("-");
      return parts.length == 1 ? new Locale(parts[0]) : new Locale(parts[0], parts[1]);
   }
}
