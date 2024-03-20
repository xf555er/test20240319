package org.apache.fop.render.ps;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
import java.io.IOException;
import org.apache.fop.render.RenderingContext;
import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.impl.ImageRawPNG;
import org.apache.xmlgraphics.ps.FormGenerator;
import org.apache.xmlgraphics.ps.ImageEncoder;
import org.apache.xmlgraphics.ps.ImageFormGenerator;
import org.apache.xmlgraphics.ps.PSGenerator;

public class PSImageHandlerRawPNG implements PSImageHandler {
   private static final ImageFlavor[] FLAVORS;

   public void handleImage(RenderingContext context, Image image, Rectangle pos) throws IOException {
      PSRenderingContext psContext = (PSRenderingContext)context;
      PSGenerator gen = psContext.getGenerator();
      ImageRawPNG png = (ImageRawPNG)image;
      float x = (float)pos.getX() / 1000.0F;
      float y = (float)pos.getY() / 1000.0F;
      float w = (float)pos.getWidth() / 1000.0F;
      float h = (float)pos.getHeight() / 1000.0F;
      Rectangle2D targetRect = new Rectangle2D.Float(x, y, w, h);
      ImageEncoder encoder = new ImageEncoderPNG(png);
      ImageInfo info = image.getInfo();
      Dimension imgDim = info.getSize().getDimensionPx();
      String imgDescription = image.getClass().getName();
      ColorModel cm = png.getColorModel();
      org.apache.xmlgraphics.ps.PSImageUtils.writeImage(encoder, imgDim, imgDescription, targetRect, cm, gen);
   }

   public void generateForm(RenderingContext context, Image image, PSImageFormResource form) throws IOException {
      PSRenderingContext psContext = (PSRenderingContext)context;
      PSGenerator gen = psContext.getGenerator();
      ImageRawPNG png = (ImageRawPNG)image;
      ImageInfo info = image.getInfo();
      String imageDescription = info.getMimeType() + " " + info.getOriginalURI();
      ImageEncoder encoder = new ImageEncoderPNG(png);
      FormGenerator formGen = new ImageFormGenerator(form.getName(), imageDescription, info.getSize().getDimensionPt(), info.getSize().getDimensionPx(), encoder, png.getColorSpace(), false);
      formGen.generate(gen);
   }

   public int getPriority() {
      return 200;
   }

   public Class getSupportedImageClass() {
      return ImageRawPNG.class;
   }

   public ImageFlavor[] getSupportedImageFlavors() {
      return FLAVORS;
   }

   public boolean isCompatible(RenderingContext targetContext, Image image) {
      if (targetContext instanceof PSRenderingContext) {
         PSRenderingContext psContext = (PSRenderingContext)targetContext;
         if (psContext.getGenerator().getPSLevel() >= 2) {
            return image == null || image instanceof ImageRawPNG;
         }
      }

      return false;
   }

   static {
      FLAVORS = new ImageFlavor[]{ImageFlavor.RAW_PNG};
   }
}
