package org.apache.fop.svg;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.SVGTextElementBridge;
import org.apache.batik.bridge.TextNode;
import org.apache.batik.bridge.TextPainter;
import org.apache.batik.gvt.GraphicsNode;
import org.w3c.dom.Element;

public abstract class AbstractFOPTextElementBridge extends SVGTextElementBridge {
   protected TextPainter textPainter;

   public AbstractFOPTextElementBridge(TextPainter textPainter) {
      this.textPainter = textPainter;
   }

   public GraphicsNode createGraphicsNode(BridgeContext ctx, Element e) {
      GraphicsNode node = super.createGraphicsNode(ctx, e);
      if (node != null) {
         ((TextNode)node).setTextPainter(this.textPainter);
      }

      return node;
   }
}
