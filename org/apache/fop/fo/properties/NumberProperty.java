package org.apache.fop.fo.properties;

import java.awt.Color;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.datatypes.PercentBaseContext;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.util.CompareUtil;

public final class NumberProperty extends Property implements Numeric {
   private static final PropertyCache CACHE = new PropertyCache();
   private final Number number;

   private NumberProperty(double num) {
      if (num == Math.floor(num)) {
         if (num < 2.147483647E9) {
            this.number = (int)num;
         } else {
            this.number = (long)num;
         }
      } else {
         this.number = num;
      }

   }

   private NumberProperty(int num) {
      this.number = num;
   }

   public static NumberProperty getInstance(Double num) {
      return (NumberProperty)CACHE.fetch(new NumberProperty(num));
   }

   public static NumberProperty getInstance(Integer num) {
      return (NumberProperty)CACHE.fetch(new NumberProperty(num));
   }

   public static NumberProperty getInstance(double num) {
      return (NumberProperty)CACHE.fetch(new NumberProperty(num));
   }

   public static NumberProperty getInstance(int num) {
      return (NumberProperty)CACHE.fetch(new NumberProperty(num));
   }

   public int getDimension() {
      return 0;
   }

   public double getNumericValue() {
      return this.number.doubleValue();
   }

   public double getNumericValue(PercentBaseContext context) {
      return this.getNumericValue();
   }

   public int getValue() {
      return this.number.intValue();
   }

   public int getValue(PercentBaseContext context) {
      return this.getValue();
   }

   public boolean isAbsolute() {
      return true;
   }

   public Number getNumber() {
      return this.number;
   }

   public Object getObject() {
      return this.number;
   }

   public Numeric getNumeric() {
      return this;
   }

   public Length getLength() {
      return FixedLength.getInstance(this.getNumericValue(), "px");
   }

   public Color getColor(FOUserAgent foUserAgent) {
      return Color.black;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof NumberProperty)) {
         return false;
      } else {
         NumberProperty other = (NumberProperty)obj;
         return CompareUtil.equal(this.number, other.number);
      }
   }

   public int hashCode() {
      return this.number.hashCode();
   }

   public static class PositiveIntegerMaker extends PropertyMaker {
      public PositiveIntegerMaker(int propId) {
         super(propId);
      }

      public Property convertProperty(Property p, PropertyList propertyList, FObj fo) throws PropertyException {
         if (p instanceof EnumProperty) {
            return EnumNumber.getInstance(p);
         } else {
            Number val = p.getNumber();
            if (val != null) {
               int i = Math.round(val.floatValue());
               if (i <= 0) {
                  i = 1;
               }

               return NumberProperty.getInstance(i);
            } else {
               return this.convertPropertyDatatype(p, propertyList, fo);
            }
         }
      }
   }

   public static class Maker extends PropertyMaker {
      public Maker(int propId) {
         super(propId);
      }

      public Property convertProperty(Property p, PropertyList propertyList, FObj fo) throws PropertyException {
         if (p instanceof NumberProperty) {
            return p;
         } else if (p instanceof EnumProperty) {
            return EnumNumber.getInstance(p);
         } else {
            Number val = p.getNumber();
            return (Property)(val != null ? NumberProperty.getInstance(val.doubleValue()) : this.convertPropertyDatatype(p, propertyList, fo));
         }
      }
   }
}
