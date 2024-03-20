package org.apache.batik.anim.dom;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.MissingResourceException;
import org.apache.batik.css.engine.CSSNavigableDocument;
import org.apache.batik.css.engine.CSSNavigableDocumentListener;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.dom.AbstractStylableDocument;
import org.apache.batik.dom.GenericAttr;
import org.apache.batik.dom.GenericAttrNS;
import org.apache.batik.dom.GenericCDATASection;
import org.apache.batik.dom.GenericComment;
import org.apache.batik.dom.GenericDocumentFragment;
import org.apache.batik.dom.GenericElement;
import org.apache.batik.dom.GenericEntityReference;
import org.apache.batik.dom.GenericProcessingInstruction;
import org.apache.batik.dom.GenericText;
import org.apache.batik.dom.StyleSheetFactory;
import org.apache.batik.dom.events.EventSupport;
import org.apache.batik.dom.svg.IdContainer;
import org.apache.batik.dom.svg.SVGContext;
import org.apache.batik.dom.util.XMLSupport;
import org.apache.batik.i18n.LocalizableSupport;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.MutationEvent;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGLangSpace;
import org.w3c.dom.svg.SVGSVGElement;

public class SVGOMDocument extends AbstractStylableDocument implements SVGDocument, SVGConstants, CSSNavigableDocument, IdContainer {
   protected static final String RESOURCES = "org.apache.batik.dom.svg.resources.Messages";
   protected transient LocalizableSupport localizableSupport = new LocalizableSupport("org.apache.batik.dom.svg.resources.Messages", this.getClass().getClassLoader());
   protected String referrer = "";
   protected ParsedURL url;
   protected transient boolean readonly;
   protected boolean isSVG12;
   protected HashMap cssNavigableDocumentListeners = new HashMap();
   protected AnimatedAttributeListener mainAnimatedAttributeListener = new AnimAttrListener();
   protected LinkedList animatedAttributeListeners = new LinkedList();
   protected transient SVGContext svgContext;

   protected SVGOMDocument() {
   }

   public SVGOMDocument(DocumentType dt, DOMImplementation impl) {
      super(dt, impl);
   }

   public void setLocale(Locale l) {
      super.setLocale(l);
      this.localizableSupport.setLocale(l);
   }

   public String formatMessage(String key, Object[] args) throws MissingResourceException {
      try {
         return super.formatMessage(key, args);
      } catch (MissingResourceException var4) {
         return this.localizableSupport.formatMessage(key, args);
      }
   }

   public String getTitle() {
      StringBuffer sb = new StringBuffer();
      boolean preserve = false;

      label35:
      for(Node n = this.getDocumentElement().getFirstChild(); n != null; n = n.getNextSibling()) {
         String ns = n.getNamespaceURI();
         if (ns != null && ns.equals("http://www.w3.org/2000/svg") && n.getLocalName().equals("title")) {
            preserve = ((SVGLangSpace)n).getXMLspace().equals("preserve");
            n = n.getFirstChild();

            while(true) {
               if (n == null) {
                  break label35;
               }

               if (n.getNodeType() == 3) {
                  sb.append(n.getNodeValue());
               }

               n = n.getNextSibling();
            }
         }
      }

      String s = sb.toString();
      return preserve ? XMLSupport.preserveXMLSpace(s) : XMLSupport.defaultXMLSpace(s);
   }

   public String getReferrer() {
      return this.referrer;
   }

   public void setReferrer(String s) {
      this.referrer = s;
   }

   public String getDomain() {
      return this.url == null ? null : this.url.getHost();
   }

   public SVGSVGElement getRootElement() {
      return (SVGSVGElement)this.getDocumentElement();
   }

   public String getURL() {
      return this.documentURI;
   }

   public URL getURLObject() {
      try {
         return new URL(this.documentURI);
      } catch (MalformedURLException var2) {
         return null;
      }
   }

   public ParsedURL getParsedURL() {
      return this.url;
   }

   public void setURLObject(URL url) {
      this.setParsedURL(new ParsedURL(url));
   }

   public void setParsedURL(ParsedURL url) {
      this.url = url;
      this.documentURI = url == null ? null : url.toString();
   }

   public void setDocumentURI(String uri) {
      this.documentURI = uri;
      this.url = uri == null ? null : new ParsedURL(uri);
   }

   public Element createElement(String tagName) throws DOMException {
      return new GenericElement(tagName.intern(), this);
   }

   public DocumentFragment createDocumentFragment() {
      return new GenericDocumentFragment(this);
   }

   public Text createTextNode(String data) {
      return new GenericText(data, this);
   }

   public Comment createComment(String data) {
      return new GenericComment(data, this);
   }

   public CDATASection createCDATASection(String data) throws DOMException {
      return new GenericCDATASection(data, this);
   }

   public ProcessingInstruction createProcessingInstruction(String target, String data) throws DOMException {
      return (ProcessingInstruction)("xml-stylesheet".equals(target) ? new SVGStyleSheetProcessingInstruction(data, this, (StyleSheetFactory)this.getImplementation()) : new GenericProcessingInstruction(target, data, this));
   }

   public Attr createAttribute(String name) throws DOMException {
      return new GenericAttr(name.intern(), this);
   }

   public EntityReference createEntityReference(String name) throws DOMException {
      return new GenericEntityReference(name, this);
   }

   public Attr createAttributeNS(String namespaceURI, String qualifiedName) throws DOMException {
      return (Attr)(namespaceURI == null ? new GenericAttr(qualifiedName.intern(), this) : new GenericAttrNS(namespaceURI.intern(), qualifiedName.intern(), this));
   }

   public Element createElementNS(String namespaceURI, String qualifiedName) throws DOMException {
      SVGDOMImplementation impl = (SVGDOMImplementation)this.implementation;
      return impl.createElementNS(this, namespaceURI, qualifiedName);
   }

   public boolean isSVG12() {
      return this.isSVG12;
   }

   public void setIsSVG12(boolean b) {
      this.isSVG12 = b;
   }

   public boolean isId(Attr node) {
      return node.getNamespaceURI() == null ? "id".equals(node.getNodeName()) : node.getNodeName().equals("xml:id");
   }

   public void setSVGContext(SVGContext ctx) {
      this.svgContext = ctx;
   }

   public SVGContext getSVGContext() {
      return this.svgContext;
   }

   public void addCSSNavigableDocumentListener(CSSNavigableDocumentListener l) {
      if (!this.cssNavigableDocumentListeners.containsKey(l)) {
         DOMNodeInsertedListenerWrapper nodeInserted = new DOMNodeInsertedListenerWrapper(l);
         DOMNodeRemovedListenerWrapper nodeRemoved = new DOMNodeRemovedListenerWrapper(l);
         DOMSubtreeModifiedListenerWrapper subtreeModified = new DOMSubtreeModifiedListenerWrapper(l);
         DOMCharacterDataModifiedListenerWrapper cdataModified = new DOMCharacterDataModifiedListenerWrapper(l);
         DOMAttrModifiedListenerWrapper attrModified = new DOMAttrModifiedListenerWrapper(l);
         this.cssNavigableDocumentListeners.put(l, new EventListener[]{nodeInserted, nodeRemoved, subtreeModified, cdataModified, attrModified});
         this.addEventListenerNS("http://www.w3.org/2001/xml-events", "DOMNodeInserted", nodeInserted, false, (Object)null);
         this.addEventListenerNS("http://www.w3.org/2001/xml-events", "DOMNodeRemoved", nodeRemoved, false, (Object)null);
         this.addEventListenerNS("http://www.w3.org/2001/xml-events", "DOMSubtreeModified", subtreeModified, false, (Object)null);
         this.addEventListenerNS("http://www.w3.org/2001/xml-events", "DOMCharacterDataModified", cdataModified, false, (Object)null);
         this.addEventListenerNS("http://www.w3.org/2001/xml-events", "DOMAttrModified", attrModified, false, (Object)null);
      }
   }

   public void removeCSSNavigableDocumentListener(CSSNavigableDocumentListener l) {
      EventListener[] listeners = (EventListener[])((EventListener[])this.cssNavigableDocumentListeners.get(l));
      if (listeners != null) {
         this.removeEventListenerNS("http://www.w3.org/2001/xml-events", "DOMNodeInserted", listeners[0], false);
         this.removeEventListenerNS("http://www.w3.org/2001/xml-events", "DOMNodeRemoved", listeners[1], false);
         this.removeEventListenerNS("http://www.w3.org/2001/xml-events", "DOMSubtreeModified", listeners[2], false);
         this.removeEventListenerNS("http://www.w3.org/2001/xml-events", "DOMCharacterDataModified", listeners[3], false);
         this.removeEventListenerNS("http://www.w3.org/2001/xml-events", "DOMAttrModified", listeners[4], false);
         this.cssNavigableDocumentListeners.remove(l);
      }
   }

   protected AnimatedAttributeListener getAnimatedAttributeListener() {
      return this.mainAnimatedAttributeListener;
   }

   protected void overrideStyleTextChanged(CSSStylableElement e, String text) {
      Iterator var3 = this.cssNavigableDocumentListeners.keySet().iterator();

      while(var3.hasNext()) {
         Object o = var3.next();
         CSSNavigableDocumentListener l = (CSSNavigableDocumentListener)o;
         l.overrideStyleTextChanged(e, text);
      }

   }

   protected void overrideStylePropertyRemoved(CSSStylableElement e, String name) {
      Iterator var3 = this.cssNavigableDocumentListeners.keySet().iterator();

      while(var3.hasNext()) {
         Object o = var3.next();
         CSSNavigableDocumentListener l = (CSSNavigableDocumentListener)o;
         l.overrideStylePropertyRemoved(e, name);
      }

   }

   protected void overrideStylePropertyChanged(CSSStylableElement e, String name, String value, String prio) {
      Iterator var5 = this.cssNavigableDocumentListeners.keySet().iterator();

      while(var5.hasNext()) {
         Object o = var5.next();
         CSSNavigableDocumentListener l = (CSSNavigableDocumentListener)o;
         l.overrideStylePropertyChanged(e, name, value, prio);
      }

   }

   public void addAnimatedAttributeListener(AnimatedAttributeListener aal) {
      if (!this.animatedAttributeListeners.contains(aal)) {
         this.animatedAttributeListeners.add(aal);
      }
   }

   public void removeAnimatedAttributeListener(AnimatedAttributeListener aal) {
      this.animatedAttributeListeners.remove(aal);
   }

   public CSSStyleDeclaration getOverrideStyle(Element elt, String pseudoElt) {
      return elt instanceof SVGStylableElement && pseudoElt == null ? ((SVGStylableElement)elt).getOverrideStyle() : null;
   }

   public boolean isReadonly() {
      return this.readonly;
   }

   public void setReadonly(boolean v) {
      this.readonly = v;
   }

   protected Node newNode() {
      return new SVGOMDocument();
   }

   protected Node copyInto(Node n) {
      super.copyInto(n);
      SVGOMDocument sd = (SVGOMDocument)n;
      sd.localizableSupport = new LocalizableSupport("org.apache.batik.dom.svg.resources.Messages", this.getClass().getClassLoader());
      sd.referrer = this.referrer;
      sd.url = this.url;
      return n;
   }

   protected Node deepCopyInto(Node n) {
      super.deepCopyInto(n);
      SVGOMDocument sd = (SVGOMDocument)n;
      sd.localizableSupport = new LocalizableSupport("org.apache.batik.dom.svg.resources.Messages", this.getClass().getClassLoader());
      sd.referrer = this.referrer;
      sd.url = this.url;
      return n;
   }

   private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
      s.defaultReadObject();
      this.localizableSupport = new LocalizableSupport("org.apache.batik.dom.svg.resources.Messages", this.getClass().getClassLoader());
   }

   protected class AnimAttrListener implements AnimatedAttributeListener {
      public void animatedAttributeChanged(Element e, AnimatedLiveAttributeValue alav) {
         Iterator var3 = SVGOMDocument.this.animatedAttributeListeners.iterator();

         while(var3.hasNext()) {
            Object animatedAttributeListener = var3.next();
            AnimatedAttributeListener aal = (AnimatedAttributeListener)animatedAttributeListener;
            aal.animatedAttributeChanged(e, alav);
         }

      }

      public void otherAnimationChanged(Element e, String type) {
         Iterator var3 = SVGOMDocument.this.animatedAttributeListeners.iterator();

         while(var3.hasNext()) {
            Object animatedAttributeListener = var3.next();
            AnimatedAttributeListener aal = (AnimatedAttributeListener)animatedAttributeListener;
            aal.otherAnimationChanged(e, type);
         }

      }
   }

   protected static class DOMAttrModifiedListenerWrapper implements EventListener {
      protected CSSNavigableDocumentListener listener;

      public DOMAttrModifiedListenerWrapper(CSSNavigableDocumentListener l) {
         this.listener = l;
      }

      public void handleEvent(Event evt) {
         evt = EventSupport.getUltimateOriginalEvent(evt);
         MutationEvent mevt = (MutationEvent)evt;
         this.listener.attrModified((Element)evt.getTarget(), (Attr)mevt.getRelatedNode(), mevt.getAttrChange(), mevt.getPrevValue(), mevt.getNewValue());
      }
   }

   protected static class DOMCharacterDataModifiedListenerWrapper implements EventListener {
      protected CSSNavigableDocumentListener listener;

      public DOMCharacterDataModifiedListenerWrapper(CSSNavigableDocumentListener l) {
         this.listener = l;
      }

      public void handleEvent(Event evt) {
         evt = EventSupport.getUltimateOriginalEvent(evt);
         this.listener.characterDataModified((Node)evt.getTarget());
      }
   }

   protected static class DOMSubtreeModifiedListenerWrapper implements EventListener {
      protected CSSNavigableDocumentListener listener;

      public DOMSubtreeModifiedListenerWrapper(CSSNavigableDocumentListener l) {
         this.listener = l;
      }

      public void handleEvent(Event evt) {
         evt = EventSupport.getUltimateOriginalEvent(evt);
         this.listener.subtreeModified((Node)evt.getTarget());
      }
   }

   protected static class DOMNodeRemovedListenerWrapper implements EventListener {
      protected CSSNavigableDocumentListener listener;

      public DOMNodeRemovedListenerWrapper(CSSNavigableDocumentListener l) {
         this.listener = l;
      }

      public void handleEvent(Event evt) {
         evt = EventSupport.getUltimateOriginalEvent(evt);
         this.listener.nodeToBeRemoved((Node)evt.getTarget());
      }
   }

   protected static class DOMNodeInsertedListenerWrapper implements EventListener {
      protected CSSNavigableDocumentListener listener;

      public DOMNodeInsertedListenerWrapper(CSSNavigableDocumentListener l) {
         this.listener = l;
      }

      public void handleEvent(Event evt) {
         evt = EventSupport.getUltimateOriginalEvent(evt);
         this.listener.nodeInserted((Node)evt.getTarget());
      }
   }
}
