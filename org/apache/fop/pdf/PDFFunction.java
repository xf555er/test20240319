package org.apache.fop.pdf;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.fop.render.gradient.Function;
import org.apache.fop.render.gradient.GradientMaker;

public class PDFFunction extends PDFObject {
   private final Function function;
   private final List pdfFunctions;

   public PDFFunction(List domain, List range, float[] cZero, float[] cOne, double interpolationExponentN) {
      this(new Function(domain, range, cZero, cOne, interpolationExponentN));
   }

   public PDFFunction(Function function) {
      this(function, Collections.EMPTY_LIST);
   }

   public PDFFunction(Function function, List pdfFunctions) {
      this.function = function;
      this.pdfFunctions = pdfFunctions;
   }

   public Function getFunction() {
      return this.function;
   }

   public byte[] toPDF() {
      return this.toByteString();
   }

   public byte[] toByteString() {
      Function.SubFunctionRenderer subFunctionRenderer = new Function.SubFunctionRenderer() {
         public void outputFunction(StringBuilder out, int functionIndex) {
            out.append(((PDFFunction)PDFFunction.this.pdfFunctions.get(functionIndex)).referencePDF());
         }
      };
      StringBuilder out = new StringBuilder();
      GradientMaker.DoubleFormatter doubleFormatter = new GradientMaker.DoubleFormatter() {
         public String formatDouble(double d) {
            return PDFNumber.doubleOut(d);
         }
      };
      this.function.output(out, doubleFormatter, subFunctionRenderer);
      return encode(out.toString());
   }

   protected boolean contentEquals(PDFObject obj) {
      if (obj == null) {
         return false;
      } else if (obj == this) {
         return true;
      } else if (!(obj instanceof PDFFunction)) {
         return false;
      } else {
         Function func = ((PDFFunction)obj).function;
         if (this.function.getFunctionType() != func.getFunctionType()) {
            return false;
         } else if (this.function.getBitsPerSample() != func.getBitsPerSample()) {
            return false;
         } else if (this.function.getOrder() != func.getOrder()) {
            return false;
         } else if (this.function.getInterpolationExponentN() != func.getInterpolationExponentN()) {
            return false;
         } else {
            if (this.function.getDomain() != null) {
               if (!this.function.getDomain().equals(func.getDomain())) {
                  return false;
               }
            } else if (func.getDomain() != null) {
               return false;
            }

            if (this.function.getRange() != null) {
               if (!this.function.getRange().equals(func.getRange())) {
                  return false;
               }
            } else if (func.getRange() != null) {
               return false;
            }

            if (this.function.getEncode() != null) {
               if (!this.function.getEncode().equals(func.getEncode())) {
                  return false;
               }
            } else if (func.getEncode() != null) {
               return false;
            }

            if (!Arrays.equals(this.function.getCZero(), func.getCZero())) {
               return false;
            } else if (!Arrays.equals(this.function.getCOne(), func.getCOne())) {
               return false;
            } else if (!this.pdfFunctions.equals(((PDFFunction)obj).pdfFunctions)) {
               return false;
            } else {
               if (this.function.getBounds() != null) {
                  if (!this.function.getBounds().equals(func.getBounds())) {
                     return false;
                  }
               } else if (func.getBounds() != null) {
                  return false;
               }

               return true;
            }
         }
      }
   }
}
