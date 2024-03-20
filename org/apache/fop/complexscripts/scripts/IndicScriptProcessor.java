package org.apache.fop.complexscripts.scripts;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.complexscripts.fonts.GlyphTable;
import org.apache.fop.complexscripts.util.CharAssociation;
import org.apache.fop.complexscripts.util.GlyphContextTester;
import org.apache.fop.complexscripts.util.GlyphSequence;
import org.apache.fop.complexscripts.util.ScriptContextTester;

public class IndicScriptProcessor extends DefaultScriptProcessor {
   private static final Log log = LogFactory.getLog(IndicScriptProcessor.class);
   private static final String[] GSUB_REQ_FEATURES = new String[]{"abvf", "abvs", "akhn", "blwf", "blws", "ccmp", "cjct", "clig", "half", "haln", "locl", "nukt", "pref", "pres", "pstf", "psts", "rkrf", "rphf", "vatu"};
   private static final String[] GSUB_OPT_FEATURES = new String[]{"afrc", "calt", "dlig"};
   private static final String[] GPOS_REQ_FEATURES = new String[]{"abvm", "blwm", "dist", "kern"};
   private static final String[] GPOS_OPT_FEATURES = new String[0];
   private final ScriptContextTester subContextTester = new SubstitutionScriptContextTester();
   private final ScriptContextTester posContextTester = new PositioningScriptContextTester();
   private static Set basicShapingFeatures = new HashSet();
   private static final String[] BASIC_SHAPING_FEATURE_STRINGS = new String[]{"abvf", "akhn", "blwf", "cjct", "half", "locl", "nukt", "pref", "pstf", "rkrf", "rphf", "vatu", "ccmp"};
   private static Set presentationFeatures;
   private static final String[] PRESENTATION_FEATURE_STRINGS;

   public static ScriptProcessor makeProcessor(String var0) {
      // $FF: Couldn't be decompiled
   }

   IndicScriptProcessor(String script) {
      super(script);
   }

   public String[] getSubstitutionFeatures() {
      return GSUB_REQ_FEATURES;
   }

   public String[] getOptionalSubstitutionFeatures() {
      return GSUB_OPT_FEATURES;
   }

   public ScriptContextTester getSubstitutionContextTester() {
      return this.subContextTester;
   }

   public String[] getPositioningFeatures() {
      return GPOS_REQ_FEATURES;
   }

   public String[] getOptionalPositioningFeatures() {
      return GPOS_OPT_FEATURES;
   }

   public ScriptContextTester getPositioningContextTester() {
      return this.posContextTester;
   }

   public GlyphSequence substitute(GlyphSequence gs, String script, String language, GlyphTable.UseSpec[] usa, ScriptContextTester sct) {
      assert usa != null;

      GlyphSequence[] sa = this.syllabize(gs, script, language);
      int i = 0;

      for(int n = sa.length; i < n; ++i) {
         GlyphSequence s = sa[i];
         GlyphTable.UseSpec[] var10 = usa;
         int var11 = usa.length;

         int var12;
         GlyphTable.UseSpec us;
         for(var12 = 0; var12 < var11; ++var12) {
            us = var10[var12];
            if (this.isBasicShapingUse(us)) {
               s.setPredications(true);
               s = us.substitute(s, script, language, sct);
            }
         }

         s = this.reorderPreBaseMatra(s);
         s = this.reorderReph(s);
         var10 = usa;
         var11 = usa.length;

         for(var12 = 0; var12 < var11; ++var12) {
            us = var10[var12];
            if (this.isPresentationUse(us)) {
               s.setPredications(true);
               s = us.substitute(s, script, language, sct);
            }
         }

         sa[i] = s;
      }

      return this.unsyllabize(gs, sa);
   }

   protected Class getSyllabizerClass() {
      return null;
   }

   private GlyphSequence[] syllabize(GlyphSequence gs, String script, String language) {
      return IndicScriptProcessor.Syllabizer.getSyllabizer(script, language, this.getSyllabizerClass()).syllabize(gs);
   }

   private GlyphSequence unsyllabize(GlyphSequence gs, GlyphSequence[] sa) {
      return GlyphSequence.join(gs, sa);
   }

   private boolean isBasicShapingUse(GlyphTable.UseSpec us) {
      assert us != null;

      return basicShapingFeatures != null ? basicShapingFeatures.contains(us.getFeature()) : false;
   }

   private boolean isPresentationUse(GlyphTable.UseSpec us) {
      assert us != null;

      return presentationFeatures != null ? presentationFeatures.contains(us.getFeature()) : false;
   }

   private GlyphSequence reorderPreBaseMatra(GlyphSequence gs) {
      int source;
      int target;
      if ((source = this.findPreBaseMatra(gs)) >= 0 && (target = this.findPreBaseMatraTarget(gs, source)) >= 0 && target != source) {
         gs = this.reorder(gs, source, target);
      }

      return gs;
   }

   protected int findPreBaseMatra(GlyphSequence gs) {
      return -1;
   }

   protected int findPreBaseMatraTarget(GlyphSequence gs, int source) {
      return -1;
   }

   private GlyphSequence reorderReph(GlyphSequence gs) {
      int source;
      int target;
      if ((source = this.findReph(gs)) >= 0 && (target = this.findRephTarget(gs, source)) >= 0 && target != source) {
         gs = this.reorder(gs, source, target);
      }

      return gs;
   }

   protected int findReph(GlyphSequence gs) {
      return -1;
   }

   protected int findRephTarget(GlyphSequence gs, int source) {
      return -1;
   }

   private GlyphSequence reorder(GlyphSequence gs, int source, int target) {
      return GlyphSequence.reorder(gs, source, 1, target);
   }

   public boolean position(GlyphSequence gs, String script, String language, int fontSize, GlyphTable.UseSpec[] usa, int[] widths, int[][] adjustments, ScriptContextTester sct) {
      boolean adjusted = super.position(gs, script, language, fontSize, usa, widths, adjustments, sct);
      return adjusted;
   }

   static {
      Collections.addAll(basicShapingFeatures, BASIC_SHAPING_FEATURE_STRINGS);
      PRESENTATION_FEATURE_STRINGS = new String[]{"abvs", "blws", "calt", "haln", "pres", "psts", "clig"};
      presentationFeatures = new HashSet();
      Collections.addAll(presentationFeatures, PRESENTATION_FEATURE_STRINGS);
   }

   protected static class Segment {
      static final int OTHER = 0;
      static final int SYLLABLE = 1;
      private int start;
      private int end;
      private int type;

      Segment(int start, int end, int type) {
         this.start = start;
         this.end = end;
         this.type = type;
      }

      int getStart() {
         return this.start;
      }

      int getEnd() {
         return this.end;
      }

      int getOffset() {
         return this.start;
      }

      int getCount() {
         return this.end - this.start;
      }

      int getType() {
         return this.type;
      }
   }

   protected static class DefaultSyllabizer extends Syllabizer {
      DefaultSyllabizer(String script, String language) {
         super(script, language);
      }

      GlyphSequence[] syllabize(GlyphSequence gs) {
         int[] ca = gs.getCharacterArray(false);
         int nc = gs.getCharacterCount();
         return nc == 0 ? new GlyphSequence[]{gs} : this.segmentize(gs, this.segmentize(ca, nc));
      }

      protected Segment[] segmentize(int[] ca, int nc) {
         Vector sv = new Vector(nc);
         int s = 0;
         int e = nc;

         while(s < e) {
            int i;
            if ((i = this.findStartOfSyllable(ca, s, e)) < e) {
               if (s < i) {
                  sv.add(new Segment(s, i, 0));
               }

               s = i;
            } else {
               if (s < e) {
                  sv.add(new Segment(s, e, 0));
               }

               s = e;
            }

            if ((i = this.findEndOfSyllable(ca, s, e)) > s) {
               if (s < i) {
                  sv.add(new Segment(s, i, 1));
               }

               s = i;
            } else {
               if (s < e) {
                  sv.add(new Segment(s, e, 0));
               }

               s = e;
            }
         }

         return (Segment[])sv.toArray(new Segment[sv.size()]);
      }

      protected GlyphSequence[] segmentize(GlyphSequence gs, Segment[] sa) {
         int ng = gs.getGlyphCount();
         int[] ga = gs.getGlyphArray(false);
         CharAssociation[] aa = gs.getAssociations(0, -1);
         Vector nsv = new Vector();
         Segment[] var7 = sa;
         int var8 = sa.length;

         for(int var9 = 0; var9 < var8; ++var9) {
            Segment s = var7[var9];
            Vector ngv = new Vector(ng);
            Vector nav = new Vector(ng);

            for(int j = 0; j < ng; ++j) {
               CharAssociation ca = aa[j];
               if (ca.contained(s.getOffset(), s.getCount())) {
                  ngv.add(ga[j]);
                  nav.add(ca);
               }
            }

            if (ngv.size() > 0) {
               nsv.add(new GlyphSequence(gs, (int[])null, toIntArray(ngv), (int[])null, (CharAssociation[])null, (CharAssociation[])nav.toArray(new CharAssociation[nav.size()]), (CharAssociation[])null));
            }
         }

         if (nsv.size() > 0) {
            return (GlyphSequence[])nsv.toArray(new GlyphSequence[nsv.size()]);
         } else {
            return new GlyphSequence[]{gs};
         }
      }

      protected int findStartOfSyllable(int[] ca, int s, int e) {
         return e;
      }

      protected int findEndOfSyllable(int[] ca, int s, int e) {
         return s;
      }

      private static int[] toIntArray(Vector iv) {
         int ni = iv.size();
         int[] ia = new int[iv.size()];
         int i = 0;

         for(int n = ni; i < n; ++i) {
            ia[i] = (Integer)iv.get(i);
         }

         return ia;
      }
   }

   protected abstract static class Syllabizer implements Comparable {
      private String script;
      private String language;
      private static Map syllabizers = new HashMap();

      Syllabizer(String script, String language) {
         this.script = script;
         this.language = language;
      }

      abstract GlyphSequence[] syllabize(GlyphSequence var1);

      public int hashCode() {
         int hc = 0;
         hc = 7 * hc + (hc ^ this.script.hashCode());
         hc = 11 * hc + (hc ^ this.language.hashCode());
         return hc;
      }

      public boolean equals(Object o) {
         if (o instanceof Syllabizer) {
            Syllabizer s = (Syllabizer)o;
            return !s.script.equals(this.script) ? false : s.language.equals(this.language);
         } else {
            return false;
         }
      }

      public int compareTo(Object o) {
         int d;
         if (o instanceof Syllabizer) {
            Syllabizer s = (Syllabizer)o;
            if ((d = this.script.compareTo(s.script)) == 0) {
               d = this.language.compareTo(s.language);
            }
         } else {
            d = -1;
         }

         return d;
      }

      static Syllabizer getSyllabizer(String script, String language, Class syllabizerClass) {
         String sid = makeSyllabizerId(script, language);
         Syllabizer s = (Syllabizer)syllabizers.get(sid);
         if (s == null) {
            if (syllabizerClass == null || (s = makeSyllabizer(script, language, syllabizerClass)) == null) {
               IndicScriptProcessor.log.warn("No syllabizer available for script '" + script + "', language '" + language + "', using default Indic syllabizer.");
               s = new DefaultSyllabizer(script, language);
            }

            syllabizers.put(sid, s);
         }

         return (Syllabizer)s;
      }

      static String makeSyllabizerId(String script, String language) {
         return script + ":" + language;
      }

      static Syllabizer makeSyllabizer(String script, String language, Class syllabizerClass) {
         Syllabizer s;
         try {
            Constructor cf = syllabizerClass.getDeclaredConstructor(String.class, String.class);
            s = (Syllabizer)cf.newInstance(script, language);
         } catch (NoSuchMethodException var5) {
            s = null;
         } catch (InstantiationException var6) {
            s = null;
         } catch (IllegalAccessException var7) {
            s = null;
         } catch (InvocationTargetException var8) {
            s = null;
         }

         return s;
      }
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
   }
}
