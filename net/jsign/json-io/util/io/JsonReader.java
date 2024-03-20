package net.jsign.json-io.util.io;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class JsonReader implements Closeable {
   private static Map BASE_READERS;
   protected final Map readers;
   protected MissingFieldHandler missingFieldHandler;
   protected final Set notCustom;
   private static final Map factory = new ConcurrentHashMap();
   private final Map objsRead;
   private final FastPushbackReader input;
   private final Map args;
   private static volatile boolean allowNanAndInfinity = false;

   static {
      Factory colFactory = new CollectionFactory();
      assignInstantiator((Class)Collection.class, colFactory);
      assignInstantiator((Class)List.class, colFactory);
      assignInstantiator((Class)Set.class, colFactory);
      assignInstantiator((Class)SortedSet.class, colFactory);
      Factory mapFactory = new MapFactory();
      assignInstantiator((Class)Map.class, mapFactory);
      assignInstantiator((Class)SortedMap.class, mapFactory);
      Map temp = new HashMap();
      temp.put(String.class, new Readers.StringReader());
      temp.put(Date.class, new Readers.DateReader());
      temp.put(AtomicBoolean.class, new Readers.AtomicBooleanReader());
      temp.put(AtomicInteger.class, new Readers.AtomicIntegerReader());
      temp.put(AtomicLong.class, new Readers.AtomicLongReader());
      temp.put(BigInteger.class, new Readers.BigIntegerReader());
      temp.put(BigDecimal.class, new Readers.BigDecimalReader());
      temp.put(java.sql.Date.class, new Readers.SqlDateReader());
      temp.put(Timestamp.class, new Readers.TimestampReader());
      temp.put(Calendar.class, new Readers.CalendarReader());
      temp.put(TimeZone.class, new Readers.TimeZoneReader());
      temp.put(Locale.class, new Readers.LocaleReader());
      temp.put(Class.class, new Readers.ClassReader());
      temp.put(StringBuilder.class, new Readers.StringBuilderReader());
      temp.put(StringBuffer.class, new Readers.StringBufferReader());
      BASE_READERS = temp;
   }

   public static boolean isAllowNanAndInfinity() {
      return allowNanAndInfinity;
   }

   public static void assignInstantiator(String n, Factory f) {
      factory.put(n, f);
   }

   public static void assignInstantiator(Class c, Factory f) {
      assignInstantiator(c.getName(), f);
   }

   public void addReader(Class c, JsonClassReaderBase reader) {
      this.readers.put(c, reader);
   }

   public void addNotCustomReader(Class c) {
      this.notCustom.add(c);
   }

   MissingFieldHandler getMissingFieldHandler() {
      return this.missingFieldHandler;
   }

   public void setMissingFieldHandler(MissingFieldHandler handler) {
      this.missingFieldHandler = handler;
   }

   public Map getArgs() {
      return this.args;
   }

   public static Map jsonToMaps(String json) {
      return jsonToMaps(json, (Map)null);
   }

   public static Map jsonToMaps(String json, Map optionalArgs) {
      if (optionalArgs == null) {
         optionalArgs = new HashMap();
      }

      ((Map)optionalArgs).put("USE_MAPS", true);
      ByteArrayInputStream ba = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
      JsonReader jr = new JsonReader(ba, (Map)optionalArgs);
      Object ret = jr.readObject();
      jr.close();
      return adjustOutputMap(ret);
   }

   private static Map adjustOutputMap(Object ret) {
      if (ret instanceof Map) {
         return (Map)ret;
      } else {
         JsonObject retMap;
         if (ret != null && ret.getClass().isArray()) {
            retMap = new JsonObject();
            retMap.put("@items", ret);
            return retMap;
         } else {
            retMap = new JsonObject();
            retMap.put("@items", new Object[]{ret});
            return retMap;
         }
      }
   }

   public JsonReader() {
      this.readers = new HashMap(BASE_READERS);
      this.notCustom = new HashSet();
      this.objsRead = new HashMap();
      this.args = new HashMap();
      this.input = null;
      this.getArgs().put("USE_MAPS", false);
      this.getArgs().put("CLASSLOADER", JsonReader.class.getClassLoader());
   }

   public JsonReader(InputStream inp, Map optionalArgs) {
      this.readers = new HashMap(BASE_READERS);
      this.notCustom = new HashSet();
      this.objsRead = new HashMap();
      this.args = new HashMap();
      this.initializeFromArgs(optionalArgs);
      this.input = new FastPushbackBufferedReader(new InputStreamReader(inp, StandardCharsets.UTF_8));
   }

   private void initializeFromArgs(Map optionalArgs) {
      if (optionalArgs == null) {
         optionalArgs = new HashMap();
      }

      Map args = this.getArgs();
      args.putAll((Map)optionalArgs);
      args.put("JSON_READER", this);
      if (!args.containsKey("CLASSLOADER")) {
         args.put("CLASSLOADER", JsonReader.class.getClassLoader());
      }

      Map typeNames = (Map)args.get("TYPE_NAME_MAP");
      Map.Entry entry;
      Iterator var6;
      if (typeNames != null) {
         Map typeNameMap = new HashMap();
         var6 = typeNames.entrySet().iterator();

         while(var6.hasNext()) {
            entry = (Map.Entry)var6.next();
            typeNameMap.put((String)entry.getValue(), (String)entry.getKey());
         }

         args.put("TYPE_NAME_MAP_REVERSE", typeNameMap);
      }

      this.setMissingFieldHandler((MissingFieldHandler)args.get("MISSING_FIELD_HANDLER"));
      Map customReaders = (Map)args.get("CUSTOM_READERS");
      if (customReaders != null) {
         var6 = customReaders.entrySet().iterator();

         while(var6.hasNext()) {
            entry = (Map.Entry)var6.next();
            this.addReader((Class)entry.getKey(), (JsonClassReaderBase)entry.getValue());
         }
      }

      Iterable notCustomReaders = (Iterable)args.get("NOT_CUSTOM_READERS");
      if (notCustomReaders != null) {
         Iterator var7 = notCustomReaders.iterator();

         while(var7.hasNext()) {
            Class c = (Class)var7.next();
            this.addNotCustomReader(c);
         }
      }

   }

   public Map getObjectsRead() {
      return this.objsRead;
   }

   public Object readObject() {
      JsonParser parser = new JsonParser(this.input, this.objsRead, this.getArgs());
      JsonObject root = new JsonObject();

      Object o;
      try {
         o = parser.readValue(root);
         if (o == "~!o~") {
            return new JsonObject();
         }
      } catch (JsonIoException var5) {
         throw var5;
      } catch (Exception var6) {
         throw new JsonIoException("error parsing JSON value", var6);
      }

      Object graph;
      if (o instanceof Object[]) {
         root.setType(Object[].class.getName());
         root.setTarget(o);
         root.put("@items", o);
         graph = this.convertParsedMapsToJava(root);
      } else {
         graph = o instanceof JsonObject ? this.convertParsedMapsToJava((JsonObject)o) : o;
      }

      return this.useMaps() ? o : graph;
   }

   protected boolean useMaps() {
      return Boolean.TRUE.equals(this.getArgs().get("USE_MAPS"));
   }

   ClassLoader getClassLoader() {
      return (ClassLoader)this.args.get("CLASSLOADER");
   }

   protected Object convertParsedMapsToJava(JsonObject root) {
      try {
         Resolver resolver = this.useMaps() ? new MapResolver(this) : new ObjectResolver(this, (ClassLoader)this.args.get("CLASSLOADER"));
         ((Resolver)resolver).createJavaObjectInstance(Object.class, root);
         Object graph = ((Resolver)resolver).convertMapsToObjects(root);
         ((Resolver)resolver).cleanup();
         this.readers.clear();
         return graph;
      } catch (Exception var5) {
         try {
            this.close();
         } catch (Exception var4) {
         }

         if (var5 instanceof JsonIoException) {
            throw (JsonIoException)var5;
         } else {
            throw new JsonIoException(this.getErrorMessage(var5.getMessage()), var5);
         }
      }
   }

   public static Object newInstance(Class c, JsonObject jsonObject) {
      if (factory.containsKey(c.getName())) {
         Factory cf = (Factory)factory.get(c.getName());
         if (cf instanceof ClassFactoryEx) {
            Map args = new HashMap();
            args.put("jsonObj", jsonObject);
            return ((ClassFactoryEx)cf).newInstance(c, args);
         } else if (cf instanceof ClassFactory) {
            return ((ClassFactory)cf).newInstance(c);
         } else {
            throw new JsonIoException("Unknown instantiator (Factory) class.  Must subclass ClassFactoryEx or ClassFactory, found: " + cf.getClass().getName());
         }
      } else {
         return MetaUtils.newInstance(c);
      }
   }

   public void close() {
      try {
         if (this.input != null) {
            this.input.close();
         }

      } catch (Exception var2) {
         throw new JsonIoException("Unable to close input", var2);
      }
   }

   private String getErrorMessage(String msg) {
      return this.input != null ? msg + "\nLast read: " + this.input.getLastSnippet() + "\nline: " + this.input.getLine() + ", col: " + this.input.getCol() : msg;
   }

   public interface ClassFactory extends Factory {
      Object newInstance(Class var1);
   }

   public interface ClassFactoryEx extends Factory {
      Object newInstance(Class var1, Map var2);
   }

   public static class CollectionFactory implements ClassFactory {
      public Object newInstance(Class c) {
         if (List.class.isAssignableFrom(c)) {
            return new ArrayList();
         } else if (SortedSet.class.isAssignableFrom(c)) {
            return new TreeSet();
         } else if (Set.class.isAssignableFrom(c)) {
            return new LinkedHashSet();
         } else if (Collection.class.isAssignableFrom(c)) {
            return new ArrayList();
         } else {
            throw new JsonIoException("CollectionFactory handed Class for which it was not expecting: " + c.getName());
         }
      }
   }

   public interface Factory {
   }

   public interface JsonClassReader extends JsonClassReaderBase {
      Object read(Object var1, Deque var2);
   }

   public interface JsonClassReaderBase {
   }

   public interface JsonClassReaderEx extends JsonClassReaderBase {
      Object read(Object var1, Deque var2, Map var3);
   }

   public static class MapFactory implements ClassFactory {
      public Object newInstance(Class c) {
         if (SortedMap.class.isAssignableFrom(c)) {
            return new TreeMap();
         } else if (Map.class.isAssignableFrom(c)) {
            return new LinkedHashMap();
         } else {
            throw new JsonIoException("MapFactory handed Class for which it was not expecting: " + c.getName());
         }
      }
   }

   public interface MissingFieldHandler {
      void fieldMissing(Object var1, String var2, Object var3);
   }
}
