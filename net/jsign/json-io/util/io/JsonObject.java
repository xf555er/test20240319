package net.jsign.json-io.util.io;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class JsonObject extends LinkedHashMap {
   static Set primitives = new HashSet();
   static Set primitiveWrappers = new HashSet();
   Object target;
   boolean isMap = false;
   String type;
   long id = -1L;
   int line;
   int col;

   static {
      primitives.add("boolean");
      primitives.add("byte");
      primitives.add("char");
      primitives.add("double");
      primitives.add("float");
      primitives.add("int");
      primitives.add("long");
      primitives.add("short");
      primitiveWrappers.add("java.lang.Boolean");
      primitiveWrappers.add("java.lang.Byte");
      primitiveWrappers.add("java.lang.Character");
      primitiveWrappers.add("java.lang.Double");
      primitiveWrappers.add("java.lang.Float");
      primitiveWrappers.add("java.lang.Integer");
      primitiveWrappers.add("java.lang.Long");
      primitiveWrappers.add("java.lang.Short");
   }

   public long getId() {
      return this.id;
   }

   public boolean hasId() {
      return this.id != -1L;
   }

   public void setType(String type) {
      this.type = type;
   }

   public String getType() {
      return this.type;
   }

   public Object getTarget() {
      return this.target;
   }

   public void setTarget(Object target) {
      this.target = target;
   }

   public Class getTargetClass() {
      return this.target.getClass();
   }

   public boolean isLogicalPrimitive() {
      return primitiveWrappers.contains(this.type) || primitives.contains(this.type) || "date".equals(this.type) || "java.math.BigInteger".equals(this.type) || "java.math.BigDecimal".equals(this.type);
   }

   public Object getPrimitiveValue() {
      if (!"boolean".equals(this.type) && !"double".equals(this.type) && !"long".equals(this.type)) {
         Number s;
         if ("byte".equals(this.type)) {
            s = (Number)this.get("value");
            return s.byteValue();
         } else if ("char".equals(this.type)) {
            String c = (String)this.get("value");
            return c.charAt(0);
         } else if ("float".equals(this.type)) {
            s = (Number)this.get("value");
            return s.floatValue();
         } else if ("int".equals(this.type)) {
            s = (Number)this.get("value");
            return s.intValue();
         } else if ("short".equals(this.type)) {
            s = (Number)this.get("value");
            return s.shortValue();
         } else {
            Object value;
            if ("date".equals(this.type)) {
               value = this.get("value");
               if (value instanceof Long) {
                  return new Date((Long)value);
               } else if (value instanceof String) {
                  return Readers.DateReader.parseDate((String)value);
               } else {
                  throw new JsonIoException("Unknown date type: " + this.type);
               }
            } else if ("java.math.BigInteger".equals(this.type)) {
               value = this.get("value");
               return Readers.bigIntegerFrom(value);
            } else if ("java.math.BigDecimal".equals(this.type)) {
               value = this.get("value");
               return Readers.bigDecimalFrom(value);
            } else {
               throw new JsonIoException("Invalid primitive type, line " + this.line + ", col " + this.col);
            }
         }
      } else {
         return this.get("value");
      }
   }

   public boolean isReference() {
      return this.containsKey("@ref");
   }

   public Long getReferenceId() {
      return (Long)this.get("@ref");
   }

   public boolean isMap() {
      return this.isMap || this.target instanceof Map;
   }

   public boolean isCollection() {
      if (this.target instanceof Collection) {
         return true;
      } else if (this.containsKey("@items") && !this.containsKey("@keys")) {
         return this.type != null && !this.type.contains("[");
      } else {
         return false;
      }
   }

   public boolean isArray() {
      if (this.target == null) {
         if (this.type != null) {
            return this.type.contains("[");
         } else {
            return this.containsKey("@items") && !this.containsKey("@keys");
         }
      } else {
         return this.target.getClass().isArray();
      }
   }

   public Object[] getArray() {
      return (Object[])this.get("@items");
   }

   public int getLength() {
      Object[] items;
      if (this.isArray()) {
         if (this.target == null) {
            items = (Object[])this.get("@items");
            return items == null ? 0 : items.length;
         } else {
            return Array.getLength(this.target);
         }
      } else if (!this.isCollection() && !this.isMap()) {
         throw new JsonIoException("getLength() called on a non-collection, line " + this.line + ", col " + this.col);
      } else {
         items = (Object[])this.get("@items");
         return items == null ? 0 : items.length;
      }
   }

   public Class getComponentType() {
      return this.target.getClass().getComponentType();
   }

   void moveBytesToMate() {
      byte[] bytes = (byte[])this.target;
      Object[] items = this.getArray();
      int len = items.length;

      for(int i = 0; i < len; ++i) {
         bytes[i] = ((Number)items[i]).byteValue();
      }

   }

   void moveCharsToMate() {
      Object[] items = this.getArray();
      if (items == null) {
         this.target = null;
      } else if (items.length == 0) {
         this.target = new char[0];
      } else {
         if (items.length != 1) {
            throw new JsonIoException("char[] should only have one String in the [], found " + items.length + ", line " + this.line + ", col " + this.col);
         }

         String s = (String)items[0];
         this.target = s.toCharArray();
      }

   }

   public Object put(Object key, Object value) {
      if (key == null) {
         return super.put((Object)null, value);
      } else if (key.equals("@type")) {
         String oldType = this.type;
         this.type = (String)value;
         return oldType;
      } else if (key.equals("@id")) {
         Long oldId = this.id;
         this.id = (Long)value;
         return oldId;
      } else {
         if ("@items".equals(key) && this.containsKey("@keys") || "@keys".equals(key) && this.containsKey("@items")) {
            this.isMap = true;
         }

         return super.put(key, value);
      }
   }

   public void clear() {
      super.clear();
      this.type = null;
   }

   void clearArray() {
      this.remove("@items");
   }

   public int size() {
      if (this.containsKey("@items")) {
         Object value = this.get("@items");
         if (value instanceof Object[]) {
            return ((Object[])value).length;
         } else if (value == null) {
            return 0;
         } else {
            throw new JsonIoException("JsonObject with @items, but no array [] associated to it, line " + this.line + ", col " + this.col);
         }
      } else {
         return this.containsKey("@ref") ? 0 : super.size();
      }
   }
}
