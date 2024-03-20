package org.apache.fop.complexscripts.util;

import java.util.Arrays;

public final class CharNormalize {
   private static final int[] DECOMPOSABLES = new int[]{2507, 2508, 2891, 2892, 3018, 3019, 3020, 3402, 3403, 3404, 3546, 3548, 3549, 3550};
   private static final int[][] DECOMPOSITIONS = new int[][]{{2503, 2494}, {2503, 2519}, {2887, 2894}, {2887, 2903}, {3014, 3006}, {3015, 3006}, {3014, 3031}, {3398, 3390}, {3399, 3390}, {3398, 3415}, {3545, 3530}, {3545, 3535}, {3545, 3535, 3530}, {3545, 3551}};
   private static final int MAX_DECOMPOSITION_LENGTH = 3;

   private CharNormalize() {
   }

   public static boolean isDecomposable(int c) {
      return Arrays.binarySearch(DECOMPOSABLES, c) >= 0;
   }

   public static int maximumDecompositionLength() {
      return 3;
   }

   public static int[] decompose(int c, int[] da) {
      int di = Arrays.binarySearch(DECOMPOSABLES, c);
      if (di >= 0) {
         return DECOMPOSITIONS[di];
      } else if (da != null && da.length > 1) {
         da[0] = c;
         da[1] = 0;
         return da;
      } else {
         return new int[]{c};
      }
   }
}
