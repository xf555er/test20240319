package org.apache.fop.complexscripts.fonts;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

public abstract class GlyphSubtable implements Comparable {
   public static final int LF_RIGHT_TO_LEFT = 1;
   public static final int LF_IGNORE_BASE = 2;
   public static final int LF_IGNORE_LIGATURE = 4;
   public static final int LF_IGNORE_MARK = 8;
   public static final int LF_USE_MARK_FILTERING_SET = 16;
   public static final int LF_RESERVED = 3584;
   public static final int LF_MARK_ATTACHMENT_TYPE = 65280;
   public static final int LF_INTERNAL_USE_REVERSE_SCAN = 65536;
   private String lookupId;
   private int sequence;
   private int flags;
   private int format;
   private GlyphMappingTable mapping;
   private WeakReference table;

   protected GlyphSubtable(String lookupId, int sequence, int flags, int format, GlyphMappingTable mapping) {
      if (lookupId != null && lookupId.length() != 0) {
         if (mapping == null) {
            throw new AdvancedTypographicTableFormatException("invalid mapping table, must not be null");
         } else {
            this.lookupId = lookupId;
            this.sequence = sequence;
            this.flags = flags;
            this.format = format;
            this.mapping = mapping;
         }
      } else {
         throw new AdvancedTypographicTableFormatException("invalid lookup identifier, must be non-empty string");
      }
   }

   public String getLookupId() {
      return this.lookupId;
   }

   public abstract int getTableType();

   public abstract int getType();

   public abstract String getTypeName();

   public abstract boolean isCompatible(GlyphSubtable var1);

   public abstract boolean usesReverseScan();

   public int getSequence() {
      return this.sequence;
   }

   public int getFlags() {
      return this.flags;
   }

   public int getFormat() {
      return this.format;
   }

   public GlyphDefinitionTable getGDEF() {
      GlyphTable gt = this.getTable();
      return gt != null ? gt.getGlyphDefinitions() : null;
   }

   public GlyphCoverageMapping getCoverage() {
      return this.mapping instanceof GlyphCoverageMapping ? (GlyphCoverageMapping)this.mapping : null;
   }

   public GlyphClassMapping getClasses() {
      return this.mapping instanceof GlyphClassMapping ? (GlyphClassMapping)this.mapping : null;
   }

   public abstract List getEntries();

   public synchronized GlyphTable getTable() {
      WeakReference r = this.table;
      return r != null ? (GlyphTable)r.get() : null;
   }

   public synchronized void setTable(GlyphTable table) throws IllegalStateException {
      WeakReference r = this.table;
      if (table == null) {
         this.table = null;
         if (r != null) {
            r.clear();
         }
      } else {
         if (r != null) {
            throw new IllegalStateException("table already set");
         }

         this.table = new WeakReference(table);
      }

   }

   public void resolveLookupReferences(Map lookupTables) {
   }

   public int getCoverageIndex(int gid) {
      return this.mapping instanceof GlyphCoverageMapping ? ((GlyphCoverageMapping)this.mapping).getCoverageIndex(gid) : -1;
   }

   public int getCoverageSize() {
      return this.mapping instanceof GlyphCoverageMapping ? ((GlyphCoverageMapping)this.mapping).getCoverageSize() : 0;
   }

   public int hashCode() {
      int hc = this.sequence;
      hc = hc * 3 + (this.lookupId.hashCode() ^ hc);
      return hc;
   }

   public boolean equals(Object o) {
      if (!(o instanceof GlyphSubtable)) {
         return false;
      } else {
         GlyphSubtable st = (GlyphSubtable)o;
         return this.lookupId.equals(st.lookupId) && this.sequence == st.sequence;
      }
   }

   public int compareTo(Object o) {
      int d;
      if (o instanceof GlyphSubtable) {
         GlyphSubtable st = (GlyphSubtable)o;
         if ((d = this.lookupId.compareTo(st.lookupId)) == 0) {
            if (this.sequence < st.sequence) {
               d = -1;
            } else if (this.sequence > st.sequence) {
               d = 1;
            }
         }
      } else {
         d = -1;
      }

      return d;
   }

   public static boolean usesReverseScan(GlyphSubtable[] subtables) {
      if (subtables != null && subtables.length != 0) {
         GlyphSubtable[] var1 = subtables;
         int var2 = subtables.length;

         for(int var3 = 0; var3 < var2; ++var3) {
            GlyphSubtable subtable = var1[var3];
            if (subtable.usesReverseScan()) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static int getFlags(GlyphSubtable[] subtables) throws IllegalStateException {
      if (subtables != null && subtables.length != 0) {
         int flags = 0;
         GlyphSubtable[] var2 = subtables;
         int var3 = subtables.length;

         int var4;
         GlyphSubtable subtable;
         int f;
         for(var4 = 0; var4 < var3; ++var4) {
            subtable = var2[var4];
            f = subtable.getFlags();
            if (flags == 0) {
               flags = f;
               break;
            }
         }

         var2 = subtables;
         var3 = subtables.length;

         for(var4 = 0; var4 < var3; ++var4) {
            subtable = var2[var4];
            f = subtable.getFlags();
            if (f != flags) {
               throw new IllegalStateException("inconsistent lookup flags " + f + ", expected " + flags);
            }
         }

         return flags | (usesReverseScan(subtables) ? 65536 : 0);
      } else {
         return 0;
      }
   }
}
