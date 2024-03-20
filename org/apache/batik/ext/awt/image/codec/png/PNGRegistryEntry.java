package org.apache.batik.ext.awt.image.codec.png;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import org.apache.batik.ext.awt.image.GraphicsUtil;
import org.apache.batik.ext.awt.image.renderable.DeferRable;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.ext.awt.image.renderable.RedRable;
import org.apache.batik.ext.awt.image.rendered.Any2sRGBRed;
import org.apache.batik.ext.awt.image.rendered.CachableRed;
import org.apache.batik.ext.awt.image.rendered.FormatRed;
import org.apache.batik.ext.awt.image.spi.ImageTagRegistry;
import org.apache.batik.ext.awt.image.spi.MagicNumberRegistryEntry;
import org.apache.batik.util.ParsedURL;

public class PNGRegistryEntry extends MagicNumberRegistryEntry {
   static final byte[] signature = new byte[]{-119, 80, 78, 71, 13, 10, 26, 10};

   public PNGRegistryEntry() {
      super("PNG", (String)"png", (String)"image/png", 0, signature);
   }

   public Filter handleStream(final InputStream inIS, ParsedURL origURL, final boolean needRawData) {
      final DeferRable dr = new DeferRable();
      final String errCode;
      final Object[] errParam;
      if (origURL != null) {
         errCode = "url.format.unreadable";
         errParam = new Object[]{"PNG", origURL};
      } else {
         errCode = "stream.format.unreadable";
         errParam = new Object[]{"PNG"};
      }

      Thread t = new Thread() {
         public void run() {
            Object filt;
            try {
               PNGDecodeParam param = new PNGDecodeParam();
               param.setExpandPalette(true);
               if (needRawData) {
                  param.setPerformGammaCorrection(false);
               } else {
                  param.setPerformGammaCorrection(true);
                  param.setDisplayExponent(2.2F);
               }

               CachableRed crxxx = new PNGRed(inIS, param);
               dr.setBounds(new Rectangle2D.Double(0.0, 0.0, (double)crxxx.getWidth(), (double)crxxx.getHeight()));
               CachableRed cr = new Any2sRGBRed(crxxx);
               CachableRed crx = new FormatRed(cr, GraphicsUtil.sRGB_Unpre);
               WritableRaster wr = (WritableRaster)crx.getData();
               ColorModel cm = crx.getColorModel();
               BufferedImage image = new BufferedImage(cm, wr, cm.isAlphaPremultiplied(), (Hashtable)null);
               CachableRed crxx = GraphicsUtil.wrap(image);
               filt = new RedRable(crxx);
            } catch (IOException var7) {
               filt = ImageTagRegistry.getBrokenLinkImage(PNGRegistryEntry.this, errCode, errParam);
            } catch (ThreadDeath var8) {
               Filter filtx = ImageTagRegistry.getBrokenLinkImage(PNGRegistryEntry.this, errCode, errParam);
               dr.setSource(filtx);
               throw var8;
            } catch (Throwable var9) {
               filt = ImageTagRegistry.getBrokenLinkImage(PNGRegistryEntry.this, errCode, errParam);
            }

            dr.setSource((Filter)filt);
         }
      };
      t.start();
      return dr;
   }
}
