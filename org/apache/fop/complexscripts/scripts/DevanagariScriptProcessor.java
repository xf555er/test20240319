package org.apache.fop.complexscripts.scripts;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.complexscripts.util.CharAssociation;
import org.apache.fop.complexscripts.util.GlyphSequence;

public class DevanagariScriptProcessor extends IndicScriptProcessor {
   private static final Log log = LogFactory.getLog(DevanagariScriptProcessor.class);
   static final short C_U = 0;
   static final short C_C = 1;
   static final short C_V = 2;
   static final short C_M = 3;
   static final short C_S = 4;
   static final short C_T = 5;
   static final short C_A = 6;
   static final short C_P = 7;
   static final short C_D = 8;
   static final short C_H = 9;
   static final short C_O = 10;
   static final short C_N = 256;
   static final short C_R = 512;
   static final short C_PRE = 1024;
   static final short C_M_TYPE = 255;
   static final short C_M_FLAGS = 32512;
   static final int CCA_START = 2304;
   static final int CCA_END = 2432;
   static final short[] CCA = new short[]{10, 10, 10, 10, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 513, 769, 1, 1, 1, 1, 1, 1, 1, 1, 3, 3, 256, 4, 3, 1027, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 9, 3, 3, 4, 5, 5, 6, 6, 3, 3, 3, 257, 257, 257, 257, 257, 257, 257, 257, 2, 2, 3, 3, 7, 7, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 4, 4, 2, 2, 2, 2, 2, 2, 0, 1, 1, 1, 1, 1, 1, 1};

   DevanagariScriptProcessor(String script) {
      super(script);
   }

   protected Class getSyllabizerClass() {
      return DevanagariSyllabizer.class;
   }

   protected int findPreBaseMatra(GlyphSequence gs) {
      int ng = gs.getGlyphCount();
      int lk = -1;

      for(int i = ng; i > 0; --i) {
         int k = i - 1;
         if (containsPreBaseMatra(gs, k)) {
            lk = k;
            break;
         }
      }

      return lk;
   }

   protected int findPreBaseMatraTarget(GlyphSequence gs, int source) {
      int ng = gs.getGlyphCount();
      int lk = -1;

      for(int i = source < ng ? source : ng; i > 0; --i) {
         int k = i - 1;
         if (containsConsonant(gs, k)) {
            if (containsHalfConsonant(gs, k)) {
               lk = k;
            } else {
               if (lk != -1) {
                  break;
               }

               lk = k;
            }
         }
      }

      return lk;
   }

   private static boolean containsPreBaseMatra(GlyphSequence gs, int k) {
      CharAssociation a = gs.getAssociation(k);
      int[] ca = gs.getCharacterArray(false);
      int i = a.getStart();

      for(int e = a.getEnd(); i < e; ++i) {
         if (isPreM(ca[i])) {
            return true;
         }
      }

      return false;
   }

   private static boolean containsConsonant(GlyphSequence gs, int k) {
      CharAssociation a = gs.getAssociation(k);
      int[] ca = gs.getCharacterArray(false);
      int i = a.getStart();

      for(int e = a.getEnd(); i < e; ++i) {
         if (isC(ca[i])) {
            return true;
         }
      }

      return false;
   }

   private static boolean containsHalfConsonant(GlyphSequence gs, int k) {
      Boolean half = (Boolean)gs.getAssociation(k).getPredication("half");
      return half != null ? half : false;
   }

   protected int findReph(GlyphSequence gs) {
      int ng = gs.getGlyphCount();
      int li = -1;

      for(int i = 0; i < ng; ++i) {
         if (containsReph(gs, i)) {
            li = i;
            break;
         }
      }

      return li;
   }

   protected int findRephTarget(GlyphSequence gs, int source) {
      int ng = gs.getGlyphCount();
      int c1 = -1;
      int c2 = -1;

      int i;
      for(i = 0; i < ng; ++i) {
         if (i != source && containsConsonant(gs, i) && !containsHalfConsonant(gs, i)) {
            c1 = i + 1;
            break;
         }
      }

      for(i = c1 >= 0 ? c1 : 0; i < ng; ++i) {
         if (containsMatra(gs, i) && !containsPreBaseMatra(gs, i)) {
            c2 = i + 1;
         } else if (containsOtherMark(gs, i)) {
            c2 = i;
            break;
         }
      }

      if (c2 >= 0) {
         return c2;
      } else {
         return c1 >= 0 ? c1 : source;
      }
   }

   private static boolean containsReph(GlyphSequence gs, int k) {
      Boolean rphf = (Boolean)gs.getAssociation(k).getPredication("rphf");
      return rphf != null ? rphf : false;
   }

   private static boolean containsMatra(GlyphSequence gs, int k) {
      CharAssociation a = gs.getAssociation(k);
      int[] ca = gs.getCharacterArray(false);
      int i = a.getStart();

      for(int e = a.getEnd(); i < e; ++i) {
         if (isM(ca[i])) {
            return true;
         }
      }

      return false;
   }

   private static boolean containsOtherMark(GlyphSequence var0, int var1) {
      // $FF: Couldn't be decompiled
   }

   static int typeOf(int c) {
      return c >= 2304 && c < 2432 ? CCA[c - 2304] & 255 : 0;
   }

   static boolean isType(int c, int t) {
      return typeOf(c) == t;
   }

   static boolean hasFlag(int c, int f) {
      if (c >= 2304 && c < 2432) {
         return (CCA[c - 2304] & f) == f;
      } else {
         return false;
      }
   }

   static boolean isC(int c) {
      return isType(c, 1);
   }

   static boolean isR(int c) {
      return isType(c, 1) && hasR(c);
   }

   static boolean isV(int c) {
      return isType(c, 2);
   }

   static boolean isN(int c) {
      return c == 2364;
   }

   static boolean isH(int c) {
      return c == 2381;
   }

   static boolean isM(int c) {
      return isType(c, 3);
   }

   static boolean isPreM(int c) {
      return isType(c, 3) && hasFlag(c, 1024);
   }

   static boolean isX(int var0) {
      // $FF: Couldn't be decompiled
   }

   static boolean hasR(int c) {
      return hasFlag(c, 512);
   }

   static boolean hasN(int c) {
      return hasFlag(c, 256);
   }

   private static class DevanagariSyllabizer extends IndicScriptProcessor.DefaultSyllabizer {
      DevanagariSyllabizer(String script, String language) {
         super(script, language);
      }

      protected int findStartOfSyllable(int[] ca, int s, int e) {
         if (s >= 0 && s < e) {
            while(s < e) {
               int c = ca[s];
               if (DevanagariScriptProcessor.isC(c)) {
                  break;
               }

               ++s;
            }

            return s;
         } else {
            return -1;
         }
      }

      protected int findEndOfSyllable(int[] ca, int s, int e) {
         if (s >= 0 && s < e) {
            int nd = 0;

            int nl;
            int i;
            for(nl = 0; (i = this.isDeadConsonant(ca, s, e)) > s; ++nd) {
               s = i;
            }

            if ((i = this.isLiveConsonant(ca, s, e)) > s) {
               s = i;
               ++nl;
            }

            return nd <= 0 && nl <= 0 ? -1 : s;
         } else {
            return -1;
         }
      }

      private int isDeadConsonant(int[] ca, int s, int e) {
         if (s < 0) {
            return -1;
         } else {
            int i = 0;
            int nc = 0;
            int nh = 0;
            int c;
            if (s + i < e) {
               c = ca[s + i];
               if (!DevanagariScriptProcessor.isC(c)) {
                  return nc > 0 && nh > 0 ? s + i : -1;
               }

               ++i;
               ++nc;
            }

            if (s + i < e) {
               c = ca[s + 1];
               if (DevanagariScriptProcessor.isN(c)) {
                  ++i;
               }
            }

            if (s + i < e) {
               c = ca[s + i];
               if (DevanagariScriptProcessor.isH(c)) {
                  ++i;
                  ++nh;
               }
            }

            return nc > 0 && nh > 0 ? s + i : -1;
         }
      }

      private int isLiveConsonant(int[] ca, int s, int e) {
         if (s < 0) {
            return -1;
         } else {
            int c;
            int i;
            int nc;
            int nv;
            int nx;
            label59: {
               i = 0;
               nc = 0;
               nv = 0;
               nx = 0;
               if (s + i < e) {
                  c = ca[s + i];
                  if (DevanagariScriptProcessor.isC(c)) {
                     ++i;
                     ++nc;
                  } else {
                     if (!DevanagariScriptProcessor.isV(c)) {
                        break label59;
                     }

                     ++i;
                     ++nv;
                  }
               }

               if (s + i < e) {
                  c = ca[s + i];
                  if (DevanagariScriptProcessor.isN(c)) {
                     ++i;
                  }
               }

               while(s + i < e) {
                  c = ca[s + i];
                  if (!DevanagariScriptProcessor.isX(c)) {
                     break;
                  }

                  ++i;
                  ++nx;
               }
            }

            if (nx == 0 && s + i < e) {
               c = ca[s + i];
               if (DevanagariScriptProcessor.isH(c)) {
                  if (nc > 0) {
                     --nc;
                  } else if (nv > 0) {
                     --nv;
                  }
               }
            }

            return nc <= 0 && nv <= 0 ? -1 : s + i;
         }
      }
   }
}
