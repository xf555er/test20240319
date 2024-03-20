package org.apache.fop.hyphenation;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.ResourceEventProducer;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.events.EventBroadcaster;
import org.xml.sax.InputSource;

public final class Hyphenator {
   private static final Log log = LogFactory.getLog(Hyphenator.class);
   private static boolean statisticsDump;
   public static final String HYPTYPE = Hyphenator.class.toString() + "HYP";
   public static final String XMLTYPE = Hyphenator.class.toString() + "XML";

   private Hyphenator() {
   }

   public static HyphenationTree getHyphenationTree(String lang, String country, InternalResourceResolver resourceResolver, Map hyphPatNames, FOUserAgent foUserAgent) {
      String llccKey = HyphenationTreeCache.constructLlccKey(lang, country);
      HyphenationTreeCache cache = foUserAgent.getHyphenationTreeCache();
      if (cache != null && !cache.isMissing(llccKey)) {
         HyphenationTree hTree = cache.getHyphenationTree(lang, country);
         if (hTree != null) {
            return hTree;
         } else {
            String key = HyphenationTreeCache.constructUserKey(lang, country, hyphPatNames);
            if (key == null) {
               key = llccKey;
            }

            if (resourceResolver != null) {
               hTree = getUserHyphenationTree(key, resourceResolver);
            }

            if (hTree == null) {
               hTree = getFopHyphenationTree(key);
            }

            if (hTree == null && country != null && !country.equals("none")) {
               return getHyphenationTree(lang, (String)null, resourceResolver, hyphPatNames, foUserAgent);
            } else {
               if (hTree != null) {
                  cache.cache(llccKey, hTree);
               } else {
                  EventBroadcaster eventBroadcaster = foUserAgent.getEventBroadcaster();
                  if (eventBroadcaster == null) {
                     log.error("Couldn't find hyphenation pattern " + llccKey);
                  } else {
                     ResourceEventProducer producer = ResourceEventProducer.Provider.get(eventBroadcaster);
                     String name = key.replace(HYPTYPE, "").replace(XMLTYPE, "");
                     producer.hyphenationNotFound(cache, name);
                  }

                  cache.noteMissing(llccKey);
               }

               return hTree;
            }
         }
      } else {
         return null;
      }
   }

   private static InputStream getResourceStream(String key) {
      InputStream is = null;

      try {
         Method getCCL = Thread.class.getMethod("getContextClassLoader");
         if (getCCL != null) {
            ClassLoader contextClassLoader = (ClassLoader)getCCL.invoke(Thread.currentThread());
            is = contextClassLoader.getResourceAsStream("hyph/" + key + ".hyp");
         }
      } catch (NoSuchMethodException var4) {
      } catch (IllegalAccessException var5) {
      } catch (InvocationTargetException var6) {
      }

      if (is == null) {
         is = Hyphenator.class.getResourceAsStream("/hyph/" + key + ".hyp");
      }

      return is;
   }

   private static HyphenationTree readHyphenationTree(InputStream in) {
      HyphenationTree hTree = null;

      try {
         ObjectInputStream ois = new ObjectInputStream(in);
         hTree = (HyphenationTree)ois.readObject();
      } catch (IOException var3) {
         log.error("I/O error while loading precompiled hyphenation pattern file", var3);
      } catch (ClassNotFoundException var4) {
         log.error("Error while reading hyphenation object from file", var4);
      }

      return hTree;
   }

   public static HyphenationTree getFopHyphenationTree(String key) {
      InputStream is = getResourceStream(key);
      if (is == null) {
         if (log.isDebugEnabled()) {
            log.debug("Couldn't find precompiled hyphenation pattern " + key + " in resources");
         }

         return null;
      } else {
         return readHyphenationTree(is);
      }
   }

   public static HyphenationTree getUserHyphenationTree(String key, InternalResourceResolver resourceResolver) {
      HyphenationTree hTree = null;
      String name = key + ".hyp";
      if (key.endsWith(HYPTYPE)) {
         name = key.replace(HYPTYPE, "");
      }

      InputStream in;
      if (!key.endsWith(XMLTYPE)) {
         try {
            in = getHyphenationTreeStream(name, resourceResolver);

            try {
               hTree = readHyphenationTree(in);
            } finally {
               IOUtils.closeQuietly(in);
            }

            return hTree;
         } catch (IOException var21) {
            if (log.isDebugEnabled()) {
               log.debug("I/O problem while trying to load " + name, var21);
            }
         }
      }

      name = key + ".xml";
      if (key.endsWith(XMLTYPE)) {
         name = key.replace(XMLTYPE, "");
      }

      hTree = new HyphenationTree();

      try {
         in = getHyphenationTreeStream(name, resourceResolver);

         try {
            InputSource src = new InputSource(in);
            src.setSystemId(name);
            hTree.loadPatterns(src);
         } finally {
            IOUtils.closeQuietly(in);
         }

         if (statisticsDump) {
            System.out.println("Stats: ");
            hTree.printStats();
         }

         return hTree;
      } catch (HyphenationException var19) {
         log.error("Can't load user patterns from XML file " + name + ": " + var19.getMessage());
         return null;
      } catch (IOException var20) {
         if (log.isDebugEnabled()) {
            log.debug("I/O problem while trying to load " + name, var20);
         }

         return null;
      }
   }

   private static InputStream getHyphenationTreeStream(String name, InternalResourceResolver resourceResolver) throws IOException {
      try {
         return new BufferedInputStream(resourceResolver.getResource(name));
      } catch (URISyntaxException var3) {
         log.debug("An exception was thrown while attempting to load " + name, var3);
         return null;
      }
   }

   public static Hyphenation hyphenate(String lang, String country, InternalResourceResolver resourceResolver, Map hyphPatNames, String word, int leftMin, int rightMin, FOUserAgent foUserAgent) {
      HyphenationTree hTree = getHyphenationTree(lang, country, resourceResolver, hyphPatNames, foUserAgent);
      return hTree == null ? null : hTree.hyphenate(word, leftMin, rightMin);
   }
}
