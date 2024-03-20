package org.apache.batik.svggen;

import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import org.apache.batik.ext.awt.g2d.GraphicContext;
import org.w3c.dom.Element;

public class SVGClip extends AbstractSVGConverter {
   public static final Shape ORIGIN = new Line2D.Float(0.0F, 0.0F, 0.0F, 0.0F);
   public static final SVGClipDescriptor NO_CLIP = new SVGClipDescriptor("none", (Element)null);
   private SVGShape shapeConverter;

   public SVGClip(SVGGeneratorContext generatorContext) {
      super(generatorContext);
      this.shapeConverter = new SVGShape(generatorContext);
   }

   public SVGDescriptor toSVG(GraphicContext gc) {
      Shape clip = gc.getClip();
      SVGClipDescriptor clipDesc = null;
      if (clip != null) {
         StringBuffer clipPathAttrBuf = new StringBuffer("url(");
         GeneralPath clipPath = new GeneralPath(clip);
         ClipKey clipKey = new ClipKey(clipPath, this.generatorContext);
         clipDesc = (SVGClipDescriptor)this.descMap.get(clipKey);
         if (clipDesc == null) {
            Element clipDef = this.clipToSVG(clip);
            if (clipDef == null) {
               clipDesc = NO_CLIP;
            } else {
               clipPathAttrBuf.append("#");
               clipPathAttrBuf.append(clipDef.getAttributeNS((String)null, "id"));
               clipPathAttrBuf.append(")");
               clipDesc = new SVGClipDescriptor(clipPathAttrBuf.toString(), clipDef);
               this.descMap.put(clipKey, clipDesc);
               this.defSet.add(clipDef);
            }
         }
      } else {
         clipDesc = NO_CLIP;
      }

      return clipDesc;
   }

   private Element clipToSVG(Shape clip) {
      Element clipDef = this.generatorContext.domFactory.createElementNS("http://www.w3.org/2000/svg", "clipPath");
      clipDef.setAttributeNS((String)null, "clipPathUnits", "userSpaceOnUse");
      clipDef.setAttributeNS((String)null, "id", this.generatorContext.idGenerator.generateID("clipPath"));
      Element clipPath = this.shapeConverter.toSVG(clip);
      if (clipPath != null) {
         clipDef.appendChild(clipPath);
         return clipDef;
      } else {
         clipDef.appendChild(this.shapeConverter.toSVG(ORIGIN));
         return clipDef;
      }
   }
}
