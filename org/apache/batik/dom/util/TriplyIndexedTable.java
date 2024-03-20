package org.apache.batik.dom.util;

public class TriplyIndexedTable {
   protected static final int INITIAL_CAPACITY = 11;
   protected Entry[] table;
   protected int count;

   public TriplyIndexedTable() {
      this.table = new Entry[11];
   }

   public TriplyIndexedTable(int c) {
      this.table = new Entry[c];
   }

   public int size() {
      return this.count;
   }

   public Object put(Object o1, Object o2, Object o3, Object value) {
      int hash = this.hashCode(o1, o2, o3) & Integer.MAX_VALUE;
      int index = hash % this.table.length;

      for(Entry e = this.table[index]; e != null; e = e.next) {
         if (e.hash == hash && e.match(o1, o2, o3)) {
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

      Entry e = new Entry(hash, o1, o2, o3, value, this.table[index]);
      this.table[index] = e;
      return null;
   }

   public Object get(Object o1, Object o2, Object o3) {
      int hash = this.hashCode(o1, o2, o3) & Integer.MAX_VALUE;
      int index = hash % this.table.length;

      for(Entry e = this.table[index]; e != null; e = e.next) {
         if (e.hash == hash && e.match(o1, o2, o3)) {
            return e.value;
         }
      }

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

   protected int hashCode(Object o1, Object o2, Object o3) {
      return (o1 == null ? 0 : o1.hashCode()) ^ (o2 == null ? 0 : o2.hashCode()) ^ (o3 == null ? 0 : o3.hashCode());
   }

   protected static class Entry {
      public int hash;
      public Object key1;
      public Object key2;
      public Object key3;
      public Object value;
      public Entry next;

      public Entry(int hash, Object key1, Object key2, Object key3, Object value, Entry next) {
         this.hash = hash;
         this.key1 = key1;
         this.key2 = key2;
         this.key3 = key3;
         this.value = value;
         this.next = next;
      }

      public boolean match(Object o1, Object o2, Object o3) {
         if (this.key1 != null) {
            if (!this.key1.equals(o1)) {
               return false;
            }
         } else if (o1 != null) {
            return false;
         }

         if (this.key2 != null) {
            if (!this.key2.equals(o2)) {
               return false;
            }
         } else if (o2 != null) {
            return false;
         }

         if (this.key3 != null) {
            return this.key3.equals(o3);
         } else {
            return o3 == null;
         }
      }
   }
}
