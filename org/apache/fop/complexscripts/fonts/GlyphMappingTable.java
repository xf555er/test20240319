package org.apache.fop.complexscripts.fonts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class GlyphMappingTable {
   public static final int GLYPH_MAPPING_TYPE_EMPTY = 0;
   public static final int GLYPH_MAPPING_TYPE_MAPPED = 1;
   public static final int GLYPH_MAPPING_TYPE_RANGE = 2;

   public int getType() {
      return -1;
   }

   public List getEntries() {
      return null;
   }

   public int getMappingSize() {
      return 0;
   }

   public int getMappedIndex(int gid) {
      return -1;
   }

   public static class MappingRange {
      private final int gidStart;
      private final int gidEnd;
      private final int index;

      public MappingRange() {
         this(0, 0, 0);
      }

      public MappingRange(int gidStart, int gidEnd, int index) {
         if (gidStart >= 0 && gidEnd >= 0 && index >= 0) {
            if (gidStart > gidEnd) {
               throw new AdvancedTypographicTableFormatException();
            } else {
               this.gidStart = gidStart;
               this.gidEnd = gidEnd;
               this.index = index;
            }
         } else {
            throw new AdvancedTypographicTableFormatException();
         }
      }

      public int getStart() {
         return this.gidStart;
      }

      public int getEnd() {
         return this.gidEnd;
      }

      public int getIndex() {
         return this.index;
      }

      public int[] getInterval() {
         return new int[]{this.gidStart, this.gidEnd};
      }

      public int[] getInterval(int[] interval) {
         if (interval != null && interval.length == 2) {
            interval[0] = this.gidStart;
            interval[1] = this.gidEnd;
            return interval;
         } else {
            throw new IllegalArgumentException();
         }
      }

      public int getLength() {
         return this.gidStart - this.gidEnd;
      }
   }

   protected abstract static class RangeMappingTable extends GlyphMappingTable {
      private int[] sa;
      private int[] ea;
      private int[] ma;
      private int miMax = -1;

      public RangeMappingTable(List entries) {
         this.populate(entries);
      }

      public int getType() {
         return 2;
      }

      public List getEntries() {
         List entries = new ArrayList();
         if (this.sa != null) {
            int i = 0;

            for(int n = this.sa.length; i < n; ++i) {
               entries.add(new MappingRange(this.sa[i], this.ea[i], this.ma[i]));
            }
         }

         return entries;
      }

      public int getMappingSize() {
         return this.miMax + 1;
      }

      public int getMappedIndex(int gid) {
         int i;
         int mi;
         if ((i = Arrays.binarySearch(this.sa, gid)) >= 0) {
            mi = this.getMappedIndex(gid, this.sa[i], this.ma[i]);
         } else if ((i = -(i + 1)) == 0) {
            mi = -1;
         } else {
            --i;
            if (gid > this.ea[i]) {
               mi = -1;
            } else {
               mi = this.getMappedIndex(gid, this.sa[i], this.ma[i]);
            }
         }

         return mi;
      }

      public abstract int getMappedIndex(int var1, int var2, int var3);

      private void populate(List entries) {
         int i = 0;
         int n = entries.size();
         int gidMax = -1;
         int miMax = -1;
         int[] sa = new int[n];
         int[] ea = new int[n];
         int[] ma = new int[n];
         Iterator var9 = entries.iterator();

         while(var9.hasNext()) {
            Object o = var9.next();
            if (!(o instanceof MappingRange)) {
               throw new AdvancedTypographicTableFormatException("illegal mapping entry, must be Integer: " + o);
            }

            MappingRange r = (MappingRange)o;
            int gs = r.getStart();
            int ge = r.getEnd();
            int mi = r.getIndex();
            if (gs >= 0 && gs <= 65535) {
               if (ge >= 0 && ge <= 65535) {
                  if (gs > ge) {
                     throw new AdvancedTypographicTableFormatException("illegal glyph range: [" + gs + "," + ge + "]: start index exceeds end index");
                  }

                  if (gs < gidMax) {
                     throw new AdvancedTypographicTableFormatException("out of order glyph range: [" + gs + "," + ge + "]");
                  }

                  if (mi < 0) {
                     throw new AdvancedTypographicTableFormatException("illegal mapping index: " + mi);
                  }

                  sa[i] = gs;
                  gidMax = ge;
                  ea[i] = ge;
                  ma[i] = mi;
                  int miLast;
                  if ((miLast = mi + (ge - gs)) > miMax) {
                     miMax = miLast;
                  }

                  ++i;
                  continue;
               }

               throw new AdvancedTypographicTableFormatException("illegal glyph range: [" + gs + "," + ge + "]: bad end index");
            }

            throw new AdvancedTypographicTableFormatException("illegal glyph range: [" + gs + "," + ge + "]: bad start index");
         }

         assert i == n;

         assert this.sa == null;

         assert this.ea == null;

         assert this.ma == null;

         this.sa = sa;
         this.ea = ea;
         this.ma = ma;
         this.miMax = miMax;
      }

      public String toString() {
         StringBuffer sb = new StringBuffer();
         sb.append('{');
         int i = 0;

         for(int n = this.sa.length; i < n; ++i) {
            if (i > 0) {
               sb.append(',');
            }

            sb.append('[');
            sb.append(Integer.toString(this.sa[i]));
            sb.append(Integer.toString(this.ea[i]));
            sb.append("]:");
            sb.append(Integer.toString(this.ma[i]));
         }

         sb.append('}');
         return sb.toString();
      }
   }

   protected static class MappedMappingTable extends GlyphMappingTable {
      public MappedMappingTable() {
      }

      public int getType() {
         return 1;
      }
   }

   protected static class EmptyMappingTable extends GlyphMappingTable {
      public EmptyMappingTable() {
         this((List)null);
      }

      public EmptyMappingTable(List entries) {
      }

      public int getType() {
         return 0;
      }

      public List getEntries() {
         return new ArrayList();
      }

      public int getMappingSize() {
         return 0;
      }

      public int getMappedIndex(int gid) {
         return -1;
      }
   }
}
