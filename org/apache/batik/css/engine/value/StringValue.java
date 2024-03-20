package org.apache.batik.css.engine.value;

import org.w3c.dom.DOMException;

public class StringValue extends AbstractValue {
   protected String value;
   protected short unitType;

   public static String getCssText(short type, String value) {
      switch (type) {
         case 19:
            char q = value.indexOf(34) != -1 ? 39 : 34;
            return q + value + q;
         case 20:
            return "url(" + value + ')';
         default:
            return value;
      }
   }

   public StringValue(short type, String s) {
      this.unitType = type;
      this.value = s;
   }

   public short getPrimitiveType() {
      return this.unitType;
   }

   public boolean equals(Object obj) {
      if (obj != null && obj instanceof StringValue) {
         StringValue v = (StringValue)obj;
         return this.unitType != v.unitType ? false : this.value.equals(v.value);
      } else {
         return false;
      }
   }

   public String getCssText() {
      return getCssText(this.unitType, this.value);
   }

   public String getStringValue() throws DOMException {
      return this.value;
   }

   public String toString() {
      return this.getCssText();
   }
}
