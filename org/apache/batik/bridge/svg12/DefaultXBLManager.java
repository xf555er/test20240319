package org.apache.batik.bridge.svg12;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.event.EventListenerList;
import org.apache.batik.anim.dom.BindableElement;
import org.apache.batik.anim.dom.XBLEventSupport;
import org.apache.batik.anim.dom.XBLOMContentElement;
import org.apache.batik.anim.dom.XBLOMDefinitionElement;
import org.apache.batik.anim.dom.XBLOMImportElement;
import org.apache.batik.anim.dom.XBLOMShadowTreeElement;
import org.apache.batik.anim.dom.XBLOMTemplateElement;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.BridgeException;
import org.apache.batik.dom.AbstractAttrNS;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.AbstractNode;
import org.apache.batik.dom.events.NodeEventTarget;
import org.apache.batik.dom.xbl.NodeXBL;
import org.apache.batik.dom.xbl.ShadowTreeEvent;
import org.apache.batik.dom.xbl.XBLManager;
import org.apache.batik.dom.xbl.XBLManagerData;
import org.apache.batik.dom.xbl.XBLShadowTreeElement;
import org.apache.batik.util.DoublyIndexedTable;
import org.apache.batik.util.XBLConstants;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.DocumentEvent;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.events.MutationEvent;

public class DefaultXBLManager implements XBLManager, XBLConstants {
   protected boolean isProcessing;
   protected Document document;
   protected BridgeContext ctx;
   protected DoublyIndexedTable definitionLists = new DoublyIndexedTable();
   protected DoublyIndexedTable definitions = new DoublyIndexedTable();
   protected Map contentManagers = new HashMap();
   protected Map imports = new HashMap();
   protected DocInsertedListener docInsertedListener = new DocInsertedListener();
   protected DocRemovedListener docRemovedListener = new DocRemovedListener();
   protected DocSubtreeListener docSubtreeListener = new DocSubtreeListener();
   protected ImportAttrListener importAttrListener = new ImportAttrListener();
   protected RefAttrListener refAttrListener = new RefAttrListener();
   protected EventListenerList bindingListenerList = new EventListenerList();
   protected EventListenerList contentSelectionChangedListenerList = new EventListenerList();

   public DefaultXBLManager(Document doc, BridgeContext ctx) {
      this.document = doc;
      this.ctx = ctx;
      ImportRecord ir = new ImportRecord((Element)null, (Node)null);
      this.imports.put((Object)null, ir);
   }

   public void startProcessing() {
      if (!this.isProcessing) {
         NodeList nl = this.document.getElementsByTagNameNS("http://www.w3.org/2004/xbl", "definition");
         XBLOMDefinitionElement[] defs = new XBLOMDefinitionElement[nl.getLength()];

         for(int i = 0; i < defs.length; ++i) {
            defs[i] = (XBLOMDefinitionElement)nl.item(i);
         }

         nl = this.document.getElementsByTagNameNS("http://www.w3.org/2004/xbl", "import");
         Element[] imports = new Element[nl.getLength()];

         for(int i = 0; i < imports.length; ++i) {
            imports[i] = (Element)nl.item(i);
         }

         AbstractDocument doc = (AbstractDocument)this.document;
         XBLEventSupport es = (XBLEventSupport)doc.initializeEventSupport();
         es.addImplementationEventListenerNS("http://www.w3.org/2001/xml-events", "DOMNodeRemoved", this.docRemovedListener, true);
         es.addImplementationEventListenerNS("http://www.w3.org/2001/xml-events", "DOMNodeInserted", this.docInsertedListener, true);
         es.addImplementationEventListenerNS("http://www.w3.org/2001/xml-events", "DOMSubtreeModified", this.docSubtreeListener, true);
         XBLOMDefinitionElement[] var6 = defs;
         int var7 = defs.length;

         int var8;
         for(var8 = 0; var8 < var7; ++var8) {
            XBLOMDefinitionElement def = var6[var8];
            if (def.getAttributeNS((String)null, "ref").length() != 0) {
               this.addDefinitionRef(def);
            } else {
               String ns = def.getElementNamespaceURI();
               String ln = def.getElementLocalName();
               this.addDefinition(ns, ln, def, (Element)null);
            }
         }

         Element[] var14 = imports;
         var7 = imports.length;

         for(var8 = 0; var8 < var7; ++var8) {
            Element anImport = var14[var8];
            this.addImport(anImport);
         }

         this.isProcessing = true;
         this.bind(this.document.getDocumentElement());
      }
   }

   public void stopProcessing() {
      if (this.isProcessing) {
         this.isProcessing = false;
         AbstractDocument doc = (AbstractDocument)this.document;
         XBLEventSupport es = (XBLEventSupport)doc.initializeEventSupport();
         es.removeImplementationEventListenerNS("http://www.w3.org/2001/xml-events", "DOMNodeRemoved", this.docRemovedListener, true);
         es.removeImplementationEventListenerNS("http://www.w3.org/2001/xml-events", "DOMNodeInserted", this.docInsertedListener, true);
         es.removeImplementationEventListenerNS("http://www.w3.org/2001/xml-events", "DOMSubtreeModified", this.docSubtreeListener, true);
         int nSlots = this.imports.values().size();
         ImportRecord[] irs = new ImportRecord[nSlots];
         this.imports.values().toArray(irs);
         ImportRecord[] var5 = irs;
         int var6 = irs.length;

         int var7;
         for(var7 = 0; var7 < var6; ++var7) {
            ImportRecord ir = var5[var7];
            if (ir.importElement.getLocalName().equals("definition")) {
               this.removeDefinitionRef(ir.importElement);
            } else {
               this.removeImport(ir.importElement);
            }
         }

         Object[] defRecs = this.definitions.getValuesArray();
         this.definitions.clear();
         Object[] var13 = defRecs;
         var7 = defRecs.length;

         for(int var14 = 0; var14 < var7; ++var14) {
            Object defRec1 = var13[var14];
            DefinitionRecord defRec = (DefinitionRecord)defRec1;
            TreeSet defs = (TreeSet)this.definitionLists.get(defRec.namespaceURI, defRec.localName);
            if (defs != null) {
               while(!defs.isEmpty()) {
                  defRec = (DefinitionRecord)defs.first();
                  defs.remove(defRec);
                  this.removeDefinition(defRec);
               }

               this.definitionLists.put(defRec.namespaceURI, defRec.localName, (Object)null);
            }
         }

         this.definitionLists = new DoublyIndexedTable();
         this.contentManagers.clear();
      }
   }

   public boolean isProcessing() {
      return this.isProcessing;
   }

   protected void addDefinitionRef(Element defRef) {
      String ref = defRef.getAttributeNS((String)null, "ref");
      Element e = this.ctx.getReferencedElement(defRef, ref);
      if ("http://www.w3.org/2004/xbl".equals(e.getNamespaceURI()) && "definition".equals(e.getLocalName())) {
         ImportRecord ir = new ImportRecord(defRef, e);
         this.imports.put(defRef, ir);
         NodeEventTarget et = (NodeEventTarget)defRef;
         et.addEventListenerNS("http://www.w3.org/2001/xml-events", "DOMAttrModified", this.refAttrListener, false, (Object)null);
         XBLOMDefinitionElement d = (XBLOMDefinitionElement)defRef;
         String ns = d.getElementNamespaceURI();
         String ln = d.getElementLocalName();
         this.addDefinition(ns, ln, (XBLOMDefinitionElement)e, defRef);
      } else {
         throw new BridgeException(this.ctx, defRef, "uri.badTarget", new Object[]{ref});
      }
   }

   protected void removeDefinitionRef(Element defRef) {
      ImportRecord ir = (ImportRecord)this.imports.get(defRef);
      NodeEventTarget et = (NodeEventTarget)defRef;
      et.removeEventListenerNS("http://www.w3.org/2001/xml-events", "DOMAttrModified", this.refAttrListener, false);
      DefinitionRecord defRec = (DefinitionRecord)this.definitions.get(ir.node, defRef);
      this.removeDefinition(defRec);
      this.imports.remove(defRef);
   }

   protected void addImport(Element imp) {
      String bindings = imp.getAttributeNS((String)null, "bindings");
      Node n = this.ctx.getReferencedNode(imp, bindings);
      if (n.getNodeType() != 1 || "http://www.w3.org/2004/xbl".equals(n.getNamespaceURI()) && "xbl".equals(n.getLocalName())) {
         ImportRecord ir = new ImportRecord(imp, n);
         this.imports.put(imp, ir);
         NodeEventTarget et = (NodeEventTarget)imp;
         et.addEventListenerNS("http://www.w3.org/2001/xml-events", "DOMAttrModified", this.importAttrListener, false, (Object)null);
         et = (NodeEventTarget)n;
         et.addEventListenerNS("http://www.w3.org/2001/xml-events", "DOMNodeInserted", ir.importInsertedListener, false, (Object)null);
         et.addEventListenerNS("http://www.w3.org/2001/xml-events", "DOMNodeRemoved", ir.importRemovedListener, false, (Object)null);
         et.addEventListenerNS("http://www.w3.org/2001/xml-events", "DOMSubtreeModified", ir.importSubtreeListener, false, (Object)null);
         this.addImportedDefinitions(imp, n);
      } else {
         throw new BridgeException(this.ctx, imp, "uri.badTarget", new Object[]{n});
      }
   }

   protected void addImportedDefinitions(Element imp, Node n) {
      if (n instanceof XBLOMDefinitionElement) {
         XBLOMDefinitionElement def = (XBLOMDefinitionElement)n;
         String ns = def.getElementNamespaceURI();
         String ln = def.getElementLocalName();
         this.addDefinition(ns, ln, def, imp);
      } else {
         for(n = n.getFirstChild(); n != null; n = n.getNextSibling()) {
            this.addImportedDefinitions(imp, n);
         }
      }

   }

   protected void removeImport(Element imp) {
      ImportRecord ir = (ImportRecord)this.imports.get(imp);
      NodeEventTarget et = (NodeEventTarget)ir.node;
      et.removeEventListenerNS("http://www.w3.org/2001/xml-events", "DOMNodeInserted", ir.importInsertedListener, false);
      et.removeEventListenerNS("http://www.w3.org/2001/xml-events", "DOMNodeRemoved", ir.importRemovedListener, false);
      et.removeEventListenerNS("http://www.w3.org/2001/xml-events", "DOMSubtreeModified", ir.importSubtreeListener, false);
      et = (NodeEventTarget)imp;
      et.removeEventListenerNS("http://www.w3.org/2001/xml-events", "DOMAttrModified", this.importAttrListener, false);
      Object[] defRecs = this.definitions.getValuesArray();
      Object[] var5 = defRecs;
      int var6 = defRecs.length;

      for(int var7 = 0; var7 < var6; ++var7) {
         Object defRec1 = var5[var7];
         DefinitionRecord defRec = (DefinitionRecord)defRec1;
         if (defRec.importElement == imp) {
            this.removeDefinition(defRec);
         }
      }

      this.imports.remove(imp);
   }

   protected void addDefinition(String namespaceURI, String localName, XBLOMDefinitionElement def, Element imp) {
      ImportRecord ir = (ImportRecord)this.imports.get(imp);
      DefinitionRecord oldDefRec = null;
      TreeSet defs = (TreeSet)this.definitionLists.get(namespaceURI, localName);
      if (defs == null) {
         defs = new TreeSet();
         this.definitionLists.put(namespaceURI, localName, defs);
      } else if (defs.size() > 0) {
         oldDefRec = (DefinitionRecord)defs.first();
      }

      XBLOMTemplateElement template = null;

      for(Node n = def.getFirstChild(); n != null; n = n.getNextSibling()) {
         if (n instanceof XBLOMTemplateElement) {
            template = (XBLOMTemplateElement)n;
            break;
         }
      }

      DefinitionRecord defRec = new DefinitionRecord(namespaceURI, localName, def, template, imp);
      defs.add(defRec);
      this.definitions.put(def, imp, defRec);
      this.addDefinitionElementListeners(def, ir);
      if (defs.first() == defRec) {
         if (oldDefRec != null) {
            XBLOMDefinitionElement oldDef = oldDefRec.definition;
            XBLOMTemplateElement oldTemplate = oldDefRec.template;
            if (oldTemplate != null) {
               this.removeTemplateElementListeners(oldTemplate, ir);
            }

            this.removeDefinitionElementListeners(oldDef, ir);
         }

         if (template != null) {
            this.addTemplateElementListeners(template, ir);
         }

         if (this.isProcessing) {
            this.rebind(namespaceURI, localName, this.document.getDocumentElement());
         }

      }
   }

   protected void addDefinitionElementListeners(XBLOMDefinitionElement def, ImportRecord ir) {
      XBLEventSupport es = (XBLEventSupport)def.initializeEventSupport();
      es.addImplementationEventListenerNS("http://www.w3.org/2001/xml-events", "DOMAttrModified", ir.defAttrListener, false);
      es.addImplementationEventListenerNS("http://www.w3.org/2001/xml-events", "DOMNodeInserted", ir.defNodeInsertedListener, false);
      es.addImplementationEventListenerNS("http://www.w3.org/2001/xml-events", "DOMNodeRemoved", ir.defNodeRemovedListener, false);
   }

   protected void addTemplateElementListeners(XBLOMTemplateElement template, ImportRecord ir) {
      XBLEventSupport es = (XBLEventSupport)template.initializeEventSupport();
      es.addImplementationEventListenerNS("http://www.w3.org/2001/xml-events", "DOMAttrModified", ir.templateMutationListener, false);
      es.addImplementationEventListenerNS("http://www.w3.org/2001/xml-events", "DOMNodeInserted", ir.templateMutationListener, false);
      es.addImplementationEventListenerNS("http://www.w3.org/2001/xml-events", "DOMNodeRemoved", ir.templateMutationListener, false);
      es.addImplementationEventListenerNS("http://www.w3.org/2001/xml-events", "DOMCharacterDataModified", ir.templateMutationListener, false);
   }

   protected void removeDefinition(DefinitionRecord defRec) {
      TreeSet defs = (TreeSet)this.definitionLists.get(defRec.namespaceURI, defRec.localName);
      if (defs != null) {
         Element imp = defRec.importElement;
         ImportRecord ir = (ImportRecord)this.imports.get(imp);
         DefinitionRecord activeDefRec = (DefinitionRecord)defs.first();
         defs.remove(defRec);
         this.definitions.remove(defRec.definition, imp);
         this.removeDefinitionElementListeners(defRec.definition, ir);
         if (defRec == activeDefRec) {
            if (defRec.template != null) {
               this.removeTemplateElementListeners(defRec.template, ir);
            }

            this.rebind(defRec.namespaceURI, defRec.localName, this.document.getDocumentElement());
         }
      }
   }

   protected void removeDefinitionElementListeners(XBLOMDefinitionElement def, ImportRecord ir) {
      XBLEventSupport es = (XBLEventSupport)def.initializeEventSupport();
      es.removeImplementationEventListenerNS("http://www.w3.org/2001/xml-events", "DOMAttrModified", ir.defAttrListener, false);
      es.removeImplementationEventListenerNS("http://www.w3.org/2001/xml-events", "DOMNodeInserted", ir.defNodeInsertedListener, false);
      es.removeImplementationEventListenerNS("http://www.w3.org/2001/xml-events", "DOMNodeRemoved", ir.defNodeRemovedListener, false);
   }

   protected void removeTemplateElementListeners(XBLOMTemplateElement template, ImportRecord ir) {
      XBLEventSupport es = (XBLEventSupport)template.initializeEventSupport();
      es.removeImplementationEventListenerNS("http://www.w3.org/2001/xml-events", "DOMAttrModified", ir.templateMutationListener, false);
      es.removeImplementationEventListenerNS("http://www.w3.org/2001/xml-events", "DOMNodeInserted", ir.templateMutationListener, false);
      es.removeImplementationEventListenerNS("http://www.w3.org/2001/xml-events", "DOMNodeRemoved", ir.templateMutationListener, false);
      es.removeImplementationEventListenerNS("http://www.w3.org/2001/xml-events", "DOMCharacterDataModified", ir.templateMutationListener, false);
   }

   protected DefinitionRecord getActiveDefinition(String namespaceURI, String localName) {
      TreeSet defs = (TreeSet)this.definitionLists.get(namespaceURI, localName);
      return defs != null && defs.size() != 0 ? (DefinitionRecord)defs.first() : null;
   }

   protected void unbind(Element e) {
      if (e instanceof BindableElement) {
         this.setActiveDefinition((BindableElement)e, (DefinitionRecord)null);
      } else {
         NodeList nl = this.getXblScopedChildNodes(e);

         for(int i = 0; i < nl.getLength(); ++i) {
            Node n = nl.item(i);
            if (n.getNodeType() == 1) {
               this.unbind((Element)n);
            }
         }
      }

   }

   protected void bind(Element e) {
      AbstractDocument doc = (AbstractDocument)e.getOwnerDocument();
      if (doc != this.document) {
         XBLManager xm = doc.getXBLManager();
         if (xm instanceof DefaultXBLManager) {
            ((DefaultXBLManager)xm).bind(e);
            return;
         }
      }

      if (e instanceof BindableElement) {
         DefinitionRecord defRec = this.getActiveDefinition(e.getNamespaceURI(), e.getLocalName());
         this.setActiveDefinition((BindableElement)e, defRec);
      } else {
         NodeList nl = this.getXblScopedChildNodes(e);

         for(int i = 0; i < nl.getLength(); ++i) {
            Node n = nl.item(i);
            if (n.getNodeType() == 1) {
               this.bind((Element)n);
            }
         }
      }

   }

   protected void rebind(String namespaceURI, String localName, Element e) {
      AbstractDocument doc = (AbstractDocument)e.getOwnerDocument();
      if (doc != this.document) {
         XBLManager xm = doc.getXBLManager();
         if (xm instanceof DefaultXBLManager) {
            ((DefaultXBLManager)xm).rebind(namespaceURI, localName, e);
            return;
         }
      }

      if (e instanceof BindableElement && namespaceURI.equals(e.getNamespaceURI()) && localName.equals(e.getLocalName())) {
         DefinitionRecord defRec = this.getActiveDefinition(e.getNamespaceURI(), e.getLocalName());
         this.setActiveDefinition((BindableElement)e, defRec);
      } else {
         NodeList nl = this.getXblScopedChildNodes(e);

         for(int i = 0; i < nl.getLength(); ++i) {
            Node n = nl.item(i);
            if (n.getNodeType() == 1) {
               this.rebind(namespaceURI, localName, (Element)n);
            }
         }
      }

   }

   protected void setActiveDefinition(BindableElement elt, DefinitionRecord defRec) {
      XBLRecord rec = this.getRecord(elt);
      rec.definitionElement = defRec == null ? null : defRec.definition;
      if (defRec != null && defRec.definition != null && defRec.template != null) {
         this.setXblShadowTree(elt, this.cloneTemplate(defRec.template));
      } else {
         this.setXblShadowTree(elt, (XBLOMShadowTreeElement)null);
      }

   }

   protected void setXblShadowTree(BindableElement elt, XBLOMShadowTreeElement newShadow) {
      XBLOMShadowTreeElement oldShadow = (XBLOMShadowTreeElement)this.getXblShadowTree(elt);
      if (oldShadow != null) {
         this.fireShadowTreeEvent(elt, "unbinding", oldShadow);
         ContentManager cm = this.getContentManager(oldShadow);
         if (cm != null) {
            cm.dispose();
         }

         elt.setShadowTree((XBLOMShadowTreeElement)null);
         XBLRecord rec = this.getRecord(oldShadow);
         rec.boundElement = null;
         oldShadow.removeEventListenerNS("http://www.w3.org/2001/xml-events", "DOMSubtreeModified", this.docSubtreeListener, false);
      }

      if (newShadow != null) {
         newShadow.addEventListenerNS("http://www.w3.org/2001/xml-events", "DOMSubtreeModified", this.docSubtreeListener, false, (Object)null);
         this.fireShadowTreeEvent(elt, "prebind", newShadow);
         elt.setShadowTree(newShadow);
         XBLRecord rec = this.getRecord(newShadow);
         rec.boundElement = elt;
         AbstractDocument doc = (AbstractDocument)elt.getOwnerDocument();
         XBLManager xm = doc.getXBLManager();
         ContentManager cm = new ContentManager(newShadow, xm);
         this.setContentManager(newShadow, cm);
      }

      this.invalidateChildNodes(elt);
      if (newShadow != null) {
         NodeList nl = this.getXblScopedChildNodes(elt);

         for(int i = 0; i < nl.getLength(); ++i) {
            Node n = nl.item(i);
            if (n.getNodeType() == 1) {
               this.bind((Element)n);
            }
         }

         this.dispatchBindingChangedEvent(elt, newShadow);
         this.fireShadowTreeEvent(elt, "bound", newShadow);
      } else {
         this.dispatchBindingChangedEvent(elt, newShadow);
      }

   }

   protected void fireShadowTreeEvent(BindableElement elt, String type, XBLShadowTreeElement e) {
      DocumentEvent de = (DocumentEvent)elt.getOwnerDocument();
      ShadowTreeEvent evt = (ShadowTreeEvent)de.createEvent("ShadowTreeEvent");
      evt.initShadowTreeEventNS("http://www.w3.org/2004/xbl", type, true, false, e);
      elt.dispatchEvent(evt);
   }

   protected XBLOMShadowTreeElement cloneTemplate(XBLOMTemplateElement template) {
      XBLOMShadowTreeElement clone = (XBLOMShadowTreeElement)template.getOwnerDocument().createElementNS("http://www.w3.org/2004/xbl", "shadowTree");
      NamedNodeMap attrs = template.getAttributes();

      for(int i = 0; i < attrs.getLength(); ++i) {
         Attr attr = (Attr)attrs.item(i);
         if (attr instanceof AbstractAttrNS) {
            clone.setAttributeNodeNS(attr);
         } else {
            clone.setAttributeNode(attr);
         }
      }

      for(Node n = template.getFirstChild(); n != null; n = n.getNextSibling()) {
         clone.appendChild(n.cloneNode(true));
      }

      return clone;
   }

   public Node getXblParentNode(Node n) {
      Node contentElement = this.getXblContentElement(n);
      Node parent = contentElement == null ? n.getParentNode() : contentElement.getParentNode();
      if (parent instanceof XBLOMContentElement) {
         parent = ((Node)parent).getParentNode();
      }

      if (parent instanceof XBLOMShadowTreeElement) {
         parent = this.getXblBoundElement((Node)parent);
      }

      return (Node)parent;
   }

   public NodeList getXblChildNodes(Node n) {
      XBLRecord rec = this.getRecord(n);
      if (rec.childNodes == null) {
         rec.childNodes = new XblChildNodes(rec);
      }

      return rec.childNodes;
   }

   public NodeList getXblScopedChildNodes(Node n) {
      XBLRecord rec = this.getRecord(n);
      if (rec.scopedChildNodes == null) {
         rec.scopedChildNodes = new XblScopedChildNodes(rec);
      }

      return rec.scopedChildNodes;
   }

   public Node getXblFirstChild(Node n) {
      NodeList nl = this.getXblChildNodes(n);
      return nl.item(0);
   }

   public Node getXblLastChild(Node n) {
      NodeList nl = this.getXblChildNodes(n);
      return nl.item(nl.getLength() - 1);
   }

   public Node getXblPreviousSibling(Node n) {
      Node p = this.getXblParentNode(n);
      if (p != null && this.getRecord(p).childNodes != null) {
         XBLRecord rec = this.getRecord(n);
         if (!rec.linksValid) {
            this.updateLinks(n);
         }

         return rec.previousSibling;
      } else {
         return n.getPreviousSibling();
      }
   }

   public Node getXblNextSibling(Node n) {
      Node p = this.getXblParentNode(n);
      if (p != null && this.getRecord(p).childNodes != null) {
         XBLRecord rec = this.getRecord(n);
         if (!rec.linksValid) {
            this.updateLinks(n);
         }

         return rec.nextSibling;
      } else {
         return n.getNextSibling();
      }
   }

   public Element getXblFirstElementChild(Node n) {
      for(n = this.getXblFirstChild(n); n != null && n.getNodeType() != 1; n = this.getXblNextSibling(n)) {
      }

      return (Element)n;
   }

   public Element getXblLastElementChild(Node n) {
      for(n = this.getXblLastChild(n); n != null && n.getNodeType() != 1; n = this.getXblPreviousSibling(n)) {
      }

      return (Element)n;
   }

   public Element getXblPreviousElementSibling(Node n) {
      do {
         n = this.getXblPreviousSibling(n);
      } while(n != null && n.getNodeType() != 1);

      return (Element)n;
   }

   public Element getXblNextElementSibling(Node n) {
      do {
         n = this.getXblNextSibling(n);
      } while(n != null && n.getNodeType() != 1);

      return (Element)n;
   }

   public Element getXblBoundElement(Node n) {
      for(; n != null && !(n instanceof XBLShadowTreeElement); n = ((Node)n).getParentNode()) {
         XBLOMContentElement content = this.getXblContentElement((Node)n);
         if (content != null) {
            n = content;
         }
      }

      if (n == null) {
         return null;
      } else {
         return this.getRecord((Node)n).boundElement;
      }
   }

   public Element getXblShadowTree(Node n) {
      if (n instanceof BindableElement) {
         BindableElement elt = (BindableElement)n;
         return elt.getShadowTree();
      } else {
         return null;
      }
   }

   public NodeList getXblDefinitions(Node n) {
      final String namespaceURI = n.getNamespaceURI();
      final String localName = n.getLocalName();
      return new NodeList() {
         public Node item(int i) {
            TreeSet defs = (TreeSet)DefaultXBLManager.this.definitionLists.get(namespaceURI, localName);
            if (defs != null && defs.size() != 0 && i == 0) {
               DefinitionRecord defRec = (DefinitionRecord)defs.first();
               return defRec.definition;
            } else {
               return null;
            }
         }

         public int getLength() {
            Set defs = (TreeSet)DefaultXBLManager.this.definitionLists.get(namespaceURI, localName);
            return defs != null && defs.size() != 0 ? 1 : 0;
         }
      };
   }

   protected XBLRecord getRecord(Node n) {
      XBLManagerData xmd = (XBLManagerData)n;
      XBLRecord rec = (XBLRecord)xmd.getManagerData();
      if (rec == null) {
         rec = new XBLRecord();
         rec.node = n;
         xmd.setManagerData(rec);
      }

      return rec;
   }

   protected void updateLinks(Node n) {
      XBLRecord rec = this.getRecord(n);
      rec.previousSibling = null;
      rec.nextSibling = null;
      rec.linksValid = true;
      Node p = this.getXblParentNode(n);
      if (p != null) {
         NodeList xcn = this.getXblChildNodes(p);
         if (xcn instanceof XblChildNodes) {
            ((XblChildNodes)xcn).update();
         }
      }

   }

   public XBLOMContentElement getXblContentElement(Node n) {
      return this.getRecord(n).contentElement;
   }

   public static int computeBubbleLimit(Node from, Node to) {
      ArrayList fromList = new ArrayList(10);

      ArrayList toList;
      for(toList = new ArrayList(10); from != null; from = ((NodeXBL)from).getXblParentNode()) {
         fromList.add(from);
      }

      while(to != null) {
         toList.add(to);
         to = ((NodeXBL)to).getXblParentNode();
      }

      int fromSize = fromList.size();
      int toSize = toList.size();

      for(int i = 0; i < fromSize && i < toSize; ++i) {
         Node n1 = (Node)fromList.get(fromSize - i - 1);
         Node n2 = (Node)toList.get(toSize - i - 1);
         if (n1 != n2) {
            for(Node prevBoundElement = ((NodeXBL)n1).getXblBoundElement(); i > 0 && prevBoundElement != fromList.get(fromSize - i - 1); --i) {
            }

            return fromSize - i - 1;
         }
      }

      return 1;
   }

   public ContentManager getContentManager(Node n) {
      Node b = this.getXblBoundElement(n);
      if (b != null) {
         Element s = this.getXblShadowTree(b);
         if (s != null) {
            Document doc = b.getOwnerDocument();
            ContentManager cm;
            if (doc != this.document) {
               DefaultXBLManager xm = (DefaultXBLManager)((AbstractDocument)doc).getXBLManager();
               cm = (ContentManager)xm.contentManagers.get(s);
            } else {
               cm = (ContentManager)this.contentManagers.get(s);
            }

            return cm;
         }
      }

      return null;
   }

   void setContentManager(Element shadow, ContentManager cm) {
      if (cm == null) {
         this.contentManagers.remove(shadow);
      } else {
         this.contentManagers.put(shadow, cm);
      }

   }

   public void invalidateChildNodes(Node n) {
      XBLRecord rec = this.getRecord(n);
      if (rec.childNodes != null) {
         rec.childNodes.invalidate();
      }

      if (rec.scopedChildNodes != null) {
         rec.scopedChildNodes.invalidate();
      }

   }

   public void addContentSelectionChangedListener(ContentSelectionChangedListener l) {
      this.contentSelectionChangedListenerList.add(ContentSelectionChangedListener.class, l);
   }

   public void removeContentSelectionChangedListener(ContentSelectionChangedListener l) {
      this.contentSelectionChangedListenerList.remove(ContentSelectionChangedListener.class, l);
   }

   protected Object[] getContentSelectionChangedListeners() {
      return this.contentSelectionChangedListenerList.getListenerList();
   }

   void shadowTreeSelectedContentChanged(Set deselected, Set selected) {
      Iterator i = deselected.iterator();

      Node n;
      while(i.hasNext()) {
         n = (Node)i.next();
         if (n.getNodeType() == 1) {
            this.unbind((Element)n);
         }
      }

      i = selected.iterator();

      while(i.hasNext()) {
         n = (Node)i.next();
         if (n.getNodeType() == 1) {
            this.bind((Element)n);
         }
      }

   }

   public void addBindingListener(BindingListener l) {
      this.bindingListenerList.add(BindingListener.class, l);
   }

   public void removeBindingListener(BindingListener l) {
      this.bindingListenerList.remove(BindingListener.class, l);
   }

   protected void dispatchBindingChangedEvent(Element bindableElement, Element shadowTree) {
      Object[] ls = this.bindingListenerList.getListenerList();

      for(int i = ls.length - 2; i >= 0; i -= 2) {
         BindingListener l = (BindingListener)ls[i + 1];
         l.bindingChanged(bindableElement, shadowTree);
      }

   }

   protected boolean isActiveDefinition(XBLOMDefinitionElement def, Element imp) {
      DefinitionRecord defRec = (DefinitionRecord)this.definitions.get(def, imp);
      if (defRec == null) {
         return false;
      } else {
         return defRec == this.getActiveDefinition(defRec.namespaceURI, defRec.localName);
      }
   }

   protected class XblScopedChildNodes extends XblChildNodes {
      public XblScopedChildNodes(XBLRecord rec) {
         super(rec);
      }

      protected void update() {
         this.size = 0;
         Node shadowTree = DefaultXBLManager.this.getXblShadowTree(this.record.node);

         for(Node n = shadowTree == null ? this.record.node.getFirstChild() : shadowTree.getFirstChild(); n != null; n = n.getNextSibling()) {
            this.collectXblScopedChildNodes(n);
         }

      }

      protected void collectXblScopedChildNodes(Node n) {
         boolean isChild = false;
         if (n.getNodeType() == 1) {
            if (!n.getNamespaceURI().equals("http://www.w3.org/2004/xbl")) {
               isChild = true;
            } else if (n instanceof XBLOMContentElement) {
               ContentManager cm = DefaultXBLManager.this.getContentManager(n);
               if (cm != null) {
                  NodeList selected = cm.getSelectedContent((XBLOMContentElement)n);

                  for(int i = 0; i < selected.getLength(); ++i) {
                     this.collectXblScopedChildNodes(selected.item(i));
                  }
               }
            }
         } else {
            isChild = true;
         }

         if (isChild) {
            this.nodes.add(n);
            ++this.size;
         }

      }
   }

   protected class XblChildNodes implements NodeList {
      protected XBLRecord record;
      protected List nodes;
      protected int size;

      public XblChildNodes(XBLRecord rec) {
         this.record = rec;
         this.nodes = new ArrayList();
         this.size = -1;
      }

      protected void update() {
         this.size = 0;
         Node shadowTree = DefaultXBLManager.this.getXblShadowTree(this.record.node);
         Node last = null;

         for(Node m = shadowTree == null ? this.record.node.getFirstChild() : shadowTree.getFirstChild(); m != null; m = m.getNextSibling()) {
            last = this.collectXblChildNodes(m, last);
         }

         if (last != null) {
            XBLRecord rec = DefaultXBLManager.this.getRecord(last);
            rec.nextSibling = null;
            rec.linksValid = true;
         }

      }

      protected Node collectXblChildNodes(Node n, Node prev) {
         boolean isChild = false;
         if (n.getNodeType() == 1) {
            if (!"http://www.w3.org/2004/xbl".equals(n.getNamespaceURI())) {
               isChild = true;
            } else if (n instanceof XBLOMContentElement) {
               ContentManager cm = DefaultXBLManager.this.getContentManager(n);
               if (cm != null) {
                  NodeList selected = cm.getSelectedContent((XBLOMContentElement)n);

                  for(int i = 0; i < selected.getLength(); ++i) {
                     prev = this.collectXblChildNodes(selected.item(i), prev);
                  }
               }
            }
         } else {
            isChild = true;
         }

         if (isChild) {
            this.nodes.add(n);
            ++this.size;
            XBLRecord rec;
            if (prev != null) {
               rec = DefaultXBLManager.this.getRecord(prev);
               rec.nextSibling = n;
               rec.linksValid = true;
            }

            rec = DefaultXBLManager.this.getRecord(n);
            rec.previousSibling = prev;
            rec.linksValid = true;
            prev = n;
         }

         return prev;
      }

      public void invalidate() {
         for(int i = 0; i < this.size; ++i) {
            XBLRecord rec = DefaultXBLManager.this.getRecord((Node)this.nodes.get(i));
            rec.previousSibling = null;
            rec.nextSibling = null;
            rec.linksValid = false;
         }

         this.nodes.clear();
         this.size = -1;
      }

      public Node getFirstNode() {
         if (this.size == -1) {
            this.update();
         }

         return this.size == 0 ? null : (Node)this.nodes.get(0);
      }

      public Node getLastNode() {
         if (this.size == -1) {
            this.update();
         }

         return this.size == 0 ? null : (Node)this.nodes.get(this.nodes.size() - 1);
      }

      public Node item(int index) {
         if (this.size == -1) {
            this.update();
         }

         return index >= 0 && index < this.size ? (Node)this.nodes.get(index) : null;
      }

      public int getLength() {
         if (this.size == -1) {
            this.update();
         }

         return this.size;
      }
   }

   protected class XBLRecord {
      public Node node;
      public XblChildNodes childNodes;
      public XblScopedChildNodes scopedChildNodes;
      public XBLOMContentElement contentElement;
      public XBLOMDefinitionElement definitionElement;
      public BindableElement boundElement;
      public boolean linksValid;
      public Node nextSibling;
      public Node previousSibling;
   }

   protected class RefAttrListener implements EventListener {
      public void handleEvent(Event evt) {
         EventTarget target = evt.getTarget();
         if (target == evt.getCurrentTarget()) {
            MutationEvent mevt = (MutationEvent)evt;
            if (mevt.getAttrName().equals("ref")) {
               Element defRef = (Element)target;
               DefaultXBLManager.this.removeDefinitionRef(defRef);
               if (mevt.getNewValue().length() == 0) {
                  XBLOMDefinitionElement def = (XBLOMDefinitionElement)defRef;
                  String ns = def.getElementNamespaceURI();
                  String ln = def.getElementLocalName();
                  DefaultXBLManager.this.addDefinition(ns, ln, (XBLOMDefinitionElement)defRef, (Element)null);
               } else {
                  DefaultXBLManager.this.addDefinitionRef(defRef);
               }
            }

         }
      }
   }

   protected class ImportAttrListener implements EventListener {
      public void handleEvent(Event evt) {
         EventTarget target = evt.getTarget();
         if (target == evt.getCurrentTarget()) {
            MutationEvent mevt = (MutationEvent)evt;
            if (mevt.getAttrName().equals("bindings")) {
               Element imp = (Element)target;
               DefaultXBLManager.this.removeImport(imp);
               DefaultXBLManager.this.addImport(imp);
            }

         }
      }
   }

   protected class DefNodeRemovedListener implements EventListener {
      protected Element importElement;

      public DefNodeRemovedListener(Element imp) {
         this.importElement = imp;
      }

      public void handleEvent(Event evt) {
         MutationEvent mevt = (MutationEvent)evt;
         Node parent = mevt.getRelatedNode();
         if (parent instanceof XBLOMDefinitionElement) {
            EventTarget target = evt.getTarget();
            if (target instanceof XBLOMTemplateElement) {
               XBLOMTemplateElement template = (XBLOMTemplateElement)target;
               DefinitionRecord defRec = (DefinitionRecord)DefaultXBLManager.this.definitions.get(parent, this.importElement);
               if (defRec != null && defRec.template == template) {
                  ImportRecord ir = (ImportRecord)DefaultXBLManager.this.imports.get(this.importElement);
                  DefaultXBLManager.this.removeTemplateElementListeners(template, ir);
                  defRec.template = null;

                  for(Node n = template.getNextSibling(); n != null; n = n.getNextSibling()) {
                     if (n instanceof XBLOMTemplateElement) {
                        defRec.template = (XBLOMTemplateElement)n;
                        break;
                     }
                  }

                  DefaultXBLManager.this.addTemplateElementListeners(defRec.template, ir);
                  DefaultXBLManager.this.rebind(defRec.namespaceURI, defRec.localName, DefaultXBLManager.this.document.getDocumentElement());
               }
            }
         }
      }
   }

   protected class DefNodeInsertedListener implements EventListener {
      protected Element importElement;

      public DefNodeInsertedListener(Element imp) {
         this.importElement = imp;
      }

      public void handleEvent(Event evt) {
         MutationEvent mevt = (MutationEvent)evt;
         Node parent = mevt.getRelatedNode();
         if (parent instanceof XBLOMDefinitionElement) {
            EventTarget target = evt.getTarget();
            if (target instanceof XBLOMTemplateElement) {
               XBLOMTemplateElement template = (XBLOMTemplateElement)target;
               DefinitionRecord defRec = (DefinitionRecord)DefaultXBLManager.this.definitions.get(parent, this.importElement);
               if (defRec != null) {
                  ImportRecord ir = (ImportRecord)DefaultXBLManager.this.imports.get(this.importElement);
                  if (defRec.template != null) {
                     for(Node n = parent.getFirstChild(); n != null; n = n.getNextSibling()) {
                        if (n == template) {
                           DefaultXBLManager.this.removeTemplateElementListeners(defRec.template, ir);
                           defRec.template = template;
                           break;
                        }

                        if (n == defRec.template) {
                           return;
                        }
                     }
                  } else {
                     defRec.template = template;
                  }

                  DefaultXBLManager.this.addTemplateElementListeners(template, ir);
                  DefaultXBLManager.this.rebind(defRec.namespaceURI, defRec.localName, DefaultXBLManager.this.document.getDocumentElement());
               }
            }
         }
      }
   }

   protected class DefAttrListener implements EventListener {
      protected Element importElement;

      public DefAttrListener(Element imp) {
         this.importElement = imp;
      }

      public void handleEvent(Event evt) {
         EventTarget target = evt.getTarget();
         if (target instanceof XBLOMDefinitionElement) {
            XBLOMDefinitionElement def = (XBLOMDefinitionElement)target;
            if (DefaultXBLManager.this.isActiveDefinition(def, this.importElement)) {
               MutationEvent mevt = (MutationEvent)evt;
               String attrName = mevt.getAttrName();
               DefinitionRecord defRec;
               if (attrName.equals("element")) {
                  defRec = (DefinitionRecord)DefaultXBLManager.this.definitions.get(def, this.importElement);
                  DefaultXBLManager.this.removeDefinition(defRec);
                  DefaultXBLManager.this.addDefinition(def.getElementNamespaceURI(), def.getElementLocalName(), def, this.importElement);
               } else if (attrName.equals("ref") && mevt.getNewValue().length() != 0) {
                  defRec = (DefinitionRecord)DefaultXBLManager.this.definitions.get(def, this.importElement);
                  DefaultXBLManager.this.removeDefinition(defRec);
                  DefaultXBLManager.this.addDefinitionRef(def);
               }

            }
         }
      }
   }

   protected class TemplateMutationListener implements EventListener {
      protected Element importElement;

      public TemplateMutationListener(Element imp) {
         this.importElement = imp;
      }

      public void handleEvent(Event evt) {
         Node n;
         for(n = (Node)evt.getTarget(); n != null && !(n instanceof XBLOMDefinitionElement); n = n.getParentNode()) {
         }

         DefinitionRecord defRec = (DefinitionRecord)DefaultXBLManager.this.definitions.get(n, this.importElement);
         if (defRec != null) {
            DefaultXBLManager.this.rebind(defRec.namespaceURI, defRec.localName, DefaultXBLManager.this.document.getDocumentElement());
         }
      }
   }

   protected class DocSubtreeListener implements EventListener {
      public void handleEvent(Event evt) {
         Object[] defs = DefaultXBLManager.this.docRemovedListener.defsToBeRemoved.toArray();
         DefaultXBLManager.this.docRemovedListener.defsToBeRemoved.clear();
         Object[] imps = defs;
         int var4 = defs.length;

         int var5;
         for(var5 = 0; var5 < var4; ++var5) {
            Object def1 = imps[var5];
            XBLOMDefinitionElement def = (XBLOMDefinitionElement)def1;
            if (def.getAttributeNS((String)null, "ref").length() == 0) {
               DefinitionRecord defRec = (DefinitionRecord)DefaultXBLManager.this.definitions.get(def, (Object)null);
               DefaultXBLManager.this.removeDefinition(defRec);
            } else {
               DefaultXBLManager.this.removeDefinitionRef(def);
            }
         }

         imps = DefaultXBLManager.this.docRemovedListener.importsToBeRemoved.toArray();
         DefaultXBLManager.this.docRemovedListener.importsToBeRemoved.clear();
         Object[] nodes = imps;
         var5 = imps.length;

         int var11;
         for(var11 = 0; var11 < var5; ++var11) {
            Object imp = nodes[var11];
            DefaultXBLManager.this.removeImport((Element)imp);
         }

         nodes = DefaultXBLManager.this.docRemovedListener.nodesToBeInvalidated.toArray();
         DefaultXBLManager.this.docRemovedListener.nodesToBeInvalidated.clear();
         Object[] var10 = nodes;
         var11 = nodes.length;

         for(int var13 = 0; var13 < var11; ++var13) {
            Object node = var10[var13];
            DefaultXBLManager.this.invalidateChildNodes((Node)node);
         }

      }
   }

   protected class DocRemovedListener implements EventListener {
      protected LinkedList defsToBeRemoved = new LinkedList();
      protected LinkedList importsToBeRemoved = new LinkedList();
      protected LinkedList nodesToBeInvalidated = new LinkedList();

      public void handleEvent(Event evt) {
         EventTarget target = evt.getTarget();
         if (target instanceof XBLOMDefinitionElement) {
            if (DefaultXBLManager.this.getXblBoundElement((Node)target) == null) {
               this.defsToBeRemoved.add(target);
            }
         } else if (target instanceof XBLOMImportElement && DefaultXBLManager.this.getXblBoundElement((Node)target) == null) {
            this.importsToBeRemoved.add(target);
         }

         Node parent = DefaultXBLManager.this.getXblParentNode((Node)target);
         if (parent != null) {
            this.nodesToBeInvalidated.add(parent);
         }

      }
   }

   protected class DocInsertedListener implements EventListener {
      public void handleEvent(Event evt) {
         EventTarget target = evt.getTarget();
         if (target instanceof XBLOMDefinitionElement) {
            if (DefaultXBLManager.this.getXblBoundElement((Node)target) == null) {
               XBLOMDefinitionElement def = (XBLOMDefinitionElement)target;
               if (def.getAttributeNS((String)null, "ref").length() == 0) {
                  DefaultXBLManager.this.addDefinition(def.getElementNamespaceURI(), def.getElementLocalName(), def, (Element)null);
               } else {
                  DefaultXBLManager.this.addDefinitionRef(def);
               }
            }
         } else if (target instanceof XBLOMImportElement) {
            if (DefaultXBLManager.this.getXblBoundElement((Node)target) == null) {
               DefaultXBLManager.this.addImport((Element)target);
            }
         } else {
            evt = XBLEventSupport.getUltimateOriginalEvent(evt);
            target = evt.getTarget();
            Node parent = DefaultXBLManager.this.getXblParentNode((Node)target);
            if (parent != null) {
               DefaultXBLManager.this.invalidateChildNodes(parent);
            }

            if (target instanceof BindableElement) {
               for(Node n = ((Node)target).getParentNode(); n != null; n = n.getParentNode()) {
                  if (n instanceof BindableElement && DefaultXBLManager.this.getRecord(n).definitionElement != null) {
                     return;
                  }
               }

               DefaultXBLManager.this.bind((Element)target);
            }
         }

      }
   }

   protected class ImportSubtreeListener implements EventListener {
      protected Element importElement;
      protected ImportRemovedListener importRemovedListener;

      public ImportSubtreeListener(Element imp, ImportRemovedListener irl) {
         this.importElement = imp;
         this.importRemovedListener = irl;
      }

      public void handleEvent(Event evt) {
         Object[] defs = this.importRemovedListener.toBeRemoved.toArray();
         this.importRemovedListener.toBeRemoved.clear();
         Object[] var3 = defs;
         int var4 = defs.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            Object def1 = var3[var5];
            XBLOMDefinitionElement def = (XBLOMDefinitionElement)def1;
            DefinitionRecord defRec = (DefinitionRecord)DefaultXBLManager.this.definitions.get(def, this.importElement);
            DefaultXBLManager.this.removeDefinition(defRec);
         }

      }
   }

   protected static class ImportRemovedListener implements EventListener {
      protected LinkedList toBeRemoved = new LinkedList();

      public void handleEvent(Event evt) {
         this.toBeRemoved.add(evt.getTarget());
      }
   }

   protected class ImportInsertedListener implements EventListener {
      protected Element importElement;

      public ImportInsertedListener(Element importElement) {
         this.importElement = importElement;
      }

      public void handleEvent(Event evt) {
         EventTarget target = evt.getTarget();
         if (target instanceof XBLOMDefinitionElement) {
            XBLOMDefinitionElement def = (XBLOMDefinitionElement)target;
            DefaultXBLManager.this.addDefinition(def.getElementNamespaceURI(), def.getElementLocalName(), def, this.importElement);
         }

      }
   }

   protected class ImportRecord {
      public Element importElement;
      public Node node;
      public DefNodeInsertedListener defNodeInsertedListener;
      public DefNodeRemovedListener defNodeRemovedListener;
      public DefAttrListener defAttrListener;
      public ImportInsertedListener importInsertedListener;
      public ImportRemovedListener importRemovedListener;
      public ImportSubtreeListener importSubtreeListener;
      public TemplateMutationListener templateMutationListener;

      public ImportRecord(Element imp, Node n) {
         this.importElement = imp;
         this.node = n;
         this.defNodeInsertedListener = DefaultXBLManager.this.new DefNodeInsertedListener(imp);
         this.defNodeRemovedListener = DefaultXBLManager.this.new DefNodeRemovedListener(imp);
         this.defAttrListener = DefaultXBLManager.this.new DefAttrListener(imp);
         this.importInsertedListener = DefaultXBLManager.this.new ImportInsertedListener(imp);
         this.importRemovedListener = new ImportRemovedListener();
         this.importSubtreeListener = DefaultXBLManager.this.new ImportSubtreeListener(imp, this.importRemovedListener);
         this.templateMutationListener = DefaultXBLManager.this.new TemplateMutationListener(imp);
      }
   }

   protected static class DefinitionRecord implements Comparable {
      public String namespaceURI;
      public String localName;
      public XBLOMDefinitionElement definition;
      public XBLOMTemplateElement template;
      public Element importElement;

      public DefinitionRecord(String ns, String ln, XBLOMDefinitionElement def, XBLOMTemplateElement t, Element imp) {
         this.namespaceURI = ns;
         this.localName = ln;
         this.definition = def;
         this.template = t;
         this.importElement = imp;
      }

      public boolean equals(Object other) {
         return this.compareTo(other) == 0;
      }

      public int compareTo(Object other) {
         DefinitionRecord rec = (DefinitionRecord)other;
         Object n1;
         Object n2;
         if (this.importElement == null) {
            n1 = this.definition;
            if (rec.importElement == null) {
               n2 = rec.definition;
            } else {
               n2 = (AbstractNode)rec.importElement;
            }
         } else if (rec.importElement == null) {
            n1 = (AbstractNode)this.importElement;
            n2 = rec.definition;
         } else if (this.definition.getOwnerDocument() == rec.definition.getOwnerDocument()) {
            n1 = this.definition;
            n2 = rec.definition;
         } else {
            n1 = (AbstractNode)this.importElement;
            n2 = (AbstractNode)rec.importElement;
         }

         short comp = ((AbstractNode)n1).compareDocumentPosition((Node)n2);
         if ((comp & 2) != 0) {
            return -1;
         } else {
            return (comp & 4) != 0 ? 1 : 0;
         }
      }
   }
}
