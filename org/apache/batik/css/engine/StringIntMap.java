package org.apache.batik.css.engine;

public class StringIntMap {
   protected Entry[] table;
   protected int count;

   public StringIntMap(int c) {
      this.table = new Entry[c - (c >> 2) + 1];
   }

   public int get(String key) {
      int hash = key.hashCode() & Integer.MAX_VALUE;
      int index = hash % this.table.length;

      for(Entry e = this.table[index]; e != null; e = e.next) {
         if (e.hash == hash && e.key.equals(key)) {
            return e.value;
         }
      }

      return -1;
   }

   public void put(String key, int value) {
      int hash = key.hashCode() & Integer.MAX_VALUE;
      int index = hash % this.table.length;

      for(Entry e = this.table[index]; e != null; e = e.next) {
         if (e.hash == hash && e.key.equals(key)) {
            e.value = value;
            return;
         }
      }

      int len = this.table.length;
      if (this.count++ >= len - (len >> 2)) {
         this.rehash();
         index = hash % this.table.length;
      }

      Entry e = new Entry(hash, key, value, this.table[index]);
      this.table[index] = e;
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
      public final int hash;
      public String key;
      public int value;
      public Entry next;

      public Entry(int hash, String key, int value, Entry next) {
         this.hash = hash;
         this.key = key;
         this.value = value;
         this.next = next;
      }
   }
}
