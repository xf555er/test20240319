package org.apache.batik.extension.svg;

import org.apache.batik.bridge.TextNode;
import org.apache.batik.bridge.TextPainter;

public class FlowExtTextNode extends TextNode {
   public FlowExtTextNode() {
      this.textPainter = FlowExtTextPainter.getInstance();
   }

   public void setTextPainter(TextPainter textPainter) {
      if (textPainter == null) {
         this.textPainter = FlowExtTextPainter.getInstance();
      } else {
         this.textPainter = textPainter;
      }

   }
}
