package org.apache.fop.render.pdf;

import java.io.OutputStream;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.pdf.PDFPage;
import org.apache.fop.pdf.PDFResourceContext;
import org.apache.fop.render.AbstractGenericSVGHandler;
import org.apache.fop.render.ImageHandlerUtil;
import org.apache.fop.render.Renderer;
import org.apache.fop.render.RendererContext;

public class PDFSVGHandler extends AbstractGenericSVGHandler implements PDFRendererContextConstants {
   private static Log log = LogFactory.getLog(PDFSVGHandler.class);

   public static PDFInfo getPDFInfo(RendererContext context) {
      PDFInfo pdfi = new PDFInfo();
      pdfi.pdfDoc = (PDFDocument)context.getProperty("pdfDoc");
      pdfi.outputStream = (OutputStream)context.getProperty("outputStream");
      pdfi.pdfPage = (PDFPage)context.getProperty("pdfPage");
      pdfi.pdfContext = (PDFResourceContext)context.getProperty("pdfContext");
      pdfi.width = (Integer)context.getProperty("width");
      pdfi.height = (Integer)context.getProperty("height");
      pdfi.fi = (FontInfo)context.getProperty("fontInfo");
      pdfi.currentFontName = (String)context.getProperty("fontName");
      pdfi.currentFontSize = (Integer)context.getProperty("fontSize");
      pdfi.currentXPosition = (Integer)context.getProperty("xpos");
      pdfi.currentYPosition = (Integer)context.getProperty("ypos");
      pdfi.cfg = (Configuration)context.getProperty("cfg");
      Map foreign = (Map)context.getProperty("foreign-attributes");
      pdfi.paintAsBitmap = ImageHandlerUtil.isConversionModeBitmap(foreign);
      return pdfi;
   }

   public boolean supportsRenderer(Renderer renderer) {
      return false;
   }

   public static class PDFInfo {
      public PDFDocument pdfDoc;
      public OutputStream outputStream;
      public PDFPage pdfPage;
      public PDFResourceContext pdfContext;
      public int width;
      public int height;
      public FontInfo fi;
      public String currentFontName;
      public int currentFontSize;
      public int currentXPosition;
      public int currentYPosition;
      public Configuration cfg;
      public boolean paintAsBitmap;
   }
}
