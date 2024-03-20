package org.apache.batik.bridge;

import java.awt.Shape;
import java.awt.geom.GeneralPath;
import org.apache.batik.anim.dom.AnimatedLiveAttributeValue;
import org.apache.batik.anim.dom.SVGOMAnimatedPoints;
import org.apache.batik.anim.dom.SVGOMPolygonElement;
import org.apache.batik.dom.svg.LiveAttributeException;
import org.apache.batik.gvt.ShapeNode;
import org.apache.batik.parser.AWTPolygonProducer;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGPoint;
import org.w3c.dom.svg.SVGPointList;

public class SVGPolygonElementBridge extends SVGDecoratedShapeElementBridge {
   protected static final Shape DEFAULT_SHAPE = new GeneralPath();

   public String getLocalName() {
      return "polygon";
   }

   public Bridge getInstance() {
      return new SVGPolygonElementBridge();
   }

   protected void buildShape(BridgeContext ctx, Element e, ShapeNode shapeNode) {
      SVGOMPolygonElement pe = (SVGOMPolygonElement)e;

      try {
         SVGOMAnimatedPoints _points = pe.getSVGOMAnimatedPoints();
         _points.check();
         SVGPointList pl = _points.getAnimatedPoints();
         int size = pl.getNumberOfItems();
         if (size == 0) {
            shapeNode.setShape(DEFAULT_SHAPE);
         } else {
            AWTPolygonProducer app = new AWTPolygonProducer();
            app.setWindingRule(CSSUtilities.convertFillRule(e));
            app.startPoints();

            for(int i = 0; i < size; ++i) {
               SVGPoint p = pl.getItem(i);
               app.point(p.getX(), p.getY());
            }

            app.endPoints();
            shapeNode.setShape(app.getShape());
         }

      } catch (LiveAttributeException var11) {
         throw new BridgeException(ctx, var11);
      }
   }

   public void handleAnimatedAttributeChanged(AnimatedLiveAttributeValue alav) {
      if (alav.getNamespaceURI() == null) {
         String ln = alav.getLocalName();
         if (ln.equals("points")) {
            this.buildShape(this.ctx, this.e, (ShapeNode)this.node);
            this.handleGeometryChanged();
            return;
         }
      }

      super.handleAnimatedAttributeChanged(alav);
   }

   protected void handleCSSPropertyChanged(int property) {
      switch (property) {
         case 17:
            this.buildShape(this.ctx, this.e, (ShapeNode)this.node);
            this.handleGeometryChanged();
            break;
         default:
            super.handleCSSPropertyChanged(property);
      }

   }
}
