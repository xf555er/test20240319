package org.apache.fop.afp;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class AFPResourceLevelDefaults {
   private static final Map RESOURCE_TYPE_NAMES = new HashMap();
   private Map defaultResourceLevels = new HashMap();

   private static void registerResourceTypeName(String name, byte type) {
      RESOURCE_TYPE_NAMES.put(name.toLowerCase(), type);
   }

   private static byte getResourceType(String resourceTypeName) {
      Byte result = (Byte)RESOURCE_TYPE_NAMES.get(resourceTypeName.toLowerCase());
      if (result == null) {
         throw new IllegalArgumentException("Unknown resource type name: " + resourceTypeName);
      } else {
         return result;
      }
   }

   public AFPResourceLevelDefaults() {
      this.setDefaultResourceLevel((byte)3, new AFPResourceLevel(AFPResourceLevel.ResourceType.INLINE));
   }

   public void setDefaultResourceLevel(String type, AFPResourceLevel level) {
      this.setDefaultResourceLevel(getResourceType(type), level);
   }

   public void setDefaultResourceLevel(byte type, AFPResourceLevel level) {
      this.defaultResourceLevels.put(type, level);
   }

   public AFPResourceLevel getDefaultResourceLevel(byte type) {
      AFPResourceLevel result = (AFPResourceLevel)this.defaultResourceLevels.get(type);
      if (result == null) {
         result = AFPResourceInfo.DEFAULT_LEVEL;
      }

      return result;
   }

   public void mergeFrom(AFPResourceLevelDefaults other) {
      Iterator var2 = other.defaultResourceLevels.entrySet().iterator();

      while(var2.hasNext()) {
         Object o = var2.next();
         Map.Entry entry = (Map.Entry)o;
         Byte type = (Byte)entry.getKey();
         AFPResourceLevel level = (AFPResourceLevel)entry.getValue();
         this.defaultResourceLevels.put(type, level);
      }

   }

   static {
      registerResourceTypeName("goca", (byte)3);
      registerResourceTypeName("bitmap", (byte)6);
   }
}
