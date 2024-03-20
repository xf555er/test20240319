package org.apache.fop.render.pdf.extensions;

import org.apache.fop.util.XMLUtil;

public class PDFObjectExtension {
   private PDFObjectType type;
   private Object value;

   PDFObjectExtension(PDFObjectType type) {
      this.type = type;
   }

   public PDFObjectType getType() {
      return this.type;
   }

   public void setValue(Object value) {
      this.value = value;
   }

   public Object getValue() {
      return this.value;
   }

   public Boolean getValueAsBoolean() {
      Object value = this.getValue();
      if (value instanceof Boolean) {
         return (Boolean)value;
      } else {
         return value instanceof String ? Boolean.valueOf((String)value) : false;
      }
   }

   public Number getValueAsNumber() {
      Object value = this.getValue();
      if (value instanceof Number) {
         return (Number)value;
      } else if (value instanceof String) {
         double d = Double.parseDouble((String)value);
         return (Number)(Math.abs(Math.floor(d) - d) < 1.0E-10 ? (long)d : d);
      } else {
         return 0;
      }
   }

   public String getValueAsString() {
      Object value = this.getValue();
      if (value == null) {
         return null;
      } else {
         return value instanceof String ? (String)value : value.toString();
      }
   }

   public String getValueAsXMLEscapedString() {
      return XMLUtil.escape(this.getValueAsString());
   }

   public String getElementName() {
      return this.type.elementName();
   }
}
