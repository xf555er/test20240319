package org.apache.batik.gvt.font;

import org.apache.batik.util.SVGConstants;

public class GVTFontFace implements SVGConstants {
   protected String familyName;
   protected float unitsPerEm;
   protected String fontWeight;
   protected String fontStyle;
   protected String fontVariant;
   protected String fontStretch;
   protected float slope;
   protected String panose1;
   protected float ascent;
   protected float descent;
   protected float strikethroughPosition;
   protected float strikethroughThickness;
   protected float underlinePosition;
   protected float underlineThickness;
   protected float overlinePosition;
   protected float overlineThickness;

   public GVTFontFace(String familyName, float unitsPerEm, String fontWeight, String fontStyle, String fontVariant, String fontStretch, float slope, String panose1, float ascent, float descent, float strikethroughPosition, float strikethroughThickness, float underlinePosition, float underlineThickness, float overlinePosition, float overlineThickness) {
      this.familyName = familyName;
      this.unitsPerEm = unitsPerEm;
      this.fontWeight = fontWeight;
      this.fontStyle = fontStyle;
      this.fontVariant = fontVariant;
      this.fontStretch = fontStretch;
      this.slope = slope;
      this.panose1 = panose1;
      this.ascent = ascent;
      this.descent = descent;
      this.strikethroughPosition = strikethroughPosition;
      this.strikethroughThickness = strikethroughThickness;
      this.underlinePosition = underlinePosition;
      this.underlineThickness = underlineThickness;
      this.overlinePosition = overlinePosition;
      this.overlineThickness = overlineThickness;
   }

   public GVTFontFace(String familyName) {
      this(familyName, 1000.0F, "all", "all", "normal", "normal", 0.0F, "0 0 0 0 0 0 0 0 0 0", 800.0F, 200.0F, 300.0F, 50.0F, -75.0F, 50.0F, 800.0F, 50.0F);
   }

   public String getFamilyName() {
      return this.familyName;
   }

   public boolean hasFamilyName(String family) {
      String ffname = this.familyName;
      if (ffname.length() < family.length()) {
         return false;
      } else {
         ffname = ffname.toLowerCase();
         int idx = ffname.indexOf(family.toLowerCase());
         if (idx == -1) {
            return false;
         } else {
            if (ffname.length() > family.length()) {
               boolean quote = false;
               char c;
               int i;
               if (idx > 0) {
                  c = ffname.charAt(idx - 1);
                  label59:
                  switch (c) {
                     case ' ':
                        i = idx - 2;

                        while(true) {
                           if (i < 0) {
                              break label59;
                           }

                           switch (ffname.charAt(i)) {
                              case ' ':
                                 --i;
                                 break;
                              case '"':
                              case '\'':
                                 quote = true;
                                 break label59;
                              default:
                                 return false;
                           }
                        }
                     case '"':
                     case '\'':
                        quote = true;
                     case ',':
                        break;
                     default:
                        return false;
                  }
               }

               if (idx + family.length() < ffname.length()) {
                  c = ffname.charAt(idx + family.length());
                  switch (c) {
                     case ' ':
                        i = idx + family.length() + 1;

                        while(i < ffname.length()) {
                           switch (ffname.charAt(i)) {
                              case ' ':
                                 ++i;
                                 break;
                              case '"':
                              case '\'':
                                 if (!quote) {
                                    return false;
                                 }

                                 return true;
                              default:
                                 return false;
                           }
                        }

                        return true;
                     case '"':
                     case '\'':
                        if (!quote) {
                           return false;
                        }
                     case ',':
                        break;
                     default:
                        return false;
                  }
               }
            }

            return true;
         }
      }
   }

   public String getFontWeight() {
      return this.fontWeight;
   }

   public String getFontStyle() {
      return this.fontStyle;
   }

   public float getUnitsPerEm() {
      return this.unitsPerEm;
   }

   public float getAscent() {
      return this.ascent;
   }

   public float getDescent() {
      return this.descent;
   }

   public float getStrikethroughPosition() {
      return this.strikethroughPosition;
   }

   public float getStrikethroughThickness() {
      return this.strikethroughThickness;
   }

   public float getUnderlinePosition() {
      return this.underlinePosition;
   }

   public float getUnderlineThickness() {
      return this.underlineThickness;
   }

   public float getOverlinePosition() {
      return this.overlinePosition;
   }

   public float getOverlineThickness() {
      return this.overlineThickness;
   }
}
