package org.apache.fop.render.pcl;

import org.apache.fop.render.RendererConfigOption;

public enum Java2DRendererOption implements RendererConfigOption {
   RENDERING_MODE("rendering", PCLRenderingMode.class, PCLRenderingMode.QUALITY),
   TEXT_RENDERING("text-rendering", Boolean.class, Boolean.FALSE),
   DISABLE_PJL("disable-pjl", Boolean.class, Boolean.FALSE),
   OPTIMIZE_RESOURCES("optimize-resources", Boolean.class, Boolean.FALSE),
   MODE_COLOR("color", Boolean.class, Boolean.FALSE);

   private final String name;
   private final Class type;
   private final Object defaultValue;

   private Java2DRendererOption(String name, Class type, Object defaultValue) {
      this.name = name;
      this.type = type;
      this.defaultValue = defaultValue;
      if (defaultValue != null && !type.isAssignableFrom(defaultValue.getClass())) {
         throw new IllegalArgumentException("default value " + defaultValue + " is not of type " + type);
      }
   }

   public String getName() {
      return this.name;
   }

   Class getType() {
      return this.type;
   }

   public Object getDefaultValue() {
      return this.defaultValue;
   }
}
