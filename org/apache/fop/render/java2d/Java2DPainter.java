package org.apache.fop.render.java2d;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.Stack;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.render.RenderingContext;
import org.apache.fop.render.intermediate.AbstractIFPainter;
import org.apache.fop.render.intermediate.BorderPainter;
import org.apache.fop.render.intermediate.GraphicsPainter;
import org.apache.fop.render.intermediate.IFContext;
import org.apache.fop.render.intermediate.IFDocumentHandler;
import org.apache.fop.render.intermediate.IFException;
import org.apache.fop.render.intermediate.IFState;
import org.apache.fop.traits.BorderProps;
import org.apache.fop.traits.RuleStyle;
import org.apache.fop.util.CharUtilities;
import org.w3c.dom.Document;

public class Java2DPainter extends AbstractIFPainter {
   protected IFContext ifContext;
   protected FontInfo fontInfo;
   private final GraphicsPainter graphicsPainter;
   private final BorderPainter borderPainter;
   protected Java2DGraphicsState g2dState;
   private Stack g2dStateStack;

   public Java2DPainter(Graphics2D g2d, IFContext context, FontInfo fontInfo) {
      this(g2d, context, fontInfo, (IFDocumentHandler)(new Java2DDocumentHandler()));
   }

   public Java2DPainter(Graphics2D g2d, IFContext context, FontInfo fontInfo, IFDocumentHandler documentHandler) {
      this(g2d, context, fontInfo, (IFState)null, documentHandler);
   }

   public Java2DPainter(Graphics2D g2d, IFContext context, FontInfo fontInfo, IFState state) {
      this(g2d, context, fontInfo, state, new Java2DDocumentHandler());
   }

   public Java2DPainter(Graphics2D g2d, IFContext context, FontInfo fontInfo, IFState state, IFDocumentHandler documentHandler) {
      super(documentHandler);
      this.g2dStateStack = new Stack();
      this.ifContext = context;
      if (state != null) {
         this.state = state.push();
      } else {
         this.state = IFState.create();
      }

      this.fontInfo = fontInfo;
      this.g2dState = new Java2DGraphicsState(g2d, fontInfo, g2d.getTransform());
      this.graphicsPainter = new Java2DGraphicsPainter(this);
      this.borderPainter = new BorderPainter(this.graphicsPainter);
   }

   public IFContext getContext() {
      return this.ifContext;
   }

   protected FontInfo getFontInfo() {
      return this.fontInfo;
   }

   protected Java2DGraphicsState getState() {
      return this.g2dState;
   }

   public void startViewport(AffineTransform transform, Dimension size, Rectangle clipRect) throws IFException {
      this.saveGraphicsState();

      try {
         this.concatenateTransformationMatrix(transform);
         if (clipRect != null) {
            this.clipRect(clipRect);
         }

      } catch (IOException var5) {
         throw new IFException("I/O error in startViewport()", var5);
      }
   }

   public void endViewport() throws IFException {
      this.restoreGraphicsState();
   }

   public void startGroup(AffineTransform transform, String layer) throws IFException {
      this.saveGraphicsState();

      try {
         this.concatenateTransformationMatrix(transform);
      } catch (IOException var4) {
         throw new IFException("I/O error in startGroup()", var4);
      }
   }

   public void endGroup() throws IFException {
      this.restoreGraphicsState();
   }

   public void drawImage(String uri, Rectangle rect) throws IFException {
      this.drawImageUsingURI(uri, rect);
   }

   protected RenderingContext createRenderingContext() {
      Java2DRenderingContext java2dContext = new Java2DRenderingContext(this.getUserAgent(), this.g2dState.getGraph(), this.getFontInfo());
      return java2dContext;
   }

   public void drawImage(Document doc, Rectangle rect) throws IFException {
      this.drawImageUsingDocument(doc, rect);
   }

   public void clipRect(Rectangle rect) throws IFException {
      this.getState().updateClip(rect);
   }

   public void clipBackground(Rectangle rect, BorderProps bpsBefore, BorderProps bpsAfter, BorderProps bpsStart, BorderProps bpsEnd) throws IFException {
   }

   public void fillRect(Rectangle rect, Paint fill) throws IFException {
      if (fill != null) {
         if (rect.width != 0 && rect.height != 0) {
            this.g2dState.updatePaint(fill);
            this.g2dState.getGraph().fill(rect);
         }

      }
   }

   public void drawBorderRect(Rectangle rect, BorderProps top, BorderProps bottom, BorderProps left, BorderProps right) throws IFException {
      if (top != null || bottom != null || left != null || right != null) {
         this.borderPainter.drawBorders(rect, top, bottom, left, right, (Color)null);
      }

   }

   public void drawLine(Point start, Point end, int width, Color color, RuleStyle style) throws IFException {
      try {
         this.graphicsPainter.drawLine(start, end, width, color, style);
      } catch (IOException var7) {
         throw new IFException("Unexpected error drawing line", var7);
      }
   }

   public void drawText(int x, int y, int letterSpacing, int wordSpacing, int[][] dp, String text) throws IFException {
      this.g2dState.updateColor(this.state.getTextColor());
      FontTriplet triplet = new FontTriplet(this.state.getFontFamily(), this.state.getFontStyle(), this.state.getFontWeight());
      Font font = this.getFontInfo().getFontInstance(triplet, this.state.getFontSize());
      this.g2dState.updateFont(font.getFontName(), this.state.getFontSize() * 1000);
      Graphics2D g2d = this.g2dState.getGraph();
      GlyphVector gv = Java2DUtil.createGlyphVector(text, g2d, font, this.fontInfo);
      Point2D cursor = new Point2D.Float(0.0F, 0.0F);
      int l = text.length();
      if (dp != null && dp[0] != null && (dp[0][0] != 0 || dp[0][1] != 0)) {
         cursor.setLocation(cursor.getX() + (double)dp[0][0], cursor.getY() - (double)dp[0][1]);
         gv.setGlyphPosition(0, cursor);
      }

      int currentIdx = 0;

      for(int i = 0; i < l; ++i) {
         int orgChar = text.codePointAt(i);
         i += CharUtilities.incrementIfNonBMP(orgChar);
         float xGlyphAdjust = 0.0F;
         float yGlyphAdjust = 0.0F;
         int cw = font.getCharWidth(orgChar);
         if (wordSpacing != 0 && CharUtilities.isAdjustableSpace(orgChar)) {
            xGlyphAdjust += (float)wordSpacing;
         }

         xGlyphAdjust += (float)letterSpacing;
         if (dp != null && i < dp.length && dp[i] != null) {
            xGlyphAdjust += (float)(dp[i][2] - dp[i][0]);
            yGlyphAdjust += (float)(dp[i][3] - dp[i][1]);
         }

         if (dp != null && i < dp.length - 1 && dp[i + 1] != null) {
            xGlyphAdjust += (float)dp[i + 1][0];
            yGlyphAdjust += (float)dp[i + 1][1];
         }

         cursor.setLocation(cursor.getX() + (double)cw + (double)xGlyphAdjust, cursor.getY() - (double)yGlyphAdjust);
         ++currentIdx;
         gv.setGlyphPosition(currentIdx, cursor);
      }

      g2d.drawGlyphVector(gv, (float)x, (float)y);
   }

   protected void saveGraphicsState() {
      this.g2dStateStack.push(this.g2dState);
      this.g2dState = new Java2DGraphicsState(this.g2dState);
   }

   protected void restoreGraphicsState() {
      this.g2dState.dispose();
      this.g2dState = (Java2DGraphicsState)this.g2dStateStack.pop();
   }

   private void concatenateTransformationMatrix(AffineTransform transform) throws IOException {
      this.g2dState.transform(transform);
   }
}
