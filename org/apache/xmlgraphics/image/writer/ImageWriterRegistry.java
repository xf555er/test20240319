package org.apache.xmlgraphics.image.writer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import org.apache.xmlgraphics.util.Service;

public final class ImageWriterRegistry {
   private static volatile ImageWriterRegistry instance;
   private Map imageWriterMap = new HashMap();
   private Map preferredOrder;

   public ImageWriterRegistry() {
      Properties props = new Properties();
      InputStream in = this.getClass().getResourceAsStream("default-preferred-order.properties");
      if (in != null) {
         try {
            try {
               props.load(in);
            } finally {
               in.close();
            }
         } catch (IOException var7) {
            throw new RuntimeException("Could not load default preferred order due to I/O error: " + var7.getMessage());
         }
      }

      this.setPreferredOrder(props);
      this.setup();
   }

   public ImageWriterRegistry(Properties preferredOrder) {
      this.setPreferredOrder(preferredOrder);
      this.setup();
   }

   private void setPreferredOrder(Properties preferredOrder) {
      Map order = new HashMap();
      Iterator var3 = preferredOrder.entrySet().iterator();

      while(var3.hasNext()) {
         Map.Entry entry = (Map.Entry)var3.next();
         order.put(entry.getKey().toString(), Integer.parseInt(entry.getValue().toString()));
      }

      this.preferredOrder = order;
   }

   public static ImageWriterRegistry getInstance() {
      if (instance == null) {
         instance = new ImageWriterRegistry();
      }

      return instance;
   }

   private void setup() {
      Iterator iter = Service.providers(ImageWriter.class);

      while(iter.hasNext()) {
         ImageWriter writer = (ImageWriter)iter.next();
         this.register(writer);
      }

   }

   private int getPriority(ImageWriter writer) {
      String key = writer.getClass().getName();

      Integer value;
      for(value = (Integer)this.preferredOrder.get(key); value == null; value = (Integer)this.preferredOrder.get(key)) {
         int pos = key.lastIndexOf(".");
         if (pos < 0) {
            break;
         }

         key = key.substring(0, pos);
      }

      return value != null ? value : 0;
   }

   public void register(ImageWriter writer, int priority) {
      String key = writer.getClass().getName();
      this.preferredOrder.put(key, priority);
      this.register(writer);
   }

   public synchronized void register(ImageWriter writer) {
      List entries = (List)this.imageWriterMap.get(writer.getMIMEType());
      if (entries == null) {
         entries = new ArrayList();
         this.imageWriterMap.put(writer.getMIMEType(), entries);
      }

      int priority = this.getPriority(writer);
      ListIterator li = ((List)entries).listIterator();

      while(li.hasNext()) {
         ImageWriter w = (ImageWriter)li.next();
         if (this.getPriority(w) < priority) {
            li.previous();
            break;
         }
      }

      li.add(writer);
   }

   public synchronized ImageWriter getWriterFor(String mime) {
      List entries = (List)this.imageWriterMap.get(mime);
      if (entries == null) {
         return null;
      } else {
         Iterator var3 = entries.iterator();

         ImageWriter writer;
         do {
            if (!var3.hasNext()) {
               return null;
            }

            writer = (ImageWriter)var3.next();
         } while(!writer.isFunctional());

         return writer;
      }
   }
}
