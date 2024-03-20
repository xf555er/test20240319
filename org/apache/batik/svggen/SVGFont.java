package org.apache.batik.svggen;

import java.awt.Font;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphMetrics;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.Map;
import org.apache.batik.ext.awt.g2d.GraphicContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class SVGFont extends AbstractSVGConverter {
   public static final float EXTRA_LIGHT;
   public static final float LIGHT;
   public static final float DEMILIGHT;
   public static final float REGULAR;
   public static final float SEMIBOLD;
   public static final float MEDIUM;
   public static final float DEMIBOLD;
   public static final float BOLD;
   public static final float HEAVY;
   public static final float EXTRABOLD;
   public static final float ULTRABOLD;
   public static final float POSTURE_REGULAR;
   public static final float POSTURE_OBLIQUE;
   static final float[] fontStyles;
   static final String[] svgStyles;
   static final float[] fontWeights;
   static final String[] svgWeights;
   static Map logicalFontMap;
   static final int COMMON_FONT_SIZE = 100;
   final Map fontStringMap = new HashMap();

   public SVGFont(SVGGeneratorContext generatorContext) {
      super(generatorContext);
   }

   public void recordFontUsage(String string, Font font) {
      Font commonSizeFont = createCommonSizeFont(font);
      String fontKey = commonSizeFont.getFamily() + commonSizeFont.getStyle();
      CharListHelper chl = (CharListHelper)this.fontStringMap.get(fontKey);
      if (chl == null) {
         chl = new CharListHelper();
      }

      for(int i = 0; i < string.length(); ++i) {
         char ch = string.charAt(i);
         chl.add(ch);
      }

      this.fontStringMap.put(fontKey, chl);
   }

   private static Font createCommonSizeFont(Font font) {
      Map attributes = new HashMap();
      attributes.put(TextAttribute.SIZE, 100.0F);
      attributes.put(TextAttribute.TRANSFORM, (Object)null);
      return font.deriveFont(attributes);
   }

   public SVGDescriptor toSVG(GraphicContext gc) {
      return this.toSVG(gc.getFont(), gc.getFontRenderContext());
   }

   public SVGFontDescriptor toSVG(Font font, FontRenderContext frc) {
      FontRenderContext localFRC = new FontRenderContext(new AffineTransform(), frc.isAntiAliased(), frc.usesFractionalMetrics());
      String fontSize = this.doubleString((double)font.getSize2D()) + "px";
      String fontWeight = weightToSVG(font);
      String fontStyle = styleToSVG(font);
      String fontFamilyStr = familyToSVG(font);
      Font commonSizeFont = createCommonSizeFont(font);
      String fontKey = commonSizeFont.getFamily() + commonSizeFont.getStyle();
      CharListHelper clh = (CharListHelper)this.fontStringMap.get(fontKey);
      if (clh == null) {
         return new SVGFontDescriptor(fontSize, fontWeight, fontStyle, fontFamilyStr, (Element)null);
      } else {
         Document domFactory = this.generatorContext.domFactory;
         SVGFontDescriptor fontDesc = (SVGFontDescriptor)this.descMap.get(fontKey);
         Element fontDef;
         if (fontDesc != null) {
            fontDef = fontDesc.getDef();
         } else {
            fontDef = domFactory.createElementNS("http://www.w3.org/2000/svg", "font");
            Element fontFace = domFactory.createElementNS("http://www.w3.org/2000/svg", "font-face");
            String svgFontFamilyString = fontFamilyStr;
            if (fontFamilyStr.startsWith("'") && fontFamilyStr.endsWith("'")) {
               svgFontFamilyString = fontFamilyStr.substring(1, fontFamilyStr.length() - 1);
            }

            fontFace.setAttributeNS((String)null, "font-family", svgFontFamilyString);
            fontFace.setAttributeNS((String)null, "font-weight", fontWeight);
            fontFace.setAttributeNS((String)null, "font-style", fontStyle);
            fontFace.setAttributeNS((String)null, "units-per-em", "100");
            fontDef.appendChild(fontFace);
            Element missingGlyphElement = domFactory.createElementNS("http://www.w3.org/2000/svg", "missing-glyph");
            int[] missingGlyphCode = new int[]{commonSizeFont.getMissingGlyphCode()};
            GlyphVector gv = commonSizeFont.createGlyphVector(localFRC, missingGlyphCode);
            Shape missingGlyphShape = gv.getGlyphOutline(0);
            GlyphMetrics gm = gv.getGlyphMetrics(0);
            AffineTransform at = AffineTransform.getScaleInstance(1.0, -1.0);
            missingGlyphShape = at.createTransformedShape(missingGlyphShape);
            missingGlyphElement.setAttributeNS((String)null, "d", SVGPath.toSVGPathData(missingGlyphShape, this.generatorContext));
            missingGlyphElement.setAttributeNS((String)null, "horiz-adv-x", String.valueOf(gm.getAdvance()));
            fontDef.appendChild(missingGlyphElement);
            fontDef.setAttributeNS((String)null, "horiz-adv-x", String.valueOf(gm.getAdvance()));
            LineMetrics lm = commonSizeFont.getLineMetrics("By", localFRC);
            fontFace.setAttributeNS((String)null, "ascent", String.valueOf(lm.getAscent()));
            fontFace.setAttributeNS((String)null, "descent", String.valueOf(lm.getDescent()));
            fontDef.setAttributeNS((String)null, "id", this.generatorContext.idGenerator.generateID("font"));
         }

         String textUsingFont = clh.getNewChars();
         clh.clearNewChars();

         for(int i = textUsingFont.length() - 1; i >= 0; --i) {
            char c = textUsingFont.charAt(i);
            String searchStr = String.valueOf(c);
            boolean foundGlyph = false;
            NodeList fontChildren = fontDef.getChildNodes();

            for(int j = 0; j < fontChildren.getLength(); ++j) {
               if (fontChildren.item(j) instanceof Element) {
                  Element childElement = (Element)fontChildren.item(j);
                  if (childElement.getAttributeNS((String)null, "unicode").equals(searchStr)) {
                     foundGlyph = true;
                     break;
                  }
               }
            }

            if (foundGlyph) {
               break;
            }

            Element glyphElement = domFactory.createElementNS("http://www.w3.org/2000/svg", "glyph");
            GlyphVector gv = commonSizeFont.createGlyphVector(localFRC, "" + c);
            Shape glyphShape = gv.getGlyphOutline(0);
            GlyphMetrics gm = gv.getGlyphMetrics(0);
            AffineTransform at = AffineTransform.getScaleInstance(1.0, -1.0);
            glyphShape = at.createTransformedShape(glyphShape);
            glyphElement.setAttributeNS((String)null, "d", SVGPath.toSVGPathData(glyphShape, this.generatorContext));
            glyphElement.setAttributeNS((String)null, "horiz-adv-x", String.valueOf(gm.getAdvance()));
            glyphElement.setAttributeNS((String)null, "unicode", String.valueOf(c));
            fontDef.appendChild(glyphElement);
         }

         SVGFontDescriptor newFontDesc = new SVGFontDescriptor(fontSize, fontWeight, fontStyle, fontFamilyStr, fontDef);
         if (fontDesc == null) {
            this.descMap.put(fontKey, newFontDesc);
            this.defSet.add(fontDef);
         }

         return newFontDesc;
      }
   }

   public static String familyToSVG(Font font) {
      String fontFamilyStr = font.getFamily();
      String logicalFontFamily = (String)logicalFontMap.get(font.getName().toLowerCase());
      if (logicalFontFamily != null) {
         fontFamilyStr = logicalFontFamily;
      } else {
         char QUOTE = true;
         fontFamilyStr = '\'' + fontFamilyStr + '\'';
      }

      return fontFamilyStr;
   }

   public static String styleToSVG(Font font) {
      Map attrMap = font.getAttributes();
      Float styleValue = (Float)attrMap.get(TextAttribute.POSTURE);
      if (styleValue == null) {
         if (font.isItalic()) {
            styleValue = TextAttribute.POSTURE_OBLIQUE;
         } else {
            styleValue = TextAttribute.POSTURE_REGULAR;
         }
      }

      float style = styleValue;
      int i = false;

      int i;
      for(i = 0; i < fontStyles.length && !(style <= fontStyles[i]); ++i) {
      }

      return svgStyles[i];
   }

   public static String weightToSVG(Font font) {
      Map attrMap = font.getAttributes();
      Float weightValue = (Float)attrMap.get(TextAttribute.WEIGHT);
      if (weightValue == null) {
         if (font.isBold()) {
            weightValue = TextAttribute.WEIGHT_BOLD;
         } else {
            weightValue = TextAttribute.WEIGHT_REGULAR;
         }
      }

      float weight = weightValue;
      int i = false;

      int i;
      for(i = 0; i < fontWeights.length && !(weight <= fontWeights[i]); ++i) {
      }

      return svgWeights[i];
   }

   static {
      EXTRA_LIGHT = TextAttribute.WEIGHT_EXTRA_LIGHT;
      LIGHT = TextAttribute.WEIGHT_LIGHT;
      DEMILIGHT = TextAttribute.WEIGHT_DEMILIGHT;
      REGULAR = TextAttribute.WEIGHT_REGULAR;
      SEMIBOLD = TextAttribute.WEIGHT_SEMIBOLD;
      MEDIUM = TextAttribute.WEIGHT_MEDIUM;
      DEMIBOLD = TextAttribute.WEIGHT_DEMIBOLD;
      BOLD = TextAttribute.WEIGHT_BOLD;
      HEAVY = TextAttribute.WEIGHT_HEAVY;
      EXTRABOLD = TextAttribute.WEIGHT_EXTRABOLD;
      ULTRABOLD = TextAttribute.WEIGHT_ULTRABOLD;
      POSTURE_REGULAR = TextAttribute.POSTURE_REGULAR;
      POSTURE_OBLIQUE = TextAttribute.POSTURE_OBLIQUE;
      fontStyles = new float[]{POSTURE_REGULAR + (POSTURE_OBLIQUE - POSTURE_REGULAR) / 2.0F};
      svgStyles = new String[]{"normal", "italic"};
      fontWeights = new float[]{EXTRA_LIGHT + (LIGHT - EXTRA_LIGHT) / 2.0F, LIGHT + (DEMILIGHT - LIGHT) / 2.0F, DEMILIGHT + (REGULAR - DEMILIGHT) / 2.0F, REGULAR + (SEMIBOLD - REGULAR) / 2.0F, SEMIBOLD + (MEDIUM - SEMIBOLD) / 2.0F, MEDIUM + (DEMIBOLD - MEDIUM) / 2.0F, DEMIBOLD + (BOLD - DEMIBOLD) / 2.0F, BOLD + (HEAVY - BOLD) / 2.0F, HEAVY + (EXTRABOLD - HEAVY) / 2.0F, EXTRABOLD + (ULTRABOLD - EXTRABOLD)};
      svgWeights = new String[]{"100", "200", "300", "normal", "500", "500", "600", "bold", "800", "800", "900"};
      logicalFontMap = new HashMap();
      logicalFontMap.put("dialog", "sans-serif");
      logicalFontMap.put("dialoginput", "monospace");
      logicalFontMap.put("monospaced", "monospace");
      logicalFontMap.put("serif", "serif");
      logicalFontMap.put("sansserif", "sans-serif");
      logicalFontMap.put("symbol", "'WingDings'");
   }

   private static class CharListHelper {
      private int nUsed = 0;
      private int[] charList = new int[40];
      private StringBuffer freshChars = new StringBuffer(40);

      CharListHelper() {
      }

      String getNewChars() {
         return this.freshChars.toString();
      }

      void clearNewChars() {
         this.freshChars = new StringBuffer(40);
      }

      boolean add(int c) {
         int pos = binSearch(this.charList, this.nUsed, c);
         if (pos >= 0) {
            return false;
         } else {
            if (this.nUsed == this.charList.length) {
               int[] t = new int[this.nUsed + 20];
               System.arraycopy(this.charList, 0, t, 0, this.nUsed);
               this.charList = t;
            }

            pos = -pos - 1;
            System.arraycopy(this.charList, pos, this.charList, pos + 1, this.nUsed - pos);
            this.charList[pos] = c;
            this.freshChars.append((char)c);
            ++this.nUsed;
            return true;
         }
      }

      static int binSearch(int[] list, int nUsed, int chr) {
         int low = 0;
         int high = nUsed - 1;

         while(low <= high) {
            int mid = low + high >>> 1;
            int midVal = list[mid];
            if (midVal < chr) {
               low = mid + 1;
            } else {
               if (midVal <= chr) {
                  return mid;
               }

               high = mid - 1;
            }
         }

         return -(low + 1);
      }
   }
}
