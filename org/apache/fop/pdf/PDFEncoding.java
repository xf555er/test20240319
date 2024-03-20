package org.apache.fop.pdf;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.apache.fop.fonts.CodePointMapping;
import org.apache.fop.fonts.SingleByteEncoding;

public class PDFEncoding extends PDFDictionary {
   public static final String STANDARD_ENCODING = "StandardEncoding";
   public static final String MAC_ROMAN_ENCODING = "MacRomanEncoding";
   public static final String MAC_EXPERT_ENCODING = "MacExpertEncoding";
   public static final String WIN_ANSI_ENCODING = "WinAnsiEncoding";
   public static final String PDF_DOC_ENCODING = "PDFDocEncoding";
   private static final Set PREDEFINED_ENCODINGS;

   public PDFEncoding(String basename) {
      this.put("Type", new PDFName("Encoding"));
      if (basename != null) {
         this.put("BaseEncoding", new PDFName(basename));
      }

   }

   static Object createPDFEncoding(SingleByteEncoding encoding, String fontName) {
      if (encoding == null) {
         return null;
      } else {
         String encodingName = null;
         CodePointMapping baseEncoding;
         if (fontName.indexOf("Symbol") >= 0) {
            baseEncoding = CodePointMapping.getMapping("SymbolEncoding");
            encodingName = baseEncoding.getName();
         } else {
            baseEncoding = CodePointMapping.getMapping("StandardEncoding");
         }

         PDFEncoding pdfEncoding = new PDFEncoding(encodingName);
         DifferencesBuilder builder = pdfEncoding.createDifferencesBuilder();
         PDFArray differences = builder.buildDifferencesArray(baseEncoding, encoding);
         if (differences.length() > 0) {
            pdfEncoding.setDifferences(differences);
            return pdfEncoding;
         } else {
            return encodingName;
         }
      }
   }

   public static boolean isPredefinedEncoding(String name) {
      return PREDEFINED_ENCODINGS.contains(name);
   }

   static boolean hasStandardEncoding(String encodingName) {
      return encodingName.equals("StandardEncoding");
   }

   public DifferencesBuilder createDifferencesBuilder() {
      return new DifferencesBuilder();
   }

   public void setDifferences(PDFArray differences) {
      this.put("Differences", differences);
   }

   static {
      Set encodings = new HashSet();
      encodings.add("StandardEncoding");
      encodings.add("MacRomanEncoding");
      encodings.add("MacExpertEncoding");
      encodings.add("WinAnsiEncoding");
      encodings.add("PDFDocEncoding");
      PREDEFINED_ENCODINGS = Collections.unmodifiableSet(encodings);
   }

   public class DifferencesBuilder {
      private int currentCode = -1;

      public PDFArray buildDifferencesArray(SingleByteEncoding encodingA, SingleByteEncoding encodingB) {
         PDFArray differences = new PDFArray();
         int start = -1;
         String[] baseNames = encodingA.getCharNameMap();
         String[] charNameMap = encodingB.getCharNameMap();
         int i = 0;

         for(int ci = charNameMap.length; i < ci; ++i) {
            String basec = baseNames[i];
            String c = charNameMap[i];
            if (!basec.equals(c)) {
               if (start != i) {
                  this.addDifference(i, differences);
                  start = i;
               }

               this.addName(c, differences);
               ++start;
            }
         }

         return differences;
      }

      private void addDifference(int code, PDFArray differences) {
         this.currentCode = code;
         differences.add(code);
      }

      private void addName(String name, PDFArray differences) {
         if (this.currentCode < 0) {
            throw new IllegalStateException("addDifference(int) must be called first");
         } else {
            differences.add(new PDFName(name));
         }
      }
   }
}
