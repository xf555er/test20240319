package org.apache.batik.ext.awt.image.spi;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.batik.util.Service;

public final class ImageWriterRegistry {
   private static ImageWriterRegistry instance;
   private final Map imageWriterMap = new HashMap();

   private ImageWriterRegistry() {
      this.setup();
   }

   public static ImageWriterRegistry getInstance() {
      Class var0 = ImageWriterRegistry.class;
      synchronized(ImageWriterRegistry.class) {
         if (instance == null) {
            instance = new ImageWriterRegistry();
         }

         return instance;
      }
   }

   private void setup() {
      Iterator iter = Service.providers(ImageWriter.class);

      while(iter.hasNext()) {
         ImageWriter writer = (ImageWriter)iter.next();
         this.register(writer);
      }

   }

   public void register(ImageWriter writer) {
      this.imageWriterMap.put(writer.getMIMEType(), writer);
   }

   public ImageWriter getWriterFor(String mime) {
      return (ImageWriter)this.imageWriterMap.get(mime);
   }
}
