package org.apache.fop.render.afp;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import org.apache.fop.afp.AFPBorderPainter;
import org.apache.fop.afp.AFPEventProducer;
import org.apache.fop.afp.AFPObjectAreaInfo;
import org.apache.fop.afp.AFPPaintingState;
import org.apache.fop.afp.AFPResourceInfo;
import org.apache.fop.afp.AFPUnitConverter;
import org.apache.fop.afp.AbstractAFPPainter;
import org.apache.fop.afp.BorderPaintingInfo;
import org.apache.fop.afp.DataStream;
import org.apache.fop.afp.RectanglePaintingInfo;
import org.apache.fop.afp.fonts.AFPFont;
import org.apache.fop.afp.fonts.AFPFontAttributes;
import org.apache.fop.afp.fonts.AFPPageFonts;
import org.apache.fop.afp.fonts.CharacterSet;
import org.apache.fop.afp.modca.AbstractPageObject;
import org.apache.fop.afp.modca.PresentationTextObject;
import org.apache.fop.afp.ptoca.PtocaBuilder;
import org.apache.fop.afp.ptoca.PtocaProducer;
import org.apache.fop.afp.util.AFPResourceAccessor;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.fonts.FontType;
import org.apache.fop.render.ImageHandlerUtil;
import org.apache.fop.render.RenderingContext;
import org.apache.fop.render.intermediate.AbstractIFPainter;
import org.apache.fop.render.intermediate.BorderPainter;
import org.apache.fop.render.intermediate.GraphicsPainter;
import org.apache.fop.render.intermediate.IFException;
import org.apache.fop.render.intermediate.IFState;
import org.apache.fop.render.intermediate.IFUtil;
import org.apache.fop.traits.BorderProps;
import org.apache.fop.traits.RuleStyle;
import org.apache.fop.util.CharUtilities;
import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageException;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageProcessingHints;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;
import org.apache.xmlgraphics.image.loader.ImageSize;
import org.apache.xmlgraphics.image.loader.impl.ImageGraphics2D;
import org.apache.xmlgraphics.java2d.Graphics2DImagePainter;
import org.w3c.dom.Document;

public class AFPPainter extends AbstractIFPainter {
   private static final int X = 0;
   private static final int Y = 1;
   private final GraphicsPainter graphicsPainter;
   private final AFPBorderPainterAdapter borderPainter;
   private final AbstractAFPPainter rectanglePainter;
   private final AFPUnitConverter unitConv;
   private final AFPEventProducer eventProducer;
   private Integer bytesAvailable;

   public AFPPainter(AFPDocumentHandler documentHandler) {
      super(documentHandler);
      this.state = IFState.create();
      this.graphicsPainter = new AFPGraphicsPainter(new AFPBorderPainter(this.getPaintingState(), this.getDataStream()));
      this.borderPainter = new AFPBorderPainterAdapter(this.graphicsPainter, this, documentHandler);
      this.rectanglePainter = documentHandler.createRectanglePainter();
      this.unitConv = this.getPaintingState().getUnitConverter();
      this.eventProducer = AFPEventProducer.Provider.get(this.getUserAgent().getEventBroadcaster());
   }

   private AFPPaintingState getPaintingState() {
      return ((AFPDocumentHandler)this.getDocumentHandler()).getPaintingState();
   }

   private DataStream getDataStream() {
      return ((AFPDocumentHandler)this.getDocumentHandler()).getDataStream();
   }

   public String getFontKey(FontTriplet triplet) throws IFException {
      try {
         return super.getFontKey(triplet);
      } catch (IFException var3) {
         this.eventProducer.invalidConfiguration((Object)null, var3);
         return super.getFontKey(FontTriplet.DEFAULT_FONT_TRIPLET);
      }
   }

   public void startViewport(AffineTransform transform, Dimension size, Rectangle clipRect) throws IFException {
      try {
         this.saveGraphicsState();
         this.concatenateTransformationMatrix(transform);
      } catch (IOException var5) {
         throw new IFException("I/O error in startViewport()", var5);
      }
   }

   public void endViewport() throws IFException {
      try {
         this.restoreGraphicsState();
      } catch (IOException var2) {
         throw new IFException("I/O error in endViewport()", var2);
      }
   }

   private void concatenateTransformationMatrix(AffineTransform at) {
      if (!at.isIdentity()) {
         this.getPaintingState().concatenate(at);
      }

   }

   public void startGroup(AffineTransform transform, String layer) throws IFException {
      try {
         this.saveGraphicsState();
         this.concatenateTransformationMatrix(transform);
      } catch (IOException var4) {
         throw new IFException("I/O error in startGroup()", var4);
      }
   }

   public void endGroup() throws IFException {
      try {
         this.restoreGraphicsState();
      } catch (IOException var2) {
         throw new IFException("I/O error in endGroup()", var2);
      }
   }

   protected Map createDefaultImageProcessingHints(ImageSessionContext sessionContext) {
      Map hints = super.createDefaultImageProcessingHints(sessionContext);
      hints.put(ImageProcessingHints.TRANSPARENCY_INTENT, "ignore");
      hints.put("CMYK", ((AFPDocumentHandler)this.getDocumentHandler()).getPaintingState().isCMYKImagesSupported());
      return hints;
   }

   protected RenderingContext createRenderingContext() {
      AFPRenderingContext renderingContext = new AFPRenderingContext(this.getUserAgent(), ((AFPDocumentHandler)this.getDocumentHandler()).getResourceManager(), this.getPaintingState(), this.getFontInfo(), this.getContext().getForeignAttributes());
      return renderingContext;
   }

   public void drawImage(String uri, Rectangle rect) throws IFException {
      PageSegmentDescriptor pageSegment = ((AFPDocumentHandler)this.getDocumentHandler()).getPageSegmentNameFor(uri);
      if (pageSegment != null) {
         float[] srcPts = new float[]{(float)rect.x, (float)rect.y};
         int[] coords = this.unitConv.mpts2units(srcPts);
         int width = Math.round(this.unitConv.mpt2units((float)rect.width));
         int height = Math.round(this.unitConv.mpt2units((float)rect.height));
         this.getDataStream().createIncludePageSegment(pageSegment.getName(), coords[0], coords[1], width, height);
         if (pageSegment.getURI() != null) {
            AFPResourceAccessor accessor = new AFPResourceAccessor(((AFPDocumentHandler)this.getDocumentHandler()).getUserAgent().getResourceResolver());

            try {
               URI resourceUri = new URI(pageSegment.getURI());
               ((AFPDocumentHandler)this.getDocumentHandler()).getResourceManager().createIncludedResourceFromExternal(pageSegment.getName(), resourceUri, accessor);
            } catch (URISyntaxException var10) {
               throw new IFException("Could not handle resource url" + pageSegment.getURI(), var10);
            } catch (IOException var11) {
               throw new IFException("Could not handle resource" + pageSegment.getURI(), var11);
            }
         }
      } else {
         this.drawImageUsingURI(uri, rect);
      }

   }

   protected void drawImage(Image image, Rectangle rect, RenderingContext context, boolean convert, Map additionalHints) throws IOException, ImageException {
      AFPRenderingContext afpContext = (AFPRenderingContext)context;
      AFPResourceInfo resourceInfo = AFPImageHandler.createResourceInformation(image.getInfo().getOriginalURI(), afpContext.getForeignAttributes());
      if (afpContext.getResourceManager().isObjectCached(resourceInfo)) {
         AFPObjectAreaInfo areaInfo = AFPImageHandler.createObjectAreaInfo(afpContext.getPaintingState(), rect);
         afpContext.getResourceManager().includeCachedObject(resourceInfo, areaInfo);
      } else {
         super.drawImage(image, rect, context, convert, additionalHints);
      }

   }

   public void drawImage(Document doc, Rectangle rect) throws IFException {
      this.drawImageUsingDocument(doc, rect);
   }

   public void clipRect(Rectangle rect) throws IFException {
   }

   private float toPoint(int mpt) {
      return (float)mpt / 1000.0F;
   }

   public void fillRect(Rectangle rect, Paint fill) throws IFException {
      if (fill != null) {
         if (rect.width != 0 && rect.height != 0) {
            if (!(fill instanceof Color)) {
               throw new UnsupportedOperationException("Non-Color paints NYI");
            }

            this.getPaintingState().setColor((Color)fill);
            RectanglePaintingInfo rectanglePaintInfo = new RectanglePaintingInfo(this.toPoint(rect.x), this.toPoint(rect.y), this.toPoint(rect.width), this.toPoint(rect.height));

            try {
               this.rectanglePainter.paint(rectanglePaintInfo);
            } catch (IOException var5) {
               throw new IFException("IO error while painting rectangle", var5);
            }
         }

      }
   }

   public void drawBorderRect(Rectangle rect, BorderProps top, BorderProps bottom, BorderProps left, BorderProps right, Color innerBackgroundColor) throws IFException {
      if (top != null || bottom != null || left != null || right != null) {
         this.borderPainter.drawBorders(rect, top, bottom, left, right, innerBackgroundColor);
      }

   }

   public void drawLine(Point start, Point end, int width, Color color, RuleStyle style) throws IFException {
      try {
         this.graphicsPainter.drawLine(start, end, width, color, style);
      } catch (IOException var7) {
         throw new IFException("I/O error in drawLine()", var7);
      }
   }

   public void drawText(int x, int y, int letterSpacing, int wordSpacing, int[][] dp, String text) throws IFException {
      new DefaultPtocaProducer(x, y, letterSpacing, wordSpacing, dp, text);
   }

   protected void saveGraphicsState() throws IOException {
      this.getPaintingState().save();
   }

   protected void restoreGraphicsState() throws IOException {
      this.getPaintingState().restore();
   }

   public void clipBackground(Rectangle rect, BorderProps bpsBefore, BorderProps bpsAfter, BorderProps bpsStart, BorderProps bpsEnd) throws IFException {
   }

   public boolean isBackgroundRequired(BorderProps bpsBefore, BorderProps bpsAfter, BorderProps bpsStart, BorderProps bpsEnd) {
      return this.borderPainter.isBackgroundRequired(bpsBefore, bpsAfter, bpsStart, bpsEnd);
   }

   public void fillBackground(Rectangle rect, Paint fill, BorderProps bpsBefore, BorderProps bpsAfter, BorderProps bpsStart, BorderProps bpsEnd) throws IFException {
   }

   private final class DefaultPtocaProducer implements PtocaProducer {
      final int[] coords;
      final int fontReference;
      final String text;
      final int[][] dp;
      final int letterSpacing;
      final int wordSpacing;
      final Font font;
      final AFPFont afpFont;
      final CharacterSet charSet;
      PresentationTextObject pto;

      private DefaultPtocaProducer(int x, int y, int letterSpacing, int wordSpacing, int[][] dp, String text) throws IFException {
         this.letterSpacing = letterSpacing;
         this.wordSpacing = wordSpacing;
         this.text = text;
         this.dp = dp;
         int fontSize = AFPPainter.this.state.getFontSize();
         AFPPainter.this.getPaintingState().setFontSize(fontSize);
         FontTriplet triplet = new FontTriplet(AFPPainter.this.state.getFontFamily(), AFPPainter.this.state.getFontStyle(), AFPPainter.this.state.getFontWeight());
         String fontKey = AFPPainter.this.getFontKey(triplet);
         Map fontMetricMap = AFPPainter.this.getFontInfo().getFonts();
         this.afpFont = (AFPFont)fontMetricMap.get(fontKey);
         this.font = AFPPainter.this.getFontInfo().getFontInstance(triplet, fontSize);
         AFPPageFonts pageFonts = AFPPainter.this.getPaintingState().getPageFonts();
         AFPFontAttributes fontAttributes = pageFonts.registerFont(fontKey, this.afpFont, fontSize);
         this.fontReference = fontAttributes.getFontReference();
         this.coords = AFPPainter.this.unitConv.mpts2units(new float[]{(float)x, (float)y});
         this.charSet = this.afpFont.getCharacterSet(fontSize);
         if (this.afpFont.isEmbeddable()) {
            try {
               ((AFPDocumentHandler)AFPPainter.this.getDocumentHandler()).getResourceManager().embedFont(this.afpFont, this.charSet);
            } catch (IOException var17) {
               throw new IFException("Error while embedding font resources", var17);
            }
         }

         AbstractPageObject page = AFPPainter.this.getDataStream().getCurrentPage();

         try {
            if (AFPPainter.this.bytesAvailable != null && AFPPainter.this.bytesAvailable < this.getSize()) {
               page.endPresentationObject();
            }

            this.pto = page.getPresentationTextObject();
            boolean success = this.pto.createControlSequences(this);
            if (!success) {
               page.endPresentationObject();
               this.pto = page.getPresentationTextObject();
               this.pto.createControlSequences(this);
            }

         } catch (IOException var16) {
            throw new IFException("I/O error in drawText()", var16);
         }
      }

      private int getSize() throws IOException {
         final ByteArrayOutputStream bos = new ByteArrayOutputStream();
         PtocaBuilder pb = new PtocaBuilder() {
            protected OutputStream getOutputStreamForControlSequence(int length) {
               return bos;
            }
         };
         this.produce(pb);
         return bos.size();
      }

      public void produce(PtocaBuilder builder) throws IOException {
         Point p = AFPPainter.this.getPaintingState().getPoint(this.coords[0], this.coords[1]);
         builder.setTextOrientation(AFPPainter.this.getPaintingState().getRotation());
         builder.absoluteMoveBaseline(p.y);
         builder.absoluteMoveInline(p.x);
         builder.setExtendedTextColor(AFPPainter.this.state.getTextColor());
         builder.setCodedFont((byte)this.fontReference);
         int l = this.text.length();
         int[] dx = IFUtil.convertDPToDX(this.dp);
         int dxl = dx != null ? dx.length : 0;
         StringBuffer sb = new StringBuffer();
         if (dxl > 0 && dx[0] != 0) {
            int dxu = Math.round(AFPPainter.this.unitConv.mpt2units((float)dx[0]));
            builder.relativeMoveInline(-dxu);
         }

         boolean usePTOCAWordSpacing = true;
         int interCharacterAdjustment = 0;
         if (this.letterSpacing != 0) {
            interCharacterAdjustment = Math.round(AFPPainter.this.unitConv.mpt2units((float)this.letterSpacing));
         }

         builder.setInterCharacterAdjustment(interCharacterAdjustment);
         int spaceWidth = this.font.getCharWidth(' ');
         int fixedSpaceCharacterIncrement = Math.round(AFPPainter.this.unitConv.mpt2units((float)(spaceWidth + this.letterSpacing)));
         int varSpaceCharacterIncrement = fixedSpaceCharacterIncrement;
         if (this.wordSpacing != 0) {
            varSpaceCharacterIncrement = Math.round(AFPPainter.this.unitConv.mpt2units((float)(spaceWidth + this.wordSpacing + this.letterSpacing)));
         }

         builder.setVariableSpaceCharacterIncrement(varSpaceCharacterIncrement);
         boolean fixedSpaceMode = false;
         int ttPos = p.x;

         for(int i = 0; i < l; ++i) {
            char orgChar = this.text.charAt(i);
            float glyphAdjust = 0.0F;
            int increment;
            if (this.afpFont.getFontType() == FontType.TRUETYPE) {
               this.flushText(builder, sb, this.charSet);
               fixedSpaceMode = true;
               increment = this.font.getCharWidth(orgChar);
               sb.append(orgChar);
               glyphAdjust += (float)increment;
            } else if (CharUtilities.isFixedWidthSpace(orgChar)) {
               this.flushText(builder, sb, this.charSet);
               builder.setVariableSpaceCharacterIncrement(fixedSpaceCharacterIncrement);
               fixedSpaceMode = true;
               sb.append(' ');
               increment = this.font.getCharWidth(orgChar);
               glyphAdjust += (float)(increment - spaceWidth);
            } else {
               if (fixedSpaceMode) {
                  this.flushText(builder, sb, this.charSet);
                  builder.setVariableSpaceCharacterIncrement(varSpaceCharacterIncrement);
                  fixedSpaceMode = false;
               }

               char ch;
               if (orgChar == 160) {
                  ch = ' ';
               } else {
                  ch = orgChar;
               }

               sb.append(ch);
            }

            if (i < dxl - 1) {
               glyphAdjust += (float)dx[i + 1];
            }

            if (this.afpFont.getFontType() == FontType.TRUETYPE) {
               this.flushText(builder, sb, this.charSet);
               ttPos += Math.round(AFPPainter.this.unitConv.mpt2units(glyphAdjust));
               builder.absoluteMoveInline(ttPos);
            } else if (glyphAdjust != 0.0F) {
               this.flushText(builder, sb, this.charSet);
               increment = Math.round(AFPPainter.this.unitConv.mpt2units(glyphAdjust));
               builder.relativeMoveInline(increment);
            }
         }

         this.flushText(builder, sb, this.charSet);
         if (this.pto != null) {
            AFPPainter.this.bytesAvailable = this.pto.getBytesAvailable();
         }

      }

      private void flushText(PtocaBuilder builder, StringBuffer sb, CharacterSet charSet) throws IOException {
         if (sb.length() > 0) {
            builder.addTransparentData(charSet.encodeChars(sb));
            sb.setLength(0);
         }

      }

      // $FF: synthetic method
      DefaultPtocaProducer(int x1, int x2, int x3, int x4, int[][] x5, String x6, Object x7) throws IFException {
         this(x1, x2, x3, x4, x5, x6);
      }
   }

   private static class AFPBorderPainterAdapter extends BorderPainter {
      private final AFPPainter painter;
      private final AFPDocumentHandler documentHandler;

      public AFPBorderPainterAdapter(GraphicsPainter graphicsPainter, AFPPainter painter, AFPDocumentHandler documentHandler) {
         super(graphicsPainter);
         this.painter = painter;
         this.documentHandler = documentHandler;
      }

      public void drawBorders(Rectangle borderRect, BorderProps bpsBefore, BorderProps bpsAfter, BorderProps bpsStart, BorderProps bpsEnd, Color innerBackgroundColor) throws IFException {
         this.drawRoundedCorners(borderRect, bpsBefore, bpsAfter, bpsStart, bpsEnd, innerBackgroundColor);
      }

      private boolean isBackgroundRequired(BorderProps bpsBefore, BorderProps bpsAfter, BorderProps bpsStart, BorderProps bpsEnd) {
         return !this.hasRoundedCorners(bpsBefore, bpsAfter, bpsStart, bpsEnd);
      }

      private boolean hasRoundedCorners(BorderProps bpsBefore, BorderProps bpsAfter, BorderProps bpsStart, BorderProps bpsEnd) {
         return bpsStart != null && bpsStart.getRadiusStart() > 0 && bpsBefore != null && bpsBefore.getRadiusStart() > 0 || bpsBefore != null && bpsBefore.getRadiusEnd() > 0 && bpsEnd != null && bpsEnd.getRadiusStart() > 0 || bpsEnd != null && bpsEnd.getRadiusEnd() > 0 && bpsAfter != null && bpsAfter.getRadiusEnd() > 0 || bpsAfter != null && bpsAfter.getRadiusStart() > 0 && bpsStart != null && bpsStart.getRadiusEnd() > 0;
      }

      private void drawRoundedCorners(Rectangle borderRect, BorderProps bpsBefore, BorderProps bpsAfter, BorderProps bpsStart, BorderProps bpsEnd, Color innerBackgroundColor) throws IFException {
         double cornerCorrectionFactor = calculateCornerCorrectionFactor(borderRect.width, borderRect.height, bpsBefore, bpsAfter, bpsStart, bpsEnd);
         boolean[] roundCorner = new boolean[]{bpsBefore != null && bpsStart != null && bpsBefore.getRadiusStart() > 0 && bpsStart.getRadiusStart() > 0 && this.isNotCollapseOuter(bpsBefore) && this.isNotCollapseOuter(bpsStart), bpsEnd != null && bpsBefore != null && bpsEnd.getRadiusStart() > 0 && bpsBefore.getRadiusEnd() > 0 && this.isNotCollapseOuter(bpsEnd) && this.isNotCollapseOuter(bpsBefore), bpsEnd != null && bpsAfter != null && bpsEnd.getRadiusEnd() > 0 && bpsAfter.getRadiusEnd() > 0 && this.isNotCollapseOuter(bpsEnd) && this.isNotCollapseOuter(bpsAfter), bpsStart != null && bpsAfter != null && bpsStart.getRadiusEnd() > 0 && bpsAfter.getRadiusStart() > 0 && this.isNotCollapseOuter(bpsStart) && this.isNotCollapseOuter(bpsAfter)};
         if (!roundCorner[0] && !roundCorner[1] && !roundCorner[2] && !roundCorner[3]) {
            try {
               this.drawRectangularBorders(borderRect, bpsBefore, bpsAfter, bpsStart, bpsEnd);
            } catch (IOException var13) {
               throw new IFException("IO error drawing borders", var13);
            }
         } else {
            String areaKey = this.makeKey(borderRect, bpsBefore, bpsEnd, bpsAfter, bpsStart, innerBackgroundColor);
            Graphics2DImagePainter painter = null;
            String name = this.documentHandler.getCachedRoundedCorner(areaKey);
            if (name == null) {
               name = this.documentHandler.cacheRoundedCorner(areaKey);
               painter = new BorderImagePainter(cornerCorrectionFactor, borderRect, bpsStart, bpsEnd, bpsBefore, bpsAfter, roundCorner, innerBackgroundColor);
            }

            this.paintCornersAsBitmap(painter, borderRect, name);
         }
      }

      private boolean isNotCollapseOuter(BorderProps bp) {
         return !bp.isCollapseOuter();
      }

      private Area makeCornerClip(int beforeRadius, int startRadius, AffineTransform transform) {
         Rectangle clipR = new Rectangle(0, 0, startRadius, beforeRadius);
         Area clip = new Area(clipR);
         Ellipse2D.Double e = new Ellipse2D.Double();
         e.x = 0.0;
         e.y = 0.0;
         e.width = (double)(2 * startRadius);
         e.height = (double)(2 * beforeRadius);
         clip.subtract(new Area(e));
         clip.transform(transform);
         return clip;
      }

      private Area makeCornerBorderBPD(int beforeRadius, int startRadius, int beforeWidth, int startWidth, AffineTransform transform) {
         Rectangle clipR = new Rectangle(0, 0, startRadius, beforeRadius);
         Ellipse2D.Double e = new Ellipse2D.Double();
         e.x = 0.0;
         e.y = 0.0;
         e.width = (double)(2 * startRadius);
         e.height = (double)(2 * beforeRadius);
         Ellipse2D.Double i = new Ellipse2D.Double();
         i.x = (double)startWidth;
         i.y = (double)beforeWidth;
         i.width = (double)(2 * (startRadius - startWidth));
         i.height = (double)(2 * (beforeRadius - beforeWidth));
         Area clip = new Area(e);
         clip.subtract(new Area(i));
         clip.intersect(new Area(clipR));
         GeneralPath cut = new GeneralPath();
         cut.moveTo(0.0F, 0.0F);
         cut.lineTo((float)startRadius, (float)startRadius * (float)beforeWidth / (float)startWidth);
         cut.lineTo((float)startRadius, 0.0F);
         clip.intersect(new Area(cut));
         clip.transform(transform);
         return clip;
      }

      private Area makeCornerBorderIPD(int beforeRadius, int startRadius, int beforeWidth, int startWidth, AffineTransform transform) {
         Rectangle clipR = new Rectangle(0, 0, startRadius, beforeRadius);
         Ellipse2D.Double e = new Ellipse2D.Double();
         e.x = 0.0;
         e.y = 0.0;
         e.width = (double)(2 * startRadius);
         e.height = (double)(2 * beforeRadius);
         Ellipse2D.Double i = new Ellipse2D.Double();
         i.x = (double)startWidth;
         i.y = (double)beforeWidth;
         i.width = (double)(2 * (startRadius - startWidth));
         i.height = (double)(2 * (beforeRadius - beforeWidth));
         Area clip = new Area(e);
         clip.subtract(new Area(i));
         clip.intersect(new Area(clipR));
         GeneralPath cut = new GeneralPath();
         cut.moveTo(0.0F, 0.0F);
         cut.lineTo((float)startRadius, (float)startRadius * (float)beforeWidth / (float)startWidth);
         cut.lineTo((float)startRadius, 0.0F);
         clip.subtract(new Area(cut));
         clip.transform(transform);
         return clip;
      }

      private String makeKey(Rectangle area, BorderProps beforeProps, BorderProps endProps, BorderProps afterProps, BorderProps startProps, Color innerBackgroundColor) {
         return this.hash(area.width + ":" + area.height + ":" + beforeProps + ":" + endProps + ":" + afterProps + ":" + startProps + ":" + innerBackgroundColor);
      }

      private String hash(String text) {
         MessageDigest md;
         try {
            md = MessageDigest.getInstance("MD5");
         } catch (Exception var8) {
            throw new RuntimeException("Internal error", var8);
         }

         byte[] result = md.digest(text.getBytes());
         StringBuffer sb = new StringBuffer();
         char[] digits = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

         for(int idx = 0; idx < 6; ++idx) {
            byte b = result[idx];
            sb.append(digits[(b & 240) >> 4]);
            sb.append(digits[b & 15]);
         }

         return sb.toString();
      }

      private void paintCornersAsBitmap(Graphics2DImagePainter painter, Rectangle boundingBox, String name) throws IFException {
         ImageInfo info = new ImageInfo(name, (String)null);
         ImageSize size = new ImageSize();
         size.setSizeInMillipoints(boundingBox.width, boundingBox.height);
         Map map = new HashMap(2);
         map.put(AFPForeignAttributeReader.RESOURCE_NAME, name);
         map.put(AFPForeignAttributeReader.RESOURCE_LEVEL, "print-file");
         AFPRenderingContext context = (AFPRenderingContext)this.painter.createRenderingContext();
         size.setResolution((double)context.getPaintingState().getResolution());
         size.calcPixelsFromSize();
         info.setSize(size);
         ImageGraphics2D img = new ImageGraphics2D(info, painter);
         Map hints = new HashMap();
         hints.put(ImageHandlerUtil.CONVERSION_MODE, "bitmap");
         hints.put("TARGET_RESOLUTION", context.getPaintingState().getResolution());

         try {
            this.painter.drawImage(img, boundingBox, context, true, hints);
         } catch (IOException var11) {
            throw new IFException("I/O error while painting corner using a bitmap", var11);
         } catch (ImageException var12) {
            throw new IFException("Image error while painting corner using a bitmap", var12);
         }
      }

      protected void arcTo(double startAngle, double endAngle, int cx, int cy, int width, int height) throws IOException {
         throw new UnsupportedOperationException("Can only deal with horizontal lines right now");
      }

      private final class BorderImagePainter implements Graphics2DImagePainter {
         private final double cornerCorrectionFactor;
         private final Rectangle borderRect;
         private final BorderProps bpsStart;
         private final BorderProps bpsEnd;
         private final BorderProps bpsBefore;
         private final BorderProps bpsAfter;
         private final boolean[] roundCorner;
         private final Color innerBackgroundColor;

         private BorderImagePainter(double cornerCorrectionFactor, Rectangle borderRect, BorderProps bpsStart, BorderProps bpsEnd, BorderProps bpsBefore, BorderProps bpsAfter, boolean[] roundCorner, Color innerBackgroundColor) {
            this.cornerCorrectionFactor = cornerCorrectionFactor;
            this.borderRect = borderRect;
            this.bpsStart = bpsStart;
            this.bpsBefore = bpsBefore;
            this.roundCorner = roundCorner;
            this.bpsEnd = bpsEnd;
            this.bpsAfter = bpsAfter;
            this.innerBackgroundColor = innerBackgroundColor;
         }

         public void paint(Graphics2D g2d, Rectangle2D area) {
            Area background = new Area(area);
            Area cornerRegion = new Area();
            Area[] cornerBorder = new Area[]{new Area(), new Area(), new Area(), new Area()};
            Area[] clip = new Area[4];
            AffineTransform transform;
            int beforeRadius;
            int startRadius;
            int beforeWidth;
            int startWidth;
            boolean corner;
            if (this.roundCorner[0]) {
               transform = new AffineTransform();
               beforeRadius = (int)(this.cornerCorrectionFactor * (double)this.bpsBefore.getRadiusStart());
               startRadius = (int)(this.cornerCorrectionFactor * (double)this.bpsStart.getRadiusStart());
               beforeWidth = this.bpsBefore.width;
               startWidth = this.bpsStart.width;
               corner = false;
               background.subtract(AFPBorderPainterAdapter.this.makeCornerClip(beforeRadius, startRadius, transform));
               clip[0] = new Area(new Rectangle(0, 0, startRadius, beforeRadius));
               clip[0].transform(transform);
               cornerRegion.add(clip[0]);
               cornerBorder[0].add(AFPBorderPainterAdapter.this.makeCornerBorderBPD(beforeRadius, startRadius, beforeWidth, startWidth, transform));
               cornerBorder[3].add(AFPBorderPainterAdapter.this.makeCornerBorderIPD(beforeRadius, startRadius, beforeWidth, startWidth, transform));
            }

            if (this.roundCorner[1]) {
               transform = new AffineTransform(-1.0F, 0.0F, 0.0F, 1.0F, (float)this.borderRect.width, 0.0F);
               beforeRadius = (int)(this.cornerCorrectionFactor * (double)this.bpsBefore.getRadiusEnd());
               startRadius = (int)(this.cornerCorrectionFactor * (double)this.bpsEnd.getRadiusStart());
               beforeWidth = this.bpsBefore.width;
               startWidth = this.bpsEnd.width;
               corner = true;
               background.subtract(AFPBorderPainterAdapter.this.makeCornerClip(beforeRadius, startRadius, transform));
               clip[1] = new Area(new Rectangle(0, 0, startRadius, beforeRadius));
               clip[1].transform(transform);
               cornerRegion.add(clip[1]);
               cornerBorder[0].add(AFPBorderPainterAdapter.this.makeCornerBorderBPD(beforeRadius, startRadius, beforeWidth, startWidth, transform));
               cornerBorder[1].add(AFPBorderPainterAdapter.this.makeCornerBorderIPD(beforeRadius, startRadius, beforeWidth, startWidth, transform));
            }

            if (this.roundCorner[2]) {
               transform = new AffineTransform(-1.0F, 0.0F, 0.0F, -1.0F, (float)this.borderRect.width, (float)this.borderRect.height);
               beforeRadius = (int)(this.cornerCorrectionFactor * (double)this.bpsAfter.getRadiusEnd());
               startRadius = (int)(this.cornerCorrectionFactor * (double)this.bpsEnd.getRadiusEnd());
               beforeWidth = this.bpsAfter.width;
               startWidth = this.bpsEnd.width;
               corner = true;
               background.subtract(AFPBorderPainterAdapter.this.makeCornerClip(beforeRadius, startRadius, transform));
               clip[2] = new Area(new Rectangle(0, 0, startRadius, beforeRadius));
               clip[2].transform(transform);
               cornerRegion.add(clip[2]);
               cornerBorder[2].add(AFPBorderPainterAdapter.this.makeCornerBorderBPD(beforeRadius, startRadius, beforeWidth, startWidth, transform));
               cornerBorder[1].add(AFPBorderPainterAdapter.this.makeCornerBorderIPD(beforeRadius, startRadius, beforeWidth, startWidth, transform));
            }

            if (this.roundCorner[3]) {
               transform = new AffineTransform(1.0F, 0.0F, 0.0F, -1.0F, 0.0F, (float)this.borderRect.height);
               beforeRadius = (int)(this.cornerCorrectionFactor * (double)this.bpsAfter.getRadiusStart());
               startRadius = (int)(this.cornerCorrectionFactor * (double)this.bpsStart.getRadiusEnd());
               beforeWidth = this.bpsAfter.width;
               startWidth = this.bpsStart.width;
               corner = true;
               background.subtract(AFPBorderPainterAdapter.this.makeCornerClip(beforeRadius, startRadius, transform));
               clip[3] = new Area(new Rectangle(0, 0, startRadius, beforeRadius));
               clip[3].transform(transform);
               cornerRegion.add(clip[3]);
               cornerBorder[2].add(AFPBorderPainterAdapter.this.makeCornerBorderBPD(beforeRadius, startRadius, beforeWidth, startWidth, transform));
               cornerBorder[3].add(AFPBorderPainterAdapter.this.makeCornerBorderIPD(beforeRadius, startRadius, beforeWidth, startWidth, transform));
            }

            g2d.setColor(this.innerBackgroundColor);
            g2d.fill(background);
            GeneralPath borderPath;
            Area border;
            if (this.bpsBefore != null && this.bpsBefore.width > 0) {
               borderPath = new GeneralPath();
               borderPath.moveTo(0.0F, 0.0F);
               borderPath.lineTo((float)this.borderRect.width, 0.0F);
               borderPath.lineTo((float)(this.borderRect.width - (this.bpsEnd == null ? 0 : this.bpsEnd.width)), (float)this.bpsBefore.width);
               borderPath.lineTo(this.bpsStart == null ? 0.0F : (float)this.bpsStart.width, (float)this.bpsBefore.width);
               border = new Area(borderPath);
               if (clip[0] != null) {
                  border.subtract(clip[0]);
               }

               if (clip[1] != null) {
                  border.subtract(clip[1]);
               }

               g2d.setColor(this.bpsBefore.color);
               g2d.fill(border);
               g2d.fill(cornerBorder[0]);
            }

            if (this.bpsEnd != null && this.bpsEnd.width > 0) {
               borderPath = new GeneralPath();
               borderPath.moveTo((float)this.borderRect.width, 0.0F);
               borderPath.lineTo((float)this.borderRect.width, (float)this.borderRect.height);
               borderPath.lineTo((float)(this.borderRect.width - this.bpsEnd.width), (float)(this.borderRect.height - (this.bpsAfter == null ? 0 : this.bpsAfter.width)));
               borderPath.lineTo((float)(this.borderRect.width - this.bpsEnd.width), this.bpsBefore == null ? 0.0F : (float)this.bpsBefore.width);
               border = new Area(borderPath);
               if (clip[2] != null) {
                  border.subtract(clip[2]);
               }

               if (clip[1] != null) {
                  border.subtract(clip[1]);
               }

               g2d.setColor(this.bpsEnd.color);
               g2d.fill(border);
               g2d.fill(cornerBorder[1]);
            }

            if (this.bpsAfter != null && this.bpsAfter.width > 0) {
               borderPath = new GeneralPath();
               borderPath.moveTo(0.0F, (float)this.borderRect.height);
               borderPath.lineTo((float)this.borderRect.width, (float)this.borderRect.height);
               borderPath.lineTo((float)(this.borderRect.width - (this.bpsEnd == null ? 0 : this.bpsEnd.width)), (float)(this.borderRect.height - this.bpsAfter.width));
               borderPath.lineTo(this.bpsStart == null ? 0.0F : (float)this.bpsStart.width, (float)(this.borderRect.height - this.bpsAfter.width));
               border = new Area(borderPath);
               if (clip[3] != null) {
                  border.subtract(clip[3]);
               }

               if (clip[2] != null) {
                  border.subtract(clip[2]);
               }

               g2d.setColor(this.bpsAfter.color);
               g2d.fill(border);
               g2d.fill(cornerBorder[2]);
            }

            if (this.bpsStart != null && this.bpsStart.width > 0) {
               borderPath = new GeneralPath();
               borderPath.moveTo((float)this.bpsStart.width, this.bpsBefore == null ? 0.0F : (float)this.bpsBefore.width);
               borderPath.lineTo((float)this.bpsStart.width, (float)(this.borderRect.height - (this.bpsAfter == null ? 0 : this.bpsAfter.width)));
               borderPath.lineTo(0.0F, (float)this.borderRect.height);
               borderPath.lineTo(0.0F, 0.0F);
               border = new Area(borderPath);
               if (clip[3] != null) {
                  border.subtract(clip[3]);
               }

               if (clip[0] != null) {
                  border.subtract(clip[0]);
               }

               g2d.setColor(this.bpsStart.color);
               g2d.fill(border);
               g2d.fill(cornerBorder[3]);
            }

         }

         public Dimension getImageSize() {
            return this.borderRect.getSize();
         }

         // $FF: synthetic method
         BorderImagePainter(double x1, Rectangle x2, BorderProps x3, BorderProps x4, BorderProps x5, BorderProps x6, boolean[] x7, Color x8, Object x9) {
            this(x1, x2, x3, x4, x5, x6, x7, x8);
         }
      }
   }

   private static final class AFPGraphicsPainter implements GraphicsPainter {
      private final AFPBorderPainter graphicsPainter;

      private AFPGraphicsPainter(AFPBorderPainter delegate) {
         this.graphicsPainter = delegate;
      }

      public void drawBorderLine(int x1, int y1, int x2, int y2, boolean horz, boolean startOrBefore, int style, Color color) throws IOException {
         BorderPaintingInfo borderPaintInfo = new BorderPaintingInfo(this.toPoints(x1), this.toPoints(y1), this.toPoints(x2), this.toPoints(y2), horz, style, color);
         this.graphicsPainter.paint(borderPaintInfo);
      }

      private float toPoints(int mpt) {
         return (float)mpt / 1000.0F;
      }

      public void drawLine(Point start, Point end, int width, Color color, RuleStyle style) throws IOException {
         if (start.y != end.y) {
            throw new UnsupportedOperationException("Can only deal with horizontal lines right now");
         } else {
            int halfWidth = width / 2;
            this.drawBorderLine(start.x, start.y - halfWidth, end.x, start.y + halfWidth, true, true, style.getEnumValue(), color);
         }
      }

      public void moveTo(int x, int y) throws IOException {
      }

      public void lineTo(int x, int y) throws IOException {
      }

      public void arcTo(double startAngle, double endAngle, int cx, int cy, int width, int height) throws IOException {
      }

      public void rotateCoordinates(double angle) throws IOException {
         throw new UnsupportedOperationException("Cannot handle coordinate rotation");
      }

      public void translateCoordinates(int xTranslate, int yTranslate) throws IOException {
         throw new UnsupportedOperationException("Cannot handle coordinate translation");
      }

      public void scaleCoordinates(float xScale, float yScale) throws IOException {
         throw new UnsupportedOperationException("Cannot handle coordinate scaling");
      }

      public void closePath() throws IOException {
      }

      public void clip() throws IOException {
      }

      public void saveGraphicsState() throws IOException {
      }

      public void restoreGraphicsState() throws IOException {
      }

      // $FF: synthetic method
      AFPGraphicsPainter(AFPBorderPainter x0, Object x1) {
         this(x0);
      }
   }
}
