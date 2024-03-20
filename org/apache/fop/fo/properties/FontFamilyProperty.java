package org.apache.fop.fo.properties;

import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.PropertyException;

public final class FontFamilyProperty extends ListProperty {
   private static final PropertyCache CACHE = new PropertyCache();

   private FontFamilyProperty(Property prop) {
      this.addProperty(prop);
   }

   private FontFamilyProperty() {
   }

   public void addProperty(Property prop) {
      if (prop.getList() != null) {
         this.list.addAll(prop.getList());
      } else {
         super.addProperty(prop);
      }

   }

   public String getString() {
      if (this.list.size() > 0) {
         Property first = (Property)this.list.get(0);
         return first.getString();
      } else {
         return super.getString();
      }
   }

   // $FF: synthetic method
   FontFamilyProperty(Object x0) {
      this();
   }

   // $FF: synthetic method
   FontFamilyProperty(Property x0, Object x1) {
      this(x0);
   }

   public static class Maker extends PropertyMaker {
      public Maker(int propId) {
         super(propId);
      }

      public Property make(PropertyList propertyList, String value, FObj fo) throws PropertyException {
         if ("inherit".equals(value)) {
            return super.make(propertyList, value, fo);
         } else {
            FontFamilyProperty prop = new FontFamilyProperty();
            int startIndex = 0;
            int commaIndex = value.indexOf(44);
            boolean parsed = false;

            while(true) {
               String tmpVal;
               do {
                  if (parsed) {
                     return (Property)FontFamilyProperty.CACHE.fetch(prop);
                  }

                  if (commaIndex == -1) {
                     tmpVal = value.substring(startIndex).trim();
                     parsed = true;
                  } else {
                     tmpVal = value.substring(startIndex, commaIndex).trim();
                     startIndex = commaIndex + 1;
                     commaIndex = value.indexOf(44, startIndex);
                  }

                  int aposIndex = tmpVal.indexOf(39);
                  int quoteIndex = tmpVal.indexOf(34);
                  if (aposIndex != -1 || quoteIndex != -1) {
                     char qChar = aposIndex == -1 ? 34 : 39;
                     if (tmpVal.lastIndexOf(qChar) != tmpVal.length() - 1) {
                        Property.log.warn("Skipping malformed value for font-family: " + tmpVal + " in \"" + value + "\".");
                        tmpVal = "";
                     } else {
                        tmpVal = tmpVal.substring(1, tmpVal.length() - 1);
                     }
                  }
               } while("".equals(tmpVal));

               for(int dblSpaceIndex = tmpVal.indexOf("  "); dblSpaceIndex != -1; dblSpaceIndex = tmpVal.indexOf("  ")) {
                  tmpVal = tmpVal.substring(0, dblSpaceIndex) + tmpVal.substring(dblSpaceIndex + 1);
               }

               prop.addProperty(StringProperty.getInstance(tmpVal));
            }
         }
      }

      public Property convertProperty(Property p, PropertyList propertyList, FObj fo) {
         return (Property)(p instanceof FontFamilyProperty ? p : new FontFamilyProperty(p));
      }
   }
}
