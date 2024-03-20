package org.apache.batik.svggen;

import java.util.HashMap;
import java.util.Map;

public class SVGIDGenerator {
   private Map prefixMap = new HashMap();

   public String generateID(String prefix) {
      Integer maxId = (Integer)this.prefixMap.get(prefix);
      if (maxId == null) {
         maxId = 0;
         this.prefixMap.put(prefix, maxId);
      }

      maxId = maxId + 1;
      this.prefixMap.put(prefix, maxId);
      return prefix + maxId;
   }
}
