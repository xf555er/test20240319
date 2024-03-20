package org.apache.batik.bridge;

import java.awt.font.FontRenderContext;
import java.text.AttributedCharacterIterator;
import org.apache.batik.gvt.font.AltGlyphHandler;
import org.apache.batik.gvt.font.GVTFont;
import org.apache.batik.gvt.font.GVTGlyphVector;
import org.apache.batik.gvt.font.Glyph;
import org.apache.batik.gvt.font.SVGGVTGlyphVector;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.Element;

public class SVGAltGlyphHandler implements AltGlyphHandler, SVGConstants {
   private BridgeContext ctx;
   private Element textElement;

   public SVGAltGlyphHandler(BridgeContext ctx, Element textElement) {
      this.ctx = ctx;
      this.textElement = textElement;
   }

   public GVTGlyphVector createGlyphVector(FontRenderContext frc, float fontSize, AttributedCharacterIterator aci) {
      try {
         if ("http://www.w3.org/2000/svg".equals(this.textElement.getNamespaceURI()) && "altGlyph".equals(this.textElement.getLocalName())) {
            SVGAltGlyphElementBridge altGlyphBridge = (SVGAltGlyphElementBridge)this.ctx.getBridge(this.textElement);
            Glyph[] glyphArray = altGlyphBridge.createAltGlyphArray(this.ctx, this.textElement, fontSize, aci);
            if (glyphArray != null) {
               return new SVGGVTGlyphVector((GVTFont)null, glyphArray, frc);
            }
         }

         return null;
      } catch (SecurityException var6) {
         this.ctx.getUserAgent().displayError(var6);
         throw var6;
      }
   }
}
