package org.apache.fop.afp.modca;

import java.io.IOException;
import java.io.OutputStream;

public enum AxisOrientation {
   RIGHT_HANDED_0(Rotation.ROTATION_0, Rotation.ROTATION_90),
   RIGHT_HANDED_90(Rotation.ROTATION_90, Rotation.ROTATION_180),
   RIGHT_HANDED_180(Rotation.ROTATION_180, Rotation.ROTATION_270),
   RIGHT_HANDED_270(Rotation.ROTATION_270, Rotation.ROTATION_0);

   private final Rotation xoaOrent;
   private final Rotation yoaOrent;

   public void writeTo(byte[] out, int offset) {
      this.xoaOrent.writeTo(out, offset);
      this.yoaOrent.writeTo(out, offset + 2);
   }

   private AxisOrientation(Rotation xoaOrent, Rotation yoaOrent) {
      this.xoaOrent = xoaOrent;
      this.yoaOrent = yoaOrent;
   }

   public void writeTo(OutputStream stream) throws IOException {
      byte[] data = new byte[4];
      this.writeTo(data, 0);
      stream.write(data);
   }

   public static AxisOrientation getRightHandedAxisOrientationFor(int orientation) {
      switch (orientation) {
         case 0:
            return RIGHT_HANDED_0;
         case 90:
            return RIGHT_HANDED_90;
         case 180:
            return RIGHT_HANDED_180;
         case 270:
            return RIGHT_HANDED_270;
         default:
            throw new IllegalArgumentException("The orientation must be one of the values 0, 90, 180, 270");
      }
   }
}
