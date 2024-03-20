package org.apache.batik.svggen;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.batik.ext.awt.image.spi.ImageWriter;
import org.apache.batik.ext.awt.image.spi.ImageWriterParams;
import org.apache.batik.ext.awt.image.spi.ImageWriterRegistry;

public class CachedImageHandlerJPEGEncoder extends DefaultCachedImageHandler {
   public static final String CACHED_JPEG_PREFIX = "jpegImage";
   public static final String CACHED_JPEG_SUFFIX = ".jpg";
   protected String refPrefix = "";

   public CachedImageHandlerJPEGEncoder(String imageDir, String urlRoot) throws SVGGraphics2DIOException {
      this.refPrefix = urlRoot + "/";
      this.setImageCacher(new ImageCacher.External(imageDir, "jpegImage", ".jpg"));
   }

   public void encodeImage(BufferedImage buf, OutputStream os) throws IOException {
      ImageWriter writer = ImageWriterRegistry.getInstance().getWriterFor("image/jpeg");
      ImageWriterParams params = new ImageWriterParams();
      params.setJPEGQuality(1.0F, false);
      writer.writeImage(buf, os, params);
   }

   public int getBufferedImageType() {
      return 1;
   }

   public String getRefPrefix() {
      return this.refPrefix;
   }
}
