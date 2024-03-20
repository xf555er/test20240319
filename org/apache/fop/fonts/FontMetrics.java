package org.apache.fop.fonts;

import java.awt.Rectangle;
import java.net.URI;
import java.util.Map;
import java.util.Set;

public interface FontMetrics {
   URI getFontURI();

   String getFontName();

   String getFullName();

   Set getFamilyNames();

   String getEmbedFontName();

   FontType getFontType();

   int getMaxAscent(int var1);

   int getAscender(int var1);

   int getCapHeight(int var1);

   int getDescender(int var1);

   int getXHeight(int var1);

   int getWidth(int var1, int var2);

   int[] getWidths();

   Rectangle getBoundingBox(int var1, int var2);

   boolean hasKerningInfo();

   Map getKerningInfo();

   int getUnderlinePosition(int var1);

   int getUnderlineThickness(int var1);

   int getStrikeoutPosition(int var1);

   int getStrikeoutThickness(int var1);

   boolean hasFeature(int var1, String var2, String var3, String var4);

   boolean isMultiByte();
}
