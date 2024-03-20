package org.apache.batik.svggen.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;

public class Lookup {
   public static final int IGNORE_BASE_GLYPHS = 2;
   public static final int IGNORE_BASE_LIGATURES = 4;
   public static final int IGNORE_BASE_MARKS = 8;
   public static final int MARK_ATTACHMENT_TYPE = 65280;
   private int type;
   private int flag;
   private int subTableCount;
   private int[] subTableOffsets;
   private LookupSubtable[] subTables;

   public Lookup(LookupSubtableFactory factory, RandomAccessFile raf, int offset) throws IOException {
      raf.seek((long)offset);
      this.type = raf.readUnsignedShort();
      this.flag = raf.readUnsignedShort();
      this.subTableCount = raf.readUnsignedShort();
      this.subTableOffsets = new int[this.subTableCount];
      this.subTables = new LookupSubtable[this.subTableCount];

      int i;
      for(i = 0; i < this.subTableCount; ++i) {
         this.subTableOffsets[i] = raf.readUnsignedShort();
      }

      for(i = 0; i < this.subTableCount; ++i) {
         this.subTables[i] = factory.read(this.type, raf, offset + this.subTableOffsets[i]);
      }

   }

   public int getType() {
      return this.type;
   }

   public int getSubtableCount() {
      return this.subTableCount;
   }

   public LookupSubtable getSubtable(int i) {
      return this.subTables[i];
   }
}
