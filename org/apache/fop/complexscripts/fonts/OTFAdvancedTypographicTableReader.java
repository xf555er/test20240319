package org.apache.fop.complexscripts.fonts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.fonts.truetype.FontFileReader;
import org.apache.fop.fonts.truetype.OFDirTabEntry;
import org.apache.fop.fonts.truetype.OFTableName;
import org.apache.fop.fonts.truetype.OpenFont;

public final class OTFAdvancedTypographicTableReader {
   private static Log log = LogFactory.getLog(OTFAdvancedTypographicTableReader.class);
   private OpenFont otf;
   private FontFileReader in;
   private GlyphDefinitionTable gdef;
   private GlyphSubstitutionTable gsub;
   private GlyphPositioningTable gpos;
   private transient Map seScripts;
   private transient Map seLanguages;
   private transient Map seFeatures;
   private transient GlyphMappingTable seMapping;
   private transient List seEntries;
   private transient List seSubtables;
   private Map processors = new HashMap();
   private static String defaultTag = "dflt";

   public OTFAdvancedTypographicTableReader(OpenFont otf, FontFileReader in) {
      assert otf != null;

      assert in != null;

      this.otf = otf;
      this.in = in;
   }

   public void readAll() throws AdvancedTypographicTableFormatException {
      try {
         this.readGDEF();
         this.readGSUB();
         this.readGPOS();
      } catch (AdvancedTypographicTableFormatException var6) {
         this.resetATStateAll();
         throw var6;
      } catch (IOException var7) {
         this.resetATStateAll();
         throw new AdvancedTypographicTableFormatException(var7.getMessage(), var7);
      } finally {
         this.resetATState();
      }

   }

   public boolean hasAdvancedTable() {
      return this.gdef != null || this.gsub != null || this.gpos != null;
   }

   public GlyphDefinitionTable getGDEF() {
      return this.gdef;
   }

   public GlyphSubstitutionTable getGSUB() {
      return this.gsub;
   }

   public GlyphPositioningTable getGPOS() {
      return this.gpos;
   }

   private void readLangSysTable(OFTableName tableTag, long langSysTable, String langSysTag) throws IOException {
      this.in.seekSet(langSysTable);
      if (log.isDebugEnabled()) {
         log.debug(tableTag + " lang sys table: " + langSysTag);
      }

      int lo = this.in.readTTFUShort();
      int rf = this.in.readTTFUShort();
      String rfi;
      if (rf != 65535) {
         rfi = "f" + rf;
      } else {
         rfi = null;
      }

      int nf = this.in.readTTFUShort();
      if (log.isDebugEnabled()) {
         log.debug(tableTag + " lang sys table reorder table: " + lo);
         log.debug(tableTag + " lang sys table required feature index: " + rf);
         log.debug(tableTag + " lang sys table non-required feature count: " + nf);
      }

      List fl = new ArrayList();

      for(int i = 0; i < nf; ++i) {
         int fi = this.in.readTTFUShort();
         if (log.isDebugEnabled()) {
            log.debug(tableTag + " lang sys table non-required feature index: " + fi);
         }

         fl.add("f" + fi);
      }

      if (this.seLanguages == null) {
         this.seLanguages = new LinkedHashMap();
      }

      this.seLanguages.put(langSysTag, new Object[]{rfi, fl});
   }

   private void readScriptTable(OFTableName tableTag, long scriptTable, String scriptTag) throws IOException {
      this.in.seekSet(scriptTable);
      if (log.isDebugEnabled()) {
         log.debug(tableTag + " script table: " + scriptTag);
      }

      int dl = this.in.readTTFUShort();
      String dt = defaultTag;
      if (dl > 0 && log.isDebugEnabled()) {
         log.debug(tableTag + " default lang sys tag: " + dt);
         log.debug(tableTag + " default lang sys table offset: " + dl);
      }

      int nl = this.in.readTTFUShort();
      List ll = new ArrayList();
      if (nl > 0) {
         String[] lta = new String[nl];
         int[] loa = new int[nl];
         int i = 0;

         int n;
         for(n = nl; i < n; ++i) {
            String lt = this.in.readTTFString(4);
            int lo = this.in.readTTFUShort();
            if (log.isDebugEnabled()) {
               log.debug(tableTag + " lang sys tag: " + lt);
               log.debug(tableTag + " lang sys table offset: " + lo);
            }

            lta[i] = lt;
            loa[i] = lo;
            if (dl == lo) {
               dl = 0;
               dt = lt;
            }

            ll.add(lt);
         }

         i = 0;

         for(n = nl; i < n; ++i) {
            this.readLangSysTable(tableTag, scriptTable + (long)loa[i], lta[i]);
         }
      }

      if (dl > 0) {
         this.readLangSysTable(tableTag, scriptTable + (long)dl, dt);
      } else if (dt != null && log.isDebugEnabled()) {
         log.debug(tableTag + " lang sys default: " + dt);
      }

      if (this.seLanguages != null) {
         this.seScripts.put(scriptTag, new Object[]{dt, ll, this.seLanguages});
      }

      this.seLanguages = null;
   }

   private void readScriptList(OFTableName tableTag, long scriptList) throws IOException {
      this.in.seekSet(scriptList);
      int ns = this.in.readTTFUShort();
      if (log.isDebugEnabled()) {
         log.debug(tableTag + " script list record count: " + ns);
      }

      if (ns > 0) {
         String[] sta = new String[ns];
         int[] soa = new int[ns];
         int i = 0;

         int n;
         for(n = ns; i < n; ++i) {
            String st = this.in.readTTFString(4);
            int so = this.in.readTTFUShort();
            if (log.isDebugEnabled()) {
               log.debug(tableTag + " script tag: " + st);
               log.debug(tableTag + " script table offset: " + so);
            }

            sta[i] = st;
            soa[i] = so;
         }

         i = 0;

         for(n = ns; i < n; ++i) {
            this.seLanguages = null;
            this.readScriptTable(tableTag, scriptList + (long)soa[i], sta[i]);
         }
      }

   }

   private void readFeatureTable(OFTableName tableTag, long featureTable, String featureTag, int featureIndex) throws IOException {
      this.in.seekSet(featureTable);
      if (log.isDebugEnabled()) {
         log.debug(tableTag + " feature table: " + featureTag);
      }

      int po = this.in.readTTFUShort();
      int nl = this.in.readTTFUShort();
      if (log.isDebugEnabled()) {
         log.debug(tableTag + " feature table parameters offset: " + po);
         log.debug(tableTag + " feature table lookup list index count: " + nl);
      }

      List lul = new ArrayList();

      for(int i = 0; i < nl; ++i) {
         int li = this.in.readTTFUShort();
         if (log.isDebugEnabled()) {
            log.debug(tableTag + " feature table lookup index: " + li);
         }

         lul.add("lu" + li);
      }

      this.seFeatures.put("f" + featureIndex, new Object[]{featureTag, lul});
   }

   private void readFeatureList(OFTableName tableTag, long featureList) throws IOException {
      this.in.seekSet(featureList);
      int nf = this.in.readTTFUShort();
      if (log.isDebugEnabled()) {
         log.debug(tableTag + " feature list record count: " + nf);
      }

      if (nf > 0) {
         String[] fta = new String[nf];
         int[] foa = new int[nf];
         int i = 0;

         int n;
         for(n = nf; i < n; ++i) {
            String ft = this.in.readTTFString(4);
            int fo = this.in.readTTFUShort();
            if (log.isDebugEnabled()) {
               log.debug(tableTag + " feature tag: " + ft);
               log.debug(tableTag + " feature table offset: " + fo);
            }

            fta[i] = ft;
            foa[i] = fo;
         }

         i = 0;

         for(n = nf; i < n; ++i) {
            if (log.isDebugEnabled()) {
               log.debug(tableTag + " feature index: " + i);
            }

            this.readFeatureTable(tableTag, featureList + (long)foa[i], fta[i], i);
         }
      }

   }

   private GlyphCoverageTable readCoverageTableFormat1(String label, long tableOffset, int coverageFormat) throws IOException {
      List entries = new ArrayList();
      this.in.seekSet(tableOffset);
      this.in.skip(2L);
      int ng = this.in.readTTFUShort();
      int[] ga = new int[ng];
      int i = 0;

      for(int n = ng; i < n; ++i) {
         int g = this.in.readTTFUShort();
         ga[i] = g;
         entries.add(g);
      }

      if (log.isDebugEnabled()) {
         log.debug(label + " glyphs: " + this.toString(ga));
      }

      return GlyphCoverageTable.createCoverageTable(entries);
   }

   private GlyphCoverageTable readCoverageTableFormat2(String label, long tableOffset, int coverageFormat) throws IOException {
      List entries = new ArrayList();
      this.in.seekSet(tableOffset);
      this.in.skip(2L);
      int nr = this.in.readTTFUShort();
      int i = 0;

      for(int n = nr; i < n; ++i) {
         int s = this.in.readTTFUShort();
         int e = this.in.readTTFUShort();
         int m = this.in.readTTFUShort();
         if (log.isDebugEnabled()) {
            log.debug(label + " range[" + i + "]: [" + s + "," + e + "]: " + m);
         }

         entries.add(new GlyphMappingTable.MappingRange(s, e, m));
      }

      return GlyphCoverageTable.createCoverageTable(entries);
   }

   private GlyphCoverageTable readCoverageTable(String label, long tableOffset) throws IOException {
      long cp = (long)this.in.getCurrentPos();
      this.in.seekSet(tableOffset);
      int cf = this.in.readTTFUShort();
      GlyphCoverageTable gct;
      if (cf == 1) {
         gct = this.readCoverageTableFormat1(label, tableOffset, cf);
      } else {
         if (cf != 2) {
            throw new AdvancedTypographicTableFormatException("unsupported coverage table format: " + cf);
         }

         gct = this.readCoverageTableFormat2(label, tableOffset, cf);
      }

      this.in.seekSet(cp);
      return gct;
   }

   private GlyphClassTable readClassDefTableFormat1(String label, long tableOffset, int classFormat) throws IOException {
      List entries = new ArrayList();
      this.in.seekSet(tableOffset);
      this.in.skip(2L);
      int sg = this.in.readTTFUShort();
      entries.add(sg);
      int ng = this.in.readTTFUShort();
      int[] ca = new int[ng];
      int i = 0;

      for(int n = ng; i < n; ++i) {
         int gc = this.in.readTTFUShort();
         ca[i] = gc;
         entries.add(gc);
      }

      if (log.isDebugEnabled()) {
         log.debug(label + " glyph classes: " + this.toString(ca));
      }

      return GlyphClassTable.createClassTable(entries);
   }

   private GlyphClassTable readClassDefTableFormat2(String label, long tableOffset, int classFormat) throws IOException {
      List entries = new ArrayList();
      this.in.seekSet(tableOffset);
      this.in.skip(2L);
      int nr = this.in.readTTFUShort();
      int i = 0;

      for(int n = nr; i < n; ++i) {
         int s = this.in.readTTFUShort();
         int e = this.in.readTTFUShort();
         int m = this.in.readTTFUShort();
         if (log.isDebugEnabled()) {
            log.debug(label + " range[" + i + "]: [" + s + "," + e + "]: " + m);
         }

         entries.add(new GlyphMappingTable.MappingRange(s, e, m));
      }

      return GlyphClassTable.createClassTable(entries);
   }

   private GlyphClassTable readClassDefTable(String label, long tableOffset) throws IOException {
      long cp = (long)this.in.getCurrentPos();
      this.in.seekSet(tableOffset);
      int cf = this.in.readTTFUShort();
      GlyphClassTable gct;
      if (cf == 1) {
         gct = this.readClassDefTableFormat1(label, tableOffset, cf);
      } else {
         if (cf != 2) {
            throw new AdvancedTypographicTableFormatException("unsupported class definition table format: " + cf);
         }

         gct = this.readClassDefTableFormat2(label, tableOffset, cf);
      }

      this.in.seekSet(cp);
      return gct;
   }

   private void readSingleSubTableFormat1(int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
      String tableTag = "GSUB";
      this.in.seekSet(subtableOffset);
      this.in.skip(2L);
      int co = this.in.readTTFUShort();
      int dg = this.in.readTTFShort();
      if (log.isDebugEnabled()) {
         log.debug(tableTag + " single substitution subtable format: " + subtableFormat + " (delta)");
         log.debug(tableTag + " single substitution coverage table offset: " + co);
         log.debug(tableTag + " single substitution delta: " + dg);
      }

      this.seMapping = this.readCoverageTable(tableTag + " single substitution coverage", subtableOffset + (long)co);
      this.seEntries.add(Integer.valueOf(dg));
   }

   private void readSingleSubTableFormat2(int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
      String tableTag = "GSUB";
      this.in.seekSet(subtableOffset);
      this.in.skip(2L);
      int co = this.in.readTTFUShort();
      int ng = this.in.readTTFUShort();
      if (log.isDebugEnabled()) {
         log.debug(tableTag + " single substitution subtable format: " + subtableFormat + " (mapped)");
         log.debug(tableTag + " single substitution coverage table offset: " + co);
         log.debug(tableTag + " single substitution glyph count: " + ng);
      }

      this.seMapping = this.readCoverageTable(tableTag + " single substitution coverage", subtableOffset + (long)co);
      int i = 0;

      for(int n = ng; i < n; ++i) {
         int gs = this.in.readTTFUShort();
         if (log.isDebugEnabled()) {
            log.debug(tableTag + " single substitution glyph[" + i + "]: " + gs);
         }

         this.seEntries.add(gs);
      }

   }

   private int readSingleSubTable(int lookupType, int lookupFlags, long subtableOffset) throws IOException {
      this.in.seekSet(subtableOffset);
      int sf = this.in.readTTFUShort();
      if (sf == 1) {
         this.readSingleSubTableFormat1(lookupType, lookupFlags, subtableOffset, sf);
      } else {
         if (sf != 2) {
            throw new AdvancedTypographicTableFormatException("unsupported single substitution subtable format: " + sf);
         }

         this.readSingleSubTableFormat2(lookupType, lookupFlags, subtableOffset, sf);
      }

      return sf;
   }

   private void readMultipleSubTableFormat1(int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
      String tableTag = "GSUB";
      this.in.seekSet(subtableOffset);
      this.in.skip(2L);
      int co = this.in.readTTFUShort();
      int ns = this.in.readTTFUShort();
      if (log.isDebugEnabled()) {
         log.debug(tableTag + " multiple substitution subtable format: " + subtableFormat + " (mapped)");
         log.debug(tableTag + " multiple substitution coverage table offset: " + co);
         log.debug(tableTag + " multiple substitution sequence count: " + ns);
      }

      this.seMapping = this.readCoverageTable(tableTag + " multiple substitution coverage", subtableOffset + (long)co);
      int[] soa = new int[ns];
      int i = 0;

      int i;
      for(i = ns; i < i; ++i) {
         soa[i] = this.in.readTTFUShort();
      }

      int[][] gsa = new int[ns][];
      i = 0;

      for(int n = ns; i < n; ++i) {
         int so = soa[i];
         int[] ga;
         if (so > 0) {
            this.in.seekSet(subtableOffset + (long)so);
            int ng = this.in.readTTFUShort();
            ga = new int[ng];

            for(int j = 0; j < ng; ++j) {
               ga[j] = this.in.readTTFUShort();
            }
         } else {
            ga = null;
         }

         if (log.isDebugEnabled()) {
            log.debug(tableTag + " multiple substitution sequence[" + i + "]: " + this.toString(ga));
         }

         gsa[i] = ga;
      }

      this.seEntries.add(gsa);
   }

   private int readMultipleSubTable(int lookupType, int lookupFlags, long subtableOffset) throws IOException {
      this.in.seekSet(subtableOffset);
      int sf = this.in.readTTFUShort();
      if (sf == 1) {
         this.readMultipleSubTableFormat1(lookupType, lookupFlags, subtableOffset, sf);
         return sf;
      } else {
         throw new AdvancedTypographicTableFormatException("unsupported multiple substitution subtable format: " + sf);
      }
   }

   private void readAlternateSubTableFormat1(int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
      String tableTag = "GSUB";
      this.in.seekSet(subtableOffset);
      this.in.skip(2L);
      int co = this.in.readTTFUShort();
      int ns = this.in.readTTFUShort();
      if (log.isDebugEnabled()) {
         log.debug(tableTag + " alternate substitution subtable format: " + subtableFormat + " (mapped)");
         log.debug(tableTag + " alternate substitution coverage table offset: " + co);
         log.debug(tableTag + " alternate substitution alternate set count: " + ns);
      }

      this.seMapping = this.readCoverageTable(tableTag + " alternate substitution coverage", subtableOffset + (long)co);
      int[] soa = new int[ns];
      int i = 0;

      int n;
      for(n = ns; i < n; ++i) {
         soa[i] = this.in.readTTFUShort();
      }

      i = 0;

      for(n = ns; i < n; ++i) {
         int so = soa[i];
         this.in.seekSet(subtableOffset + (long)so);
         int ng = this.in.readTTFUShort();
         int[] ga = new int[ng];

         for(int j = 0; j < ng; ++j) {
            int gs = this.in.readTTFUShort();
            ga[j] = gs;
         }

         if (log.isDebugEnabled()) {
            log.debug(tableTag + " alternate substitution alternate set[" + i + "]: " + this.toString(ga));
         }

         this.seEntries.add(ga);
      }

   }

   private int readAlternateSubTable(int lookupType, int lookupFlags, long subtableOffset) throws IOException {
      this.in.seekSet(subtableOffset);
      int sf = this.in.readTTFUShort();
      if (sf == 1) {
         this.readAlternateSubTableFormat1(lookupType, lookupFlags, subtableOffset, sf);
         return sf;
      } else {
         throw new AdvancedTypographicTableFormatException("unsupported alternate substitution subtable format: " + sf);
      }
   }

   private void readLigatureSubTableFormat1(int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
      String tableTag = "GSUB";
      this.in.seekSet(subtableOffset);
      this.in.skip(2L);
      int co = this.in.readTTFUShort();
      int ns = this.in.readTTFUShort();
      if (log.isDebugEnabled()) {
         log.debug(tableTag + " ligature substitution subtable format: " + subtableFormat + " (mapped)");
         log.debug(tableTag + " ligature substitution coverage table offset: " + co);
         log.debug(tableTag + " ligature substitution ligature set count: " + ns);
      }

      this.seMapping = this.readCoverageTable(tableTag + " ligature substitution coverage", subtableOffset + (long)co);
      int[] soa = new int[ns];
      int i = 0;

      int n;
      for(n = ns; i < n; ++i) {
         soa[i] = this.in.readTTFUShort();
      }

      i = 0;

      for(n = ns; i < n; ++i) {
         int so = soa[i];
         this.in.seekSet(subtableOffset + (long)so);
         int nl = this.in.readTTFUShort();
         int[] loa = new int[nl];

         for(int j = 0; j < nl; ++j) {
            loa[j] = this.in.readTTFUShort();
         }

         List ligs = new ArrayList();

         for(int j = 0; j < nl; ++j) {
            int lo = loa[j];
            this.in.seekSet(subtableOffset + (long)so + (long)lo);
            int lg = this.in.readTTFUShort();
            int nc = this.in.readTTFUShort();
            int[] ca = new int[nc - 1];

            for(int k = 0; k < nc - 1; ++k) {
               ca[k] = this.in.readTTFUShort();
            }

            if (log.isDebugEnabled()) {
               log.debug(tableTag + " ligature substitution ligature set[" + i + "]: ligature(" + lg + "), components: " + this.toString(ca));
            }

            ligs.add(new GlyphSubstitutionTable.Ligature(lg, ca));
         }

         this.seEntries.add(new GlyphSubstitutionTable.LigatureSet(ligs));
      }

   }

   private int readLigatureSubTable(int lookupType, int lookupFlags, long subtableOffset) throws IOException {
      this.in.seekSet(subtableOffset);
      int sf = this.in.readTTFUShort();
      if (sf == 1) {
         this.readLigatureSubTableFormat1(lookupType, lookupFlags, subtableOffset, sf);
         return sf;
      } else {
         throw new AdvancedTypographicTableFormatException("unsupported ligature substitution subtable format: " + sf);
      }
   }

   private GlyphTable.RuleLookup[] readRuleLookups(int numLookups, String header) throws IOException {
      GlyphTable.RuleLookup[] la = new GlyphTable.RuleLookup[numLookups];
      int i = 0;

      for(int n = numLookups; i < n; ++i) {
         int sequenceIndex = this.in.readTTFUShort();
         int lookupIndex = this.in.readTTFUShort();
         la[i] = new GlyphTable.RuleLookup(sequenceIndex, lookupIndex);
         if (log.isDebugEnabled() && header != null) {
            log.debug(header + "lookup[" + i + "]: " + la[i]);
         }
      }

      return la;
   }

   private void readContextualSubTableFormat1(int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
      String tableTag = "GSUB";
      this.in.seekSet(subtableOffset);
      this.in.skip(2L);
      int co = this.in.readTTFUShort();
      int nrs = this.in.readTTFUShort();
      int[] rsoa = new int[nrs];

      int i;
      for(i = 0; i < nrs; ++i) {
         rsoa[i] = this.in.readTTFUShort();
      }

      if (log.isDebugEnabled()) {
         log.debug(tableTag + " contextual substitution format: " + subtableFormat + " (glyphs)");
         log.debug(tableTag + " contextual substitution coverage table offset: " + co);
         log.debug(tableTag + " contextual substitution rule set count: " + nrs);

         for(i = 0; i < nrs; ++i) {
            log.debug(tableTag + " contextual substitution rule set offset[" + i + "]: " + rsoa[i]);
         }
      }

      GlyphCoverageTable ct;
      if (co > 0) {
         ct = this.readCoverageTable(tableTag + " contextual substitution coverage", subtableOffset + (long)co);
      } else {
         ct = null;
      }

      GlyphTable.RuleSet[] rsa = new GlyphTable.RuleSet[nrs];
      String header = null;

      for(int i = 0; i < nrs; ++i) {
         int rso = rsoa[i];
         GlyphTable.HomogeneousRuleSet rs;
         if (rso <= 0) {
            rs = null;
         } else {
            this.in.seekSet(subtableOffset + (long)rso);
            int nr = this.in.readTTFUShort();
            int[] roa = new int[nr];
            GlyphTable.Rule[] ra = new GlyphTable.Rule[nr];

            int j;
            for(j = 0; j < nr; ++j) {
               roa[j] = this.in.readTTFUShort();
            }

            j = 0;

            while(true) {
               if (j >= nr) {
                  rs = new GlyphTable.HomogeneousRuleSet(ra);
                  break;
               }

               int ro = roa[j];
               GlyphTable.GlyphSequenceRule r;
               if (ro <= 0) {
                  r = null;
               } else {
                  this.in.seekSet(subtableOffset + (long)rso + (long)ro);
                  int ng = this.in.readTTFUShort();
                  int nl = this.in.readTTFUShort();
                  int[] glyphs = new int[ng - 1];
                  int k = 0;

                  for(int nk = glyphs.length; k < nk; ++k) {
                     glyphs[k] = this.in.readTTFUShort();
                  }

                  if (log.isDebugEnabled()) {
                     header = tableTag + " contextual substitution lookups @rule[" + i + "][" + j + "]: ";
                  }

                  GlyphTable.RuleLookup[] lookups = this.readRuleLookups(nl, header);
                  r = new GlyphTable.GlyphSequenceRule(lookups, ng, glyphs);
               }

               ra[j] = r;
               ++j;
            }
         }

         rsa[i] = rs;
      }

      this.seMapping = ct;
      this.seEntries.add(rsa);
   }

   private void readContextualSubTableFormat2(int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
      String tableTag = "GSUB";
      this.in.seekSet(subtableOffset);
      this.in.skip(2L);
      int co = this.in.readTTFUShort();
      int cdo = this.in.readTTFUShort();
      int ngc = this.in.readTTFUShort();
      int[] csoa = new int[ngc];

      int i;
      for(i = 0; i < ngc; ++i) {
         csoa[i] = this.in.readTTFUShort();
      }

      if (log.isDebugEnabled()) {
         log.debug(tableTag + " contextual substitution format: " + subtableFormat + " (glyph classes)");
         log.debug(tableTag + " contextual substitution coverage table offset: " + co);
         log.debug(tableTag + " contextual substitution class set count: " + ngc);

         for(i = 0; i < ngc; ++i) {
            log.debug(tableTag + " contextual substitution class set offset[" + i + "]: " + csoa[i]);
         }
      }

      GlyphCoverageTable ct;
      if (co > 0) {
         ct = this.readCoverageTable(tableTag + " contextual substitution coverage", subtableOffset + (long)co);
      } else {
         ct = null;
      }

      GlyphClassTable cdt;
      if (cdo > 0) {
         cdt = this.readClassDefTable(tableTag + " contextual substitution class definition", subtableOffset + (long)cdo);
      } else {
         cdt = null;
      }

      GlyphTable.RuleSet[] rsa = new GlyphTable.RuleSet[ngc];
      String header = null;

      for(int i = 0; i < ngc; ++i) {
         int cso = csoa[i];
         GlyphTable.HomogeneousRuleSet rs;
         if (cso <= 0) {
            rs = null;
         } else {
            this.in.seekSet(subtableOffset + (long)cso);
            int nr = this.in.readTTFUShort();
            int[] roa = new int[nr];
            GlyphTable.Rule[] ra = new GlyphTable.Rule[nr];

            int j;
            for(j = 0; j < nr; ++j) {
               roa[j] = this.in.readTTFUShort();
            }

            j = 0;

            while(true) {
               if (j >= nr) {
                  rs = new GlyphTable.HomogeneousRuleSet(ra);
                  break;
               }

               int ro = roa[j];
               GlyphTable.ClassSequenceRule r;
               if (ro <= 0) {
                  assert ro > 0 : "unexpected null subclass rule offset";

                  r = null;
               } else {
                  this.in.seekSet(subtableOffset + (long)cso + (long)ro);
                  int ng = this.in.readTTFUShort();
                  int nl = this.in.readTTFUShort();
                  int[] classes = new int[ng - 1];
                  int k = 0;

                  for(int nk = classes.length; k < nk; ++k) {
                     classes[k] = this.in.readTTFUShort();
                  }

                  if (log.isDebugEnabled()) {
                     header = tableTag + " contextual substitution lookups @rule[" + i + "][" + j + "]: ";
                  }

                  GlyphTable.RuleLookup[] lookups = this.readRuleLookups(nl, header);
                  r = new GlyphTable.ClassSequenceRule(lookups, ng, classes);
               }

               ra[j] = r;
               ++j;
            }
         }

         rsa[i] = rs;
      }

      this.seMapping = ct;
      this.seEntries.add(cdt);
      this.seEntries.add(ngc);
      this.seEntries.add(rsa);
   }

   private void readContextualSubTableFormat3(int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
      String tableTag = "GSUB";
      this.in.seekSet(subtableOffset);
      this.in.skip(2L);
      int ng = this.in.readTTFUShort();
      int nl = this.in.readTTFUShort();
      int[] gcoa = new int[ng];

      int i;
      for(i = 0; i < ng; ++i) {
         gcoa[i] = this.in.readTTFUShort();
      }

      if (log.isDebugEnabled()) {
         log.debug(tableTag + " contextual substitution format: " + subtableFormat + " (glyph sets)");
         log.debug(tableTag + " contextual substitution glyph input sequence length count: " + ng);
         log.debug(tableTag + " contextual substitution lookup count: " + nl);

         for(i = 0; i < ng; ++i) {
            log.debug(tableTag + " contextual substitution coverage table offset[" + i + "]: " + gcoa[i]);
         }
      }

      GlyphCoverageTable[] gca = new GlyphCoverageTable[ng];

      for(int i = 0; i < ng; ++i) {
         int gco = gcoa[i];
         GlyphCoverageTable gct;
         if (gco > 0) {
            gct = this.readCoverageTable(tableTag + " contextual substitution coverage[" + i + "]", subtableOffset + (long)gco);
         } else {
            gct = null;
         }

         gca[i] = gct;
      }

      String header = null;
      if (log.isDebugEnabled()) {
         header = tableTag + " contextual substitution lookups: ";
      }

      GlyphTable.RuleLookup[] lookups = this.readRuleLookups(nl, header);
      GlyphTable.Rule r = new GlyphTable.CoverageSequenceRule(lookups, ng, gca);
      GlyphTable.RuleSet rs = new GlyphTable.HomogeneousRuleSet(new GlyphTable.Rule[]{r});
      GlyphTable.RuleSet[] rsa = new GlyphTable.RuleSet[]{rs};

      assert gca != null && gca.length > 0;

      this.seMapping = gca[0];
      this.seEntries.add(rsa);
   }

   private int readContextualSubTable(int lookupType, int lookupFlags, long subtableOffset) throws IOException {
      this.in.seekSet(subtableOffset);
      int sf = this.in.readTTFUShort();
      if (sf == 1) {
         this.readContextualSubTableFormat1(lookupType, lookupFlags, subtableOffset, sf);
      } else if (sf == 2) {
         this.readContextualSubTableFormat2(lookupType, lookupFlags, subtableOffset, sf);
      } else {
         if (sf != 3) {
            throw new AdvancedTypographicTableFormatException("unsupported contextual substitution subtable format: " + sf);
         }

         this.readContextualSubTableFormat3(lookupType, lookupFlags, subtableOffset, sf);
      }

      return sf;
   }

   private void readChainedContextualSubTableFormat1(int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
      String tableTag = "GSUB";
      this.in.seekSet(subtableOffset);
      this.in.skip(2L);
      int co = this.in.readTTFUShort();
      int nrs = this.in.readTTFUShort();
      int[] rsoa = new int[nrs];

      int i;
      for(i = 0; i < nrs; ++i) {
         rsoa[i] = this.in.readTTFUShort();
      }

      if (log.isDebugEnabled()) {
         log.debug(tableTag + " chained contextual substitution format: " + subtableFormat + " (glyphs)");
         log.debug(tableTag + " chained contextual substitution coverage table offset: " + co);
         log.debug(tableTag + " chained contextual substitution rule set count: " + nrs);

         for(i = 0; i < nrs; ++i) {
            log.debug(tableTag + " chained contextual substitution rule set offset[" + i + "]: " + rsoa[i]);
         }
      }

      GlyphCoverageTable ct;
      if (co > 0) {
         ct = this.readCoverageTable(tableTag + " chained contextual substitution coverage", subtableOffset + (long)co);
      } else {
         ct = null;
      }

      GlyphTable.RuleSet[] rsa = new GlyphTable.RuleSet[nrs];
      String header = null;

      for(int i = 0; i < nrs; ++i) {
         int rso = rsoa[i];
         GlyphTable.HomogeneousRuleSet rs;
         if (rso <= 0) {
            rs = null;
         } else {
            this.in.seekSet(subtableOffset + (long)rso);
            int nr = this.in.readTTFUShort();
            int[] roa = new int[nr];
            GlyphTable.Rule[] ra = new GlyphTable.Rule[nr];

            int j;
            for(j = 0; j < nr; ++j) {
               roa[j] = this.in.readTTFUShort();
            }

            j = 0;

            while(true) {
               if (j >= nr) {
                  rs = new GlyphTable.HomogeneousRuleSet(ra);
                  break;
               }

               int ro = roa[j];
               GlyphTable.ChainedGlyphSequenceRule r;
               if (ro <= 0) {
                  r = null;
               } else {
                  this.in.seekSet(subtableOffset + (long)rso + (long)ro);
                  int nbg = this.in.readTTFUShort();
                  int[] backtrackGlyphs = new int[nbg];
                  int nig = 0;

                  for(int nk = backtrackGlyphs.length; nig < nk; ++nig) {
                     backtrackGlyphs[nig] = this.in.readTTFUShort();
                  }

                  nig = this.in.readTTFUShort();
                  int[] glyphs = new int[nig - 1];
                  int nlg = 0;

                  for(int nk = glyphs.length; nlg < nk; ++nlg) {
                     glyphs[nlg] = this.in.readTTFUShort();
                  }

                  nlg = this.in.readTTFUShort();
                  int[] lookaheadGlyphs = new int[nlg];
                  int nl = 0;

                  for(int nk = lookaheadGlyphs.length; nl < nk; ++nl) {
                     lookaheadGlyphs[nl] = this.in.readTTFUShort();
                  }

                  nl = this.in.readTTFUShort();
                  if (log.isDebugEnabled()) {
                     header = tableTag + " contextual substitution lookups @rule[" + i + "][" + j + "]: ";
                  }

                  GlyphTable.RuleLookup[] lookups = this.readRuleLookups(nl, header);
                  r = new GlyphTable.ChainedGlyphSequenceRule(lookups, nig, glyphs, backtrackGlyphs, lookaheadGlyphs);
               }

               ra[j] = r;
               ++j;
            }
         }

         rsa[i] = rs;
      }

      this.seMapping = ct;
      this.seEntries.add(rsa);
   }

   private void readChainedContextualSubTableFormat2(int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
      String tableTag = "GSUB";
      this.in.seekSet(subtableOffset);
      this.in.skip(2L);
      int co = this.in.readTTFUShort();
      int bcdo = this.in.readTTFUShort();
      int icdo = this.in.readTTFUShort();
      int lcdo = this.in.readTTFUShort();
      int ngc = this.in.readTTFUShort();
      int[] csoa = new int[ngc];

      int i;
      for(i = 0; i < ngc; ++i) {
         csoa[i] = this.in.readTTFUShort();
      }

      if (log.isDebugEnabled()) {
         log.debug(tableTag + " chained contextual substitution format: " + subtableFormat + " (glyph classes)");
         log.debug(tableTag + " chained contextual substitution coverage table offset: " + co);
         log.debug(tableTag + " chained contextual substitution class set count: " + ngc);

         for(i = 0; i < ngc; ++i) {
            log.debug(tableTag + " chained contextual substitution class set offset[" + i + "]: " + csoa[i]);
         }
      }

      GlyphCoverageTable ct;
      if (co > 0) {
         ct = this.readCoverageTable(tableTag + " chained contextual substitution coverage", subtableOffset + (long)co);
      } else {
         ct = null;
      }

      GlyphClassTable bcdt;
      if (bcdo > 0) {
         bcdt = this.readClassDefTable(tableTag + " contextual substitution backtrack class definition", subtableOffset + (long)bcdo);
      } else {
         bcdt = null;
      }

      GlyphClassTable icdt;
      if (icdo > 0) {
         icdt = this.readClassDefTable(tableTag + " contextual substitution input class definition", subtableOffset + (long)icdo);
      } else {
         icdt = null;
      }

      GlyphClassTable lcdt;
      if (lcdo > 0) {
         lcdt = this.readClassDefTable(tableTag + " contextual substitution lookahead class definition", subtableOffset + (long)lcdo);
      } else {
         lcdt = null;
      }

      GlyphTable.RuleSet[] rsa = new GlyphTable.RuleSet[ngc];
      String header = null;

      for(int i = 0; i < ngc; ++i) {
         int cso = csoa[i];
         GlyphTable.HomogeneousRuleSet rs;
         if (cso <= 0) {
            rs = null;
         } else {
            this.in.seekSet(subtableOffset + (long)cso);
            int nr = this.in.readTTFUShort();
            int[] roa = new int[nr];
            GlyphTable.Rule[] ra = new GlyphTable.Rule[nr];

            int j;
            for(j = 0; j < nr; ++j) {
               roa[j] = this.in.readTTFUShort();
            }

            j = 0;

            while(true) {
               if (j >= nr) {
                  rs = new GlyphTable.HomogeneousRuleSet(ra);
                  break;
               }

               int ro = roa[j];
               GlyphTable.ChainedClassSequenceRule r;
               if (ro <= 0) {
                  r = null;
               } else {
                  this.in.seekSet(subtableOffset + (long)cso + (long)ro);
                  int nbc = this.in.readTTFUShort();
                  int[] backtrackClasses = new int[nbc];
                  int nic = 0;

                  for(int nk = backtrackClasses.length; nic < nk; ++nic) {
                     backtrackClasses[nic] = this.in.readTTFUShort();
                  }

                  nic = this.in.readTTFUShort();
                  int[] classes = new int[nic - 1];
                  int nlc = 0;

                  for(int nk = classes.length; nlc < nk; ++nlc) {
                     classes[nlc] = this.in.readTTFUShort();
                  }

                  nlc = this.in.readTTFUShort();
                  int[] lookaheadClasses = new int[nlc];
                  int nl = 0;

                  for(int nk = lookaheadClasses.length; nl < nk; ++nl) {
                     lookaheadClasses[nl] = this.in.readTTFUShort();
                  }

                  nl = this.in.readTTFUShort();
                  if (log.isDebugEnabled()) {
                     header = tableTag + " contextual substitution lookups @rule[" + i + "][" + j + "]: ";
                  }

                  GlyphTable.RuleLookup[] lookups = this.readRuleLookups(nl, header);
                  r = new GlyphTable.ChainedClassSequenceRule(lookups, nic, classes, backtrackClasses, lookaheadClasses);
               }

               ra[j] = r;
               ++j;
            }
         }

         rsa[i] = rs;
      }

      this.seMapping = ct;
      this.seEntries.add(icdt);
      this.seEntries.add(bcdt);
      this.seEntries.add(lcdt);
      this.seEntries.add(ngc);
      this.seEntries.add(rsa);
   }

   private void readChainedContextualSubTableFormat3(int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
      String tableTag = "GSUB";
      this.in.seekSet(subtableOffset);
      this.in.skip(2L);
      int nbg = this.in.readTTFUShort();
      int[] bgcoa = new int[nbg];

      int nig;
      for(nig = 0; nig < nbg; ++nig) {
         bgcoa[nig] = this.in.readTTFUShort();
      }

      nig = this.in.readTTFUShort();
      int[] igcoa = new int[nig];

      int nlg;
      for(nlg = 0; nlg < nig; ++nlg) {
         igcoa[nlg] = this.in.readTTFUShort();
      }

      nlg = this.in.readTTFUShort();
      int[] lgcoa = new int[nlg];

      int nl;
      for(nl = 0; nl < nlg; ++nl) {
         lgcoa[nl] = this.in.readTTFUShort();
      }

      nl = this.in.readTTFUShort();
      if (log.isDebugEnabled()) {
         log.debug(tableTag + " chained contextual substitution format: " + subtableFormat + " (glyph sets)");
         log.debug(tableTag + " chained contextual substitution backtrack glyph count: " + nbg);

         int i;
         for(i = 0; i < nbg; ++i) {
            log.debug(tableTag + " chained contextual substitution backtrack coverage table offset[" + i + "]: " + bgcoa[i]);
         }

         log.debug(tableTag + " chained contextual substitution input glyph count: " + nig);

         for(i = 0; i < nig; ++i) {
            log.debug(tableTag + " chained contextual substitution input coverage table offset[" + i + "]: " + igcoa[i]);
         }

         log.debug(tableTag + " chained contextual substitution lookahead glyph count: " + nlg);

         for(i = 0; i < nlg; ++i) {
            log.debug(tableTag + " chained contextual substitution lookahead coverage table offset[" + i + "]: " + lgcoa[i]);
         }

         log.debug(tableTag + " chained contextual substitution lookup count: " + nl);
      }

      GlyphCoverageTable[] bgca = new GlyphCoverageTable[nbg];

      int i;
      for(int i = 0; i < nbg; ++i) {
         i = bgcoa[i];
         GlyphCoverageTable bgct;
         if (i > 0) {
            bgct = this.readCoverageTable(tableTag + " chained contextual substitution backtrack coverage[" + i + "]", subtableOffset + (long)i);
         } else {
            bgct = null;
         }

         bgca[i] = bgct;
      }

      GlyphCoverageTable[] igca = new GlyphCoverageTable[nig];

      int i;
      for(i = 0; i < nig; ++i) {
         i = igcoa[i];
         GlyphCoverageTable igct;
         if (i > 0) {
            igct = this.readCoverageTable(tableTag + " chained contextual substitution input coverage[" + i + "]", subtableOffset + (long)i);
         } else {
            igct = null;
         }

         igca[i] = igct;
      }

      GlyphCoverageTable[] lgca = new GlyphCoverageTable[nlg];

      for(i = 0; i < nlg; ++i) {
         int lgco = lgcoa[i];
         GlyphCoverageTable lgct;
         if (lgco > 0) {
            lgct = this.readCoverageTable(tableTag + " chained contextual substitution lookahead coverage[" + i + "]", subtableOffset + (long)lgco);
         } else {
            lgct = null;
         }

         lgca[i] = lgct;
      }

      String header = null;
      if (log.isDebugEnabled()) {
         header = tableTag + " chained contextual substitution lookups: ";
      }

      GlyphTable.RuleLookup[] lookups = this.readRuleLookups(nl, header);
      GlyphTable.Rule r = new GlyphTable.ChainedCoverageSequenceRule(lookups, nig, igca, bgca, lgca);
      GlyphTable.RuleSet rs = new GlyphTable.HomogeneousRuleSet(new GlyphTable.Rule[]{r});
      GlyphTable.RuleSet[] rsa = new GlyphTable.RuleSet[]{rs};

      assert igca != null && igca.length > 0;

      this.seMapping = igca[0];
      this.seEntries.add(rsa);
   }

   private int readChainedContextualSubTable(int lookupType, int lookupFlags, long subtableOffset) throws IOException {
      this.in.seekSet(subtableOffset);
      int sf = this.in.readTTFUShort();
      if (sf == 1) {
         this.readChainedContextualSubTableFormat1(lookupType, lookupFlags, subtableOffset, sf);
      } else if (sf == 2) {
         this.readChainedContextualSubTableFormat2(lookupType, lookupFlags, subtableOffset, sf);
      } else {
         if (sf != 3) {
            throw new AdvancedTypographicTableFormatException("unsupported chained contextual substitution subtable format: " + sf);
         }

         this.readChainedContextualSubTableFormat3(lookupType, lookupFlags, subtableOffset, sf);
      }

      return sf;
   }

   private void readExtensionSubTableFormat1(int lookupType, int lookupFlags, int lookupSequence, int subtableSequence, long subtableOffset, int subtableFormat) throws IOException {
      String tableTag = "GSUB";
      this.in.seekSet(subtableOffset);
      this.in.skip(2L);
      int lt = this.in.readTTFUShort();
      long eo = this.in.readTTFULong();
      if (log.isDebugEnabled()) {
         log.debug(tableTag + " extension substitution subtable format: " + subtableFormat);
         log.debug(tableTag + " extension substitution lookup type: " + lt);
         log.debug(tableTag + " extension substitution lookup table offset: " + eo);
      }

      this.readGSUBSubtable(lt, lookupFlags, lookupSequence, subtableSequence, subtableOffset + eo);
   }

   private int readExtensionSubTable(int lookupType, int lookupFlags, int lookupSequence, int subtableSequence, long subtableOffset) throws IOException {
      this.in.seekSet(subtableOffset);
      int sf = this.in.readTTFUShort();
      if (sf == 1) {
         this.readExtensionSubTableFormat1(lookupType, lookupFlags, lookupSequence, subtableSequence, subtableOffset, sf);
         return sf;
      } else {
         throw new AdvancedTypographicTableFormatException("unsupported extension substitution subtable format: " + sf);
      }
   }

   private void readReverseChainedSingleSubTableFormat1(int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
      String tableTag = "GSUB";
      this.in.seekSet(subtableOffset);
      this.in.skip(2L);
      int co = this.in.readTTFUShort();
      int nbg = this.in.readTTFUShort();
      int[] bgcoa = new int[nbg];

      int nlg;
      for(nlg = 0; nlg < nbg; ++nlg) {
         bgcoa[nlg] = this.in.readTTFUShort();
      }

      nlg = this.in.readTTFUShort();
      int[] lgcoa = new int[nlg];

      int ng;
      for(ng = 0; ng < nlg; ++ng) {
         lgcoa[ng] = this.in.readTTFUShort();
      }

      ng = this.in.readTTFUShort();
      int[] glyphs = new int[ng];
      int i = 0;

      for(int n = ng; i < n; ++i) {
         glyphs[i] = this.in.readTTFUShort();
      }

      if (log.isDebugEnabled()) {
         log.debug(tableTag + " reverse chained contextual substitution format: " + subtableFormat);
         log.debug(tableTag + " reverse chained contextual substitution coverage table offset: " + co);
         log.debug(tableTag + " reverse chained contextual substitution backtrack glyph count: " + nbg);

         for(i = 0; i < nbg; ++i) {
            log.debug(tableTag + " reverse chained contextual substitution backtrack coverage table offset[" + i + "]: " + bgcoa[i]);
         }

         log.debug(tableTag + " reverse chained contextual substitution lookahead glyph count: " + nlg);

         for(i = 0; i < nlg; ++i) {
            log.debug(tableTag + " reverse chained contextual substitution lookahead coverage table offset[" + i + "]: " + lgcoa[i]);
         }

         log.debug(tableTag + " reverse chained contextual substitution glyphs: " + this.toString(glyphs));
      }

      GlyphCoverageTable ct = this.readCoverageTable(tableTag + " reverse chained contextual substitution coverage", subtableOffset + (long)co);
      GlyphCoverageTable[] bgca = new GlyphCoverageTable[nbg];

      int i;
      for(int i = 0; i < nbg; ++i) {
         i = bgcoa[i];
         GlyphCoverageTable bgct;
         if (i > 0) {
            bgct = this.readCoverageTable(tableTag + " reverse chained contextual substitution backtrack coverage[" + i + "]", subtableOffset + (long)i);
         } else {
            bgct = null;
         }

         bgca[i] = bgct;
      }

      GlyphCoverageTable[] lgca = new GlyphCoverageTable[nlg];

      for(i = 0; i < nlg; ++i) {
         int lgco = lgcoa[i];
         GlyphCoverageTable lgct;
         if (lgco > 0) {
            lgct = this.readCoverageTable(tableTag + " reverse chained contextual substitution lookahead coverage[" + i + "]", subtableOffset + (long)lgco);
         } else {
            lgct = null;
         }

         lgca[i] = lgct;
      }

      this.seMapping = ct;
      this.seEntries.add(bgca);
      this.seEntries.add(lgca);
      this.seEntries.add(glyphs);
   }

   private int readReverseChainedSingleSubTable(int lookupType, int lookupFlags, long subtableOffset) throws IOException {
      this.in.seekSet(subtableOffset);
      int sf = this.in.readTTFUShort();
      if (sf == 1) {
         this.readReverseChainedSingleSubTableFormat1(lookupType, lookupFlags, subtableOffset, sf);
         return sf;
      } else {
         throw new AdvancedTypographicTableFormatException("unsupported reverse chained single substitution subtable format: " + sf);
      }
   }

   private void readGSUBSubtable(int lookupType, int lookupFlags, int lookupSequence, int subtableSequence, long subtableOffset) throws IOException {
      this.initATSubState();
      int subtableFormat = -1;
      switch (lookupType) {
         case 1:
            subtableFormat = this.readSingleSubTable(lookupType, lookupFlags, subtableOffset);
            break;
         case 2:
            subtableFormat = this.readMultipleSubTable(lookupType, lookupFlags, subtableOffset);
            break;
         case 3:
            subtableFormat = this.readAlternateSubTable(lookupType, lookupFlags, subtableOffset);
            break;
         case 4:
            subtableFormat = this.readLigatureSubTable(lookupType, lookupFlags, subtableOffset);
            break;
         case 5:
            subtableFormat = this.readContextualSubTable(lookupType, lookupFlags, subtableOffset);
            break;
         case 6:
            subtableFormat = this.readChainedContextualSubTable(lookupType, lookupFlags, subtableOffset);
            break;
         case 7:
            subtableFormat = this.readExtensionSubTable(lookupType, lookupFlags, lookupSequence, subtableSequence, subtableOffset);
            break;
         case 8:
            subtableFormat = this.readReverseChainedSingleSubTable(lookupType, lookupFlags, subtableOffset);
      }

      this.extractSESubState(1, lookupType, lookupFlags, lookupSequence, subtableSequence, subtableFormat);
      this.resetATSubState();
   }

   private GlyphPositioningTable.DeviceTable readPosDeviceTable(long subtableOffset, long deviceTableOffset) throws IOException {
      long cp = (long)this.in.getCurrentPos();
      this.in.seekSet(subtableOffset + deviceTableOffset);
      int ss = this.in.readTTFUShort();
      int es = this.in.readTTFUShort();
      int df = this.in.readTTFUShort();
      byte s1;
      short m1;
      byte dm;
      short dd;
      byte s2;
      if (df == 1) {
         s1 = 14;
         m1 = 3;
         dm = 1;
         dd = 4;
         s2 = 2;
      } else if (df == 2) {
         s1 = 12;
         m1 = 15;
         dm = 7;
         dd = 16;
         s2 = 4;
      } else {
         if (df != 3) {
            log.debug("unsupported device table delta format: " + df + ", ignoring device table");
            return null;
         }

         s1 = 8;
         m1 = 255;
         dm = 127;
         dd = 256;
         s2 = 8;
      }

      int n = es - ss + 1;
      if (n < 0) {
         log.debug("invalid device table delta count: " + n + ", ignoring device table");
         return null;
      } else {
         int[] da = new int[n];
         int i = 0;

         while(i < n && s2 > 0) {
            int p = this.in.readTTFUShort();
            int j = 0;

            for(int k = 16 / s2; j < k; ++j) {
               int d = p >> s1 & m1;
               if (d > dm) {
                  d -= dd;
               }

               if (i >= n) {
                  break;
               }

               da[i++] = d;
               p <<= s2;
            }
         }

         this.in.seekSet(cp);
         return new GlyphPositioningTable.DeviceTable(ss, es, da);
      }
   }

   private GlyphPositioningTable.Value readPosValue(long subtableOffset, int valueFormat) throws IOException {
      int xp;
      if ((valueFormat & 1) != 0) {
         xp = this.otf.convertTTFUnit2PDFUnit(this.in.readTTFShort());
      } else {
         xp = 0;
      }

      int yp;
      if ((valueFormat & 2) != 0) {
         yp = this.otf.convertTTFUnit2PDFUnit(this.in.readTTFShort());
      } else {
         yp = 0;
      }

      int xa;
      if ((valueFormat & 4) != 0) {
         xa = this.otf.convertTTFUnit2PDFUnit(this.in.readTTFShort());
      } else {
         xa = 0;
      }

      int ya;
      if ((valueFormat & 8) != 0) {
         ya = this.otf.convertTTFUnit2PDFUnit(this.in.readTTFShort());
      } else {
         ya = 0;
      }

      GlyphPositioningTable.DeviceTable xpd;
      if ((valueFormat & 16) != 0) {
         int xpdo = this.in.readTTFUShort();
         xpd = this.readPosDeviceTable(subtableOffset, (long)xpdo);
      } else {
         xpd = null;
      }

      GlyphPositioningTable.DeviceTable ypd;
      if ((valueFormat & 32) != 0) {
         int ypdo = this.in.readTTFUShort();
         ypd = this.readPosDeviceTable(subtableOffset, (long)ypdo);
      } else {
         ypd = null;
      }

      GlyphPositioningTable.DeviceTable xad;
      if ((valueFormat & 64) != 0) {
         int xado = this.in.readTTFUShort();
         xad = this.readPosDeviceTable(subtableOffset, (long)xado);
      } else {
         xad = null;
      }

      GlyphPositioningTable.DeviceTable yad;
      if ((valueFormat & 128) != 0) {
         int yado = this.in.readTTFUShort();
         yad = this.readPosDeviceTable(subtableOffset, (long)yado);
      } else {
         yad = null;
      }

      return new GlyphPositioningTable.Value(xp, yp, xa, ya, xpd, ypd, xad, yad);
   }

   private void readSinglePosTableFormat1(int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
      String tableTag = "GPOS";
      this.in.seekSet(subtableOffset);
      this.in.skip(2L);
      int co = this.in.readTTFUShort();
      int vf = this.in.readTTFUShort();
      GlyphPositioningTable.Value v = this.readPosValue(subtableOffset, vf);
      if (log.isDebugEnabled()) {
         log.debug(tableTag + " single positioning subtable format: " + subtableFormat + " (delta)");
         log.debug(tableTag + " single positioning coverage table offset: " + co);
         log.debug(tableTag + " single positioning value: " + v);
      }

      GlyphCoverageTable ct = this.readCoverageTable(tableTag + " single positioning coverage", subtableOffset + (long)co);
      this.seMapping = ct;
      this.seEntries.add(v);
   }

   private void readSinglePosTableFormat2(int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
      String tableTag = "GPOS";
      this.in.seekSet(subtableOffset);
      this.in.skip(2L);
      int co = this.in.readTTFUShort();
      int vf = this.in.readTTFUShort();
      int nv = this.in.readTTFUShort();
      if (log.isDebugEnabled()) {
         log.debug(tableTag + " single positioning subtable format: " + subtableFormat + " (mapped)");
         log.debug(tableTag + " single positioning coverage table offset: " + co);
         log.debug(tableTag + " single positioning value count: " + nv);
      }

      GlyphCoverageTable ct = this.readCoverageTable(tableTag + " single positioning coverage", subtableOffset + (long)co);
      GlyphPositioningTable.Value[] pva = new GlyphPositioningTable.Value[nv];
      int i = 0;

      for(int n = nv; i < n; ++i) {
         GlyphPositioningTable.Value pv = this.readPosValue(subtableOffset, vf);
         if (log.isDebugEnabled()) {
            log.debug(tableTag + " single positioning value[" + i + "]: " + pv);
         }

         pva[i] = pv;
      }

      this.seMapping = ct;
      this.seEntries.add(pva);
   }

   private int readSinglePosTable(int lookupType, int lookupFlags, long subtableOffset) throws IOException {
      this.in.seekSet(subtableOffset);
      int sf = this.in.readTTFUShort();
      if (sf == 1) {
         this.readSinglePosTableFormat1(lookupType, lookupFlags, subtableOffset, sf);
      } else {
         if (sf != 2) {
            throw new AdvancedTypographicTableFormatException("unsupported single positioning subtable format: " + sf);
         }

         this.readSinglePosTableFormat2(lookupType, lookupFlags, subtableOffset, sf);
      }

      return sf;
   }

   private GlyphPositioningTable.PairValues readPosPairValues(long subtableOffset, boolean hasGlyph, int vf1, int vf2) throws IOException {
      int glyph;
      if (hasGlyph) {
         glyph = this.in.readTTFUShort();
      } else {
         glyph = 0;
      }

      GlyphPositioningTable.Value v1;
      if (vf1 != 0) {
         v1 = this.readPosValue(subtableOffset, vf1);
      } else {
         v1 = null;
      }

      GlyphPositioningTable.Value v2;
      if (vf2 != 0) {
         v2 = this.readPosValue(subtableOffset, vf2);
      } else {
         v2 = null;
      }

      return new GlyphPositioningTable.PairValues(glyph, v1, v2);
   }

   private GlyphPositioningTable.PairValues[] readPosPairSetTable(long subtableOffset, int pairSetTableOffset, int vf1, int vf2) throws IOException {
      String tableTag = "GPOS";
      long cp = (long)this.in.getCurrentPos();
      this.in.seekSet(subtableOffset + (long)pairSetTableOffset);
      int npv = this.in.readTTFUShort();
      if (log.isDebugEnabled()) {
         log.debug(tableTag + " pair set table offset: " + pairSetTableOffset);
         log.debug(tableTag + " pair set table values count: " + npv);
      }

      GlyphPositioningTable.PairValues[] pva = new GlyphPositioningTable.PairValues[npv];
      int i = 0;

      for(int n = npv; i < n; ++i) {
         GlyphPositioningTable.PairValues pv = this.readPosPairValues(subtableOffset, true, vf1, vf2);
         pva[i] = pv;
         if (log.isDebugEnabled()) {
            log.debug(tableTag + " pair set table value[" + i + "]: " + pv);
         }
      }

      this.in.seekSet(cp);
      return pva;
   }

   private void readPairPosTableFormat1(int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
      String tableTag = "GPOS";
      this.in.seekSet(subtableOffset);
      this.in.skip(2L);
      int co = this.in.readTTFUShort();
      int vf1 = this.in.readTTFUShort();
      int vf2 = this.in.readTTFUShort();
      int nps = this.in.readTTFUShort();
      if (log.isDebugEnabled()) {
         log.debug(tableTag + " pair positioning subtable format: " + subtableFormat + " (glyphs)");
         log.debug(tableTag + " pair positioning coverage table offset: " + co);
         log.debug(tableTag + " pair positioning value format #1: " + vf1);
         log.debug(tableTag + " pair positioning value format #2: " + vf2);
      }

      GlyphCoverageTable ct = this.readCoverageTable(tableTag + " pair positioning coverage", subtableOffset + (long)co);
      GlyphPositioningTable.PairValues[][] pvm = new GlyphPositioningTable.PairValues[nps][];
      int i = 0;

      for(int n = nps; i < n; ++i) {
         int pso = this.in.readTTFUShort();
         pvm[i] = this.readPosPairSetTable(subtableOffset, pso, vf1, vf2);
      }

      this.seMapping = ct;
      this.seEntries.add(pvm);
   }

   private void readPairPosTableFormat2(int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
      String tableTag = "GPOS";
      this.in.seekSet(subtableOffset);
      this.in.skip(2L);
      int co = this.in.readTTFUShort();
      int vf1 = this.in.readTTFUShort();
      int vf2 = this.in.readTTFUShort();
      int cd1o = this.in.readTTFUShort();
      int cd2o = this.in.readTTFUShort();
      int nc1 = this.in.readTTFUShort();
      int nc2 = this.in.readTTFUShort();
      if (log.isDebugEnabled()) {
         log.debug(tableTag + " pair positioning subtable format: " + subtableFormat + " (glyph classes)");
         log.debug(tableTag + " pair positioning coverage table offset: " + co);
         log.debug(tableTag + " pair positioning value format #1: " + vf1);
         log.debug(tableTag + " pair positioning value format #2: " + vf2);
         log.debug(tableTag + " pair positioning class def table #1 offset: " + cd1o);
         log.debug(tableTag + " pair positioning class def table #2 offset: " + cd2o);
         log.debug(tableTag + " pair positioning class #1 count: " + nc1);
         log.debug(tableTag + " pair positioning class #2 count: " + nc2);
      }

      GlyphCoverageTable ct = this.readCoverageTable(tableTag + " pair positioning coverage", subtableOffset + (long)co);
      GlyphClassTable cdt1 = this.readClassDefTable(tableTag + " pair positioning class definition #1", subtableOffset + (long)cd1o);
      GlyphClassTable cdt2 = this.readClassDefTable(tableTag + " pair positioning class definition #2", subtableOffset + (long)cd2o);
      GlyphPositioningTable.PairValues[][] pvm = new GlyphPositioningTable.PairValues[nc1][nc2];

      for(int i = 0; i < nc1; ++i) {
         for(int j = 0; j < nc2; ++j) {
            GlyphPositioningTable.PairValues pv = this.readPosPairValues(subtableOffset, false, vf1, vf2);
            pvm[i][j] = pv;
            if (log.isDebugEnabled()) {
               log.debug(tableTag + " pair set table value[" + i + "][" + j + "]: " + pv);
            }
         }
      }

      this.seMapping = ct;
      this.seEntries.add(cdt1);
      this.seEntries.add(cdt2);
      this.seEntries.add(nc1);
      this.seEntries.add(nc2);
      this.seEntries.add(pvm);
   }

   private int readPairPosTable(int lookupType, int lookupFlags, long subtableOffset) throws IOException {
      this.in.seekSet(subtableOffset);
      int sf = this.in.readTTFUShort();
      if (sf == 1) {
         this.readPairPosTableFormat1(lookupType, lookupFlags, subtableOffset, sf);
      } else {
         if (sf != 2) {
            throw new AdvancedTypographicTableFormatException("unsupported pair positioning subtable format: " + sf);
         }

         this.readPairPosTableFormat2(lookupType, lookupFlags, subtableOffset, sf);
      }

      return sf;
   }

   private GlyphPositioningTable.Anchor readPosAnchor(long anchorTableOffset) throws IOException {
      long cp = (long)this.in.getCurrentPos();
      this.in.seekSet(anchorTableOffset);
      int af = this.in.readTTFUShort();
      GlyphPositioningTable.Anchor a;
      int x;
      int y;
      if (af == 1) {
         x = this.otf.convertTTFUnit2PDFUnit(this.in.readTTFShort());
         y = this.otf.convertTTFUnit2PDFUnit(this.in.readTTFShort());
         a = new GlyphPositioningTable.Anchor(x, y);
      } else {
         int xdo;
         if (af == 2) {
            x = this.otf.convertTTFUnit2PDFUnit(this.in.readTTFShort());
            y = this.otf.convertTTFUnit2PDFUnit(this.in.readTTFShort());
            xdo = this.in.readTTFUShort();
            a = new GlyphPositioningTable.Anchor(x, y, xdo);
         } else {
            if (af != 3) {
               throw new AdvancedTypographicTableFormatException("unsupported positioning anchor format: " + af);
            }

            x = this.otf.convertTTFUnit2PDFUnit(this.in.readTTFShort());
            y = this.otf.convertTTFUnit2PDFUnit(this.in.readTTFShort());
            xdo = this.in.readTTFUShort();
            int ydo = this.in.readTTFUShort();
            GlyphPositioningTable.DeviceTable xd;
            if (xdo != 0) {
               xd = this.readPosDeviceTable(cp, (long)xdo);
            } else {
               xd = null;
            }

            GlyphPositioningTable.DeviceTable yd;
            if (ydo != 0) {
               yd = this.readPosDeviceTable(cp, (long)ydo);
            } else {
               yd = null;
            }

            a = new GlyphPositioningTable.Anchor(x, y, xd, yd);
         }
      }

      this.in.seekSet(cp);
      return a;
   }

   private void readCursivePosTableFormat1(int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
      String tableTag = "GPOS";
      this.in.seekSet(subtableOffset);
      this.in.skip(2L);
      int co = this.in.readTTFUShort();
      int ec = this.in.readTTFUShort();
      if (log.isDebugEnabled()) {
         log.debug(tableTag + " cursive positioning subtable format: " + subtableFormat);
         log.debug(tableTag + " cursive positioning coverage table offset: " + co);
         log.debug(tableTag + " cursive positioning entry/exit count: " + ec);
      }

      GlyphCoverageTable ct = this.readCoverageTable(tableTag + " cursive positioning coverage", subtableOffset + (long)co);
      GlyphPositioningTable.Anchor[] aa = new GlyphPositioningTable.Anchor[ec * 2];
      int i = 0;

      for(int n = ec; i < n; ++i) {
         int eno = this.in.readTTFUShort();
         int exo = this.in.readTTFUShort();
         GlyphPositioningTable.Anchor ena;
         if (eno > 0) {
            ena = this.readPosAnchor(subtableOffset + (long)eno);
         } else {
            ena = null;
         }

         GlyphPositioningTable.Anchor exa;
         if (exo > 0) {
            exa = this.readPosAnchor(subtableOffset + (long)exo);
         } else {
            exa = null;
         }

         aa[i * 2 + 0] = ena;
         aa[i * 2 + 1] = exa;
         if (log.isDebugEnabled()) {
            if (ena != null) {
               log.debug(tableTag + " cursive entry anchor [" + i + "]: " + ena);
            }

            if (exa != null) {
               log.debug(tableTag + " cursive exit anchor  [" + i + "]: " + exa);
            }
         }
      }

      this.seMapping = ct;
      this.seEntries.add(aa);
   }

   private int readCursivePosTable(int lookupType, int lookupFlags, long subtableOffset) throws IOException {
      this.in.seekSet(subtableOffset);
      int sf = this.in.readTTFUShort();
      if (sf == 1) {
         this.readCursivePosTableFormat1(lookupType, lookupFlags, subtableOffset, sf);
         return sf;
      } else {
         throw new AdvancedTypographicTableFormatException("unsupported cursive positioning subtable format: " + sf);
      }
   }

   private void readMarkToBasePosTableFormat1(int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
      String tableTag = "GPOS";
      this.in.seekSet(subtableOffset);
      this.in.skip(2L);
      int mco = this.in.readTTFUShort();
      int bco = this.in.readTTFUShort();
      int nmc = this.in.readTTFUShort();
      int mao = this.in.readTTFUShort();
      int bao = this.in.readTTFUShort();
      if (log.isDebugEnabled()) {
         log.debug(tableTag + " mark-to-base positioning subtable format: " + subtableFormat);
         log.debug(tableTag + " mark-to-base positioning mark coverage table offset: " + mco);
         log.debug(tableTag + " mark-to-base positioning base coverage table offset: " + bco);
         log.debug(tableTag + " mark-to-base positioning mark class count: " + nmc);
         log.debug(tableTag + " mark-to-base positioning mark array offset: " + mao);
         log.debug(tableTag + " mark-to-base positioning base array offset: " + bao);
      }

      GlyphCoverageTable mct = this.readCoverageTable(tableTag + " mark-to-base positioning mark coverage", subtableOffset + (long)mco);
      GlyphCoverageTable bct = this.readCoverageTable(tableTag + " mark-to-base positioning base coverage", subtableOffset + (long)bco);
      this.in.seekSet(subtableOffset + (long)mao);
      int nm = this.in.readTTFUShort();
      if (log.isDebugEnabled()) {
         log.debug(tableTag + " mark-to-base positioning mark count: " + nm);
      }

      GlyphPositioningTable.MarkAnchor[] maa = new GlyphPositioningTable.MarkAnchor[nm];

      int i;
      int i;
      for(i = 0; i < nm; ++i) {
         int mc = this.in.readTTFUShort();
         i = this.in.readTTFUShort();
         GlyphPositioningTable.Anchor a;
         if (i > 0) {
            a = this.readPosAnchor(subtableOffset + (long)mao + (long)i);
         } else {
            a = null;
         }

         GlyphPositioningTable.MarkAnchor ma;
         if (a != null) {
            ma = new GlyphPositioningTable.MarkAnchor(mc, a);
         } else {
            ma = null;
         }

         maa[i] = ma;
         if (log.isDebugEnabled()) {
            log.debug(tableTag + " mark-to-base positioning mark anchor[" + i + "]: " + ma);
         }
      }

      this.in.seekSet(subtableOffset + (long)bao);
      i = this.in.readTTFUShort();
      if (log.isDebugEnabled()) {
         log.debug(tableTag + " mark-to-base positioning base count: " + i);
      }

      GlyphPositioningTable.Anchor[][] bam = new GlyphPositioningTable.Anchor[i][nmc];

      for(i = 0; i < i; ++i) {
         for(int j = 0; j < nmc; ++j) {
            int ao = this.in.readTTFUShort();
            GlyphPositioningTable.Anchor a;
            if (ao > 0) {
               a = this.readPosAnchor(subtableOffset + (long)bao + (long)ao);
            } else {
               a = null;
            }

            bam[i][j] = a;
            if (log.isDebugEnabled()) {
               log.debug(tableTag + " mark-to-base positioning base anchor[" + i + "][" + j + "]: " + a);
            }
         }
      }

      this.seMapping = mct;
      this.seEntries.add(bct);
      this.seEntries.add(nmc);
      this.seEntries.add(maa);
      this.seEntries.add(bam);
   }

   private int readMarkToBasePosTable(int lookupType, int lookupFlags, long subtableOffset) throws IOException {
      this.in.seekSet(subtableOffset);
      int sf = this.in.readTTFUShort();
      if (sf == 1) {
         this.readMarkToBasePosTableFormat1(lookupType, lookupFlags, subtableOffset, sf);
         return sf;
      } else {
         throw new AdvancedTypographicTableFormatException("unsupported mark-to-base positioning subtable format: " + sf);
      }
   }

   private void readMarkToLigaturePosTableFormat1(int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
      String tableTag = "GPOS";
      this.in.seekSet(subtableOffset);
      this.in.skip(2L);
      int mco = this.in.readTTFUShort();
      int lco = this.in.readTTFUShort();
      int nmc = this.in.readTTFUShort();
      int mao = this.in.readTTFUShort();
      int lao = this.in.readTTFUShort();
      if (log.isDebugEnabled()) {
         log.debug(tableTag + " mark-to-ligature positioning subtable format: " + subtableFormat);
         log.debug(tableTag + " mark-to-ligature positioning mark coverage table offset: " + mco);
         log.debug(tableTag + " mark-to-ligature positioning ligature coverage table offset: " + lco);
         log.debug(tableTag + " mark-to-ligature positioning mark class count: " + nmc);
         log.debug(tableTag + " mark-to-ligature positioning mark array offset: " + mao);
         log.debug(tableTag + " mark-to-ligature positioning ligature array offset: " + lao);
      }

      GlyphCoverageTable mct = this.readCoverageTable(tableTag + " mark-to-ligature positioning mark coverage", subtableOffset + (long)mco);
      GlyphCoverageTable lct = this.readCoverageTable(tableTag + " mark-to-ligature positioning ligature coverage", subtableOffset + (long)lco);
      this.in.seekSet(subtableOffset + (long)mao);
      int nm = this.in.readTTFUShort();
      if (log.isDebugEnabled()) {
         log.debug(tableTag + " mark-to-ligature positioning mark count: " + nm);
      }

      GlyphPositioningTable.MarkAnchor[] maa = new GlyphPositioningTable.MarkAnchor[nm];

      int nl;
      int mxc;
      for(nl = 0; nl < nm; ++nl) {
         int mc = this.in.readTTFUShort();
         mxc = this.in.readTTFUShort();
         GlyphPositioningTable.Anchor a;
         if (mxc > 0) {
            a = this.readPosAnchor(subtableOffset + (long)mao + (long)mxc);
         } else {
            a = null;
         }

         GlyphPositioningTable.MarkAnchor ma;
         if (a != null) {
            ma = new GlyphPositioningTable.MarkAnchor(mc, a);
         } else {
            ma = null;
         }

         maa[nl] = ma;
         if (log.isDebugEnabled()) {
            log.debug(tableTag + " mark-to-ligature positioning mark anchor[" + nl + "]: " + ma);
         }
      }

      this.in.seekSet(subtableOffset + (long)lao);
      nl = this.in.readTTFUShort();
      if (log.isDebugEnabled()) {
         log.debug(tableTag + " mark-to-ligature positioning ligature count: " + nl);
      }

      int[] laoa = new int[nl];

      for(mxc = 0; mxc < nl; ++mxc) {
         laoa[mxc] = this.in.readTTFUShort();
      }

      mxc = 0;

      int lato;
      int i;
      for(int i = 0; i < nl; ++i) {
         i = laoa[i];
         this.in.seekSet(subtableOffset + (long)lao + (long)i);
         lato = this.in.readTTFUShort();
         if (lato > mxc) {
            mxc = lato;
         }
      }

      if (log.isDebugEnabled()) {
         log.debug(tableTag + " mark-to-ligature positioning maximum component count: " + mxc);
      }

      GlyphPositioningTable.Anchor[][][] lam = new GlyphPositioningTable.Anchor[nl][][];

      for(i = 0; i < nl; ++i) {
         lato = laoa[i];
         this.in.seekSet(subtableOffset + (long)lao + (long)lato);
         int cc = this.in.readTTFUShort();
         GlyphPositioningTable.Anchor[][] lcm = new GlyphPositioningTable.Anchor[cc][nmc];

         for(int j = 0; j < cc; ++j) {
            for(int k = 0; k < nmc; ++k) {
               int ao = this.in.readTTFUShort();
               GlyphPositioningTable.Anchor a;
               if (ao > 0) {
                  a = this.readPosAnchor(subtableOffset + (long)lao + (long)lato + (long)ao);
               } else {
                  a = null;
               }

               lcm[j][k] = a;
               if (log.isDebugEnabled()) {
                  log.debug(tableTag + " mark-to-ligature positioning ligature anchor[" + i + "][" + j + "][" + k + "]: " + a);
               }
            }
         }

         lam[i] = lcm;
      }

      this.seMapping = mct;
      this.seEntries.add(lct);
      this.seEntries.add(nmc);
      this.seEntries.add(mxc);
      this.seEntries.add(maa);
      this.seEntries.add(lam);
   }

   private int readMarkToLigaturePosTable(int lookupType, int lookupFlags, long subtableOffset) throws IOException {
      this.in.seekSet(subtableOffset);
      int sf = this.in.readTTFUShort();
      if (sf == 1) {
         this.readMarkToLigaturePosTableFormat1(lookupType, lookupFlags, subtableOffset, sf);
         return sf;
      } else {
         throw new AdvancedTypographicTableFormatException("unsupported mark-to-ligature positioning subtable format: " + sf);
      }
   }

   private void readMarkToMarkPosTableFormat1(int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
      String tableTag = "GPOS";
      this.in.seekSet(subtableOffset);
      this.in.skip(2L);
      int m1co = this.in.readTTFUShort();
      int m2co = this.in.readTTFUShort();
      int nmc = this.in.readTTFUShort();
      int m1ao = this.in.readTTFUShort();
      int m2ao = this.in.readTTFUShort();
      if (log.isDebugEnabled()) {
         log.debug(tableTag + " mark-to-mark positioning subtable format: " + subtableFormat);
         log.debug(tableTag + " mark-to-mark positioning mark #1 coverage table offset: " + m1co);
         log.debug(tableTag + " mark-to-mark positioning mark #2 coverage table offset: " + m2co);
         log.debug(tableTag + " mark-to-mark positioning mark class count: " + nmc);
         log.debug(tableTag + " mark-to-mark positioning mark #1 array offset: " + m1ao);
         log.debug(tableTag + " mark-to-mark positioning mark #2 array offset: " + m2ao);
      }

      GlyphCoverageTable mct1 = this.readCoverageTable(tableTag + " mark-to-mark positioning mark #1 coverage", subtableOffset + (long)m1co);
      GlyphCoverageTable mct2 = this.readCoverageTable(tableTag + " mark-to-mark positioning mark #2 coverage", subtableOffset + (long)m2co);
      this.in.seekSet(subtableOffset + (long)m1ao);
      int nm1 = this.in.readTTFUShort();
      if (log.isDebugEnabled()) {
         log.debug(tableTag + " mark-to-mark positioning mark #1 count: " + nm1);
      }

      GlyphPositioningTable.MarkAnchor[] maa = new GlyphPositioningTable.MarkAnchor[nm1];

      int i;
      int i;
      for(i = 0; i < nm1; ++i) {
         int mc = this.in.readTTFUShort();
         i = this.in.readTTFUShort();
         GlyphPositioningTable.Anchor a;
         if (i > 0) {
            a = this.readPosAnchor(subtableOffset + (long)m1ao + (long)i);
         } else {
            a = null;
         }

         GlyphPositioningTable.MarkAnchor ma;
         if (a != null) {
            ma = new GlyphPositioningTable.MarkAnchor(mc, a);
         } else {
            ma = null;
         }

         maa[i] = ma;
         if (log.isDebugEnabled()) {
            log.debug(tableTag + " mark-to-mark positioning mark #1 anchor[" + i + "]: " + ma);
         }
      }

      this.in.seekSet(subtableOffset + (long)m2ao);
      i = this.in.readTTFUShort();
      if (log.isDebugEnabled()) {
         log.debug(tableTag + " mark-to-mark positioning mark #2 count: " + i);
      }

      GlyphPositioningTable.Anchor[][] mam = new GlyphPositioningTable.Anchor[i][nmc];

      for(i = 0; i < i; ++i) {
         for(int j = 0; j < nmc; ++j) {
            int ao = this.in.readTTFUShort();
            GlyphPositioningTable.Anchor a;
            if (ao > 0) {
               a = this.readPosAnchor(subtableOffset + (long)m2ao + (long)ao);
            } else {
               a = null;
            }

            mam[i][j] = a;
            if (log.isDebugEnabled()) {
               log.debug(tableTag + " mark-to-mark positioning mark #2 anchor[" + i + "][" + j + "]: " + a);
            }
         }
      }

      this.seMapping = mct1;
      this.seEntries.add(mct2);
      this.seEntries.add(nmc);
      this.seEntries.add(maa);
      this.seEntries.add(mam);
   }

   private int readMarkToMarkPosTable(int lookupType, int lookupFlags, long subtableOffset) throws IOException {
      this.in.seekSet(subtableOffset);
      int sf = this.in.readTTFUShort();
      if (sf == 1) {
         this.readMarkToMarkPosTableFormat1(lookupType, lookupFlags, subtableOffset, sf);
         return sf;
      } else {
         throw new AdvancedTypographicTableFormatException("unsupported mark-to-mark positioning subtable format: " + sf);
      }
   }

   private void readContextualPosTableFormat1(int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
      String tableTag = "GPOS";
      this.in.seekSet(subtableOffset);
      this.in.skip(2L);
      int co = this.in.readTTFUShort();
      int nrs = this.in.readTTFUShort();
      int[] rsoa = new int[nrs];

      int i;
      for(i = 0; i < nrs; ++i) {
         rsoa[i] = this.in.readTTFUShort();
      }

      if (log.isDebugEnabled()) {
         log.debug(tableTag + " contextual positioning subtable format: " + subtableFormat + " (glyphs)");
         log.debug(tableTag + " contextual positioning coverage table offset: " + co);
         log.debug(tableTag + " contextual positioning rule set count: " + nrs);

         for(i = 0; i < nrs; ++i) {
            log.debug(tableTag + " contextual positioning rule set offset[" + i + "]: " + rsoa[i]);
         }
      }

      GlyphCoverageTable ct;
      if (co > 0) {
         ct = this.readCoverageTable(tableTag + " contextual positioning coverage", subtableOffset + (long)co);
      } else {
         ct = null;
      }

      GlyphTable.RuleSet[] rsa = new GlyphTable.RuleSet[nrs];
      String header = null;

      for(int i = 0; i < nrs; ++i) {
         int rso = rsoa[i];
         GlyphTable.HomogeneousRuleSet rs;
         if (rso <= 0) {
            rs = null;
         } else {
            this.in.seekSet(subtableOffset + (long)rso);
            int nr = this.in.readTTFUShort();
            int[] roa = new int[nr];
            GlyphTable.Rule[] ra = new GlyphTable.Rule[nr];

            int j;
            for(j = 0; j < nr; ++j) {
               roa[j] = this.in.readTTFUShort();
            }

            j = 0;

            while(true) {
               if (j >= nr) {
                  rs = new GlyphTable.HomogeneousRuleSet(ra);
                  break;
               }

               int ro = roa[j];
               GlyphTable.GlyphSequenceRule r;
               if (ro <= 0) {
                  r = null;
               } else {
                  this.in.seekSet(subtableOffset + (long)rso + (long)ro);
                  int ng = this.in.readTTFUShort();
                  int nl = this.in.readTTFUShort();
                  int[] glyphs = new int[ng - 1];
                  int k = 0;

                  for(int nk = glyphs.length; k < nk; ++k) {
                     glyphs[k] = this.in.readTTFUShort();
                  }

                  if (log.isDebugEnabled()) {
                     header = tableTag + " contextual positioning lookups @rule[" + i + "][" + j + "]: ";
                  }

                  GlyphTable.RuleLookup[] lookups = this.readRuleLookups(nl, header);
                  r = new GlyphTable.GlyphSequenceRule(lookups, ng, glyphs);
               }

               ra[j] = r;
               ++j;
            }
         }

         rsa[i] = rs;
      }

      this.seMapping = ct;
      this.seEntries.add(rsa);
   }

   private void readContextualPosTableFormat2(int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
      String tableTag = "GPOS";
      this.in.seekSet(subtableOffset);
      this.in.skip(2L);
      int co = this.in.readTTFUShort();
      int cdo = this.in.readTTFUShort();
      int ngc = this.in.readTTFUShort();
      int[] csoa = new int[ngc];

      int i;
      for(i = 0; i < ngc; ++i) {
         csoa[i] = this.in.readTTFUShort();
      }

      if (log.isDebugEnabled()) {
         log.debug(tableTag + " contextual positioning subtable format: " + subtableFormat + " (glyph classes)");
         log.debug(tableTag + " contextual positioning coverage table offset: " + co);
         log.debug(tableTag + " contextual positioning class set count: " + ngc);

         for(i = 0; i < ngc; ++i) {
            log.debug(tableTag + " contextual positioning class set offset[" + i + "]: " + csoa[i]);
         }
      }

      GlyphCoverageTable ct;
      if (co > 0) {
         ct = this.readCoverageTable(tableTag + " contextual positioning coverage", subtableOffset + (long)co);
      } else {
         ct = null;
      }

      GlyphClassTable cdt;
      if (cdo > 0) {
         cdt = this.readClassDefTable(tableTag + " contextual positioning class definition", subtableOffset + (long)cdo);
      } else {
         cdt = null;
      }

      GlyphTable.RuleSet[] rsa = new GlyphTable.RuleSet[ngc];
      String header = null;

      for(int i = 0; i < ngc; ++i) {
         int cso = csoa[i];
         GlyphTable.HomogeneousRuleSet rs;
         if (cso <= 0) {
            rs = null;
         } else {
            this.in.seekSet(subtableOffset + (long)cso);
            int nr = this.in.readTTFUShort();
            int[] roa = new int[nr];
            GlyphTable.Rule[] ra = new GlyphTable.Rule[nr];

            int j;
            for(j = 0; j < nr; ++j) {
               roa[j] = this.in.readTTFUShort();
            }

            j = 0;

            while(true) {
               if (j >= nr) {
                  rs = new GlyphTable.HomogeneousRuleSet(ra);
                  break;
               }

               int ro = roa[j];
               GlyphTable.ClassSequenceRule r;
               if (ro <= 0) {
                  r = null;
               } else {
                  this.in.seekSet(subtableOffset + (long)cso + (long)ro);
                  int ng = this.in.readTTFUShort();
                  int nl = this.in.readTTFUShort();
                  int[] classes = new int[ng - 1];
                  int k = 0;

                  for(int nk = classes.length; k < nk; ++k) {
                     classes[k] = this.in.readTTFUShort();
                  }

                  if (log.isDebugEnabled()) {
                     header = tableTag + " contextual positioning lookups @rule[" + i + "][" + j + "]: ";
                  }

                  GlyphTable.RuleLookup[] lookups = this.readRuleLookups(nl, header);
                  r = new GlyphTable.ClassSequenceRule(lookups, ng, classes);
               }

               ra[j] = r;
               ++j;
            }
         }

         rsa[i] = rs;
      }

      this.seMapping = ct;
      this.seEntries.add(cdt);
      this.seEntries.add(ngc);
      this.seEntries.add(rsa);
   }

   private void readContextualPosTableFormat3(int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
      String tableTag = "GPOS";
      this.in.seekSet(subtableOffset);
      this.in.skip(2L);
      int ng = this.in.readTTFUShort();
      int nl = this.in.readTTFUShort();
      int[] gcoa = new int[ng];

      int i;
      for(i = 0; i < ng; ++i) {
         gcoa[i] = this.in.readTTFUShort();
      }

      if (log.isDebugEnabled()) {
         log.debug(tableTag + " contextual positioning subtable format: " + subtableFormat + " (glyph sets)");
         log.debug(tableTag + " contextual positioning glyph input sequence length count: " + ng);
         log.debug(tableTag + " contextual positioning lookup count: " + nl);

         for(i = 0; i < ng; ++i) {
            log.debug(tableTag + " contextual positioning coverage table offset[" + i + "]: " + gcoa[i]);
         }
      }

      GlyphCoverageTable[] gca = new GlyphCoverageTable[ng];

      for(int i = 0; i < ng; ++i) {
         int gco = gcoa[i];
         GlyphCoverageTable gct;
         if (gco > 0) {
            gct = this.readCoverageTable(tableTag + " contextual positioning coverage[" + i + "]", subtableOffset + (long)gcoa[i]);
         } else {
            gct = null;
         }

         gca[i] = gct;
      }

      String header = null;
      if (log.isDebugEnabled()) {
         header = tableTag + " contextual positioning lookups: ";
      }

      GlyphTable.RuleLookup[] lookups = this.readRuleLookups(nl, header);
      GlyphTable.Rule r = new GlyphTable.CoverageSequenceRule(lookups, ng, gca);
      GlyphTable.RuleSet rs = new GlyphTable.HomogeneousRuleSet(new GlyphTable.Rule[]{r});
      GlyphTable.RuleSet[] rsa = new GlyphTable.RuleSet[]{rs};

      assert gca != null && gca.length > 0;

      this.seMapping = gca[0];
      this.seEntries.add(rsa);
   }

   private int readContextualPosTable(int lookupType, int lookupFlags, long subtableOffset) throws IOException {
      this.in.seekSet(subtableOffset);
      int sf = this.in.readTTFUShort();
      if (sf == 1) {
         this.readContextualPosTableFormat1(lookupType, lookupFlags, subtableOffset, sf);
      } else if (sf == 2) {
         this.readContextualPosTableFormat2(lookupType, lookupFlags, subtableOffset, sf);
      } else {
         if (sf != 3) {
            throw new AdvancedTypographicTableFormatException("unsupported contextual positioning subtable format: " + sf);
         }

         this.readContextualPosTableFormat3(lookupType, lookupFlags, subtableOffset, sf);
      }

      return sf;
   }

   private void readChainedContextualPosTableFormat1(int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
      String tableTag = "GPOS";
      this.in.seekSet(subtableOffset);
      this.in.skip(2L);
      int co = this.in.readTTFUShort();
      int nrs = this.in.readTTFUShort();
      int[] rsoa = new int[nrs];

      int i;
      for(i = 0; i < nrs; ++i) {
         rsoa[i] = this.in.readTTFUShort();
      }

      if (log.isDebugEnabled()) {
         log.debug(tableTag + " chained contextual positioning subtable format: " + subtableFormat + " (glyphs)");
         log.debug(tableTag + " chained contextual positioning coverage table offset: " + co);
         log.debug(tableTag + " chained contextual positioning rule set count: " + nrs);

         for(i = 0; i < nrs; ++i) {
            log.debug(tableTag + " chained contextual positioning rule set offset[" + i + "]: " + rsoa[i]);
         }
      }

      GlyphCoverageTable ct;
      if (co > 0) {
         ct = this.readCoverageTable(tableTag + " chained contextual positioning coverage", subtableOffset + (long)co);
      } else {
         ct = null;
      }

      GlyphTable.RuleSet[] rsa = new GlyphTable.RuleSet[nrs];
      String header = null;

      for(int i = 0; i < nrs; ++i) {
         int rso = rsoa[i];
         GlyphTable.HomogeneousRuleSet rs;
         if (rso <= 0) {
            rs = null;
         } else {
            this.in.seekSet(subtableOffset + (long)rso);
            int nr = this.in.readTTFUShort();
            int[] roa = new int[nr];
            GlyphTable.Rule[] ra = new GlyphTable.Rule[nr];

            int j;
            for(j = 0; j < nr; ++j) {
               roa[j] = this.in.readTTFUShort();
            }

            j = 0;

            while(true) {
               if (j >= nr) {
                  rs = new GlyphTable.HomogeneousRuleSet(ra);
                  break;
               }

               int ro = roa[j];
               GlyphTable.ChainedGlyphSequenceRule r;
               if (ro <= 0) {
                  r = null;
               } else {
                  this.in.seekSet(subtableOffset + (long)rso + (long)ro);
                  int nbg = this.in.readTTFUShort();
                  int[] backtrackGlyphs = new int[nbg];
                  int nig = 0;

                  for(int nk = backtrackGlyphs.length; nig < nk; ++nig) {
                     backtrackGlyphs[nig] = this.in.readTTFUShort();
                  }

                  nig = this.in.readTTFUShort();
                  int[] glyphs = new int[nig - 1];
                  int nlg = 0;

                  for(int nk = glyphs.length; nlg < nk; ++nlg) {
                     glyphs[nlg] = this.in.readTTFUShort();
                  }

                  nlg = this.in.readTTFUShort();
                  int[] lookaheadGlyphs = new int[nlg];
                  int nl = 0;

                  for(int nk = lookaheadGlyphs.length; nl < nk; ++nl) {
                     lookaheadGlyphs[nl] = this.in.readTTFUShort();
                  }

                  nl = this.in.readTTFUShort();
                  if (log.isDebugEnabled()) {
                     header = tableTag + " contextual positioning lookups @rule[" + i + "][" + j + "]: ";
                  }

                  GlyphTable.RuleLookup[] lookups = this.readRuleLookups(nl, header);
                  r = new GlyphTable.ChainedGlyphSequenceRule(lookups, nig, glyphs, backtrackGlyphs, lookaheadGlyphs);
               }

               ra[j] = r;
               ++j;
            }
         }

         rsa[i] = rs;
      }

      this.seMapping = ct;
      this.seEntries.add(rsa);
   }

   private void readChainedContextualPosTableFormat2(int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
      String tableTag = "GPOS";
      this.in.seekSet(subtableOffset);
      this.in.skip(2L);
      int co = this.in.readTTFUShort();
      int bcdo = this.in.readTTFUShort();
      int icdo = this.in.readTTFUShort();
      int lcdo = this.in.readTTFUShort();
      int ngc = this.in.readTTFUShort();
      int[] csoa = new int[ngc];

      int i;
      for(i = 0; i < ngc; ++i) {
         csoa[i] = this.in.readTTFUShort();
      }

      if (log.isDebugEnabled()) {
         log.debug(tableTag + " chained contextual positioning subtable format: " + subtableFormat + " (glyph classes)");
         log.debug(tableTag + " chained contextual positioning coverage table offset: " + co);
         log.debug(tableTag + " chained contextual positioning class set count: " + ngc);

         for(i = 0; i < ngc; ++i) {
            log.debug(tableTag + " chained contextual positioning class set offset[" + i + "]: " + csoa[i]);
         }
      }

      GlyphCoverageTable ct;
      if (co > 0) {
         ct = this.readCoverageTable(tableTag + " chained contextual positioning coverage", subtableOffset + (long)co);
      } else {
         ct = null;
      }

      GlyphClassTable bcdt;
      if (bcdo > 0) {
         bcdt = this.readClassDefTable(tableTag + " contextual positioning backtrack class definition", subtableOffset + (long)bcdo);
      } else {
         bcdt = null;
      }

      GlyphClassTable icdt;
      if (icdo > 0) {
         icdt = this.readClassDefTable(tableTag + " contextual positioning input class definition", subtableOffset + (long)icdo);
      } else {
         icdt = null;
      }

      GlyphClassTable lcdt;
      if (lcdo > 0) {
         lcdt = this.readClassDefTable(tableTag + " contextual positioning lookahead class definition", subtableOffset + (long)lcdo);
      } else {
         lcdt = null;
      }

      GlyphTable.RuleSet[] rsa = new GlyphTable.RuleSet[ngc];
      String header = null;

      for(int i = 0; i < ngc; ++i) {
         int cso = csoa[i];
         GlyphTable.HomogeneousRuleSet rs;
         if (cso <= 0) {
            rs = null;
         } else {
            this.in.seekSet(subtableOffset + (long)cso);
            int nr = this.in.readTTFUShort();
            int[] roa = new int[nr];
            GlyphTable.Rule[] ra = new GlyphTable.Rule[nr];

            int j;
            for(j = 0; j < nr; ++j) {
               roa[j] = this.in.readTTFUShort();
            }

            j = 0;

            while(true) {
               if (j >= nr) {
                  rs = new GlyphTable.HomogeneousRuleSet(ra);
                  break;
               }

               int ro = roa[j];
               GlyphTable.ChainedClassSequenceRule r;
               if (ro <= 0) {
                  r = null;
               } else {
                  this.in.seekSet(subtableOffset + (long)cso + (long)ro);
                  int nbc = this.in.readTTFUShort();
                  int[] backtrackClasses = new int[nbc];
                  int nic = 0;

                  for(int nk = backtrackClasses.length; nic < nk; ++nic) {
                     backtrackClasses[nic] = this.in.readTTFUShort();
                  }

                  nic = this.in.readTTFUShort();
                  int[] classes = new int[nic - 1];
                  int nlc = 0;

                  for(int nk = classes.length; nlc < nk; ++nlc) {
                     classes[nlc] = this.in.readTTFUShort();
                  }

                  nlc = this.in.readTTFUShort();
                  int[] lookaheadClasses = new int[nlc];
                  int nl = 0;

                  for(int nk = lookaheadClasses.length; nl < nk; ++nl) {
                     lookaheadClasses[nl] = this.in.readTTFUShort();
                  }

                  nl = this.in.readTTFUShort();
                  if (log.isDebugEnabled()) {
                     header = tableTag + " contextual positioning lookups @rule[" + i + "][" + j + "]: ";
                  }

                  GlyphTable.RuleLookup[] lookups = this.readRuleLookups(nl, header);
                  r = new GlyphTable.ChainedClassSequenceRule(lookups, nic, classes, backtrackClasses, lookaheadClasses);
               }

               ra[j] = r;
               ++j;
            }
         }

         rsa[i] = rs;
      }

      this.seMapping = ct;
      this.seEntries.add(icdt);
      this.seEntries.add(bcdt);
      this.seEntries.add(lcdt);
      this.seEntries.add(ngc);
      this.seEntries.add(rsa);
   }

   private void readChainedContextualPosTableFormat3(int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
      String tableTag = "GPOS";
      this.in.seekSet(subtableOffset);
      this.in.skip(2L);
      int nbg = this.in.readTTFUShort();
      int[] bgcoa = new int[nbg];

      int nig;
      for(nig = 0; nig < nbg; ++nig) {
         bgcoa[nig] = this.in.readTTFUShort();
      }

      nig = this.in.readTTFUShort();
      int[] igcoa = new int[nig];

      int nlg;
      for(nlg = 0; nlg < nig; ++nlg) {
         igcoa[nlg] = this.in.readTTFUShort();
      }

      nlg = this.in.readTTFUShort();
      int[] lgcoa = new int[nlg];

      int nl;
      for(nl = 0; nl < nlg; ++nl) {
         lgcoa[nl] = this.in.readTTFUShort();
      }

      nl = this.in.readTTFUShort();
      if (log.isDebugEnabled()) {
         log.debug(tableTag + " chained contextual positioning subtable format: " + subtableFormat + " (glyph sets)");
         log.debug(tableTag + " chained contextual positioning backtrack glyph count: " + nbg);

         int i;
         for(i = 0; i < nbg; ++i) {
            log.debug(tableTag + " chained contextual positioning backtrack coverage table offset[" + i + "]: " + bgcoa[i]);
         }

         log.debug(tableTag + " chained contextual positioning input glyph count: " + nig);

         for(i = 0; i < nig; ++i) {
            log.debug(tableTag + " chained contextual positioning input coverage table offset[" + i + "]: " + igcoa[i]);
         }

         log.debug(tableTag + " chained contextual positioning lookahead glyph count: " + nlg);

         for(i = 0; i < nlg; ++i) {
            log.debug(tableTag + " chained contextual positioning lookahead coverage table offset[" + i + "]: " + lgcoa[i]);
         }

         log.debug(tableTag + " chained contextual positioning lookup count: " + nl);
      }

      GlyphCoverageTable[] bgca = new GlyphCoverageTable[nbg];

      int i;
      for(int i = 0; i < nbg; ++i) {
         i = bgcoa[i];
         GlyphCoverageTable bgct;
         if (i > 0) {
            bgct = this.readCoverageTable(tableTag + " chained contextual positioning backtrack coverage[" + i + "]", subtableOffset + (long)i);
         } else {
            bgct = null;
         }

         bgca[i] = bgct;
      }

      GlyphCoverageTable[] igca = new GlyphCoverageTable[nig];

      int i;
      for(i = 0; i < nig; ++i) {
         i = igcoa[i];
         GlyphCoverageTable igct;
         if (i > 0) {
            igct = this.readCoverageTable(tableTag + " chained contextual positioning input coverage[" + i + "]", subtableOffset + (long)i);
         } else {
            igct = null;
         }

         igca[i] = igct;
      }

      GlyphCoverageTable[] lgca = new GlyphCoverageTable[nlg];

      for(i = 0; i < nlg; ++i) {
         int lgco = lgcoa[i];
         GlyphCoverageTable lgct;
         if (lgco > 0) {
            lgct = this.readCoverageTable(tableTag + " chained contextual positioning lookahead coverage[" + i + "]", subtableOffset + (long)lgco);
         } else {
            lgct = null;
         }

         lgca[i] = lgct;
      }

      String header = null;
      if (log.isDebugEnabled()) {
         header = tableTag + " chained contextual positioning lookups: ";
      }

      GlyphTable.RuleLookup[] lookups = this.readRuleLookups(nl, header);
      GlyphTable.Rule r = new GlyphTable.ChainedCoverageSequenceRule(lookups, nig, igca, bgca, lgca);
      GlyphTable.RuleSet rs = new GlyphTable.HomogeneousRuleSet(new GlyphTable.Rule[]{r});
      GlyphTable.RuleSet[] rsa = new GlyphTable.RuleSet[]{rs};

      assert igca != null && igca.length > 0;

      this.seMapping = igca[0];
      this.seEntries.add(rsa);
   }

   private int readChainedContextualPosTable(int lookupType, int lookupFlags, long subtableOffset) throws IOException {
      this.in.seekSet(subtableOffset);
      int sf = this.in.readTTFUShort();
      if (sf == 1) {
         this.readChainedContextualPosTableFormat1(lookupType, lookupFlags, subtableOffset, sf);
      } else if (sf == 2) {
         this.readChainedContextualPosTableFormat2(lookupType, lookupFlags, subtableOffset, sf);
      } else {
         if (sf != 3) {
            throw new AdvancedTypographicTableFormatException("unsupported chained contextual positioning subtable format: " + sf);
         }

         this.readChainedContextualPosTableFormat3(lookupType, lookupFlags, subtableOffset, sf);
      }

      return sf;
   }

   private void readExtensionPosTableFormat1(int lookupType, int lookupFlags, int lookupSequence, int subtableSequence, long subtableOffset, int subtableFormat) throws IOException {
      String tableTag = "GPOS";
      this.in.seekSet(subtableOffset);
      this.in.skip(2L);
      int lt = this.in.readTTFUShort();
      long eo = this.in.readTTFULong();
      if (log.isDebugEnabled()) {
         log.debug(tableTag + " extension positioning subtable format: " + subtableFormat);
         log.debug(tableTag + " extension positioning lookup type: " + lt);
         log.debug(tableTag + " extension positioning lookup table offset: " + eo);
      }

      this.readGPOSSubtable(lt, lookupFlags, lookupSequence, subtableSequence, subtableOffset + eo);
   }

   private int readExtensionPosTable(int lookupType, int lookupFlags, int lookupSequence, int subtableSequence, long subtableOffset) throws IOException {
      this.in.seekSet(subtableOffset);
      int sf = this.in.readTTFUShort();
      if (sf == 1) {
         this.readExtensionPosTableFormat1(lookupType, lookupFlags, lookupSequence, subtableSequence, subtableOffset, sf);
         return sf;
      } else {
         throw new AdvancedTypographicTableFormatException("unsupported extension positioning subtable format: " + sf);
      }
   }

   private void readGPOSSubtable(int lookupType, int lookupFlags, int lookupSequence, int subtableSequence, long subtableOffset) throws IOException {
      this.initATSubState();
      int subtableFormat = -1;
      switch (lookupType) {
         case 1:
            subtableFormat = this.readSinglePosTable(lookupType, lookupFlags, subtableOffset);
            break;
         case 2:
            subtableFormat = this.readPairPosTable(lookupType, lookupFlags, subtableOffset);
            break;
         case 3:
            subtableFormat = this.readCursivePosTable(lookupType, lookupFlags, subtableOffset);
            break;
         case 4:
            subtableFormat = this.readMarkToBasePosTable(lookupType, lookupFlags, subtableOffset);
            break;
         case 5:
            subtableFormat = this.readMarkToLigaturePosTable(lookupType, lookupFlags, subtableOffset);
            break;
         case 6:
            subtableFormat = this.readMarkToMarkPosTable(lookupType, lookupFlags, subtableOffset);
            break;
         case 7:
            subtableFormat = this.readContextualPosTable(lookupType, lookupFlags, subtableOffset);
            break;
         case 8:
            subtableFormat = this.readChainedContextualPosTable(lookupType, lookupFlags, subtableOffset);
            break;
         case 9:
            subtableFormat = this.readExtensionPosTable(lookupType, lookupFlags, lookupSequence, subtableSequence, subtableOffset);
      }

      this.extractSESubState(2, lookupType, lookupFlags, lookupSequence, subtableSequence, subtableFormat);
      this.resetATSubState();
   }

   private void readLookupTable(OFTableName tableTag, int lookupSequence, long lookupTable) throws IOException {
      boolean isGSUB = tableTag.equals(OFTableName.GSUB);
      boolean isGPOS = tableTag.equals(OFTableName.GPOS);
      this.in.seekSet(lookupTable);
      int lt = this.in.readTTFUShort();
      int lf = this.in.readTTFUShort();
      int ns = this.in.readTTFUShort();
      if (log.isDebugEnabled()) {
         String lts;
         if (isGSUB) {
            lts = OTFAdvancedTypographicTableReader.GSUBLookupType.toString(lt);
         } else if (isGPOS) {
            lts = OTFAdvancedTypographicTableReader.GPOSLookupType.toString(lt);
         } else {
            lts = "?";
         }

         log.debug(tableTag + " lookup table type: " + lt + " (" + lts + ")");
         log.debug(tableTag + " lookup table flags: " + lf + " (" + OTFAdvancedTypographicTableReader.LookupFlag.toString(lf) + ")");
         log.debug(tableTag + " lookup table subtable count: " + ns);
      }

      int[] soa = new int[ns];

      int i;
      int so;
      for(i = 0; i < ns; ++i) {
         so = this.in.readTTFUShort();
         if (log.isDebugEnabled()) {
            log.debug(tableTag + " lookup table subtable offset: " + so);
         }

         soa[i] = so;
      }

      if ((lf & 16) != 0) {
         i = this.in.readTTFUShort();
         if (log.isDebugEnabled()) {
            log.debug(tableTag + " lookup table mark filter set: " + i);
         }
      }

      for(i = 0; i < ns; ++i) {
         so = soa[i];
         if (isGSUB) {
            this.readGSUBSubtable(lt, lf, lookupSequence, i, lookupTable + (long)so);
         } else if (isGPOS) {
            this.readGPOSSubtable(lt, lf, lookupSequence, i, lookupTable + (long)so);
         }
      }

   }

   private void readLookupList(OFTableName tableTag, long lookupList) throws IOException {
      this.in.seekSet(lookupList);
      int nl = this.in.readTTFUShort();
      if (log.isDebugEnabled()) {
         log.debug(tableTag + " lookup list record count: " + nl);
      }

      if (nl > 0) {
         int[] loa = new int[nl];
         int i = 0;

         int n;
         for(n = nl; i < n; ++i) {
            int lo = this.in.readTTFUShort();
            if (log.isDebugEnabled()) {
               log.debug(tableTag + " lookup table offset: " + lo);
            }

            loa[i] = lo;
         }

         i = 0;

         for(n = nl; i < n; ++i) {
            if (log.isDebugEnabled()) {
               log.debug(tableTag + " lookup index: " + i);
            }

            this.readLookupTable(tableTag, i, lookupList + (long)loa[i]);
         }
      }

   }

   private void readCommonLayoutTables(OFTableName tableTag, long scriptList, long featureList, long lookupList) throws IOException {
      if (scriptList > 0L) {
         this.readScriptList(tableTag, scriptList);
      }

      if (featureList > 0L) {
         this.readFeatureList(tableTag, featureList);
      }

      if (lookupList > 0L) {
         this.readLookupList(tableTag, lookupList);
      }

   }

   private void readGDEFClassDefTable(OFTableName tableTag, int lookupSequence, long subtableOffset) throws IOException {
      this.initATSubState();
      this.in.seekSet(subtableOffset);
      GlyphClassTable ct = this.readClassDefTable(tableTag + " glyph class definition table", subtableOffset);
      this.seMapping = ct;
      this.extractSESubState(5, 1, 0, lookupSequence, 0, 1);
      this.resetATSubState();
   }

   private void readGDEFAttachmentTable(OFTableName tableTag, int lookupSequence, long subtableOffset) throws IOException {
      this.initATSubState();
      this.in.seekSet(subtableOffset);
      int co = this.in.readTTFUShort();
      if (log.isDebugEnabled()) {
         log.debug(tableTag + " attachment point coverage table offset: " + co);
      }

      GlyphCoverageTable ct = this.readCoverageTable(tableTag + " attachment point coverage", subtableOffset + (long)co);
      this.seMapping = ct;
      this.extractSESubState(5, 2, 0, lookupSequence, 0, 1);
      this.resetATSubState();
   }

   private void readGDEFLigatureCaretTable(OFTableName tableTag, int lookupSequence, long subtableOffset) throws IOException {
      this.initATSubState();
      this.in.seekSet(subtableOffset);
      int co = this.in.readTTFUShort();
      int nl = this.in.readTTFUShort();
      int[] lgto = new int[nl];

      int i;
      for(i = 0; i < nl; ++i) {
         lgto[i] = this.in.readTTFUShort();
      }

      if (log.isDebugEnabled()) {
         log.debug(tableTag + " ligature caret coverage table offset: " + co);
         log.debug(tableTag + " ligature caret ligature glyph count: " + nl);

         for(i = 0; i < nl; ++i) {
            log.debug(tableTag + " ligature glyph table offset[" + i + "]: " + lgto[i]);
         }
      }

      GlyphCoverageTable ct = this.readCoverageTable(tableTag + " ligature caret coverage", subtableOffset + (long)co);
      this.seMapping = ct;
      this.extractSESubState(5, 3, 0, lookupSequence, 0, 1);
      this.resetATSubState();
   }

   private void readGDEFMarkAttachmentTable(OFTableName tableTag, int lookupSequence, long subtableOffset) throws IOException {
      this.initATSubState();
      this.in.seekSet(subtableOffset);
      GlyphClassTable ct = this.readClassDefTable(tableTag + " glyph class definition table", subtableOffset);
      this.seMapping = ct;
      this.extractSESubState(5, 4, 0, lookupSequence, 0, 1);
      this.resetATSubState();
   }

   private void readGDEFMarkGlyphsTableFormat1(OFTableName tableTag, int lookupSequence, long subtableOffset, int subtableFormat) throws IOException {
      this.initATSubState();
      this.in.seekSet(subtableOffset);
      this.in.skip(2L);
      int nmc = this.in.readTTFUShort();
      long[] mso = new long[nmc];

      int i;
      for(i = 0; i < nmc; ++i) {
         mso[i] = this.in.readTTFULong();
      }

      if (log.isDebugEnabled()) {
         log.debug(tableTag + " mark set subtable format: " + subtableFormat + " (glyph sets)");
         log.debug(tableTag + " mark set class count: " + nmc);

         for(i = 0; i < nmc; ++i) {
            log.debug(tableTag + " mark set coverage table offset[" + i + "]: " + mso[i]);
         }
      }

      GlyphCoverageTable[] msca = new GlyphCoverageTable[nmc];

      for(int i = 0; i < nmc; ++i) {
         msca[i] = this.readCoverageTable(tableTag + " mark set coverage[" + i + "]", subtableOffset + mso[i]);
      }

      GlyphClassTable ct = GlyphClassTable.createClassTable(Arrays.asList(msca));
      this.seMapping = ct;
      this.extractSESubState(5, 4, 0, lookupSequence, 0, 1);
      this.resetATSubState();
   }

   private void readGDEFMarkGlyphsTable(OFTableName tableTag, int lookupSequence, long subtableOffset) throws IOException {
      this.in.seekSet(subtableOffset);
      int sf = this.in.readTTFUShort();
      if (sf == 1) {
         this.readGDEFMarkGlyphsTableFormat1(tableTag, lookupSequence, subtableOffset, sf);
      } else {
         throw new AdvancedTypographicTableFormatException("unsupported mark glyph sets subtable format: " + sf);
      }
   }

   private void readGDEF() throws IOException {
      OFTableName tableTag = OFTableName.GDEF;
      this.initATState();
      OFDirTabEntry dirTab = this.otf.getDirectoryEntry(tableTag);
      if (this.gdef != null) {
         if (log.isDebugEnabled()) {
            log.debug(tableTag + ": ignoring duplicate table");
         }
      } else if (dirTab != null) {
         this.otf.seekTab(this.in, tableTag, 0L);
         long version = this.in.readTTFULong();
         if (log.isDebugEnabled()) {
            log.debug(tableTag + " version: " + version / 65536L + "." + version % 65536L);
         }

         int cdo = this.in.readTTFUShort();
         int apo = this.in.readTTFUShort();
         int lco = this.in.readTTFUShort();
         int mao = this.in.readTTFUShort();
         int mgo;
         if (version >= 65538L) {
            mgo = this.in.readTTFUShort();
         } else {
            mgo = 0;
         }

         if (log.isDebugEnabled()) {
            log.debug(tableTag + " glyph class definition table offset: " + cdo);
            log.debug(tableTag + " attachment point list offset: " + apo);
            log.debug(tableTag + " ligature caret list offset: " + lco);
            log.debug(tableTag + " mark attachment class definition table offset: " + mao);
            log.debug(tableTag + " mark glyph set definitions table offset: " + mgo);
         }

         int seqno = 0;
         long to = dirTab.getOffset();
         if (cdo != 0) {
            this.readGDEFClassDefTable(tableTag, seqno++, to + (long)cdo);
         }

         if (apo != 0) {
            this.readGDEFAttachmentTable(tableTag, seqno++, to + (long)apo);
         }

         if (lco != 0) {
            this.readGDEFLigatureCaretTable(tableTag, seqno++, to + (long)lco);
         }

         if (mao != 0) {
            this.readGDEFMarkAttachmentTable(tableTag, seqno++, to + (long)mao);
         }

         if (mgo != 0) {
            this.readGDEFMarkGlyphsTable(tableTag, seqno++, to + (long)mgo);
         }

         GlyphDefinitionTable gdef;
         if ((gdef = this.constructGDEF()) != null) {
            this.gdef = gdef;
         }
      }

   }

   private void readGSUB() throws IOException {
      OFTableName tableTag = OFTableName.GSUB;
      this.initATState();
      OFDirTabEntry dirTab = this.otf.getDirectoryEntry(tableTag);
      if (this.gpos != null) {
         if (log.isDebugEnabled()) {
            log.debug(tableTag + ": ignoring duplicate table");
         }
      } else if (dirTab != null) {
         this.otf.seekTab(this.in, tableTag, 0L);
         int version = this.in.readTTFLong();
         if (log.isDebugEnabled()) {
            log.debug(tableTag + " version: " + version / 65536 + "." + version % 65536);
         }

         int slo = this.in.readTTFUShort();
         int flo = this.in.readTTFUShort();
         int llo = this.in.readTTFUShort();
         if (log.isDebugEnabled()) {
            log.debug(tableTag + " script list offset: " + slo);
            log.debug(tableTag + " feature list offset: " + flo);
            log.debug(tableTag + " lookup list offset: " + llo);
         }

         long to = dirTab.getOffset();
         this.readCommonLayoutTables(tableTag, to + (long)slo, to + (long)flo, to + (long)llo);
         GlyphSubstitutionTable gsub;
         if ((gsub = this.constructGSUB()) != null) {
            this.gsub = gsub;
         }
      }

   }

   private void readGPOS() throws IOException {
      OFTableName tableTag = OFTableName.GPOS;
      this.initATState();
      OFDirTabEntry dirTab = this.otf.getDirectoryEntry(tableTag);
      if (this.gpos != null) {
         if (log.isDebugEnabled()) {
            log.debug(tableTag + ": ignoring duplicate table");
         }
      } else if (dirTab != null) {
         this.otf.seekTab(this.in, tableTag, 0L);
         int version = this.in.readTTFLong();
         if (log.isDebugEnabled()) {
            log.debug(tableTag + " version: " + version / 65536 + "." + version % 65536);
         }

         int slo = this.in.readTTFUShort();
         int flo = this.in.readTTFUShort();
         int llo = this.in.readTTFUShort();
         if (log.isDebugEnabled()) {
            log.debug(tableTag + " script list offset: " + slo);
            log.debug(tableTag + " feature list offset: " + flo);
            log.debug(tableTag + " lookup list offset: " + llo);
         }

         long to = dirTab.getOffset();
         this.readCommonLayoutTables(tableTag, to + (long)slo, to + (long)flo, to + (long)llo);
         GlyphPositioningTable gpos;
         if ((gpos = this.constructGPOS()) != null) {
            this.gpos = gpos;
         }
      }

   }

   private GlyphDefinitionTable constructGDEF() {
      GlyphDefinitionTable gdef = null;
      List subtables;
      if ((subtables = this.constructGDEFSubtables()) != null && subtables.size() > 0) {
         gdef = new GlyphDefinitionTable(subtables, this.processors);
      }

      this.resetATState();
      return gdef;
   }

   private GlyphSubstitutionTable constructGSUB() {
      GlyphSubstitutionTable gsub = null;
      Map lookups;
      List subtables;
      if ((lookups = this.constructLookups()) != null && (subtables = this.constructGSUBSubtables()) != null && lookups.size() > 0 && subtables.size() > 0) {
         gsub = new GlyphSubstitutionTable(this.gdef, lookups, subtables, this.processors);
      }

      this.resetATState();
      return gsub;
   }

   private GlyphPositioningTable constructGPOS() {
      GlyphPositioningTable gpos = null;
      Map lookups;
      List subtables;
      if ((lookups = this.constructLookups()) != null && (subtables = this.constructGPOSSubtables()) != null && lookups.size() > 0 && subtables.size() > 0) {
         gpos = new GlyphPositioningTable(this.gdef, lookups, subtables, this.processors);
      }

      this.resetATState();
      return gpos;
   }

   private void constructLookupsFeature(Map lookups, String st, String lt, String fid) {
      Object[] fp = (Object[])((Object[])this.seFeatures.get(fid));
      if (fp != null) {
         assert fp.length == 2;

         String ft = (String)fp[0];
         List lul = (List)fp[1];
         if (ft != null && lul != null && lul.size() > 0) {
            GlyphTable.LookupSpec ls = new GlyphTable.LookupSpec(st, lt, ft);
            lookups.put(ls, lul);
         }
      }

   }

   private void constructLookupsFeatures(Map lookups, String st, String lt, List fids) {
      Iterator var5 = fids.iterator();

      while(var5.hasNext()) {
         Object fid1 = var5.next();
         String fid = (String)fid1;
         this.constructLookupsFeature(lookups, st, lt, fid);
      }

   }

   private void constructLookupsLanguage(Map lookups, String st, String lt, Map languages) {
      Object[] lp = (Object[])((Object[])languages.get(lt));
      if (lp != null) {
         assert lp.length == 2;

         if (lp[0] != null) {
            this.constructLookupsFeature(lookups, st, lt, (String)lp[0]);
         }

         if (lp[1] != null) {
            this.constructLookupsFeatures(lookups, st, lt, (List)lp[1]);
         }
      }

   }

   private void constructLookupsLanguages(Map lookups, String st, List ll, Map languages) {
      Iterator var5 = ll.iterator();

      while(var5.hasNext()) {
         Object aLl = var5.next();
         String lt = (String)aLl;
         this.constructLookupsLanguage(lookups, st, lt, languages);
      }

   }

   private Map constructLookups() {
      Map lookups = new LinkedHashMap();
      Iterator var2 = this.seScripts.keySet().iterator();

      while(var2.hasNext()) {
         Object o = var2.next();
         String st = (String)o;
         Object[] sp = (Object[])((Object[])this.seScripts.get(st));
         if (sp != null) {
            assert sp.length == 3;

            Map languages = (Map)sp[2];
            if (sp[0] != null) {
               this.constructLookupsLanguage(lookups, st, (String)sp[0], languages);
            }

            if (sp[1] != null) {
               this.constructLookupsLanguages(lookups, st, (List)sp[1], languages);
            }
         }
      }

      return lookups;
   }

   private List constructGDEFSubtables() {
      List subtables = new ArrayList();
      if (this.seSubtables != null) {
         Iterator var2 = this.seSubtables.iterator();

         while(var2.hasNext()) {
            Object seSubtable = var2.next();
            Object[] stp = (Object[])((Object[])seSubtable);
            GlyphSubtable st;
            if ((st = this.constructGDEFSubtable(stp)) != null) {
               subtables.add(st);
            }
         }
      }

      return subtables;
   }

   private GlyphSubtable constructGDEFSubtable(Object[] stp) {
      GlyphSubtable st = null;

      assert stp != null && stp.length == 8;

      Integer tt = (Integer)stp[0];
      Integer lt = (Integer)stp[1];
      Integer ln = (Integer)stp[2];
      Integer lf = (Integer)stp[3];
      Integer sn = (Integer)stp[4];
      Integer sf = (Integer)stp[5];
      GlyphMappingTable mapping = (GlyphMappingTable)stp[6];
      List entries = (List)stp[7];
      if (tt == 5) {
         int type = OTFAdvancedTypographicTableReader.GDEFLookupType.getSubtableType(lt);
         String lid = "lu" + ln;
         int sequence = sn;
         int flags = lf;
         int format = sf;
         st = GlyphDefinitionTable.createSubtable(type, lid, sequence, flags, format, mapping, entries);
      }

      return st;
   }

   private List constructGSUBSubtables() {
      List subtables = new ArrayList();
      if (this.seSubtables != null) {
         Iterator var2 = this.seSubtables.iterator();

         while(var2.hasNext()) {
            Object seSubtable = var2.next();
            Object[] stp = (Object[])((Object[])seSubtable);
            GlyphSubtable st;
            if ((st = this.constructGSUBSubtable(stp)) != null) {
               subtables.add(st);
            }
         }
      }

      return subtables;
   }

   private GlyphSubtable constructGSUBSubtable(Object[] stp) {
      GlyphSubtable st = null;

      assert stp != null && stp.length == 8;

      Integer tt = (Integer)stp[0];
      Integer lt = (Integer)stp[1];
      Integer ln = (Integer)stp[2];
      Integer lf = (Integer)stp[3];
      Integer sn = (Integer)stp[4];
      Integer sf = (Integer)stp[5];
      GlyphCoverageTable coverage = (GlyphCoverageTable)stp[6];
      List entries = (List)stp[7];
      if (tt == 1) {
         int type = OTFAdvancedTypographicTableReader.GSUBLookupType.getSubtableType(lt);
         String lid = "lu" + ln;
         int sequence = sn;
         int flags = lf;
         int format = sf;
         st = GlyphSubstitutionTable.createSubtable(type, lid, sequence, flags, format, coverage, entries);
      }

      return st;
   }

   private List constructGPOSSubtables() {
      List subtables = new ArrayList();
      if (this.seSubtables != null) {
         Iterator var2 = this.seSubtables.iterator();

         while(var2.hasNext()) {
            Object seSubtable = var2.next();
            Object[] stp = (Object[])((Object[])seSubtable);
            GlyphSubtable st;
            if ((st = this.constructGPOSSubtable(stp)) != null) {
               subtables.add(st);
            }
         }
      }

      return subtables;
   }

   private GlyphSubtable constructGPOSSubtable(Object[] stp) {
      GlyphSubtable st = null;

      assert stp != null && stp.length == 8;

      Integer tt = (Integer)stp[0];
      Integer lt = (Integer)stp[1];
      Integer ln = (Integer)stp[2];
      Integer lf = (Integer)stp[3];
      Integer sn = (Integer)stp[4];
      Integer sf = (Integer)stp[5];
      GlyphCoverageTable coverage = (GlyphCoverageTable)stp[6];
      List entries = (List)stp[7];
      if (tt == 2) {
         int type = OTFAdvancedTypographicTableReader.GSUBLookupType.getSubtableType(lt);
         String lid = "lu" + ln;
         int sequence = sn;
         int flags = lf;
         int format = sf;
         st = GlyphPositioningTable.createSubtable(type, lid, sequence, flags, format, coverage, entries);
      }

      return st;
   }

   private void initATState() {
      this.seScripts = new LinkedHashMap();
      this.seLanguages = new LinkedHashMap();
      this.seFeatures = new LinkedHashMap();
      this.seSubtables = new ArrayList();
      this.resetATSubState();
   }

   private void resetATState() {
      this.seScripts = null;
      this.seLanguages = null;
      this.seFeatures = null;
      this.seSubtables = null;
      this.resetATSubState();
   }

   private void initATSubState() {
      this.seMapping = null;
      this.seEntries = new ArrayList();
   }

   private void extractSESubState(int tableType, int lookupType, int lookupFlags, int lookupSequence, int subtableSequence, int subtableFormat) {
      if (this.seEntries != null && (tableType == 5 || this.seEntries.size() > 0) && this.seSubtables != null) {
         Integer tt = tableType;
         Integer lt = lookupType;
         Integer ln = lookupSequence;
         Integer lf = lookupFlags;
         Integer sn = subtableSequence;
         Integer sf = subtableFormat;
         this.seSubtables.add(new Object[]{tt, lt, ln, lf, sn, sf, this.seMapping, this.seEntries});
      }

   }

   private void resetATSubState() {
      this.seMapping = null;
      this.seEntries = null;
   }

   private void resetATStateAll() {
      this.resetATState();
      this.gdef = null;
      this.gsub = null;
      this.gpos = null;
   }

   private String toString(int[] ia) {
      StringBuffer sb = new StringBuffer();
      if (ia != null && ia.length != 0) {
         boolean first = true;
         int[] var4 = ia;
         int var5 = ia.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            int anIa = var4[var6];
            if (!first) {
               sb.append(' ');
            } else {
               first = false;
            }

            sb.append(anIa);
         }
      } else {
         sb.append('-');
      }

      return sb.toString();
   }

   static final class LookupFlag {
      static final int RIGHT_TO_LEFT = 1;
      static final int IGNORE_BASE_GLYPHS = 2;
      static final int IGNORE_LIGATURE = 4;
      static final int IGNORE_MARKS = 8;
      static final int USE_MARK_FILTERING_SET = 16;
      static final int MARK_ATTACHMENT_TYPE = 65280;

      private LookupFlag() {
      }

      public static String toString(int flags) {
         StringBuffer sb = new StringBuffer();
         boolean first = true;
         if ((flags & 1) != 0) {
            if (first) {
               first = false;
            } else {
               sb.append('|');
            }

            sb.append("RightToLeft");
         }

         if ((flags & 2) != 0) {
            if (first) {
               first = false;
            } else {
               sb.append('|');
            }

            sb.append("IgnoreBaseGlyphs");
         }

         if ((flags & 4) != 0) {
            if (first) {
               first = false;
            } else {
               sb.append('|');
            }

            sb.append("IgnoreLigature");
         }

         if ((flags & 8) != 0) {
            if (first) {
               first = false;
            } else {
               sb.append('|');
            }

            sb.append("IgnoreMarks");
         }

         if ((flags & 16) != 0) {
            if (first) {
               first = false;
            } else {
               sb.append('|');
            }

            sb.append("UseMarkFilteringSet");
         }

         if (sb.length() == 0) {
            sb.append('-');
         }

         return sb.toString();
      }
   }

   static final class GPOSLookupType {
      static final int SINGLE = 1;
      static final int PAIR = 2;
      static final int CURSIVE = 3;
      static final int MARK_TO_BASE = 4;
      static final int MARK_TO_LIGATURE = 5;
      static final int MARK_TO_MARK = 6;
      static final int CONTEXTUAL = 7;
      static final int CHAINED_CONTEXTUAL = 8;
      static final int EXTENSION = 9;

      private GPOSLookupType() {
      }

      public static String toString(int type) {
         String s;
         switch (type) {
            case 1:
               s = "Single";
               break;
            case 2:
               s = "Pair";
               break;
            case 3:
               s = "Cursive";
               break;
            case 4:
               s = "MarkToBase";
               break;
            case 5:
               s = "MarkToLigature";
               break;
            case 6:
               s = "MarkToMark";
               break;
            case 7:
               s = "Contextual";
               break;
            case 8:
               s = "ChainedContextual";
               break;
            case 9:
               s = "Extension";
               break;
            default:
               s = "?";
         }

         return s;
      }
   }

   static final class GSUBLookupType {
      static final int SINGLE = 1;
      static final int MULTIPLE = 2;
      static final int ALTERNATE = 3;
      static final int LIGATURE = 4;
      static final int CONTEXTUAL = 5;
      static final int CHAINED_CONTEXTUAL = 6;
      static final int EXTENSION = 7;
      static final int REVERSE_CHAINED_SINGLE = 8;

      private GSUBLookupType() {
      }

      public static int getSubtableType(int lt) {
         byte st;
         switch (lt) {
            case 1:
               st = 1;
               break;
            case 2:
               st = 2;
               break;
            case 3:
               st = 3;
               break;
            case 4:
               st = 4;
               break;
            case 5:
               st = 5;
               break;
            case 6:
               st = 6;
               break;
            case 7:
               st = 7;
               break;
            case 8:
               st = 8;
               break;
            default:
               st = -1;
         }

         return st;
      }

      public static String toString(int type) {
         String s;
         switch (type) {
            case 1:
               s = "Single";
               break;
            case 2:
               s = "Multiple";
               break;
            case 3:
               s = "Alternate";
               break;
            case 4:
               s = "Ligature";
               break;
            case 5:
               s = "Contextual";
               break;
            case 6:
               s = "ChainedContextual";
               break;
            case 7:
               s = "Extension";
               break;
            case 8:
               s = "ReverseChainedSingle";
               break;
            default:
               s = "?";
         }

         return s;
      }
   }

   static final class GDEFLookupType {
      static final int GLYPH_CLASS = 1;
      static final int ATTACHMENT_POINT = 2;
      static final int LIGATURE_CARET = 3;
      static final int MARK_ATTACHMENT = 4;

      private GDEFLookupType() {
      }

      public static int getSubtableType(int lt) {
         byte st;
         switch (lt) {
            case 1:
               st = 1;
               break;
            case 2:
               st = 2;
               break;
            case 3:
               st = 3;
               break;
            case 4:
               st = 4;
               break;
            default:
               st = -1;
         }

         return st;
      }

      public static String toString(int type) {
         String s;
         switch (type) {
            case 1:
               s = "GlyphClass";
               break;
            case 2:
               s = "AttachmentPoint";
               break;
            case 3:
               s = "LigatureCaret";
               break;
            case 4:
               s = "MarkAttachment";
               break;
            default:
               s = "?";
         }

         return s;
      }
   }
}
