package org.apache.fop.complexscripts.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CharAssociation implements Cloneable {
   private final int offset;
   private final int count;
   private final int[] subIntervals;
   private Map predications;
   private static volatile Map predicationMergers;
   private static final int[] SORT_INCREMENTS_16 = new int[]{1391376, 463792, 198768, 86961, 33936, 13776, 4592, 1968, 861, 336, 112, 48, 21, 7, 3, 1};
   private static final int[] SORT_INCREMENTS_03 = new int[]{7, 3, 1};

   public CharAssociation(int offset, int count, int[] subIntervals) {
      this.offset = offset;
      this.count = count;
      this.subIntervals = subIntervals != null && subIntervals.length > 2 ? subIntervals : null;
   }

   public CharAssociation(int offset, int count) {
      this(offset, count, (int[])null);
   }

   public CharAssociation(int[] subIntervals) {
      this(getSubIntervalsStart(subIntervals), getSubIntervalsLength(subIntervals), subIntervals);
   }

   public int getOffset() {
      return this.offset;
   }

   public int getCount() {
      return this.count;
   }

   public int getStart() {
      return this.getOffset();
   }

   public int getEnd() {
      return this.getOffset() + this.getCount();
   }

   public boolean isDisjoint() {
      return this.subIntervals != null;
   }

   public int[] getSubIntervals() {
      return this.subIntervals;
   }

   public int getSubIntervalCount() {
      return this.subIntervals != null ? this.subIntervals.length / 2 : 0;
   }

   public boolean contained(int offset, int count) {
      int s = offset;
      int e = offset + count;
      int ns;
      int i;
      if (this.isDisjoint()) {
         ns = this.getSubIntervalCount();

         for(i = 0; i < ns; ++i) {
            int s0 = this.subIntervals[2 * i + 0];
            int e0 = this.subIntervals[2 * i + 1];
            if (s0 >= s && e0 <= e) {
               return true;
            }
         }

         return false;
      } else {
         ns = this.getStart();
         i = this.getEnd();
         return ns >= offset && i <= e;
      }
   }

   public void setPredication(String key, Object value) {
      if (this.predications == null) {
         this.predications = new HashMap();
      }

      if (this.predications != null) {
         this.predications.put(key, value);
      }

   }

   public Object getPredication(String key) {
      return this.predications != null ? this.predications.get(key) : null;
   }

   public void mergePredication(String key, Object value) {
      if (this.predications == null) {
         this.predications = new HashMap();
      }

      if (this.predications != null) {
         if (this.predications.containsKey(key)) {
            Object v1 = this.predications.get(key);
            this.predications.put(key, mergePredicationValues(key, v1, value));
         } else {
            this.predications.put(key, value);
         }
      }

   }

   public static Object mergePredicationValues(String key, Object v1, Object v2) {
      PredicationMerger pm = getPredicationMerger(key);
      if (pm != null) {
         return pm.merge(key, v1, v2);
      } else {
         return v2 != null ? v2 : v1;
      }
   }

   public void mergePredications(CharAssociation ca) {
      if (ca.predications != null) {
         Iterator var2 = ca.predications.entrySet().iterator();

         while(var2.hasNext()) {
            Map.Entry e = (Map.Entry)var2.next();
            this.mergePredication((String)e.getKey(), e.getValue());
         }
      }

   }

   public Object clone() {
      try {
         CharAssociation ca = (CharAssociation)super.clone();
         if (this.predications != null) {
            ca.predications = new HashMap(this.predications);
         }

         return ca;
      } catch (CloneNotSupportedException var2) {
         return null;
      }
   }

   public static void setPredicationMerger(String key, PredicationMerger pm) {
      if (predicationMergers == null) {
         predicationMergers = new HashMap();
      }

      if (predicationMergers != null) {
         predicationMergers.put(key, pm);
      }

   }

   public static PredicationMerger getPredicationMerger(String key) {
      return predicationMergers != null ? (PredicationMerger)predicationMergers.get(key) : null;
   }

   public static CharAssociation[] replicate(CharAssociation a, int repeat) {
      CharAssociation[] aa = new CharAssociation[repeat];
      int i = 0;

      for(int n = aa.length; i < n; ++i) {
         aa[i] = (CharAssociation)a.clone();
      }

      return aa;
   }

   public static CharAssociation join(CharAssociation[] aa) {
      int[] ia = extractIntervals(aa);
      CharAssociation ca;
      if (ia != null && ia.length != 0) {
         if (ia.length == 2) {
            int s = ia[0];
            int e = ia[1];
            ca = new CharAssociation(s, e - s);
         } else {
            ca = new CharAssociation(mergeIntervals(ia));
         }
      } else {
         ca = new CharAssociation(0, 0);
      }

      return mergePredicates(ca, aa);
   }

   private static CharAssociation mergePredicates(CharAssociation ca, CharAssociation[] aa) {
      CharAssociation[] var2 = aa;
      int var3 = aa.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         CharAssociation a = var2[var4];
         ca.mergePredications(a);
      }

      return ca;
   }

   private static int getSubIntervalsStart(int[] ia) {
      int us = Integer.MAX_VALUE;
      int ue = Integer.MIN_VALUE;
      if (ia != null) {
         int i = 0;

         for(int n = ia.length; i < n; i += 2) {
            int s = ia[i + 0];
            int e = ia[i + 1];
            if (s < us) {
               us = s;
            }

            if (e > ue) {
               ue = e;
            }
         }

         if (ue < 0) {
            ue = 0;
         }

         if (us > ue) {
            us = ue;
         }
      }

      return us;
   }

   private static int getSubIntervalsLength(int[] ia) {
      int us = Integer.MAX_VALUE;
      int ue = Integer.MIN_VALUE;
      if (ia != null) {
         int i = 0;

         for(int n = ia.length; i < n; i += 2) {
            int s = ia[i + 0];
            int e = ia[i + 1];
            if (s < us) {
               us = s;
            }

            if (e > ue) {
               ue = e;
            }
         }

         if (ue < 0) {
            ue = 0;
         }

         if (us > ue) {
            us = ue;
         }
      }

      return ue - us;
   }

   private static int[] extractIntervals(CharAssociation[] aa) {
      int ni = 0;
      CharAssociation[] var2 = aa;
      int var3 = aa.length;

      int i;
      for(i = 0; i < var3; ++i) {
         CharAssociation a = var2[i];
         if (a.isDisjoint()) {
            ni += a.getSubIntervalCount();
         } else {
            ++ni;
         }
      }

      int[] sa = new int[ni];
      int[] ea = new int[ni];
      i = 0;

      for(int k = 0; i < aa.length; ++i) {
         CharAssociation a = aa[i];
         if (a.isDisjoint()) {
            int[] da = a.getSubIntervals();

            for(int j = 0; j < da.length; j += 2) {
               sa[k] = da[j + 0];
               ea[k] = da[j + 1];
               ++k;
            }
         } else {
            sa[k] = a.getStart();
            ea[k] = a.getEnd();
            ++k;
         }
      }

      return sortIntervals(sa, ea);
   }

   private static int[] sortIntervals(int[] sa, int[] ea) {
      assert sa != null;

      assert ea != null;

      assert sa.length == ea.length;

      int ni = sa.length;
      int[] incr = ni < 21 ? SORT_INCREMENTS_03 : SORT_INCREMENTS_16;
      int[] ia = incr;
      int i = incr.length;

      for(int var6 = 0; var6 < i; ++var6) {
         int anIncr = ia[var6];
         int h = anIncr;
         int i = anIncr;

         for(int n = ni; i < n; ++i) {
            int s1 = sa[i];
            int e1 = ea[i];

            int j;
            for(j = i; j >= h; j -= h) {
               int s2 = sa[j - h];
               int e2 = ea[j - h];
               if (s2 > s1) {
                  sa[j] = s2;
                  ea[j] = e2;
               } else {
                  if (s2 != s1 || e2 <= e1) {
                     break;
                  }

                  sa[j] = s2;
                  ea[j] = e2;
               }
            }

            sa[j] = s1;
            ea[j] = e1;
         }
      }

      ia = new int[ni * 2];

      for(i = 0; i < ni; ++i) {
         ia[i * 2 + 0] = sa[i];
         ia[i * 2 + 1] = ea[i];
      }

      return ia;
   }

   private static int[] mergeIntervals(int[] ia) {
      int ni = ia.length;
      int i = 0;
      int n = ni;
      int nm = 0;
      int ie = -1;

      int is;
      int s;
      for(is = -1; i < n; i += 2) {
         int s = ia[i + 0];
         s = ia[i + 1];
         if (ie >= 0 && s <= ie) {
            if (s >= is && s > ie) {
               ie = s;
            }
         } else {
            is = s;
            ie = s;
            ++nm;
         }
      }

      int[] mi = new int[nm * 2];
      i = 0;
      n = ni;
      nm = 0;
      ie = -1;

      for(is = -1; i < n; i += 2) {
         s = ia[i + 0];
         int e = ia[i + 1];
         int k = nm * 2;
         if (ie >= 0 && s <= ie) {
            if (s >= is) {
               if (e > ie) {
                  ie = e;
               }

               mi[k - 1] = ie;
            }
         } else {
            is = s;
            ie = e;
            mi[k + 0] = s;
            mi[k + 1] = e;
            ++nm;
         }
      }

      return mi;
   }

   public String toString() {
      StringBuffer sb = new StringBuffer();
      sb.append('[');
      sb.append(this.offset);
      sb.append(',');
      sb.append(this.count);
      sb.append(']');
      return sb.toString();
   }

   interface PredicationMerger {
      Object merge(String var1, Object var2, Object var3);
   }
}
