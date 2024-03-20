package org.apache.batik.bridge;

public class FlowTextNode extends TextNode {
   public FlowTextNode() {
      this.textPainter = FlowTextPainter.getInstance();
   }

   public void setTextPainter(TextPainter textPainter) {
      if (textPainter == null) {
         this.textPainter = FlowTextPainter.getInstance();
      } else {
         this.textPainter = textPainter;
      }

   }
}
