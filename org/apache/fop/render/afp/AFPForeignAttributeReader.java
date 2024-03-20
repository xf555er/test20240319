package org.apache.fop.render.afp;

import java.net.URI;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.afp.AFPResourceInfo;
import org.apache.fop.afp.AFPResourceLevel;
import org.apache.xmlgraphics.util.QName;

public class AFPForeignAttributeReader {
   private static final Log LOG = LogFactory.getLog("org.apache.xmlgraphics.afp");
   public static final QName RESOURCE_NAME = new QName("http://xmlgraphics.apache.org/fop/extensions/afp", "afp:resource-name");
   public static final QName RESOURCE_LEVEL = new QName("http://xmlgraphics.apache.org/fop/extensions/afp", "afp:resource-level");
   public static final QName RESOURCE_GROUP_URI = new QName("http://xmlgraphics.apache.org/fop/extensions/afp", "afp:resource-group-file");

   public AFPResourceInfo getResourceInfo(Map foreignAttributes) {
      AFPResourceInfo resourceInfo = new AFPResourceInfo();
      if (foreignAttributes != null && !foreignAttributes.isEmpty()) {
         String resourceName = (String)foreignAttributes.get(RESOURCE_NAME);
         if (resourceName != null) {
            resourceInfo.setName(resourceName);
         }

         AFPResourceLevel level = this.getResourceLevel(foreignAttributes);
         if (level != null) {
            resourceInfo.setLevel(level);
         }
      }

      return resourceInfo;
   }

   public AFPResourceLevel getResourceLevel(Map foreignAttributes) {
      AFPResourceLevel resourceLevel = null;
      if (foreignAttributes != null && !foreignAttributes.isEmpty() && foreignAttributes.containsKey(RESOURCE_LEVEL)) {
         String levelString = (String)foreignAttributes.get(RESOURCE_LEVEL);
         resourceLevel = AFPResourceLevel.valueOf(levelString);
         if (resourceLevel != null && resourceLevel.isExternal()) {
            String resourceGroupUri = (String)foreignAttributes.get(RESOURCE_GROUP_URI);
            if (resourceGroupUri == null) {
               String msg = RESOURCE_GROUP_URI + " not specified";
               throw new UnsupportedOperationException(msg);
            }

            resourceLevel.setExternalUri(URI.create(resourceGroupUri));
         }
      }

      return resourceLevel;
   }
}
