package org.apache.batik.bridge;

import java.awt.geom.Line2D;
import org.apache.batik.anim.dom.AbstractSVGAnimatedLength;
import org.apache.batik.anim.dom.AnimatedLiveAttributeValue;
import org.apache.batik.anim.dom.SVGOMLineElement;
import org.apache.batik.dom.svg.LiveAttributeException;
import org.apache.batik.gvt.ShapeNode;
import org.apache.batik.gvt.ShapePainter;
import org.w3c.dom.Element;

public class SVGLineElementBridge extends SVGDecoratedShapeElementBridge {
   public String getLocalName() {
      return "line";
   }

   public Bridge getInstance() {
      return new SVGLineElementBridge();
   }

   protected ShapePainter createFillStrokePainter(BridgeContext ctx, Element e, ShapeNode shapeNode) {
      return PaintServer.convertStrokePainter(e, shapeNode, ctx);
   }

   protected void buildShape(BridgeContext ctx, Element e, ShapeNode shapeNode) {
      try {
         SVGOMLineElement le = (SVGOMLineElement)e;
         AbstractSVGAnimatedLength _x1 = (AbstractSVGAnimatedLength)le.getX1();
         float x1 = _x1.getCheckedValue();
         AbstractSVGAnimatedLength _y1 = (AbstractSVGAnimatedLength)le.getY1();
         float y1 = _y1.getCheckedValue();
         AbstractSVGAnimatedLength _x2 = (AbstractSVGAnimatedLength)le.getX2();
         float x2 = _x2.getCheckedValue();
         AbstractSVGAnimatedLength _y2 = (AbstractSVGAnimatedLength)le.getY2();
         float y2 = _y2.getCheckedValue();
         shapeNode.setShape(new Line2D.Float(x1, y1, x2, y2));
      } catch (LiveAttributeException var13) {
         throw new BridgeException(ctx, var13);
      }
   }

   public void handleAnimatedAttributeChanged(AnimatedLiveAttributeValue alav) {
      if (alav.getNamespaceURI() == null) {
         String ln = alav.getLocalName();
         if (ln.equals("x1") || ln.equals("y1") || ln.equals("x2") || ln.equals("y2")) {
            this.buildShape(this.ctx, this.e, (ShapeNode)this.node);
            this.handleGeometryChanged();
            return;
         }
      }

      super.handleAnimatedAttributeChanged(alav);
   }
}
