package org.apache.batik.extension.svg;

import java.awt.font.FontRenderContext;
import java.awt.geom.Point2D;
import java.text.AttributedCharacterIterator;
import org.apache.batik.bridge.TextLayoutFactory;
import org.apache.batik.bridge.TextSpanLayout;

public class FlowExtTextLayoutFactory implements TextLayoutFactory {
   public TextSpanLayout createTextLayout(AttributedCharacterIterator aci, int[] charMap, Point2D offset, FontRenderContext frc) {
      return new FlowExtGlyphLayout(aci, charMap, offset, frc);
   }
}
