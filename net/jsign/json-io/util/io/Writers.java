package net.jsign.json-io.util.io;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Writers {
   protected static void writeJsonUtf8String(String s, Writer output) throws IOException {
      JsonWriter.writeJsonUtf8String(s, output);
   }

   public static class AtomicBooleanWriter implements JsonWriter.JsonClassWriter {
      public void write(Object obj, boolean showType, Writer output) throws IOException {
         if (showType) {
            AtomicBoolean value = (AtomicBoolean)obj;
            output.write("\"value\":");
            output.write(value.toString());
         } else {
            this.writePrimitiveForm(obj, output);
         }

      }

      public boolean hasPrimitiveForm() {
         return true;
      }

      public void writePrimitiveForm(Object o, Writer output) throws IOException {
         AtomicBoolean value = (AtomicBoolean)o;
         output.write(value.toString());
      }
   }

   public static class AtomicIntegerWriter implements JsonWriter.JsonClassWriter {
      public void write(Object obj, boolean showType, Writer output) throws IOException {
         if (showType) {
            AtomicInteger value = (AtomicInteger)obj;
            output.write("\"value\":");
            output.write(value.toString());
         } else {
            this.writePrimitiveForm(obj, output);
         }

      }

      public boolean hasPrimitiveForm() {
         return true;
      }

      public void writePrimitiveForm(Object o, Writer output) throws IOException {
         AtomicInteger value = (AtomicInteger)o;
         output.write(value.toString());
      }
   }

   public static class AtomicLongWriter implements JsonWriter.JsonClassWriter {
      public void write(Object obj, boolean showType, Writer output) throws IOException {
         if (showType) {
            AtomicLong value = (AtomicLong)obj;
            output.write("\"value\":");
            output.write(value.toString());
         } else {
            this.writePrimitiveForm(obj, output);
         }

      }

      public boolean hasPrimitiveForm() {
         return true;
      }

      public void writePrimitiveForm(Object o, Writer output) throws IOException {
         AtomicLong value = (AtomicLong)o;
         output.write(value.toString());
      }
   }

   public static class BigDecimalWriter implements JsonWriter.JsonClassWriter {
      public void write(Object obj, boolean showType, Writer output) throws IOException {
         if (showType) {
            BigDecimal big = (BigDecimal)obj;
            output.write("\"value\":\"");
            output.write(big.toPlainString());
            output.write(34);
         } else {
            this.writePrimitiveForm(obj, output);
         }

      }

      public boolean hasPrimitiveForm() {
         return true;
      }

      public void writePrimitiveForm(Object o, Writer output) throws IOException {
         BigDecimal big = (BigDecimal)o;
         output.write(34);
         output.write(big.toPlainString());
         output.write(34);
      }
   }

   public static class BigIntegerWriter implements JsonWriter.JsonClassWriter {
      public void write(Object obj, boolean showType, Writer output) throws IOException {
         if (showType) {
            BigInteger big = (BigInteger)obj;
            output.write("\"value\":\"");
            output.write(big.toString(10));
            output.write(34);
         } else {
            this.writePrimitiveForm(obj, output);
         }

      }

      public boolean hasPrimitiveForm() {
         return true;
      }

      public void writePrimitiveForm(Object o, Writer output) throws IOException {
         BigInteger big = (BigInteger)o;
         output.write(34);
         output.write(big.toString(10));
         output.write(34);
      }
   }

   public static class CalendarWriter implements JsonWriter.JsonClassWriter {
      public void write(Object obj, boolean showType, Writer output) throws IOException {
         Calendar cal = (Calendar)obj;
         ((SimpleDateFormat)MetaUtils.dateFormat.get()).setTimeZone(cal.getTimeZone());
         output.write("\"time\":\"");
         output.write(((SimpleDateFormat)MetaUtils.dateFormat.get()).format(cal.getTime()));
         output.write("\",\"zone\":\"");
         output.write(cal.getTimeZone().getID());
         output.write(34);
      }

      public boolean hasPrimitiveForm() {
         return false;
      }

      public void writePrimitiveForm(Object o, Writer output) throws IOException {
      }
   }

   public static class ClassWriter implements JsonWriter.JsonClassWriter {
      public void write(Object obj, boolean showType, Writer output) throws IOException {
         String value = ((Class)obj).getName();
         output.write("\"value\":");
         Writers.writeJsonUtf8String(value, output);
      }

      public boolean hasPrimitiveForm() {
         return true;
      }

      public void writePrimitiveForm(Object o, Writer output) throws IOException {
         Writers.writeJsonUtf8String(((Class)o).getName(), output);
      }
   }

   public static class DateWriter implements JsonWriter.JsonClassWriter, JsonWriter.JsonClassWriterEx {
      public void write(Object obj, boolean showType, Writer output) throws IOException {
         throw new JsonIoException("Should never be called.");
      }

      public void write(Object obj, boolean showType, Writer output, Map args) throws IOException {
         Date date = (Date)obj;
         Object dateFormat = args.get("DATE_FORMAT");
         if (dateFormat instanceof String) {
            dateFormat = new SimpleDateFormat((String)dateFormat, Locale.ENGLISH);
            args.put("DATE_FORMAT", dateFormat);
         }

         if (showType) {
            output.write("\"value\":");
         }

         if (dateFormat instanceof Format) {
            output.write("\"");
            output.write(((Format)dateFormat).format(date));
            output.write("\"");
         } else {
            output.write(Long.toString(((Date)obj).getTime()));
         }

      }

      public boolean hasPrimitiveForm() {
         return true;
      }

      public void writePrimitiveForm(Object o, Writer output) throws IOException {
         throw new JsonIoException("Should never be called.");
      }

      public void writePrimitiveForm(Object o, Writer output, Map args) throws IOException {
         if (args.containsKey("DATE_FORMAT")) {
            this.write(o, false, output, args);
         } else {
            output.write(Long.toString(((Date)o).getTime()));
         }

      }
   }

   public static class JsonStringWriter implements JsonWriter.JsonClassWriter {
      public void write(Object obj, boolean showType, Writer output) throws IOException {
         output.write("\"value\":");
         Writers.writeJsonUtf8String((String)obj, output);
      }

      public boolean hasPrimitiveForm() {
         return true;
      }

      public void writePrimitiveForm(Object o, Writer output) throws IOException {
         Writers.writeJsonUtf8String((String)o, output);
      }
   }

   public static class LocaleWriter implements JsonWriter.JsonClassWriter {
      public void write(Object obj, boolean showType, Writer output) throws IOException {
         Locale locale = (Locale)obj;
         output.write("\"language\":\"");
         output.write(locale.getLanguage());
         output.write("\",\"country\":\"");
         output.write(locale.getCountry());
         output.write("\",\"variant\":\"");
         output.write(locale.getVariant());
         output.write(34);
      }

      public boolean hasPrimitiveForm() {
         return false;
      }

      public void writePrimitiveForm(Object o, Writer output) throws IOException {
      }
   }

   public static class StringBufferWriter implements JsonWriter.JsonClassWriter {
      public void write(Object obj, boolean showType, Writer output) throws IOException {
         StringBuffer buffer = (StringBuffer)obj;
         output.write("\"value\":\"");
         output.write(buffer.toString());
         output.write(34);
      }

      public boolean hasPrimitiveForm() {
         return true;
      }

      public void writePrimitiveForm(Object o, Writer output) throws IOException {
         StringBuffer buffer = (StringBuffer)o;
         output.write(34);
         output.write(buffer.toString());
         output.write(34);
      }
   }

   public static class StringBuilderWriter implements JsonWriter.JsonClassWriter {
      public void write(Object obj, boolean showType, Writer output) throws IOException {
         StringBuilder builder = (StringBuilder)obj;
         output.write("\"value\":\"");
         output.write(builder.toString());
         output.write(34);
      }

      public boolean hasPrimitiveForm() {
         return true;
      }

      public void writePrimitiveForm(Object o, Writer output) throws IOException {
         StringBuilder builder = (StringBuilder)o;
         output.write(34);
         output.write(builder.toString());
         output.write(34);
      }
   }

   public static class TimeZoneWriter implements JsonWriter.JsonClassWriter {
      public void write(Object obj, boolean showType, Writer output) throws IOException {
         TimeZone cal = (TimeZone)obj;
         output.write("\"zone\":\"");
         output.write(cal.getID());
         output.write(34);
      }

      public boolean hasPrimitiveForm() {
         return false;
      }

      public void writePrimitiveForm(Object o, Writer output) throws IOException {
      }
   }

   public static class TimestampWriter implements JsonWriter.JsonClassWriter {
      public void write(Object o, boolean showType, Writer output) throws IOException {
         Timestamp tstamp = (Timestamp)o;
         output.write("\"time\":\"");
         output.write(Long.toString(tstamp.getTime() / 1000L * 1000L));
         output.write("\",\"nanos\":\"");
         output.write(Integer.toString(tstamp.getNanos()));
         output.write(34);
      }

      public boolean hasPrimitiveForm() {
         return false;
      }

      public void writePrimitiveForm(Object o, Writer output) throws IOException {
      }
   }
}
