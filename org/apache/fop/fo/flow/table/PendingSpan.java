package org.apache.fop.fo.flow.table;

class PendingSpan {
   private int rowsLeft;

   public PendingSpan(int rows) {
      this.rowsLeft = rows;
   }

   public int getRowsLeft() {
      return this.rowsLeft;
   }

   public int decrRowsLeft() {
      return this.rowsLeft > 0 ? --this.rowsLeft : 0;
   }
}
