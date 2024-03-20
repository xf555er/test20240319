package org.apache.fop.fo.properties;

import org.apache.fop.fo.FOPropertyMapping;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.PropertyException;

public class BoxCornerPropShorthandParser extends GenericShorthandParser {
   protected Property convertValueForProperty(int propId, Property property, PropertyMaker maker, PropertyList propertyList) throws PropertyException {
      String name = FOPropertyMapping.getPropertyName(propId);
      Property p = null;
      int count = property.getList().size();
      if (name.indexOf("border-start") <= -1 && name.indexOf("border-end") <= -1) {
         if (name.indexOf("border-before") > -1 || name.indexOf("border-after") > -1) {
            p = this.getElement(property, count > 1 ? 1 : 0);
         }
      } else {
         p = this.getElement(property, 0);
      }

      return p != null ? maker.convertShorthandProperty(propertyList, p, (FObj)null) : p;
   }
}
