package org.apache.fop.render.pcl.fonts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PCLCharacterDefinition {
   private int charCode;
   private int charDefinitionSize;
   private byte[] glyfData;
   private boolean hasContinuation;
   private PCLCharacterFormat charFormat;
   private PCLCharacterClass charClass;
   private List composites;
   private boolean isComposite;

   public PCLCharacterDefinition(int charCode, PCLCharacterFormat charFormat, PCLCharacterClass charClass, byte[] glyfData, boolean isComposite) {
      this.charCode = charCode;
      this.charFormat = charFormat;
      this.charClass = charClass;
      this.glyfData = glyfData;
      this.isComposite = isComposite;
      this.charDefinitionSize = glyfData.length + 4 + 2 + 2;
      this.hasContinuation = this.charDefinitionSize > 32767;
      this.composites = new ArrayList();
   }

   public byte[] getCharacterCommand() throws IOException {
      return PCLByteWriterUtil.writeCommand(String.format("*c%dE", this.isComposite ? '\uffff' : this.charCode));
   }

   public byte[] getCharacterDefinitionCommand() throws IOException {
      return PCLByteWriterUtil.writeCommand(String.format("(s%dW", 10 + this.glyfData.length));
   }

   public byte[] getData() throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      int remainder;
      if (!this.hasContinuation) {
         this.writeCharacterDescriptorHeader(0, baos);
         baos.write(this.glyfData);
      } else {
         int continuations = this.glyfData.length / 32767;

         for(int i = 0; i < continuations; ++i) {
            this.writeCharacterDescriptorHeader(i == 0 ? 0 : 1, baos);
            int continuationStart = i * 32767;
            remainder = continuationStart - this.glyfData.length < 32767 ? continuationStart - this.glyfData.length : 32767;
            baos.write(this.glyfData, continuationStart, remainder);
         }
      }

      baos.write(0);
      byte[] charBytes = baos.toByteArray();
      long sum = 0L;

      for(remainder = 4; remainder < charBytes.length; ++remainder) {
         sum += (long)charBytes[remainder];
      }

      remainder = (int)(sum % 256L);
      baos.write(256 - remainder);
      return baos.toByteArray();
   }

   private void writeCharacterDescriptorHeader(int continuation, ByteArrayOutputStream baos) throws IOException {
      baos.write(PCLByteWriterUtil.unsignedByte(this.charFormat.getValue()));
      baos.write(continuation);
      baos.write(PCLByteWriterUtil.unsignedByte(2));
      baos.write(PCLByteWriterUtil.unsignedByte(this.charClass.getValue()));
      baos.write(PCLByteWriterUtil.unsignedInt(this.glyfData.length + 4));
      baos.write(PCLByteWriterUtil.unsignedInt(this.charCode));
   }

   public void addCompositeGlyph(PCLCharacterDefinition composite) {
      this.composites.add(composite);
   }

   public List getCompositeGlyphs() {
      return this.composites;
   }

   public static enum PCLCharacterClass {
      Bitmap(1),
      CompressedBitmap(2),
      Contour_Intellifont(3),
      Compound_Contour_Intellifont(4),
      TrueType(15);

      private int value;

      private PCLCharacterClass(int value) {
         this.value = value;
      }

      public int getValue() {
         return this.value;
      }
   }

   public static enum PCLCharacterFormat {
      LaserJet_Raster(4),
      Intellifont(10),
      TrueType(15);

      private int value;

      private PCLCharacterFormat(int value) {
         this.value = value;
      }

      public int getValue() {
         return this.value;
      }
   }
}
