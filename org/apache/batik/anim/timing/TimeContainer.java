package org.apache.batik.anim.timing;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public abstract class TimeContainer extends TimedElement {
   protected List children = new LinkedList();

   public void addChild(TimedElement e) {
      if (e == this) {
         throw new IllegalArgumentException("recursive datastructure not allowed here!");
      } else {
         this.children.add(e);
         e.parent = this;
         this.setRoot(e, this.root);
         this.root.fireElementAdded(e);
         this.root.currentIntervalWillUpdate();
      }
   }

   protected void setRoot(TimedElement e, TimedDocumentRoot root) {
      e.root = root;
      if (e instanceof TimeContainer) {
         TimeContainer c = (TimeContainer)e;
         Iterator var4 = c.children.iterator();

         while(var4.hasNext()) {
            Object aChildren = var4.next();
            TimedElement te = (TimedElement)aChildren;
            this.setRoot(te, root);
         }
      }

   }

   public void removeChild(TimedElement e) {
      this.children.remove(e);
      e.parent = null;
      this.setRoot(e, (TimedDocumentRoot)null);
      this.root.fireElementRemoved(e);
      this.root.currentIntervalWillUpdate();
   }

   public TimedElement[] getChildren() {
      return (TimedElement[])((TimedElement[])this.children.toArray(new TimedElement[this.children.size()]));
   }

   protected float sampleAt(float parentSimpleTime, boolean hyperlinking) {
      super.sampleAt(parentSimpleTime, hyperlinking);
      return this.sampleChildren(parentSimpleTime, hyperlinking);
   }

   protected float sampleChildren(float parentSimpleTime, boolean hyperlinking) {
      float mint = Float.POSITIVE_INFINITY;
      Iterator var4 = this.children.iterator();

      while(var4.hasNext()) {
         Object aChildren = var4.next();
         TimedElement e = (TimedElement)aChildren;
         float t = e.sampleAt(parentSimpleTime, hyperlinking);
         if (t < mint) {
            mint = t;
         }
      }

      return mint;
   }

   protected void reset(boolean clearCurrentBegin) {
      super.reset(clearCurrentBegin);
      Iterator var2 = this.children.iterator();

      while(var2.hasNext()) {
         Object aChildren = var2.next();
         TimedElement e = (TimedElement)aChildren;
         e.reset(clearCurrentBegin);
      }

   }

   protected boolean isConstantAnimation() {
      return false;
   }

   public abstract float getDefaultBegin(TimedElement var1);
}
