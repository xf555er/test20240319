package org.apache.batik.ext.awt.image.spi;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import org.apache.batik.ext.awt.image.GraphicsUtil;
import org.apache.batik.ext.awt.image.renderable.DeferRable;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.ext.awt.image.renderable.RedRable;
import org.apache.batik.util.ParsedURL;

public class JDKRegistryEntry extends AbstractRegistryEntry implements URLRegistryEntry {
   public static final float PRIORITY = 1000000.0F;

   public JDKRegistryEntry() {
      super("JDK", 1000000.0F, new String[0], new String[]{"image/gif"});
   }

   public boolean isCompatibleURL(ParsedURL purl) {
      try {
         new URL(purl.toString());
         return true;
      } catch (MalformedURLException var3) {
         return false;
      }
   }

   public Filter handleURL(ParsedURL purl, boolean needRawData) {
      final URL url;
      try {
         url = new URL(purl.toString());
      } catch (MalformedURLException var8) {
         return null;
      }

      final DeferRable dr = new DeferRable();
      final String errCode;
      final Object[] errParam;
      if (purl != null) {
         errCode = "url.format.unreadable";
         errParam = new Object[]{"JDK", url};
      } else {
         errCode = "stream.format.unreadable";
         errParam = new Object[]{"JDK"};
      }

      Thread t = new Thread() {
         public void run() {
            Filter filt = null;

            try {
               Toolkit tk = Toolkit.getDefaultToolkit();
               Image img = tk.createImage(url);
               if (img != null) {
                  RenderedImage ri = JDKRegistryEntry.this.loadImage(img, dr);
                  if (ri != null) {
                     filt = new RedRable(GraphicsUtil.wrap(ri));
                  }
               }
            } catch (ThreadDeath var5) {
               Filter filtx = ImageTagRegistry.getBrokenLinkImage(JDKRegistryEntry.this, errCode, errParam);
               dr.setSource(filtx);
               throw var5;
            } catch (Throwable var6) {
            }

            if (filt == null) {
               filt = ImageTagRegistry.getBrokenLinkImage(JDKRegistryEntry.this, errCode, errParam);
            }

            dr.setSource((Filter)filt);
         }
      };
      t.start();
      return dr;
   }

   public RenderedImage loadImage(Image img, DeferRable dr) {
      if (img instanceof RenderedImage) {
         return (RenderedImage)img;
      } else {
         MyImgObs observer = new MyImgObs();
         Toolkit.getDefaultToolkit().prepareImage(img, -1, -1, observer);
         observer.waitTilWidthHeightDone();
         if (observer.imageError) {
            return null;
         } else {
            int width = observer.width;
            int height = observer.height;
            dr.setBounds(new Rectangle2D.Double(0.0, 0.0, (double)width, (double)height));
            BufferedImage bi = new BufferedImage(width, height, 2);
            Graphics2D g2d = bi.createGraphics();
            observer.waitTilImageDone();
            if (observer.imageError) {
               return null;
            } else {
               dr.setProperties(new HashMap());
               g2d.drawImage(img, 0, 0, (ImageObserver)null);
               g2d.dispose();
               return bi;
            }
         }
      }
   }

   public static class MyImgObs implements ImageObserver {
      boolean widthDone = false;
      boolean heightDone = false;
      boolean imageDone = false;
      int width = -1;
      int height = -1;
      boolean imageError = false;
      int IMG_BITS = 224;

      public void clear() {
         this.width = -1;
         this.height = -1;
         this.widthDone = false;
         this.heightDone = false;
         this.imageDone = false;
      }

      public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
         synchronized(this) {
            boolean notify = false;
            if ((infoflags & 1) != 0) {
               this.width = width;
            }

            if ((infoflags & 2) != 0) {
               this.height = height;
            }

            if ((infoflags & 32) != 0) {
               this.width = width;
               this.height = height;
            }

            if ((infoflags & this.IMG_BITS) != 0) {
               if (!this.widthDone || !this.heightDone || !this.imageDone) {
                  this.widthDone = true;
                  this.heightDone = true;
                  this.imageDone = true;
                  notify = true;
               }

               if ((infoflags & 64) != 0) {
                  this.imageError = true;
               }
            }

            if (!this.widthDone && this.width != -1) {
               notify = true;
               this.widthDone = true;
            }

            if (!this.heightDone && this.height != -1) {
               notify = true;
               this.heightDone = true;
            }

            if (notify) {
               this.notifyAll();
            }

            return true;
         }
      }

      public synchronized void waitTilWidthHeightDone() {
         while(!this.widthDone || !this.heightDone) {
            try {
               this.wait();
            } catch (InterruptedException var2) {
            }
         }

      }

      public synchronized void waitTilWidthDone() {
         while(!this.widthDone) {
            try {
               this.wait();
            } catch (InterruptedException var2) {
            }
         }

      }

      public synchronized void waitTilHeightDone() {
         while(!this.heightDone) {
            try {
               this.wait();
            } catch (InterruptedException var2) {
            }
         }

      }

      public synchronized void waitTilImageDone() {
         while(!this.imageDone) {
            try {
               this.wait();
            } catch (InterruptedException var2) {
            }
         }

      }
   }
}
