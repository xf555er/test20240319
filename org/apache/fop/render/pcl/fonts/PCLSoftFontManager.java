package org.apache.fop.render.pcl.fonts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.fop.fonts.CustomFont;
import org.apache.fop.fonts.Typeface;
import org.apache.fop.fonts.truetype.GlyfTable;
import org.apache.fop.fonts.truetype.OFDirTabEntry;
import org.apache.fop.fonts.truetype.OFMtxEntry;
import org.apache.fop.fonts.truetype.OFTableName;
import org.apache.fop.render.java2d.CustomFontMetricsMapper;

public class PCLSoftFontManager {
   private Map fontReaderMap;
   private PCLFontReader fontReader;
   private List fonts = new ArrayList();
   private static final int SOFT_FONT_SIZE = 255;

   public PCLSoftFontManager(Map fontReaderMap) {
      this.fontReaderMap = fontReaderMap;
   }

   public ByteArrayOutputStream makeSoftFont(Typeface font, String text) throws IOException {
      if (!this.fontReaderMap.containsKey(font)) {
         this.fontReaderMap.put(font, PCLFontReaderFactory.createInstance(font));
      }

      this.fontReader = (PCLFontReader)this.fontReaderMap.get(font);
      List mappedGlyphs = this.mapFontGlyphs(font);
      if (mappedGlyphs.isEmpty()) {
         mappedGlyphs.add(new HashMap());
      }

      if (this.fontReader == null) {
         return null;
      } else {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         PCLSoftFont softFont = null;
         Iterator var6 = mappedGlyphs.iterator();

         while(var6.hasNext()) {
            Map glyphSet = (Map)var6.next();
            softFont = this.getSoftFont(font, text, mappedGlyphs, softFont);
            softFont.setMappedChars(glyphSet);
            this.writeFontID(softFont.getFontID(), baos);
            this.writeFontHeader(softFont.getMappedChars(), baos);
            softFont.setCharacterOffsets(this.fontReader.getCharacterOffsets());
            softFont.setOpenFont(this.fontReader.getFontFile());
            softFont.setReader(this.fontReader.getFontFileReader());
            softFont.setMtxCharIndexes(this.fontReader.scanMtxCharacters());
         }

         return baos;
      }
   }

   private PCLSoftFont getSoftFont(Typeface font, String text, List mappedGlyphs, PCLSoftFont last) {
      Iterator fontIterator;
      PCLSoftFont sftFont;
      if (text == null) {
         fontIterator = this.fonts.iterator();

         while(fontIterator.hasNext()) {
            sftFont = (PCLSoftFont)fontIterator.next();
            if (sftFont.getTypeface().equals(font)) {
               fontIterator.remove();
               return sftFont;
            }
         }
      }

      fontIterator = this.fonts.iterator();

      do {
         if (!fontIterator.hasNext()) {
            PCLSoftFont f = new PCLSoftFont(this.fonts.size() + 1, font, ((Map)mappedGlyphs.get(0)).size() != 0);
            this.fonts.add(f);
            return f;
         }

         sftFont = (PCLSoftFont)fontIterator.next();
      } while(!sftFont.getTypeface().equals(font) || sftFont == last || sftFont.getCharCount() + this.countNonMatches(sftFont, text) >= 255);

      return sftFont;
   }

   private List mapFontGlyphs(Typeface tf) throws IOException {
      List mappedGlyphs = new ArrayList();
      if (tf instanceof CustomFontMetricsMapper) {
         CustomFontMetricsMapper fontMetrics = (CustomFontMetricsMapper)tf;
         CustomFont customFont = (CustomFont)fontMetrics.getRealFont();
         mappedGlyphs = this.mapGlyphs(customFont.getUsedGlyphs(), customFont);
      }

      return (List)mappedGlyphs;
   }

   private List mapGlyphs(Map usedGlyphs, CustomFont font) throws IOException {
      int charCount = 32;
      int charCountComposite = 32;
      List mappedGlyphs = new ArrayList();
      Map fontGlyphs = new HashMap();
      Map fontGlyphsComposite = new HashMap();
      Iterator var8 = usedGlyphs.entrySet().iterator();

      while(var8.hasNext()) {
         Map.Entry entry = (Map.Entry)var8.next();
         int glyphID = (Integer)entry.getKey();
         if (glyphID != 0) {
            char unicode = font.getUnicodeFromGID(glyphID);
            if (charCount > 255) {
               mappedGlyphs.add(fontGlyphs);
               charCount = 32;
               fontGlyphs = new HashMap();
            }

            if (this.isComposite(font, unicode)) {
               fontGlyphsComposite.put(unicode, charCountComposite++);
            } else {
               fontGlyphs.put(unicode, charCount++);
            }
         }
      }

      if (fontGlyphs.size() > 0) {
         mappedGlyphs.add(fontGlyphs);
      }

      if (fontGlyphsComposite.size() > 0) {
         mappedGlyphs.add(fontGlyphsComposite);
      }

      return mappedGlyphs;
   }

   private boolean isComposite(CustomFont customFont, int unicode) throws IOException {
      OFDirTabEntry glyfTableInfo = this.fontReader.getFontFile().getDirectoryEntry(OFTableName.GLYF);
      if (glyfTableInfo == null) {
         return false;
      } else {
         List mtx = this.fontReader.getFontFile().getMtx();
         Map subsetGlyphs = customFont.getUsedGlyphs();
         GlyfTable glyfTable = new GlyfTable(this.fontReader.getFontFileReader(), (OFMtxEntry[])mtx.toArray(new OFMtxEntry[mtx.size()]), glyfTableInfo, subsetGlyphs);
         Map mtxCharacters = this.fontReader.scanMtxCharacters();
         if (mtxCharacters.containsKey(unicode)) {
            int mtxChar = (Integer)mtxCharacters.get(unicode);
            return glyfTable.isComposite(mtxChar);
         } else {
            return false;
         }
      }
   }

   private void writeFontID(int fontID, OutputStream os) throws IOException {
      os.write(this.assignFontID(fontID));
   }

   public byte[] assignFontID(int fontID) throws IOException {
      return PCLByteWriterUtil.writeCommand(String.format("*c%dD", fontID));
   }

   private void writeFontHeader(Map mappedGlyphs, OutputStream os) throws IOException {
      ByteArrayOutputStream header = new ByteArrayOutputStream();
      header.write(PCLByteWriterUtil.unsignedInt(this.fontReader.getDescriptorSize()));
      header.write(PCLByteWriterUtil.unsignedByte(this.fontReader.getHeaderFormat()));
      header.write(PCLByteWriterUtil.unsignedByte(this.fontReader.getFontType()));
      header.write(PCLByteWriterUtil.unsignedByte(this.fontReader.getStyleMSB()));
      header.write(0);
      header.write(PCLByteWriterUtil.unsignedInt(this.fontReader.getBaselinePosition()));
      header.write(PCLByteWriterUtil.unsignedInt(this.fontReader.getCellWidth()));
      header.write(PCLByteWriterUtil.unsignedInt(this.fontReader.getCellHeight()));
      header.write(PCLByteWriterUtil.unsignedByte(this.fontReader.getOrientation()));
      header.write(this.fontReader.getSpacing());
      header.write(PCLByteWriterUtil.unsignedInt(this.fontReader.getSymbolSet()));
      header.write(PCLByteWriterUtil.unsignedInt(this.fontReader.getPitch()));
      header.write(PCLByteWriterUtil.unsignedInt(this.fontReader.getHeight()));
      header.write(PCLByteWriterUtil.unsignedInt(this.fontReader.getXHeight()));
      header.write(PCLByteWriterUtil.signedByte(this.fontReader.getWidthType()));
      header.write(PCLByteWriterUtil.unsignedByte(this.fontReader.getStyleLSB()));
      header.write(PCLByteWriterUtil.signedByte(this.fontReader.getStrokeWeight()));
      header.write(PCLByteWriterUtil.unsignedByte(this.fontReader.getTypefaceLSB()));
      header.write(PCLByteWriterUtil.unsignedByte(this.fontReader.getTypefaceMSB()));
      header.write(PCLByteWriterUtil.unsignedByte(this.fontReader.getSerifStyle()));
      header.write(PCLByteWriterUtil.unsignedByte(this.fontReader.getQuality()));
      header.write(PCLByteWriterUtil.signedByte(this.fontReader.getPlacement()));
      header.write(PCLByteWriterUtil.signedByte(this.fontReader.getUnderlinePosition()));
      header.write(PCLByteWriterUtil.unsignedByte(this.fontReader.getUnderlineThickness()));
      header.write(PCLByteWriterUtil.unsignedInt(this.fontReader.getTextHeight()));
      header.write(PCLByteWriterUtil.unsignedInt(this.fontReader.getTextWidth()));
      header.write(PCLByteWriterUtil.unsignedInt(this.fontReader.getFirstCode()));
      header.write(PCLByteWriterUtil.unsignedInt(this.fontReader.getLastCode()));
      header.write(PCLByteWriterUtil.unsignedByte(this.fontReader.getPitchExtended()));
      header.write(PCLByteWriterUtil.unsignedByte(this.fontReader.getHeightExtended()));
      header.write(PCLByteWriterUtil.unsignedInt(this.fontReader.getCapHeight()));
      header.write(PCLByteWriterUtil.unsignedLongInt(this.fontReader.getFontNumber()));
      header.write(PCLByteWriterUtil.padBytes(this.fontReader.getFontName().getBytes("US-ASCII"), 16, 32));
      header.write(PCLByteWriterUtil.unsignedInt(this.fontReader.getScaleFactor()));
      header.write(PCLByteWriterUtil.signedInt(this.fontReader.getMasterUnderlinePosition()));
      header.write(PCLByteWriterUtil.unsignedInt(this.fontReader.getMasterUnderlineThickness()));
      header.write(PCLByteWriterUtil.unsignedByte(this.fontReader.getFontScalingTechnology()));
      header.write(PCLByteWriterUtil.unsignedByte(this.fontReader.getVariety()));
      this.writeSegmentedFontData(header, mappedGlyphs);
      os.write(this.getFontHeaderCommand(header.size()));
      os.write(header.toByteArray());
   }

   private void writeSegmentedFontData(ByteArrayOutputStream header, Map mappedGlyphs) throws IOException {
      List fontSegments = this.fontReader.getFontSegments(mappedGlyphs);
      Iterator var4 = fontSegments.iterator();

      while(var4.hasNext()) {
         PCLFontSegment segment = (PCLFontSegment)var4.next();
         this.writeFontSegment(header, segment);
      }

      header.write(0);
      long sum = 0L;
      byte[] headerBytes = header.toByteArray();

      int remainder;
      for(remainder = 64; remainder < headerBytes.length; ++remainder) {
         sum += (long)headerBytes[remainder];
      }

      remainder = (int)(sum % 256L);
      header.write(256 - remainder);
   }

   private byte[] getFontHeaderCommand(int headerSize) throws IOException {
      return PCLByteWriterUtil.writeCommand(String.format(")s%dW", headerSize));
   }

   private void writeFontSegment(ByteArrayOutputStream header, PCLFontSegment segment) throws IOException {
      header.write(PCLByteWriterUtil.unsignedInt(segment.getIdentifier().getValue()));
      header.write(PCLByteWriterUtil.unsignedInt(segment.getData().length));
      header.write(segment.getData());
   }

   public PCLSoftFont getSoftFont(Typeface font, String text) {
      Iterator var3 = this.fonts.iterator();

      PCLSoftFont sftFont;
      do {
         if (!var3.hasNext()) {
            return null;
         }

         sftFont = (PCLSoftFont)var3.next();
      } while(!sftFont.getTypeface().equals(font) || sftFont.getCharCount() + this.countNonMatches(sftFont, text) >= 255);

      return sftFont;
   }

   public PCLSoftFont getSoftFontFromID(int index) {
      return (PCLSoftFont)this.fonts.get(index - 1);
   }

   private int countNonMatches(PCLSoftFont font, String text) {
      int result = 0;
      char[] var4 = text.toCharArray();
      int var5 = var4.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         char ch = var4[var6];
         int value = font.getUnicodeCodePoint(ch);
         if (value == -1) {
            ++result;
         }
      }

      return result;
   }

   public int getSoftFontID(Typeface tf) throws IOException {
      PCLSoftFont font = this.getSoftFont(tf, "");

      for(int i = 0; i < this.fonts.size(); ++i) {
         if (((PCLSoftFont)this.fonts.get(i)).equals(font)) {
            return i + 1;
         }
      }

      return -1;
   }

   public List getTextSegments(String text, Typeface font) {
      List textSegments = new ArrayList();
      int curFontID = -1;
      String current = "";
      char[] var6 = text.toCharArray();
      int var7 = var6.length;

      for(int var8 = 0; var8 < var7; ++var8) {
         char ch = var6[var8];
         if (ch == 160) {
            ch = ' ';
         }

         Iterator var10 = this.fonts.iterator();

         while(var10.hasNext()) {
            PCLSoftFont softFont = (PCLSoftFont)var10.next();
            if (curFontID == -1) {
               curFontID = softFont.getFontID();
            }

            if (softFont.getCharIndex(ch) != -1 && softFont.getTypeface().equals(font)) {
               if (current.length() > 0 && curFontID != softFont.getFontID()) {
                  textSegments.add(new PCLTextSegment(curFontID, current));
                  current = "";
                  curFontID = softFont.getFontID();
               }

               if (curFontID != softFont.getFontID()) {
                  curFontID = softFont.getFontID();
               }

               current = current + ch;
               break;
            }
         }
      }

      if (current.length() > 0) {
         textSegments.add(new PCLTextSegment(curFontID, current));
      }

      return textSegments;
   }

   public static class PCLTextSegment {
      private String text;
      private int fontID;

      public PCLTextSegment(int fontID, String text) {
         this.text = text;
         this.fontID = fontID;
      }

      public String getText() {
         return this.text;
      }

      public int getFontID() {
         return this.fontID;
      }
   }
}
