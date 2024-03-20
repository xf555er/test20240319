package org.apache.batik.util;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;

public class SoftDoublyIndexedTable {
   protected static final int INITIAL_CAPACITY = 11;
   protected Entry[] table;
   protected int count;
   protected ReferenceQueue referenceQueue = new ReferenceQueue();

   public SoftDoublyIndexedTable() {
      this.table = new Entry[11];
   }

   public SoftDoublyIndexedTable(int c) {
      this.table = new Entry[c];
   }

   public int size() {
      return this.count;
   }

   public Object get(Object o1, Object o2) {
      int hash = this.hashCode(o1, o2) & Integer.MAX_VALUE;
      int index = hash % this.table.length;

      for(Entry e = this.table[index]; e != null; e = e.next) {
         if (e.hash == hash && e.match(o1, o2)) {
            return e.get();
         }
      }

      return null;
   }

   public Object put(Object o1, Object o2, Object value) {
      this.removeClearedEntries();
      int hash = this.hashCode(o1, o2) & Integer.MAX_VALUE;
      int index = hash % this.table.length;
      Entry e = this.table[index];
      if (e != null) {
         if (e.hash == hash && e.match(o1, o2)) {
            Object old = e.get();
            this.table[index] = new Entry(hash, o1, o2, value, e.next);
            return old;
         }

         Entry o = e;

         for(e = e.next; e != null; e = e.next) {
            if (e.hash == hash && e.match(o1, o2)) {
               Object old = e.get();
               e = new Entry(hash, o1, o2, value, e.next);
               o.next = e;
               return old;
            }

            o = e;
         }
      }

      int len = this.table.length;
      if (this.count++ >= len - (len >> 2)) {
         this.rehash();
         index = hash % this.table.length;
      }

      this.table[index] = new Entry(hash, o1, o2, value, this.table[index]);
      return null;
   }

   public void clear() {
      this.table = new Entry[11];
      this.count = 0;
      this.referenceQueue = new ReferenceQueue();
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

   protected int hashCode(Object o1, Object o2) {
      int result = o1 == null ? 0 : o1.hashCode();
      return result ^ (o2 == null ? 0 : o2.hashCode());
   }

   protected void removeClearedEntries() {
      Entry e;
      for(; (e = (Entry)this.referenceQueue.poll()) != null; --this.count) {
         int index = e.hash % this.table.length;
         Entry t = this.table[index];
         if (t == e) {
            this.table[index] = e.next;
         } else {
            while(t != null) {
               Entry c = t.next;
               if (c == e) {
                  t.next = e.next;
                  break;
               }

               t = c;
            }
         }
      }

   }

   protected class Entry extends SoftReference {
      public int hash;
      public Object key1;
      public Object key2;
      public Entry next;

      public Entry(int hash, Object key1, Object key2, Object value, Entry next) {
         super(value, SoftDoublyIndexedTable.this.referenceQueue);
         this.hash = hash;
         this.key1 = key1;
         this.key2 = key2;
         this.next = next;
      }

      public boolean match(Object o1, Object o2) {
         if (this.key1 != null) {
            if (!this.key1.equals(o1)) {
               return false;
            }
         } else if (o1 != null) {
            return false;
         }

         if (this.key2 != null) {
            return this.key2.equals(o2);
         } else {
            return o2 == null;
         }
      }
   }
}
