package org.apache.fop.render.pcl.fonts.truetype;

import java.io.IOException;
import org.apache.fop.fonts.truetype.FontFileReader;

public class PCLTTFPCLTFontTable extends PCLTTFTable {
   private long version;
   private long fontNumber;
   private int pitch;
   private int xHeight;
   private int style;
   private int typeFamily;
   private int capHeight;
   private int symbolSet;
   private String typeface;
   private String characterComplement;
   private String filename;
   private int strokeWeight;
   private int widthType;
   private int serifStyle;

   public PCLTTFPCLTFontTable(FontFileReader in) throws IOException {
      super(in);
      this.version = this.reader.readTTFULong();
      this.fontNumber = this.reader.readTTFULong();
      this.pitch = this.reader.readTTFUShort();
      this.xHeight = this.reader.readTTFUShort();
      this.style = this.reader.readTTFUShort();
      this.typeFamily = this.reader.readTTFUShort();
      this.capHeight = this.reader.readTTFUShort();
      this.symbolSet = this.reader.readTTFUShort();
      this.typeface = this.reader.readTTFString(16);
      this.characterComplement = this.reader.readTTFString(8);
      this.filename = this.reader.readTTFString(6);
      this.strokeWeight = this.reader.readTTFUShort();
      this.widthType = this.reader.readTTFUShort();
      this.serifStyle = this.reader.readTTFUByte();
   }

   public long getVersion() {
      return this.version;
   }

   public long getFontNumber() {
      return this.fontNumber;
   }

   public int getPitch() {
      return this.pitch;
   }

   public int getXHeight() {
      return this.xHeight;
   }

   public int getStyle() {
      return this.style;
   }

   public int getTypeFamily() {
      return this.typeFamily;
   }

   public int getCapHeight() {
      return this.capHeight;
   }

   public int getSymbolSet() {
      return this.symbolSet;
   }

   public String getTypeface() {
      return this.typeface;
   }

   public String getCharacterComplement() {
      return this.characterComplement;
   }

   public String getFilename() {
      return this.filename;
   }

   public int getStrokeWeight() {
      return this.strokeWeight;
   }

   public int getWidthType() {
      return this.widthType;
   }

   public int getSerifStyle() {
      return this.serifStyle;
   }
}
