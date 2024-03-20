package org.apache.fop.complexscripts.fonts;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class GlyphClassTable extends GlyphMappingTable implements GlyphClassMapping {
   public static final int GLYPH_CLASS_TYPE_EMPTY = 0;
   public static final int GLYPH_CLASS_TYPE_MAPPED = 1;
   public static final int GLYPH_CLASS_TYPE_RANGE = 2;
   public static final int GLYPH_CLASS_TYPE_COVERAGE_SET = 3;
   private GlyphClassMapping cm;

   private GlyphClassTable(GlyphClassMapping cm) {
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

   public int getClassSize(int set) {
      return this.cm.getClassSize(set);
   }

   public int getClassIndex(int gid, int set) {
      return this.cm.getClassIndex(gid, set);
   }

   public static GlyphClassTable createClassTable(List entries) {
      Object cm;
      if (entries != null && entries.size() != 0) {
         if (isMappedClass(entries)) {
            cm = new MappedClassTable(entries);
         } else if (isRangeClass(entries)) {
            cm = new RangeClassTable(entries);
         } else if (isCoverageSetClass(entries)) {
            cm = new CoverageSetClassTable(entries);
         } else {
            cm = null;
         }
      } else {
         cm = new EmptyClassTable(entries);
      }

      assert cm != null : "unknown class type";

      return new GlyphClassTable((GlyphClassMapping)cm);
   }

   private static boolean isMappedClass(List entries) {
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

   private static boolean isRangeClass(List entries) {
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

   private static boolean isCoverageSetClass(List entries) {
      if (entries != null && entries.size() != 0) {
         Iterator var1 = entries.iterator();

         Object o;
         do {
            if (!var1.hasNext()) {
               return true;
            }

            o = var1.next();
         } while(o instanceof GlyphCoverageTable);

         return false;
      } else {
         return false;
      }
   }

   private static class CoverageSetClassTable extends GlyphMappingTable.EmptyMappingTable implements GlyphClassMapping {
      private static final Log LOG = LogFactory.getLog(CoverageSetClassTable.class);

      public CoverageSetClassTable(List entries) {
         LOG.warn("coverage set class table not yet supported");
      }

      public int getType() {
         return 3;
      }

      public int getClassSize(int set) {
         return 0;
      }

      public int getClassIndex(int gid, int set) {
         return -1;
      }
   }

   private static class RangeClassTable extends GlyphMappingTable.RangeMappingTable implements GlyphClassMapping {
      public RangeClassTable(List entries) {
         super(entries);
      }

      public int getMappedIndex(int gid, int s, int m) {
         return m;
      }

      public int getClassSize(int set) {
         return this.getMappingSize();
      }

      public int getClassIndex(int gid, int set) {
         return this.getMappedIndex(gid);
      }
   }

   private static class MappedClassTable extends GlyphMappingTable.MappedMappingTable implements GlyphClassMapping {
      private int firstGlyph;
      private int[] gca;
      private int gcMax = -1;

      public MappedClassTable(List entries) {
         this.populate(entries);
      }

      public List getEntries() {
         List entries = new ArrayList();
         entries.add(this.firstGlyph);
         if (this.gca != null) {
            int[] var2 = this.gca;
            int var3 = var2.length;

            for(int var4 = 0; var4 < var3; ++var4) {
               int aGca = var2[var4];
               entries.add(aGca);
            }
         }

         return entries;
      }

      public int getMappingSize() {
         return this.gcMax + 1;
      }

      public int getMappedIndex(int gid) {
         int i = gid - this.firstGlyph;
         return i >= 0 && i < this.gca.length ? this.gca[i] : -1;
      }

      public int getClassSize(int set) {
         return this.getMappingSize();
      }

      public int getClassIndex(int gid, int set) {
         return this.getMappedIndex(gid);
      }

      private void populate(List entries) {
         Iterator it = entries.iterator();
         int firstGlyph = 0;
         if (it.hasNext()) {
            Object o = it.next();
            if (!(o instanceof Integer)) {
               throw new AdvancedTypographicTableFormatException("illegal entry, first entry must be Integer denoting first glyph value, but is: " + o);
            }

            firstGlyph = (Integer)o;
         }

         int i = 0;
         int n = entries.size() - 1;
         int gcMax = -1;
         int[] gca = new int[n];

         while(it.hasNext()) {
            Object o = it.next();
            if (!(o instanceof Integer)) {
               throw new AdvancedTypographicTableFormatException("illegal mapping entry, must be Integer: " + o);
            }

            int gc = (Integer)o;
            gca[i++] = gc;
            if (gc > gcMax) {
               gcMax = gc;
            }
         }

         assert i == n;

         assert this.gca == null;

         this.firstGlyph = firstGlyph;
         this.gca = gca;
         this.gcMax = gcMax;
      }

      public String toString() {
         StringBuffer sb = new StringBuffer();
         sb.append("{ firstGlyph = " + this.firstGlyph + ", classes = {");
         int i = 0;

         for(int n = this.gca.length; i < n; ++i) {
            if (i > 0) {
               sb.append(',');
            }

            sb.append(Integer.toString(this.gca[i]));
         }

         sb.append("} }");
         return sb.toString();
      }
   }

   private static class EmptyClassTable extends GlyphMappingTable.EmptyMappingTable implements GlyphClassMapping {
      public EmptyClassTable(List entries) {
         super(entries);
      }

      public int getClassSize(int set) {
         return 0;
      }

      public int getClassIndex(int gid, int set) {
         return -1;
      }
   }
}
