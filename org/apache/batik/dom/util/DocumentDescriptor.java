package org.apache.batik.dom.util;

import org.apache.batik.util.CleanerThread;
import org.w3c.dom.Element;

public class DocumentDescriptor {
   protected static final int INITIAL_CAPACITY = 101;
   protected Entry[] table = new Entry[101];
   protected int count;

   public int getNumberOfElements() {
      synchronized(this) {
         return this.count;
      }
   }

   public int getLocationLine(Element elt) {
      synchronized(this) {
         int hash = elt.hashCode() & Integer.MAX_VALUE;
         int index = hash % this.table.length;

         for(Entry e = this.table[index]; e != null; e = e.next) {
            if (e.hash == hash) {
               Object o = e.get();
               if (o == elt) {
                  return e.locationLine;
               }
            }
         }

         return 0;
      }
   }

   public int getLocationColumn(Element elt) {
      synchronized(this) {
         int hash = elt.hashCode() & Integer.MAX_VALUE;
         int index = hash % this.table.length;

         for(Entry e = this.table[index]; e != null; e = e.next) {
            if (e.hash == hash) {
               Object o = e.get();
               if (o == elt) {
                  return e.locationColumn;
               }
            }
         }

         return 0;
      }
   }

   public void setLocation(Element elt, int line, int col) {
      synchronized(this) {
         int hash = elt.hashCode() & Integer.MAX_VALUE;
         int index = hash % this.table.length;

         for(Entry e = this.table[index]; e != null; e = e.next) {
            if (e.hash == hash) {
               Object o = e.get();
               if (o == elt) {
                  e.locationLine = line;
               }
            }
         }

         int len = this.table.length;
         if (this.count++ >= len - (len >> 2)) {
            this.rehash();
            index = hash % this.table.length;
         }

         Entry e = new Entry(hash, elt, line, col, this.table[index]);
         this.table[index] = e;
      }
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

   protected void removeEntry(Entry e) {
      synchronized(this) {
         int hash = e.hash;
         int index = hash % this.table.length;
         Entry curr = this.table[index];

         Entry prev;
         for(prev = null; curr != e; curr = curr.next) {
            prev = curr;
         }

         if (curr != null) {
            if (prev == null) {
               this.table[index] = curr.next;
            } else {
               prev.next = curr.next;
            }

            --this.count;
         }
      }
   }

   protected class Entry extends CleanerThread.WeakReferenceCleared {
      public int hash;
      public int locationLine;
      public int locationColumn;
      public Entry next;

      public Entry(int hash, Element element, int locationLine, int locationColumn, Entry next) {
         super(element);
         this.hash = hash;
         this.locationLine = locationLine;
         this.locationColumn = locationColumn;
         this.next = next;
      }

      public void cleared() {
         DocumentDescriptor.this.removeEntry(this);
      }
   }
}
