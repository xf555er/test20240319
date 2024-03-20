package org.apache.fop.render.pcl.fonts.truetype;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.fop.fonts.truetype.GlyfTable;
import org.apache.fop.fonts.truetype.OFDirTabEntry;
import org.apache.fop.fonts.truetype.OFMtxEntry;
import org.apache.fop.fonts.truetype.OFTableName;
import org.apache.fop.fonts.truetype.TTFFile;
import org.apache.fop.render.pcl.fonts.PCLCharacterDefinition;
import org.apache.fop.render.pcl.fonts.PCLCharacterWriter;
import org.apache.fop.render.pcl.fonts.PCLSoftFont;

public class PCLTTFCharacterWriter extends PCLCharacterWriter {
   private List mtx;
   private OFDirTabEntry tabEntry;

   public PCLTTFCharacterWriter(PCLSoftFont softFont) throws IOException {
      super(softFont);
   }

   public byte[] writeCharacterDefinitions(String text) throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      char[] var3 = text.toCharArray();
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         char ch = var3[var5];
         if (!this.font.hasPreviouslyWritten(ch)) {
            PCLCharacterDefinition pclChar = this.getCharacterDefinition(ch);
            this.writePCLCharacter(baos, pclChar);
            List compositeGlyphs = pclChar.getCompositeGlyphs();
            Iterator var10 = compositeGlyphs.iterator();

            while(var10.hasNext()) {
               PCLCharacterDefinition composite = (PCLCharacterDefinition)var10.next();
               this.writePCLCharacter(baos, composite);
            }
         }
      }

      return baos.toByteArray();
   }

   private void writePCLCharacter(ByteArrayOutputStream baos, PCLCharacterDefinition pclChar) throws IOException {
      baos.write(pclChar.getCharacterCommand());
      baos.write(pclChar.getCharacterDefinitionCommand());
      baos.write(pclChar.getData());
   }

   private PCLCharacterDefinition getCharacterDefinition(int unicode) throws IOException {
      if (this.mtx == null) {
         this.mtx = this.openFont.getMtx();
         this.tabEntry = this.openFont.getDirectoryEntry(OFTableName.GLYF);
      }

      if (this.openFont.seekTab(this.fontReader, OFTableName.GLYF, 0L)) {
         int charIndex = this.font.getMtxCharIndex(unicode);
         if (charIndex == 0) {
            charIndex = this.font.getCmapGlyphIndex(unicode);
         }

         Map subsetGlyphs = new HashMap();
         subsetGlyphs.put(charIndex, 1);
         byte[] glyphData = this.getGlyphData(charIndex);
         this.font.writeCharacter(unicode);
         PCLCharacterDefinition newChar = new PCLCharacterDefinition(this.font.getCharCode((char)unicode), PCLCharacterDefinition.PCLCharacterFormat.TrueType, PCLCharacterDefinition.PCLCharacterClass.TrueType, glyphData, false);
         GlyfTable glyfTable = new GlyfTable(this.fontReader, (OFMtxEntry[])this.mtx.toArray(new OFMtxEntry[this.mtx.size()]), this.tabEntry, subsetGlyphs);
         if (glyfTable.isComposite(charIndex)) {
            Set composites = glyfTable.retrieveComposedGlyphs(charIndex);
            Iterator var8 = composites.iterator();

            while(var8.hasNext()) {
               Integer compositeIndex = (Integer)var8.next();
               byte[] compositeData = this.getGlyphData(compositeIndex);
               newChar.addCompositeGlyph(new PCLCharacterDefinition(compositeIndex, PCLCharacterDefinition.PCLCharacterFormat.TrueType, PCLCharacterDefinition.PCLCharacterClass.TrueType, compositeData, true));
            }
         }

         return newChar;
      } else {
         return null;
      }
   }

   private byte[] getGlyphData(int charIndex) throws IOException {
      OFMtxEntry entry = (OFMtxEntry)this.mtx.get(charIndex);
      int nextOffset = false;
      int nextOffset;
      if (charIndex < this.mtx.size() - 1) {
         OFMtxEntry nextEntry = (OFMtxEntry)this.mtx.get(charIndex + 1);
         nextOffset = (int)nextEntry.getOffset();
      } else {
         nextOffset = (int)((TTFFile)this.openFont).getLastGlyfLocation();
      }

      int glyphOffset = (int)entry.getOffset();
      int glyphLength = nextOffset - glyphOffset;
      byte[] glyphData = new byte[0];
      if (glyphLength > 0) {
         glyphData = this.fontReader.getBytes((int)this.tabEntry.getOffset() + glyphOffset, glyphLength);
      }

      return glyphData;
   }
}
