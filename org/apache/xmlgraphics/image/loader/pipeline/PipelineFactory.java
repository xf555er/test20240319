package org.apache.xmlgraphics.image.loader.pipeline;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageManager;
import org.apache.xmlgraphics.image.loader.impl.CompositeImageLoader;
import org.apache.xmlgraphics.image.loader.spi.ImageConverter;
import org.apache.xmlgraphics.image.loader.spi.ImageImplRegistry;
import org.apache.xmlgraphics.image.loader.spi.ImageLoader;
import org.apache.xmlgraphics.image.loader.spi.ImageLoaderFactory;
import org.apache.xmlgraphics.image.loader.util.Penalty;
import org.apache.xmlgraphics.util.dijkstra.DefaultEdgeDirectory;
import org.apache.xmlgraphics.util.dijkstra.DijkstraAlgorithm;
import org.apache.xmlgraphics.util.dijkstra.Vertex;

public class PipelineFactory {
   protected static final Log log = LogFactory.getLog(PipelineFactory.class);
   private ImageManager manager;
   private int converterEdgeDirectoryVersion = -1;
   private DefaultEdgeDirectory converterEdgeDirectory;

   public PipelineFactory(ImageManager manager) {
      this.manager = manager;
   }

   private DefaultEdgeDirectory getEdgeDirectory() {
      ImageImplRegistry registry = this.manager.getRegistry();
      if (registry.getImageConverterModifications() != this.converterEdgeDirectoryVersion) {
         Collection converters = registry.getImageConverters();
         DefaultEdgeDirectory dir = new DefaultEdgeDirectory();
         Iterator var4 = converters.iterator();

         while(var4.hasNext()) {
            Object converter1 = var4.next();
            ImageConverter converter = (ImageConverter)converter1;
            Penalty penalty = Penalty.toPenalty(converter.getConversionPenalty());
            penalty = penalty.add(registry.getAdditionalPenalty(converter.getClass().getName()));
            dir.addEdge(new ImageConversionEdge(converter, penalty));
         }

         this.converterEdgeDirectoryVersion = registry.getImageConverterModifications();
         this.converterEdgeDirectory = dir;
      }

      return this.converterEdgeDirectory;
   }

   public ImageProviderPipeline newImageConverterPipeline(Image originalImage, ImageFlavor targetFlavor) {
      DefaultEdgeDirectory dir = this.getEdgeDirectory();
      ImageRepresentation destination = new ImageRepresentation(targetFlavor);
      ImageProviderPipeline pipeline = this.findPipeline(dir, originalImage.getFlavor(), destination);
      return pipeline;
   }

   public ImageProviderPipeline newImageConverterPipeline(ImageInfo imageInfo, ImageFlavor targetFlavor) {
      ImageProviderPipeline[] candidates = this.determineCandidatePipelines(imageInfo, targetFlavor);
      if (candidates.length > 0) {
         Arrays.sort(candidates, new PipelineComparator());
         ImageProviderPipeline pipeline = candidates[0];
         if (pipeline != null && log.isDebugEnabled()) {
            log.debug("Pipeline: " + pipeline + " with penalty " + pipeline.getConversionPenalty());
         }

         return pipeline;
      } else {
         return null;
      }
   }

   public ImageProviderPipeline[] determineCandidatePipelines(ImageInfo imageInfo, ImageFlavor targetFlavor) {
      String originalMime = imageInfo.getMimeType();
      ImageImplRegistry registry = this.manager.getRegistry();
      List candidates = new ArrayList();
      DefaultEdgeDirectory dir = this.getEdgeDirectory();
      ImageLoaderFactory[] loaderFactories = registry.getImageLoaderFactories(imageInfo, targetFlavor);
      int i;
      if (loaderFactories != null) {
         Object loader;
         if (loaderFactories.length == 1) {
            loader = loaderFactories[0].newImageLoader(targetFlavor);
         } else {
            int count = loaderFactories.length;
            ImageLoader[] loaders = new ImageLoader[count];

            for(i = 0; i < count; ++i) {
               loaders[i] = loaderFactories[i].newImageLoader(targetFlavor);
            }

            loader = new CompositeImageLoader(loaders);
         }

         ImageProviderPipeline pipeline = new ImageProviderPipeline(this.manager.getCache(), (ImageLoader)loader);
         candidates.add(pipeline);
      } else {
         if (log.isTraceEnabled()) {
            log.trace("No ImageLoaderFactory found that can load this format (" + targetFlavor + ") directly. Trying ImageConverters instead...");
         }

         ImageRepresentation destination = new ImageRepresentation(targetFlavor);
         loaderFactories = registry.getImageLoaderFactories(originalMime);
         if (loaderFactories != null) {
            ImageLoaderFactory[] var22 = loaderFactories;
            int var23 = loaderFactories.length;

            for(i = 0; i < var23; ++i) {
               ImageLoaderFactory loaderFactory = var22[i];
               ImageFlavor[] flavors = loaderFactory.getSupportedFlavors(originalMime);
               ImageFlavor[] var14 = flavors;
               int var15 = flavors.length;

               for(int var16 = 0; var16 < var15; ++var16) {
                  ImageFlavor flavor = var14[var16];
                  ImageProviderPipeline pipeline = this.findPipeline(dir, flavor, destination);
                  if (pipeline != null) {
                     ImageLoader loader = loaderFactory.newImageLoader(flavor);
                     pipeline.setImageLoader(loader);
                     candidates.add(pipeline);
                  }
               }
            }
         }
      }

      return (ImageProviderPipeline[])((ImageProviderPipeline[])candidates.toArray(new ImageProviderPipeline[candidates.size()]));
   }

   private ImageProviderPipeline findPipeline(DefaultEdgeDirectory dir, ImageFlavor originFlavor, ImageRepresentation destination) {
      DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(dir);
      ImageRepresentation origin = new ImageRepresentation(originFlavor);
      dijkstra.execute(origin, destination);
      if (log.isTraceEnabled()) {
         log.trace("Lowest penalty: " + dijkstra.getLowestPenalty(destination));
      }

      Vertex prev = destination;
      Vertex pred = dijkstra.getPredecessor(destination);
      if (pred == null) {
         if (log.isTraceEnabled()) {
            log.trace("No route found!");
         }

         return null;
      } else {
         LinkedList stops;
         for(stops = new LinkedList(); (pred = dijkstra.getPredecessor((Vertex)prev)) != null; prev = pred) {
            ImageConversionEdge edge = (ImageConversionEdge)dir.getBestEdge(pred, (Vertex)prev);
            stops.addFirst(edge);
         }

         ImageProviderPipeline pipeline = new ImageProviderPipeline(this.manager.getCache(), (ImageLoader)null);
         Iterator var10 = stops.iterator();

         while(var10.hasNext()) {
            Object stop = var10.next();
            ImageConversionEdge edge = (ImageConversionEdge)stop;
            pipeline.addConverter(edge.getImageConverter());
         }

         return pipeline;
      }
   }

   public ImageProviderPipeline[] determineCandidatePipelines(ImageInfo imageInfo, ImageFlavor[] flavors) {
      List candidates = new ArrayList();
      ImageFlavor[] var4 = flavors;
      int var5 = flavors.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         ImageFlavor flavor = var4[var6];
         ImageProviderPipeline pipeline = this.newImageConverterPipeline(imageInfo, flavor);
         if (pipeline != null) {
            Penalty p = pipeline.getConversionPenalty(this.manager.getRegistry());
            if (!p.isInfinitePenalty()) {
               candidates.add(pipeline);
            }
         }
      }

      return (ImageProviderPipeline[])((ImageProviderPipeline[])candidates.toArray(new ImageProviderPipeline[candidates.size()]));
   }

   public ImageProviderPipeline[] determineCandidatePipelines(Image sourceImage, ImageFlavor[] flavors) {
      List candidates = new ArrayList();
      ImageFlavor[] var4 = flavors;
      int var5 = flavors.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         ImageFlavor flavor = var4[var6];
         ImageProviderPipeline pipeline = this.newImageConverterPipeline(sourceImage, flavor);
         if (pipeline != null) {
            candidates.add(pipeline);
         }
      }

      return (ImageProviderPipeline[])((ImageProviderPipeline[])candidates.toArray(new ImageProviderPipeline[candidates.size()]));
   }

   private static class PipelineComparator implements Comparator, Serializable {
      private static final long serialVersionUID = 1161513617996198090L;

      private PipelineComparator() {
      }

      public int compare(Object o1, Object o2) {
         ImageProviderPipeline p1 = (ImageProviderPipeline)o1;
         ImageProviderPipeline p2 = (ImageProviderPipeline)o2;
         return p1.getConversionPenalty() - p2.getConversionPenalty();
      }

      // $FF: synthetic method
      PipelineComparator(Object x0) {
         this();
      }
   }
}
