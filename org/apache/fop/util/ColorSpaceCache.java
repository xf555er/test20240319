package org.apache.fop.util;

import java.awt.color.ColorSpace;
import java.awt.color.ICC_Profile;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.xmlgraphics.java2d.color.ICCColorSpaceWithIntent;
import org.apache.xmlgraphics.java2d.color.RenderingIntent;

public class ColorSpaceCache {
   private static Log log = LogFactory.getLog(ColorSpaceCache.class);
   private InternalResourceResolver resourceResolver;
   private Map colorSpaceMap = Collections.synchronizedMap(new HashMap());

   public ColorSpaceCache(InternalResourceResolver resourceResolver) {
      this.resourceResolver = resourceResolver;
   }

   public ColorSpace get(String profileName, String iccProfileSrc, RenderingIntent renderingIntent) {
      String key = profileName + ":" + iccProfileSrc;
      ColorSpace colorSpace = null;
      if (!this.colorSpaceMap.containsKey(key)) {
         try {
            ICC_Profile iccProfile = null;
            InputStream stream = this.resourceResolver.getResource(iccProfileSrc);
            if (stream != null) {
               iccProfile = ICC_Profile.getInstance(stream);
            }

            if (iccProfile != null) {
               colorSpace = new ICCColorSpaceWithIntent(iccProfile, renderingIntent, profileName, iccProfileSrc);
            }
         } catch (Exception var8) {
            log.warn("Exception thrown resolving the color space: " + var8.getMessage());
         }

         if (colorSpace != null) {
            this.colorSpaceMap.put(key, colorSpace);
         } else {
            log.warn("Color profile '" + iccProfileSrc + "' not found.");
         }
      } else {
         colorSpace = (ColorSpace)this.colorSpaceMap.get(key);
      }

      return (ColorSpace)colorSpace;
   }
}
