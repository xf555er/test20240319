package org.apache.batik.bridge;

import java.awt.Cursor;
import java.awt.geom.Dimension2D;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import org.apache.batik.anim.dom.AnimatedAttributeListener;
import org.apache.batik.anim.dom.AnimatedLiveAttributeValue;
import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.anim.dom.SVGOMDocument;
import org.apache.batik.anim.dom.SVGOMElement;
import org.apache.batik.anim.dom.SVGStylableElement;
import org.apache.batik.bridge.svg12.SVG12BridgeContext;
import org.apache.batik.bridge.svg12.SVG12BridgeExtension;
import org.apache.batik.css.engine.CSSContext;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSEngineEvent;
import org.apache.batik.css.engine.CSSEngineListener;
import org.apache.batik.css.engine.CSSEngineUserAgent;
import org.apache.batik.css.engine.SystemColorSupport;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.dom.AbstractNode;
import org.apache.batik.dom.events.NodeEventTarget;
import org.apache.batik.dom.svg.SVGContext;
import org.apache.batik.dom.xbl.XBLManager;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.script.ImportInfo;
import org.apache.batik.script.Interpreter;
import org.apache.batik.script.InterpreterPool;
import org.apache.batik.util.CleanerThread;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.util.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.events.MouseEvent;
import org.w3c.dom.events.MutationEvent;
import org.w3c.dom.svg.SVGDocument;

public class BridgeContext implements ErrorConstants, CSSContext {
   protected Document document;
   protected boolean isSVG12;
   protected GVTBuilder gvtBuilder;
   protected Map interpreterMap;
   private Map fontFamilyMap;
   protected Map viewportMap;
   protected List viewportStack;
   protected UserAgent userAgent;
   protected Map elementNodeMap;
   protected Map nodeElementMap;
   protected Map namespaceURIMap;
   protected Bridge defaultBridge;
   protected Set reservedNamespaceSet;
   protected Map elementDataMap;
   protected InterpreterPool interpreterPool;
   protected DocumentLoader documentLoader;
   protected Dimension2D documentSize;
   protected TextPainter textPainter;
   public static final int STATIC = 0;
   public static final int INTERACTIVE = 1;
   public static final int DYNAMIC = 2;
   protected int dynamicStatus;
   protected UpdateManager updateManager;
   protected XBLManager xblManager;
   protected BridgeContext primaryContext;
   protected HashSet childContexts;
   protected SVGAnimationEngine animationEngine;
   protected int animationLimitingMode;
   protected float animationLimitingAmount;
   private static InterpreterPool sharedPool = new InterpreterPool();
   protected Set eventListenerSet;
   protected EventListener domCharacterDataModifiedEventListener;
   protected EventListener domAttrModifiedEventListener;
   protected EventListener domNodeInsertedEventListener;
   protected EventListener domNodeRemovedEventListener;
   protected CSSEngineListener cssPropertiesChangedListener;
   protected AnimatedAttributeListener animatedAttributeListener;
   protected FocusManager focusManager;
   protected CursorManager cursorManager;
   protected List extensions;
   protected static List globalExtensions = null;

   protected BridgeContext() {
      this.interpreterMap = new HashMap(7);
      this.viewportMap = new WeakHashMap();
      this.viewportStack = new LinkedList();
      this.dynamicStatus = 0;
      this.childContexts = new HashSet();
      this.eventListenerSet = new HashSet();
      this.cursorManager = new CursorManager(this);
      this.extensions = null;
   }

   public final FontFamilyResolver getFontFamilyResolver() {
      return this.userAgent.getFontFamilyResolver();
   }

   public BridgeContext(UserAgent userAgent) {
      this(userAgent, sharedPool, new DocumentLoader(userAgent));
   }

   public BridgeContext(UserAgent userAgent, DocumentLoader loader) {
      this(userAgent, sharedPool, loader);
   }

   public BridgeContext(UserAgent userAgent, InterpreterPool interpreterPool, DocumentLoader documentLoader) {
      this.interpreterMap = new HashMap(7);
      this.viewportMap = new WeakHashMap();
      this.viewportStack = new LinkedList();
      this.dynamicStatus = 0;
      this.childContexts = new HashSet();
      this.eventListenerSet = new HashSet();
      this.cursorManager = new CursorManager(this);
      this.extensions = null;
      this.userAgent = userAgent;
      this.viewportMap.put(userAgent, new UserAgentViewport(userAgent));
      this.interpreterPool = interpreterPool;
      this.documentLoader = documentLoader;
   }

   protected void finalize() {
      if (this.primaryContext != null) {
         this.dispose();
      }

   }

   public BridgeContext createSubBridgeContext(SVGOMDocument newDoc) {
      CSSEngine eng = newDoc.getCSSEngine();
      BridgeContext subCtx;
      if (eng != null) {
         subCtx = (BridgeContext)newDoc.getCSSEngine().getCSSContext();
         return subCtx;
      } else {
         subCtx = this.createBridgeContext(newDoc);
         subCtx.primaryContext = this.primaryContext != null ? this.primaryContext : this;
         subCtx.primaryContext.childContexts.add(new WeakReference(subCtx));
         subCtx.dynamicStatus = this.dynamicStatus;
         subCtx.setGVTBuilder(this.getGVTBuilder());
         subCtx.setTextPainter(this.getTextPainter());
         subCtx.setDocument(newDoc);
         subCtx.initializeDocument(newDoc);
         if (this.isInteractive()) {
            subCtx.addUIEventListeners(newDoc);
         }

         return subCtx;
      }
   }

   public BridgeContext createBridgeContext(SVGOMDocument doc) {
      return (BridgeContext)(doc.isSVG12() ? new SVG12BridgeContext(this.getUserAgent(), this.getDocumentLoader()) : new BridgeContext(this.getUserAgent(), this.getDocumentLoader()));
   }

   protected void initializeDocument(Document document) {
      SVGOMDocument doc = (SVGOMDocument)document;
      CSSEngine eng = doc.getCSSEngine();
      if (eng == null) {
         SVGDOMImplementation impl = (SVGDOMImplementation)doc.getImplementation();
         eng = impl.createCSSEngine(doc, this);
         eng.setCSSEngineUserAgent(new CSSEngineUserAgentWrapper(this.userAgent));
         doc.setCSSEngine(eng);
         eng.setMedia(this.userAgent.getMedia());
         String uri = this.userAgent.getUserStyleSheetURI();
         if (uri != null) {
            try {
               ParsedURL url = new ParsedURL(uri);
               eng.setUserAgentStyleSheet(eng.parseStyleSheet(url, "all"));
            } catch (Exception var7) {
               this.userAgent.displayError(var7);
            }
         }

         eng.setAlternateStyleSheet(this.userAgent.getAlternateStyleSheet());
      }

   }

   public CSSEngine getCSSEngineForElement(Element e) {
      SVGOMDocument doc = (SVGOMDocument)e.getOwnerDocument();
      return doc.getCSSEngine();
   }

   public void setTextPainter(TextPainter textPainter) {
      this.textPainter = textPainter;
   }

   public TextPainter getTextPainter() {
      return this.textPainter;
   }

   public Document getDocument() {
      return this.document;
   }

   protected void setDocument(Document document) {
      if (this.document != document) {
         this.fontFamilyMap = null;
      }

      this.document = document;
      this.isSVG12 = ((SVGOMDocument)document).isSVG12();
      this.registerSVGBridges();
   }

   public Map getFontFamilyMap() {
      if (this.fontFamilyMap == null) {
         this.fontFamilyMap = new HashMap();
      }

      return this.fontFamilyMap;
   }

   protected void setFontFamilyMap(Map fontFamilyMap) {
      this.fontFamilyMap = fontFamilyMap;
   }

   public void setElementData(Node n, Object data) {
      if (this.elementDataMap == null) {
         this.elementDataMap = new WeakHashMap();
      }

      this.elementDataMap.put(n, new SoftReference(data));
   }

   public Object getElementData(Node n) {
      if (this.elementDataMap == null) {
         return null;
      } else {
         Object o = this.elementDataMap.get(n);
         if (o == null) {
            return null;
         } else {
            SoftReference sr = (SoftReference)o;
            o = sr.get();
            if (o == null) {
               this.elementDataMap.remove(n);
            }

            return o;
         }
      }
   }

   public UserAgent getUserAgent() {
      return this.userAgent;
   }

   protected void setUserAgent(UserAgent userAgent) {
      this.userAgent = userAgent;
   }

   public GVTBuilder getGVTBuilder() {
      return this.gvtBuilder;
   }

   protected void setGVTBuilder(GVTBuilder gvtBuilder) {
      this.gvtBuilder = gvtBuilder;
   }

   public InterpreterPool getInterpreterPool() {
      return this.interpreterPool;
   }

   public FocusManager getFocusManager() {
      return this.focusManager;
   }

   public CursorManager getCursorManager() {
      return this.cursorManager;
   }

   protected void setInterpreterPool(InterpreterPool interpreterPool) {
      this.interpreterPool = interpreterPool;
   }

   public Interpreter getInterpreter(String language) {
      if (this.document == null) {
         throw new RuntimeException("Unknown document");
      } else {
         Interpreter interpreter = (Interpreter)this.interpreterMap.get(language);
         if (interpreter == null) {
            try {
               interpreter = this.interpreterPool.createInterpreter(this.document, language, (ImportInfo)null);
               String[] mimeTypes = interpreter.getMimeTypes();
               String[] var4 = mimeTypes;
               int var5 = mimeTypes.length;

               for(int var6 = 0; var6 < var5; ++var6) {
                  String mimeType = var4[var6];
                  this.interpreterMap.put(mimeType, interpreter);
               }
            } catch (Exception var8) {
               if (this.userAgent != null) {
                  this.userAgent.displayError(var8);
                  return null;
               }
            }
         }

         if (interpreter == null && this.userAgent != null) {
            this.userAgent.displayError(new Exception("Unknown language: " + language));
         }

         return interpreter;
      }
   }

   public DocumentLoader getDocumentLoader() {
      return this.documentLoader;
   }

   protected void setDocumentLoader(DocumentLoader newDocumentLoader) {
      this.documentLoader = newDocumentLoader;
   }

   public Dimension2D getDocumentSize() {
      return this.documentSize;
   }

   protected void setDocumentSize(Dimension2D d) {
      this.documentSize = d;
   }

   public boolean isDynamic() {
      return this.dynamicStatus == 2;
   }

   public boolean isInteractive() {
      return this.dynamicStatus != 0;
   }

   public void setDynamicState(int status) {
      this.dynamicStatus = status;
   }

   public void setDynamic(boolean dynamic) {
      if (dynamic) {
         this.setDynamicState(2);
      } else {
         this.setDynamicState(0);
      }

   }

   public void setInteractive(boolean interactive) {
      if (interactive) {
         this.setDynamicState(1);
      } else {
         this.setDynamicState(0);
      }

   }

   public UpdateManager getUpdateManager() {
      return this.updateManager;
   }

   protected void setUpdateManager(UpdateManager um) {
      this.updateManager = um;
   }

   protected void setUpdateManager(BridgeContext ctx, UpdateManager um) {
      ctx.setUpdateManager(um);
   }

   protected void setXBLManager(BridgeContext ctx, XBLManager xm) {
      ctx.xblManager = xm;
   }

   public boolean isSVG12() {
      return this.isSVG12;
   }

   public BridgeContext getPrimaryBridgeContext() {
      return this.primaryContext != null ? this.primaryContext : this;
   }

   public BridgeContext[] getChildContexts() {
      BridgeContext[] res = new BridgeContext[this.childContexts.size()];
      Iterator it = this.childContexts.iterator();

      for(int i = 0; i < res.length; ++i) {
         WeakReference wr = (WeakReference)it.next();
         res[i] = (BridgeContext)wr.get();
      }

      return res;
   }

   public SVGAnimationEngine getAnimationEngine() {
      if (this.animationEngine == null) {
         this.animationEngine = new SVGAnimationEngine(this.document, this);
         this.setAnimationLimitingMode();
      }

      return this.animationEngine;
   }

   public URIResolver createURIResolver(SVGDocument doc, DocumentLoader dl) {
      return new URIResolver(doc, dl);
   }

   public Node getReferencedNode(Element e, String uri) {
      try {
         SVGDocument document = (SVGDocument)e.getOwnerDocument();
         URIResolver ur = this.createURIResolver(document, this.documentLoader);
         Node ref = ur.getNode(uri, e);
         if (ref == null) {
            throw new BridgeException(this, e, "uri.badTarget", new Object[]{uri});
         } else {
            SVGOMDocument refDoc = (SVGOMDocument)((SVGOMDocument)(ref.getNodeType() == 9 ? ref : ref.getOwnerDocument()));
            if (refDoc != document) {
               this.createSubBridgeContext(refDoc);
            }

            return ref;
         }
      } catch (MalformedURLException var7) {
         throw new BridgeException(this, e, var7, "uri.malformed", new Object[]{uri});
      } catch (InterruptedIOException var8) {
         throw new InterruptedBridgeException();
      } catch (IOException var9) {
         throw new BridgeException(this, e, var9, "uri.io", new Object[]{uri});
      } catch (SecurityException var10) {
         throw new BridgeException(this, e, var10, "uri.unsecure", new Object[]{uri});
      }
   }

   public Element getReferencedElement(Element e, String uri) {
      Node ref = this.getReferencedNode(e, uri);
      if (ref != null && ref.getNodeType() != 1) {
         throw new BridgeException(this, e, "uri.referenceDocument", new Object[]{uri});
      } else {
         return (Element)ref;
      }
   }

   public Viewport getViewport(Element e) {
      if (this.viewportStack != null) {
         return this.viewportStack.size() == 0 ? (Viewport)this.viewportMap.get(this.userAgent) : (Viewport)this.viewportStack.get(0);
      } else {
         for(e = SVGUtilities.getParentElement(e); e != null; e = SVGUtilities.getParentElement(e)) {
            Viewport viewport = (Viewport)this.viewportMap.get(e);
            if (viewport != null) {
               return viewport;
            }
         }

         return (Viewport)this.viewportMap.get(this.userAgent);
      }
   }

   public void openViewport(Element e, Viewport viewport) {
      this.viewportMap.put(e, viewport);
      if (this.viewportStack == null) {
         this.viewportStack = new LinkedList();
      }

      this.viewportStack.add(0, viewport);
   }

   public void removeViewport(Element e) {
      this.viewportMap.remove(e);
   }

   public void closeViewport(Element e) {
      this.viewportStack.remove(0);
      if (this.viewportStack.size() == 0) {
         this.viewportStack = null;
      }

   }

   public void bind(Node node, GraphicsNode gn) {
      if (this.elementNodeMap == null) {
         this.elementNodeMap = new WeakHashMap();
         this.nodeElementMap = new WeakHashMap();
      }

      this.elementNodeMap.put(node, new SoftReference(gn));
      this.nodeElementMap.put(gn, new SoftReference(node));
   }

   public void unbind(Node node) {
      if (this.elementNodeMap != null) {
         GraphicsNode gn = null;
         SoftReference sr = (SoftReference)this.elementNodeMap.get(node);
         if (sr != null) {
            gn = (GraphicsNode)sr.get();
         }

         this.elementNodeMap.remove(node);
         if (gn != null) {
            this.nodeElementMap.remove(gn);
         }

      }
   }

   public GraphicsNode getGraphicsNode(Node node) {
      if (this.elementNodeMap != null) {
         SoftReference sr = (SoftReference)this.elementNodeMap.get(node);
         if (sr != null) {
            return (GraphicsNode)sr.get();
         }
      }

      return null;
   }

   public Element getElement(GraphicsNode gn) {
      if (this.nodeElementMap != null) {
         SoftReference sr = (SoftReference)this.nodeElementMap.get(gn);
         if (sr != null) {
            Node n = (Node)sr.get();
            if (n.getNodeType() == 1) {
               return (Element)n;
            }
         }
      }

      return null;
   }

   public boolean hasGraphicsNodeBridge(Element element) {
      if (this.namespaceURIMap != null && element != null) {
         String localName = element.getLocalName();
         String namespaceURI = element.getNamespaceURI();
         namespaceURI = namespaceURI == null ? "" : namespaceURI;
         HashMap localNameMap = (HashMap)this.namespaceURIMap.get(namespaceURI);
         return localNameMap == null ? false : localNameMap.get(localName) instanceof GraphicsNodeBridge;
      } else {
         return false;
      }
   }

   public DocumentBridge getDocumentBridge() {
      return new SVGDocumentBridge();
   }

   public Bridge getBridge(Element element) {
      if (this.namespaceURIMap != null && element != null) {
         String localName = element.getLocalName();
         String namespaceURI = element.getNamespaceURI();
         namespaceURI = namespaceURI == null ? "" : namespaceURI;
         return this.getBridge(namespaceURI, localName);
      } else {
         return null;
      }
   }

   public Bridge getBridge(String namespaceURI, String localName) {
      Bridge bridge = null;
      if (this.namespaceURIMap != null) {
         HashMap localNameMap = (HashMap)this.namespaceURIMap.get(namespaceURI);
         if (localNameMap != null) {
            bridge = (Bridge)localNameMap.get(localName);
         }
      }

      if (bridge == null && (this.reservedNamespaceSet == null || !this.reservedNamespaceSet.contains(namespaceURI))) {
         bridge = this.defaultBridge;
      }

      if (this.isDynamic()) {
         return bridge == null ? null : bridge.getInstance();
      } else {
         return bridge;
      }
   }

   public void putBridge(String namespaceURI, String localName, Bridge bridge) {
      if (namespaceURI.equals(bridge.getNamespaceURI()) && localName.equals(bridge.getLocalName())) {
         if (this.namespaceURIMap == null) {
            this.namespaceURIMap = new HashMap();
         }

         namespaceURI = namespaceURI == null ? "" : namespaceURI;
         HashMap localNameMap = (HashMap)this.namespaceURIMap.get(namespaceURI);
         if (localNameMap == null) {
            localNameMap = new HashMap();
            this.namespaceURIMap.put(namespaceURI, localNameMap);
         }

         localNameMap.put(localName, bridge);
      } else {
         throw new RuntimeException("Invalid Bridge: " + namespaceURI + "/" + bridge.getNamespaceURI() + " " + localName + "/" + bridge.getLocalName() + " " + bridge.getClass());
      }
   }

   public void putBridge(Bridge bridge) {
      this.putBridge(bridge.getNamespaceURI(), bridge.getLocalName(), bridge);
   }

   public void removeBridge(String namespaceURI, String localName) {
      if (this.namespaceURIMap != null) {
         namespaceURI = namespaceURI == null ? "" : namespaceURI;
         HashMap localNameMap = (HashMap)this.namespaceURIMap.get(namespaceURI);
         if (localNameMap != null) {
            localNameMap.remove(localName);
            if (localNameMap.isEmpty()) {
               this.namespaceURIMap.remove(namespaceURI);
               if (this.namespaceURIMap.isEmpty()) {
                  this.namespaceURIMap = null;
               }
            }
         }

      }
   }

   public void setDefaultBridge(Bridge bridge) {
      this.defaultBridge = bridge;
   }

   public void putReservedNamespaceURI(String namespaceURI) {
      if (namespaceURI == null) {
         namespaceURI = "";
      }

      if (this.reservedNamespaceSet == null) {
         this.reservedNamespaceSet = new HashSet();
      }

      this.reservedNamespaceSet.add(namespaceURI);
   }

   public void removeReservedNamespaceURI(String namespaceURI) {
      if (namespaceURI == null) {
         namespaceURI = "";
      }

      if (this.reservedNamespaceSet != null) {
         this.reservedNamespaceSet.remove(namespaceURI);
         if (this.reservedNamespaceSet.isEmpty()) {
            this.reservedNamespaceSet = null;
         }
      }

   }

   public void addUIEventListeners(Document doc) {
      NodeEventTarget evtTarget = (NodeEventTarget)doc.getDocumentElement();
      DOMMouseOverEventListener domMouseOverListener = new DOMMouseOverEventListener();
      evtTarget.addEventListenerNS("http://www.w3.org/2001/xml-events", "mouseover", domMouseOverListener, true, (Object)null);
      this.storeEventListenerNS(evtTarget, "http://www.w3.org/2001/xml-events", "mouseover", domMouseOverListener, true);
      DOMMouseOutEventListener domMouseOutListener = new DOMMouseOutEventListener();
      evtTarget.addEventListenerNS("http://www.w3.org/2001/xml-events", "mouseout", domMouseOutListener, true, (Object)null);
      this.storeEventListenerNS(evtTarget, "http://www.w3.org/2001/xml-events", "mouseout", domMouseOutListener, true);
   }

   public void removeUIEventListeners(Document doc) {
      EventTarget evtTarget = (EventTarget)doc.getDocumentElement();
      synchronized(this.eventListenerSet) {
         Iterator var4 = this.eventListenerSet.iterator();

         while(var4.hasNext()) {
            Object anEventListenerSet = var4.next();
            EventListenerMememto elm = (EventListenerMememto)anEventListenerSet;
            NodeEventTarget et = elm.getTarget();
            if (et == evtTarget) {
               EventListener el = elm.getListener();
               boolean uc = elm.getUseCapture();
               String t = elm.getEventType();
               boolean n = elm.getNamespaced();
               if (et != null && el != null && t != null) {
                  if (n) {
                     String ns = elm.getNamespaceURI();
                     et.removeEventListenerNS(ns, t, el, uc);
                  } else {
                     et.removeEventListener(t, el, uc);
                  }
               }
            }
         }

      }
   }

   public void addDOMListeners() {
      SVGOMDocument doc = (SVGOMDocument)this.document;
      this.domAttrModifiedEventListener = new DOMAttrModifiedEventListener();
      doc.addEventListenerNS("http://www.w3.org/2001/xml-events", "DOMAttrModified", this.domAttrModifiedEventListener, true, (Object)null);
      this.domNodeInsertedEventListener = new DOMNodeInsertedEventListener();
      doc.addEventListenerNS("http://www.w3.org/2001/xml-events", "DOMNodeInserted", this.domNodeInsertedEventListener, true, (Object)null);
      this.domNodeRemovedEventListener = new DOMNodeRemovedEventListener();
      doc.addEventListenerNS("http://www.w3.org/2001/xml-events", "DOMNodeRemoved", this.domNodeRemovedEventListener, true, (Object)null);
      this.domCharacterDataModifiedEventListener = new DOMCharacterDataModifiedEventListener();
      doc.addEventListenerNS("http://www.w3.org/2001/xml-events", "DOMCharacterDataModified", this.domCharacterDataModifiedEventListener, true, (Object)null);
      this.animatedAttributeListener = new AnimatedAttrListener();
      doc.addAnimatedAttributeListener(this.animatedAttributeListener);
      this.focusManager = new FocusManager(this.document);
      CSSEngine cssEngine = doc.getCSSEngine();
      this.cssPropertiesChangedListener = new CSSPropertiesChangedListener();
      cssEngine.addCSSEngineListener(this.cssPropertiesChangedListener);
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

   protected void storeEventListener(EventTarget t, String s, EventListener l, boolean b) {
      synchronized(this.eventListenerSet) {
         this.eventListenerSet.add(new EventListenerMememto(t, s, l, b, this));
      }
   }

   protected void storeEventListenerNS(EventTarget t, String n, String s, EventListener l, boolean b) {
      synchronized(this.eventListenerSet) {
         this.eventListenerSet.add(new EventListenerMememto(t, n, s, l, b, this));
      }
   }

   public void addGVTListener(Document doc) {
      BridgeEventSupport.addGVTListener(this, doc);
   }

   protected void clearChildContexts() {
      this.childContexts.clear();
   }

   public void dispose() {
      this.clearChildContexts();
      synchronized(this.eventListenerSet) {
         Iterator var2 = this.eventListenerSet.iterator();

         while(true) {
            if (!var2.hasNext()) {
               break;
            }

            Object anEventListenerSet = var2.next();
            EventListenerMememto m = (EventListenerMememto)anEventListenerSet;
            NodeEventTarget et = m.getTarget();
            EventListener el = m.getListener();
            boolean uc = m.getUseCapture();
            String t = m.getEventType();
            boolean n = m.getNamespaced();
            if (et != null && el != null && t != null) {
               if (n) {
                  String ns = m.getNamespaceURI();
                  et.removeEventListenerNS(ns, t, el, uc);
               } else {
                  et.removeEventListener(t, el, uc);
               }
            }
         }
      }

      if (this.document != null) {
         this.removeDOMListeners();
         AbstractGraphicsNodeBridge.disposeTree(this.document);
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

      if (this.elementDataMap != null) {
         this.elementDataMap.clear();
      }

      if (this.nodeElementMap != null) {
         this.nodeElementMap.clear();
      }

      if (this.elementNodeMap != null) {
         this.elementNodeMap.clear();
      }

   }

   protected static SVGContext getSVGContext(Node node) {
      if (node instanceof SVGOMElement) {
         return ((SVGOMElement)node).getSVGContext();
      } else {
         return node instanceof SVGOMDocument ? ((SVGOMDocument)node).getSVGContext() : null;
      }
   }

   protected static BridgeUpdateHandler getBridgeUpdateHandler(Node node) {
      SVGContext ctx = getSVGContext(node);
      return ctx == null ? null : (BridgeUpdateHandler)ctx;
   }

   public Value getSystemColor(String ident) {
      return SystemColorSupport.getSystemColor(ident);
   }

   public Value getDefaultFontFamily() {
      SVGOMDocument doc = (SVGOMDocument)this.document;
      SVGStylableElement root = (SVGStylableElement)doc.getRootElement();
      String str = this.userAgent.getDefaultFontFamily();
      return doc.getCSSEngine().parsePropertyValue(root, "font-family", str);
   }

   public float getLighterFontWeight(float f) {
      return this.userAgent.getLighterFontWeight(f);
   }

   public float getBolderFontWeight(float f) {
      return this.userAgent.getBolderFontWeight(f);
   }

   public float getPixelUnitToMillimeter() {
      return this.userAgent.getPixelUnitToMillimeter();
   }

   public float getPixelToMillimeter() {
      return this.getPixelUnitToMillimeter();
   }

   public float getMediumFontSize() {
      return this.userAgent.getMediumFontSize();
   }

   public float getBlockWidth(Element elt) {
      return this.getViewport(elt).getWidth();
   }

   public float getBlockHeight(Element elt) {
      return this.getViewport(elt).getHeight();
   }

   public void checkLoadExternalResource(ParsedURL resourceURL, ParsedURL docURL) throws SecurityException {
      this.userAgent.checkLoadExternalResource(resourceURL, docURL);
   }

   public boolean isDynamicDocument(Document doc) {
      return BaseScriptingEnvironment.isDynamicDocument(this, doc);
   }

   public boolean isInteractiveDocument(Document doc) {
      Element root = ((SVGDocument)doc).getRootElement();
      return !"http://www.w3.org/2000/svg".equals(root.getNamespaceURI()) ? false : this.checkInteractiveElement(root);
   }

   public boolean checkInteractiveElement(Element e) {
      return this.checkInteractiveElement((SVGDocument)e.getOwnerDocument(), e);
   }

   public boolean checkInteractiveElement(SVGDocument doc, Element e) {
      String tag = e.getLocalName();
      if ("a".equals(tag)) {
         return true;
      } else if ("title".equals(tag)) {
         return e.getParentNode() != doc.getRootElement();
      } else if ("desc".equals(tag)) {
         return e.getParentNode() != doc.getRootElement();
      } else if ("cursor".equals(tag)) {
         return true;
      } else if (e.getAttribute("cursor").length() > 0) {
         return true;
      } else {
         String svg_ns = "http://www.w3.org/2000/svg";

         for(Node n = e.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == 1) {
               Element child = (Element)n;
               if ("http://www.w3.org/2000/svg".equals(child.getNamespaceURI()) && this.checkInteractiveElement(child)) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   public void setAnimationLimitingNone() {
      this.animationLimitingMode = 0;
      if (this.animationEngine != null) {
         this.setAnimationLimitingMode();
      }

   }

   public void setAnimationLimitingCPU(float pc) {
      this.animationLimitingMode = 1;
      this.animationLimitingAmount = pc;
      if (this.animationEngine != null) {
         this.setAnimationLimitingMode();
      }

   }

   public void setAnimationLimitingFPS(float fps) {
      this.animationLimitingMode = 2;
      this.animationLimitingAmount = fps;
      if (this.animationEngine != null) {
         this.setAnimationLimitingMode();
      }

   }

   protected void setAnimationLimitingMode() {
      switch (this.animationLimitingMode) {
         case 0:
            this.animationEngine.setAnimationLimitingNone();
            break;
         case 1:
            this.animationEngine.setAnimationLimitingCPU(this.animationLimitingAmount);
            break;
         case 2:
            this.animationEngine.setAnimationLimitingFPS(this.animationLimitingAmount);
      }

   }

   public void registerSVGBridges() {
      UserAgent ua = this.getUserAgent();
      List ext = this.getBridgeExtensions(this.document);
      Iterator var3 = ext.iterator();

      while(var3.hasNext()) {
         Object anExt = var3.next();
         BridgeExtension be = (BridgeExtension)anExt;
         be.registerTags(this);
         ua.registerExtension(be);
      }

   }

   public List getBridgeExtensions(Document doc) {
      Element root = ((SVGOMDocument)doc).getRootElement();
      String ver = root.getAttributeNS((String)null, "version");
      Object svgBE;
      if (ver.length() != 0 && !ver.equals("1.0") && !ver.equals("1.1")) {
         svgBE = new SVG12BridgeExtension();
      } else {
         svgBE = new SVGBridgeExtension();
      }

      float priority = ((BridgeExtension)svgBE).getPriority();
      this.extensions = new LinkedList(getGlobalBridgeExtensions());
      ListIterator li = this.extensions.listIterator();

      while(true) {
         if (!li.hasNext()) {
            li.add(svgBE);
            break;
         }

         BridgeExtension lbe = (BridgeExtension)li.next();
         if (lbe.getPriority() > priority) {
            li.previous();
            li.add(svgBE);
            break;
         }
      }

      return this.extensions;
   }

   public static synchronized List getGlobalBridgeExtensions() {
      if (globalExtensions != null) {
         return globalExtensions;
      } else {
         globalExtensions = new LinkedList();
         Iterator iter = Service.providers(BridgeExtension.class);

         while(true) {
            label26:
            while(iter.hasNext()) {
               BridgeExtension be = (BridgeExtension)iter.next();
               float priority = be.getPriority();
               ListIterator li = globalExtensions.listIterator();

               BridgeExtension lbe;
               do {
                  if (!li.hasNext()) {
                     li.add(be);
                     continue label26;
                  }

                  lbe = (BridgeExtension)li.next();
               } while(!(lbe.getPriority() > priority));

               li.previous();
               li.add(be);
            }

            return globalExtensions;
         }
      }
   }

   public static class CSSEngineUserAgentWrapper implements CSSEngineUserAgent {
      UserAgent ua;

      CSSEngineUserAgentWrapper(UserAgent ua) {
         this.ua = ua;
      }

      public void displayError(Exception ex) {
         this.ua.displayError(ex);
      }

      public void displayMessage(String message) {
         this.ua.displayMessage(message);
      }
   }

   protected class AnimatedAttrListener implements AnimatedAttributeListener {
      public AnimatedAttrListener() {
      }

      public void animatedAttributeChanged(Element e, AnimatedLiveAttributeValue alav) {
         BridgeUpdateHandler h = BridgeContext.getBridgeUpdateHandler(e);
         if (h != null) {
            try {
               h.handleAnimatedAttributeChanged(alav);
            } catch (Exception var5) {
               BridgeContext.this.userAgent.displayError(var5);
            }
         }

      }

      public void otherAnimationChanged(Element e, String type) {
         BridgeUpdateHandler h = BridgeContext.getBridgeUpdateHandler(e);
         if (h != null) {
            try {
               h.handleOtherAnimationChanged(type);
            } catch (Exception var5) {
               BridgeContext.this.userAgent.displayError(var5);
            }
         }

      }
   }

   protected class CSSPropertiesChangedListener implements CSSEngineListener {
      public CSSPropertiesChangedListener() {
      }

      public void propertiesChanged(CSSEngineEvent evt) {
         Element elem = evt.getElement();
         SVGContext ctx = BridgeContext.getSVGContext(elem);
         if (ctx == null) {
            GraphicsNode pgn = BridgeContext.this.getGraphicsNode(elem.getParentNode());
            if (pgn == null || !(pgn instanceof CompositeGraphicsNode)) {
               return;
            }

            CompositeGraphicsNode parent = (CompositeGraphicsNode)pgn;
            int[] properties = evt.getProperties();
            int[] var7 = properties;
            int var8 = properties.length;

            for(int var9 = 0; var9 < var8; ++var9) {
               int property = var7[var9];
               if (property == 12) {
                  if (CSSUtilities.convertDisplay(elem)) {
                     GVTBuilder builder = BridgeContext.this.getGVTBuilder();
                     GraphicsNode childNode = builder.build(BridgeContext.this, (Element)elem);
                     if (childNode != null) {
                        int idx = -1;

                        for(Node ps = elem.getPreviousSibling(); ps != null; ps = ps.getPreviousSibling()) {
                           if (ps.getNodeType() == 1) {
                              Element pse = (Element)ps;
                              GraphicsNode gn = BridgeContext.this.getGraphicsNode(pse);
                              if (gn != null) {
                                 idx = parent.indexOf(gn);
                                 if (idx != -1) {
                                    break;
                                 }
                              }
                           }
                        }

                        ++idx;
                        parent.add(idx, childNode);
                     }
                  }
                  break;
               }
            }
         }

         if (ctx != null && ctx instanceof BridgeUpdateHandler) {
            ((BridgeUpdateHandler)ctx).handleCSSEngineEvent(evt);
         }

      }
   }

   protected class DOMCharacterDataModifiedEventListener implements EventListener {
      public DOMCharacterDataModifiedEventListener() {
      }

      public void handleEvent(Event evt) {
         Node node;
         for(node = (Node)evt.getTarget(); node != null && !(node instanceof SVGOMElement); node = (Node)((AbstractNode)node).getParentNodeEventTarget()) {
         }

         BridgeUpdateHandler h = BridgeContext.getBridgeUpdateHandler(node);
         if (h != null) {
            try {
               h.handleDOMCharacterDataModified((MutationEvent)evt);
            } catch (Exception var5) {
               BridgeContext.this.userAgent.displayError(var5);
            }
         }

      }
   }

   protected class DOMNodeRemovedEventListener implements EventListener {
      public DOMNodeRemovedEventListener() {
      }

      public void handleEvent(Event evt) {
         Node node = (Node)evt.getTarget();
         BridgeUpdateHandler h = BridgeContext.getBridgeUpdateHandler(node);
         if (h != null) {
            try {
               h.handleDOMNodeRemovedEvent((MutationEvent)evt);
            } catch (Exception var5) {
               BridgeContext.this.userAgent.displayError(var5);
            }
         }

      }
   }

   protected class DOMNodeInsertedEventListener implements EventListener {
      public DOMNodeInsertedEventListener() {
      }

      public void handleEvent(Event evt) {
         MutationEvent me = (MutationEvent)evt;
         BridgeUpdateHandler h = BridgeContext.getBridgeUpdateHandler(me.getRelatedNode());
         if (h != null) {
            try {
               h.handleDOMNodeInsertedEvent(me);
            } catch (InterruptedBridgeException var5) {
            } catch (Exception var6) {
               BridgeContext.this.userAgent.displayError(var6);
            }
         }

      }
   }

   protected class DOMMouseOverEventListener implements EventListener {
      public DOMMouseOverEventListener() {
      }

      public void handleEvent(Event evt) {
         Element target = (Element)evt.getTarget();
         Cursor cursor = CSSUtilities.convertCursor(target, BridgeContext.this);
         if (cursor != null) {
            BridgeContext.this.userAgent.setSVGCursor(cursor);
         }

      }
   }

   protected class DOMMouseOutEventListener implements EventListener {
      public DOMMouseOutEventListener() {
      }

      public void handleEvent(Event evt) {
         MouseEvent me = (MouseEvent)evt;
         Element newTarget = (Element)me.getRelatedTarget();
         Cursor cursor = CursorManager.DEFAULT_CURSOR;
         if (newTarget != null) {
            cursor = CSSUtilities.convertCursor(newTarget, BridgeContext.this);
         }

         if (cursor == null) {
            cursor = CursorManager.DEFAULT_CURSOR;
         }

         BridgeContext.this.userAgent.setSVGCursor(cursor);
      }
   }

   protected class DOMAttrModifiedEventListener implements EventListener {
      public DOMAttrModifiedEventListener() {
      }

      public void handleEvent(Event evt) {
         Node node = (Node)evt.getTarget();
         BridgeUpdateHandler h = BridgeContext.getBridgeUpdateHandler(node);
         if (h != null) {
            try {
               h.handleDOMAttrModifiedEvent((MutationEvent)evt);
            } catch (Exception var5) {
               BridgeContext.this.userAgent.displayError(var5);
            }
         }

      }
   }

   protected static class EventListenerMememto {
      public SoftReference target;
      public SoftReference listener;
      public boolean useCapture;
      public String namespaceURI;
      public String eventType;
      public boolean namespaced;

      public EventListenerMememto(EventTarget t, String s, EventListener l, boolean b, BridgeContext ctx) {
         Set set = ctx.eventListenerSet;
         this.target = new SoftReferenceMememto(t, this, set);
         this.listener = new SoftReferenceMememto(l, this, set);
         this.eventType = s;
         this.useCapture = b;
      }

      public EventListenerMememto(EventTarget t, String n, String s, EventListener l, boolean b, BridgeContext ctx) {
         this(t, s, l, b, ctx);
         this.namespaceURI = n;
         this.namespaced = true;
      }

      public EventListener getListener() {
         return (EventListener)this.listener.get();
      }

      public NodeEventTarget getTarget() {
         return (NodeEventTarget)this.target.get();
      }

      public boolean getUseCapture() {
         return this.useCapture;
      }

      public String getNamespaceURI() {
         return this.namespaceURI;
      }

      public String getEventType() {
         return this.eventType;
      }

      public boolean getNamespaced() {
         return this.namespaced;
      }
   }

   public static class SoftReferenceMememto extends CleanerThread.SoftReferenceCleared {
      Object mememto;
      Set set;

      SoftReferenceMememto(Object ref, Object mememto, Set set) {
         super(ref);
         this.mememto = mememto;
         this.set = set;
      }

      public void cleared() {
         synchronized(this.set) {
            this.set.remove(this.mememto);
            this.mememto = null;
            this.set = null;
         }
      }
   }
}
