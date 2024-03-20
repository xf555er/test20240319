package org.apache.batik.gvt.event;

import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.lang.reflect.Array;
import java.util.EventListener;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;
import javax.swing.event.EventListenerList;
import org.apache.batik.gvt.GraphicsNode;

public class AWTEventDispatcher implements EventDispatcher, MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {
   protected GraphicsNode root;
   protected AffineTransform baseTransform;
   protected EventListenerList glisteners;
   protected GraphicsNode lastHit;
   protected GraphicsNode currentKeyEventTarget;
   protected List eventQueue = new LinkedList();
   protected boolean eventDispatchEnabled = true;
   protected int eventQueueMaxSize = 10;
   static final int MAX_QUEUE_SIZE = 10;
   private int nodeIncrementEventID = 401;
   private int nodeIncrementEventCode = 9;
   private int nodeIncrementEventModifiers = 0;
   private int nodeDecrementEventID = 401;
   private int nodeDecrementEventCode = 9;
   private int nodeDecrementEventModifiers = 64;

   public void setRootNode(GraphicsNode root) {
      if (this.root != root) {
         this.eventQueue.clear();
      }

      this.root = root;
   }

   public GraphicsNode getRootNode() {
      return this.root;
   }

   public void setBaseTransform(AffineTransform t) {
      if (this.baseTransform != t && (this.baseTransform == null || !this.baseTransform.equals(t))) {
         this.eventQueue.clear();
      }

      this.baseTransform = t;
   }

   public AffineTransform getBaseTransform() {
      return new AffineTransform(this.baseTransform);
   }

   public void mousePressed(MouseEvent evt) {
      this.dispatchEvent(evt);
   }

   public void mouseReleased(MouseEvent evt) {
      this.dispatchEvent(evt);
   }

   public void mouseEntered(MouseEvent evt) {
      this.dispatchEvent(evt);
   }

   public void mouseExited(MouseEvent evt) {
      this.dispatchEvent(evt);
   }

   public void mouseClicked(MouseEvent evt) {
      this.dispatchEvent(evt);
   }

   public void mouseMoved(MouseEvent evt) {
      this.dispatchEvent(evt);
   }

   public void mouseDragged(MouseEvent evt) {
      this.dispatchEvent(evt);
   }

   public void mouseWheelMoved(MouseWheelEvent evt) {
      this.dispatchEvent(evt);
   }

   public void keyPressed(KeyEvent evt) {
      this.dispatchEvent(evt);
   }

   public void keyReleased(KeyEvent evt) {
      this.dispatchEvent(evt);
   }

   public void keyTyped(KeyEvent evt) {
      this.dispatchEvent(evt);
   }

   public void addGraphicsNodeMouseListener(GraphicsNodeMouseListener l) {
      if (this.glisteners == null) {
         this.glisteners = new EventListenerList();
      }

      this.glisteners.add(GraphicsNodeMouseListener.class, l);
   }

   public void removeGraphicsNodeMouseListener(GraphicsNodeMouseListener l) {
      if (this.glisteners != null) {
         this.glisteners.remove(GraphicsNodeMouseListener.class, l);
      }

   }

   public void addGraphicsNodeMouseWheelListener(GraphicsNodeMouseWheelListener l) {
      if (this.glisteners == null) {
         this.glisteners = new EventListenerList();
      }

      this.glisteners.add(GraphicsNodeMouseWheelListener.class, l);
   }

   public void removeGraphicsNodeMouseWheelListener(GraphicsNodeMouseWheelListener l) {
      if (this.glisteners != null) {
         this.glisteners.remove(GraphicsNodeMouseWheelListener.class, l);
      }

   }

   public void addGraphicsNodeKeyListener(GraphicsNodeKeyListener l) {
      if (this.glisteners == null) {
         this.glisteners = new EventListenerList();
      }

      this.glisteners.add(GraphicsNodeKeyListener.class, l);
   }

   public void removeGraphicsNodeKeyListener(GraphicsNodeKeyListener l) {
      if (this.glisteners != null) {
         this.glisteners.remove(GraphicsNodeKeyListener.class, l);
      }

   }

   public EventListener[] getListeners(Class listenerType) {
      Object array = Array.newInstance(listenerType, this.glisteners.getListenerCount(listenerType));
      Object[] pairElements = this.glisteners.getListenerList();
      int i = 0;

      for(int j = 0; i < pairElements.length - 1; i += 2) {
         if (pairElements[i].equals(listenerType)) {
            Array.set(array, j, pairElements[i + 1]);
            ++j;
         }
      }

      return (EventListener[])((EventListener[])array);
   }

   public void setEventDispatchEnabled(boolean b) {
      this.eventDispatchEnabled = b;
      if (this.eventDispatchEnabled) {
         while(this.eventQueue.size() > 0) {
            EventObject evt = (EventObject)this.eventQueue.remove(0);
            this.dispatchEvent(evt);
         }
      }

   }

   public void setEventQueueMaxSize(int n) {
      this.eventQueueMaxSize = n;
      if (n == 0) {
         this.eventQueue.clear();
      }

      while(this.eventQueue.size() > this.eventQueueMaxSize) {
         this.eventQueue.remove(0);
      }

   }

   public void dispatchEvent(EventObject evt) {
      if (this.root != null) {
         if (this.eventDispatchEnabled) {
            if (evt instanceof MouseWheelEvent) {
               this.dispatchMouseWheelEvent((MouseWheelEvent)evt);
            } else if (evt instanceof MouseEvent) {
               this.dispatchMouseEvent((MouseEvent)evt);
            } else if (evt instanceof KeyEvent) {
               InputEvent e = (InputEvent)evt;
               if (this.isNodeIncrementEvent(e)) {
                  this.incrementKeyTarget();
               } else if (this.isNodeDecrementEvent(e)) {
                  this.decrementKeyTarget();
               } else {
                  this.dispatchKeyEvent((KeyEvent)evt);
               }
            }

         } else {
            if (this.eventQueueMaxSize > 0) {
               this.eventQueue.add(evt);

               while(this.eventQueue.size() > this.eventQueueMaxSize) {
                  this.eventQueue.remove(0);
               }
            }

         }
      }
   }

   protected int getCurrentLockState() {
      Toolkit t = Toolkit.getDefaultToolkit();
      int lockState = 0;

      try {
         if (t.getLockingKeyState(262)) {
            ++lockState;
         }
      } catch (UnsupportedOperationException var7) {
      }

      lockState <<= 1;

      try {
         if (t.getLockingKeyState(145)) {
            ++lockState;
         }
      } catch (UnsupportedOperationException var6) {
      }

      lockState <<= 1;

      try {
         if (t.getLockingKeyState(144)) {
            ++lockState;
         }
      } catch (UnsupportedOperationException var5) {
      }

      lockState <<= 1;

      try {
         if (t.getLockingKeyState(20)) {
            ++lockState;
         }
      } catch (UnsupportedOperationException var4) {
      }

      return lockState;
   }

   protected void dispatchKeyEvent(KeyEvent evt) {
      this.currentKeyEventTarget = this.lastHit;
      GraphicsNode target = this.currentKeyEventTarget == null ? this.root : this.currentKeyEventTarget;
      this.processKeyEvent(new GraphicsNodeKeyEvent(target, evt.getID(), evt.getWhen(), evt.getModifiersEx(), this.getCurrentLockState(), evt.getKeyCode(), evt.getKeyChar(), evt.getKeyLocation()));
   }

   protected void dispatchMouseEvent(MouseEvent evt) {
      Point2D p = new Point2D.Float((float)evt.getX(), (float)evt.getY());
      Point2D gnp = p;
      if (this.baseTransform != null) {
         gnp = this.baseTransform.transform(p, (Point2D)null);
      }

      GraphicsNode node = this.root.nodeHitAt((Point2D)gnp);
      if (node != null) {
         try {
            node.getGlobalTransform().createInverse().transform((Point2D)gnp, (Point2D)gnp);
         } catch (NoninvertibleTransformException var8) {
         }
      }

      Point screenPos;
      if (!evt.getComponent().isShowing()) {
         screenPos = new Point(0, 0);
      } else {
         screenPos = evt.getComponent().getLocationOnScreen();
         screenPos.x += evt.getX();
         screenPos.y += evt.getY();
      }

      int currentLockState = this.getCurrentLockState();
      GraphicsNodeMouseEvent gvtevt;
      if (this.lastHit != node) {
         if (this.lastHit != null) {
            gvtevt = new GraphicsNodeMouseEvent(this.lastHit, 505, evt.getWhen(), evt.getModifiersEx(), currentLockState, evt.getButton(), (float)((Point2D)gnp).getX(), (float)((Point2D)gnp).getY(), (int)Math.floor(p.getX()), (int)Math.floor(p.getY()), screenPos.x, screenPos.y, evt.getClickCount(), node);
            this.processMouseEvent(gvtevt);
         }

         if (node != null) {
            gvtevt = new GraphicsNodeMouseEvent(node, 504, evt.getWhen(), evt.getModifiersEx(), currentLockState, evt.getButton(), (float)((Point2D)gnp).getX(), (float)((Point2D)gnp).getY(), (int)Math.floor(p.getX()), (int)Math.floor(p.getY()), screenPos.x, screenPos.y, evt.getClickCount(), this.lastHit);
            this.processMouseEvent(gvtevt);
         }
      }

      if (node != null) {
         gvtevt = new GraphicsNodeMouseEvent(node, evt.getID(), evt.getWhen(), evt.getModifiersEx(), currentLockState, evt.getButton(), (float)((Point2D)gnp).getX(), (float)((Point2D)gnp).getY(), (int)Math.floor(p.getX()), (int)Math.floor(p.getY()), screenPos.x, screenPos.y, evt.getClickCount(), (GraphicsNode)null);
         this.processMouseEvent(gvtevt);
      } else {
         gvtevt = new GraphicsNodeMouseEvent(this.root, evt.getID(), evt.getWhen(), evt.getModifiersEx(), currentLockState, evt.getButton(), (float)((Point2D)gnp).getX(), (float)((Point2D)gnp).getY(), (int)Math.floor(p.getX()), (int)Math.floor(p.getY()), screenPos.x, screenPos.y, evt.getClickCount(), (GraphicsNode)null);
         this.processMouseEvent(gvtevt);
      }

      this.lastHit = node;
   }

   protected void dispatchMouseWheelEvent(MouseWheelEvent evt) {
      if (this.lastHit != null) {
         this.processMouseWheelEvent(new GraphicsNodeMouseWheelEvent(this.lastHit, evt.getID(), evt.getWhen(), evt.getModifiersEx(), this.getCurrentLockState(), evt.getWheelRotation()));
      }

   }

   protected void processMouseEvent(GraphicsNodeMouseEvent evt) {
      if (this.glisteners != null) {
         GraphicsNodeMouseListener[] listeners = (GraphicsNodeMouseListener[])((GraphicsNodeMouseListener[])this.getListeners(GraphicsNodeMouseListener.class));
         GraphicsNodeMouseListener[] var3;
         int var4;
         int var5;
         GraphicsNodeMouseListener listener5;
         switch (evt.getID()) {
            case 500:
               var3 = listeners;
               var4 = listeners.length;

               for(var5 = 0; var5 < var4; ++var5) {
                  listener5 = var3[var5];
                  listener5.mouseClicked(evt);
               }

               return;
            case 501:
               var3 = listeners;
               var4 = listeners.length;

               for(var5 = 0; var5 < var4; ++var5) {
                  listener5 = var3[var5];
                  listener5.mousePressed(evt);
               }

               return;
            case 502:
               var3 = listeners;
               var4 = listeners.length;

               for(var5 = 0; var5 < var4; ++var5) {
                  listener5 = var3[var5];
                  listener5.mouseReleased(evt);
               }

               return;
            case 503:
               var3 = listeners;
               var4 = listeners.length;

               for(var5 = 0; var5 < var4; ++var5) {
                  listener5 = var3[var5];
                  listener5.mouseMoved(evt);
               }

               return;
            case 504:
               var3 = listeners;
               var4 = listeners.length;

               for(var5 = 0; var5 < var4; ++var5) {
                  listener5 = var3[var5];
                  listener5.mouseEntered(evt);
               }

               return;
            case 505:
               var3 = listeners;
               var4 = listeners.length;

               for(var5 = 0; var5 < var4; ++var5) {
                  listener5 = var3[var5];
                  listener5.mouseExited(evt);
               }

               return;
            case 506:
               var3 = listeners;
               var4 = listeners.length;

               for(var5 = 0; var5 < var4; ++var5) {
                  listener5 = var3[var5];
                  listener5.mouseDragged(evt);
               }

               return;
            default:
               throw new IllegalArgumentException("Unknown Mouse Event type: " + evt.getID());
         }
      }
   }

   protected void processMouseWheelEvent(GraphicsNodeMouseWheelEvent evt) {
      if (this.glisteners != null) {
         GraphicsNodeMouseWheelListener[] listeners = (GraphicsNodeMouseWheelListener[])((GraphicsNodeMouseWheelListener[])this.getListeners(GraphicsNodeMouseWheelListener.class));
         GraphicsNodeMouseWheelListener[] var3 = listeners;
         int var4 = listeners.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            GraphicsNodeMouseWheelListener listener = var3[var5];
            listener.mouseWheelMoved(evt);
         }
      }

   }

   public void processKeyEvent(GraphicsNodeKeyEvent evt) {
      if (this.glisteners != null) {
         GraphicsNodeKeyListener[] listeners = (GraphicsNodeKeyListener[])((GraphicsNodeKeyListener[])this.getListeners(GraphicsNodeKeyListener.class));
         GraphicsNodeKeyListener[] var3;
         int var4;
         int var5;
         GraphicsNodeKeyListener listener1;
         label35:
         switch (evt.getID()) {
            case 400:
               var3 = listeners;
               var4 = listeners.length;
               var5 = 0;

               while(true) {
                  if (var5 >= var4) {
                     break label35;
                  }

                  listener1 = var3[var5];
                  listener1.keyTyped(evt);
                  ++var5;
               }
            case 401:
               var3 = listeners;
               var4 = listeners.length;
               var5 = 0;

               while(true) {
                  if (var5 >= var4) {
                     break label35;
                  }

                  listener1 = var3[var5];
                  listener1.keyPressed(evt);
                  ++var5;
               }
            case 402:
               var3 = listeners;
               var4 = listeners.length;
               var5 = 0;

               while(true) {
                  if (var5 >= var4) {
                     break label35;
                  }

                  listener1 = var3[var5];
                  listener1.keyReleased(evt);
                  ++var5;
               }
            default:
               throw new IllegalArgumentException("Unknown Key Event type: " + evt.getID());
         }
      }

      evt.consume();
   }

   private void incrementKeyTarget() {
      throw new UnsupportedOperationException("Increment not implemented.");
   }

   private void decrementKeyTarget() {
      throw new UnsupportedOperationException("Decrement not implemented.");
   }

   public void setNodeIncrementEvent(InputEvent e) {
      this.nodeIncrementEventID = e.getID();
      if (e instanceof KeyEvent) {
         this.nodeIncrementEventCode = ((KeyEvent)e).getKeyCode();
      }

      this.nodeIncrementEventModifiers = e.getModifiersEx();
   }

   public void setNodeDecrementEvent(InputEvent e) {
      this.nodeDecrementEventID = e.getID();
      if (e instanceof KeyEvent) {
         this.nodeDecrementEventCode = ((KeyEvent)e).getKeyCode();
      }

      this.nodeDecrementEventModifiers = e.getModifiersEx();
   }

   protected boolean isNodeIncrementEvent(InputEvent e) {
      if (e.getID() != this.nodeIncrementEventID) {
         return false;
      } else if (e instanceof KeyEvent && ((KeyEvent)e).getKeyCode() != this.nodeIncrementEventCode) {
         return false;
      } else {
         return (e.getModifiersEx() & this.nodeIncrementEventModifiers) != 0;
      }
   }

   protected boolean isNodeDecrementEvent(InputEvent e) {
      if (e.getID() != this.nodeDecrementEventID) {
         return false;
      } else if (e instanceof KeyEvent && ((KeyEvent)e).getKeyCode() != this.nodeDecrementEventCode) {
         return false;
      } else {
         return (e.getModifiersEx() & this.nodeDecrementEventModifiers) != 0;
      }
   }

   protected static boolean isMetaDown(int modifiers) {
      return (modifiers & 256) != 0;
   }
}
