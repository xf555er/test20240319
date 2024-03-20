package org.apache.fop.svg.font;

import java.awt.font.TextAttribute;
import java.text.AttributedCharacterIterator;
import java.util.Map;
import org.apache.batik.gvt.font.GVTFontFace;
import org.apache.batik.gvt.font.GVTFontFamily;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.svg.ACIUtils;

public class FOPGVTFontFamily implements GVTFontFamily {
   private final FontInfo fontInfo;
   private final FontTriplet fontTriplet;
   private final String familyName;
   private GVTFontFace fontFace;

   public FOPGVTFontFamily(FontInfo fontInfo, String familyName, FontTriplet triplet, GVTFontFace fontFace) {
      this.fontInfo = fontInfo;
      this.fontTriplet = triplet;
      this.familyName = familyName;
      this.fontFace = fontFace;
   }

   public FontInfo getFontInfo() {
      return this.fontInfo;
   }

   public FontTriplet getFontTriplet() {
      return this.fontTriplet;
   }

   public String getFontKey() {
      return this.fontInfo.getInternalFontKey(this.fontTriplet);
   }

   public String getFamilyName() {
      return this.familyName;
   }

   public GVTFontFace getFontFace() {
      return this.fontFace;
   }

   public FOPGVTFont deriveFont(float size, AttributedCharacterIterator aci) {
      return this.deriveFont(size, aci.getAttributes());
   }

   public FOPGVTFont deriveFont(float size, Map attrs) {
      Float fontWeight = (Float)attrs.get(TextAttribute.WEIGHT);
      int weight = fontWeight == null ? this.fontTriplet.getWeight() : ACIUtils.toCSSWeight(fontWeight);
      Float fontStyle = (Float)attrs.get(TextAttribute.POSTURE);
      String style = fontStyle == null ? this.fontTriplet.getStyle() : ACIUtils.toStyle(fontStyle);
      FontTriplet triplet = this.fontInfo.fontLookup(this.fontTriplet.getName(), style, weight);
      return new FOPGVTFont(this.fontInfo.getFontInstance(triplet, (int)(size * 1000.0F)), this);
   }

   public boolean isComplex() {
      return false;
   }
}
