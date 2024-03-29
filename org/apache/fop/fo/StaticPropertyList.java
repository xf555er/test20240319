package org.apache.fop.fo;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.properties.Property;

public class StaticPropertyList extends PropertyList {
   private final Property[] explicit = new Property[295];
   private final Property[] values = new Property[295];

   public StaticPropertyList(FObj fObjToAttach, PropertyList parentPropertyList) {
      super(fObjToAttach, parentPropertyList);
   }

   public Property getExplicit(int propId) {
      return this.explicit[propId];
   }

   public void putExplicit(int propId, Property value) {
      this.explicit[propId] = value;
      if (this.values[propId] != null) {
         this.values[propId] = value;
      }

   }

   public Property get(int propId, boolean bTryInherit, boolean bTryDefault) throws PropertyException {
      Property p = this.values[propId];
      if (p == null) {
         p = super.get(propId, bTryInherit, bTryDefault);
         this.values[propId] = p;
      }

      return p;
   }
}
