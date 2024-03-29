package org.apache.fop.fo.properties;

import org.apache.fop.datatypes.CompoundDatatype;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.util.CompareUtil;

public class LengthPairProperty extends Property implements CompoundDatatype {
   private Property ipd;
   private Property bpd;

   public LengthPairProperty() {
   }

   public LengthPairProperty(Property ipd, Property bpd) {
      this();
      this.ipd = ipd;
      this.bpd = bpd;
   }

   public LengthPairProperty(Property len) {
      this(len, len);
   }

   public void setComponent(int cmpId, Property cmpnValue, boolean bIsDefault) {
      if (cmpId == 512) {
         this.bpd = cmpnValue;
      } else if (cmpId == 1536) {
         this.ipd = cmpnValue;
      }

   }

   public Property getComponent(int cmpId) {
      if (cmpId == 512) {
         return this.getBPD();
      } else {
         return cmpId == 1536 ? this.getIPD() : null;
      }
   }

   public Property getIPD() {
      return this.ipd;
   }

   public Property getBPD() {
      return this.bpd;
   }

   public String toString() {
      return "LengthPair[ipd:" + this.getIPD().getObject() + ", bpd:" + this.getBPD().getObject() + "]";
   }

   public LengthPairProperty getLengthPair() {
      return this;
   }

   public Object getObject() {
      return this;
   }

   public int hashCode() {
      int prime = true;
      int result = 1;
      result = 31 * result + CompareUtil.getHashCode(this.bpd);
      result = 31 * result + CompareUtil.getHashCode(this.ipd);
      return result;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof LengthPairProperty)) {
         return false;
      } else {
         LengthPairProperty other = (LengthPairProperty)obj;
         return CompareUtil.equal(this.bpd, other.bpd) && CompareUtil.equal(this.ipd, other.ipd);
      }
   }

   public static class Maker extends CompoundPropertyMaker {
      public Maker(int propId) {
         super(propId);
      }

      public Property makeNewProperty() {
         return new LengthPairProperty();
      }

      public Property convertProperty(Property p, PropertyList propertyList, FObj fo) throws PropertyException {
         return p instanceof LengthPairProperty ? p : super.convertProperty(p, propertyList, fo);
      }
   }
}
