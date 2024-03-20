package org.apache.fop.afp;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.afp.modca.GraphicsObject;
import org.apache.fop.afp.util.CubicBezierApproximator;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.render.afp.AFPImageHandlerRenderedImage;
import org.apache.fop.render.afp.AFPRenderingContext;
import org.apache.fop.svg.NativeImageHandler;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageSize;
import org.apache.xmlgraphics.image.loader.impl.ImageRendered;
import org.apache.xmlgraphics.java2d.AbstractGraphics2D;
import org.apache.xmlgraphics.java2d.GraphicContext;
import org.apache.xmlgraphics.java2d.GraphicsConfigurationWithTransparency;
import org.apache.xmlgraphics.java2d.StrokingTextHandler;
import org.apache.xmlgraphics.java2d.TextHandler;

public class AFPGraphics2D extends AbstractGraphics2D implements NativeImageHandler {
   private static final Log LOG = LogFactory.getLog(AFPGraphics2D.class);
   private static final int X = 0;
   private static final int Y = 1;
   private static final int X1 = 0;
   private static final int Y1 = 1;
   private static final int X2 = 2;
   private static final int Y2 = 3;
   private static final int X3 = 4;
   private static final int Y3 = 5;
   private GraphicsObject graphicsObj;
   protected TextHandler fallbackTextHandler = new StrokingTextHandler();
   protected TextHandler customTextHandler;
   private AFPResourceManager resourceManager;
   private AFPResourceInfo resourceInfo;
   private AFPPaintingState paintingState;
   private final GraphicsConfigurationWithTransparency graphicsConfig = new GraphicsConfigurationWithTransparency();
   private FontInfo fontInfo;

   public AFPGraphics2D(boolean textAsShapes, AFPPaintingState paintingState, AFPResourceManager resourceManager, AFPResourceInfo resourceInfo, FontInfo fontInfo) {
      super(textAsShapes);
      this.setPaintingState(paintingState);
      this.setResourceManager(resourceManager);
      this.setResourceInfo(resourceInfo);
      this.setFontInfo(fontInfo);
   }

   public AFPGraphics2D(AFPGraphics2D g2d) {
      super(g2d);
      this.paintingState = g2d.paintingState;
      this.resourceManager = g2d.resourceManager;
      this.resourceInfo = g2d.resourceInfo;
      this.fontInfo = g2d.fontInfo;
      this.graphicsObj = g2d.graphicsObj;
      this.fallbackTextHandler = g2d.fallbackTextHandler;
      this.customTextHandler = g2d.customTextHandler;
   }

   private void setResourceManager(AFPResourceManager resourceManager) {
      this.resourceManager = resourceManager;
   }

   public AFPResourceManager getResourceManager() {
      return this.resourceManager;
   }

   private void setResourceInfo(AFPResourceInfo resourceInfo) {
      this.resourceInfo = resourceInfo;
   }

   public GraphicsObject getGraphicsObject() {
      return this.graphicsObj;
   }

   public void setGraphicsObject(GraphicsObject obj) {
      this.graphicsObj = obj;
   }

   private void setPaintingState(AFPPaintingState paintingState) {
      this.paintingState = paintingState;
   }

   public AFPPaintingState getPaintingState() {
      return this.paintingState;
   }

   private void setFontInfo(FontInfo fontInfo) {
      this.fontInfo = fontInfo;
   }

   public FontInfo getFontInfo() {
      return this.fontInfo;
   }

   public void setGraphicContext(GraphicContext gc) {
      this.gc = gc;
   }

   private int getResolution() {
      return this.paintingState.getResolution();
   }

   public double convertToAbsoluteLength(double length) {
      AffineTransform current = this.getTransform();
      double mult = (double)this.getResolution() / 72.0;
      double factor = -current.getScaleY() / mult;
      return length * factor;
   }

   protected void applyStroke(Stroke stroke) {
      if (stroke instanceof BasicStroke) {
         BasicStroke basicStroke = (BasicStroke)stroke;
         float lineWidth = basicStroke.getLineWidth();
         float correction = this.paintingState.getLineWidthCorrection();
         this.graphicsObj.setLineWidth(lineWidth * correction);
         float[] dashArray = basicStroke.getDashArray();
         if (this.paintingState.setDashArray(dashArray)) {
            byte type = 0;
            if (dashArray != null) {
               type = 1;
               if (dashArray.length == 2) {
                  if (dashArray[0] < dashArray[1]) {
                     type = 2;
                  } else if (dashArray[0] > dashArray[1]) {
                     type = 5;
                  }
               } else if (dashArray.length == 4) {
                  if (dashArray[0] > dashArray[1] && dashArray[2] < dashArray[3]) {
                     type = 3;
                  } else if (dashArray[0] < dashArray[1] && dashArray[2] < dashArray[3]) {
                     type = 4;
                  }
               } else if (dashArray.length == 6 && dashArray[0] > dashArray[1] && dashArray[2] < dashArray[3] && dashArray[4] < dashArray[5]) {
                  type = 6;
               }
            }

            this.graphicsObj.setLineType(type);
         }
      } else {
         LOG.warn("Unsupported Stroke: " + stroke.getClass().getName());
      }

   }

   private boolean applyPaint(Paint paint, boolean fill) {
      if (paint instanceof Color) {
         return true;
      } else {
         LOG.debug("NYI: applyPaint() " + paint + " fill=" + fill);
         return false;
      }
   }

   private void doDrawing(Shape shape, boolean fill) {
      if (!fill) {
         this.graphicsObj.newSegment();
      }

      this.graphicsObj.setColor(this.gc.getColor());
      this.applyPaint(this.gc.getPaint(), fill);
      if (fill) {
         this.graphicsObj.beginArea();
      } else {
         this.applyStroke(this.gc.getStroke());
      }

      AffineTransform trans = this.gc.getTransform();
      PathIterator iter = shape.getPathIterator(trans);
      double[] dstPts;
      int[] coords;
      if (shape instanceof Line2D) {
         dstPts = new double[6];
         iter.currentSegment(dstPts);
         coords = new int[]{(int)Math.round(dstPts[0]), (int)Math.round(dstPts[1]), 0, 0};
         iter.next();
         iter.currentSegment(dstPts);
         coords[2] = (int)Math.round(dstPts[0]);
         coords[3] = (int)Math.round(dstPts[1]);
         this.graphicsObj.addLine(coords);
      } else if (shape instanceof Rectangle2D) {
         dstPts = new double[6];
         iter.currentSegment(dstPts);
         coords = new int[]{0, 0, (int)Math.round(dstPts[0]), (int)Math.round(dstPts[1])};
         iter.next();
         iter.next();
         iter.currentSegment(dstPts);
         coords[0] = (int)Math.round(dstPts[0]);
         coords[1] = (int)Math.round(dstPts[1]);
         this.graphicsObj.addBox(coords);
      } else if (shape instanceof Ellipse2D) {
         dstPts = new double[6];
         Ellipse2D elip = (Ellipse2D)shape;
         double scale = trans.getScaleX();
         double radiusWidth = elip.getWidth() / 2.0;
         double radiusHeight = elip.getHeight() / 2.0;
         this.graphicsObj.setArcParams((int)Math.round(radiusWidth * scale), (int)Math.round(radiusHeight * scale), 0, 0);
         double[] srcPts = new double[]{elip.getCenterX(), elip.getCenterY()};
         trans.transform(srcPts, 0, dstPts, 0, 1);
         int mh = true;
         int mhr = false;
         this.graphicsObj.addFullArc((int)Math.round(dstPts[0]), (int)Math.round(dstPts[1]), 1, 0);
      } else {
         this.processPathIterator(iter);
      }

      if (fill) {
         this.graphicsObj.endArea();
      }

   }

   private void processPathIterator(PathIterator iter) {
      double[] dstPts = new double[6];
      double[] currentPosition = new double[2];
      List fillets = new ArrayList();

      label36:
      for(int[] openingCoords = new int[2]; !iter.isDone(); iter.next()) {
         switch (iter.currentSegment(dstPts)) {
            case 0:
               this.flush(fillets);
               openingCoords = new int[]{(int)Math.round(dstPts[0]), (int)Math.round(dstPts[1])};
               currentPosition = new double[]{dstPts[0], dstPts[1]};
               this.graphicsObj.setCurrentPosition(openingCoords);
               break;
            case 1:
               this.flush(fillets);
               this.graphicsObj.addLine(new int[]{(int)Math.round(dstPts[0]), (int)Math.round(dstPts[1])}, true);
               currentPosition = new double[]{dstPts[0], dstPts[1]};
               break;
            case 2:
               this.flush(fillets);
               this.graphicsObj.addFillet(new int[]{(int)Math.round(dstPts[0]), (int)Math.round(dstPts[1]), (int)Math.round(dstPts[2]), (int)Math.round(dstPts[3])}, true);
               currentPosition = new double[]{dstPts[2], dstPts[3]};
               break;
            case 3:
               double[] cubicCoords = new double[]{currentPosition[0], currentPosition[1], dstPts[0], dstPts[1], dstPts[2], dstPts[3], dstPts[4], dstPts[5]};
               double[][] quadParts = CubicBezierApproximator.fixedMidPointApproximation(cubicCoords);
               if (quadParts.length < 4) {
                  break;
               }

               double[][] var8 = quadParts;
               int var9 = quadParts.length;
               int var10 = 0;

               while(true) {
                  if (var10 >= var9) {
                     continue label36;
                  }

                  double[] quadPts = var8[var10];
                  if (quadPts != null && quadPts.length == 4) {
                     fillets.add((int)Math.round(quadPts[0]));
                     fillets.add((int)Math.round(quadPts[1]));
                     fillets.add((int)Math.round(quadPts[2]));
                     fillets.add((int)Math.round(quadPts[3]));
                     currentPosition = new double[]{quadPts[2], quadPts[3]};
                  }

                  ++var10;
               }
            case 4:
               this.flush(fillets);
               this.graphicsObj.addLine(openingCoords, true);
               currentPosition = new double[]{(double)openingCoords[0], (double)openingCoords[1]};
               break;
            default:
               LOG.debug("Unrecognised path iterator type");
         }
      }

      this.flush(fillets);
   }

   private void flush(List fillets) {
      List intList = this.listToIntLists(fillets);
      Iterator var3 = intList.iterator();

      while(var3.hasNext()) {
         int[] ints = (int[])var3.next();
         this.graphicsObj.addFillet(ints, true);
      }

   }

   private List listToIntLists(List input) {
      List out = new ArrayList();

      while(!input.isEmpty()) {
         int[] data = new int[Math.min(100, input.size())];

         for(int i = 0; i < data.length; ++i) {
            data[i] = (Integer)input.remove(0);
         }

         out.add(data);
      }

      return out;
   }

   public void draw(Shape shape) {
      LOG.debug("draw() shape=" + shape);
      this.doDrawing(shape, false);
   }

   public void fill(Shape shape) {
      LOG.debug("fill() shape=" + shape);
      this.doDrawing(shape, true);
   }

   public void handleIOException(IOException ioe) {
      LOG.error(ioe.getMessage());
      ioe.printStackTrace();
   }

   public void drawString(String str, float x, float y) {
      try {
         if (this.customTextHandler != null && !this.textAsShapes) {
            this.customTextHandler.drawString(this, str, x, y);
         } else {
            this.fallbackTextHandler.drawString(this, str, x, y);
         }
      } catch (IOException var5) {
         this.handleIOException(var5);
      }

   }

   public GraphicsConfiguration getDeviceConfiguration() {
      return this.graphicsConfig;
   }

   public Graphics create() {
      return new AFPGraphics2D(this);
   }

   public void dispose() {
      this.graphicsObj = null;
   }

   public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
      return this.drawImage(img, x, y, img.getWidth(observer), img.getHeight(observer), observer);
   }

   private BufferedImage buildBufferedImage(Dimension size) {
      return new BufferedImage(size.width, size.height, 2);
   }

   private boolean drawBufferedImage(Image img, BufferedImage bufferedImage, int width, int height, ImageObserver observer) {
      Graphics2D g2d = bufferedImage.createGraphics();

      boolean var11;
      try {
         g2d.setComposite(AlphaComposite.SrcOver);
         Color color = new Color(1, 1, 1, 0);
         g2d.setBackground(color);
         g2d.setPaint(color);
         g2d.fillRect(0, 0, width, height);
         int imageWidth = bufferedImage.getWidth();
         int imageHeight = bufferedImage.getHeight();
         Rectangle clipRect = new Rectangle(0, 0, imageWidth, imageHeight);
         g2d.clip(clipRect);
         g2d.setComposite(this.gc.getComposite());
         var11 = g2d.drawImage(img, 0, 0, imageWidth, imageHeight, observer);
      } finally {
         g2d.dispose();
      }

      return var11;
   }

   public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer) {
      Dimension imageSize = new Dimension(width, height);
      BufferedImage bufferedImage = this.buildBufferedImage(imageSize);
      boolean drawn = this.drawBufferedImage(img, bufferedImage, width, height, observer);
      if (drawn) {
         this.drawRenderedImage(bufferedImage, new AffineTransform());
      }

      return false;
   }

   public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
      int imgWidth = img.getWidth();
      int imgHeight = img.getHeight();
      AffineTransform gat = this.gc.getTransform();
      int graphicsObjectHeight = this.graphicsObj.getObjectEnvironmentGroup().getObjectAreaDescriptor().getHeight();
      double toMillipointFactor = 72000.0 / (double)this.paintingState.getResolution();
      double x = gat.getTranslateX();
      double y = -(gat.getTranslateY() - (double)graphicsObjectHeight);
      x = toMillipointFactor * x;
      y = toMillipointFactor * y;
      double w = toMillipointFactor * (double)imgWidth * gat.getScaleX();
      double h = toMillipointFactor * (double)imgHeight * -gat.getScaleY();
      AFPImageHandlerRenderedImage handler = new AFPImageHandlerRenderedImage();
      String uri = null;
      if (this.resourceManager.getResourceLevelDefaults().getDefaultResourceLevel((byte)3).isPrintFile()) {
         uri = this.resourceInfo.getUri();
      }

      ImageInfo imageInfo = new ImageInfo(uri, (String)null);
      imageInfo.setSize(new ImageSize(img.getWidth(), img.getHeight(), (double)this.paintingState.getResolution()));
      imageInfo.getSize().calcSizeFromPixels();
      ImageRendered red = new ImageRendered(imageInfo, img, (Color)null);
      Rectangle targetPos = new Rectangle((int)Math.round(x), (int)Math.round(y), (int)Math.round(w), (int)Math.round(h));
      AFPRenderingContext context = new AFPRenderingContext((FOUserAgent)null, this.resourceManager, this.paintingState, this.fontInfo, (Map)null);
      this.resourceManager.includeCached = false;

      try {
         handler.handleImage(context, red, targetPos);
      } catch (IOException var24) {
         this.handleIOException(var24);
      }

      this.resourceManager.includeCached = true;
   }

   public void setCustomTextHandler(TextHandler handler) {
      this.customTextHandler = handler;
   }

   public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
      LOG.debug("drawRenderableImage() NYI: img=" + img + ", xform=" + xform);
   }

   public FontMetrics getFontMetrics(Font f) {
      LOG.debug("getFontMetrics() NYI: f=" + f);
      return null;
   }

   public void setXORMode(Color col) {
      LOG.debug("setXORMode() NYI: col=" + col);
   }

   public void addNativeImage(org.apache.xmlgraphics.image.loader.Image image, float x, float y, float width, float height) {
      LOG.debug("NYI: addNativeImage() image=" + image + ",x=" + x + ",y=" + y + ",width=" + width + ",height=" + height);
   }

   public void copyArea(int x, int y, int width, int height, int dx, int dy) {
      LOG.debug("copyArea() NYI: ");
   }

   public void clearRect(int x, int y, int width, int height) {
   }
}
