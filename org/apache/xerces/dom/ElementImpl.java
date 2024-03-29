package org.apache.xerces.dom;

import org.apache.xerces.util.URI;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.ElementTraversal;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.TypeInfo;

public class ElementImpl extends ParentNode implements Element, ElementTraversal, TypeInfo {
   static final long serialVersionUID = 3717253516652722278L;
   protected String name;
   protected AttributeMap attributes;

   public ElementImpl(CoreDocumentImpl var1, String var2) {
      super(var1);
      this.name = var2;
      this.needsSyncData(true);
   }

   protected ElementImpl() {
   }

   void rename(String var1) {
      if (this.needsSyncData()) {
         this.synchronizeData();
      }

      if (this.ownerDocument.errorChecking) {
         int var2 = var1.indexOf(58);
         String var3;
         if (var2 != -1) {
            var3 = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NAMESPACE_ERR", (Object[])null);
            throw new DOMException((short)14, var3);
         }

         if (!CoreDocumentImpl.isXMLName(var1, this.ownerDocument.isXML11Version())) {
            var3 = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "INVALID_CHARACTER_ERR", (Object[])null);
            throw new DOMException((short)5, var3);
         }
      }

      this.name = var1;
      this.reconcileDefaultAttributes();
   }

   public short getNodeType() {
      return 1;
   }

   public String getNodeName() {
      if (this.needsSyncData()) {
         this.synchronizeData();
      }

      return this.name;
   }

   public NamedNodeMap getAttributes() {
      if (this.needsSyncData()) {
         this.synchronizeData();
      }

      if (this.attributes == null) {
         this.attributes = new AttributeMap(this, (NamedNodeMapImpl)null);
      }

      return this.attributes;
   }

   public Node cloneNode(boolean var1) {
      ElementImpl var2 = (ElementImpl)super.cloneNode(var1);
      if (this.attributes != null) {
         var2.attributes = (AttributeMap)this.attributes.cloneMap(var2);
      }

      return var2;
   }

   public String getBaseURI() {
      if (this.needsSyncData()) {
         this.synchronizeData();
      }

      if (this.attributes != null) {
         Attr var1 = this.getXMLBaseAttribute();
         if (var1 != null) {
            String var2 = var1.getNodeValue();
            if (var2.length() != 0) {
               try {
                  URI var3 = new URI(var2, true);
                  if (var3.isAbsoluteURI()) {
                     return var3.toString();
                  }

                  String var4 = this.ownerNode != null ? this.ownerNode.getBaseURI() : null;
                  if (var4 != null) {
                     try {
                        URI var5 = new URI(var4);
                        var3.absolutize(var5);
                        return var3.toString();
                     } catch (URI.MalformedURIException var6) {
                        return null;
                     }
                  }

                  return null;
               } catch (URI.MalformedURIException var7) {
                  return null;
               }
            }
         }
      }

      return this.ownerNode != null ? this.ownerNode.getBaseURI() : null;
   }

   protected Attr getXMLBaseAttribute() {
      return (Attr)this.attributes.getNamedItem("xml:base");
   }

   protected void setOwnerDocument(CoreDocumentImpl var1) {
      super.setOwnerDocument(var1);
      if (this.attributes != null) {
         this.attributes.setOwnerDocument(var1);
      }

   }

   public String getAttribute(String var1) {
      if (this.needsSyncData()) {
         this.synchronizeData();
      }

      if (this.attributes == null) {
         return "";
      } else {
         Attr var2 = (Attr)this.attributes.getNamedItem(var1);
         return var2 == null ? "" : var2.getValue();
      }
   }

   public Attr getAttributeNode(String var1) {
      if (this.needsSyncData()) {
         this.synchronizeData();
      }

      return this.attributes == null ? null : (Attr)this.attributes.getNamedItem(var1);
   }

   public NodeList getElementsByTagName(String var1) {
      return new DeepNodeListImpl(this, var1);
   }

   public String getTagName() {
      if (this.needsSyncData()) {
         this.synchronizeData();
      }

      return this.name;
   }

   public void normalize() {
      if (!this.isNormalized()) {
         if (this.needsSyncChildren()) {
            this.synchronizeChildren();
         }

         ChildNode var2;
         for(ChildNode var1 = this.firstChild; var1 != null; var1 = var2) {
            var2 = var1.nextSibling;
            if (var1.getNodeType() == 3) {
               if (var2 != null && var2.getNodeType() == 3) {
                  ((Text)var1).appendData(var2.getNodeValue());
                  this.removeChild(var2);
                  var2 = var1;
               } else if (var1.getNodeValue() == null || var1.getNodeValue().length() == 0) {
                  this.removeChild(var1);
               }
            } else if (var1.getNodeType() == 1) {
               var1.normalize();
            }
         }

         if (this.attributes != null) {
            for(int var3 = 0; var3 < this.attributes.getLength(); ++var3) {
               Node var4 = this.attributes.item(var3);
               var4.normalize();
            }
         }

         this.isNormalized(true);
      }
   }

   public void removeAttribute(String var1) {
      if (this.ownerDocument.errorChecking && this.isReadOnly()) {
         String var2 = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NO_MODIFICATION_ALLOWED_ERR", (Object[])null);
         throw new DOMException((short)7, var2);
      } else {
         if (this.needsSyncData()) {
            this.synchronizeData();
         }

         if (this.attributes != null) {
            this.attributes.safeRemoveNamedItem(var1);
         }
      }
   }

   public Attr removeAttributeNode(Attr var1) throws DOMException {
      String var2;
      if (this.ownerDocument.errorChecking && this.isReadOnly()) {
         var2 = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NO_MODIFICATION_ALLOWED_ERR", (Object[])null);
         throw new DOMException((short)7, var2);
      } else {
         if (this.needsSyncData()) {
            this.synchronizeData();
         }

         if (this.attributes == null) {
            var2 = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NOT_FOUND_ERR", (Object[])null);
            throw new DOMException((short)8, var2);
         } else {
            return (Attr)this.attributes.removeItem(var1, true);
         }
      }
   }

   public void setAttribute(String var1, String var2) {
      if (this.ownerDocument.errorChecking && this.isReadOnly()) {
         String var4 = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NO_MODIFICATION_ALLOWED_ERR", (Object[])null);
         throw new DOMException((short)7, var4);
      } else {
         if (this.needsSyncData()) {
            this.synchronizeData();
         }

         Attr var3 = this.getAttributeNode(var1);
         if (var3 == null) {
            var3 = this.getOwnerDocument().createAttribute(var1);
            if (this.attributes == null) {
               this.attributes = new AttributeMap(this, (NamedNodeMapImpl)null);
            }

            var3.setNodeValue(var2);
            this.attributes.setNamedItem(var3);
         } else {
            var3.setNodeValue(var2);
         }

      }
   }

   public Attr setAttributeNode(Attr var1) throws DOMException {
      if (this.needsSyncData()) {
         this.synchronizeData();
      }

      if (this.ownerDocument.errorChecking) {
         String var2;
         if (this.isReadOnly()) {
            var2 = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NO_MODIFICATION_ALLOWED_ERR", (Object[])null);
            throw new DOMException((short)7, var2);
         }

         if (var1.getOwnerDocument() != this.ownerDocument) {
            var2 = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "WRONG_DOCUMENT_ERR", (Object[])null);
            throw new DOMException((short)4, var2);
         }
      }

      if (this.attributes == null) {
         this.attributes = new AttributeMap(this, (NamedNodeMapImpl)null);
      }

      return (Attr)this.attributes.setNamedItem(var1);
   }

   public String getAttributeNS(String var1, String var2) {
      if (this.needsSyncData()) {
         this.synchronizeData();
      }

      if (this.attributes == null) {
         return "";
      } else {
         Attr var3 = (Attr)this.attributes.getNamedItemNS(var1, var2);
         return var3 == null ? "" : var3.getValue();
      }
   }

   public void setAttributeNS(String var1, String var2, String var3) {
      if (this.ownerDocument.errorChecking && this.isReadOnly()) {
         String var8 = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NO_MODIFICATION_ALLOWED_ERR", (Object[])null);
         throw new DOMException((short)7, var8);
      } else {
         if (this.needsSyncData()) {
            this.synchronizeData();
         }

         int var4 = var2.indexOf(58);
         String var5;
         String var6;
         if (var4 < 0) {
            var5 = null;
            var6 = var2;
         } else {
            var5 = var2.substring(0, var4);
            var6 = var2.substring(var4 + 1);
         }

         Attr var7 = this.getAttributeNodeNS(var1, var6);
         if (var7 == null) {
            var7 = this.getOwnerDocument().createAttributeNS(var1, var2);
            if (this.attributes == null) {
               this.attributes = new AttributeMap(this, (NamedNodeMapImpl)null);
            }

            var7.setNodeValue(var3);
            this.attributes.setNamedItemNS(var7);
         } else {
            if (var7 instanceof AttrNSImpl) {
               ((AttrNSImpl)var7).name = var5 != null ? var5 + ":" + var6 : var6;
            } else {
               var7 = ((CoreDocumentImpl)this.getOwnerDocument()).createAttributeNS(var1, var2, var6);
               this.attributes.setNamedItemNS(var7);
            }

            var7.setNodeValue(var3);
         }

      }
   }

   public void removeAttributeNS(String var1, String var2) {
      if (this.ownerDocument.errorChecking && this.isReadOnly()) {
         String var3 = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NO_MODIFICATION_ALLOWED_ERR", (Object[])null);
         throw new DOMException((short)7, var3);
      } else {
         if (this.needsSyncData()) {
            this.synchronizeData();
         }

         if (this.attributes != null) {
            this.attributes.safeRemoveNamedItemNS(var1, var2);
         }
      }
   }

   public Attr getAttributeNodeNS(String var1, String var2) {
      if (this.needsSyncData()) {
         this.synchronizeData();
      }

      return this.attributes == null ? null : (Attr)this.attributes.getNamedItemNS(var1, var2);
   }

   public Attr setAttributeNodeNS(Attr var1) throws DOMException {
      if (this.needsSyncData()) {
         this.synchronizeData();
      }

      if (this.ownerDocument.errorChecking) {
         String var2;
         if (this.isReadOnly()) {
            var2 = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NO_MODIFICATION_ALLOWED_ERR", (Object[])null);
            throw new DOMException((short)7, var2);
         }

         if (var1.getOwnerDocument() != this.ownerDocument) {
            var2 = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "WRONG_DOCUMENT_ERR", (Object[])null);
            throw new DOMException((short)4, var2);
         }
      }

      if (this.attributes == null) {
         this.attributes = new AttributeMap(this, (NamedNodeMapImpl)null);
      }

      return (Attr)this.attributes.setNamedItemNS(var1);
   }

   protected int setXercesAttributeNode(Attr var1) {
      if (this.needsSyncData()) {
         this.synchronizeData();
      }

      if (this.attributes == null) {
         this.attributes = new AttributeMap(this, (NamedNodeMapImpl)null);
      }

      return this.attributes.addItem(var1);
   }

   protected int getXercesAttribute(String var1, String var2) {
      if (this.needsSyncData()) {
         this.synchronizeData();
      }

      return this.attributes == null ? -1 : this.attributes.getNamedItemIndex(var1, var2);
   }

   public boolean hasAttributes() {
      if (this.needsSyncData()) {
         this.synchronizeData();
      }

      return this.attributes != null && this.attributes.getLength() != 0;
   }

   public boolean hasAttribute(String var1) {
      return this.getAttributeNode(var1) != null;
   }

   public boolean hasAttributeNS(String var1, String var2) {
      return this.getAttributeNodeNS(var1, var2) != null;
   }

   public NodeList getElementsByTagNameNS(String var1, String var2) {
      return new DeepNodeListImpl(this, var1, var2);
   }

   public boolean isEqualNode(Node var1) {
      if (!super.isEqualNode(var1)) {
         return false;
      } else {
         boolean var2 = this.hasAttributes();
         if (var2 != ((Element)var1).hasAttributes()) {
            return false;
         } else {
            if (var2) {
               NamedNodeMap var3 = this.getAttributes();
               NamedNodeMap var4 = ((Element)var1).getAttributes();
               int var5 = var3.getLength();
               if (var5 != var4.getLength()) {
                  return false;
               }

               for(int var6 = 0; var6 < var5; ++var6) {
                  Node var7 = var3.item(var6);
                  Node var8;
                  if (var7.getLocalName() == null) {
                     var8 = var4.getNamedItem(var7.getNodeName());
                     if (var8 == null || !((NodeImpl)var7).isEqualNode(var8)) {
                        return false;
                     }
                  } else {
                     var8 = var4.getNamedItemNS(var7.getNamespaceURI(), var7.getLocalName());
                     if (var8 == null || !((NodeImpl)var7).isEqualNode(var8)) {
                        return false;
                     }
                  }
               }
            }

            return true;
         }
      }
   }

   public void setIdAttributeNode(Attr var1, boolean var2) {
      if (this.needsSyncData()) {
         this.synchronizeData();
      }

      if (this.ownerDocument.errorChecking) {
         String var3;
         if (this.isReadOnly()) {
            var3 = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NO_MODIFICATION_ALLOWED_ERR", (Object[])null);
            throw new DOMException((short)7, var3);
         }

         if (var1.getOwnerElement() != this) {
            var3 = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NOT_FOUND_ERR", (Object[])null);
            throw new DOMException((short)8, var3);
         }
      }

      ((AttrImpl)var1).isIdAttribute(var2);
      if (!var2) {
         this.ownerDocument.removeIdentifier(var1.getValue());
      } else {
         this.ownerDocument.putIdentifier(var1.getValue(), this);
      }

   }

   public void setIdAttribute(String var1, boolean var2) {
      if (this.needsSyncData()) {
         this.synchronizeData();
      }

      Attr var3 = this.getAttributeNode(var1);
      String var4;
      if (var3 == null) {
         var4 = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NOT_FOUND_ERR", (Object[])null);
         throw new DOMException((short)8, var4);
      } else {
         if (this.ownerDocument.errorChecking) {
            if (this.isReadOnly()) {
               var4 = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NO_MODIFICATION_ALLOWED_ERR", (Object[])null);
               throw new DOMException((short)7, var4);
            }

            if (var3.getOwnerElement() != this) {
               var4 = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NOT_FOUND_ERR", (Object[])null);
               throw new DOMException((short)8, var4);
            }
         }

         ((AttrImpl)var3).isIdAttribute(var2);
         if (!var2) {
            this.ownerDocument.removeIdentifier(var3.getValue());
         } else {
            this.ownerDocument.putIdentifier(var3.getValue(), this);
         }

      }
   }

   public void setIdAttributeNS(String var1, String var2, boolean var3) {
      if (this.needsSyncData()) {
         this.synchronizeData();
      }

      Attr var4 = this.getAttributeNodeNS(var1, var2);
      String var5;
      if (var4 == null) {
         var5 = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NOT_FOUND_ERR", (Object[])null);
         throw new DOMException((short)8, var5);
      } else {
         if (this.ownerDocument.errorChecking) {
            if (this.isReadOnly()) {
               var5 = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NO_MODIFICATION_ALLOWED_ERR", (Object[])null);
               throw new DOMException((short)7, var5);
            }

            if (var4.getOwnerElement() != this) {
               var5 = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NOT_FOUND_ERR", (Object[])null);
               throw new DOMException((short)8, var5);
            }
         }

         ((AttrImpl)var4).isIdAttribute(var3);
         if (!var3) {
            this.ownerDocument.removeIdentifier(var4.getValue());
         } else {
            this.ownerDocument.putIdentifier(var4.getValue(), this);
         }

      }
   }

   public String getTypeName() {
      return null;
   }

   public String getTypeNamespace() {
      return null;
   }

   public boolean isDerivedFrom(String var1, String var2, int var3) {
      return false;
   }

   public TypeInfo getSchemaTypeInfo() {
      if (this.needsSyncData()) {
         this.synchronizeData();
      }

      return this;
   }

   public void setReadOnly(boolean var1, boolean var2) {
      super.setReadOnly(var1, var2);
      if (this.attributes != null) {
         this.attributes.setReadOnly(var1, true);
      }

   }

   protected void synchronizeData() {
      this.needsSyncData(false);
      boolean var1 = this.ownerDocument.getMutationEvents();
      this.ownerDocument.setMutationEvents(false);
      this.setupDefaultAttributes();
      this.ownerDocument.setMutationEvents(var1);
   }

   void moveSpecifiedAttributes(ElementImpl var1) {
      if (this.needsSyncData()) {
         this.synchronizeData();
      }

      if (var1.hasAttributes()) {
         if (this.attributes == null) {
            this.attributes = new AttributeMap(this, (NamedNodeMapImpl)null);
         }

         this.attributes.moveSpecifiedAttributes(var1.attributes);
      }

   }

   protected void setupDefaultAttributes() {
      NamedNodeMapImpl var1 = this.getDefaultAttributes();
      if (var1 != null) {
         this.attributes = new AttributeMap(this, var1);
      }

   }

   protected void reconcileDefaultAttributes() {
      if (this.attributes != null) {
         NamedNodeMapImpl var1 = this.getDefaultAttributes();
         this.attributes.reconcileDefaults(var1);
      }

   }

   protected NamedNodeMapImpl getDefaultAttributes() {
      DocumentTypeImpl var1 = (DocumentTypeImpl)this.ownerDocument.getDoctype();
      if (var1 == null) {
         return null;
      } else {
         ElementDefinitionImpl var2 = (ElementDefinitionImpl)var1.getElements().getNamedItem(this.getNodeName());
         return var2 == null ? null : (NamedNodeMapImpl)var2.getAttributes();
      }
   }

   public final int getChildElementCount() {
      int var1 = 0;

      for(Element var2 = this.getFirstElementChild(); var2 != null; var2 = ((ElementImpl)var2).getNextElementSibling()) {
         ++var1;
      }

      return var1;
   }

   public final Element getFirstElementChild() {
      Node var1 = this.getFirstChild();

      while(var1 != null) {
         switch (var1.getNodeType()) {
            case 1:
               return (Element)var1;
            case 5:
               Element var2 = this.getFirstElementChild(var1);
               if (var2 != null) {
                  return var2;
               }
            default:
               var1 = var1.getNextSibling();
         }
      }

      return null;
   }

   public final Element getLastElementChild() {
      Node var1 = this.getLastChild();

      while(var1 != null) {
         switch (var1.getNodeType()) {
            case 1:
               return (Element)var1;
            case 5:
               Element var2 = this.getLastElementChild(var1);
               if (var2 != null) {
                  return var2;
               }
            default:
               var1 = var1.getPreviousSibling();
         }
      }

      return null;
   }

   public final Element getNextElementSibling() {
      Node var1 = this.getNextLogicalSibling(this);

      while(var1 != null) {
         switch (var1.getNodeType()) {
            case 1:
               return (Element)var1;
            case 5:
               Element var2 = this.getFirstElementChild(var1);
               if (var2 != null) {
                  return var2;
               }
            default:
               var1 = this.getNextLogicalSibling(var1);
         }
      }

      return null;
   }

   public final Element getPreviousElementSibling() {
      Node var1 = this.getPreviousLogicalSibling(this);

      while(var1 != null) {
         switch (var1.getNodeType()) {
            case 1:
               return (Element)var1;
            case 5:
               Element var2 = this.getLastElementChild(var1);
               if (var2 != null) {
                  return var2;
               }
            default:
               var1 = this.getPreviousLogicalSibling(var1);
         }
      }

      return null;
   }

   private Element getFirstElementChild(Node var1) {
      Node var3;
      for(Node var2 = var1; var1 != null; var1 = var3) {
         if (var1.getNodeType() == 1) {
            return (Element)var1;
         }

         var3 = var1.getFirstChild();

         while(var3 == null && var2 != var1) {
            var3 = var1.getNextSibling();
            if (var3 == null) {
               var1 = var1.getParentNode();
               if (var1 == null || var2 == var1) {
                  return null;
               }
            }
         }
      }

      return null;
   }

   private Element getLastElementChild(Node var1) {
      Node var3;
      for(Node var2 = var1; var1 != null; var1 = var3) {
         if (var1.getNodeType() == 1) {
            return (Element)var1;
         }

         var3 = var1.getLastChild();

         while(var3 == null && var2 != var1) {
            var3 = var1.getPreviousSibling();
            if (var3 == null) {
               var1 = var1.getParentNode();
               if (var1 == null || var2 == var1) {
                  return null;
               }
            }
         }
      }

      return null;
   }

   private Node getNextLogicalSibling(Node var1) {
      Node var2 = var1.getNextSibling();
      if (var2 == null) {
         for(Node var3 = var1.getParentNode(); var3 != null && var3.getNodeType() == 5; var3 = var3.getParentNode()) {
            var2 = var3.getNextSibling();
            if (var2 != null) {
               break;
            }
         }
      }

      return var2;
   }

   private Node getPreviousLogicalSibling(Node var1) {
      Node var2 = var1.getPreviousSibling();
      if (var2 == null) {
         for(Node var3 = var1.getParentNode(); var3 != null && var3.getNodeType() == 5; var3 = var3.getParentNode()) {
            var2 = var3.getPreviousSibling();
            if (var2 != null) {
               break;
            }
         }
      }

      return var2;
   }
}
