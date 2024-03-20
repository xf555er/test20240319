package org.apache.batik.gvt;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import org.apache.batik.util.HaltingThread;

public class CompositeGraphicsNode extends AbstractGraphicsNode implements List {
   public static final Rectangle2D VIEWPORT = new Rectangle();
   public static final Rectangle2D NULL_RECT = new Rectangle();
   protected GraphicsNode[] children;
   protected volatile int count;
   protected volatile int modCount;
   protected Rectangle2D backgroundEnableRgn = null;
   private volatile Rectangle2D geometryBounds;
   private volatile Rectangle2D primitiveBounds;
   private volatile Rectangle2D sensitiveBounds;
   private Shape outline;

   public List getChildren() {
      return this;
   }

   public void setBackgroundEnable(Rectangle2D bgRgn) {
      this.backgroundEnableRgn = bgRgn;
   }

   public Rectangle2D getBackgroundEnable() {
      return this.backgroundEnableRgn;
   }

   public void setVisible(boolean isVisible) {
      this.isVisible = isVisible;
   }

   public void primitivePaint(Graphics2D g2d) {
      if (this.count != 0) {
         Thread currentThread = Thread.currentThread();

         for(int i = 0; i < this.count; ++i) {
            if (HaltingThread.hasBeenHalted(currentThread)) {
               return;
            }

            GraphicsNode node = this.children[i];
            if (node != null) {
               node.paint(g2d);
            }
         }

      }
   }

   protected void invalidateGeometryCache() {
      super.invalidateGeometryCache();
      this.geometryBounds = null;
      this.primitiveBounds = null;
      this.sensitiveBounds = null;
      this.outline = null;
   }

   public Rectangle2D getPrimitiveBounds() {
      if (this.primitiveBounds != null) {
         return this.primitiveBounds == NULL_RECT ? null : this.primitiveBounds;
      } else {
         Thread currentThread = Thread.currentThread();
         int i = 0;
         Rectangle2D bounds = null;

         while(bounds == null && i < this.count) {
            bounds = this.children[i++].getTransformedBounds(IDENTITY);
            if ((i & 15) == 0 && HaltingThread.hasBeenHalted(currentThread)) {
               break;
            }
         }

         if (HaltingThread.hasBeenHalted(currentThread)) {
            this.invalidateGeometryCache();
            return null;
         } else if (bounds == null) {
            this.primitiveBounds = NULL_RECT;
            return null;
         } else {
            this.primitiveBounds = bounds;

            while(i < this.count) {
               Rectangle2D ctb = this.children[i++].getTransformedBounds(IDENTITY);
               if (ctb != null) {
                  if (this.primitiveBounds == null) {
                     return null;
                  }

                  this.primitiveBounds.add(ctb);
               }

               if ((i & 15) == 0 && HaltingThread.hasBeenHalted(currentThread)) {
                  break;
               }
            }

            if (HaltingThread.hasBeenHalted(currentThread)) {
               this.invalidateGeometryCache();
            }

            return this.primitiveBounds;
         }
      }
   }

   public static Rectangle2D getTransformedBBox(Rectangle2D r2d, AffineTransform t) {
      if (t != null && r2d != null) {
         double x = r2d.getX();
         double w = r2d.getWidth();
         double y = r2d.getY();
         double h = r2d.getHeight();
         double sx = t.getScaleX();
         double sy = t.getScaleY();
         if (sx < 0.0) {
            x = -(x + w);
            sx = -sx;
         }

         if (sy < 0.0) {
            y = -(y + h);
            sy = -sy;
         }

         return new Rectangle2D.Float((float)(x * sx + t.getTranslateX()), (float)(y * sy + t.getTranslateY()), (float)(w * sx), (float)(h * sy));
      } else {
         return r2d;
      }
   }

   public Rectangle2D getTransformedPrimitiveBounds(AffineTransform txf) {
      AffineTransform t = txf;
      if (this.transform != null) {
         t = new AffineTransform(txf);
         t.concatenate(this.transform);
      }

      if (t != null && (t.getShearX() != 0.0 || t.getShearY() != 0.0)) {
         int i = 0;

         Rectangle2D tpb;
         for(tpb = null; tpb == null && i < this.count; tpb = this.children[i++].getTransformedBounds(t)) {
         }

         while(i < this.count) {
            Rectangle2D ctb = this.children[i++].getTransformedBounds(t);
            if (ctb != null) {
               tpb.add(ctb);
            }
         }

         return tpb;
      } else {
         return getTransformedBBox(this.getPrimitiveBounds(), t);
      }
   }

   public Rectangle2D getGeometryBounds() {
      if (this.geometryBounds == null) {
         int i;
         for(i = 0; this.geometryBounds == null && i < this.count; this.geometryBounds = this.children[i++].getTransformedGeometryBounds(IDENTITY)) {
         }

         while(i < this.count) {
            Rectangle2D cgb = this.children[i++].getTransformedGeometryBounds(IDENTITY);
            if (cgb != null) {
               if (this.geometryBounds == null) {
                  return this.getGeometryBounds();
               }

               this.geometryBounds.add(cgb);
            }
         }
      }

      return this.geometryBounds;
   }

   public Rectangle2D getTransformedGeometryBounds(AffineTransform txf) {
      AffineTransform t = txf;
      if (this.transform != null) {
         t = new AffineTransform(txf);
         t.concatenate(this.transform);
      }

      if (t == null || t.getShearX() == 0.0 && t.getShearY() == 0.0) {
         return getTransformedBBox(this.getGeometryBounds(), t);
      } else {
         Rectangle2D gb = null;

         int i;
         for(i = 0; gb == null && i < this.count; gb = this.children[i++].getTransformedGeometryBounds(t)) {
         }

         Rectangle2D cgb = null;

         while(i < this.count) {
            cgb = this.children[i++].getTransformedGeometryBounds(t);
            if (cgb != null) {
               gb.add(cgb);
            }
         }

         return gb;
      }
   }

   public Rectangle2D getSensitiveBounds() {
      if (this.sensitiveBounds != null) {
         return this.sensitiveBounds;
      } else {
         int i;
         for(i = 0; this.sensitiveBounds == null && i < this.count; this.sensitiveBounds = this.children[i++].getTransformedSensitiveBounds(IDENTITY)) {
         }

         while(i < this.count) {
            Rectangle2D cgb = this.children[i++].getTransformedSensitiveBounds(IDENTITY);
            if (cgb != null) {
               if (this.sensitiveBounds == null) {
                  return this.getSensitiveBounds();
               }

               this.sensitiveBounds.add(cgb);
            }
         }

         return this.sensitiveBounds;
      }
   }

   public Rectangle2D getTransformedSensitiveBounds(AffineTransform txf) {
      AffineTransform t = txf;
      if (this.transform != null) {
         t = new AffineTransform(txf);
         t.concatenate(this.transform);
      }

      if (t != null && (t.getShearX() != 0.0 || t.getShearY() != 0.0)) {
         Rectangle2D sb = null;

         int i;
         for(i = 0; sb == null && i < this.count; sb = this.children[i++].getTransformedSensitiveBounds(t)) {
         }

         while(i < this.count) {
            Rectangle2D csb = this.children[i++].getTransformedSensitiveBounds(t);
            if (csb != null) {
               sb.add(csb);
            }
         }

         return sb;
      } else {
         return getTransformedBBox(this.getSensitiveBounds(), t);
      }
   }

   public boolean contains(Point2D p) {
      Rectangle2D bounds = this.getSensitiveBounds();
      if (this.count > 0 && bounds != null && bounds.contains(p)) {
         Point2D pt = null;
         Point2D cp = null;

         for(int i = 0; i < this.count; ++i) {
            AffineTransform t = this.children[i].getInverseTransform();
            if (t != null) {
               pt = t.transform(p, pt);
               cp = pt;
            } else {
               cp = p;
            }

            if (this.children[i].contains(cp)) {
               return true;
            }
         }
      }

      return false;
   }

   public GraphicsNode nodeHitAt(Point2D p) {
      Rectangle2D bounds = this.getSensitiveBounds();
      if (this.count > 0 && bounds != null && bounds.contains(p)) {
         Point2D pt = null;
         Point2D cp = null;

         for(int i = this.count - 1; i >= 0; --i) {
            AffineTransform t = this.children[i].getInverseTransform();
            if (t != null) {
               pt = t.transform(p, pt);
               cp = pt;
            } else {
               cp = p;
            }

            GraphicsNode node = this.children[i].nodeHitAt(cp);
            if (node != null) {
               return node;
            }
         }
      }

      return null;
   }

   public Shape getOutline() {
      if (this.outline != null) {
         return this.outline;
      } else {
         this.outline = new GeneralPath();

         for(int i = 0; i < this.count; ++i) {
            Shape childOutline = this.children[i].getOutline();
            if (childOutline != null) {
               AffineTransform tr = this.children[i].getTransform();
               if (tr != null) {
                  ((GeneralPath)this.outline).append(tr.createTransformedShape(childOutline), false);
               } else {
                  ((GeneralPath)this.outline).append(childOutline, false);
               }
            }
         }

         return this.outline;
      }
   }

   protected void setRoot(RootGraphicsNode newRoot) {
      super.setRoot(newRoot);

      for(int i = 0; i < this.count; ++i) {
         GraphicsNode node = this.children[i];
         ((AbstractGraphicsNode)node).setRoot(newRoot);
      }

   }

   public int size() {
      return this.count;
   }

   public boolean isEmpty() {
      return this.count == 0;
   }

   public boolean contains(Object node) {
      return this.indexOf(node) >= 0;
   }

   public Iterator iterator() {
      return new Itr();
   }

   public Object[] toArray() {
      GraphicsNode[] result = new GraphicsNode[this.count];
      System.arraycopy(this.children, 0, result, 0, this.count);
      return result;
   }

   public Object[] toArray(Object[] a) {
      if (((Object[])a).length < this.count) {
         a = new GraphicsNode[this.count];
      }

      System.arraycopy(this.children, 0, a, 0, this.count);
      if (((Object[])a).length > this.count) {
         ((Object[])a)[this.count] = null;
      }

      return (Object[])a;
   }

   public Object get(int index) {
      this.checkRange(index);
      return this.children[index];
   }

   public Object set(int index, Object o) {
      if (!(o instanceof GraphicsNode)) {
         throw new IllegalArgumentException(o + " is not a GraphicsNode");
      } else {
         this.checkRange(index);
         GraphicsNode node = (GraphicsNode)o;
         this.fireGraphicsNodeChangeStarted(node);
         if (node.getParent() != null) {
            node.getParent().getChildren().remove(node);
         }

         GraphicsNode oldNode = this.children[index];
         this.children[index] = node;
         ((AbstractGraphicsNode)node).setParent(this);
         ((AbstractGraphicsNode)oldNode).setParent((CompositeGraphicsNode)null);
         ((AbstractGraphicsNode)node).setRoot(this.getRoot());
         ((AbstractGraphicsNode)oldNode).setRoot((RootGraphicsNode)null);
         this.invalidateGeometryCache();
         this.fireGraphicsNodeChangeCompleted();
         return oldNode;
      }
   }

   public boolean add(Object o) {
      if (!(o instanceof GraphicsNode)) {
         throw new IllegalArgumentException(o + " is not a GraphicsNode");
      } else {
         GraphicsNode node = (GraphicsNode)o;
         this.fireGraphicsNodeChangeStarted(node);
         if (node.getParent() != null) {
            node.getParent().getChildren().remove(node);
         }

         this.ensureCapacity(this.count + 1);
         this.children[this.count++] = node;
         ((AbstractGraphicsNode)node).setParent(this);
         ((AbstractGraphicsNode)node).setRoot(this.getRoot());
         this.invalidateGeometryCache();
         this.fireGraphicsNodeChangeCompleted();
         return true;
      }
   }

   public void add(int index, Object o) {
      if (!(o instanceof GraphicsNode)) {
         throw new IllegalArgumentException(o + " is not a GraphicsNode");
      } else if (index <= this.count && index >= 0) {
         GraphicsNode node = (GraphicsNode)o;
         this.fireGraphicsNodeChangeStarted(node);
         if (node.getParent() != null) {
            node.getParent().getChildren().remove(node);
         }

         this.ensureCapacity(this.count + 1);
         System.arraycopy(this.children, index, this.children, index + 1, this.count - index);
         this.children[index] = node;
         ++this.count;
         ((AbstractGraphicsNode)node).setParent(this);
         ((AbstractGraphicsNode)node).setRoot(this.getRoot());
         this.invalidateGeometryCache();
         this.fireGraphicsNodeChangeCompleted();
      } else {
         throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + this.count);
      }
   }

   public boolean addAll(Collection c) {
      throw new UnsupportedOperationException();
   }

   public boolean addAll(int index, Collection c) {
      throw new UnsupportedOperationException();
   }

   public boolean remove(Object o) {
      if (!(o instanceof GraphicsNode)) {
         throw new IllegalArgumentException(o + " is not a GraphicsNode");
      } else {
         GraphicsNode node = (GraphicsNode)o;
         if (node.getParent() != this) {
            return false;
         } else {
            int index;
            for(index = 0; node != this.children[index]; ++index) {
            }

            this.remove(index);
            return true;
         }
      }
   }

   public Object remove(int index) {
      this.checkRange(index);
      GraphicsNode oldNode = this.children[index];
      this.fireGraphicsNodeChangeStarted(oldNode);
      ++this.modCount;
      int numMoved = this.count - index - 1;
      if (numMoved > 0) {
         System.arraycopy(this.children, index + 1, this.children, index, numMoved);
      }

      this.children[--this.count] = null;
      if (this.count == 0) {
         this.children = null;
      }

      ((AbstractGraphicsNode)oldNode).setParent((CompositeGraphicsNode)null);
      ((AbstractGraphicsNode)oldNode).setRoot((RootGraphicsNode)null);
      this.invalidateGeometryCache();
      this.fireGraphicsNodeChangeCompleted();
      return oldNode;
   }

   public boolean removeAll(Collection c) {
      throw new UnsupportedOperationException();
   }

   public boolean retainAll(Collection c) {
      throw new UnsupportedOperationException();
   }

   public void clear() {
      throw new UnsupportedOperationException();
   }

   public boolean containsAll(Collection c) {
      Iterator var2 = c.iterator();

      Object aC;
      do {
         if (!var2.hasNext()) {
            return true;
         }

         aC = var2.next();
      } while(this.contains(aC));

      return false;
   }

   public int indexOf(Object node) {
      if (node != null && node instanceof GraphicsNode) {
         if (((GraphicsNode)node).getParent() == this) {
            int iCount = this.count;
            GraphicsNode[] workList = this.children;

            for(int i = 0; i < iCount; ++i) {
               if (node == workList[i]) {
                  return i;
               }
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   public int lastIndexOf(Object node) {
      if (node != null && node instanceof GraphicsNode) {
         if (((GraphicsNode)node).getParent() == this) {
            for(int i = this.count - 1; i >= 0; --i) {
               if (node == this.children[i]) {
                  return i;
               }
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   public ListIterator listIterator() {
      return this.listIterator(0);
   }

   public ListIterator listIterator(int index) {
      if (index >= 0 && index <= this.count) {
         return new ListItr(index);
      } else {
         throw new IndexOutOfBoundsException("Index: " + index);
      }
   }

   public List subList(int fromIndex, int toIndex) {
      throw new UnsupportedOperationException();
   }

   private void checkRange(int index) {
      if (index >= this.count || index < 0) {
         throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + this.count);
      }
   }

   public void ensureCapacity(int minCapacity) {
      if (this.children == null) {
         this.children = new GraphicsNode[4];
      }

      ++this.modCount;
      int oldCapacity = this.children.length;
      if (minCapacity > oldCapacity) {
         GraphicsNode[] oldData = this.children;
         int newCapacity = oldCapacity + oldCapacity / 2 + 1;
         if (newCapacity < minCapacity) {
            newCapacity = minCapacity;
         }

         this.children = new GraphicsNode[newCapacity];
         System.arraycopy(oldData, 0, this.children, 0, this.count);
      }

   }

   private class ListItr extends Itr implements ListIterator {
      ListItr(int index) {
         super(null);
         this.cursor = index;
      }

      public boolean hasPrevious() {
         return this.cursor != 0;
      }

      public Object previous() {
         try {
            Object previous = CompositeGraphicsNode.this.get(--this.cursor);
            this.checkForComodification();
            this.lastRet = this.cursor;
            return previous;
         } catch (IndexOutOfBoundsException var2) {
            this.checkForComodification();
            throw new NoSuchElementException();
         }
      }

      public int nextIndex() {
         return this.cursor;
      }

      public int previousIndex() {
         return this.cursor - 1;
      }

      public void set(Object o) {
         if (this.lastRet == -1) {
            throw new IllegalStateException();
         } else {
            this.checkForComodification();

            try {
               CompositeGraphicsNode.this.set(this.lastRet, o);
               this.expectedModCount = CompositeGraphicsNode.this.modCount;
            } catch (IndexOutOfBoundsException var3) {
               throw new ConcurrentModificationException();
            }
         }
      }

      public void add(Object o) {
         this.checkForComodification();

         try {
            CompositeGraphicsNode.this.add(this.cursor++, o);
            this.lastRet = -1;
            this.expectedModCount = CompositeGraphicsNode.this.modCount;
         } catch (IndexOutOfBoundsException var3) {
            throw new ConcurrentModificationException();
         }
      }
   }

   private class Itr implements Iterator {
      int cursor;
      int lastRet;
      int expectedModCount;

      private Itr() {
         this.cursor = 0;
         this.lastRet = -1;
         this.expectedModCount = CompositeGraphicsNode.this.modCount;
      }

      public boolean hasNext() {
         return this.cursor != CompositeGraphicsNode.this.count;
      }

      public Object next() {
         try {
            Object next = CompositeGraphicsNode.this.get(this.cursor);
            this.checkForComodification();
            this.lastRet = this.cursor++;
            return next;
         } catch (IndexOutOfBoundsException var2) {
            this.checkForComodification();
            throw new NoSuchElementException();
         }
      }

      public void remove() {
         if (this.lastRet == -1) {
            throw new IllegalStateException();
         } else {
            this.checkForComodification();

            try {
               CompositeGraphicsNode.this.remove(this.lastRet);
               if (this.lastRet < this.cursor) {
                  --this.cursor;
               }

               this.lastRet = -1;
               this.expectedModCount = CompositeGraphicsNode.this.modCount;
            } catch (IndexOutOfBoundsException var2) {
               throw new ConcurrentModificationException();
            }
         }
      }

      final void checkForComodification() {
         if (CompositeGraphicsNode.this.modCount != this.expectedModCount) {
            throw new ConcurrentModificationException();
         }
      }

      // $FF: synthetic method
      Itr(Object x1) {
         this();
      }
   }
}
