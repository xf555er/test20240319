package org.apache.fop.image.loader.batik;

import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.util.Map;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DefaultFontFamilyResolver;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.svg.SimpleSVGUserAgent;
import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageException;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageManager;
import org.apache.xmlgraphics.image.loader.ImageProcessingHints;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;
import org.apache.xmlgraphics.image.loader.XMLNamespaceEnabledImageFlavor;
import org.apache.xmlgraphics.image.loader.impl.AbstractImageConverter;
import org.apache.xmlgraphics.image.loader.impl.ImageGraphics2D;
import org.apache.xmlgraphics.image.loader.impl.ImageXMLDOM;
import org.apache.xmlgraphics.java2d.Graphics2DImagePainter;
import org.w3c.dom.Document;

public class ImageConverterSVG2G2D extends AbstractImageConverter {
   private static Log log = LogFactory.getLog(ImageConverterSVG2G2D.class);

   public Image convert(Image src, Map hints) throws ImageException {
      this.checkSourceFlavor(src);
      ImageXMLDOM svg = (ImageXMLDOM)src;
      if (!"http://www.w3.org/2000/svg".equals(svg.getRootNamespace())) {
         throw new IllegalArgumentException("XML DOM is not in the SVG namespace: " + svg.getRootNamespace());
      } else {
         float pxToMillimeter = 0.35277778F;
         Number ptm = (Number)hints.get(ImageProcessingHints.SOURCE_RESOLUTION);
         if (ptm != null) {
            pxToMillimeter = (float)(25.399999618530273 / ptm.doubleValue());
         }

         UserAgent ua = this.createBatikUserAgent(pxToMillimeter);
         GVTBuilder builder = new GVTBuilder();
         ImageManager imageManager = (ImageManager)hints.get(ImageProcessingHints.IMAGE_MANAGER);
         ImageSessionContext sessionContext = (ImageSessionContext)hints.get(ImageProcessingHints.IMAGE_SESSION_CONTEXT);
         boolean useEnhancedBridgeContext = imageManager != null && sessionContext != null;
         BridgeContext ctx = useEnhancedBridgeContext ? new GenericFOPBridgeContext(ua, (FontInfo)null, imageManager, sessionContext) : new BridgeContext(ua);
         Document doc = svg.getDocument();
         Document clonedDoc = BatikUtil.cloneSVGDocument(doc);

         GraphicsNode root;
         try {
            root = builder.build((BridgeContext)ctx, (Document)clonedDoc);
         } catch (Exception var21) {
            throw new ImageException("GVT tree could not be built for SVG graphic", var21);
         }

         int width = svg.getSize().getWidthMpt();
         int height = svg.getSize().getHeightMpt();
         Dimension imageSize = new Dimension(width, height);
         Graphics2DImagePainter painter = this.createPainter((BridgeContext)ctx, root, imageSize);
         ImageInfo imageInfo = src.getInfo();
         ImageGraphics2D g2dImage = new ImageGraphics2D(imageInfo, painter);
         return g2dImage;
      }
   }

   protected SimpleSVGUserAgent createBatikUserAgent(float pxToMillimeter) {
      return new SimpleSVGUserAgent(pxToMillimeter, new AffineTransform(), DefaultFontFamilyResolver.SINGLETON) {
         public void displayMessage(String message) {
            ImageConverterSVG2G2D.log.info(message);
         }

         public void displayError(Exception e) {
            ImageConverterSVG2G2D.log.error("Error converting SVG to a Java2D graphic", e);
         }

         public void displayError(String message) {
            ImageConverterSVG2G2D.log.error(message);
         }
      };
   }

   protected Graphics2DImagePainter createPainter(BridgeContext ctx, GraphicsNode root, Dimension imageSize) {
      return new Graphics2DImagePainterImpl(root, ctx, imageSize);
   }

   public ImageFlavor getSourceFlavor() {
      return XMLNamespaceEnabledImageFlavor.SVG_DOM;
   }

   public ImageFlavor getTargetFlavor() {
      return ImageFlavor.GRAPHICS2D;
   }
}
