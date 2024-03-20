package org.apache.batik.css.parser;

import org.w3c.css.sac.SACMediaList;

public class CSSSACMediaList implements SACMediaList {
   protected String[] list = new String[3];
   protected int length;

   public int getLength() {
      return this.length;
   }

   public String item(int index) {
      return index >= 0 && index < this.length ? this.list[index] : null;
   }

   public void append(String item) {
      if (this.length == this.list.length) {
         String[] tmp = this.list;
         this.list = new String[1 + this.list.length + this.list.length / 2];
         System.arraycopy(tmp, 0, this.list, 0, tmp.length);
      }

      this.list[this.length++] = item;
   }
}
