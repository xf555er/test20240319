package org.apache.fop.afp.fonts;

import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RasterFont extends AFPFont {
   protected static final Log LOG = LogFactory.getLog("org.apache.fop.afp.fonts");
   private final SortedMap charSets = new TreeMap();
   private Map substitutionCharSets;
   private CharacterSet charSet;

   public RasterFont(String name, boolean embeddable) {
      super(name, embeddable);
   }

   public void addCharacterSet(int size, CharacterSet characterSet) {
      this.charSets.put(size, characterSet);
      this.charSet = characterSet;
   }

   public CharacterSet getCharacterSet(int sizeInMpt) {
      Integer requestedSize = sizeInMpt;
      CharacterSet csm = (CharacterSet)this.charSets.get(requestedSize);
      double sizeInPt = (double)sizeInMpt / 1000.0;
      if (csm != null) {
         return csm;
      } else {
         if (this.substitutionCharSets != null) {
            csm = (CharacterSet)this.substitutionCharSets.get(requestedSize);
         }

         if (csm == null && !this.charSets.isEmpty()) {
            SortedMap smallerSizes = this.charSets.headMap(requestedSize);
            SortedMap largerSizes = this.charSets.tailMap(requestedSize);
            int smallerSize = smallerSizes.isEmpty() ? 0 : (Integer)smallerSizes.lastKey();
            int largerSize = largerSizes.isEmpty() ? Integer.MAX_VALUE : (Integer)largerSizes.firstKey();
            Integer fontSize;
            if (!smallerSizes.isEmpty() && sizeInMpt - smallerSize <= largerSize - sizeInMpt) {
               fontSize = smallerSize;
            } else {
               fontSize = largerSize;
            }

            csm = (CharacterSet)this.charSets.get(fontSize);
            if (csm != null) {
               if (this.substitutionCharSets == null) {
                  this.substitutionCharSets = new HashMap();
               }

               this.substitutionCharSets.put(requestedSize, csm);
               if (!(Math.abs((double)fontSize / 1000.0 - sizeInPt) < 0.1)) {
                  String msg = "No " + sizeInPt + "pt font " + this.getFontName() + " found, substituted with " + (float)fontSize / 1000.0F + "pt font";
                  LOG.warn(msg);
               }
            }
         }

         if (csm == null) {
            String msg = "No font found for font " + this.getFontName() + " with point size " + sizeInPt;
            LOG.error(msg);
            throw new FontRuntimeException(msg);
         } else {
            return csm;
         }
      }
   }

   private int metricsToAbsoluteSize(CharacterSet cs, int value, int givenSize) {
      int nominalVerticalSize = cs.getNominalVerticalSize();
      return nominalVerticalSize != 0 ? value * nominalVerticalSize : value * givenSize;
   }

   private int metricsToAbsoluteSize(CharacterSet cs, double value, int givenSize) {
      int nominalVerticalSize = cs.getNominalVerticalSize();
      return nominalVerticalSize != 0 ? (int)(value * (double)nominalVerticalSize) : (int)(value * (double)givenSize);
   }

   public int getAscender(int size) {
      CharacterSet cs = this.getCharacterSet(size);
      return this.metricsToAbsoluteSize(cs, cs.getAscender(), size);
   }

   public int getUnderlinePosition(int size) {
      CharacterSet cs = this.getCharacterSet(size);
      return this.metricsToAbsoluteSize(cs, cs.getUnderscorePosition(), size);
   }

   public int getUnderlineThickness(int size) {
      CharacterSet cs = this.getCharacterSet(size);
      int underscoreWidth = cs.getUnderscoreWidth();
      return underscoreWidth == 0 ? super.getUnderlineThickness(size) : this.metricsToAbsoluteSize(cs, underscoreWidth, size);
   }

   public int getCapHeight(int size) {
      CharacterSet cs = this.getCharacterSet(size);
      return this.metricsToAbsoluteSize(cs, cs.getCapHeight(), size);
   }

   public int getDescender(int size) {
      CharacterSet cs = this.getCharacterSet(size);
      return this.metricsToAbsoluteSize(cs, cs.getDescender(), size);
   }

   public int getXHeight(int size) {
      CharacterSet cs = this.getCharacterSet(size);
      return this.metricsToAbsoluteSize(cs, cs.getXHeight(), size);
   }

   public int getWidth(int character, int size) {
      CharacterSet cs = this.getCharacterSet(size);
      return this.metricsToAbsoluteSize(cs, cs.getWidth(toUnicodeCodepoint(character), 1), size);
   }

   public Rectangle getBoundingBox(int character, int size) {
      CharacterSet cs = this.getCharacterSet(size);
      Rectangle characterBox = cs.getCharacterBox(toUnicodeCodepoint(character), 1);
      int x = this.metricsToAbsoluteSize(cs, characterBox.getX(), size);
      int y = this.metricsToAbsoluteSize(cs, characterBox.getY(), size);
      int w = this.metricsToAbsoluteSize(cs, characterBox.getWidth(), size);
      int h = this.metricsToAbsoluteSize(cs, characterBox.getHeight(), size);
      return new Rectangle(x, y, w, h);
   }

   public boolean hasChar(char c) {
      return this.charSet.hasChar(c);
   }

   public char mapChar(char c) {
      return this.charSet.mapChar(c);
   }

   public String getEncodingName() {
      return this.charSet.getEncoding();
   }
}
