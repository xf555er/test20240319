package org.apache.fop.afp.modca;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.fop.afp.modca.triplets.MappingOptionTriplet;
import org.apache.fop.afp.modca.triplets.MeasurementUnitsTriplet;
import org.apache.fop.afp.modca.triplets.ObjectAreaSizeTriplet;
import org.apache.fop.afp.util.BinaryUtils;

public class IncludeObject extends AbstractNamedAFPObject {
   public static final byte TYPE_PAGE_SEGMENT = 95;
   public static final byte TYPE_OTHER = -110;
   public static final byte TYPE_GRAPHIC = -69;
   public static final byte TYPE_BARCODE = -21;
   public static final byte TYPE_IMAGE = -5;
   private byte objectType = -110;
   private int xoaOset;
   private int yoaOset;
   private AxisOrientation oaOrent;
   private int xocaOset;
   private int yocaOset;

   public IncludeObject(String name) {
      super(name);
      this.oaOrent = AxisOrientation.RIGHT_HANDED_0;
      this.xocaOset = -1;
      this.yocaOset = -1;
   }

   public void setObjectAreaOrientation(int orientation) {
      this.oaOrent = AxisOrientation.getRightHandedAxisOrientationFor(orientation);
   }

   public void setObjectAreaOffset(int x, int y) {
      this.xoaOset = x;
      this.yoaOset = y;
   }

   public void setContentAreaOffset(int x, int y) {
      this.xocaOset = x;
      this.yocaOset = y;
   }

   public void setObjectType(byte type) {
      this.objectType = type;
   }

   public void writeToStream(OutputStream os) throws IOException {
      byte[] data = new byte[36];
      super.copySF(data, (byte)-81, (byte)-61);
      int tripletDataLength = this.getTripletDataLength();
      byte[] len = BinaryUtils.convert(35 + tripletDataLength, 2);
      data[1] = len[0];
      data[2] = len[1];
      data[17] = 0;
      data[18] = this.objectType;
      writeOsetTo(data, 19, this.xoaOset);
      writeOsetTo(data, 22, this.yoaOset);
      this.oaOrent.writeTo(data, 25);
      writeOsetTo(data, 29, this.xocaOset);
      writeOsetTo(data, 32, this.yocaOset);
      data[35] = 1;
      os.write(data);
      this.writeTriplets(os);
   }

   private static void writeOsetTo(byte[] out, int offset, int oset) {
      if (oset > -1) {
         byte[] y = BinaryUtils.convert(oset, 3);
         out[offset] = y[0];
         out[offset + 1] = y[1];
         out[offset + 2] = y[2];
      } else {
         out[offset] = -1;
         out[offset + 1] = -1;
         out[offset + 2] = -1;
      }

   }

   private String getObjectTypeName() {
      String objectTypeName = null;
      if (this.objectType == 95) {
         objectTypeName = "page segment";
      } else if (this.objectType == -110) {
         objectTypeName = "other";
      } else if (this.objectType == -69) {
         objectTypeName = "graphic";
      } else if (this.objectType == -21) {
         objectTypeName = "barcode";
      } else if (this.objectType == -5) {
         objectTypeName = "image";
      }

      return objectTypeName;
   }

   public String toString() {
      return "IncludeObject{name=" + this.getName() + ", objectType=" + this.getObjectTypeName() + ", xoaOset=" + this.xoaOset + ", yoaOset=" + this.yoaOset + ", oaOrent" + this.oaOrent + ", xocaOset=" + this.xocaOset + ", yocaOset=" + this.yocaOset + "}";
   }

   public void setMappingOption(byte optionValue) {
      this.addTriplet(new MappingOptionTriplet(optionValue));
   }

   public void setObjectAreaSize(int x, int y) {
      this.addTriplet(new ObjectAreaSizeTriplet(x, y));
   }

   public void setMeasurementUnits(int xRes, int yRes) {
      this.addTriplet(new MeasurementUnitsTriplet(xRes, xRes));
   }
}
