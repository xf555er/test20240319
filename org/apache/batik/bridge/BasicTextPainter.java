package org.apache.batik.bridge;

import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

public abstract class BasicTextPainter implements TextPainter {
   private static TextLayoutFactory textLayoutFactory = new ConcreteTextLayoutFactory();
   protected FontRenderContext fontRenderContext = new FontRenderContext(new AffineTransform(), true, true);
   protected FontRenderContext aaOffFontRenderContext = new FontRenderContext(new AffineTransform(), false, true);

   protected TextLayoutFactory getTextLayoutFactory() {
      return textLayoutFactory;
   }

   public Mark selectAt(double x, double y, TextNode node) {
      return this.hitTest(x, y, node);
   }

   public Mark selectTo(double x, double y, Mark beginMark) {
      return beginMark == null ? null : this.hitTest(x, y, beginMark.getTextNode());
   }

   public Rectangle2D getGeometryBounds(TextNode node) {
      return this.getOutline(node).getBounds2D();
   }

   protected abstract Mark hitTest(double var1, double var3, TextNode var5);

   protected static class BasicMark implements Mark {
      private TextNode node;
      private TextHit hit;

      protected BasicMark(TextNode node, TextHit hit) {
         this.hit = hit;
         this.node = node;
      }

      public TextHit getHit() {
         return this.hit;
      }

      public TextNode getTextNode() {
         return this.node;
      }

      public int getCharIndex() {
         return this.hit.getCharIndex();
      }
   }
}
