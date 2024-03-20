package org.apache.batik.bridge.svg12;

import org.apache.batik.anim.dom.XBLEventSupport;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.Messages;
import org.apache.batik.bridge.SVGUtilities;
import org.apache.batik.bridge.ScriptingEnvironment;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.AbstractElement;
import org.apache.batik.dom.events.EventSupport;
import org.apache.batik.dom.svg12.SVGGlobal;
import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.dom.util.TriplyIndexedTable;
import org.apache.batik.script.Interpreter;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

public class SVG12ScriptingEnvironment extends ScriptingEnvironment {
   public static final String HANDLER_SCRIPT_DESCRIPTION = "SVG12ScriptingEnvironment.constant.handler.script.description";
   protected TriplyIndexedTable handlerScriptingListeners;

   public SVG12ScriptingEnvironment(BridgeContext ctx) {
      super(ctx);
   }

   protected void addDocumentListeners() {
      this.domNodeInsertedListener = new DOMNodeInsertedListener();
      this.domNodeRemovedListener = new DOMNodeRemovedListener();
      this.domAttrModifiedListener = new DOMAttrModifiedListener();
      AbstractDocument doc = (AbstractDocument)this.document;
      XBLEventSupport es = (XBLEventSupport)doc.initializeEventSupport();
      es.addImplementationEventListenerNS("http://www.w3.org/2001/xml-events", "DOMNodeInserted", this.domNodeInsertedListener, false);
      es.addImplementationEventListenerNS("http://www.w3.org/2001/xml-events", "DOMNodeRemoved", this.domNodeRemovedListener, false);
      es.addImplementationEventListenerNS("http://www.w3.org/2001/xml-events", "DOMAttrModified", this.domAttrModifiedListener, false);
   }

   protected void removeDocumentListeners() {
      AbstractDocument doc = (AbstractDocument)this.document;
      XBLEventSupport es = (XBLEventSupport)doc.initializeEventSupport();
      es.removeImplementationEventListenerNS("http://www.w3.org/2001/xml-events", "DOMNodeInserted", this.domNodeInsertedListener, false);
      es.removeImplementationEventListenerNS("http://www.w3.org/2001/xml-events", "DOMNodeRemoved", this.domNodeRemovedListener, false);
      es.removeImplementationEventListenerNS("http://www.w3.org/2001/xml-events", "DOMAttrModified", this.domAttrModifiedListener, false);
   }

   protected void addScriptingListenersOn(Element elt) {
      String eltNS = elt.getNamespaceURI();
      String eltLN = elt.getLocalName();
      if ("http://www.w3.org/2000/svg".equals(eltNS) && "handler".equals(eltLN)) {
         AbstractElement tgt = (AbstractElement)elt.getParentNode();
         String eventType = elt.getAttributeNS("http://www.w3.org/2001/xml-events", "event");
         String eventNamespaceURI = "http://www.w3.org/2001/xml-events";
         if (eventType.indexOf(58) != -1) {
            String prefix = DOMUtilities.getPrefix(eventType);
            eventType = DOMUtilities.getLocalName(eventType);
            eventNamespaceURI = elt.lookupNamespaceURI(prefix);
         }

         EventListener listener = new HandlerScriptingEventListener(eventNamespaceURI, eventType, (AbstractElement)elt);
         tgt.addEventListenerNS(eventNamespaceURI, eventType, listener, false, (Object)null);
         if (this.handlerScriptingListeners == null) {
            this.handlerScriptingListeners = new TriplyIndexedTable();
         }

         this.handlerScriptingListeners.put(eventNamespaceURI, eventType, elt, listener);
      }

      super.addScriptingListenersOn(elt);
   }

   protected void removeScriptingListenersOn(Element elt) {
      String eltNS = elt.getNamespaceURI();
      String eltLN = elt.getLocalName();
      if ("http://www.w3.org/2000/svg".equals(eltNS) && "handler".equals(eltLN)) {
         AbstractElement tgt = (AbstractElement)elt.getParentNode();
         String eventType = elt.getAttributeNS("http://www.w3.org/2001/xml-events", "event");
         String eventNamespaceURI = "http://www.w3.org/2001/xml-events";
         if (eventType.indexOf(58) != -1) {
            String prefix = DOMUtilities.getPrefix(eventType);
            eventType = DOMUtilities.getLocalName(eventType);
            eventNamespaceURI = elt.lookupNamespaceURI(prefix);
         }

         EventListener listener = (EventListener)this.handlerScriptingListeners.put(eventNamespaceURI, eventType, elt, (Object)null);
         tgt.removeEventListenerNS(eventNamespaceURI, eventType, listener, false);
      }

      super.removeScriptingListenersOn(elt);
   }

   public org.apache.batik.bridge.Window createWindow(Interpreter interp, String lang) {
      return new Global(interp, lang);
   }

   protected class Global extends ScriptingEnvironment.Window implements SVGGlobal {
      public Global(Interpreter interp, String lang) {
         super(interp, lang);
      }

      public void startMouseCapture(EventTarget target, boolean sendAll, boolean autoRelease) {
         ((SVG12BridgeContext)SVG12ScriptingEnvironment.this.bridgeContext.getPrimaryBridgeContext()).startMouseCapture(target, sendAll, autoRelease);
      }

      public void stopMouseCapture() {
         ((SVG12BridgeContext)SVG12ScriptingEnvironment.this.bridgeContext.getPrimaryBridgeContext()).stopMouseCapture();
      }
   }

   protected class HandlerScriptingEventListener implements EventListener {
      protected String eventNamespaceURI;
      protected String eventType;
      protected AbstractElement handlerElement;

      public HandlerScriptingEventListener(String ns, String et, AbstractElement e) {
         this.eventNamespaceURI = ns;
         this.eventType = et;
         this.handlerElement = e;
      }

      public void handleEvent(Event evt) {
         Element elt = (Element)evt.getCurrentTarget();
         String script = this.handlerElement.getTextContent();
         if (script.length() != 0) {
            DocumentLoader dl = SVG12ScriptingEnvironment.this.bridgeContext.getDocumentLoader();
            AbstractDocument d = (AbstractDocument)this.handlerElement.getOwnerDocument();
            int line = dl.getLineNumber(this.handlerElement);
            String desc = Messages.formatMessage("SVG12ScriptingEnvironment.constant.handler.script.description", new Object[]{d.getDocumentURI(), this.eventNamespaceURI, this.eventType, line});
            String lang = this.handlerElement.getAttributeNS((String)null, "contentScriptType");
            if (lang.length() == 0) {
               Element e;
               for(e = elt; e != null && (!"http://www.w3.org/2000/svg".equals(e.getNamespaceURI()) || !"svg".equals(e.getLocalName())); e = SVGUtilities.getParentElement(e)) {
               }

               if (e == null) {
                  return;
               }

               lang = e.getAttributeNS((String)null, "contentScriptType");
            }

            SVG12ScriptingEnvironment.this.runEventHandler(script, evt, lang, desc);
         }
      }
   }

   protected class DOMAttrModifiedListener extends ScriptingEnvironment.DOMAttrModifiedListener {
      protected DOMAttrModifiedListener() {
         super();
      }

      public void handleEvent(Event evt) {
         super.handleEvent(EventSupport.getUltimateOriginalEvent(evt));
      }
   }

   protected class DOMNodeRemovedListener extends ScriptingEnvironment.DOMNodeRemovedListener {
      protected DOMNodeRemovedListener() {
         super();
      }

      public void handleEvent(Event evt) {
         super.handleEvent(EventSupport.getUltimateOriginalEvent(evt));
      }
   }

   protected class DOMNodeInsertedListener extends ScriptingEnvironment.DOMNodeInsertedListener {
      protected DOMNodeInsertedListener() {
         super();
      }

      public void handleEvent(Event evt) {
         super.handleEvent(EventSupport.getUltimateOriginalEvent(evt));
      }
   }
}
