package org.xml.sax.helpers;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public final class XMLReaderFactory {
   private static final String property = "org.xml.sax.driver";

   private XMLReaderFactory() {
   }

   public static XMLReader createXMLReader() throws SAXException {
      String var0 = null;
      ClassLoader var1 = NewInstance.getClassLoader();

      try {
         var0 = System.getProperty("org.xml.sax.driver");
      } catch (Exception var7) {
      }

      if (var0 == null) {
         try {
            String var2 = "META-INF/services/org.xml.sax.driver";
            InputStream var3;
            if (var1 == null) {
               var3 = ClassLoader.getSystemResourceAsStream(var2);
            } else {
               var3 = var1.getResourceAsStream(var2);
            }

            if (var3 != null) {
               BufferedReader var4 = new BufferedReader(new InputStreamReader(var3, "UTF8"));
               var0 = var4.readLine();
               var3.close();
            }
         } catch (Exception var6) {
         }
      }

      if (var0 == null) {
      }

      if (var0 != null) {
         return loadClass(var1, var0);
      } else {
         try {
            return new ParserAdapter(ParserFactory.makeParser());
         } catch (Exception var5) {
            throw new SAXException("Can't create default XMLReader; is system property org.xml.sax.driver set?");
         }
      }
   }

   public static XMLReader createXMLReader(String var0) throws SAXException {
      return loadClass(NewInstance.getClassLoader(), var0);
   }

   private static XMLReader loadClass(ClassLoader var0, String var1) throws SAXException {
      try {
         return (XMLReader)NewInstance.newInstance(var0, var1);
      } catch (ClassNotFoundException var6) {
         throw new SAXException("SAX2 driver class " + var1 + " not found", var6);
      } catch (IllegalAccessException var7) {
         throw new SAXException("SAX2 driver class " + var1 + " found but cannot be loaded", var7);
      } catch (InstantiationException var8) {
         throw new SAXException("SAX2 driver class " + var1 + " loaded but cannot be instantiated (no empty public constructor?)", var8);
      } catch (ClassCastException var9) {
         throw new SAXException("SAX2 driver class " + var1 + " does not implement XMLReader", var9);
      }
   }
}
