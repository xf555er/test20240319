package org.apache.batik.swing.gvt;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.apache.batik.bridge.InterruptedBridgeException;
import org.apache.batik.gvt.renderer.ImageRenderer;
import org.apache.batik.util.EventDispatcher;
import org.apache.batik.util.HaltingThread;

public class GVTTreeRenderer extends HaltingThread {
   protected ImageRenderer renderer;
   protected Shape areaOfInterest;
   protected int width;
   protected int height;
   protected AffineTransform user2DeviceTransform;
   protected boolean doubleBuffering;
   protected List listeners = Collections.synchronizedList(new LinkedList());
   static EventDispatcher.Dispatcher prepareDispatcher = new EventDispatcher.Dispatcher() {
      public void dispatch(Object listener, Object event) {
         ((GVTTreeRendererListener)listener).gvtRenderingPrepare((GVTTreeRendererEvent)event);
      }
   };
   static EventDispatcher.Dispatcher startedDispatcher = new EventDispatcher.Dispatcher() {
      public void dispatch(Object listener, Object event) {
         ((GVTTreeRendererListener)listener).gvtRenderingStarted((GVTTreeRendererEvent)event);
      }
   };
   static EventDispatcher.Dispatcher cancelledDispatcher = new EventDispatcher.Dispatcher() {
      public void dispatch(Object listener, Object event) {
         ((GVTTreeRendererListener)listener).gvtRenderingCancelled((GVTTreeRendererEvent)event);
      }
   };
   static EventDispatcher.Dispatcher completedDispatcher = new EventDispatcher.Dispatcher() {
      public void dispatch(Object listener, Object event) {
         ((GVTTreeRendererListener)listener).gvtRenderingCompleted((GVTTreeRendererEvent)event);
      }
   };
   static EventDispatcher.Dispatcher failedDispatcher = new EventDispatcher.Dispatcher() {
      public void dispatch(Object listener, Object event) {
         ((GVTTreeRendererListener)listener).gvtRenderingFailed((GVTTreeRendererEvent)event);
      }
   };

   public GVTTreeRenderer(ImageRenderer r, AffineTransform usr2dev, boolean dbuffer, Shape aoi, int width, int height) {
      this.renderer = r;
      this.areaOfInterest = aoi;
      this.user2DeviceTransform = usr2dev;
      this.doubleBuffering = dbuffer;
      this.width = width;
      this.height = height;
   }

   public void run() {
      GVTTreeRendererEvent ev = new GVTTreeRendererEvent(this, (BufferedImage)null);

      try {
         this.fireEvent(prepareDispatcher, ev);
         this.renderer.setTransform(this.user2DeviceTransform);
         this.renderer.setDoubleBuffered(this.doubleBuffering);
         this.renderer.updateOffScreen(this.width, this.height);
         this.renderer.clearOffScreen();
         if (this.isHalted()) {
            this.fireEvent(cancelledDispatcher, ev);
            return;
         }

         ev = new GVTTreeRendererEvent(this, this.renderer.getOffScreen());
         this.fireEvent(startedDispatcher, ev);
         if (this.isHalted()) {
            this.fireEvent(cancelledDispatcher, ev);
            return;
         }

         this.renderer.repaint(this.areaOfInterest);
         if (this.isHalted()) {
            this.fireEvent(cancelledDispatcher, ev);
            return;
         }

         ev = new GVTTreeRendererEvent(this, this.renderer.getOffScreen());
         this.fireEvent(completedDispatcher, ev);
      } catch (NoClassDefFoundError var3) {
      } catch (InterruptedBridgeException var4) {
         this.fireEvent(cancelledDispatcher, ev);
      } catch (ThreadDeath var5) {
         this.fireEvent(failedDispatcher, ev);
         throw var5;
      } catch (Throwable var6) {
         var6.printStackTrace();
         this.fireEvent(failedDispatcher, ev);
      }

   }

   public void fireEvent(EventDispatcher.Dispatcher dispatcher, Object event) {
      EventDispatcher.fireEvent(dispatcher, this.listeners, event, true);
   }

   public void addGVTTreeRendererListener(GVTTreeRendererListener l) {
      this.listeners.add(l);
   }

   public void removeGVTTreeRendererListener(GVTTreeRendererListener l) {
      this.listeners.remove(l);
   }
}
