package org.apache.fop.render.bitmap;

import org.apache.fop.render.java2d.Java2DRenderingSettings;
import org.apache.xmlgraphics.image.writer.ImageWriterParams;

public class BitmapRenderingSettings extends Java2DRenderingSettings {
   private ImageWriterParams writerParams;
   private int bufferedImageType;
   private boolean antialiasing;
   private boolean qualityRendering;

   public BitmapRenderingSettings() {
      this.bufferedImageType = (Integer)BitmapRendererOption.COLOR_MODE.getDefaultValue();
      this.antialiasing = (Boolean)BitmapRendererOption.ANTI_ALIASING.getDefaultValue();
      this.qualityRendering = (Boolean)BitmapRendererOption.RENDERING_QUALITY.getDefaultValue();
      this.writerParams = new ImageWriterParams();
   }

   public ImageWriterParams getWriterParams() {
      return this.writerParams;
   }

   public int getBufferedImageType() {
      return this.bufferedImageType;
   }

   public void setBufferedImageType(int bufferedImageType) {
      this.bufferedImageType = bufferedImageType;
   }

   public void setAntiAliasing(boolean value) {
      this.antialiasing = value;
   }

   public boolean isAntiAliasingEnabled() {
      return this.antialiasing;
   }

   public void setQualityRendering(boolean quality) {
      this.qualityRendering = quality;
   }

   public boolean isQualityRenderingEnabled() {
      return this.qualityRendering;
   }

   public void setCompressionMethod(String compressionMethod) {
      this.writerParams.setCompressionMethod(compressionMethod);
   }

   public String getCompressionMethod() {
      return this.writerParams.getCompressionMethod();
   }

   public void setResolution(int dpi) {
      this.writerParams.setResolution(dpi);
   }
}
