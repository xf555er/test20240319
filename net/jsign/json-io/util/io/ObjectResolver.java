package net.jsign.json-io.util.io;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ObjectResolver extends Resolver {
   private final ClassLoader classLoader;
   protected JsonReader.MissingFieldHandler missingFieldHandler;

   protected ObjectResolver(JsonReader reader, ClassLoader classLoader) {
      super(reader);
      this.classLoader = classLoader;
      this.missingFieldHandler = reader.getMissingFieldHandler();
   }

   public void traverseFields(Deque stack, JsonObject jsonObj) {
      Object javaMate = jsonObj.target;
      Iterator i = jsonObj.entrySet().iterator();
      Class cls = javaMate.getClass();

      while(i.hasNext()) {
         Map.Entry e = (Map.Entry)i.next();
         String key = (String)e.getKey();
         Field field = MetaUtils.getField(cls, key);
         Object rhs = e.getValue();
         if (field != null) {
            this.assignField(stack, jsonObj, field, rhs);
         } else if (this.missingFieldHandler != null) {
            this.handleMissingField(stack, jsonObj, rhs, key);
         }
      }

   }

   protected void assignField(Deque stack, JsonObject jsonObj, Field field, Object rhs) {
      Object target = jsonObj.target;

      try {
         Class fieldType = field.getType();
         if (rhs == null) {
            if (fieldType.isPrimitive()) {
               field.set(target, MetaUtils.convert(fieldType, "0"));
            } else {
               field.set(target, (Object)null);
            }

         } else {
            if (rhs instanceof JsonObject) {
               if (field.getGenericType() instanceof ParameterizedType) {
                  this.markUntypedObjects(field.getGenericType(), rhs, MetaUtils.getDeepDeclaredFields(fieldType));
               }

               JsonObject job = (JsonObject)rhs;
               String type = job.type;
               if (type == null || type.isEmpty()) {
                  job.setType(fieldType.getName());
               }
            }

            JsonObject jObj;
            if (rhs == "~!o~") {
               jObj = new JsonObject();
               jObj.type = fieldType.getName();
               Object value = this.createJavaObjectInstance(fieldType, jObj);
               field.set(target, value);
            } else {
               Object special;
               if ((special = this.readIfMatching(rhs, fieldType, stack)) != null) {
                  field.set(target, special);
               } else if (rhs.getClass().isArray()) {
                  Object[] elements = (Object[])rhs;
                  JsonObject jsonArray = new JsonObject();
                  if (char[].class == fieldType) {
                     if (elements.length == 0) {
                        field.set(target, new char[0]);
                     } else {
                        field.set(target, ((String)elements[0]).toCharArray());
                     }
                  } else {
                     jsonArray.put("@items", elements);
                     this.createJavaObjectInstance(fieldType, jsonArray);
                     field.set(target, jsonArray.target);
                     stack.addFirst(jsonArray);
                  }
               } else if (rhs instanceof JsonObject) {
                  jObj = (JsonObject)rhs;
                  Long ref = jObj.getReferenceId();
                  if (ref != null) {
                     JsonObject refObject = this.getReferencedObj(ref);
                     if (refObject.target != null) {
                        field.set(target, refObject.target);
                     } else {
                        this.unresolvedRefs.add(new Resolver.UnresolvedReference(jsonObj, field.getName(), ref));
                     }
                  } else {
                     field.set(target, this.createJavaObjectInstance(fieldType, jObj));
                     if (!MetaUtils.isLogicalPrimitive(jObj.getTargetClass())) {
                        stack.addFirst((JsonObject)rhs);
                     }
                  }
               } else if (MetaUtils.isPrimitive(fieldType)) {
                  field.set(target, MetaUtils.convert(fieldType, rhs));
               } else if (rhs instanceof String && "".equals(((String)rhs).trim()) && fieldType != String.class) {
                  field.set(target, (Object)null);
               } else {
                  field.set(target, rhs);
               }
            }

         }
      } catch (Exception var11) {
         String message = var11.getClass().getSimpleName() + " setting field '" + field.getName() + "' on target: " + safeToString(target) + " with value: " + rhs;
         if (MetaUtils.loadClassException != null) {
            message = message + " Caused by: " + MetaUtils.loadClassException + " (which created a LinkedHashMap instead of the desired class)";
         }

         throw new JsonIoException(message, var11);
      }
   }

   protected void handleMissingField(Deque stack, JsonObject jsonObj, Object rhs, String missingField) {
      Object target = jsonObj.target;

      try {
         if (rhs == null) {
            this.storeMissingField(target, missingField, (Object)null);
         } else {
            if (rhs == "~!o~") {
               this.storeMissingField(target, missingField, (Object)null);
            } else {
               Object special;
               if ((special = this.readIfMatching(rhs, (Class)null, stack)) != null) {
                  this.storeMissingField(target, missingField, special);
               } else if (rhs.getClass().isArray()) {
                  this.storeMissingField(target, missingField, (Object)null);
               } else if (rhs instanceof JsonObject) {
                  JsonObject jObj = (JsonObject)rhs;
                  Long ref = jObj.getReferenceId();
                  if (ref != null) {
                     JsonObject refObject = this.getReferencedObj(ref);
                     this.storeMissingField(target, missingField, refObject.target);
                  } else if (jObj.getType() != null) {
                     Object createJavaObjectInstance = this.createJavaObjectInstance((Class)null, jObj);
                     if (!MetaUtils.isLogicalPrimitive(jObj.getTargetClass())) {
                        stack.addFirst((JsonObject)rhs);
                     }

                     this.storeMissingField(target, missingField, createJavaObjectInstance);
                  } else {
                     this.storeMissingField(target, missingField, (Object)null);
                  }
               } else {
                  this.storeMissingField(target, missingField, rhs);
               }
            }

         }
      } catch (Exception var10) {
         String message = var10.getClass().getSimpleName() + " missing field '" + missingField + "' on target: " + safeToString(target) + " with value: " + rhs;
         if (MetaUtils.loadClassException != null) {
            message = message + " Caused by: " + MetaUtils.loadClassException + " (which created a LinkedHashMap instead of the desired class)";
         }

         throw new JsonIoException(message, var10);
      }
   }

   private void storeMissingField(Object target, String missingField, Object value) {
      this.missingFields.add(new Resolver.Missingfields(target, missingField, value));
   }

   private static String safeToString(Object o) {
      if (o == null) {
         return "null";
      } else {
         try {
            return o.toString();
         } catch (Exception var1) {
            return o.getClass().toString();
         }
      }
   }

   protected void traverseCollection(Deque stack, JsonObject jsonObj) {
      Object[] items = jsonObj.getArray();
      if (items != null && items.length != 0) {
         Collection col = (Collection)jsonObj.target;
         boolean isList = col instanceof List;
         int idx = 0;
         Object[] var10 = items;
         int var9 = items.length;

         for(int var8 = 0; var8 < var9; ++var8) {
            Object element = var10[var8];
            if (element == null) {
               col.add((Object)null);
            } else if (element == "~!o~") {
               col.add(new JsonObject());
            } else {
               Object special;
               if ((special = this.readIfMatching(element, (Class)null, stack)) != null) {
                  col.add(special);
               } else if (!(element instanceof String) && !(element instanceof Boolean) && !(element instanceof Double) && !(element instanceof Long)) {
                  JsonObject jObj;
                  if (element.getClass().isArray()) {
                     jObj = new JsonObject();
                     jObj.put("@items", element);
                     this.createJavaObjectInstance(Object.class, jObj);
                     col.add(jObj.target);
                     this.convertMapsToObjects(jObj);
                  } else {
                     jObj = (JsonObject)element;
                     Long ref = jObj.getReferenceId();
                     if (ref != null) {
                        JsonObject refObject = this.getReferencedObj(ref);
                        if (refObject.target != null) {
                           col.add(refObject.target);
                        } else {
                           this.unresolvedRefs.add(new Resolver.UnresolvedReference(jsonObj, idx, ref));
                           if (isList) {
                              col.add((Object)null);
                           }
                        }
                     } else {
                        this.createJavaObjectInstance(Object.class, jObj);
                        if (!MetaUtils.isLogicalPrimitive(jObj.getTargetClass())) {
                           this.convertMapsToObjects(jObj);
                        }

                        col.add(jObj.target);
                     }
                  }
               } else {
                  col.add(element);
               }
            }

            ++idx;
         }

         jsonObj.remove("@items");
      }
   }

   protected void traverseArray(Deque stack, JsonObject jsonObj) {
      int len = jsonObj.getLength();
      if (len != 0) {
         Class compType = jsonObj.getComponentType();
         if (Character.TYPE != compType) {
            if (Byte.TYPE == compType) {
               jsonObj.moveBytesToMate();
               jsonObj.clearArray();
            } else {
               boolean isPrimitive = MetaUtils.isPrimitive(compType);
               Object array = jsonObj.target;
               Object[] items = jsonObj.getArray();

               for(int i = 0; i < len; ++i) {
                  Object element = items[i];
                  if (element == null) {
                     Array.set(array, i, (Object)null);
                  } else if (element == "~!o~") {
                     Object arrayElement = this.createJavaObjectInstance(compType, new JsonObject());
                     Array.set(array, i, arrayElement);
                  } else {
                     Object special;
                     if ((special = this.readIfMatching(element, compType, stack)) != null) {
                        Array.set(array, i, special);
                     } else if (isPrimitive) {
                        Array.set(array, i, MetaUtils.convert(compType, element));
                     } else {
                        JsonObject jsonObject;
                        if (!element.getClass().isArray()) {
                           if (element instanceof JsonObject) {
                              jsonObject = (JsonObject)element;
                              Long ref = jsonObject.getReferenceId();
                              if (ref != null) {
                                 JsonObject refObject = this.getReferencedObj(ref);
                                 if (refObject.target != null) {
                                    Array.set(array, i, refObject.target);
                                 } else {
                                    this.unresolvedRefs.add(new Resolver.UnresolvedReference(jsonObj, i, ref));
                                 }
                              } else {
                                 Object arrayElement = this.createJavaObjectInstance(compType, jsonObject);
                                 Array.set(array, i, arrayElement);
                                 if (!MetaUtils.isLogicalPrimitive(arrayElement.getClass())) {
                                    stack.addFirst(jsonObject);
                                 }
                              }
                           } else if (element instanceof String && "".equals(((String)element).trim()) && compType != String.class && compType != Object.class) {
                              Array.set(array, i, (Object)null);
                           } else {
                              Array.set(array, i, element);
                           }
                        } else if (char[].class != compType) {
                           jsonObject = new JsonObject();
                           jsonObject.put("@items", element);
                           Array.set(array, i, this.createJavaObjectInstance(compType, jsonObject));
                           stack.addFirst(jsonObject);
                        } else {
                           Object[] jsonArray = (Object[])element;
                           if (jsonArray.length == 0) {
                              Array.set(array, i, new char[0]);
                           } else {
                              String value = (String)jsonArray[0];
                              int numChars = value.length();
                              char[] chars = new char[numChars];

                              for(int j = 0; j < numChars; ++j) {
                                 chars[j] = value.charAt(j);
                              }

                              Array.set(array, i, chars);
                           }
                        }
                     }
                  }
               }

               jsonObj.clearArray();
            }
         }
      }
   }

   protected Object readIfMatching(Object o, Class compType, Deque stack) {
      if (o == null) {
         throw new JsonIoException("Bug in json-io, null must be checked before calling this method.");
      } else if (compType != null && this.notCustom(compType)) {
         return null;
      } else {
         boolean isJsonObject = o instanceof JsonObject;
         if (!isJsonObject && compType == null) {
            return null;
         } else {
            boolean needsType = false;
            Class c;
            if (isJsonObject) {
               JsonObject jObj = (JsonObject)o;
               if (jObj.isReference()) {
                  return null;
               }

               if (jObj.target == null) {
                  String typeStr = null;

                  try {
                     Object type = jObj.type;
                     if (type != null) {
                        typeStr = (String)type;
                        c = MetaUtils.classForName((String)type, this.classLoader);
                     } else {
                        if (compType == null) {
                           return null;
                        }

                        c = compType;
                        needsType = true;
                     }

                     this.createJavaObjectInstance(c, jObj);
                  } catch (Exception var10) {
                     throw new JsonIoException("Class listed in @type [" + typeStr + "] is not found", var10);
                  }
               } else {
                  c = jObj.target.getClass();
               }
            } else {
               c = compType;
            }

            if (this.notCustom(c)) {
               return null;
            } else {
               JsonReader.JsonClassReaderBase closestReader = this.getCustomReader(c);
               if (closestReader == null) {
                  return null;
               } else {
                  if (needsType) {
                     ((JsonObject)o).setType(c.getName());
                  }

                  Object read;
                  if (closestReader instanceof JsonReader.JsonClassReaderEx) {
                     read = ((JsonReader.JsonClassReaderEx)closestReader).read(o, stack, this.getReader().getArgs());
                  } else {
                     read = ((JsonReader.JsonClassReader)closestReader).read(o, stack);
                  }

                  return read;
               }
            }
         }
      }
   }

   private void markUntypedObjects(Type type, Object rhs, Map classFields) {
      Deque stack = new ArrayDeque();
      stack.addFirst(new Object[]{type, rhs});

      while(true) {
         label121:
         while(true) {
            Type t;
            Object instance;
            Class clazz;
            Type[] typeArgs;
            do {
               do {
                  label66:
                  do {
                     while(!stack.isEmpty()) {
                        Object[] item = (Object[])stack.removeFirst();
                        t = (Type)item[0];
                        instance = item[1];
                        if (t instanceof ParameterizedType) {
                           clazz = getRawType(t);
                           ParameterizedType pType = (ParameterizedType)t;
                           typeArgs = pType.getActualTypeArguments();
                           continue label66;
                        }

                        stampTypeOnJsonObject(instance, t);
                     }

                     return;
                  } while(typeArgs == null);
               } while(typeArgs.length < 1);
            } while(clazz == null);

            stampTypeOnJsonObject(instance, t);
            Object[] array;
            if (Map.class.isAssignableFrom(clazz)) {
               Map map = (Map)instance;
               if (!map.containsKey("@keys") && !map.containsKey("@items") && map instanceof JsonObject) {
                  convertMapToKeysItems((JsonObject)map);
               }

               array = (Object[])map.get("@keys");
               getTemplateTraverseWorkItem(stack, array, typeArgs[0]);
               Object[] items = (Object[])map.get("@items");
               getTemplateTraverseWorkItem(stack, items, typeArgs[1]);
            } else {
               JsonObject jObj;
               Iterator var13;
               if (Collection.class.isAssignableFrom(clazz)) {
                  Object o;
                  if (instance instanceof Object[]) {
                     Object[] array = (Object[])instance;

                     for(int i = 0; i < array.length; ++i) {
                        o = array[i];
                        stack.addFirst(new Object[]{t, o});
                        if (o instanceof JsonObject) {
                           stack.addFirst(new Object[]{t, o});
                        } else if (o instanceof Object[]) {
                           JsonObject coll = new JsonObject();
                           coll.type = clazz.getName();
                           List items = Arrays.asList((Object[])o);
                           coll.put("@items", items.toArray());
                           stack.addFirst(new Object[]{t, items});
                           array[i] = coll;
                        } else {
                           stack.addFirst(new Object[]{t, o});
                        }
                     }
                  } else if (instance instanceof Collection) {
                     Collection col = (Collection)instance;
                     var13 = col.iterator();

                     while(var13.hasNext()) {
                        Object o = var13.next();
                        stack.addFirst(new Object[]{typeArgs[0], o});
                     }
                  } else if (instance instanceof JsonObject) {
                     jObj = (JsonObject)instance;
                     array = jObj.getArray();
                     if (array != null) {
                        Object[] var16 = array;
                        int var27 = array.length;

                        for(int var25 = 0; var25 < var27; ++var25) {
                           o = var16[var25];
                           stack.addFirst(new Object[]{typeArgs[0], o});
                        }
                     }
                  }
               } else if (instance instanceof JsonObject) {
                  jObj = (JsonObject)instance;
                  var13 = jObj.entrySet().iterator();

                  while(true) {
                     Map.Entry entry;
                     Field field;
                     do {
                        do {
                           String fieldName;
                           do {
                              if (!var13.hasNext()) {
                                 continue label121;
                              }

                              entry = (Map.Entry)var13.next();
                              fieldName = (String)entry.getKey();
                           } while(fieldName.startsWith("this$"));

                           field = (Field)classFields.get(fieldName);
                        } while(field == null);
                     } while(field.getType().getTypeParameters().length <= 0 && !(field.getGenericType() instanceof TypeVariable));

                     stack.addFirst(new Object[]{typeArgs[0], entry.getValue()});
                  }
               }
            }
         }
      }
   }

   private static void getTemplateTraverseWorkItem(Deque stack, Object[] items, Type type) {
      if (items != null && items.length >= 1) {
         Class rawType = getRawType(type);
         if (rawType != null && Collection.class.isAssignableFrom(rawType)) {
            stack.add(new Object[]{type, items});
         } else {
            Object[] var7 = items;
            int var6 = items.length;

            for(int var5 = 0; var5 < var6; ++var5) {
               Object o = var7[var5];
               stack.add(new Object[]{type, o});
            }
         }

      }
   }

   private static void stampTypeOnJsonObject(Object o, Type t) {
      Class clazz = t instanceof Class ? (Class)t : getRawType(t);
      if (o instanceof JsonObject && clazz != null) {
         JsonObject jObj = (JsonObject)o;
         if ((jObj.type == null || jObj.type.isEmpty()) && jObj.target == null) {
            jObj.type = clazz.getName();
         }
      }

   }

   public static Class getRawType(Type t) {
      if (t instanceof ParameterizedType) {
         ParameterizedType pType = (ParameterizedType)t;
         if (pType.getRawType() instanceof Class) {
            return (Class)pType.getRawType();
         }
      }

      return null;
   }
}
