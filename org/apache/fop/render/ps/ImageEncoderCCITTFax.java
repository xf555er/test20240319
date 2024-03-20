package org.apache.fop.render.ps;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.xmlgraphics.image.loader.impl.ImageRawCCITTFax;
import org.apache.xmlgraphics.ps.ImageEncoder;
import org.apache.xmlgraphics.ps.PSDictionary;

public class ImageEncoderCCITTFax implements ImageEncoder {
   private final ImageRawCCITTFax ccitt;

   public ImageEncoderCCITTFax(ImageRawCCITTFax ccitt) {
      this.ccitt = ccitt;
   }

   public void writeTo(OutputStream out) throws IOException {
      this.ccitt.writeTo(out);
   }

   public String getImplicitFilter() {
      PSDictionary dict = new PSDictionary();
      dict.put("/Columns", this.ccitt.getSize().getWidthPx());
      int compression = this.ccitt.getCompression();
      switch (compression) {
         case 2:
            dict.put("/K", 0);
            break;
         case 3:
            dict.put("/K", 1);
            break;
         case 4:
            dict.put("/K", -1);
            break;
         default:
            throw new IllegalStateException("Invalid compression scheme: " + compression);
      }

      return dict.toString() + " /CCITTFaxDecode";
   }
}
