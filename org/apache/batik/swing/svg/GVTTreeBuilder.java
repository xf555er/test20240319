package org.apache.batik.swing.svg;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.BridgeException;
import org.apache.batik.bridge.DynamicGVTBuilder;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.InterruptedBridgeException;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.EventDispatcher;
import org.apache.batik.util.HaltingThread;
import org.w3c.dom.Document;
import org.w3c.dom.svg.SVGDocument;

public class GVTTreeBuilder extends HaltingThread {
   protected SVGDocument svgDocument;
   protected BridgeContext bridgeContext;
   protected List listeners = Collections.synchronizedList(new LinkedList());
   protected Exception exception;
   static EventDispatcher.Dispatcher startedDispatcher = new EventDispatcher.Dispatcher() {
      public void dispatch(Object listener, Object event) {
         ((GVTTreeBuilderListener)listener).gvtBuildStarted((GVTTreeBuilderEvent)event);
      }
   };
   static EventDispatcher.Dispatcher completedDispatcher = new EventDispatcher.Dispatcher() {
      public void dispatch(Object listener, Object event) {
         ((GVTTreeBuilderListener)listener).gvtBuildCompleted((GVTTreeBuilderEvent)event);
      }
   };
   static EventDispatcher.Dispatcher cancelledDispatcher = new EventDispatcher.Dispatcher() {
      public void dispatch(Object listener, Object event) {
         ((GVTTreeBuilderListener)listener).gvtBuildCancelled((GVTTreeBuilderEvent)event);
      }
   };
   static EventDispatcher.Dispatcher failedDispatcher = new EventDispatcher.Dispatcher() {
      public void dispatch(Object listener, Object event) {
         ((GVTTreeBuilderListener)listener).gvtBuildFailed((GVTTreeBuilderEvent)event);
      }
   };

   public GVTTreeBuilder(SVGDocument doc, BridgeContext bc) {
      this.svgDocument = doc;
      this.bridgeContext = bc;
   }

   public void run() {
      GVTTreeBuilderEvent ev = new GVTTreeBuilderEvent(this, (GraphicsNode)null);

      try {
         try {
            this.fireEvent(startedDispatcher, ev);
            if (this.isHalted()) {
               this.fireEvent(cancelledDispatcher, ev);
               return;
            }

            GVTBuilder builder = null;
            if (this.bridgeContext.isDynamic()) {
               builder = new DynamicGVTBuilder();
            } else {
               builder = new GVTBuilder();
            }

            GraphicsNode gvtRoot = ((GVTBuilder)builder).build(this.bridgeContext, (Document)this.svgDocument);
            if (this.isHalted()) {
               this.fireEvent(cancelledDispatcher, ev);
               return;
            }

            ev = new GVTTreeBuilderEvent(this, gvtRoot);
            this.fireEvent(completedDispatcher, ev);
         } catch (InterruptedBridgeException var11) {
            this.fireEvent(cancelledDispatcher, ev);
         } catch (BridgeException var12) {
            this.exception = var12;
            ev = new GVTTreeBuilderEvent(this, var12.getGraphicsNode());
            this.fireEvent(failedDispatcher, ev);
         } catch (Exception var13) {
            this.exception = var13;
            this.fireEvent(failedDispatcher, ev);
         } catch (ThreadDeath var14) {
            this.exception = new Exception(var14.getMessage());
            this.fireEvent(failedDispatcher, ev);
            throw var14;
         } catch (Throwable var15) {
            var15.printStackTrace();
            this.exception = new Exception(var15.getMessage());
            this.fireEvent(failedDispatcher, ev);
         }

      } finally {
         ;
      }
   }

   public Exception getException() {
      return this.exception;
   }

   public void addGVTTreeBuilderListener(GVTTreeBuilderListener l) {
      this.listeners.add(l);
   }

   public void removeGVTTreeBuilderListener(GVTTreeBuilderListener l) {
      this.listeners.remove(l);
   }

   public void fireEvent(EventDispatcher.Dispatcher dispatcher, Object event) {
      EventDispatcher.fireEvent(dispatcher, this.listeners, event, true);
   }
}
