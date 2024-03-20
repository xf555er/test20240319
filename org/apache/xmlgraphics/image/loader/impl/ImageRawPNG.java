package org.apache.xmlgraphics.image.loader.impl;

import java.awt.Color;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.ColorModel;
import java.io.InputStream;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.ImageInfo;

public class ImageRawPNG extends ImageRawStream {
   private ColorModel cm;
   private ICC_Profile iccProfile;
   private int bitDepth;
   private boolean isTransparent;
   private int grayTransparentAlpha;
   private int redTransparentAlpha;
   private int greenTransparentAlpha;
   private int blueTransparentAlpha;
   private int renderingIntent = -1;

   public ImageRawPNG(ImageInfo info, InputStream in, ColorModel colorModel, int bitDepth, ICC_Profile iccProfile) {
      super(info, ImageFlavor.RAW_PNG, in);
      this.iccProfile = iccProfile;
      this.cm = colorModel;
      this.bitDepth = bitDepth;
   }

   public int getBitDepth() {
      return this.bitDepth;
   }

   public ICC_Profile getICCProfile() {
      return this.iccProfile;
   }

   public ColorModel getColorModel() {
      return this.cm;
   }

   public ColorSpace getColorSpace() {
      return this.cm.getColorSpace();
   }

   protected void setGrayTransparentAlpha(int gray) {
      this.isTransparent = true;
      this.grayTransparentAlpha = gray;
   }

   protected void setRGBTransparentAlpha(int red, int green, int blue) {
      this.isTransparent = true;
      this.redTransparentAlpha = red;
      this.greenTransparentAlpha = green;
      this.blueTransparentAlpha = blue;
   }

   protected void setTransparent() {
      this.isTransparent = true;
   }

   public boolean isTransparent() {
      return this.isTransparent;
   }

   public Color getTransparentColor() {
      Color color = null;
      if (!this.isTransparent) {
         return color;
      } else {
         if (this.cm.getNumColorComponents() == 3) {
            color = new Color(this.redTransparentAlpha, this.greenTransparentAlpha, this.blueTransparentAlpha);
         } else {
            color = new Color(this.grayTransparentAlpha, 0, 0);
         }

         return color;
      }
   }

   public void setRenderingIntent(int intent) {
      this.renderingIntent = intent;
   }

   public int getRenderingIntent() {
      return this.renderingIntent;
   }
}
