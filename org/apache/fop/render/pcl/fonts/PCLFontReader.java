package org.apache.fop.render.pcl.fonts;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.apache.fop.fonts.CustomFont;
import org.apache.fop.fonts.Typeface;
import org.apache.fop.fonts.truetype.FontFileReader;
import org.apache.fop.fonts.truetype.OpenFont;

public abstract class PCLFontReader {
   protected Typeface typeface;
   protected CustomFont font;

   public PCLFontReader(Typeface font) {
      this.typeface = font;
   }

   public void setFont(CustomFont mbFont) {
      this.font = mbFont;
   }

   public abstract int getDescriptorSize();

   public abstract int getHeaderFormat();

   public abstract int getFontType();

   public abstract int getStyleMSB();

   public abstract int getBaselinePosition();

   public abstract int getCellWidth();

   public abstract int getCellHeight();

   public abstract int getOrientation();

   public abstract int getSpacing();

   public abstract int getSymbolSet();

   public abstract int getPitch();

   public abstract int getHeight();

   public abstract int getXHeight();

   public abstract int getWidthType();

   public abstract int getStyleLSB();

   public abstract int getStrokeWeight();

   public abstract int getTypefaceLSB();

   public abstract int getTypefaceMSB();

   public abstract int getSerifStyle();

   public abstract int getQuality();

   public abstract int getPlacement();

   public abstract int getUnderlinePosition();

   public abstract int getUnderlineThickness();

   public abstract int getTextHeight();

   public abstract int getTextWidth();

   public abstract int getFirstCode();

   public abstract int getLastCode();

   public abstract int getPitchExtended();

   public abstract int getHeightExtended();

   public abstract int getCapHeight();

   public abstract int getFontNumber();

   public abstract String getFontName();

   public abstract int getScaleFactor() throws IOException;

   public abstract int getMasterUnderlinePosition() throws IOException;

   public abstract int getMasterUnderlineThickness() throws IOException;

   public abstract int getFontScalingTechnology();

   public abstract int getVariety();

   public abstract Map scanMtxCharacters() throws IOException;

   public abstract List getFontSegments(Map var1) throws IOException;

   public abstract Map getCharacterOffsets() throws IOException;

   public abstract OpenFont getFontFile();

   public abstract FontFileReader getFontFileReader();

   protected int getMSB(int s) {
      return s >> 8;
   }

   protected int getLSB(int s) {
      byte b1 = (byte)(s >> 8);
      return s;
   }
}
