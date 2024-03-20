package org.apache.fop.fonts.substitute;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.fonts.FontUtil;

public class FontQualifier {
   private static Log log = LogFactory.getLog(FontQualifier.class);
   private AttributeValue fontFamilyAttributeValue;
   private AttributeValue fontStyleAttributeValue;
   private AttributeValue fontWeightAttributeValue;

   public void setFontFamily(String fontFamily) {
      AttributeValue fontFamilyAttribute = AttributeValue.valueOf(fontFamily);
      if (fontFamilyAttribute == null) {
         log.error("Invalid font-family value '" + fontFamily + "'");
      } else {
         this.fontFamilyAttributeValue = fontFamilyAttribute;
      }
   }

   public void setFontStyle(String fontStyle) {
      AttributeValue fontStyleAttribute = AttributeValue.valueOf(fontStyle);
      if (fontStyleAttribute != null) {
         this.fontStyleAttributeValue = fontStyleAttribute;
      }

   }

   public void setFontWeight(String fontWeight) {
      AttributeValue fontWeightAttribute = AttributeValue.valueOf(fontWeight);
      if (fontWeightAttribute != null) {
         Iterator var3 = fontWeightAttribute.iterator();

         while(var3.hasNext()) {
            Object weightObj = var3.next();
            if (weightObj instanceof String) {
               String weightString = ((String)weightObj).trim();

               try {
                  FontUtil.parseCSS2FontWeight(weightString);
               } catch (IllegalArgumentException var7) {
                  log.error("Invalid font-weight value '" + weightString + "'");
                  return;
               }
            }
         }

         this.fontWeightAttributeValue = fontWeightAttribute;
      }

   }

   public AttributeValue getFontFamily() {
      return this.fontFamilyAttributeValue;
   }

   public AttributeValue getFontStyle() {
      return this.fontStyleAttributeValue == null ? AttributeValue.valueOf("normal") : this.fontStyleAttributeValue;
   }

   public AttributeValue getFontWeight() {
      return this.fontWeightAttributeValue == null ? AttributeValue.valueOf(Integer.toString(400)) : this.fontWeightAttributeValue;
   }

   public boolean hasFontWeight() {
      return this.fontWeightAttributeValue != null;
   }

   public boolean hasFontStyle() {
      return this.fontStyleAttributeValue != null;
   }

   protected List match(FontInfo fontInfo) {
      AttributeValue fontFamilyValue = this.getFontFamily();
      AttributeValue weightValue = this.getFontWeight();
      AttributeValue styleValue = this.getFontStyle();
      List matchingTriplets = new ArrayList();
      Iterator var6 = fontFamilyValue.iterator();

      label71:
      while(true) {
         String fontFamilyString;
         Map triplets;
         do {
            if (!var6.hasNext()) {
               return matchingTriplets;
            }

            Object aFontFamilyValue = var6.next();
            fontFamilyString = (String)aFontFamilyValue;
            triplets = fontInfo.getFontTriplets();
         } while(triplets == null);

         Set tripletSet = triplets.keySet();
         Iterator var11 = tripletSet.iterator();

         while(true) {
            FontTriplet triplet;
            String fontName;
            do {
               if (!var11.hasNext()) {
                  continue label71;
               }

               Object aTripletSet = var11.next();
               triplet = (FontTriplet)aTripletSet;
               fontName = triplet.getName();
            } while(!fontFamilyString.toLowerCase().equals(fontName.toLowerCase()));

            boolean weightMatched = false;
            int fontWeight = triplet.getWeight();
            Iterator var17 = weightValue.iterator();

            while(var17.hasNext()) {
               Object weightObj = var17.next();
               if (weightObj instanceof FontWeightRange) {
                  FontWeightRange intRange = (FontWeightRange)weightObj;
                  if (intRange.isWithinRange(fontWeight)) {
                     weightMatched = true;
                  }
               } else {
                  int fontWeightValue;
                  if (weightObj instanceof String) {
                     String fontWeightString = (String)weightObj;
                     fontWeightValue = FontUtil.parseCSS2FontWeight(fontWeightString);
                     if (fontWeightValue == fontWeight) {
                        weightMatched = true;
                     }
                  } else if (weightObj instanceof Integer) {
                     Integer fontWeightInteger = (Integer)weightObj;
                     fontWeightValue = fontWeightInteger;
                     if (fontWeightValue == fontWeight) {
                        weightMatched = true;
                     }
                  }
               }
            }

            boolean styleMatched = false;
            String fontStyleString = triplet.getStyle();
            Iterator var26 = styleValue.iterator();

            while(var26.hasNext()) {
               Object aStyleValue = var26.next();
               String style = (String)aStyleValue;
               if (fontStyleString.equals(style)) {
                  styleMatched = true;
               }
            }

            if (weightMatched && styleMatched) {
               matchingTriplets.add(triplet);
            }
         }
      }
   }

   protected FontTriplet bestMatch(FontInfo fontInfo) {
      List matchingTriplets = this.match(fontInfo);
      FontTriplet bestTriplet = null;
      if (matchingTriplets.size() == 1) {
         bestTriplet = (FontTriplet)matchingTriplets.get(0);
      } else {
         Iterator var4 = matchingTriplets.iterator();

         while(var4.hasNext()) {
            Object matchingTriplet = var4.next();
            FontTriplet triplet = (FontTriplet)matchingTriplet;
            if (bestTriplet == null) {
               bestTriplet = triplet;
            } else {
               int priority = triplet.getPriority();
               if (priority < bestTriplet.getPriority()) {
                  bestTriplet = triplet;
               }
            }
         }
      }

      return bestTriplet;
   }

   public List getTriplets() {
      List triplets = new ArrayList();
      AttributeValue fontFamilyValue = this.getFontFamily();
      Iterator var3 = fontFamilyValue.iterator();

      while(var3.hasNext()) {
         Object aFontFamilyValue = var3.next();
         String name = (String)aFontFamilyValue;
         AttributeValue styleValue = this.getFontStyle();
         Iterator var7 = styleValue.iterator();

         label45:
         while(var7.hasNext()) {
            Object aStyleValue = var7.next();
            String style = (String)aStyleValue;
            AttributeValue weightValue = this.getFontWeight();
            Iterator var11 = weightValue.iterator();

            while(true) {
               while(true) {
                  if (!var11.hasNext()) {
                     continue label45;
                  }

                  Object weightObj = var11.next();
                  if (weightObj instanceof FontWeightRange) {
                     FontWeightRange fontWeightRange = (FontWeightRange)weightObj;
                     int[] weightRange = fontWeightRange.toArray();
                     int[] var15 = weightRange;
                     int var16 = weightRange.length;

                     for(int var17 = 0; var17 < var16; ++var17) {
                        int aWeightRange = var15[var17];
                        triplets.add(new FontTriplet(name, style, aWeightRange));
                     }
                  } else {
                     int weight;
                     if (weightObj instanceof String) {
                        String weightString = (String)weightObj;
                        weight = FontUtil.parseCSS2FontWeight(weightString);
                        triplets.add(new FontTriplet(name, style, weight));
                     } else if (weightObj instanceof Integer) {
                        Integer weightInteger = (Integer)weightObj;
                        weight = weightInteger;
                        triplets.add(new FontTriplet(name, style, weight));
                     }
                  }
               }
            }
         }
      }

      return triplets;
   }

   public String toString() {
      String str = "";
      if (this.fontFamilyAttributeValue != null) {
         str = str + "font-family=" + this.fontFamilyAttributeValue;
      }

      if (this.fontStyleAttributeValue != null) {
         if (str.length() > 0) {
            str = str + ", ";
         }

         str = str + "font-style=" + this.fontStyleAttributeValue;
      }

      if (this.fontWeightAttributeValue != null) {
         if (str.length() > 0) {
            str = str + ", ";
         }

         str = str + "font-weight=" + this.fontWeightAttributeValue;
      }

      return str;
   }
}
