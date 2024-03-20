package org.apache.batik.swing.gvt;

import java.awt.AWTPermission;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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
import java.awt.image.BufferedImage;
import java.text.CharacterIterator;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JComponent;
import org.apache.batik.bridge.Mark;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.event.AWTEventDispatcher;
import org.apache.batik.gvt.event.EventDispatcher;
import org.apache.batik.gvt.event.SelectionAdapter;
import org.apache.batik.gvt.event.SelectionEvent;
import org.apache.batik.gvt.renderer.ConcreteImageRendererFactory;
import org.apache.batik.gvt.renderer.ImageRenderer;
import org.apache.batik.gvt.renderer.ImageRendererFactory;
import org.apache.batik.util.HaltingThread;
import org.apache.batik.util.Platform;

public class JGVTComponent extends JComponent {
   protected Listener listener;
   protected GVTTreeRenderer gvtTreeRenderer;
   protected GraphicsNode gvtRoot;
   protected ImageRendererFactory rendererFactory;
   protected ImageRenderer renderer;
   protected List gvtTreeRendererListeners;
   protected boolean needRender;
   protected boolean progressivePaint;
   protected HaltingThread progressivePaintThread;
   protected BufferedImage image;
   protected AffineTransform initialTransform;
   protected AffineTransform renderingTransform;
   protected AffineTransform paintingTransform;
   protected List interactors;
   protected Interactor interactor;
   protected List overlays;
   protected List jgvtListeners;
   protected AWTEventDispatcher eventDispatcher;
   protected TextSelectionManager textSelectionManager;
   protected boolean doubleBufferedRendering;
   protected boolean eventsEnabled;
   protected boolean selectableText;
   protected boolean useUnixTextSelection;
   protected boolean suspendInteractions;
   protected boolean disableInteractions;

   public JGVTComponent() {
      this(false, false);
   }

   public JGVTComponent(boolean eventsEnabled, boolean selectableText) {
      this.rendererFactory = new ConcreteImageRendererFactory();
      this.gvtTreeRendererListeners = Collections.synchronizedList(new LinkedList());
      this.initialTransform = new AffineTransform();
      this.renderingTransform = new AffineTransform();
      this.interactors = new LinkedList();
      this.overlays = new LinkedList();
      this.jgvtListeners = null;
      this.useUnixTextSelection = true;
      this.setBackground(Color.white);
      this.eventsEnabled = eventsEnabled;
      this.selectableText = selectableText;
      this.listener = this.createListener();
      this.addAWTListeners();
      this.addGVTTreeRendererListener(this.listener);
      this.addComponentListener(new ComponentAdapter() {
         public void componentResized(ComponentEvent e) {
            if (JGVTComponent.this.updateRenderingTransform()) {
               JGVTComponent.this.scheduleGVTRendering();
            }

         }
      });
   }

   protected Listener createListener() {
      return new Listener();
   }

   protected void addAWTListeners() {
      this.addKeyListener(this.listener);
      this.addMouseListener(this.listener);
      this.addMouseMotionListener(this.listener);
      this.addMouseWheelListener(this.listener);
   }

   public void setDisableInteractions(boolean b) {
      this.disableInteractions = b;
   }

   public boolean getDisableInteractions() {
      return this.disableInteractions;
   }

   public void setUseUnixTextSelection(boolean b) {
      this.useUnixTextSelection = b;
   }

   public void getUseUnixTextSelection(boolean b) {
      this.useUnixTextSelection = b;
   }

   public List getInteractors() {
      return this.interactors;
   }

   public List getOverlays() {
      return this.overlays;
   }

   public BufferedImage getOffScreen() {
      return this.image;
   }

   public void addJGVTComponentListener(JGVTComponentListener listener) {
      if (this.jgvtListeners == null) {
         this.jgvtListeners = new LinkedList();
      }

      this.jgvtListeners.add(listener);
   }

   public void removeJGVTComponentListener(JGVTComponentListener listener) {
      if (this.jgvtListeners != null) {
         this.jgvtListeners.remove(listener);
      }
   }

   public void resetRenderingTransform() {
      this.setRenderingTransform(this.initialTransform);
   }

   public void stopProcessing() {
      if (this.gvtTreeRenderer != null) {
         this.needRender = false;
         this.gvtTreeRenderer.halt();
         this.haltProgressivePaintThread();
      }

   }

   public GraphicsNode getGraphicsNode() {
      return this.gvtRoot;
   }

   public void setGraphicsNode(GraphicsNode gn) {
      this.setGraphicsNode(gn, true);
      this.initialTransform = new AffineTransform();
      this.updateRenderingTransform();
      this.setRenderingTransform(this.initialTransform, true);
   }

   protected void setGraphicsNode(GraphicsNode gn, boolean createDispatcher) {
      this.gvtRoot = gn;
      if (gn != null && createDispatcher) {
         this.initializeEventHandling();
      }

      if (this.eventDispatcher != null) {
         this.eventDispatcher.setRootNode(gn);
      }

   }

   protected void initializeEventHandling() {
      if (this.eventsEnabled) {
         this.eventDispatcher = this.createEventDispatcher();
         if (this.selectableText) {
            this.textSelectionManager = this.createTextSelectionManager(this.eventDispatcher);
            this.textSelectionManager.addSelectionListener(new UnixTextSelectionListener());
         }
      }

   }

   protected AWTEventDispatcher createEventDispatcher() {
      return new AWTEventDispatcher();
   }

   protected TextSelectionManager createTextSelectionManager(EventDispatcher ed) {
      return new TextSelectionManager(this, ed);
   }

   public TextSelectionManager getTextSelectionManager() {
      return this.textSelectionManager;
   }

   public void setSelectionOverlayColor(Color color) {
      if (this.textSelectionManager != null) {
         this.textSelectionManager.setSelectionOverlayColor(color);
      }

   }

   public Color getSelectionOverlayColor() {
      return this.textSelectionManager != null ? this.textSelectionManager.getSelectionOverlayColor() : null;
   }

   public void setSelectionOverlayStrokeColor(Color color) {
      if (this.textSelectionManager != null) {
         this.textSelectionManager.setSelectionOverlayStrokeColor(color);
      }

   }

   public Color getSelectionOverlayStrokeColor() {
      return this.textSelectionManager != null ? this.textSelectionManager.getSelectionOverlayStrokeColor() : null;
   }

   public void setSelectionOverlayXORMode(boolean state) {
      if (this.textSelectionManager != null) {
         this.textSelectionManager.setSelectionOverlayXORMode(state);
      }

   }

   public boolean isSelectionOverlayXORMode() {
      return this.textSelectionManager != null ? this.textSelectionManager.isSelectionOverlayXORMode() : false;
   }

   public void select(Mark start, Mark end) {
      if (this.textSelectionManager != null) {
         this.textSelectionManager.setSelection(start, end);
      }

   }

   public void deselectAll() {
      if (this.textSelectionManager != null) {
         this.textSelectionManager.clearSelection();
      }

   }

   public void setProgressivePaint(boolean b) {
      if (this.progressivePaint != b) {
         this.progressivePaint = b;
         this.haltProgressivePaintThread();
      }

   }

   public boolean getProgressivePaint() {
      return this.progressivePaint;
   }

   public Rectangle getRenderRect() {
      Dimension d = this.getSize();
      return new Rectangle(0, 0, d.width, d.height);
   }

   public void immediateRepaint() {
      if (EventQueue.isDispatchThread()) {
         Rectangle visRect = this.getRenderRect();
         if (this.doubleBufferedRendering) {
            this.repaint(visRect.x, visRect.y, visRect.width, visRect.height);
         } else {
            this.paintImmediately(visRect.x, visRect.y, visRect.width, visRect.height);
         }
      } else {
         try {
            EventQueue.invokeAndWait(new Runnable() {
               public void run() {
                  Rectangle visRect = JGVTComponent.this.getRenderRect();
                  if (JGVTComponent.this.doubleBufferedRendering) {
                     JGVTComponent.this.repaint(visRect.x, visRect.y, visRect.width, visRect.height);
                  } else {
                     JGVTComponent.this.paintImmediately(visRect.x, visRect.y, visRect.width, visRect.height);
                  }

               }
            });
         } catch (Exception var2) {
         }
      }

   }

   public void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2d = (Graphics2D)g;
      Rectangle visRect = this.getRenderRect();
      g2d.setComposite(AlphaComposite.SrcOver);
      g2d.setPaint(this.getBackground());
      g2d.fillRect(visRect.x, visRect.y, visRect.width, visRect.height);
      if (this.image != null) {
         if (this.paintingTransform != null) {
            g2d.transform(this.paintingTransform);
         }

         g2d.drawRenderedImage(this.image, (AffineTransform)null);
         g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
         Iterator var4 = this.overlays.iterator();

         while(var4.hasNext()) {
            Object overlay = var4.next();
            ((Overlay)overlay).paint(g);
         }
      }

   }

   public void setPaintingTransform(AffineTransform at) {
      this.paintingTransform = at;
      this.immediateRepaint();
   }

   public AffineTransform getPaintingTransform() {
      return this.paintingTransform;
   }

   public void setRenderingTransform(AffineTransform at) {
      this.setRenderingTransform(at, true);
   }

   public void setRenderingTransform(AffineTransform at, boolean performRedraw) {
      this.renderingTransform = new AffineTransform(at);
      this.suspendInteractions = true;
      if (this.eventDispatcher != null) {
         try {
            this.eventDispatcher.setBaseTransform(this.renderingTransform.createInverse());
         } catch (NoninvertibleTransformException var6) {
            this.handleException(var6);
         }
      }

      if (this.jgvtListeners != null) {
         Iterator iter = this.jgvtListeners.iterator();
         ComponentEvent ce = new ComponentEvent(this, 1337);

         while(iter.hasNext()) {
            JGVTComponentListener l = (JGVTComponentListener)iter.next();
            l.componentTransformChanged(ce);
         }
      }

      if (performRedraw) {
         this.scheduleGVTRendering();
      }

   }

   public AffineTransform getInitialTransform() {
      return new AffineTransform(this.initialTransform);
   }

   public AffineTransform getRenderingTransform() {
      return new AffineTransform(this.renderingTransform);
   }

   public void setDoubleBufferedRendering(boolean b) {
      this.doubleBufferedRendering = b;
   }

   public boolean getDoubleBufferedRendering() {
      return this.doubleBufferedRendering;
   }

   public void addGVTTreeRendererListener(GVTTreeRendererListener l) {
      this.gvtTreeRendererListeners.add(l);
   }

   public void removeGVTTreeRendererListener(GVTTreeRendererListener l) {
      this.gvtTreeRendererListeners.remove(l);
   }

   public void flush() {
      this.renderer.flush();
   }

   public void flush(Rectangle r) {
      this.renderer.flush(r);
   }

   protected ImageRenderer createImageRenderer() {
      return this.rendererFactory.createStaticImageRenderer();
   }

   protected void renderGVTTree() {
      Rectangle visRect = this.getRenderRect();
      if (this.gvtRoot != null && visRect.width > 0 && visRect.height > 0) {
         if (this.renderer == null || this.renderer.getTree() != this.gvtRoot) {
            this.renderer = this.createImageRenderer();
            this.renderer.setTree(this.gvtRoot);
         }

         AffineTransform inv;
         try {
            inv = this.renderingTransform.createInverse();
         } catch (NoninvertibleTransformException var6) {
            throw new IllegalStateException("NoninvertibleTransformEx:" + var6.getMessage());
         }

         Shape s = inv.createTransformedShape(visRect);
         this.gvtTreeRenderer = new GVTTreeRenderer(this.renderer, this.renderingTransform, this.doubleBufferedRendering, s, visRect.width, visRect.height);
         this.gvtTreeRenderer.setPriority(1);
         Iterator var4 = this.gvtTreeRendererListeners.iterator();

         while(var4.hasNext()) {
            Object gvtTreeRendererListener = var4.next();
            this.gvtTreeRenderer.addGVTTreeRendererListener((GVTTreeRendererListener)gvtTreeRendererListener);
         }

         if (this.eventDispatcher != null) {
            this.eventDispatcher.setEventDispatchEnabled(false);
         }

         this.gvtTreeRenderer.start();
      }
   }

   protected boolean computeRenderingTransform() {
      this.initialTransform = new AffineTransform();
      if (!this.initialTransform.equals(this.renderingTransform)) {
         this.setRenderingTransform(this.initialTransform, false);
         return true;
      } else {
         return false;
      }
   }

   protected boolean updateRenderingTransform() {
      return false;
   }

   protected void handleException(Exception e) {
   }

   protected void releaseRenderingReferences() {
      this.eventDispatcher = null;
      if (this.textSelectionManager != null) {
         this.overlays.remove(this.textSelectionManager.getSelectionOverlay());
         this.textSelectionManager = null;
      }

      this.renderer = null;
      this.image = null;
      this.gvtRoot = null;
   }

   protected void scheduleGVTRendering() {
      if (this.gvtTreeRenderer != null) {
         this.needRender = true;
         this.gvtTreeRenderer.halt();
      } else {
         this.renderGVTTree();
      }

   }

   private void haltProgressivePaintThread() {
      if (this.progressivePaintThread != null) {
         this.progressivePaintThread.halt();
         this.progressivePaintThread = null;
      }

   }

   protected class UnixTextSelectionListener extends SelectionAdapter {
      public void selectionDone(SelectionEvent evt) {
         if (JGVTComponent.this.useUnixTextSelection) {
            Object o = evt.getSelection();
            if (o instanceof CharacterIterator) {
               CharacterIterator iter = (CharacterIterator)o;
               SecurityManager securityManager = System.getSecurityManager();
               if (securityManager != null) {
                  try {
                     securityManager.checkPermission(new AWTPermission("accessClipboard"));
                  } catch (SecurityException var8) {
                     return;
                  }
               }

               int sz = iter.getEndIndex() - iter.getBeginIndex();
               if (sz != 0) {
                  char[] cbuff = new char[sz];
                  cbuff[0] = iter.first();

                  for(int i = 1; i < cbuff.length; ++i) {
                     cbuff[i] = iter.next();
                  }

                  final String strSel = new String(cbuff);
                  (new Thread() {
                     public void run() {
                        Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
                        StringSelection sel = new StringSelection(strSel);
                        cb.setContents(sel, sel);
                     }
                  }).start();
               }
            }
         }
      }
   }

   protected class Listener implements GVTTreeRendererListener, KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {
      boolean checkClick = false;
      boolean hadDrag = false;
      int startX;
      int startY;
      long startTime;
      long fakeClickTime;
      int MAX_DISP = 16;
      long CLICK_TIME = 200L;

      public void gvtRenderingPrepare(GVTTreeRendererEvent e) {
         JGVTComponent.this.suspendInteractions = true;
         if (!JGVTComponent.this.progressivePaint && !JGVTComponent.this.doubleBufferedRendering) {
            JGVTComponent.this.image = null;
         }

      }

      public void gvtRenderingStarted(GVTTreeRendererEvent e) {
         if (JGVTComponent.this.progressivePaint && !JGVTComponent.this.doubleBufferedRendering) {
            JGVTComponent.this.image = e.getImage();
            JGVTComponent.this.progressivePaintThread = new HaltingThread() {
               public void run() {
                  final Thread thisThread = this;

                  try {
                     while(!hasBeenHalted()) {
                        EventQueue.invokeLater(new Runnable() {
                           public void run() {
                              if (JGVTComponent.this.progressivePaintThread == thisThread) {
                                 Rectangle vRect = JGVTComponent.this.getRenderRect();
                                 JGVTComponent.this.repaint(vRect.x, vRect.y, vRect.width, vRect.height);
                              }

                           }
                        });
                        sleep(200L);
                     }
                  } catch (InterruptedException var3) {
                  } catch (ThreadDeath var4) {
                     throw var4;
                  } catch (Throwable var5) {
                     var5.printStackTrace();
                  }

               }
            };
            JGVTComponent.this.progressivePaintThread.setPriority(2);
            JGVTComponent.this.progressivePaintThread.start();
         }

         if (!JGVTComponent.this.doubleBufferedRendering) {
            JGVTComponent.this.paintingTransform = null;
            JGVTComponent.this.suspendInteractions = false;
         }

      }

      public void gvtRenderingCompleted(GVTTreeRendererEvent e) {
         JGVTComponent.this.haltProgressivePaintThread();
         if (JGVTComponent.this.doubleBufferedRendering) {
            JGVTComponent.this.paintingTransform = null;
            JGVTComponent.this.suspendInteractions = false;
         }

         JGVTComponent.this.gvtTreeRenderer = null;
         if (JGVTComponent.this.needRender) {
            JGVTComponent.this.renderGVTTree();
            JGVTComponent.this.needRender = false;
         } else {
            JGVTComponent.this.image = e.getImage();
            JGVTComponent.this.immediateRepaint();
         }

         if (JGVTComponent.this.eventDispatcher != null) {
            JGVTComponent.this.eventDispatcher.setEventDispatchEnabled(true);
         }

      }

      public void gvtRenderingCancelled(GVTTreeRendererEvent e) {
         this.renderingStopped();
      }

      public void gvtRenderingFailed(GVTTreeRendererEvent e) {
         this.renderingStopped();
      }

      private void renderingStopped() {
         JGVTComponent.this.haltProgressivePaintThread();
         if (JGVTComponent.this.doubleBufferedRendering) {
            JGVTComponent.this.suspendInteractions = false;
         }

         JGVTComponent.this.gvtTreeRenderer = null;
         if (JGVTComponent.this.needRender) {
            JGVTComponent.this.renderGVTTree();
            JGVTComponent.this.needRender = false;
         } else {
            JGVTComponent.this.immediateRepaint();
         }

         if (JGVTComponent.this.eventDispatcher != null) {
            JGVTComponent.this.eventDispatcher.setEventDispatchEnabled(true);
         }

      }

      public void keyTyped(KeyEvent e) {
         this.selectInteractor(e);
         if (JGVTComponent.this.interactor != null) {
            JGVTComponent.this.interactor.keyTyped(e);
            this.deselectInteractor();
         } else if (JGVTComponent.this.eventDispatcher != null) {
            this.dispatchKeyTyped(e);
         }

      }

      protected void dispatchKeyTyped(KeyEvent e) {
         JGVTComponent.this.eventDispatcher.keyTyped(e);
      }

      public void keyPressed(KeyEvent e) {
         this.selectInteractor(e);
         if (JGVTComponent.this.interactor != null) {
            JGVTComponent.this.interactor.keyPressed(e);
            this.deselectInteractor();
         } else if (JGVTComponent.this.eventDispatcher != null) {
            this.dispatchKeyPressed(e);
         }

      }

      protected void dispatchKeyPressed(KeyEvent e) {
         JGVTComponent.this.eventDispatcher.keyPressed(e);
      }

      public void keyReleased(KeyEvent e) {
         this.selectInteractor(e);
         if (JGVTComponent.this.interactor != null) {
            JGVTComponent.this.interactor.keyReleased(e);
            this.deselectInteractor();
         } else if (JGVTComponent.this.eventDispatcher != null) {
            this.dispatchKeyReleased(e);
         }

      }

      protected void dispatchKeyReleased(KeyEvent e) {
         JGVTComponent.this.eventDispatcher.keyReleased(e);
      }

      public void mouseClicked(MouseEvent e) {
         if (this.fakeClickTime != e.getWhen()) {
            this.handleMouseClicked(e);
         }

      }

      public void handleMouseClicked(MouseEvent e) {
         this.selectInteractor(e);
         if (JGVTComponent.this.interactor != null) {
            JGVTComponent.this.interactor.mouseClicked(e);
            this.deselectInteractor();
         } else if (JGVTComponent.this.eventDispatcher != null) {
            this.dispatchMouseClicked(e);
         }

      }

      protected void dispatchMouseClicked(MouseEvent e) {
         JGVTComponent.this.eventDispatcher.mouseClicked(e);
      }

      public void mousePressed(MouseEvent e) {
         this.startX = e.getX();
         this.startY = e.getY();
         this.startTime = e.getWhen();
         this.checkClick = true;
         this.selectInteractor(e);
         if (JGVTComponent.this.interactor != null) {
            JGVTComponent.this.interactor.mousePressed(e);
            this.deselectInteractor();
         } else if (JGVTComponent.this.eventDispatcher != null) {
            this.dispatchMousePressed(e);
         }

      }

      protected void dispatchMousePressed(MouseEvent e) {
         JGVTComponent.this.eventDispatcher.mousePressed(e);
      }

      public void mouseReleased(MouseEvent e) {
         if (this.checkClick && this.hadDrag) {
            int dx = this.startX - e.getX();
            int dy = this.startY - e.getY();
            long cTime = e.getWhen();
            if (dx * dx + dy * dy < this.MAX_DISP && cTime - this.startTime < this.CLICK_TIME) {
               MouseEvent click = new MouseEvent(e.getComponent(), 500, e.getWhen(), e.getModifiersEx(), e.getX(), e.getY(), e.getClickCount(), e.isPopupTrigger());
               this.fakeClickTime = click.getWhen();
               this.handleMouseClicked(click);
            }
         }

         this.checkClick = false;
         this.hadDrag = false;
         this.selectInteractor(e);
         if (JGVTComponent.this.interactor != null) {
            JGVTComponent.this.interactor.mouseReleased(e);
            this.deselectInteractor();
         } else if (JGVTComponent.this.eventDispatcher != null) {
            this.dispatchMouseReleased(e);
         }

      }

      protected void dispatchMouseReleased(MouseEvent e) {
         JGVTComponent.this.eventDispatcher.mouseReleased(e);
      }

      public void mouseEntered(MouseEvent e) {
         this.selectInteractor(e);
         if (JGVTComponent.this.interactor != null) {
            JGVTComponent.this.interactor.mouseEntered(e);
            this.deselectInteractor();
         } else if (JGVTComponent.this.eventDispatcher != null) {
            this.dispatchMouseEntered(e);
         }

      }

      protected void dispatchMouseEntered(MouseEvent e) {
         JGVTComponent.this.eventDispatcher.mouseEntered(e);
      }

      public void mouseExited(MouseEvent e) {
         this.selectInteractor(e);
         if (JGVTComponent.this.interactor != null) {
            JGVTComponent.this.interactor.mouseExited(e);
            this.deselectInteractor();
         } else if (JGVTComponent.this.eventDispatcher != null) {
            this.dispatchMouseExited(e);
         }

      }

      protected void dispatchMouseExited(MouseEvent e) {
         JGVTComponent.this.eventDispatcher.mouseExited(e);
      }

      public void mouseDragged(MouseEvent e) {
         this.hadDrag = true;
         int dx = this.startX - e.getX();
         int dy = this.startY - e.getY();
         if (dx * dx + dy * dy > this.MAX_DISP) {
            this.checkClick = false;
         }

         this.selectInteractor(e);
         if (JGVTComponent.this.interactor != null) {
            JGVTComponent.this.interactor.mouseDragged(e);
            this.deselectInteractor();
         } else if (JGVTComponent.this.eventDispatcher != null) {
            this.dispatchMouseDragged(e);
         }

      }

      protected void dispatchMouseDragged(MouseEvent e) {
         JGVTComponent.this.eventDispatcher.mouseDragged(e);
      }

      public void mouseMoved(MouseEvent e) {
         this.selectInteractor(e);
         if (JGVTComponent.this.interactor != null) {
            if (Platform.isOSX && JGVTComponent.this.interactor instanceof AbstractZoomInteractor) {
               this.mouseDragged(e);
            } else {
               JGVTComponent.this.interactor.mouseMoved(e);
            }

            this.deselectInteractor();
         } else if (JGVTComponent.this.eventDispatcher != null) {
            this.dispatchMouseMoved(e);
         }

      }

      protected void dispatchMouseMoved(MouseEvent e) {
         JGVTComponent.this.eventDispatcher.mouseMoved(e);
      }

      public void mouseWheelMoved(MouseWheelEvent e) {
         if (JGVTComponent.this.eventDispatcher != null) {
            this.dispatchMouseWheelMoved(e);
         }

      }

      protected void dispatchMouseWheelMoved(MouseWheelEvent e) {
         JGVTComponent.this.eventDispatcher.mouseWheelMoved(e);
      }

      protected void selectInteractor(InputEvent ie) {
         if (!JGVTComponent.this.disableInteractions && !JGVTComponent.this.suspendInteractions && JGVTComponent.this.interactor == null && JGVTComponent.this.gvtRoot != null) {
            Iterator var2 = JGVTComponent.this.interactors.iterator();

            while(var2.hasNext()) {
               Object interactor1 = var2.next();
               Interactor i = (Interactor)interactor1;
               if (i.startInteraction(ie)) {
                  JGVTComponent.this.interactor = i;
                  break;
               }
            }
         }

      }

      protected void deselectInteractor() {
         if (JGVTComponent.this.interactor.endInteraction()) {
            JGVTComponent.this.interactor = null;
         }

      }
   }
}
