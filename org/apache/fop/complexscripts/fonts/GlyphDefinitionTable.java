package org.apache.fop.complexscripts.fonts;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.complexscripts.scripts.ScriptProcessor;
import org.apache.fop.complexscripts.util.GlyphSequence;

public class GlyphDefinitionTable extends GlyphTable {
   private static final Log log = LogFactory.getLog(GlyphDefinitionTable.class);
   public static final int GDEF_LOOKUP_TYPE_GLYPH_CLASS = 1;
   public static final int GDEF_LOOKUP_TYPE_ATTACHMENT_POINT = 2;
   public static final int GDEF_LOOKUP_TYPE_LIGATURE_CARET = 3;
   public static final int GDEF_LOOKUP_TYPE_MARK_ATTACHMENT = 4;
   public static final int GLYPH_CLASS_BASE = 1;
   public static final int GLYPH_CLASS_LIGATURE = 2;
   public static final int GLYPH_CLASS_MARK = 3;
   public static final int GLYPH_CLASS_COMPONENT = 4;
   private GlyphClassSubtable gct;
   private MarkAttachmentSubtable mat;

   public GlyphDefinitionTable(List subtables, Map processors) {
      super((GlyphTable)null, new HashMap(0), processors);
      if (subtables != null && subtables.size() != 0) {
         Iterator var3 = subtables.iterator();

         while(var3.hasNext()) {
            Object o = var3.next();
            if (!(o instanceof GlyphDefinitionSubtable)) {
               throw new AdvancedTypographicTableFormatException("subtable must be a glyph definition subtable");
            }

            this.addSubtable((GlyphSubtable)o);
         }

         this.freezeSubtables();
      } else {
         throw new AdvancedTypographicTableFormatException("subtables must be non-empty");
      }
   }

   public GlyphSequence reorderCombiningMarks(GlyphSequence gs, int[] widths, int[][] gpa, String script, String language) {
      ScriptProcessor sp = ScriptProcessor.getInstance(script, this.processors);
      return sp.reorderCombiningMarks(this, gs, widths, gpa, script, language);
   }

   protected void addSubtable(GlyphSubtable subtable) {
      if (subtable instanceof GlyphClassSubtable) {
         this.gct = (GlyphClassSubtable)subtable;
      } else if (!(subtable instanceof AttachmentPointSubtable) && !(subtable instanceof LigatureCaretSubtable)) {
         if (!(subtable instanceof MarkAttachmentSubtable)) {
            throw new UnsupportedOperationException("unsupported glyph definition subtable type: " + subtable);
         }

         this.mat = (MarkAttachmentSubtable)subtable;
      }

   }

   public boolean isGlyphClass(int gid, int gc) {
      return this.gct != null ? this.gct.isGlyphClass(gid, gc) : false;
   }

   public int getGlyphClass(int gid) {
      return this.gct != null ? this.gct.getGlyphClass(gid) : -1;
   }

   public boolean isMarkAttachClass(int gid, int mac) {
      return this.mat != null ? this.mat.isMarkAttachClass(gid, mac) : false;
   }

   public int getMarkAttachClass(int gid) {
      return this.mat != null ? this.mat.getMarkAttachClass(gid) : -1;
   }

   public static int getLookupTypeFromName(String name) {
      String s = name.toLowerCase();
      byte t;
      if ("glyphclass".equals(s)) {
         t = 1;
      } else if ("attachmentpoint".equals(s)) {
         t = 2;
      } else if ("ligaturecaret".equals(s)) {
         t = 3;
      } else if ("markattachment".equals(s)) {
         t = 4;
      } else {
         t = -1;
      }

      return t;
   }

   public static String getLookupTypeName(int type) {
      String tn = null;
      switch (type) {
         case 1:
            tn = "glyphclass";
            break;
         case 2:
            tn = "attachmentpoint";
            break;
         case 3:
            tn = "ligaturecaret";
            break;
         case 4:
            tn = "markattachment";
            break;
         default:
            tn = "unknown";
      }

      return tn;
   }

   public static GlyphSubtable createSubtable(int type, String id, int sequence, int flags, int format, GlyphMappingTable mapping, List entries) {
      GlyphSubtable st = null;
      switch (type) {
         case 1:
            st = GlyphDefinitionTable.GlyphClassSubtable.create(id, sequence, flags, format, mapping, entries);
            break;
         case 2:
            st = GlyphDefinitionTable.AttachmentPointSubtable.create(id, sequence, flags, format, mapping, entries);
            break;
         case 3:
            st = GlyphDefinitionTable.LigatureCaretSubtable.create(id, sequence, flags, format, mapping, entries);
            break;
         case 4:
            st = GlyphDefinitionTable.MarkAttachmentSubtable.create(id, sequence, flags, format, mapping, entries);
      }

      return st;
   }

   private static class MarkAttachmentSubtableFormat1 extends MarkAttachmentSubtable {
      MarkAttachmentSubtableFormat1(String id, int sequence, int flags, int format, GlyphMappingTable mapping, List entries) {
         super(id, sequence, flags, format, mapping, entries);
      }

      public List getEntries() {
         return null;
      }

      public boolean isCompatible(GlyphSubtable subtable) {
         return subtable instanceof MarkAttachmentSubtable;
      }

      public boolean isMarkAttachClass(int gid, int mac) {
         GlyphClassMapping cm = this.getClasses();
         if (cm != null) {
            return cm.getClassIndex(gid, 0) == mac;
         } else {
            return false;
         }
      }

      public int getMarkAttachClass(int gid) {
         GlyphClassMapping cm = this.getClasses();
         return cm != null ? cm.getClassIndex(gid, 0) : -1;
      }
   }

   private abstract static class MarkAttachmentSubtable extends GlyphDefinitionSubtable {
      MarkAttachmentSubtable(String id, int sequence, int flags, int format, GlyphMappingTable mapping, List entries) {
         super(id, sequence, flags, format, mapping);
      }

      public int getType() {
         return 4;
      }

      public abstract boolean isMarkAttachClass(int var1, int var2);

      public abstract int getMarkAttachClass(int var1);

      static GlyphDefinitionSubtable create(String id, int sequence, int flags, int format, GlyphMappingTable mapping, List entries) {
         if (format == 1) {
            return new MarkAttachmentSubtableFormat1(id, sequence, flags, format, mapping, entries);
         } else {
            throw new UnsupportedOperationException();
         }
      }
   }

   private static class LigatureCaretSubtableFormat1 extends LigatureCaretSubtable {
      LigatureCaretSubtableFormat1(String id, int sequence, int flags, int format, GlyphMappingTable mapping, List entries) {
         super(id, sequence, flags, format, mapping, entries);
      }

      public List getEntries() {
         return null;
      }

      public boolean isCompatible(GlyphSubtable subtable) {
         return subtable instanceof LigatureCaretSubtable;
      }
   }

   private abstract static class LigatureCaretSubtable extends GlyphDefinitionSubtable {
      LigatureCaretSubtable(String id, int sequence, int flags, int format, GlyphMappingTable mapping, List entries) {
         super(id, sequence, flags, format, mapping);
      }

      public int getType() {
         return 3;
      }

      static GlyphDefinitionSubtable create(String id, int sequence, int flags, int format, GlyphMappingTable mapping, List entries) {
         if (format == 1) {
            return new LigatureCaretSubtableFormat1(id, sequence, flags, format, mapping, entries);
         } else {
            throw new UnsupportedOperationException();
         }
      }
   }

   private static class AttachmentPointSubtableFormat1 extends AttachmentPointSubtable {
      AttachmentPointSubtableFormat1(String id, int sequence, int flags, int format, GlyphMappingTable mapping, List entries) {
         super(id, sequence, flags, format, mapping, entries);
      }

      public List getEntries() {
         return null;
      }

      public boolean isCompatible(GlyphSubtable subtable) {
         return subtable instanceof AttachmentPointSubtable;
      }
   }

   private abstract static class AttachmentPointSubtable extends GlyphDefinitionSubtable {
      AttachmentPointSubtable(String id, int sequence, int flags, int format, GlyphMappingTable mapping, List entries) {
         super(id, sequence, flags, format, mapping);
      }

      public int getType() {
         return 2;
      }

      static GlyphDefinitionSubtable create(String id, int sequence, int flags, int format, GlyphMappingTable mapping, List entries) {
         if (format == 1) {
            return new AttachmentPointSubtableFormat1(id, sequence, flags, format, mapping, entries);
         } else {
            throw new UnsupportedOperationException();
         }
      }
   }

   private static class GlyphClassSubtableFormat1 extends GlyphClassSubtable {
      GlyphClassSubtableFormat1(String id, int sequence, int flags, int format, GlyphMappingTable mapping, List entries) {
         super(id, sequence, flags, format, mapping, entries);
      }

      public List getEntries() {
         return null;
      }

      public boolean isCompatible(GlyphSubtable subtable) {
         return subtable instanceof GlyphClassSubtable;
      }

      public boolean isGlyphClass(int gid, int gc) {
         GlyphClassMapping cm = this.getClasses();
         if (cm != null) {
            return cm.getClassIndex(gid, 0) == gc;
         } else {
            return false;
         }
      }

      public int getGlyphClass(int gid) {
         GlyphClassMapping cm = this.getClasses();
         return cm != null ? cm.getClassIndex(gid, 0) : -1;
      }
   }

   private abstract static class GlyphClassSubtable extends GlyphDefinitionSubtable {
      GlyphClassSubtable(String id, int sequence, int flags, int format, GlyphMappingTable mapping, List entries) {
         super(id, sequence, flags, format, mapping);
      }

      public int getType() {
         return 1;
      }

      public abstract boolean isGlyphClass(int var1, int var2);

      public abstract int getGlyphClass(int var1);

      static GlyphDefinitionSubtable create(String id, int sequence, int flags, int format, GlyphMappingTable mapping, List entries) {
         if (format == 1) {
            return new GlyphClassSubtableFormat1(id, sequence, flags, format, mapping, entries);
         } else {
            throw new UnsupportedOperationException();
         }
      }
   }
}
