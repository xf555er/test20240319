package org.apache.fop.fo.properties;

import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.PropertyException;

public class DimensionPropertyMaker extends CorrespondingPropertyMaker {
   private int[][] extraCorresponding;

   public DimensionPropertyMaker(PropertyMaker baseMaker) {
      super(baseMaker);
   }

   public void setExtraCorresponding(int[][] extraCorresponding) {
      if (extraCorresponding == null) {
         throw new NullPointerException();
      } else {
         for(int i = 0; i < extraCorresponding.length; ++i) {
            int[] eca = extraCorresponding[i];
            if (eca == null || eca.length != 4) {
               throw new IllegalArgumentException("bad sub-array @ [" + i + "]");
            }
         }

         this.extraCorresponding = extraCorresponding;
      }
   }

   public boolean isCorrespondingForced(PropertyList propertyList) {
      if (super.isCorrespondingForced(propertyList)) {
         return true;
      } else {
         int[][] var2 = this.extraCorresponding;
         int var3 = var2.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            int[] anExtraCorresponding = var2[var4];
            int wmcorr = anExtraCorresponding[0];
            if (propertyList.getExplicit(wmcorr) != null) {
               return true;
            }
         }

         return false;
      }
   }

   public Property compute(PropertyList propertyList) throws PropertyException {
      Property p = super.compute(propertyList);
      if (p == null) {
         p = this.baseMaker.make(propertyList);
      }

      int wmcorr = propertyList.selectFromWritingMode(this.extraCorresponding[0][0], this.extraCorresponding[0][1], this.extraCorresponding[0][2], this.extraCorresponding[0][3]);
      Property subprop = propertyList.getExplicitOrShorthand(wmcorr);
      if (subprop != null) {
         this.baseMaker.setSubprop(p, 3072, subprop);
      }

      wmcorr = propertyList.selectFromWritingMode(this.extraCorresponding[1][0], this.extraCorresponding[1][1], this.extraCorresponding[1][2], this.extraCorresponding[1][3]);
      subprop = propertyList.getExplicitOrShorthand(wmcorr);
      if (subprop != null) {
         this.baseMaker.setSubprop(p, 2560, subprop);
      }

      return p;
   }
}
