package org.apache.fop.afp.fonts;

import org.apache.fop.afp.AFPEventProducer;

public abstract class AbstractOutlineFont extends AFPFont {
   protected CharacterSet charSet;
   private final AFPEventProducer eventProducer;

   public AbstractOutlineFont(String name, boolean embeddable, CharacterSet charSet, AFPEventProducer eventProducer) {
      super(name, embeddable);
      this.charSet = charSet;
      this.eventProducer = eventProducer;
   }

   AFPEventProducer getAFPEventProducer() {
      return this.eventProducer;
   }

   public CharacterSet getCharacterSet() {
      return this.charSet;
   }

   public CharacterSet getCharacterSet(int size) {
      return this.charSet;
   }

   public int getAscender(int size) {
      return this.charSet.getAscender() * size;
   }

   public int getUnderlinePosition(int size) {
      return this.charSet.getUnderscorePosition() * size;
   }

   public int getUnderlineThickness(int size) {
      int underscoreWidth = this.charSet.getUnderscoreWidth();
      return underscoreWidth == 0 ? super.getUnderlineThickness(size) : underscoreWidth * size;
   }

   public int getCapHeight(int size) {
      return this.charSet.getCapHeight() * size;
   }

   public int getDescender(int size) {
      return this.charSet.getDescender() * size;
   }

   public int getXHeight(int size) {
      return this.charSet.getXHeight() * size;
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
