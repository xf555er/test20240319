package org.apache.fop.render.afp;

import java.net.URI;
import org.apache.fop.afp.AFPResourceLevelDefaults;

public interface AFPCustomizable {
   void setBitsPerPixel(int var1);

   void setColorImages(boolean var1);

   void setNativeImagesSupported(boolean var1);

   void setCMYKImagesSupported(boolean var1);

   void setShadingMode(AFPShadingMode var1);

   void setDitheringQuality(float var1);

   void setBitmapEncodingQuality(float var1);

   void setResolution(int var1);

   void setLineWidthCorrection(float var1);

   void setWrapPSeg(boolean var1);

   void setFS45(boolean var1);

   boolean getWrapPSeg();

   boolean getFS45();

   int getResolution();

   void setGOCAEnabled(boolean var1);

   boolean isGOCAEnabled();

   void setStrokeGOCAText(boolean var1);

   boolean isStrokeGOCAText();

   void setDefaultResourceGroupUri(URI var1);

   void setResourceLevelDefaults(AFPResourceLevelDefaults var1);

   void canEmbedJpeg(boolean var1);
}
