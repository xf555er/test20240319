package org.apache.fop.afp.svg;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.afp.AFPGraphics2D;
import org.apache.fop.afp.AFPPaintingState;
import org.apache.fop.afp.AFPResourceManager;
import org.apache.fop.afp.fonts.AFPFont;
import org.apache.fop.afp.fonts.AFPFontAttributes;
import org.apache.fop.afp.fonts.AFPPageFonts;
import org.apache.fop.afp.fonts.CharacterSet;
import org.apache.fop.afp.modca.GraphicsObject;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.svg.FOPTextHandlerAdapter;

public class AFPTextHandler extends FOPTextHandlerAdapter {
   private static Log log = LogFactory.getLog(AFPTextHandler.class);
   protected Font overrideFont;
   private final FontInfo fontInfo;
   private AFPResourceManager resourceManager;

   public AFPTextHandler(FontInfo fontInfo, AFPResourceManager resourceManager) {
      this.fontInfo = fontInfo;
      this.resourceManager = resourceManager;
   }

   public FontInfo getFontInfo() {
      return this.fontInfo;
   }

   private int registerPageFont(AFPPageFonts pageFonts, String internalFontName, int fontSize) {
      AFPFont afpFont = (AFPFont)this.fontInfo.getFonts().get(internalFontName);
      AFPFontAttributes afpFontAttributes = pageFonts.registerFont(internalFontName, afpFont, fontSize);
      if (afpFont.isEmbeddable()) {
         try {
            CharacterSet charSet = afpFont.getCharacterSet(fontSize);
            this.resourceManager.embedFont(afpFont, charSet);
         } catch (IOException var7) {
            throw new RuntimeException("Error while embedding font resources", var7);
         }
      }

      return afpFontAttributes.getFontReference();
   }

   public void drawString(Graphics2D g, String str, float x, float y) {
      if (log.isDebugEnabled()) {
         log.debug("drawString() str=" + str + ", x=" + x + ", y=" + y);
      }

      if (g instanceof AFPGraphics2D) {
         AFPGraphics2D g2d = (AFPGraphics2D)g;
         GraphicsObject graphicsObj = g2d.getGraphicsObject();
         Color color = g2d.getColor();
         AFPPaintingState paintingState = g2d.getPaintingState();
         paintingState.setColor(color);
         graphicsObj.setColor(color);
         int fontReference = false;
         AFPPageFonts pageFonts = paintingState.getPageFonts();
         if (this.overrideFont != null) {
            String internalFontName = this.overrideFont.getFontName();
            int fontSize = this.overrideFont.getFontSize();
            if (log.isDebugEnabled()) {
               log.debug("  with overriding font: " + internalFontName + ", " + fontSize);
            }

            fontSize = (int)Math.round(g2d.convertToAbsoluteLength((double)fontSize));
            int fontReference = this.registerPageFont(pageFonts, internalFontName, fontSize);
            AFPFont afpFont = (AFPFont)this.fontInfo.getFonts().get(internalFontName);
            CharacterSet charSet = afpFont.getCharacterSet(fontSize);
            graphicsObj.setCharacterSet(fontReference);
            graphicsObj.addString(str, Math.round(x), Math.round(y), charSet);
         }
      } else {
         g.drawString(str, x, y);
      }

   }

   public void setOverrideFont(Font overrideFont) {
      this.overrideFont = overrideFont;
   }
}
