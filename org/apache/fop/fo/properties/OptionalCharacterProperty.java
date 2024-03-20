package org.apache.fop.fo.properties;

import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;

public final class OptionalCharacterProperty extends Property {
   private static final PropertyCache CACHE = new PropertyCache();
   private final Character character;

   private OptionalCharacterProperty(Character character) {
      this.character = character;
   }

   public static OptionalCharacterProperty getInstance(Character character) {
      return (OptionalCharacterProperty)CACHE.fetch(new OptionalCharacterProperty(character));
   }

   public Object getObject() {
      return this.character;
   }

   public char getCharacter() {
      return this.character == null ? '\u0000' : this.character;
   }

   public String getString() {
      return this.character == null ? "" : this.character.toString();
   }

   public boolean equals(Object obj) {
      if (obj instanceof OptionalCharacterProperty) {
         OptionalCharacterProperty ocp = (OptionalCharacterProperty)obj;
         if (this.character == null && ocp.character == null) {
            return true;
         } else {
            return this.character != null && this.character.equals(ocp.character);
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.character == null ? 0 : this.character.hashCode();
   }

   public static class Maker extends PropertyMaker {
      public Maker(int propId) {
         super(propId);
      }

      public Property make(PropertyList propertyList, String value, FObj fo) {
         if (value.isEmpty()) {
            return OptionalCharacterProperty.getInstance((Character)null);
         } else {
            char c = value.charAt(0);
            return OptionalCharacterProperty.getInstance(c);
         }
      }
   }
}
