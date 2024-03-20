package org.apache.fop.fo.properties;

import java.util.List;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.NCnameProperty;
import org.apache.fop.fo.expr.PropertyException;

public class TextDecorationMaker extends ListProperty.Maker {
   public TextDecorationMaker(int propId) {
      super(propId);
   }

   public Property convertProperty(Property p, PropertyList propertyList, FObj fo) throws PropertyException {
      ListProperty listProp = (ListProperty)super.convertProperty(p, propertyList, fo);
      List lst = listProp.getList();
      boolean none = false;
      boolean under = false;
      boolean over = false;
      boolean through = false;
      boolean blink = false;
      int enumValue = -1;
      int i = lst.size();

      while(true) {
         --i;
         if (i < 0) {
            return listProp;
         }

         Property prop = (Property)lst.get(i);
         if (prop instanceof NCnameProperty) {
            prop = this.checkEnumValues(prop.getString());
            lst.set(i, prop);
         }

         if (prop != null) {
            enumValue = prop.getEnum();
         }

         switch (enumValue) {
            case 17:
            case 77:
            case 86:
            case 90:
            case 91:
            case 92:
            case 103:
            case 153:
               if (none) {
                  throw new PropertyException("'none' specified, no additional values allowed");
               }

               switch (enumValue) {
                  case 92:
                  case 153:
                     if (!under) {
                        under = true;
                        continue;
                     }
                  case 91:
                  case 103:
                     if (!over) {
                        over = true;
                        continue;
                     }
                  case 77:
                  case 90:
                     if (!through) {
                        through = true;
                        continue;
                     }
                  case 17:
                  case 86:
                     if (!blink) {
                        blink = true;
                        continue;
                     }
                  default:
                     throw new PropertyException("Invalid combination of values");
               }
            case 95:
               if (under | over | through | blink) {
                  throw new PropertyException("Invalid combination of values");
               }

               none = true;
               break;
            default:
               throw new PropertyException("Invalid value specified: " + p);
         }
      }
   }
}
