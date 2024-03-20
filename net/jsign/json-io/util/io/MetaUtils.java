package net.jsign.json-io.util.io;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MetaUtils {
   private static final Map classMetaCache = new ConcurrentHashMap();
   private static final Set prims = new HashSet();
   private static final Map nameToClass = new HashMap();
   private static final Byte[] byteCache = new Byte[256];
   private static final Character[] charCache = new Character[128];
   private static final Pattern extraQuotes = Pattern.compile("([\"]*)([^\"]*)([\"]*)");
   private static final Class[] emptyClassArray = new Class[0];
   private static final ConcurrentMap constructors = new ConcurrentHashMap();
   private static final Collection unmodifiableCollection = Collections.unmodifiableCollection(new ArrayList());
   private static final Collection unmodifiableSet = Collections.unmodifiableSet(new HashSet());
   private static final Collection unmodifiableSortedSet = Collections.unmodifiableSortedSet(new TreeSet());
   private static final Map unmodifiableMap = Collections.unmodifiableMap(new HashMap());
   private static final Map unmodifiableSortedMap = Collections.unmodifiableSortedMap(new TreeMap());
   static final ThreadLocal dateFormat = new ThreadLocal() {
      public SimpleDateFormat initialValue() {
         return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
      }
   };
   private static boolean useUnsafe = false;
   private static Unsafe unsafe;
   static Exception loadClassException;

   static {
      prims.add(Byte.class);
      prims.add(Integer.class);
      prims.add(Long.class);
      prims.add(Double.class);
      prims.add(Character.class);
      prims.add(Float.class);
      prims.add(Boolean.class);
      prims.add(Short.class);
      nameToClass.put("string", String.class);
      nameToClass.put("boolean", Boolean.TYPE);
      nameToClass.put("char", Character.TYPE);
      nameToClass.put("byte", Byte.TYPE);
      nameToClass.put("short", Short.TYPE);
      nameToClass.put("int", Integer.TYPE);
      nameToClass.put("long", Long.TYPE);
      nameToClass.put("float", Float.TYPE);
      nameToClass.put("double", Double.TYPE);
      nameToClass.put("date", Date.class);
      nameToClass.put("class", Class.class);

      int i;
      for(i = 0; i < byteCache.length; ++i) {
         byteCache[i] = (byte)(i - 128);
      }

      for(i = 0; i < charCache.length; ++i) {
         charCache[i] = (char)i;
      }

   }

   public static Field getField(Class c, String field) {
      return (Field)getDeepDeclaredFields(c).get(field);
   }

   public static Map getDeepDeclaredFields(Class c) {
      Map classFields = (Map)classMetaCache.get(c);
      if (classFields != null) {
         return classFields;
      } else {
         Map classFields = new LinkedHashMap();

         for(Class curr = c; curr != null; curr = curr.getSuperclass()) {
            try {
               Field[] local = curr.getDeclaredFields();
               Field[] var7 = local;
               int var6 = local.length;

               for(int var5 = 0; var5 < var6; ++var5) {
                  Field field = var7[var5];
                  if ((field.getModifiers() & 8) == 0 && (!"metaClass".equals(field.getName()) || !"groovy.lang.MetaClass".equals(field.getType().getName()))) {
                     try {
                        field.setAccessible(true);
                     } catch (Exception var8) {
                     }

                     if (classFields.containsKey(field.getName())) {
                        classFields.put(curr.getName() + '.' + field.getName(), field);
                     } else {
                        classFields.put(field.getName(), field);
                     }
                  }
               }
            } catch (ThreadDeath var9) {
               throw var9;
            } catch (Throwable var10) {
            }
         }

         classMetaCache.put(c, classFields);
         return classFields;
      }
   }

   public static int getDistance(Class a, Class b) {
      if (a.isInterface()) {
         return getDistanceToInterface(a, b);
      } else {
         Class curr = b;
         int distance = 0;

         while(curr != a) {
            ++distance;
            curr = curr.getSuperclass();
            if (curr == null) {
               return Integer.MAX_VALUE;
            }
         }

         return distance;
      }
   }

   static int getDistanceToInterface(Class to, Class from) {
      Set possibleCandidates = new LinkedHashSet();
      Class[] interfaces = from.getInterfaces();
      Class[] var7 = interfaces;
      int var6 = interfaces.length;

      for(int var5 = 0; var5 < var6; ++var5) {
         Class interfase = var7[var5];
         if (to.equals(interfase)) {
            return 1;
         }

         if (to.isAssignableFrom(interfase)) {
            possibleCandidates.add(interfase);
         }
      }

      if (from.getSuperclass() != null && to.isAssignableFrom(from.getSuperclass())) {
         possibleCandidates.add(from.getSuperclass());
      }

      int minimum = Integer.MAX_VALUE;
      Iterator var10 = possibleCandidates.iterator();

      while(var10.hasNext()) {
         Class candidate = (Class)var10.next();
         int distance = getDistanceToInterface(to, candidate);
         if (distance < minimum) {
            ++distance;
            minimum = distance;
         }
      }

      return minimum;
   }

   public static boolean isPrimitive(Class c) {
      return c.isPrimitive() || prims.contains(c);
   }

   public static boolean isLogicalPrimitive(Class c) {
      return c.isPrimitive() || prims.contains(c) || String.class.isAssignableFrom(c) || Number.class.isAssignableFrom(c) || Date.class.isAssignableFrom(c) || c.isEnum() || c.equals(Class.class);
   }

   static Class classForName(String name, ClassLoader classLoader, boolean failOnClassLoadingError) {
      if (name != null && !name.isEmpty()) {
         Class c = (Class)nameToClass.get(name);

         try {
            loadClassException = null;
            return c == null ? loadClass(name, classLoader) : c;
         } catch (Exception var5) {
            loadClassException = var5;
            if (failOnClassLoadingError) {
               throw new JsonIoException("Unable to create class: " + name, var5);
            } else {
               return LinkedHashMap.class;
            }
         }
      } else {
         throw new JsonIoException("Class name cannot be null or empty.");
      }
   }

   static Class classForName(String name, ClassLoader classLoader) {
      return classForName(name, classLoader, false);
   }

   private static Class loadClass(String name, ClassLoader classLoader) throws ClassNotFoundException {
      String className = name;
      boolean arrayType = false;

      Class primitiveArray;
      int startpos;
      for(primitiveArray = null; className.startsWith("["); className = className.substring(startpos)) {
         arrayType = true;
         if (className.endsWith(";")) {
            className = className.substring(0, className.length() - 1);
         }

         if (className.equals("[B")) {
            primitiveArray = byte[].class;
         } else if (className.equals("[S")) {
            primitiveArray = short[].class;
         } else if (className.equals("[I")) {
            primitiveArray = int[].class;
         } else if (className.equals("[J")) {
            primitiveArray = long[].class;
         } else if (className.equals("[F")) {
            primitiveArray = float[].class;
         } else if (className.equals("[D")) {
            primitiveArray = double[].class;
         } else if (className.equals("[Z")) {
            primitiveArray = boolean[].class;
         } else if (className.equals("[C")) {
            primitiveArray = char[].class;
         }

         startpos = className.startsWith("[L") ? 2 : 1;
      }

      Class currentClass = null;
      if (primitiveArray == null) {
         try {
            currentClass = classLoader.loadClass(className);
         } catch (ClassNotFoundException var6) {
            currentClass = Thread.currentThread().getContextClassLoader().loadClass(className);
         }
      }

      if (arrayType) {
         for(currentClass = primitiveArray != null ? primitiveArray : Array.newInstance(currentClass, 0).getClass(); name.startsWith("[["); name = name.substring(1)) {
            currentClass = Array.newInstance(currentClass, 0).getClass();
         }
      }

      return currentClass;
   }

   static String removeLeadingAndTrailingQuotes(String s) {
      Matcher m = extraQuotes.matcher(s);
      if (m.find()) {
         s = m.group(2);
      }

      return s;
   }

   public static Object newInstance(Class c) {
      if (c.isAssignableFrom(ProcessBuilder.class) && c != Object.class) {
         throw new IllegalArgumentException("For security reasons, json-io does not allow instantiation of the ProcessBuilder class.");
      } else if (unmodifiableSortedMap.getClass().isAssignableFrom(c)) {
         return new TreeMap();
      } else if (unmodifiableMap.getClass().isAssignableFrom(c)) {
         return new LinkedHashMap();
      } else if (unmodifiableSortedSet.getClass().isAssignableFrom(c)) {
         return new TreeSet();
      } else if (unmodifiableSet.getClass().isAssignableFrom(c)) {
         return new LinkedHashSet();
      } else if (unmodifiableCollection.getClass().isAssignableFrom(c)) {
         return new ArrayList();
      } else if (c.isInterface()) {
         throw new JsonIoException("Cannot instantiate unknown interface: " + c.getName());
      } else {
         Object[] constructorInfo = (Object[])constructors.get(c);
         if (constructorInfo != null) {
            Constructor constructor = (Constructor)constructorInfo[0];
            if (constructor == null && useUnsafe) {
               try {
                  return unsafe.allocateInstance(c);
               } catch (Exception var7) {
                  throw new JsonIoException("Could not instantiate " + c.getName(), var7);
               }
            } else if (constructor == null) {
               throw new JsonIoException("No constructor found to instantiate " + c.getName());
            } else {
               Boolean useNull = (Boolean)constructorInfo[1];
               Class[] paramTypes = constructor.getParameterTypes();
               if (paramTypes != null && paramTypes.length != 0) {
                  Object[] values = fillArgs(paramTypes, useNull);

                  try {
                     return constructor.newInstance(values);
                  } catch (Exception var8) {
                     throw new JsonIoException("Could not instantiate " + c.getName(), var8);
                  }
               } else {
                  try {
                     return constructor.newInstance();
                  } catch (Exception var9) {
                     throw new JsonIoException("Could not instantiate " + c.getName(), var9);
                  }
               }
            }
         } else {
            Object[] ret = newInstanceEx(c);
            constructors.put(c, new Object[]{ret[1], ret[2]});
            return ret[0];
         }
      }
   }

   static Object[] newInstanceEx(Class c) {
      try {
         Constructor constructor = c.getConstructor(emptyClassArray);
         return constructor != null ? new Object[]{constructor.newInstance(), constructor, true} : tryOtherConstruction(c);
      } catch (Exception var2) {
         return tryOtherConstruction(c);
      }
   }

   static Object[] tryOtherConstruction(Class c) {
      Constructor[] constructors = c.getDeclaredConstructors();
      if (constructors.length == 0) {
         throw new JsonIoException("Cannot instantiate '" + c.getName() + "' - Primitive, interface, array[] or void");
      } else {
         List constructorList = Arrays.asList(constructors);
         Collections.sort(constructorList, new Comparator() {
            public int compare(Constructor c1, Constructor c2) {
               int c1Vis = c1.getModifiers();
               int c2Vis = c2.getModifiers();
               if (c1Vis == c2Vis) {
                  return MetaUtils.compareConstructors(c1, c2);
               } else if (Modifier.isPublic(c1Vis) != Modifier.isPublic(c2Vis)) {
                  return Modifier.isPublic(c1Vis) ? -1 : 1;
               } else if (Modifier.isProtected(c1Vis) != Modifier.isProtected(c2Vis)) {
                  return Modifier.isProtected(c1Vis) ? -1 : 1;
               } else if (Modifier.isPrivate(c1Vis) != Modifier.isPrivate(c2Vis)) {
                  return Modifier.isPrivate(c1Vis) ? 1 : -1;
               } else {
                  return 0;
               }
            }
         });
         Iterator var4 = constructorList.iterator();

         Constructor constructor;
         Class[] argTypes;
         Object[] values;
         while(var4.hasNext()) {
            constructor = (Constructor)var4.next();
            constructor.setAccessible(true);
            argTypes = constructor.getParameterTypes();
            values = fillArgs(argTypes, true);

            try {
               return new Object[]{constructor.newInstance(values), constructor, true};
            } catch (Exception var9) {
            }
         }

         var4 = constructorList.iterator();

         while(var4.hasNext()) {
            constructor = (Constructor)var4.next();
            constructor.setAccessible(true);
            argTypes = constructor.getParameterTypes();
            values = fillArgs(argTypes, false);

            try {
               return new Object[]{constructor.newInstance(values), constructor, false};
            } catch (Exception var8) {
            }
         }

         if (useUnsafe) {
            try {
               return new Object[]{unsafe.allocateInstance(c), null, null};
            } catch (Exception var7) {
            }
         }

         throw new JsonIoException("Could not instantiate " + c.getName() + " using any constructor");
      }
   }

   private static int compareConstructors(Constructor c1, Constructor c2) {
      Class[] c1ParamTypes = c1.getParameterTypes();
      Class[] c2ParamTypes = c2.getParameterTypes();
      if (c1ParamTypes.length != c2ParamTypes.length) {
         return c1ParamTypes.length - c2ParamTypes.length;
      } else {
         int len = c1ParamTypes.length;

         for(int i = 0; i < len; ++i) {
            Class class1 = c1ParamTypes[i];
            Class class2 = c2ParamTypes[i];
            int compare = class1.getName().compareTo(class2.getName());
            if (compare != 0) {
               return compare;
            }
         }

         return 0;
      }
   }

   static Object[] fillArgs(Class[] argTypes, boolean useNull) {
      Object[] values = new Object[argTypes.length];

      for(int i = 0; i < argTypes.length; ++i) {
         Class argType = argTypes[i];
         if (isPrimitive(argType)) {
            values[i] = convert(argType, (Object)null);
         } else if (useNull) {
            values[i] = null;
         } else if (argType == String.class) {
            values[i] = "";
         } else if (argType == Date.class) {
            values[i] = new Date();
         } else if (List.class.isAssignableFrom(argType)) {
            values[i] = new ArrayList();
         } else if (SortedSet.class.isAssignableFrom(argType)) {
            values[i] = new TreeSet();
         } else if (Set.class.isAssignableFrom(argType)) {
            values[i] = new LinkedHashSet();
         } else if (SortedMap.class.isAssignableFrom(argType)) {
            values[i] = new TreeMap();
         } else if (Map.class.isAssignableFrom(argType)) {
            values[i] = new LinkedHashMap();
         } else if (Collection.class.isAssignableFrom(argType)) {
            values[i] = new ArrayList();
         } else if (Calendar.class.isAssignableFrom(argType)) {
            values[i] = Calendar.getInstance();
         } else if (TimeZone.class.isAssignableFrom(argType)) {
            values[i] = TimeZone.getDefault();
         } else if (argType == BigInteger.class) {
            values[i] = BigInteger.TEN;
         } else if (argType == BigDecimal.class) {
            values[i] = BigDecimal.TEN;
         } else if (argType == StringBuilder.class) {
            values[i] = new StringBuilder();
         } else if (argType == StringBuffer.class) {
            values[i] = new StringBuffer();
         } else if (argType == Locale.class) {
            values[i] = Locale.FRANCE;
         } else if (argType == Class.class) {
            values[i] = String.class;
         } else if (argType == Timestamp.class) {
            values[i] = new Timestamp(System.currentTimeMillis());
         } else if (argType == java.sql.Date.class) {
            values[i] = new java.sql.Date(System.currentTimeMillis());
         } else if (argType == URL.class) {
            try {
               values[i] = new URL("http://localhost");
            } catch (MalformedURLException var5) {
               values[i] = null;
            }
         } else if (argType == Object.class) {
            values[i] = new Object();
         } else {
            values[i] = null;
         }
      }

      return values;
   }

   static Object convert(Class c, Object rhs) {
      try {
         String rhs;
         if (c != Boolean.TYPE && c != Boolean.class) {
            if (c != Byte.TYPE && c != Byte.class) {
               if (c != Character.TYPE && c != Character.class) {
                  if (c != Double.TYPE && c != Double.class) {
                     if (c != Float.TYPE && c != Float.class) {
                        if (c != Integer.TYPE && c != Integer.class) {
                           if (c != Long.TYPE && c != Long.class) {
                              if (c != Short.TYPE && c != Short.class) {
                                 if (c == Date.class) {
                                    if (rhs instanceof String) {
                                       return Readers.DateReader.parseDate((String)rhs);
                                    }

                                    if (rhs instanceof Long) {
                                       return new Date((Long)rhs);
                                    }
                                 } else {
                                    if (c == BigInteger.class) {
                                       return Readers.bigIntegerFrom(rhs);
                                    }

                                    if (c == BigDecimal.class) {
                                       return Readers.bigDecimalFrom(rhs);
                                    }
                                 }

                                 throw new JsonIoException("Class '" + c.getName() + "' does not have primitive wrapper.");
                              } else if (rhs instanceof String) {
                                 rhs = removeLeadingAndTrailingQuotes((String)rhs);
                                 if ("".equals(rhs)) {
                                    rhs = "0";
                                 }

                                 return Short.parseShort((String)rhs);
                              } else {
                                 return rhs != null ? ((Number)rhs).shortValue() : 0;
                              }
                           } else if (rhs instanceof String) {
                              rhs = removeLeadingAndTrailingQuotes((String)rhs);
                              if ("".equals(rhs)) {
                                 rhs = "0";
                              }

                              return Long.parseLong((String)rhs);
                           } else {
                              return rhs != null ? ((Number)rhs).longValue() : 0L;
                           }
                        } else if (rhs instanceof String) {
                           rhs = removeLeadingAndTrailingQuotes((String)rhs);
                           if ("".equals(rhs)) {
                              rhs = "0";
                           }

                           return Integer.parseInt((String)rhs);
                        } else {
                           return rhs != null ? ((Number)rhs).intValue() : 0;
                        }
                     } else if (rhs instanceof String) {
                        rhs = removeLeadingAndTrailingQuotes((String)rhs);
                        if ("".equals(rhs)) {
                           rhs = "0.0f";
                        }

                        return Float.parseFloat((String)rhs);
                     } else {
                        return rhs != null ? ((Number)rhs).floatValue() : 0.0F;
                     }
                  } else if (rhs instanceof String) {
                     rhs = removeLeadingAndTrailingQuotes((String)rhs);
                     if ("".equals(rhs)) {
                        rhs = "0.0";
                     }

                     return Double.parseDouble((String)rhs);
                  } else {
                     return rhs != null ? ((Number)rhs).doubleValue() : 0.0;
                  }
               } else if (rhs == null) {
                  return '\u0000';
               } else if (rhs instanceof String) {
                  if (rhs.equals("\"")) {
                     return '"';
                  } else {
                     rhs = removeLeadingAndTrailingQuotes((String)rhs);
                     if ("".equals(rhs)) {
                        rhs = "\u0000";
                     }

                     return ((CharSequence)rhs).charAt(0);
                  }
               } else if (rhs instanceof Character) {
                  return rhs;
               } else {
                  throw new JsonIoException("Class '" + c.getName() + "' does not have primitive wrapper.");
               }
            } else if (rhs instanceof String) {
               rhs = removeLeadingAndTrailingQuotes((String)rhs);
               if ("".equals(rhs)) {
                  rhs = "0";
               }

               return Byte.parseByte((String)rhs);
            } else {
               return rhs != null ? byteCache[((Number)rhs).byteValue() + 128] : 0;
            }
         } else if (rhs instanceof String) {
            rhs = removeLeadingAndTrailingQuotes((String)rhs);
            if ("".equals(rhs)) {
               rhs = "false";
            }

            return Boolean.parseBoolean((String)rhs);
         } else {
            return rhs != null ? rhs : Boolean.FALSE;
         }
      } catch (Exception var4) {
         String className = c == null ? "null" : c.getName();
         throw new JsonIoException("Error creating primitive wrapper instance for Class: " + className, var4);
      }
   }

   static final class Unsafe {
      private final Object sunUnsafe;
      private final Method allocateInstance;

      public Object allocateInstance(Class clazz) {
         String name;
         try {
            return this.allocateInstance.invoke(this.sunUnsafe, clazz);
         } catch (IllegalAccessException var4) {
            name = clazz == null ? "null" : clazz.getName();
            throw new JsonIoException("Unable to create instance of class: " + name, var4);
         } catch (IllegalArgumentException var5) {
            name = clazz == null ? "null" : clazz.getName();
            throw new JsonIoException("Unable to create instance of class: " + name, var5);
         } catch (InvocationTargetException var6) {
            name = clazz == null ? "null" : clazz.getName();
            throw new JsonIoException("Unable to create instance of class: " + name, (Throwable)(var6.getCause() != null ? var6.getCause() : var6));
         }
      }
   }
}
