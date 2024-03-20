package net.jsign.bouncycastle.pqc.math.linearalgebra;

public abstract class Matrix {
   protected int numRows;
   protected int numColumns;

   public int getNumRows() {
      return this.numRows;
   }

   public int getNumColumns() {
      return this.numColumns;
   }
}
