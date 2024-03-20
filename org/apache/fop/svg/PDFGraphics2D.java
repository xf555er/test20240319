package org.apache.fop.svg;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.awt.image.renderable.RenderableImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.batik.ext.awt.LinearGradientPaint;
import org.apache.batik.ext.awt.MultipleGradientPaint;
import org.apache.batik.ext.awt.RadialGradientPaint;
import org.apache.batik.ext.awt.RenderingHintsKeyExt;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.PatternPaint;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontSetup;
import org.apache.fop.fonts.Typeface;
import org.apache.fop.pdf.BitmapImage;
import org.apache.fop.pdf.PDFAnnotList;
import org.apache.fop.pdf.PDFColor;
import org.apache.fop.pdf.PDFColorHandler;
import org.apache.fop.pdf.PDFConformanceException;
import org.apache.fop.pdf.PDFDeviceColorSpace;
import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.pdf.PDFFunction;
import org.apache.fop.pdf.PDFGState;
import org.apache.fop.pdf.PDFImage;
import org.apache.fop.pdf.PDFImageXObject;
import org.apache.fop.pdf.PDFNumber;
import org.apache.fop.pdf.PDFPaintingState;
import org.apache.fop.pdf.PDFPattern;
import org.apache.fop.pdf.PDFReference;
import org.apache.fop.pdf.PDFResourceContext;
import org.apache.fop.pdf.PDFResources;
import org.apache.fop.pdf.PDFShading;
import org.apache.fop.pdf.PDFText;
import org.apache.fop.pdf.PDFXObject;
import org.apache.fop.render.gradient.Function;
import org.apache.fop.render.gradient.GradientMaker;
import org.apache.fop.render.gradient.Pattern;
import org.apache.fop.render.gradient.Shading;
import org.apache.fop.render.pdf.ImageRawCCITTFaxAdapter;
import org.apache.fop.render.pdf.ImageRawJPEGAdapter;
import org.apache.fop.render.pdf.ImageRenderedAdapter;
import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageSize;
import org.apache.xmlgraphics.image.loader.impl.ImageRawCCITTFax;
import org.apache.xmlgraphics.image.loader.impl.ImageRawJPEG;
import org.apache.xmlgraphics.image.loader.impl.ImageRendered;
import org.apache.xmlgraphics.java2d.AbstractGraphics2D;
import org.apache.xmlgraphics.java2d.GraphicContext;
import org.apache.xmlgraphics.java2d.GraphicsConfigurationWithTransparency;

public class PDFGraphics2D extends AbstractGraphics2D implements NativeImageHandler {
   private static final AffineTransform IDENTITY_TRANSFORM = new AffineTransform();
   private static final int DEC = 8;
   static final int OPAQUE = 255;
   protected PDFDocument pdfDoc;
   protected PDFResourceContext resourceContext;
   protected PDFReference pageRef;
   protected PDFPaintingState paintingState;
   protected PDFColorHandler colorHandler;
   protected int baseLevel;
   protected int nativeCount;
   protected FontInfo fontInfo;
   protected Font ovFontState;
   protected StringWriter currentStream;
   protected String currentFontName;
   protected float currentFontSize;
   protected OutputStream outputStream;
   private TransparencyIgnoredEventListener transparencyIgnoredEventListener;
   private Graphics2D fmg;

   public PDFGraphics2D(boolean textAsShapes, FontInfo fi, PDFDocument doc, PDFResourceContext page, PDFReference pref, String font, float size, TransparencyIgnoredEventListener listener) {
      this(textAsShapes);
      this.pdfDoc = doc;
      this.colorHandler = new PDFColorHandler(doc.getResources());
      this.resourceContext = page;
      this.currentFontName = font;
      this.currentFontSize = size;
      this.fontInfo = fi;
      this.pageRef = pref;
      this.paintingState = new PDFPaintingState();
      this.transparencyIgnoredEventListener = listener;
   }

   protected PDFGraphics2D(boolean textAsShapes) {
      super(textAsShapes);
      this.currentStream = new StringWriter();
      BufferedImage bi = new BufferedImage(1, 1, 2);
      this.fmg = bi.createGraphics();
   }

   public PDFGraphics2D(PDFGraphics2D g) {
      super(g);
      this.currentStream = new StringWriter();
      BufferedImage bi = new BufferedImage(1, 1, 2);
      this.fmg = bi.createGraphics();
      this.pdfDoc = g.pdfDoc;
      this.colorHandler = g.colorHandler;
      this.resourceContext = g.resourceContext;
      this.currentFontName = g.currentFontName;
      this.currentFontSize = g.currentFontSize;
      this.fontInfo = g.fontInfo;
      this.pageRef = g.pageRef;
      this.paintingState = g.paintingState;
      this.currentStream = g.currentStream;
      this.nativeCount = g.nativeCount;
      this.outputStream = g.outputStream;
      this.ovFontState = g.ovFontState;
      this.transparencyIgnoredEventListener = g.transparencyIgnoredEventListener;
   }

   public Graphics create() {
      return new PDFGraphics2D(this);
   }

   protected void handleIOException(IOException ioe) {
      ioe.printStackTrace();
   }

   protected void preparePainting() {
   }

   public void setPaintingState(PDFPaintingState state) {
      this.paintingState = state;
      this.baseLevel = this.paintingState.getStackLevel();
   }

   public void setOutputStream(OutputStream os) {
      this.outputStream = os;
   }

   public String getString() {
      return this.currentStream.toString();
   }

   public StringBuffer getBuffer() {
      return this.currentStream.getBuffer();
   }

   public PDFReference getPageReference() {
      return this.pageRef;
   }

   public void setGraphicContext(GraphicContext c) {
      this.gc = c;
      this.setPrivateHints();
   }

   private void setPrivateHints() {
      this.setRenderingHint(RenderingHintsKeyExt.KEY_AVOID_TILE_PAINTING, RenderingHintsKeyExt.VALUE_AVOID_TILE_PAINTING_ON);
   }

   public void setOverrideFontState(Font infont) {
      this.ovFontState = infont;
   }

   private void concatMatrix(double[] matrix) {
      this.currentStream.write(PDFNumber.doubleOut(matrix[0], 8) + " " + PDFNumber.doubleOut(matrix[1], 8) + " " + PDFNumber.doubleOut(matrix[2], 8) + " " + PDFNumber.doubleOut(matrix[3], 8) + " " + PDFNumber.doubleOut(matrix[4], 8) + " " + PDFNumber.doubleOut(matrix[5], 8) + " cm\n");
   }

   private void concatMatrix(AffineTransform transform) {
      if (!transform.isIdentity()) {
         double[] matrix = new double[6];
         transform.getMatrix(matrix);
         this.concatMatrix(matrix);
      }

   }

   protected AffineTransform getBaseTransform() {
      AffineTransform at = new AffineTransform(this.paintingState.getTransform());
      return at;
   }

   public void addLink(Rectangle2D bounds, AffineTransform trans, String dest, int linkType) {
      if (this.pdfDoc.getProfile().isAnnotationAllowed()) {
         this.preparePainting();
         AffineTransform at = this.getTransform();
         Shape b = at.createTransformedShape(bounds);
         b = trans.createTransformedShape(b);
         if (b != null) {
            Rectangle rect = b.getBounds();
            if (linkType != 0) {
               String idDest;
               if (dest.startsWith("#")) {
                  idDest = dest.substring(1);
                  this.resourceContext.addAnnotation(this.pdfDoc.getFactory().makeLink(rect, idDest, true));
               } else {
                  idDest = "/FitR " + dest;
                  this.resourceContext.addAnnotation(this.pdfDoc.getFactory().makeLink(rect, this.getPageReference().toString(), idDest));
               }
            } else {
               this.resourceContext.addAnnotation(this.pdfDoc.getFactory().makeLink(rect, dest, linkType, 0.0F));
            }
         }

      }
   }

   public void addNativeImage(Image image, float x, float y, float width, float height) {
      this.preparePainting();
      String key = image.getInfo().getOriginalURI();
      if (key == null) {
         key = "__AddNative_" + this.hashCode() + "_" + this.nativeCount;
         ++this.nativeCount;
      }

      Object pdfImage;
      if (image instanceof ImageRawJPEG) {
         pdfImage = new ImageRawJPEGAdapter((ImageRawJPEG)image, key);
      } else {
         if (!(image instanceof ImageRawCCITTFax)) {
            throw new IllegalArgumentException("Unsupported Image subclass: " + image.getClass().getName());
         }

         pdfImage = new ImageRawCCITTFaxAdapter((ImageRawCCITTFax)image, key);
      }

      PDFXObject xObject = this.pdfDoc.addImage(this.resourceContext, (PDFImage)pdfImage);
      this.flushPDFDocument();
      AffineTransform at = new AffineTransform();
      at.translate((double)x, (double)y);
      this.useXObject(xObject, at, width, height);
   }

   private void flushPDFDocument() {
      if (this.outputStream != null && !this.pdfDoc.isLinearizationEnabled()) {
         try {
            this.pdfDoc.output(this.outputStream);
         } catch (IOException var2) {
         }
      }

   }

   public boolean drawImage(java.awt.Image img, int x, int y, ImageObserver observer) {
      this.preparePainting();
      int width = img.getWidth(observer);
      int height = img.getHeight(observer);
      return width != -1 && height != -1 ? this.drawImage(img, x, y, width, height, observer) : false;
   }

   private BufferedImage buildBufferedImage(Dimension size) {
      return new BufferedImage(size.width, size.height, 2);
   }

   public boolean drawImage(java.awt.Image img, int x, int y, int width, int height, ImageObserver observer) {
      this.preparePainting();
      String key = "TempImage:" + img.toString();
      PDFXObject xObject = this.pdfDoc.getXObject(key);
      if (xObject == null) {
         Dimension size = new Dimension(width, height);
         BufferedImage buf = this.buildBufferedImage(size);
         Graphics2D g = buf.createGraphics();
         g.setComposite(AlphaComposite.SrcOver);
         g.setBackground(new Color(1, 1, 1, 0));
         g.setPaint(new Color(1, 1, 1, 0));
         g.fillRect(0, 0, width, height);
         int imageWidth = buf.getWidth();
         int imageHeight = buf.getHeight();
         g.clip(new Rectangle(0, 0, imageWidth, imageHeight));
         g.setComposite(this.gc.getComposite());
         boolean drawn = g.drawImage(img, 0, 0, imageWidth, imageHeight, observer);
         if (!drawn) {
            return false;
         }

         g.dispose();
         xObject = this.addRenderedImage(key, buf);
      } else {
         this.resourceContext.addXObject(xObject);
      }

      AffineTransform at = new AffineTransform();
      at.translate((double)x, (double)y);
      this.useXObject(xObject, at, (float)width, (float)height);
      return true;
   }

   public void dispose() {
      this.pdfDoc = null;
      this.fontInfo = null;
      this.currentStream = null;
      this.currentFontName = null;
   }

   public void draw(Shape s) {
      this.preparePainting();
      Color c = this.getColor();
      if (c.getAlpha() != 0) {
         AffineTransform trans = this.getTransform();
         double[] tranvals = new double[6];
         trans.getMatrix(tranvals);
         Shape imclip = this.getClip();
         boolean newClip = this.paintingState.checkClip(imclip);
         boolean newTransform = this.paintingState.checkTransform(trans) && !trans.isIdentity();
         if (newClip || newTransform) {
            this.saveGraphicsState();
            if (newTransform) {
               this.concatMatrix(tranvals);
            }

            if (newClip) {
               this.writeClip(imclip);
            }
         }

         this.applyAlpha(255, c.getAlpha());
         c = this.getColor();
         this.applyColor(c, false);
         c = this.getBackground();
         this.applyColor(c, true);
         Paint paint = this.getPaint();
         if (this.paintingState.setPaint(paint) && !this.applyPaint(paint, false)) {
            Shape ss = this.getStroke().createStrokedShape(s);
            this.applyUnknownPaint(paint, ss);
            if (newClip || newTransform) {
               this.restoreGraphicsState();
            }

         } else {
            this.applyStroke(this.getStroke());
            PathIterator iter = s.getPathIterator(IDENTITY_TRANSFORM);
            this.processPathIterator(iter);
            this.doDrawing(false, true, false);
            if (newClip || newTransform) {
               this.restoreGraphicsState();
            }

         }
      }
   }

   protected void writeClip(Shape s) {
      if (s != null) {
         PathIterator iter = s.getPathIterator(IDENTITY_TRANSFORM);
         if (!iter.isDone()) {
            this.preparePainting();
            this.processPathIterator(iter);
            this.currentStream.write("W\n");
            this.currentStream.write("n\n");
         }
      }
   }

   protected void applyColor(Color col, boolean fill) {
      this.preparePainting();
      if (col.getColorSpace().getType() == 9 && this.pdfDoc.getProfile().getPDFAMode().isPart1()) {
         throw new PDFConformanceException("PDF/A-1 does not allow mixing DeviceRGB and DeviceCMYK.");
      } else {
         boolean doWrite = false;
         if (fill) {
            if (this.paintingState.setBackColor(col)) {
               doWrite = true;
            }
         } else if (this.paintingState.setColor(col)) {
            doWrite = true;
         }

         if (doWrite) {
            StringBuffer sb = new StringBuffer();
            this.colorHandler.establishColor(sb, col, fill);
            this.currentStream.write(sb.toString());
         }

      }
   }

   protected boolean applyPaint(Paint paint, boolean fill) {
      this.preparePainting();
      if (paint instanceof Color) {
         return true;
      } else {
         if (paint instanceof GradientPaint) {
            GradientPaint gpaint = (GradientPaint)paint;
            paint = new LinearGradientPaint((float)gpaint.getPoint1().getX(), (float)gpaint.getPoint1().getY(), (float)gpaint.getPoint2().getX(), (float)gpaint.getPoint2().getY(), new float[]{0.0F, 1.0F}, new Color[]{gpaint.getColor1(), gpaint.getColor2()}, gpaint.isCyclic() ? LinearGradientPaint.REPEAT : LinearGradientPaint.NO_CYCLE);
         }

         PDFPattern pdfPattern;
         Pattern pattern;
         if (paint instanceof LinearGradientPaint && this.gradientSupported((LinearGradientPaint)paint)) {
            pattern = GradientMaker.makeLinearGradient((LinearGradientPaint)paint, this.getBaseTransform(), this.getTransform());
            pdfPattern = this.createPDFPattern(pattern);
            this.currentStream.write(pdfPattern.getColorSpaceOut(fill));
            return true;
         } else if (paint instanceof RadialGradientPaint && this.gradientSupported((RadialGradientPaint)paint)) {
            pattern = GradientMaker.makeRadialGradient((RadialGradientPaint)paint, this.getBaseTransform(), this.getTransform());
            pdfPattern = this.createPDFPattern(pattern);
            this.currentStream.write(pdfPattern.getColorSpaceOut(fill));
            return true;
         } else if (paint instanceof PatternPaint) {
            PatternPaint pp = (PatternPaint)paint;
            return this.createPattern(pp, fill);
         } else {
            return false;
         }
      }
   }

   private PDFPattern createPDFPattern(Pattern pattern) {
      Shading shading = pattern.getShading();
      Function function = shading.getFunction();
      List pdfFunctions = new ArrayList(function.getFunctions().size());
      Iterator var5 = function.getFunctions().iterator();

      while(var5.hasNext()) {
         Function f = (Function)var5.next();
         pdfFunctions.add(this.registerFunction(new PDFFunction(f)));
      }

      PDFFunction pdfFunction = this.registerFunction(new PDFFunction(function, pdfFunctions));
      PDFShading pdfShading = new PDFShading(shading.getShadingType(), shading.getColorSpace(), shading.getCoords(), pdfFunction);
      pdfShading = this.registerShading(pdfShading);
      PDFPattern pdfPattern = new PDFPattern(pattern.getPatternType(), pdfShading, (List)null, (StringBuffer)null, pattern.getMatrix());
      return this.registerPattern(pdfPattern);
   }

   private boolean gradientSupported(MultipleGradientPaint gradient) {
      return !this.gradientContainsTransparency(gradient) && !this.gradientIsRepeated(gradient);
   }

   private boolean gradientContainsTransparency(MultipleGradientPaint gradient) {
      Color[] var2 = gradient.getColors();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Color color = var2[var4];
         if (color.getAlpha() != 255) {
            return true;
         }
      }

      return false;
   }

   private boolean gradientIsRepeated(MultipleGradientPaint gradient) {
      return gradient.getCycleMethod() != MultipleGradientPaint.NO_CYCLE;
   }

   private boolean createPattern(PatternPaint pp, boolean fill) {
      this.preparePainting();
      FontInfo specialFontInfo = new FontInfo();
      boolean base14Kerning = false;
      FontSetup.setup(specialFontInfo, base14Kerning);
      PDFResources res = this.pdfDoc.getFactory().makeResources();
      PDFResourceContext context = new PDFResourceContext(res);
      PDFGraphics2D pattGraphic = new PDFGraphics2D(this.textAsShapes, specialFontInfo, this.pdfDoc, context, this.getPageReference(), "", 0.0F, this.transparencyIgnoredEventListener);
      pattGraphic.setGraphicContext(new GraphicContext());
      pattGraphic.gc.validateTransformStack();
      pattGraphic.setRenderingHints(this.getRenderingHints());
      pattGraphic.setOutputStream(this.outputStream);
      GraphicsNode gn = pp.getGraphicsNode();
      Rectangle2D rect = pp.getPatternRect();
      gn.paint(pattGraphic);
      List bbox = new ArrayList();
      bbox.add(rect.getX());
      bbox.add(rect.getHeight() + rect.getY());
      bbox.add(rect.getWidth() + rect.getX());
      bbox.add(rect.getY());
      AffineTransform transform = new AffineTransform(this.getBaseTransform());
      transform.concatenate(this.getTransform());
      transform.concatenate(pp.getPatternTransform());
      List theMatrix = new ArrayList();
      double[] mat = new double[6];
      transform.getMatrix(mat);
      double[] var14 = mat;
      int var15 = mat.length;

      for(int var16 = 0; var16 < var15; ++var16) {
         double aMat = var14[var16];
         theMatrix.add(aMat);
      }

      res.addFonts(this.pdfDoc, specialFontInfo);
      PDFPattern myPat = this.pdfDoc.getFactory().makePattern(this.resourceContext, 1, res, 1, 1, bbox, rect.getWidth(), rect.getHeight(), theMatrix, (List)null, pattGraphic.getBuffer());
      this.currentStream.write(myPat.getColorSpaceOut(fill));
      PDFAnnotList annots = context.getAnnotations();
      if (annots != null) {
         this.pdfDoc.addObject(annots);
      }

      this.flushPDFDocument();
      return true;
   }

   protected boolean applyUnknownPaint(Paint paint, Shape shape) {
      this.preparePainting();
      Shape clip = this.getClip();
      Rectangle2D usrBounds = shape.getBounds2D();
      if (clip != null) {
         Rectangle2D usrClipBounds = clip.getBounds2D();
         if (!usrClipBounds.intersects(usrBounds)) {
            return true;
         }

         Rectangle2D.intersect(usrBounds, usrClipBounds, usrBounds);
      }

      double usrX = usrBounds.getX();
      double usrY = usrBounds.getY();
      double usrW = usrBounds.getWidth();
      double usrH = usrBounds.getHeight();
      AffineTransform at = this.getTransform();
      Rectangle devShapeBounds = at.createTransformedShape(shape).getBounds();
      Rectangle devBounds;
      if (clip != null) {
         Rectangle devClipBounds = at.createTransformedShape(clip).getBounds();
         if (!devClipBounds.intersects(devShapeBounds)) {
            return true;
         }

         devBounds = devShapeBounds.intersection(devClipBounds);
      } else {
         devBounds = devShapeBounds;
      }

      int devX = devBounds.x;
      int devY = devBounds.y;
      int devW = devBounds.width;
      int devH = devBounds.height;
      ColorSpace rgbCS = ColorSpace.getInstance(1000);
      ColorModel rgbCM = new DirectColorModel(rgbCS, 32, 16711680, 65280, 255, -16777216, false, 0);
      PaintContext pctx = paint.createContext(rgbCM, devBounds, usrBounds, at, this.getRenderingHints());
      PDFXObject imageInfo = this.pdfDoc.getXObject("TempImage:" + pctx.toString());
      if (imageInfo != null) {
         this.resourceContext.addXObject((PDFXObject)imageInfo);
      } else {
         Raster r = pctx.getRaster(devX, devY, devW, devH);

         assert r instanceof WritableRaster;

         WritableRaster wr = (WritableRaster)r;
         wr = wr.createWritableTranslatedChild(0, 0);
         ColorModel pcm = pctx.getColorModel();
         BufferedImage bi = new BufferedImage(pcm, wr, pcm.isAlphaPremultiplied(), (Hashtable)null);
         byte[] rgb = new byte[devW * devH * 3];
         int[] line = new int[devW];
         int rgbIdx = 0;
         byte[] mask;
         int x;
         int y;
         int val;
         if (pcm.hasAlpha()) {
            mask = new byte[devW * devH];
            int maskIdx = 0;

            for(y = 0; y < devH; ++y) {
               bi.getRGB(0, y, devW, 1, line, 0, devW);

               for(x = 0; x < devW; ++x) {
                  val = line[x];
                  mask[maskIdx++] = (byte)(val >>> 24);
                  rgb[rgbIdx++] = (byte)(val >> 16 & 255);
                  rgb[rgbIdx++] = (byte)(val >> 8 & 255);
                  rgb[rgbIdx++] = (byte)(val & 255);
               }
            }
         } else {
            mask = null;

            for(y = 0; y < devH; ++y) {
               bi.getRGB(0, y, devW, 1, line, 0, devW);

               for(x = 0; x < devW; ++x) {
                  val = line[x];
                  rgb[rgbIdx++] = (byte)(val >> 16 & 255);
                  rgb[rgbIdx++] = (byte)(val >> 8 & 255);
                  rgb[rgbIdx++] = (byte)(val & 255);
               }
            }
         }

         PDFReference maskRef = null;
         BitmapImage fopimg;
         if (mask != null) {
            fopimg = new BitmapImage("TempImageMask:" + pctx.toString(), devW, devH, mask, (PDFReference)null);
            fopimg.setColorSpace(new PDFDeviceColorSpace(1));
            PDFImageXObject xobj = this.pdfDoc.addImage(this.resourceContext, fopimg);
            maskRef = xobj.makeReference();
            this.flushPDFDocument();
         }

         fopimg = new BitmapImage("TempImage:" + pctx.toString(), devW, devH, rgb, maskRef);
         fopimg.setTransparent(new PDFColor(255, 255, 255));
         imageInfo = this.pdfDoc.addImage(this.resourceContext, fopimg);
         this.flushPDFDocument();
      }

      this.currentStream.write("q\n");
      this.writeClip(shape);
      this.currentStream.write("" + PDFNumber.doubleOut(usrW) + " 0 0 " + PDFNumber.doubleOut(-usrH) + " " + PDFNumber.doubleOut(usrX) + " " + PDFNumber.doubleOut(usrY + usrH) + " cm\n" + ((PDFXObject)imageInfo).getName() + " Do\nQ\n");
      return true;
   }

   protected void applyStroke(Stroke stroke) {
      this.preparePainting();
      if (stroke instanceof BasicStroke) {
         BasicStroke bs = (BasicStroke)stroke;
         float[] da = bs.getDashArray();
         int count;
         if (da == null) {
            this.currentStream.write("[] 0 d\n");
         } else {
            this.currentStream.write("[");

            for(count = 0; count < da.length; ++count) {
               this.currentStream.write(PDFNumber.doubleOut((double)da[count]));
               if (count < da.length - 1) {
                  this.currentStream.write(" ");
               }
            }

            this.currentStream.write("] ");
            float offset = bs.getDashPhase();
            this.currentStream.write(PDFNumber.doubleOut((double)offset) + " d\n");
         }

         count = bs.getEndCap();
         switch (count) {
            case 0:
               this.currentStream.write("0 J\n");
               break;
            case 1:
               this.currentStream.write("1 J\n");
               break;
            case 2:
               this.currentStream.write("2 J\n");
         }

         int lj = bs.getLineJoin();
         switch (lj) {
            case 0:
               this.currentStream.write("0 j\n");
               break;
            case 1:
               this.currentStream.write("1 j\n");
               break;
            case 2:
               this.currentStream.write("2 j\n");
         }

         float lw = bs.getLineWidth();
         this.currentStream.write(PDFNumber.doubleOut((double)lw) + " w\n");
         float ml = Math.max(1.0F, bs.getMiterLimit());
         this.currentStream.write(PDFNumber.doubleOut((double)ml) + " M\n");
      }

   }

   public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
      String key = "TempImage:" + img.toString();
      this.drawInnerRenderedImage(key, img, xform);
   }

   public void drawInnerRenderedImage(String key, RenderedImage img, AffineTransform xform) {
      this.preparePainting();
      PDFXObject xObject = this.pdfDoc.getXObject(key);
      if (xObject == null) {
         xObject = this.addRenderedImage(key, img);
      } else {
         this.resourceContext.addXObject(xObject);
      }

      this.useXObject(xObject, xform, (float)img.getWidth(), (float)img.getHeight());
   }

   private void useXObject(PDFXObject xObject, AffineTransform xform, float width, float height) {
      this.currentStream.write("q\n");
      this.concatMatrix(this.getTransform());
      Shape imclip = this.getClip();
      this.writeClip(imclip);
      this.concatMatrix(xform);
      String w = PDFNumber.doubleOut((double)width, 8);
      String h = PDFNumber.doubleOut((double)height, 8);
      this.currentStream.write("" + w + " 0 0 -" + h + " 0 " + h + " cm\n" + xObject.getName() + " Do\nQ\n");
   }

   private PDFXObject addRenderedImage(String key, RenderedImage img) {
      ImageInfo info = new ImageInfo((String)null, "image/unknown");
      ImageSize size = new ImageSize(img.getWidth(), img.getHeight(), 72.0);
      info.setSize(size);
      ImageRendered imgRend = new ImageRendered(info, img, (Color)null);
      ImageRenderedAdapter adapter = new ImageRenderedAdapter(imgRend, key);
      PDFXObject xObject = this.pdfDoc.addImage(this.resourceContext, adapter);
      this.flushPDFDocument();
      return xObject;
   }

   public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
      this.drawRenderedImage(img.createDefaultRendering(), xform);
   }

   public void drawString(String s, float x, float y) {
      this.preparePainting();
      AffineTransform fontTransform = null;
      Font fontState;
      if (this.ovFontState == null) {
         java.awt.Font gFont = this.getFont();
         fontTransform = gFont.getTransform();
         fontState = this.fontInfo.getFontInstanceForAWTFont(gFont);
      } else {
         fontState = this.fontInfo.getFontInstance(this.ovFontState.getFontTriplet(), this.ovFontState.getFontSize());
         this.ovFontState = null;
      }

      this.updateCurrentFont(fontState);
      this.saveGraphicsState();
      Color c = this.getColor();
      this.applyColor(c, true);
      this.applyPaint(this.getPaint(), true);
      this.applyAlpha(c.getAlpha(), 255);
      Map kerning = fontState.getKerning();
      boolean kerningAvailable = kerning != null && !kerning.isEmpty();
      boolean useMultiByte = this.isMultiByteFont(this.currentFontName);
      String startText = useMultiByte ? "<" : "(";
      String endText = useMultiByte ? "> " : ") ";
      AffineTransform trans = this.getTransform();
      double[] vals = new double[6];
      trans.getMatrix(vals);
      this.concatMatrix(vals);
      Shape imclip = this.getClip();
      this.writeClip(imclip);
      this.currentStream.write("BT\n");
      AffineTransform localTransform = new AffineTransform();
      localTransform.translate((double)x, (double)y);
      if (fontTransform != null) {
         localTransform.concatenate(fontTransform);
      }

      localTransform.scale(1.0, -1.0);
      double[] lt = new double[6];
      localTransform.getMatrix(lt);
      this.currentStream.write(PDFNumber.doubleOut(lt[0]) + " " + PDFNumber.doubleOut(lt[1]) + " " + PDFNumber.doubleOut(lt[2]) + " " + PDFNumber.doubleOut(lt[3]) + " " + PDFNumber.doubleOut(lt[4]) + " " + PDFNumber.doubleOut(lt[5]) + " Tm [" + startText);
      int l = s.length();

      for(int i = 0; i < l; ++i) {
         char ch = fontState.mapChar(s.charAt(i));
         if (!useMultiByte) {
            if (ch > 127) {
               this.currentStream.write("\\");
               this.currentStream.write(Integer.toOctalString(ch));
            } else {
               switch (ch) {
                  case '(':
                  case ')':
                  case '\\':
                     this.currentStream.write("\\");
                  default:
                     this.currentStream.write(ch);
               }
            }
         } else {
            this.currentStream.write(PDFText.toUnicodeHex(ch));
         }

         if (kerningAvailable && i + 1 < l) {
            this.addKerning(this.currentStream, Integer.valueOf(ch), Integer.valueOf(fontState.mapChar(s.charAt(i + 1))), kerning, startText, endText);
         }
      }

      this.currentStream.write(endText);
      this.currentStream.write("] TJ\n");
      this.currentStream.write("ET\n");
      this.restoreGraphicsState();
   }

   protected void applyAlpha(int fillAlpha, int strokeAlpha) {
      if (fillAlpha != 255 || strokeAlpha != 255) {
         Object profile = this.isTransparencyAllowed();
         if (profile == null) {
            Map vals = new HashMap();
            if (fillAlpha != 255) {
               vals.put("ca", (float)fillAlpha / 255.0F);
            }

            if (strokeAlpha != 255) {
               vals.put("CA", (float)strokeAlpha / 255.0F);
            }

            PDFGState gstate = this.pdfDoc.getFactory().makeGState(vals, this.paintingState.getGState());
            this.resourceContext.addGState(gstate);
            this.currentStream.write("/" + gstate.getName() + " gs\n");
         } else if (this.transparencyIgnoredEventListener != null) {
            this.transparencyIgnoredEventListener.transparencyIgnored(profile);
         }
      }

   }

   protected void updateCurrentFont(Font font) {
      String name = font.getFontName();
      float size = (float)font.getFontSize() / 1000.0F;
      if (!name.equals(this.currentFontName) || size != this.currentFontSize) {
         this.currentFontName = name;
         this.currentFontSize = size;
         this.currentStream.write("/" + name + " " + size + " Tf\n");
      }

   }

   /** @deprecated */
   @Deprecated
   protected Font getInternalFontForAWTFont(java.awt.Font awtFont) {
      return this.fontInfo.getFontInstanceForAWTFont(awtFont);
   }

   protected boolean isMultiByteFont(String name) {
      Typeface f = (Typeface)this.fontInfo.getFonts().get(name);
      return f.isMultiByte();
   }

   private void addKerning(StringWriter buf, Integer ch1, Integer ch2, Map kerning, String startText, String endText) {
      this.preparePainting();
      Map kernPair = (Map)kerning.get(ch1);
      if (kernPair != null) {
         Integer width = (Integer)kernPair.get(ch2);
         if (width != null) {
            this.currentStream.write(endText + -width + " " + startText);
         }
      }

   }

   public void fill(Shape s) {
      this.preparePainting();
      Color c = this.getBackground();
      if (c.getAlpha() == 0) {
         c = this.getColor();
         if (c.getAlpha() == 0) {
            return;
         }
      }

      AffineTransform trans = this.getTransform();
      double[] tranvals = new double[6];
      trans.getMatrix(tranvals);
      Shape imclip = this.getClip();
      boolean newClip = this.paintingState.checkClip(imclip);
      boolean newTransform = this.paintingState.checkTransform(trans) && !trans.isIdentity();
      if (newClip || newTransform) {
         this.saveGraphicsState();
         if (newTransform) {
            this.concatMatrix(tranvals);
         }

         if (newClip) {
            this.writeClip(imclip);
         }
      }

      this.applyAlpha(c.getAlpha(), 255);
      c = this.getColor();
      this.applyColor(c, true);
      c = this.getBackground();
      this.applyColor(c, false);
      Paint paint = this.getPaint();
      if (this.paintingState.setPaint(paint) && !this.applyPaint(paint, true)) {
         this.applyUnknownPaint(paint, s);
         if (newClip || newTransform) {
            this.restoreGraphicsState();
         }

      } else {
         if (s instanceof Rectangle2D) {
            Rectangle2D rect = (Rectangle2D)s;
            this.currentStream.write(PDFNumber.doubleOut(rect.getMinX(), 8) + " " + PDFNumber.doubleOut(rect.getMinY(), 8) + " ");
            this.currentStream.write(PDFNumber.doubleOut(rect.getWidth(), 8) + " " + PDFNumber.doubleOut(rect.getHeight(), 8) + " re ");
            this.doDrawing(true, false, false);
         } else {
            PathIterator iter = s.getPathIterator(IDENTITY_TRANSFORM);
            this.processPathIterator(iter);
            this.doDrawing(true, false, iter.getWindingRule() == 0);
         }

         if (newClip || newTransform) {
            this.restoreGraphicsState();
         }

      }
   }

   void saveGraphicsState() {
      this.currentStream.write("q\n");
      this.paintingState.save();
   }

   void restoreGraphicsState() {
      this.currentStream.write("Q\n");
      this.paintingState.restore();
   }

   protected Object isTransparencyAllowed() {
      return this.pdfDoc.getProfile().isTransparencyAllowed();
   }

   public void processPathIterator(PathIterator iter) {
      double lastX = 0.0;

      for(double lastY = 0.0; !iter.isDone(); iter.next()) {
         double[] vals = new double[6];
         int type = iter.currentSegment(vals);
         switch (type) {
            case 0:
               lastX = vals[0];
               lastY = vals[1];
               this.currentStream.write(PDFNumber.doubleOut(vals[0], 8) + " " + PDFNumber.doubleOut(vals[1], 8) + " m\n");
               break;
            case 1:
               lastX = vals[0];
               lastY = vals[1];
               this.currentStream.write(PDFNumber.doubleOut(vals[0], 8) + " " + PDFNumber.doubleOut(vals[1], 8) + " l\n");
               break;
            case 2:
               double controlPointAX = lastX + 0.6666666666666666 * (vals[0] - lastX);
               double controlPointAY = lastY + 0.6666666666666666 * (vals[1] - lastY);
               double controlPointBX = vals[2] + 0.6666666666666666 * (vals[0] - vals[2]);
               double controlPointBY = vals[3] + 0.6666666666666666 * (vals[1] - vals[3]);
               this.currentStream.write(PDFNumber.doubleOut(controlPointAX, 8) + " " + PDFNumber.doubleOut(controlPointAY, 8) + " " + PDFNumber.doubleOut(controlPointBX, 8) + " " + PDFNumber.doubleOut(controlPointBY, 8) + " " + PDFNumber.doubleOut(vals[2], 8) + " " + PDFNumber.doubleOut(vals[3], 8) + " c\n");
               lastX = vals[2];
               lastY = vals[3];
               break;
            case 3:
               lastX = vals[4];
               lastY = vals[5];
               this.currentStream.write(PDFNumber.doubleOut(vals[0], 8) + " " + PDFNumber.doubleOut(vals[1], 8) + " " + PDFNumber.doubleOut(vals[2], 8) + " " + PDFNumber.doubleOut(vals[3], 8) + " " + PDFNumber.doubleOut(vals[4], 8) + " " + PDFNumber.doubleOut(vals[5], 8) + " c\n");
               break;
            case 4:
               this.currentStream.write("h\n");
         }
      }

   }

   protected void doDrawing(boolean fill, boolean stroke, boolean nonzero) {
      this.preparePainting();
      if (fill) {
         if (stroke) {
            if (nonzero) {
               this.currentStream.write("B*\n");
            } else {
               this.currentStream.write("B\n");
            }
         } else if (nonzero) {
            this.currentStream.write("f*\n");
         } else {
            this.currentStream.write("f\n");
         }
      } else {
         this.currentStream.write("S\n");
      }

   }

   public java.awt.GraphicsConfiguration getDeviceConfiguration() {
      return new GraphicsConfigurationWithTransparency();
   }

   public FontMetrics getFontMetrics(java.awt.Font f) {
      return this.fmg.getFontMetrics(f);
   }

   public void setXORMode(Color c1) {
   }

   public void copyArea(int x, int y, int width, int height, int dx, int dy) {
   }

   public PDFFunction registerFunction(PDFFunction function) {
      return this.pdfDoc.getFactory().registerFunction(function);
   }

   public PDFShading registerShading(PDFShading shading) {
      return this.pdfDoc.getFactory().registerShading(this.resourceContext, shading);
   }

   public PDFPattern registerPattern(PDFPattern pattern) {
      return this.pdfDoc.getFactory().registerPattern(this.resourceContext, pattern);
   }

   public interface TransparencyIgnoredEventListener {
      void transparencyIgnored(Object var1);
   }
}
