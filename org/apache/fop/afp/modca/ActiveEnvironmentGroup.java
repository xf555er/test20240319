package org.apache.fop.afp.modca;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.fop.afp.AFPDataObjectInfo;
import org.apache.fop.afp.Factory;
import org.apache.fop.afp.fonts.AFPFont;
import org.apache.fop.afp.modca.triplets.AbstractTriplet;
import org.apache.fop.afp.modca.triplets.EncodingTriplet;
import org.apache.fop.afp.util.BinaryUtils;
import org.apache.fop.fonts.FontType;
import org.apache.fop.render.afp.AFPFontConfig;

public final class ActiveEnvironmentGroup extends AbstractEnvironmentGroup {
   private final List mapCodedFonts = new ArrayList();
   private List mapPageSegments;
   private ObjectAreaDescriptor objectAreaDescriptor;
   private ObjectAreaPosition objectAreaPosition;
   private PresentationTextDescriptor presentationTextDataDescriptor;
   private PageDescriptor pageDescriptor;
   private final Factory factory;
   private MapDataResource mdr;

   public ActiveEnvironmentGroup(Factory factory, String name, int width, int height, int widthRes, int heightRes) {
      super(name);
      this.factory = factory;
      this.pageDescriptor = factory.createPageDescriptor(width, height, widthRes, heightRes);
      this.objectAreaDescriptor = factory.createObjectAreaDescriptor(width, height, widthRes, heightRes);
      this.presentationTextDataDescriptor = factory.createPresentationTextDataDescriptor(width, height, widthRes, heightRes);
   }

   public void setObjectAreaPosition(int x, int y, int rotation) {
      this.objectAreaPosition = this.factory.createObjectAreaPosition(x, y, rotation);
   }

   public PageDescriptor getPageDescriptor() {
      return this.pageDescriptor;
   }

   public PresentationTextDescriptor getPresentationTextDataDescriptor() {
      return this.presentationTextDataDescriptor;
   }

   public void writeContent(OutputStream os) throws IOException {
      super.writeTriplets(os);
      this.writeObjects(this.mapCodedFonts, os);
      this.writeObjects(this.mapDataResources, os);
      this.writeObjects(this.mapPageOverlays, os);
      this.writeObjects(this.mapPageSegments, os);
      if (this.pageDescriptor != null) {
         this.pageDescriptor.writeToStream(os);
      }

      if (this.objectAreaDescriptor != null && this.objectAreaPosition != null) {
         this.objectAreaDescriptor.writeToStream(os);
         this.objectAreaPosition.writeToStream(os);
      }

      if (this.presentationTextDataDescriptor != null) {
         this.presentationTextDataDescriptor.writeToStream(os);
      }

   }

   protected void writeStart(OutputStream os) throws IOException {
      byte[] data = new byte[17];
      this.copySF(data, (byte)-88, (byte)-55);
      os.write(data);
   }

   protected void writeEnd(OutputStream os) throws IOException {
      byte[] data = new byte[17];
      this.copySF(data, (byte)-87, (byte)-55);
      os.write(data);
   }

   public void createFont(int fontRef, AFPFont font, int size, int orientation) {
      if (font.getFontType() == FontType.TRUETYPE) {
         if (this.mdr == null) {
            this.mdr = this.factory.createMapDataResource();
            this.mapCodedFonts.add(this.mdr);
         }

         this.mdr.addTriplet(new EncodingTriplet(1200));
         String name = font.getFontName();
         if (((AFPFontConfig.AFPTrueTypeFont)font).getTTC() != null) {
            name = ((AFPFontConfig.AFPTrueTypeFont)font).getTTC();
         }

         this.mdr.setFullyQualifiedName((byte)-34, (byte)0, name, true);
         this.mdr.addTriplet(new FontFullyQualifiedNameTriplet((byte)fontRef));
         setupTruetypeMDR(this.mdr, false);
         this.mdr.addTriplet(new DataObjectFontTriplet(size / 1000));
         this.mdr.finishElement();
      } else {
         MapCodedFont mapCodedFont = this.getCurrentMapCodedFont();
         if (mapCodedFont == null) {
            mapCodedFont = this.factory.createMapCodedFont();
            this.mapCodedFonts.add(mapCodedFont);
         }

         try {
            mapCodedFont.addFont(fontRef, font, size, orientation);
         } catch (MaximumSizeExceededException var9) {
            mapCodedFont = this.factory.createMapCodedFont();
            this.mapCodedFonts.add(mapCodedFont);

            try {
               mapCodedFont.addFont(fontRef, font, size, orientation);
            } catch (MaximumSizeExceededException var8) {
               LOG.error("createFont():: resulted in a MaximumSizeExceededException");
            }
         }
      }

   }

   public static void setupTruetypeMDR(AbstractTripletStructuredObject mdr, boolean res) {
      AFPDataObjectInfo dataInfo = new AFPDataObjectInfo();
      dataInfo.setMimeType("image/x-afp+truetype");
      mdr.setObjectClassification((byte)65, dataInfo.getObjectType(), res, false, res);
   }

   private MapCodedFont getCurrentMapCodedFont() {
      int size = this.mapCodedFonts.size();
      if (size > 0) {
         AbstractStructuredObject font = (AbstractStructuredObject)this.mapCodedFonts.get(size - 1);
         if (font instanceof MapCodedFont) {
            return (MapCodedFont)font;
         }
      }

      return null;
   }

   public void addMapPageSegment(String name) {
      try {
         this.needMapPageSegment().addPageSegment(name);
      } catch (MaximumSizeExceededException var3) {
         throw new IllegalStateException("Internal error: " + var3.getMessage());
      }
   }

   private MapPageSegment getCurrentMapPageSegment() {
      return (MapPageSegment)this.getLastElement(this.mapPageSegments);
   }

   private MapPageSegment needMapPageSegment() {
      if (this.mapPageSegments == null) {
         this.mapPageSegments = new ArrayList();
      }

      MapPageSegment seg = this.getCurrentMapPageSegment();
      if (seg == null || seg.isFull()) {
         seg = new MapPageSegment();
         this.mapPageSegments.add(seg);
      }

      return seg;
   }

   static class DataObjectFontTriplet extends AbstractTriplet {
      private int pointSize;

      public DataObjectFontTriplet(int size) {
         super((byte)-117);
         this.pointSize = size;
      }

      public int getDataLength() {
         return 16;
      }

      public void writeToStream(OutputStream os) throws IOException {
         byte[] data = this.getData();
         data[3] = 32;
         byte[] pointSizeBytes = BinaryUtils.convert(this.pointSize * 20, 2);
         data[4] = pointSizeBytes[0];
         data[5] = pointSizeBytes[1];
         data[11] = 3;
         data[13] = 1;
         os.write(data);
      }
   }

   public static class FontFullyQualifiedNameTriplet extends AbstractTriplet {
      private byte fqName;

      public FontFullyQualifiedNameTriplet(byte fqName) {
         super((byte)2);
         this.fqName = fqName;
      }

      public int getDataLength() {
         return 5;
      }

      public void writeToStream(OutputStream os) throws IOException {
         byte[] data = this.getData();
         data[2] = -66;
         data[3] = 0;
         data[4] = this.fqName;
         os.write(data);
      }
   }
}
