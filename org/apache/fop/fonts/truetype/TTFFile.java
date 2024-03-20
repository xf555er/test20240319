package org.apache.fop.fonts.truetype;

import java.io.IOException;

public class TTFFile extends OpenFont {
   public TTFFile() {
      this(true, false);
   }

   public TTFFile(boolean useKerning, boolean useAdvanced) {
      super(useKerning, useAdvanced);
   }

   protected void readName() throws IOException {
      this.seekTab(this.fontFile, OFTableName.NAME, 2L);
      int i = this.fontFile.getCurrentPos();
      int n = this.fontFile.readTTFUShort();
      int j = this.fontFile.readTTFUShort() + i - 2;

      for(i += 4; n-- > 0; i += 12) {
         this.fontFile.seekSet((long)i);
         int platformID = this.fontFile.readTTFUShort();
         int encodingID = this.fontFile.readTTFUShort();
         int languageID = this.fontFile.readTTFUShort();
         int k = this.fontFile.readTTFUShort();
         int l = this.fontFile.readTTFUShort();
         if ((platformID == 1 || platformID == 3) && (encodingID == 0 || encodingID == 1)) {
            this.fontFile.seekSet((long)(j + this.fontFile.readTTFUShort()));
            String txt;
            if (platformID == 3) {
               txt = this.fontFile.readTTFString(l, encodingID);
            } else {
               txt = this.fontFile.readTTFString(l);
            }

            if (this.log.isDebugEnabled()) {
               this.log.debug(platformID + " " + encodingID + " " + languageID + " " + k + " " + txt);
            }

            switch (k) {
               case 0:
                  if (this.notice.length() == 0) {
                     this.notice = txt;
                  }
                  break;
               case 1:
               case 16:
                  this.familyNames.add(txt);
                  break;
               case 2:
                  if (this.subFamilyName.length() == 0) {
                     this.subFamilyName = txt;
                  }
               case 3:
               case 5:
               case 7:
               case 8:
               case 9:
               case 10:
               case 11:
               case 12:
               case 13:
               case 14:
               case 15:
               default:
                  break;
               case 4:
                  if (this.fullName.length() == 0 || platformID == 3 && languageID == 1033) {
                     this.fullName = txt;
                  }
                  break;
               case 6:
                  if (this.postScriptName.length() == 0) {
                     this.postScriptName = txt;
                  }
            }
         }
      }

   }

   private void readGlyf() throws IOException {
      OFDirTabEntry dirTab = (OFDirTabEntry)this.dirTabs.get(OFTableName.GLYF);
      if (dirTab == null) {
         throw new IOException("glyf table not found, cannot continue");
      } else {
         for(int i = 0; i < this.numberOfGlyphs - 1; ++i) {
            if (this.mtxTab[i].getOffset() != this.mtxTab[i + 1].getOffset()) {
               this.fontFile.seekSet(dirTab.getOffset() + this.mtxTab[i].getOffset());
               this.fontFile.skip(2L);
               int[] bbox = new int[]{this.fontFile.readTTFShort(), this.fontFile.readTTFShort(), this.fontFile.readTTFShort(), this.fontFile.readTTFShort()};
               this.mtxTab[i].setBoundingBox(bbox);
            } else {
               this.mtxTab[i].setBoundingBox(this.mtxTab[0].getBoundingBox());
            }
         }

         long n = ((OFDirTabEntry)this.dirTabs.get(OFTableName.GLYF)).getOffset();

         for(int i = 0; i < this.numberOfGlyphs; ++i) {
            if (i + 1 < this.mtxTab.length && this.mtxTab[i].getOffset() == this.mtxTab[i + 1].getOffset()) {
               int bbox0 = this.mtxTab[0].getBoundingBox()[0];
               int[] bbox = new int[]{bbox0, bbox0, bbox0, bbox0};
               this.mtxTab[i].setBoundingBox(bbox);
            } else {
               this.fontFile.seekSet(n + this.mtxTab[i].getOffset());
               this.fontFile.skip(2L);
               int[] bbox = new int[]{this.fontFile.readTTFShort(), this.fontFile.readTTFShort(), this.fontFile.readTTFShort(), this.fontFile.readTTFShort()};
               this.mtxTab[i].setBoundingBox(bbox);
            }

            if (this.log.isTraceEnabled()) {
               this.log.trace(this.mtxTab[i].toString(this));
            }
         }

      }
   }

   protected void updateBBoxAndOffset() throws IOException {
      this.readIndexToLocation();
      this.readGlyf();
   }

   protected final void readIndexToLocation() throws IOException {
      if (!this.seekTab(this.fontFile, OFTableName.LOCA, 0L)) {
         throw new IOException("'loca' table not found, happens when the font file doesn't contain TrueType outlines (trying to read an OpenType CFF font maybe?)");
      } else {
         for(int i = 0; i < this.numberOfGlyphs; ++i) {
            this.mtxTab[i].setOffset(this.locaFormat == 1 ? this.fontFile.readTTFULong() : (long)(this.fontFile.readTTFUShort() << 1));
         }

         this.lastLoca = this.locaFormat == 1 ? this.fontFile.readTTFULong() : (long)(this.fontFile.readTTFUShort() << 1);
      }
   }

   public long getLastGlyfLocation() {
      return this.lastLoca;
   }

   protected void initializeFont(FontFileReader in) throws IOException {
      this.fontFile = in;
   }
}
