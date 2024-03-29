package org.apache.xmlgraphics.image.loader.impl;

import java.io.IOException;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageException;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;
import org.apache.xmlgraphics.image.loader.spi.ImageLoader;

public class CompositeImageLoader extends AbstractImageLoader {
   protected static final Log log = LogFactory.getLog(CompositeImageLoader.class);
   private ImageLoader[] loaders;

   public CompositeImageLoader(ImageLoader[] loaders) {
      if (loaders != null && loaders.length != 0) {
         int i = 1;

         for(int c = loaders.length; i < c; ++i) {
            if (!loaders[0].getTargetFlavor().equals(loaders[i].getTargetFlavor())) {
               throw new IllegalArgumentException("All ImageLoaders must produce the same target flavor");
            }
         }

         this.loaders = loaders;
      } else {
         throw new IllegalArgumentException("Must at least pass one ImageLoader as parameter");
      }
   }

   public ImageFlavor getTargetFlavor() {
      return this.loaders[0].getTargetFlavor();
   }

   public int getUsagePenalty() {
      int maxPenalty = 0;
      int i = 1;

      for(int c = this.loaders.length; i < c; ++i) {
         maxPenalty = Math.max(maxPenalty, this.loaders[i].getUsagePenalty());
      }

      return maxPenalty;
   }

   public Image loadImage(ImageInfo info, Map hints, ImageSessionContext session) throws ImageException, IOException {
      ImageException firstException = null;
      ImageLoader[] var5 = this.loaders;
      int var6 = var5.length;
      int var7 = 0;

      while(var7 < var6) {
         ImageLoader loader = var5[var7];

         try {
            Image img = loader.loadImage(info, hints, session);
            if (img != null && firstException != null) {
               log.debug("First ImageLoader failed (" + firstException.getMessage() + "). Fallback was successful.");
            }

            return img;
         } catch (ImageException var10) {
            if (firstException == null) {
               firstException = var10;
            }

            ++var7;
         }
      }

      throw firstException;
   }

   public String toString() {
      StringBuffer sb = new StringBuffer("[");

      for(int i = 0; i < this.loaders.length; ++i) {
         if (i > 0) {
            sb.append(",");
         }

         sb.append(this.loaders[i].toString());
      }

      sb.append("]");
      return sb.toString();
   }
}
