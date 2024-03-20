package org.apache.batik.transcoder;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TranscodingHints extends HashMap {
   public TranscodingHints() {
      this((Map)null);
   }

   public TranscodingHints(Map init) {
      super(7);
      if (init != null) {
         this.putAll(init);
      }

   }

   public boolean containsKey(Object key) {
      return super.containsKey(key);
   }

   public Object get(Object key) {
      return super.get(key);
   }

   public Object put(Object key, Object value) {
      if (!((Key)key).isCompatibleValue(value)) {
         throw new IllegalArgumentException(value + " incompatible with " + key);
      } else {
         return super.put(key, value);
      }
   }

   public Object remove(Object key) {
      return super.remove(key);
   }

   public void putAll(TranscodingHints hints) {
      super.putAll(hints);
   }

   public void putAll(Map m) {
      if (m instanceof TranscodingHints) {
         this.putAll((TranscodingHints)m);
      } else {
         Iterator var2 = m.entrySet().iterator();

         while(var2.hasNext()) {
            Object o = var2.next();
            Map.Entry entry = (Map.Entry)o;
            this.put(entry.getKey(), entry.getValue());
         }
      }

   }

   public abstract static class Key {
      protected Key() {
      }

      public abstract boolean isCompatibleValue(Object var1);
   }
}
