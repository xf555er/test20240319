package org.apache.fop.afp.fonts;

import java.awt.Rectangle;
import java.lang.Character.UnicodeBlock;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.afp.AFPEventProducer;

public class DoubleByteFont extends AbstractOutlineFont {
   private static final Log log = LogFactory.getLog(DoubleByteFont.class);
   private final Set charsProcessed = new HashSet();
   private static final Set IDEOGRAPHIC = new HashSet();

   public DoubleByteFont(String name, boolean embeddable, CharacterSet charSet, AFPEventProducer eventProducer) {
      super(name, embeddable, charSet, eventProducer);
   }

   public int getWidth(int character, int size) {
      int charWidth;
      try {
         charWidth = this.charSet.getWidth(toUnicodeCodepoint(character), size);
      } catch (IllegalArgumentException var5) {
         if (!this.charsProcessed.contains(character)) {
            this.charsProcessed.add(character);
            this.getAFPEventProducer().charactersetMissingMetrics(this, (char)character, this.charSet.getName().trim());
         }

         charWidth = -1;
      }

      if (charWidth == -1) {
         charWidth = this.getDefaultCharacterWidth(character) * size;
      }

      return charWidth;
   }

   private int getDefaultCharacterWidth(int character) {
      int nominalCharIncrement = this.charSet.getNominalCharIncrement();
      return nominalCharIncrement > 0 ? nominalCharIncrement : this.inferCharWidth(character);
   }

   public Rectangle getBoundingBox(int character, int size) {
      Rectangle characterBox = this.getBoundingBoxOrNull(character, size);
      if (characterBox == null) {
         characterBox = this.getDefaultCharacterBox(character, size);
      }

      return characterBox;
   }

   private Rectangle getBoundingBoxOrNull(int character, int size) {
      Rectangle characterBox = null;

      try {
         characterBox = this.charSet.getCharacterBox(toUnicodeCodepoint(character), size);
      } catch (IllegalArgumentException var5) {
         if (!this.charsProcessed.contains(character)) {
            this.charsProcessed.add(character);
            this.getAFPEventProducer().charactersetMissingMetrics(this, (char)character, this.charSet.getName().trim());
         }
      }

      return characterBox;
   }

   private Rectangle getDefaultCharacterBox(int character, int size) {
      return this.getBoundingBoxOrNull(45, size);
   }

   private int inferCharWidth(int character) {
      boolean isIdeographic = false;
      Character.UnicodeBlock charBlock = UnicodeBlock.of((char)character);
      if (charBlock == null) {
         isIdeographic = false;
      } else if (IDEOGRAPHIC.contains(charBlock)) {
         isIdeographic = true;
      } else {
         isIdeographic = false;
      }

      return isIdeographic ? this.charSet.getEmSpaceIncrement() : this.charSet.getSpaceIncrement();
   }

   static {
      IDEOGRAPHIC.add(UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS);
      IDEOGRAPHIC.add(UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS);
      IDEOGRAPHIC.add(UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A);
   }
}
