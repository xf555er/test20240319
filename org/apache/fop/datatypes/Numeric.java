package org.apache.fop.datatypes;

public interface Numeric {
   double getNumericValue();

   double getNumericValue(PercentBaseContext var1);

   int getDimension();

   boolean isAbsolute();

   int getValue();

   int getValue(PercentBaseContext var1);

   int getEnum();
}
