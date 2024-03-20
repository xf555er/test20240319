package org.apache.fop.render.ps;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.imageio.ImageIO;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.fop.image.loader.batik.BatikImageFlavors;
import org.apache.fop.image.loader.batik.BatikUtil;
import org.apache.fop.image.loader.batik.ImageConverterSVG2G2D;
import org.apache.fop.render.ImageHandler;
import org.apache.fop.render.RenderingContext;
import org.apache.fop.render.ps.svg.PSSVGGraphics2D;
import org.apache.fop.svg.SVGEventProducer;
import org.apache.fop.svg.SVGUserAgent;
import org.apache.fop.svg.font.FOPFontFamilyResolverImpl;
import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageException;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.impl.ImageGraphics2D;
import org.apache.xmlgraphics.image.loader.impl.ImageXMLDOM;
import org.apache.xmlgraphics.java2d.GraphicContext;
import org.apache.xmlgraphics.ps.ImageEncoder;
import org.apache.xmlgraphics.ps.ImageEncodingHelper;
import org.apache.xmlgraphics.ps.PSGenerator;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PSImageHandlerSVG implements ImageHandler {
   private static final Color FALLBACK_COLOR = new Color(255, 33, 117);
   private HashMap gradientsFound = new HashMap();
   private static final ImageFlavor[] FLAVORS;

   public void handleImage(RenderingContext context, Image image, Rectangle pos) throws IOException {
      PSRenderingContext psContext = (PSRenderingContext)context;
      PSGenerator gen = psContext.getGenerator();
      ImageXMLDOM imageSVG = (ImageXMLDOM)image;
      if (this.shouldRaster(imageSVG)) {
         InputStream is = this.renderSVGToInputStream(imageSVG, pos);
         float x = (float)pos.getX() / 1000.0F;
         float y = (float)pos.getY() / 1000.0F;
         float w = (float)pos.getWidth() / 1000.0F;
         float h = (float)pos.getHeight() / 1000.0F;
         Rectangle2D targetRect = new Rectangle2D.Double((double)x, (double)y, (double)w, (double)h);
         MaskedImage mi = this.convertToRGB(ImageIO.read(is));
         BufferedImage ri = mi.getImage();
         ImageEncoder encoder = ImageEncodingHelper.createRenderedImageEncoder(ri);
         Dimension imgDim = new Dimension(ri.getWidth(), ri.getHeight());
         String imgDescription = ri.getClass().getName();
         ImageEncodingHelper helper = new ImageEncodingHelper(ri);
         ColorModel cm = helper.getEncodedColorModel();
         PSImageUtils.writeImage(encoder, imgDim, imgDescription, targetRect, cm, gen, ri, mi.getMaskColor());
      } else {
         boolean strokeText = shouldStrokeText(imageSVG.getDocument().getChildNodes());
         SVGUserAgent ua = new SVGUserAgent(context.getUserAgent(), new FOPFontFamilyResolverImpl(psContext.getFontInfo()), new AffineTransform());
         PSSVGGraphics2D graphics = new PSSVGGraphics2D(strokeText, gen);
         graphics.setGraphicContext(new GraphicContext());
         BridgeContext ctx = new PSBridgeContext(ua, strokeText ? null : psContext.getFontInfo(), context.getUserAgent().getImageManager(), context.getUserAgent().getImageSessionContext());
         Document clonedDoc = BatikUtil.cloneSVGDocument(imageSVG.getDocument());

         GraphicsNode root;
         try {
            GVTBuilder builder = new GVTBuilder();
            root = builder.build(ctx, (Document)clonedDoc);
         } catch (Exception var22) {
            SVGEventProducer eventProducer = SVGEventProducer.Provider.get(context.getUserAgent().getEventBroadcaster());
            eventProducer.svgNotBuilt(this, var22, image.getInfo().getOriginalURI());
            return;
         }

         float w = (float)ctx.getDocumentSize().getWidth() * 1000.0F;
         float h = (float)ctx.getDocumentSize().getHeight() * 1000.0F;
         float sx = (float)pos.width / w;
         float sy = (float)pos.height / h;
         gen.commentln("%FOPBeginSVG");
         gen.saveGraphicsState();
         boolean clip = false;
         gen.concatMatrix((double)sx, 0.0, 0.0, (double)sy, pos.getMinX() / 1000.0, pos.getMinY() / 1000.0);
         AffineTransform transform = new AffineTransform();
         transform.translate(pos.getMinX(), pos.getMinY());
         gen.getCurrentState().concatMatrix(transform);

         try {
            root.paint(graphics);
         } catch (Exception var21) {
            SVGEventProducer eventProducer = SVGEventProducer.Provider.get(context.getUserAgent().getEventBroadcaster());
            eventProducer.svgRenderingError(this, var21, image.getInfo().getOriginalURI());
         }

         gen.restoreGraphicsState();
         gen.commentln("%FOPEndSVG");
      }

   }

   private InputStream renderSVGToInputStream(ImageXMLDOM imageSVG, Rectangle destinationRect) throws IOException {
      Float widthSVG = this.getDimension(imageSVG.getDocument(), "width");
      Float heightSVG = this.getDimension(imageSVG.getDocument(), "height");
      Rectangle rectangle;
      int width;
      int height;
      if (widthSVG != null && heightSVG != null) {
         width = (int)(widthSVG * 8.0F);
         height = (int)(heightSVG * 8.0F);
         rectangle = new Rectangle(0, 0, width, height);
      } else {
         int scale = 10;
         width = destinationRect.width / scale;
         height = destinationRect.height / scale;
         rectangle = new Rectangle(0, 0, destinationRect.width / 100, destinationRect.height / 100);
         destinationRect.width *= scale;
         destinationRect.height *= scale;
      }

      BufferedImage image = new BufferedImage(width, height, 2);
      Graphics2D graphics2D = image.createGraphics();

      try {
         ImageGraphics2D img = (ImageGraphics2D)(new ImageConverterSVG2G2D()).convert(imageSVG, new HashMap());
         img.getGraphics2DImagePainter().paint(graphics2D, rectangle);
      } catch (ImageException var11) {
         throw new IOException(var11);
      }

      ByteArrayOutputStream os = new ByteArrayOutputStream();
      ImageIO.write(image, "png", os);
      return new ByteArrayInputStream(os.toByteArray());
   }

   private MaskedImage convertToRGB(BufferedImage alphaImage) {
      int[] red = new int[256];
      int[] green = new int[256];
      int[] blue = new int[256];
      BufferedImage rgbImage = new BufferedImage(alphaImage.getWidth(), alphaImage.getHeight(), 1);

      int cx;
      int cy;
      for(int cx = 0; cx < alphaImage.getWidth(); ++cx) {
         for(cx = 0; cx < alphaImage.getHeight(); ++cx) {
            cy = alphaImage.getRGB(cx, cx);
            Color pixelColor = new Color(cy);
            ++red[pixelColor.getRed()];
            ++green[pixelColor.getGreen()];
            ++blue[pixelColor.getBlue()];
         }
      }

      Color alphaSwap = null;

      for(cx = 0; cx < 256; ++cx) {
         if (red[cx] == 0) {
            alphaSwap = new Color(cx, 0, 0);
            break;
         }

         if (green[cx] == 0) {
            alphaSwap = new Color(0, cx, 0);
            break;
         }

         if (blue[cx] == 0) {
            alphaSwap = new Color(0, 0, cx);
            break;
         }
      }

      if (alphaSwap == null) {
         alphaSwap = FALLBACK_COLOR;
      }

      for(cx = 0; cx < alphaImage.getWidth(); ++cx) {
         for(cy = 0; cy < alphaImage.getHeight(); ++cy) {
            int pixelValue = alphaImage.getRGB(cx, cy);
            if (pixelValue == 0) {
               rgbImage.setRGB(cx, cy, alphaSwap.getRGB());
            } else {
               rgbImage.setRGB(cx, cy, alphaImage.getRGB(cx, cy));
            }
         }
      }

      return new MaskedImage(rgbImage, alphaSwap);
   }

   private Float getDimension(Document document, String dimension) {
      if (document.getFirstChild().getAttributes().getNamedItem(dimension) != null) {
         String width = document.getFirstChild().getAttributes().getNamedItem(dimension).getNodeValue();
         width = width.replaceAll("[^\\d.]", "");
         return Float.parseFloat(width);
      } else {
         return null;
      }
   }

   private boolean shouldRaster(ImageXMLDOM image) {
      boolean var3;
      try {
         List gradMatches = new ArrayList();
         gradMatches.add("radialGradient");
         gradMatches.add("linearGradient");
         var3 = this.recurseSVGElements(image.getDocument().getChildNodes(), gradMatches, false);
      } finally {
         this.gradientsFound.clear();
      }

      return var3;
   }

   private boolean recurseSVGElements(NodeList childNodes, List gradMatches, boolean isMatched) {
      boolean opacityFound = false;

      for(int i = 0; i < childNodes.getLength(); ++i) {
         Node curNode = childNodes.item(i);
         String opacityValue;
         if (isMatched && curNode.getLocalName() != null && curNode.getLocalName().equals("stop")) {
            if (curNode.getAttributes().getNamedItem("style") != null) {
               String[] stylePairs = curNode.getAttributes().getNamedItem("style").getNodeValue().split(";");
               String[] var8 = stylePairs;
               int var9 = stylePairs.length;

               for(int var10 = 0; var10 < var9; ++var10) {
                  String stylePair = var8[var10];
                  String[] style = stylePair.split(":");
                  if (style[0].equalsIgnoreCase("stop-opacity") && Double.parseDouble(style[1]) < 1.0) {
                     return true;
                  }
               }
            }

            if (curNode.getAttributes().getNamedItem("stop-opacity") != null) {
               opacityValue = curNode.getAttributes().getNamedItem("stop-opacity").getNodeValue();
               if (Double.parseDouble(opacityValue) < 1.0) {
                  return true;
               }
            }
         }

         opacityValue = curNode.getLocalName();
         boolean inMatch = false;
         if (isMatched) {
            inMatch = true;
         } else {
            inMatch = opacityValue != null && gradMatches.contains(opacityValue);
            if (inMatch) {
               this.gradientsFound.put(curNode.getAttributes().getNamedItem("id").getNodeValue(), opacityValue);
            }
         }

         opacityFound = this.recurseSVGElements(curNode.getChildNodes(), gradMatches, inMatch);
         if (opacityFound) {
            return true;
         }
      }

      return opacityFound;
   }

   public static boolean shouldStrokeText(NodeList childNodes) {
      for(int i = 0; i < childNodes.getLength(); ++i) {
         Node curNode = childNodes.item(i);
         if (shouldStrokeText(curNode.getChildNodes())) {
            return true;
         }

         if ("text".equals(curNode.getLocalName())) {
            return curNode.getAttributes().getNamedItem("filter") != null;
         }
      }

      return false;
   }

   public int getPriority() {
      return 400;
   }

   public Class getSupportedImageClass() {
      return ImageXMLDOM.class;
   }

   public ImageFlavor[] getSupportedImageFlavors() {
      return FLAVORS;
   }

   public boolean isCompatible(RenderingContext targetContext, Image image) {
      if (!(targetContext instanceof PSRenderingContext)) {
         return false;
      } else {
         PSRenderingContext psContext = (PSRenderingContext)targetContext;
         return !psContext.isCreateForms() && (image == null || image instanceof ImageXMLDOM && image.getFlavor().isCompatible(BatikImageFlavors.SVG_DOM));
      }
   }

   static {
      FLAVORS = new ImageFlavor[]{BatikImageFlavors.SVG_DOM};
   }

   private static class MaskedImage {
      private Color maskColor = new Color(0, 0, 0);
      private BufferedImage image;

      public MaskedImage(BufferedImage image, Color maskColor) {
         this.image = image;
         this.maskColor = maskColor;
      }

      public Color getMaskColor() {
         return this.maskColor;
      }

      public BufferedImage getImage() {
         return this.image;
      }
   }
}
