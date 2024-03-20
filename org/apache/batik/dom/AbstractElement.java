package org.apache.batik.dom;

import java.io.Serializable;
import org.apache.batik.dom.events.DOMMutationEvent;
import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.w3c.dom.ElementTraversal;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.TypeInfo;

public abstract class AbstractElement extends AbstractParentChildNode implements Element, ElementTraversal {
   protected NamedNodeMap attributes;
   protected TypeInfo typeInfo;

   protected AbstractElement() {
   }

   protected AbstractElement(String name, AbstractDocument owner) {
      this.ownerDocument = owner;
      if (owner.getStrictErrorChecking() && !DOMUtilities.isValidName(name)) {
         throw this.createDOMException((short)5, "xml.name", new Object[]{name});
      }
   }

   public short getNodeType() {
      return 1;
   }

   public boolean hasAttributes() {
      return this.attributes != null && this.attributes.getLength() != 0;
   }

   public NamedNodeMap getAttributes() {
      return this.attributes == null ? (this.attributes = this.createAttributes()) : this.attributes;
   }

   public String getTagName() {
      return this.getNodeName();
   }

   public boolean hasAttribute(String name) {
      return this.attributes != null && this.attributes.getNamedItem(name) != null;
   }

   public String getAttribute(String name) {
      if (this.attributes == null) {
         return "";
      } else {
         Attr attr = (Attr)this.attributes.getNamedItem(name);
         return attr == null ? "" : attr.getValue();
      }
   }

   public void setAttribute(String name, String value) throws DOMException {
      if (this.attributes == null) {
         this.attributes = this.createAttributes();
      }

      Attr attr = this.getAttributeNode(name);
      if (attr == null) {
         attr = this.getOwnerDocument().createAttribute(name);
         attr.setValue(value);
         this.attributes.setNamedItem(attr);
      } else {
         attr.setValue(value);
      }

   }

   public void removeAttribute(String name) throws DOMException {
      if (this.hasAttribute(name)) {
         this.attributes.removeNamedItem(name);
      }
   }

   public Attr getAttributeNode(String name) {
      return this.attributes == null ? null : (Attr)this.attributes.getNamedItem(name);
   }

   public Attr setAttributeNode(Attr newAttr) throws DOMException {
      if (newAttr == null) {
         return null;
      } else {
         if (this.attributes == null) {
            this.attributes = this.createAttributes();
         }

         return (Attr)this.attributes.setNamedItemNS(newAttr);
      }
   }

   public Attr removeAttributeNode(Attr oldAttr) throws DOMException {
      if (oldAttr == null) {
         return null;
      } else if (this.attributes == null) {
         throw this.createDOMException((short)8, "attribute.missing", new Object[]{oldAttr.getName()});
      } else {
         String nsURI = oldAttr.getNamespaceURI();
         return (Attr)this.attributes.removeNamedItemNS(nsURI, nsURI == null ? oldAttr.getNodeName() : oldAttr.getLocalName());
      }
   }

   public void normalize() {
      super.normalize();
      if (this.attributes != null) {
         NamedNodeMap map = this.getAttributes();

         for(int i = map.getLength() - 1; i >= 0; --i) {
            map.item(i).normalize();
         }
      }

   }

   public boolean hasAttributeNS(String namespaceURI, String localName) {
      if (namespaceURI != null && namespaceURI.length() == 0) {
         namespaceURI = null;
      }

      return this.attributes != null && this.attributes.getNamedItemNS(namespaceURI, localName) != null;
   }

   public String getAttributeNS(String namespaceURI, String localName) {
      if (this.attributes == null) {
         return "";
      } else {
         if (namespaceURI != null && namespaceURI.length() == 0) {
            namespaceURI = null;
         }

         Attr attr = (Attr)this.attributes.getNamedItemNS(namespaceURI, localName);
         return attr == null ? "" : attr.getValue();
      }
   }

   public void setAttributeNS(String namespaceURI, String qualifiedName, String value) throws DOMException {
      if (this.attributes == null) {
         this.attributes = this.createAttributes();
      }

      if (namespaceURI != null && namespaceURI.length() == 0) {
         namespaceURI = null;
      }

      Attr attr = this.getAttributeNodeNS(namespaceURI, qualifiedName);
      if (attr == null) {
         attr = this.getOwnerDocument().createAttributeNS(namespaceURI, qualifiedName);
         attr.setValue(value);
         this.attributes.setNamedItemNS(attr);
      } else {
         attr.setValue(value);
      }

   }

   public void removeAttributeNS(String namespaceURI, String localName) throws DOMException {
      if (namespaceURI != null && namespaceURI.length() == 0) {
         namespaceURI = null;
      }

      if (this.hasAttributeNS(namespaceURI, localName)) {
         this.attributes.removeNamedItemNS(namespaceURI, localName);
      }
   }

   public Attr getAttributeNodeNS(String namespaceURI, String localName) {
      if (namespaceURI != null && namespaceURI.length() == 0) {
         namespaceURI = null;
      }

      return this.attributes == null ? null : (Attr)this.attributes.getNamedItemNS(namespaceURI, localName);
   }

   public Attr setAttributeNodeNS(Attr newAttr) throws DOMException {
      if (newAttr == null) {
         return null;
      } else {
         if (this.attributes == null) {
            this.attributes = this.createAttributes();
         }

         return (Attr)this.attributes.setNamedItemNS(newAttr);
      }
   }

   public TypeInfo getSchemaTypeInfo() {
      if (this.typeInfo == null) {
         this.typeInfo = new ElementTypeInfo();
      }

      return this.typeInfo;
   }

   public void setIdAttribute(String name, boolean isId) throws DOMException {
      AbstractAttr a = (AbstractAttr)this.getAttributeNode(name);
      if (a == null) {
         throw this.createDOMException((short)8, "attribute.missing", new Object[]{name});
      } else if (a.isReadonly()) {
         throw this.createDOMException((short)7, "readonly.node", new Object[]{name});
      } else {
         this.updateIdEntry(a, isId);
         a.isIdAttr = isId;
      }
   }

   public void setIdAttributeNS(String ns, String ln, boolean isId) throws DOMException {
      if (ns != null && ns.length() == 0) {
         ns = null;
      }

      AbstractAttr a = (AbstractAttr)this.getAttributeNodeNS(ns, ln);
      if (a == null) {
         throw this.createDOMException((short)8, "attribute.missing", new Object[]{ns, ln});
      } else if (a.isReadonly()) {
         throw this.createDOMException((short)7, "readonly.node", new Object[]{a.getNodeName()});
      } else {
         this.updateIdEntry(a, isId);
         a.isIdAttr = isId;
      }
   }

   public void setIdAttributeNode(Attr attr, boolean isId) throws DOMException {
      AbstractAttr a = (AbstractAttr)attr;
      if (a.isReadonly()) {
         throw this.createDOMException((short)7, "readonly.node", new Object[]{a.getNodeName()});
      } else {
         this.updateIdEntry(a, isId);
         a.isIdAttr = isId;
      }
   }

   private void updateIdEntry(AbstractAttr a, boolean isId) {
      if (a.isIdAttr) {
         if (!isId) {
            this.ownerDocument.removeIdEntry(this, a.getValue());
         }
      } else if (isId) {
         this.ownerDocument.addIdEntry(this, a.getValue());
      }

   }

   protected Attr getIdAttribute() {
      NamedNodeMap nnm = this.getAttributes();
      if (nnm == null) {
         return null;
      } else {
         int len = nnm.getLength();

         for(int i = 0; i < len; ++i) {
            AbstractAttr a = (AbstractAttr)nnm.item(i);
            if (a.isId()) {
               return a;
            }
         }

         return null;
      }
   }

   protected String getId() {
      Attr a = this.getIdAttribute();
      if (a != null) {
         String id = a.getNodeValue();
         if (id.length() > 0) {
            return id;
         }
      }

      return null;
   }

   protected void nodeAdded(Node node) {
      this.invalidateElementsByTagName(node);
   }

   protected void nodeToBeRemoved(Node node) {
      this.invalidateElementsByTagName(node);
   }

   private void invalidateElementsByTagName(Node node) {
      if (node.getNodeType() == 1) {
         AbstractDocument ad = this.getCurrentDocument();
         String ns = node.getNamespaceURI();
         String nm = node.getNodeName();
         String ln = ns == null ? node.getNodeName() : node.getLocalName();
         Node n = this;

         while(n != null) {
            switch (((Node)n).getNodeType()) {
               case 1:
               case 9:
                  AbstractParentNode.ElementsByTagName l = ad.getElementsByTagName((Node)n, nm);
                  if (l != null) {
                     l.invalidate();
                  }

                  l = ad.getElementsByTagName((Node)n, "*");
                  if (l != null) {
                     l.invalidate();
                  }

                  AbstractParentNode.ElementsByTagNameNS lns = ad.getElementsByTagNameNS((Node)n, ns, ln);
                  if (lns != null) {
                     lns.invalidate();
                  }

                  lns = ad.getElementsByTagNameNS((Node)n, "*", ln);
                  if (lns != null) {
                     lns.invalidate();
                  }

                  lns = ad.getElementsByTagNameNS((Node)n, ns, "*");
                  if (lns != null) {
                     lns.invalidate();
                  }

                  lns = ad.getElementsByTagNameNS((Node)n, "*", "*");
                  if (lns != null) {
                     lns.invalidate();
                  }
               default:
                  n = ((Node)n).getParentNode();
            }
         }

         for(Node c = node.getFirstChild(); c != null; c = c.getNextSibling()) {
            this.invalidateElementsByTagName(c);
         }

      }
   }

   protected NamedNodeMap createAttributes() {
      return new NamedNodeHashMap();
   }

   protected Node export(Node n, AbstractDocument d) {
      super.export(n, d);
      AbstractElement ae = (AbstractElement)n;
      if (this.attributes != null) {
         NamedNodeMap map = this.attributes;

         for(int i = map.getLength() - 1; i >= 0; --i) {
            AbstractAttr aa = (AbstractAttr)map.item(i);
            if (aa.getSpecified()) {
               Attr attr = (Attr)aa.deepExport(aa.cloneNode(false), d);
               if (aa instanceof AbstractAttrNS) {
                  ae.setAttributeNodeNS(attr);
               } else {
                  ae.setAttributeNode(attr);
               }
            }
         }
      }

      return n;
   }

   protected Node deepExport(Node n, AbstractDocument d) {
      super.deepExport(n, d);
      AbstractElement ae = (AbstractElement)n;
      if (this.attributes != null) {
         NamedNodeMap map = this.attributes;

         for(int i = map.getLength() - 1; i >= 0; --i) {
            AbstractAttr aa = (AbstractAttr)map.item(i);
            if (aa.getSpecified()) {
               Attr attr = (Attr)aa.deepExport(aa.cloneNode(false), d);
               if (aa instanceof AbstractAttrNS) {
                  ae.setAttributeNodeNS(attr);
               } else {
                  ae.setAttributeNode(attr);
               }
            }
         }
      }

      return n;
   }

   protected Node copyInto(Node n) {
      super.copyInto(n);
      AbstractElement ae = (AbstractElement)n;
      if (this.attributes != null) {
         NamedNodeMap map = this.attributes;

         for(int i = map.getLength() - 1; i >= 0; --i) {
            AbstractAttr aa = (AbstractAttr)map.item(i).cloneNode(true);
            if (aa instanceof AbstractAttrNS) {
               ae.setAttributeNodeNS(aa);
            } else {
               ae.setAttributeNode(aa);
            }
         }
      }

      return n;
   }

   protected Node deepCopyInto(Node n) {
      super.deepCopyInto(n);
      AbstractElement ae = (AbstractElement)n;
      if (this.attributes != null) {
         NamedNodeMap map = this.attributes;

         for(int i = map.getLength() - 1; i >= 0; --i) {
            AbstractAttr aa = (AbstractAttr)map.item(i).cloneNode(true);
            if (aa instanceof AbstractAttrNS) {
               ae.setAttributeNodeNS(aa);
            } else {
               ae.setAttributeNode(aa);
            }
         }
      }

      return n;
   }

   protected void checkChildType(Node n, boolean replace) {
      switch (n.getNodeType()) {
         case 1:
         case 3:
         case 4:
         case 5:
         case 7:
         case 8:
         case 11:
            return;
         case 2:
         case 6:
         case 9:
         case 10:
         default:
            throw this.createDOMException((short)3, "child.type", new Object[]{Integer.valueOf(this.getNodeType()), this.getNodeName(), Integer.valueOf(n.getNodeType()), n.getNodeName()});
      }
   }

   public void fireDOMAttrModifiedEvent(String name, Attr node, String oldv, String newv, short change) {
      switch (change) {
         case 1:
            if (node.isId()) {
               this.ownerDocument.updateIdEntry(this, oldv, newv);
            }

            this.attrModified(node, oldv, newv);
            break;
         case 2:
            if (node.isId()) {
               this.ownerDocument.addIdEntry(this, newv);
            }

            this.attrAdded(node, newv);
            break;
         default:
            if (node.isId()) {
               this.ownerDocument.removeIdEntry(this, oldv);
            }

            this.attrRemoved(node, oldv);
      }

      AbstractDocument doc = this.getCurrentDocument();
      if (doc.getEventsEnabled() && !oldv.equals(newv)) {
         DOMMutationEvent ev = (DOMMutationEvent)doc.createEvent("MutationEvents");
         ev.initMutationEventNS("http://www.w3.org/2001/xml-events", "DOMAttrModified", true, false, node, oldv, newv, name, change);
         this.dispatchEvent(ev);
      }

   }

   protected void attrAdded(Attr node, String newv) {
   }

   protected void attrModified(Attr node, String oldv, String newv) {
   }

   protected void attrRemoved(Attr node, String oldv) {
   }

   public Element getFirstElementChild() {
      for(Node n = this.getFirstChild(); n != null; n = n.getNextSibling()) {
         if (n.getNodeType() == 1) {
            return (Element)n;
         }
      }

      return null;
   }

   public Element getLastElementChild() {
      for(Node n = this.getLastChild(); n != null; n = n.getPreviousSibling()) {
         if (n.getNodeType() == 1) {
            return (Element)n;
         }
      }

      return null;
   }

   public Element getNextElementSibling() {
      for(Node n = this.getNextSibling(); n != null; n = n.getNextSibling()) {
         if (n.getNodeType() == 1) {
            return (Element)n;
         }
      }

      return null;
   }

   public Element getPreviousElementSibling() {
      Node n;
      for(n = this.getPreviousSibling(); n != null; n = n.getPreviousSibling()) {
         if (n.getNodeType() == 1) {
            return (Element)n;
         }
      }

      return (Element)n;
   }

   public int getChildElementCount() {
      this.getChildNodes();
      return this.childNodes.elementChildren;
   }

   public static class ElementTypeInfo implements TypeInfo {
      public String getTypeNamespace() {
         return null;
      }

      public String getTypeName() {
         return null;
      }

      public boolean isDerivedFrom(String ns, String name, int method) {
         return false;
      }
   }

   protected static class Entry implements Serializable {
      public int hash;
      public String namespaceURI;
      public String name;
      public Node value;
      public Entry next;

      public Entry(int hash, String ns, String nm, Node value, Entry next) {
         this.hash = hash;
         this.namespaceURI = ns;
         this.name = nm;
         this.value = value;
         this.next = next;
      }

      public boolean match(String ns, String nm) {
         if (this.namespaceURI != null) {
            if (!this.namespaceURI.equals(ns)) {
               return false;
            }
         } else if (ns != null) {
            return false;
         }

         return this.name.equals(nm);
      }
   }

   public class NamedNodeHashMap implements NamedNodeMap, Serializable {
      protected static final int INITIAL_CAPACITY = 3;
      protected Entry[] table = new Entry[3];
      protected int count;

      public Node getNamedItem(String name) {
         return name == null ? null : this.get((String)null, name);
      }

      public Node setNamedItem(Node arg) throws DOMException {
         if (arg == null) {
            return null;
         } else {
            this.checkNode(arg);
            return this.setNamedItem((String)null, arg.getNodeName(), arg);
         }
      }

      public Node removeNamedItem(String name) throws DOMException {
         return this.removeNamedItemNS((String)null, name);
      }

      public Node item(int index) {
         if (index >= 0 && index < this.count) {
            int j = 0;
            Entry[] var3 = this.table;
            int var4 = var3.length;

            for(int var5 = 0; var5 < var4; ++var5) {
               Entry aTable = var3[var5];
               Entry e = aTable;
               if (aTable != null) {
                  do {
                     if (j++ == index) {
                        return e.value;
                     }

                     e = e.next;
                  } while(e != null);
               }
            }

            return null;
         } else {
            return null;
         }
      }

      public int getLength() {
         return this.count;
      }

      public Node getNamedItemNS(String namespaceURI, String localName) {
         if (namespaceURI != null && namespaceURI.length() == 0) {
            namespaceURI = null;
         }

         return this.get(namespaceURI, localName);
      }

      public Node setNamedItemNS(Node arg) throws DOMException {
         if (arg == null) {
            return null;
         } else {
            String nsURI = arg.getNamespaceURI();
            return this.setNamedItem(nsURI, nsURI == null ? arg.getNodeName() : arg.getLocalName(), arg);
         }
      }

      public Node removeNamedItemNS(String namespaceURI, String localName) throws DOMException {
         if (AbstractElement.this.isReadonly()) {
            throw AbstractElement.this.createDOMException((short)7, "readonly.node.map", new Object[0]);
         } else if (localName == null) {
            throw AbstractElement.this.createDOMException((short)8, "attribute.missing", new Object[]{""});
         } else {
            if (namespaceURI != null && namespaceURI.length() == 0) {
               namespaceURI = null;
            }

            AbstractAttr n = (AbstractAttr)this.remove(namespaceURI, localName);
            if (n == null) {
               throw AbstractElement.this.createDOMException((short)8, "attribute.missing", new Object[]{localName});
            } else {
               n.setOwnerElement((AbstractElement)null);
               AbstractElement.this.fireDOMAttrModifiedEvent(n.getNodeName(), n, n.getNodeValue(), "", (short)3);
               return n;
            }
         }
      }

      public Node setNamedItem(String ns, String name, Node arg) throws DOMException {
         if (ns != null && ns.length() == 0) {
            ns = null;
         }

         ((AbstractAttr)arg).setOwnerElement(AbstractElement.this);
         AbstractAttr result = (AbstractAttr)this.put(ns, name, arg);
         if (result != null) {
            result.setOwnerElement((AbstractElement)null);
            AbstractElement.this.fireDOMAttrModifiedEvent(name, result, result.getNodeValue(), "", (short)3);
         }

         AbstractElement.this.fireDOMAttrModifiedEvent(name, (Attr)arg, "", arg.getNodeValue(), (short)2);
         return result;
      }

      protected void checkNode(Node arg) {
         if (AbstractElement.this.isReadonly()) {
            throw AbstractElement.this.createDOMException((short)7, "readonly.node.map", new Object[0]);
         } else if (AbstractElement.this.getOwnerDocument() != arg.getOwnerDocument()) {
            throw AbstractElement.this.createDOMException((short)4, "node.from.wrong.document", new Object[]{Integer.valueOf(arg.getNodeType()), arg.getNodeName()});
         } else if (arg.getNodeType() == 2 && ((Attr)arg).getOwnerElement() != null) {
            throw AbstractElement.this.createDOMException((short)4, "inuse.attribute", new Object[]{arg.getNodeName()});
         }
      }

      protected Node get(String ns, String nm) {
         int hash = this.hashCode(ns, nm) & Integer.MAX_VALUE;
         int index = hash % this.table.length;

         for(Entry e = this.table[index]; e != null; e = e.next) {
            if (e.hash == hash && e.match(ns, nm)) {
               return e.value;
            }
         }

         return null;
      }

      protected Node put(String ns, String nm, Node value) {
         int hash = this.hashCode(ns, nm) & Integer.MAX_VALUE;
         int index = hash % this.table.length;

         for(Entry e = this.table[index]; e != null; e = e.next) {
            if (e.hash == hash && e.match(ns, nm)) {
               Node old = e.value;
               e.value = value;
               return old;
            }
         }

         int len = this.table.length;
         if (this.count++ >= len - (len >> 2)) {
            this.rehash();
            index = hash % this.table.length;
         }

         Entry ex = new Entry(hash, ns, nm, value, this.table[index]);
         this.table[index] = ex;
         return null;
      }

      protected Node remove(String ns, String nm) {
         int hash = this.hashCode(ns, nm) & Integer.MAX_VALUE;
         int index = hash % this.table.length;
         Entry p = null;

         for(Entry e = this.table[index]; e != null; e = e.next) {
            if (e.hash == hash && e.match(ns, nm)) {
               Node result = e.value;
               if (p == null) {
                  this.table[index] = e.next;
               } else {
                  p.next = e.next;
               }

               --this.count;
               return result;
            }

            p = e;
         }

         return null;
      }

      protected void rehash() {
         Entry[] oldTable = this.table;
         this.table = new Entry[oldTable.length * 2 + 1];

         Entry e;
         int index;
         for(int i = oldTable.length - 1; i >= 0; --i) {
            for(Entry old = oldTable[i]; old != null; this.table[index] = e) {
               e = old;
               old = old.next;
               index = e.hash % this.table.length;
               e.next = this.table[index];
            }
         }

      }

      protected int hashCode(String ns, String nm) {
         int result = ns == null ? 0 : ns.hashCode();
         return result ^ nm.hashCode();
      }
   }
}
