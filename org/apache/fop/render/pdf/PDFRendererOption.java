package org.apache.fop.render.pdf;

import java.net.URI;
import java.net.URISyntaxException;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.pdf.PDFAMode;
import org.apache.fop.pdf.PDFUAMode;
import org.apache.fop.pdf.PDFVTMode;
import org.apache.fop.pdf.PDFXMode;
import org.apache.fop.pdf.Version;
import org.apache.fop.render.RendererConfigOption;

public enum PDFRendererOption implements RendererConfigOption {
   FILTER_LIST("filterList", (Object)null) {
      Object deserialize(String value) {
         throw new UnsupportedOperationException();
      }
   },
   PDF_A_MODE("pdf-a-mode", PDFAMode.DISABLED) {
      PDFAMode deserialize(String value) {
         return PDFAMode.getValueOf(value);
      }
   },
   PDF_UA_MODE("pdf-ua-mode", PDFUAMode.DISABLED) {
      PDFUAMode deserialize(String value) {
         return PDFUAMode.getValueOf(value);
      }
   },
   PDF_X_MODE("pdf-x-mode", PDFXMode.DISABLED) {
      PDFXMode deserialize(String value) {
         return PDFXMode.getValueOf(value);
      }
   },
   PDF_VT_MODE("pdf-vt-mode", PDFVTMode.DISABLED) {
      PDFVTMode deserialize(String value) {
         return PDFVTMode.getValueOf(value);
      }
   },
   VERSION("version") {
      Version deserialize(String value) {
         return Version.getValueOf(value);
      }
   },
   DISABLE_SRGB_COLORSPACE("disable-srgb-colorspace", false) {
      Boolean deserialize(String value) {
         return Boolean.valueOf(value);
      }
   },
   MERGE_FONTS("merge-fonts", false) {
      Boolean deserialize(String value) {
         return Boolean.valueOf(value);
      }
   },
   LINEARIZATION("linearization", false) {
      Boolean deserialize(String value) {
         return Boolean.valueOf(value);
      }
   },
   FORM_XOBJECT("form-xobject", false) {
      Boolean deserialize(String value) {
         return Boolean.valueOf(value);
      }
   },
   OUTPUT_PROFILE("output-profile") {
      URI deserialize(String value) {
         try {
            return InternalResourceResolver.cleanURI(value);
         } catch (URISyntaxException var3) {
            throw new RuntimeException(var3);
         }
      }
   };

   private final String name;
   private final Object defaultValue;

   private PDFRendererOption(String name, Object defaultValue) {
      this.name = name;
      this.defaultValue = defaultValue;
   }

   private PDFRendererOption(String name) {
      this(name, (Object)null);
   }

   public String getName() {
      return this.name;
   }

   public Object getDefaultValue() {
      return this.defaultValue;
   }

   public Object parse(Object object) {
      return object instanceof String ? this.deserialize((String)object) : object;
   }

   abstract Object deserialize(String var1);

   // $FF: synthetic method
   PDFRendererOption(String x2, Object x3, Object x4) {
      this(x2, x3);
   }

   // $FF: synthetic method
   PDFRendererOption(String x2, Object x3) {
      this(x2);
   }
}
