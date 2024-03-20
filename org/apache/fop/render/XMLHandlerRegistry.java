package org.apache.fop.render;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlgraphics.util.Service;

public class XMLHandlerRegistry {
   private static Log log = LogFactory.getLog(XMLHandlerRegistry.class);
   private Map handlers = new HashMap();

   public XMLHandlerRegistry() {
      this.discoverXMLHandlers();
   }

   private void setDefaultXMLHandler(XMLHandler handler) {
      this.addXMLHandler("*", handler);
   }

   public void addXMLHandler(String classname) {
      try {
         XMLHandler handlerInstance = (XMLHandler)Class.forName(classname).getDeclaredConstructor().newInstance();
         this.addXMLHandler(handlerInstance);
      } catch (ClassNotFoundException var3) {
         throw new IllegalArgumentException("Could not find " + classname);
      } catch (InstantiationException var4) {
         throw new IllegalArgumentException("Could not instantiate " + classname);
      } catch (IllegalAccessException var5) {
         throw new IllegalArgumentException("Could not access " + classname);
      } catch (ClassCastException var6) {
         throw new IllegalArgumentException(classname + " is not an " + XMLHandler.class.getName());
      } catch (NoSuchMethodException var7) {
         throw new IllegalArgumentException(var7);
      } catch (InvocationTargetException var8) {
         throw new IllegalArgumentException(var8);
      }
   }

   public void addXMLHandler(XMLHandler handler) {
      String ns = handler.getNamespace();
      if (ns == null) {
         this.setDefaultXMLHandler(handler);
      } else {
         this.addXMLHandler(ns, handler);
      }

   }

   private void addXMLHandler(String ns, XMLHandler handler) {
      List lst = (List)this.handlers.get(ns);
      if (lst == null) {
         lst = new ArrayList();
         this.handlers.put(ns, lst);
      }

      ((List)lst).add(handler);
   }

   public XMLHandler getXMLHandler(Renderer renderer, String ns) {
      List lst = (List)this.handlers.get(ns);
      XMLHandler handler = this.getXMLHandler(renderer, lst);
      if (handler == null) {
         lst = (List)this.handlers.get("*");
         handler = this.getXMLHandler(renderer, lst);
      }

      return handler;
   }

   private XMLHandler getXMLHandler(Renderer renderer, List lst) {
      if (lst != null) {
         Iterator var4 = lst.iterator();

         while(var4.hasNext()) {
            XMLHandler aLst = (XMLHandler)var4.next();
            if (aLst.supportsRenderer(renderer)) {
               return aLst;
            }
         }
      }

      return null;
   }

   private void discoverXMLHandlers() {
      Iterator providers = Service.providers(XMLHandler.class);
      if (providers != null) {
         while(providers.hasNext()) {
            XMLHandler handler = (XMLHandler)providers.next();

            try {
               if (log.isDebugEnabled()) {
                  log.debug("Dynamically adding XMLHandler: " + handler.getClass().getName());
               }

               this.addXMLHandler(handler);
            } catch (IllegalArgumentException var4) {
               log.error("Error while adding XMLHandler", var4);
            }
         }
      }

   }
}
