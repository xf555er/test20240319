package org.apache.batik.transcoder.image;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.batik.ext.awt.image.spi.ImageWriter;
import org.apache.batik.ext.awt.image.spi.ImageWriterParams;
import org.apache.batik.ext.awt.image.spi.ImageWriterRegistry;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.image.resources.Messages;

public class JPEGTranscoder extends ImageTranscoder {
   public static final TranscodingHints.Key KEY_QUALITY = new QualityKey();

   public JPEGTranscoder() {
      this.hints.put(ImageTranscoder.KEY_BACKGROUND_COLOR, Color.white);
   }

   public BufferedImage createImage(int width, int height) {
      return new BufferedImage(width, height, 1);
   }

   public void writeImage(BufferedImage img, TranscoderOutput output) throws TranscoderException {
      OutputStream ostream = output.getOutputStream();
      OutputStream ostream = new OutputStreamWrapper(ostream);

      try {
         float quality;
         if (this.hints.containsKey(KEY_QUALITY)) {
            quality = (Float)this.hints.get(KEY_QUALITY);
         } else {
            TranscoderException te = new TranscoderException(Messages.formatMessage("jpeg.unspecifiedQuality", (Object[])null));
            this.handler.error(te);
            quality = 0.75F;
         }

         ImageWriter writer = ImageWriterRegistry.getInstance().getWriterFor("image/jpeg");
         ImageWriterParams params = new ImageWriterParams();
         params.setJPEGQuality(quality, true);
         float PixSzMM = this.userAgent.getPixelUnitToMillimeter();
         int PixSzInch = (int)(25.4 / (double)PixSzMM + 0.5);
         params.setResolution(PixSzInch);
         writer.writeImage(img, ostream, params);
         ostream.flush();
      } catch (IOException var9) {
         throw new TranscoderException(var9);
      }
   }

   private static class OutputStreamWrapper extends OutputStream {
      OutputStream os;

      OutputStreamWrapper(OutputStream os) {
         this.os = os;
      }

      public void close() throws IOException {
         if (this.os != null) {
            try {
               this.os.close();
            } catch (IOException var2) {
               this.os = null;
            }

         }
      }

      public void flush() throws IOException {
         if (this.os != null) {
            try {
               this.os.flush();
            } catch (IOException var2) {
               this.os = null;
            }

         }
      }

      public void write(byte[] b) throws IOException {
         if (this.os != null) {
            try {
               this.os.write(b);
            } catch (IOException var3) {
               this.os = null;
            }

         }
      }

      public void write(byte[] b, int off, int len) throws IOException {
         if (this.os != null) {
            try {
               this.os.write(b, off, len);
            } catch (IOException var5) {
               this.os = null;
            }

         }
      }

      public void write(int b) throws IOException {
         if (this.os != null) {
            try {
               this.os.write(b);
            } catch (IOException var3) {
               this.os = null;
            }

         }
      }
   }

   private static class QualityKey extends TranscodingHints.Key {
      private QualityKey() {
      }

      public boolean isCompatibleValue(Object v) {
         if (!(v instanceof Float)) {
            return false;
         } else {
            float q = (Float)v;
            return q > 0.0F && q <= 1.0F;
         }
      }

      // $FF: synthetic method
      QualityKey(Object x0) {
         this();
      }
   }
}
