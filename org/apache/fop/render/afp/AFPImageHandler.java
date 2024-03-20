package org.apache.fop.render.afp;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.Map;
import org.apache.fop.afp.AFPDataObjectInfo;
import org.apache.fop.afp.AFPObjectAreaInfo;
import org.apache.fop.afp.AFPPaintingState;
import org.apache.fop.afp.AFPResourceInfo;
import org.apache.fop.afp.AFPUnitConverter;
import org.apache.fop.render.ImageHandlerBase;
import org.apache.fop.render.RendererContext;

public abstract class AFPImageHandler implements ImageHandlerBase {
   private static final int X = 0;
   private static final int Y = 1;
   private static final AFPForeignAttributeReader FOREIGN_ATTRIBUTE_READER = new AFPForeignAttributeReader();

   public AFPDataObjectInfo generateDataObjectInfo(AFPRendererImageInfo rendererImageInfo) throws IOException {
      AFPDataObjectInfo dataObjectInfo = this.createDataObjectInfo();
      dataObjectInfo.setResourceInfo(createResourceInformation(rendererImageInfo.getURI(), rendererImageInfo.getForeignAttributes()));
      Point origin = rendererImageInfo.getOrigin();
      Rectangle2D position = rendererImageInfo.getPosition();
      int srcX = Math.round((float)origin.x + (float)position.getX());
      int srcY = Math.round((float)origin.y + (float)position.getY());
      Rectangle targetRect = new Rectangle(srcX, srcY, (int)Math.round(position.getWidth()), (int)Math.round(position.getHeight()));
      RendererContext context = rendererImageInfo.getRendererContext();

      assert context instanceof AFPRendererContext;

      AFPRendererContext rendererContext = (AFPRendererContext)context;
      AFPInfo afpInfo = rendererContext.getInfo();
      AFPPaintingState paintingState = afpInfo.getPaintingState();
      dataObjectInfo.setObjectAreaInfo(createObjectAreaInfo(paintingState, targetRect));
      return dataObjectInfo;
   }

   public static AFPResourceInfo createResourceInformation(String uri, Map foreignAttributes) {
      AFPResourceInfo resourceInfo = FOREIGN_ATTRIBUTE_READER.getResourceInfo(foreignAttributes);
      resourceInfo.setUri(uri);
      return resourceInfo;
   }

   public static AFPObjectAreaInfo createObjectAreaInfo(AFPPaintingState paintingState, Rectangle targetRect) {
      AFPUnitConverter unitConv = paintingState.getUnitConverter();
      int[] coords = unitConv.mpts2units(new float[]{(float)targetRect.x, (float)targetRect.y});
      int width = (int)Math.ceil((double)unitConv.mpt2units((float)targetRect.width));
      int height = (int)Math.ceil((double)unitConv.mpt2units((float)targetRect.height));
      int resolution = paintingState.getResolution();
      AFPObjectAreaInfo objectAreaInfo = new AFPObjectAreaInfo(coords[0], coords[1], width, height, resolution, paintingState.getRotation());
      return objectAreaInfo;
   }

   protected abstract AFPDataObjectInfo createDataObjectInfo();
}
