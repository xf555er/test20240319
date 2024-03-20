package org.apache.fop.complexscripts.fonts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.complexscripts.util.GlyphSequence;
import org.apache.fop.complexscripts.util.ScriptContextTester;

public class GlyphTable {
   private static final Log log = LogFactory.getLog(GlyphTable.class);
   public static final int GLYPH_TABLE_TYPE_SUBSTITUTION = 1;
   public static final int GLYPH_TABLE_TYPE_POSITIONING = 2;
   public static final int GLYPH_TABLE_TYPE_JUSTIFICATION = 3;
   public static final int GLYPH_TABLE_TYPE_BASELINE = 4;
   public static final int GLYPH_TABLE_TYPE_DEFINITION = 5;
   private GlyphTable gdef;
   private Map lookups;
   private Map lookupTables;
   private Map matchedLookups;
   private boolean frozen;
   protected Map processors;

   public GlyphTable(GlyphTable gdef, Map lookups, Map processors) {
      this.processors = processors;
      if (gdef != null && !(gdef instanceof GlyphDefinitionTable)) {
         throw new AdvancedTypographicTableFormatException("bad glyph definition table");
      } else if (lookups == null) {
         throw new AdvancedTypographicTableFormatException("lookups must be non-null map");
      } else {
         this.gdef = gdef;
         this.lookups = lookups;
         this.lookupTables = new LinkedHashMap();
         this.matchedLookups = new HashMap();
      }
   }

   public GlyphDefinitionTable getGlyphDefinitions() {
      return (GlyphDefinitionTable)this.gdef;
   }

   public List getLookups() {
      return this.matchLookupSpecs("*", "*", "*");
   }

   public List getLookupTables() {
      TreeSet lids = new TreeSet(this.lookupTables.keySet());
      List ltl = new ArrayList(lids.size());
      Iterator var3 = lids.iterator();

      while(var3.hasNext()) {
         Object lid1 = var3.next();
         String lid = (String)lid1;
         ltl.add(this.lookupTables.get(lid));
      }

      return ltl;
   }

   public LookupTable getLookupTable(String lid) {
      return (LookupTable)this.lookupTables.get(lid);
   }

   protected void addSubtable(GlyphSubtable subtable) {
      if (this.frozen) {
         throw new IllegalStateException("glyph table is frozen, subtable addition prohibited");
      } else {
         subtable.setTable(this);
         String lid = subtable.getLookupId();
         LookupTable lt;
         if (this.lookupTables.containsKey(lid)) {
            lt = (LookupTable)this.lookupTables.get(lid);
            lt.addSubtable(subtable);
         } else {
            lt = new LookupTable(lid, subtable);
            this.lookupTables.put(lid, lt);
         }

      }
   }

   protected void freezeSubtables() {
      if (!this.frozen) {
         Iterator var1 = this.lookupTables.values().iterator();

         while(var1.hasNext()) {
            Object o = var1.next();
            LookupTable lt = (LookupTable)o;
            lt.freezeSubtables(this.lookupTables);
         }

         this.frozen = true;
      }

   }

   public List matchLookupSpecs(String script, String language, String feature) {
      Set keys = this.lookups.keySet();
      List matches = new ArrayList();
      Iterator var6 = keys.iterator();

      while(true) {
         LookupSpec ls;
         do {
            do {
               do {
                  if (!var6.hasNext()) {
                     return matches;
                  }

                  Object key = var6.next();
                  ls = (LookupSpec)key;
               } while(!"*".equals(script) && !ls.getScript().equals(script));
            } while(!"*".equals(language) && !ls.getLanguage().equals(language));
         } while(!"*".equals(feature) && !ls.getFeature().equals(feature));

         matches.add(ls);
      }
   }

   public Map matchLookups(String script, String language, String feature) {
      LookupSpec lsm = new LookupSpec(script, language, feature, true, true);
      Map lm = (Map)this.matchedLookups.get(lsm);
      if (lm == null) {
         lm = new LinkedHashMap();
         List lsl = this.matchLookupSpecs(script, language, feature);
         Iterator var7 = lsl.iterator();

         while(var7.hasNext()) {
            Object aLsl = var7.next();
            LookupSpec ls = (LookupSpec)aLsl;
            ((Map)lm).put(ls, this.findLookupTables(ls));
         }

         this.matchedLookups.put(lsm, lm);
      }

      return (Map)(((Map)lm).isEmpty() && !OTFScript.isDefault(script) && !OTFScript.isWildCard(script) ? this.matchLookups("DFLT", "dflt", feature) : lm);
   }

   public List findLookupTables(LookupSpec ls) {
      TreeSet lts = new TreeSet();
      List ids;
      if ((ids = (List)this.lookups.get(ls)) != null) {
         Iterator var4 = ids.iterator();

         while(var4.hasNext()) {
            Object id = var4.next();
            String lid = (String)id;
            LookupTable lt;
            if ((lt = (LookupTable)this.lookupTables.get(lid)) != null) {
               lts.add(lt);
            }
         }
      }

      return new ArrayList(lts);
   }

   public UseSpec[] assembleLookups(String[] features, Map lookups) {
      TreeSet uss = new TreeSet();
      String[] var4 = features;
      int var5 = features.length;

      label37:
      for(int var6 = 0; var6 < var5; ++var6) {
         String feature = var4[var6];
         Iterator var8 = lookups.entrySet().iterator();

         while(true) {
            List ltl;
            do {
               Map.Entry e;
               LookupSpec ls;
               do {
                  if (!var8.hasNext()) {
                     continue label37;
                  }

                  Object o = var8.next();
                  e = (Map.Entry)o;
                  ls = (LookupSpec)e.getKey();
               } while(!ls.getFeature().equals(feature));

               ltl = (List)e.getValue();
            } while(ltl == null);

            Iterator var13 = ltl.iterator();

            while(var13.hasNext()) {
               Object aLtl = var13.next();
               LookupTable lt = (LookupTable)aLtl;
               uss.add(new UseSpec(lt, feature));
            }
         }
      }

      return (UseSpec[])uss.toArray(new UseSpec[uss.size()]);
   }

   public boolean hasFeature(String script, String language, String feature) {
      UseSpec[] usa = this.assembleLookups(new String[]{feature}, this.matchLookups(script, language, feature));
      return usa.length > 0;
   }

   public String toString() {
      StringBuffer sb = new StringBuffer(super.toString());
      sb.append("{");
      sb.append("lookups={");
      sb.append(this.lookups.toString());
      sb.append("},lookupTables={");
      sb.append(this.lookupTables.toString());
      sb.append("}}");
      return sb.toString();
   }

   public static int getTableTypeFromName(String name) {
      String s = name.toLowerCase();
      byte t;
      if ("gsub".equals(s)) {
         t = 1;
      } else if ("gpos".equals(s)) {
         t = 2;
      } else if ("jstf".equals(s)) {
         t = 3;
      } else if ("base".equals(s)) {
         t = 4;
      } else if ("gdef".equals(s)) {
         t = 5;
      } else {
         t = -1;
      }

      return t;
   }

   public static void resolveLookupReferences(RuleSet[] rsa, Map lookupTables) {
      if (rsa != null && lookupTables != null) {
         RuleSet[] var2 = rsa;
         int var3 = rsa.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            RuleSet rs = var2[var4];
            if (rs != null) {
               rs.resolveLookupReferences(lookupTables);
            }
         }
      }

   }

   public static class HomogeneousRuleSet extends RuleSet {
      public HomogeneousRuleSet(Rule[] rules) throws AdvancedTypographicTableFormatException {
         super(rules);
         Rule r0 = null;
         int i = 1;

         int i;
         for(i = rules.length; r0 == null && i < i; ++i) {
            if (rules[i] != null) {
               r0 = rules[i];
            }
         }

         if (r0 != null) {
            Class c = r0.getClass();
            i = 1;

            for(int n = rules.length; i < n; ++i) {
               Rule r = rules[i];
               if (r != null && !c.isInstance(r)) {
                  throw new AdvancedTypographicTableFormatException("rules[" + i + "] is not an instance of " + c.getName());
               }
            }
         }

      }
   }

   public static class RuleSet {
      private final Rule[] rules;

      public RuleSet(Rule[] rules) throws AdvancedTypographicTableFormatException {
         if (rules == null) {
            throw new AdvancedTypographicTableFormatException("rules[] is null");
         } else {
            this.rules = rules;
         }
      }

      public Rule[] getRules() {
         return this.rules;
      }

      public void resolveLookupReferences(Map lookupTables) {
         if (this.rules != null) {
            Rule[] var2 = this.rules;
            int var3 = var2.length;

            for(int var4 = 0; var4 < var3; ++var4) {
               Rule r = var2[var4];
               if (r != null) {
                  r.resolveLookupReferences(lookupTables);
               }
            }
         }

      }

      public String toString() {
         return "{ rules = " + Arrays.toString(this.rules) + " }";
      }
   }

   public static class ChainedCoverageSequenceRule extends CoverageSequenceRule {
      private final GlyphCoverageTable[] backtrackCoverages;
      private final GlyphCoverageTable[] lookaheadCoverages;

      public ChainedCoverageSequenceRule(RuleLookup[] lookups, int inputSequenceLength, GlyphCoverageTable[] coverages, GlyphCoverageTable[] backtrackCoverages, GlyphCoverageTable[] lookaheadCoverages) {
         super(lookups, inputSequenceLength, coverages);

         assert backtrackCoverages != null;

         assert lookaheadCoverages != null;

         this.backtrackCoverages = backtrackCoverages;
         this.lookaheadCoverages = lookaheadCoverages;
      }

      public GlyphCoverageTable[] getBacktrackCoverages() {
         return this.backtrackCoverages;
      }

      public GlyphCoverageTable[] getLookaheadCoverages() {
         return this.lookaheadCoverages;
      }

      public String toString() {
         StringBuffer sb = new StringBuffer();
         sb.append("{ ");
         sb.append("lookups = " + Arrays.toString(this.getLookups()));
         sb.append(", coverages = " + Arrays.toString(this.getCoverages()));
         sb.append(", backtrackCoverages = " + Arrays.toString(this.backtrackCoverages));
         sb.append(", lookaheadCoverages = " + Arrays.toString(this.lookaheadCoverages));
         sb.append(" }");
         return sb.toString();
      }
   }

   public static class ChainedClassSequenceRule extends ClassSequenceRule {
      private final int[] backtrackClasses;
      private final int[] lookaheadClasses;

      public ChainedClassSequenceRule(RuleLookup[] lookups, int inputSequenceLength, int[] classes, int[] backtrackClasses, int[] lookaheadClasses) {
         super(lookups, inputSequenceLength, classes);

         assert backtrackClasses != null;

         assert lookaheadClasses != null;

         this.backtrackClasses = backtrackClasses;
         this.lookaheadClasses = lookaheadClasses;
      }

      public int[] getBacktrackClasses() {
         return this.backtrackClasses;
      }

      public int[] getLookaheadClasses() {
         return this.lookaheadClasses;
      }

      public String toString() {
         StringBuffer sb = new StringBuffer();
         sb.append("{ ");
         sb.append("lookups = " + Arrays.toString(this.getLookups()));
         sb.append(", classes = " + Arrays.toString(this.getClasses()));
         sb.append(", backtrackClasses = " + Arrays.toString(this.backtrackClasses));
         sb.append(", lookaheadClasses = " + Arrays.toString(this.lookaheadClasses));
         sb.append(" }");
         return sb.toString();
      }
   }

   public static class ChainedGlyphSequenceRule extends GlyphSequenceRule {
      private final int[] backtrackGlyphs;
      private final int[] lookaheadGlyphs;

      public ChainedGlyphSequenceRule(RuleLookup[] lookups, int inputSequenceLength, int[] glyphs, int[] backtrackGlyphs, int[] lookaheadGlyphs) {
         super(lookups, inputSequenceLength, glyphs);

         assert backtrackGlyphs != null;

         assert lookaheadGlyphs != null;

         this.backtrackGlyphs = backtrackGlyphs;
         this.lookaheadGlyphs = lookaheadGlyphs;
      }

      public int[] getBacktrackGlyphs() {
         return this.backtrackGlyphs;
      }

      public int[] getLookaheadGlyphs() {
         return this.lookaheadGlyphs;
      }

      public String toString() {
         StringBuffer sb = new StringBuffer();
         sb.append("{ ");
         sb.append("lookups = " + Arrays.toString(this.getLookups()));
         sb.append(", glyphs = " + Arrays.toString(this.getGlyphs()));
         sb.append(", backtrackGlyphs = " + Arrays.toString(this.backtrackGlyphs));
         sb.append(", lookaheadGlyphs = " + Arrays.toString(this.lookaheadGlyphs));
         sb.append(" }");
         return sb.toString();
      }
   }

   public static class CoverageSequenceRule extends Rule {
      private final GlyphCoverageTable[] coverages;

      public CoverageSequenceRule(RuleLookup[] lookups, int inputSequenceLength, GlyphCoverageTable[] coverages) {
         super(lookups, inputSequenceLength);

         assert coverages != null;

         this.coverages = coverages;
      }

      public GlyphCoverageTable[] getCoverages() {
         return this.coverages;
      }

      public String toString() {
         StringBuffer sb = new StringBuffer();
         sb.append("{ ");
         sb.append("lookups = " + Arrays.toString(this.getLookups()));
         sb.append(", coverages = " + Arrays.toString(this.coverages));
         sb.append(" }");
         return sb.toString();
      }
   }

   public static class ClassSequenceRule extends Rule {
      private final int[] classes;

      public ClassSequenceRule(RuleLookup[] lookups, int inputSequenceLength, int[] classes) {
         super(lookups, inputSequenceLength);

         assert classes != null;

         this.classes = classes;
      }

      public int[] getClasses() {
         return this.classes;
      }

      public int[] getClasses(int firstClass) {
         int[] ca = new int[this.classes.length + 1];
         ca[0] = firstClass;
         System.arraycopy(this.classes, 0, ca, 1, this.classes.length);
         return ca;
      }

      public String toString() {
         StringBuffer sb = new StringBuffer();
         sb.append("{ ");
         sb.append("lookups = " + Arrays.toString(this.getLookups()));
         sb.append(", classes = " + Arrays.toString(this.classes));
         sb.append(" }");
         return sb.toString();
      }
   }

   public static class GlyphSequenceRule extends Rule {
      private final int[] glyphs;

      public GlyphSequenceRule(RuleLookup[] lookups, int inputSequenceLength, int[] glyphs) {
         super(lookups, inputSequenceLength);

         assert glyphs != null;

         this.glyphs = glyphs;
      }

      public int[] getGlyphs() {
         return this.glyphs;
      }

      public int[] getGlyphs(int firstGlyph) {
         int[] ga = new int[this.glyphs.length + 1];
         ga[0] = firstGlyph;
         System.arraycopy(this.glyphs, 0, ga, 1, this.glyphs.length);
         return ga;
      }

      public String toString() {
         StringBuffer sb = new StringBuffer();
         sb.append("{ ");
         sb.append("lookups = " + Arrays.toString(this.getLookups()));
         sb.append(", glyphs = " + Arrays.toString(this.glyphs));
         sb.append(" }");
         return sb.toString();
      }
   }

   public abstract static class Rule {
      private final RuleLookup[] lookups;
      private final int inputSequenceLength;

      protected Rule(RuleLookup[] lookups, int inputSequenceLength) {
         assert lookups != null;

         this.lookups = lookups;
         this.inputSequenceLength = inputSequenceLength;
      }

      public RuleLookup[] getLookups() {
         return this.lookups;
      }

      public int getInputSequenceLength() {
         return this.inputSequenceLength;
      }

      public void resolveLookupReferences(Map lookupTables) {
         if (this.lookups != null) {
            RuleLookup[] var2 = this.lookups;
            int var3 = var2.length;

            for(int var4 = 0; var4 < var3; ++var4) {
               RuleLookup l = var2[var4];
               if (l != null) {
                  l.resolveLookupReferences(lookupTables);
               }
            }
         }

      }

      public String toString() {
         return "{ lookups = " + Arrays.toString(this.lookups) + ", inputSequenceLength = " + this.inputSequenceLength + " }";
      }
   }

   public static class RuleLookup {
      private final int sequenceIndex;
      private final int lookupIndex;
      private LookupTable lookup;

      public RuleLookup(int sequenceIndex, int lookupIndex) {
         this.sequenceIndex = sequenceIndex;
         this.lookupIndex = lookupIndex;
         this.lookup = null;
      }

      public int getSequenceIndex() {
         return this.sequenceIndex;
      }

      public int getLookupIndex() {
         return this.lookupIndex;
      }

      public LookupTable getLookup() {
         return this.lookup;
      }

      public void resolveLookupReferences(Map lookupTables) {
         if (lookupTables != null) {
            String lid = "lu" + Integer.toString(this.lookupIndex);
            LookupTable lt = (LookupTable)lookupTables.get(lid);
            if (lt != null) {
               this.lookup = lt;
            } else {
               GlyphTable.log.warn("unable to resolve glyph lookup table reference '" + lid + "' amongst lookup tables: " + lookupTables.values());
            }
         }

      }

      public String toString() {
         return "{ sequenceIndex = " + this.sequenceIndex + ", lookupIndex = " + this.lookupIndex + " }";
      }
   }

   public static class UseSpec implements Comparable {
      private final LookupTable lookupTable;
      private final String feature;

      public UseSpec(LookupTable lookupTable, String feature) {
         this.lookupTable = lookupTable;
         this.feature = feature;
      }

      public LookupTable getLookupTable() {
         return this.lookupTable;
      }

      public String getFeature() {
         return this.feature;
      }

      public GlyphSequence substitute(GlyphSequence gs, String script, String language, ScriptContextTester sct) {
         return this.lookupTable.substitute(gs, script, language, this.feature, sct);
      }

      public boolean position(GlyphSequence gs, String script, String language, int fontSize, int[] widths, int[][] adjustments, ScriptContextTester sct) {
         return this.lookupTable.position(gs, script, language, this.feature, fontSize, widths, adjustments, sct);
      }

      public int hashCode() {
         return this.lookupTable.hashCode();
      }

      public boolean equals(Object o) {
         if (o instanceof UseSpec) {
            UseSpec u = (UseSpec)o;
            return this.lookupTable.equals(u.lookupTable);
         } else {
            return false;
         }
      }

      public int compareTo(Object o) {
         if (o instanceof UseSpec) {
            UseSpec u = (UseSpec)o;
            return this.lookupTable.compareTo(u.lookupTable);
         } else {
            return -1;
         }
      }
   }

   public static class LookupTable implements Comparable {
      private final String id;
      private final int idOrdinal;
      private final List subtables;
      private boolean doesSub;
      private boolean doesPos;
      private boolean frozen;
      private GlyphSubtable[] subtablesArray;
      private static GlyphSubtable[] subtablesArrayEmpty = new GlyphSubtable[0];

      public LookupTable(String id, GlyphSubtable subtable) {
         this(id, makeSingleton(subtable));
      }

      public LookupTable(String id, List subtables) {
         assert id != null;

         assert id.length() != 0;

         assert id.startsWith("lu");

         this.id = id;
         this.idOrdinal = Integer.parseInt(id.substring(2));
         this.subtables = new LinkedList();
         if (subtables != null) {
            Iterator var3 = subtables.iterator();

            while(var3.hasNext()) {
               Object subtable = var3.next();
               GlyphSubtable st = (GlyphSubtable)subtable;
               this.addSubtable(st);
            }
         }

      }

      public GlyphSubtable[] getSubtables() {
         if (this.frozen) {
            return this.subtablesArray != null ? this.subtablesArray : subtablesArrayEmpty;
         } else if (this.doesSub) {
            return (GlyphSubtable[])this.subtables.toArray(new GlyphSubstitutionSubtable[this.subtables.size()]);
         } else {
            return this.doesPos ? (GlyphSubtable[])this.subtables.toArray(new GlyphPositioningSubtable[this.subtables.size()]) : null;
         }
      }

      public boolean addSubtable(GlyphSubtable subtable) {
         boolean added = false;
         if (this.frozen) {
            throw new IllegalStateException("glyph table is frozen, subtable addition prohibited");
         } else {
            this.validateSubtable(subtable);
            ListIterator lit = this.subtables.listIterator(0);

            while(lit.hasNext()) {
               GlyphSubtable st = (GlyphSubtable)lit.next();
               int d;
               if ((d = subtable.compareTo(st)) < 0) {
                  lit.set(subtable);
                  lit.add(st);
                  added = true;
               } else if (d == 0) {
                  added = false;
                  subtable = null;
               }
            }

            if (!added && subtable != null) {
               this.subtables.add(subtable);
               added = true;
            }

            return added;
         }
      }

      private void validateSubtable(GlyphSubtable subtable) {
         if (subtable == null) {
            throw new AdvancedTypographicTableFormatException("subtable must be non-null");
         } else {
            if (subtable instanceof GlyphSubstitutionSubtable) {
               if (this.doesPos) {
                  throw new AdvancedTypographicTableFormatException("subtable must be positioning subtable, but is: " + subtable);
               }

               this.doesSub = true;
            }

            if (subtable instanceof GlyphPositioningSubtable) {
               if (this.doesSub) {
                  throw new AdvancedTypographicTableFormatException("subtable must be substitution subtable, but is: " + subtable);
               }

               this.doesPos = true;
            }

            if (this.subtables.size() > 0) {
               GlyphSubtable st = (GlyphSubtable)this.subtables.get(0);
               if (!st.isCompatible(subtable)) {
                  throw new AdvancedTypographicTableFormatException("subtable " + subtable + " is not compatible with subtable " + st);
               }
            }

         }
      }

      public void freezeSubtables(Map lookupTables) {
         if (!this.frozen) {
            GlyphSubtable[] sta = this.getSubtables();
            this.resolveLookupReferences(sta, lookupTables);
            this.subtablesArray = sta;
            this.frozen = true;
         }

      }

      private void resolveLookupReferences(GlyphSubtable[] subtables, Map lookupTables) {
         if (subtables != null) {
            GlyphSubtable[] var3 = subtables;
            int var4 = subtables.length;

            for(int var5 = 0; var5 < var4; ++var5) {
               GlyphSubtable st = var3[var5];
               if (st != null) {
                  st.resolveLookupReferences(lookupTables);
               }
            }
         }

      }

      public boolean performsSubstitution() {
         return this.doesSub;
      }

      public GlyphSequence substitute(GlyphSequence gs, String script, String language, String feature, ScriptContextTester sct) {
         return this.performsSubstitution() ? GlyphSubstitutionSubtable.substitute(gs, script, language, feature, (GlyphSubstitutionSubtable[])((GlyphSubstitutionSubtable[])this.subtablesArray), sct) : gs;
      }

      public GlyphSequence substitute(GlyphSubstitutionState ss, int sequenceIndex) {
         return this.performsSubstitution() ? GlyphSubstitutionSubtable.substitute(ss, (GlyphSubstitutionSubtable[])((GlyphSubstitutionSubtable[])this.subtablesArray), sequenceIndex) : ss.getInput();
      }

      public boolean performsPositioning() {
         return this.doesPos;
      }

      public boolean position(GlyphSequence gs, String script, String language, String feature, int fontSize, int[] widths, int[][] adjustments, ScriptContextTester sct) {
         return this.performsPositioning() ? GlyphPositioningSubtable.position(gs, script, language, feature, fontSize, (GlyphPositioningSubtable[])((GlyphPositioningSubtable[])this.subtablesArray), widths, adjustments, sct) : false;
      }

      public boolean position(GlyphPositioningState ps, int sequenceIndex) {
         return this.performsPositioning() ? GlyphPositioningSubtable.position(ps, (GlyphPositioningSubtable[])((GlyphPositioningSubtable[])this.subtablesArray), sequenceIndex) : false;
      }

      public int hashCode() {
         return this.idOrdinal;
      }

      public boolean equals(Object o) {
         if (o instanceof LookupTable) {
            LookupTable lt = (LookupTable)o;
            return this.idOrdinal == lt.idOrdinal;
         } else {
            return false;
         }
      }

      public int compareTo(Object o) {
         if (o instanceof LookupTable) {
            LookupTable lt = (LookupTable)o;
            int i = this.idOrdinal;
            int j = lt.idOrdinal;
            if (i < j) {
               return -1;
            } else {
               return i > j ? 1 : 0;
            }
         } else {
            return -1;
         }
      }

      public String toString() {
         StringBuffer sb = new StringBuffer();
         sb.append("{ ");
         sb.append("id = " + this.id);
         sb.append(", subtables = " + this.subtables);
         sb.append(" }");
         return sb.toString();
      }

      private static List makeSingleton(GlyphSubtable subtable) {
         if (subtable == null) {
            return null;
         } else {
            List stl = new ArrayList(1);
            stl.add(subtable);
            return stl;
         }
      }
   }

   public static class LookupSpec implements Comparable {
      private final String script;
      private final String language;
      private final String feature;

      public LookupSpec(String script, String language, String feature) {
         this(script, language, feature, false, false);
      }

      LookupSpec(String script, String language, String feature, boolean permitEmpty, boolean permitWildcard) {
         if (script == null || !permitEmpty && script.length() == 0) {
            throw new AdvancedTypographicTableFormatException("script must be non-empty string");
         } else if (language != null && (permitEmpty || language.length() != 0)) {
            if (feature == null || !permitEmpty && feature.length() == 0) {
               throw new AdvancedTypographicTableFormatException("feature must be non-empty string");
            } else if (!permitWildcard && script.equals("*")) {
               throw new AdvancedTypographicTableFormatException("script must not be wildcard");
            } else if (!permitWildcard && language.equals("*")) {
               throw new AdvancedTypographicTableFormatException("language must not be wildcard");
            } else if (!permitWildcard && feature.equals("*")) {
               throw new AdvancedTypographicTableFormatException("feature must not be wildcard");
            } else {
               this.script = script.trim();
               this.language = language.trim();
               this.feature = feature.trim();
            }
         } else {
            throw new AdvancedTypographicTableFormatException("language must be non-empty string");
         }
      }

      public String getScript() {
         return this.script;
      }

      public String getLanguage() {
         return this.language;
      }

      public String getFeature() {
         return this.feature;
      }

      public int hashCode() {
         int hc = 0;
         hc = 7 * hc + (hc ^ this.script.hashCode());
         hc = 11 * hc + (hc ^ this.language.hashCode());
         hc = 17 * hc + (hc ^ this.feature.hashCode());
         return hc;
      }

      public boolean equals(Object o) {
         if (o instanceof LookupSpec) {
            LookupSpec l = (LookupSpec)o;
            if (!l.script.equals(this.script)) {
               return false;
            } else {
               return !l.language.equals(this.language) ? false : l.feature.equals(this.feature);
            }
         } else {
            return false;
         }
      }

      public int compareTo(Object o) {
         int d;
         if (o instanceof LookupSpec) {
            LookupSpec ls = (LookupSpec)o;
            if ((d = this.script.compareTo(ls.script)) == 0 && (d = this.language.compareTo(ls.language)) == 0 && (d = this.feature.compareTo(ls.feature)) == 0) {
               d = 0;
            }
         } else {
            d = -1;
         }

         return d;
      }

      public String toString() {
         StringBuffer sb = new StringBuffer(super.toString());
         sb.append("{");
         sb.append("<'" + this.script + "'");
         sb.append(",'" + this.language + "'");
         sb.append(",'" + this.feature + "'");
         sb.append(">}");
         return sb.toString();
      }
   }
}
