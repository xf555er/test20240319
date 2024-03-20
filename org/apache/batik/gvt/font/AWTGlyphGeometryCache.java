package org.apache.batik.gvt.font;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;

public class AWTGlyphGeometryCache {
   protected static final int INITIAL_CAPACITY = 71;
   protected Entry[] table;
   protected int count;
   protected ReferenceQueue referenceQueue = new ReferenceQueue();

   public AWTGlyphGeometryCache() {
      this.table = new Entry[71];
   }

   public AWTGlyphGeometryCache(int c) {
      this.table = new Entry[c];
   }

   public int size() {
      return this.count;
   }

   public Value get(char c) {
      int hash = this.hashCode(c) & Integer.MAX_VALUE;
      int index = hash % this.table.length;

      for(Entry e = this.table[index]; e != null; e = e.next) {
         if (e.hash == hash && e.match(c)) {
            return (Value)e.get();
         }
      }

      return null;
   }

   public Value put(char c, Value value) {
      this.removeClearedEntries();
      int hash = this.hashCode(c) & Integer.MAX_VALUE;
      int index = hash % this.table.length;
      Entry e = this.table[index];
      if (e != null) {
         if (e.hash == hash && e.match(c)) {
            Object old = e.get();
            this.table[index] = new Entry(hash, c, value, e.next);
            return (Value)old;
         }

         Entry o = e;

         for(e = e.next; e != null; e = e.next) {
            if (e.hash == hash && e.match(c)) {
               Object old = e.get();
               e = new Entry(hash, c, value, e.next);
               o.next = e;
               return (Value)old;
            }

            o = e;
         }
      }

      int len = this.table.length;
      if (this.count++ >= len - (len >> 2)) {
         this.rehash();
         index = hash % this.table.length;
      }

      this.table[index] = new Entry(hash, c, value, this.table[index]);
      return null;
   }

   public void clear() {
      this.table = new Entry[71];
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

   protected int hashCode(char c) {
      return c;
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
      public char c;
      public Entry next;

      public Entry(int hash, char c, Value value, Entry next) {
         super(value, AWTGlyphGeometryCache.this.referenceQueue);
         this.hash = hash;
         this.c = c;
         this.next = next;
      }

      public boolean match(char o2) {
         return this.c == o2;
      }
   }

   public static class Value {
      protected Shape outline;
      protected Rectangle2D gmB;
      protected Rectangle2D outlineBounds;

      public Value(Shape outline, Rectangle2D gmB) {
         this.outline = outline;
         this.outlineBounds = outline.getBounds2D();
         this.gmB = gmB;
      }

      public Shape getOutline() {
         return this.outline;
      }

      public Rectangle2D getBounds2D() {
         return this.gmB;
      }

      public Rectangle2D getOutlineBounds2D() {
         return this.outlineBounds;
      }
   }
}
