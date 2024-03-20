package net.jsign.json-io.util.io;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

abstract class Resolver {
   final Collection unresolvedRefs = new ArrayList();
   protected final JsonReader reader;
   private static final NullClass nullReader = new NullClass((NullClass)null);
   final Map readerCache = new HashMap();
   private final Collection prettyMaps = new ArrayList();
   private final boolean useMaps;
   private final Object unknownClass;
   private final boolean failOnUnknownType;
   private static final Map coercedTypes = new LinkedHashMap();
   protected final Collection missingFields = new ArrayList();
   Class singletonMap = Collections.singletonMap("foo", "bar").getClass();

   static {
      coercedTypes.put("java.util.Arrays$ArrayList", ArrayList.class);
      coercedTypes.put("java.util.LinkedHashMap$LinkedKeySet", LinkedHashSet.class);
      coercedTypes.put("java.util.LinkedHashMap$LinkedValues", ArrayList.class);
      coercedTypes.put("java.util.HashMap$KeySet", HashSet.class);
      coercedTypes.put("java.util.HashMap$Values", ArrayList.class);
      coercedTypes.put("java.util.TreeMap$KeySet", TreeSet.class);
      coercedTypes.put("java.util.TreeMap$Values", ArrayList.class);
      coercedTypes.put("java.util.concurrent.ConcurrentHashMap$KeySet", LinkedHashSet.class);
      coercedTypes.put("java.util.concurrent.ConcurrentHashMap$KeySetView", LinkedHashSet.class);
      coercedTypes.put("java.util.concurrent.ConcurrentHashMap$Values", ArrayList.class);
      coercedTypes.put("java.util.concurrent.ConcurrentHashMap$ValuesView", ArrayList.class);
      coercedTypes.put("java.util.concurrent.ConcurrentSkipListMap$KeySet", LinkedHashSet.class);
      coercedTypes.put("java.util.concurrent.ConcurrentSkipListMap$Values", ArrayList.class);
      coercedTypes.put("java.util.IdentityHashMap$KeySet", LinkedHashSet.class);
      coercedTypes.put("java.util.IdentityHashMap$Values", ArrayList.class);
   }

   protected Resolver(JsonReader reader) {
      this.reader = reader;
      Map optionalArgs = reader.getArgs();
      optionalArgs.put("OBJECT_RESOLVER", this);
      this.useMaps = Boolean.TRUE.equals(optionalArgs.get("USE_MAPS"));
      this.unknownClass = optionalArgs.containsKey("UNKNOWN_OBJECT") ? optionalArgs.get("UNKNOWN_OBJECT") : null;
      this.failOnUnknownType = Boolean.TRUE.equals(optionalArgs.get("FAIL_ON_UNKNOWN_TYPE"));
   }

   protected JsonReader getReader() {
      return this.reader;
   }

   protected Object convertMapsToObjects(JsonObject root) {
      Deque stack = new ArrayDeque();
      stack.addFirst(root);

      while(!stack.isEmpty()) {
         JsonObject jsonObj = (JsonObject)stack.removeFirst();
         if (jsonObj.isArray()) {
            this.traverseArray(stack, jsonObj);
         } else if (jsonObj.isCollection()) {
            this.traverseCollection(stack, jsonObj);
         } else if (jsonObj.isMap()) {
            this.traverseMap(stack, jsonObj);
         } else {
            Object special;
            if ((special = this.readIfMatching(jsonObj, (Class)null, stack)) != null) {
               jsonObj.target = special;
            } else {
               this.traverseFields(stack, jsonObj);
            }
         }
      }

      return root.target;
   }

   protected abstract Object readIfMatching(Object var1, Class var2, Deque var3);

   public abstract void traverseFields(Deque var1, JsonObject var2);

   protected abstract void traverseCollection(Deque var1, JsonObject var2);

   protected abstract void traverseArray(Deque var1, JsonObject var2);

   protected void cleanup() {
      this.patchUnresolvedReferences();
      this.rehashMaps();
      this.reader.getObjectsRead().clear();
      this.unresolvedRefs.clear();
      this.prettyMaps.clear();
      this.readerCache.clear();
      this.handleMissingFields();
   }

   private void handleMissingFields() {
      JsonReader.MissingFieldHandler missingFieldHandler = this.reader.getMissingFieldHandler();
      if (missingFieldHandler != null) {
         Iterator var3 = this.missingFields.iterator();

         while(var3.hasNext()) {
            Missingfields mf = (Missingfields)var3.next();
            missingFieldHandler.fieldMissing(mf.target, mf.fieldName, mf.value);
         }
      }

   }

   protected void traverseMap(Deque stack, JsonObject jsonObj) {
      convertMapToKeysItems(jsonObj);
      Object[] keys = (Object[])jsonObj.get("@keys");
      Object[] items = jsonObj.getArray();
      if (keys != null && items != null) {
         int size = keys.length;
         if (size != items.length) {
            throw new JsonIoException("Map written with @keys and @items entries of different sizes");
         } else {
            Object[] mapKeys = buildCollection(stack, keys, size);
            Object[] mapValues = buildCollection(stack, items, size);
            this.prettyMaps.add(new Object[]{jsonObj, mapKeys, mapValues});
         }
      } else if (keys != items) {
         throw new JsonIoException("Map written where one of @keys or @items is empty");
      }
   }

   private static Object[] buildCollection(Deque stack, Object[] items, int size) {
      JsonObject jsonCollection = new JsonObject();
      jsonCollection.put("@items", items);
      Object[] javaKeys = new Object[size];
      jsonCollection.target = javaKeys;
      stack.addFirst(jsonCollection);
      return javaKeys;
   }

   protected static void convertMapToKeysItems(JsonObject map) {
      if (!map.containsKey("@keys") && !map.isReference()) {
         Object[] keys = new Object[map.size()];
         Object[] values = new Object[map.size()];
         int i = 0;

         for(Iterator var5 = map.entrySet().iterator(); var5.hasNext(); ++i) {
            Object e = var5.next();
            Map.Entry entry = (Map.Entry)e;
            keys[i] = entry.getKey();
            values[i] = entry.getValue();
         }

         String saveType = map.getType();
         map.clear();
         map.setType(saveType);
         map.put("@keys", keys);
         map.put("@items", values);
      }

   }

   protected Object createJavaObjectInstance(Class clazz, JsonObject jsonObj) {
      boolean useMapsLocal = this.useMaps;
      String type = jsonObj.type;
      Object mate;
      if ("java.lang.Object".equals(type)) {
         mate = jsonObj.get("value");
         if (jsonObj.keySet().size() == 1 && mate != null) {
            type = mate.getClass().getName();
         }
      }

      if (type != null) {
         Class c;
         try {
            c = MetaUtils.classForName(type, this.reader.getClassLoader(), this.failOnUnknownType);
         } catch (Exception var9) {
            if (useMapsLocal) {
               jsonObj.type = null;
               jsonObj.target = null;
               return jsonObj;
            }

            String name = clazz == null ? "null" : clazz.getName();
            throw new JsonIoException("Unable to create class: " + name, var9);
         }

         if (c.isArray()) {
            Object[] items = jsonObj.getArray();
            int size = items == null ? 0 : items.length;
            if (c == char[].class) {
               jsonObj.moveCharsToMate();
               mate = jsonObj.target;
            } else {
               mate = Array.newInstance(c.getComponentType(), size);
            }
         } else if (MetaUtils.isPrimitive(c)) {
            mate = MetaUtils.convert(c, jsonObj.get("value"));
         } else if (c == Class.class) {
            mate = MetaUtils.classForName((String)jsonObj.get("value"), this.reader.getClassLoader());
         } else if (c.isEnum()) {
            mate = this.getEnum(c, jsonObj);
         } else if (Enum.class.isAssignableFrom(c)) {
            mate = this.getEnum(c.getSuperclass(), jsonObj);
         } else if (EnumSet.class.isAssignableFrom(c)) {
            mate = this.getEnumSet(c, jsonObj);
         } else if ((mate = this.coerceCertainTypes(c.getName())) == null) {
            if (this.singletonMap.isAssignableFrom(c)) {
               Object key = jsonObj.keySet().iterator().next();
               Object value = jsonObj.values().iterator().next();
               mate = Collections.singletonMap(key, value);
            } else {
               mate = newInstance(c, jsonObj);
            }
         }
      } else {
         Object[] items = jsonObj.getArray();
         if (clazz.isArray() || items != null && clazz == Object.class && !jsonObj.containsKey("@keys")) {
            int size = items == null ? 0 : items.length;
            mate = Array.newInstance(clazz.isArray() ? clazz.getComponentType() : Object.class, size);
         } else if (clazz.isEnum()) {
            mate = this.getEnum(clazz, jsonObj);
         } else if (Enum.class.isAssignableFrom(clazz)) {
            mate = this.getEnum(clazz.getSuperclass(), jsonObj);
         } else if (EnumSet.class.isAssignableFrom(clazz)) {
            mate = this.getEnumSet(clazz, jsonObj);
         } else if ((mate = this.coerceCertainTypes(clazz.getName())) == null) {
            if (clazz == Object.class && !useMapsLocal) {
               if (this.unknownClass == null) {
                  mate = new JsonObject();
                  ((JsonObject)mate).type = Map.class.getName();
               } else {
                  if (!(this.unknownClass instanceof String)) {
                     throw new JsonIoException("Unable to determine object type at column: " + jsonObj.col + ", line: " + jsonObj.line + ", content: " + jsonObj);
                  }

                  mate = newInstance(MetaUtils.classForName(((String)this.unknownClass).trim(), this.reader.getClassLoader()), jsonObj);
               }
            } else {
               mate = newInstance(clazz, jsonObj);
            }
         }
      }

      jsonObj.target = mate;
      return jsonObj.target;
   }

   protected Object coerceCertainTypes(String type) {
      Class clazz = (Class)coercedTypes.get(type);
      return clazz == null ? null : MetaUtils.newInstance(clazz);
   }

   protected JsonObject getReferencedObj(Long ref) {
      JsonObject refObject = (JsonObject)this.reader.getObjectsRead().get(ref);
      if (refObject == null) {
         throw new JsonIoException("Forward reference @ref: " + ref + ", but no object defined (@id) with that value");
      } else {
         return refObject;
      }
   }

   protected JsonReader.JsonClassReaderBase getCustomReader(Class c) {
      JsonReader.JsonClassReaderBase reader = (JsonReader.JsonClassReaderBase)this.readerCache.get(c);
      if (reader == null) {
         reader = this.forceGetCustomReader(c);
         this.readerCache.put(c, reader);
      }

      return reader == nullReader ? null : reader;
   }

   private JsonReader.JsonClassReaderBase forceGetCustomReader(Class c) {
      JsonReader.JsonClassReaderBase closestReader = nullReader;
      int minDistance = Integer.MAX_VALUE;
      Iterator var5 = this.getReaders().entrySet().iterator();

      while(var5.hasNext()) {
         Map.Entry entry = (Map.Entry)var5.next();
         Class clz = (Class)entry.getKey();
         if (clz == c) {
            return (JsonReader.JsonClassReaderBase)entry.getValue();
         }

         int distance = MetaUtils.getDistance(clz, c);
         if (distance < minDistance) {
            minDistance = distance;
            closestReader = (JsonReader.JsonClassReaderBase)entry.getValue();
         }
      }

      return (JsonReader.JsonClassReaderBase)closestReader;
   }

   private Object getEnum(Class c, JsonObject jsonObj) {
      try {
         return Enum.valueOf(c, (String)jsonObj.get("name"));
      } catch (Exception var3) {
         return Enum.valueOf(c, (String)jsonObj.get("java.lang.Enum.name"));
      }
   }

   private Object getEnumSet(Class c, JsonObject jsonObj) {
      Object[] items = jsonObj.getArray();
      if (items != null && items.length != 0) {
         JsonObject item = (JsonObject)items[0];
         String type = item.getType();
         Class enumClass = MetaUtils.classForName(type, this.reader.getClassLoader());
         EnumSet enumSet = null;
         Object[] var11 = items;
         int var10 = items.length;

         for(int var9 = 0; var9 < var10; ++var9) {
            Object objectItem = var11[var9];
            item = (JsonObject)objectItem;
            Enum enumItem = (Enum)this.getEnum(enumClass, item);
            if (enumSet == null) {
               enumSet = EnumSet.of(enumItem);
            } else {
               enumSet.add(enumItem);
            }
         }

         return enumSet;
      } else {
         return newInstance(c, jsonObj);
      }
   }

   protected void patchUnresolvedReferences() {
      for(Iterator i = this.unresolvedRefs.iterator(); i.hasNext(); i.remove()) {
         UnresolvedReference ref = (UnresolvedReference)i.next();
         Object objToFix = ref.referencingObj.target;
         JsonObject objReferenced = (JsonObject)this.reader.getObjectsRead().get(ref.refId);
         if (ref.index >= 0) {
            if (objToFix instanceof List) {
               List list = (List)objToFix;
               list.set(ref.index, objReferenced.target);
            } else if (objToFix instanceof Collection) {
               Collection col = (Collection)objToFix;
               col.add(objReferenced.target);
            } else {
               Array.set(objToFix, ref.index, objReferenced.target);
            }
         } else {
            Field field = MetaUtils.getField(objToFix.getClass(), ref.field);
            if (field != null) {
               try {
                  field.set(objToFix, objReferenced.target);
               } catch (Exception var7) {
                  throw new JsonIoException("Error setting field while resolving references '" + field.getName() + "', @ref = " + ref.refId, var7);
               }
            }
         }
      }

   }

   protected void rehashMaps() {
      boolean useMapsLocal = this.useMaps;
      Iterator var3 = this.prettyMaps.iterator();

      while(true) {
         Object[] javaKeys;
         Object[] javaValues;
         Object map;
         do {
            if (!var3.hasNext()) {
               return;
            }

            Object[] mapPieces = (Object[])var3.next();
            JsonObject jObj = (JsonObject)mapPieces[0];
            if (useMapsLocal) {
               map = jObj;
               javaKeys = (Object[])jObj.remove("@keys");
               javaValues = (Object[])jObj.remove("@items");
            } else {
               map = (Map)jObj.target;
               javaKeys = (Object[])mapPieces[1];
               javaValues = (Object[])mapPieces[2];
               jObj.clear();
            }
         } while(this.singletonMap.isAssignableFrom(map.getClass()));

         for(int j = 0; javaKeys != null && j < javaKeys.length; ++j) {
            ((Map)map).put(javaKeys[j], javaValues[j]);
         }
      }
   }

   public static Object newInstance(Class c, JsonObject jsonObject) {
      return JsonReader.newInstance(c, jsonObject);
   }

   protected Map getReaders() {
      return this.reader.readers;
   }

   protected boolean notCustom(Class cls) {
      return this.reader.notCustom.contains(cls);
   }

   protected static class Missingfields {
      private Object target;
      private String fieldName;
      private Object value;

      public Missingfields(Object target, String fieldName, Object value) {
         this.target = target;
         this.fieldName = fieldName;
         this.value = value;
      }
   }

   private static final class NullClass implements JsonReader.JsonClassReaderBase {
      private NullClass() {
      }

      // $FF: synthetic method
      NullClass(NullClass var1) {
         this();
      }
   }

   static final class UnresolvedReference {
      private final JsonObject referencingObj;
      private String field;
      private final long refId;
      private int index = -1;

      UnresolvedReference(JsonObject referrer, String fld, long id) {
         this.referencingObj = referrer;
         this.field = fld;
         this.refId = id;
      }

      UnresolvedReference(JsonObject referrer, int idx, long id) {
         this.referencingObj = referrer;
         this.index = idx;
         this.refId = id;
      }
   }
}
