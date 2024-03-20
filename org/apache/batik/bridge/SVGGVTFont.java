package org.apache.batik.bridge;

import java.awt.font.FontRenderContext;
import java.text.AttributedCharacterIterator;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.dom.util.XMLSupport;
import org.apache.batik.gvt.font.GVTFont;
import org.apache.batik.gvt.font.GVTFontFace;
import org.apache.batik.gvt.font.GVTGlyphVector;
import org.apache.batik.gvt.font.GVTLineMetrics;
import org.apache.batik.gvt.font.Glyph;
import org.apache.batik.gvt.font.Kern;
import org.apache.batik.gvt.font.KerningTable;
import org.apache.batik.gvt.font.SVGGVTGlyphVector;
import org.apache.batik.gvt.text.GVTAttributedCharacterIterator;
import org.apache.batik.gvt.text.TextPaintInfo;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.Element;

public final class SVGGVTFont implements GVTFont, SVGConstants {
   public static final AttributedCharacterIterator.Attribute PAINT_INFO;
   private float fontSize;
   private GVTFontFace fontFace;
   private String[] glyphUnicodes;
   private String[] glyphNames;
   private String[] glyphLangs;
   private String[] glyphOrientations;
   private String[] glyphForms;
   private Element[] glyphElements;
   private Element[] hkernElements;
   private Element[] vkernElements;
   private BridgeContext ctx;
   private Element textElement;
   private Element missingGlyphElement;
   private KerningTable hKerningTable;
   private KerningTable vKerningTable;
   private String language;
   private String orientation;
   private float scale;
   private GVTLineMetrics lineMetrics = null;

   public SVGGVTFont(float fontSize, GVTFontFace fontFace, String[] glyphUnicodes, String[] glyphNames, String[] glyphLangs, String[] glyphOrientations, String[] glyphForms, BridgeContext ctx, Element[] glyphElements, Element missingGlyphElement, Element[] hkernElements, Element[] vkernElements, Element textElement) {
      this.fontFace = fontFace;
      this.fontSize = fontSize;
      this.glyphUnicodes = glyphUnicodes;
      this.glyphNames = glyphNames;
      this.glyphLangs = glyphLangs;
      this.glyphOrientations = glyphOrientations;
      this.glyphForms = glyphForms;
      this.ctx = ctx;
      this.glyphElements = glyphElements;
      this.missingGlyphElement = missingGlyphElement;
      this.hkernElements = hkernElements;
      this.vkernElements = vkernElements;
      this.scale = fontSize / fontFace.getUnitsPerEm();
      this.textElement = textElement;
      this.language = XMLSupport.getXMLLang(textElement);
      Value v = CSSUtilities.getComputedStyle(textElement, 59);
      if (v.getStringValue().startsWith("tb")) {
         this.orientation = "v";
      } else {
         this.orientation = "h";
      }

      this.createKerningTables();
   }

   private void createKerningTables() {
      Kern[] hEntries = new Kern[this.hkernElements.length];

      for(int i = 0; i < this.hkernElements.length; ++i) {
         Element hkernElement = this.hkernElements[i];
         SVGHKernElementBridge hkernBridge = (SVGHKernElementBridge)this.ctx.getBridge(hkernElement);
         Kern hkern = hkernBridge.createKern(this.ctx, hkernElement, this);
         hEntries[i] = hkern;
      }

      this.hKerningTable = new KerningTable(hEntries);
      Kern[] vEntries = new Kern[this.vkernElements.length];

      for(int i = 0; i < this.vkernElements.length; ++i) {
         Element vkernElement = this.vkernElements[i];
         SVGVKernElementBridge vkernBridge = (SVGVKernElementBridge)this.ctx.getBridge(vkernElement);
         Kern vkern = vkernBridge.createKern(this.ctx, vkernElement, this);
         vEntries[i] = vkern;
      }

      this.vKerningTable = new KerningTable(vEntries);
   }

   public float getHKern(int glyphCode1, int glyphCode2) {
      if (glyphCode1 >= 0 && glyphCode1 < this.glyphUnicodes.length && glyphCode2 >= 0 && glyphCode2 < this.glyphUnicodes.length) {
         float ret = this.hKerningTable.getKerningValue(glyphCode1, glyphCode2, this.glyphUnicodes[glyphCode1], this.glyphUnicodes[glyphCode2]);
         return ret * this.scale;
      } else {
         return 0.0F;
      }
   }

   public float getVKern(int glyphCode1, int glyphCode2) {
      if (glyphCode1 >= 0 && glyphCode1 < this.glyphUnicodes.length && glyphCode2 >= 0 && glyphCode2 < this.glyphUnicodes.length) {
         float ret = this.vKerningTable.getKerningValue(glyphCode1, glyphCode2, this.glyphUnicodes[glyphCode1], this.glyphUnicodes[glyphCode2]);
         return ret * this.scale;
      } else {
         return 0.0F;
      }
   }

   public int[] getGlyphCodesForName(String name) {
      List glyphCodes = new ArrayList();

      for(int i = 0; i < this.glyphNames.length; ++i) {
         if (this.glyphNames[i] != null && this.glyphNames[i].equals(name)) {
            glyphCodes.add(i);
         }
      }

      int[] glyphCodeArray = new int[glyphCodes.size()];

      for(int i = 0; i < glyphCodes.size(); ++i) {
         glyphCodeArray[i] = (Integer)glyphCodes.get(i);
      }

      return glyphCodeArray;
   }

   public int[] getGlyphCodesForUnicode(String unicode) {
      List glyphCodes = new ArrayList();

      for(int i = 0; i < this.glyphUnicodes.length; ++i) {
         if (this.glyphUnicodes[i] != null && this.glyphUnicodes[i].equals(unicode)) {
            glyphCodes.add(i);
         }
      }

      int[] glyphCodeArray = new int[glyphCodes.size()];

      for(int i = 0; i < glyphCodes.size(); ++i) {
         glyphCodeArray[i] = (Integer)glyphCodes.get(i);
      }

      return glyphCodeArray;
   }

   private boolean languageMatches(String glyphLang) {
      if (glyphLang != null && glyphLang.length() != 0) {
         StringTokenizer st = new StringTokenizer(glyphLang, ",");

         String s;
         do {
            if (!st.hasMoreTokens()) {
               return false;
            }

            s = st.nextToken();
         } while(!s.equals(this.language) && (!s.startsWith(this.language) || s.length() <= this.language.length() || s.charAt(this.language.length()) != '-'));

         return true;
      } else {
         return true;
      }
   }

   private boolean orientationMatches(String glyphOrientation) {
      return glyphOrientation != null && glyphOrientation.length() != 0 ? glyphOrientation.equals(this.orientation) : true;
   }

   private boolean formMatches(String glyphUnicode, String glyphForm, AttributedCharacterIterator aci, int currentIndex) {
      if (aci != null && glyphForm != null && glyphForm.length() != 0) {
         aci.setIndex(currentIndex);
         Integer form = (Integer)aci.getAttribute(GVTAttributedCharacterIterator.TextAttribute.ARABIC_FORM);
         if (form != null && !form.equals(GVTAttributedCharacterIterator.TextAttribute.ARABIC_NONE)) {
            if (glyphUnicode.length() > 1) {
               boolean matched = true;

               for(int j = 1; j < glyphUnicode.length(); ++j) {
                  char c = aci.next();
                  if (glyphUnicode.charAt(j) != c) {
                     matched = false;
                     break;
                  }
               }

               aci.setIndex(currentIndex);
               if (matched) {
                  aci.setIndex(currentIndex + glyphUnicode.length() - 1);
                  Integer lastForm = (Integer)aci.getAttribute(GVTAttributedCharacterIterator.TextAttribute.ARABIC_FORM);
                  aci.setIndex(currentIndex);
                  if (form != null && lastForm != null) {
                     if (form.equals(GVTAttributedCharacterIterator.TextAttribute.ARABIC_TERMINAL) && lastForm.equals(GVTAttributedCharacterIterator.TextAttribute.ARABIC_INITIAL)) {
                        return glyphForm.equals("isolated");
                     }

                     if (form.equals(GVTAttributedCharacterIterator.TextAttribute.ARABIC_TERMINAL)) {
                        return glyphForm.equals("terminal");
                     }

                     if (form.equals(GVTAttributedCharacterIterator.TextAttribute.ARABIC_MEDIAL) && lastForm.equals(GVTAttributedCharacterIterator.TextAttribute.ARABIC_MEDIAL)) {
                        return glyphForm.equals("medial");
                     }
                  }
               }
            }

            if (form.equals(GVTAttributedCharacterIterator.TextAttribute.ARABIC_ISOLATED)) {
               return glyphForm.equals("isolated");
            } else if (form.equals(GVTAttributedCharacterIterator.TextAttribute.ARABIC_TERMINAL)) {
               return glyphForm.equals("terminal");
            } else if (form.equals(GVTAttributedCharacterIterator.TextAttribute.ARABIC_INITIAL)) {
               return glyphForm.equals("initial");
            } else {
               return form.equals(GVTAttributedCharacterIterator.TextAttribute.ARABIC_MEDIAL) ? glyphForm.equals("medial") : false;
            }
         } else {
            return false;
         }
      } else {
         return true;
      }
   }

   public boolean canDisplayGivenName(String name) {
      for(int i = 0; i < this.glyphNames.length; ++i) {
         if (this.glyphNames[i] != null && this.glyphNames[i].equals(name) && this.languageMatches(this.glyphLangs[i]) && this.orientationMatches(this.glyphOrientations[i])) {
            return true;
         }
      }

      return false;
   }

   public boolean canDisplay(char c) {
      for(int i = 0; i < this.glyphUnicodes.length; ++i) {
         if (this.glyphUnicodes[i].indexOf(c) != -1 && this.languageMatches(this.glyphLangs[i]) && this.orientationMatches(this.glyphOrientations[i])) {
            return true;
         }
      }

      return false;
   }

   public int canDisplayUpTo(char[] text, int start, int limit) {
      StringCharacterIterator sci = new StringCharacterIterator(new String(text));
      return this.canDisplayUpTo((CharacterIterator)sci, start, limit);
   }

   public int canDisplayUpTo(CharacterIterator iter, int start, int limit) {
      AttributedCharacterIterator aci = null;
      if (iter instanceof AttributedCharacterIterator) {
         aci = (AttributedCharacterIterator)iter;
      }

      char c = iter.setIndex(start);

      for(int currentIndex = start; c != '\uffff' && currentIndex < limit; currentIndex = iter.getIndex()) {
         boolean foundMatchingGlyph = false;

         for(int i = 0; i < this.glyphUnicodes.length; ++i) {
            if (this.glyphUnicodes[i].indexOf(c) == 0 && this.languageMatches(this.glyphLangs[i]) && this.orientationMatches(this.glyphOrientations[i]) && this.formMatches(this.glyphUnicodes[i], this.glyphForms[i], aci, currentIndex)) {
               if (this.glyphUnicodes[i].length() == 1) {
                  foundMatchingGlyph = true;
                  break;
               }

               boolean matched = true;

               for(int j = 1; j < this.glyphUnicodes[i].length(); ++j) {
                  c = iter.next();
                  if (this.glyphUnicodes[i].charAt(j) != c) {
                     matched = false;
                     break;
                  }
               }

               if (matched) {
                  foundMatchingGlyph = true;
                  break;
               }

               c = iter.setIndex(currentIndex);
            }
         }

         if (!foundMatchingGlyph) {
            return currentIndex;
         }

         c = iter.next();
      }

      return -1;
   }

   public int canDisplayUpTo(String str) {
      StringCharacterIterator sci = new StringCharacterIterator(str);
      return this.canDisplayUpTo((CharacterIterator)sci, 0, str.length());
   }

   public GVTGlyphVector createGlyphVector(FontRenderContext frc, char[] chars) {
      StringCharacterIterator sci = new StringCharacterIterator(new String(chars));
      return this.createGlyphVector(frc, (CharacterIterator)sci);
   }

   public GVTGlyphVector createGlyphVector(FontRenderContext frc, CharacterIterator ci) {
      AttributedCharacterIterator aci = null;
      if (ci instanceof AttributedCharacterIterator) {
         aci = (AttributedCharacterIterator)ci;
      }

      List glyphs = new ArrayList();

      for(char c = ci.first(); c != '\uffff'; c = ci.next()) {
         boolean foundMatchingGlyph = false;

         for(int i = 0; i < this.glyphUnicodes.length; ++i) {
            if (this.glyphUnicodes[i].indexOf(c) == 0 && this.languageMatches(this.glyphLangs[i]) && this.orientationMatches(this.glyphOrientations[i]) && this.formMatches(this.glyphUnicodes[i], this.glyphForms[i], aci, ci.getIndex())) {
               if (this.glyphUnicodes[i].length() == 1) {
                  Element glyphElement = this.glyphElements[i];
                  SVGGlyphElementBridge glyphBridge = (SVGGlyphElementBridge)this.ctx.getBridge(glyphElement);
                  TextPaintInfo tpi = null;
                  if (aci != null) {
                     tpi = (TextPaintInfo)aci.getAttribute(PAINT_INFO);
                  }

                  Glyph glyph = glyphBridge.createGlyph(this.ctx, glyphElement, this.textElement, i, this.fontSize, this.fontFace, tpi);
                  glyphs.add(glyph);
                  foundMatchingGlyph = true;
                  break;
               }

               int current = ci.getIndex();
               boolean matched = true;

               for(int j = 1; j < this.glyphUnicodes[i].length(); ++j) {
                  c = ci.next();
                  if (this.glyphUnicodes[i].charAt(j) != c) {
                     matched = false;
                     break;
                  }
               }

               if (matched) {
                  Element glyphElement = this.glyphElements[i];
                  SVGGlyphElementBridge glyphBridge = (SVGGlyphElementBridge)this.ctx.getBridge(glyphElement);
                  TextPaintInfo tpi = null;
                  if (aci != null) {
                     aci.setIndex(ci.getIndex());
                     tpi = (TextPaintInfo)aci.getAttribute(PAINT_INFO);
                  }

                  Glyph glyph = glyphBridge.createGlyph(this.ctx, glyphElement, this.textElement, i, this.fontSize, this.fontFace, tpi);
                  glyphs.add(glyph);
                  foundMatchingGlyph = true;
                  break;
               }

               c = ci.setIndex(current);
            }
         }

         if (!foundMatchingGlyph) {
            SVGGlyphElementBridge glyphBridge = (SVGGlyphElementBridge)this.ctx.getBridge(this.missingGlyphElement);
            TextPaintInfo tpi = null;
            if (aci != null) {
               aci.setIndex(ci.getIndex());
               tpi = (TextPaintInfo)aci.getAttribute(PAINT_INFO);
            }

            Glyph glyph = glyphBridge.createGlyph(this.ctx, this.missingGlyphElement, this.textElement, -1, this.fontSize, this.fontFace, tpi);
            glyphs.add(glyph);
         }
      }

      int numGlyphs = glyphs.size();
      Glyph[] glyphArray = (Glyph[])((Glyph[])glyphs.toArray(new Glyph[numGlyphs]));
      return new SVGGVTGlyphVector(this, glyphArray, frc);
   }

   public GVTGlyphVector createGlyphVector(FontRenderContext frc, int[] glyphCodes, CharacterIterator ci) {
      int nGlyphs = glyphCodes.length;
      StringBuffer workBuff = new StringBuffer(nGlyphs);
      int[] var6 = glyphCodes;
      int var7 = glyphCodes.length;

      for(int var8 = 0; var8 < var7; ++var8) {
         int glyphCode = var6[var8];
         workBuff.append(this.glyphUnicodes[glyphCode]);
      }

      StringCharacterIterator sci = new StringCharacterIterator(workBuff.toString());
      return this.createGlyphVector(frc, (CharacterIterator)sci);
   }

   public GVTGlyphVector createGlyphVector(FontRenderContext frc, String str) {
      StringCharacterIterator sci = new StringCharacterIterator(str);
      return this.createGlyphVector(frc, (CharacterIterator)sci);
   }

   public GVTFont deriveFont(float size) {
      return new SVGGVTFont(size, this.fontFace, this.glyphUnicodes, this.glyphNames, this.glyphLangs, this.glyphOrientations, this.glyphForms, this.ctx, this.glyphElements, this.missingGlyphElement, this.hkernElements, this.vkernElements, this.textElement);
   }

   public String getFamilyName() {
      return this.fontFace.getFamilyName();
   }

   protected GVTLineMetrics getLineMetrics(int beginIndex, int limit) {
      if (this.lineMetrics != null) {
         return this.lineMetrics;
      } else {
         float fontHeight = this.fontFace.getUnitsPerEm();
         float scale = this.fontSize / fontHeight;
         float ascent = this.fontFace.getAscent() * scale;
         float descent = this.fontFace.getDescent() * scale;
         float[] baselineOffsets = new float[]{0.0F, (ascent + descent) / 2.0F - ascent, -ascent};
         float stOffset = this.fontFace.getStrikethroughPosition() * -scale;
         float stThickness = this.fontFace.getStrikethroughThickness() * scale;
         float ulOffset = this.fontFace.getUnderlinePosition() * scale;
         float ulThickness = this.fontFace.getUnderlineThickness() * scale;
         float olOffset = this.fontFace.getOverlinePosition() * -scale;
         float olThickness = this.fontFace.getOverlineThickness() * scale;
         this.lineMetrics = new GVTLineMetrics(ascent, 0, baselineOffsets, descent, fontHeight, fontHeight, limit - beginIndex, stOffset, stThickness, ulOffset, ulThickness, olOffset, olThickness);
         return this.lineMetrics;
      }
   }

   public GVTLineMetrics getLineMetrics(char[] chars, int beginIndex, int limit, FontRenderContext frc) {
      return this.getLineMetrics(beginIndex, limit);
   }

   public GVTLineMetrics getLineMetrics(CharacterIterator ci, int beginIndex, int limit, FontRenderContext frc) {
      return this.getLineMetrics(beginIndex, limit);
   }

   public GVTLineMetrics getLineMetrics(String str, FontRenderContext frc) {
      StringCharacterIterator sci = new StringCharacterIterator(str);
      return this.getLineMetrics((CharacterIterator)sci, 0, str.length(), frc);
   }

   public GVTLineMetrics getLineMetrics(String str, int beginIndex, int limit, FontRenderContext frc) {
      StringCharacterIterator sci = new StringCharacterIterator(str);
      return this.getLineMetrics((CharacterIterator)sci, beginIndex, limit, frc);
   }

   public float getSize() {
      return this.fontSize;
   }

   public String toString() {
      return this.fontFace.getFamilyName() + " " + this.fontFace.getFontWeight() + " " + this.fontFace.getFontStyle();
   }

   static {
      PAINT_INFO = GVTAttributedCharacterIterator.TextAttribute.PAINT_INFO;
   }
}
