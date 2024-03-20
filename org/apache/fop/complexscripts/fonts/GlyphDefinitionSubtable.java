package org.apache.fop.complexscripts.fonts;

public abstract class GlyphDefinitionSubtable extends GlyphSubtable implements GlyphDefinition {
   protected GlyphDefinitionSubtable(String id, int sequence, int flags, int format, GlyphMappingTable mapping) {
      super(id, sequence, flags, format, mapping);
   }

   public int getTableType() {
      return 5;
   }

   public String getTypeName() {
      return GlyphDefinitionTable.getLookupTypeName(this.getType());
   }

   public boolean usesReverseScan() {
      return false;
   }

   public boolean hasDefinition(int gi) {
      GlyphCoverageMapping cvm;
      if ((cvm = this.getCoverage()) != null && cvm.getCoverageIndex(gi) >= 0) {
         return true;
      } else {
         GlyphClassMapping clm;
         return (clm = this.getClasses()) != null && clm.getClassIndex(gi, 0) >= 0;
      }
   }
}
