package org.apache.batik.util;

public class DoublyIndexedSet {
   protected DoublyIndexedTable table = new DoublyIndexedTable();
   protected static Object value = new Object();

   public int size() {
      return this.table.size();
   }

   public void add(Object o1, Object o2) {
      this.table.put(o1, o2, value);
   }

   public void remove(Object o1, Object o2) {
      this.table.remove(o1, o2);
   }

   public boolean contains(Object o1, Object o2) {
      return this.table.get(o1, o2) != null;
   }

   public void clear() {
      this.table.clear();
   }
}
