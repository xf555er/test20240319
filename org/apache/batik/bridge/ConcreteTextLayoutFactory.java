package org.apache.batik.bridge;

import java.awt.font.FontRenderContext;
import java.awt.geom.Point2D;
import java.text.AttributedCharacterIterator;

public class ConcreteTextLayoutFactory implements TextLayoutFactory {
   public TextSpanLayout createTextLayout(AttributedCharacterIterator aci, int[] charMap, Point2D offset, FontRenderContext frc) {
      return new GlyphLayout(aci, charMap, offset, frc);
   }
}
