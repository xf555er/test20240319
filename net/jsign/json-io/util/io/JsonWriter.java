package net.jsign.json-io.util.io;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class JsonWriter implements Closeable, Flushable {
   private static Map BASE_WRITERS;
   private final Map writers;
   private final Map writerCache;
   private final Set notCustom;
   private static final Object[] byteStrings = new Object[256];
   private static final String NEW_LINE = System.getProperty("line.separator");
   private static final Long ZERO = 0L;
   private static final NullClass nullWriter = new NullClass();
   private final Map objVisited;
   private final Map objsReferenced;
   private final Writer out;
   private Map typeNameMap;
   private boolean shortMetaKeys;
   private boolean neverShowType;
   private boolean alwaysShowType;
   private boolean isPrettyPrint;
   private boolean isEnumPublicOnly;
   private boolean writeLongsAsStrings;
   private boolean skipNullFields;
   private boolean forceMapFormatWithKeyArrays;
   private long identity;
   private int depth;
   final Map args;
   private static volatile boolean allowNanAndInfinity;

   static {
      for(short i = -128; i <= 127; ++i) {
         char[] chars = Integer.toString(i).toCharArray();
         byteStrings[i + 128] = chars;
      }

      Map temp = new HashMap();
      temp.put(String.class, new Writers.JsonStringWriter());
      temp.put(Date.class, new Writers.DateWriter());
      temp.put(AtomicBoolean.class, new Writers.AtomicBooleanWriter());
      temp.put(AtomicInteger.class, new Writers.AtomicIntegerWriter());
      temp.put(AtomicLong.class, new Writers.AtomicLongWriter());
      temp.put(BigInteger.class, new Writers.BigIntegerWriter());
      temp.put(BigDecimal.class, new Writers.BigDecimalWriter());
      temp.put(java.sql.Date.class, new Writers.DateWriter());
      temp.put(Timestamp.class, new Writers.TimestampWriter());
      temp.put(Calendar.class, new Writers.CalendarWriter());
      temp.put(TimeZone.class, new Writers.TimeZoneWriter());
      temp.put(Locale.class, new Writers.LocaleWriter());
      temp.put(Class.class, new Writers.ClassWriter());
      temp.put(StringBuilder.class, new Writers.StringBuilderWriter());
      temp.put(StringBuffer.class, new Writers.StringBufferWriter());
      BASE_WRITERS = temp;
      allowNanAndInfinity = false;
   }

   public static boolean isAllowNanAndInfinity() {
      return allowNanAndInfinity;
   }

   protected String getSubstituteTypeNameIfExists(String typeName) {
      return this.typeNameMap == null ? null : (String)this.typeNameMap.get(typeName);
   }

   protected String getSubstituteTypeName(String typeName) {
      if (this.typeNameMap == null) {
         return typeName;
      } else {
         String shortName = (String)this.typeNameMap.get(typeName);
         return shortName == null ? typeName : shortName;
      }
   }

   public static String objectToJson(Object item, Map optionalArgs) {
      try {
         ByteArrayOutputStream stream = new ByteArrayOutputStream();
         JsonWriter writer = new JsonWriter(stream, optionalArgs);
         writer.write(item);
         writer.close();
         return new String(stream.toByteArray(), "UTF-8");
      } catch (Exception var4) {
         throw new JsonIoException("Unable to convert object to JSON", var4);
      }
   }

   public JsonWriter(OutputStream out, Map optionalArgs) {
      this.writers = new HashMap(BASE_WRITERS);
      this.writerCache = new HashMap();
      this.notCustom = new HashSet();
      this.objVisited = new IdentityHashMap();
      this.objsReferenced = new IdentityHashMap();
      this.typeNameMap = null;
      this.shortMetaKeys = false;
      this.neverShowType = false;
      this.alwaysShowType = false;
      this.isPrettyPrint = false;
      this.isEnumPublicOnly = false;
      this.writeLongsAsStrings = false;
      this.skipNullFields = false;
      this.forceMapFormatWithKeyArrays = false;
      this.identity = 1L;
      this.depth = 0;
      this.args = new HashMap();
      if (optionalArgs == null) {
         optionalArgs = new HashMap();
      }

      this.args.putAll((Map)optionalArgs);
      this.args.put("JSON_WRITER", this);
      this.typeNameMap = (Map)this.args.get("TYPE_NAME_MAP");
      this.shortMetaKeys = isTrue(this.args.get("SHORT_META_KEYS"));
      this.alwaysShowType = isTrue(this.args.get("TYPE"));
      this.neverShowType = Boolean.FALSE.equals(this.args.get("TYPE")) || "false".equals(this.args.get("TYPE"));
      this.isPrettyPrint = isTrue(this.args.get("PRETTY_PRINT"));
      this.isEnumPublicOnly = isTrue(this.args.get("ENUM_PUBLIC_ONLY"));
      this.writeLongsAsStrings = isTrue(this.args.get("WLAS"));
      this.writeLongsAsStrings = isTrue(this.args.get("WLAS"));
      this.skipNullFields = isTrue(this.args.get("SKIP_NULL"));
      this.forceMapFormatWithKeyArrays = isTrue(this.args.get("FORCE_MAP_FORMAT_ARRAY_KEYS_ITEMS"));
      if (!this.args.containsKey("CLASSLOADER")) {
         this.args.put("CLASSLOADER", JsonWriter.class.getClassLoader());
      }

      Map customWriters = (Map)this.args.get("CUSTOM_WRITERS");
      if (customWriters != null) {
         Iterator var5 = customWriters.entrySet().iterator();

         while(var5.hasNext()) {
            Map.Entry entry = (Map.Entry)var5.next();
            this.addWriter((Class)entry.getKey(), (JsonClassWriterBase)entry.getValue());
         }
      }

      Collection notCustomClasses = (Collection)this.args.get("NOT_CUSTOM_WRITERS");
      if (notCustomClasses != null) {
         Iterator var6 = notCustomClasses.iterator();

         while(var6.hasNext()) {
            Class c = (Class)var6.next();
            this.addNotCustomWriter(c);
         }
      }

      Map.Entry entry;
      Iterator var8;
      Class c;
      List fields;
      ArrayList newList;
      Map classFields;
      String field;
      Iterator var14;
      Field f;
      Map blackList;
      HashMap copy;
      if (((Map)optionalArgs).containsKey("FIELD_SPECIFIERS")) {
         blackList = (Map)this.args.get("FIELD_SPECIFIERS");
         copy = new HashMap();
         var8 = blackList.entrySet().iterator();

         while(var8.hasNext()) {
            entry = (Map.Entry)var8.next();
            c = (Class)entry.getKey();
            fields = (List)entry.getValue();
            newList = new ArrayList(fields.size());
            classFields = MetaUtils.getDeepDeclaredFields(c);
            var14 = fields.iterator();

            while(var14.hasNext()) {
               field = (String)var14.next();
               f = (Field)classFields.get(field);
               if (f == null) {
                  throw new JsonIoException("Unable to locate field: " + field + " on class: " + c.getName() + ". Make sure the fields in the FIELD_SPECIFIERS map existing on the associated class.");
               }

               newList.add(f);
            }

            copy.put(c, newList);
         }

         this.args.put("FIELD_SPECIFIERS", copy);
      } else {
         this.args.put("FIELD_SPECIFIERS", new HashMap());
      }

      if (((Map)optionalArgs).containsKey("FIELD_NAME_BLACK_LIST")) {
         blackList = (Map)this.args.get("FIELD_NAME_BLACK_LIST");
         copy = new HashMap();
         var8 = blackList.entrySet().iterator();

         while(var8.hasNext()) {
            entry = (Map.Entry)var8.next();
            c = (Class)entry.getKey();
            fields = (List)entry.getValue();
            newList = new ArrayList(fields.size());
            classFields = MetaUtils.getDeepDeclaredFields(c);
            var14 = fields.iterator();

            while(var14.hasNext()) {
               field = (String)var14.next();
               f = (Field)classFields.get(field);
               if (f == null) {
                  throw new JsonIoException("Unable to locate field: " + field + " on class: " + c.getName() + ". Make sure the fields in the FIELD_NAME_BLACK_LIST map existing on the associated class.");
               }

               newList.add(f);
            }

            copy.put(c, newList);
         }

         this.args.put("FIELD_BLACK_LIST", copy);
      } else {
         this.args.put("FIELD_BLACK_LIST", new HashMap());
      }

      try {
         this.out = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
      } catch (UnsupportedEncodingException var16) {
         throw new JsonIoException("UTF-8 not supported on your JVM.  Unable to convert object to JSON.", var16);
      }
   }

   ClassLoader getClassLoader() {
      return (ClassLoader)this.args.get("CLASSLOADER");
   }

   static boolean isTrue(Object setting) {
      if (setting instanceof Boolean) {
         return Boolean.TRUE.equals(setting);
      } else if (setting instanceof String) {
         return "true".equalsIgnoreCase((String)setting);
      } else if (setting instanceof Number) {
         return ((Number)setting).intValue() != 0;
      } else {
         return false;
      }
   }

   public void tabIn() throws IOException {
      this.tab(this.out, 1);
   }

   public void newLine() throws IOException {
      this.tab(this.out, 0);
   }

   public void tabOut() throws IOException {
      this.tab(this.out, -1);
   }

   private void tab(Writer output, int delta) throws IOException {
      if (this.isPrettyPrint) {
         output.write(NEW_LINE);
         this.depth += delta;

         for(int i = 0; i < this.depth; ++i) {
            output.write("  ");
         }

      }
   }

   public boolean writeIfMatching(Object o, boolean showType, Writer output) {
      if (this.neverShowType) {
         showType = false;
      }

      Class c = o.getClass();
      if (this.notCustom.contains(c)) {
         return false;
      } else {
         try {
            return this.writeCustom(c, o, showType, output);
         } catch (IOException var6) {
            throw new JsonIoException("Unable to write custom formatted object:", var6);
         }
      }
   }

   public boolean writeArrayElementIfMatching(Class arrayComponentClass, Object o, boolean showType, Writer output) {
      if (o.getClass().isAssignableFrom(arrayComponentClass) && !this.notCustom.contains(o.getClass())) {
         try {
            return this.writeCustom(arrayComponentClass, o, showType, output);
         } catch (IOException var6) {
            throw new JsonIoException("Unable to write custom formatted object as array element:", var6);
         }
      } else {
         return false;
      }
   }

   protected boolean writeCustom(Class arrayComponentClass, Object o, boolean showType, Writer output) throws IOException {
      if (this.neverShowType) {
         showType = false;
      }

      JsonClassWriterBase closestWriter = this.getCustomWriter(arrayComponentClass);
      if (closestWriter == null) {
         return false;
      } else if (this.writeOptionalReference(o)) {
         return true;
      } else {
         boolean referenced = this.objsReferenced.containsKey(o);
         if (closestWriter instanceof JsonClassWriter) {
            JsonClassWriter writer = (JsonClassWriter)closestWriter;
            if (writer.hasPrimitiveForm() && (!referenced && !showType || closestWriter instanceof Writers.JsonStringWriter)) {
               if (writer instanceof Writers.DateWriter) {
                  ((Writers.DateWriter)writer).writePrimitiveForm(o, output, this.args);
               } else {
                  writer.writePrimitiveForm(o, output);
               }

               return true;
            }
         }

         output.write(123);
         this.tabIn();
         if (referenced) {
            this.writeId(this.getId(o));
            if (showType) {
               output.write(44);
               this.newLine();
            }
         }

         if (showType) {
            this.writeType(o, output);
         }

         if (referenced || showType) {
            output.write(44);
            this.newLine();
         }

         if (closestWriter instanceof JsonClassWriterEx) {
            ((JsonClassWriterEx)closestWriter).write(o, showType || referenced, output, this.args);
         } else {
            ((JsonClassWriter)closestWriter).write(o, showType || referenced, output);
         }

         this.tabOut();
         output.write(125);
         return true;
      }
   }

   private JsonClassWriterBase getCustomWriter(Class c) {
      JsonClassWriterBase writer = (JsonClassWriterBase)this.writerCache.get(c);
      if (writer == null) {
         writer = this.forceGetCustomWriter(c);
         this.writerCache.put(c, writer);
      }

      return writer == nullWriter ? null : writer;
   }

   private JsonClassWriterBase forceGetCustomWriter(Class c) {
      JsonClassWriterBase closestWriter = nullWriter;
      int minDistance = Integer.MAX_VALUE;
      Iterator var5 = this.writers.entrySet().iterator();

      while(var5.hasNext()) {
         Map.Entry entry = (Map.Entry)var5.next();
         Class clz = (Class)entry.getKey();
         if (clz == c) {
            return (JsonClassWriterBase)entry.getValue();
         }

         int distance = MetaUtils.getDistance(clz, c);
         if (distance < minDistance) {
            minDistance = distance;
            closestWriter = (JsonClassWriterBase)entry.getValue();
         }
      }

      return (JsonClassWriterBase)closestWriter;
   }

   public void addWriter(Class c, JsonClassWriterBase writer) {
      this.writers.put(c, writer);
   }

   public void addNotCustomWriter(Class c) {
      this.notCustom.add(c);
   }

   public void write(Object obj) {
      this.traceReferences(obj);
      this.objVisited.clear();

      try {
         this.writeImpl(obj, true);
      } catch (Exception var3) {
         throw new JsonIoException("Error writing object to JSON:", var3);
      }

      this.flush();
      this.objVisited.clear();
      this.objsReferenced.clear();
   }

   protected void traceReferences(Object root) {
      if (root != null) {
         Map fieldSpecifiers = (Map)this.args.get("FIELD_SPECIFIERS");
         Deque stack = new ArrayDeque();
         stack.addFirst(root);
         Map visited = this.objVisited;
         Map referenced = this.objsReferenced;

         while(true) {
            while(!stack.isEmpty()) {
               Object obj = stack.removeFirst();
               if (!MetaUtils.isLogicalPrimitive(obj.getClass())) {
                  Long id = (Long)visited.get(obj);
                  if (id != null) {
                     if (id == ZERO) {
                        id = Long.valueOf((long)(this.identity++));
                        visited.put(obj, id);
                        referenced.put(obj, id);
                     }
                     continue;
                  }

                  visited.put(obj, ZERO);
               }

               Class clazz = obj.getClass();
               if (clazz.isArray()) {
                  if (!MetaUtils.isLogicalPrimitive(clazz.getComponentType())) {
                     int len = Array.getLength(obj);

                     for(int i = 0; i < len; ++i) {
                        Object o = Array.get(obj, i);
                        if (o != null) {
                           stack.addFirst(o);
                        }
                     }
                  }
               } else if (Map.class.isAssignableFrom(clazz)) {
                  try {
                     Map map = (Map)obj;
                     Iterator var10 = map.entrySet().iterator();

                     while(var10.hasNext()) {
                        Object item = var10.next();
                        Map.Entry entry = (Map.Entry)item;
                        if (entry.getValue() != null) {
                           stack.addFirst(entry.getValue());
                        }

                        if (entry.getKey() != null) {
                           stack.addFirst(entry.getKey());
                        }
                     }
                  } catch (UnsupportedOperationException var12) {
                  }
               } else if (Collection.class.isAssignableFrom(clazz)) {
                  Iterator var9 = ((Collection)obj).iterator();

                  while(var9.hasNext()) {
                     Object item = var9.next();
                     if (item != null) {
                        stack.addFirst(item);
                     }
                  }
               } else if (!MetaUtils.isLogicalPrimitive(obj.getClass())) {
                  this.traceFields(stack, obj, fieldSpecifiers);
               }
            }

            return;
         }
      }
   }

   protected void traceFields(Deque stack, Object obj, Map fieldSpecifiers) {
      Collection fields = getFieldsUsingSpecifier(obj.getClass(), fieldSpecifiers);
      Collection fieldsBySpec = fields;
      if (fields == null) {
         fields = MetaUtils.getDeepDeclaredFields(obj.getClass()).values();
      }

      Iterator var7 = ((Collection)fields).iterator();

      while(true) {
         Field field;
         do {
            if (!var7.hasNext()) {
               return;
            }

            field = (Field)var7.next();
         } while((field.getModifiers() & 128) != 0 && (fieldsBySpec == null || !((Collection)fieldsBySpec).contains(field)));

         try {
            Object o = field.get(obj);
            if (o != null && !MetaUtils.isLogicalPrimitive(o.getClass())) {
               stack.addFirst(o);
            }
         } catch (Exception var9) {
         }
      }
   }

   private static List getFieldsUsingSpecifier(Class classBeingWritten, Map fieldSpecifiers) {
      Iterator i = fieldSpecifiers.entrySet().iterator();
      int minDistance = Integer.MAX_VALUE;
      List fields = null;

      while(i.hasNext()) {
         Map.Entry entry = (Map.Entry)i.next();
         Class c = (Class)entry.getKey();
         if (c == classBeingWritten) {
            return (List)entry.getValue();
         }

         int distance = MetaUtils.getDistance(c, classBeingWritten);
         if (distance < minDistance) {
            minDistance = distance;
            fields = (List)entry.getValue();
         }
      }

      return fields;
   }

   private boolean writeOptionalReference(Object obj) throws IOException {
      if (obj == null) {
         return false;
      } else if (MetaUtils.isLogicalPrimitive(obj.getClass())) {
         return false;
      } else {
         Writer output = this.out;
         if (this.objVisited.containsKey(obj)) {
            String id = this.getId(obj);
            if (id == null) {
               return false;
            } else {
               output.write(this.shortMetaKeys ? "{\"@r\":" : "{\"@ref\":");
               output.write(id);
               output.write(125);
               return true;
            }
         } else {
            this.objVisited.put(obj, (Object)null);
            return false;
         }
      }
   }

   public void writeImpl(Object obj, boolean showType) throws IOException {
      this.writeImpl(obj, showType, true, true);
   }

   public void writeImpl(Object obj, boolean showType, boolean allowRef, boolean allowCustom) throws IOException {
      if (this.neverShowType) {
         showType = false;
      }

      if (obj == null) {
         this.out.write("null");
      } else if (!allowCustom || !this.writeIfMatching(obj, showType, this.out)) {
         if (!allowRef || !this.writeOptionalReference(obj)) {
            if (obj.getClass().isArray()) {
               this.writeArray(obj, showType);
            } else if (obj instanceof Collection) {
               this.writeCollection((Collection)obj, showType);
            } else if (obj instanceof JsonObject) {
               JsonObject jObj = (JsonObject)obj;
               if (jObj.isArray()) {
                  this.writeJsonObjectArray(jObj, showType);
               } else if (jObj.isCollection()) {
                  this.writeJsonObjectCollection(jObj, showType);
               } else if (jObj.isMap()) {
                  if (!this.writeJsonObjectMapWithStringKeys(jObj, showType)) {
                     this.writeJsonObjectMap(jObj, showType);
                  }
               } else {
                  this.writeJsonObjectObject(jObj, showType);
               }
            } else if (obj instanceof Map) {
               if (!this.writeMapWithStringKeys((Map)obj, showType)) {
                  this.writeMap((Map)obj, showType);
               }
            } else {
               this.writeObject(obj, showType, false);
            }

         }
      }
   }

   private void writeId(String id) throws IOException {
      this.out.write(this.shortMetaKeys ? "\"@i\":" : "\"@id\":");
      this.out.write(id == null ? "0" : id);
   }

   private void writeType(Object obj, Writer output) throws IOException {
      if (!this.neverShowType) {
         output.write(this.shortMetaKeys ? "\"@t\":\"" : "\"@type\":\"");
         Class c = obj.getClass();
         String typeName = c.getName();
         String shortName = this.getSubstituteTypeNameIfExists(typeName);
         if (shortName != null) {
            output.write(shortName);
            output.write(34);
         } else {
            String s = c.getName();
            if (s.equals("java.lang.Boolean")) {
               output.write("boolean");
            } else if (s.equals("java.lang.Byte")) {
               output.write("byte");
            } else if (s.equals("java.lang.Character")) {
               output.write("char");
            } else if (s.equals("java.lang.Class")) {
               output.write("class");
            } else if (s.equals("java.lang.Double")) {
               output.write("double");
            } else if (s.equals("java.lang.Float")) {
               output.write("float");
            } else if (s.equals("java.lang.Integer")) {
               output.write("int");
            } else if (s.equals("java.lang.Long")) {
               output.write("long");
            } else if (s.equals("java.lang.Short")) {
               output.write("short");
            } else if (s.equals("java.lang.String")) {
               output.write("string");
            } else if (s.equals("java.util.Date")) {
               output.write("date");
            } else {
               output.write(c.getName());
            }

            output.write(34);
         }
      }
   }

   private void writePrimitive(Object obj, boolean showType) throws IOException {
      if (this.neverShowType) {
         showType = false;
      }

      if (obj instanceof Character) {
         writeJsonUtf8String(String.valueOf(obj), this.out);
      } else if (obj instanceof Long && this.writeLongsAsStrings) {
         if (showType) {
            this.out.write(this.shortMetaKeys ? "{\"@t\":\"" : "{\"@type\":\"");
            this.out.write(this.getSubstituteTypeName("long"));
            this.out.write("\",\"value\":\"");
            this.out.write(obj.toString());
            this.out.write("\"}");
         } else {
            this.out.write(34);
            this.out.write(obj.toString());
            this.out.write(34);
         }
      } else if (isAllowNanAndInfinity() || !(obj instanceof Double) || !Double.isNaN((Double)obj) && !Double.isInfinite((Double)obj)) {
         if (isAllowNanAndInfinity() || !(obj instanceof Float) || !Float.isNaN((Float)obj) && !Float.isInfinite((Float)obj)) {
            this.out.write(obj.toString());
         } else {
            this.out.write("null");
         }
      } else {
         this.out.write("null");
      }

   }

   private void writeArray(Object array, boolean showType) throws IOException {
      if (this.neverShowType) {
         showType = false;
      }

      Class arrayType = array.getClass();
      int len = Array.getLength(array);
      boolean referenced = this.objsReferenced.containsKey(array);
      boolean typeWritten = showType && !arrayType.equals(Object[].class);
      Writer output = this.out;
      if (typeWritten || referenced) {
         output.write(123);
         this.tabIn();
      }

      if (referenced) {
         this.writeId(this.getId(array));
         output.write(44);
         this.newLine();
      }

      if (typeWritten) {
         this.writeType(array, output);
         output.write(44);
         this.newLine();
      }

      if (len == 0) {
         if (!typeWritten && !referenced) {
            output.write("[]");
         } else {
            output.write(this.shortMetaKeys ? "\"@e\":[]" : "\"@items\":[]");
            this.tabOut();
            output.write(125);
         }

      } else {
         if (!typeWritten && !referenced) {
            output.write(91);
         } else {
            output.write(this.shortMetaKeys ? "\"@e\":[" : "\"@items\":[");
         }

         this.tabIn();
         int lenMinus1 = len - 1;
         if (byte[].class == arrayType) {
            this.writeByteArray((byte[])array, lenMinus1);
         } else if (char[].class == arrayType) {
            writeJsonUtf8String(new String((char[])array), output);
         } else if (short[].class == arrayType) {
            this.writeShortArray((short[])array, lenMinus1);
         } else if (int[].class == arrayType) {
            this.writeIntArray((int[])array, lenMinus1);
         } else if (long[].class == arrayType) {
            this.writeLongArray((long[])array, lenMinus1);
         } else if (float[].class == arrayType) {
            this.writeFloatArray((float[])array, lenMinus1);
         } else if (double[].class == arrayType) {
            this.writeDoubleArray((double[])array, lenMinus1);
         } else if (boolean[].class == arrayType) {
            this.writeBooleanArray((boolean[])array, lenMinus1);
         } else {
            Class componentClass = array.getClass().getComponentType();
            boolean isPrimitiveArray = MetaUtils.isPrimitive(componentClass);

            for(int i = 0; i < len; ++i) {
               Object value = Array.get(array, i);
               if (value == null) {
                  output.write("null");
               } else if (!this.writeArrayElementIfMatching(componentClass, value, false, output)) {
                  if (!isPrimitiveArray && !(value instanceof Boolean) && !(value instanceof Long) && !(value instanceof Double)) {
                     if (this.neverShowType && MetaUtils.isPrimitive(value.getClass())) {
                        this.writePrimitive(value, false);
                     } else {
                        boolean forceType = value.getClass() != componentClass;
                        this.writeImpl(value, forceType || this.alwaysShowType);
                     }
                  } else {
                     this.writePrimitive(value, value.getClass() != componentClass);
                  }
               }

               if (i != lenMinus1) {
                  output.write(44);
                  this.newLine();
               }
            }
         }

         this.tabOut();
         output.write(93);
         if (typeWritten || referenced) {
            this.tabOut();
            output.write(125);
         }

      }
   }

   private void writeBooleanArray(boolean[] booleans, int lenMinus1) throws IOException {
      Writer output = this.out;

      for(int i = 0; i < lenMinus1; ++i) {
         output.write(booleans[i] ? "true," : "false,");
      }

      output.write(Boolean.toString(booleans[lenMinus1]));
   }

   private void writeDoubleArray(double[] doubles, int lenMinus1) throws IOException {
      Writer output = this.out;

      for(int i = 0; i < lenMinus1; ++i) {
         output.write(this.doubleToString(doubles[i]));
         output.write(44);
      }

      output.write(this.doubleToString(doubles[lenMinus1]));
   }

   private void writeFloatArray(float[] floats, int lenMinus1) throws IOException {
      Writer output = this.out;

      for(int i = 0; i < lenMinus1; ++i) {
         output.write(this.floatToString(floats[i]));
         output.write(44);
      }

      output.write(this.floatToString(floats[lenMinus1]));
   }

   private String doubleToString(double d) {
      if (isAllowNanAndInfinity()) {
         return Double.toString(d);
      } else {
         return !Double.isNaN(d) && !Double.isInfinite(d) ? Double.toString(d) : "null";
      }
   }

   private String floatToString(float d) {
      if (isAllowNanAndInfinity()) {
         return Float.toString(d);
      } else {
         return !Float.isNaN(d) && !Float.isInfinite(d) ? Float.toString(d) : "null";
      }
   }

   private void writeLongArray(long[] longs, int lenMinus1) throws IOException {
      Writer output = this.out;
      int i;
      if (this.writeLongsAsStrings) {
         for(i = 0; i < lenMinus1; ++i) {
            output.write(34);
            output.write(Long.toString(longs[i]));
            output.write(34);
            output.write(44);
         }

         output.write(34);
         output.write(Long.toString(longs[lenMinus1]));
         output.write(34);
      } else {
         for(i = 0; i < lenMinus1; ++i) {
            output.write(Long.toString(longs[i]));
            output.write(44);
         }

         output.write(Long.toString(longs[lenMinus1]));
      }

   }

   private void writeIntArray(int[] ints, int lenMinus1) throws IOException {
      Writer output = this.out;

      for(int i = 0; i < lenMinus1; ++i) {
         output.write(Integer.toString(ints[i]));
         output.write(44);
      }

      output.write(Integer.toString(ints[lenMinus1]));
   }

   private void writeShortArray(short[] shorts, int lenMinus1) throws IOException {
      Writer output = this.out;

      for(int i = 0; i < lenMinus1; ++i) {
         output.write(Integer.toString(shorts[i]));
         output.write(44);
      }

      output.write(Integer.toString(shorts[lenMinus1]));
   }

   private void writeByteArray(byte[] bytes, int lenMinus1) throws IOException {
      Writer output = this.out;
      Object[] byteStrs = byteStrings;

      for(int i = 0; i < lenMinus1; ++i) {
         output.write((char[])byteStrs[bytes[i] + 128]);
         output.write(44);
      }

      output.write((char[])byteStrs[bytes[lenMinus1] + 128]);
   }

   private void writeCollection(Collection col, boolean showType) throws IOException {
      if (this.neverShowType) {
         showType = false;
      }

      Writer output = this.out;
      boolean referenced = this.objsReferenced.containsKey(col);
      boolean isEmpty = col.isEmpty();
      if (!referenced && !showType) {
         if (isEmpty) {
            output.write(91);
         }
      } else {
         output.write(123);
         this.tabIn();
      }

      this.writeIdAndTypeIfNeeded(col, showType, referenced);
      if (isEmpty) {
         if (!referenced && !showType) {
            output.write(93);
         } else {
            this.tabOut();
            output.write(125);
         }

      } else {
         this.beginCollection(showType, referenced);
         Iterator i = col.iterator();
         this.writeElements(output, i);
         this.tabOut();
         output.write(93);
         if (showType || referenced) {
            this.tabOut();
            output.write("}");
         }

      }
   }

   private void writeElements(Writer output, Iterator i) throws IOException {
      while(i.hasNext()) {
         this.writeCollectionElement(i.next());
         if (i.hasNext()) {
            output.write(44);
            this.newLine();
         }
      }

   }

   private void writeIdAndTypeIfNeeded(Object col, boolean showType, boolean referenced) throws IOException {
      if (this.neverShowType) {
         showType = false;
      }

      if (referenced) {
         this.writeId(this.getId(col));
      }

      if (showType) {
         if (referenced) {
            this.out.write(44);
            this.newLine();
         }

         this.writeType(col, this.out);
      }

   }

   private void beginCollection(boolean showType, boolean referenced) throws IOException {
      if (!showType && !referenced) {
         this.out.write(91);
      } else {
         this.out.write(44);
         this.newLine();
         this.out.write(this.shortMetaKeys ? "\"@e\":[" : "\"@items\":[");
      }

      this.tabIn();
   }

   private void writeJsonObjectArray(JsonObject jObj, boolean showType) throws IOException {
      if (this.neverShowType) {
         showType = false;
      }

      int len = jObj.getLength();
      String type = jObj.type;
      Class arrayClass;
      if (type != null && !Object[].class.getName().equals(type)) {
         arrayClass = MetaUtils.classForName(type, this.getClassLoader());
      } else {
         arrayClass = Object[].class;
      }

      Writer output = this.out;
      boolean isObjectArray = Object[].class == arrayClass;
      Class componentClass = arrayClass.getComponentType();
      boolean referenced = this.objsReferenced.containsKey(jObj) && jObj.hasId();
      boolean typeWritten = showType && !isObjectArray;
      if (typeWritten || referenced) {
         output.write(123);
         this.tabIn();
      }

      if (referenced) {
         this.writeId(Long.toString(jObj.id));
         output.write(44);
         this.newLine();
      }

      if (typeWritten) {
         output.write(this.shortMetaKeys ? "\"@t\":\"" : "\"@type\":\"");
         output.write(this.getSubstituteTypeName(arrayClass.getName()));
         output.write("\",");
         this.newLine();
      }

      if (len == 0) {
         if (!typeWritten && !referenced) {
            output.write("[]");
         } else {
            output.write(this.shortMetaKeys ? "\"@e\":[]" : "\"@items\":[]");
            this.tabOut();
            output.write("}");
         }

      } else {
         if (!typeWritten && !referenced) {
            output.write(91);
         } else {
            output.write(this.shortMetaKeys ? "\"@e\":[" : "\"@items\":[");
         }

         this.tabIn();
         Object[] items = (Object[])jObj.get("@items");
         int lenMinus1 = len - 1;

         for(int i = 0; i < len; ++i) {
            Object value = items[i];
            if (value == null) {
               output.write("null");
            } else if (Character.class != componentClass && Character.TYPE != componentClass) {
               if (!(value instanceof Boolean) && !(value instanceof Long) && !(value instanceof Double)) {
                  if (this.neverShowType && MetaUtils.isPrimitive(value.getClass())) {
                     this.writePrimitive(value, false);
                  } else if (value instanceof String) {
                     writeJsonUtf8String((String)value, output);
                  } else if (!this.writeArrayElementIfMatching(componentClass, value, false, output)) {
                     boolean forceType = value.getClass() != componentClass;
                     this.writeImpl(value, forceType || this.alwaysShowType);
                  }
               } else {
                  this.writePrimitive(value, value.getClass() != componentClass);
               }
            } else {
               writeJsonUtf8String((String)value, output);
            }

            if (i != lenMinus1) {
               output.write(44);
               this.newLine();
            }
         }

         this.tabOut();
         output.write(93);
         if (typeWritten || referenced) {
            this.tabOut();
            output.write(125);
         }

      }
   }

   private void writeJsonObjectCollection(JsonObject jObj, boolean showType) throws IOException {
      if (this.neverShowType) {
         showType = false;
      }

      String type = jObj.type;
      Class colClass = MetaUtils.classForName(type, this.getClassLoader());
      boolean referenced = this.objsReferenced.containsKey(jObj) && jObj.hasId();
      Writer output = this.out;
      int len = jObj.getLength();
      if (referenced || showType || len == 0) {
         output.write(123);
         this.tabIn();
      }

      if (referenced) {
         this.writeId(String.valueOf(jObj.id));
      }

      if (showType) {
         if (referenced) {
            output.write(44);
            this.newLine();
         }

         output.write(this.shortMetaKeys ? "\"@t\":\"" : "\"@type\":\"");
         output.write(this.getSubstituteTypeName(colClass.getName()));
         output.write(34);
      }

      if (len == 0) {
         this.tabOut();
         output.write(125);
      } else {
         this.beginCollection(showType, referenced);
         Object[] items = (Object[])jObj.get("@items");
         int itemsLen = items.length;
         int itemsLenMinus1 = itemsLen - 1;

         for(int i = 0; i < itemsLen; ++i) {
            this.writeCollectionElement(items[i]);
            if (i != itemsLenMinus1) {
               output.write(44);
               this.newLine();
            }
         }

         this.tabOut();
         output.write("]");
         if (showType || referenced) {
            this.tabOut();
            output.write(125);
         }

      }
   }

   private void writeJsonObjectMap(JsonObject jObj, boolean showType) throws IOException {
      if (this.neverShowType) {
         showType = false;
      }

      boolean referenced = this.objsReferenced.containsKey(jObj) && jObj.hasId();
      Writer output = this.out;
      output.write(123);
      this.tabIn();
      if (referenced) {
         this.writeId(String.valueOf(jObj.getId()));
      }

      if (showType) {
         if (referenced) {
            output.write(44);
            this.newLine();
         }

         String type = jObj.getType();
         if (type != null) {
            Class mapClass = MetaUtils.classForName(type, this.getClassLoader());
            output.write(this.shortMetaKeys ? "\"@t\":\"" : "\"@type\":\"");
            output.write(this.getSubstituteTypeName(mapClass.getName()));
            output.write(34);
         } else {
            showType = false;
         }
      }

      if (jObj.isEmpty()) {
         this.tabOut();
         output.write(125);
      } else {
         if (showType) {
            output.write(44);
            this.newLine();
         }

         output.write(this.shortMetaKeys ? "\"@k\":[" : "\"@keys\":[");
         this.tabIn();
         Iterator i = jObj.keySet().iterator();
         this.writeElements(output, i);
         this.tabOut();
         output.write("],");
         this.newLine();
         output.write(this.shortMetaKeys ? "\"@e\":[" : "\"@items\":[");
         this.tabIn();
         i = jObj.values().iterator();
         this.writeElements(output, i);
         this.tabOut();
         output.write(93);
         this.tabOut();
         output.write(125);
      }
   }

   private boolean writeJsonObjectMapWithStringKeys(JsonObject jObj, boolean showType) throws IOException {
      if (this.neverShowType) {
         showType = false;
      }

      if (!this.forceMapFormatWithKeyArrays && ensureJsonPrimitiveKeys(jObj)) {
         boolean referenced = this.objsReferenced.containsKey(jObj) && jObj.hasId();
         Writer output = this.out;
         output.write(123);
         this.tabIn();
         if (referenced) {
            this.writeId(String.valueOf(jObj.getId()));
         }

         if (showType) {
            if (referenced) {
               output.write(44);
               this.newLine();
            }

            String type = jObj.getType();
            if (type != null) {
               Class mapClass = MetaUtils.classForName(type, this.getClassLoader());
               output.write(this.shortMetaKeys ? "\"@t\":\"" : "\"@type\":\"");
               output.write(this.getSubstituteTypeName(mapClass.getName()));
               output.write(34);
            } else {
               showType = false;
            }
         }

         if (jObj.isEmpty()) {
            this.tabOut();
            output.write(125);
            return true;
         } else {
            if (showType) {
               output.write(44);
               this.newLine();
            }

            return this.writeMapBody(jObj.entrySet().iterator());
         }
      } else {
         return false;
      }
   }

   private void writeJsonObjectObject(JsonObject jObj, boolean showType) throws IOException {
      if (this.neverShowType) {
         showType = false;
      }

      Writer output = this.out;
      boolean referenced = this.objsReferenced.containsKey(jObj) && jObj.hasId();
      showType = showType && jObj.type != null;
      Class type = null;
      output.write(123);
      this.tabIn();
      if (referenced) {
         this.writeId(String.valueOf(jObj.id));
      }

      if (showType) {
         if (referenced) {
            output.write(44);
            this.newLine();
         }

         output.write(this.shortMetaKeys ? "\"@t\":\"" : "\"@type\":\"");
         output.write(this.getSubstituteTypeName(jObj.type));
         output.write(34);

         try {
            type = MetaUtils.classForName(jObj.type, this.getClassLoader());
         } catch (Exception var11) {
            type = null;
         }
      }

      if (jObj.isEmpty()) {
         this.tabOut();
         output.write(125);
      } else {
         if (showType || referenced) {
            output.write(44);
            this.newLine();
         }

         Iterator i = jObj.entrySet().iterator();
         boolean first = true;

         while(true) {
            while(true) {
               Map.Entry entry;
               do {
                  if (!i.hasNext()) {
                     this.tabOut();
                     output.write(125);
                     return;
                  }

                  entry = (Map.Entry)i.next();
               } while(this.skipNullFields && entry.getValue() == null);

               if (!first) {
                  output.write(44);
                  this.newLine();
               }

               first = false;
               String fieldName = (String)entry.getKey();
               output.write(34);
               output.write(fieldName);
               output.write("\":");
               Object value = entry.getValue();
               if (value == null) {
                  output.write("null");
               } else if (this.neverShowType && MetaUtils.isPrimitive(value.getClass())) {
                  this.writePrimitive(value, false);
               } else if (!(value instanceof BigDecimal) && !(value instanceof BigInteger)) {
                  if (!(value instanceof Number) && !(value instanceof Boolean)) {
                     if (value instanceof String) {
                        writeJsonUtf8String((String)value, output);
                     } else if (value instanceof Character) {
                        writeJsonUtf8String(String.valueOf(value), output);
                     } else {
                        this.writeImpl(value, !doesValueTypeMatchFieldType(type, fieldName, value));
                     }
                  } else {
                     output.write(value.toString());
                  }
               } else {
                  this.writeImpl(value, !doesValueTypeMatchFieldType(type, fieldName, value));
               }
            }
         }
      }
   }

   private static boolean doesValueTypeMatchFieldType(Class type, String fieldName, Object value) {
      if (type != null) {
         Map classFields = MetaUtils.getDeepDeclaredFields(type);
         Field field = (Field)classFields.get(fieldName);
         return field != null && value.getClass() == field.getType();
      } else {
         return false;
      }
   }

   private void writeMap(Map map, boolean showType) throws IOException {
      if (this.neverShowType) {
         showType = false;
      }

      Writer output = this.out;
      boolean referenced = this.objsReferenced.containsKey(map);
      output.write(123);
      this.tabIn();
      if (referenced) {
         this.writeId(this.getId(map));
      }

      if (showType) {
         if (referenced) {
            output.write(44);
            this.newLine();
         }

         this.writeType(map, output);
      }

      if (map.isEmpty()) {
         this.tabOut();
         output.write(125);
      } else {
         if (showType || referenced) {
            output.write(44);
            this.newLine();
         }

         output.write(this.shortMetaKeys ? "\"@k\":[" : "\"@keys\":[");
         this.tabIn();
         Iterator i = map.keySet().iterator();
         this.writeElements(output, i);
         this.tabOut();
         output.write("],");
         this.newLine();
         output.write(this.shortMetaKeys ? "\"@e\":[" : "\"@items\":[");
         this.tabIn();
         i = map.values().iterator();
         this.writeElements(output, i);
         this.tabOut();
         output.write(93);
         this.tabOut();
         output.write(125);
      }
   }

   private boolean writeMapWithStringKeys(Map map, boolean showType) throws IOException {
      if (this.neverShowType) {
         showType = false;
      }

      if (!this.forceMapFormatWithKeyArrays && ensureJsonPrimitiveKeys(map)) {
         boolean referenced = this.objsReferenced.containsKey(map);
         this.out.write(123);
         this.tabIn();
         this.writeIdAndTypeIfNeeded(map, showType, referenced);
         if (map.isEmpty()) {
            this.tabOut();
            this.out.write(125);
            return true;
         } else {
            if (showType || referenced) {
               this.out.write(44);
               this.newLine();
            }

            return this.writeMapBody(map.entrySet().iterator());
         }
      } else {
         return false;
      }
   }

   private boolean writeMapBody(Iterator i) throws IOException {
      Writer output = this.out;

      while(i.hasNext()) {
         Map.Entry att2value = (Map.Entry)i.next();
         writeJsonUtf8String((String)att2value.getKey(), output);
         output.write(":");
         this.writeCollectionElement(att2value.getValue());
         if (i.hasNext()) {
            output.write(44);
            this.newLine();
         }
      }

      this.tabOut();
      output.write(125);
      return true;
   }

   public static boolean ensureJsonPrimitiveKeys(Map map) {
      Iterator var2 = map.keySet().iterator();

      while(var2.hasNext()) {
         Object o = var2.next();
         if (!(o instanceof String)) {
            return false;
         }
      }

      return true;
   }

   private void writeCollectionElement(Object o) throws IOException {
      if (o == null) {
         this.out.write("null");
      } else if (!(o instanceof Boolean) && !(o instanceof Double)) {
         if (o instanceof Long) {
            this.writePrimitive(o, this.writeLongsAsStrings);
         } else if (o instanceof String) {
            writeJsonUtf8String((String)o, this.out);
         } else if (this.neverShowType && MetaUtils.isPrimitive(o.getClass())) {
            this.writePrimitive(o, false);
         } else {
            this.writeImpl(o, true);
         }
      } else {
         this.writePrimitive(o, false);
      }

   }

   public void writeObject(Object obj, boolean showType, boolean bodyOnly) throws IOException {
      if (this.neverShowType) {
         showType = false;
      }

      boolean referenced = this.objsReferenced.containsKey(obj);
      if (!bodyOnly) {
         this.out.write(123);
         this.tabIn();
         if (referenced) {
            this.writeId(this.getId(obj));
         }

         if (referenced && showType) {
            this.out.write(44);
            this.newLine();
         }

         if (showType) {
            this.writeType(obj, this.out);
         }
      }

      boolean first = !showType;
      if (referenced && !showType) {
         first = false;
      }

      Map fieldSpecifiers = (Map)this.args.get("FIELD_SPECIFIERS");
      List fieldBlackListForClass = getFieldsUsingSpecifier(obj.getClass(), (Map)this.args.get("FIELD_BLACK_LIST"));
      List externallySpecifiedFields = getFieldsUsingSpecifier(obj.getClass(), fieldSpecifiers);
      if (externallySpecifiedFields != null) {
         Iterator var10 = externallySpecifiedFields.iterator();

         label68:
         while(true) {
            Field field;
            do {
               if (!var10.hasNext()) {
                  break label68;
               }

               field = (Field)var10.next();
            } while(fieldBlackListForClass != null && fieldBlackListForClass.contains(field));

            first = this.writeField(obj, first, field.getName(), field, true);
         }
      } else {
         Map classFields = MetaUtils.getDeepDeclaredFields(obj.getClass());
         Iterator var11 = classFields.entrySet().iterator();

         label57:
         while(true) {
            String fieldName;
            Field field;
            do {
               if (!var11.hasNext()) {
                  break label57;
               }

               Map.Entry entry = (Map.Entry)var11.next();
               fieldName = (String)entry.getKey();
               field = (Field)entry.getValue();
            } while(fieldBlackListForClass != null && fieldBlackListForClass.contains(field));

            first = this.writeField(obj, first, fieldName, field, false);
         }
      }

      if (!bodyOnly) {
         this.tabOut();
         this.out.write(125);
      }

   }

   private boolean writeField(Object obj, boolean first, String fieldName, Field field, boolean allowTransient) throws IOException {
      if (!allowTransient && (field.getModifiers() & 128) != 0) {
         return first;
      } else {
         int modifiers = field.getModifiers();
         if (Enum.class.isAssignableFrom(field.getDeclaringClass()) && !"name".equals(field.getName())) {
            if (!Modifier.isPublic(modifiers) && this.isEnumPublicOnly) {
               return first;
            }

            if ("ordinal".equals(field.getName()) || "internal".equals(field.getName())) {
               return first;
            }
         }

         Object o;
         try {
            o = field.get(obj);
         } catch (Exception var10) {
            o = null;
         }

         if (this.skipNullFields && o == null) {
            return first;
         } else {
            if (!first) {
               this.out.write(44);
               this.newLine();
            }

            writeJsonUtf8String(fieldName, this.out);
            this.out.write(58);
            if (o == null) {
               this.out.write("null");
               return false;
            } else {
               Class type = field.getType();
               boolean forceType = o.getClass() != type;
               if (MetaUtils.isPrimitive(type) || this.neverShowType && MetaUtils.isPrimitive(o.getClass())) {
                  this.writePrimitive(o, false);
               } else {
                  this.writeImpl(o, forceType || this.alwaysShowType, true, true);
               }

               return false;
            }
         }
      }
   }

   public static void writeJsonUtf8String(String s, Writer output) throws IOException {
      output.write(34);
      int len = s.length();

      for(int i = 0; i < len; ++i) {
         char c = s.charAt(i);
         if (c < ' ') {
            switch (c) {
               case '\b':
                  output.write("\\b");
                  break;
               case '\t':
                  output.write("\\t");
                  break;
               case '\n':
                  output.write("\\n");
                  break;
               case '\u000b':
               default:
                  output.write(String.format("\\u%04X", Integer.valueOf(c)));
                  break;
               case '\f':
                  output.write("\\f");
                  break;
               case '\r':
                  output.write("\\r");
            }
         } else if (c != '\\' && c != '"') {
            output.write(c);
         } else {
            output.write(92);
            output.write(c);
         }
      }

      output.write(34);
   }

   public void flush() {
      try {
         if (this.out != null) {
            this.out.flush();
         }
      } catch (Exception var1) {
      }

   }

   public void close() {
      try {
         this.out.close();
      } catch (Exception var1) {
      }

      this.writerCache.clear();
      this.writers.clear();
   }

   private String getId(Object o) {
      if (o instanceof JsonObject) {
         long id = ((JsonObject)o).id;
         if (id != -1L) {
            return String.valueOf(id);
         }
      }

      Long id = (Long)this.objsReferenced.get(o);
      return id == null ? null : Long.toString(id);
   }

   public interface JsonClassWriter extends JsonClassWriterBase {
      void write(Object var1, boolean var2, Writer var3) throws IOException;

      boolean hasPrimitiveForm();

      void writePrimitiveForm(Object var1, Writer var2) throws IOException;
   }

   public interface JsonClassWriterBase {
   }

   public interface JsonClassWriterEx extends JsonClassWriterBase {
      void write(Object var1, boolean var2, Writer var3, Map var4) throws IOException;
   }

   static final class NullClass implements JsonClassWriterBase {
   }
}
