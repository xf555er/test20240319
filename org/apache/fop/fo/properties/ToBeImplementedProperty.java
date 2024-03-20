package org.apache.fop.fo.properties;

import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;

public class ToBeImplementedProperty extends Property {
   public ToBeImplementedProperty(int propId) {
   }

   public int hashCode() {
      return 0;
   }

   public boolean equals(Object obj) {
      return true;
   }

   public static class Maker extends PropertyMaker {
      public Maker(int propId) {
         super(propId);
      }

      public Property convertProperty(Property p, PropertyList propertyList, FObj fo) {
         if (p instanceof ToBeImplementedProperty) {
            return p;
         } else {
            ToBeImplementedProperty val = new ToBeImplementedProperty(this.getPropId());
            return val;
         }
      }
   }
}
