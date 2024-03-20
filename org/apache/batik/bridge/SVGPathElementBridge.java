package org.apache.batik.bridge;

import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import org.apache.batik.anim.dom.AnimatedLiveAttributeValue;
import org.apache.batik.anim.dom.SVGOMAnimatedPathData;
import org.apache.batik.anim.dom.SVGOMPathElement;
import org.apache.batik.dom.svg.LiveAttributeException;
import org.apache.batik.dom.svg.SVGAnimatedPathDataSupport;
import org.apache.batik.dom.svg.SVGPathContext;
import org.apache.batik.ext.awt.geom.PathLength;
import org.apache.batik.gvt.ShapeNode;
import org.apache.batik.parser.AWTPathProducer;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGPathSegList;

public class SVGPathElementBridge extends SVGDecoratedShapeElementBridge implements SVGPathContext {
   protected static final Shape DEFAULT_SHAPE = new GeneralPath();
   protected Shape pathLengthShape;
   protected PathLength pathLength;

   public String getLocalName() {
      return "path";
   }

   public Bridge getInstance() {
      return new SVGPathElementBridge();
   }

   protected void buildShape(BridgeContext ctx, Element e, ShapeNode shapeNode) {
      SVGOMPathElement pe = (SVGOMPathElement)e;
      AWTPathProducer app = new AWTPathProducer();

      try {
         SVGOMAnimatedPathData _d = pe.getAnimatedPathData();
         _d.check();
         SVGPathSegList p = _d.getAnimatedPathSegList();
         app.setWindingRule(CSSUtilities.convertFillRule(e));
         SVGAnimatedPathDataSupport.handlePathSegList(p, app);
      } catch (LiveAttributeException var11) {
         throw new BridgeException(ctx, var11);
      } finally {
         shapeNode.setShape(app.getShape());
      }

   }

   public void handleAnimatedAttributeChanged(AnimatedLiveAttributeValue alav) {
      if (alav.getNamespaceURI() == null && alav.getLocalName().equals("d")) {
         this.buildShape(this.ctx, this.e, (ShapeNode)this.node);
         this.handleGeometryChanged();
      } else {
         super.handleAnimatedAttributeChanged(alav);
      }

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

   protected PathLength getPathLengthObj() {
      Shape s = ((ShapeNode)this.node).getShape();
      if (this.pathLengthShape != s) {
         this.pathLength = new PathLength(s);
         this.pathLengthShape = s;
      }

      return this.pathLength;
   }

   public float getTotalLength() {
      PathLength pl = this.getPathLengthObj();
      return pl.lengthOfPath();
   }

   public Point2D getPointAtLength(float distance) {
      PathLength pl = this.getPathLengthObj();
      return pl.pointAtLength(distance);
   }

   public int getPathSegAtLength(float distance) {
      PathLength pl = this.getPathLengthObj();
      return pl.segmentAtLength(distance);
   }
}
