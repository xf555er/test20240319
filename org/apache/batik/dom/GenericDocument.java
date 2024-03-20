package org.apache.batik.dom;

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

public class GenericDocument extends AbstractDocument {
   protected static final String ATTR_ID = "id";
   protected boolean readonly;

   protected GenericDocument() {
   }

   public GenericDocument(DocumentType dt, DOMImplementation impl) {
      super(dt, impl);
   }

   public boolean isReadonly() {
      return this.readonly;
   }

   public void setReadonly(boolean v) {
      this.readonly = v;
   }

   public boolean isId(Attr node) {
      return node.getNamespaceURI() != null ? false : "id".equals(node.getNodeName());
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
      return new GenericProcessingInstruction(target, data, this);
   }

   public Attr createAttribute(String name) throws DOMException {
      return new GenericAttr(name.intern(), this);
   }

   public EntityReference createEntityReference(String name) throws DOMException {
      return new GenericEntityReference(name, this);
   }

   public Element createElementNS(String namespaceURI, String qualifiedName) throws DOMException {
      if (namespaceURI != null && namespaceURI.length() == 0) {
         namespaceURI = null;
      }

      return (Element)(namespaceURI == null ? new GenericElement(qualifiedName.intern(), this) : new GenericElementNS(namespaceURI.intern(), qualifiedName.intern(), this));
   }

   public Attr createAttributeNS(String namespaceURI, String qualifiedName) throws DOMException {
      if (namespaceURI != null && namespaceURI.length() == 0) {
         namespaceURI = null;
      }

      return (Attr)(namespaceURI == null ? new GenericAttr(qualifiedName.intern(), this) : new GenericAttrNS(namespaceURI.intern(), qualifiedName.intern(), this));
   }

   protected Node newNode() {
      return new GenericDocument();
   }
}
