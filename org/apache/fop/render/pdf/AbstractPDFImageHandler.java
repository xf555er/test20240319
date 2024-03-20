package org.apache.fop.render.pdf;

import java.awt.Rectangle;
import java.io.IOException;
import org.apache.fop.pdf.PDFImage;
import org.apache.fop.pdf.PDFXObject;
import org.apache.fop.render.ImageHandler;
import org.apache.fop.render.RenderingContext;
import org.apache.xmlgraphics.image.loader.Image;

abstract class AbstractPDFImageHandler implements ImageHandler {
   public void handleImage(RenderingContext context, Image image, Rectangle pos) throws IOException {
      assert context instanceof PDFRenderingContext;

      PDFRenderingContext pdfContext = (PDFRenderingContext)context;
      PDFContentGenerator generator = pdfContext.getGenerator();
      PDFImage pdfimage = this.createPDFImage(image, image.getInfo().getOriginalURI());
      PDFXObject xobj = generator.getDocument().addImage(generator.getResourceContext(), pdfimage);
      float x = (float)pos.getX() / 1000.0F;
      float y = (float)pos.getY() / 1000.0F;
      float w = (float)pos.getWidth() / 1000.0F;
      float h = (float)pos.getHeight() / 1000.0F;
      if (context.getUserAgent().isAccessibilityEnabled()) {
         PDFLogicalStructureHandler.MarkedContentInfo mci = pdfContext.getMarkedContentInfo();
         generator.placeImage(x, y, w, h, xobj, mci.tag, mci.mcid);
      } else {
         generator.placeImage(x, y, w, h, xobj);
      }

   }

   abstract PDFImage createPDFImage(Image var1, String var2);
}
