package org.apache.fop.fo.properties;

import java.util.List;
import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.util.CompareUtil;

public final class CommonFont {
   private static final PropertyCache CACHE = new PropertyCache();
   private int hash = -1;
   private final FontFamilyProperty fontFamily;
   private final EnumProperty fontSelectionStrategy;
   private final EnumProperty fontStretch;
   private final EnumProperty fontStyle;
   private final EnumProperty fontVariant;
   private final EnumProperty fontWeight;
   public final Length fontSize;
   public final Numeric fontSizeAdjust;

   private CommonFont(FontFamilyProperty fontFamily, EnumProperty fontSelectionStrategy, EnumProperty fontStretch, EnumProperty fontStyle, EnumProperty fontVariant, EnumProperty fontWeight, Length fontSize, Numeric fontSizeAdjust) {
      this.fontFamily = fontFamily;
      this.fontSelectionStrategy = fontSelectionStrategy;
      this.fontStretch = fontStretch;
      this.fontStyle = fontStyle;
      this.fontVariant = fontVariant;
      this.fontWeight = fontWeight;
      this.fontSize = fontSize;
      this.fontSizeAdjust = fontSizeAdjust;
   }

   public static CommonFont getInstance(PropertyList pList) throws PropertyException {
      FontFamilyProperty fontFamily = (FontFamilyProperty)pList.get(101);
      EnumProperty fontSelectionStrategy = (EnumProperty)pList.get(102);
      EnumProperty fontStretch = (EnumProperty)pList.get(105);
      EnumProperty fontStyle = (EnumProperty)pList.get(106);
      EnumProperty fontVariant = (EnumProperty)pList.get(107);
      EnumProperty fontWeight = (EnumProperty)pList.get(108);
      Numeric fontSizeAdjust = pList.get(104).getNumeric();
      Length fontSize = pList.get(103).getLength();
      CommonFont commonFont = new CommonFont(fontFamily, fontSelectionStrategy, fontStretch, fontStyle, fontVariant, fontWeight, fontSize, fontSizeAdjust);
      return (CommonFont)CACHE.fetch(commonFont);
   }

   private String[] getFontFamily() {
      List lst = this.fontFamily.getList();
      String[] fontFamily = new String[lst.size()];
      int i = 0;

      for(int c = lst.size(); i < c; ++i) {
         fontFamily[i] = ((Property)lst.get(i)).getString();
      }

      return fontFamily;
   }

   public String getFirstFontFamily() {
      return ((Property)this.fontFamily.list.get(0)).getString();
   }

   public int getFontSelectionStrategy() {
      return this.fontSelectionStrategy.getEnum();
   }

   public int getFontStretch() {
      return this.fontStretch.getEnum();
   }

   public int getFontStyle() {
      return this.fontStyle.getEnum();
   }

   public int getFontVariant() {
      return this.fontVariant.getEnum();
   }

   public int getFontWeight() {
      return this.fontWeight.getEnum();
   }

   public Length getFontSize() {
      return this.fontSize;
   }

   public Numeric getFontSizeAdjust() {
      return this.fontSizeAdjust;
   }

   public FontTriplet[] getFontState(FontInfo fontInfo) {
      short fw;
      switch (this.fontWeight.getEnum()) {
         case 167:
            fw = 100;
            break;
         case 168:
            fw = 200;
            break;
         case 169:
            fw = 300;
            break;
         case 170:
            fw = 400;
            break;
         case 171:
            fw = 500;
            break;
         case 172:
            fw = 600;
            break;
         case 173:
            fw = 700;
            break;
         case 174:
            fw = 800;
            break;
         case 175:
            fw = 900;
            break;
         default:
            fw = 400;
      }

      String style;
      switch (this.fontStyle.getEnum()) {
         case 162:
            style = "italic";
            break;
         case 163:
            style = "oblique";
            break;
         case 164:
            style = "backslant";
            break;
         default:
            style = "normal";
      }

      FontTriplet[] triplets = fontInfo.fontLookup((String[])this.getFontFamily(), style, fw);
      return triplets;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof CommonFont)) {
         return false;
      } else {
         CommonFont other = (CommonFont)obj;
         return CompareUtil.equal(this.fontFamily, other.fontFamily) && CompareUtil.equal(this.fontSelectionStrategy, other.fontSelectionStrategy) && CompareUtil.equal(this.fontSize, other.fontSize) && CompareUtil.equal(this.fontSizeAdjust, other.fontSizeAdjust) && CompareUtil.equal(this.fontStretch, other.fontStretch) && CompareUtil.equal(this.fontStyle, other.fontStyle) && CompareUtil.equal(this.fontVariant, other.fontVariant) && CompareUtil.equal(this.fontWeight, other.fontWeight);
      }
   }

   public int hashCode() {
      if (this.hash == -1) {
         int hash = 17;
         hash = 37 * hash + CompareUtil.getHashCode(this.fontSize);
         hash = 37 * hash + CompareUtil.getHashCode(this.fontSizeAdjust);
         hash = 37 * hash + CompareUtil.getHashCode(this.fontFamily);
         hash = 37 * hash + CompareUtil.getHashCode(this.fontSelectionStrategy);
         hash = 37 * hash + CompareUtil.getHashCode(this.fontStretch);
         hash = 37 * hash + CompareUtil.getHashCode(this.fontStyle);
         hash = 37 * hash + CompareUtil.getHashCode(this.fontVariant);
         hash = 37 * hash + CompareUtil.getHashCode(this.fontWeight);
         this.hash = hash;
      }

      return this.hash;
   }
}
