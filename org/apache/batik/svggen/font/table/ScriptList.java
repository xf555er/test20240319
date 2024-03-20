package org.apache.batik.svggen.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;

public class ScriptList {
   private int scriptCount = 0;
   private ScriptRecord[] scriptRecords;
   private Script[] scripts;

   protected ScriptList(RandomAccessFile raf, int offset) throws IOException {
      raf.seek((long)offset);
      this.scriptCount = raf.readUnsignedShort();
      this.scriptRecords = new ScriptRecord[this.scriptCount];
      this.scripts = new Script[this.scriptCount];

      int i;
      for(i = 0; i < this.scriptCount; ++i) {
         this.scriptRecords[i] = new ScriptRecord(raf);
      }

      for(i = 0; i < this.scriptCount; ++i) {
         this.scripts[i] = new Script(raf, offset + this.scriptRecords[i].getOffset());
      }

   }

   public int getScriptCount() {
      return this.scriptCount;
   }

   public ScriptRecord getScriptRecord(int i) {
      return this.scriptRecords[i];
   }

   public Script findScript(String tag) {
      if (tag.length() != 4) {
         return null;
      } else {
         int tagVal = tag.charAt(0) << 24 | tag.charAt(1) << 16 | tag.charAt(2) << 8 | tag.charAt(3);

         for(int i = 0; i < this.scriptCount; ++i) {
            if (this.scriptRecords[i].getTag() == tagVal) {
               return this.scripts[i];
            }
         }

         return null;
      }
   }
}
