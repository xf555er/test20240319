package org.apache.xmlgraphics.image.loader.spi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.util.Penalty;
import org.apache.xmlgraphics.util.Service;

public class ImageImplRegistry {
   protected static final Log log = LogFactory.getLog(ImageImplRegistry.class);
   public static final int INFINITE_PENALTY = Integer.MAX_VALUE;
   private List preloaders;
   private int lastPreloaderIdentifier;
   private int lastPreloaderSort;
   private Map loaders;
   private List converters;
   private int converterModifications;
   private Map additionalPenalties;
   private static ImageImplRegistry defaultInstance = new ImageImplRegistry();

   public ImageImplRegistry(boolean discover) {
      this.preloaders = new ArrayList();
      this.loaders = new HashMap();
      this.converters = new ArrayList();
      this.additionalPenalties = new HashMap();
      if (discover) {
         this.discoverClasspathImplementations();
      }

   }

   public ImageImplRegistry() {
      this(true);
   }

   public static ImageImplRegistry getDefaultInstance() {
      return defaultInstance;
   }

   public void discoverClasspathImplementations() {
      Iterator iter = Service.providers(ImagePreloader.class);

      while(iter.hasNext()) {
         this.registerPreloader((ImagePreloader)iter.next());
      }

      iter = Service.providers(ImageLoaderFactory.class);

      while(iter.hasNext()) {
         this.registerLoaderFactory((ImageLoaderFactory)iter.next());
      }

      iter = Service.providers(ImageConverter.class);

      while(iter.hasNext()) {
         this.registerConverter((ImageConverter)iter.next());
      }

   }

   public void registerPreloader(ImagePreloader preloader) {
      if (log.isDebugEnabled()) {
         log.debug("Registered " + preloader.getClass().getName() + " with priority " + preloader.getPriority());
      }

      this.preloaders.add(this.newPreloaderHolder(preloader));
   }

   private synchronized PreloaderHolder newPreloaderHolder(ImagePreloader preloader) {
      PreloaderHolder holder = new PreloaderHolder();
      holder.preloader = preloader;
      holder.identifier = ++this.lastPreloaderIdentifier;
      return holder;
   }

   private synchronized void sortPreloaders() {
      if (this.lastPreloaderIdentifier != this.lastPreloaderSort) {
         Collections.sort(this.preloaders, new Comparator() {
            public int compare(Object o1, Object o2) {
               PreloaderHolder h1 = (PreloaderHolder)o1;
               long p1 = (long)h1.preloader.getPriority();
               p1 += (long)ImageImplRegistry.this.getAdditionalPenalty(h1.preloader.getClass().getName()).getValue();
               PreloaderHolder h2 = (PreloaderHolder)o2;
               int p2 = h2.preloader.getPriority();
               p2 += ImageImplRegistry.this.getAdditionalPenalty(h2.preloader.getClass().getName()).getValue();
               int diff = Penalty.truncate(p1 - (long)p2);
               if (diff != 0) {
                  return diff;
               } else {
                  diff = h1.identifier - h2.identifier;
                  return diff;
               }
            }
         });
         this.lastPreloaderSort = this.lastPreloaderIdentifier;
      }

   }

   public void registerLoaderFactory(ImageLoaderFactory loaderFactory) {
      if (!loaderFactory.isAvailable()) {
         if (log.isDebugEnabled()) {
            log.debug("ImageLoaderFactory reports not available: " + loaderFactory.getClass().getName());
         }

      } else {
         String[] mimes = loaderFactory.getSupportedMIMETypes();
         String[] var3 = mimes;
         int var4 = mimes.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            String mime = var3[var5];
            synchronized(this.loaders) {
               Map flavorMap = (Map)this.loaders.get(mime);
               if (flavorMap == null) {
                  flavorMap = new HashMap();
                  this.loaders.put(mime, flavorMap);
               }

               ImageFlavor[] flavors = loaderFactory.getSupportedFlavors(mime);
               ImageFlavor[] var10 = flavors;
               int var11 = flavors.length;

               for(int var12 = 0; var12 < var11; ++var12) {
                  ImageFlavor flavor = var10[var12];
                  List factoryList = (List)((Map)flavorMap).get(flavor);
                  if (factoryList == null) {
                     factoryList = new ArrayList();
                     ((Map)flavorMap).put(flavor, factoryList);
                  }

                  ((List)factoryList).add(loaderFactory);
                  if (log.isDebugEnabled()) {
                     log.debug("Registered " + loaderFactory.getClass().getName() + ": MIME = " + mime + ", Flavor = " + flavor);
                  }
               }
            }
         }

      }
   }

   public Collection getImageConverters() {
      return Collections.unmodifiableList(this.converters);
   }

   public int getImageConverterModifications() {
      return this.converterModifications;
   }

   public void registerConverter(ImageConverter converter) {
      this.converters.add(converter);
      ++this.converterModifications;
      if (log.isDebugEnabled()) {
         log.debug("Registered: " + converter.getClass().getName());
      }

   }

   public Iterator getPreloaderIterator() {
      this.sortPreloaders();
      Iterator iter = this.preloaders.iterator();
      MyIterator i = new MyIterator();
      i.iter = iter;
      return i;
   }

   public ImageLoaderFactory getImageLoaderFactory(ImageInfo imageInfo, ImageFlavor flavor) {
      String mime = imageInfo.getMimeType();
      Map flavorMap = (Map)this.loaders.get(mime);
      if (flavorMap != null) {
         List factoryList = (List)flavorMap.get(flavor);
         if (factoryList != null && factoryList.size() > 0) {
            Iterator iter = factoryList.iterator();
            int bestPenalty = Integer.MAX_VALUE;
            ImageLoaderFactory bestFactory = null;

            while(iter.hasNext()) {
               ImageLoaderFactory factory = (ImageLoaderFactory)iter.next();
               if (factory.isSupported(imageInfo)) {
                  ImageLoader loader = factory.newImageLoader(flavor);
                  int penalty = loader.getUsagePenalty();
                  if (penalty < bestPenalty) {
                     bestPenalty = penalty;
                     bestFactory = factory;
                  }
               }
            }

            return bestFactory;
         }
      }

      return null;
   }

   public ImageLoaderFactory[] getImageLoaderFactories(ImageInfo imageInfo, ImageFlavor flavor) {
      String mime = imageInfo.getMimeType();
      Collection matches = new TreeSet(new ImageLoaderFactoryComparator(flavor));
      Map flavorMap = (Map)this.loaders.get(mime);
      if (flavorMap != null) {
         Iterator var6 = flavorMap.entrySet().iterator();

         while(true) {
            List factoryList;
            do {
               do {
                  Map.Entry e;
                  ImageFlavor checkFlavor;
                  do {
                     if (!var6.hasNext()) {
                        return matches.size() == 0 ? null : (ImageLoaderFactory[])((ImageLoaderFactory[])matches.toArray(new ImageLoaderFactory[matches.size()]));
                     }

                     Object i = var6.next();
                     e = (Map.Entry)i;
                     checkFlavor = (ImageFlavor)e.getKey();
                  } while(!checkFlavor.isCompatible(flavor));

                  factoryList = (List)e.getValue();
               } while(factoryList == null);
            } while(factoryList.size() <= 0);

            Iterator var11 = factoryList.iterator();

            while(var11.hasNext()) {
               Object aFactoryList = var11.next();
               ImageLoaderFactory factory = (ImageLoaderFactory)aFactoryList;
               if (factory.isSupported(imageInfo)) {
                  matches.add(factory);
               }
            }
         }
      } else {
         return matches.size() == 0 ? null : (ImageLoaderFactory[])((ImageLoaderFactory[])matches.toArray(new ImageLoaderFactory[matches.size()]));
      }
   }

   public ImageLoaderFactory[] getImageLoaderFactories(String mime) {
      Map flavorMap = (Map)this.loaders.get(mime);
      if (flavorMap != null) {
         Set factories = new HashSet();
         Iterator var4 = flavorMap.values().iterator();

         while(var4.hasNext()) {
            Object o = var4.next();
            List factoryList = (List)o;
            factories.addAll(factoryList);
         }

         int factoryCount = factories.size();
         if (factoryCount > 0) {
            return (ImageLoaderFactory[])((ImageLoaderFactory[])factories.toArray(new ImageLoaderFactory[factoryCount]));
         }
      }

      return new ImageLoaderFactory[0];
   }

   public void setAdditionalPenalty(String className, Penalty penalty) {
      if (penalty != null) {
         this.additionalPenalties.put(className, penalty);
      } else {
         this.additionalPenalties.remove(className);
      }

      this.lastPreloaderSort = -1;
   }

   public Penalty getAdditionalPenalty(String className) {
      Penalty p = (Penalty)this.additionalPenalties.get(className);
      return p != null ? p : Penalty.ZERO_PENALTY;
   }

   private class ImageLoaderFactoryComparator implements Comparator {
      private ImageFlavor targetFlavor;

      public ImageLoaderFactoryComparator(ImageFlavor targetFlavor) {
         this.targetFlavor = targetFlavor;
      }

      public int compare(Object o1, Object o2) {
         ImageLoaderFactory f1 = (ImageLoaderFactory)o1;
         ImageLoader l1 = f1.newImageLoader(this.targetFlavor);
         long p1 = (long)l1.getUsagePenalty();
         p1 += (long)ImageImplRegistry.this.getAdditionalPenalty(l1.getClass().getName()).getValue();
         ImageLoaderFactory f2 = (ImageLoaderFactory)o2;
         ImageLoader l2 = f2.newImageLoader(this.targetFlavor);
         long p2 = (long)ImageImplRegistry.this.getAdditionalPenalty(l2.getClass().getName()).getValue();
         return Penalty.truncate(p1 - p2);
      }
   }

   static class MyIterator implements Iterator {
      Iterator iter;

      public boolean hasNext() {
         return this.iter.hasNext();
      }

      public Object next() {
         Object obj = this.iter.next();
         return obj != null ? ((PreloaderHolder)obj).preloader : null;
      }

      public void remove() {
         this.iter.remove();
      }
   }

   private static class PreloaderHolder {
      private ImagePreloader preloader;
      private int identifier;

      private PreloaderHolder() {
      }

      public String toString() {
         return this.preloader + " " + this.identifier;
      }

      // $FF: synthetic method
      PreloaderHolder(Object x0) {
         this();
      }
   }
}
