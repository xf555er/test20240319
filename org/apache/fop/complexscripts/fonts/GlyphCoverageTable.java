package org.apache.fop.complexscripts.fonts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class GlyphCoverageTable extends GlyphMappingTable implements GlyphCoverageMapping {
   private static final Log log = LogFactory.getLog(GlyphCoverageTable.class);
   public static final int GLYPH_COVERAGE_TYPE_EMPTY = 0;
   public static final int GLYPH_COVERAGE_TYPE_MAPPED = 1;
   public static final int GLYPH_COVERAGE_TYPE_RANGE = 2;
   private GlyphCoverageMapping cm;

   private GlyphCoverageTable(GlyphCoverageMapping cm) {
      assert cm != null;

      assert cm instanceof GlyphMappingTable;

      this.cm = cm;
   }

   public int getType() {
      return ((GlyphMappingTable)this.cm).getType();
   }

   public List getEntries() {
      return ((GlyphMappingTable)this.cm).getEntries();
   }

   public int getCoverageSize() {
      return this.cm.getCoverageSize();
   }

   public int getCoverageIndex(int gid) {
      return this.cm.getCoverageIndex(gid);
   }

   public static GlyphCoverageTable createCoverageTable(List entries) {
      Object cm;
      if (entries != null && entries.size() != 0) {
         if (isMappedCoverage(entries)) {
            cm = new MappedCoverageTable(entries);
         } else if (isRangeCoverage(entries)) {
            cm = new RangeCoverageTable(entries);
         } else {
            cm = null;
         }
      } else {
         cm = new EmptyCoverageTable(entries);
      }

      assert cm != null : "unknown coverage type";

      return new GlyphCoverageTable((GlyphCoverageMapping)cm);
   }

   private static boolean isMappedCoverage(List entries) {
      if (entries != null && entries.size() != 0) {
         Iterator var1 = entries.iterator();

         Object o;
         do {
            if (!var1.hasNext()) {
               return true;
            }

            o = var1.next();
         } while(o instanceof Integer);

         return false;
      } else {
         return false;
      }
   }

   private static boolean isRangeCoverage(List entries) {
      if (entries != null && entries.size() != 0) {
         Iterator var1 = entries.iterator();

         Object o;
         do {
            if (!var1.hasNext()) {
               return true;
            }

            o = var1.next();
         } while(o instanceof GlyphMappingTable.MappingRange);

         return false;
      } else {
         return false;
      }
   }

   private static class RangeCoverageTable extends GlyphMappingTable.RangeMappingTable implements GlyphCoverageMapping {
      public RangeCoverageTable(List entries) {
         super(entries);
      }

      public int getMappedIndex(int gid, int s, int m) {
         return m + gid - s;
      }

      public int getCoverageSize() {
         return this.getMappingSize();
      }

      public int getCoverageIndex(int gid) {
         return this.getMappedIndex(gid);
      }
   }

   private static class MappedCoverageTable extends GlyphMappingTable.MappedMappingTable implements GlyphCoverageMapping {
      private int[] map;

      public MappedCoverageTable(List entries) {
         this.populate(entries);
      }

      public List getEntries() {
         List entries = new ArrayList();
         if (this.map != null) {
            int[] var2 = this.map;
            int var3 = var2.length;

            for(int var4 = 0; var4 < var3; ++var4) {
               int aMap = var2[var4];
               entries.add(aMap);
            }
         }

         return entries;
      }

      public int getMappingSize() {
         return this.map != null ? this.map.length : 0;
      }

      public int getMappedIndex(int gid) {
         int i;
         return (i = Arrays.binarySearch(this.map, gid)) >= 0 ? i : -1;
      }

      public int getCoverageSize() {
         return this.getMappingSize();
      }

      public int getCoverageIndex(int gid) {
         return this.getMappedIndex(gid);
      }

      private void populate(List entries) {
         int i = 0;
         int skipped = 0;
         int n = entries.size();
         int gidMax = -1;
         int[] map = new int[n];
         Iterator var7 = entries.iterator();

         while(var7.hasNext()) {
            Object o = var7.next();
            if (!(o instanceof Integer)) {
               throw new AdvancedTypographicTableFormatException("illegal coverage entry, must be Integer: " + o);
            }

            int gid = (Integer)o;
            if (gid < 0 || gid >= 65536) {
               throw new AdvancedTypographicTableFormatException("illegal glyph index: " + gid);
            }

            if (gid > gidMax) {
               int var10001 = i++;
               gidMax = gid;
               map[var10001] = gid;
            } else {
               GlyphCoverageTable.log.info("ignoring out of order or duplicate glyph index: " + gid);
               ++skipped;
            }
         }

         assert i + skipped == n;

         assert this.map == null;

         this.map = map;
      }

      public String toString() {
         StringBuffer sb = new StringBuffer();
         sb.append('{');
         int i = 0;

         for(int n = this.map.length; i < n; ++i) {
            if (i > 0) {
               sb.append(',');
            }

            sb.append(Integer.toString(this.map[i]));
         }

         sb.append('}');
         return sb.toString();
      }
   }

   private static class EmptyCoverageTable extends GlyphMappingTable.EmptyMappingTable implements GlyphCoverageMapping {
      public EmptyCoverageTable(List entries) {
         super(entries);
      }

      public int getCoverageSize() {
         return 0;
      }

      public int getCoverageIndex(int gid) {
         return -1;
      }
   }
}
