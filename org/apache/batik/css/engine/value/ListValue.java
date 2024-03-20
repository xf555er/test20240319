package org.apache.batik.css.engine.value;

import org.w3c.dom.DOMException;

public class ListValue extends AbstractValue {
   protected int length;
   protected Value[] items = new Value[5];
   protected char separator = ',';

   public ListValue() {
   }

   public ListValue(char s) {
      this.separator = s;
   }

   public char getSeparatorChar() {
      return this.separator;
   }

   public short getCssValueType() {
      return 2;
   }

   public String getCssText() {
      StringBuffer sb = new StringBuffer(this.length * 8);
      if (this.length > 0) {
         sb.append(this.items[0].getCssText());
      }

      for(int i = 1; i < this.length; ++i) {
         sb.append(this.separator);
         sb.append(this.items[i].getCssText());
      }

      return sb.toString();
   }

   public int getLength() throws DOMException {
      return this.length;
   }

   public Value item(int index) throws DOMException {
      return this.items[index];
   }

   public String toString() {
      return this.getCssText();
   }

   public void append(Value v) {
      if (this.length == this.items.length) {
         Value[] t = new Value[this.length * 2];
         System.arraycopy(this.items, 0, t, 0, this.length);
         this.items = t;
      }

      this.items[this.length++] = v;
   }
}
