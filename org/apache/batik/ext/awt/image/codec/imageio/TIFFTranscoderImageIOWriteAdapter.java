package org.apache.batik.ext.awt.image.codec.imageio;

import java.awt.image.BufferedImage;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.batik.ext.awt.image.GraphicsUtil;
import org.apache.batik.ext.awt.image.rendered.FormatRed;
import org.apache.batik.ext.awt.image.spi.ImageWriter;
import org.apache.batik.ext.awt.image.spi.ImageWriterParams;
import org.apache.batik.ext.awt.image.spi.ImageWriterRegistry;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.image.TIFFTranscoder;

public class TIFFTranscoderImageIOWriteAdapter implements TIFFTranscoder.WriteAdapter {
   public void writeImage(TIFFTranscoder transcoder, BufferedImage img, TranscoderOutput output) throws TranscoderException {
      TranscodingHints hints = transcoder.getTranscodingHints();
      ImageWriter writer = ImageWriterRegistry.getInstance().getWriterFor("image/tiff");
      ImageWriterParams params = new ImageWriterParams();
      float PixSzMM = transcoder.getUserAgent().getPixelUnitToMillimeter();
      int PixSzInch = (int)(25.4 / (double)PixSzMM + 0.5);
      params.setResolution(PixSzInch);
      if (hints.containsKey(TIFFTranscoder.KEY_COMPRESSION_METHOD)) {
         String method = (String)hints.get(TIFFTranscoder.KEY_COMPRESSION_METHOD);
         if ("packbits".equals(method)) {
            params.setCompressionMethod("PackBits");
         } else if ("deflate".equals(method)) {
            params.setCompressionMethod("Deflate");
         } else if ("lzw".equals(method)) {
            params.setCompressionMethod("LZW");
         } else if ("jpeg".equals(method)) {
            params.setCompressionMethod("JPEG");
         }
      }

      try {
         OutputStream ostream = output.getOutputStream();
         int w = img.getWidth();
         int h = img.getHeight();
         SinglePixelPackedSampleModel sppsm = (SinglePixelPackedSampleModel)img.getSampleModel();
         int bands = sppsm.getNumBands();
         int[] off = new int[bands];

         for(int i = 0; i < bands; off[i] = i++) {
         }

         SampleModel sm = new PixelInterleavedSampleModel(0, w, h, bands, w * bands, off);
         RenderedImage rimg = new FormatRed(GraphicsUtil.wrap(img), sm);
         writer.writeImage(rimg, ostream, params);
         ostream.flush();
      } catch (IOException var17) {
         throw new TranscoderException(var17);
      }
   }
}
