package org.apache.batik.ext.awt.image.codec.png;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.batik.ext.awt.image.rendered.IndexImage;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.image.PNGTranscoder;

public class PNGTranscoderInternalCodecWriteAdapter implements PNGTranscoder.WriteAdapter {
   public void writeImage(PNGTranscoder transcoder, BufferedImage img, TranscoderOutput output) throws TranscoderException {
      TranscodingHints hints = transcoder.getTranscodingHints();
      int n = true;
      if (hints.containsKey(PNGTranscoder.KEY_INDEXED)) {
         int n = (Integer)hints.get(PNGTranscoder.KEY_INDEXED);
         if (n == 1 || n == 2 || n == 4 || n == 8) {
            img = IndexImage.getIndexedImage(img, 1 << n);
         }
      }

      PNGEncodeParam params = PNGEncodeParam.getDefaultEncodeParam(img);
      if (params instanceof PNGEncodeParam.RGB) {
         ((PNGEncodeParam.RGB)params).setBackgroundRGB(new int[]{255, 255, 255});
      }

      float gamma;
      if (hints.containsKey(PNGTranscoder.KEY_GAMMA)) {
         gamma = (Float)hints.get(PNGTranscoder.KEY_GAMMA);
         if (gamma > 0.0F) {
            params.setGamma(gamma);
         }

         params.setChromaticity(PNGTranscoder.DEFAULT_CHROMA);
      } else {
         params.setSRGBIntent(0);
      }

      gamma = transcoder.getUserAgent().getPixelUnitToMillimeter();
      int numPix = (int)((double)(1000.0F / gamma) + 0.5);
      params.setPhysicalDimension(numPix, numPix, 1);

      try {
         OutputStream ostream = output.getOutputStream();
         PNGImageEncoder pngEncoder = new PNGImageEncoder(ostream, params);
         pngEncoder.encode(img);
         ostream.flush();
      } catch (IOException var11) {
         throw new TranscoderException(var11);
      }
   }
}
