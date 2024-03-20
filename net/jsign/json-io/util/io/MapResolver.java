package net.jsign.json-io.util.io;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MapResolver extends Resolver {
   protected MapResolver(JsonReader reader) {
      super(reader);
   }

   protected Object readIfMatching(Object o, Class compType, Deque stack) {
      return null;
   }

   public void traverseFields(Deque stack, JsonObject jsonObj) {
      Object target = jsonObj.target;
      Iterator var5 = jsonObj.entrySet().iterator();

      while(true) {
         while(var5.hasNext()) {
            Map.Entry e = (Map.Entry)var5.next();
            String fieldName = (String)e.getKey();
            Field field = target != null ? MetaUtils.getField(target.getClass(), fieldName) : null;
            Object rhs = e.getValue();
            if (rhs == null) {
               jsonObj.put(fieldName, (Object)null);
            } else if (rhs == "~!o~") {
               jsonObj.put(fieldName, new JsonObject());
            } else {
               JsonObject jObj;
               if (rhs.getClass().isArray()) {
                  jObj = new JsonObject();
                  jObj.put("@items", rhs);
                  stack.addFirst(jObj);
                  jsonObj.put(fieldName, rhs);
               } else if (rhs instanceof JsonObject) {
                  jObj = (JsonObject)rhs;
                  if (field != null && MetaUtils.isLogicalPrimitive(field.getType())) {
                     jObj.put("value", MetaUtils.convert(field.getType(), jObj.get("value")));
                  } else {
                     Long refId = jObj.getReferenceId();
                     if (refId != null) {
                        JsonObject refObject = this.getReferencedObj(refId);
                        jsonObj.put(fieldName, refObject);
                     } else {
                        stack.addFirst(jObj);
                     }
                  }
               } else if (field != null) {
                  Class fieldType = field.getType();
                  if (!MetaUtils.isPrimitive(fieldType) && !BigDecimal.class.equals(fieldType) && !BigInteger.class.equals(fieldType) && !Date.class.equals(fieldType)) {
                     if (rhs instanceof String && fieldType != String.class && fieldType != StringBuilder.class && fieldType != StringBuffer.class && "".equals(((String)rhs).trim())) {
                        jsonObj.put(fieldName, (Object)null);
                     }
                  } else {
                     jsonObj.put(fieldName, MetaUtils.convert(fieldType, rhs));
                  }
               }
            }
         }

         jsonObj.target = null;
         return;
      }
   }

   protected void traverseCollection(Deque stack, JsonObject jsonObj) {
      Object[] items = jsonObj.getArray();
      if (items != null && items.length != 0) {
         int idx = 0;
         List copy = new ArrayList(items.length);
         Object[] var9 = items;
         int var8 = items.length;

         for(int var7 = 0; var7 < var8; ++var7) {
            Object element = var9[var7];
            if (element == "~!o~") {
               copy.add(new JsonObject());
            } else {
               copy.add(element);
               JsonObject jsonObject;
               if (element instanceof Object[]) {
                  jsonObject = new JsonObject();
                  jsonObject.put("@items", element);
                  stack.addFirst(jsonObject);
               } else if (element instanceof JsonObject) {
                  jsonObject = (JsonObject)element;
                  Long refId = jsonObject.getReferenceId();
                  if (refId != null) {
                     JsonObject refObject = this.getReferencedObj(refId);
                     copy.set(idx, refObject);
                  } else {
                     stack.addFirst(jsonObject);
                  }
               }

               ++idx;
            }
         }

         jsonObj.target = null;

         for(int i = 0; i < items.length; ++i) {
            items[i] = copy.get(i);
         }

      }
   }

   protected void traverseArray(Deque stack, JsonObject jsonObj) {
      this.traverseCollection(stack, jsonObj);
   }
}
