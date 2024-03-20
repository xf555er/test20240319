package org.apache.batik.bridge.svg12;

import java.util.Iterator;
import org.apache.batik.anim.dom.SVGOMDocument;
import org.apache.batik.anim.dom.XBLEventSupport;
import org.apache.batik.anim.dom.XBLOMShadowTreeElement;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.BridgeUpdateHandler;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.ScriptingEnvironment;
import org.apache.batik.bridge.URIResolver;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.AbstractNode;
import org.apache.batik.dom.events.EventSupport;
import org.apache.batik.dom.events.NodeEventTarget;
import org.apache.batik.dom.xbl.NodeXBL;
import org.apache.batik.dom.xbl.XBLManager;
import org.apache.batik.script.Interpreter;
import org.apache.batik.script.InterpreterPool;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.svg.SVGDocument;

public class SVG12BridgeContext extends BridgeContext {
   protected XBLBindingListener bindingListener;
   protected XBLContentListener contentListener;
   protected EventTarget mouseCaptureTarget;
   protected boolean mouseCaptureSendAll;
   protected boolean mouseCaptureAutoRelease;

   public SVG12BridgeContext(UserAgent userAgent) {
      super(userAgent);
   }

   public SVG12BridgeContext(UserAgent userAgent, DocumentLoader loader) {
      super(userAgent, loader);
   }

   public SVG12BridgeContext(UserAgent userAgent, InterpreterPool interpreterPool, DocumentLoader documentLoader) {
      super(userAgent, interpreterPool, documentLoader);
   }

   public URIResolver createURIResolver(SVGDocument doc, DocumentLoader dl) {
      return new SVG12URIResolver(doc, dl);
   }

   public void addGVTListener(Document doc) {
      SVG12BridgeEventSupport.addGVTListener(this, doc);
   }

   public void dispose() {
      this.clearChildContexts();
      synchronized(this.eventListenerSet) {
         Iterator var2 = this.eventListenerSet.iterator();

         while(var2.hasNext()) {
            Object anEventListenerSet = var2.next();
            BridgeContext.EventListenerMememto m = (BridgeContext.EventListenerMememto)anEventListenerSet;
            NodeEventTarget et = m.getTarget();
            EventListener el = m.getListener();
            boolean uc = m.getUseCapture();
            String t = m.getEventType();
            boolean in = m.getNamespaced();
            if (et != null && el != null && t != null) {
               String ns;
               if (m instanceof ImplementationEventListenerMememto) {
                  ns = m.getNamespaceURI();
                  Node nde = (Node)et;
                  AbstractNode n = (AbstractNode)nde.getOwnerDocument();
                  if (n != null) {
                     XBLEventSupport es = (XBLEventSupport)n.initializeEventSupport();
                     es.removeImplementationEventListenerNS(ns, t, el, uc);
                  }
               } else if (in) {
                  ns = m.getNamespaceURI();
                  et.removeEventListenerNS(ns, t, el, uc);
               } else {
                  et.removeEventListener(t, el, uc);
               }
            }
         }
      }

      if (this.document != null) {
         this.removeDOMListeners();
         this.removeBindingListener();
      }

      if (this.animationEngine != null) {
         this.animationEngine.dispose();
         this.animationEngine = null;
      }

      Iterator var1 = this.interpreterMap.values().iterator();

      while(var1.hasNext()) {
         Object o = var1.next();
         Interpreter interpreter = (Interpreter)o;
         if (interpreter != null) {
            interpreter.dispose();
         }
      }

      this.interpreterMap.clear();
      if (this.focusManager != null) {
         this.focusManager.dispose();
      }

   }

   public void addBindingListener() {
      AbstractDocument doc = (AbstractDocument)this.document;
      DefaultXBLManager xm = (DefaultXBLManager)doc.getXBLManager();
      if (xm != null) {
         this.bindingListener = new XBLBindingListener();
         xm.addBindingListener(this.bindingListener);
         this.contentListener = new XBLContentListener();
         xm.addContentSelectionChangedListener(this.contentListener);
      }

   }

   public void removeBindingListener() {
      AbstractDocument doc = (AbstractDocument)this.document;
      XBLManager xm = doc.getXBLManager();
      if (xm instanceof DefaultXBLManager) {
         DefaultXBLManager dxm = (DefaultXBLManager)xm;
         dxm.removeBindingListener(this.bindingListener);
         dxm.removeContentSelectionChangedListener(this.contentListener);
      }

   }

   public void addDOMListeners() {
      SVGOMDocument doc = (SVGOMDocument)this.document;
      XBLEventSupport evtSupport = (XBLEventSupport)doc.initializeEventSupport();
      this.domAttrModifiedEventListener = new EventListenerWrapper(new BridgeContext.DOMAttrModifiedEventListener());
      evtSupport.addImplementationEventListenerNS("http://www.w3.org/2001/xml-events", "DOMAttrModified", this.domAttrModifiedEventListener, true);
      this.domNodeInsertedEventListener = new EventListenerWrapper(new BridgeContext.DOMNodeInsertedEventListener());
      evtSupport.addImplementationEventListenerNS("http://www.w3.org/2001/xml-events", "DOMNodeInserted", this.domNodeInsertedEventListener, true);
      this.domNodeRemovedEventListener = new EventListenerWrapper(new BridgeContext.DOMNodeRemovedEventListener());
      evtSupport.addImplementationEventListenerNS("http://www.w3.org/2001/xml-events", "DOMNodeRemoved", this.domNodeRemovedEventListener, true);
      this.domCharacterDataModifiedEventListener = new EventListenerWrapper(new BridgeContext.DOMCharacterDataModifiedEventListener());
      evtSupport.addImplementationEventListenerNS("http://www.w3.org/2001/xml-events", "DOMCharacterDataModified", this.domCharacterDataModifiedEventListener, true);
      this.animatedAttributeListener = new BridgeContext.AnimatedAttrListener();
      doc.addAnimatedAttributeListener(this.animatedAttributeListener);
      this.focusManager = new SVG12FocusManager(this.document);
      CSSEngine cssEngine = doc.getCSSEngine();
      this.cssPropertiesChangedListener = new BridgeContext.CSSPropertiesChangedListener();
      cssEngine.addCSSEngineListener(this.cssPropertiesChangedListener);
   }

   public void addUIEventListeners(Document doc) {
      EventTarget evtTarget = (EventTarget)doc.getDocumentElement();
      AbstractNode n = (AbstractNode)evtTarget;
      XBLEventSupport evtSupport = (XBLEventSupport)n.initializeEventSupport();
      EventListener domMouseOverListener = new EventListenerWrapper(new BridgeContext.DOMMouseOverEventListener());
      evtSupport.addImplementationEventListenerNS("http://www.w3.org/2001/xml-events", "mouseover", domMouseOverListener, true);
      this.storeImplementationEventListenerNS(evtTarget, "http://www.w3.org/2001/xml-events", "mouseover", domMouseOverListener, true);
      EventListener domMouseOutListener = new EventListenerWrapper(new BridgeContext.DOMMouseOutEventListener());
      evtSupport.addImplementationEventListenerNS("http://www.w3.org/2001/xml-events", "mouseout", domMouseOutListener, true);
      this.storeImplementationEventListenerNS(evtTarget, "http://www.w3.org/2001/xml-events", "mouseout", domMouseOutListener, true);
   }

   public void removeUIEventListeners(Document doc) {
      EventTarget evtTarget = (EventTarget)doc.getDocumentElement();
      AbstractNode n = (AbstractNode)evtTarget;
      XBLEventSupport es = (XBLEventSupport)n.initializeEventSupport();
      synchronized(this.eventListenerSet) {
         Iterator var6 = this.eventListenerSet.iterator();

         while(var6.hasNext()) {
            Object anEventListenerSet = var6.next();
            BridgeContext.EventListenerMememto elm = (BridgeContext.EventListenerMememto)anEventListenerSet;
            NodeEventTarget et = elm.getTarget();
            if (et == evtTarget) {
               EventListener el = elm.getListener();
               boolean uc = elm.getUseCapture();
               String t = elm.getEventType();
               boolean in = elm.getNamespaced();
               if (et != null && el != null && t != null) {
                  String ns;
                  if (elm instanceof ImplementationEventListenerMememto) {
                     ns = elm.getNamespaceURI();
                     es.removeImplementationEventListenerNS(ns, t, el, uc);
                  } else if (in) {
                     ns = elm.getNamespaceURI();
                     et.removeEventListenerNS(ns, t, el, uc);
                  } else {
                     et.removeEventListener(t, el, uc);
                  }
               }
            }
         }

      }
   }

   protected void removeDOMListeners() {
      SVGOMDocument doc = (SVGOMDocument)this.document;
      doc.removeEventListenerNS("http://www.w3.org/2001/xml-events", "DOMAttrModified", this.domAttrModifiedEventListener, true);
      doc.removeEventListenerNS("http://www.w3.org/2001/xml-events", "DOMNodeInserted", this.domNodeInsertedEventListener, true);
      doc.removeEventListenerNS("http://www.w3.org/2001/xml-events", "DOMNodeRemoved", this.domNodeRemovedEventListener, true);
      doc.removeEventListenerNS("http://www.w3.org/2001/xml-events", "DOMCharacterDataModified", this.domCharacterDataModifiedEventListener, true);
      doc.removeAnimatedAttributeListener(this.animatedAttributeListener);
      CSSEngine cssEngine = doc.getCSSEngine();
      if (cssEngine != null) {
         cssEngine.removeCSSEngineListener(this.cssPropertiesChangedListener);
         cssEngine.dispose();
         doc.setCSSEngine((CSSEngine)null);
      }

   }

   protected void storeImplementationEventListenerNS(EventTarget t, String ns, String s, EventListener l, boolean b) {
      synchronized(this.eventListenerSet) {
         ImplementationEventListenerMememto m = new ImplementationEventListenerMememto(t, ns, s, l, b, this);
         this.eventListenerSet.add(m);
      }
   }

   public BridgeContext createSubBridgeContext(SVGOMDocument newDoc) {
      CSSEngine eng = newDoc.getCSSEngine();
      if (eng != null) {
         return (BridgeContext)newDoc.getCSSEngine().getCSSContext();
      } else {
         BridgeContext subCtx = super.createSubBridgeContext(newDoc);
         if (this.isDynamic() && subCtx.isDynamic()) {
            this.setUpdateManager(subCtx, this.updateManager);
            if (this.updateManager != null) {
               Object se;
               if (newDoc.isSVG12()) {
                  se = new SVG12ScriptingEnvironment(subCtx);
               } else {
                  se = new ScriptingEnvironment(subCtx);
               }

               ((ScriptingEnvironment)se).loadScripts();
               ((ScriptingEnvironment)se).dispatchSVGLoadEvent();
               if (newDoc.isSVG12()) {
                  DefaultXBLManager xm = new DefaultXBLManager(newDoc, subCtx);
                  this.setXBLManager(subCtx, xm);
                  newDoc.setXBLManager(xm);
                  xm.startProcessing();
               }
            }
         }

         return subCtx;
      }
   }

   public void startMouseCapture(EventTarget target, boolean sendAll, boolean autoRelease) {
      this.mouseCaptureTarget = target;
      this.mouseCaptureSendAll = sendAll;
      this.mouseCaptureAutoRelease = autoRelease;
   }

   public void stopMouseCapture() {
      this.mouseCaptureTarget = null;
   }

   protected class XBLContentListener implements ContentSelectionChangedListener {
      public void contentSelectionChanged(ContentSelectionChangedEvent csce) {
         Element e = (Element)csce.getContentElement().getParentNode();
         if (e instanceof XBLOMShadowTreeElement) {
            e = ((NodeXBL)e).getXblBoundElement();
         }

         BridgeUpdateHandler h = SVG12BridgeContext.getBridgeUpdateHandler(e);
         if (h instanceof SVG12BridgeUpdateHandler) {
            SVG12BridgeUpdateHandler h12 = (SVG12BridgeUpdateHandler)h;

            try {
               h12.handleContentSelectionChangedEvent(csce);
            } catch (Exception var6) {
               SVG12BridgeContext.this.userAgent.displayError(var6);
            }
         }

      }
   }

   protected class XBLBindingListener implements BindingListener {
      public void bindingChanged(Element bindableElement, Element shadowTree) {
         BridgeUpdateHandler h = SVG12BridgeContext.getBridgeUpdateHandler(bindableElement);
         if (h instanceof SVG12BridgeUpdateHandler) {
            SVG12BridgeUpdateHandler h12 = (SVG12BridgeUpdateHandler)h;

            try {
               h12.handleBindingEvent(bindableElement, shadowTree);
            } catch (Exception var6) {
               SVG12BridgeContext.this.userAgent.displayError(var6);
            }
         }

      }
   }

   protected static class EventListenerWrapper implements EventListener {
      protected EventListener listener;

      public EventListenerWrapper(EventListener l) {
         this.listener = l;
      }

      public void handleEvent(Event evt) {
         this.listener.handleEvent(EventSupport.getUltimateOriginalEvent(evt));
      }

      public String toString() {
         return super.toString() + " [wrapping " + this.listener.toString() + "]";
      }
   }

   protected static class ImplementationEventListenerMememto extends BridgeContext.EventListenerMememto {
      public ImplementationEventListenerMememto(EventTarget t, String s, EventListener l, boolean b, BridgeContext c) {
         super(t, s, l, b, c);
      }

      public ImplementationEventListenerMememto(EventTarget t, String n, String s, EventListener l, boolean b, BridgeContext c) {
         super(t, n, s, l, b, c);
      }
   }
}
