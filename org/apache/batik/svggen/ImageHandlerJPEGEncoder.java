package org.apache.batik.svggen;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.batik.ext.awt.image.spi.ImageWriter;
import org.apache.batik.ext.awt.image.spi.ImageWriterParams;
import org.apache.batik.ext.awt.image.spi.ImageWriterRegistry;

public class ImageHandlerJPEGEncoder extends AbstractImageHandlerEncoder {
   public ImageHandlerJPEGEncoder(String imageDir, String urlRoot) throws SVGGraphics2DIOException {
      super(imageDir, urlRoot);
   }

   public final String getSuffix() {
      return ".jpg";
   }

   public final String getPrefix() {
      return "jpegImage";
   }

   public void encodeImage(BufferedImage buf, File imageFile) throws SVGGraphics2DIOException {
      try {
         OutputStream os = new FileOutputStream(imageFile);

         try {
            ImageWriter writer = ImageWriterRegistry.getInstance().getWriterFor("image/jpeg");
            ImageWriterParams params = new ImageWriterParams();
            params.setJPEGQuality(1.0F, false);
            writer.writeImage(buf, os, params);
         } finally {
            os.close();
         }

      } catch (IOException var10) {
         throw new SVGGraphics2DIOException("could not write image File " + imageFile.getName());
      }
   }

   public BufferedImage buildBufferedImage(Dimension size) {
      return new BufferedImage(size.width, size.height, 1);
   }
}
