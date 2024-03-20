package org.apache.fop.render.java2d;

import org.apache.fop.render.RendererConfigOption;

public enum Java2DRendererOption implements RendererConfigOption {
   JAVA2D_TRANSPARENT_PAGE_BACKGROUND("transparent-page-background");

   private final String name;

   private Java2DRendererOption(String name) {
      this.name = name;
   }

   public String getName() {
      return this.name;
   }

   public Object getDefaultValue() {
      return null;
   }
}
