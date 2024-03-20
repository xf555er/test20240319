package org.apache.fop.fo.expr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.LengthBase;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.datatypes.PercentBase;
import org.apache.fop.fo.properties.ColorProperty;
import org.apache.fop.fo.properties.FixedLength;
import org.apache.fop.fo.properties.ListProperty;
import org.apache.fop.fo.properties.NumberProperty;
import org.apache.fop.fo.properties.PercentLength;
import org.apache.fop.fo.properties.Property;
import org.apache.fop.fo.properties.StringProperty;

public final class PropertyParser extends PropertyTokenizer {
   private PropertyInfo propInfo;
   private static final String RELUNIT = "em";
   private static final HashMap FUNCTION_TABLE = new HashMap();

   public static Property parse(String expr, PropertyInfo propInfo) throws PropertyException {
      try {
         return (new PropertyParser(expr, propInfo)).parseProperty();
      } catch (PropertyException var3) {
         var3.setPropertyInfo(propInfo);
         throw var3;
      }
   }

   private PropertyParser(String propExpr, PropertyInfo pInfo) {
      super(propExpr);
      this.propInfo = pInfo;
   }

   private Property parseProperty() throws PropertyException {
      this.next();
      if (this.currentToken == 0) {
         return StringProperty.getInstance("");
      } else {
         ListProperty propList = null;

         while(true) {
            Property prop = this.parseAdditiveExpr();
            if (this.currentToken == 0) {
               if (propList != null) {
                  propList.addProperty(prop);
                  return propList;
               }

               return prop;
            }

            if (propList == null) {
               propList = new ListProperty(prop);
            } else {
               propList.addProperty(prop);
            }
         }
      }
   }

   private Property parseAdditiveExpr() throws PropertyException {
      Property prop = this.parseMultiplicativeExpr();

      while(true) {
         switch (this.currentToken) {
            case 8:
               this.next();
               prop = this.evalAddition(prop.getNumeric(), this.parseMultiplicativeExpr().getNumeric());
               break;
            case 9:
               this.next();
               prop = this.evalSubtraction(prop.getNumeric(), this.parseMultiplicativeExpr().getNumeric());
               break;
            default:
               return prop;
         }
      }
   }

   private Property parseMultiplicativeExpr() throws PropertyException {
      Property prop = this.parseUnaryExpr();

      while(true) {
         switch (this.currentToken) {
            case 2:
               this.next();
               prop = this.evalMultiply(prop.getNumeric(), this.parseUnaryExpr().getNumeric());
               break;
            case 10:
               this.next();
               prop = this.evalModulo(prop.getNumber(), this.parseUnaryExpr().getNumber());
               break;
            case 11:
               this.next();
               prop = this.evalDivide(prop.getNumeric(), this.parseUnaryExpr().getNumeric());
               break;
            default:
               return prop;
         }
      }
   }

   private Property parseUnaryExpr() throws PropertyException {
      if (this.currentToken == 9) {
         this.next();
         return this.evalNegate(this.parseUnaryExpr().getNumeric());
      } else {
         return this.parsePrimaryExpr();
      }
   }

   private void expectRpar() throws PropertyException {
      if (this.currentToken != 4) {
         throw new PropertyException("expected )");
      } else {
         this.next();
      }
   }

   private Property parsePrimaryExpr() throws PropertyException {
      if (this.currentToken == 13) {
         this.next();
      }

      Object prop;
      Property prop;
      switch (this.currentToken) {
         case 1:
            prop = new NCnameProperty(this.currentTokenValue);
            break;
         case 2:
         case 4:
         case 6:
         case 8:
         case 9:
         case 10:
         case 11:
         case 13:
         default:
            throw new PropertyException("syntax error");
         case 3:
            this.next();
            prop = this.parseAdditiveExpr();
            this.expectRpar();
            return prop;
         case 5:
            prop = StringProperty.getInstance(this.currentTokenValue);
            break;
         case 7:
            Function function = (Function)FUNCTION_TABLE.get(this.currentTokenValue);
            if (function == null) {
               throw new PropertyException("no such function: " + this.currentTokenValue);
            }

            this.next();
            this.propInfo.pushFunction(function);
            prop = function.eval(this.parseArgs(function), this.propInfo);
            this.propInfo.popFunction();
            return prop;
         case 12:
            int numLen = this.currentTokenValue.length() - this.currentUnitLength;
            String unitPart = this.currentTokenValue.substring(numLen);
            double numPart = Double.parseDouble(this.currentTokenValue.substring(0, numLen));
            if ("em".equals(unitPart)) {
               prop = (Property)NumericOp.multiply(NumberProperty.getInstance(numPart), this.propInfo.currentFontSize());
            } else if ("px".equals(unitPart)) {
               float resolution = this.propInfo.getPropertyList().getFObj().getUserAgent().getSourceResolution();
               prop = FixedLength.getInstance(numPart, unitPart, 72.0F / resolution);
            } else {
               prop = FixedLength.getInstance(numPart, unitPart);
            }
            break;
         case 14:
            double pcval = Double.parseDouble(this.currentTokenValue.substring(0, this.currentTokenValue.length() - 1)) / 100.0;
            PercentBase pcBase = this.propInfo.getPercentBase();
            if (pcBase != null) {
               if (pcBase.getDimension() == 0) {
                  prop = NumberProperty.getInstance(pcval * pcBase.getBaseValue());
               } else {
                  if (pcBase.getDimension() != 1) {
                     throw new PropertyException("Illegal percent dimension value");
                  }

                  if (pcBase instanceof LengthBase) {
                     if (pcval == 0.0) {
                        prop = FixedLength.ZERO_FIXED_LENGTH;
                        break;
                     }

                     Length base = ((LengthBase)pcBase).getBaseLength();
                     if (base != null && base.isAbsolute()) {
                        prop = FixedLength.getInstance(pcval * (double)base.getValue());
                        break;
                     }
                  }

                  prop = new PercentLength(pcval, pcBase);
               }
            } else {
               prop = NumberProperty.getInstance(pcval);
            }
            break;
         case 15:
            prop = ColorProperty.getInstance(this.propInfo.getUserAgent(), this.currentTokenValue);
            break;
         case 16:
            prop = NumberProperty.getInstance(Double.valueOf(this.currentTokenValue));
            break;
         case 17:
            prop = NumberProperty.getInstance(Integer.valueOf(this.currentTokenValue));
      }

      this.next();
      return (Property)prop;
   }

   Property[] parseArgs(Function function) throws PropertyException {
      int numReq = function.getRequiredArgsCount();
      int numOpt = function.getOptionalArgsCount();
      boolean hasVar = function.hasVariableArgs();
      List args = new ArrayList(numReq + numOpt);
      int i;
      if (this.currentToken == 4) {
         this.next();
      } else {
         while(true) {
            Property p = this.parseAdditiveExpr();
            i = args.size();
            if (i >= numReq && i - numReq >= numOpt && !hasVar) {
               throw new PropertyException("Unexpected function argument at index " + i);
            }

            args.add(p);
            if (this.currentToken != 13) {
               this.expectRpar();
               break;
            }

            this.next();
         }
      }

      int numArgs = args.size();
      if (numArgs < numReq) {
         throw new PropertyException("Expected " + numReq + " required arguments, but only " + numArgs + " specified");
      } else {
         for(i = 0; i < numOpt; ++i) {
            if (args.size() < numReq + i + 1) {
               args.add(function.getOptionalArgDefault(i, this.propInfo));
            }
         }

         return (Property[])args.toArray(new Property[args.size()]);
      }
   }

   private Property evalAddition(Numeric op1, Numeric op2) throws PropertyException {
      if (op1 != null && op2 != null) {
         return (Property)NumericOp.addition(op1, op2);
      } else {
         throw new PropertyException("Non numeric operand in addition");
      }
   }

   private Property evalSubtraction(Numeric op1, Numeric op2) throws PropertyException {
      if (op1 != null && op2 != null) {
         return (Property)NumericOp.subtraction(op1, op2);
      } else {
         throw new PropertyException("Non numeric operand in subtraction");
      }
   }

   private Property evalNegate(Numeric op) throws PropertyException {
      if (op == null) {
         throw new PropertyException("Non numeric operand to unary minus");
      } else {
         return (Property)NumericOp.negate(op);
      }
   }

   private Property evalMultiply(Numeric op1, Numeric op2) throws PropertyException {
      if (op1 != null && op2 != null) {
         return (Property)NumericOp.multiply(op1, op2);
      } else {
         throw new PropertyException("Non numeric operand in multiplication");
      }
   }

   private Property evalDivide(Numeric op1, Numeric op2) throws PropertyException {
      if (op1 != null && op2 != null) {
         return (Property)NumericOp.divide(op1, op2);
      } else {
         throw new PropertyException("Non numeric operand in division");
      }
   }

   private Property evalModulo(Number op1, Number op2) throws PropertyException {
      if (op1 != null && op2 != null) {
         return NumberProperty.getInstance(op1.doubleValue() % op2.doubleValue());
      } else {
         throw new PropertyException("Non number operand to modulo");
      }
   }

   static {
      FUNCTION_TABLE.put("ceiling", new CeilingFunction());
      FUNCTION_TABLE.put("floor", new FloorFunction());
      FUNCTION_TABLE.put("round", new RoundFunction());
      FUNCTION_TABLE.put("min", new MinFunction());
      FUNCTION_TABLE.put("max", new MaxFunction());
      FUNCTION_TABLE.put("abs", new AbsFunction());
      FUNCTION_TABLE.put("rgb", new RGBColorFunction());
      FUNCTION_TABLE.put("system-color", new SystemColorFunction());
      FUNCTION_TABLE.put("from-table-column", new FromTableColumnFunction());
      FUNCTION_TABLE.put("inherited-property-value", new InheritedPropFunction());
      FUNCTION_TABLE.put("from-nearest-specified-value", new FromNearestSpecifiedValueFunction());
      FUNCTION_TABLE.put("from-parent", new FromParentFunction());
      FUNCTION_TABLE.put("proportional-column-width", new ProportionalColumnWidthFunction());
      FUNCTION_TABLE.put("label-end", new LabelEndFunction());
      FUNCTION_TABLE.put("body-start", new BodyStartFunction());
      FUNCTION_TABLE.put("rgb-icc", new RGBICCColorFunction());
      FUNCTION_TABLE.put("rgb-named-color", new RGBNamedColorFunction());
      FUNCTION_TABLE.put("cie-lab-color", new CIELabColorFunction());
      FUNCTION_TABLE.put("cmyk", new CMYKColorFunction());
      FUNCTION_TABLE.put("oca", new OCAColorFunction());
   }
}
