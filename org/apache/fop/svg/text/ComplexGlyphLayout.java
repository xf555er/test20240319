package org.apache.fop.svg.text;

import java.awt.font.FontRenderContext;
import java.awt.geom.Point2D;
import java.text.AttributedCharacterIterator;
import org.apache.batik.bridge.GlyphLayout;
import org.apache.batik.gvt.font.GVTFont;
import org.apache.batik.gvt.font.GVTGlyphVector;
import org.apache.batik.gvt.text.GVTAttributedCharacterIterator;
import org.apache.fop.fonts.Font;
import org.apache.fop.svg.font.FOPGVTFont;

public class ComplexGlyphLayout extends GlyphLayout {
   public ComplexGlyphLayout(AttributedCharacterIterator aci, int[] charMap, Point2D offset, FontRenderContext frc) {
      super(aci, charMap, offset, frc);
   }

   protected void doExplicitGlyphLayout() {
      GVTGlyphVector gv = this.gv;
      gv.performDefaultLayout();
      int ng = gv.getNumGlyphs();
      if (ng > 0) {
         this.advance = gv.getGlyphPosition(ng);
      } else {
         this.advance = new Point2D.Float(0.0F, 0.0F);
      }

      this.layoutApplied = true;
   }

   public static final boolean mayRequireComplexLayout(AttributedCharacterIterator aci) {
      boolean rv = false;
      GVTAttributedCharacterIterator.TextAttribute attrFont = GVTAttributedCharacterIterator.TextAttribute.GVT_FONT;
      int indexSave = aci.getIndex();
      aci.first();

      do {
         GVTFont gvtFont = (GVTFont)aci.getAttribute(attrFont);
         if (gvtFont != null) {
            if (gvtFont instanceof FOPGVTFont) {
               Font f = ((FOPGVTFont)gvtFont).getFont();
               if (f.performsSubstitution() || f.performsPositioning()) {
                  rv = true;
                  break;
               }
            }

            aci.setIndex(aci.getRunLimit(attrFont));
         }
      } while(aci.next() != '\uffff');

      aci.setIndex(indexSave);
      return rv;
   }
}
