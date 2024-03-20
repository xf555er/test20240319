package org.apache.batik.bridge;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.anim.dom.SVGOMDocument;
import org.apache.batik.anim.dom.SVGOMScriptElement;
import org.apache.batik.dom.AbstractElement;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.dom.events.NodeEventTarget;
import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.dom.util.SAXDocumentFactory;
import org.apache.batik.script.Interpreter;
import org.apache.batik.script.InterpreterException;
import org.apache.batik.script.ScriptEventWrapper;
import org.apache.batik.util.EncodingUtilities;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.util.RunnableQueue;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.MutationEvent;
import org.w3c.dom.svg.SVGDocument;

public class ScriptingEnvironment extends BaseScriptingEnvironment {
   public static final String[] SVG_EVENT_ATTRS = new String[]{"onabort", "onerror", "onresize", "onscroll", "onunload", "onzoom", "onbegin", "onend", "onrepeat", "onfocusin", "onfocusout", "onactivate", "onclick", "onmousedown", "onmouseup", "onmouseover", "onmouseout", "onmousemove", "onkeypress", "onkeydown", "onkeyup"};
   public static final String[] SVG_DOM_EVENT = new String[]{"SVGAbort", "SVGError", "SVGResize", "SVGScroll", "SVGUnload", "SVGZoom", "beginEvent", "endEvent", "repeatEvent", "DOMFocusIn", "DOMFocusOut", "DOMActivate", "click", "mousedown", "mouseup", "mouseover", "mouseout", "mousemove", "keypress", "keydown", "keyup"};
   protected Timer timer = new Timer(true);
   protected UpdateManager updateManager;
   protected RunnableQueue updateRunnableQueue;
   protected EventListener domNodeInsertedListener;
   protected EventListener domNodeRemovedListener;
   protected EventListener domAttrModifiedListener;
   protected EventListener svgAbortListener = new ScriptingEventListener("onabort");
   protected EventListener svgErrorListener = new ScriptingEventListener("onerror");
   protected EventListener svgResizeListener = new ScriptingEventListener("onresize");
   protected EventListener svgScrollListener = new ScriptingEventListener("onscroll");
   protected EventListener svgUnloadListener = new ScriptingEventListener("onunload");
   protected EventListener svgZoomListener = new ScriptingEventListener("onzoom");
   protected EventListener beginListener = new ScriptingEventListener("onbegin");
   protected EventListener endListener = new ScriptingEventListener("onend");
   protected EventListener repeatListener = new ScriptingEventListener("onrepeat");
   protected EventListener focusinListener = new ScriptingEventListener("onfocusin");
   protected EventListener focusoutListener = new ScriptingEventListener("onfocusout");
   protected EventListener activateListener = new ScriptingEventListener("onactivate");
   protected EventListener clickListener = new ScriptingEventListener("onclick");
   protected EventListener mousedownListener = new ScriptingEventListener("onmousedown");
   protected EventListener mouseupListener = new ScriptingEventListener("onmouseup");
   protected EventListener mouseoverListener = new ScriptingEventListener("onmouseover");
   protected EventListener mouseoutListener = new ScriptingEventListener("onmouseout");
   protected EventListener mousemoveListener = new ScriptingEventListener("onmousemove");
   protected EventListener keypressListener = new ScriptingEventListener("onkeypress");
   protected EventListener keydownListener = new ScriptingEventListener("onkeydown");
   protected EventListener keyupListener = new ScriptingEventListener("onkeyup");
   protected EventListener[] listeners;
   Map attrToDOMEvent;
   Map attrToListener;

   public ScriptingEnvironment(BridgeContext ctx) {
      super(ctx);
      this.listeners = new EventListener[]{this.svgAbortListener, this.svgErrorListener, this.svgResizeListener, this.svgScrollListener, this.svgUnloadListener, this.svgZoomListener, this.beginListener, this.endListener, this.repeatListener, this.focusinListener, this.focusoutListener, this.activateListener, this.clickListener, this.mousedownListener, this.mouseupListener, this.mouseoverListener, this.mouseoutListener, this.mousemoveListener, this.keypressListener, this.keydownListener, this.keyupListener};
      this.attrToDOMEvent = new HashMap(SVG_EVENT_ATTRS.length);
      this.attrToListener = new HashMap(SVG_EVENT_ATTRS.length);

      for(int i = 0; i < SVG_EVENT_ATTRS.length; ++i) {
         this.attrToDOMEvent.put(SVG_EVENT_ATTRS[i], SVG_DOM_EVENT[i]);
         this.attrToListener.put(SVG_EVENT_ATTRS[i], this.listeners[i]);
      }

      this.updateManager = ctx.getUpdateManager();
      this.updateRunnableQueue = this.updateManager.getUpdateRunnableQueue();
      this.addScriptingListeners(this.document.getDocumentElement());
      this.addDocumentListeners();
   }

   protected void addDocumentListeners() {
      this.domNodeInsertedListener = new DOMNodeInsertedListener();
      this.domNodeRemovedListener = new DOMNodeRemovedListener();
      this.domAttrModifiedListener = new DOMAttrModifiedListener();
      NodeEventTarget et = (NodeEventTarget)this.document;
      et.addEventListenerNS("http://www.w3.org/2001/xml-events", "DOMNodeInserted", this.domNodeInsertedListener, false, (Object)null);
      et.addEventListenerNS("http://www.w3.org/2001/xml-events", "DOMNodeRemoved", this.domNodeRemovedListener, false, (Object)null);
      et.addEventListenerNS("http://www.w3.org/2001/xml-events", "DOMAttrModified", this.domAttrModifiedListener, false, (Object)null);
   }

   protected void removeDocumentListeners() {
      NodeEventTarget et = (NodeEventTarget)this.document;
      et.removeEventListenerNS("http://www.w3.org/2001/xml-events", "DOMNodeInserted", this.domNodeInsertedListener, false);
      et.removeEventListenerNS("http://www.w3.org/2001/xml-events", "DOMNodeRemoved", this.domNodeRemovedListener, false);
      et.removeEventListenerNS("http://www.w3.org/2001/xml-events", "DOMAttrModified", this.domAttrModifiedListener, false);
   }

   protected org.apache.batik.bridge.Window createWindow(Interpreter interp, String lang) {
      return new Window(interp, lang);
   }

   public void runEventHandler(String script, Event evt, String lang, String desc) {
      Interpreter interpreter = this.getInterpreter(lang);
      if (interpreter != null) {
         try {
            this.checkCompatibleScriptURL(lang, this.docPURL);
            Object event;
            if (evt instanceof ScriptEventWrapper) {
               event = ((ScriptEventWrapper)evt).getEventObject();
            } else {
               event = evt;
            }

            interpreter.bindObject("event", event);
            interpreter.bindObject("evt", event);
            interpreter.evaluate(new StringReader(script), desc);
         } catch (IOException var7) {
         } catch (InterpreterException var8) {
            this.handleInterpreterException(var8);
         } catch (SecurityException var9) {
            this.handleSecurityException(var9);
         }

      }
   }

   public void interrupt() {
      this.timer.cancel();
      this.removeScriptingListeners(this.document.getDocumentElement());
      this.removeDocumentListeners();
   }

   public void addScriptingListeners(Node node) {
      if (node.getNodeType() == 1) {
         this.addScriptingListenersOn((Element)node);
      }

      for(Node n = node.getFirstChild(); n != null; n = n.getNextSibling()) {
         this.addScriptingListeners(n);
      }

   }

   protected void addScriptingListenersOn(Element elt) {
      NodeEventTarget target = (NodeEventTarget)elt;
      if ("http://www.w3.org/2000/svg".equals(elt.getNamespaceURI())) {
         if ("svg".equals(elt.getLocalName())) {
            if (elt.hasAttributeNS((String)null, "onabort")) {
               target.addEventListenerNS("http://www.w3.org/2001/xml-events", "SVGAbort", this.svgAbortListener, false, (Object)null);
            }

            if (elt.hasAttributeNS((String)null, "onerror")) {
               target.addEventListenerNS("http://www.w3.org/2001/xml-events", "SVGError", this.svgErrorListener, false, (Object)null);
            }

            if (elt.hasAttributeNS((String)null, "onresize")) {
               target.addEventListenerNS("http://www.w3.org/2001/xml-events", "SVGResize", this.svgResizeListener, false, (Object)null);
            }

            if (elt.hasAttributeNS((String)null, "onscroll")) {
               target.addEventListenerNS("http://www.w3.org/2001/xml-events", "SVGScroll", this.svgScrollListener, false, (Object)null);
            }

            if (elt.hasAttributeNS((String)null, "onunload")) {
               target.addEventListenerNS("http://www.w3.org/2001/xml-events", "SVGUnload", this.svgUnloadListener, false, (Object)null);
            }

            if (elt.hasAttributeNS((String)null, "onzoom")) {
               target.addEventListenerNS("http://www.w3.org/2001/xml-events", "SVGZoom", this.svgZoomListener, false, (Object)null);
            }
         } else {
            String name = elt.getLocalName();
            if (name.equals("set") || name.startsWith("animate")) {
               if (elt.hasAttributeNS((String)null, "onbegin")) {
                  target.addEventListenerNS("http://www.w3.org/2001/xml-events", "beginEvent", this.beginListener, false, (Object)null);
               }

               if (elt.hasAttributeNS((String)null, "onend")) {
                  target.addEventListenerNS("http://www.w3.org/2001/xml-events", "endEvent", this.endListener, false, (Object)null);
               }

               if (elt.hasAttributeNS((String)null, "onrepeat")) {
                  target.addEventListenerNS("http://www.w3.org/2001/xml-events", "repeatEvent", this.repeatListener, false, (Object)null);
               }

               return;
            }
         }
      }

      if (elt.hasAttributeNS((String)null, "onfocusin")) {
         target.addEventListenerNS("http://www.w3.org/2001/xml-events", "DOMFocusIn", this.focusinListener, false, (Object)null);
      }

      if (elt.hasAttributeNS((String)null, "onfocusout")) {
         target.addEventListenerNS("http://www.w3.org/2001/xml-events", "DOMFocusOut", this.focusoutListener, false, (Object)null);
      }

      if (elt.hasAttributeNS((String)null, "onactivate")) {
         target.addEventListenerNS("http://www.w3.org/2001/xml-events", "DOMActivate", this.activateListener, false, (Object)null);
      }

      if (elt.hasAttributeNS((String)null, "onclick")) {
         target.addEventListenerNS("http://www.w3.org/2001/xml-events", "click", this.clickListener, false, (Object)null);
      }

      if (elt.hasAttributeNS((String)null, "onmousedown")) {
         target.addEventListenerNS("http://www.w3.org/2001/xml-events", "mousedown", this.mousedownListener, false, (Object)null);
      }

      if (elt.hasAttributeNS((String)null, "onmouseup")) {
         target.addEventListenerNS("http://www.w3.org/2001/xml-events", "mouseup", this.mouseupListener, false, (Object)null);
      }

      if (elt.hasAttributeNS((String)null, "onmouseover")) {
         target.addEventListenerNS("http://www.w3.org/2001/xml-events", "mouseover", this.mouseoverListener, false, (Object)null);
      }

      if (elt.hasAttributeNS((String)null, "onmouseout")) {
         target.addEventListenerNS("http://www.w3.org/2001/xml-events", "mouseout", this.mouseoutListener, false, (Object)null);
      }

      if (elt.hasAttributeNS((String)null, "onmousemove")) {
         target.addEventListenerNS("http://www.w3.org/2001/xml-events", "mousemove", this.mousemoveListener, false, (Object)null);
      }

      if (elt.hasAttributeNS((String)null, "onkeypress")) {
         target.addEventListenerNS("http://www.w3.org/2001/xml-events", "keypress", this.keypressListener, false, (Object)null);
      }

      if (elt.hasAttributeNS((String)null, "onkeydown")) {
         target.addEventListenerNS("http://www.w3.org/2001/xml-events", "keydown", this.keydownListener, false, (Object)null);
      }

      if (elt.hasAttributeNS((String)null, "onkeyup")) {
         target.addEventListenerNS("http://www.w3.org/2001/xml-events", "keyup", this.keyupListener, false, (Object)null);
      }

   }

   protected void removeScriptingListeners(Node node) {
      if (node.getNodeType() == 1) {
         this.removeScriptingListenersOn((Element)node);
      }

      for(Node n = node.getFirstChild(); n != null; n = n.getNextSibling()) {
         this.removeScriptingListeners(n);
      }

   }

   protected void removeScriptingListenersOn(Element elt) {
      NodeEventTarget target = (NodeEventTarget)elt;
      if ("http://www.w3.org/2000/svg".equals(elt.getNamespaceURI())) {
         if ("svg".equals(elt.getLocalName())) {
            target.removeEventListenerNS("http://www.w3.org/2001/xml-events", "SVGAbort", this.svgAbortListener, false);
            target.removeEventListenerNS("http://www.w3.org/2001/xml-events", "SVGError", this.svgErrorListener, false);
            target.removeEventListenerNS("http://www.w3.org/2001/xml-events", "SVGResize", this.svgResizeListener, false);
            target.removeEventListenerNS("http://www.w3.org/2001/xml-events", "SVGScroll", this.svgScrollListener, false);
            target.removeEventListenerNS("http://www.w3.org/2001/xml-events", "SVGUnload", this.svgUnloadListener, false);
            target.removeEventListenerNS("http://www.w3.org/2001/xml-events", "SVGZoom", this.svgZoomListener, false);
         } else {
            String name = elt.getLocalName();
            if (name.equals("set") || name.startsWith("animate")) {
               target.removeEventListenerNS("http://www.w3.org/2001/xml-events", "beginEvent", this.beginListener, false);
               target.removeEventListenerNS("http://www.w3.org/2001/xml-events", "endEvent", this.endListener, false);
               target.removeEventListenerNS("http://www.w3.org/2001/xml-events", "repeatEvent", this.repeatListener, false);
               return;
            }
         }
      }

      target.removeEventListenerNS("http://www.w3.org/2001/xml-events", "DOMFocusIn", this.focusinListener, false);
      target.removeEventListenerNS("http://www.w3.org/2001/xml-events", "DOMFocusOut", this.focusoutListener, false);
      target.removeEventListenerNS("http://www.w3.org/2001/xml-events", "DOMActivate", this.activateListener, false);
      target.removeEventListenerNS("http://www.w3.org/2001/xml-events", "click", this.clickListener, false);
      target.removeEventListenerNS("http://www.w3.org/2001/xml-events", "mousedown", this.mousedownListener, false);
      target.removeEventListenerNS("http://www.w3.org/2001/xml-events", "mouseup", this.mouseupListener, false);
      target.removeEventListenerNS("http://www.w3.org/2001/xml-events", "mouseover", this.mouseoverListener, false);
      target.removeEventListenerNS("http://www.w3.org/2001/xml-events", "mouseout", this.mouseoutListener, false);
      target.removeEventListenerNS("http://www.w3.org/2001/xml-events", "mousemove", this.mousemoveListener, false);
      target.removeEventListenerNS("http://www.w3.org/2001/xml-events", "keypress", this.keypressListener, false);
      target.removeEventListenerNS("http://www.w3.org/2001/xml-events", "keydown", this.keydownListener, false);
      target.removeEventListenerNS("http://www.w3.org/2001/xml-events", "keyup", this.keyupListener, false);
   }

   protected void updateScriptingListeners(Element elt, String attr) {
      String domEvt = (String)this.attrToDOMEvent.get(attr);
      if (domEvt != null) {
         EventListener listener = (EventListener)this.attrToListener.get(attr);
         NodeEventTarget target = (NodeEventTarget)elt;
         if (elt.hasAttributeNS((String)null, attr)) {
            target.addEventListenerNS("http://www.w3.org/2001/xml-events", domEvt, listener, false, (Object)null);
         } else {
            target.removeEventListenerNS("http://www.w3.org/2001/xml-events", domEvt, listener, false);
         }

      }
   }

   protected class ScriptingEventListener implements EventListener {
      protected String attribute;

      public ScriptingEventListener(String attr) {
         this.attribute = attr;
      }

      public void handleEvent(Event evt) {
         Element elt = (Element)evt.getCurrentTarget();
         String script = elt.getAttributeNS((String)null, this.attribute);
         if (script.length() != 0) {
            DocumentLoader dl = ScriptingEnvironment.this.bridgeContext.getDocumentLoader();
            SVGDocument d = (SVGDocument)elt.getOwnerDocument();
            int line = dl.getLineNumber(elt);
            String desc = Messages.formatMessage("BaseScriptingEnvironment.constant.event.script.description", new Object[]{d.getURL(), this.attribute, line});

            Element e;
            for(e = elt; e != null && (!"http://www.w3.org/2000/svg".equals(e.getNamespaceURI()) || !"svg".equals(e.getLocalName())); e = SVGUtilities.getParentElement(e)) {
            }

            if (e != null) {
               String lang = e.getAttributeNS((String)null, "contentScriptType");
               ScriptingEnvironment.this.runEventHandler(script, evt, lang, desc);
            }
         }
      }
   }

   protected class DOMAttrModifiedListener implements EventListener {
      public void handleEvent(Event evt) {
         MutationEvent me = (MutationEvent)evt;
         if (me.getAttrChange() != 1) {
            ScriptingEnvironment.this.updateScriptingListeners((Element)me.getTarget(), me.getAttrName());
         }

      }
   }

   protected class DOMNodeRemovedListener implements EventListener {
      public void handleEvent(Event evt) {
         ScriptingEnvironment.this.removeScriptingListeners((Node)evt.getTarget());
      }
   }

   protected class DOMNodeInsertedListener implements EventListener {
      protected LinkedList toExecute = new LinkedList();

      public void handleEvent(Event evt) {
         Node n = (Node)evt.getTarget();
         ScriptingEnvironment.this.addScriptingListeners(n);
         this.gatherScriptElements(n);

         while(!this.toExecute.isEmpty()) {
            ScriptingEnvironment.this.loadScript((AbstractElement)this.toExecute.removeFirst());
         }

      }

      protected void gatherScriptElements(Node n) {
         if (n.getNodeType() == 1) {
            if (n instanceof SVGOMScriptElement) {
               this.toExecute.add(n);
            } else {
               for(n = n.getFirstChild(); n != null; n = n.getNextSibling()) {
                  this.gatherScriptElements(n);
               }
            }
         }

      }
   }

   protected class Window implements org.apache.batik.bridge.Window {
      protected Interpreter interpreter;
      protected String language;
      protected Location location;
      static final String DEFLATE = "deflate";
      static final String GZIP = "gzip";
      static final String UTF_8 = "UTF-8";

      public Window(Interpreter interp, String lang) {
         this.interpreter = interp;
         this.language = lang;
      }

      public Object setInterval(String script, long interval) {
         IntervalScriptTimerTask tt = new IntervalScriptTimerTask(script);
         ScriptingEnvironment.this.timer.schedule(tt, interval, interval);
         return tt;
      }

      public Object setInterval(Runnable r, long interval) {
         IntervalRunnableTimerTask tt = new IntervalRunnableTimerTask(r);
         ScriptingEnvironment.this.timer.schedule(tt, interval, interval);
         return tt;
      }

      public void clearInterval(Object interval) {
         if (interval != null) {
            ((TimerTask)interval).cancel();
         }
      }

      public Object setTimeout(String script, long timeout) {
         TimeoutScriptTimerTask tt = new TimeoutScriptTimerTask(script);
         ScriptingEnvironment.this.timer.schedule(tt, timeout);
         return tt;
      }

      public Object setTimeout(Runnable r, long timeout) {
         TimeoutRunnableTimerTask tt = new TimeoutRunnableTimerTask(r);
         ScriptingEnvironment.this.timer.schedule(tt, timeout);
         return tt;
      }

      public void clearTimeout(Object timeout) {
         if (timeout != null) {
            ((TimerTask)timeout).cancel();
         }
      }

      public Node parseXML(String text, Document doc) {
         SAXSVGDocumentFactory df = new SAXSVGDocumentFactory(XMLResourceDescriptor.getXMLParserClassName());
         URL urlObj = null;
         if (doc instanceof SVGOMDocument) {
            urlObj = ((SVGOMDocument)doc).getURLObject();
         }

         if (urlObj == null) {
            urlObj = ((SVGOMDocument)ScriptingEnvironment.this.bridgeContext.getDocument()).getURLObject();
         }

         String uri = urlObj == null ? "" : urlObj.toString();
         Node res = DOMUtilities.parseXML(text, doc, uri, (Map)null, (String)null, df);
         if (res != null) {
            return res;
         } else {
            if (doc instanceof SVGOMDocument) {
               Map prefixes = new HashMap();
               prefixes.put("xmlns", "http://www.w3.org/2000/xmlns/");
               prefixes.put("xmlns:xlink", "http://www.w3.org/1999/xlink");
               res = DOMUtilities.parseXML(text, doc, uri, prefixes, "svg", df);
               if (res != null) {
                  return res;
               }
            }

            SAXDocumentFactory sdf;
            if (doc != null) {
               sdf = new SAXDocumentFactory(doc.getImplementation(), XMLResourceDescriptor.getXMLParserClassName());
            } else {
               sdf = new SAXDocumentFactory(new GenericDOMImplementation(), XMLResourceDescriptor.getXMLParserClassName());
            }

            return DOMUtilities.parseXML(text, doc, uri, (Map)null, (String)null, sdf);
         }
      }

      public String printNode(Node n) {
         try {
            Writer writer = new StringWriter();
            DOMUtilities.writeNode(n, writer);
            writer.close();
            return writer.toString();
         } catch (IOException var3) {
            throw new RuntimeException(var3);
         }
      }

      public void getURL(String uri, org.apache.batik.bridge.Window.URLResponseHandler h) {
         this.getURL(uri, h, (String)null);
      }

      public void getURL(final String uri, final org.apache.batik.bridge.Window.URLResponseHandler h, final String enc) {
         Thread t = new Thread() {
            public void run() {
               try {
                  ParsedURL burl = ((SVGOMDocument)ScriptingEnvironment.this.document).getParsedURL();
                  final ParsedURL purl = new ParsedURL(burl, uri);
                  String e = null;
                  if (enc != null) {
                     e = EncodingUtilities.javaEncoding(enc);
                     e = e == null ? enc : e;
                  }

                  InputStream is = purl.openStream();
                  InputStreamReader rx;
                  if (e == null) {
                     rx = new InputStreamReader(is);
                  } else {
                     try {
                        rx = new InputStreamReader(is, e);
                     } catch (UnsupportedEncodingException var9) {
                        rx = new InputStreamReader(is);
                     }
                  }

                  Reader r = new BufferedReader(rx);
                  final StringBuffer sb = new StringBuffer();
                  char[] buf = new char[4096];

                  int read;
                  while((read = r.read(buf, 0, buf.length)) != -1) {
                     sb.append(buf, 0, read);
                  }

                  r.close();
                  ScriptingEnvironment.this.updateRunnableQueue.invokeLater(new Runnable() {
                     public void run() {
                        try {
                           h.getURLDone(true, purl.getContentType(), sb.toString());
                        } catch (Exception var2) {
                           if (ScriptingEnvironment.this.userAgent != null) {
                              ScriptingEnvironment.this.userAgent.displayError(var2);
                           }
                        }

                     }
                  });
               } catch (Exception var10) {
                  if (var10 instanceof SecurityException) {
                     ScriptingEnvironment.this.userAgent.displayError(var10);
                  }

                  ScriptingEnvironment.this.updateRunnableQueue.invokeLater(new Runnable() {
                     public void run() {
                        try {
                           h.getURLDone(false, (String)null, (String)null);
                        } catch (Exception var2) {
                           if (ScriptingEnvironment.this.userAgent != null) {
                              ScriptingEnvironment.this.userAgent.displayError(var2);
                           }
                        }

                     }
                  });
               }

            }
         };
         t.setPriority(1);
         t.start();
      }

      public void postURL(String uri, String content, org.apache.batik.bridge.Window.URLResponseHandler h) {
         this.postURL(uri, content, h, "text/plain", (String)null);
      }

      public void postURL(String uri, String content, org.apache.batik.bridge.Window.URLResponseHandler h, String mimeType) {
         this.postURL(uri, content, h, mimeType, (String)null);
      }

      public void postURL(final String uri, final String content, final org.apache.batik.bridge.Window.URLResponseHandler h, final String mimeType, final String fEnc) {
         Thread t = new Thread() {
            public void run() {
               try {
                  String base = ScriptingEnvironment.this.document.getDocumentURI();
                  URL url;
                  if (base == null) {
                     url = new URL(uri);
                  } else {
                     url = new URL(new URL(base), uri);
                  }

                  final URLConnection conn = url.openConnection();
                  conn.setDoOutput(true);
                  conn.setDoInput(true);
                  conn.setUseCaches(false);
                  conn.setRequestProperty("Content-Type", mimeType);
                  OutputStream os = conn.getOutputStream();
                  String e = null;
                  String enc = fEnc;
                  if (enc != null) {
                     if (enc.startsWith("deflate")) {
                        os = new DeflaterOutputStream((OutputStream)os);
                        if (enc.length() > "deflate".length()) {
                           enc = enc.substring("deflate".length() + 1);
                        } else {
                           enc = "";
                        }

                        conn.setRequestProperty("Content-Encoding", "deflate");
                     }

                     if (enc.startsWith("gzip")) {
                        os = new GZIPOutputStream((OutputStream)os);
                        if (enc.length() > "gzip".length()) {
                           enc = enc.substring("gzip".length() + 1);
                        } else {
                           enc = "";
                        }

                        conn.setRequestProperty("Content-Encoding", "deflate");
                     }

                     if (enc.length() != 0) {
                        e = EncodingUtilities.javaEncoding(enc);
                        if (e == null) {
                           e = "UTF-8";
                        }
                     } else {
                        e = "UTF-8";
                     }
                  }

                  OutputStreamWriter w;
                  if (e == null) {
                     w = new OutputStreamWriter((OutputStream)os);
                  } else {
                     w = new OutputStreamWriter((OutputStream)os, e);
                  }

                  w.write(content);
                  w.flush();
                  w.close();
                  ((OutputStream)os).close();
                  InputStream is = conn.getInputStream();
                  e = "UTF-8";
                  InputStreamReader rx;
                  if (e == null) {
                     rx = new InputStreamReader(is);
                  } else {
                     rx = new InputStreamReader(is, e);
                  }

                  Reader r = new BufferedReader(rx);
                  final StringBuffer sb = new StringBuffer();
                  char[] buf = new char[4096];

                  int read;
                  while((read = r.read(buf, 0, buf.length)) != -1) {
                     sb.append(buf, 0, read);
                  }

                  r.close();
                  ScriptingEnvironment.this.updateRunnableQueue.invokeLater(new Runnable() {
                     public void run() {
                        try {
                           h.getURLDone(true, conn.getContentType(), sb.toString());
                        } catch (Exception var2) {
                           if (ScriptingEnvironment.this.userAgent != null) {
                              ScriptingEnvironment.this.userAgent.displayError(var2);
                           }
                        }

                     }
                  });
               } catch (Exception var13) {
                  if (var13 instanceof SecurityException) {
                     ScriptingEnvironment.this.userAgent.displayError(var13);
                  }

                  ScriptingEnvironment.this.updateRunnableQueue.invokeLater(new Runnable() {
                     public void run() {
                        try {
                           h.getURLDone(false, (String)null, (String)null);
                        } catch (Exception var2) {
                           if (ScriptingEnvironment.this.userAgent != null) {
                              ScriptingEnvironment.this.userAgent.displayError(var2);
                           }
                        }

                     }
                  });
               }

            }
         };
         t.setPriority(1);
         t.start();
      }

      public void alert(String message) {
         if (ScriptingEnvironment.this.userAgent != null) {
            ScriptingEnvironment.this.userAgent.showAlert(message);
         }

      }

      public boolean confirm(String message) {
         return ScriptingEnvironment.this.userAgent != null ? ScriptingEnvironment.this.userAgent.showConfirm(message) : false;
      }

      public String prompt(String message) {
         return ScriptingEnvironment.this.userAgent != null ? ScriptingEnvironment.this.userAgent.showPrompt(message) : null;
      }

      public String prompt(String message, String defVal) {
         return ScriptingEnvironment.this.userAgent != null ? ScriptingEnvironment.this.userAgent.showPrompt(message, defVal) : null;
      }

      public BridgeContext getBridgeContext() {
         return ScriptingEnvironment.this.bridgeContext;
      }

      public Interpreter getInterpreter() {
         return this.interpreter;
      }

      public org.apache.batik.w3c.dom.Window getParent() {
         return null;
      }

      public org.apache.batik.w3c.dom.Location getLocation() {
         if (this.location == null) {
            this.location = new Location(ScriptingEnvironment.this.bridgeContext);
         }

         return this.location;
      }

      protected class TimeoutRunnableTimerTask extends TimerTask {
         private Runnable r;

         public TimeoutRunnableTimerTask(Runnable r) {
            this.r = r;
         }

         public void run() {
            ScriptingEnvironment.this.updateRunnableQueue.invokeLater(new Runnable() {
               public void run() {
                  try {
                     TimeoutRunnableTimerTask.this.r.run();
                  } catch (Exception var2) {
                     if (ScriptingEnvironment.this.userAgent != null) {
                        ScriptingEnvironment.this.userAgent.displayError(var2);
                     }
                  }

               }
            });
         }
      }

      protected class TimeoutScriptTimerTask extends TimerTask {
         private String script;

         public TimeoutScriptTimerTask(String script) {
            this.script = script;
         }

         public void run() {
            ScriptingEnvironment.this.updateRunnableQueue.invokeLater(ScriptingEnvironment.this.new EvaluateRunnable(this.script, Window.this.interpreter));
         }
      }

      protected class IntervalRunnableTimerTask extends TimerTask {
         protected EvaluateRunnableRunnable eihr;

         public IntervalRunnableTimerTask(Runnable r) {
            this.eihr = ScriptingEnvironment.this.new EvaluateRunnableRunnable(r);
         }

         public void run() {
            synchronized(this.eihr) {
               if (this.eihr.count > 1) {
                  return;
               }

               ++this.eihr.count;
            }

            ScriptingEnvironment.this.updateRunnableQueue.invokeLater(this.eihr);
            synchronized(this.eihr) {
               if (this.eihr.error) {
                  this.cancel();
               }

            }
         }
      }

      protected class IntervalScriptTimerTask extends TimerTask {
         protected EvaluateIntervalRunnable eir;

         public IntervalScriptTimerTask(String script) {
            this.eir = ScriptingEnvironment.this.new EvaluateIntervalRunnable(script, Window.this.interpreter);
         }

         public void run() {
            synchronized(this.eir) {
               if (this.eir.count > 1) {
                  return;
               }

               ++this.eir.count;
            }

            synchronized(ScriptingEnvironment.this.updateRunnableQueue.getIteratorLock()) {
               if (ScriptingEnvironment.this.updateRunnableQueue.getThread() == null) {
                  this.cancel();
                  return;
               }

               ScriptingEnvironment.this.updateRunnableQueue.invokeLater(this.eir);
            }

            synchronized(this.eir) {
               if (this.eir.error) {
                  this.cancel();
               }

            }
         }
      }
   }

   protected class EvaluateRunnableRunnable implements Runnable {
      public int count;
      public boolean error;
      protected Runnable runnable;

      public EvaluateRunnableRunnable(Runnable r) {
         this.runnable = r;
      }

      public void run() {
         synchronized(this) {
            if (this.error) {
               return;
            }

            --this.count;
         }

         try {
            this.runnable.run();
         } catch (Exception var5) {
            if (ScriptingEnvironment.this.userAgent != null) {
               ScriptingEnvironment.this.userAgent.displayError(var5);
            } else {
               var5.printStackTrace();
            }

            synchronized(this) {
               this.error = true;
            }
         }

      }
   }

   protected class EvaluateIntervalRunnable implements Runnable {
      public int count;
      public boolean error;
      protected Interpreter interpreter;
      protected String script;

      public EvaluateIntervalRunnable(String s, Interpreter interp) {
         this.interpreter = interp;
         this.script = s;
      }

      public void run() {
         synchronized(this) {
            if (this.error) {
               return;
            }

            --this.count;
         }

         try {
            this.interpreter.evaluate(this.script);
         } catch (InterpreterException var7) {
            ScriptingEnvironment.this.handleInterpreterException(var7);
            synchronized(this) {
               this.error = true;
            }
         } catch (Exception var8) {
            if (ScriptingEnvironment.this.userAgent != null) {
               ScriptingEnvironment.this.userAgent.displayError(var8);
            } else {
               var8.printStackTrace();
            }

            synchronized(this) {
               this.error = true;
            }
         }

      }
   }

   protected class EvaluateRunnable implements Runnable {
      protected Interpreter interpreter;
      protected String script;

      public EvaluateRunnable(String s, Interpreter interp) {
         this.interpreter = interp;
         this.script = s;
      }

      public void run() {
         try {
            this.interpreter.evaluate(this.script);
         } catch (InterpreterException var2) {
            ScriptingEnvironment.this.handleInterpreterException(var2);
         }

      }
   }
}
