package org.apache.batik.transcoder.image;

import java.awt.image.BufferedImage;
import java.awt.image.SinglePixelPackedSampleModel;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.image.resources.Messages;
import org.apache.batik.transcoder.keys.FloatKey;
import org.apache.batik.transcoder.keys.IntegerKey;

public class PNGTranscoder extends ImageTranscoder {
   public static final TranscodingHints.Key KEY_GAMMA = new FloatKey();
   public static final float[] DEFAULT_CHROMA = new float[]{0.3127F, 0.329F, 0.64F, 0.33F, 0.3F, 0.6F, 0.15F, 0.06F};
   public static final TranscodingHints.Key KEY_INDEXED = new IntegerKey();

   public PNGTranscoder() {
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
      OutputStream ostream = output.getOutputStream();
      if (ostream == null) {
         throw new TranscoderException(Messages.formatMessage("png.badoutput", (Object[])null));
      } else {
         boolean forceTransparentWhite = false;
         if (this.hints.containsKey(KEY_FORCE_TRANSPARENT_WHITE)) {
            forceTransparentWhite = (Boolean)this.hints.get(KEY_FORCE_TRANSPARENT_WHITE);
         }

         if (forceTransparentWhite) {
            SinglePixelPackedSampleModel sppsm = (SinglePixelPackedSampleModel)img.getSampleModel();
            this.forceTransparentWhite(img, sppsm);
         }

         WriteAdapter adapter = this.getWriteAdapter("org.apache.batik.ext.awt.image.codec.png.PNGTranscoderInternalCodecWriteAdapter");
         if (adapter == null) {
            adapter = this.getWriteAdapter("org.apache.batik.transcoder.image.PNGTranscoderImageIOWriteAdapter");
         }

         if (adapter == null) {
            throw new TranscoderException("Could not write PNG file because no WriteAdapter is availble");
         } else {
            adapter.writeImage(this, img, output);
         }
      }
   }

   public interface WriteAdapter {
      void writeImage(PNGTranscoder var1, BufferedImage var2, TranscoderOutput var3) throws TranscoderException;
   }
}
