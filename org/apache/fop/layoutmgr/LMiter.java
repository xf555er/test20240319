package org.apache.fop.layoutmgr;

import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public class LMiter implements ListIterator {
   protected List listLMs;
   protected int curPos;
   private LayoutManager lp;

   public LMiter(LayoutManager lp) {
      this.lp = lp;
      this.listLMs = lp.getChildLMs();
   }

   public boolean hasNext() {
      return this.curPos < this.listLMs.size() || this.lp.createNextChildLMs(this.curPos);
   }

   public boolean hasPrevious() {
      return this.curPos > 0;
   }

   public LayoutManager previous() throws NoSuchElementException {
      if (this.curPos > 0) {
         return (LayoutManager)this.listLMs.get(--this.curPos);
      } else {
         throw new NoSuchElementException();
      }
   }

   public LayoutManager next() throws NoSuchElementException {
      if (this.curPos < this.listLMs.size()) {
         return (LayoutManager)this.listLMs.get(this.curPos++);
      } else {
         throw new NoSuchElementException();
      }
   }

   public void remove() throws NoSuchElementException {
      if (this.curPos > 0) {
         this.listLMs.remove(--this.curPos);
      } else {
         throw new NoSuchElementException();
      }
   }

   public void add(LayoutManager lm) throws UnsupportedOperationException {
      throw new UnsupportedOperationException("LMiter doesn't support add");
   }

   public void set(LayoutManager lm) throws UnsupportedOperationException {
      throw new UnsupportedOperationException("LMiter doesn't support set");
   }

   public int nextIndex() {
      return this.curPos;
   }

   public int previousIndex() {
      return this.curPos - 1;
   }
}
