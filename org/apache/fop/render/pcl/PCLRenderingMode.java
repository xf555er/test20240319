package org.apache.fop.render.pcl;

import java.io.ObjectStreamException;

public enum PCLRenderingMode {
   QUALITY("quality", 1.0F),
   SPEED("speed", 0.25F),
   BITMAP("bitmap", 1.0F);

   private static final long serialVersionUID = 6359884255324755026L;
   private String name;
   private float defaultDitheringQuality;

   private PCLRenderingMode(String name, float defaultDitheringQuality) {
      this.name = name;
      this.defaultDitheringQuality = defaultDitheringQuality;
   }

   public String getName() {
      return this.name;
   }

   public float getDefaultDitheringQuality() {
      return this.defaultDitheringQuality;
   }

   public static PCLRenderingMode getValueOf(String name) {
      PCLRenderingMode[] var1 = values();
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         PCLRenderingMode mode = var1[var3];
         if (mode.getName().equalsIgnoreCase(name)) {
            return mode;
         }
      }

      throw new IllegalArgumentException("Illegal value for enumeration: " + name);
   }

   private Object readResolve() throws ObjectStreamException {
      return getValueOf(this.getName());
   }

   public String toString() {
      return "PCLRenderingMode:" + this.name;
   }
}
