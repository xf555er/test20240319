package org.apache.batik.ext.awt.image.codec.png;

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.batik.ext.awt.image.spi.ImageWriter;
import org.apache.batik.ext.awt.image.spi.ImageWriterParams;

public class PNGImageWriter implements ImageWriter {
   public void writeImage(RenderedImage image, OutputStream out) throws IOException {
      this.writeImage(image, out, (ImageWriterParams)null);
   }

   public void writeImage(RenderedImage image, OutputStream out, ImageWriterParams params) throws IOException {
      PNGImageEncoder encoder = new PNGImageEncoder(out, (PNGEncodeParam)null);
      encoder.encode(image);
   }

   public String getMIMEType() {
      return "image/png";
   }
}
