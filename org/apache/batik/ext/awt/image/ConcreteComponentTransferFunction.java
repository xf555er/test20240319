package org.apache.batik.ext.awt.image;

public final class ConcreteComponentTransferFunction implements ComponentTransferFunction {
   private int type;
   private float slope;
   private float[] tableValues;
   private float intercept;
   private float amplitude;
   private float exponent;
   private float offset;

   private ConcreteComponentTransferFunction() {
   }

   public static ComponentTransferFunction getIdentityTransfer() {
      ConcreteComponentTransferFunction f = new ConcreteComponentTransferFunction();
      f.type = 0;
      return f;
   }

   public static ComponentTransferFunction getTableTransfer(float[] tableValues) {
      ConcreteComponentTransferFunction f = new ConcreteComponentTransferFunction();
      f.type = 1;
      if (tableValues == null) {
         throw new IllegalArgumentException();
      } else if (tableValues.length < 2) {
         throw new IllegalArgumentException();
      } else {
         f.tableValues = new float[tableValues.length];
         System.arraycopy(tableValues, 0, f.tableValues, 0, tableValues.length);
         return f;
      }
   }

   public static ComponentTransferFunction getDiscreteTransfer(float[] tableValues) {
      ConcreteComponentTransferFunction f = new ConcreteComponentTransferFunction();
      f.type = 2;
      if (tableValues == null) {
         throw new IllegalArgumentException();
      } else if (tableValues.length < 2) {
         throw new IllegalArgumentException();
      } else {
         f.tableValues = new float[tableValues.length];
         System.arraycopy(tableValues, 0, f.tableValues, 0, tableValues.length);
         return f;
      }
   }

   public static ComponentTransferFunction getLinearTransfer(float slope, float intercept) {
      ConcreteComponentTransferFunction f = new ConcreteComponentTransferFunction();
      f.type = 3;
      f.slope = slope;
      f.intercept = intercept;
      return f;
   }

   public static ComponentTransferFunction getGammaTransfer(float amplitude, float exponent, float offset) {
      ConcreteComponentTransferFunction f = new ConcreteComponentTransferFunction();
      f.type = 4;
      f.amplitude = amplitude;
      f.exponent = exponent;
      f.offset = offset;
      return f;
   }

   public int getType() {
      return this.type;
   }

   public float getSlope() {
      return this.slope;
   }

   public float[] getTableValues() {
      return this.tableValues;
   }

   public float getIntercept() {
      return this.intercept;
   }

   public float getAmplitude() {
      return this.amplitude;
   }

   public float getExponent() {
      return this.exponent;
   }

   public float getOffset() {
      return this.offset;
   }
}
