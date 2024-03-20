package org.apache.batik.svggen;

import org.w3c.dom.Element;

public class SVGFilterDescriptor {
   private Element def;
   private String filterValue;

   public SVGFilterDescriptor(String filterValue) {
      this.filterValue = filterValue;
   }

   public SVGFilterDescriptor(String filterValue, Element def) {
      this(filterValue);
      this.def = def;
   }

   public String getFilterValue() {
      return this.filterValue;
   }

   public Element getDef() {
      return this.def;
   }
}
