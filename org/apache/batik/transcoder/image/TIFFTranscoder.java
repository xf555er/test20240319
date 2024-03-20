package org.apache.batik.transcoder.image;

import java.awt.image.BufferedImage;
import java.awt.image.SinglePixelPackedSampleModel;
import java.lang.reflect.InvocationTargetException;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.keys.StringKey;

public class TIFFTranscoder extends ImageTranscoder {
   public static final TranscodingHints.Key KEY_FORCE_TRANSPARENT_WHITE;
   public static final TranscodingHints.Key KEY_COMPRESSION_METHOD;

   public TIFFTranscoder() {
      this.hints.put(KEY_FORCE_TRANSPARENT_WHITE, Boolean.FALSE);
   }

   public UserAgent getUserAgent() {
      return this.userAgent;
   }

   public BufferedImage createImage(int width, int height) {
      return new BufferedImage(width, height, 2);
   }

   private WriteAdapter getWriteAdapter(String className) {
      try {
         Class clazz = Class.forName(className);
         WriteAdapter adapter = (WriteAdapter)clazz.getDeclaredConstructor().newInstance();
         return adapter;
      } catch (ClassNotFoundException var4) {
         return null;
      } catch (InstantiationException var5) {
         return null;
      } catch (IllegalAccessException var6) {
         return null;
      } catch (NoSuchMethodException var7) {
         return null;
      } catch (InvocationTargetException var8) {
         return null;
      }
   }

   public void writeImage(BufferedImage img, TranscoderOutput output) throws TranscoderException {
      boolean forceTransparentWhite = false;
      if (this.hints.containsKey(PNGTranscoder.KEY_FORCE_TRANSPARENT_WHITE)) {
         forceTransparentWhite = (Boolean)this.hints.get(PNGTranscoder.KEY_FORCE_TRANSPARENT_WHITE);
      }

      if (forceTransparentWhite) {
         SinglePixelPackedSampleModel sppsm = (SinglePixelPackedSampleModel)img.getSampleModel();
         this.forceTransparentWhite(img, sppsm);
      }

      WriteAdapter adapter = this.getWriteAdapter("org.apache.batik.ext.awt.image.codec.tiff.TIFFTranscoderInternalCodecWriteAdapter");
      if (adapter == null) {
         adapter = this.getWriteAdapter("org.apache.batik.ext.awt.image.codec.imageio.TIFFTranscoderImageIOWriteAdapter");
      }

      if (adapter == null) {
         throw new TranscoderException("Could not write TIFF file because no WriteAdapter is availble");
      } else {
         adapter.writeImage(this, img, output);
      }
   }

   static {
      KEY_FORCE_TRANSPARENT_WHITE = ImageTranscoder.KEY_FORCE_TRANSPARENT_WHITE;
      KEY_COMPRESSION_METHOD = new StringKey();
   }

   public interface WriteAdapter {
      void writeImage(TIFFTranscoder var1, BufferedImage var2, TranscoderOutput var3) throws TranscoderException;
   }
}
