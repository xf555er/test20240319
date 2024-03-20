package org.apache.batik.bridge;

import java.awt.Graphics2D;
import java.awt.font.TextLayout;
import java.awt.geom.Point2D;
import java.text.AttributedCharacterIterator;

public abstract class ConcreteTextPainter extends BasicTextPainter {
   public void paint(AttributedCharacterIterator aci, Point2D location, TextNode.Anchor anchor, Graphics2D g2d) {
      TextLayout layout = new TextLayout(aci, this.fontRenderContext);
      float advance = layout.getAdvance();
      float tx = 0.0F;
      switch (anchor.getType()) {
         case 1:
            tx = -advance / 2.0F;
            break;
         case 2:
            tx = -advance;
      }

      layout.draw(g2d, (float)(location.getX() + (double)tx), (float)location.getY());
   }
}
