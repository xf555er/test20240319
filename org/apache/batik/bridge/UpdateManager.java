package org.apache.batik.bridge;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.batik.anim.dom.SVGOMDocument;
import org.apache.batik.bridge.svg12.DefaultXBLManager;
import org.apache.batik.bridge.svg12.SVG12BridgeContext;
import org.apache.batik.bridge.svg12.SVG12ScriptingEnvironment;
import org.apache.batik.dom.events.AbstractEvent;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.RootGraphicsNode;
import org.apache.batik.gvt.UpdateTracker;
import org.apache.batik.gvt.renderer.ImageRenderer;
import org.apache.batik.util.EventDispatcher;
import org.apache.batik.util.RunnableQueue;
import org.w3c.dom.Document;
import org.w3c.dom.events.DocumentEvent;
import org.w3c.dom.events.EventTarget;

public class UpdateManager {
   static final int MIN_REPAINT_TIME;
   protected BridgeContext bridgeContext;
   protected Document document;
   protected RunnableQueue updateRunnableQueue;
   protected RunnableQueue.RunHandler runHandler;
   protected volatile boolean running;
   protected volatile boolean suspendCalled;
   protected List listeners = Collections.synchronizedList(new LinkedList());
   protected ScriptingEnvironment scriptingEnvironment;
   protected RepaintManager repaintManager;
   protected UpdateTracker updateTracker;
   protected GraphicsNode graphicsNode;
   protected boolean started;
   protected BridgeContext[] secondaryBridgeContexts;
   protected ScriptingEnvironment[] secondaryScriptingEnvironments;
   protected int minRepaintTime;
   long outOfDateTime = 0L;
   List suspensionList = new ArrayList();
   int nextSuspensionIndex = 1;
   long allResumeTime = -1L;
   Timer repaintTriggerTimer = null;
   TimerTask repaintTimerTask = null;
   static EventDispatcher.Dispatcher startedDispatcher;
   static EventDispatcher.Dispatcher stoppedDispatcher;
   static EventDispatcher.Dispatcher suspendedDispatcher;
   static EventDispatcher.Dispatcher resumedDispatcher;
   static EventDispatcher.Dispatcher updateStartedDispatcher;
   static EventDispatcher.Dispatcher updateCompletedDispatcher;
   static EventDispatcher.Dispatcher updateFailedDispatcher;

   public UpdateManager(BridgeContext ctx, GraphicsNode gn, Document doc) {
      this.bridgeContext = ctx;
      this.bridgeContext.setUpdateManager(this);
      this.document = doc;
      this.updateRunnableQueue = RunnableQueue.createRunnableQueue();
      this.runHandler = this.createRunHandler();
      this.updateRunnableQueue.setRunHandler(this.runHandler);
      this.graphicsNode = gn;
      this.scriptingEnvironment = this.initializeScriptingEnvironment(this.bridgeContext);
      this.secondaryBridgeContexts = (BridgeContext[])ctx.getChildContexts().clone();
      this.secondaryScriptingEnvironments = new ScriptingEnvironment[this.secondaryBridgeContexts.length];

      for(int i = 0; i < this.secondaryBridgeContexts.length; ++i) {
         BridgeContext resCtx = this.secondaryBridgeContexts[i];
         if (((SVGOMDocument)resCtx.getDocument()).isSVG12()) {
            resCtx.setUpdateManager(this);
            ScriptingEnvironment se = this.initializeScriptingEnvironment(resCtx);
            this.secondaryScriptingEnvironments[i] = se;
         }
      }

      this.minRepaintTime = MIN_REPAINT_TIME;
   }

   public int getMinRepaintTime() {
      return this.minRepaintTime;
   }

   public void setMinRepaintTime(int minRepaintTime) {
      this.minRepaintTime = minRepaintTime;
   }

   protected ScriptingEnvironment initializeScriptingEnvironment(BridgeContext ctx) {
      SVGOMDocument d = (SVGOMDocument)ctx.getDocument();
      Object se;
      if (d.isSVG12()) {
         se = new SVG12ScriptingEnvironment(ctx);
         ctx.xblManager = new DefaultXBLManager(d, ctx);
         d.setXBLManager(ctx.xblManager);
      } else {
         se = new ScriptingEnvironment(ctx);
      }

      return (ScriptingEnvironment)se;
   }

   public synchronized void dispatchSVGLoadEvent() throws InterruptedException {
      this.dispatchSVGLoadEvent(this.bridgeContext, this.scriptingEnvironment);

      for(int i = 0; i < this.secondaryScriptingEnvironments.length; ++i) {
         BridgeContext ctx = this.secondaryBridgeContexts[i];
         if (((SVGOMDocument)ctx.getDocument()).isSVG12()) {
            ScriptingEnvironment se = this.secondaryScriptingEnvironments[i];
            this.dispatchSVGLoadEvent(ctx, se);
         }
      }

      this.secondaryBridgeContexts = null;
      this.secondaryScriptingEnvironments = null;
   }

   protected void dispatchSVGLoadEvent(BridgeContext ctx, ScriptingEnvironment se) {
      se.loadScripts();
      se.dispatchSVGLoadEvent();
      if (ctx.isSVG12() && ctx.xblManager != null) {
         SVG12BridgeContext ctx12 = (SVG12BridgeContext)ctx;
         ctx12.addBindingListener();
         ctx12.xblManager.startProcessing();
      }

   }

   public void dispatchSVGZoomEvent() throws InterruptedException {
      this.scriptingEnvironment.dispatchSVGZoomEvent();
   }

   public void dispatchSVGScrollEvent() throws InterruptedException {
      this.scriptingEnvironment.dispatchSVGScrollEvent();
   }

   public void dispatchSVGResizeEvent() throws InterruptedException {
      this.scriptingEnvironment.dispatchSVGResizeEvent();
   }

   public void manageUpdates(final ImageRenderer r) {
      this.updateRunnableQueue.preemptLater(new Runnable() {
         public void run() {
            synchronized(UpdateManager.this) {
               UpdateManager.this.running = true;
               UpdateManager.this.updateTracker = new UpdateTracker();
               RootGraphicsNode root = UpdateManager.this.graphicsNode.getRoot();
               if (root != null) {
                  root.addTreeGraphicsNodeChangeListener(UpdateManager.this.updateTracker);
               }

               UpdateManager.this.repaintManager = new RepaintManager(r);
               UpdateManagerEvent ev = new UpdateManagerEvent(UpdateManager.this, (BufferedImage)null, (List)null);
               UpdateManager.this.fireEvent(UpdateManager.startedDispatcher, ev);
               UpdateManager.this.started = true;
            }
         }
      });
      this.resume();
   }

   public BridgeContext getBridgeContext() {
      return this.bridgeContext;
   }

   public RunnableQueue getUpdateRunnableQueue() {
      return this.updateRunnableQueue;
   }

   public RepaintManager getRepaintManager() {
      return this.repaintManager;
   }

   public UpdateTracker getUpdateTracker() {
      return this.updateTracker;
   }

   public Document getDocument() {
      return this.document;
   }

   public ScriptingEnvironment getScriptingEnvironment() {
      return this.scriptingEnvironment;
   }

   public synchronized boolean isRunning() {
      return this.running;
   }

   public synchronized void suspend() {
      if (this.updateRunnableQueue.getQueueState() == RunnableQueue.RUNNING) {
         this.updateRunnableQueue.suspendExecution(false);
      }

      this.suspendCalled = true;
   }

   public synchronized void resume() {
      if (this.updateRunnableQueue.getQueueState() != RunnableQueue.RUNNING) {
         this.updateRunnableQueue.resumeExecution();
      }

   }

   public void interrupt() {
      Runnable r = new Runnable() {
         public void run() {
            synchronized(UpdateManager.this) {
               if (UpdateManager.this.started) {
                  UpdateManager.this.dispatchSVGUnLoadEvent();
               } else {
                  UpdateManager.this.running = false;
                  UpdateManager.this.scriptingEnvironment.interrupt();
                  UpdateManager.this.updateRunnableQueue.getThread().halt();
               }

            }
         }
      };

      try {
         this.updateRunnableQueue.preemptLater(r);
         this.updateRunnableQueue.resumeExecution();
      } catch (IllegalStateException var3) {
      }

   }

   public void dispatchSVGUnLoadEvent() {
      if (!this.started) {
         throw new IllegalStateException("UpdateManager not started.");
      } else {
         this.updateRunnableQueue.preemptLater(new Runnable() {
            public void run() {
               synchronized(UpdateManager.this) {
                  AbstractEvent evt = (AbstractEvent)((DocumentEvent)UpdateManager.this.document).createEvent("SVGEvents");
                  String type;
                  if (UpdateManager.this.bridgeContext.isSVG12()) {
                     type = "unload";
                  } else {
                     type = "SVGUnload";
                  }

                  evt.initEventNS("http://www.w3.org/2001/xml-events", type, false, false);
                  ((EventTarget)((EventTarget)UpdateManager.this.document.getDocumentElement())).dispatchEvent(evt);
                  UpdateManager.this.running = false;
                  UpdateManager.this.scriptingEnvironment.interrupt();
                  UpdateManager.this.updateRunnableQueue.getThread().halt();
                  UpdateManager.this.bridgeContext.dispose();
                  UpdateManagerEvent ev = new UpdateManagerEvent(UpdateManager.this, (BufferedImage)null, (List)null);
                  UpdateManager.this.fireEvent(UpdateManager.stoppedDispatcher, ev);
               }
            }
         });
         this.resume();
      }
   }

   public void updateRendering(AffineTransform u2d, boolean dbr, Shape aoi, int width, int height) {
      this.repaintManager.setupRenderer(u2d, dbr, aoi, width, height);
      List l = new ArrayList(1);
      l.add(aoi);
      this.updateRendering(l, false);
   }

   public void updateRendering(AffineTransform u2d, boolean dbr, boolean cpt, Shape aoi, int width, int height) {
      this.repaintManager.setupRenderer(u2d, dbr, aoi, width, height);
      List l = new ArrayList(1);
      l.add(aoi);
      this.updateRendering(l, cpt);
   }

   protected void updateRendering(List areas, boolean clearPaintingTransform) {
      UpdateManagerEvent ev;
      try {
         UpdateManagerEvent ev = new UpdateManagerEvent(this, this.repaintManager.getOffScreen(), (List)null);
         this.fireEvent(updateStartedDispatcher, ev);
         Collection c = this.repaintManager.updateRendering(areas);
         List l = new ArrayList(c);
         ev = new UpdateManagerEvent(this, this.repaintManager.getOffScreen(), l, clearPaintingTransform);
         this.fireEvent(updateCompletedDispatcher, ev);
      } catch (ThreadDeath var6) {
         ev = new UpdateManagerEvent(this, (BufferedImage)null, (List)null);
         this.fireEvent(updateFailedDispatcher, ev);
         throw var6;
      } catch (Throwable var7) {
         ev = new UpdateManagerEvent(this, (BufferedImage)null, (List)null);
         this.fireEvent(updateFailedDispatcher, ev);
      }

   }

   protected void repaint() {
      if (!this.updateTracker.hasChanged()) {
         this.outOfDateTime = 0L;
      } else {
         long ctime = System.currentTimeMillis();
         if (ctime < this.allResumeTime) {
            this.createRepaintTimer();
         } else {
            if (this.allResumeTime > 0L) {
               this.releaseAllRedrawSuspension();
            }

            if (ctime - this.outOfDateTime < (long)this.minRepaintTime) {
               synchronized(this.updateRunnableQueue.getIteratorLock()) {
                  Iterator i = this.updateRunnableQueue.iterator();

                  while(i.hasNext()) {
                     if (!(i.next() instanceof NoRepaintRunnable)) {
                        return;
                     }
                  }
               }
            }

            List dirtyAreas = this.updateTracker.getDirtyAreas();
            this.updateTracker.clear();
            if (dirtyAreas != null) {
               this.updateRendering(dirtyAreas, false);
            }

            this.outOfDateTime = 0L;
         }
      }
   }

   public void forceRepaint() {
      if (!this.updateTracker.hasChanged()) {
         this.outOfDateTime = 0L;
      } else {
         List dirtyAreas = this.updateTracker.getDirtyAreas();
         this.updateTracker.clear();
         if (dirtyAreas != null) {
            this.updateRendering(dirtyAreas, false);
         }

         this.outOfDateTime = 0L;
      }
   }

   void createRepaintTimer() {
      if (this.repaintTimerTask == null) {
         if (this.allResumeTime >= 0L) {
            if (this.repaintTriggerTimer == null) {
               this.repaintTriggerTimer = new Timer(true);
            }

            long delay = this.allResumeTime - System.currentTimeMillis();
            if (delay < 0L) {
               delay = 0L;
            }

            this.repaintTimerTask = new RepaintTimerTask(this);
            this.repaintTriggerTimer.schedule(this.repaintTimerTask, delay);
         }
      }
   }

   void resetRepaintTimer() {
      if (this.repaintTimerTask != null) {
         if (this.allResumeTime >= 0L) {
            if (this.repaintTriggerTimer == null) {
               this.repaintTriggerTimer = new Timer(true);
            }

            long delay = this.allResumeTime - System.currentTimeMillis();
            if (delay < 0L) {
               delay = 0L;
            }

            this.repaintTimerTask = new RepaintTimerTask(this);
            this.repaintTriggerTimer.schedule(this.repaintTimerTask, delay);
         }
      }
   }

   int addRedrawSuspension(int max_wait_milliseconds) {
      long resumeTime = System.currentTimeMillis() + (long)max_wait_milliseconds;
      SuspensionInfo si = new SuspensionInfo(this.nextSuspensionIndex++, resumeTime);
      if (resumeTime > this.allResumeTime) {
         this.allResumeTime = resumeTime;
         this.resetRepaintTimer();
      }

      this.suspensionList.add(si);
      return si.getIndex();
   }

   void releaseAllRedrawSuspension() {
      this.suspensionList.clear();
      this.allResumeTime = -1L;
      this.resetRepaintTimer();
   }

   boolean releaseRedrawSuspension(int index) {
      if (index > this.nextSuspensionIndex) {
         return false;
      } else if (this.suspensionList.size() == 0) {
         return true;
      } else {
         int lo = 0;
         int hi = this.suspensionList.size() - 1;

         while(lo < hi) {
            int mid = lo + hi >> 1;
            SuspensionInfo si = (SuspensionInfo)this.suspensionList.get(mid);
            int idx = si.getIndex();
            if (idx == index) {
               hi = mid;
               lo = mid;
            } else if (idx < index) {
               lo = mid + 1;
            } else {
               hi = mid - 1;
            }
         }

         SuspensionInfo si = (SuspensionInfo)this.suspensionList.get(lo);
         int idx = si.getIndex();
         if (idx != index) {
            return true;
         } else {
            this.suspensionList.remove(lo);
            if (this.suspensionList.size() == 0) {
               this.allResumeTime = -1L;
               this.resetRepaintTimer();
            } else {
               long resumeTime = si.getResumeMilli();
               if (resumeTime == this.allResumeTime) {
                  this.allResumeTime = this.findNewAllResumeTime();
                  this.resetRepaintTimer();
               }
            }

            return true;
         }
      }
   }

   long findNewAllResumeTime() {
      long ret = -1L;
      Iterator var3 = this.suspensionList.iterator();

      while(var3.hasNext()) {
         Object aSuspensionList = var3.next();
         SuspensionInfo si = (SuspensionInfo)aSuspensionList;
         long t = si.getResumeMilli();
         if (t > ret) {
            ret = t;
         }
      }

      return ret;
   }

   public void addUpdateManagerListener(UpdateManagerListener l) {
      this.listeners.add(l);
   }

   public void removeUpdateManagerListener(UpdateManagerListener l) {
      this.listeners.remove(l);
   }

   protected void fireEvent(EventDispatcher.Dispatcher dispatcher, Object event) {
      EventDispatcher.fireEvent(dispatcher, this.listeners, event, false);
   }

   protected RunnableQueue.RunHandler createRunHandler() {
      return new UpdateManagerRunHander();
   }

   static {
      int value = 20;

      try {
         String s = System.getProperty("org.apache.batik.min_repaint_time", "20");
         value = Integer.parseInt(s);
      } catch (SecurityException var6) {
      } catch (NumberFormatException var7) {
      } finally {
         MIN_REPAINT_TIME = value;
      }

      startedDispatcher = new EventDispatcher.Dispatcher() {
         public void dispatch(Object listener, Object event) {
            ((UpdateManagerListener)listener).managerStarted((UpdateManagerEvent)event);
         }
      };
      stoppedDispatcher = new EventDispatcher.Dispatcher() {
         public void dispatch(Object listener, Object event) {
            ((UpdateManagerListener)listener).managerStopped((UpdateManagerEvent)event);
         }
      };
      suspendedDispatcher = new EventDispatcher.Dispatcher() {
         public void dispatch(Object listener, Object event) {
            ((UpdateManagerListener)listener).managerSuspended((UpdateManagerEvent)event);
         }
      };
      resumedDispatcher = new EventDispatcher.Dispatcher() {
         public void dispatch(Object listener, Object event) {
            ((UpdateManagerListener)listener).managerResumed((UpdateManagerEvent)event);
         }
      };
      updateStartedDispatcher = new EventDispatcher.Dispatcher() {
         public void dispatch(Object listener, Object event) {
            ((UpdateManagerListener)listener).updateStarted((UpdateManagerEvent)event);
         }
      };
      updateCompletedDispatcher = new EventDispatcher.Dispatcher() {
         public void dispatch(Object listener, Object event) {
            ((UpdateManagerListener)listener).updateCompleted((UpdateManagerEvent)event);
         }
      };
      updateFailedDispatcher = new EventDispatcher.Dispatcher() {
         public void dispatch(Object listener, Object event) {
            ((UpdateManagerListener)listener).updateFailed((UpdateManagerEvent)event);
         }
      };
   }

   protected class UpdateManagerRunHander extends RunnableQueue.RunHandlerAdapter {
      public void runnableStart(RunnableQueue rq, Runnable r) {
         if (UpdateManager.this.running && !(r instanceof NoRepaintRunnable) && UpdateManager.this.outOfDateTime == 0L) {
            UpdateManager.this.outOfDateTime = System.currentTimeMillis();
         }

      }

      public void runnableInvoked(RunnableQueue rq, Runnable r) {
         if (UpdateManager.this.running && !(r instanceof NoRepaintRunnable)) {
            UpdateManager.this.repaint();
         }

      }

      public void executionSuspended(RunnableQueue rq) {
         synchronized(UpdateManager.this) {
            if (UpdateManager.this.suspendCalled) {
               UpdateManager.this.running = false;
               UpdateManagerEvent ev = new UpdateManagerEvent(this, (BufferedImage)null, (List)null);
               UpdateManager.this.fireEvent(UpdateManager.suspendedDispatcher, ev);
            }

         }
      }

      public void executionResumed(RunnableQueue rq) {
         synchronized(UpdateManager.this) {
            if (UpdateManager.this.suspendCalled && !UpdateManager.this.running) {
               UpdateManager.this.running = true;
               UpdateManager.this.suspendCalled = false;
               UpdateManagerEvent ev = new UpdateManagerEvent(this, (BufferedImage)null, (List)null);
               UpdateManager.this.fireEvent(UpdateManager.resumedDispatcher, ev);
            }

         }
      }
   }

   protected static class RepaintTimerTask extends TimerTask {
      UpdateManager um;

      RepaintTimerTask(UpdateManager um) {
         this.um = um;
      }

      public void run() {
         RunnableQueue rq = this.um.getUpdateRunnableQueue();
         if (rq != null) {
            rq.invokeLater(new Runnable() {
               public void run() {
               }
            });
         }
      }
   }

   protected static class SuspensionInfo {
      int index;
      long resumeMilli;

      public SuspensionInfo(int index, long resumeMilli) {
         this.index = index;
         this.resumeMilli = resumeMilli;
      }

      public int getIndex() {
         return this.index;
      }

      public long getResumeMilli() {
         return this.resumeMilli;
      }
   }
}
