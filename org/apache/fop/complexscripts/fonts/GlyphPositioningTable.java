package org.apache.fop.complexscripts.fonts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.complexscripts.scripts.ScriptProcessor;
import org.apache.fop.complexscripts.util.GlyphSequence;
import org.apache.fop.complexscripts.util.GlyphTester;

public class GlyphPositioningTable extends GlyphTable {
   private static final Log log = LogFactory.getLog(GlyphPositioningTable.class);
   public static final int GPOS_LOOKUP_TYPE_SINGLE = 1;
   public static final int GPOS_LOOKUP_TYPE_PAIR = 2;
   public static final int GPOS_LOOKUP_TYPE_CURSIVE = 3;
   public static final int GPOS_LOOKUP_TYPE_MARK_TO_BASE = 4;
   public static final int GPOS_LOOKUP_TYPE_MARK_TO_LIGATURE = 5;
   public static final int GPOS_LOOKUP_TYPE_MARK_TO_MARK = 6;
   public static final int GPOS_LOOKUP_TYPE_CONTEXTUAL = 7;
   public static final int GPOS_LOOKUP_TYPE_CHAINED_CONTEXTUAL = 8;
   public static final int GPOS_LOOKUP_TYPE_EXTENSION_POSITIONING = 9;

   public GlyphPositioningTable(GlyphDefinitionTable gdef, Map lookups, List subtables, Map processors) {
      super(gdef, lookups, processors);
      if (subtables != null && subtables.size() != 0) {
         Iterator var5 = subtables.iterator();

         while(var5.hasNext()) {
            Object o = var5.next();
            if (!(o instanceof GlyphPositioningSubtable)) {
               throw new AdvancedTypographicTableFormatException("subtable must be a glyph positioning subtable");
            }

            this.addSubtable((GlyphSubtable)o);
         }

         this.freezeSubtables();
      } else {
         throw new AdvancedTypographicTableFormatException("subtables must be non-empty");
      }
   }

   public static int getLookupTypeFromName(String name) {
      String s = name.toLowerCase();
      byte t;
      if ("single".equals(s)) {
         t = 1;
      } else if ("pair".equals(s)) {
         t = 2;
      } else if ("cursive".equals(s)) {
         t = 3;
      } else if ("marktobase".equals(s)) {
         t = 4;
      } else if ("marktoligature".equals(s)) {
         t = 5;
      } else if ("marktomark".equals(s)) {
         t = 6;
      } else if ("contextual".equals(s)) {
         t = 7;
      } else if ("chainedcontextual".equals(s)) {
         t = 8;
      } else if ("extensionpositioning".equals(s)) {
         t = 9;
      } else {
         t = -1;
      }

      return t;
   }

   public static String getLookupTypeName(int type) {
      String tn;
      switch (type) {
         case 1:
            tn = "single";
            break;
         case 2:
            tn = "pair";
            break;
         case 3:
            tn = "cursive";
            break;
         case 4:
            tn = "marktobase";
            break;
         case 5:
            tn = "marktoligature";
            break;
         case 6:
            tn = "marktomark";
            break;
         case 7:
            tn = "contextual";
            break;
         case 8:
            tn = "chainedcontextual";
            break;
         case 9:
            tn = "extensionpositioning";
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
            st = GlyphPositioningTable.SingleSubtable.create(id, sequence, flags, format, coverage, entries);
            break;
         case 2:
            st = GlyphPositioningTable.PairSubtable.create(id, sequence, flags, format, coverage, entries);
            break;
         case 3:
            st = GlyphPositioningTable.CursiveSubtable.create(id, sequence, flags, format, coverage, entries);
            break;
         case 4:
            st = GlyphPositioningTable.MarkToBaseSubtable.create(id, sequence, flags, format, coverage, entries);
            break;
         case 5:
            st = GlyphPositioningTable.MarkToLigatureSubtable.create(id, sequence, flags, format, coverage, entries);
            break;
         case 6:
            st = GlyphPositioningTable.MarkToMarkSubtable.create(id, sequence, flags, format, coverage, entries);
            break;
         case 7:
            st = GlyphPositioningTable.ContextualSubtable.create(id, sequence, flags, format, coverage, entries);
            break;
         case 8:
            st = GlyphPositioningTable.ChainedContextualSubtable.create(id, sequence, flags, format, coverage, entries);
      }

      return st;
   }

   public static GlyphSubtable createSubtable(int type, String id, int sequence, int flags, int format, List coverage, List entries) {
      return createSubtable(type, id, sequence, flags, format, GlyphCoverageTable.createCoverageTable(coverage), entries);
   }

   public boolean position(GlyphSequence gs, String script, String language, int fontSize, int[] widths, int[][] adjustments) {
      Map lookups = this.matchLookups(script, language, "*");
      if (lookups != null && lookups.size() > 0) {
         ScriptProcessor sp = ScriptProcessor.getInstance(script, this.processors);
         return sp.position(this, gs, script, language, fontSize, lookups, widths, adjustments);
      } else {
         return false;
      }
   }

   public static class MarkAnchor extends Anchor {
      private final int markClass;

      public MarkAnchor(int markClass, Anchor a) {
         super(a);
         this.markClass = markClass;
      }

      public int getMarkClass() {
         return this.markClass;
      }

      public String toString() {
         return "{ markClass = " + this.markClass + ", anchor = " + super.toString() + " }";
      }
   }

   public static class Anchor {
      private final int x;
      private final int y;
      private final int anchorPoint;
      private final DeviceTable xDevice;
      private final DeviceTable yDevice;

      public Anchor(int x, int y) {
         this(x, y, -1, (DeviceTable)null, (DeviceTable)null);
      }

      public Anchor(int x, int y, int anchorPoint) {
         this(x, y, anchorPoint, (DeviceTable)null, (DeviceTable)null);
      }

      public Anchor(int x, int y, DeviceTable xDevice, DeviceTable yDevice) {
         this(x, y, -1, xDevice, yDevice);
      }

      protected Anchor(Anchor a) {
         this(a.x, a.y, a.anchorPoint, a.xDevice, a.yDevice);
      }

      private Anchor(int x, int y, int anchorPoint, DeviceTable xDevice, DeviceTable yDevice) {
         assert anchorPoint >= 0 || anchorPoint == -1;

         this.x = x;
         this.y = y;
         this.anchorPoint = anchorPoint;
         this.xDevice = xDevice;
         this.yDevice = yDevice;
      }

      public int getX() {
         return this.x;
      }

      public int getY() {
         return this.y;
      }

      public int getAnchorPoint() {
         return this.anchorPoint;
      }

      public DeviceTable getXDevice() {
         return this.xDevice;
      }

      public DeviceTable getYDevice() {
         return this.yDevice;
      }

      public Value getAlignmentAdjustment(Anchor a) {
         assert a != null;

         return new Value(this.x - a.x, this.y - a.y, 0, 0, (DeviceTable)null, (DeviceTable)null, (DeviceTable)null, (DeviceTable)null);
      }

      public String toString() {
         StringBuffer sb = new StringBuffer();
         sb.append("{ [" + this.x + "," + this.y + "]");
         if (this.anchorPoint != -1) {
            sb.append(", anchorPoint = " + this.anchorPoint);
         }

         if (this.xDevice != null) {
            sb.append(", xDevice = " + this.xDevice);
         }

         if (this.yDevice != null) {
            sb.append(", yDevice = " + this.yDevice);
         }

         sb.append(" }");
         return sb.toString();
      }
   }

   public static class PairValues {
      private final int glyph;
      private final Value value1;
      private final Value value2;

      public PairValues(int glyph, Value value1, Value value2) {
         assert glyph >= 0;

         this.glyph = glyph;
         this.value1 = value1;
         this.value2 = value2;
      }

      public int getGlyph() {
         return this.glyph;
      }

      public Value getValue1() {
         return this.value1;
      }

      public Value getValue2() {
         return this.value2;
      }

      public String toString() {
         StringBuffer sb = new StringBuffer();
         boolean first = true;
         sb.append("{ ");
         if (this.glyph != 0) {
            if (!first) {
               sb.append(", ");
            } else {
               first = false;
            }

            sb.append("glyph = " + this.glyph);
         }

         if (this.value1 != null) {
            if (!first) {
               sb.append(", ");
            } else {
               first = false;
            }

            sb.append("value1 = " + this.value1);
         }

         if (this.value2 != null) {
            if (!first) {
               sb.append(", ");
            } else {
               first = false;
            }

            sb.append("value2 = " + this.value2);
         }

         sb.append(" }");
         return sb.toString();
      }
   }

   public static class Value {
      public static final int X_PLACEMENT = 1;
      public static final int Y_PLACEMENT = 2;
      public static final int X_ADVANCE = 4;
      public static final int Y_ADVANCE = 8;
      public static final int X_PLACEMENT_DEVICE = 16;
      public static final int Y_PLACEMENT_DEVICE = 32;
      public static final int X_ADVANCE_DEVICE = 64;
      public static final int Y_ADVANCE_DEVICE = 128;
      public static final int IDX_X_PLACEMENT = 0;
      public static final int IDX_Y_PLACEMENT = 1;
      public static final int IDX_X_ADVANCE = 2;
      public static final int IDX_Y_ADVANCE = 3;
      private int xPlacement;
      private int yPlacement;
      private int xAdvance;
      private int yAdvance;
      private final DeviceTable xPlaDevice;
      private final DeviceTable yPlaDevice;
      private final DeviceTable xAdvDevice;
      private final DeviceTable yAdvDevice;

      public Value(int xPlacement, int yPlacement, int xAdvance, int yAdvance, DeviceTable xPlaDevice, DeviceTable yPlaDevice, DeviceTable xAdvDevice, DeviceTable yAdvDevice) {
         this.xPlacement = xPlacement;
         this.yPlacement = yPlacement;
         this.xAdvance = xAdvance;
         this.yAdvance = yAdvance;
         this.xPlaDevice = xPlaDevice;
         this.yPlaDevice = yPlaDevice;
         this.xAdvDevice = xAdvDevice;
         this.yAdvDevice = yAdvDevice;
      }

      public int getXPlacement() {
         return this.xPlacement;
      }

      public int getYPlacement() {
         return this.yPlacement;
      }

      public int getXAdvance() {
         return this.xAdvance;
      }

      public int getYAdvance() {
         return this.yAdvance;
      }

      public DeviceTable getXPlaDevice() {
         return this.xPlaDevice;
      }

      public DeviceTable getYPlaDevice() {
         return this.yPlaDevice;
      }

      public DeviceTable getXAdvDevice() {
         return this.xAdvDevice;
      }

      public DeviceTable getYAdvDevice() {
         return this.yAdvDevice;
      }

      public void adjust(int xPlacement, int yPlacement, int xAdvance, int yAdvance) {
         this.xPlacement += xPlacement;
         this.yPlacement += yPlacement;
         this.xAdvance += xAdvance;
         this.yAdvance += yAdvance;
      }

      public boolean adjust(int[] adjustments, int fontSize) {
         boolean adjust = false;
         int dv;
         if ((dv = this.xPlacement) != 0) {
            adjustments[0] += dv;
            adjust = true;
         }

         if ((dv = this.yPlacement) != 0) {
            adjustments[1] += dv;
            adjust = true;
         }

         if ((dv = this.xAdvance) != 0) {
            adjustments[2] += dv;
            adjust = true;
         }

         if ((dv = this.yAdvance) != 0) {
            adjustments[3] += dv;
            adjust = true;
         }

         if (fontSize != 0) {
            DeviceTable dt;
            if ((dt = this.xPlaDevice) != null && (dv = dt.findAdjustment(fontSize)) != 0) {
               adjustments[0] += dv;
               adjust = true;
            }

            if ((dt = this.yPlaDevice) != null && (dv = dt.findAdjustment(fontSize)) != 0) {
               adjustments[1] += dv;
               adjust = true;
            }

            if ((dt = this.xAdvDevice) != null && (dv = dt.findAdjustment(fontSize)) != 0) {
               adjustments[2] += dv;
               adjust = true;
            }

            if ((dt = this.yAdvDevice) != null && (dv = dt.findAdjustment(fontSize)) != 0) {
               adjustments[3] += dv;
               adjust = true;
            }
         }

         return adjust;
      }

      public String toString() {
         StringBuffer sb = new StringBuffer();
         boolean first = true;
         sb.append("{ ");
         if (this.xPlacement != 0) {
            if (!first) {
               sb.append(", ");
            } else {
               first = false;
            }

            sb.append("xPlacement = " + this.xPlacement);
         }

         if (this.yPlacement != 0) {
            if (!first) {
               sb.append(", ");
            } else {
               first = false;
            }

            sb.append("yPlacement = " + this.yPlacement);
         }

         if (this.xAdvance != 0) {
            if (!first) {
               sb.append(", ");
            } else {
               first = false;
            }

            sb.append("xAdvance = " + this.xAdvance);
         }

         if (this.yAdvance != 0) {
            if (!first) {
               sb.append(", ");
            } else {
               first = false;
            }

            sb.append("yAdvance = " + this.yAdvance);
         }

         if (this.xPlaDevice != null) {
            if (!first) {
               sb.append(", ");
            } else {
               first = false;
            }

            sb.append("xPlaDevice = " + this.xPlaDevice);
         }

         if (this.yPlaDevice != null) {
            if (!first) {
               sb.append(", ");
            } else {
               first = false;
            }

            sb.append("xPlaDevice = " + this.yPlaDevice);
         }

         if (this.xAdvDevice != null) {
            if (!first) {
               sb.append(", ");
            } else {
               first = false;
            }

            sb.append("xAdvDevice = " + this.xAdvDevice);
         }

         if (this.yAdvDevice != null) {
            if (!first) {
               sb.append(", ");
            } else {
               first = false;
            }

            sb.append("xAdvDevice = " + this.yAdvDevice);
         }

         sb.append(" }");
         return sb.toString();
      }
   }

   public static class DeviceTable {
      private final int startSize;
      private final int endSize;
      private final int[] deltas;

      public DeviceTable(int startSize, int endSize, int[] deltas) {
         assert startSize >= 0;

         assert startSize <= endSize;

         assert deltas != null;

         assert deltas.length == endSize - startSize + 1;

         this.startSize = startSize;
         this.endSize = endSize;
         this.deltas = deltas;
      }

      public int getStartSize() {
         return this.startSize;
      }

      public int getEndSize() {
         return this.endSize;
      }

      public int[] getDeltas() {
         return this.deltas;
      }

      public int findAdjustment(int fontSize) {
         int fs = fontSize / 1000;
         if (fs < this.startSize) {
            return 0;
         } else {
            return fs <= this.endSize ? this.deltas[fs - this.startSize] * 1000 : 0;
         }
      }

      public String toString() {
         return "{ start = " + this.startSize + ", end = " + this.endSize + ", deltas = " + Arrays.toString(this.deltas) + "}";
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

      public GlyphTable.RuleLookup[] getLookups(int ci, int gi, GlyphPositioningState ps, int[] rv) {
         assert ps != null;

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
                     if (this.matches(ps, igca, 0, rv)) {
                        GlyphCoverageTable[] bgca = cr.getBacktrackCoverages();
                        if (this.matches(ps, bgca, -1, (int[])null)) {
                           GlyphCoverageTable[] lgca = cr.getLookaheadCoverages();
                           if (this.matches(ps, lgca, rv[0], (int[])null)) {
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

      private boolean matches(GlyphPositioningState ps, GlyphCoverageTable[] gca, int offset, int[] rv) {
         return GlyphPositioningTable.ContextualSubtableFormat3.matches(ps, gca, offset, rv);
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

      public void resolveLookupReferences(Map lookupTables) {
         GlyphTable.resolveLookupReferences(this.rsa, lookupTables);
      }

      public GlyphTable.RuleLookup[] getLookups(int ci, int gi, GlyphPositioningState ps, int[] rv) {
         assert ps != null;

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
                     int[] ica = cr.getClasses(this.icdt.getClassIndex(gi, ps.getClassMatchSet(gi)));
                     if (this.matches(ps, this.icdt, ica, 0, rv)) {
                        int[] bca = cr.getBacktrackClasses();
                        if (this.matches(ps, this.bcdt, bca, -1, (int[])null)) {
                           int[] lca = cr.getLookaheadClasses();
                           if (this.matches(ps, this.lcdt, lca, rv[0], (int[])null)) {
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

      private boolean matches(GlyphPositioningState ps, GlyphClassTable cdt, int[] classes, int offset, int[] rv) {
         return GlyphPositioningTable.ContextualSubtableFormat2.matches(ps, cdt, classes, offset, rv);
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

      public GlyphTable.RuleLookup[] getLookups(int ci, int gi, GlyphPositioningState ps, int[] rv) {
         assert ps != null;

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
                     if (this.matches(ps, iga, 0, rv)) {
                        int[] bga = cr.getBacktrackGlyphs();
                        if (this.matches(ps, bga, -1, (int[])null)) {
                           int[] lga = cr.getLookaheadGlyphs();
                           if (this.matches(ps, lga, rv[0], (int[])null)) {
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

      private boolean matches(GlyphPositioningState ps, int[] glyphs, int offset, int[] rv) {
         return GlyphPositioningTable.ContextualSubtableFormat1.matches(ps, glyphs, offset, rv);
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

   private abstract static class ChainedContextualSubtable extends GlyphPositioningSubtable {
      ChainedContextualSubtable(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List entries) {
         super(id, sequence, flags, format, coverage);
      }

      public int getType() {
         return 8;
      }

      public boolean isCompatible(GlyphSubtable subtable) {
         return subtable instanceof ChainedContextualSubtable;
      }

      public boolean position(GlyphPositioningState ps) {
         boolean applied = false;
         int gi = ps.getGlyph();
         int ci;
         if ((ci = this.getCoverageIndex(gi)) >= 0) {
            int[] rv = new int[1];
            GlyphTable.RuleLookup[] la = this.getLookups(ci, gi, ps, rv);
            if (la != null) {
               ps.apply(la, rv[0]);
               applied = true;
            }
         }

         return applied;
      }

      public abstract GlyphTable.RuleLookup[] getLookups(int var1, int var2, GlyphPositioningState var3, int[] var4);

      static GlyphPositioningSubtable create(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List entries) {
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

      public GlyphTable.RuleLookup[] getLookups(int ci, int gi, GlyphPositioningState ps, int[] rv) {
         assert ps != null;

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
                     if (matches(ps, gca, 0, rv)) {
                        return r.getLookups();
                     }
                  }
               }
            }
         }

         return null;
      }

      static boolean matches(GlyphPositioningState ps, GlyphCoverageTable[] gca, int offset, int[] rv) {
         if (gca != null && gca.length != 0) {
            boolean reverse = offset < 0;
            GlyphTester ignores = ps.getIgnoreDefault();
            int[] counts = ps.getGlyphsAvailable(offset, reverse, ignores);
            int nga = counts[0];
            int ngm = gca.length;
            if (nga < ngm) {
               return false;
            } else {
               int[] ga = ps.getGlyphs(offset, ngm, reverse, ignores, (int[])null, counts);

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

      public GlyphTable.RuleLookup[] getLookups(int ci, int gi, GlyphPositioningState ps, int[] rv) {
         assert ps != null;

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
                     int[] ca = cr.getClasses(this.cdt.getClassIndex(gi, ps.getClassMatchSet(gi)));
                     if (matches(ps, this.cdt, ca, 0, rv)) {
                        return r.getLookups();
                     }
                  }
               }
            }
         }

         return null;
      }

      static boolean matches(GlyphPositioningState ps, GlyphClassTable cdt, int[] classes, int offset, int[] rv) {
         if (cdt != null && classes != null && classes.length != 0) {
            boolean reverse = offset < 0;
            GlyphTester ignores = ps.getIgnoreDefault();
            int[] counts = ps.getGlyphsAvailable(offset, reverse, ignores);
            int nga = counts[0];
            int ngm = classes.length;
            if (nga < ngm) {
               return false;
            } else {
               int[] ga = ps.getGlyphs(offset, ngm, reverse, ignores, (int[])null, counts);

               for(int k = 0; k < ngm; ++k) {
                  int gi = ga[k];
                  int ms = ps.getClassMatchSet(gi);
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

      public GlyphTable.RuleLookup[] getLookups(int ci, int gi, GlyphPositioningState ps, int[] rv) {
         assert ps != null;

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
                     if (matches(ps, iga, 0, rv)) {
                        return r.getLookups();
                     }
                  }
               }
            }
         }

         return null;
      }

      static boolean matches(GlyphPositioningState ps, int[] glyphs, int offset, int[] rv) {
         if (glyphs != null && glyphs.length != 0) {
            boolean reverse = offset < 0;
            GlyphTester ignores = ps.getIgnoreDefault();
            int[] counts = ps.getGlyphsAvailable(offset, reverse, ignores);
            int nga = counts[0];
            int ngm = glyphs.length;
            if (nga < ngm) {
               return false;
            } else {
               int[] ga = ps.getGlyphs(offset, ngm, reverse, ignores, (int[])null, counts);

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

   private abstract static class ContextualSubtable extends GlyphPositioningSubtable {
      ContextualSubtable(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List entries) {
         super(id, sequence, flags, format, coverage);
      }

      public int getType() {
         return 7;
      }

      public boolean isCompatible(GlyphSubtable subtable) {
         return subtable instanceof ContextualSubtable;
      }

      public boolean position(GlyphPositioningState ps) {
         boolean applied = false;
         int gi = ps.getGlyph();
         int ci;
         if ((ci = this.getCoverageIndex(gi)) >= 0) {
            int[] rv = new int[1];
            GlyphTable.RuleLookup[] la = this.getLookups(ci, gi, ps, rv);
            if (la != null) {
               ps.apply(la, rv[0]);
               applied = true;
            }
         }

         return applied;
      }

      public abstract GlyphTable.RuleLookup[] getLookups(int var1, int var2, GlyphPositioningState var3, int[] var4);

      static GlyphPositioningSubtable create(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List entries) {
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

   private static class MarkToMarkSubtableFormat1 extends MarkToMarkSubtable {
      private GlyphCoverageTable mct2;
      private int nmc;
      private MarkAnchor[] maa;
      private Anchor[][] mam;

      MarkToMarkSubtableFormat1(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List entries) {
         super(id, sequence, flags, format, coverage, entries);
         this.populate(entries);
      }

      public List getEntries() {
         if (this.mct2 != null && this.maa != null && this.nmc > 0 && this.mam != null) {
            List entries = new ArrayList(4);
            entries.add(this.mct2);
            entries.add(this.nmc);
            entries.add(this.maa);
            entries.add(this.mam);
            return entries;
         } else {
            return null;
         }
      }

      public MarkAnchor getMark1Anchor(int ciMark1, int giMark1) {
         return this.maa != null && ciMark1 < this.maa.length ? this.maa[ciMark1] : null;
      }

      public Anchor getMark2Anchor(int giMark2, int markClass) {
         int ciMark2;
         if (this.mct2 != null && (ciMark2 = this.mct2.getCoverageIndex(giMark2)) >= 0 && this.mam != null && ciMark2 < this.mam.length) {
            Anchor[] ma = this.mam[ciMark2];
            if (ma != null && markClass < ma.length) {
               return ma[markClass];
            }
         }

         return null;
      }

      private void populate(List entries) {
         if (entries == null) {
            throw new AdvancedTypographicTableFormatException("illegal entries, must be non-null");
         } else if (entries.size() != 4) {
            throw new AdvancedTypographicTableFormatException("illegal entries, " + entries.size() + " entries present, but requires 4 entries");
         } else {
            Object o;
            if ((o = entries.get(0)) != null && o instanceof GlyphCoverageTable) {
               this.mct2 = (GlyphCoverageTable)o;
               if ((o = entries.get(1)) != null && o instanceof Integer) {
                  this.nmc = (Integer)((Integer)o);
                  if ((o = entries.get(2)) != null && o instanceof MarkAnchor[]) {
                     this.maa = (MarkAnchor[])((MarkAnchor[])o);
                     if ((o = entries.get(3)) != null && o instanceof Anchor[][]) {
                        this.mam = (Anchor[][])((Anchor[][])o);
                     } else {
                        throw new AdvancedTypographicTableFormatException("illegal entries, fourth entry must be a Anchor[][], but is: " + (o != null ? o.getClass() : null));
                     }
                  } else {
                     throw new AdvancedTypographicTableFormatException("illegal entries, third entry must be a MarkAnchor[], but is: " + (o != null ? o.getClass() : null));
                  }
               } else {
                  throw new AdvancedTypographicTableFormatException("illegal entries, second entry must be an Integer, but is: " + (o != null ? o.getClass() : null));
               }
            } else {
               throw new AdvancedTypographicTableFormatException("illegal entries, first entry must be an GlyphCoverageTable, but is: " + (o != null ? o.getClass() : null));
            }
         }
      }
   }

   private abstract static class MarkToMarkSubtable extends GlyphPositioningSubtable {
      MarkToMarkSubtable(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List entries) {
         super(id, sequence, flags, format, coverage);
      }

      public int getType() {
         return 6;
      }

      public boolean isCompatible(GlyphSubtable subtable) {
         return subtable instanceof MarkToMarkSubtable;
      }

      public boolean position(GlyphPositioningState ps) {
         boolean applied = false;
         int giMark1 = ps.getGlyph();
         int ciMark1;
         if ((ciMark1 = this.getCoverageIndex(giMark1)) >= 0) {
            MarkAnchor ma = this.getMark1Anchor(ciMark1, giMark1);
            if (ma != null && ps.hasPrev()) {
               Anchor anchor = this.getMark2Anchor(ps.getUnprocessedGlyph(-1), ma.getMarkClass());
               if (anchor != null && ps.adjust(anchor.getAlignmentAdjustment(ma))) {
                  ps.setAdjusted(true);
               }

               ps.consume(1);
               applied = true;
            }
         }

         return applied;
      }

      public abstract MarkAnchor getMark1Anchor(int var1, int var2);

      public abstract Anchor getMark2Anchor(int var1, int var2);

      static GlyphPositioningSubtable create(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List entries) {
         if (format == 1) {
            return new MarkToMarkSubtableFormat1(id, sequence, flags, format, coverage, entries);
         } else {
            throw new UnsupportedOperationException();
         }
      }
   }

   private static class MarkToLigatureSubtableFormat1 extends MarkToLigatureSubtable {
      private GlyphCoverageTable lct;
      private int nmc;
      private int mxc;
      private MarkAnchor[] maa;
      private Anchor[][][] lam;

      MarkToLigatureSubtableFormat1(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List entries) {
         super(id, sequence, flags, format, coverage, entries);
         this.populate(entries);
      }

      public List getEntries() {
         if (this.lam != null) {
            List entries = new ArrayList(5);
            entries.add(this.lct);
            entries.add(this.nmc);
            entries.add(this.mxc);
            entries.add(this.maa);
            entries.add(this.lam);
            return entries;
         } else {
            return null;
         }
      }

      public MarkAnchor getMarkAnchor(int ciMark, int giMark) {
         return this.maa != null && ciMark < this.maa.length ? this.maa[ciMark] : null;
      }

      public int getMaxComponentCount() {
         return this.mxc;
      }

      public Anchor getLigatureAnchor(int giLig, int maxComponents, int component, int markClass) {
         int ciLig;
         if (this.lct != null && (ciLig = this.lct.getCoverageIndex(giLig)) >= 0 && this.lam != null && ciLig < this.lam.length) {
            Anchor[][] lcm = this.lam[ciLig];
            if (component < maxComponents) {
               Anchor[] la = lcm[component];
               if (la != null && markClass < la.length) {
                  return la[markClass];
               }
            }
         }

         return null;
      }

      private void populate(List entries) {
         if (entries == null) {
            throw new AdvancedTypographicTableFormatException("illegal entries, must be non-null");
         } else if (entries.size() != 5) {
            throw new AdvancedTypographicTableFormatException("illegal entries, " + entries.size() + " entries present, but requires 5 entries");
         } else {
            Object o;
            if ((o = entries.get(0)) != null && o instanceof GlyphCoverageTable) {
               this.lct = (GlyphCoverageTable)o;
               if ((o = entries.get(1)) != null && o instanceof Integer) {
                  this.nmc = (Integer)((Integer)o);
                  if ((o = entries.get(2)) != null && o instanceof Integer) {
                     this.mxc = (Integer)((Integer)o);
                     if ((o = entries.get(3)) != null && o instanceof MarkAnchor[]) {
                        this.maa = (MarkAnchor[])((MarkAnchor[])o);
                        if ((o = entries.get(4)) != null && o instanceof Anchor[][][]) {
                           this.lam = (Anchor[][][])((Anchor[][][])o);
                        } else {
                           throw new AdvancedTypographicTableFormatException("illegal entries, fifth entry must be a Anchor[][][], but is: " + (o != null ? o.getClass() : null));
                        }
                     } else {
                        throw new AdvancedTypographicTableFormatException("illegal entries, fourth entry must be a MarkAnchor[], but is: " + (o != null ? o.getClass() : null));
                     }
                  } else {
                     throw new AdvancedTypographicTableFormatException("illegal entries, third entry must be an Integer, but is: " + (o != null ? o.getClass() : null));
                  }
               } else {
                  throw new AdvancedTypographicTableFormatException("illegal entries, second entry must be an Integer, but is: " + (o != null ? o.getClass() : null));
               }
            } else {
               throw new AdvancedTypographicTableFormatException("illegal entries, first entry must be an GlyphCoverageTable, but is: " + (o != null ? o.getClass() : null));
            }
         }
      }
   }

   private abstract static class MarkToLigatureSubtable extends GlyphPositioningSubtable {
      MarkToLigatureSubtable(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List entries) {
         super(id, sequence, flags, format, coverage);
      }

      public int getType() {
         return 5;
      }

      public boolean isCompatible(GlyphSubtable subtable) {
         return subtable instanceof MarkToLigatureSubtable;
      }

      public boolean position(GlyphPositioningState ps) {
         boolean applied = false;
         int giMark = ps.getGlyph();
         int ciMark;
         if ((ciMark = this.getCoverageIndex(giMark)) >= 0) {
            MarkAnchor ma = this.getMarkAnchor(ciMark, giMark);
            int mxc = this.getMaxComponentCount();
            if (ma != null) {
               int i = 0;

               for(int n = ps.getPosition(); i < n; ++i) {
                  int glyphIndex = ps.getUnprocessedGlyph(-(i + 1));
                  if (!ps.isMark(glyphIndex)) {
                     Anchor anchor = this.getLigatureAnchor(glyphIndex, mxc, i, ma.getMarkClass());
                     if (anchor != null && ps.adjust(anchor.getAlignmentAdjustment(ma))) {
                        ps.setAdjusted(true);
                     }

                     ps.consume(1);
                     applied = true;
                     break;
                  }
               }
            }
         }

         return applied;
      }

      public abstract MarkAnchor getMarkAnchor(int var1, int var2);

      public abstract int getMaxComponentCount();

      public abstract Anchor getLigatureAnchor(int var1, int var2, int var3, int var4);

      static GlyphPositioningSubtable create(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List entries) {
         if (format == 1) {
            return new MarkToLigatureSubtableFormat1(id, sequence, flags, format, coverage, entries);
         } else {
            throw new UnsupportedOperationException();
         }
      }
   }

   private static class MarkToBaseSubtableFormat1 extends MarkToBaseSubtable {
      private GlyphCoverageTable bct;
      private int nmc;
      private MarkAnchor[] maa;
      private Anchor[][] bam;

      MarkToBaseSubtableFormat1(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List entries) {
         super(id, sequence, flags, format, coverage, entries);
         this.populate(entries);
      }

      public List getEntries() {
         if (this.bct != null && this.maa != null && this.nmc > 0 && this.bam != null) {
            List entries = new ArrayList(4);
            entries.add(this.bct);
            entries.add(this.nmc);
            entries.add(this.maa);
            entries.add(this.bam);
            return entries;
         } else {
            return null;
         }
      }

      public MarkAnchor getMarkAnchor(int ciMark, int giMark) {
         return this.maa != null && ciMark < this.maa.length ? this.maa[ciMark] : null;
      }

      public Anchor getBaseAnchor(int giBase, int markClass) {
         int ciBase;
         if (this.bct != null && (ciBase = this.bct.getCoverageIndex(giBase)) >= 0 && this.bam != null && ciBase < this.bam.length) {
            Anchor[] ba = this.bam[ciBase];
            if (ba != null && markClass < ba.length) {
               return ba[markClass];
            }
         }

         return null;
      }

      private void populate(List entries) {
         if (entries == null) {
            throw new AdvancedTypographicTableFormatException("illegal entries, must be non-null");
         } else if (entries.size() != 4) {
            throw new AdvancedTypographicTableFormatException("illegal entries, " + entries.size() + " entries present, but requires 4 entries");
         } else {
            Object o;
            if ((o = entries.get(0)) != null && o instanceof GlyphCoverageTable) {
               this.bct = (GlyphCoverageTable)o;
               if ((o = entries.get(1)) != null && o instanceof Integer) {
                  this.nmc = (Integer)((Integer)o);
                  if ((o = entries.get(2)) != null && o instanceof MarkAnchor[]) {
                     this.maa = (MarkAnchor[])((MarkAnchor[])o);
                     if ((o = entries.get(3)) != null && o instanceof Anchor[][]) {
                        this.bam = (Anchor[][])((Anchor[][])o);
                     } else {
                        throw new AdvancedTypographicTableFormatException("illegal entries, fourth entry must be a Anchor[][], but is: " + (o != null ? o.getClass() : null));
                     }
                  } else {
                     throw new AdvancedTypographicTableFormatException("illegal entries, third entry must be a MarkAnchor[], but is: " + (o != null ? o.getClass() : null));
                  }
               } else {
                  throw new AdvancedTypographicTableFormatException("illegal entries, second entry must be an Integer, but is: " + (o != null ? o.getClass() : null));
               }
            } else {
               throw new AdvancedTypographicTableFormatException("illegal entries, first entry must be an GlyphCoverageTable, but is: " + (o != null ? o.getClass() : null));
            }
         }
      }
   }

   private abstract static class MarkToBaseSubtable extends GlyphPositioningSubtable {
      MarkToBaseSubtable(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List entries) {
         super(id, sequence, flags, format, coverage);
      }

      public int getType() {
         return 4;
      }

      public boolean isCompatible(GlyphSubtable subtable) {
         return subtable instanceof MarkToBaseSubtable;
      }

      public boolean position(GlyphPositioningState ps) {
         boolean applied = false;
         int giMark = ps.getGlyph();
         int ciMark;
         if ((ciMark = this.getCoverageIndex(giMark)) >= 0) {
            MarkAnchor ma = this.getMarkAnchor(ciMark, giMark);
            if (ma != null) {
               int i = 0;

               for(int n = ps.getPosition(); i < n; ++i) {
                  int gi = ps.getGlyph(-(i + 1));
                  int unprocessedGlyph = ps.getUnprocessedGlyph(-(i + 1));
                  if (!ps.isMark(gi) || !ps.isMark(unprocessedGlyph)) {
                     Anchor a = this.getBaseAnchor(gi, ma.getMarkClass());
                     if (a != null) {
                        Value v = a.getAlignmentAdjustment(ma);
                        int[] aa = ps.getAdjustment();
                        if (aa[2] == 0) {
                           v.adjust(0, 0, -ps.getWidth(giMark), 0);
                        }

                        if ("khmr".equals(ps.script)) {
                           v.adjust(-ps.getWidth(gi), -v.yPlacement, 0, 0);
                        }

                        if (ps.adjust(v)) {
                           ps.setAdjusted(true);
                        }
                     }

                     ps.consume(1);
                     applied = true;
                     break;
                  }
               }
            }
         }

         return applied;
      }

      public abstract MarkAnchor getMarkAnchor(int var1, int var2);

      public abstract Anchor getBaseAnchor(int var1, int var2);

      static GlyphPositioningSubtable create(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List entries) {
         if (format == 1) {
            return new MarkToBaseSubtableFormat1(id, sequence, flags, format, coverage, entries);
         } else {
            throw new UnsupportedOperationException();
         }
      }
   }

   private static class CursiveSubtableFormat1 extends CursiveSubtable {
      private Anchor[] aa;

      CursiveSubtableFormat1(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List entries) {
         super(id, sequence, flags, format, coverage, entries);
         this.populate(entries);
      }

      public List getEntries() {
         if (this.aa != null) {
            List entries = new ArrayList(1);
            entries.add(this.aa);
            return entries;
         } else {
            return null;
         }
      }

      public Anchor[] getExitEntryAnchors(int ci1, int ci2) {
         if (ci1 >= 0 && ci2 >= 0) {
            int ai1 = ci1 * 2 + 1;
            int ai2 = ci2 * 2 + 0;
            if (this.aa != null && ai1 < this.aa.length && ai2 < this.aa.length) {
               Anchor exa = this.aa[ai1];
               Anchor ena = this.aa[ai2];
               if (exa != null && ena != null) {
                  return new Anchor[]{exa, ena};
               }
            }
         }

         return null;
      }

      private void populate(List entries) {
         if (entries == null) {
            throw new AdvancedTypographicTableFormatException("illegal entries, must be non-null");
         } else if (entries.size() != 1) {
            throw new AdvancedTypographicTableFormatException("illegal entries, " + entries.size() + " entries present, but requires 1 entry");
         } else {
            Object o;
            if ((o = entries.get(0)) != null && o instanceof Anchor[]) {
               if (((Anchor[])((Anchor[])o)).length % 2 != 0) {
                  throw new AdvancedTypographicTableFormatException("illegal entries, Anchor[] array must have an even number of entries, but has: " + ((Anchor[])((Anchor[])o)).length);
               } else {
                  this.aa = (Anchor[])((Anchor[])o);
               }
            } else {
               throw new AdvancedTypographicTableFormatException("illegal entries, first (and only) entry must be a Anchor[], but is: " + (o != null ? o.getClass() : null));
            }
         }
      }
   }

   private abstract static class CursiveSubtable extends GlyphPositioningSubtable {
      CursiveSubtable(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List entries) {
         super(id, sequence, flags, format, coverage);
      }

      public int getType() {
         return 3;
      }

      public boolean isCompatible(GlyphSubtable subtable) {
         return subtable instanceof CursiveSubtable;
      }

      public boolean position(GlyphPositioningState ps) {
         boolean applied = false;
         int gi = ps.getGlyph(0);
         int ci;
         if ((ci = this.getCoverageIndex(gi)) >= 0) {
            int[] counts = ps.getGlyphsAvailable(0);
            int nga = counts[0];
            if (nga > 1) {
               int[] iga = ps.getGlyphs(0, 2, (int[])null, counts);
               if (iga != null && iga.length == 2) {
                  int gi2 = iga[1];
                  int ci2 = this.getCoverageIndex(gi2);
                  Anchor[] aa = this.getExitEntryAnchors(ci, ci2);
                  if (aa != null) {
                     Anchor exa = aa[0];
                     Anchor ena = aa[1];
                     int enw = ps.getWidth(gi2);
                     if (exa != null && ena != null) {
                        Value v = ena.getAlignmentAdjustment(exa);
                        v.adjust(-enw, 0, 0, 0);
                        if (ps.adjust(v)) {
                           ps.setAdjusted(true);
                        }
                     }

                     ps.consume(1);
                     applied = true;
                  }
               }
            }
         }

         return applied;
      }

      public abstract Anchor[] getExitEntryAnchors(int var1, int var2);

      static GlyphPositioningSubtable create(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List entries) {
         if (format == 1) {
            return new CursiveSubtableFormat1(id, sequence, flags, format, coverage, entries);
         } else {
            throw new UnsupportedOperationException();
         }
      }
   }

   private static class PairSubtableFormat2 extends PairSubtable {
      private GlyphClassTable cdt1;
      private GlyphClassTable cdt2;
      private int nc1;
      private int nc2;
      private PairValues[][] pvm;

      PairSubtableFormat2(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List entries) {
         super(id, sequence, flags, format, coverage, entries);
         this.populate(entries);
      }

      public List getEntries() {
         if (this.pvm != null) {
            List entries = new ArrayList(5);
            entries.add(this.cdt1);
            entries.add(this.cdt2);
            entries.add(this.nc1);
            entries.add(this.nc2);
            entries.add(this.pvm);
            return entries;
         } else {
            return null;
         }
      }

      public PairValues getPairValues(int ci, int gi1, int gi2) {
         if (this.pvm != null) {
            int c1 = this.cdt1.getClassIndex(gi1, 0);
            if (c1 >= 0 && c1 < this.nc1 && c1 < this.pvm.length) {
               PairValues[] pvt = this.pvm[c1];
               if (pvt != null) {
                  int c2 = this.cdt2.getClassIndex(gi2, 0);
                  if (c2 >= 0 && c2 < this.nc2 && c2 < pvt.length) {
                     return pvt[c2];
                  }
               }
            }
         }

         return null;
      }

      private void populate(List entries) {
         if (entries == null) {
            throw new AdvancedTypographicTableFormatException("illegal entries, must be non-null");
         } else if (entries.size() != 5) {
            throw new AdvancedTypographicTableFormatException("illegal entries, " + entries.size() + " entries present, but requires 5 entries");
         } else {
            Object o;
            if ((o = entries.get(0)) != null && o instanceof GlyphClassTable) {
               this.cdt1 = (GlyphClassTable)o;
               if ((o = entries.get(1)) != null && o instanceof GlyphClassTable) {
                  this.cdt2 = (GlyphClassTable)o;
                  if ((o = entries.get(2)) != null && o instanceof Integer) {
                     this.nc1 = (Integer)((Integer)o);
                     if ((o = entries.get(3)) != null && o instanceof Integer) {
                        this.nc2 = (Integer)((Integer)o);
                        if ((o = entries.get(4)) != null && o instanceof PairValues[][]) {
                           this.pvm = (PairValues[][])((PairValues[][])o);
                        } else {
                           throw new AdvancedTypographicTableFormatException("illegal entries, fifth entry must be a PairValues[][], but is: " + (o != null ? o.getClass() : null));
                        }
                     } else {
                        throw new AdvancedTypographicTableFormatException("illegal entries, fourth entry must be an Integer, but is: " + (o != null ? o.getClass() : null));
                     }
                  } else {
                     throw new AdvancedTypographicTableFormatException("illegal entries, third entry must be an Integer, but is: " + (o != null ? o.getClass() : null));
                  }
               } else {
                  throw new AdvancedTypographicTableFormatException("illegal entries, second entry must be an GlyphClassTable, but is: " + (o != null ? o.getClass() : null));
               }
            } else {
               throw new AdvancedTypographicTableFormatException("illegal entries, first entry must be an GlyphClassTable, but is: " + (o != null ? o.getClass() : null));
            }
         }
      }
   }

   private static class PairSubtableFormat1 extends PairSubtable {
      private PairValues[][] pvm;

      PairSubtableFormat1(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List entries) {
         super(id, sequence, flags, format, coverage, entries);
         this.populate(entries);
      }

      public List getEntries() {
         if (this.pvm != null) {
            List entries = new ArrayList(1);
            entries.add(this.pvm);
            return entries;
         } else {
            return null;
         }
      }

      public PairValues getPairValues(int ci, int gi1, int gi2) {
         if (this.pvm != null && ci < this.pvm.length) {
            PairValues[] pvt = this.pvm[ci];
            PairValues[] var5 = pvt;
            int var6 = pvt.length;

            for(int var7 = 0; var7 < var6; ++var7) {
               PairValues pv = var5[var7];
               if (pv != null) {
                  int g = pv.getGlyph();
                  if (g >= gi2) {
                     if (g == gi2) {
                        return pv;
                     }
                     break;
                  }
               }
            }
         }

         return null;
      }

      private void populate(List entries) {
         if (entries == null) {
            throw new AdvancedTypographicTableFormatException("illegal entries, must be non-null");
         } else if (entries.size() != 1) {
            throw new AdvancedTypographicTableFormatException("illegal entries, " + entries.size() + " entries present, but requires 1 entry");
         } else {
            Object o;
            if ((o = entries.get(0)) != null && o instanceof PairValues[][]) {
               this.pvm = (PairValues[][])((PairValues[][])o);
            } else {
               throw new AdvancedTypographicTableFormatException("illegal entries, first (and only) entry must be a PairValues[][], but is: " + (o != null ? o.getClass() : null));
            }
         }
      }
   }

   private abstract static class PairSubtable extends GlyphPositioningSubtable {
      PairSubtable(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List entries) {
         super(id, sequence, flags, format, coverage);
      }

      public int getType() {
         return 2;
      }

      public boolean isCompatible(GlyphSubtable subtable) {
         return subtable instanceof PairSubtable;
      }

      public boolean position(GlyphPositioningState ps) {
         boolean applied = false;
         int gi = ps.getGlyph(0);
         int ci;
         if ((ci = this.getCoverageIndex(gi)) >= 0) {
            int[] counts = ps.getGlyphsAvailable(0);
            int nga = counts[0];
            if (nga > 1) {
               int[] iga = ps.getGlyphs(0, 2, (int[])null, counts);
               if (iga != null && iga.length == 2) {
                  PairValues pv = this.getPairValues(ci, iga[0], iga[1]);
                  if (pv != null) {
                     int offset = 0;

                     int offsetLast;
                     for(offsetLast = counts[0] + counts[1]; offset < offsetLast && ps.isIgnoredGlyph(offset); ++offset) {
                        ps.consume(1);
                     }

                     Value v1 = pv.getValue1();
                     if (v1 != null) {
                        if (ps.adjust(v1, offset)) {
                           ps.setAdjusted(true);
                        }

                        ps.consume(1);
                        ++offset;
                     }

                     while(offset < offsetLast && ps.isIgnoredGlyph(offset)) {
                        ps.consume(1);
                        ++offset;
                     }

                     Value v2 = pv.getValue2();
                     if (v2 != null) {
                        if (ps.adjust(v2, offset)) {
                           ps.setAdjusted(true);
                        }

                        ps.consume(1);
                        ++offset;
                     }

                     applied = true;
                  }
               }
            }
         }

         return applied;
      }

      public abstract PairValues getPairValues(int var1, int var2, int var3);

      static GlyphPositioningSubtable create(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List entries) {
         if (format == 1) {
            return new PairSubtableFormat1(id, sequence, flags, format, coverage, entries);
         } else if (format == 2) {
            return new PairSubtableFormat2(id, sequence, flags, format, coverage, entries);
         } else {
            throw new UnsupportedOperationException();
         }
      }
   }

   private static class SingleSubtableFormat2 extends SingleSubtable {
      private Value[] values;

      SingleSubtableFormat2(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List entries) {
         super(id, sequence, flags, format, coverage, entries);
         this.populate(entries);
      }

      public List getEntries() {
         if (this.values != null) {
            List entries = new ArrayList(this.values.length);
            Collections.addAll(entries, this.values);
            return entries;
         } else {
            return null;
         }
      }

      public Value getValue(int ci, int gi) {
         return this.values != null && ci < this.values.length ? this.values[ci] : null;
      }

      private void populate(List entries) {
         if (entries == null) {
            throw new AdvancedTypographicTableFormatException("illegal entries, must be non-null");
         } else if (entries.size() != 1) {
            throw new AdvancedTypographicTableFormatException("illegal entries, " + entries.size() + " entries present, but requires 1 entry");
         } else {
            Object o;
            if ((o = entries.get(0)) != null && o instanceof Value[]) {
               Value[] va = (Value[])((Value[])o);
               if (va.length != this.getCoverageSize()) {
                  throw new AdvancedTypographicTableFormatException("illegal values array, " + entries.size() + " values present, but requires " + this.getCoverageSize() + " values");
               } else {
                  assert this.values == null;

                  this.values = va;
               }
            } else {
               throw new AdvancedTypographicTableFormatException("illegal entries, single entry must be a Value[], but is: " + (o != null ? o.getClass() : null));
            }
         }
      }
   }

   private static class SingleSubtableFormat1 extends SingleSubtable {
      private Value value;
      private int ciMax;

      SingleSubtableFormat1(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List entries) {
         super(id, sequence, flags, format, coverage, entries);
         this.populate(entries);
      }

      public List getEntries() {
         if (this.value != null) {
            List entries = new ArrayList(1);
            entries.add(this.value);
            return entries;
         } else {
            return null;
         }
      }

      public Value getValue(int ci, int gi) {
         return this.value != null && ci <= this.ciMax ? this.value : null;
      }

      private void populate(List entries) {
         if (entries != null && entries.size() == 1) {
            Object o = entries.get(0);
            if (o instanceof Value) {
               Value v = (Value)o;

               assert this.value == null;

               this.value = v;
               this.ciMax = this.getCoverageSize() - 1;
            } else {
               throw new AdvancedTypographicTableFormatException("illegal entries entry, must be Value, but is: " + (o != null ? o.getClass() : null));
            }
         } else {
            throw new AdvancedTypographicTableFormatException("illegal entries, must be non-null and contain exactly one entry");
         }
      }
   }

   private abstract static class SingleSubtable extends GlyphPositioningSubtable {
      SingleSubtable(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List entries) {
         super(id, sequence, flags, format, coverage);
      }

      public int getType() {
         return 1;
      }

      public boolean isCompatible(GlyphSubtable subtable) {
         return subtable instanceof SingleSubtable;
      }

      public boolean position(GlyphPositioningState ps) {
         int gi = ps.getGlyph();
         int ci;
         if ((ci = this.getCoverageIndex(gi)) < 0) {
            return false;
         } else {
            Value v = this.getValue(ci, gi);
            if (v != null) {
               if (ps.adjust(v)) {
                  ps.setAdjusted(true);
               }

               ps.consume(1);
            }

            return true;
         }
      }

      public abstract Value getValue(int var1, int var2);

      static GlyphPositioningSubtable create(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List entries) {
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
