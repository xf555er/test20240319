package org.apache.batik.swing.svg;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.InterruptedBridgeException;
import org.apache.batik.bridge.UpdateManager;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.EventDispatcher;
import org.apache.batik.util.HaltingThread;
import org.w3c.dom.svg.SVGDocument;

public class SVGLoadEventDispatcher extends HaltingThread {
   protected SVGDocument svgDocument;
   protected GraphicsNode root;
   protected BridgeContext bridgeContext;
   protected UpdateManager updateManager;
   protected List listeners = Collections.synchronizedList(new LinkedList());
   protected Exception exception;
   static EventDispatcher.Dispatcher startedDispatcher = new EventDispatcher.Dispatcher() {
      public void dispatch(Object listener, Object event) {
         ((SVGLoadEventDispatcherListener)listener).svgLoadEventDispatchStarted((SVGLoadEventDispatcherEvent)event);
      }
   };
   static EventDispatcher.Dispatcher completedDispatcher = new EventDispatcher.Dispatcher() {
      public void dispatch(Object listener, Object event) {
         ((SVGLoadEventDispatcherListener)listener).svgLoadEventDispatchCompleted((SVGLoadEventDispatcherEvent)event);
      }
   };
   static EventDispatcher.Dispatcher cancelledDispatcher = new EventDispatcher.Dispatcher() {
      public void dispatch(Object listener, Object event) {
         ((SVGLoadEventDispatcherListener)listener).svgLoadEventDispatchCancelled((SVGLoadEventDispatcherEvent)event);
      }
   };
   static EventDispatcher.Dispatcher failedDispatcher = new EventDispatcher.Dispatcher() {
      public void dispatch(Object listener, Object event) {
         ((SVGLoadEventDispatcherListener)listener).svgLoadEventDispatchFailed((SVGLoadEventDispatcherEvent)event);
      }
   };

   public SVGLoadEventDispatcher(GraphicsNode gn, SVGDocument doc, BridgeContext bc, UpdateManager um) {
      this.svgDocument = doc;
      this.root = gn;
      this.bridgeContext = bc;
      this.updateManager = um;
   }

   public void run() {
      SVGLoadEventDispatcherEvent ev = new SVGLoadEventDispatcherEvent(this, this.root);

      try {
         this.fireEvent(startedDispatcher, ev);
         if (this.isHalted()) {
            this.fireEvent(cancelledDispatcher, ev);
            return;
         }

         this.updateManager.dispatchSVGLoadEvent();
         if (this.isHalted()) {
            this.fireEvent(cancelledDispatcher, ev);
            return;
         }

         this.fireEvent(completedDispatcher, ev);
      } catch (InterruptedException var3) {
         this.fireEvent(cancelledDispatcher, ev);
      } catch (InterruptedBridgeException var4) {
         this.fireEvent(cancelledDispatcher, ev);
      } catch (Exception var5) {
         this.exception = var5;
         this.fireEvent(failedDispatcher, ev);
      } catch (ThreadDeath var6) {
         this.exception = new Exception(var6.getMessage());
         this.fireEvent(failedDispatcher, ev);
         throw var6;
      } catch (Throwable var7) {
         var7.printStackTrace();
         this.exception = new Exception(var7.getMessage());
         this.fireEvent(failedDispatcher, ev);
      }

   }

   public UpdateManager getUpdateManager() {
      return this.updateManager;
   }

   public Exception getException() {
      return this.exception;
   }

   public void addSVGLoadEventDispatcherListener(SVGLoadEventDispatcherListener l) {
      this.listeners.add(l);
   }

   public void removeSVGLoadEventDispatcherListener(SVGLoadEventDispatcherListener l) {
      this.listeners.remove(l);
   }

   public void fireEvent(EventDispatcher.Dispatcher dispatcher, Object event) {
      EventDispatcher.fireEvent(dispatcher, this.listeners, event, true);
   }
}
