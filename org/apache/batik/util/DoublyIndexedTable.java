package org.apache.batik.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class DoublyIndexedTable {
   protected int initialCapacity;
   protected Entry[] table;
   protected int count;

   public DoublyIndexedTable() {
      this(16);
   }

   public DoublyIndexedTable(int c) {
      this.initialCapacity = c;
      this.table = new Entry[c];
   }

   public DoublyIndexedTable(DoublyIndexedTable other) {
      this.initialCapacity = other.initialCapacity;
      this.table = new Entry[other.table.length];

      for(int i = 0; i < other.table.length; ++i) {
         Entry newE = null;

         for(Entry e = other.table[i]; e != null; e = e.next) {
            newE = new Entry(e.hash, e.key1, e.key2, e.value, newE);
         }

         this.table[i] = newE;
      }

      this.count = other.count;
   }

   public int size() {
      return this.count;
   }

   public Object put(Object o1, Object o2, Object value) {
      int hash = this.hashCode(o1, o2) & Integer.MAX_VALUE;
      int index = hash % this.table.length;

      for(Entry e = this.table[index]; e != null; e = e.next) {
         if (e.hash == hash && e.match(o1, o2)) {
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

      Entry e = new Entry(hash, o1, o2, value, this.table[index]);
      this.table[index] = e;
      return null;
   }

   public Object get(Object o1, Object o2) {
      int hash = this.hashCode(o1, o2) & Integer.MAX_VALUE;
      int index = hash % this.table.length;

      for(Entry e = this.table[index]; e != null; e = e.next) {
         if (e.hash == hash && e.match(o1, o2)) {
            return e.value;
         }
      }

      return null;
   }

   public Object remove(Object o1, Object o2) {
      int hash = this.hashCode(o1, o2) & Integer.MAX_VALUE;
      int index = hash % this.table.length;
      Entry e = this.table[index];
      if (e == null) {
         return null;
      } else if (e.hash == hash && e.match(o1, o2)) {
         this.table[index] = e.next;
         --this.count;
         return e.value;
      } else {
         Entry prev = e;

         for(e = e.next; e != null; e = e.next) {
            if (e.hash == hash && e.match(o1, o2)) {
               prev.next = e.next;
               --this.count;
               return e.value;
            }

            prev = e;
         }

         return null;
      }
   }

   public Object[] getValuesArray() {
      Object[] values = new Object[this.count];
      int i = 0;
      Entry[] var3 = this.table;
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         Entry aTable = var3[var5];

         for(Entry e = aTable; e != null; e = e.next) {
            values[i++] = e.value;
         }
      }

      return values;
   }

   public void clear() {
      this.table = new Entry[this.initialCapacity];
      this.count = 0;
   }

   public Iterator iterator() {
      return new TableIterator();
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

   protected class TableIterator implements Iterator {
      private int nextIndex;
      private Entry nextEntry;
      private boolean finished;

      public TableIterator() {
         while(this.nextIndex < DoublyIndexedTable.this.table.length) {
            this.nextEntry = DoublyIndexedTable.this.table[this.nextIndex];
            if (this.nextEntry != null) {
               break;
            }

            ++this.nextIndex;
         }

         this.finished = this.nextEntry == null;
      }

      public boolean hasNext() {
         return !this.finished;
      }

      public Object next() {
         if (this.finished) {
            throw new NoSuchElementException();
         } else {
            Entry ret = this.nextEntry;
            this.findNext();
            return ret;
         }
      }

      protected void findNext() {
         this.nextEntry = this.nextEntry.next;
         if (this.nextEntry == null) {
            ++this.nextIndex;

            while(this.nextIndex < DoublyIndexedTable.this.table.length) {
               this.nextEntry = DoublyIndexedTable.this.table[this.nextIndex];
               if (this.nextEntry != null) {
                  break;
               }

               ++this.nextIndex;
            }
         }

         this.finished = this.nextEntry == null;
      }

      public void remove() {
         throw new UnsupportedOperationException();
      }
   }

   public static class Entry {
      protected int hash;
      protected Object key1;
      protected Object key2;
      protected Object value;
      protected Entry next;

      public Entry(int hash, Object key1, Object key2, Object value, Entry next) {
         this.hash = hash;
         this.key1 = key1;
         this.key2 = key2;
         this.value = value;
         this.next = next;
      }

      public Object getKey1() {
         return this.key1;
      }

      public Object getKey2() {
         return this.key2;
      }

      public Object getValue() {
         return this.value;
      }

      protected boolean match(Object o1, Object o2) {
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
