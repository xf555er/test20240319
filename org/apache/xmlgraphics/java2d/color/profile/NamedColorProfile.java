package org.apache.xmlgraphics.java2d.color.profile;

import org.apache.xmlgraphics.java2d.color.NamedColorSpace;
import org.apache.xmlgraphics.java2d.color.RenderingIntent;

public class NamedColorProfile {
   private String profileName;
   private String copyright;
   private NamedColorSpace[] namedColors;
   private RenderingIntent renderingIntent;

   public NamedColorProfile(String profileName, String copyright, NamedColorSpace[] namedColors, RenderingIntent intent) {
      this.renderingIntent = RenderingIntent.PERCEPTUAL;
      this.profileName = profileName;
      this.copyright = copyright;
      this.namedColors = namedColors;
      this.renderingIntent = intent;
   }

   public RenderingIntent getRenderingIntent() {
      return this.renderingIntent;
   }

   public NamedColorSpace[] getNamedColors() {
      NamedColorSpace[] copy = new NamedColorSpace[this.namedColors.length];
      System.arraycopy(this.namedColors, 0, copy, 0, this.namedColors.length);
      return copy;
   }

   public NamedColorSpace getNamedColor(String name) {
      if (this.namedColors != null) {
         NamedColorSpace[] var2 = this.namedColors;
         int var3 = var2.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            NamedColorSpace namedColor = var2[var4];
            if (namedColor.getColorName().equals(name)) {
               return namedColor;
            }
         }
      }

      return null;
   }

   public String getProfileName() {
      return this.profileName;
   }

   public String getCopyright() {
      return this.copyright;
   }

   public String toString() {
      StringBuffer sb = new StringBuffer("Named color profile: ");
      sb.append(this.getProfileName());
      sb.append(", ").append(this.namedColors.length).append(" colors");
      return sb.toString();
   }
}
