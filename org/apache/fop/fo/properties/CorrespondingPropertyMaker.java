package org.apache.fop.fo.properties;

import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.PropertyException;

public class CorrespondingPropertyMaker {
   protected PropertyMaker baseMaker;
   protected int lrtb;
   protected int rltb;
   protected int tbrl;
   protected int tblr;
   protected boolean useParent;
   private boolean relative;

   public CorrespondingPropertyMaker(PropertyMaker baseMaker) {
      this.baseMaker = baseMaker;
      baseMaker.setCorresponding(this);
   }

   public void setCorresponding(int lrtb, int rltb, int tbrl, int tblr) {
      this.lrtb = lrtb;
      this.rltb = rltb;
      this.tbrl = tbrl;
      this.tblr = tblr;
   }

   public void setUseParent(boolean useParent) {
      this.useParent = useParent;
   }

   public void setRelative(boolean relative) {
      this.relative = relative;
   }

   public boolean isCorrespondingForced(PropertyList propertyList) {
      if (!this.relative) {
         return false;
      } else {
         PropertyList pList = this.getWMPropertyList(propertyList);
         if (pList != null) {
            int correspondingId = pList.selectFromWritingMode(this.lrtb, this.rltb, this.tbrl, this.tblr);
            if (pList.getExplicit(correspondingId) != null) {
               return true;
            }
         }

         return false;
      }
   }

   public Property compute(PropertyList propertyList) throws PropertyException {
      PropertyList pList = this.getWMPropertyList(propertyList);
      if (pList == null) {
         return null;
      } else {
         int correspondingId = pList.selectFromWritingMode(this.lrtb, this.rltb, this.tbrl, this.tblr);
         Property p = propertyList.getExplicitOrShorthand(correspondingId);
         if (p != null) {
            FObj parentFO = propertyList.getParentFObj();
            p = this.baseMaker.convertProperty(p, propertyList, parentFO);
         }

         return p;
      }
   }

   protected PropertyList getWMPropertyList(PropertyList pList) {
      return this.useParent ? pList.getParentPropertyList() : pList;
   }
}
