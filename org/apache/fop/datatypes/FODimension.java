package org.apache.fop.datatypes;

public class FODimension {
   public int ipd;
   public int bpd;

   public FODimension(int ipd, int bpd) {
      this.ipd = ipd;
      this.bpd = bpd;
   }

   public int getIPD() {
      return this.ipd;
   }

   public int getBPD() {
      return this.bpd;
   }

   public String toString() {
      StringBuffer sb = new StringBuffer(super.toString());
      sb.append(" {ipd=").append(Integer.toString(this.ipd));
      sb.append(", bpd=").append(Integer.toString(this.bpd));
      sb.append("}");
      return sb.toString();
   }
}
