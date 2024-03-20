package org.apache.fop.render.gradient;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.apache.fop.pdf.PDFDeviceColorSpace;

public class Shading {
   private final int shadingType;
   private final PDFDeviceColorSpace colorSpace;
   private final List coords;
   private final Function function;
   private final List extend;
   private final int bitsPerCoordinate;
   private final int bitsPerFlag;
   private final boolean antiAlias;
   private final int bitsPerComponent;
   private final int verticesPerRow;

   public Shading(int shadingType, PDFDeviceColorSpace colorSpace, List coords, Function function) {
      this.shadingType = shadingType;
      this.colorSpace = colorSpace;
      this.antiAlias = false;
      this.coords = coords;
      this.function = function;
      this.extend = Arrays.asList(true, true);
      this.bitsPerCoordinate = 0;
      this.bitsPerFlag = 0;
      this.bitsPerComponent = 0;
      this.verticesPerRow = 0;
   }

   public int getShadingType() {
      return this.shadingType;
   }

   public PDFDeviceColorSpace getColorSpace() {
      return this.colorSpace;
   }

   public List getCoords() {
      return this.coords;
   }

   public Function getFunction() {
      return this.function;
   }

   public List getExtend() {
      return this.extend;
   }

   public int getBitsPerCoordinate() {
      return this.bitsPerCoordinate;
   }

   public int getBitsPerFlag() {
      return this.bitsPerFlag;
   }

   public boolean isAntiAlias() {
      return this.antiAlias;
   }

   public int getBitsPerComponent() {
      return this.bitsPerComponent;
   }

   public int getVerticesPerRow() {
      return this.verticesPerRow;
   }

   public void output(StringBuilder out, GradientMaker.DoubleFormatter doubleFormatter, FunctionRenderer functionRenderer) {
      out.append("<<\n/ShadingType " + this.shadingType + "\n");
      if (this.colorSpace != null) {
         out.append("/ColorSpace /" + this.colorSpace.getName() + "\n");
      }

      if (this.antiAlias) {
         out.append("/AntiAlias " + this.antiAlias + "\n");
      }

      switch (this.shadingType) {
         case 1:
            this.outputShadingType1(out, doubleFormatter, functionRenderer);
            break;
         case 2:
         case 3:
            this.outputShadingType2or3(out, doubleFormatter, functionRenderer);
            break;
         case 4:
         case 6:
         case 7:
            this.outputShadingType4or6or7(out, doubleFormatter, functionRenderer);
            break;
         case 5:
            this.outputShadingType5(out, doubleFormatter, functionRenderer);
            break;
         default:
            throw new UnsupportedOperationException("Shading type " + this.shadingType);
      }

      out.append(">>");
   }

   private void outputShadingType1(StringBuilder out, GradientMaker.DoubleFormatter doubleFormatter, FunctionRenderer functionRenderer) {
      this.outputFunction(out, functionRenderer);
   }

   private void outputShadingType2or3(StringBuilder out, GradientMaker.DoubleFormatter doubleFormatter, FunctionRenderer functionRenderer) {
      if (this.coords != null) {
         out.append("/Coords ");
         GradientMaker.outputDoubles(out, doubleFormatter, this.coords);
         out.append("\n");
      }

      out.append("/Extend [ ");
      Iterator var4 = this.extend.iterator();

      while(var4.hasNext()) {
         Boolean b = (Boolean)var4.next();
         out.append(b);
         out.append(" ");
      }

      out.append("]\n");
      this.outputFunction(out, functionRenderer);
   }

   private void outputShadingType4or6or7(StringBuilder out, GradientMaker.DoubleFormatter doubleFormatter, FunctionRenderer functionRenderer) {
      if (this.bitsPerCoordinate > 0) {
         out.append("/BitsPerCoordinate " + this.bitsPerCoordinate + "\n");
      } else {
         out.append("/BitsPerCoordinate 1 \n");
      }

      if (this.bitsPerComponent > 0) {
         out.append("/BitsPerComponent " + this.bitsPerComponent + "\n");
      } else {
         out.append("/BitsPerComponent 1 \n");
      }

      if (this.bitsPerFlag > 0) {
         out.append("/BitsPerFlag " + this.bitsPerFlag + "\n");
      } else {
         out.append("/BitsPerFlag 2 \n");
      }

      this.outputFunction(out, functionRenderer);
   }

   private void outputShadingType5(StringBuilder out, GradientMaker.DoubleFormatter doubleFormatter, FunctionRenderer functionRenderer) {
      if (this.bitsPerCoordinate > 0) {
         out.append("/BitsPerCoordinate " + this.bitsPerCoordinate + "\n");
      } else {
         out.append("/BitsPerCoordinate 1 \n");
      }

      if (this.bitsPerComponent > 0) {
         out.append("/BitsPerComponent " + this.bitsPerComponent + "\n");
      } else {
         out.append("/BitsPerComponent 1 \n");
      }

      this.outputFunction(out, functionRenderer);
      if (this.verticesPerRow > 0) {
         out.append("/VerticesPerRow " + this.verticesPerRow + "\n");
      } else {
         out.append("/VerticesPerRow 2 \n");
      }

   }

   private void outputFunction(StringBuilder out, FunctionRenderer functionRenderer) {
      if (this.function != null) {
         out.append("/Function ");
         functionRenderer.outputFunction(out);
         out.append("\n");
      }

   }

   public interface FunctionRenderer {
      void outputFunction(StringBuilder var1);
   }
}
