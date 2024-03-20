package org.apache.fop.layoutmgr;

public class Position {
   private LayoutManager layoutManager;
   private int index;

   public Position(LayoutManager lm) {
      this.index = -1;
      this.layoutManager = lm;
   }

   public Position(LayoutManager lm, int index) {
      this(lm);
      this.setIndex(index);
   }

   public LayoutManager getLM() {
      return this.layoutManager;
   }

   public LayoutManager getLM(int depth) {
      Position subPos = this.getPosition(depth);
      return subPos == null ? null : subPos.getLM();
   }

   public Position getPosition() {
      return null;
   }

   public Position getPosition(int depth) {
      Position subPos = this;

      for(int i = 0; i < depth && subPos != null; subPos = subPos.getPosition()) {
         ++i;
      }

      return subPos;
   }

   public boolean generatesAreas() {
      return false;
   }

   public void setIndex(int value) {
      this.index = value;
   }

   public int getIndex() {
      return this.index;
   }

   protected String getShortLMName() {
      if (this.getLM() != null) {
         String lm = this.getLM().toString();
         int idx = lm.lastIndexOf(46);
         return idx >= 0 && lm.indexOf(64) > 0 ? lm.substring(idx + 1) : lm;
      } else {
         return "null";
      }
   }

   public String toString() {
      StringBuffer sb = new StringBuffer();
      sb.append("Position:").append(this.getIndex()).append("(");
      sb.append(this.getShortLMName());
      sb.append(")");
      return sb.toString();
   }
}
