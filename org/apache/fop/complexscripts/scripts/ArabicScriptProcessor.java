package org.apache.fop.complexscripts.scripts;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.complexscripts.bidi.BidiClass;
import org.apache.fop.complexscripts.fonts.GlyphDefinitionTable;
import org.apache.fop.complexscripts.util.CharAssociation;
import org.apache.fop.complexscripts.util.GlyphContextTester;
import org.apache.fop.complexscripts.util.GlyphSequence;
import org.apache.fop.complexscripts.util.ScriptContextTester;

public class ArabicScriptProcessor extends DefaultScriptProcessor {
   private static final Log log = LogFactory.getLog(ArabicScriptProcessor.class);
   private static final String[] GSUB_FEATURES = new String[]{"calt", "ccmp", "fina", "init", "isol", "liga", "medi", "rlig"};
   private static final String[] GPOS_FEATURES = new String[]{"curs", "kern", "mark", "mkmk"};
   private final ScriptContextTester subContextTester = new SubstitutionScriptContextTester();
   private final ScriptContextTester posContextTester = new PositioningScriptContextTester();
   private static final int[] ISOLATED_INITIALS = new int[]{1569, 1570, 1571, 1572, 1573, 1575, 1583, 1584, 1585, 1586, 1608, 1649, 1650, 1651, 1653, 1654, 1655, 1672, 1673, 1674, 1675, 1676, 1677, 1678, 1679, 1680, 1681, 1682, 1683, 1684, 1685, 1686, 1687, 1688, 1689, 1732, 1733, 1734, 1735, 1736, 1737, 1738, 1739, 1743, 1774, 1775};
   private static final int[] ISOLATED_FINALS = new int[]{1569};

   ArabicScriptProcessor(String script) {
      super(script);
   }

   public String[] getSubstitutionFeatures() {
      return GSUB_FEATURES;
   }

   public ScriptContextTester getSubstitutionContextTester() {
      return this.subContextTester;
   }

   public String[] getPositioningFeatures() {
      return GPOS_FEATURES;
   }

   public ScriptContextTester getPositioningContextTester() {
      return this.posContextTester;
   }

   public GlyphSequence reorderCombiningMarks(GlyphDefinitionTable gdef, GlyphSequence gs, int[] widths, int[][] gpa, String script, String language) {
      return gs;
   }

   private static boolean inFinalContext(String script, String language, String feature, GlyphSequence gs, int index, int flags) {
      CharAssociation a = gs.getAssociation(index);
      int[] ca = gs.getCharacterArray(false);
      int nc = gs.getCharacterCount();
      if (nc == 0) {
         return false;
      } else {
         int s = a.getStart();
         int e = a.getEnd();
         if (!hasFinalPrecedingContext(ca, nc, s, e)) {
            return false;
         } else if (!hasFinalThisContext(ca, nc, s, e)) {
            return false;
         } else if (forceFinalThisContext(ca, nc, s, e)) {
            return true;
         } else {
            return hasFinalSucceedingContext(ca, nc, s, e);
         }
      }
   }

   private static boolean inInitialContext(String script, String language, String feature, GlyphSequence gs, int index, int flags) {
      CharAssociation a = gs.getAssociation(index);
      int[] ca = gs.getCharacterArray(false);
      int nc = gs.getCharacterCount();
      if (nc == 0) {
         return false;
      } else {
         int s = a.getStart();
         int e = a.getEnd();
         if (!hasInitialPrecedingContext(ca, nc, s, e)) {
            return false;
         } else if (!hasInitialThisContext(ca, nc, s, e)) {
            return false;
         } else {
            return hasInitialSucceedingContext(ca, nc, s, e);
         }
      }
   }

   private static boolean inIsolateContext(String script, String language, String feature, GlyphSequence gs, int index, int flags) {
      CharAssociation a = gs.getAssociation(index);
      int nc = gs.getCharacterCount();
      if (nc == 0) {
         return false;
      } else {
         return a.getStart() == 0 && a.getEnd() == nc;
      }
   }

   private static boolean inLigatureContext(String script, String language, String feature, GlyphSequence gs, int index, int flags) {
      CharAssociation a = gs.getAssociation(index);
      int[] ca = gs.getCharacterArray(false);
      int nc = gs.getCharacterCount();
      if (nc == 0) {
         return false;
      } else {
         int s = a.getStart();
         int e = a.getEnd();
         if (!hasLigaturePrecedingContext(ca, nc, s, e)) {
            return false;
         } else {
            return hasLigatureSucceedingContext(ca, nc, s, e);
         }
      }
   }

   private static boolean inMedialContext(String script, String language, String feature, GlyphSequence gs, int index, int flags) {
      CharAssociation a = gs.getAssociation(index);
      int[] ca = gs.getCharacterArray(false);
      int nc = gs.getCharacterCount();
      if (nc == 0) {
         return false;
      } else {
         int s = a.getStart();
         int e = a.getEnd();
         if (!hasMedialPrecedingContext(ca, nc, s, e)) {
            return false;
         } else if (!hasMedialThisContext(ca, nc, s, e)) {
            return false;
         } else {
            return hasMedialSucceedingContext(ca, nc, s, e);
         }
      }
   }

   private static boolean hasFinalPrecedingContext(int[] ca, int nc, int s, int e) {
      int chp = 0;
      int clp = 0;

      for(int i = s; i > 0; --i) {
         int k = i - 1;
         if (k >= 0 && k < nc) {
            chp = ca[k];
            clp = BidiClass.getBidiClass(chp);
            if (clp != 14) {
               break;
            }
         }
      }

      if (clp != 5) {
         return isZWJ(chp);
      } else {
         return !hasIsolateInitial(chp);
      }
   }

   private static boolean hasFinalThisContext(int[] ca, int nc, int s, int e) {
      int chl = 0;
      int cll = 0;
      int i = 0;

      for(int n = e - s; i < n; ++i) {
         int k = n - i - 1;
         int j = s + k;
         if (j >= 0 && j < nc) {
            chl = ca[j];
            cll = BidiClass.getBidiClass(chl);
            if (cll != 14 && !isZWJ(chl)) {
               break;
            }
         }
      }

      if (cll != 5) {
         return false;
      } else {
         return !hasIsolateFinal(chl);
      }
   }

   private static boolean forceFinalThisContext(int[] ca, int nc, int s, int e) {
      int chl = 0;
      int cll = 0;
      int i = 0;

      for(int n = e - s; i < n; ++i) {
         int k = n - i - 1;
         int j = s + k;
         if (j >= 0 && j < nc) {
            chl = ca[j];
            cll = BidiClass.getBidiClass(chl);
            if (cll != 14 && !isZWJ(chl)) {
               break;
            }
         }
      }

      if (cll != 5) {
         return false;
      } else {
         return hasIsolateInitial(chl);
      }
   }

   private static boolean hasFinalSucceedingContext(int[] ca, int nc, int s, int e) {
      int chs = 0;
      int cls = 0;
      int i = e;

      for(int n = nc; i < n; ++i) {
         chs = ca[i];
         cls = BidiClass.getBidiClass(chs);
         if (cls != 14) {
            break;
         }
      }

      if (cls != 5) {
         return !isZWJ(chs);
      } else {
         return hasIsolateFinal(chs);
      }
   }

   private static boolean hasInitialPrecedingContext(int[] ca, int nc, int s, int e) {
      int chp = 0;
      int clp = 0;

      for(int i = s; i > 0; --i) {
         int k = i - 1;
         if (k >= 0 && k < nc) {
            chp = ca[k];
            clp = BidiClass.getBidiClass(chp);
            if (clp != 14) {
               break;
            }
         }
      }

      if (clp != 5) {
         return !isZWJ(chp);
      } else {
         return hasIsolateInitial(chp);
      }
   }

   private static boolean hasInitialThisContext(int[] ca, int nc, int s, int e) {
      int chf = 0;
      int clf = 0;
      int i = 0;

      for(int n = e - s; i < n; ++i) {
         int k = s + i;
         if (k >= 0 && k < nc) {
            chf = ca[s + i];
            clf = BidiClass.getBidiClass(chf);
            if (clf != 14 && !isZWJ(chf)) {
               break;
            }
         }
      }

      if (clf != 5) {
         return false;
      } else {
         return !hasIsolateInitial(chf);
      }
   }

   private static boolean hasInitialSucceedingContext(int[] ca, int nc, int s, int e) {
      int chs = 0;
      int cls = 0;
      int i = e;

      for(int n = nc; i < n; ++i) {
         chs = ca[i];
         cls = BidiClass.getBidiClass(chs);
         if (cls != 14) {
            break;
         }
      }

      if (cls != 5) {
         return isZWJ(chs);
      } else {
         return !hasIsolateFinal(chs);
      }
   }

   private static boolean hasMedialPrecedingContext(int[] ca, int nc, int s, int e) {
      int chp = 0;
      int clp = 0;

      for(int i = s; i > 0; --i) {
         int k = i - 1;
         if (k >= 0 && k < nc) {
            chp = ca[k];
            clp = BidiClass.getBidiClass(chp);
            if (clp != 14) {
               break;
            }
         }
      }

      if (clp != 5) {
         return isZWJ(chp);
      } else {
         return !hasIsolateInitial(chp);
      }
   }

   private static boolean hasMedialThisContext(int[] ca, int nc, int s, int e) {
      int chf = 0;
      int clf = 0;
      int chl = 0;

      int cll;
      int i;
      for(cll = e - s; chl < cll; ++chl) {
         i = s + chl;
         if (i >= 0 && i < nc) {
            chf = ca[s + chl];
            clf = BidiClass.getBidiClass(chf);
            if (clf != 14 && !isZWJ(chf)) {
               break;
            }
         }
      }

      if (clf != 5) {
         return false;
      } else {
         chl = 0;
         cll = 0;
         i = 0;

         for(int n = e - s; i < n; ++i) {
            int k = n - i - 1;
            int j = s + k;
            if (j >= 0 && j < nc) {
               chl = ca[j];
               cll = BidiClass.getBidiClass(chl);
               if (cll != 14 && !isZWJ(chl)) {
                  break;
               }
            }
         }

         if (cll != 5) {
            return false;
         } else if (hasIsolateFinal(chf)) {
            return false;
         } else {
            return !hasIsolateInitial(chl);
         }
      }
   }

   private static boolean hasMedialSucceedingContext(int[] ca, int nc, int s, int e) {
      int chs = 0;
      int cls = 0;
      int i = e;

      for(int n = nc; i < n; ++i) {
         chs = ca[i];
         cls = BidiClass.getBidiClass(chs);
         if (cls != 14) {
            break;
         }
      }

      if (cls != 5) {
         return isZWJ(chs);
      } else {
         return !hasIsolateFinal(chs);
      }
   }

   private static boolean hasLigaturePrecedingContext(int[] ca, int nc, int s, int e) {
      return true;
   }

   private static boolean hasLigatureSucceedingContext(int[] ca, int nc, int s, int e) {
      int chs = false;
      int cls = 0;
      int i = e;

      for(int n = nc; i < n; ++i) {
         int chs = ca[i];
         cls = BidiClass.getBidiClass(chs);
         if (cls != 14) {
            break;
         }
      }

      return cls == 5;
   }

   private static boolean hasIsolateInitial(int ch) {
      return Arrays.binarySearch(ISOLATED_INITIALS, ch) >= 0;
   }

   private static boolean hasIsolateFinal(int ch) {
      return Arrays.binarySearch(ISOLATED_FINALS, ch) >= 0;
   }

   private static boolean isZWJ(int ch) {
      return ch == 8205;
   }

   private static class PositioningScriptContextTester implements ScriptContextTester {
      private static Map testerMap = new HashMap();

      private PositioningScriptContextTester() {
      }

      public GlyphContextTester getTester(String feature) {
         return (GlyphContextTester)testerMap.get(feature);
      }

      // $FF: synthetic method
      PositioningScriptContextTester(Object x0) {
         this();
      }
   }

   private static class SubstitutionScriptContextTester implements ScriptContextTester {
      private static Map testerMap = new HashMap();

      private SubstitutionScriptContextTester() {
      }

      public GlyphContextTester getTester(String feature) {
         return (GlyphContextTester)testerMap.get(feature);
      }

      // $FF: synthetic method
      SubstitutionScriptContextTester(Object x0) {
         this();
      }

      static {
         testerMap.put("fina", new GlyphContextTester() {
            public boolean test(String script, String language, String feature, GlyphSequence gs, int index, int flags) {
               return ArabicScriptProcessor.inFinalContext(script, language, feature, gs, index, flags);
            }
         });
         testerMap.put("init", new GlyphContextTester() {
            public boolean test(String script, String language, String feature, GlyphSequence gs, int index, int flags) {
               return ArabicScriptProcessor.inInitialContext(script, language, feature, gs, index, flags);
            }
         });
         testerMap.put("isol", new GlyphContextTester() {
            public boolean test(String script, String language, String feature, GlyphSequence gs, int index, int flags) {
               return ArabicScriptProcessor.inIsolateContext(script, language, feature, gs, index, flags);
            }
         });
         testerMap.put("liga", new GlyphContextTester() {
            public boolean test(String script, String language, String feature, GlyphSequence gs, int index, int flags) {
               return ArabicScriptProcessor.inLigatureContext(script, language, feature, gs, index, flags);
            }
         });
         testerMap.put("medi", new GlyphContextTester() {
            public boolean test(String script, String language, String feature, GlyphSequence gs, int index, int flags) {
               return ArabicScriptProcessor.inMedialContext(script, language, feature, gs, index, flags);
            }
         });
      }
   }
}
