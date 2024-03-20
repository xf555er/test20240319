package org.apache.batik.svggen;

import java.util.HashSet;
import java.util.Set;

public class SVGAttribute {
   private String name;
   private Set applicabilitySet;
   private boolean isSetInclusive;

   public SVGAttribute(Set applicabilitySet, boolean isSetInclusive) {
      if (applicabilitySet == null) {
         applicabilitySet = new HashSet();
      }

      this.applicabilitySet = (Set)applicabilitySet;
      this.isSetInclusive = isSetInclusive;
   }

   public boolean appliesTo(String tag) {
      boolean tagInMap = this.applicabilitySet.contains(tag);
      if (this.isSetInclusive) {
         return tagInMap;
      } else {
         return !tagInMap;
      }
   }
}
