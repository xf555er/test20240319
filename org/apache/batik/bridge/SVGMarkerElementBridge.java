package org.apache.batik.bridge;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.ext.awt.image.renderable.ClipRable8Bit;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.Marker;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class SVGMarkerElementBridge extends AnimatableGenericSVGBridge implements MarkerBridge, ErrorConstants {
   protected SVGMarkerElementBridge() {
   }

   public String getLocalName() {
      return "marker";
   }

   public Marker createMarker(BridgeContext ctx, Element markerElement, Element paintedElement) {
      GVTBuilder builder = ctx.getGVTBuilder();
      CompositeGraphicsNode markerContentNode = new CompositeGraphicsNode();
      boolean hasChildren = false;

      for(Node n = markerElement.getFirstChild(); n != null; n = n.getNextSibling()) {
         if (n.getNodeType() == 1) {
            Element child = (Element)n;
            GraphicsNode markerNode = builder.build(ctx, child);
            if (markerNode != null) {
               hasChildren = true;
               markerContentNode.getChildren().add(markerNode);
            }
         }
      }

      if (!hasChildren) {
         return null;
      } else {
         org.apache.batik.parser.UnitProcessor.Context uctx = UnitProcessor.createContext(ctx, paintedElement);
         float markerWidth = 3.0F;
         String s = markerElement.getAttributeNS((String)null, "markerWidth");
         if (s.length() != 0) {
            markerWidth = UnitProcessor.svgHorizontalLengthToUserSpace(s, "markerWidth", uctx);
         }

         if (markerWidth == 0.0F) {
            return null;
         } else {
            float markerHeight = 3.0F;
            s = markerElement.getAttributeNS((String)null, "markerHeight");
            if (s.length() != 0) {
               markerHeight = UnitProcessor.svgVerticalLengthToUserSpace(s, "markerHeight", uctx);
            }

            if (markerHeight == 0.0F) {
               return null;
            } else {
               s = markerElement.getAttributeNS((String)null, "orient");
               double orient;
               if (s.length() == 0) {
                  orient = 0.0;
               } else if ("auto".equals(s)) {
                  orient = Double.NaN;
               } else {
                  try {
                     orient = (double)SVGUtilities.convertSVGNumber(s);
                  } catch (NumberFormatException var22) {
                     throw new BridgeException(ctx, markerElement, var22, "attribute.malformed", new Object[]{"orient", s});
                  }
               }

               Value val = CSSUtilities.getComputedStyle(paintedElement, 52);
               float strokeWidth = val.getFloatValue();
               s = markerElement.getAttributeNS((String)null, "markerUnits");
               short unitsType;
               if (s.length() == 0) {
                  unitsType = 3;
               } else {
                  unitsType = SVGUtilities.parseMarkerCoordinateSystem(markerElement, "markerUnits", s, ctx);
               }

               AffineTransform markerTxf;
               if (unitsType == 3) {
                  markerTxf = new AffineTransform();
                  markerTxf.scale((double)strokeWidth, (double)strokeWidth);
               } else {
                  markerTxf = new AffineTransform();
               }

               AffineTransform preserveAspectRatioTransform = ViewBox.getPreserveAspectRatioTransform(markerElement, markerWidth, markerHeight, ctx);
               if (preserveAspectRatioTransform == null) {
                  return null;
               } else {
                  markerTxf.concatenate(preserveAspectRatioTransform);
                  markerContentNode.setTransform(markerTxf);
                  if (CSSUtilities.convertOverflow(markerElement)) {
                     float[] offsets = CSSUtilities.convertClip(markerElement);
                     Rectangle2D.Float markerClip;
                     if (offsets == null) {
                        markerClip = new Rectangle2D.Float(0.0F, 0.0F, strokeWidth * markerWidth, strokeWidth * markerHeight);
                     } else {
                        markerClip = new Rectangle2D.Float(offsets[3], offsets[0], strokeWidth * markerWidth - offsets[1] - offsets[3], strokeWidth * markerHeight - offsets[2] - offsets[0]);
                     }

                     CompositeGraphicsNode comp = new CompositeGraphicsNode();
                     comp.getChildren().add(markerContentNode);
                     Filter clipSrc = comp.getGraphicsNodeRable(true);
                     comp.setClip(new ClipRable8Bit(clipSrc, markerClip));
                     markerContentNode = comp;
                  }

                  float refX = 0.0F;
                  s = markerElement.getAttributeNS((String)null, "refX");
                  if (s.length() != 0) {
                     refX = UnitProcessor.svgHorizontalCoordinateToUserSpace(s, "refX", uctx);
                  }

                  float refY = 0.0F;
                  s = markerElement.getAttributeNS((String)null, "refY");
                  if (s.length() != 0) {
                     refY = UnitProcessor.svgVerticalCoordinateToUserSpace(s, "refY", uctx);
                  }

                  float[] ref = new float[]{refX, refY};
                  markerTxf.transform(ref, 0, ref, 0, 1);
                  Marker marker = new Marker(markerContentNode, new Point2D.Float(ref[0], ref[1]), orient);
                  return marker;
               }
            }
         }
      }
   }
}
