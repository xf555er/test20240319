package org.apache.fop.fo.properties;

import org.apache.fop.datatypes.PercentBaseContext;
import org.apache.fop.fo.FObj;
import org.apache.fop.util.CompareUtil;

public class TableColLength extends LengthProperty {
   private double tcolUnits;
   private FObj column;

   public TableColLength(double tcolUnits, FObj column) {
      this.tcolUnits = tcolUnits;
      this.column = column;
   }

   public double getTableUnits() {
      return this.tcolUnits;
   }

   public boolean isAbsolute() {
      return false;
   }

   public double getNumericValue() {
      throw new UnsupportedOperationException("Must call getNumericValue with PercentBaseContext");
   }

   public double getNumericValue(PercentBaseContext context) {
      return this.tcolUnits * (double)context.getBaseLength(11, this.column);
   }

   public int getValue() {
      throw new UnsupportedOperationException("Must call getValue with PercentBaseContext");
   }

   public int getValue(PercentBaseContext context) {
      return (int)(this.tcolUnits * (double)context.getBaseLength(11, this.column));
   }

   public String toString() {
      return Double.toString(this.tcolUnits) + " table-column-units";
   }

   public int hashCode() {
      int prime = true;
      int result = 1;
      result = 31 * result + CompareUtil.getHashCode(this.column);
      result = 31 * result + CompareUtil.getHashCode(this.tcolUnits);
      return result;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof TableColLength)) {
         return false;
      } else {
         TableColLength other = (TableColLength)obj;
         return CompareUtil.equal(this.column, other.column) && CompareUtil.equal(this.tcolUnits, other.tcolUnits);
      }
   }
}
