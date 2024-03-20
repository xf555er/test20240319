package org.apache.batik.css.parser;

import org.w3c.css.sac.LexicalUnit;

public abstract class CSSLexicalUnit implements LexicalUnit {
   public static final String UNIT_TEXT_CENTIMETER = "cm";
   public static final String UNIT_TEXT_DEGREE = "deg";
   public static final String UNIT_TEXT_EM = "em";
   public static final String UNIT_TEXT_EX = "ex";
   public static final String UNIT_TEXT_GRADIAN = "grad";
   public static final String UNIT_TEXT_HERTZ = "Hz";
   public static final String UNIT_TEXT_INCH = "in";
   public static final String UNIT_TEXT_KILOHERTZ = "kHz";
   public static final String UNIT_TEXT_MILLIMETER = "mm";
   public static final String UNIT_TEXT_MILLISECOND = "ms";
   public static final String UNIT_TEXT_PERCENTAGE = "%";
   public static final String UNIT_TEXT_PICA = "pc";
   public static final String UNIT_TEXT_PIXEL = "px";
   public static final String UNIT_TEXT_POINT = "pt";
   public static final String UNIT_TEXT_RADIAN = "rad";
   public static final String UNIT_TEXT_REAL = "";
   public static final String UNIT_TEXT_SECOND = "s";
   public static final String TEXT_RGBCOLOR = "rgb";
   public static final String TEXT_RECT_FUNCTION = "rect";
   public static final String TEXT_COUNTER_FUNCTION = "counter";
   public static final String TEXT_COUNTERS_FUNCTION = "counters";
   protected short lexicalUnitType;
   protected LexicalUnit nextLexicalUnit;
   protected LexicalUnit previousLexicalUnit;

   protected CSSLexicalUnit(short t, LexicalUnit prev) {
      this.lexicalUnitType = t;
      this.previousLexicalUnit = prev;
      if (prev != null) {
         ((CSSLexicalUnit)prev).nextLexicalUnit = this;
      }

   }

   public short getLexicalUnitType() {
      return this.lexicalUnitType;
   }

   public LexicalUnit getNextLexicalUnit() {
      return this.nextLexicalUnit;
   }

   public void setNextLexicalUnit(LexicalUnit lu) {
      this.nextLexicalUnit = lu;
   }

   public LexicalUnit getPreviousLexicalUnit() {
      return this.previousLexicalUnit;
   }

   public void setPreviousLexicalUnit(LexicalUnit lu) {
      this.previousLexicalUnit = lu;
   }

   public int getIntegerValue() {
      throw new IllegalStateException();
   }

   public float getFloatValue() {
      throw new IllegalStateException();
   }

   public String getDimensionUnitText() {
      switch (this.lexicalUnitType) {
         case 14:
            return "";
         case 15:
            return "em";
         case 16:
            return "ex";
         case 17:
            return "px";
         case 18:
            return "in";
         case 19:
            return "cm";
         case 20:
            return "mm";
         case 21:
            return "pt";
         case 22:
            return "pc";
         case 23:
            return "%";
         case 24:
         case 25:
         case 26:
         case 27:
         default:
            throw new IllegalStateException("No Unit Text for type: " + this.lexicalUnitType);
         case 28:
            return "deg";
         case 29:
            return "grad";
         case 30:
            return "rad";
         case 31:
            return "ms";
         case 32:
            return "s";
         case 33:
            return "Hz";
         case 34:
            return "kHz";
      }
   }

   public String getFunctionName() {
      throw new IllegalStateException();
   }

   public LexicalUnit getParameters() {
      throw new IllegalStateException();
   }

   public String getStringValue() {
      throw new IllegalStateException();
   }

   public LexicalUnit getSubValues() {
      throw new IllegalStateException();
   }

   public static CSSLexicalUnit createSimple(short t, LexicalUnit prev) {
      return new SimpleLexicalUnit(t, prev);
   }

   public static CSSLexicalUnit createInteger(int val, LexicalUnit prev) {
      return new IntegerLexicalUnit(val, prev);
   }

   public static CSSLexicalUnit createFloat(short t, float val, LexicalUnit prev) {
      return new FloatLexicalUnit(t, val, prev);
   }

   public static CSSLexicalUnit createDimension(float val, String dim, LexicalUnit prev) {
      return new DimensionLexicalUnit(val, dim, prev);
   }

   public static CSSLexicalUnit createFunction(String f, LexicalUnit params, LexicalUnit prev) {
      return new FunctionLexicalUnit(f, params, prev);
   }

   public static CSSLexicalUnit createPredefinedFunction(short t, LexicalUnit params, LexicalUnit prev) {
      return new PredefinedFunctionLexicalUnit(t, params, prev);
   }

   public static CSSLexicalUnit createString(short t, String val, LexicalUnit prev) {
      return new StringLexicalUnit(t, val, prev);
   }

   protected static class StringLexicalUnit extends CSSLexicalUnit {
      protected String value;

      public StringLexicalUnit(short t, String val, LexicalUnit prev) {
         super(t, prev);
         this.value = val;
      }

      public String getStringValue() {
         return this.value;
      }
   }

   protected static class PredefinedFunctionLexicalUnit extends CSSLexicalUnit {
      protected LexicalUnit parameters;

      public PredefinedFunctionLexicalUnit(short t, LexicalUnit params, LexicalUnit prev) {
         super(t, prev);
         this.parameters = params;
      }

      public String getFunctionName() {
         switch (this.lexicalUnitType) {
            case 25:
               return "counter";
            case 26:
               return "counters";
            case 27:
               return "rgb";
            case 38:
               return "rect";
            default:
               return super.getFunctionName();
         }
      }

      public LexicalUnit getParameters() {
         return this.parameters;
      }
   }

   protected static class FunctionLexicalUnit extends CSSLexicalUnit {
      protected String name;
      protected LexicalUnit parameters;

      public FunctionLexicalUnit(String f, LexicalUnit params, LexicalUnit prev) {
         super((short)41, prev);
         this.name = f;
         this.parameters = params;
      }

      public String getFunctionName() {
         return this.name;
      }

      public LexicalUnit getParameters() {
         return this.parameters;
      }
   }

   protected static class DimensionLexicalUnit extends CSSLexicalUnit {
      protected float value;
      protected String dimension;

      public DimensionLexicalUnit(float val, String dim, LexicalUnit prev) {
         super((short)42, prev);
         this.value = val;
         this.dimension = dim;
      }

      public float getFloatValue() {
         return this.value;
      }

      public String getDimensionUnitText() {
         return this.dimension;
      }
   }

   protected static class FloatLexicalUnit extends CSSLexicalUnit {
      protected float value;

      public FloatLexicalUnit(short t, float val, LexicalUnit prev) {
         super(t, prev);
         this.value = val;
      }

      public float getFloatValue() {
         return this.value;
      }
   }

   protected static class IntegerLexicalUnit extends CSSLexicalUnit {
      protected int value;

      public IntegerLexicalUnit(int val, LexicalUnit prev) {
         super((short)13, prev);
         this.value = val;
      }

      public int getIntegerValue() {
         return this.value;
      }
   }

   protected static class SimpleLexicalUnit extends CSSLexicalUnit {
      public SimpleLexicalUnit(short t, LexicalUnit prev) {
         super(t, prev);
      }
   }
}
