package org.apache.batik.ext.awt.image.codec.imageio;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
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

public abstract class AbstractImageIORegistryEntry extends MagicNumberRegistryEntry {
   public AbstractImageIORegistryEntry(String name, String[] exts, String[] mimeTypes, MagicNumberRegistryEntry.MagicNumber[] magicNumbers) {
      super(name, 1100.0F, exts, mimeTypes, magicNumbers);
   }

   public AbstractImageIORegistryEntry(String name, String ext, String mimeType, int offset, byte[] magicNumber) {
      super(name, 1100.0F, ext, mimeType, offset, magicNumber);
   }

   public Filter handleStream(final InputStream inIS, ParsedURL origURL, boolean needRawData) {
      final DeferRable dr = new DeferRable();
      final String errCode;
      final Object[] errParam;
      if (origURL != null) {
         errCode = "url.format.unreadable";
         errParam = new Object[]{this.getFormatName(), origURL};
      } else {
         errCode = "stream.format.unreadable";
         errParam = new Object[]{this.getFormatName()};
      }

      Thread t = new Thread() {
         public void run() {
            Object filt;
            try {
               Iterator iter = ImageIO.getImageReadersByMIMEType(AbstractImageIORegistryEntry.this.getMimeTypes().get(0).toString());
               if (!iter.hasNext()) {
                  throw new UnsupportedOperationException("No image reader for " + AbstractImageIORegistryEntry.this.getFormatName() + " available!");
               }

               ImageReader reader = (ImageReader)iter.next();
               ImageInputStream imageIn = ImageIO.createImageInputStream(inIS);
               reader.setInput(imageIn, true);
               int imageIndex = 0;
               dr.setBounds(new Rectangle2D.Double(0.0, 0.0, (double)reader.getWidth(imageIndex), (double)reader.getHeight(imageIndex)));
               BufferedImage bi = reader.read(imageIndex);
               CachableRed crxx = GraphicsUtil.wrap(bi);
               CachableRed cr = new Any2sRGBRed(crxx);
               CachableRed crx = new FormatRed(cr, GraphicsUtil.sRGB_Unpre);
               WritableRaster wr = (WritableRaster)crx.getData();
               ColorModel cm = crx.getColorModel();
               BufferedImage image = new BufferedImage(cm, wr, cm.isAlphaPremultiplied(), (Hashtable)null);
               crxx = GraphicsUtil.wrap(image);
               filt = new RedRable(crxx);
            } catch (IOException var11) {
               filt = ImageTagRegistry.getBrokenLinkImage(AbstractImageIORegistryEntry.this, errCode, errParam);
            } catch (ThreadDeath var12) {
               Filter filtx = ImageTagRegistry.getBrokenLinkImage(AbstractImageIORegistryEntry.this, errCode, errParam);
               dr.setSource(filtx);
               throw var12;
            } catch (Throwable var13) {
               filt = ImageTagRegistry.getBrokenLinkImage(AbstractImageIORegistryEntry.this, errCode, errParam);
            }

            dr.setSource((Filter)filt);
         }
      };
      t.start();
      return dr;
   }
}
