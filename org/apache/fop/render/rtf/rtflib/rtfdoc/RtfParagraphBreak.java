package org.apache.fop.render.rtf.rtflib.rtfdoc;

import java.io.IOException;
import java.io.Writer;

public class RtfParagraphBreak extends RtfElement {
   private static final String DEFAULT_PARAGRAPH = "par";
   private String controlWord = "par";

   RtfParagraphBreak(RtfContainer parent, Writer w) throws IOException {
      super(parent, w);
   }

   public boolean isEmpty() {
      return false;
   }

   protected void writeRtfContent() throws IOException {
      if (this.controlWord != null) {
         this.writeControlWord(this.controlWord);
      }

   }

   public boolean canHide() {
      return this.controlWord.equals("par");
   }

   public void switchControlWord(String controlWord) {
      this.controlWord = controlWord;
   }
}
