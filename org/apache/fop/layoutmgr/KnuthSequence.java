package org.apache.fop.layoutmgr;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import org.apache.fop.util.ListUtil;

public abstract class KnuthSequence extends ArrayList {
   public KnuthSequence() {
   }

   public KnuthSequence(List list) {
      super(list);
   }

   public void startSequence() {
   }

   public abstract KnuthSequence endSequence();

   public abstract boolean canAppendSequence(KnuthSequence var1);

   public abstract boolean appendSequence(KnuthSequence var1, boolean var2, BreakElement var3);

   public abstract boolean appendSequence(KnuthSequence var1);

   public boolean appendSequenceOrClose(KnuthSequence sequence) {
      if (!this.appendSequence(sequence)) {
         this.endSequence();
         return false;
      } else {
         return true;
      }
   }

   public boolean appendSequenceOrClose(KnuthSequence sequence, boolean keepTogether, BreakElement breakElement) {
      if (!this.appendSequence(sequence, keepTogether, breakElement)) {
         this.endSequence();
         return false;
      } else {
         return true;
      }
   }

   public void wrapPositions(LayoutManager lm) {
      ListIterator listIter = this.listIterator();

      while(listIter.hasNext()) {
         ListElement element = (ListElement)listIter.next();
         element.setPosition(lm.notifyPos(new NonLeafPosition(lm, element.getPosition())));
      }

   }

   public ListElement getLast() {
      return this.isEmpty() ? null : (ListElement)ListUtil.getLast(this);
   }

   public ListElement removeLast() {
      return this.isEmpty() ? null : (ListElement)ListUtil.removeLast(this);
   }

   public ListElement getElement(int index) {
      return index < this.size() && index >= 0 ? (ListElement)this.get(index) : null;
   }

   protected int getFirstBoxIndex(int startIndex) {
      if (startIndex >= 0 && startIndex < this.size()) {
         int boxIndex = startIndex;

         for(Iterator iter = this.listIterator(startIndex); iter.hasNext() && !((ListElement)iter.next()).isBox(); ++boxIndex) {
         }

         return boxIndex;
      } else {
         return startIndex;
      }
   }

   public abstract boolean isInlineSequence();

   public String toString() {
      return "<KnuthSequence " + super.toString() + ">";
   }
}
