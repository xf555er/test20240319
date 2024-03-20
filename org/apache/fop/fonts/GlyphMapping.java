package org.apache.fop.fonts;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.complexscripts.util.CharScript;
import org.apache.fop.traits.MinOptMax;
import org.apache.fop.util.CharUtilities;

public class GlyphMapping {
   private static final Log LOG = LogFactory.getLog(GlyphMapping.class);
   public final int startIndex;
   public final int endIndex;
   private int wordCharLength;
   public final int wordSpaceCount;
   public int letterSpaceCount;
   public MinOptMax areaIPD;
   public final boolean isHyphenated;
   public final boolean isSpace;
   public boolean breakOppAfter;
   public final Font font;
   public final int level;
   public final int[][] gposAdjustments;
   public String mapping;
   public List associations;

   public GlyphMapping(int startIndex, int endIndex, int wordSpaceCount, int letterSpaceCount, MinOptMax areaIPD, boolean isHyphenated, boolean isSpace, boolean breakOppAfter, Font font, int level, int[][] gposAdjustments) {
      this(startIndex, endIndex, wordSpaceCount, letterSpaceCount, areaIPD, isHyphenated, isSpace, breakOppAfter, font, level, gposAdjustments, (String)null, (List)null);
   }

   public GlyphMapping(int startIndex, int endIndex, int wordSpaceCount, int letterSpaceCount, MinOptMax areaIPD, boolean isHyphenated, boolean isSpace, boolean breakOppAfter, Font font, int level, int[][] gposAdjustments, String mapping, List associations) {
      assert startIndex <= endIndex;

      this.startIndex = startIndex;
      this.endIndex = endIndex;
      this.wordCharLength = -1;
      this.wordSpaceCount = wordSpaceCount;
      this.letterSpaceCount = letterSpaceCount;
      this.areaIPD = areaIPD;
      this.isHyphenated = isHyphenated;
      this.isSpace = isSpace;
      this.breakOppAfter = breakOppAfter;
      this.font = font;
      this.level = level;
      this.gposAdjustments = gposAdjustments;
      this.mapping = mapping;
      this.associations = associations;
   }

   public static GlyphMapping doGlyphMapping(TextFragment text, int startIndex, int endIndex, Font font, MinOptMax letterSpaceIPD, MinOptMax[] letterSpaceAdjustArray, char precedingChar, char breakOpportunityChar, boolean endsWithHyphen, int level, boolean dontOptimizeForIdentityMapping, boolean retainAssociations, boolean retainControls) {
      GlyphMapping mapping;
      if (!font.performsSubstitution() && !font.performsPositioning()) {
         mapping = processWordNoMapping(text, startIndex, endIndex, font, letterSpaceIPD, letterSpaceAdjustArray, precedingChar, breakOpportunityChar, endsWithHyphen, level);
      } else {
         mapping = processWordMapping(text, startIndex, endIndex, font, breakOpportunityChar, endsWithHyphen, level, dontOptimizeForIdentityMapping, retainAssociations, retainControls);
      }

      return mapping;
   }

   private static GlyphMapping processWordMapping(TextFragment text, int startIndex, int endIndex, Font font, char breakOpportunityChar, boolean endsWithHyphen, int level, boolean dontOptimizeForIdentityMapping, boolean retainAssociations, boolean retainControls) {
      int nLS = 0;
      String script = text.getScript();
      String language = text.getLanguage();
      if (LOG.isDebugEnabled()) {
         LOG.debug("PW: [" + startIndex + "," + endIndex + "]: { +M, level = " + level + " }");
      }

      CharSequence ics = text.subSequence(startIndex, endIndex);
      if (script == null || "auto".equals(script)) {
         script = CharScript.scriptTagFromCode(CharScript.dominantScript(ics));
      }

      if (language == null || "none".equals(language)) {
         language = "dflt";
      }

      List associations = retainAssociations ? new ArrayList() : null;
      if ("zyyy".equals(script) || "auto".equals(script)) {
         script = "*";
      }

      CharSequence mcs = font.performSubstitution(ics, script, language, associations, retainControls);
      int[][] gpa = (int[][])null;
      if (font.performsPositioning()) {
         gpa = font.performPositioning(mcs, script, language);
      }

      if (useKerningAdjustments(font, script, language)) {
         gpa = getKerningAdjustments(mcs, font, gpa);
      }

      mcs = font.reorderCombiningMarks(mcs, gpa, script, language, associations);
      MinOptMax ipd = MinOptMax.ZERO;
      int i = 0;

      for(int n = mcs.length(); i < n; ++i) {
         int c = mcs.charAt(i);
         if (CharUtilities.containsSurrogatePairAt(mcs, i)) {
            char var10000 = (char)c;
            ++i;
            c = Character.toCodePoint(var10000, mcs.charAt(i));
         }

         int w = font.getCharWidth(c);
         if (w < 0) {
            w = 0;
         }

         if (gpa != null) {
            w += gpa[i][2];
         }

         ipd = ipd.plus(w);
      }

      return new GlyphMapping(startIndex, endIndex, 0, nLS, ipd, endsWithHyphen, false, breakOpportunityChar != 0, font, level, gpa, !dontOptimizeForIdentityMapping && CharUtilities.isSameSequence(mcs, ics) ? null : mcs.toString(), associations);
   }

   private static boolean useKerningAdjustments(Font font, String script, String language) {
      return font.hasKerning() && !font.hasFeature(2, script, language, "kern");
   }

   private static int[][] getKerningAdjustments(CharSequence mcs, Font font, int[][] gpa) {
      int numCodepoints = Character.codePointCount(mcs, 0, mcs.length());
      int[] kernings = new int[numCodepoints];
      int prevCp = -1;
      int i = 0;

      for(Iterator var7 = CharUtilities.codepointsIter(mcs).iterator(); var7.hasNext(); ++i) {
         int cp = (Integer)var7.next();
         if (prevCp >= 0) {
            kernings[i] = font.getKernValue(prevCp, cp);
         }

         prevCp = cp;
      }

      boolean hasKerning = false;
      int[] var13 = kernings;
      int var9 = kernings.length;

      for(int var10 = 0; var10 < var9; ++var10) {
         int kerningValue = var13[var10];
         if (kerningValue != 0) {
            hasKerning = true;
            break;
         }
      }

      if (hasKerning) {
         if (gpa == null) {
            gpa = new int[numCodepoints][4];
         }

         for(i = 0; i < numCodepoints; ++i) {
            if (i > 0) {
               gpa[i - 1][2] += kernings[i];
            }
         }

         return gpa;
      } else {
         return (int[][])null;
      }
   }

   private static GlyphMapping processWordNoMapping(TextFragment text, int startIndex, int endIndex, Font font, MinOptMax letterSpaceIPD, MinOptMax[] letterSpaceAdjustArray, char precedingChar, char breakOpportunityChar, boolean endsWithHyphen, int level) {
      boolean kerning = font.hasKerning();
      MinOptMax wordIPD = MinOptMax.ZERO;
      if (LOG.isDebugEnabled()) {
         LOG.debug("PW: [" + startIndex + "," + endIndex + "]: { -M, level = " + level + " }");
      }

      CharSequence ics = text.subSequence(startIndex, endIndex);
      int offset = 0;

      int letterSpaces;
      for(Iterator var14 = CharUtilities.codepointsIter(ics).iterator(); var14.hasNext(); ++offset) {
         letterSpaces = (Integer)var14.next();
         int charWidth = font.getCharWidth(letterSpaces);
         wordIPD = wordIPD.plus(charWidth);
         if (kerning) {
            int kern = 0;
            if (offset > 0) {
               int previousChar = Character.codePointAt(ics, offset - 1);
               kern = font.getKernValue(previousChar, letterSpaces);
            } else if (precedingChar != 0) {
               kern = font.getKernValue(precedingChar, letterSpaces);
            }

            if (kern != 0) {
               addToLetterAdjust(letterSpaceAdjustArray, startIndex + offset, kern);
               wordIPD = wordIPD.plus(kern);
            }
         }
      }

      int wordLength;
      if (kerning && breakOpportunityChar != 0 && !isSpace(breakOpportunityChar) && endIndex > 0 && endsWithHyphen) {
         wordLength = text.charAt(endIndex - 1);
         if (Character.isLowSurrogate((char)wordLength)) {
            char highSurrogate = text.charAt(endIndex - 2);
            wordLength = Character.toCodePoint(highSurrogate, (char)wordLength);
         }

         letterSpaces = font.getKernValue(wordLength, breakOpportunityChar);
         if (letterSpaces != 0) {
            addToLetterAdjust(letterSpaceAdjustArray, endIndex, letterSpaces);
         }
      }

      wordLength = endIndex - startIndex;
      letterSpaces = 0;
      if (wordLength != 0) {
         letterSpaces = wordLength - 1;
         if (breakOpportunityChar != 0 && !isSpace(breakOpportunityChar)) {
            ++letterSpaces;
         }
      }

      assert letterSpaces >= 0;

      wordIPD = wordIPD.plus(letterSpaceIPD.mult(letterSpaces));
      return new GlyphMapping(startIndex, endIndex, 0, letterSpaces, wordIPD, endsWithHyphen, false, breakOpportunityChar != 0 && !isSpace(breakOpportunityChar), font, level, (int[][])null);
   }

   private static void addToLetterAdjust(MinOptMax[] letterSpaceAdjustArray, int index, int width) {
      if (letterSpaceAdjustArray[index] == null) {
         letterSpaceAdjustArray[index] = MinOptMax.getInstance(width);
      } else {
         letterSpaceAdjustArray[index] = letterSpaceAdjustArray[index].plus(width);
      }

   }

   public static boolean isSpace(char ch) {
      return ch == ' ' || CharUtilities.isNonBreakableSpace(ch) || CharUtilities.isFixedWidthSpace(ch);
   }

   public int getWordLength() {
      if (this.wordCharLength == -1) {
         if (this.mapping != null) {
            this.wordCharLength = this.mapping.length();
         } else {
            assert this.endIndex >= this.startIndex;

            this.wordCharLength = this.endIndex - this.startIndex;
         }
      }

      return this.wordCharLength;
   }

   public void addToAreaIPD(MinOptMax idp) {
      this.areaIPD = this.areaIPD.plus(idp);
   }

   public String toString() {
      return super.toString() + "{interval = [" + this.startIndex + "," + this.endIndex + "], isSpace = " + this.isSpace + ", level = " + this.level + ", areaIPD = " + this.areaIPD + ", letterSpaceCount = " + this.letterSpaceCount + ", wordSpaceCount = " + this.wordSpaceCount + ", isHyphenated = " + this.isHyphenated + ", font = " + this.font + "}";
   }
}
