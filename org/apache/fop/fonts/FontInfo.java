package org.apache.fop.fonts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FontInfo {
   protected static final Log log = LogFactory.getLog(FontInfo.class);
   private Map usedFonts = new HashMap();
   private Map triplets = new HashMap();
   private Map tripletPriorities = new HashMap();
   private Map fonts = new HashMap();
   private Map fontInstanceCache;
   private FontEventListener eventListener;

   public void setEventListener(FontEventListener listener) {
      this.eventListener = listener;
   }

   public boolean isSetupValid() {
      this.tripletPriorities = null;
      return this.triplets.containsKey(Font.DEFAULT_FONT);
   }

   public void addFontProperties(String name, String family, String style, int weight) {
      this.addFontProperties(name, createFontKey(family, style, weight));
   }

   public void addFontProperties(String name, String[] families, String style, int weight) {
      String[] var5 = families;
      int var6 = families.length;

      for(int var7 = 0; var7 < var6; ++var7) {
         String family = var5[var7];
         this.addFontProperties(name, family, style, weight);
      }

   }

   public void addFontProperties(String internalFontKey, FontTriplet triplet) {
      if (log.isDebugEnabled()) {
         log.debug("Registering: " + triplet + " under " + internalFontKey);
      }

      String oldName = (String)this.triplets.get(triplet);
      int newPriority = triplet.getPriority();
      if (oldName != null) {
         int oldPriority = (Integer)this.tripletPriorities.get(triplet);
         if (oldPriority < newPriority) {
            this.logDuplicateFont(triplet, false, oldName, oldPriority, internalFontKey, newPriority);
            return;
         }

         this.logDuplicateFont(triplet, true, oldName, oldPriority, internalFontKey, newPriority);
      }

      this.triplets.put(triplet, internalFontKey);
      this.tripletPriorities.put(triplet, newPriority);
   }

   private void logDuplicateFont(FontTriplet triplet, boolean replacing, String oldKey, int oldPriority, String newKey, int newPriority) {
      if (log.isDebugEnabled()) {
         log.debug(triplet + (replacing ? ": Replacing " : ": Not replacing ") + ((Typeface)this.fonts.get(this.triplets.get(triplet))).getFullName() + " (priority=" + oldPriority + ") by " + ((Typeface)this.fonts.get(newKey)).getFullName() + " (priority=" + newPriority + ")");
      }

   }

   public void addMetrics(String internalFontKey, FontMetrics metrics) {
      if (metrics instanceof Typeface) {
         ((Typeface)metrics).setEventListener(this.eventListener);
      }

      this.fonts.put(internalFontKey, (Typeface)metrics);
   }

   private FontTriplet fontLookup(String family, String style, int weight, boolean substitutable) {
      if (log.isTraceEnabled()) {
         log.trace("Font lookup: " + family + " " + style + " " + weight + (substitutable ? " substitutable" : ""));
      }

      FontTriplet startKey = createFontKey(family, style, weight);
      FontTriplet fontTriplet = startKey;
      String internalFontKey = this.getInternalFontKey(startKey);
      if (internalFontKey == null) {
         fontTriplet = this.fuzzyFontLookup(family, style, weight, startKey, substitutable);
      }

      if (fontTriplet != null) {
         if (fontTriplet != startKey) {
            this.notifyFontReplacement(startKey, fontTriplet);
         }

         return fontTriplet;
      } else {
         return null;
      }
   }

   private FontTriplet fuzzyFontLookup(String family, String style, int weight, FontTriplet startKey, boolean substitutable) {
      String internalFontKey = null;
      FontTriplet key;
      if (!family.equals(startKey.getName())) {
         key = createFontKey(family, style, weight);
         internalFontKey = this.getInternalFontKey(key);
         if (internalFontKey != null) {
            return key;
         }
      }

      key = this.findAdjustWeight(family, style, weight);
      if (key != null) {
         internalFontKey = this.getInternalFontKey(key);
      }

      if (!substitutable && internalFontKey == null) {
         return null;
      } else {
         if (internalFontKey == null && !style.equals("normal")) {
            key = createFontKey(family, "normal", weight);
            internalFontKey = this.getInternalFontKey(key);
         }

         if (internalFontKey == null && !style.equals("normal")) {
            key = this.findAdjustWeight(family, "normal", weight);
            if (key != null) {
               internalFontKey = this.getInternalFontKey(key);
            }
         }

         if (internalFontKey == null) {
            return this.fuzzyFontLookup("any", style, weight, startKey, false);
         } else {
            if (key == null && internalFontKey == null) {
               key = Font.DEFAULT_FONT;
               internalFontKey = this.getInternalFontKey(key);
            }

            return internalFontKey != null ? key : null;
         }
      }
   }

   public void useFont(String internalName) {
      this.usedFonts.put(internalName, this.fonts.get(internalName));
   }

   private Map getFontInstanceCache() {
      if (this.fontInstanceCache == null) {
         this.fontInstanceCache = new HashMap();
      }

      return this.fontInstanceCache;
   }

   public Font getFontInstance(FontTriplet triplet, int fontSize) {
      Map sizes = (Map)this.getFontInstanceCache().get(triplet);
      if (sizes == null) {
         sizes = new HashMap();
         this.getFontInstanceCache().put(triplet, sizes);
      }

      Integer size = fontSize;
      Font font = (Font)((Map)sizes).get(size);
      if (font == null) {
         String fontKey = this.getInternalFontKey(triplet);
         this.useFont(fontKey);
         FontMetrics metrics = this.getMetricsFor(fontKey);
         font = new Font(fontKey, triplet, metrics, fontSize);
         ((Map)sizes).put(size, font);
      }

      return font;
   }

   private List getTripletsForName(String fontName) {
      List matchedTriplets = new ArrayList();
      Iterator var3 = this.triplets.keySet().iterator();

      while(var3.hasNext()) {
         FontTriplet triplet = (FontTriplet)var3.next();
         String tripletName = triplet.getName();
         if (tripletName.toLowerCase().equals(fontName.toLowerCase())) {
            matchedTriplets.add(triplet);
         }
      }

      return matchedTriplets;
   }

   public Font getFontInstanceForAWTFont(java.awt.Font awtFont) {
      String awtFontName = awtFont.getName();
      String awtFontFamily = awtFont.getFamily();
      String awtFontStyle = awtFont.isItalic() ? "italic" : "normal";
      int awtFontWeight = awtFont.isBold() ? 700 : 400;
      FontTriplet matchedTriplet = null;
      List triplets = this.getTripletsForName(awtFontName);
      if (!triplets.isEmpty()) {
         Iterator var8 = triplets.iterator();

         while(var8.hasNext()) {
            FontTriplet triplet = (FontTriplet)var8.next();
            boolean styleMatched = triplet.getStyle().equals(awtFontStyle);
            boolean weightMatched = triplet.getWeight() == awtFontWeight;
            if (styleMatched && weightMatched) {
               matchedTriplet = triplet;
               break;
            }
         }
      }

      if (matchedTriplet == null) {
         if (awtFontFamily.equals("sanserif")) {
            awtFontFamily = "sans-serif";
         }

         matchedTriplet = this.fontLookup(awtFontFamily, awtFontStyle, awtFontWeight);
      }

      int fontSize = Math.round(awtFont.getSize2D() * 1000.0F);
      return this.getFontInstance(matchedTriplet, fontSize);
   }

   public FontTriplet fontLookup(String family, String style, int weight) {
      return this.fontLookup(family, style, weight, true);
   }

   private List fontLookup(String[] families, String style, int weight, boolean substitutable) {
      List matchingTriplets = new ArrayList();
      FontTriplet triplet = null;
      String[] var7 = families;
      int var8 = families.length;

      for(int var9 = 0; var9 < var8; ++var9) {
         String family = var7[var9];
         triplet = this.fontLookup(family, style, weight, substitutable);
         if (triplet != null) {
            matchingTriplets.add(triplet);
         }
      }

      return matchingTriplets;
   }

   public FontTriplet[] fontLookup(String[] families, String style, int weight) {
      if (families.length == 0) {
         throw new IllegalArgumentException("Specify at least one font family");
      } else {
         List matchedTriplets = this.fontLookup(families, style, weight, false);
         if (matchedTriplets.size() == 0) {
            matchedTriplets = this.fontLookup(families, style, weight, true);
         }

         if (matchedTriplets.size() == 0) {
            StringBuffer sb = new StringBuffer();
            int i = 0;

            for(int c = families.length; i < c; ++i) {
               if (i > 0) {
                  sb.append(", ");
               }

               sb.append(families[i]);
            }

            throw new IllegalStateException("fontLookup must return an array with at least one FontTriplet on the last call. Lookup: " + sb);
         } else {
            FontTriplet[] fontTriplets = new FontTriplet[matchedTriplets.size()];
            matchedTriplets.toArray(fontTriplets);
            return fontTriplets;
         }
      }
   }

   private void notifyFontReplacement(FontTriplet replacedKey, FontTriplet newKey) {
      if (this.eventListener != null) {
         this.eventListener.fontSubstituted(this, replacedKey, newKey);
      }

   }

   public void notifyStrokingSVGTextAsShapes(String fontFamily) {
      if (this.eventListener != null) {
         this.eventListener.svgTextStrokedAsShapes(this, fontFamily);
      }

   }

   public FontTriplet findAdjustWeight(String family, String style, int weight) {
      FontTriplet key = null;
      String f = null;
      int newWeight = weight;
      if (weight >= 400) {
         if (weight != 400 && weight != 500) {
            if (weight > 500) {
               while(f == null && newWeight < 1000) {
                  newWeight += 100;
                  key = createFontKey(family, style, newWeight);
                  f = this.getInternalFontKey(key);
               }

               for(newWeight = weight; f == null && newWeight > 400; f = this.getInternalFontKey(key)) {
                  newWeight -= 100;
                  key = createFontKey(family, style, newWeight);
               }
            }
         } else {
            key = createFontKey(family, style, 400);
            f = this.getInternalFontKey(key);
         }
      } else {
         label68:
         while(true) {
            if (f != null || newWeight <= 100) {
               newWeight = weight;

               while(true) {
                  if (f != null || newWeight >= 400) {
                     break label68;
                  }

                  newWeight += 100;
                  key = createFontKey(family, style, newWeight);
                  f = this.getInternalFontKey(key);
               }
            }

            newWeight -= 100;
            key = createFontKey(family, style, newWeight);
            f = this.getInternalFontKey(key);
         }
      }

      if (f == null && weight != 400) {
         key = createFontKey(family, style, 400);
         f = this.getInternalFontKey(key);
      }

      return f != null ? key : null;
   }

   public boolean hasFont(String family, String style, int weight) {
      FontTriplet key = createFontKey(family, style, weight);
      return this.triplets.containsKey(key);
   }

   public String getInternalFontKey(FontTriplet triplet) {
      return (String)this.triplets.get(triplet);
   }

   public static FontTriplet createFontKey(String family, String style, int weight) {
      return new FontTriplet(family, style, weight);
   }

   public Map getFonts() {
      return Collections.unmodifiableMap(this.fonts);
   }

   public Map getFontTriplets() {
      return this.triplets;
   }

   public Map getUsedFonts() {
      return this.usedFonts;
   }

   public FontMetrics getMetricsFor(String fontName) {
      Typeface metrics = (Typeface)this.fonts.get(fontName);
      this.usedFonts.put(fontName, metrics);
      return metrics;
   }

   public List getTripletsFor(String fontName) {
      List foundTriplets = new ArrayList();
      Iterator var3 = this.triplets.entrySet().iterator();

      while(var3.hasNext()) {
         Map.Entry tripletEntry = (Map.Entry)var3.next();
         if (fontName.equals(tripletEntry.getValue())) {
            foundTriplets.add(tripletEntry.getKey());
         }
      }

      return foundTriplets;
   }

   public FontTriplet getTripletFor(String fontName) {
      List foundTriplets = this.getTripletsFor(fontName);
      if (foundTriplets.size() > 0) {
         Collections.sort(foundTriplets);
         return (FontTriplet)foundTriplets.get(0);
      } else {
         return null;
      }
   }

   public String getFontStyleFor(String fontName) {
      FontTriplet triplet = this.getTripletFor(fontName);
      return triplet != null ? triplet.getStyle() : "";
   }

   public int getFontWeightFor(String fontName) {
      FontTriplet triplet = this.getTripletFor(fontName);
      return triplet != null ? triplet.getWeight() : 0;
   }

   public void dumpAllTripletsToSystemOut() {
      SortedSet entries = new TreeSet();
      Iterator var2 = this.triplets.keySet().iterator();

      String str;
      while(var2.hasNext()) {
         FontTriplet triplet = (FontTriplet)var2.next();
         str = this.getInternalFontKey(triplet);
         FontMetrics metrics = this.getMetricsFor(str);
         entries.add(triplet.toString() + " -> " + str + " -> " + metrics.getFontName() + "\n");
      }

      StringBuffer stringBuffer = new StringBuffer();
      Iterator var7 = entries.iterator();

      while(var7.hasNext()) {
         str = (String)var7.next();
         stringBuffer.append(str);
      }

      System.out.println(stringBuffer.toString());
   }
}
