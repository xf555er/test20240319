package org.apache.fop.fo.pagination;

import org.apache.fop.complexscripts.util.NumberConverter;

public class PageNumberGenerator {
   private NumberConverter converter;

   public PageNumberGenerator(String format, int groupingSeparator, int groupingSize, int letterValue, String features, String language, String country) {
      this.converter = new NumberConverter(format, groupingSeparator, groupingSize, letterValue, features, language, country);
   }

   public String makeFormattedPageNumber(int number) {
      return this.converter.convert((long)number);
   }
}
