package org.apache.fop.render.pdf;

import java.net.URI;
import java.util.EnumMap;
import java.util.Map;
import org.apache.fop.pdf.PDFAMode;
import org.apache.fop.pdf.PDFEncryptionParams;
import org.apache.fop.pdf.PDFUAMode;
import org.apache.fop.pdf.PDFVTMode;
import org.apache.fop.pdf.PDFXMode;
import org.apache.fop.pdf.Version;

public final class PDFRendererOptionsConfig {
   static final PDFRendererOptionsConfig DEFAULT;
   private final Map properties = new EnumMap(PDFRendererOption.class);
   private final PDFEncryptionParams encryptionConfig;

   PDFRendererOptionsConfig(Map props, PDFEncryptionParams encryptionParams) {
      this.properties.putAll(props);
      this.encryptionConfig = copyPDFEncryptionParams(encryptionParams);
   }

   private static PDFEncryptionParams copyPDFEncryptionParams(PDFEncryptionParams source) {
      return source == null ? null : new PDFEncryptionParams(source);
   }

   PDFRendererOptionsConfig merge(PDFRendererOptionsConfig config) {
      return config == null ? this : new PDFRendererOptionsConfig(merge(this.properties, config.properties), config.getEncryptionParameters() == null ? copyPDFEncryptionParams(this.encryptionConfig) : copyPDFEncryptionParams(config.getEncryptionParameters()));
   }

   private static Map merge(Map first, Map second) {
      EnumMap merged = new EnumMap(PDFRendererOption.class);
      PDFRendererOption[] var3 = PDFRendererOption.values();
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         PDFRendererOption option = var3[var5];
         Object value = second.get(option);
         if (value != null) {
            merged.put(option, value);
         } else {
            merged.put(option, first.get(option));
         }
      }

      return merged;
   }

   public Map getFilterMap() {
      return (Map)this.properties.get(PDFRendererOption.FILTER_LIST);
   }

   public PDFAMode getPDFAMode() {
      return (PDFAMode)this.properties.get(PDFRendererOption.PDF_A_MODE);
   }

   public PDFUAMode getPDFUAMode() {
      return (PDFUAMode)this.properties.get(PDFRendererOption.PDF_UA_MODE);
   }

   public PDFXMode getPDFXMode() {
      return (PDFXMode)this.properties.get(PDFRendererOption.PDF_X_MODE);
   }

   public PDFVTMode getPDFVTMode() {
      return (PDFVTMode)this.properties.get(PDFRendererOption.PDF_VT_MODE);
   }

   public PDFEncryptionParams getEncryptionParameters() {
      return this.encryptionConfig;
   }

   public URI getOutputProfileURI() {
      return (URI)this.properties.get(PDFRendererOption.OUTPUT_PROFILE);
   }

   public Boolean getDisableSRGBColorSpace() {
      return (Boolean)this.properties.get(PDFRendererOption.DISABLE_SRGB_COLORSPACE);
   }

   public Version getPDFVersion() {
      return (Version)this.properties.get(PDFRendererOption.VERSION);
   }

   public Boolean getMergeFontsEnabled() {
      return (Boolean)this.properties.get(PDFRendererOption.MERGE_FONTS);
   }

   public Boolean getLinearizationEnabled() {
      return (Boolean)this.properties.get(PDFRendererOption.LINEARIZATION);
   }

   public Boolean getFormXObjectEnabled() {
      return (Boolean)this.properties.get(PDFRendererOption.FORM_XOBJECT);
   }

   static {
      EnumMap props = new EnumMap(PDFRendererOption.class);
      PDFRendererOption[] var1 = PDFRendererOption.values();
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         PDFRendererOption option = var1[var3];
         props.put(option, option.getDefaultValue());
      }

      DEFAULT = new PDFRendererOptionsConfig(props, (PDFEncryptionParams)null);
   }
}
