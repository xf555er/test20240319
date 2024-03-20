package org.xml.sax.helpers;

import org.xml.sax.Parser;

/** @deprecated */
public class ParserFactory {
   private ParserFactory() {
   }

   public static Parser makeParser() throws ClassNotFoundException, IllegalAccessException, InstantiationException, NullPointerException, ClassCastException {
      String var0 = System.getProperty("org.xml.sax.parser");
      if (var0 == null) {
         throw new NullPointerException("No value for sax.parser property");
      } else {
         return makeParser(var0);
      }
   }

   public static Parser makeParser(String var0) throws ClassNotFoundException, IllegalAccessException, InstantiationException, ClassCastException {
      return (Parser)NewInstance.newInstance(NewInstance.getClassLoader(), var0);
   }
}
