package org.apache.batik.bridge;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.CharacterIterator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.batik.gvt.font.GVTFont;
import org.apache.batik.gvt.font.GVTFontFamily;
import org.apache.batik.gvt.font.GVTGlyphMetrics;
import org.apache.batik.gvt.font.GVTLineMetrics;
import org.apache.batik.gvt.text.AttributedCharacterSpanIterator;
import org.apache.batik.gvt.text.BidiAttributedCharacterIterator;
import org.apache.batik.gvt.text.GVTAttributedCharacterIterator;
import org.apache.batik.gvt.text.TextPaintInfo;
import org.apache.batik.gvt.text.TextPath;

public class StrokingTextPainter extends BasicTextPainter {
   public static final AttributedCharacterIterator.Attribute PAINT_INFO;
   public static final AttributedCharacterIterator.Attribute FLOW_REGIONS;
   public static final AttributedCharacterIterator.Attribute FLOW_PARAGRAPH;
   public static final AttributedCharacterIterator.Attribute TEXT_COMPOUND_ID;
   public static final AttributedCharacterIterator.Attribute GVT_FONT;
   public static final AttributedCharacterIterator.Attribute GVT_FONTS;
   public static final AttributedCharacterIterator.Attribute BIDI_LEVEL;
   public static final AttributedCharacterIterator.Attribute XPOS;
   public static final AttributedCharacterIterator.Attribute YPOS;
   public static final AttributedCharacterIterator.Attribute TEXTPATH;
   public static final AttributedCharacterIterator.Attribute WRITING_MODE;
   public static final Integer WRITING_MODE_TTB;
   public static final Integer WRITING_MODE_RTL;
   public static final AttributedCharacterIterator.Attribute ANCHOR_TYPE;
   public static final Integer ADJUST_SPACING;
   public static final Integer ADJUST_ALL;
   public static final GVTAttributedCharacterIterator.TextAttribute ALT_GLYPH_HANDLER;
   static Set extendedAtts;
   protected static TextPainter singleton;

   public static TextPainter getInstance() {
      return singleton;
   }

   public void paint(TextNode node, Graphics2D g2d) {
      AttributedCharacterIterator aci = node.getAttributedCharacterIterator();
      if (aci != null) {
         List textRuns = this.getTextRuns(node, aci);
         this.paintDecorations(textRuns, g2d, 1);
         this.paintDecorations(textRuns, g2d, 4);
         this.paintTextRuns(textRuns, g2d);
         this.paintDecorations(textRuns, g2d, 2);
      }
   }

   protected void printAttrs(AttributedCharacterIterator aci) {
      aci.first();
      int start = aci.getBeginIndex();
      System.out.print("AttrRuns: ");

      while(aci.current() != '\uffff') {
         int end = aci.getRunLimit();
         System.out.print("" + (end - start) + ", ");
         aci.setIndex(end);
         start = end;
      }

      System.out.println("");
   }

   public List getTextRuns(TextNode node, AttributedCharacterIterator aci) {
      List textRuns = node.getTextRuns();
      if (textRuns != null) {
         return textRuns;
      } else {
         AttributedCharacterIterator[] chunkACIs = this.getTextChunkACIs(aci);
         textRuns = this.computeTextRuns(node, aci, chunkACIs);
         node.setTextRuns(textRuns);
         return node.getTextRuns();
      }
   }

   public List computeTextRuns(TextNode node, AttributedCharacterIterator aci, AttributedCharacterIterator[] chunkACIs) {
      int[][] chunkCharMaps = new int[chunkACIs.length][];
      int chunkStart = aci.getBeginIndex();

      for(int i = 0; i < chunkACIs.length; ++i) {
         BidiAttributedCharacterIterator iter = new BidiAttributedCharacterIterator(chunkACIs[i], this.fontRenderContext, chunkStart);
         chunkACIs[i] = iter;
         chunkCharMaps[i] = iter.getCharMap();
         chunkStart += chunkACIs[i].getEndIndex() - chunkACIs[i].getBeginIndex();
      }

      return this.computeTextRuns(node, aci, chunkACIs, chunkCharMaps);
   }

   protected List computeTextRuns(TextNode node, AttributedCharacterIterator aci, AttributedCharacterIterator[] chunkACIs, int[][] chunkCharMaps) {
      int chunkStart = aci.getBeginIndex();

      for(int i = 0; i < chunkACIs.length; ++i) {
         chunkACIs[i] = this.createModifiedACIForFontMatching(chunkACIs[i]);
         chunkStart += chunkACIs[i].getEndIndex() - chunkACIs[i].getBeginIndex();
      }

      List perNodeRuns = new ArrayList();
      TextChunk prevChunk = null;
      int currentChunk = 0;
      Point2D location = node.getLocation();

      TextChunk chunk;
      do {
         chunkACIs[currentChunk].first();
         List perChunkRuns = new ArrayList();
         chunk = this.getTextChunk(node, chunkACIs[currentChunk], chunkCharMaps != null ? chunkCharMaps[currentChunk] : null, perChunkRuns, prevChunk);
         List perChunkRuns = this.reorderTextRuns(chunk, perChunkRuns);
         chunkACIs[currentChunk].first();
         if (chunk != null) {
            location = this.adjustChunkOffsets(location, perChunkRuns, chunk);
         }

         perNodeRuns.addAll(perChunkRuns);
         prevChunk = chunk;
         ++currentChunk;
      } while(chunk != null && currentChunk < chunkACIs.length);

      return perNodeRuns;
   }

   protected List reorderTextRuns(TextChunk chunk, List runs) {
      return runs;
   }

   protected AttributedCharacterIterator[] getTextChunkACIs(AttributedCharacterIterator aci) {
      List aciList = new ArrayList();
      int chunkStartIndex = aci.getBeginIndex();
      aci.first();
      Object writingMode = aci.getAttribute(WRITING_MODE);

      int start;
      int end;
      for(boolean vertical = writingMode == WRITING_MODE_TTB; aci.setIndex(chunkStartIndex) != '\uffff'; chunkStartIndex = start) {
         TextPath prevTextPath = null;
         start = chunkStartIndex;

         for(int end = false; aci.setIndex(start) != '\uffff'; start = end) {
            TextPath textPath = (TextPath)aci.getAttribute(TEXTPATH);
            if (start != chunkStartIndex) {
               Float runY;
               if (vertical) {
                  runY = (Float)aci.getAttribute(YPOS);
                  if (runY != null && !runY.isNaN()) {
                     break;
                  }
               } else {
                  runY = (Float)aci.getAttribute(XPOS);
                  if (runY != null && !runY.isNaN()) {
                     break;
                  }
               }

               if (prevTextPath == null && textPath != null || prevTextPath != null && textPath == null) {
                  break;
               }
            }

            prevTextPath = textPath;
            if (aci.getAttribute(FLOW_PARAGRAPH) != null) {
               end = aci.getRunLimit(FLOW_PARAGRAPH);
               aci.setIndex(end);
               break;
            }

            end = aci.getRunLimit(TEXT_COMPOUND_ID);
            if (start == chunkStartIndex) {
               TextNode.Anchor anchor = (TextNode.Anchor)aci.getAttribute(ANCHOR_TYPE);
               if (anchor != TextNode.Anchor.START) {
                  Float runY;
                  if (vertical) {
                     runY = (Float)aci.getAttribute(YPOS);
                     if (runY == null || runY.isNaN()) {
                        continue;
                     }
                  } else {
                     runY = (Float)aci.getAttribute(XPOS);
                     if (runY == null || runY.isNaN()) {
                        continue;
                     }
                  }

                  for(int i = start + 1; i < end; chunkStartIndex = i++) {
                     aci.setIndex(i);
                     Float runY;
                     if (vertical) {
                        runY = (Float)aci.getAttribute(YPOS);
                        if (runY == null || runY.isNaN()) {
                           break;
                        }
                     } else {
                        runY = (Float)aci.getAttribute(XPOS);
                        if (runY == null || runY.isNaN()) {
                           break;
                        }
                     }

                     aciList.add(new AttributedCharacterSpanIterator(aci, i - 1, i));
                  }
               }
            }
         }

         start = aci.getIndex();
         aciList.add(new AttributedCharacterSpanIterator(aci, chunkStartIndex, start));
      }

      AttributedCharacterIterator[] aciArray = new AttributedCharacterIterator[aciList.size()];
      Iterator iter = aciList.iterator();

      for(end = 0; iter.hasNext(); ++end) {
         aciArray[end] = (AttributedCharacterIterator)iter.next();
      }

      return aciArray;
   }

   protected AttributedCharacterIterator createModifiedACIForFontMatching(AttributedCharacterIterator aci) {
      aci.first();
      AttributedString as = null;
      int asOff = 0;
      int begin = aci.getBeginIndex();
      boolean moreChunks = true;
      int end = aci.getRunStart(TEXT_COMPOUND_ID);

      while(moreChunks) {
         int start = end;
         end = aci.getRunLimit(TEXT_COMPOUND_ID);
         int aciLength = end - start;
         List fonts = (List)aci.getAttribute(GVT_FONTS);
         float fontSize = 12.0F;
         Float fsFloat = (Float)aci.getAttribute(TextAttribute.SIZE);
         if (fsFloat != null) {
            fontSize = fsFloat;
         }

         if (fonts.size() == 0) {
            fonts.add(this.getFontFamilyResolver().getDefault().deriveFont(fontSize, aci));
         }

         boolean[] fontAssigned = new boolean[aciLength];
         if (as == null) {
            as = new AttributedString(aci);
         }

         GVTFont defaultFont = null;
         int numSet = 0;
         int firstUnset = start;
         Iterator var17 = fonts.iterator();

         label117:
         while(var17.hasNext()) {
            Object font1 = var17.next();
            int currentIndex = firstUnset;
            boolean firstUnsetSet = false;
            aci.setIndex(firstUnset);
            GVTFont font = (GVTFont)font1;
            if (defaultFont == null) {
               defaultFont = font;
            }

            while(true) {
               while(currentIndex < end) {
                  int displayUpToIndex = font.canDisplayUpTo((CharacterIterator)aci, currentIndex, end);
                  Object altGlyphElement = aci.getAttribute(ALT_GLYPH_HANDLER);
                  if (altGlyphElement != null) {
                     displayUpToIndex = -1;
                  }

                  if (displayUpToIndex == -1) {
                     displayUpToIndex = end;
                  }

                  if (displayUpToIndex <= currentIndex) {
                     if (!firstUnsetSet) {
                        firstUnset = currentIndex;
                        firstUnsetSet = true;
                     }

                     ++currentIndex;
                  } else {
                     int runStart = -1;

                     for(int j = currentIndex; j < displayUpToIndex; ++j) {
                        if (fontAssigned[j - start]) {
                           if (runStart != -1) {
                              as.addAttribute(GVT_FONT, font, runStart - begin, j - begin);
                              runStart = -1;
                           }
                        } else if (runStart == -1) {
                           runStart = j;
                        }

                        fontAssigned[j - start] = true;
                        ++numSet;
                     }

                     if (runStart != -1) {
                        as.addAttribute(GVT_FONT, font, runStart - begin, displayUpToIndex - begin);
                     }

                     currentIndex = displayUpToIndex + 1;
                  }
               }

               if (numSet == aciLength) {
                  break label117;
               }
               break;
            }
         }

         int runStart = -1;
         GVTFontFamily prevFF = null;
         GVTFont prevF = defaultFont;

         for(int i = 0; i < aciLength; ++i) {
            if (fontAssigned[i]) {
               if (runStart != -1) {
                  as.addAttribute(GVT_FONT, prevF, runStart + asOff, i + asOff);
                  runStart = -1;
                  prevF = null;
                  prevFF = null;
               }
            } else {
               char c = aci.setIndex(start + i);
               GVTFontFamily fontFamily = this.getFontFamilyResolver().getFamilyThatCanDisplay(c);
               if (runStart == -1) {
                  runStart = i;
                  prevFF = fontFamily;
                  if (fontFamily == null) {
                     prevF = defaultFont;
                  } else {
                     prevF = fontFamily.deriveFont(fontSize, aci);
                  }
               } else if (prevFF != fontFamily) {
                  as.addAttribute(GVT_FONT, prevF, runStart + asOff, i + asOff);
                  runStart = i;
                  prevFF = fontFamily;
                  if (fontFamily == null) {
                     prevF = defaultFont;
                  } else {
                     prevF = fontFamily.deriveFont(fontSize, aci);
                  }
               }
            }
         }

         if (runStart != -1) {
            as.addAttribute(GVT_FONT, prevF, runStart + asOff, aciLength + asOff);
         }

         asOff += aciLength;
         if (aci.setIndex(end) == '\uffff') {
            moreChunks = false;
         }
      }

      if (as != null) {
         return as.getIterator();
      } else {
         return aci;
      }
   }

   protected FontFamilyResolver getFontFamilyResolver() {
      return DefaultFontFamilyResolver.SINGLETON;
   }

   protected Set getTextRunBoundaryAttributes() {
      return extendedAtts;
   }

   protected TextChunk getTextChunk(TextNode node, AttributedCharacterIterator aci, int[] charMap, List textRuns, TextChunk prevChunk) {
      int beginChunk = 0;
      if (prevChunk != null) {
         beginChunk = prevChunk.end;
      }

      int endChunk = beginChunk;
      int begin = aci.getIndex();
      if (aci.current() == '\uffff') {
         return null;
      } else {
         Point2D.Float offset = new Point2D.Float(0.0F, 0.0F);
         Point2D.Float advance = new Point2D.Float(0.0F, 0.0F);
         boolean isChunkStart = true;
         TextSpanLayout layout = null;
         Set textRunBoundaryAttributes = this.getTextRunBoundaryAttributes();

         while(true) {
            int start = aci.getRunStart(textRunBoundaryAttributes);
            int end = aci.getRunLimit(textRunBoundaryAttributes);
            AttributedCharacterIterator runaci = new AttributedCharacterSpanIterator(aci, start, end);
            int[] subCharMap = new int[end - start];
            if (charMap != null) {
               System.arraycopy(charMap, start - begin, subCharMap, 0, subCharMap.length);
            } else {
               int i = 0;

               for(int n = subCharMap.length; i < n; subCharMap[i] = i++) {
               }
            }

            FontRenderContext frc = this.fontRenderContext;
            RenderingHints rh = node.getRenderingHints();
            if (rh != null && rh.get(RenderingHints.KEY_TEXT_ANTIALIASING) == RenderingHints.VALUE_TEXT_ANTIALIAS_OFF) {
               frc = this.aaOffFontRenderContext;
            }

            layout = this.getTextLayoutFactory().createTextLayout(runaci, subCharMap, offset, frc);
            textRuns.add(new TextRun(layout, runaci, isChunkStart));
            Point2D layoutAdvance = layout.getAdvance2D();
            advance.x += (float)layoutAdvance.getX();
            advance.y += (float)layoutAdvance.getY();
            ++endChunk;
            if (aci.setIndex(end) == '\uffff') {
               return new TextChunk(beginChunk, endChunk, advance);
            }

            isChunkStart = false;
         }
      }
   }

   protected Point2D adjustChunkOffsets(Point2D location, List textRuns, TextChunk chunk) {
      int numRuns = chunk.end - chunk.begin;
      TextRun r = (TextRun)textRuns.get(0);
      int anchorType = r.getAnchorType();
      Float length = r.getLength();
      Integer lengthAdj = r.getLengthAdjust();
      boolean doAdjust = true;
      if (length == null || length.isNaN()) {
         doAdjust = false;
      }

      int numChars = 0;

      for(int i = 0; i < numRuns; ++i) {
         r = (TextRun)textRuns.get(i);
         AttributedCharacterIterator aci = r.getACI();
         numChars += aci.getEndIndex() - aci.getBeginIndex();
      }

      if (lengthAdj == GVTAttributedCharacterIterator.TextAttribute.ADJUST_SPACING && numChars == 1) {
         doAdjust = false;
      }

      float xScale = 1.0F;
      float yScale = 1.0F;
      r = (TextRun)textRuns.get(numRuns - 1);
      TextSpanLayout layout = r.getLayout();
      GVTGlyphMetrics lastMetrics = layout.getGlyphMetrics(layout.getGlyphCount() - 1);
      GVTLineMetrics lastLineMetrics = layout.getLineMetrics();
      Rectangle2D lastBounds = lastMetrics.getBounds2D();
      float halfLeading = (lastMetrics.getVerticalAdvance() - (lastLineMetrics.getAscent() + lastLineMetrics.getDescent())) / 2.0F;
      float lastW = (float)(lastBounds.getWidth() + lastBounds.getX());
      float lastH = (float)((double)(halfLeading + lastLineMetrics.getAscent()) + lastBounds.getHeight() + lastBounds.getY());
      Point2D.Float visualAdvance;
      if (!doAdjust) {
         visualAdvance = new Point2D.Float((float)chunk.advance.getX(), (float)(chunk.advance.getY() + (double)lastH - (double)lastMetrics.getVerticalAdvance()));
      } else {
         Point2D advance = chunk.advance;
         double adv;
         if (layout.isVertical()) {
            if (lengthAdj == ADJUST_SPACING) {
               yScale = (float)((double)(length - lastH) / (advance.getY() - (double)lastMetrics.getVerticalAdvance()));
            } else {
               adv = advance.getY() + (double)lastH - (double)lastMetrics.getVerticalAdvance();
               yScale = (float)((double)length / adv);
            }

            visualAdvance = new Point2D.Float(0.0F, length);
         } else {
            if (lengthAdj == ADJUST_SPACING) {
               xScale = (float)((double)(length - lastW) / (advance.getX() - (double)lastMetrics.getHorizontalAdvance()));
            } else {
               adv = advance.getX() + (double)lastW - (double)lastMetrics.getHorizontalAdvance();
               xScale = (float)((double)length / adv);
            }

            visualAdvance = new Point2D.Float(length, 0.0F);
         }

         Point2D.Float adv = new Point2D.Float(0.0F, 0.0F);

         for(int i = 0; i < numRuns; ++i) {
            r = (TextRun)textRuns.get(i);
            layout = r.getLayout();
            layout.setScale(xScale, yScale, lengthAdj == ADJUST_SPACING);
            Point2D lAdv = layout.getAdvance2D();
            adv.x += (float)lAdv.getX();
            adv.y += (float)lAdv.getY();
         }

         chunk.advance = adv;
      }

      float dx = 0.0F;
      float dy = 0.0F;
      switch (anchorType) {
         case 1:
            dx = (float)(-visualAdvance.getX() / 2.0);
            dy = (float)(-visualAdvance.getY() / 2.0);
            break;
         case 2:
            dx = (float)(-visualAdvance.getX());
            dy = (float)(-visualAdvance.getY());
      }

      r = (TextRun)textRuns.get(0);
      layout = r.getLayout();
      AttributedCharacterIterator runaci = r.getACI();
      runaci.first();
      boolean vertical = layout.isVertical();
      Float runX = (Float)runaci.getAttribute(XPOS);
      Float runY = (Float)runaci.getAttribute(YPOS);
      TextPath textPath = (TextPath)runaci.getAttribute(TEXTPATH);
      float absX = (float)location.getX();
      float absY = (float)location.getY();
      float tpShiftX = 0.0F;
      float tpShiftY = 0.0F;
      if (runX != null && !runX.isNaN()) {
         absX = runX;
         tpShiftX = absX;
      }

      if (runY != null && !runY.isNaN()) {
         absY = runY;
         tpShiftY = absY;
      }

      if (vertical) {
         absY += dy;
         tpShiftY += dy;
         tpShiftX = 0.0F;
      } else {
         absX += dx;
         tpShiftX += dx;
         tpShiftY = 0.0F;
      }

      for(int i = 0; i < numRuns; ++i) {
         r = (TextRun)textRuns.get(i);
         layout = r.getLayout();
         runaci = r.getACI();
         runaci.first();
         textPath = (TextPath)runaci.getAttribute(TEXTPATH);
         if (vertical) {
            runX = (Float)runaci.getAttribute(XPOS);
            if (runX != null && !runX.isNaN()) {
               absX = runX;
            }
         } else {
            runY = (Float)runaci.getAttribute(YPOS);
            if (runY != null && !runY.isNaN()) {
               absY = runY;
            }
         }

         Point2D ladv;
         if (textPath == null) {
            layout.setOffset(new Point2D.Float(absX, absY));
            ladv = layout.getAdvance2D();
            absX = (float)((double)absX + ladv.getX());
            absY = (float)((double)absY + ladv.getY());
         } else {
            layout.setOffset(new Point2D.Float(tpShiftX, tpShiftY));
            ladv = layout.getAdvance2D();
            tpShiftX += (float)ladv.getX();
            tpShiftY += (float)ladv.getY();
            ladv = layout.getTextPathAdvance();
            absX = (float)ladv.getX();
            absY = (float)ladv.getY();
         }
      }

      return new Point2D.Float(absX, absY);
   }

   protected void paintDecorations(List textRuns, Graphics2D g2d, int decorationType) {
      Paint prevPaint = null;
      Paint prevStrokePaint = null;
      Stroke prevStroke = null;
      boolean prevVisible = true;
      Rectangle2D decorationRect = null;
      double yLoc = 0.0;
      double height = 0.0;

      boolean visible;
      for(Iterator var13 = textRuns.iterator(); var13.hasNext(); prevVisible = visible) {
         Object textRun1 = var13.next();
         TextRun textRun = (TextRun)textRun1;
         AttributedCharacterIterator runaci = textRun.getACI();
         runaci.first();
         Paint paint = null;
         Stroke stroke = null;
         Paint strokePaint = null;
         visible = true;
         TextPaintInfo tpi = (TextPaintInfo)runaci.getAttribute(PAINT_INFO);
         if (tpi != null) {
            visible = tpi.visible;
            if (tpi.composite != null) {
               g2d.setComposite(tpi.composite);
            }

            switch (decorationType) {
               case 1:
                  paint = tpi.underlinePaint;
                  stroke = tpi.underlineStroke;
                  strokePaint = tpi.underlineStrokePaint;
                  break;
               case 2:
                  paint = tpi.strikethroughPaint;
                  stroke = tpi.strikethroughStroke;
                  strokePaint = tpi.strikethroughStrokePaint;
                  break;
               case 3:
               default:
                  return;
               case 4:
                  paint = tpi.overlinePaint;
                  stroke = tpi.overlineStroke;
                  strokePaint = tpi.overlineStrokePaint;
            }
         }

         Shape decorationShape;
         Rectangle2D r2d;
         if (textRun.isFirstRunInChunk()) {
            decorationShape = textRun.getLayout().getDecorationOutline(decorationType);
            r2d = decorationShape.getBounds2D();
            yLoc = r2d.getY();
            height = r2d.getHeight();
         }

         if (textRun.isFirstRunInChunk() || paint != prevPaint || stroke != prevStroke || strokePaint != prevStrokePaint || visible != prevVisible) {
            if (prevVisible && decorationRect != null) {
               if (prevPaint != null) {
                  g2d.setPaint(prevPaint);
                  g2d.fill(decorationRect);
               }

               if (prevStroke != null && prevStrokePaint != null) {
                  g2d.setPaint(prevStrokePaint);
                  g2d.setStroke(prevStroke);
                  g2d.draw(decorationRect);
               }
            }

            decorationRect = null;
         }

         if ((paint != null || strokePaint != null) && !textRun.getLayout().isVertical() && !textRun.getLayout().isOnATextPath()) {
            decorationShape = textRun.getLayout().getDecorationOutline(decorationType);
            if (decorationRect == null) {
               r2d = decorationShape.getBounds2D();
               decorationRect = new Rectangle2D.Double(r2d.getX(), yLoc, r2d.getWidth(), height);
            } else {
               r2d = decorationShape.getBounds2D();
               double minX = Math.min(decorationRect.getX(), r2d.getX());
               double maxX = Math.max(decorationRect.getMaxX(), r2d.getMaxX());
               decorationRect.setRect(minX, yLoc, maxX - minX, height);
            }
         }

         prevPaint = paint;
         prevStroke = stroke;
         prevStrokePaint = strokePaint;
      }

      if (prevVisible && decorationRect != null) {
         if (prevPaint != null) {
            g2d.setPaint(prevPaint);
            g2d.fill(decorationRect);
         }

         if (prevStroke != null && prevStrokePaint != null) {
            g2d.setPaint(prevStrokePaint);
            g2d.setStroke(prevStroke);
            g2d.draw(decorationRect);
         }
      }

   }

   protected void paintTextRuns(List textRuns, Graphics2D g2d) {
      TextRun textRun;
      for(Iterator var3 = textRuns.iterator(); var3.hasNext(); textRun.getLayout().draw(g2d)) {
         Object textRun1 = var3.next();
         textRun = (TextRun)textRun1;
         AttributedCharacterIterator runaci = textRun.getACI();
         runaci.first();
         TextPaintInfo tpi = (TextPaintInfo)runaci.getAttribute(PAINT_INFO);
         if (tpi != null && tpi.composite != null) {
            g2d.setComposite(tpi.composite);
         }
      }

   }

   public Shape getOutline(TextNode node) {
      GeneralPath outline = null;
      AttributedCharacterIterator aci = node.getAttributedCharacterIterator();
      if (aci == null) {
         return null;
      } else {
         List textRuns = this.getTextRuns(node, aci);
         Iterator var5 = textRuns.iterator();

         while(var5.hasNext()) {
            Object textRun1 = var5.next();
            TextRun textRun = (TextRun)textRun1;
            TextSpanLayout textRunLayout = textRun.getLayout();
            GeneralPath textRunOutline = new GeneralPath(textRunLayout.getOutline());
            if (outline == null) {
               outline = textRunOutline;
            } else {
               outline.setWindingRule(1);
               outline.append(textRunOutline, false);
            }
         }

         Shape underline = this.getDecorationOutline(textRuns, 1);
         Shape strikeThrough = this.getDecorationOutline(textRuns, 2);
         Shape overline = this.getDecorationOutline(textRuns, 4);
         if (underline != null) {
            if (outline == null) {
               outline = new GeneralPath(underline);
            } else {
               outline.setWindingRule(1);
               outline.append(underline, false);
            }
         }

         if (strikeThrough != null) {
            if (outline == null) {
               outline = new GeneralPath(strikeThrough);
            } else {
               outline.setWindingRule(1);
               outline.append(strikeThrough, false);
            }
         }

         if (overline != null) {
            if (outline == null) {
               outline = new GeneralPath(overline);
            } else {
               outline.setWindingRule(1);
               outline.append(overline, false);
            }
         }

         return outline;
      }
   }

   public Rectangle2D getBounds2D(TextNode node) {
      AttributedCharacterIterator aci = node.getAttributedCharacterIterator();
      if (aci == null) {
         return null;
      } else {
         List textRuns = this.getTextRuns(node, aci);
         Rectangle2D bounds = null;
         Iterator var5 = textRuns.iterator();

         while(var5.hasNext()) {
            Object textRun1 = var5.next();
            TextRun textRun = (TextRun)textRun1;
            TextSpanLayout textRunLayout = textRun.getLayout();
            Rectangle2D runBounds = textRunLayout.getBounds2D();
            if (runBounds != null) {
               if (bounds == null) {
                  bounds = runBounds;
               } else {
                  bounds.add(runBounds);
               }
            }
         }

         Shape underline = this.getDecorationStrokeOutline(textRuns, 1);
         if (underline != null) {
            if (bounds == null) {
               bounds = underline.getBounds2D();
            } else {
               bounds.add(underline.getBounds2D());
            }
         }

         Shape strikeThrough = this.getDecorationStrokeOutline(textRuns, 2);
         if (strikeThrough != null) {
            if (bounds == null) {
               bounds = strikeThrough.getBounds2D();
            } else {
               bounds.add(strikeThrough.getBounds2D());
            }
         }

         Shape overline = this.getDecorationStrokeOutline(textRuns, 4);
         if (overline != null) {
            if (bounds == null) {
               bounds = overline.getBounds2D();
            } else {
               bounds.add(overline.getBounds2D());
            }
         }

         return bounds;
      }
   }

   protected Shape getDecorationOutline(List textRuns, int decorationType) {
      GeneralPath outline = null;
      Paint prevPaint = null;
      Paint prevStrokePaint = null;
      Stroke prevStroke = null;
      Rectangle2D decorationRect = null;
      double yLoc = 0.0;
      double height = 0.0;

      Paint strokePaint;
      for(Iterator var12 = textRuns.iterator(); var12.hasNext(); prevStrokePaint = strokePaint) {
         Object textRun1 = var12.next();
         TextRun textRun = (TextRun)textRun1;
         AttributedCharacterIterator runaci = textRun.getACI();
         runaci.first();
         Paint paint = null;
         Stroke stroke = null;
         strokePaint = null;
         TextPaintInfo tpi = (TextPaintInfo)runaci.getAttribute(PAINT_INFO);
         if (tpi != null) {
            switch (decorationType) {
               case 1:
                  paint = tpi.underlinePaint;
                  stroke = tpi.underlineStroke;
                  strokePaint = tpi.underlineStrokePaint;
                  break;
               case 2:
                  paint = tpi.strikethroughPaint;
                  stroke = tpi.strikethroughStroke;
                  strokePaint = tpi.strikethroughStrokePaint;
                  break;
               case 3:
               default:
                  return null;
               case 4:
                  paint = tpi.overlinePaint;
                  stroke = tpi.overlineStroke;
                  strokePaint = tpi.overlineStrokePaint;
            }
         }

         Shape decorationShape;
         Rectangle2D r2d;
         if (textRun.isFirstRunInChunk()) {
            decorationShape = textRun.getLayout().getDecorationOutline(decorationType);
            r2d = decorationShape.getBounds2D();
            yLoc = r2d.getY();
            height = r2d.getHeight();
         }

         if ((textRun.isFirstRunInChunk() || paint != prevPaint || stroke != prevStroke || strokePaint != prevStrokePaint) && decorationRect != null) {
            if (outline == null) {
               outline = new GeneralPath(decorationRect);
            } else {
               outline.append(decorationRect, false);
            }

            decorationRect = null;
         }

         if ((paint != null || strokePaint != null) && !textRun.getLayout().isVertical() && !textRun.getLayout().isOnATextPath()) {
            decorationShape = textRun.getLayout().getDecorationOutline(decorationType);
            if (decorationRect == null) {
               r2d = decorationShape.getBounds2D();
               decorationRect = new Rectangle2D.Double(r2d.getX(), yLoc, r2d.getWidth(), height);
            } else {
               r2d = decorationShape.getBounds2D();
               double minX = Math.min(decorationRect.getX(), r2d.getX());
               double maxX = Math.max(decorationRect.getMaxX(), r2d.getMaxX());
               decorationRect.setRect(minX, yLoc, maxX - minX, height);
            }
         }

         prevPaint = paint;
         prevStroke = stroke;
      }

      if (decorationRect != null) {
         if (outline == null) {
            outline = new GeneralPath(decorationRect);
         } else {
            outline.append(decorationRect, false);
         }
      }

      return outline;
   }

   protected Shape getDecorationStrokeOutline(List textRuns, int decorationType) {
      GeneralPath outline = null;
      Paint prevPaint = null;
      Paint prevStrokePaint = null;
      Stroke prevStroke = null;
      Rectangle2D decorationRect = null;
      double yLoc = 0.0;
      double height = 0.0;

      Paint strokePaint;
      for(Iterator var12 = textRuns.iterator(); var12.hasNext(); prevStrokePaint = strokePaint) {
         Object textRun1 = var12.next();
         TextRun textRun = (TextRun)textRun1;
         AttributedCharacterIterator runaci = textRun.getACI();
         runaci.first();
         Paint paint = null;
         Stroke stroke = null;
         strokePaint = null;
         TextPaintInfo tpi = (TextPaintInfo)runaci.getAttribute(PAINT_INFO);
         if (tpi != null) {
            switch (decorationType) {
               case 1:
                  paint = tpi.underlinePaint;
                  stroke = tpi.underlineStroke;
                  strokePaint = tpi.underlineStrokePaint;
                  break;
               case 2:
                  paint = tpi.strikethroughPaint;
                  stroke = tpi.strikethroughStroke;
                  strokePaint = tpi.strikethroughStrokePaint;
                  break;
               case 3:
               default:
                  return null;
               case 4:
                  paint = tpi.overlinePaint;
                  stroke = tpi.overlineStroke;
                  strokePaint = tpi.overlineStrokePaint;
            }
         }

         Shape decorationShape;
         Rectangle2D r2d;
         if (textRun.isFirstRunInChunk()) {
            decorationShape = textRun.getLayout().getDecorationOutline(decorationType);
            r2d = decorationShape.getBounds2D();
            yLoc = r2d.getY();
            height = r2d.getHeight();
         }

         if ((textRun.isFirstRunInChunk() || paint != prevPaint || stroke != prevStroke || strokePaint != prevStrokePaint) && decorationRect != null) {
            Shape s = null;
            if (prevStroke != null && prevStrokePaint != null) {
               s = prevStroke.createStrokedShape(decorationRect);
            } else if (prevPaint != null) {
               s = decorationRect;
            }

            if (s != null) {
               if (outline == null) {
                  outline = new GeneralPath((Shape)s);
               } else {
                  outline.append((Shape)s, false);
               }
            }

            decorationRect = null;
         }

         if ((paint != null || strokePaint != null) && !textRun.getLayout().isVertical() && !textRun.getLayout().isOnATextPath()) {
            decorationShape = textRun.getLayout().getDecorationOutline(decorationType);
            if (decorationRect == null) {
               r2d = decorationShape.getBounds2D();
               decorationRect = new Rectangle2D.Double(r2d.getX(), yLoc, r2d.getWidth(), height);
            } else {
               r2d = decorationShape.getBounds2D();
               double minX = Math.min(decorationRect.getX(), r2d.getX());
               double maxX = Math.max(decorationRect.getMaxX(), r2d.getMaxX());
               decorationRect.setRect(minX, yLoc, maxX - minX, height);
            }
         }

         prevPaint = paint;
         prevStroke = stroke;
      }

      if (decorationRect != null) {
         Shape s = null;
         if (prevStroke != null && prevStrokePaint != null) {
            s = prevStroke.createStrokedShape(decorationRect);
         } else if (prevPaint != null) {
            s = decorationRect;
         }

         if (s != null) {
            if (outline == null) {
               outline = new GeneralPath((Shape)s);
            } else {
               outline.append((Shape)s, false);
            }
         }
      }

      return outline;
   }

   public Mark getMark(TextNode node, int index, boolean leadingEdge) {
      AttributedCharacterIterator aci = node.getAttributedCharacterIterator();
      if (aci == null) {
         return null;
      } else if (index >= aci.getBeginIndex() && index <= aci.getEndIndex()) {
         TextHit textHit = new TextHit(index, leadingEdge);
         return new BasicTextPainter.BasicMark(node, textHit);
      } else {
         return null;
      }
   }

   protected Mark hitTest(double x, double y, TextNode node) {
      AttributedCharacterIterator aci = node.getAttributedCharacterIterator();
      if (aci == null) {
         return null;
      } else {
         List textRuns = this.getTextRuns(node, aci);
         if (textRuns != null) {
            Iterator var8 = textRuns.iterator();

            while(var8.hasNext()) {
               Object textRun1 = var8.next();
               TextRun textRun = (TextRun)textRun1;
               TextSpanLayout layout = textRun.getLayout();
               TextHit textHit = layout.hitTestChar((float)x, (float)y);
               Rectangle2D bounds = layout.getBounds2D();
               if (textHit != null && bounds != null && bounds.contains(x, y)) {
                  return new BasicTextPainter.BasicMark(node, textHit);
               }
            }
         }

         return null;
      }
   }

   public Mark selectFirst(TextNode node) {
      AttributedCharacterIterator aci = node.getAttributedCharacterIterator();
      if (aci == null) {
         return null;
      } else {
         TextHit textHit = new TextHit(aci.getBeginIndex(), false);
         return new BasicTextPainter.BasicMark(node, textHit);
      }
   }

   public Mark selectLast(TextNode node) {
      AttributedCharacterIterator aci = node.getAttributedCharacterIterator();
      if (aci == null) {
         return null;
      } else {
         TextHit textHit = new TextHit(aci.getEndIndex() - 1, false);
         return new BasicTextPainter.BasicMark(node, textHit);
      }
   }

   public int[] getSelected(Mark startMark, Mark finishMark) {
      if (startMark != null && finishMark != null) {
         BasicTextPainter.BasicMark start;
         BasicTextPainter.BasicMark finish;
         try {
            start = (BasicTextPainter.BasicMark)startMark;
            finish = (BasicTextPainter.BasicMark)finishMark;
         } catch (ClassCastException var16) {
            throw new RuntimeException("This Mark was not instantiated by this TextPainter class!");
         }

         TextNode textNode = start.getTextNode();
         if (textNode == null) {
            return null;
         } else if (textNode != finish.getTextNode()) {
            throw new RuntimeException("Markers are from different TextNodes!");
         } else {
            AttributedCharacterIterator aci = textNode.getAttributedCharacterIterator();
            if (aci == null) {
               return null;
            } else {
               int[] result = new int[]{start.getHit().getCharIndex(), finish.getHit().getCharIndex()};
               List textRuns = this.getTextRuns(textNode, aci);
               Iterator trI = textRuns.iterator();
               int startGlyphIndex = -1;
               int endGlyphIndex = -1;
               TextSpanLayout startLayout = null;
               TextSpanLayout endLayout = null;

               while(trI.hasNext()) {
                  TextRun tr = (TextRun)trI.next();
                  TextSpanLayout tsl = tr.getLayout();
                  if (startGlyphIndex == -1) {
                     startGlyphIndex = tsl.getGlyphIndex(result[0]);
                     if (startGlyphIndex != -1) {
                        startLayout = tsl;
                     }
                  }

                  if (endGlyphIndex == -1) {
                     endGlyphIndex = tsl.getGlyphIndex(result[1]);
                     if (endGlyphIndex != -1) {
                        endLayout = tsl;
                     }
                  }

                  if (startGlyphIndex != -1 && endGlyphIndex != -1) {
                     break;
                  }
               }

               if (startLayout != null && endLayout != null) {
                  int startCharCount = startLayout.getCharacterCount(startGlyphIndex, startGlyphIndex);
                  int endCharCount = endLayout.getCharacterCount(endGlyphIndex, endGlyphIndex);
                  if (startCharCount > 1) {
                     if (result[0] > result[1] && startLayout.isLeftToRight()) {
                        result[0] += startCharCount - 1;
                     } else if (result[1] > result[0] && !startLayout.isLeftToRight()) {
                        result[0] -= startCharCount - 1;
                     }
                  }

                  if (endCharCount > 1) {
                     if (result[1] > result[0] && endLayout.isLeftToRight()) {
                        result[1] += endCharCount - 1;
                     } else if (result[0] > result[1] && !endLayout.isLeftToRight()) {
                        result[1] -= endCharCount - 1;
                     }
                  }

                  return result;
               } else {
                  return null;
               }
            }
         }
      } else {
         return null;
      }
   }

   public Shape getHighlightShape(Mark beginMark, Mark endMark) {
      if (beginMark != null && endMark != null) {
         BasicTextPainter.BasicMark begin;
         BasicTextPainter.BasicMark end;
         try {
            begin = (BasicTextPainter.BasicMark)beginMark;
            end = (BasicTextPainter.BasicMark)endMark;
         } catch (ClassCastException var16) {
            throw new RuntimeException("This Mark was not instantiated by this TextPainter class!");
         }

         TextNode textNode = begin.getTextNode();
         if (textNode == null) {
            return null;
         } else if (textNode != end.getTextNode()) {
            throw new RuntimeException("Markers are from different TextNodes!");
         } else {
            AttributedCharacterIterator aci = textNode.getAttributedCharacterIterator();
            if (aci == null) {
               return null;
            } else {
               int beginIndex = begin.getHit().getCharIndex();
               int endIndex = end.getHit().getCharIndex();
               if (beginIndex > endIndex) {
                  int tmpIndex = beginIndex;
                  beginIndex = endIndex;
                  endIndex = tmpIndex;
               }

               List textRuns = this.getTextRuns(textNode, aci);
               GeneralPath highlightedShape = new GeneralPath();
               Iterator var11 = textRuns.iterator();

               while(var11.hasNext()) {
                  Object textRun1 = var11.next();
                  TextRun textRun = (TextRun)textRun1;
                  TextSpanLayout layout = textRun.getLayout();
                  Shape layoutHighlightedShape = layout.getHighlightShape(beginIndex, endIndex);
                  if (layoutHighlightedShape != null && !layoutHighlightedShape.getBounds().isEmpty()) {
                     highlightedShape.append(layoutHighlightedShape, false);
                  }
               }

               return highlightedShape;
            }
         }
      } else {
         return null;
      }
   }

   static {
      PAINT_INFO = GVTAttributedCharacterIterator.TextAttribute.PAINT_INFO;
      FLOW_REGIONS = GVTAttributedCharacterIterator.TextAttribute.FLOW_REGIONS;
      FLOW_PARAGRAPH = GVTAttributedCharacterIterator.TextAttribute.FLOW_PARAGRAPH;
      TEXT_COMPOUND_ID = GVTAttributedCharacterIterator.TextAttribute.TEXT_COMPOUND_ID;
      GVT_FONT = GVTAttributedCharacterIterator.TextAttribute.GVT_FONT;
      GVT_FONTS = GVTAttributedCharacterIterator.TextAttribute.GVT_FONTS;
      BIDI_LEVEL = GVTAttributedCharacterIterator.TextAttribute.BIDI_LEVEL;
      XPOS = GVTAttributedCharacterIterator.TextAttribute.X;
      YPOS = GVTAttributedCharacterIterator.TextAttribute.Y;
      TEXTPATH = GVTAttributedCharacterIterator.TextAttribute.TEXTPATH;
      WRITING_MODE = GVTAttributedCharacterIterator.TextAttribute.WRITING_MODE;
      WRITING_MODE_TTB = GVTAttributedCharacterIterator.TextAttribute.WRITING_MODE_TTB;
      WRITING_MODE_RTL = GVTAttributedCharacterIterator.TextAttribute.WRITING_MODE_RTL;
      ANCHOR_TYPE = GVTAttributedCharacterIterator.TextAttribute.ANCHOR_TYPE;
      ADJUST_SPACING = GVTAttributedCharacterIterator.TextAttribute.ADJUST_SPACING;
      ADJUST_ALL = GVTAttributedCharacterIterator.TextAttribute.ADJUST_ALL;
      ALT_GLYPH_HANDLER = GVTAttributedCharacterIterator.TextAttribute.ALT_GLYPH_HANDLER;
      extendedAtts = new HashSet();
      extendedAtts.add(FLOW_PARAGRAPH);
      extendedAtts.add(TEXT_COMPOUND_ID);
      extendedAtts.add(GVT_FONT);
      singleton = new StrokingTextPainter();
   }

   public static class TextRun {
      protected AttributedCharacterIterator aci;
      protected TextSpanLayout layout;
      protected int anchorType;
      protected boolean firstRunInChunk;
      protected Float length;
      protected Integer lengthAdjust;
      private int level;
      private int reversals;

      public TextRun(TextSpanLayout layout, AttributedCharacterIterator aci, boolean firstRunInChunk) {
         this.layout = layout;
         this.aci = aci;
         this.aci.first();
         this.firstRunInChunk = firstRunInChunk;
         this.anchorType = 0;
         TextNode.Anchor anchor = (TextNode.Anchor)aci.getAttribute(GVTAttributedCharacterIterator.TextAttribute.ANCHOR_TYPE);
         if (anchor != null) {
            this.anchorType = anchor.getType();
         }

         if (aci.getAttribute(StrokingTextPainter.WRITING_MODE) == StrokingTextPainter.WRITING_MODE_RTL) {
            if (this.anchorType == 0) {
               this.anchorType = 2;
            } else if (this.anchorType == 2) {
               this.anchorType = 0;
            }
         }

         this.length = (Float)aci.getAttribute(GVTAttributedCharacterIterator.TextAttribute.BBOX_WIDTH);
         this.lengthAdjust = (Integer)aci.getAttribute(GVTAttributedCharacterIterator.TextAttribute.LENGTH_ADJUST);
         Integer level = (Integer)aci.getAttribute(StrokingTextPainter.BIDI_LEVEL);
         if (level != null) {
            this.level = level;
         } else {
            this.level = -1;
         }

      }

      public AttributedCharacterIterator getACI() {
         return this.aci;
      }

      public TextSpanLayout getLayout() {
         return this.layout;
      }

      public int getAnchorType() {
         return this.anchorType;
      }

      public Float getLength() {
         return this.length;
      }

      public Integer getLengthAdjust() {
         return this.lengthAdjust;
      }

      public boolean isFirstRunInChunk() {
         return this.firstRunInChunk;
      }

      public int getBidiLevel() {
         return this.level;
      }

      public void reverse() {
         ++this.reversals;
      }

      public void maybeReverseGlyphs(boolean mirror) {
         if ((this.reversals & 1) == 1) {
            this.layout.maybeReverse(mirror);
         }

      }
   }

   public static class TextChunk {
      public int begin;
      public int end;
      public Point2D advance;

      public TextChunk(int begin, int end, Point2D advance) {
         this.begin = begin;
         this.end = end;
         this.advance = new Point2D.Float((float)advance.getX(), (float)advance.getY());
      }
   }
}
