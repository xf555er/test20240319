package org.apache.xmlgraphics.image.loader.impl;

import java.awt.Color;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.Map;
import javax.imageio.stream.ImageInputStream;
import javax.xml.transform.Source;
import org.apache.xmlgraphics.image.codec.png.PNGDecodeParam;
import org.apache.xmlgraphics.image.codec.png.PNGImageDecoder;
import org.apache.xmlgraphics.image.codec.util.ImageInputStreamSeekableStreamAdapter;
import org.apache.xmlgraphics.image.codec.util.SeekableStream;
import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageException;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;
import org.apache.xmlgraphics.image.loader.util.ImageUtil;

public class ImageLoaderPNG extends AbstractImageLoader {
   public Image loadImage(ImageInfo info, Map hints, ImageSessionContext session) throws ImageException, IOException {
      Source src = session.needSource(info.getOriginalURI());
      ImageInputStream imgStream = ImageUtil.needImageInputStream(src);
      SeekableStream seekStream = new ImageInputStreamSeekableStreamAdapter(imgStream);
      PNGImageDecoder decoder = new PNGImageDecoder(seekStream, new PNGDecodeParam());
      RenderedImage image = decoder.decodeAsRenderedImage();
      return new ImageRendered(info, image, (Color)null);
   }

   public ImageFlavor getTargetFlavor() {
      return ImageFlavor.RENDERED_IMAGE;
   }

   public int getUsagePenalty() {
      return 1000;
   }
}
