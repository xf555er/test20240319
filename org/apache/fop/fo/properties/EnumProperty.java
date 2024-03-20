package org.apache.fop.fo.properties;

import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.util.CompareUtil;

public final class EnumProperty extends Property {
   private static final PropertyCache CACHE = new PropertyCache();
   private final int value;
   private final String text;

   private EnumProperty(int explicitValue, String text) {
      this.value = explicitValue;
      this.text = text;
   }

   public static EnumProperty getInstance(int explicitValue, String text) {
      return (EnumProperty)CACHE.fetch(new EnumProperty(explicitValue, text));
   }

   public int getEnum() {
      return this.value;
   }

   public Object getObject() {
      return this.text;
   }

   public boolean equals(Object obj) {
      if (!(obj instanceof EnumProperty)) {
         return false;
      } else {
         EnumProperty ep = (EnumProperty)obj;
         return this.value == ep.value && CompareUtil.equal(this.text, ep.text);
      }
   }

   public int hashCode() {
      return this.value + this.text.hashCode();
   }

   public static class Maker extends PropertyMaker {
      public Maker(int propId) {
         super(propId);
      }

      public Property checkEnumValues(String value) {
         return super.checkEnumValues(value);
      }

      public Property convertProperty(Property p, PropertyList propertyList, FObj fo) throws PropertyException {
         return p instanceof EnumProperty ? p : super.convertProperty(p, propertyList, fo);
      }
   }
}
