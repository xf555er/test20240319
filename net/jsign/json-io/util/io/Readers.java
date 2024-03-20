package net.jsign.json-io.util.io;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Readers {
   private static final Pattern datePattern1 = Pattern.compile("(\\d{4})[./-](\\d{1,2})[./-](\\d{1,2})");
   private static final Pattern datePattern2 = Pattern.compile("(\\d{1,2})[./-](\\d{1,2})[./-](\\d{4})");
   private static final Pattern datePattern3 = Pattern.compile("(January|Jan|February|Feb|March|Mar|April|Apr|May|June|Jun|July|Jul|August|Aug|September|Sept|Sep|October|Oct|November|Nov|December|Dec)[ ]*[,]?[ ]*(\\d{1,2})(st|nd|rd|th|)[ ]*[,]?[ ]*(\\d{4})", 2);
   private static final Pattern datePattern4 = Pattern.compile("(\\d{1,2})(st|nd|rd|th|)[ ]*[,]?[ ]*(January|Jan|February|Feb|March|Mar|April|Apr|May|June|Jun|July|Jul|August|Aug|September|Sept|Sep|October|Oct|November|Nov|December|Dec)[ ]*[,]?[ ]*(\\d{4})", 2);
   private static final Pattern datePattern5 = Pattern.compile("(\\d{4})[ ]*[,]?[ ]*(January|Jan|February|Feb|March|Mar|April|Apr|May|June|Jun|July|Jul|August|Aug|September|Sept|Sep|October|Oct|November|Nov|December|Dec)[ ]*[,]?[ ]*(\\d{1,2})(st|nd|rd|th|)", 2);
   private static final Pattern datePattern6 = Pattern.compile("(monday|mon|tuesday|tues|tue|wednesday|wed|thursday|thur|thu|friday|fri|saturday|sat|sunday|sun)[ ]+(January|Jan|February|Feb|March|Mar|April|Apr|May|June|Jun|July|Jul|August|Aug|September|Sept|Sep|October|Oct|November|Nov|December|Dec)[ ]+(\\d{1,2})[ ]+(\\d{2}:\\d{2}:\\d{2})[ ]+[A-Z]{1,4}\\s+(\\d{4})", 2);
   private static final Pattern timePattern1 = Pattern.compile("(\\d{2})[.:](\\d{2})[.:](\\d{2})[.](\\d{1,10})([+-]\\d{2}[:]?\\d{2}|Z)?");
   private static final Pattern timePattern2 = Pattern.compile("(\\d{2})[.:](\\d{2})[.:](\\d{2})([+-]\\d{2}[:]?\\d{2}|Z)?");
   private static final Pattern timePattern3 = Pattern.compile("(\\d{2})[.:](\\d{2})([+-]\\d{2}[:]?\\d{2}|Z)?");
   private static final Pattern dayPattern = Pattern.compile("(monday|mon|tuesday|tues|tue|wednesday|wed|thursday|thur|thu|friday|fri|saturday|sat|sunday|sun)", 2);
   private static final Map months = new LinkedHashMap();

   static {
      months.put("jan", "1");
      months.put("january", "1");
      months.put("feb", "2");
      months.put("february", "2");
      months.put("mar", "3");
      months.put("march", "3");
      months.put("apr", "4");
      months.put("april", "4");
      months.put("may", "5");
      months.put("jun", "6");
      months.put("june", "6");
      months.put("jul", "7");
      months.put("july", "7");
      months.put("aug", "8");
      months.put("august", "8");
      months.put("sep", "9");
      months.put("sept", "9");
      months.put("september", "9");
      months.put("oct", "10");
      months.put("october", "10");
      months.put("nov", "11");
      months.put("november", "11");
      months.put("dec", "12");
      months.put("december", "12");
   }

   private static Object getValueFromJsonObject(Object o, Object value, String typeName) {
      if (o instanceof JsonObject) {
         JsonObject jObj = (JsonObject)o;
         if (!jObj.containsKey("value")) {
            throw new JsonIoException(typeName + " defined as JSON {} object, missing 'value' field");
         }

         value = jObj.get("value");
      }

      return value;
   }

   public static BigInteger bigIntegerFrom(Object value) {
      if (value == null) {
         return null;
      } else if (value instanceof BigInteger) {
         return (BigInteger)value;
      } else if (value instanceof String) {
         String s = (String)value;
         if ("".equals(s.trim())) {
            return null;
         } else {
            try {
               return new BigInteger(MetaUtils.removeLeadingAndTrailingQuotes(s));
            } catch (Exception var3) {
               throw new JsonIoException("Could not parse '" + value + "' as BigInteger.", var3);
            }
         }
      } else if (value instanceof BigDecimal) {
         BigDecimal bd = (BigDecimal)value;
         return bd.toBigInteger();
      } else if (value instanceof Boolean) {
         return (Boolean)value ? BigInteger.ONE : BigInteger.ZERO;
      } else if (!(value instanceof Double) && !(value instanceof Float)) {
         if (!(value instanceof Long) && !(value instanceof Integer) && !(value instanceof Short) && !(value instanceof Byte)) {
            throw new JsonIoException("Could not convert value: " + value.toString() + " to BigInteger.");
         } else {
            return new BigInteger(value.toString());
         }
      } else {
         return (new BigDecimal(((Number)value).doubleValue())).toBigInteger();
      }
   }

   public static BigDecimal bigDecimalFrom(Object value) {
      if (value == null) {
         return null;
      } else if (value instanceof BigDecimal) {
         return (BigDecimal)value;
      } else if (value instanceof String) {
         String s = (String)value;
         if ("".equals(s.trim())) {
            return null;
         } else {
            try {
               return new BigDecimal(MetaUtils.removeLeadingAndTrailingQuotes(s));
            } catch (Exception var3) {
               throw new JsonIoException("Could not parse '" + s + "' as BigDecimal.", var3);
            }
         }
      } else if (value instanceof BigInteger) {
         return new BigDecimal((BigInteger)value);
      } else if (value instanceof Boolean) {
         return (Boolean)value ? BigDecimal.ONE : BigDecimal.ZERO;
      } else if (!(value instanceof Long) && !(value instanceof Integer) && !(value instanceof Double) && !(value instanceof Short) && !(value instanceof Byte) && !(value instanceof Float)) {
         throw new JsonIoException("Could not convert value: " + value.toString() + " to BigInteger.");
      } else {
         return new BigDecimal(value.toString());
      }
   }

   static Class classForName(String name, ClassLoader classLoader) {
      return MetaUtils.classForName(name, classLoader);
   }

   static Object newInstance(Class c, JsonObject jsonObject) {
      return JsonReader.newInstance(c, jsonObject);
   }

   public static class AtomicBooleanReader implements JsonReader.JsonClassReaderEx {
      public Object read(Object o, Deque stack, Map args) {
         Object value = Readers.getValueFromJsonObject(o, o, "AtomicBoolean");
         if (value instanceof String) {
            String state = (String)value;
            return "".equals(state.trim()) ? null : new AtomicBoolean("true".equalsIgnoreCase(state));
         } else if (value instanceof Boolean) {
            return new AtomicBoolean((Boolean)value);
         } else if (value instanceof Number && !(value instanceof Double) && !(value instanceof Float)) {
            return new AtomicBoolean(((Number)value).longValue() != 0L);
         } else {
            throw new JsonIoException("Unknown value in JSON assigned to AtomicBoolean, value type = " + value.getClass().getName());
         }
      }
   }

   public static class AtomicIntegerReader implements JsonReader.JsonClassReaderEx {
      public Object read(Object o, Deque stack, Map args) {
         Object value = Readers.getValueFromJsonObject(o, o, "AtomicInteger");
         if (value instanceof String) {
            String num = (String)value;
            return "".equals(num.trim()) ? null : new AtomicInteger(Integer.parseInt(MetaUtils.removeLeadingAndTrailingQuotes(num)));
         } else if (value instanceof Number && !(value instanceof Double) && !(value instanceof Float)) {
            return new AtomicInteger(((Number)value).intValue());
         } else {
            throw new JsonIoException("Unknown value in JSON assigned to AtomicInteger, value type = " + value.getClass().getName());
         }
      }
   }

   public static class AtomicLongReader implements JsonReader.JsonClassReaderEx {
      public Object read(Object o, Deque stack, Map args) {
         Object value = Readers.getValueFromJsonObject(o, o, "AtomicLong");
         if (value instanceof String) {
            String num = (String)value;
            return "".equals(num.trim()) ? null : new AtomicLong(Long.parseLong(MetaUtils.removeLeadingAndTrailingQuotes(num)));
         } else if (value instanceof Number && !(value instanceof Double) && !(value instanceof Float)) {
            return new AtomicLong(((Number)value).longValue());
         } else {
            throw new JsonIoException("Unknown value in JSON assigned to AtomicLong, value type = " + value.getClass().getName());
         }
      }
   }

   public static class BigDecimalReader implements JsonReader.JsonClassReaderEx {
      public Object read(Object o, Deque stack, Map args) {
         JsonObject jObj = null;
         Object value = o;
         if (o instanceof JsonObject) {
            jObj = (JsonObject)o;
            if (!jObj.containsKey("value")) {
               throw new JsonIoException("BigDecimal missing 'value' field");
            }

            value = jObj.get("value");
         }

         if (value instanceof JsonObject) {
            JsonObject valueObj = (JsonObject)value;
            if ("java.math.BigInteger".equals(valueObj.type)) {
               BigIntegerReader reader = new BigIntegerReader();
               value = reader.read(value, stack, args);
            } else {
               if (!"java.math.BigDecimal".equals(valueObj.type)) {
                  return Readers.bigDecimalFrom(valueObj.get("value"));
               }

               value = this.read(value, stack, args);
            }
         }

         BigDecimal x = Readers.bigDecimalFrom(value);
         if (jObj != null) {
            jObj.target = x;
         }

         return x;
      }
   }

   public static class BigIntegerReader implements JsonReader.JsonClassReaderEx {
      public Object read(Object o, Deque stack, Map args) {
         JsonObject jObj = null;
         Object value = o;
         if (o instanceof JsonObject) {
            jObj = (JsonObject)o;
            if (!jObj.containsKey("value")) {
               throw new JsonIoException("BigInteger missing 'value' field");
            }

            value = jObj.get("value");
         }

         if (value instanceof JsonObject) {
            JsonObject valueObj = (JsonObject)value;
            if ("java.math.BigDecimal".equals(valueObj.type)) {
               BigDecimalReader reader = new BigDecimalReader();
               value = reader.read(value, stack, args);
            } else {
               if (!"java.math.BigInteger".equals(valueObj.type)) {
                  return Readers.bigIntegerFrom(valueObj.get("value"));
               }

               value = this.read(value, stack, args);
            }
         }

         BigInteger x = Readers.bigIntegerFrom(value);
         if (jObj != null) {
            jObj.target = x;
         }

         return x;
      }
   }

   public static class CalendarReader implements JsonReader.JsonClassReaderEx {
      public Object read(Object o, Deque stack, Map args) {
         String time = null;

         try {
            JsonObject jObj = (JsonObject)o;
            time = (String)jObj.get("time");
            if (time == null) {
               throw new JsonIoException("Calendar missing 'time' field");
            } else {
               Date date = ((SimpleDateFormat)MetaUtils.dateFormat.get()).parse(time);
               Class c;
               if (jObj.getTarget() != null) {
                  c = jObj.getTarget().getClass();
               } else {
                  Object type = jObj.type;
                  c = Readers.classForName((String)type, (ClassLoader)args.get("CLASSLOADER"));
               }

               Calendar calendar = (Calendar)Readers.newInstance(c, jObj);
               calendar.setTime(date);
               jObj.setTarget(calendar);
               String zone = (String)jObj.get("zone");
               if (zone != null) {
                  calendar.setTimeZone(TimeZone.getTimeZone(zone));
               }

               return calendar;
            }
         } catch (Exception var10) {
            throw new JsonIoException("Failed to parse calendar, time: " + time);
         }
      }
   }

   public static class ClassReader implements JsonReader.JsonClassReaderEx {
      public Object read(Object o, Deque stack, Map args) {
         if (o instanceof String) {
            return Readers.classForName((String)o, (ClassLoader)args.get("CLASSLOADER"));
         } else {
            JsonObject jObj = (JsonObject)o;
            if (jObj.containsKey("value")) {
               jObj.target = Readers.classForName((String)jObj.get("value"), (ClassLoader)args.get("CLASSLOADER"));
               return jObj.target;
            } else {
               throw new JsonIoException("Class missing 'value' field");
            }
         }
      }
   }

   public static class DateReader implements JsonReader.JsonClassReaderEx {
      public Object read(Object o, Deque stack, Map args) {
         if (o instanceof Long) {
            return new Date((Long)o);
         } else if (o instanceof String) {
            return parseDate((String)o);
         } else if (o instanceof JsonObject) {
            JsonObject jObj = (JsonObject)o;
            Object val = jObj.get("value");
            if (val instanceof Long) {
               return new Date((Long)val);
            } else if (val instanceof String) {
               return parseDate((String)val);
            } else {
               throw new JsonIoException("Unable to parse date: " + o);
            }
         } else {
            throw new JsonIoException("Unable to parse date, encountered unknown object: " + o);
         }
      }

      static Date parseDate(String dateStr) {
         dateStr = dateStr.trim();
         if (dateStr.isEmpty()) {
            return null;
         } else {
            Matcher matcher = Readers.datePattern1.matcher(dateStr);
            String month = null;
            String mon = null;
            String year;
            String day;
            String remains;
            if (matcher.find()) {
               year = matcher.group(1);
               month = matcher.group(2);
               day = matcher.group(3);
               remains = matcher.replaceFirst("");
            } else {
               matcher = Readers.datePattern2.matcher(dateStr);
               if (matcher.find()) {
                  month = matcher.group(1);
                  day = matcher.group(2);
                  year = matcher.group(3);
                  remains = matcher.replaceFirst("");
               } else {
                  matcher = Readers.datePattern3.matcher(dateStr);
                  if (matcher.find()) {
                     mon = matcher.group(1);
                     day = matcher.group(2);
                     year = matcher.group(4);
                     remains = matcher.replaceFirst("");
                  } else {
                     matcher = Readers.datePattern4.matcher(dateStr);
                     if (matcher.find()) {
                        day = matcher.group(1);
                        mon = matcher.group(3);
                        year = matcher.group(4);
                        remains = matcher.replaceFirst("");
                     } else {
                        matcher = Readers.datePattern5.matcher(dateStr);
                        if (matcher.find()) {
                           year = matcher.group(1);
                           mon = matcher.group(2);
                           day = matcher.group(3);
                           remains = matcher.replaceFirst("");
                        } else {
                           matcher = Readers.datePattern6.matcher(dateStr);
                           if (!matcher.find()) {
                              throw new JsonIoException("Unable to parse: " + dateStr);
                           }

                           year = matcher.group(5);
                           mon = matcher.group(2);
                           day = matcher.group(3);
                           remains = matcher.group(4);
                        }
                     }
                  }
               }
            }

            if (mon != null) {
               month = (String)Readers.months.get(mon.trim().toLowerCase());
            }

            String hour = null;
            String min = null;
            String sec = "00";
            String milli = "0";
            String tz = null;
            remains = remains.trim();
            matcher = Readers.timePattern1.matcher(remains);
            if (matcher.find()) {
               hour = matcher.group(1);
               min = matcher.group(2);
               sec = matcher.group(3);
               milli = matcher.group(4);
               if (matcher.groupCount() > 4) {
                  tz = matcher.group(5);
               }
            } else {
               matcher = Readers.timePattern2.matcher(remains);
               if (matcher.find()) {
                  hour = matcher.group(1);
                  min = matcher.group(2);
                  sec = matcher.group(3);
                  if (matcher.groupCount() > 3) {
                     tz = matcher.group(4);
                  }
               } else {
                  matcher = Readers.timePattern3.matcher(remains);
                  if (matcher.find()) {
                     hour = matcher.group(1);
                     min = matcher.group(2);
                     if (matcher.groupCount() > 2) {
                        tz = matcher.group(3);
                     }
                  } else {
                     matcher = null;
                  }
               }
            }

            if (matcher != null) {
               remains = matcher.replaceFirst("");
            }

            if (remains != null && remains.length() > 0) {
               Matcher dayMatcher = Readers.dayPattern.matcher(remains);
               if (dayMatcher.find()) {
                  remains = dayMatcher.replaceFirst("").trim();
               }
            }

            if (remains != null && remains.length() > 0) {
               remains = remains.trim();
               if (!remains.equals(",") && !remains.equals("T")) {
                  throw new JsonIoException("Issue parsing data/time, other characters present: " + remains);
               }
            }

            Calendar c = Calendar.getInstance();
            c.clear();
            if (tz != null) {
               if ("z".equalsIgnoreCase(tz)) {
                  c.setTimeZone(TimeZone.getTimeZone("GMT"));
               } else {
                  c.setTimeZone(TimeZone.getTimeZone("GMT" + tz));
               }
            }

            int y = Integer.parseInt(year);
            int m = Integer.parseInt(month) - 1;
            int d = Integer.parseInt(day);
            if (m >= 0 && m <= 11) {
               if (d >= 1 && d <= 31) {
                  if (matcher == null) {
                     c.set(y, m, d);
                  } else {
                     int h = Integer.parseInt(hour);
                     int mn = Integer.parseInt(min);
                     int s = Integer.parseInt(sec);
                     int ms = Integer.parseInt(milli);
                     if (h > 23) {
                        throw new JsonIoException("Hour must be between 0 and 23 inclusive, time: " + dateStr);
                     }

                     if (mn > 59) {
                        throw new JsonIoException("Minute must be between 0 and 59 inclusive, time: " + dateStr);
                     }

                     if (s > 59) {
                        throw new JsonIoException("Second must be between 0 and 59 inclusive, time: " + dateStr);
                     }

                     c.set(y, m, d, h, mn, s);
                     c.set(14, ms);
                  }

                  return c.getTime();
               } else {
                  throw new JsonIoException("Day must be between 1 and 31 inclusive, date: " + dateStr);
               }
            } else {
               throw new JsonIoException("Month must be between 1 and 12 inclusive, date: " + dateStr);
            }
         }
      }
   }

   public static class LocaleReader implements JsonReader.JsonClassReaderEx {
      public Object read(Object o, Deque stack, Map args) {
         JsonObject jObj = (JsonObject)o;
         Object language = jObj.get("language");
         if (language == null) {
            throw new JsonIoException("java.util.Locale must specify 'language' field");
         } else {
            Object country = jObj.get("country");
            Object variant = jObj.get("variant");
            if (country == null) {
               jObj.target = new Locale((String)language);
               return jObj.target;
            } else if (variant == null) {
               jObj.target = new Locale((String)language, (String)country);
               return jObj.target;
            } else {
               jObj.target = new Locale((String)language, (String)country, (String)variant);
               return jObj.target;
            }
         }
      }
   }

   public static class SqlDateReader extends DateReader {
      public Object read(Object o, Deque stack, Map args) {
         return new java.sql.Date(((Date)super.read(o, stack, args)).getTime());
      }
   }

   public static class StringBufferReader implements JsonReader.JsonClassReaderEx {
      public Object read(Object o, Deque stack, Map args) {
         if (o instanceof String) {
            return new StringBuffer((String)o);
         } else {
            JsonObject jObj = (JsonObject)o;
            if (jObj.containsKey("value")) {
               jObj.target = new StringBuffer((String)jObj.get("value"));
               return jObj.target;
            } else {
               throw new JsonIoException("StringBuffer missing 'value' field");
            }
         }
      }
   }

   public static class StringBuilderReader implements JsonReader.JsonClassReaderEx {
      public Object read(Object o, Deque stack, Map args) {
         if (o instanceof String) {
            return new StringBuilder((String)o);
         } else {
            JsonObject jObj = (JsonObject)o;
            if (jObj.containsKey("value")) {
               jObj.target = new StringBuilder((String)jObj.get("value"));
               return jObj.target;
            } else {
               throw new JsonIoException("StringBuilder missing 'value' field");
            }
         }
      }
   }

   public static class StringReader implements JsonReader.JsonClassReaderEx {
      public Object read(Object o, Deque stack, Map args) {
         if (o instanceof String) {
            return o;
         } else if (MetaUtils.isPrimitive(o.getClass())) {
            return o.toString();
         } else {
            JsonObject jObj = (JsonObject)o;
            if (jObj.containsKey("value")) {
               jObj.target = jObj.get("value");
               return jObj.target;
            } else {
               throw new JsonIoException("String missing 'value' field");
            }
         }
      }
   }

   public static class TimeZoneReader implements JsonReader.JsonClassReaderEx {
      public Object read(Object o, Deque stack, Map args) {
         JsonObject jObj = (JsonObject)o;
         Object zone = jObj.get("zone");
         if (zone == null) {
            throw new JsonIoException("java.util.TimeZone must specify 'zone' field");
         } else {
            jObj.target = TimeZone.getTimeZone((String)zone);
            return jObj.target;
         }
      }
   }

   public static class TimestampReader implements JsonReader.JsonClassReaderEx {
      public Object read(Object o, Deque stack, Map args) {
         JsonObject jObj = (JsonObject)o;
         Object time = jObj.get("time");
         if (time == null) {
            throw new JsonIoException("java.sql.Timestamp must specify 'time' field");
         } else {
            Object nanos = jObj.get("nanos");
            if (nanos == null) {
               jObj.target = new Timestamp(Long.valueOf((String)time));
               return jObj.target;
            } else {
               Timestamp tstamp = new Timestamp(Long.valueOf((String)time));
               tstamp.setNanos(Integer.valueOf((String)nanos));
               jObj.target = tstamp;
               return jObj.target;
            }
         }
      }
   }
}
