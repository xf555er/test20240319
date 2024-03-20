package org.apache.xmlgraphics.image.loader.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import javax.imageio.stream.ImageInputStream;
import javax.xml.transform.Source;
import org.apache.xmlgraphics.image.codec.png.PNGImageDecoder;
import org.apache.xmlgraphics.image.loader.ImageContext;
import org.apache.xmlgraphics.image.loader.ImageException;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageSize;
import org.apache.xmlgraphics.image.loader.util.ImageUtil;

public class PreloaderRawPNG extends AbstractImagePreloader {
   public ImageInfo preloadImage(String uri, Source src, ImageContext context) throws ImageException, IOException {
      if (!ImageUtil.hasImageInputStream(src)) {
         return null;
      } else {
         ImageInputStream in = ImageUtil.needImageInputStream(src);
         long bb = ByteBuffer.wrap(this.getHeader(in, 8)).getLong();
         if (bb != -8552249625308161526L) {
            return null;
         } else {
            in.mark();
            ImageSize size = new ImageSize();
            size.setResolution((double)context.getSourceResolution());

            try {
               PNGImageDecoder.readPNGHeader(in, size);
            } finally {
               in.reset();
            }

            ImageInfo info = new ImageInfo(uri, "image/png");
            info.setSize(size);
            return info;
         }
      }
   }

   public int getPriority() {
      return 2000;
   }
}
