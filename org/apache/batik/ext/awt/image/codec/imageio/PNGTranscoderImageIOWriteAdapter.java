package org.apache.batik.ext.awt.image.codec.imageio;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.batik.ext.awt.image.rendered.IndexImage;
import org.apache.batik.ext.awt.image.spi.ImageWriter;
import org.apache.batik.ext.awt.image.spi.ImageWriterParams;
import org.apache.batik.ext.awt.image.spi.ImageWriterRegistry;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.image.PNGTranscoder;

public class PNGTranscoderImageIOWriteAdapter implements PNGTranscoder.WriteAdapter {
   public void writeImage(PNGTranscoder transcoder, BufferedImage img, TranscoderOutput output) throws TranscoderException {
      TranscodingHints hints = transcoder.getTranscodingHints();
      int n = true;
      if (hints.containsKey(PNGTranscoder.KEY_INDEXED)) {
         int n = (Integer)hints.get(PNGTranscoder.KEY_INDEXED);
         if (n == 1 || n == 2 || n == 4 || n == 8) {
            img = IndexImage.getIndexedImage(img, 1 << n);
         }
      }

      ImageWriter writer = ImageWriterRegistry.getInstance().getWriterFor("image/png");
      ImageWriterParams params = new ImageWriterParams();
      float PixSzMM = transcoder.getUserAgent().getPixelUnitToMillimeter();
      int PixSzInch = (int)(25.4 / (double)PixSzMM + 0.5);
      params.setResolution(PixSzInch);

      try {
         OutputStream ostream = output.getOutputStream();
         writer.writeImage(img, ostream, params);
         ostream.flush();
      } catch (IOException var11) {
         throw new TranscoderException(var11);
      }
   }
}
