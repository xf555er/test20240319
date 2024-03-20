package org.apache.batik.swing.svg;

import java.io.InterruptedIOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.util.EventDispatcher;
import org.apache.batik.util.HaltingThread;
import org.w3c.dom.svg.SVGDocument;

public class SVGDocumentLoader extends HaltingThread {
   protected String url;
   protected DocumentLoader loader;
   protected Exception exception;
   protected List listeners = Collections.synchronizedList(new LinkedList());
   static EventDispatcher.Dispatcher startedDispatcher = new EventDispatcher.Dispatcher() {
      public void dispatch(Object listener, Object event) {
         ((SVGDocumentLoaderListener)listener).documentLoadingStarted((SVGDocumentLoaderEvent)event);
      }
   };
   static EventDispatcher.Dispatcher completedDispatcher = new EventDispatcher.Dispatcher() {
      public void dispatch(Object listener, Object event) {
         ((SVGDocumentLoaderListener)listener).documentLoadingCompleted((SVGDocumentLoaderEvent)event);
      }
   };
   static EventDispatcher.Dispatcher cancelledDispatcher = new EventDispatcher.Dispatcher() {
      public void dispatch(Object listener, Object event) {
         ((SVGDocumentLoaderListener)listener).documentLoadingCancelled((SVGDocumentLoaderEvent)event);
      }
   };
   static EventDispatcher.Dispatcher failedDispatcher = new EventDispatcher.Dispatcher() {
      public void dispatch(Object listener, Object event) {
         ((SVGDocumentLoaderListener)listener).documentLoadingFailed((SVGDocumentLoaderEvent)event);
      }
   };

   public SVGDocumentLoader(String u, DocumentLoader l) {
      this.url = u;
      this.loader = l;
   }

   public void run() {
      SVGDocumentLoaderEvent evt = new SVGDocumentLoaderEvent(this, (SVGDocument)null);

      try {
         this.fireEvent(startedDispatcher, evt);
         if (this.isHalted()) {
            this.fireEvent(cancelledDispatcher, evt);
            return;
         }

         SVGDocument svgDocument = (SVGDocument)this.loader.loadDocument(this.url);
         if (this.isHalted()) {
            this.fireEvent(cancelledDispatcher, evt);
            return;
         }

         evt = new SVGDocumentLoaderEvent(this, svgDocument);
         this.fireEvent(completedDispatcher, evt);
      } catch (InterruptedIOException var3) {
         this.fireEvent(cancelledDispatcher, evt);
      } catch (Exception var4) {
         this.exception = var4;
         this.fireEvent(failedDispatcher, evt);
      } catch (ThreadDeath var5) {
         this.exception = new Exception(var5.getMessage());
         this.fireEvent(failedDispatcher, evt);
         throw var5;
      } catch (Throwable var6) {
         var6.printStackTrace();
         this.exception = new Exception(var6.getMessage());
         this.fireEvent(failedDispatcher, evt);
      }

   }

   public Exception getException() {
      return this.exception;
   }

   public void addSVGDocumentLoaderListener(SVGDocumentLoaderListener l) {
      this.listeners.add(l);
   }

   public void removeSVGDocumentLoaderListener(SVGDocumentLoaderListener l) {
      this.listeners.remove(l);
   }

   public void fireEvent(EventDispatcher.Dispatcher dispatcher, Object event) {
      EventDispatcher.fireEvent(dispatcher, this.listeners, event, true);
   }
}
