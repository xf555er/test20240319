package org.apache.batik.bridge;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import org.apache.batik.anim.dom.AbstractSVGAnimatedLength;
import org.apache.batik.anim.dom.AnimatedLiveAttributeValue;
import org.apache.batik.anim.dom.SVGOMCircleElement;
import org.apache.batik.dom.svg.LiveAttributeException;
import org.apache.batik.gvt.ShapeNode;
import org.apache.batik.gvt.ShapePainter;
import org.w3c.dom.Element;

public class SVGCircleElementBridge extends SVGShapeElementBridge {
   public String getLocalName() {
      return "circle";
   }

   public Bridge getInstance() {
      return new SVGCircleElementBridge();
   }

   protected void buildShape(BridgeContext ctx, Element e, ShapeNode shapeNode) {
      try {
         SVGOMCircleElement ce = (SVGOMCircleElement)e;
         AbstractSVGAnimatedLength _cx = (AbstractSVGAnimatedLength)ce.getCx();
         float cx = _cx.getCheckedValue();
         AbstractSVGAnimatedLength _cy = (AbstractSVGAnimatedLength)ce.getCy();
         float cy = _cy.getCheckedValue();
         AbstractSVGAnimatedLength _r = (AbstractSVGAnimatedLength)ce.getR();
         float r = _r.getCheckedValue();
         float x = cx - r;
         float y = cy - r;
         float w = r * 2.0F;
         shapeNode.setShape(new Ellipse2D.Float(x, y, w, w));
      } catch (LiveAttributeException var14) {
         throw new BridgeException(ctx, var14);
      }
   }

   public void handleAnimatedAttributeChanged(AnimatedLiveAttributeValue alav) {
      if (alav.getNamespaceURI() == null) {
         String ln = alav.getLocalName();
         if (ln.equals("cx") || ln.equals("cy") || ln.equals("r")) {
            this.buildShape(this.ctx, this.e, (ShapeNode)this.node);
            this.handleGeometryChanged();
            return;
         }
      }

      super.handleAnimatedAttributeChanged(alav);
   }

   protected ShapePainter createShapePainter(BridgeContext ctx, Element e, ShapeNode shapeNode) {
      Rectangle2D r2d = shapeNode.getShape().getBounds2D();
      return r2d.getWidth() != 0.0 && r2d.getHeight() != 0.0 ? super.createShapePainter(ctx, e, shapeNode) : null;
   }
}
