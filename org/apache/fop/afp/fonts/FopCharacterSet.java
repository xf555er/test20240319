package org.apache.fop.afp.fonts;

import java.awt.Rectangle;
import org.apache.fop.afp.AFPEventProducer;
import org.apache.fop.afp.util.AFPResourceAccessor;
import org.apache.fop.fonts.Typeface;

public class FopCharacterSet extends CharacterSet {
   private Typeface charSet;

   public FopCharacterSet(String codePage, String encoding, String name, Typeface charSet, AFPEventProducer eventProducer) {
      super(codePage, encoding, CharacterSetType.SINGLE_BYTE, name, (AFPResourceAccessor)null, eventProducer);
      this.charSet = charSet;
   }

   public FopCharacterSet(String codePage, String encoding, String name, Typeface charSet, AFPResourceAccessor accessor, AFPEventProducer eventProducer) {
      super(codePage, encoding, CharacterSetType.SINGLE_BYTE, name, accessor, eventProducer);
      this.charSet = charSet;
   }

   public int getAscender() {
      return this.charSet.getAscender(1);
   }

   public int getCapHeight() {
      return this.charSet.getCapHeight(1);
   }

   public int getDescender() {
      return this.charSet.getDescender(1);
   }

   public int getXHeight() {
      return this.charSet.getXHeight(1);
   }

   public int getWidth(char character, int size) {
      return this.charSet.getWidth(character, size);
   }

   public Rectangle getCharacterBox(char character, int size) {
      return this.charSet.getBoundingBox(character, size);
   }

   public int getUnderscoreWidth() {
      return this.charSet.getUnderlineThickness(1);
   }

   public int getUnderscorePosition() {
      return this.charSet.getUnderlinePosition(1);
   }

   public char mapChar(char c) {
      return this.charSet.mapChar(c);
   }
}
