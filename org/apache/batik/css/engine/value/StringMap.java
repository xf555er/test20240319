package org.apache.batik.css.engine.value;

public class StringMap {
   protected static final int INITIAL_CAPACITY = 11;
   protected Entry[] table;
   protected int count;

   public StringMap() {
      this.table = new Entry[11];
   }

   public StringMap(StringMap t) {
      this.count = t.count;
      this.table = new Entry[t.table.length];

      for(int i = 0; i < this.table.length; ++i) {
         Entry e = t.table[i];
         Entry n = null;
         if (e != null) {
            n = new Entry(e.hash, e.key, e.value, (Entry)null);
            this.table[i] = n;

            for(e = e.next; e != null; e = e.next) {
               n.next = new Entry(e.hash, e.key, e.value, (Entry)null);
               n = n.next;
            }
         }
      }

   }

   public Object get(String key) {
      int hash = key.hashCode() & Integer.MAX_VALUE;
      int index = hash % this.table.length;

      for(Entry e = this.table[index]; e != null; e = e.next) {
         if (e.hash == hash && e.key == key) {
            return e.value;
         }
      }

      return null;
   }

   public Object put(String key, Object value) {
      int hash = key.hashCode() & Integer.MAX_VALUE;
      int index = hash % this.table.length;

      for(Entry e = this.table[index]; e != null; e = e.next) {
         if (e.hash == hash && e.key == key) {
            Object old = e.value;
            e.value = value;
            return old;
         }
      }

      int len = this.table.length;
      if (this.count++ >= len - (len >> 2)) {
         this.rehash();
         index = hash % this.table.length;
      }

      Entry e = new Entry(hash, key, value, this.table[index]);
      this.table[index] = e;
      return null;
   }

   protected void rehash() {
      Entry[] oldTable = this.table;
      this.table = new Entry[oldTable.length * 2 + 1];

      Entry e;
      int index;
      for(int i = oldTable.length - 1; i >= 0; --i) {
         for(Entry old = oldTable[i]; old != null; this.table[index] = e) {
            e = old;
            old = old.next;
            index = e.hash % this.table.length;
            e.next = this.table[index];
         }
      }

   }

   protected static class Entry {
      public int hash;
      public String key;
      public Object value;
      public Entry next;

      public Entry(int hash, String key, Object value, Entry next) {
         this.hash = hash;
         this.key = key;
         this.value = value;
         this.next = next;
      }
   }
}
