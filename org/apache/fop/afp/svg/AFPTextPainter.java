package org.apache.fop.afp.svg;

import java.awt.Graphics2D;
import org.apache.batik.bridge.FontFamilyResolver;
import org.apache.batik.bridge.StrokingTextPainter;
import org.apache.fop.afp.AFPGraphics2D;
import org.apache.fop.svg.AbstractFOPTextPainter;
import org.apache.fop.svg.FOPTextHandler;

public class AFPTextPainter extends AbstractFOPTextPainter {
   public AFPTextPainter(FOPTextHandler nativeTextHandler, FontFamilyResolver fopFontFamilyResolver) {
      super(nativeTextHandler, new FOPStrokingTextPainter(fopFontFamilyResolver));
   }

   protected boolean isSupportedGraphics2D(Graphics2D g2d) {
      return g2d instanceof AFPGraphics2D;
   }

   private static class FOPStrokingTextPainter extends StrokingTextPainter {
      private final FontFamilyResolver fopFontFontFamily;

      FOPStrokingTextPainter(FontFamilyResolver fopFontFontFamily) {
         this.fopFontFontFamily = fopFontFontFamily;
      }

      protected FontFamilyResolver getFontFamilyResolver() {
         return this.fopFontFontFamily;
      }
   }
}
