package org.apache.batik.svggen;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.batik.ext.awt.image.spi.ImageWriter;
import org.apache.batik.ext.awt.image.spi.ImageWriterRegistry;

public class ImageHandlerPNGEncoder extends AbstractImageHandlerEncoder {
   public ImageHandlerPNGEncoder(String imageDir, String urlRoot) throws SVGGraphics2DIOException {
      super(imageDir, urlRoot);
   }

   public final String getSuffix() {
      return ".png";
   }

   public final String getPrefix() {
      return "pngImage";
   }

   public void encodeImage(BufferedImage buf, File imageFile) throws SVGGraphics2DIOException {
      try {
         OutputStream os = new FileOutputStream(imageFile);

         try {
            ImageWriter writer = ImageWriterRegistry.getInstance().getWriterFor("image/png");
            writer.writeImage(buf, os);
         } finally {
            os.close();
         }

      } catch (IOException var9) {
         throw new SVGGraphics2DIOException("could not write image File " + imageFile.getName());
      }
   }

   public BufferedImage buildBufferedImage(Dimension size) {
      return new BufferedImage(size.width, size.height, 2);
   }
}
