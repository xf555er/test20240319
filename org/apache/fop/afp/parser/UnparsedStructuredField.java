package org.apache.fop.afp.parser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;

public final class UnparsedStructuredField {
   private final Introducer introducer;
   private final byte[] extData;
   private final byte[] data;

   UnparsedStructuredField(Introducer introducer, byte[] data, byte[] extData) {
      this.introducer = introducer;
      this.data = data;
      if (extData != null) {
         this.extData = extData;
      } else {
         this.extData = null;
      }

   }

   public String toString() {
      StringBuffer sb = new StringBuffer("Structured Field: ");
      sb.append(Integer.toHexString(this.getSfTypeID()).toUpperCase());
      sb.append(", len=");
      sb.append((new DecimalFormat("00000")).format((long)this.getSfLength()));
      sb.append(" ").append(this.getTypeCodeAsString());
      sb.append(" ").append(this.getCategoryCodeAsString());
      if (this.isSfiExtensionPresent()) {
         sb.append(", SFI extension present");
      }

      if (this.isSfiSegmentedData()) {
         sb.append(", segmented data");
      }

      if (this.isSfiPaddingPresent()) {
         sb.append(", with padding");
      }

      return sb.toString();
   }

   private String getTypeCodeAsString() {
      switch (this.getSfTypeCode() & 255) {
         case 160:
            return "Attribute";
         case 162:
            return "CopyCount";
         case 166:
            return "Descriptor";
         case 167:
            return "Control";
         case 168:
            return "Begin";
         case 169:
            return "End";
         case 171:
            return "Map";
         case 172:
            return "Position";
         case 173:
            return "Process";
         case 175:
            return "Include";
         case 176:
            return "Table";
         case 177:
            return "Migration";
         case 178:
            return "Variable";
         case 180:
            return "Link";
         case 238:
            return "Data";
         default:
            return "Unknown:" + Integer.toHexString(this.getSfTypeCode()).toUpperCase();
      }
   }

   private String getCategoryCodeAsString() {
      switch (this.getSfCategoryCode() & 255) {
         case 95:
            return "Page Segment";
         case 107:
            return "Object Area";
         case 119:
            return "Color Attribute Table";
         case 123:
            return "IM Image";
         case 136:
            return "Medium";
         case 137:
            return "Font";
         case 138:
            return "Coded Font";
         case 144:
            return "Process Element";
         case 146:
            return "Object Container";
         case 155:
            return "Presentation Text";
         case 167:
            return "Index";
         case 168:
            return "Document";
         case 173:
            return "Page Group";
         case 175:
            return "Page";
         case 187:
            return "Graphics";
         case 195:
            return "Data Resource";
         case 196:
            return "Document Environment Group (DEG)";
         case 198:
            return "Resource Group";
         case 199:
            return "Object Environment Group (OEG)";
         case 201:
            return "Active Environment Group (AEG)";
         case 204:
            return "Medium Map";
         case 205:
            return "Form Map";
         case 206:
            return "Name Resource";
         case 216:
            return "Page Overlay";
         case 217:
            return "Resource Environment Group (REG)";
         case 223:
            return "Overlay";
         case 234:
            return "Data Supression";
         case 235:
            return "Bar Code";
         case 238:
            return "No Operation";
         case 251:
            return "Image";
         default:
            return "Unknown:" + Integer.toHexString(this.getSfTypeCode()).toUpperCase();
      }
   }

   public short getSfLength() {
      return this.introducer.length;
   }

   public int getSfTypeID() {
      return (this.getSfClassCode() & 255) << 16 | (this.getSfTypeCode() & 255) << 8 | this.getSfCategoryCode() & 255;
   }

   public byte getSfClassCode() {
      return this.introducer.classCode;
   }

   public byte getSfTypeCode() {
      return this.introducer.typeCode;
   }

   public byte getSfCategoryCode() {
      return this.introducer.categoryCode;
   }

   public boolean isSfiExtensionPresent() {
      return this.introducer.extensionPresent && this.extData != null;
   }

   public boolean isSfiSegmentedData() {
      return this.introducer.segmentedData;
   }

   public boolean isSfiPaddingPresent() {
      return this.introducer.paddingPresent;
   }

   public short getExtLength() {
      return this.extData != null ? (short)(this.extData.length + 1) : 0;
   }

   byte[] getExtData() {
      if (this.extData == null) {
         return new byte[0];
      } else {
         byte[] rtn = new byte[this.extData.length];
         System.arraycopy(this.extData, 0, rtn, 0, rtn.length);
         return rtn;
      }
   }

   public byte[] getData() {
      if (this.data == null) {
         return new byte[0];
      } else {
         byte[] rtn = new byte[this.data.length];
         System.arraycopy(this.data, 0, rtn, 0, rtn.length);
         return rtn;
      }
   }

   byte[] getIntroducerData() {
      return this.introducer.getIntroducerData();
   }

   public byte[] getCompleteFieldAsBytes() {
      ByteArrayOutputStream baos = new ByteArrayOutputStream(this.getSfLength());

      try {
         this.writeTo(baos);
      } catch (IOException var3) {
      }

      return baos.toByteArray();
   }

   public void writeTo(OutputStream out) throws IOException {
      out.write(this.introducer.introducerData);
      if (this.isSfiExtensionPresent()) {
         out.write(this.extData.length + 1);
         out.write(this.extData);
      }

      out.write(this.data);
   }

   static final class Introducer {
      private final short length;
      private final byte classCode;
      private final byte typeCode;
      private final byte categoryCode;
      private final boolean extensionPresent;
      private final boolean segmentedData;
      private final boolean paddingPresent;
      private final byte[] introducerData;

      Introducer(byte[] introducerData) throws IOException {
         this.introducerData = introducerData;
         DataInputStream iis = new DataInputStream(new ByteArrayInputStream(introducerData));
         this.length = iis.readShort();
         this.classCode = iis.readByte();
         this.typeCode = iis.readByte();
         this.categoryCode = iis.readByte();
         byte f = iis.readByte();
         this.extensionPresent = (f & 1) != 0;
         this.segmentedData = (f & 4) != 0;
         this.paddingPresent = (f & 16) != 0;
      }

      public short getLength() {
         return this.length;
      }

      public byte getClassCode() {
         return this.classCode;
      }

      public byte getTypeCode() {
         return this.typeCode;
      }

      public byte getCategoryCode() {
         return this.categoryCode;
      }

      public boolean isExtensionPresent() {
         return this.extensionPresent;
      }

      public boolean isSegmentedData() {
         return this.segmentedData;
      }

      public boolean isPaddingPresent() {
         return this.paddingPresent;
      }

      public byte[] getIntroducerData() {
         byte[] rtn = new byte[this.introducerData.length];
         System.arraycopy(this.introducerData, 0, rtn, 0, rtn.length);
         return rtn;
      }
   }
}
