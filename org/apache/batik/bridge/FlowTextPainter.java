package org.apache.batik.bridge;

import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.text.AttributedCharacterIterator;
import java.text.CharacterIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.apache.batik.gvt.flow.BlockInfo;
import org.apache.batik.gvt.flow.FlowRegions;
import org.apache.batik.gvt.flow.GlyphGroupInfo;
import org.apache.batik.gvt.flow.LineInfo;
import org.apache.batik.gvt.flow.RegionInfo;
import org.apache.batik.gvt.flow.TextLineBreaks;
import org.apache.batik.gvt.flow.WordInfo;
import org.apache.batik.gvt.font.GVTFont;
import org.apache.batik.gvt.font.GVTGlyphVector;
import org.apache.batik.gvt.font.GVTLineMetrics;
import org.apache.batik.gvt.font.MultiGlyphVector;
import org.apache.batik.gvt.text.GVTAttributedCharacterIterator;

public class FlowTextPainter extends StrokingTextPainter {
   protected static TextPainter singleton = new FlowTextPainter();
   public static final char SOFT_HYPHEN = '\u00ad';
   public static final char ZERO_WIDTH_SPACE = '\u200b';
   public static final char ZERO_WIDTH_JOINER = '\u200d';
   public static final char SPACE = ' ';
   public static final AttributedCharacterIterator.Attribute WORD_LIMIT;
   public static final AttributedCharacterIterator.Attribute FLOW_REGIONS;
   public static final AttributedCharacterIterator.Attribute FLOW_LINE_BREAK;
   public static final AttributedCharacterIterator.Attribute LINE_HEIGHT;
   public static final AttributedCharacterIterator.Attribute GVT_FONT;
   protected static Set szAtts;

   public static TextPainter getInstance() {
      return singleton;
   }

   public List getTextRuns(TextNode node, AttributedCharacterIterator aci) {
      List textRuns = node.getTextRuns();
      if (textRuns != null) {
         return textRuns;
      } else {
         AttributedCharacterIterator[] chunkACIs = this.getTextChunkACIs(aci);
         textRuns = this.computeTextRuns(node, aci, chunkACIs);
         aci.first();
         List rgns = (List)aci.getAttribute(FLOW_REGIONS);
         if (rgns != null) {
            Iterator i = textRuns.iterator();
            List chunkLayouts = new ArrayList();
            StrokingTextPainter.TextRun tr = (StrokingTextPainter.TextRun)i.next();
            List layouts = new ArrayList();
            chunkLayouts.add(layouts);
            layouts.add(tr.getLayout());

            for(; i.hasNext(); layouts.add(tr.getLayout())) {
               tr = (StrokingTextPainter.TextRun)i.next();
               if (tr.isFirstRunInChunk()) {
                  layouts = new ArrayList();
                  chunkLayouts.add(layouts);
               }
            }

            textWrap(chunkACIs, chunkLayouts, rgns, this.fontRenderContext);
         }

         node.setTextRuns(textRuns);
         return node.getTextRuns();
      }
   }

   public static boolean textWrap(AttributedCharacterIterator[] acis, List chunkLayouts, List flowRects, FontRenderContext frc) {
      WordInfo[][] wordInfos = new WordInfo[acis.length][];
      Iterator clIter = chunkLayouts.iterator();
      float prevBotMargin = 0.0F;
      int numWords = 0;
      BlockInfo[] blockInfos = new BlockInfo[acis.length];
      float[] topSkip = new float[acis.length];

      AttributedCharacterIterator aci;
      for(int chunk = 0; clIter.hasNext(); ++chunk) {
         aci = acis[chunk];
         List gvl = new LinkedList();
         List layouts = (List)clIter.next();
         Iterator var14 = layouts.iterator();

         while(var14.hasNext()) {
            Object layout = var14.next();
            GlyphLayout gl = (GlyphLayout)layout;
            gvl.add(gl.getGlyphVector());
         }

         GVTGlyphVector gv = new MultiGlyphVector(gvl);
         wordInfos[chunk] = doWordAnalysis(gv, aci, numWords, frc);
         aci.first();
         BlockInfo bi = (BlockInfo)aci.getAttribute(FLOW_PARAGRAPH);
         bi.initLineInfo(frc);
         blockInfos[chunk] = bi;
         if (prevBotMargin > bi.getTopMargin()) {
            topSkip[chunk] = prevBotMargin;
         } else {
            topSkip[chunk] = bi.getTopMargin();
         }

         prevBotMargin = bi.getBottomMargin();
         numWords += wordInfos[chunk].length;
      }

      Iterator frIter = flowRects.iterator();
      aci = null;
      int currWord = 0;
      int chunk = 0;
      List lineInfos = new LinkedList();

      WordInfo[] chunkInfo;
      while(frIter.hasNext()) {
         RegionInfo currentRegion = (RegionInfo)frIter.next();
         FlowRegions fr = new FlowRegions(currentRegion.getShape());

         while(chunk < wordInfos.length) {
            chunkInfo = wordInfos[chunk];
            BlockInfo bi = blockInfos[chunk];
            WordInfo wi = chunkInfo[currWord];
            Object flowLine = wi.getFlowLine();
            double lh = (double)Math.max(wi.getLineHeight(), bi.getLineHeight());
            LineInfo li = new LineInfo(fr, bi, true);
            double newY = li.getCurrentY() + (double)topSkip[chunk];
            topSkip[chunk] = 0.0F;
            if (li.gotoY(newY)) {
               break;
            }

            while(!li.addWord(wi)) {
               newY = li.getCurrentY() + lh * 0.1;
               if (li.gotoY(newY)) {
                  break;
               }
            }

            if (fr.done()) {
               break;
            }

            ++currWord;

            for(; currWord < chunkInfo.length; ++currWord) {
               wi = chunkInfo[currWord];
               if (wi.getFlowLine() != flowLine || !li.addWord(wi)) {
                  li.layout();
                  lineInfos.add(li);
                  li = null;
                  flowLine = wi.getFlowLine();
                  lh = (double)Math.max(wi.getLineHeight(), bi.getLineHeight());
                  if (!fr.newLine(lh)) {
                     break;
                  }

                  li = new LineInfo(fr, bi, false);

                  while(!li.addWord(wi)) {
                     newY = li.getCurrentY() + lh * 0.1;
                     if (li.gotoY(newY)) {
                        break;
                     }
                  }

                  if (fr.done()) {
                     break;
                  }
               }
            }

            if (li != null) {
               li.setParaEnd(true);
               li.layout();
            }

            if (fr.done()) {
               break;
            }

            ++chunk;
            currWord = 0;
            if (bi.isFlowRegionBreak() || !fr.newLine(lh)) {
               break;
            }
         }

         if (chunk == wordInfos.length) {
            break;
         }
      }

      boolean overflow;
      for(overflow = chunk < wordInfos.length; chunk < wordInfos.length; currWord = 0) {
         for(chunkInfo = wordInfos[chunk]; currWord < chunkInfo.length; ++currWord) {
            WordInfo wi = chunkInfo[currWord];
            int numGG = wi.getNumGlyphGroups();

            for(int gg = 0; gg < numGG; ++gg) {
               GlyphGroupInfo ggi = wi.getGlyphGroup(gg);
               GVTGlyphVector gv = ggi.getGlyphVector();
               int end = ggi.getEnd();

               for(int g = ggi.getStart(); g <= end; ++g) {
                  gv.setGlyphVisible(g, false);
               }
            }
         }

         ++chunk;
      }

      return overflow;
   }

   static int[] allocWordMap(int[] wordMap, int sz) {
      if (wordMap != null) {
         if (sz <= wordMap.length) {
            return wordMap;
         }

         if (sz < wordMap.length * 2) {
            sz = wordMap.length * 2;
         }
      }

      int[] ret = new int[sz];
      int ext = wordMap != null ? wordMap.length : 0;
      if (sz < ext) {
         ext = sz;
      }

      if (ext != 0) {
         System.arraycopy(wordMap, 0, ret, 0, ext);
      }

      Arrays.fill(ret, ext, sz, -1);
      return ret;
   }

   static WordInfo[] doWordAnalysis(GVTGlyphVector gv, AttributedCharacterIterator aci, int numWords, FontRenderContext frc) {
      int numGlyphs = gv.getNumGlyphs();
      int[] glyphWords = new int[numGlyphs];
      int[] wordMap = allocWordMap((int[])null, 10);
      int maxWord = 0;
      int aciIdx = aci.getBeginIndex();

      int words;
      int aciEnd;
      int word;
      int aciWordStart;
      for(words = 0; words < numGlyphs; ++words) {
         int cnt = gv.getCharacterCount(words, words);
         aci.setIndex(aciIdx);
         Integer integer = (Integer)aci.getAttribute(WORD_LIMIT);
         aciEnd = integer - numWords;
         if (aciEnd > maxWord) {
            maxWord = aciEnd;
            wordMap = allocWordMap(wordMap, aciEnd + 1);
         }

         ++aciIdx;

         for(word = 1; word < cnt; ++word) {
            aci.setIndex(aciIdx);
            integer = (Integer)aci.getAttribute(WORD_LIMIT);
            aciWordStart = integer - numWords;
            if (aciWordStart > maxWord) {
               maxWord = aciWordStart;
               wordMap = allocWordMap(wordMap, aciWordStart + 1);
            }

            if (aciWordStart < aciEnd) {
               wordMap[aciEnd] = aciWordStart;
               aciEnd = aciWordStart;
            } else if (aciWordStart > aciEnd) {
               wordMap[aciWordStart] = aciEnd;
            }

            ++aciIdx;
         }

         glyphWords[words] = aciEnd;
      }

      words = 0;
      WordInfo[] cWordMap = new WordInfo[maxWord + 1];

      for(int i = 0; i <= maxWord; ++i) {
         aciEnd = wordMap[i];
         if (aciEnd == -1) {
            cWordMap[i] = new WordInfo(words++);
         } else {
            word = aciEnd;

            for(aciEnd = wordMap[i]; aciEnd != -1; aciEnd = wordMap[aciEnd]) {
               word = aciEnd;
            }

            wordMap[i] = word;
            cWordMap[i] = cWordMap[word];
         }
      }

      int[] wordMap = null;
      WordInfo[] wordInfos = new WordInfo[words];

      for(aciEnd = 0; aciEnd <= maxWord; ++aciEnd) {
         WordInfo wi = cWordMap[aciEnd];
         wordInfos[wi.getIndex()] = cWordMap[aciEnd];
      }

      aciIdx = aci.getBeginIndex();
      aciEnd = aci.getEndIndex();
      char ch = aci.setIndex(aciIdx);
      aciWordStart = aciIdx;
      GVTFont gvtFont = (GVTFont)aci.getAttribute(GVT_FONT);
      float lineHeight = 1.0F;
      Float lineHeightFloat = (Float)aci.getAttribute(LINE_HEIGHT);
      if (lineHeightFloat != null) {
         lineHeight = lineHeightFloat;
      }

      int runLimit = aci.getRunLimit(szAtts);
      WordInfo prevWI = null;
      float[] lastAdvAdj = new float[numGlyphs];
      float[] advAdj = new float[numGlyphs];
      boolean[] hideLast = new boolean[numGlyphs];
      boolean[] hide = new boolean[numGlyphs];
      boolean[] space = new boolean[numGlyphs];
      float[] glyphPos = gv.getGlyphPositions(0, numGlyphs + 1, (float[])null);

      int cWord;
      int i;
      for(int i = 0; i < numGlyphs; ++i) {
         char pch = ch;
         ch = aci.setIndex(aciIdx);
         Integer integer = (Integer)aci.getAttribute(WORD_LIMIT);
         WordInfo theWI = cWordMap[integer - numWords];
         if (theWI.getFlowLine() == null) {
            theWI.setFlowLine(aci.getAttribute(FLOW_LINE_BREAK));
         }

         if (prevWI == null) {
            prevWI = theWI;
         } else if (prevWI != theWI) {
            GVTLineMetrics lm = gvtFont.getLineMetrics((CharacterIterator)aci, aciWordStart, aciIdx, frc);
            prevWI.addLineMetrics(gvtFont, lm);
            prevWI.addLineHeight(lineHeight);
            aciWordStart = aciIdx;
            prevWI = theWI;
         }

         i = gv.getCharacterCount(i, i);
         if (i == 1) {
            float kern;
            switch (ch) {
               case ' ':
                  space[i] = true;
                  cWord = aci.next();
                  aci.previous();
                  kern = gvtFont.getHKern(pch, cWord);
                  lastAdvAdj[i] = -(glyphPos[2 * i + 2] - glyphPos[2 * i] + kern);
                  break;
               case '\u00ad':
                  hideLast[i] = true;
                  cWord = aci.next();
                  aci.previous();
                  kern = gvtFont.getHKern(pch, cWord);
                  advAdj[i] = -(glyphPos[2 * i + 2] - glyphPos[2 * i] + kern);
                  break;
               case '\u200b':
                  hide[i] = true;
                  break;
               case '\u200d':
                  hide[i] = true;
            }
         }

         aciIdx += i;
         if (aciIdx > runLimit && aciIdx < aciEnd) {
            GVTLineMetrics lm = gvtFont.getLineMetrics((CharacterIterator)aci, aciWordStart, runLimit, frc);
            prevWI.addLineMetrics(gvtFont, lm);
            prevWI.addLineHeight(lineHeight);
            prevWI = null;
            aciWordStart = aciIdx;
            aci.setIndex(aciIdx);
            gvtFont = (GVTFont)aci.getAttribute(GVT_FONT);
            Float f = (Float)aci.getAttribute(LINE_HEIGHT);
            lineHeight = f;
            runLimit = aci.getRunLimit(szAtts);
         }
      }

      GVTLineMetrics lm = gvtFont.getLineMetrics((CharacterIterator)aci, aciWordStart, runLimit, frc);
      prevWI.addLineMetrics(gvtFont, lm);
      prevWI.addLineHeight(lineHeight);
      int[] wordGlyphCounts = new int[words];

      int var10002;
      for(int i = 0; i < numGlyphs; ++i) {
         int word = glyphWords[i];
         i = cWordMap[word].getIndex();
         glyphWords[i] = i;
         var10002 = wordGlyphCounts[i]++;
      }

      cWordMap = null;
      int[][] wordGlyphs = new int[words][];
      int[] wordGlyphGroupsCounts = new int[words];

      int glyphGroup;
      for(i = 0; i < numGlyphs; ++i) {
         cWord = glyphWords[i];
         int[] wgs = wordGlyphs[cWord];
         if (wgs == null) {
            wgs = wordGlyphs[cWord] = new int[wordGlyphCounts[cWord]];
            wordGlyphCounts[cWord] = 0;
         }

         glyphGroup = wordGlyphCounts[cWord];
         wgs[glyphGroup] = i;
         if (glyphGroup == 0) {
            var10002 = wordGlyphGroupsCounts[cWord]++;
         } else if (wgs[glyphGroup - 1] != i - 1) {
            var10002 = wordGlyphGroupsCounts[cWord]++;
         }

         var10002 = wordGlyphCounts[cWord]++;
      }

      for(i = 0; i < words; ++i) {
         cWord = wordGlyphGroupsCounts[i];
         GlyphGroupInfo[] wordGlyphGroups = new GlyphGroupInfo[cWord];
         int prev;
         if (cWord == 1) {
            int[] glyphs = wordGlyphs[i];
            int start = glyphs[0];
            prev = glyphs[glyphs.length - 1];
            wordGlyphGroups[0] = new GlyphGroupInfo(gv, start, prev, hide, hideLast[prev], glyphPos, advAdj, lastAdvAdj, space);
         } else {
            glyphGroup = 0;
            int[] glyphs = wordGlyphs[i];
            prev = glyphs[0];
            int start = prev;

            int j;
            for(j = 1; j < glyphs.length; ++j) {
               if (prev + 1 != glyphs[j]) {
                  int end = glyphs[j - 1];
                  wordGlyphGroups[glyphGroup] = new GlyphGroupInfo(gv, start, end, hide, hideLast[end], glyphPos, advAdj, lastAdvAdj, space);
                  start = glyphs[j];
                  ++glyphGroup;
               }

               prev = glyphs[j];
            }

            j = glyphs[glyphs.length - 1];
            wordGlyphGroups[glyphGroup] = new GlyphGroupInfo(gv, start, j, hide, hideLast[j], glyphPos, advAdj, lastAdvAdj, space);
         }

         wordInfos[i].setGlyphGroups(wordGlyphGroups);
      }

      return wordInfos;
   }

   static {
      WORD_LIMIT = TextLineBreaks.WORD_LIMIT;
      FLOW_REGIONS = GVTAttributedCharacterIterator.TextAttribute.FLOW_REGIONS;
      FLOW_LINE_BREAK = GVTAttributedCharacterIterator.TextAttribute.FLOW_LINE_BREAK;
      LINE_HEIGHT = GVTAttributedCharacterIterator.TextAttribute.LINE_HEIGHT;
      GVT_FONT = GVTAttributedCharacterIterator.TextAttribute.GVT_FONT;
      szAtts = new HashSet();
      szAtts.add(TextAttribute.SIZE);
      szAtts.add(GVT_FONT);
      szAtts.add(LINE_HEIGHT);
   }
}
