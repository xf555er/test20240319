package org.apache.fop.complexscripts.fonts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.complexscripts.scripts.ScriptProcessor;
import org.apache.fop.complexscripts.util.CharAssociation;
import org.apache.fop.complexscripts.util.GlyphSequence;
import org.apache.fop.complexscripts.util.GlyphTester;
import org.apache.fop.fonts.MultiByteFont;

public class GlyphSubstitutionTable extends GlyphTable {
   private static final Log log = LogFactory.getLog(GlyphSubstitutionTable.class);
   public static final int GSUB_LOOKUP_TYPE_SINGLE = 1;
   public static final int GSUB_LOOKUP_TYPE_MULTIPLE = 2;
   public static final int GSUB_LOOKUP_TYPE_ALTERNATE = 3;
   public static final int GSUB_LOOKUP_TYPE_LIGATURE = 4;
   public static final int GSUB_LOOKUP_TYPE_CONTEXTUAL = 5;
   public static final int GSUB_LOOKUP_TYPE_CHAINED_CONTEXTUAL = 6;
   public static final int GSUB_LOOKUP_TYPE_EXTENSION_SUBSTITUTION = 7;
   public static final int GSUB_LOOKUP_TYPE_REVERSE_CHAINED_SINGLE = 8;

   public GlyphSubstitutionTable(GlyphDefinitionTable gdef, Map lookups, List subtables, Map processors) {
      super(gdef, lookups, processors);
      if (subtables != null && subtables.size() != 0) {
         Iterator var5 = subtables.iterator();

         while(var5.hasNext()) {
            Object o = var5.next();
            if (!(o instanceof GlyphSubstitutionSubtable)) {
               throw new AdvancedTypographicTableFormatException("subtable must be a glyph substitution subtable");
            }

            this.addSubtable((GlyphSubtable)o);
         }

         this.freezeSubtables();
      } else {
         throw new AdvancedTypographicTableFormatException("subtables must be non-empty");
      }
   }

   public GlyphSequence substitute(GlyphSequence gs, String script, String language) {
      Map lookups = this.matchLookups(script, language, "*");
      GlyphSequence ogs;
      if (lookups != null && lookups.size() > 0) {
         ScriptProcessor sp = ScriptProcessor.getInstance(script, this.processors);
         ogs = sp.substitute(this, gs, script, language, lookups);
      } else {
         ogs = gs;
      }

      return ogs;
   }

   public CharSequence preProcess(CharSequence charSequence, String script, MultiByteFont font, List associations) {
      ScriptProcessor scriptProcessor = ScriptProcessor.getInstance(script, this.processors);
      return scriptProcessor.preProcess(charSequence, font, associations);
   }

   public static int getLookupTypeFromName(String name) {
      String s = name.toLowerCase();
      byte t;
      if ("single".equals(s)) {
         t = 1;
      } else if ("multiple".equals(s)) {
         t = 2;
      } else if ("alternate".equals(s)) {
         t = 3;
      } else if ("ligature".equals(s)) {
         t = 4;
      } else if ("contextual".equals(s)) {
         t = 5;
      } else if ("chainedcontextual".equals(s)) {
         t = 6;
      } else if ("extensionsubstitution".equals(s)) {
         t = 7;
      } else if ("reversechainiingcontextualsingle".equals(s)) {
         t = 8;
      } else {
         t = -1;
      }

      return t;
   }

   public static String getLookupTypeName(int type) {
      String tn = null;
      switch (type) {
         case 1:
            tn = "single";
            break;
         case 2:
            tn = "multiple";
            break;
         case 3:
            tn = "alternate";
            break;
         case 4:
            tn = "ligature";
            break;
         case 5:
            tn = "contextual";
            break;
         case 6:
            tn = "chainedcontextual";
            break;
         case 7:
            tn = "extensionsubstitution";
            break;
         case 8:
            tn = "reversechainiingcontextualsingle";
            break;
         default:
            tn = "unknown";
      }

      return tn;
   }

   public static GlyphSubtable createSubtable(int type, String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List entries) {
      GlyphSubtable st = null;
      switch (type) {
         case 1:
            st = GlyphSubstitutionTable.SingleSubtable.create(id, sequence, flags, format, coverage, entries);
            break;
         case 2:
            st = GlyphSubstitutionTable.MultipleSubtable.create(id, sequence, flags, format, coverage, entries);
            break;
         case 3:
            st = GlyphSubstitutionTable.AlternateSubtable.create(id, sequence, flags, format, coverage, entries);
            break;
         case 4:
            st = GlyphSubstitutionTable.LigatureSubtable.create(id, sequence, flags, format, coverage, entries);
            break;
         case 5:
            st = GlyphSubstitutionTable.ContextualSubtable.create(id, sequence, flags, format, coverage, entries);
            break;
         case 6:
            st = GlyphSubstitutionTable.ChainedContextualSubtable.create(id, sequence, flags, format, coverage, entries);
         case 7:
         default:
            break;
         case 8:
            st = GlyphSubstitutionTable.ReverseChainedSingleSubtable.create(id, sequence, flags, format, coverage, entries);
      }

      return st;
   }

   public static GlyphSubtable createSubtable(int type, String id, int sequence, int flags, int format, List coverage, List entries) {
      return createSubtable(type, id, sequence, flags, format, GlyphCoverageTable.createCoverageTable(coverage), entries);
   }

   public static class LigatureSet {
      private final Ligature[] ligatures;
      private final int maxComponents;

      public LigatureSet(List ligatures) {
         this((Ligature[])((Ligature[])ligatures.toArray(new Ligature[ligatures.size()])));
      }

      public LigatureSet(Ligature[] ligatures) {
         if (ligatures == null) {
            throw new AdvancedTypographicTableFormatException("invalid ligatures, must be non-null array");
         } else {
            this.ligatures = ligatures;
            int ncMax = -1;
            Ligature[] var3 = ligatures;
            int var4 = ligatures.length;

            for(int var5 = 0; var5 < var4; ++var5) {
               Ligature l = var3[var5];
               int nc = l.getNumComponents() + 1;
               if (nc > ncMax) {
                  ncMax = nc;
               }
            }

            this.maxComponents = ncMax;
         }
      }

      public Ligature[] getLigatures() {
         return this.ligatures;
      }

      public int getNumLigatures() {
         return this.ligatures.length;
      }

      public int getMaxComponents() {
         return this.maxComponents;
      }

      public String toString() {
         StringBuffer sb = new StringBuffer();
         sb.append("{ligs={");
         int i = 0;

         for(int n = this.ligatures.length; i < n; ++i) {
            if (i > 0) {
               sb.append(',');
            }

            sb.append(this.ligatures[i]);
         }

         sb.append("}}");
         return sb.toString();
      }
   }

   public static class Ligature {
      private final int ligature;
      private final int[] components;

      public Ligature(int ligature, int[] components) {
         if (ligature >= 0 && ligature <= 65535) {
            if (components == null) {
               throw new AdvancedTypographicTableFormatException("invalid ligature components, must be non-null array");
            } else {
               int[] var3 = components;
               int var4 = components.length;

               for(int var5 = 0; var5 < var4; ++var5) {
                  int gc = var3[var5];
                  if (gc < 0 || gc > 65535) {
                     throw new AdvancedTypographicTableFormatException("invalid component glyph index: " + gc);
                  }
               }

               this.ligature = ligature;
               this.components = components;
            }
         } else {
            throw new AdvancedTypographicTableFormatException("invalid ligature glyph index: " + ligature);
         }
      }

      public int getLigature() {
         return this.ligature;
      }

      public int[] getComponents() {
         return this.components;
      }

      public int getNumComponents() {
         return this.components.length;
      }

      public boolean matchesComponents(int[] glyphs) {
         if (glyphs.length < this.components.length + 1) {
            return false;
         } else {
            int i = 0;

            for(int n = this.components.length; i < n; ++i) {
               if (glyphs[i + 1] != this.components[i]) {
                  return false;
               }
            }

            return true;
         }
      }

      public String toString() {
         StringBuffer sb = new StringBuffer();
         sb.append("{components={");
         int i = 0;

         for(int n = this.components.length; i < n; ++i) {
            if (i > 0) {
               sb.append(',');
            }

            sb.append(Integer.toString(this.components[i]));
         }

         sb.append("},ligature=");
         sb.append(Integer.toString(this.ligature));
         sb.append("}");
         return sb.toString();
      }
   }

   private static class ReverseChainedSingleSubtableFormat1 extends ReverseChainedSingleSubtable {
      ReverseChainedSingleSubtableFormat1(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List entries) {
         super(id, sequence, flags, format, coverage, entries);
         this.populate(entries);
      }

      public List getEntries() {
         return null;
      }

      private void populate(List entries) {
      }
   }

   private abstract static class ReverseChainedSingleSubtable extends GlyphSubstitutionSubtable {
      public ReverseChainedSingleSubtable(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List entries) {
         super(id, sequence, flags, format, coverage);
      }

      public int getType() {
         return 8;
      }

      public boolean isCompatible(GlyphSubtable subtable) {
         return subtable instanceof ReverseChainedSingleSubtable;
      }

      public boolean usesReverseScan() {
         return true;
      }

      static GlyphSubstitutionSubtable create(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List entries) {
         if (format == 1) {
            return new ReverseChainedSingleSubtableFormat1(id, sequence, flags, format, coverage, entries);
         } else {
            throw new UnsupportedOperationException();
         }
      }
   }

   private static class ChainedContextualSubtableFormat3 extends ChainedContextualSubtable {
      private GlyphTable.RuleSet[] rsa;

      ChainedContextualSubtableFormat3(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List entries) {
         super(id, sequence, flags, format, coverage, entries);
         this.populate(entries);
      }

      public List getEntries() {
         if (this.rsa != null) {
            List entries = new ArrayList(1);
            entries.add(this.rsa);
            return entries;
         } else {
            return null;
         }
      }

      public void resolveLookupReferences(Map lookupTables) {
         GlyphTable.resolveLookupReferences(this.rsa, lookupTables);
      }

      public GlyphTable.RuleLookup[] getLookups(int ci, int gi, GlyphSubstitutionState ss, int[] rv) {
         assert ss != null;

         assert rv != null && rv.length > 0;

         assert this.rsa != null;

         if (this.rsa.length > 0) {
            GlyphTable.RuleSet rs = this.rsa[0];
            if (rs != null) {
               GlyphTable.Rule[] ra = rs.getRules();
               GlyphTable.Rule[] var7 = ra;
               int var8 = ra.length;

               for(int var9 = 0; var9 < var8; ++var9) {
                  GlyphTable.Rule r = var7[var9];
                  if (r != null && r instanceof GlyphTable.ChainedCoverageSequenceRule) {
                     GlyphTable.ChainedCoverageSequenceRule cr = (GlyphTable.ChainedCoverageSequenceRule)r;
                     GlyphCoverageTable[] igca = cr.getCoverages();
                     if (this.matches(ss, igca, 0, rv)) {
                        GlyphCoverageTable[] bgca = cr.getBacktrackCoverages();
                        if (this.matches(ss, bgca, -1, (int[])null)) {
                           GlyphCoverageTable[] lgca = cr.getLookaheadCoverages();
                           if (this.matches(ss, lgca, rv[0], (int[])null)) {
                              return r.getLookups();
                           }
                        }
                     }
                  }
               }
            }
         }

         return null;
      }

      private boolean matches(GlyphSubstitutionState ss, GlyphCoverageTable[] gca, int offset, int[] rv) {
         return GlyphSubstitutionTable.ContextualSubtableFormat3.matches(ss, gca, offset, rv);
      }

      private void populate(List entries) {
         if (entries == null) {
            throw new AdvancedTypographicTableFormatException("illegal entries, must be non-null");
         } else if (entries.size() != 1) {
            throw new AdvancedTypographicTableFormatException("illegal entries, " + entries.size() + " entries present, but requires 1 entry");
         } else {
            Object o;
            if ((o = entries.get(0)) != null && o instanceof GlyphTable.RuleSet[]) {
               this.rsa = (GlyphTable.RuleSet[])((GlyphTable.RuleSet[])o);
            } else {
               throw new AdvancedTypographicTableFormatException("illegal entries, first entry must be an RuleSet[], but is: " + (o != null ? o.getClass() : null));
            }
         }
      }
   }

   private static class ChainedContextualSubtableFormat2 extends ChainedContextualSubtable {
      private GlyphClassTable icdt;
      private GlyphClassTable bcdt;
      private GlyphClassTable lcdt;
      private int ngc;
      private GlyphTable.RuleSet[] rsa;

      ChainedContextualSubtableFormat2(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List entries) {
         super(id, sequence, flags, format, coverage, entries);
         this.populate(entries);
      }

      public List getEntries() {
         if (this.rsa != null) {
            List entries = new ArrayList(5);
            entries.add(this.icdt);
            entries.add(this.bcdt);
            entries.add(this.lcdt);
            entries.add(this.ngc);
            entries.add(this.rsa);
            return entries;
         } else {
            return null;
         }
      }

      public GlyphTable.RuleLookup[] getLookups(int ci, int gi, GlyphSubstitutionState ss, int[] rv) {
         assert ss != null;

         assert rv != null && rv.length > 0;

         assert this.rsa != null;

         if (this.rsa.length > 0) {
            GlyphTable.RuleSet rs = this.rsa[0];
            if (rs != null) {
               GlyphTable.Rule[] ra = rs.getRules();
               GlyphTable.Rule[] var7 = ra;
               int var8 = ra.length;

               for(int var9 = 0; var9 < var8; ++var9) {
                  GlyphTable.Rule r = var7[var9];
                  if (r != null && r instanceof GlyphTable.ChainedClassSequenceRule) {
                     GlyphTable.ChainedClassSequenceRule cr = (GlyphTable.ChainedClassSequenceRule)r;
                     int[] ica = cr.getClasses(this.icdt.getClassIndex(gi, ss.getClassMatchSet(gi)));
                     if (this.matches(ss, this.icdt, ica, 0, rv)) {
                        int[] bca = cr.getBacktrackClasses();
                        if (this.matches(ss, this.bcdt, bca, -1, (int[])null)) {
                           int[] lca = cr.getLookaheadClasses();
                           if (this.matches(ss, this.lcdt, lca, rv[0], (int[])null)) {
                              return r.getLookups();
                           }
                        }
                     }
                  }
               }
            }
         }

         return null;
      }

      private boolean matches(GlyphSubstitutionState ss, GlyphClassTable cdt, int[] classes, int offset, int[] rv) {
         return GlyphSubstitutionTable.ContextualSubtableFormat2.matches(ss, cdt, classes, offset, rv);
      }

      public void resolveLookupReferences(Map lookupTables) {
         GlyphTable.resolveLookupReferences(this.rsa, lookupTables);
      }

      private void populate(List entries) {
         if (entries == null) {
            throw new AdvancedTypographicTableFormatException("illegal entries, must be non-null");
         } else if (entries.size() != 5) {
            throw new AdvancedTypographicTableFormatException("illegal entries, " + entries.size() + " entries present, but requires 5 entries");
         } else {
            Object o;
            if ((o = entries.get(0)) != null && o instanceof GlyphClassTable) {
               this.icdt = (GlyphClassTable)o;
               if ((o = entries.get(1)) != null && !(o instanceof GlyphClassTable)) {
                  throw new AdvancedTypographicTableFormatException("illegal entries, second entry must be an GlyphClassTable, but is: " + o.getClass());
               } else {
                  this.bcdt = (GlyphClassTable)o;
                  if ((o = entries.get(2)) != null && !(o instanceof GlyphClassTable)) {
                     throw new AdvancedTypographicTableFormatException("illegal entries, third entry must be an GlyphClassTable, but is: " + o.getClass());
                  } else {
                     this.lcdt = (GlyphClassTable)o;
                     if ((o = entries.get(3)) != null && o instanceof Integer) {
                        this.ngc = (Integer)((Integer)o);
                        if ((o = entries.get(4)) != null && o instanceof GlyphTable.RuleSet[]) {
                           this.rsa = (GlyphTable.RuleSet[])((GlyphTable.RuleSet[])o);
                           if (this.rsa.length != this.ngc) {
                              throw new AdvancedTypographicTableFormatException("illegal entries, RuleSet[] length is " + this.rsa.length + ", but expected " + this.ngc + " glyph classes");
                           }
                        } else {
                           throw new AdvancedTypographicTableFormatException("illegal entries, fifth entry must be an RuleSet[], but is: " + (o != null ? o.getClass() : null));
                        }
                     } else {
                        throw new AdvancedTypographicTableFormatException("illegal entries, fourth entry must be an Integer, but is: " + (o != null ? o.getClass() : null));
                     }
                  }
               }
            } else {
               throw new AdvancedTypographicTableFormatException("illegal entries, first entry must be an GlyphClassTable, but is: " + (o != null ? o.getClass() : null));
            }
         }
      }
   }

   private static class ChainedContextualSubtableFormat1 extends ChainedContextualSubtable {
      private GlyphTable.RuleSet[] rsa;

      ChainedContextualSubtableFormat1(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List entries) {
         super(id, sequence, flags, format, coverage, entries);
         this.populate(entries);
      }

      public List getEntries() {
         if (this.rsa != null) {
            List entries = new ArrayList(1);
            entries.add(this.rsa);
            return entries;
         } else {
            return null;
         }
      }

      public void resolveLookupReferences(Map lookupTables) {
         GlyphTable.resolveLookupReferences(this.rsa, lookupTables);
      }

      public GlyphTable.RuleLookup[] getLookups(int ci, int gi, GlyphSubstitutionState ss, int[] rv) {
         assert ss != null;

         assert rv != null && rv.length > 0;

         assert this.rsa != null;

         if (this.rsa.length > 0) {
            GlyphTable.RuleSet rs = this.rsa[0];
            if (rs != null) {
               GlyphTable.Rule[] ra = rs.getRules();
               GlyphTable.Rule[] var7 = ra;
               int var8 = ra.length;

               for(int var9 = 0; var9 < var8; ++var9) {
                  GlyphTable.Rule r = var7[var9];
                  if (r != null && r instanceof GlyphTable.ChainedGlyphSequenceRule) {
                     GlyphTable.ChainedGlyphSequenceRule cr = (GlyphTable.ChainedGlyphSequenceRule)r;
                     int[] iga = cr.getGlyphs(gi);
                     if (this.matches(ss, iga, 0, rv)) {
                        int[] bga = cr.getBacktrackGlyphs();
                        if (this.matches(ss, bga, -1, (int[])null)) {
                           int[] lga = cr.getLookaheadGlyphs();
                           if (this.matches(ss, lga, rv[0], (int[])null)) {
                              return r.getLookups();
                           }
                        }
                     }
                  }
               }
            }
         }

         return null;
      }

      private boolean matches(GlyphSubstitutionState ss, int[] glyphs, int offset, int[] rv) {
         return GlyphSubstitutionTable.ContextualSubtableFormat1.matches(ss, glyphs, offset, rv);
      }

      private void populate(List entries) {
         if (entries == null) {
            throw new AdvancedTypographicTableFormatException("illegal entries, must be non-null");
         } else if (entries.size() != 1) {
            throw new AdvancedTypographicTableFormatException("illegal entries, " + entries.size() + " entries present, but requires 1 entry");
         } else {
            Object o;
            if ((o = entries.get(0)) != null && o instanceof GlyphTable.RuleSet[]) {
               this.rsa = (GlyphTable.RuleSet[])((GlyphTable.RuleSet[])o);
            } else {
               throw new AdvancedTypographicTableFormatException("illegal entries, first entry must be an RuleSet[], but is: " + (o != null ? o.getClass() : null));
            }
         }
      }
   }

   private abstract static class ChainedContextualSubtable extends GlyphSubstitutionSubtable {
      public ChainedContextualSubtable(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List entries) {
         super(id, sequence, flags, format, coverage);
      }

      public int getType() {
         return 6;
      }

      public boolean isCompatible(GlyphSubtable subtable) {
         return subtable instanceof ChainedContextualSubtable;
      }

      public boolean substitute(GlyphSubstitutionState ss) {
         int gi = ss.getGlyph();
         int ci;
         if ((ci = this.getCoverageIndex(gi)) < 0) {
            return false;
         } else {
            int[] rv = new int[1];
            GlyphTable.RuleLookup[] la = this.getLookups(ci, gi, ss, rv);
            if (la != null) {
               ss.apply(la, rv[0]);
               return true;
            } else {
               return false;
            }
         }
      }

      public abstract GlyphTable.RuleLookup[] getLookups(int var1, int var2, GlyphSubstitutionState var3, int[] var4);

      static GlyphSubstitutionSubtable create(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List entries) {
         if (format == 1) {
            return new ChainedContextualSubtableFormat1(id, sequence, flags, format, coverage, entries);
         } else if (format == 2) {
            return new ChainedContextualSubtableFormat2(id, sequence, flags, format, coverage, entries);
         } else if (format == 3) {
            return new ChainedContextualSubtableFormat3(id, sequence, flags, format, coverage, entries);
         } else {
            throw new UnsupportedOperationException();
         }
      }
   }

   private static class ContextualSubtableFormat3 extends ContextualSubtable {
      private GlyphTable.RuleSet[] rsa;

      ContextualSubtableFormat3(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List entries) {
         super(id, sequence, flags, format, coverage, entries);
         this.populate(entries);
      }

      public List getEntries() {
         if (this.rsa != null) {
            List entries = new ArrayList(1);
            entries.add(this.rsa);
            return entries;
         } else {
            return null;
         }
      }

      public void resolveLookupReferences(Map lookupTables) {
         GlyphTable.resolveLookupReferences(this.rsa, lookupTables);
      }

      public GlyphTable.RuleLookup[] getLookups(int ci, int gi, GlyphSubstitutionState ss, int[] rv) {
         assert ss != null;

         assert rv != null && rv.length > 0;

         assert this.rsa != null;

         if (this.rsa.length > 0) {
            GlyphTable.RuleSet rs = this.rsa[0];
            if (rs != null) {
               GlyphTable.Rule[] ra = rs.getRules();
               GlyphTable.Rule[] var7 = ra;
               int var8 = ra.length;

               for(int var9 = 0; var9 < var8; ++var9) {
                  GlyphTable.Rule r = var7[var9];
                  if (r != null && r instanceof GlyphTable.ChainedCoverageSequenceRule) {
                     GlyphTable.ChainedCoverageSequenceRule cr = (GlyphTable.ChainedCoverageSequenceRule)r;
                     GlyphCoverageTable[] gca = cr.getCoverages();
                     if (matches(ss, gca, 0, rv)) {
                        return r.getLookups();
                     }
                  }
               }
            }
         }

         return null;
      }

      static boolean matches(GlyphSubstitutionState ss, GlyphCoverageTable[] gca, int offset, int[] rv) {
         if (gca != null && gca.length != 0) {
            boolean reverse = offset < 0;
            GlyphTester ignores = ss.getIgnoreDefault();
            int[] counts = ss.getGlyphsAvailable(offset, reverse, ignores);
            int nga = counts[0];
            int ngm = gca.length;
            if (nga < ngm) {
               return false;
            } else {
               int[] ga = ss.getGlyphs(offset, ngm, reverse, ignores, (int[])null, counts);

               for(int k = 0; k < ngm; ++k) {
                  GlyphCoverageTable ct = gca[k];
                  if (ct != null && ct.getCoverageIndex(ga[k]) < 0) {
                     return false;
                  }
               }

               if (rv != null) {
                  rv[0] = counts[0] + counts[1];
               }

               return true;
            }
         } else {
            return true;
         }
      }

      private void populate(List entries) {
         if (entries == null) {
            throw new AdvancedTypographicTableFormatException("illegal entries, must be non-null");
         } else if (entries.size() != 1) {
            throw new AdvancedTypographicTableFormatException("illegal entries, " + entries.size() + " entries present, but requires 1 entry");
         } else {
            Object o;
            if ((o = entries.get(0)) != null && o instanceof GlyphTable.RuleSet[]) {
               this.rsa = (GlyphTable.RuleSet[])((GlyphTable.RuleSet[])o);
            } else {
               throw new AdvancedTypographicTableFormatException("illegal entries, first entry must be an RuleSet[], but is: " + (o != null ? o.getClass() : null));
            }
         }
      }
   }

   private static class ContextualSubtableFormat2 extends ContextualSubtable {
      private GlyphClassTable cdt;
      private int ngc;
      private GlyphTable.RuleSet[] rsa;

      ContextualSubtableFormat2(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List entries) {
         super(id, sequence, flags, format, coverage, entries);
         this.populate(entries);
      }

      public List getEntries() {
         if (this.rsa != null) {
            List entries = new ArrayList(3);
            entries.add(this.cdt);
            entries.add(this.ngc);
            entries.add(this.rsa);
            return entries;
         } else {
            return null;
         }
      }

      public void resolveLookupReferences(Map lookupTables) {
         GlyphTable.resolveLookupReferences(this.rsa, lookupTables);
      }

      public GlyphTable.RuleLookup[] getLookups(int ci, int gi, GlyphSubstitutionState ss, int[] rv) {
         assert ss != null;

         assert rv != null && rv.length > 0;

         assert this.rsa != null;

         if (this.rsa.length > 0) {
            GlyphTable.RuleSet rs = this.rsa[0];
            if (rs != null) {
               GlyphTable.Rule[] ra = rs.getRules();
               GlyphTable.Rule[] var7 = ra;
               int var8 = ra.length;

               for(int var9 = 0; var9 < var8; ++var9) {
                  GlyphTable.Rule r = var7[var9];
                  if (r != null && r instanceof GlyphTable.ChainedClassSequenceRule) {
                     GlyphTable.ChainedClassSequenceRule cr = (GlyphTable.ChainedClassSequenceRule)r;
                     int[] ca = cr.getClasses(this.cdt.getClassIndex(gi, ss.getClassMatchSet(gi)));
                     if (matches(ss, this.cdt, ca, 0, rv)) {
                        return r.getLookups();
                     }
                  }
               }
            }
         }

         return null;
      }

      static boolean matches(GlyphSubstitutionState ss, GlyphClassTable cdt, int[] classes, int offset, int[] rv) {
         if (cdt != null && classes != null && classes.length != 0) {
            boolean reverse = offset < 0;
            GlyphTester ignores = ss.getIgnoreDefault();
            int[] counts = ss.getGlyphsAvailable(offset, reverse, ignores);
            int nga = counts[0];
            int ngm = classes.length;
            if (nga < ngm) {
               return false;
            } else {
               int[] ga = ss.getGlyphs(offset, ngm, reverse, ignores, (int[])null, counts);

               for(int k = 0; k < ngm; ++k) {
                  int gi = ga[k];
                  int ms = ss.getClassMatchSet(gi);
                  int gc = cdt.getClassIndex(gi, ms);
                  if (gc < 0 || gc >= cdt.getClassSize(ms)) {
                     return false;
                  }

                  if (gc != classes[k]) {
                     return false;
                  }
               }

               if (rv != null) {
                  rv[0] = counts[0] + counts[1];
               }

               return true;
            }
         } else {
            return true;
         }
      }

      private void populate(List entries) {
         if (entries == null) {
            throw new AdvancedTypographicTableFormatException("illegal entries, must be non-null");
         } else if (entries.size() != 3) {
            throw new AdvancedTypographicTableFormatException("illegal entries, " + entries.size() + " entries present, but requires 3 entries");
         } else {
            Object o;
            if ((o = entries.get(0)) != null && o instanceof GlyphClassTable) {
               this.cdt = (GlyphClassTable)o;
               if ((o = entries.get(1)) != null && o instanceof Integer) {
                  this.ngc = (Integer)((Integer)o);
                  if ((o = entries.get(2)) != null && o instanceof GlyphTable.RuleSet[]) {
                     this.rsa = (GlyphTable.RuleSet[])((GlyphTable.RuleSet[])o);
                     if (this.rsa.length != this.ngc) {
                        throw new AdvancedTypographicTableFormatException("illegal entries, RuleSet[] length is " + this.rsa.length + ", but expected " + this.ngc + " glyph classes");
                     }
                  } else {
                     throw new AdvancedTypographicTableFormatException("illegal entries, third entry must be an RuleSet[], but is: " + (o != null ? o.getClass() : null));
                  }
               } else {
                  throw new AdvancedTypographicTableFormatException("illegal entries, second entry must be an Integer, but is: " + (o != null ? o.getClass() : null));
               }
            } else {
               throw new AdvancedTypographicTableFormatException("illegal entries, first entry must be an GlyphClassTable, but is: " + (o != null ? o.getClass() : null));
            }
         }
      }
   }

   private static class ContextualSubtableFormat1 extends ContextualSubtable {
      private GlyphTable.RuleSet[] rsa;

      ContextualSubtableFormat1(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List entries) {
         super(id, sequence, flags, format, coverage, entries);
         this.populate(entries);
      }

      public List getEntries() {
         if (this.rsa != null) {
            List entries = new ArrayList(1);
            entries.add(this.rsa);
            return entries;
         } else {
            return null;
         }
      }

      public void resolveLookupReferences(Map lookupTables) {
         GlyphTable.resolveLookupReferences(this.rsa, lookupTables);
      }

      public GlyphTable.RuleLookup[] getLookups(int ci, int gi, GlyphSubstitutionState ss, int[] rv) {
         assert ss != null;

         assert rv != null && rv.length > 0;

         assert this.rsa != null;

         if (this.rsa.length > 0) {
            GlyphTable.RuleSet rs = this.rsa[0];
            if (rs != null) {
               GlyphTable.Rule[] ra = rs.getRules();
               GlyphTable.Rule[] var7 = ra;
               int var8 = ra.length;

               for(int var9 = 0; var9 < var8; ++var9) {
                  GlyphTable.Rule r = var7[var9];
                  if (r != null && r instanceof GlyphTable.ChainedGlyphSequenceRule) {
                     GlyphTable.ChainedGlyphSequenceRule cr = (GlyphTable.ChainedGlyphSequenceRule)r;
                     int[] iga = cr.getGlyphs(gi);
                     if (matches(ss, iga, 0, rv)) {
                        return r.getLookups();
                     }
                  }
               }
            }
         }

         return null;
      }

      static boolean matches(GlyphSubstitutionState ss, int[] glyphs, int offset, int[] rv) {
         if (glyphs != null && glyphs.length != 0) {
            boolean reverse = offset < 0;
            GlyphTester ignores = ss.getIgnoreDefault();
            int[] counts = ss.getGlyphsAvailable(offset, reverse, ignores);
            int nga = counts[0];
            int ngm = glyphs.length;
            if (nga < ngm) {
               return false;
            } else {
               int[] ga = ss.getGlyphs(offset, ngm, reverse, ignores, (int[])null, counts);

               for(int k = 0; k < ngm; ++k) {
                  if (ga[k] != glyphs[k]) {
                     return false;
                  }
               }

               if (rv != null) {
                  rv[0] = counts[0] + counts[1];
               }

               return true;
            }
         } else {
            return true;
         }
      }

      private void populate(List entries) {
         if (entries == null) {
            throw new AdvancedTypographicTableFormatException("illegal entries, must be non-null");
         } else if (entries.size() != 1) {
            throw new AdvancedTypographicTableFormatException("illegal entries, " + entries.size() + " entries present, but requires 1 entry");
         } else {
            Object o;
            if ((o = entries.get(0)) != null && o instanceof GlyphTable.RuleSet[]) {
               this.rsa = (GlyphTable.RuleSet[])((GlyphTable.RuleSet[])o);
            } else {
               throw new AdvancedTypographicTableFormatException("illegal entries, first entry must be an RuleSet[], but is: " + (o != null ? o.getClass() : null));
            }
         }
      }
   }

   private abstract static class ContextualSubtable extends GlyphSubstitutionSubtable {
      public ContextualSubtable(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List entries) {
         super(id, sequence, flags, format, coverage);
      }

      public int getType() {
         return 5;
      }

      public boolean isCompatible(GlyphSubtable subtable) {
         return subtable instanceof ContextualSubtable;
      }

      public boolean substitute(GlyphSubstitutionState ss) {
         int gi = ss.getGlyph();
         int ci;
         if ((ci = this.getCoverageIndex(gi)) < 0) {
            return false;
         } else {
            int[] rv = new int[1];
            GlyphTable.RuleLookup[] la = this.getLookups(ci, gi, ss, rv);
            if (la != null) {
               ss.apply(la, rv[0]);
            }

            return true;
         }
      }

      public abstract GlyphTable.RuleLookup[] getLookups(int var1, int var2, GlyphSubstitutionState var3, int[] var4);

      static GlyphSubstitutionSubtable create(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List entries) {
         if (format == 1) {
            return new ContextualSubtableFormat1(id, sequence, flags, format, coverage, entries);
         } else if (format == 2) {
            return new ContextualSubtableFormat2(id, sequence, flags, format, coverage, entries);
         } else if (format == 3) {
            return new ContextualSubtableFormat3(id, sequence, flags, format, coverage, entries);
         } else {
            throw new UnsupportedOperationException();
         }
      }
   }

   private static class LigatureSubtableFormat1 extends LigatureSubtable {
      private LigatureSet[] ligatureSets;

      public LigatureSubtableFormat1(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List entries) {
         super(id, sequence, flags, format, coverage, entries);
         this.populate(entries);
      }

      public List getEntries() {
         List entries = new ArrayList(this.ligatureSets.length);
         Collections.addAll(entries, this.ligatureSets);
         return entries;
      }

      public LigatureSet getLigatureSetForCoverageIndex(int ci, int gi) throws IllegalArgumentException {
         if (this.ligatureSets == null) {
            return null;
         } else if (ci >= this.ligatureSets.length) {
            throw new IllegalArgumentException("coverage index " + ci + " out of range, maximum coverage index is " + this.ligatureSets.length);
         } else {
            return this.ligatureSets[ci];
         }
      }

      private void populate(List entries) {
         int i = 0;
         int n = entries.size();
         LigatureSet[] ligatureSets = new LigatureSet[n];

         Object o;
         for(Iterator var5 = entries.iterator(); var5.hasNext(); ligatureSets[i++] = (LigatureSet)o) {
            o = var5.next();
            if (!(o instanceof LigatureSet)) {
               throw new AdvancedTypographicTableFormatException("illegal ligatures entry, must be LigatureSet: " + o);
            }
         }

         assert i == n;

         assert this.ligatureSets == null;

         this.ligatureSets = ligatureSets;
      }
   }

   private abstract static class LigatureSubtable extends GlyphSubstitutionSubtable {
      public LigatureSubtable(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List entries) {
         super(id, sequence, flags, format, coverage);
      }

      public int getType() {
         return 4;
      }

      public boolean isCompatible(GlyphSubtable subtable) {
         return subtable instanceof LigatureSubtable;
      }

      public boolean substitute(GlyphSubstitutionState ss) {
         int gi = ss.getGlyph();
         int ci;
         if ((ci = this.getCoverageIndex(gi)) < 0) {
            return false;
         } else {
            LigatureSet ls = this.getLigatureSetForCoverageIndex(ci, gi);
            if (ls != null) {
               boolean reverse = false;
               GlyphTester ignores = ss.getIgnoreDefault();
               int[] counts = ss.getGlyphsAvailable(0, reverse, ignores);
               int nga = counts[0];
               if (nga > 1) {
                  int[] iga = ss.getGlyphs(0, nga, reverse, ignores, (int[])null, counts);
                  Ligature l = this.findLigature(ls, iga);
                  if (l != null) {
                     int go = l.getLigature();
                     if (go < 0 || go > 65535) {
                        go = 65535;
                     }

                     int nmg = 1 + l.getNumComponents();
                     ss.getGlyphs(0, nmg, reverse, ignores, (int[])null, counts);
                     nga = counts[0];
                     int ngi = counts[1];
                     CharAssociation[] laa = ss.getAssociations(0, nga);
                     ss.putGlyph(go, CharAssociation.join(laa), Boolean.TRUE);
                     if (ngi > 0) {
                        ss.putGlyphs(ss.getIgnoredGlyphs(0, ngi), ss.getIgnoredAssociations(0, ngi), (Object)null);
                     }

                     ss.consume(nga + ngi);
                  }
               }
            }

            return true;
         }
      }

      private Ligature findLigature(LigatureSet ls, int[] glyphs) {
         Ligature[] la = ls.getLigatures();
         int k = -1;
         int maxComponents = -1;
         int i = 0;

         for(int n = la.length; i < n; ++i) {
            Ligature l = la[i];
            if (l.matchesComponents(glyphs)) {
               int nc = l.getNumComponents();
               if (nc > maxComponents) {
                  maxComponents = nc;
                  k = i;
               }
            }
         }

         if (k >= 0) {
            return la[k];
         } else {
            return null;
         }
      }

      public abstract LigatureSet getLigatureSetForCoverageIndex(int var1, int var2) throws IllegalArgumentException;

      static GlyphSubstitutionSubtable create(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List entries) {
         if (format == 1) {
            return new LigatureSubtableFormat1(id, sequence, flags, format, coverage, entries);
         } else {
            throw new UnsupportedOperationException();
         }
      }
   }

   private static class AlternateSubtableFormat1 extends AlternateSubtable {
      private int[][] gaa;

      AlternateSubtableFormat1(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List entries) {
         super(id, sequence, flags, format, coverage, entries);
         this.populate(entries);
      }

      public List getEntries() {
         List entries = new ArrayList(this.gaa.length);
         Collections.addAll(entries, this.gaa);
         return entries;
      }

      public int[] getAlternatesForCoverageIndex(int ci, int gi) throws IllegalArgumentException {
         if (this.gaa == null) {
            return null;
         } else if (ci >= this.gaa.length) {
            throw new IllegalArgumentException("coverage index " + ci + " out of range, maximum coverage index is " + this.gaa.length);
         } else {
            return this.gaa[ci];
         }
      }

      private void populate(List entries) {
         int i = 0;
         int n = entries.size();
         int[][] gaa = new int[n][];

         Object o;
         for(Iterator var5 = entries.iterator(); var5.hasNext(); gaa[i++] = (int[])((int[])o)) {
            o = var5.next();
            if (!(o instanceof int[])) {
               throw new AdvancedTypographicTableFormatException("illegal entries entry, must be int[]: " + o);
            }
         }

         assert i == n;

         assert this.gaa == null;

         this.gaa = gaa;
      }
   }

   private abstract static class AlternateSubtable extends GlyphSubstitutionSubtable {
      public AlternateSubtable(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List entries) {
         super(id, sequence, flags, format, coverage);
      }

      public int getType() {
         return 3;
      }

      public boolean isCompatible(GlyphSubtable subtable) {
         return subtable instanceof AlternateSubtable;
      }

      public boolean substitute(GlyphSubstitutionState ss) {
         int gi = ss.getGlyph();
         int ci;
         if ((ci = this.getCoverageIndex(gi)) < 0) {
            return false;
         } else {
            int[] ga = this.getAlternatesForCoverageIndex(ci, gi);
            int ai = ss.getAlternatesIndex(ci);
            int go;
            if (ai >= 0 && ai < ga.length) {
               go = ga[ai];
            } else {
               go = gi;
            }

            if (go < 0 || go > 65535) {
               go = 65535;
            }

            ss.putGlyph(go, ss.getAssociation(), Boolean.TRUE);
            ss.consume(1);
            return true;
         }
      }

      public abstract int[] getAlternatesForCoverageIndex(int var1, int var2) throws IllegalArgumentException;

      static GlyphSubstitutionSubtable create(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List entries) {
         if (format == 1) {
            return new AlternateSubtableFormat1(id, sequence, flags, format, coverage, entries);
         } else {
            throw new UnsupportedOperationException();
         }
      }
   }

   private static class MultipleSubtableFormat1 extends MultipleSubtable {
      private int[][] gsa;

      MultipleSubtableFormat1(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List entries) {
         super(id, sequence, flags, format, coverage, entries);
         this.populate(entries);
      }

      public List getEntries() {
         if (this.gsa != null) {
            List entries = new ArrayList(1);
            entries.add(this.gsa);
            return entries;
         } else {
            return null;
         }
      }

      public int[] getGlyphsForCoverageIndex(int ci, int gi) throws IllegalArgumentException {
         if (this.gsa == null) {
            return null;
         } else if (ci >= this.gsa.length) {
            throw new IllegalArgumentException("coverage index " + ci + " out of range, maximum coverage index is " + this.gsa.length);
         } else {
            return this.gsa[ci];
         }
      }

      private void populate(List entries) {
         if (entries == null) {
            throw new AdvancedTypographicTableFormatException("illegal entries, must be non-null");
         } else if (entries.size() != 1) {
            throw new AdvancedTypographicTableFormatException("illegal entries, " + entries.size() + " entries present, but requires 1 entry");
         } else {
            Object o;
            if ((o = entries.get(0)) != null && o instanceof int[][]) {
               this.gsa = (int[][])((int[][])o);
            } else {
               throw new AdvancedTypographicTableFormatException("illegal entries, first entry must be an int[][], but is: " + (o != null ? o.getClass() : null));
            }
         }
      }
   }

   private abstract static class MultipleSubtable extends GlyphSubstitutionSubtable {
      public MultipleSubtable(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List entries) {
         super(id, sequence, flags, format, coverage);
      }

      public int getType() {
         return 2;
      }

      public boolean isCompatible(GlyphSubtable subtable) {
         return subtable instanceof MultipleSubtable;
      }

      public boolean substitute(GlyphSubstitutionState ss) {
         int gi = ss.getGlyph();
         int ci;
         if ((ci = this.getCoverageIndex(gi)) < 0) {
            return false;
         } else {
            int[] ga = this.getGlyphsForCoverageIndex(ci, gi);
            if (ga != null) {
               ss.putGlyphs(ga, CharAssociation.replicate(ss.getAssociation(), ga.length), Boolean.TRUE);
               ss.consume(1);
            }

            return true;
         }
      }

      public abstract int[] getGlyphsForCoverageIndex(int var1, int var2) throws IllegalArgumentException;

      static GlyphSubstitutionSubtable create(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List entries) {
         if (format == 1) {
            return new MultipleSubtableFormat1(id, sequence, flags, format, coverage, entries);
         } else {
            throw new UnsupportedOperationException();
         }
      }
   }

   private static class SingleSubtableFormat2 extends SingleSubtable {
      private int[] glyphs;

      SingleSubtableFormat2(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List entries) {
         super(id, sequence, flags, format, coverage, entries);
         this.populate(entries);
      }

      public List getEntries() {
         List entries = new ArrayList(this.glyphs.length);
         int[] var2 = this.glyphs;
         int var3 = var2.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            int glyph = var2[var4];
            entries.add(glyph);
         }

         return entries;
      }

      public int getGlyphForCoverageIndex(int ci, int gi) throws IllegalArgumentException {
         if (this.glyphs == null) {
            return -1;
         } else if (ci >= this.glyphs.length) {
            throw new IllegalArgumentException("coverage index " + ci + " out of range, maximum coverage index is " + this.glyphs.length);
         } else {
            return this.glyphs[ci];
         }
      }

      private void populate(List entries) {
         int i = 0;
         int n = entries.size();
         int[] glyphs = new int[n];

         int gid;
         for(Iterator var5 = entries.iterator(); var5.hasNext(); glyphs[i++] = gid) {
            Object o = var5.next();
            if (!(o instanceof Integer)) {
               throw new AdvancedTypographicTableFormatException("illegal entries entry, must be Integer: " + o);
            }

            gid = (Integer)o;
            if (gid < 0 || gid >= 65536) {
               throw new AdvancedTypographicTableFormatException("illegal glyph index: " + gid);
            }
         }

         assert i == n;

         assert this.glyphs == null;

         this.glyphs = glyphs;
      }
   }

   private static class SingleSubtableFormat1 extends SingleSubtable {
      private int delta;
      private int ciMax;

      SingleSubtableFormat1(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List entries) {
         super(id, sequence, flags, format, coverage, entries);
         this.populate(entries);
      }

      public List getEntries() {
         List entries = new ArrayList(1);
         entries.add(this.delta);
         return entries;
      }

      public int getGlyphForCoverageIndex(int ci, int gi) throws IllegalArgumentException {
         if (ci <= this.ciMax) {
            return gi + this.delta;
         } else {
            throw new IllegalArgumentException("coverage index " + ci + " out of range, maximum coverage index is " + this.ciMax);
         }
      }

      private void populate(List entries) {
         if (entries != null && entries.size() == 1) {
            Object o = entries.get(0);
            int delta = false;
            if (o instanceof Integer) {
               int delta = (Integer)o;
               this.delta = delta;
               this.ciMax = this.getCoverageSize() - 1;
            } else {
               throw new AdvancedTypographicTableFormatException("illegal entries entry, must be Integer, but is: " + o);
            }
         } else {
            throw new AdvancedTypographicTableFormatException("illegal entries, must be non-null and contain exactly one entry");
         }
      }
   }

   private abstract static class SingleSubtable extends GlyphSubstitutionSubtable {
      SingleSubtable(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List entries) {
         super(id, sequence, flags, format, coverage);
      }

      public int getType() {
         return 1;
      }

      public boolean isCompatible(GlyphSubtable subtable) {
         return subtable instanceof SingleSubtable;
      }

      public boolean substitute(GlyphSubstitutionState ss) {
         int gi = ss.getGlyph();
         int ci;
         if ((ci = this.getCoverageIndex(gi)) < 0) {
            return false;
         } else {
            int go = this.getGlyphForCoverageIndex(ci, gi);
            if (go < 0 || go > 65535) {
               go = 65535;
            }

            ss.putGlyph(go, ss.getAssociation(), Boolean.TRUE);
            ss.consume(1);
            return true;
         }
      }

      public abstract int getGlyphForCoverageIndex(int var1, int var2) throws IllegalArgumentException;

      static GlyphSubstitutionSubtable create(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List entries) {
         if (format == 1) {
            return new SingleSubtableFormat1(id, sequence, flags, format, coverage, entries);
         } else if (format == 2) {
            return new SingleSubtableFormat2(id, sequence, flags, format, coverage, entries);
         } else {
            throw new UnsupportedOperationException();
         }
      }
   }
}
