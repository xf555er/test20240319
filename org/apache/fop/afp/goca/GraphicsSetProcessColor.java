package org.apache.fop.afp.goca;

import java.awt.Color;
import java.awt.color.ColorSpace;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.xmlgraphics.java2d.color.CIELabColorSpace;
import org.apache.xmlgraphics.java2d.color.ColorUtil;
import org.apache.xmlgraphics.java2d.color.ColorWithAlternatives;

public class GraphicsSetProcessColor extends AbstractGraphicsDrawingOrder {
   private static final byte RGB = 1;
   private static final byte CMYK = 4;
   private static final byte CIELAB = 8;
   private final Color color;
   private final int componentsSize;

   public GraphicsSetProcessColor(Color color) {
      if (color instanceof ColorWithAlternatives) {
         ColorWithAlternatives cwa = (ColorWithAlternatives)color;
         Color alt = cwa.getFirstAlternativeOfType(9);
         if (alt != null) {
            this.color = alt;
            this.componentsSize = 4;
            return;
         }
      }

      ColorSpace cs = color.getColorSpace();
      int colSpaceType = cs.getType();
      if (colSpaceType == 9) {
         this.color = color;
      } else if (cs instanceof CIELabColorSpace) {
         this.color = color;
      } else if (!color.getColorSpace().isCS_sRGB()) {
         this.color = ColorUtil.toSRGBColor(color);
      } else {
         this.color = color;
      }

      this.componentsSize = this.color.getColorSpace().getNumComponents();
   }

   public int getDataLength() {
      return 12 + this.componentsSize;
   }

   byte getOrderCode() {
      return -78;
   }

   public void writeToStream(OutputStream os) throws IOException {
      float[] colorComponents = this.color.getColorComponents((float[])null);
      ColorSpace cs = this.color.getColorSpace();
      int colSpaceType = cs.getType();
      ByteArrayOutputStream baout = new ByteArrayOutputStream();
      DataOutputStream dout = null;
      byte colspace;
      byte[] colsizes;
      float[] var9;
      int a;
      int b;
      float colorComponent;
      int l;
      if (colSpaceType == 9) {
         colspace = 4;
         colsizes = new byte[]{8, 8, 8, 8};
         var9 = colorComponents;
         a = colorComponents.length;

         for(b = 0; b < a; ++b) {
            colorComponent = var9[b];
            baout.write(Math.round(colorComponent * 255.0F));
         }
      } else if (colSpaceType == 5) {
         colspace = 1;
         colsizes = new byte[]{8, 8, 8, 0};
         var9 = colorComponents;
         a = colorComponents.length;

         for(b = 0; b < a; ++b) {
            colorComponent = var9[b];
            baout.write(Math.round(colorComponent * 255.0F));
         }
      } else {
         if (!(cs instanceof CIELabColorSpace)) {
            IOUtils.closeQuietly((OutputStream)baout);
            throw new IllegalStateException();
         }

         colspace = 8;
         colsizes = new byte[]{8, 8, 8, 0};
         dout = new DataOutputStream(baout);
         l = Math.round(colorComponents[0] * 100.0F);
         a = Math.round(colorComponents[1] * 255.0F) - 128;
         b = Math.round(colorComponents[2] * 255.0F) - 128;
         dout.writeByte(l);
         dout.writeByte(a);
         dout.writeByte(b);
      }

      l = this.getDataLength();
      byte[] data = new byte[]{this.getOrderCode(), (byte)(l - 2), 0, colspace, 0, 0, 0, 0, colsizes[0], colsizes[1], colsizes[2], colsizes[3]};
      os.write(data);
      baout.writeTo(os);
      IOUtils.closeQuietly((OutputStream)dout);
      IOUtils.closeQuietly((OutputStream)baout);
   }

   public String toString() {
      return "GraphicsSetProcessColor(col=" + this.color + ")";
   }
}
