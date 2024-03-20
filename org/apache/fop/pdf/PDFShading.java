package org.apache.fop.pdf;

import java.util.List;
import org.apache.fop.render.gradient.GradientMaker;
import org.apache.fop.render.gradient.Shading;

public class PDFShading extends PDFObject {
   protected String shadingName;
   private final Shading shading;
   private final PDFFunction pdfFunction;

   public PDFShading(int shadingType, PDFDeviceColorSpace colorSpace, List coords, PDFFunction pdfFunction) {
      this.shading = new Shading(shadingType, colorSpace, coords, pdfFunction.getFunction());
      this.pdfFunction = pdfFunction;
   }

   public String getName() {
      return this.shadingName;
   }

   public void setName(String name) {
      if (name.indexOf(" ") >= 0) {
         throw new IllegalArgumentException("Shading name must not contain any spaces");
      } else {
         this.shadingName = name;
      }
   }

   public String toPDFString() {
      Shading.FunctionRenderer functionRenderer = new Shading.FunctionRenderer() {
         public void outputFunction(StringBuilder out) {
            out.append(PDFShading.this.pdfFunction.referencePDF());
         }
      };
      StringBuilder out = new StringBuilder();
      GradientMaker.DoubleFormatter doubleFormatter = new GradientMaker.DoubleFormatter() {
         public String formatDouble(double d) {
            return PDFNumber.doubleOut(d);
         }
      };
      this.shading.output(out, doubleFormatter, functionRenderer);
      return out.toString();
   }

   protected boolean contentEquals(PDFObject obj) {
      if (obj == null) {
         return false;
      } else if (obj == this) {
         return true;
      } else if (!(obj instanceof PDFShading)) {
         return false;
      } else {
         Shading other = ((PDFShading)obj).shading;
         if (this.shading.getShadingType() != other.getShadingType()) {
            return false;
         } else if (this.shading.isAntiAlias() != other.isAntiAlias()) {
            return false;
         } else if (this.shading.getBitsPerCoordinate() != other.getBitsPerCoordinate()) {
            return false;
         } else if (this.shading.getBitsPerFlag() != other.getBitsPerFlag()) {
            return false;
         } else if (this.shading.getBitsPerComponent() != other.getBitsPerComponent()) {
            return false;
         } else if (this.shading.getVerticesPerRow() != other.getVerticesPerRow()) {
            return false;
         } else {
            if (this.shading.getColorSpace() != null) {
               if (!this.shading.getColorSpace().equals(other.getColorSpace())) {
                  return false;
               }
            } else if (other.getColorSpace() != null) {
               return false;
            }

            if (this.shading.getCoords() != null) {
               if (!this.shading.getCoords().equals(other.getCoords())) {
                  return false;
               }
            } else if (other.getCoords() != null) {
               return false;
            }

            if (this.shading.getExtend() != null) {
               if (!this.shading.getExtend().equals(other.getExtend())) {
                  return false;
               }
            } else if (other.getExtend() != null) {
               return false;
            }

            if (this.shading.getFunction() != null) {
               if (!this.shading.getFunction().equals(other.getFunction())) {
                  return false;
               }
            } else if (other.getFunction() != null) {
               return false;
            }

            return true;
         }
      }
   }
}
