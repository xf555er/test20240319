package org.apache.fop.fo.properties;

import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.expr.PropertyInfo;
import org.apache.fop.fo.expr.PropertyParser;

public class FontWeightPropertyMaker extends EnumProperty.Maker {
   public FontWeightPropertyMaker(int propId) {
      super(propId);
   }

   public Property make(PropertyList pList, String value, FObj fo) throws PropertyException {
      if ("inherit".equals(value)) {
         return super.make(pList, value, fo);
      } else {
         String pValue = this.checkValueKeywords(value);
         Property newProp = this.checkEnumValues(pValue);
         int enumValue = newProp != null ? ((Property)newProp).getEnum() : -1;
         if (enumValue != 165 && enumValue != 166) {
            if (enumValue == -1) {
               newProp = PropertyParser.parse(value, new PropertyInfo(this, pList));
            }
         } else {
            Property parentProp = pList.getInherited(108);
            if (enumValue == 165) {
               enumValue = parentProp.getEnum();
               switch (enumValue) {
                  case 167:
                     newProp = EnumProperty.getInstance(168, "200");
                     break;
                  case 168:
                     newProp = EnumProperty.getInstance(169, "300");
                     break;
                  case 169:
                     newProp = EnumProperty.getInstance(170, "400");
                     break;
                  case 170:
                     newProp = EnumProperty.getInstance(171, "500");
                     break;
                  case 171:
                     newProp = EnumProperty.getInstance(172, "600");
                     break;
                  case 172:
                     newProp = EnumProperty.getInstance(173, "700");
                     break;
                  case 173:
                     newProp = EnumProperty.getInstance(174, "800");
                     break;
                  case 174:
                  case 175:
                     newProp = EnumProperty.getInstance(175, "900");
               }
            } else {
               enumValue = parentProp.getEnum();
               switch (enumValue) {
                  case 167:
                  case 168:
                     newProp = EnumProperty.getInstance(167, "100");
                     break;
                  case 169:
                     newProp = EnumProperty.getInstance(168, "200");
                     break;
                  case 170:
                     newProp = EnumProperty.getInstance(169, "300");
                     break;
                  case 171:
                     newProp = EnumProperty.getInstance(170, "400");
                     break;
                  case 172:
                     newProp = EnumProperty.getInstance(171, "500");
                     break;
                  case 173:
                     newProp = EnumProperty.getInstance(172, "600");
                     break;
                  case 174:
                     newProp = EnumProperty.getInstance(173, "700");
                     break;
                  case 175:
                     newProp = EnumProperty.getInstance(174, "800");
               }
            }
         }

         if (newProp != null) {
            newProp = this.convertProperty((Property)newProp, pList, fo);
         }

         return (Property)newProp;
      }
   }
}
