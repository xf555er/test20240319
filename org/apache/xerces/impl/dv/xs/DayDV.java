package org.apache.xerces.impl.dv.xs;

import javax.xml.datatype.XMLGregorianCalendar;
import org.apache.xerces.impl.dv.InvalidDatatypeValueException;
import org.apache.xerces.impl.dv.ValidationContext;

public class DayDV extends AbstractDateTimeDV {
   private static final int DAY_SIZE = 5;

   public Object getActualValue(String var1, ValidationContext var2) throws InvalidDatatypeValueException {
      try {
         return this.parse(var1);
      } catch (Exception var4) {
         throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.1", new Object[]{var1, "gDay"});
      }
   }

   protected AbstractDateTimeDV.DateTimeData parse(String var1) throws SchemaDateTimeException {
      AbstractDateTimeDV.DateTimeData var2 = new AbstractDateTimeDV.DateTimeData(var1, this);
      int var3 = var1.length();
      if (var1.charAt(0) == '-' && var1.charAt(1) == '-' && var1.charAt(2) == '-') {
         var2.year = 2000;
         var2.month = 1;
         var2.day = this.parseInt(var1, 3, 5);
         if (5 < var3) {
            if (!this.isNextCharUTCSign(var1, 5, var3)) {
               throw new SchemaDateTimeException("Error in day parsing");
            }

            this.getTimeZone(var1, var2, 5, var3);
         }

         this.validateDateTime(var2);
         this.saveUnnormalized(var2);
         if (var2.utc != 0 && var2.utc != 90) {
            this.normalize(var2);
         }

         var2.position = 2;
         return var2;
      } else {
         throw new SchemaDateTimeException("Error in day parsing");
      }
   }

   protected String dateToString(AbstractDateTimeDV.DateTimeData var1) {
      StringBuffer var2 = new StringBuffer(6);
      var2.append('-');
      var2.append('-');
      var2.append('-');
      this.append(var2, var1.day, 2);
      this.append(var2, (char)var1.utc, 0);
      return var2.toString();
   }

   protected XMLGregorianCalendar getXMLGregorianCalendar(AbstractDateTimeDV.DateTimeData var1) {
      return AbstractDateTimeDV.datatypeFactory.newXMLGregorianCalendar(Integer.MIN_VALUE, Integer.MIN_VALUE, var1.unNormDay, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, var1.hasTimeZone() ? var1.timezoneHr * 60 + var1.timezoneMin : Integer.MIN_VALUE);
   }
}
