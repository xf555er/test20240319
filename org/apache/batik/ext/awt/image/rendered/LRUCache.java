package org.apache.batik.ext.awt.image.rendered;

import org.apache.batik.util.DoublyLinkedList;

public class LRUCache {
   private DoublyLinkedList free = null;
   private DoublyLinkedList used = null;
   private int maxSize = 0;

   public LRUCache(int size) {
      if (size <= 0) {
         size = 1;
      }

      this.maxSize = size;
      this.free = new DoublyLinkedList();

      for(this.used = new DoublyLinkedList(); size > 0; --size) {
         this.free.add(new LRUNode());
      }

   }

   public int getUsed() {
      return this.used.getSize();
   }

   public synchronized void setSize(int newSz) {
      int i;
      if (this.maxSize < newSz) {
         for(i = this.maxSize; i < newSz; ++i) {
            this.free.add(new LRUNode());
         }
      } else if (this.maxSize > newSz) {
         for(i = this.used.getSize(); i > newSz; --i) {
            LRUNode nde = (LRUNode)this.used.getTail();
            this.used.remove(nde);
            nde.setObj((LRUObj)null);
         }
      }

      this.maxSize = newSz;
   }

   public synchronized void flush() {
      while(this.used.getSize() > 0) {
         LRUNode nde = (LRUNode)this.used.pop();
         nde.setObj((LRUObj)null);
         this.free.add(nde);
      }

   }

   public synchronized void remove(LRUObj obj) {
      LRUNode nde = obj.lruGet();
      if (nde != null) {
         this.used.remove(nde);
         nde.setObj((LRUObj)null);
         this.free.add(nde);
      }
   }

   public synchronized void touch(LRUObj obj) {
      LRUNode nde = obj.lruGet();
      if (nde != null) {
         this.used.touch(nde);
      }
   }

   public synchronized void add(LRUObj obj) {
      LRUNode nde = obj.lruGet();
      if (nde != null) {
         this.used.touch(nde);
      } else {
         if (this.free.getSize() > 0) {
            nde = (LRUNode)this.free.pop();
            nde.setObj(obj);
            this.used.add(nde);
         } else {
            nde = (LRUNode)this.used.getTail();
            nde.setObj(obj);
            this.used.touch(nde);
         }

      }
   }

   protected synchronized void print() {
      System.out.println("In Use: " + this.used.getSize() + " Free: " + this.free.getSize());
      LRUNode nde = (LRUNode)this.used.getHead();
      if (nde != null) {
         do {
            System.out.println(nde.getObj());
            nde = (LRUNode)nde.getNext();
         } while(nde != this.used.getHead());

      }
   }

   public static class LRUNode extends DoublyLinkedList.Node {
      private LRUObj obj = null;

      public LRUObj getObj() {
         return this.obj;
      }

      protected void setObj(LRUObj newObj) {
         if (this.obj != null) {
            this.obj.lruRemove();
         }

         this.obj = newObj;
         if (this.obj != null) {
            this.obj.lruSet(this);
         }

      }
   }

   public interface LRUObj {
      void lruSet(LRUNode var1);

      LRUNode lruGet();

      void lruRemove();
   }
}
