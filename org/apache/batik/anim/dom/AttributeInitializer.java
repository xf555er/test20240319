package org.apache.batik.anim.dom;

import org.apache.batik.util.DoublyIndexedTable;

public class AttributeInitializer {
   protected String[] keys;
   protected int length;
   protected DoublyIndexedTable values = new DoublyIndexedTable();

   public AttributeInitializer(int capacity) {
      this.keys = new String[capacity * 3];
   }

   public void addAttribute(String ns, String prefix, String ln, String val) {
      int len = this.keys.length;
      if (this.length == len) {
         String[] t = new String[len * 2];
         System.arraycopy(this.keys, 0, t, 0, len);
         this.keys = t;
      }

      this.keys[this.length++] = ns;
      this.keys[this.length++] = prefix;
      this.keys[this.length++] = ln;
      this.values.put(ns, ln, val);
   }

   public void initializeAttributes(AbstractElement elt) {
      for(int i = this.length - 1; i >= 2; i -= 3) {
         this.resetAttribute(elt, this.keys[i - 2], this.keys[i - 1], this.keys[i]);
      }

   }

   public boolean resetAttribute(AbstractElement elt, String ns, String prefix, String ln) {
      String val = (String)this.values.get(ns, ln);
      if (val == null) {
         return false;
      } else {
         if (prefix != null) {
            ln = prefix + ':' + ln;
         }

         elt.setUnspecifiedAttribute(ns, ln, val);
         return true;
      }
   }
}
