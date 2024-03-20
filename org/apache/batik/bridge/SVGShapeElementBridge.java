package org.apache.batik.bridge;

import java.awt.RenderingHints;
import org.apache.batik.css.engine.CSSEngineEvent;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.ShapeNode;
import org.apache.batik.gvt.ShapePainter;
import org.w3c.dom.Element;

public abstract class SVGShapeElementBridge extends AbstractGraphicsNodeBridge {
   protected boolean hasNewShapePainter;

   protected SVGShapeElementBridge() {
   }

   public GraphicsNode createGraphicsNode(BridgeContext ctx, Element e) {
      ShapeNode shapeNode = (ShapeNode)super.createGraphicsNode(ctx, e);
      if (shapeNode == null) {
         return null;
      } else {
         this.associateSVGContext(ctx, e, shapeNode);
         this.buildShape(ctx, e, shapeNode);
         RenderingHints hints = null;
         hints = CSSUtilities.convertColorRendering(e, hints);
         hints = CSSUtilities.convertShapeRendering(e, hints);
         if (hints != null) {
            shapeNode.setRenderingHints(hints);
         }

         return shapeNode;
      }
   }

   protected GraphicsNode instantiateGraphicsNode() {
      return new ShapeNode();
   }

   public void buildGraphicsNode(BridgeContext ctx, Element e, GraphicsNode node) {
      ShapeNode shapeNode = (ShapeNode)node;
      shapeNode.setShapePainter(this.createShapePainter(ctx, e, shapeNode));
      super.buildGraphicsNode(ctx, e, node);
   }

   protected ShapePainter createShapePainter(BridgeContext ctx, Element e, ShapeNode shapeNode) {
      return PaintServer.convertFillAndStroke(e, shapeNode, ctx);
   }

   protected abstract void buildShape(BridgeContext var1, Element var2, ShapeNode var3);

   public boolean isComposite() {
      return false;
   }

   protected void handleGeometryChanged() {
      super.handleGeometryChanged();
      ShapeNode shapeNode = (ShapeNode)this.node;
      shapeNode.setShapePainter(this.createShapePainter(this.ctx, this.e, shapeNode));
   }

   public void handleCSSEngineEvent(CSSEngineEvent evt) {
      this.hasNewShapePainter = false;
      super.handleCSSEngineEvent(evt);
   }

   protected void handleCSSPropertyChanged(int property) {
      RenderingHints hints;
      switch (property) {
         case 9:
            hints = this.node.getRenderingHints();
            hints = CSSUtilities.convertColorRendering(this.e, hints);
            if (hints != null) {
               this.node.setRenderingHints(hints);
            }
            break;
         case 10:
         case 11:
         case 12:
         case 13:
         case 14:
         case 17:
         case 18:
         case 19:
         case 20:
         case 21:
         case 22:
         case 23:
         case 24:
         case 25:
         case 26:
         case 27:
         case 28:
         case 29:
         case 30:
         case 31:
         case 32:
         case 33:
         case 34:
         case 35:
         case 36:
         case 37:
         case 38:
         case 39:
         case 40:
         case 41:
         case 43:
         case 44:
         default:
            super.handleCSSPropertyChanged(property);
            break;
         case 15:
         case 16:
         case 45:
         case 46:
         case 47:
         case 48:
         case 49:
         case 50:
         case 51:
         case 52:
            if (!this.hasNewShapePainter) {
               this.hasNewShapePainter = true;
               ShapeNode shapeNode = (ShapeNode)this.node;
               shapeNode.setShapePainter(this.createShapePainter(this.ctx, this.e, shapeNode));
            }
            break;
         case 42:
            hints = this.node.getRenderingHints();
            hints = CSSUtilities.convertShapeRendering(this.e, hints);
            if (hints != null) {
               this.node.setRenderingHints(hints);
            }
      }

   }
}
