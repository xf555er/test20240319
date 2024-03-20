package org.apache.batik.dom;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.WeakHashMap;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;
import org.apache.batik.dom.events.DocumentEventSupport;
import org.apache.batik.dom.events.EventSupport;
import org.apache.batik.dom.traversal.TraversalSupport;
import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.dom.xbl.GenericXBLManager;
import org.apache.batik.dom.xbl.XBLManager;
import org.apache.batik.i18n.Localizable;
import org.apache.batik.i18n.LocalizableSupport;
import org.apache.batik.util.CleanerThread;
import org.apache.batik.util.SoftDoublyIndexedTable;
import org.apache.batik.w3c.dom.events.MutationNameEvent;
import org.apache.xml.utils.PrefixResolver;
import org.apache.xpath.XPath;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XObject;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMError;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.DOMLocator;
import org.w3c.dom.DOMStringList;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.DocumentEvent;
import org.w3c.dom.events.Event;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;
import org.w3c.dom.traversal.TreeWalker;
import org.w3c.dom.xpath.XPathEvaluator;
import org.w3c.dom.xpath.XPathException;
import org.w3c.dom.xpath.XPathExpression;
import org.w3c.dom.xpath.XPathNSResolver;
import org.w3c.dom.xpath.XPathResult;

public abstract class AbstractDocument extends AbstractParentNode implements Document, DocumentEvent, DocumentTraversal, Localizable, XPathEvaluator {
   protected static final String RESOURCES = "org.apache.batik.dom.resources.Messages";
   protected transient LocalizableSupport localizableSupport = new LocalizableSupport("org.apache.batik.dom.resources.Messages", this.getClass().getClassLoader());
   protected transient DOMImplementation implementation;
   protected transient TraversalSupport traversalSupport;
   protected transient DocumentEventSupport documentEventSupport;
   protected transient boolean eventsEnabled;
   protected transient WeakHashMap elementsByTagNames;
   protected transient WeakHashMap elementsByTagNamesNS;
   protected String inputEncoding;
   protected String xmlEncoding;
   protected String xmlVersion = "1.0";
   protected boolean xmlStandalone;
   protected String documentURI;
   protected boolean strictErrorChecking = true;
   protected DocumentConfiguration domConfig;
   protected transient XBLManager xblManager = new GenericXBLManager();
   protected transient Map elementsById;

   protected AbstractDocument() {
   }

   public AbstractDocument(DocumentType dt, DOMImplementation impl) {
      this.implementation = impl;
      if (dt != null) {
         if (dt instanceof GenericDocumentType) {
            GenericDocumentType gdt = (GenericDocumentType)dt;
            if (gdt.getOwnerDocument() == null) {
               gdt.setOwnerDocument(this);
            }
         }

         this.appendChild(dt);
      }

   }

   public void setDocumentInputEncoding(String ie) {
      this.inputEncoding = ie;
   }

   public void setDocumentXmlEncoding(String xe) {
      this.xmlEncoding = xe;
   }

   public void setLocale(Locale l) {
      this.localizableSupport.setLocale(l);
   }

   public Locale getLocale() {
      return this.localizableSupport.getLocale();
   }

   public String formatMessage(String key, Object[] args) throws MissingResourceException {
      return this.localizableSupport.formatMessage(key, args);
   }

   public boolean getEventsEnabled() {
      return this.eventsEnabled;
   }

   public void setEventsEnabled(boolean b) {
      this.eventsEnabled = b;
   }

   public String getNodeName() {
      return "#document";
   }

   public short getNodeType() {
      return 9;
   }

   public DocumentType getDoctype() {
      for(Node n = this.getFirstChild(); n != null; n = n.getNextSibling()) {
         if (n.getNodeType() == 10) {
            return (DocumentType)n;
         }
      }

      return null;
   }

   public void setDoctype(DocumentType dt) {
      if (dt != null) {
         this.appendChild(dt);
         ((ExtendedNode)dt).setReadonly(true);
      }

   }

   public DOMImplementation getImplementation() {
      return this.implementation;
   }

   public Element getDocumentElement() {
      for(Node n = this.getFirstChild(); n != null; n = n.getNextSibling()) {
         if (n.getNodeType() == 1) {
            return (Element)n;
         }
      }

      return null;
   }

   public Node importNode(Node importedNode, boolean deep) throws DOMException {
      return this.importNode(importedNode, deep, false);
   }

   public Node importNode(Node importedNode, boolean deep, boolean trimId) {
      Object result;
      switch (importedNode.getNodeType()) {
         case 1:
            Element e = this.createElementNS(importedNode.getNamespaceURI(), importedNode.getNodeName());
            result = e;
            if (importedNode.hasAttributes()) {
               NamedNodeMap attr = importedNode.getAttributes();
               int len = attr.getLength();

               for(int i = 0; i < len; ++i) {
                  Attr a = (Attr)attr.item(i);
                  if (a.getSpecified()) {
                     AbstractAttr aa = (AbstractAttr)this.importNode(a, true);
                     if (trimId && aa.isId()) {
                        aa.setIsId(false);
                     }

                     e.setAttributeNodeNS(aa);
                  }
               }
            }
            break;
         case 2:
            result = this.createAttributeNS(importedNode.getNamespaceURI(), importedNode.getNodeName());
            break;
         case 3:
            result = this.createTextNode(importedNode.getNodeValue());
            deep = false;
            break;
         case 4:
            result = this.createCDATASection(importedNode.getNodeValue());
            deep = false;
            break;
         case 5:
            result = this.createEntityReference(importedNode.getNodeName());
            break;
         case 6:
         case 9:
         default:
            throw this.createDOMException((short)9, "import.node", new Object[0]);
         case 7:
            result = this.createProcessingInstruction(importedNode.getNodeName(), importedNode.getNodeValue());
            deep = false;
            break;
         case 8:
            result = this.createComment(importedNode.getNodeValue());
            deep = false;
            break;
         case 10:
            DocumentType docType = (DocumentType)importedNode;
            GenericDocumentType copy = new GenericDocumentType(docType.getName(), docType.getPublicId(), docType.getSystemId());
            copy.ownerDocument = this;
            result = copy;
            break;
         case 11:
            result = this.createDocumentFragment();
      }

      if (importedNode instanceof AbstractNode) {
         this.fireUserDataHandlers((short)2, importedNode, (Node)result);
      }

      if (deep) {
         for(Node n = importedNode.getFirstChild(); n != null; n = n.getNextSibling()) {
            ((Node)result).appendChild(this.importNode(n, true));
         }
      }

      return (Node)result;
   }

   public Node cloneNode(boolean deep) {
      Document n = (Document)this.newNode();
      this.copyInto(n);
      this.fireUserDataHandlers((short)1, this, n);
      if (deep) {
         for(Node c = this.getFirstChild(); c != null; c = c.getNextSibling()) {
            n.appendChild(n.importNode(c, deep));
         }
      }

      return n;
   }

   public abstract boolean isId(Attr var1);

   public Element getElementById(String id) {
      return this.getChildElementById(this.getDocumentElement(), id);
   }

   public Element getChildElementById(Node requestor, String id) {
      if (id != null && id.length() != 0) {
         if (this.elementsById == null) {
            return null;
         } else {
            Node root = this.getRoot(requestor);
            Object o = this.elementsById.get(id);
            if (o == null) {
               return null;
            } else if (o instanceof IdSoftRef) {
               o = ((IdSoftRef)o).get();
               if (o == null) {
                  this.elementsById.remove(id);
                  return null;
               } else {
                  Element e = (Element)o;
                  return this.getRoot(e) == root ? e : null;
               }
            } else {
               List l = (List)o;
               Iterator li = l.iterator();

               while(li.hasNext()) {
                  IdSoftRef sr = (IdSoftRef)li.next();
                  o = sr.get();
                  if (o == null) {
                     li.remove();
                  } else {
                     Element e = (Element)o;
                     if (this.getRoot(e) == root) {
                        return e;
                     }
                  }
               }

               return null;
            }
         }
      } else {
         return null;
      }
   }

   protected Node getRoot(Node n) {
      Node r;
      for(r = n; n != null; n = n.getParentNode()) {
         r = n;
      }

      return r;
   }

   public void removeIdEntry(Element e, String id) {
      if (id != null) {
         if (this.elementsById != null) {
            synchronized(this.elementsById) {
               Object o = this.elementsById.get(id);
               if (o != null) {
                  if (o instanceof IdSoftRef) {
                     this.elementsById.remove(id);
                  } else {
                     List l = (List)o;
                     Iterator li = l.iterator();

                     while(li.hasNext()) {
                        IdSoftRef ip = (IdSoftRef)li.next();
                        o = ip.get();
                        if (o == null) {
                           li.remove();
                        } else if (e == o) {
                           li.remove();
                           break;
                        }
                     }

                     if (l.size() == 0) {
                        this.elementsById.remove(id);
                     }

                  }
               }
            }
         }
      }
   }

   public void addIdEntry(Element e, String id) {
      if (id != null) {
         if (this.elementsById == null) {
            Map tmp = new HashMap();
            tmp.put(id, new IdSoftRef(e, id));
            this.elementsById = tmp;
         } else {
            synchronized(this.elementsById) {
               Object o = this.elementsById.get(id);
               if (o == null) {
                  this.elementsById.put(id, new IdSoftRef(e, id));
               } else if (o instanceof IdSoftRef) {
                  IdSoftRef ip = (IdSoftRef)o;
                  Object r = ip.get();
                  if (r == null) {
                     this.elementsById.put(id, new IdSoftRef(e, id));
                  } else {
                     List l = new ArrayList(4);
                     ip.setList(l);
                     l.add(ip);
                     l.add(new IdSoftRef(e, id, l));
                     this.elementsById.put(id, l);
                  }
               } else {
                  List l = (List)o;
                  l.add(new IdSoftRef(e, id, l));
               }
            }
         }
      }
   }

   public void updateIdEntry(Element e, String oldId, String newId) {
      if (oldId != newId && (oldId == null || !oldId.equals(newId))) {
         this.removeIdEntry(e, oldId);
         this.addIdEntry(e, newId);
      }
   }

   public AbstractParentNode.ElementsByTagName getElementsByTagName(Node n, String ln) {
      if (this.elementsByTagNames == null) {
         return null;
      } else {
         SoftDoublyIndexedTable t = (SoftDoublyIndexedTable)this.elementsByTagNames.get(n);
         return t == null ? null : (AbstractParentNode.ElementsByTagName)t.get((Object)null, ln);
      }
   }

   public void putElementsByTagName(Node n, String ln, AbstractParentNode.ElementsByTagName l) {
      if (this.elementsByTagNames == null) {
         this.elementsByTagNames = new WeakHashMap(11);
      }

      SoftDoublyIndexedTable t = (SoftDoublyIndexedTable)this.elementsByTagNames.get(n);
      if (t == null) {
         this.elementsByTagNames.put(n, t = new SoftDoublyIndexedTable());
      }

      t.put((Object)null, ln, l);
   }

   public AbstractParentNode.ElementsByTagNameNS getElementsByTagNameNS(Node n, String ns, String ln) {
      if (this.elementsByTagNamesNS == null) {
         return null;
      } else {
         SoftDoublyIndexedTable t = (SoftDoublyIndexedTable)this.elementsByTagNamesNS.get(n);
         return t == null ? null : (AbstractParentNode.ElementsByTagNameNS)t.get(ns, ln);
      }
   }

   public void putElementsByTagNameNS(Node n, String ns, String ln, AbstractParentNode.ElementsByTagNameNS l) {
      if (this.elementsByTagNamesNS == null) {
         this.elementsByTagNamesNS = new WeakHashMap(11);
      }

      SoftDoublyIndexedTable t = (SoftDoublyIndexedTable)this.elementsByTagNamesNS.get(n);
      if (t == null) {
         this.elementsByTagNamesNS.put(n, t = new SoftDoublyIndexedTable());
      }

      t.put(ns, ln, l);
   }

   public Event createEvent(String eventType) throws DOMException {
      if (this.documentEventSupport == null) {
         this.documentEventSupport = ((AbstractDOMImplementation)this.implementation).createDocumentEventSupport();
      }

      return this.documentEventSupport.createEvent(eventType);
   }

   public boolean canDispatch(String ns, String eventType) {
      if (eventType == null) {
         return false;
      } else {
         if (ns != null && ns.length() == 0) {
            ns = null;
         }

         if (ns != null && !ns.equals("http://www.w3.org/2001/xml-events")) {
            return false;
         } else {
            return eventType.equals("Event") || eventType.equals("MutationEvent") || eventType.equals("MutationNameEvent") || eventType.equals("UIEvent") || eventType.equals("MouseEvent") || eventType.equals("KeyEvent") || eventType.equals("KeyboardEvent") || eventType.equals("TextEvent") || eventType.equals("CustomEvent");
         }
      }
   }

   public NodeIterator createNodeIterator(Node root, int whatToShow, NodeFilter filter, boolean entityReferenceExpansion) throws DOMException {
      if (this.traversalSupport == null) {
         this.traversalSupport = new TraversalSupport();
      }

      return this.traversalSupport.createNodeIterator(this, root, whatToShow, filter, entityReferenceExpansion);
   }

   public TreeWalker createTreeWalker(Node root, int whatToShow, NodeFilter filter, boolean entityReferenceExpansion) throws DOMException {
      return TraversalSupport.createTreeWalker(this, root, whatToShow, filter, entityReferenceExpansion);
   }

   public void detachNodeIterator(NodeIterator it) {
      this.traversalSupport.detachNodeIterator(it);
   }

   public void nodeToBeRemoved(Node node) {
      if (this.traversalSupport != null) {
         this.traversalSupport.nodeToBeRemoved(node);
      }

   }

   protected AbstractDocument getCurrentDocument() {
      return this;
   }

   protected Node export(Node n, Document d) {
      throw this.createDOMException((short)9, "import.document", new Object[0]);
   }

   protected Node deepExport(Node n, Document d) {
      throw this.createDOMException((short)9, "import.document", new Object[0]);
   }

   protected Node copyInto(Node n) {
      super.copyInto(n);
      AbstractDocument ad = (AbstractDocument)n;
      ad.implementation = this.implementation;
      ad.localizableSupport = new LocalizableSupport("org.apache.batik.dom.resources.Messages", this.getClass().getClassLoader());
      ad.inputEncoding = this.inputEncoding;
      ad.xmlEncoding = this.xmlEncoding;
      ad.xmlVersion = this.xmlVersion;
      ad.xmlStandalone = this.xmlStandalone;
      ad.documentURI = this.documentURI;
      ad.strictErrorChecking = this.strictErrorChecking;
      return n;
   }

   protected Node deepCopyInto(Node n) {
      super.deepCopyInto(n);
      AbstractDocument ad = (AbstractDocument)n;
      ad.implementation = this.implementation;
      ad.localizableSupport = new LocalizableSupport("org.apache.batik.dom.resources.Messages", this.getClass().getClassLoader());
      return n;
   }

   protected void checkChildType(Node n, boolean replace) {
      short t = n.getNodeType();
      switch (t) {
         case 1:
         case 7:
         case 8:
         case 10:
         case 11:
            if ((replace || t != 1 || this.getDocumentElement() == null) && (t != 10 || this.getDoctype() == null)) {
               return;
            } else {
               throw this.createDOMException((short)9, "document.child.already.exists", new Object[]{Integer.valueOf(t), n.getNodeName()});
            }
         case 2:
         case 3:
         case 4:
         case 5:
         case 6:
         case 9:
         default:
            throw this.createDOMException((short)3, "child.type", new Object[]{Integer.valueOf(this.getNodeType()), this.getNodeName(), Integer.valueOf(t), n.getNodeName()});
      }
   }

   public String getInputEncoding() {
      return this.inputEncoding;
   }

   public String getXmlEncoding() {
      return this.xmlEncoding;
   }

   public boolean getXmlStandalone() {
      return this.xmlStandalone;
   }

   public void setXmlStandalone(boolean b) throws DOMException {
      this.xmlStandalone = b;
   }

   public String getXmlVersion() {
      return this.xmlVersion;
   }

   public void setXmlVersion(String v) throws DOMException {
      if (v != null && (v.equals("1.0") || v.equals("1.1"))) {
         this.xmlVersion = v;
      } else {
         throw this.createDOMException((short)9, "xml.version", new Object[]{v});
      }
   }

   public boolean getStrictErrorChecking() {
      return this.strictErrorChecking;
   }

   public void setStrictErrorChecking(boolean b) {
      this.strictErrorChecking = b;
   }

   public String getDocumentURI() {
      return this.documentURI;
   }

   public void setDocumentURI(String uri) {
      this.documentURI = uri;
   }

   public DOMConfiguration getDomConfig() {
      if (this.domConfig == null) {
         this.domConfig = new DocumentConfiguration();
      }

      return this.domConfig;
   }

   public Node adoptNode(Node n) throws DOMException {
      if (!(n instanceof AbstractNode)) {
         return null;
      } else {
         switch (n.getNodeType()) {
            case 6:
            case 12:
               return null;
            case 7:
            case 8:
            case 11:
            default:
               AbstractNode an = (AbstractNode)n;
               if (an.isReadonly()) {
                  throw this.createDOMException((short)7, "readonly.node", new Object[]{Integer.valueOf(an.getNodeType()), an.getNodeName()});
               }

               Node parent = n.getParentNode();
               if (parent != null) {
                  parent.removeChild(n);
               }

               this.adoptNode1((AbstractNode)n);
               return n;
            case 9:
               throw this.createDOMException((short)9, "adopt.document", new Object[0]);
            case 10:
               throw this.createDOMException((short)9, "adopt.document.type", new Object[0]);
         }
      }
   }

   protected void adoptNode1(AbstractNode n) {
      n.ownerDocument = this;
      AbstractAttr attr;
      label36:
      switch (n.getNodeType()) {
         case 1:
            NamedNodeMap nnm = n.getAttributes();
            int len = nnm.getLength();
            int i = 0;

            while(true) {
               if (i >= len) {
                  break label36;
               }

               attr = (AbstractAttr)nnm.item(i);
               if (attr.getSpecified()) {
                  this.adoptNode1(attr);
               }

               ++i;
            }
         case 2:
            attr = (AbstractAttr)n;
            attr.ownerElement = null;
            attr.unspecified = false;
         case 3:
         case 4:
         default:
            break;
         case 5:
            while(n.getFirstChild() != null) {
               n.removeChild(n.getFirstChild());
            }
      }

      this.fireUserDataHandlers((short)5, n, (Node)null);
      Node m = n.getFirstChild();

      while(m != null) {
         switch (m.getNodeType()) {
            case 6:
            case 10:
            case 12:
               return;
            default:
               this.adoptNode1((AbstractNode)m);
               m = m.getNextSibling();
         }
      }

   }

   public Node renameNode(Node n, String ns, String qn) {
      AbstractNode an = (AbstractNode)n;
      if (an == this.getDocumentElement()) {
         throw this.createDOMException((short)9, "rename.document.element", new Object[0]);
      } else {
         int nt = n.getNodeType();
         if (nt != 1 && nt != 2) {
            throw this.createDOMException((short)9, "rename.node", new Object[]{Integer.valueOf(nt), n.getNodeName()});
         } else if ((!this.xmlVersion.equals("1.1") || DOMUtilities.isValidName11(qn)) && DOMUtilities.isValidName(qn)) {
            if (n.getOwnerDocument() != this) {
               throw this.createDOMException((short)9, "node.from.wrong.document", new Object[]{Integer.valueOf(nt), n.getNodeName()});
            } else {
               int i = qn.indexOf(58);
               if (i != 0 && i != qn.length() - 1) {
                  String prefix = DOMUtilities.getPrefix(qn);
                  if (ns != null && ns.length() == 0) {
                     ns = null;
                  }

                  if (prefix != null && ns == null) {
                     throw this.createDOMException((short)14, "prefix", new Object[]{Integer.valueOf(nt), n.getNodeName(), prefix});
                  } else if (!this.strictErrorChecking || (!"xml".equals(prefix) || "http://www.w3.org/XML/1998/namespace".equals(ns)) && (!"xmlns".equals(prefix) || "http://www.w3.org/2000/xmlns/".equals(ns))) {
                     String prevNamespaceURI = n.getNamespaceURI();
                     String prevNodeName = n.getNodeName();
                     if (nt != 1) {
                        Element e;
                        if (n instanceof AbstractAttrNS) {
                           AbstractAttrNS a = (AbstractAttrNS)n;
                           e = a.getOwnerElement();
                           if (e != null) {
                              e.removeAttributeNode(a);
                           }

                           a.namespaceURI = ns;
                           a.nodeName = qn;
                           if (e != null) {
                              e.setAttributeNodeNS(a);
                           }

                           this.fireUserDataHandlers((short)4, a, (Node)null);
                           if (this.getEventsEnabled()) {
                              MutationNameEvent ev = (MutationNameEvent)this.createEvent("MutationNameEvent");
                              ev.initMutationNameEventNS("http://www.w3.org/2001/xml-events", "DOMAttrNameChanged", true, false, a, prevNamespaceURI, prevNodeName);
                              this.dispatchEvent(ev);
                           }

                           return a;
                        } else {
                           AbstractAttr a = (AbstractAttr)n;
                           e = a.getOwnerElement();
                           if (e != null) {
                              e.removeAttributeNode(a);
                           }

                           AbstractAttr a2 = (AbstractAttr)this.createAttributeNS(ns, qn);
                           a2.setNodeValue(a.getNodeValue());
                           a2.userData = a.userData == null ? null : (HashMap)a.userData.clone();
                           a2.userDataHandlers = a.userDataHandlers == null ? null : (HashMap)a.userDataHandlers.clone();
                           if (e != null) {
                              e.setAttributeNodeNS(a2);
                           }

                           this.fireUserDataHandlers((short)4, a, a2);
                           if (this.getEventsEnabled()) {
                              MutationNameEvent ev = (MutationNameEvent)this.createEvent("MutationNameEvent");
                              ev.initMutationNameEventNS("http://www.w3.org/2001/xml-events", "DOMAttrNameChanged", true, false, a2, prevNamespaceURI, prevNodeName);
                              this.dispatchEvent(ev);
                           }

                           return a2;
                        }
                     } else {
                        Node parent = n.getParentNode();
                        AbstractElement e = (AbstractElement)this.createElementNS(ns, qn);
                        EventSupport es1 = an.getEventSupport();
                        EventSupport next;
                        if (es1 != null) {
                           next = e.getEventSupport();
                           if (next == null) {
                              AbstractDOMImplementation di = (AbstractDOMImplementation)this.implementation;
                              next = di.createEventSupport(e);
                              this.setEventsEnabled(true);
                              e.eventSupport = next;
                           }

                           es1.moveEventListeners(e.getEventSupport());
                        }

                        e.userData = e.userData == null ? null : (HashMap)an.userData.clone();
                        e.userDataHandlers = e.userDataHandlers == null ? null : (HashMap)an.userDataHandlers.clone();
                        next = null;
                        if (parent != null) {
                           n.getNextSibling();
                           parent.removeChild(n);
                        }

                        while(n.getFirstChild() != null) {
                           e.appendChild(n.getFirstChild());
                        }

                        NamedNodeMap nnm = n.getAttributes();

                        for(int j = 0; j < nnm.getLength(); ++j) {
                           Attr a = (Attr)nnm.item(j);
                           e.setAttributeNodeNS(a);
                        }

                        if (parent != null) {
                           if (next == null) {
                              parent.appendChild(e);
                           } else {
                              parent.insertBefore(next, e);
                           }
                        }

                        this.fireUserDataHandlers((short)4, n, e);
                        if (this.getEventsEnabled()) {
                           MutationNameEvent ev = (MutationNameEvent)this.createEvent("MutationNameEvent");
                           ev.initMutationNameEventNS("http://www.w3.org/2001/xml-events", "DOMElementNameChanged", true, false, (Node)null, prevNamespaceURI, prevNodeName);
                           this.dispatchEvent(ev);
                        }

                        return e;
                     }
                  } else {
                     throw this.createDOMException((short)14, "namespace", new Object[]{Integer.valueOf(nt), n.getNodeName(), ns});
                  }
               } else {
                  throw this.createDOMException((short)14, "qname", new Object[]{Integer.valueOf(nt), n.getNodeName(), qn});
               }
            }
         } else {
            throw this.createDOMException((short)9, "wf.invalid.name", new Object[]{qn});
         }
      }
   }

   public void normalizeDocument() {
      if (this.domConfig == null) {
         this.domConfig = new DocumentConfiguration();
      }

      boolean cdataSections = this.domConfig.getBooleanParameter("cdata-sections");
      boolean comments = this.domConfig.getBooleanParameter("comments");
      boolean elementContentWhitespace = this.domConfig.getBooleanParameter("element-content-whitespace");
      boolean namespaceDeclarations = this.domConfig.getBooleanParameter("namespace-declarations");
      boolean namespaces = this.domConfig.getBooleanParameter("namespaces");
      boolean splitCdataSections = this.domConfig.getBooleanParameter("split-cdata-sections");
      DOMErrorHandler errorHandler = (DOMErrorHandler)this.domConfig.getParameter("error-handler");
      this.normalizeDocument(this.getDocumentElement(), cdataSections, comments, elementContentWhitespace, namespaceDeclarations, namespaces, splitCdataSections, errorHandler);
   }

   protected boolean normalizeDocument(Element e, boolean cdataSections, boolean comments, boolean elementContentWhitepace, boolean namespaceDeclarations, boolean namespaces, boolean splitCdataSections, DOMErrorHandler errorHandler) {
      AbstractElement ae = (AbstractElement)e;
      Node n = e.getFirstChild();

      while(true) {
         Node m;
         String ens;
         while(n != null) {
            int nt = ((Node)n).getNodeType();
            if (nt != 3 && (cdataSections || nt != 4)) {
               if (nt == 4 && splitCdataSections) {
                  if (!this.splitCdata(e, (Node)n, errorHandler)) {
                     return false;
                  }
               } else if (nt == 8 && !comments) {
                  Node next = ((Node)n).getPreviousSibling();
                  if (next == null) {
                     next = ((Node)n).getNextSibling();
                  }

                  e.removeChild((Node)n);
                  n = next;
                  continue;
               }
            } else {
               Node t = n;
               StringBuffer sb = new StringBuffer();
               sb.append(((Node)n).getNodeValue());

               Node n;
               for(n = ((Node)n).getNextSibling(); n != null && (n.getNodeType() == 3 || !cdataSections && n.getNodeType() == 4); n = m) {
                  sb.append(n.getNodeValue());
                  m = n.getNextSibling();
                  e.removeChild(n);
               }

               ens = sb.toString();
               if (ens.length() == 0) {
                  Node next = n.getNextSibling();
                  e.removeChild(n);
                  n = next;
                  continue;
               }

               if (!ens.equals(((Node)n).getNodeValue())) {
                  if (!cdataSections && nt == 3) {
                     n = this.createTextNode(ens);
                     e.replaceChild((Node)n, (Node)t);
                  } else {
                     n = n;
                     ((Node)t).setNodeValue(ens);
                  }
               } else {
                  n = n;
               }

               if (!elementContentWhitepace) {
                  nt = ((Node)n).getNodeType();
                  if (nt == 3) {
                     AbstractText tn = (AbstractText)n;
                     if (tn.isElementContentWhitespace()) {
                        Node next = ((Node)n).getNextSibling();
                        e.removeChild((Node)n);
                        n = next;
                        continue;
                     }
                  }
               }

               if (nt == 4 && splitCdataSections && !this.splitCdata(e, (Node)n, errorHandler)) {
                  return false;
               }
            }

            n = ((Node)n).getNextSibling();
         }

         NamedNodeMap nnm = e.getAttributes();
         LinkedList toRemove = new LinkedList();
         HashMap names = new HashMap();

         String ans;
         int i;
         Attr a;
         String s;
         for(i = 0; i < nnm.getLength(); ++i) {
            a = (Attr)nnm.item(i);
            s = a.getPrefix();
            if (a != null && "xmlns".equals(s) || a.getNodeName().equals("xmlns")) {
               if (!namespaceDeclarations) {
                  toRemove.add(a);
               } else {
                  ans = a.getNodeValue();
                  if (!a.getNodeValue().equals("http://www.w3.org/2000/xmlns/") && ans.equals("http://www.w3.org/2000/xmlns/")) {
                     names.put(s, ans);
                  }
               }
            }
         }

         if (!namespaceDeclarations) {
            Iterator var29 = toRemove.iterator();

            while(var29.hasNext()) {
               Object aToRemove = var29.next();
               e.removeAttributeNode((Attr)aToRemove);
            }
         } else if (namespaces) {
            ens = e.getNamespaceURI();
            if (ens != null) {
               String eprefix = e.getPrefix();
               if (!this.compareStrings(ae.lookupNamespaceURI(eprefix), ens)) {
                  e.setAttributeNS("http://www.w3.org/2000/xmlns/", eprefix == null ? "xmlns" : "xmlns:" + eprefix, ens);
               }
            } else if (e.getLocalName() != null && ae.lookupNamespaceURI((String)null) == null) {
               e.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", "");
            }

            nnm = e.getAttributes();

            for(int i = 0; i < nnm.getLength(); ++i) {
               Attr a = (Attr)nnm.item(i);
               ans = a.getNamespaceURI();
               if (ans == null) {
                  if (a.getLocalName() == null) {
                  }
               } else {
                  String apre = a.getPrefix();
                  if ((apre == null || !apre.equals("xml") && !apre.equals("xmlns")) && !ans.equals("http://www.w3.org/2000/xmlns/")) {
                     String aprens = apre == null ? null : ae.lookupNamespaceURI(apre);
                     if (apre == null || aprens == null || !aprens.equals(ans)) {
                        String newpre = ae.lookupPrefix(ans);
                        if (newpre != null) {
                           a.setPrefix(newpre);
                        } else if (apre != null && ae.lookupNamespaceURI(apre) == null) {
                           e.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:" + apre, ans);
                        } else {
                           int index = 1;

                           do {
                              newpre = "NS" + index;
                           } while(ae.lookupPrefix(newpre) != null);

                           e.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:" + newpre, ans);
                           a.setPrefix(newpre);
                        }
                     }
                  }
               }
            }
         }

         nnm = e.getAttributes();

         for(i = 0; i < nnm.getLength(); ++i) {
            a = (Attr)nnm.item(i);
            if (!this.checkName(a.getNodeName()) && errorHandler != null && !errorHandler.handleError(this.createDOMError("wf-invalid-character-in-node-name", (short)2, "wf.invalid.name", new Object[]{a.getNodeName()}, a, (Exception)null))) {
               return false;
            }

            if (!this.checkChars(a.getNodeValue()) && errorHandler != null && !errorHandler.handleError(this.createDOMError("wf-invalid-character", (short)2, "wf.invalid.character", new Object[]{2, a.getNodeName(), a.getNodeValue()}, a, (Exception)null))) {
               return false;
            }
         }

         for(m = e.getFirstChild(); m != null; m = m.getNextSibling()) {
            int nt = m.getNodeType();
            switch (nt) {
               case 1:
                  if (!this.checkName(m.getNodeName()) && errorHandler != null && !errorHandler.handleError(this.createDOMError("wf-invalid-character-in-node-name", (short)2, "wf.invalid.name", new Object[]{m.getNodeName()}, m, (Exception)null))) {
                     return false;
                  }

                  if (!this.normalizeDocument((Element)m, cdataSections, comments, elementContentWhitepace, namespaceDeclarations, namespaces, splitCdataSections, errorHandler)) {
                     return false;
                  }
               case 2:
               case 5:
               case 6:
               default:
                  break;
               case 3:
                  s = m.getNodeValue();
                  if (!this.checkChars(s) && errorHandler != null && !errorHandler.handleError(this.createDOMError("wf-invalid-character", (short)2, "wf.invalid.character", new Object[]{Integer.valueOf(m.getNodeType()), m.getNodeName(), s}, m, (Exception)null))) {
                     return false;
                  }
                  break;
               case 4:
                  s = m.getNodeValue();
                  if ((!this.checkChars(s) || s.indexOf("]]>") != -1) && errorHandler != null && !errorHandler.handleError(this.createDOMError("wf-invalid-character", (short)2, "wf.invalid.character", new Object[]{Integer.valueOf(m.getNodeType()), m.getNodeName(), s}, m, (Exception)null))) {
                     return false;
                  }
                  break;
               case 7:
                  if (m.getNodeName().equalsIgnoreCase("xml") && errorHandler != null && !errorHandler.handleError(this.createDOMError("wf-invalid-character-in-node-name", (short)2, "wf.invalid.name", new Object[]{m.getNodeName()}, m, (Exception)null))) {
                     return false;
                  }

                  s = m.getNodeValue();
                  if ((!this.checkChars(s) || s.indexOf("?>") != -1) && errorHandler != null && !errorHandler.handleError(this.createDOMError("wf-invalid-character", (short)2, "wf.invalid.character", new Object[]{Integer.valueOf(m.getNodeType()), m.getNodeName(), s}, m, (Exception)null))) {
                     return false;
                  }
                  break;
               case 8:
                  s = m.getNodeValue();
                  if ((!this.checkChars(s) || s.indexOf("--") != -1 || s.charAt(s.length() - 1) == '-') && errorHandler != null && !errorHandler.handleError(this.createDOMError("wf-invalid-character", (short)2, "wf.invalid.character", new Object[]{Integer.valueOf(m.getNodeType()), m.getNodeName(), s}, m, (Exception)null))) {
                     return false;
                  }
            }
         }

         return true;
      }
   }

   protected boolean splitCdata(Element e, Node n, DOMErrorHandler errorHandler) {
      String s2 = n.getNodeValue();
      int index = s2.indexOf("]]>");
      if (index != -1) {
         String before = s2.substring(0, index + 2);
         String after = s2.substring(index + 2);
         n.setNodeValue(before);
         Node next = n.getNextSibling();
         if (next == null) {
            e.appendChild(this.createCDATASection(after));
         } else {
            e.insertBefore(this.createCDATASection(after), next);
         }

         if (errorHandler != null && !errorHandler.handleError(this.createDOMError("cdata-sections-splitted", (short)1, "cdata.section.split", new Object[0], n, (Exception)null))) {
            return false;
         }
      }

      return true;
   }

   protected boolean checkChars(String s) {
      int len = s.length();
      int i;
      if (this.xmlVersion.equals("1.1")) {
         for(i = 0; i < len; ++i) {
            if (!DOMUtilities.isXML11Character(s.charAt(i))) {
               return false;
            }
         }
      } else {
         for(i = 0; i < len; ++i) {
            if (!DOMUtilities.isXMLCharacter(s.charAt(i))) {
               return false;
            }
         }
      }

      return true;
   }

   protected boolean checkName(String s) {
      return this.xmlVersion.equals("1.1") ? DOMUtilities.isValidName11(s) : DOMUtilities.isValidName(s);
   }

   protected DOMError createDOMError(String type, short severity, String key, Object[] args, Node related, Exception e) {
      try {
         return new DocumentError(type, severity, this.getCurrentDocument().formatMessage(key, args), related, e);
      } catch (Exception var8) {
         return new DocumentError(type, severity, key, related, e);
      }
   }

   public void setTextContent(String s) throws DOMException {
   }

   public void setXBLManager(XBLManager m) {
      boolean wasProcessing = this.xblManager.isProcessing();
      this.xblManager.stopProcessing();
      if (m == null) {
         m = new GenericXBLManager();
      }

      this.xblManager = (XBLManager)m;
      if (wasProcessing) {
         this.xblManager.startProcessing();
      }

   }

   public XBLManager getXBLManager() {
      return this.xblManager;
   }

   public XPathExpression createExpression(String expression, XPathNSResolver resolver) throws DOMException, XPathException {
      return new XPathExpr(expression, resolver);
   }

   public XPathNSResolver createNSResolver(Node n) {
      return new XPathNodeNSResolver(n);
   }

   public Object evaluate(String expression, Node contextNode, XPathNSResolver resolver, short type, Object result) throws XPathException, DOMException {
      XPathExpression xpath = this.createExpression(expression, resolver);
      return xpath.evaluate(contextNode, type, result);
   }

   public XPathException createXPathException(short type, String key, Object[] args) {
      try {
         return new XPathException(type, this.formatMessage(key, args));
      } catch (Exception var5) {
         return new XPathException(type, key);
      }
   }

   public Node getXblParentNode() {
      return this.xblManager.getXblParentNode(this);
   }

   public NodeList getXblChildNodes() {
      return this.xblManager.getXblChildNodes(this);
   }

   public NodeList getXblScopedChildNodes() {
      return this.xblManager.getXblScopedChildNodes(this);
   }

   public Node getXblFirstChild() {
      return this.xblManager.getXblFirstChild(this);
   }

   public Node getXblLastChild() {
      return this.xblManager.getXblLastChild(this);
   }

   public Node getXblPreviousSibling() {
      return this.xblManager.getXblPreviousSibling(this);
   }

   public Node getXblNextSibling() {
      return this.xblManager.getXblNextSibling(this);
   }

   public Element getXblFirstElementChild() {
      return this.xblManager.getXblFirstElementChild(this);
   }

   public Element getXblLastElementChild() {
      return this.xblManager.getXblLastElementChild(this);
   }

   public Element getXblPreviousElementSibling() {
      return this.xblManager.getXblPreviousElementSibling(this);
   }

   public Element getXblNextElementSibling() {
      return this.xblManager.getXblNextElementSibling(this);
   }

   public Element getXblBoundElement() {
      return this.xblManager.getXblBoundElement(this);
   }

   public Element getXblShadowTree() {
      return this.xblManager.getXblShadowTree(this);
   }

   public NodeList getXblDefinitions() {
      return this.xblManager.getXblDefinitions(this);
   }

   private void writeObject(ObjectOutputStream s) throws IOException {
      s.defaultWriteObject();
      s.writeObject(this.implementation.getClass().getName());
   }

   private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
      s.defaultReadObject();
      this.localizableSupport = new LocalizableSupport("org.apache.batik.dom.resources.Messages", this.getClass().getClassLoader());
      Class c = Class.forName((String)s.readObject());

      try {
         Method m = c.getMethod("getDOMImplementation", (Class[])null);
         this.implementation = (DOMImplementation)m.invoke((Object)null, (Object[])null);
      } catch (Exception var6) {
         if (!DOMImplementation.class.isAssignableFrom(c)) {
            throw new SecurityException("Trying to create object that is not a DOMImplementation.");
         }

         try {
            this.implementation = (DOMImplementation)c.getDeclaredConstructor().newInstance();
         } catch (Exception var5) {
         }
      }

   }

   protected static class XPathNodeNSResolver implements XPathNSResolver {
      protected Node contextNode;

      public XPathNodeNSResolver(Node n) {
         this.contextNode = n;
      }

      public String lookupNamespaceURI(String prefix) {
         return this.contextNode.lookupNamespaceURI(prefix);
      }
   }

   protected class XPathExpr implements XPathExpression {
      protected XPath xpath;
      protected XPathNSResolver resolver;
      protected NSPrefixResolver prefixResolver;
      protected XPathContext context;

      public XPathExpr(String expr, XPathNSResolver res) throws DOMException, XPathException {
         this.resolver = res;
         this.prefixResolver = new NSPrefixResolver();

         try {
            this.xpath = new XPath(expr, (SourceLocator)null, this.prefixResolver, 0);
            this.context = new XPathContext();
         } catch (TransformerException var5) {
            throw AbstractDocument.this.createXPathException((short)51, "xpath.invalid.expression", new Object[]{expr, var5.getMessage()});
         }
      }

      public Object evaluate(Node contextNode, short type, Object res) throws XPathException, DOMException {
         if ((contextNode.getNodeType() == 9 || contextNode.getOwnerDocument() == AbstractDocument.this) && (contextNode.getNodeType() != 9 || contextNode == AbstractDocument.this)) {
            if (type >= 0 && type <= 9) {
               switch (contextNode.getNodeType()) {
                  case 5:
                  case 6:
                  case 10:
                  case 11:
                  case 12:
                     throw AbstractDocument.this.createDOMException((short)9, "xpath.invalid.context.node", new Object[]{Integer.valueOf(contextNode.getNodeType()), contextNode.getNodeName()});
                  case 7:
                  case 8:
                  case 9:
                  default:
                     this.context.reset();
                     XObject result = null;

                     try {
                        result = this.xpath.execute(this.context, contextNode, this.prefixResolver);
                     } catch (TransformerException var7) {
                        throw AbstractDocument.this.createXPathException((short)51, "xpath.error", new Object[]{this.xpath.getPatternString(), var7.getMessage()});
                     }

                     try {
                        switch (type) {
                           case 0:
                              switch (result.getType()) {
                                 case 1:
                                    return this.convertBoolean(result);
                                 case 2:
                                    return this.convertNumber(result);
                                 case 3:
                                    return this.convertString(result);
                                 case 4:
                                    return this.convertNodeIterator(result, (short)4);
                              }
                           default:
                              return null;
                           case 1:
                              return this.convertNumber(result);
                           case 2:
                              return this.convertString(result);
                           case 3:
                              return this.convertBoolean(result);
                           case 4:
                           case 5:
                           case 6:
                           case 7:
                              return this.convertNodeIterator(result, type);
                           case 8:
                           case 9:
                              return this.convertSingleNode(result, type);
                        }
                     } catch (TransformerException var6) {
                        throw AbstractDocument.this.createXPathException((short)52, "xpath.cannot.convert.result", new Object[]{Integer.valueOf(type), var6.getMessage()});
                     }
               }
            } else {
               throw AbstractDocument.this.createDOMException((short)9, "xpath.invalid.result.type", new Object[]{Integer.valueOf(type)});
            }
         } else {
            throw AbstractDocument.this.createDOMException((short)4, "node.from.wrong.document", new Object[]{Integer.valueOf(contextNode.getNodeType()), contextNode.getNodeName()});
         }
      }

      protected Result convertSingleNode(XObject xo, short type) throws TransformerException {
         return new Result(xo.nodelist().item(0), type);
      }

      protected Result convertBoolean(XObject xo) throws TransformerException {
         return new Result(xo.bool());
      }

      protected Result convertNumber(XObject xo) throws TransformerException {
         return new Result(xo.num());
      }

      protected Result convertString(XObject xo) {
         return new Result(xo.str());
      }

      protected Result convertNodeIterator(XObject xo, short type) throws TransformerException {
         return new Result(xo.nodelist(), type);
      }

      protected class NSPrefixResolver implements PrefixResolver {
         public String getBaseIdentifier() {
            return null;
         }

         public String getNamespaceForPrefix(String prefix) {
            return XPathExpr.this.resolver == null ? null : XPathExpr.this.resolver.lookupNamespaceURI(prefix);
         }

         public String getNamespaceForPrefix(String prefix, Node context) {
            return XPathExpr.this.resolver == null ? null : XPathExpr.this.resolver.lookupNamespaceURI(prefix);
         }

         public boolean handlesNullPrefixes() {
            return false;
         }
      }

      public class Result implements XPathResult {
         protected short resultType;
         protected double numberValue;
         protected String stringValue;
         protected boolean booleanValue;
         protected Node singleNodeValue;
         protected NodeList iterator;
         protected int iteratorPosition;

         public Result(Node n, short type) {
            this.resultType = type;
            this.singleNodeValue = n;
         }

         public Result(boolean b) throws TransformerException {
            this.resultType = 3;
            this.booleanValue = b;
         }

         public Result(double d) throws TransformerException {
            this.resultType = 1;
            this.numberValue = d;
         }

         public Result(String s) {
            this.resultType = 2;
            this.stringValue = s;
         }

         public Result(NodeList nl, short type) {
            this.resultType = type;
            this.iterator = nl;
         }

         public short getResultType() {
            return this.resultType;
         }

         public boolean getBooleanValue() {
            if (this.resultType != 3) {
               throw AbstractDocument.this.createXPathException((short)52, "xpath.invalid.result.type", new Object[]{Integer.valueOf(this.resultType)});
            } else {
               return this.booleanValue;
            }
         }

         public double getNumberValue() {
            if (this.resultType != 1) {
               throw AbstractDocument.this.createXPathException((short)52, "xpath.invalid.result.type", new Object[]{Integer.valueOf(this.resultType)});
            } else {
               return this.numberValue;
            }
         }

         public String getStringValue() {
            if (this.resultType != 2) {
               throw AbstractDocument.this.createXPathException((short)52, "xpath.invalid.result.type", new Object[]{Integer.valueOf(this.resultType)});
            } else {
               return this.stringValue;
            }
         }

         public Node getSingleNodeValue() {
            if (this.resultType != 8 && this.resultType != 9) {
               throw AbstractDocument.this.createXPathException((short)52, "xpath.invalid.result.type", new Object[]{Integer.valueOf(this.resultType)});
            } else {
               return this.singleNodeValue;
            }
         }

         public boolean getInvalidIteratorState() {
            return false;
         }

         public int getSnapshotLength() {
            if (this.resultType != 6 && this.resultType != 7) {
               throw AbstractDocument.this.createXPathException((short)52, "xpath.invalid.result.type", new Object[]{Integer.valueOf(this.resultType)});
            } else {
               return this.iterator.getLength();
            }
         }

         public Node iterateNext() {
            if (this.resultType != 4 && this.resultType != 5) {
               throw AbstractDocument.this.createXPathException((short)52, "xpath.invalid.result.type", new Object[]{Integer.valueOf(this.resultType)});
            } else {
               return this.iterator.item(this.iteratorPosition++);
            }
         }

         public Node snapshotItem(int i) {
            if (this.resultType != 6 && this.resultType != 7) {
               throw AbstractDocument.this.createXPathException((short)52, "xpath.invalid.result.type", new Object[]{Integer.valueOf(this.resultType)});
            } else {
               return this.iterator.item(i);
            }
         }
      }
   }

   protected class DocumentConfiguration implements DOMConfiguration {
      protected String[] booleanParamNames = new String[]{"canonical-form", "cdata-sections", "check-character-normalization", "comments", "datatype-normalization", "element-content-whitespace", "entities", "infoset", "namespaces", "namespace-declarations", "normalize-characters", "split-cdata-sections", "validate", "validate-if-schema", "well-formed"};
      protected boolean[] booleanParamValues = new boolean[]{false, true, false, true, false, false, true, false, true, true, false, true, false, false, true};
      protected boolean[] booleanParamReadOnly = new boolean[]{true, false, true, false, true, false, false, false, false, false, true, false, true, true, false};
      protected Map booleanParamIndexes = new HashMap();
      protected Object errorHandler;
      protected ParameterNameList paramNameList;

      protected DocumentConfiguration() {
         for(int i = 0; i < this.booleanParamNames.length; ++i) {
            this.booleanParamIndexes.put(this.booleanParamNames[i], i);
         }

      }

      public void setParameter(String name, Object value) {
         if ("error-handler".equals(name)) {
            if (value != null && !(value instanceof DOMErrorHandler)) {
               throw AbstractDocument.this.createDOMException((short)17, "domconfig.param.type", new Object[]{name});
            } else {
               this.errorHandler = value;
            }
         } else {
            Integer i = (Integer)this.booleanParamIndexes.get(name);
            if (i == null) {
               throw AbstractDocument.this.createDOMException((short)8, "domconfig.param.not.found", new Object[]{name});
            } else if (value == null) {
               throw AbstractDocument.this.createDOMException((short)9, "domconfig.param.value", new Object[]{name});
            } else if (!(value instanceof Boolean)) {
               throw AbstractDocument.this.createDOMException((short)17, "domconfig.param.type", new Object[]{name});
            } else {
               int index = i;
               boolean val = (Boolean)value;
               if (this.booleanParamReadOnly[index] && this.booleanParamValues[index] != val) {
                  throw AbstractDocument.this.createDOMException((short)9, "domconfig.param.value", new Object[]{name});
               } else {
                  this.booleanParamValues[index] = val;
                  if (name.equals("infoset")) {
                     this.setParameter("validate-if-schema", Boolean.FALSE);
                     this.setParameter("entities", Boolean.FALSE);
                     this.setParameter("datatype-normalization", Boolean.FALSE);
                     this.setParameter("cdata-sections", Boolean.FALSE);
                     this.setParameter("well-formed", Boolean.TRUE);
                     this.setParameter("element-content-whitespace", Boolean.TRUE);
                     this.setParameter("comments", Boolean.TRUE);
                     this.setParameter("namespaces", Boolean.TRUE);
                  }

               }
            }
         }
      }

      public Object getParameter(String name) {
         if ("error-handler".equals(name)) {
            return this.errorHandler;
         } else {
            Integer index = (Integer)this.booleanParamIndexes.get(name);
            if (index == null) {
               throw AbstractDocument.this.createDOMException((short)8, "domconfig.param.not.found", new Object[]{name});
            } else {
               return this.booleanParamValues[index] ? Boolean.TRUE : Boolean.FALSE;
            }
         }
      }

      public boolean getBooleanParameter(String name) {
         Boolean b = (Boolean)this.getParameter(name);
         return b;
      }

      public boolean canSetParameter(String name, Object value) {
         if (name.equals("error-handler")) {
            return value == null || value instanceof DOMErrorHandler;
         } else {
            Integer i = (Integer)this.booleanParamIndexes.get(name);
            if (i != null && value != null && value instanceof Boolean) {
               int index = i;
               boolean val = (Boolean)value;
               return !this.booleanParamReadOnly[index] || this.booleanParamValues[index] == val;
            } else {
               return false;
            }
         }
      }

      public DOMStringList getParameterNames() {
         if (this.paramNameList == null) {
            this.paramNameList = new ParameterNameList();
         }

         return this.paramNameList;
      }

      protected class ParameterNameList implements DOMStringList {
         public String item(int index) {
            if (index < 0) {
               return null;
            } else if (index < DocumentConfiguration.this.booleanParamNames.length) {
               return DocumentConfiguration.this.booleanParamNames[index];
            } else {
               return index == DocumentConfiguration.this.booleanParamNames.length ? "error-handler" : null;
            }
         }

         public int getLength() {
            return DocumentConfiguration.this.booleanParamNames.length + 1;
         }

         public boolean contains(String s) {
            if ("error-handler".equals(s)) {
               return true;
            } else {
               String[] var2 = DocumentConfiguration.this.booleanParamNames;
               int var3 = var2.length;

               for(int var4 = 0; var4 < var3; ++var4) {
                  String booleanParamName = var2[var4];
                  if (booleanParamName.equals(s)) {
                     return true;
                  }
               }

               return false;
            }
         }
      }
   }

   protected static class DocumentError implements DOMError {
      protected String type;
      protected short severity;
      protected String message;
      protected Node relatedNode;
      protected Object relatedException;
      protected DOMLocator domLocator;

      public DocumentError(String type, short severity, String message, Node relatedNode, Exception relatedException) {
         this.type = type;
         this.severity = severity;
         this.message = message;
         this.relatedNode = relatedNode;
         this.relatedException = relatedException;
      }

      public String getType() {
         return this.type;
      }

      public short getSeverity() {
         return this.severity;
      }

      public String getMessage() {
         return this.message;
      }

      public Object getRelatedData() {
         return this.relatedNode;
      }

      public Object getRelatedException() {
         return this.relatedException;
      }

      public DOMLocator getLocation() {
         if (this.domLocator == null) {
            this.domLocator = new ErrorLocation(this.relatedNode);
         }

         return this.domLocator;
      }

      protected static class ErrorLocation implements DOMLocator {
         protected Node node;

         public ErrorLocation(Node n) {
            this.node = n;
         }

         public int getLineNumber() {
            return -1;
         }

         public int getColumnNumber() {
            return -1;
         }

         public int getByteOffset() {
            return -1;
         }

         public int getUtf16Offset() {
            return -1;
         }

         public Node getRelatedNode() {
            return this.node;
         }

         public String getUri() {
            AbstractDocument doc = (AbstractDocument)this.node.getOwnerDocument();
            return doc.getDocumentURI();
         }
      }
   }

   protected class IdSoftRef extends CleanerThread.SoftReferenceCleared {
      String id;
      List list;

      IdSoftRef(Object o, String id) {
         super(o);
         this.id = id;
      }

      IdSoftRef(Object o, String id, List list) {
         super(o);
         this.id = id;
         this.list = list;
      }

      public void setList(List list) {
         this.list = list;
      }

      public void cleared() {
         if (AbstractDocument.this.elementsById != null) {
            synchronized(AbstractDocument.this.elementsById) {
               if (this.list != null) {
                  this.list.remove(this);
               } else {
                  Object o = AbstractDocument.this.elementsById.remove(this.id);
                  if (o != this) {
                     AbstractDocument.this.elementsById.put(this.id, o);
                  }
               }

            }
         }
      }
   }
}
