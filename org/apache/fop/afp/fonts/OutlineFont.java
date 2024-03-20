package org.apache.fop.afp.fonts;

import java.awt.Rectangle;
import org.apache.fop.afp.AFPEventProducer;

public class OutlineFont extends AbstractOutlineFont {
   public OutlineFont(String name, boolean embeddable, CharacterSet charSet, AFPEventProducer eventProducer) {
      super(name, embeddable, charSet, eventProducer);
   }

   public int getWidth(int character, int size) {
      return this.charSet.getWidth(toUnicodeCodepoint(character), size);
   }

   public Rectangle getBoundingBox(int character, int size) {
      return this.charSet.getCharacterBox(toUnicodeCodepoint(character), size);
   }
}
