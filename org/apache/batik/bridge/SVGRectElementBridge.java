package org.apache.batik.bridge;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import org.apache.batik.anim.dom.AbstractSVGAnimatedLength;
import org.apache.batik.anim.dom.AnimatedLiveAttributeValue;
import org.apache.batik.anim.dom.SVGOMRectElement;
import org.apache.batik.dom.svg.LiveAttributeException;
import org.apache.batik.gvt.ShapeNode;
import org.apache.batik.gvt.ShapePainter;
import org.w3c.dom.Element;

public class SVGRectElementBridge extends SVGShapeElementBridge {
   public String getLocalName() {
      return "rect";
   }

   public Bridge getInstance() {
      return new SVGRectElementBridge();
   }

   protected void buildShape(BridgeContext ctx, Element e, ShapeNode shapeNode) {
      try {
         SVGOMRectElement re = (SVGOMRectElement)e;
         AbstractSVGAnimatedLength _x = (AbstractSVGAnimatedLength)re.getX();
         float x = _x.getCheckedValue();
         AbstractSVGAnimatedLength _y = (AbstractSVGAnimatedLength)re.getY();
         float y = _y.getCheckedValue();
         AbstractSVGAnimatedLength _width = (AbstractSVGAnimatedLength)re.getWidth();
         float w = _width.getCheckedValue();
         AbstractSVGAnimatedLength _height = (AbstractSVGAnimatedLength)re.getHeight();
         float h = _height.getCheckedValue();
         AbstractSVGAnimatedLength _rx = (AbstractSVGAnimatedLength)re.getRx();
         float rx = _rx.getCheckedValue();
         if (rx > w / 2.0F) {
            rx = w / 2.0F;
         }

         AbstractSVGAnimatedLength _ry = (AbstractSVGAnimatedLength)re.getRy();
         float ry = _ry.getCheckedValue();
         if (ry > h / 2.0F) {
            ry = h / 2.0F;
         }

         Object shape;
         if (rx != 0.0F && ry != 0.0F) {
            shape = new RoundRectangle2D.Float(x, y, w, h, rx * 2.0F, ry * 2.0F);
         } else {
            shape = new Rectangle2D.Float(x, y, w, h);
         }

         shapeNode.setShape((Shape)shape);
      } catch (LiveAttributeException var18) {
         throw new BridgeException(ctx, var18);
      }
   }

   public void handleAnimatedAttributeChanged(AnimatedLiveAttributeValue alav) {
      if (alav.getNamespaceURI() == null) {
         String ln = alav.getLocalName();
         if (ln.equals("x") || ln.equals("y") || ln.equals("width") || ln.equals("height") || ln.equals("rx") || ln.equals("ry")) {
            this.buildShape(this.ctx, this.e, (ShapeNode)this.node);
            this.handleGeometryChanged();
            return;
         }
      }

      super.handleAnimatedAttributeChanged(alav);
   }

   protected ShapePainter createShapePainter(BridgeContext ctx, Element e, ShapeNode shapeNode) {
      Shape shape = shapeNode.getShape();
      Rectangle2D r2d = shape.getBounds2D();
      return r2d.getWidth() != 0.0 && r2d.getHeight() != 0.0 ? super.createShapePainter(ctx, e, shapeNode) : null;
   }
}
