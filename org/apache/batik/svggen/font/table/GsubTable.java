package org.apache.batik.svggen.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;

public class GsubTable implements Table, LookupSubtableFactory {
   private ScriptList scriptList;
   private FeatureList featureList;
   private LookupList lookupList;

   protected GsubTable(DirectoryEntry de, RandomAccessFile raf) throws IOException {
      raf.seek((long)de.getOffset());
      raf.readInt();
      int scriptListOffset = raf.readUnsignedShort();
      int featureListOffset = raf.readUnsignedShort();
      int lookupListOffset = raf.readUnsignedShort();
      this.scriptList = new ScriptList(raf, de.getOffset() + scriptListOffset);
      this.featureList = new FeatureList(raf, de.getOffset() + featureListOffset);
      this.lookupList = new LookupList(raf, de.getOffset() + lookupListOffset, this);
   }

   public LookupSubtable read(int type, RandomAccessFile raf, int offset) throws IOException {
      LookupSubtable s = null;
      switch (type) {
         case 1:
            s = SingleSubst.read(raf, offset);
         case 2:
         case 3:
         case 5:
         case 6:
         default:
            break;
         case 4:
            s = LigatureSubst.read(raf, offset);
      }

      return (LookupSubtable)s;
   }

   public int getType() {
      return 1196643650;
   }

   public ScriptList getScriptList() {
      return this.scriptList;
   }

   public FeatureList getFeatureList() {
      return this.featureList;
   }

   public LookupList getLookupList() {
      return this.lookupList;
   }

   public String toString() {
      return "GSUB";
   }
}
