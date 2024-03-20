package org.apache.fop.fonts;

import org.apache.fop.apps.io.InternalResourceResolver;

public abstract class CIDFont extends CustomFont {
   protected int[] width;

   public CIDFont(InternalResourceResolver resourceResolver) {
      super(resourceResolver);
   }

   public abstract CIDFontType getCIDType();

   public abstract String getRegistry();

   public abstract String getOrdering();

   public abstract int getSupplement();

   public abstract CIDSet getCIDSet();

   public abstract boolean hasCodePoint(int var1);

   public abstract int mapCodePoint(int var1);

   public int getDefaultWidth() {
      return 0;
   }

   public boolean isMultiByte() {
      return this.getFontType() != FontType.TYPE1C;
   }
}
