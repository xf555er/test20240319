package org.apache.fop.render.pdf.extensions;

public class PDFActionExtension extends PDFDictionaryExtension {
   public static final String PROPERTY_TYPE = "type";

   PDFActionExtension() {
      super(PDFDictionaryType.Action);
   }
}
