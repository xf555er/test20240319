package org.apache.fop.render.ps;

import org.apache.fop.render.RendererConfigOption;

public enum PSRendererOption implements RendererConfigOption {
   AUTO_ROTATE_LANDSCAPE("auto-rotate-landscape", false),
   LANGUAGE_LEVEL("language-level", 3),
   OPTIMIZE_RESOURCES("optimize-resources", false),
   SAFE_SET_PAGE_DEVICE("safe-set-page-device", false),
   DSC_COMPLIANT("dsc-compliant", true),
   RENDERING_MODE("rendering", PSRenderingMode.QUALITY),
   ACROBAT_DOWNSAMPLE("acrobat-downsample", false);

   private final String name;
   private final Object defaultValue;

   private PSRendererOption(String name, Object defaultValue) {
      this.name = name;
      this.defaultValue = defaultValue;
   }

   public String getName() {
      return this.name;
   }

   public Object getDefaultValue() {
      return this.defaultValue;
   }
}
