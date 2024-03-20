package org.apache.fop.render.gradient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Function {
   private int functionType;
   private List domain;
   private List range;
   private int bitsPerSample;
   private int order;
   private List encode;
   private float[] cZero;
   private float[] cOne;
   private double interpolationExponentN;
   private List functions;
   private List bounds;
   private byte[] datasource;
   private List size;

   public Function(List domain, List range, float[] cZero, float[] cOne, double interpolationExponentN) {
      this(2, domain, range);
      this.cZero = cZero;
      this.cOne = cOne;
      this.interpolationExponentN = interpolationExponentN;
   }

   public Function(List domain, List range, List functions, List bounds, List encode) {
      this(3, domain, range);
      this.functions = functions;
      this.bounds = bounds;
      this.encode = this.makeEncode(encode);
   }

   public void setCZero(float[] cZero) {
      this.cZero = cZero;
   }

   public void setCOne(float[] cOne) {
      this.cOne = cOne;
   }

   private List makeEncode(List encode) {
      if (encode != null) {
         return encode;
      } else {
         List encode = new ArrayList(this.functions.size() * 2);

         for(int i = 0; i < this.functions.size(); ++i) {
            encode.add(0.0);
            encode.add(1.0);
         }

         return encode;
      }
   }

   private Function(int functionType, List domain, List range) {
      this.bitsPerSample = 1;
      this.order = 1;
      this.interpolationExponentN = 1.0;
      this.functionType = functionType;
      this.domain = domain == null ? Arrays.asList(0.0, 1.0) : domain;
      this.range = range;
   }

   public Function(List domain, List range, List encode, byte[] datasource, int bitsPerSample, List size) {
      this(0, domain, range);
      this.encode = encode;
      this.datasource = datasource;
      this.bitsPerSample = bitsPerSample;
      this.size = size;
   }

   public int getFunctionType() {
      return this.functionType;
   }

   public List getBounds() {
      return this.bounds;
   }

   public List getDomain() {
      return this.domain;
   }

   public List getEncode() {
      return this.encode;
   }

   public List getFunctions() {
      return this.functions == null ? Collections.emptyList() : this.functions;
   }

   public int getBitsPerSample() {
      return this.bitsPerSample;
   }

   public double getInterpolationExponentN() {
      return this.interpolationExponentN;
   }

   public int getOrder() {
      return this.order;
   }

   public List getRange() {
      return this.range;
   }

   public float[] getCZero() {
      return this.cZero;
   }

   public float[] getCOne() {
      return this.cOne;
   }

   public String output(StringBuilder out, GradientMaker.DoubleFormatter doubleFormatter, SubFunctionRenderer subFunctionRenderer) {
      out.append("<<\n/FunctionType " + this.functionType + "\n");
      this.outputDomain(out, doubleFormatter);
      int i;
      if (this.functionType == 0) {
         this.outputEncode(out, doubleFormatter);
         this.outputBitsPerSample(out);
         this.outputOrder(out);
         this.outputRange(out, doubleFormatter);
         out.append("\n/DataSource <");
         byte[] var4 = this.datasource;
         int var5 = var4.length;

         for(i = 0; i < var5; ++i) {
            byte b = var4[i];
            out.append(String.format("%02x", b & 255));
         }

         out.append(">\n");
         out.append("/Size [");
         Iterator var8 = this.size.iterator();

         while(var8.hasNext()) {
            Integer i = (Integer)var8.next();
            out.append(i);
            out.append(" ");
         }

         out.append("]\n");
         out.append(">>");
      } else if (this.functionType == 2) {
         this.outputRange(out, doubleFormatter);
         this.outputCZero(out, doubleFormatter);
         this.outputCOne(out, doubleFormatter);
         this.outputInterpolationExponentN(out, doubleFormatter);
         out.append(">>");
      } else if (this.functionType == 3) {
         this.outputRange(out, doubleFormatter);
         int numberOfFunctions;
         if (!this.functions.isEmpty()) {
            out.append("/Functions [ ");

            for(numberOfFunctions = 0; numberOfFunctions < this.functions.size(); ++numberOfFunctions) {
               subFunctionRenderer.outputFunction(out, numberOfFunctions);
               out.append(' ');
            }

            out.append("]\n");
         }

         this.outputEncode(out, doubleFormatter);
         out.append("/Bounds ");
         if (this.bounds != null) {
            GradientMaker.outputDoubles(out, doubleFormatter, this.bounds);
         } else if (!this.functions.isEmpty()) {
            numberOfFunctions = this.functions.size();
            String functionsFraction = doubleFormatter.formatDouble(1.0 / (double)numberOfFunctions);
            out.append("[ ");

            for(i = 0; i + 1 < numberOfFunctions; ++i) {
               out.append(functionsFraction);
               out.append(" ");
            }

            out.append("]");
         }

         out.append("\n>>");
      } else if (this.functionType == 4) {
         this.outputRange(out, doubleFormatter);
         out.append(">>");
      }

      return out.toString();
   }

   private void outputDomain(StringBuilder p, GradientMaker.DoubleFormatter doubleFormatter) {
      p.append("/Domain ");
      GradientMaker.outputDoubles(p, doubleFormatter, this.domain);
      p.append("\n");
   }

   private void outputBitsPerSample(StringBuilder out) {
      out.append("/BitsPerSample " + this.bitsPerSample + "\n");
   }

   private void outputOrder(StringBuilder out) {
      if (this.order == 1 || this.order == 3) {
         out.append("\n/Order " + this.order + "\n");
      }

   }

   private void outputRange(StringBuilder out, GradientMaker.DoubleFormatter doubleFormatter) {
      if (this.range != null) {
         out.append("/Range ");
         GradientMaker.outputDoubles(out, doubleFormatter, this.range);
         out.append("\n");
      }

   }

   private void outputEncode(StringBuilder out, GradientMaker.DoubleFormatter doubleFormatter) {
      out.append("/Encode ");
      GradientMaker.outputDoubles(out, doubleFormatter, this.encode);
      out.append("\n");
   }

   private void outputCZero(StringBuilder out, GradientMaker.DoubleFormatter doubleFormatter) {
      if (this.cZero != null) {
         out.append("/C0 [ ");
         float[] var3 = this.cZero;
         int var4 = var3.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            float c = var3[var5];
            out.append(doubleFormatter.formatDouble((double)c));
            out.append(" ");
         }

         out.append("]\n");
      }

   }

   private void outputCOne(StringBuilder out, GradientMaker.DoubleFormatter doubleFormatter) {
      if (this.cOne != null) {
         out.append("/C1 [ ");
         float[] var3 = this.cOne;
         int var4 = var3.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            float c = var3[var5];
            out.append(doubleFormatter.formatDouble((double)c));
            out.append(" ");
         }

         out.append("]\n");
      }

   }

   private void outputInterpolationExponentN(StringBuilder out, GradientMaker.DoubleFormatter doubleFormatter) {
      out.append("/N ");
      out.append(doubleFormatter.formatDouble(this.interpolationExponentN));
      out.append("\n");
   }

   public interface SubFunctionRenderer {
      void outputFunction(StringBuilder var1, int var2);
   }
}
