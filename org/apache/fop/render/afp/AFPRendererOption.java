package org.apache.fop.render.afp;

import java.net.URI;
import org.apache.fop.afp.AFPResourceLevelDefaults;
import org.apache.fop.render.RendererConfigOption;

public enum AFPRendererOption implements RendererConfigOption {
   DEFAULT_RESOURCE_LEVELS("default-resource-levels", AFPResourceLevelDefaults.class),
   IMAGES("images", (Class)null),
   IMAGES_JPEG("jpeg", (Class)null),
   IMAGES_DITHERING_QUALITY("dithering-quality", Float.class),
   IMAGES_FS45("fs45", Boolean.class),
   IMAGES_MAPPING_OPTION("mapping_option", Byte.class),
   IMAGES_MODE("mode", Boolean.class),
   IMAGES_NATIVE("native", Boolean.class),
   IMAGES_WRAP_PSEG("pseg", Boolean.class),
   JPEG_ALLOW_JPEG_EMBEDDING("allow-embedding", Boolean.class),
   JPEG_BITMAP_ENCODING_QUALITY("bitmap-encoding-quality", Float.class),
   RENDERER_RESOLUTION("renderer-resolution", Integer.class),
   RESOURCE_GROUP_URI("resource-group-file", URI.class),
   SHADING("shading", AFPShadingMode.class),
   LINE_WIDTH_CORRECTION("line-width-correction", Float.class),
   GOCA("goca", Boolean.class),
   GOCA_WRAP_PSEG("pseg", Boolean.class),
   GOCA_TEXT("text", Boolean.class);

   private final String name;
   private final Class type;

   private AFPRendererOption(String name, Class type) {
      this.name = name;
      this.type = type;
   }

   public String getName() {
      return this.name;
   }

   public Class getType() {
      return this.type;
   }

   public Object getDefaultValue() {
      return null;
   }
}
