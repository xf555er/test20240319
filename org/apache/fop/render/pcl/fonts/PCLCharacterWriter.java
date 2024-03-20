package org.apache.fop.render.pcl.fonts;

import java.io.IOException;
import org.apache.fop.fonts.truetype.FontFileReader;
import org.apache.fop.fonts.truetype.OpenFont;

public abstract class PCLCharacterWriter {
   protected PCLSoftFont font;
   protected OpenFont openFont;
   protected FontFileReader fontReader;

   public PCLCharacterWriter(PCLSoftFont font) throws IOException {
      this.font = font;
      this.openFont = font.getOpenFont();
      this.fontReader = font.getReader();
   }

   public abstract byte[] writeCharacterDefinitions(String var1) throws IOException;
}
