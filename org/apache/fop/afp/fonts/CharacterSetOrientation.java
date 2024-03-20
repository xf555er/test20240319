package org.apache.fop.afp.fonts;

import java.awt.Rectangle;

public class CharacterSetOrientation {
   private int ascender;
   private int descender;
   private int capHeight;
   private IntegerKeyStore characterMetrics;
   private int xHeight;
   private final int orientation;
   private final int spaceIncrement;
   private final int emSpaceIncrement;
   private final int nomCharIncrement;
   private int underscoreWidth;
   private int underscorePosition;

   public CharacterSetOrientation(int orientation, int spaceIncrement, int emSpaceIncrement, int nomCharIncrement) {
      this.orientation = orientation;
      this.spaceIncrement = spaceIncrement;
      this.emSpaceIncrement = emSpaceIncrement;
      this.nomCharIncrement = nomCharIncrement;
      this.characterMetrics = new IntegerKeyStore();
   }

   public int getAscender() {
      return this.ascender;
   }

   public int getCapHeight() {
      return this.capHeight;
   }

   public int getDescender() {
      return this.descender;
   }

   public int getUnderscoreWidth() {
      return this.underscoreWidth;
   }

   public int getUnderscorePosition() {
      return this.underscorePosition;
   }

   public int getOrientation() {
      return this.orientation;
   }

   public int getXHeight() {
      return this.xHeight;
   }

   public int getWidth(char character, int size) {
      CharacterMetrics cm = this.getCharacterMetrics(character);
      return cm == null ? -1 : size * cm.width;
   }

   private CharacterMetrics getCharacterMetrics(char character) {
      return (CharacterMetrics)this.characterMetrics.get(Integer.valueOf(character));
   }

   public Rectangle getCharacterBox(char character, int size) {
      CharacterMetrics cm = this.getCharacterMetrics(character);
      return scale(cm == null ? this.getFallbackCharacterBox() : cm.characterBox, size);
   }

   private static Rectangle scale(Rectangle rectangle, int size) {
      return rectangle == null ? null : new Rectangle((int)((double)size * rectangle.getX()), (int)((double)size * rectangle.getY()), (int)((double)size * rectangle.getWidth()), (int)((double)size * rectangle.getHeight()));
   }

   private Rectangle getFallbackCharacterBox() {
      return new Rectangle(0, 0, 0, 0);
   }

   public void setAscender(int ascender) {
      this.ascender = ascender;
   }

   public void setCapHeight(int capHeight) {
      this.capHeight = capHeight;
   }

   public void setDescender(int descender) {
      this.descender = descender;
   }

   public void setUnderscoreWidth(int underscoreWidth) {
      this.underscoreWidth = underscoreWidth;
   }

   public void setUnderscorePosition(int underscorePosition) {
      this.underscorePosition = underscorePosition;
   }

   public void setCharacterMetrics(char character, int width, Rectangle characterBox) {
      this.characterMetrics.put(Integer.valueOf(character), new CharacterMetrics(width, characterBox));
   }

   public void setXHeight(int xHeight) {
      this.xHeight = xHeight;
   }

   public int getSpaceIncrement() {
      return this.spaceIncrement;
   }

   public int getEmSpaceIncrement() {
      return this.emSpaceIncrement;
   }

   public int getNominalCharIncrement() {
      return this.nomCharIncrement;
   }

   private static class CharacterMetrics {
      public final int width;
      public final Rectangle characterBox;

      public CharacterMetrics(int width, Rectangle characterBox) {
         this.width = width;
         this.characterBox = characterBox;
      }
   }
}
