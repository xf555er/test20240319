package org.apache.fop.render.bitmap;

import java.awt.Color;
import org.apache.fop.render.RendererConfigOption;

public enum BitmapRendererOption implements RendererConfigOption {
   JAVA2D_TRANSPARENT_PAGE_BACKGROUND("transparent-page-background", false),
   BACKGROUND_COLOR("background-color", Color.WHITE),
   ANTI_ALIASING("anti-aliasing", true),
   RENDERING_QUALITY_ELEMENT("rendering"),
   RENDERING_QUALITY("quality", true),
   RENDERING_SPEED("speed"),
   COLOR_MODE("color-mode", 2),
   COLOR_MODE_RGBA("rgba"),
   COLOR_MODE_RGB("rgb"),
   COLOR_MODE_GRAY("gray"),
   COLOR_MODE_BINARY("binary"),
   COLOR_MODE_BILEVEL("bi-level");

   private final String name;
   private final Object defaultValue;

   private BitmapRendererOption(String name, Object defaultValue) {
      this.name = name;
      this.defaultValue = defaultValue;
   }

   private BitmapRendererOption(String name) {
      this(name, (Object)null);
   }

   public String getName() {
      return this.name;
   }

   public Object getDefaultValue() {
      return this.defaultValue;
   }

   public static BitmapRendererOption getValue(String str) {
      BitmapRendererOption[] var1 = values();
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         BitmapRendererOption opt = var1[var3];
         if (opt.getName().equalsIgnoreCase(str)) {
            return opt;
         }
      }

      return null;
   }
}
