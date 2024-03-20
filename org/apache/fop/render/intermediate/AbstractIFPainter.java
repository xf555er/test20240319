package org.apache.fop.render.intermediate;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.ResourceEventProducer;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.fonts.MultiByteFont;
import org.apache.fop.fonts.truetype.SVGGlyphData;
import org.apache.fop.render.ImageHandler;
import org.apache.fop.render.ImageHandlerRegistry;
import org.apache.fop.render.ImageHandlerUtil;
import org.apache.fop.render.RenderingContext;
import org.apache.fop.traits.BorderProps;
import org.apache.fop.traits.RuleStyle;
import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageException;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageManager;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;
import org.apache.xmlgraphics.image.loader.util.ImageUtil;
import org.w3c.dom.Document;
import org.xml.sax.Locator;

public abstract class AbstractIFPainter implements IFPainter {
   private static Log log = LogFactory.getLog(AbstractIFPainter.class);
   protected static final String INSTREAM_OBJECT_URI = "(instream-object)";
   protected IFState state;
   private final IFDocumentHandler documentHandler;

   public AbstractIFPainter(IFDocumentHandler documentHandler) {
      this.documentHandler = documentHandler;
   }

   protected String getFontKey(FontTriplet triplet) throws IFException {
      String key = this.getFontInfo().getInternalFontKey(triplet);
      if (key == null) {
         throw new IFException("The font triplet is not available: \"" + triplet + "\" for the MIME type: \"" + this.documentHandler.getMimeType() + "\"");
      } else {
         return key;
      }
   }

   public IFContext getContext() {
      return this.documentHandler.getContext();
   }

   protected FontInfo getFontInfo() {
      return this.documentHandler.getFontInfo();
   }

   protected IFDocumentHandler getDocumentHandler() {
      return this.documentHandler;
   }

   protected FOUserAgent getUserAgent() {
      return this.getContext().getUserAgent();
   }

   private AffineTransform combine(AffineTransform[] transforms) {
      AffineTransform at = new AffineTransform();
      AffineTransform[] var3 = transforms;
      int var4 = transforms.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         AffineTransform transform = var3[var5];
         at.concatenate(transform);
      }

      return at;
   }

   public void startViewport(AffineTransform[] transforms, Dimension size, Rectangle clipRect) throws IFException {
      this.startViewport(this.combine(transforms), size, clipRect);
   }

   public void startGroup(AffineTransform[] transforms, String layer) throws IFException {
      this.startGroup(this.combine(transforms), layer);
   }

   protected abstract RenderingContext createRenderingContext();

   protected void drawImageUsingImageHandler(ImageInfo info, Rectangle rect) throws ImageException, IOException {
      ImageManager manager = this.getUserAgent().getImageManager();
      ImageSessionContext sessionContext = this.getUserAgent().getImageSessionContext();
      ImageHandlerRegistry imageHandlerRegistry = this.getUserAgent().getImageHandlerRegistry();
      RenderingContext context = this.createRenderingContext();
      Map hints = this.createDefaultImageProcessingHints(sessionContext);
      context.putHints(hints);
      ImageFlavor[] flavors = imageHandlerRegistry.getSupportedFlavors(context);
      info.getCustomObjects().put("warningincustomobject", true);
      Image img = manager.getImage(info, flavors, hints, sessionContext);
      if (info.getCustomObjects().get("warning") != null) {
         ResourceEventProducer eventProducer = ResourceEventProducer.Provider.get(this.getUserAgent().getEventBroadcaster());
         eventProducer.imageWarning(this, (String)info.getCustomObjects().get("warning"));
      }

      try {
         this.drawImage(img, rect, context);
      } catch (IOException var12) {
         ResourceEventProducer eventProducer = ResourceEventProducer.Provider.get(this.getUserAgent().getEventBroadcaster());
         eventProducer.imageWritingError(this, var12);
      }

   }

   protected Map createDefaultImageProcessingHints(ImageSessionContext sessionContext) {
      Map hints = ImageUtil.getDefaultHints(sessionContext);
      Object conversionMode = this.getContext().getForeignAttribute(ImageHandlerUtil.CONVERSION_MODE);
      if (conversionMode != null) {
         hints.put(ImageHandlerUtil.CONVERSION_MODE, conversionMode);
      }

      hints.put("page-number", this.documentHandler.getContext().getPageNumber());
      return hints;
   }

   protected void drawImage(Image image, Rectangle rect, RenderingContext context) throws IOException, ImageException {
      this.drawImage(image, rect, context, false, (Map)null);
   }

   protected void drawImage(Image image, Rectangle rect, RenderingContext context, boolean convert, Map additionalHints) throws IOException, ImageException {
      ImageManager manager = this.getUserAgent().getImageManager();
      ImageHandlerRegistry imageHandlerRegistry = this.getUserAgent().getImageHandlerRegistry();
      context.putHints(additionalHints);
      Image effImage;
      if (convert) {
         Map hints = this.createDefaultImageProcessingHints(this.getUserAgent().getImageSessionContext());
         if (additionalHints != null) {
            hints.putAll(additionalHints);
         }

         effImage = manager.convertImage(image, imageHandlerRegistry.getSupportedFlavors(context), hints);
      } else {
         effImage = image;
      }

      ImageHandler handler = imageHandlerRegistry.getHandler(context, effImage);
      if (handler == null) {
         throw new UnsupportedOperationException("No ImageHandler available for image: " + effImage.getInfo() + " (" + effImage.getClass().getName() + ")");
      } else {
         if (log.isTraceEnabled()) {
            log.trace("Using ImageHandler: " + handler.getClass().getName());
         }

         context.putHint("fontinfo", this.getFontInfo());
         handler.handleImage(context, effImage, rect);
      }
   }

   protected ImageInfo getImageInfo(String uri) {
      ImageManager manager = this.getUserAgent().getImageManager();

      ResourceEventProducer eventProducer;
      try {
         ImageSessionContext sessionContext = this.getUserAgent().getImageSessionContext();
         return manager.getImageInfo(uri, sessionContext);
      } catch (ImageException var5) {
         eventProducer = ResourceEventProducer.Provider.get(this.getUserAgent().getEventBroadcaster());
         eventProducer.imageError(this, uri, var5, (Locator)null);
      } catch (FileNotFoundException var6) {
         eventProducer = ResourceEventProducer.Provider.get(this.getUserAgent().getEventBroadcaster());
         eventProducer.imageNotFound(this, uri, var6, (Locator)null);
      } catch (IOException var7) {
         eventProducer = ResourceEventProducer.Provider.get(this.getUserAgent().getEventBroadcaster());
         eventProducer.imageIOError(this, uri, var7, (Locator)null);
      }

      return null;
   }

   protected void drawImageUsingURI(String uri, Rectangle rect) {
      ImageManager manager = this.getUserAgent().getImageManager();
      ImageInfo info = null;

      ResourceEventProducer eventProducer;
      try {
         ImageSessionContext sessionContext = this.getUserAgent().getImageSessionContext();
         info = manager.getImageInfo(uri, sessionContext);
         this.drawImageUsingImageHandler(info, rect);
      } catch (ImageException var7) {
         eventProducer = ResourceEventProducer.Provider.get(this.getUserAgent().getEventBroadcaster());
         eventProducer.imageError(this, info != null ? info.toString() : uri, var7, (Locator)null);
      } catch (FileNotFoundException var8) {
         eventProducer = ResourceEventProducer.Provider.get(this.getUserAgent().getEventBroadcaster());
         eventProducer.imageNotFound(this, info != null ? info.toString() : uri, var8, (Locator)null);
      } catch (IOException var9) {
         eventProducer = ResourceEventProducer.Provider.get(this.getUserAgent().getEventBroadcaster());
         eventProducer.imageIOError(this, info != null ? info.toString() : uri, var9, (Locator)null);
      }

   }

   protected void drawImageUsingDocument(Document doc, Rectangle rect) {
      ImageManager manager = this.getUserAgent().getImageManager();
      ImageInfo info = null;

      ResourceEventProducer eventProducer;
      try {
         info = manager.preloadImage((String)null, (Source)(new DOMSource(doc)));
         this.drawImageUsingImageHandler(info, rect);
      } catch (ImageException var7) {
         eventProducer = ResourceEventProducer.Provider.get(this.getUserAgent().getEventBroadcaster());
         eventProducer.imageError(this, info != null ? info.toString() : "(instream-object)", var7, (Locator)null);
      } catch (FileNotFoundException var8) {
         eventProducer = ResourceEventProducer.Provider.get(this.getUserAgent().getEventBroadcaster());
         eventProducer.imageNotFound(this, info != null ? info.toString() : "(instream-object)", var8, (Locator)null);
      } catch (IOException var9) {
         eventProducer = ResourceEventProducer.Provider.get(this.getUserAgent().getEventBroadcaster());
         eventProducer.imageIOError(this, info != null ? info.toString() : "(instream-object)", var9, (Locator)null);
      }

   }

   public void drawBorderRect(Rectangle rect, BorderProps top, BorderProps bottom, BorderProps left, BorderProps right, Color innerBackgroundColor) throws IFException {
      Rectangle b;
      if (top != null) {
         b = new Rectangle(rect.x, rect.y, rect.width, top.width);
         this.fillRect(b, top.color);
      }

      if (right != null) {
         b = new Rectangle(rect.x + rect.width - right.width, rect.y, right.width, rect.height);
         this.fillRect(b, right.color);
      }

      if (bottom != null) {
         b = new Rectangle(rect.x, rect.y + rect.height - bottom.width, rect.width, bottom.width);
         this.fillRect(b, bottom.color);
      }

      if (left != null) {
         b = new Rectangle(rect.x, rect.y, left.width, rect.height);
         this.fillRect(b, left.color);
      }

   }

   protected boolean hasOnlySolidBorders(BorderProps top, BorderProps bottom, BorderProps left, BorderProps right) {
      if (top != null && top.style != 133) {
         return false;
      } else if (bottom != null && bottom.style != 133) {
         return false;
      } else if (left != null && left.style != 133) {
         return false;
      } else {
         return right == null || right.style == 133;
      }
   }

   public void drawLine(Point start, Point end, int width, Color color, RuleStyle style) throws IFException {
      Rectangle rect = this.getLineBoundingBox(start, end, width);
      this.fillRect(rect, color);
   }

   protected Rectangle getLineBoundingBox(Point start, Point end, int width) {
      int leftx;
      if (start.y == end.y) {
         leftx = start.y - width / 2;
         return new Rectangle(start.x, leftx, end.x - start.x, width);
      } else if (start.x == end.y) {
         leftx = start.x - width / 2;
         return new Rectangle(leftx, start.x, width, end.y - start.y);
      } else {
         throw new IllegalArgumentException("Only horizontal or vertical lines are supported at the moment.");
      }
   }

   public void setFont(String family, String style, Integer weight, String variant, Integer size, Color color) throws IFException {
      if (family != null) {
         this.state.setFontFamily(family);
      }

      if (style != null) {
         this.state.setFontStyle(style);
      }

      if (weight != null) {
         this.state.setFontWeight(weight);
      }

      if (variant != null) {
         this.state.setFontVariant(variant);
      }

      if (size != null) {
         this.state.setFontSize(size);
      }

      if (color != null) {
         this.state.setTextColor(color);
      }

   }

   public static AffineTransform toPoints(AffineTransform transform) {
      double[] matrix = new double[6];
      transform.getMatrix(matrix);
      matrix[4] /= 1000.0;
      matrix[5] /= 1000.0;
      return new AffineTransform(matrix);
   }

   public boolean isBackgroundRequired(BorderProps bpsBefore, BorderProps bpsAfter, BorderProps bpsStart, BorderProps bpsEnd) {
      return true;
   }

   public void drawText(int x, int y, int letterSpacing, int wordSpacing, int[][] dp, String text, boolean nextIsSpace) throws IFException {
      this.drawText(x, y, letterSpacing, wordSpacing, dp, text);
   }

   protected void drawSVGText(MultiByteFont multiByteFont, FontTriplet triplet, int x, int y, String text, IFState state) throws IFException {
      int sizeMillipoints = state.getFontSize();
      Font font = this.getFontInfo().getFontInstance(triplet, sizeMillipoints);
      int newx = x;
      char[] var10 = text.toCharArray();
      int var11 = var10.length;

      for(int var12 = 0; var12 < var11; ++var12) {
         char c = var10[var12];
         SVGGlyphData svg = multiByteFont.getSVG(c);
         if (svg != null) {
            int codePoint = font.mapCodePoint(c);
            String dataURL = svg.getDataURL(multiByteFont.getCapHeight());
            Rectangle boundingBox = multiByteFont.getBoundingBox(codePoint, (int)((float)sizeMillipoints / 1000.0F));
            boundingBox.y = y - boundingBox.height - boundingBox.y;
            boundingBox.x = newx;
            boundingBox.width = (int)((float)sizeMillipoints * svg.scale);
            boundingBox.height = (int)((float)sizeMillipoints * svg.scale);
            this.drawImage(dataURL, boundingBox);
         }

         newx += font.getCharWidth(c);
      }

   }
}
