package org.apache.batik.util;

public class DoublyLinkedList {
   private Node head = null;
   private int size = 0;

   public synchronized int getSize() {
      return this.size;
   }

   public synchronized void empty() {
      while(this.size > 0) {
         this.pop();
      }

   }

   public Node getHead() {
      return this.head;
   }

   public Node getTail() {
      return this.head.getPrev();
   }

   public void touch(Node nde) {
      if (nde != null) {
         nde.insertBefore(this.head);
         this.head = nde;
      }
   }

   public void add(int index, Node nde) {
      if (nde != null) {
         if (index == 0) {
            nde.insertBefore(this.head);
            this.head = nde;
         } else if (index == this.size) {
            nde.insertBefore(this.head);
         } else {
            Node after;
            for(after = this.head; index != 0; --index) {
               after = after.getNext();
            }

            nde.insertBefore(after);
         }

         ++this.size;
      }
   }

   public void add(Node nde) {
      if (nde != null) {
         nde.insertBefore(this.head);
         this.head = nde;
         ++this.size;
      }
   }

   public void remove(Node nde) {
      if (nde != null) {
         if (nde == this.head) {
            if (this.head.getNext() == this.head) {
               this.head = null;
            } else {
               this.head = this.head.getNext();
            }
         }

         nde.unlink();
         --this.size;
      }
   }

   public Node pop() {
      if (this.head == null) {
         return null;
      } else {
         Node nde = this.head;
         this.remove(nde);
         return nde;
      }
   }

   public Node unpush() {
      if (this.head == null) {
         return null;
      } else {
         Node nde = this.getTail();
         this.remove(nde);
         return nde;
      }
   }

   public void push(Node nde) {
      nde.insertBefore(this.head);
      if (this.head == null) {
         this.head = nde;
      }

      ++this.size;
   }

   public void unpop(Node nde) {
      nde.insertBefore(this.head);
      this.head = nde;
      ++this.size;
   }

   public static class Node {
      private Node next = null;
      private Node prev = null;

      public final Node getNext() {
         return this.next;
      }

      public final Node getPrev() {
         return this.prev;
      }

      protected final void setNext(Node newNext) {
         this.next = newNext;
      }

      protected final void setPrev(Node newPrev) {
         this.prev = newPrev;
      }

      protected final void unlink() {
         if (this.getNext() != null) {
            this.getNext().setPrev(this.getPrev());
         }

         if (this.getPrev() != null) {
            this.getPrev().setNext(this.getNext());
         }

         this.setNext((Node)null);
         this.setPrev((Node)null);
      }

      protected final void insertBefore(Node nde) {
         if (this != nde) {
            if (this.getPrev() != null) {
               this.unlink();
            }

            if (nde == null) {
               this.setNext(this);
               this.setPrev(this);
            } else {
               this.setNext(nde);
               this.setPrev(nde.getPrev());
               nde.setPrev(this);
               if (this.getPrev() != null) {
                  this.getPrev().setNext(this);
               }
            }

         }
      }
   }
}
