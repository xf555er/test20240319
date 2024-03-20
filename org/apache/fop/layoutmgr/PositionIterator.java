package org.apache.fop.layoutmgr;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class PositionIterator implements Iterator {
   private Iterator parentIter;
   private Object nextObj;
   private LayoutManager childLM;
   private boolean hasNext;

   public PositionIterator(Iterator parentIter) {
      this.parentIter = parentIter;
      this.lookAhead();
   }

   public LayoutManager getNextChildLM() {
      if (this.childLM == null && this.nextObj != null) {
         this.childLM = this.getLM(this.nextObj);
         this.hasNext = true;
      }

      return this.childLM;
   }

   protected LayoutManager getLM(Object nextObj) {
      return this.getPos(nextObj).getLM();
   }

   protected Position getPos(Object nextObj) {
      if (nextObj instanceof Position) {
         return (Position)nextObj;
      } else {
         throw new IllegalArgumentException("Cannot obtain Position from the given object.");
      }
   }

   private void lookAhead() {
      if (this.parentIter.hasNext()) {
         this.hasNext = true;
         this.nextObj = this.parentIter.next();
      } else {
         this.endIter();
      }

   }

   protected boolean checkNext() {
      LayoutManager lm = this.getLM(this.nextObj);
      if (this.childLM == null) {
         this.childLM = lm;
      } else if (this.childLM != lm && lm != null) {
         this.hasNext = false;
         this.childLM = null;
         return false;
      }

      return true;
   }

   protected void endIter() {
      this.hasNext = false;
      this.nextObj = null;
      this.childLM = null;
   }

   public boolean hasNext() {
      return this.hasNext && this.checkNext();
   }

   public Position next() throws NoSuchElementException {
      if (this.hasNext) {
         Position retPos = this.getPos(this.nextObj);
         this.lookAhead();
         return retPos;
      } else {
         throw new NoSuchElementException("PosIter");
      }
   }

   public Object peekNext() {
      return this.nextObj;
   }

   public void remove() throws UnsupportedOperationException {
      throw new UnsupportedOperationException("PositionIterator doesn't support remove");
   }
}
