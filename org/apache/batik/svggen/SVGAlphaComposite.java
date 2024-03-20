package org.apache.batik.svggen;

import java.awt.AlphaComposite;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.batik.ext.awt.g2d.GraphicContext;
import org.w3c.dom.Element;

public class SVGAlphaComposite extends AbstractSVGConverter {
   private Map compositeDefsMap = new HashMap();
   private boolean backgroundAccessRequired = false;

   public SVGAlphaComposite(SVGGeneratorContext generatorContext) {
      super(generatorContext);
      this.compositeDefsMap.put(AlphaComposite.Src, this.compositeToSVG(AlphaComposite.Src));
      this.compositeDefsMap.put(AlphaComposite.SrcIn, this.compositeToSVG(AlphaComposite.SrcIn));
      this.compositeDefsMap.put(AlphaComposite.SrcOut, this.compositeToSVG(AlphaComposite.SrcOut));
      this.compositeDefsMap.put(AlphaComposite.DstIn, this.compositeToSVG(AlphaComposite.DstIn));
      this.compositeDefsMap.put(AlphaComposite.DstOut, this.compositeToSVG(AlphaComposite.DstOut));
      this.compositeDefsMap.put(AlphaComposite.DstOver, this.compositeToSVG(AlphaComposite.DstOver));
      this.compositeDefsMap.put(AlphaComposite.Clear, this.compositeToSVG(AlphaComposite.Clear));
   }

   public List getAlphaCompositeFilterSet() {
      return new LinkedList(this.compositeDefsMap.values());
   }

   public boolean requiresBackgroundAccess() {
      return this.backgroundAccessRequired;
   }

   public SVGDescriptor toSVG(GraphicContext gc) {
      return this.toSVG((AlphaComposite)gc.getComposite());
   }

   public SVGCompositeDescriptor toSVG(AlphaComposite composite) {
      SVGCompositeDescriptor compositeDesc = (SVGCompositeDescriptor)this.descMap.get(composite);
      if (compositeDesc == null) {
         String opacityValue = this.doubleString((double)composite.getAlpha());
         String filterValue = null;
         Element filterDef = null;
         if (composite.getRule() != 3) {
            AlphaComposite majorComposite = AlphaComposite.getInstance(composite.getRule());
            filterDef = (Element)this.compositeDefsMap.get(majorComposite);
            this.defSet.add(filterDef);
            StringBuffer filterAttrBuf = new StringBuffer("url(");
            filterAttrBuf.append("#");
            filterAttrBuf.append(filterDef.getAttributeNS((String)null, "id"));
            filterAttrBuf.append(")");
            filterValue = filterAttrBuf.toString();
         } else {
            filterValue = "none";
         }

         compositeDesc = new SVGCompositeDescriptor(opacityValue, filterValue, filterDef);
         this.descMap.put(composite, compositeDesc);
      }

      if (composite.getRule() != 3) {
         this.backgroundAccessRequired = true;
      }

      return compositeDesc;
   }

   private Element compositeToSVG(AlphaComposite composite) {
      String operator = null;
      String input1 = null;
      String input2 = null;
      String k2 = "0";
      String id = null;
      switch (composite.getRule()) {
         case 1:
            operator = "arithmetic";
            input1 = "SourceGraphic";
            input2 = "BackgroundImage";
            id = "alphaCompositeClear";
            break;
         case 2:
            operator = "arithmetic";
            input1 = "SourceGraphic";
            input2 = "BackgroundImage";
            id = "alphaCompositeSrc";
            k2 = "1";
            break;
         case 3:
         default:
            throw new RuntimeException("invalid rule:" + composite.getRule());
         case 4:
            operator = "over";
            input2 = "SourceGraphic";
            input1 = "BackgroundImage";
            id = "alphaCompositeDstOver";
            break;
         case 5:
            operator = "in";
            input1 = "SourceGraphic";
            input2 = "BackgroundImage";
            id = "alphaCompositeSrcIn";
            break;
         case 6:
            operator = "in";
            input2 = "SourceGraphic";
            input1 = "BackgroundImage";
            id = "alphaCompositeDstIn";
            break;
         case 7:
            operator = "out";
            input1 = "SourceGraphic";
            input2 = "BackgroundImage";
            id = "alphaCompositeSrcOut";
            break;
         case 8:
            operator = "out";
            input2 = "SourceGraphic";
            input1 = "BackgroundImage";
            id = "alphaCompositeDstOut";
      }

      Element compositeFilter = this.generatorContext.domFactory.createElementNS("http://www.w3.org/2000/svg", "filter");
      compositeFilter.setAttributeNS((String)null, "id", id);
      compositeFilter.setAttributeNS((String)null, "filterUnits", "objectBoundingBox");
      compositeFilter.setAttributeNS((String)null, "x", "0%");
      compositeFilter.setAttributeNS((String)null, "y", "0%");
      compositeFilter.setAttributeNS((String)null, "width", "100%");
      compositeFilter.setAttributeNS((String)null, "height", "100%");
      Element feComposite = this.generatorContext.domFactory.createElementNS("http://www.w3.org/2000/svg", "feComposite");
      feComposite.setAttributeNS((String)null, "operator", operator);
      feComposite.setAttributeNS((String)null, "in", input1);
      feComposite.setAttributeNS((String)null, "in2", input2);
      feComposite.setAttributeNS((String)null, "k2", k2);
      feComposite.setAttributeNS((String)null, "result", "composite");
      Element feFlood = this.generatorContext.domFactory.createElementNS("http://www.w3.org/2000/svg", "feFlood");
      feFlood.setAttributeNS((String)null, "flood-color", "white");
      feFlood.setAttributeNS((String)null, "flood-opacity", "1");
      feFlood.setAttributeNS((String)null, "result", "flood");
      Element feMerge = this.generatorContext.domFactory.createElementNS("http://www.w3.org/2000/svg", "feMerge");
      Element feMergeNodeFlood = this.generatorContext.domFactory.createElementNS("http://www.w3.org/2000/svg", "feMergeNode");
      feMergeNodeFlood.setAttributeNS((String)null, "in", "flood");
      Element feMergeNodeComposite = this.generatorContext.domFactory.createElementNS("http://www.w3.org/2000/svg", "feMergeNode");
      feMergeNodeComposite.setAttributeNS((String)null, "in", "composite");
      feMerge.appendChild(feMergeNodeFlood);
      feMerge.appendChild(feMergeNodeComposite);
      compositeFilter.appendChild(feFlood);
      compositeFilter.appendChild(feComposite);
      compositeFilter.appendChild(feMerge);
      return compositeFilter;
   }
}
