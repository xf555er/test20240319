package org.apache.fop.render;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.util.Service;

public class ImageHandlerRegistry {
   private static Log log = LogFactory.getLog(ImageHandlerRegistry.class);
   private static final Comparator HANDLER_COMPARATOR = new Comparator() {
      public int compare(ImageHandler o1, ImageHandler o2) {
         return o1.getPriority() - o2.getPriority();
      }
   };
   private Map handlers = new HashMap();
   private List handlerList = new LinkedList();
   private int handlerRegistrations;

   public ImageHandlerRegistry() {
      this.discoverHandlers();
   }

   public void addHandler(String classname) {
      try {
         ImageHandler handlerInstance = (ImageHandler)Class.forName(classname).getDeclaredConstructor().newInstance();
         this.addHandler(handlerInstance);
      } catch (ClassNotFoundException var3) {
         throw new IllegalArgumentException("Could not find " + classname);
      } catch (InstantiationException var4) {
         throw new IllegalArgumentException("Could not instantiate " + classname);
      } catch (IllegalAccessException var5) {
         throw new IllegalArgumentException("Could not access " + classname);
      } catch (ClassCastException var6) {
         throw new IllegalArgumentException(classname + " is not an " + ImageHandler.class.getName());
      } catch (NoSuchMethodException var7) {
         throw new IllegalArgumentException(var7);
      } catch (InvocationTargetException var8) {
         throw new IllegalArgumentException(var8);
      }
   }

   public synchronized void addHandler(ImageHandler handler) {
      Class imageClass = handler.getSupportedImageClass();
      this.handlers.put(imageClass, handler);
      ListIterator iter = this.handlerList.listIterator();

      while(iter.hasNext()) {
         ImageHandler h = (ImageHandler)iter.next();
         if (HANDLER_COMPARATOR.compare(handler, h) < 0) {
            iter.previous();
            break;
         }
      }

      iter.add(handler);
      ++this.handlerRegistrations;
   }

   public ImageHandler getHandler(RenderingContext targetContext, Image image) {
      Iterator var3 = this.handlerList.iterator();

      ImageHandler h;
      do {
         if (!var3.hasNext()) {
            return null;
         }

         h = (ImageHandler)var3.next();
      } while(!h.isCompatible(targetContext, image));

      return h;
   }

   public synchronized ImageFlavor[] getSupportedFlavors(RenderingContext context) {
      List flavors = new ArrayList();
      Iterator var3 = this.handlerList.iterator();

      while(var3.hasNext()) {
         ImageHandler handler = (ImageHandler)var3.next();
         if (handler.isCompatible(context, (Image)null)) {
            ImageFlavor[] f = handler.getSupportedImageFlavors();
            Collections.addAll(flavors, f);
         }
      }

      return (ImageFlavor[])flavors.toArray(new ImageFlavor[flavors.size()]);
   }

   private void discoverHandlers() {
      Iterator providers = Service.providers(ImageHandler.class);
      if (providers != null) {
         while(providers.hasNext()) {
            ImageHandler handler = (ImageHandler)providers.next();

            try {
               if (log.isDebugEnabled()) {
                  log.debug("Dynamically adding ImageHandler: " + handler.getClass().getName());
               }

               this.addHandler(handler);
            } catch (IllegalArgumentException var4) {
               log.error("Error while adding ImageHandler", var4);
            }
         }
      }

   }
}
