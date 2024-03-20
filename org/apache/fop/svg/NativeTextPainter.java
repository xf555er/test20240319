package org.apache.fop.svg;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.batik.bridge.FontFamilyResolver;
import org.apache.batik.bridge.GlyphLayout;
import org.apache.batik.bridge.SVGGVTFont;
import org.apache.batik.bridge.StrokingTextPainter;
import org.apache.batik.bridge.TextLayoutFactory;
import org.apache.batik.bridge.TextNode;
import org.apache.batik.bridge.TextSpanLayout;
import org.apache.batik.gvt.font.GVTGlyphVector;
import org.apache.batik.gvt.text.TextPaintInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.svg.font.FOPFontFamilyResolverImpl;
import org.apache.fop.svg.font.FOPGVTFont;
import org.apache.fop.svg.font.FOPGVTGlyphVector;
import org.apache.fop.svg.text.BidiAttributedCharacterIterator;
import org.apache.fop.svg.text.ComplexGlyphLayout;
import org.apache.fop.util.CharUtilities;

public abstract class NativeTextPainter extends StrokingTextPainter {
   protected static final Log log = LogFactory.getLog(NativeTextPainter.class);
   private static final boolean DEBUG = false;
   protected final FontInfo fontInfo;
   protected final FontFamilyResolver fontFamilyResolver;
   protected Font font;
   protected TextPaintInfo tpi;
   private static final TextLayoutFactory COMPLEX_SCRIPT_TEXT_LAYOUT_FACTORY = new TextLayoutFactory() {
      public TextSpanLayout createTextLayout(AttributedCharacterIterator aci, int[] charMap, Point2D offset, FontRenderContext frc) {
         return (TextSpanLayout)(ComplexGlyphLayout.mayRequireComplexLayout(aci) ? new ComplexGlyphLayout(aci, charMap, offset, frc) : new GlyphLayout(aci, charMap, offset, frc));
      }
   };

   public NativeTextPainter(FontInfo fontInfo) {
      this.fontInfo = fontInfo;
      this.fontFamilyResolver = new FOPFontFamilyResolverImpl(fontInfo);
   }

   protected abstract boolean isSupported(Graphics2D var1);

   protected final void paintTextRun(StrokingTextPainter.TextRun textRun, Graphics2D g2d) throws IOException {
      this.logTextRun(textRun);
      AttributedCharacterIterator runaci = textRun.getACI();
      runaci.first();
      this.tpi = (TextPaintInfo)runaci.getAttribute(PAINT_INFO);
      if (this.tpi != null && this.tpi.visible) {
         if (this.tpi.composite != null) {
            g2d.setComposite(this.tpi.composite);
         }

         TextSpanLayout layout = textRun.getLayout();
         GVTGlyphVector gv = layout.getGlyphVector();
         if (!(gv.getFont() instanceof FOPGVTFont)) {
            assert gv.getFont() == null || gv.getFont() instanceof SVGGVTFont;

            textRun.getLayout().draw(g2d);
         } else {
            GeneralPath debugShapes = new GeneralPath();
            this.preparePainting(g2d);
            this.saveGraphicsState();
            this.setInitialTransform(g2d.getTransform());
            this.clip(g2d.getClip());
            this.beginTextObject();
            this.writeGlyphs((FOPGVTGlyphVector)gv, debugShapes);
            this.endTextObject();
            this.restoreGraphicsState();
         }
      }
   }

   protected void writeGlyphs(FOPGVTGlyphVector gv, GeneralPath debugShapes) throws IOException {
      AffineTransform localTransform = new AffineTransform();
      Point2D prevPos = null;
      AffineTransform prevGlyphTransform = null;
      this.font = ((FOPGVTFont)gv.getFont()).getFont();
      int index = 0;

      for(int c = gv.getNumGlyphs(); index < c; ++index) {
         if (gv.isGlyphVisible(index)) {
            Point2D glyphPos = gv.getGlyphPosition(index);
            AffineTransform glyphTransform = gv.getGlyphTransform(index);
            if (log.isTraceEnabled()) {
               log.trace("pos " + glyphPos + ", transform " + glyphTransform);
            }

            localTransform.setToIdentity();
            localTransform.translate(glyphPos.getX(), glyphPos.getY());
            if (glyphTransform != null) {
               localTransform.concatenate(glyphTransform);
            }

            localTransform.scale(1.0, -1.0);
            this.positionGlyph(prevPos, glyphPos, glyphTransform != null || prevGlyphTransform != null);
            char glyph = (char)gv.getGlyphCode(index);
            prevPos = glyphPos;
            prevGlyphTransform = glyphTransform;
            this.writeGlyph(glyph, localTransform);
         }
      }

   }

   protected void paintTextRuns(List textRuns, Graphics2D g2d) {
      if (log.isTraceEnabled()) {
         log.trace("paintTextRuns: count = " + textRuns.size());
      }

      if (!this.isSupported(g2d)) {
         super.paintTextRuns(textRuns, g2d);
      } else {
         Iterator var3 = textRuns.iterator();

         while(var3.hasNext()) {
            Object textRun1 = var3.next();
            StrokingTextPainter.TextRun textRun = (StrokingTextPainter.TextRun)textRun1;

            try {
               this.paintTextRun(textRun, g2d);
            } catch (IOException var7) {
               throw new RuntimeException(var7);
            }
         }

      }
   }

   protected CharSequence collectCharacters(AttributedCharacterIterator runaci) {
      StringBuffer chars = new StringBuffer();
      runaci.first();

      while(runaci.getIndex() < runaci.getEndIndex()) {
         chars.append(runaci.current());
         runaci.next();
      }

      return chars;
   }

   public List computeTextRuns(TextNode node, AttributedCharacterIterator nodeACI, AttributedCharacterIterator[] chunkACIs) {
      nodeACI.first();
      int defaultBidiLevel = (Integer)nodeACI.getAttribute(WRITING_MODE) == WRITING_MODE_RTL ? 1 : 0;
      int i = 0;

      for(int n = chunkACIs.length; i < n; ++i) {
         chunkACIs[i] = new BidiAttributedCharacterIterator(chunkACIs[i], defaultBidiLevel);
      }

      return super.computeTextRuns(node, nodeACI, chunkACIs, (int[][])null);
   }

   protected Set getTextRunBoundaryAttributes() {
      Set textRunBoundaryAttributes = super.getTextRunBoundaryAttributes();
      if (!textRunBoundaryAttributes.contains(BIDI_LEVEL)) {
         textRunBoundaryAttributes.add(BIDI_LEVEL);
      }

      return textRunBoundaryAttributes;
   }

   protected List reorderTextRuns(StrokingTextPainter.TextChunk chunk, List runs) {
      int mn = -1;
      int mx = -1;
      Iterator var5 = runs.iterator();

      while(true) {
         int level;
         do {
            do {
               if (!var5.hasNext()) {
                  if (mx > 0) {
                     int l1 = mx;

                     for(int l2 = (mn & 1) == 0 ? mn + 1 : mn; l1 >= l2; --l1) {
                        runs = this.reorderRuns(runs, l1);
                     }
                  }

                  boolean mirror = true;
                  this.reverseGlyphs(runs, mirror);
                  return runs;
               }

               StrokingTextPainter.TextRun r = (StrokingTextPainter.TextRun)var5.next();
               level = r.getBidiLevel();
            } while(level < 0);

            if (mn < 0 || level < mn) {
               mn = level;
            }
         } while(mx >= 0 && level <= mx);

         mx = level;
      }
   }

   private List reorderRuns(List runs, int level) {
      assert level >= 0;

      List runsNew = new ArrayList();
      int i = 0;

      for(int n = ((List)runs).size(); i < n; ++i) {
         StrokingTextPainter.TextRun tri = (StrokingTextPainter.TextRun)((List)runs).get(i);
         if (tri.getBidiLevel() < level) {
            runsNew.add(tri);
         } else {
            int e;
            for(e = i; e < n; ++e) {
               StrokingTextPainter.TextRun tre = (StrokingTextPainter.TextRun)((List)runs).get(e);
               if (tre.getBidiLevel() < level) {
                  break;
               }
            }

            if (i < e) {
               runsNew.addAll(this.reverseRuns((List)runs, i, e));
            }

            i = e - 1;
         }
      }

      if (!runsNew.equals(runs)) {
         runs = runsNew;
      }

      return (List)runs;
   }

   private List reverseRuns(List runs, int s, int e) {
      int n = e - s;
      List runsNew = new ArrayList(n);
      if (n > 0) {
         for(int i = 0; i < n; ++i) {
            int k = n - i - 1;
            StrokingTextPainter.TextRun tr = (StrokingTextPainter.TextRun)runs.get(s + k);
            tr.reverse();
            runsNew.add(tr);
         }
      }

      return runsNew;
   }

   private void reverseGlyphs(List runs, boolean mirror) {
      Iterator var3 = runs.iterator();

      while(var3.hasNext()) {
         StrokingTextPainter.TextRun r = (StrokingTextPainter.TextRun)var3.next();
         r.maybeReverseGlyphs(mirror);
      }

   }

   protected abstract void preparePainting(Graphics2D var1);

   protected abstract void saveGraphicsState() throws IOException;

   protected abstract void restoreGraphicsState() throws IOException;

   protected abstract void setInitialTransform(AffineTransform var1) throws IOException;

   protected abstract void clip(Shape var1) throws IOException;

   protected abstract void beginTextObject() throws IOException;

   protected abstract void endTextObject() throws IOException;

   protected abstract void positionGlyph(Point2D var1, Point2D var2, boolean var3);

   protected abstract void writeGlyph(char var1, AffineTransform var2) throws IOException;

   protected final void logTextRun(StrokingTextPainter.TextRun textRun) {
      AttributedCharacterIterator runaci = textRun.getACI();
      TextSpanLayout layout = textRun.getLayout();
      runaci.first();
      if (log.isTraceEnabled()) {
         int charCount = runaci.getEndIndex() - runaci.getBeginIndex();
         log.trace("================================================");
         log.trace("New text run:");
         log.trace("char count: " + charCount);
         log.trace("range: " + runaci.getBeginIndex() + " - " + runaci.getEndIndex());
         log.trace("glyph count: " + layout.getGlyphCount());
      }

   }

   protected final void logCharacter(char ch, TextSpanLayout layout, int index, boolean visibleChar) {
      if (log.isTraceEnabled()) {
         log.trace("glyph " + index + " -> " + layout.getGlyphIndex(index) + " => " + ch);
         if (CharUtilities.isAnySpace(ch) && ch != ' ') {
            log.trace("Space found: " + Integer.toHexString(ch));
         } else if (ch == 8205) {
            log.trace("ZWJ found: " + Integer.toHexString(ch));
         } else if (ch == 173) {
            log.trace("Soft hyphen found: " + Integer.toHexString(ch));
         }

         if (!visibleChar) {
            log.trace("Invisible glyph found: " + Integer.toHexString(ch));
         }
      }

   }

   protected FontFamilyResolver getFontFamilyResolver() {
      return this.fontFamilyResolver;
   }

   protected TextLayoutFactory getTextLayoutFactory() {
      return COMPLEX_SCRIPT_TEXT_LAYOUT_FACTORY;
   }
}
