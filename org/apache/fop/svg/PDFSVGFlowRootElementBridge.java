package org.apache.fop.svg;

import org.apache.batik.bridge.TextNode;
import org.apache.batik.bridge.TextPainter;
import org.apache.batik.bridge.svg12.SVGFlowRootElementBridge;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.fop.fonts.FontInfo;

public class PDFSVGFlowRootElementBridge extends SVGFlowRootElementBridge {
   private PDFTextPainter textPainter;

   public PDFSVGFlowRootElementBridge(FontInfo fontInfo) {
      this.textPainter = new PDFFlowTextPainter(fontInfo);
   }

   protected GraphicsNode instantiateGraphicsNode() {
      GraphicsNode node = super.instantiateGraphicsNode();
      if (node != null) {
         ((TextNode)node).setTextPainter(this.getTextPainter());
      }

      return node;
   }

   public TextPainter getTextPainter() {
      return this.textPainter;
   }
}
