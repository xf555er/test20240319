package org.apache.fop.afp.fonts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class IntegerKeyStore {
   private static final int RANGE_BIT_SIZE = 8;
   private static final int RANGE_SIZE = 256;
   private final Map arrays = new HashMap();

   public void put(Integer index, Object value) {
      if (index < 0) {
         throw new IndexOutOfBoundsException();
      } else {
         int rangeKey = index >> 8;
         int rangeIndex = index % 256;
         ArrayList range = (ArrayList)this.arrays.get(rangeKey);
         if (range == null) {
            range = new ArrayList(Collections.nCopies(256, (Object)null));
            this.arrays.put(rangeKey, range);
         }

         range.set(rangeIndex, value);
      }
   }

   public Object get(Integer index) {
      if (index < 0) {
         throw new IndexOutOfBoundsException();
      } else {
         int rangeKey = index >> 8;
         int rangeIndex = index % 256;
         ArrayList range = (ArrayList)this.arrays.get(rangeKey);
         return range == null ? null : range.get(rangeIndex);
      }
   }
}
