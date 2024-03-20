package org.apache.batik.bridge;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import org.apache.batik.anim.dom.AbstractSVGAnimatedLength;
import org.apache.batik.anim.dom.AnimatedLiveAttributeValue;
import org.apache.batik.anim.dom.SVGOMEllipseElement;
import org.apache.batik.dom.svg.LiveAttributeException;
import org.apache.batik.gvt.ShapeNode;
import org.apache.batik.gvt.ShapePainter;
import org.w3c.dom.Element;

public class SVGEllipseElementBridge extends SVGShapeElementBridge {
   public String getLocalName() {
      return "ellipse";
   }

   public Bridge getInstance() {
      return new SVGEllipseElementBridge();
   }

   protected void buildShape(BridgeContext ctx, Element e, ShapeNode shapeNode) {
      try {
         SVGOMEllipseElement ee = (SVGOMEllipseElement)e;
         AbstractSVGAnimatedLength _cx = (AbstractSVGAnimatedLength)ee.getCx();
         float cx = _cx.getCheckedValue();
         AbstractSVGAnimatedLength _cy = (AbstractSVGAnimatedLength)ee.getCy();
         float cy = _cy.getCheckedValue();
         AbstractSVGAnimatedLength _rx = (AbstractSVGAnimatedLength)ee.getRx();
         float rx = _rx.getCheckedValue();
         AbstractSVGAnimatedLength _ry = (AbstractSVGAnimatedLength)ee.getRy();
         float ry = _ry.getCheckedValue();
         shapeNode.setShape(new Ellipse2D.Float(cx - rx, cy - ry, rx * 2.0F, ry * 2.0F));
      } catch (LiveAttributeException var13) {
         throw new BridgeException(ctx, var13);
      }
   }

   public void handleAnimatedAttributeChanged(AnimatedLiveAttributeValue alav) {
      if (alav.getNamespaceURI() == null) {
         String ln = alav.getLocalName();
         if (ln.equals("cx") || ln.equals("cy") || ln.equals("rx") || ln.equals("ry")) {
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
