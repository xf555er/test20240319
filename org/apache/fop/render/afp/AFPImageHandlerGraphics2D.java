package org.apache.fop.render.afp;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import org.apache.fop.afp.AFPDataObjectInfo;
import org.apache.fop.afp.AFPGraphics2D;
import org.apache.fop.afp.AFPGraphicsObjectInfo;
import org.apache.fop.afp.AFPPaintingState;
import org.apache.fop.afp.AFPResourceInfo;
import org.apache.fop.afp.AFPResourceManager;
import org.apache.fop.render.ImageHandler;
import org.apache.fop.render.ImageHandlerUtil;
import org.apache.fop.render.RenderingContext;
import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.impl.ImageGraphics2D;
import org.apache.xmlgraphics.java2d.GraphicContext;

public class AFPImageHandlerGraphics2D extends AFPImageHandler implements ImageHandler {
   private static final ImageFlavor[] FLAVORS;

   private void setDefaultResourceLevel(AFPGraphicsObjectInfo graphicsObjectInfo, AFPResourceManager resourceManager) {
      AFPResourceInfo resourceInfo = graphicsObjectInfo.getResourceInfo();
      if (!resourceInfo.levelChanged()) {
         resourceInfo.setLevel(resourceManager.getResourceLevelDefaults().getDefaultResourceLevel((byte)3));
      }

   }

   public int getPriority() {
      return 200;
   }

   public Class getSupportedImageClass() {
      return ImageGraphics2D.class;
   }

   public ImageFlavor[] getSupportedImageFlavors() {
      return FLAVORS;
   }

   protected AFPDataObjectInfo createDataObjectInfo() {
      return new AFPGraphicsObjectInfo();
   }

   public void handleImage(RenderingContext context, Image image, Rectangle pos) throws IOException {
      AFPRenderingContext afpContext = (AFPRenderingContext)context;
      AFPDataObjectInfo info = this.createDataObjectInfo();

      assert info instanceof AFPGraphicsObjectInfo;

      AFPGraphicsObjectInfo graphicsObjectInfo = (AFPGraphicsObjectInfo)info;
      graphicsObjectInfo.setCreatePageSegment(afpContext.getPaintingState().getWrapGocaPSeg());
      graphicsObjectInfo.setResourceInfo(createResourceInformation(image.getInfo().getOriginalURI(), afpContext.getForeignAttributes()));
      graphicsObjectInfo.setObjectAreaInfo(createObjectAreaInfo(afpContext.getPaintingState(), pos));
      this.setDefaultResourceLevel(graphicsObjectInfo, afpContext.getResourceManager());
      AFPPaintingState paintingState = afpContext.getPaintingState();
      paintingState.save();
      AffineTransform placement = new AffineTransform();
      placement.translate((double)pos.x, (double)pos.y);
      paintingState.concatenate(placement);
      ImageGraphics2D imageG2D = (ImageGraphics2D)image;
      boolean textAsShapes = paintingState.isStrokeGOCAText();
      AFPGraphics2D g2d = new AFPGraphics2D(textAsShapes, afpContext.getPaintingState(), afpContext.getResourceManager(), graphicsObjectInfo.getResourceInfo(), textAsShapes ? null : afpContext.getFontInfo());
      g2d.setGraphicContext(new GraphicContext());
      graphicsObjectInfo.setGraphics2D(g2d);
      graphicsObjectInfo.setPainter(imageG2D.getGraphics2DImagePainter());
      afpContext.getResourceManager().createObject(graphicsObjectInfo);
      paintingState.restore();
   }

   public boolean isCompatible(RenderingContext targetContext, Image image) {
      boolean supported = (image == null || image instanceof ImageGraphics2D) && targetContext instanceof AFPRenderingContext;
      if (supported) {
         AFPRenderingContext afpContext = (AFPRenderingContext)targetContext;
         if (!afpContext.getPaintingState().isGOCAEnabled()) {
            return false;
         }

         String mode = (String)targetContext.getHint(ImageHandlerUtil.CONVERSION_MODE);
         if (ImageHandlerUtil.isConversionModeBitmap(mode)) {
            return false;
         }
      }

      return supported;
   }

   static {
      FLAVORS = new ImageFlavor[]{ImageFlavor.GRAPHICS2D};
   }
}
