package org.apache.fop.fo.properties;

import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;

public final class CharacterProperty extends Property {
   private static final PropertyCache CACHE = new PropertyCache();
   private final char character;

   private CharacterProperty(char character) {
      this.character = character;
   }

   public static CharacterProperty getInstance(char character) {
      return (CharacterProperty)CACHE.fetch(new CharacterProperty(character));
   }

   public Object getObject() {
      return this.character;
   }

   public char getCharacter() {
      return this.character;
   }

   public String getString() {
      return Character.toString(this.character);
   }

   public boolean equals(Object obj) {
      if (obj instanceof CharacterProperty) {
         return this.character == ((CharacterProperty)obj).character;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.character;
   }

   public static class Maker extends PropertyMaker {
      public Maker(int propId) {
         super(propId);
      }

      public Property make(PropertyList propertyList, String value, FObj fo) {
         char c = value.charAt(0);
         return CharacterProperty.getInstance(c);
      }
   }
}
