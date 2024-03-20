package org.apache.fop.fo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.extensions.ExtensionAttachment;
import org.apache.fop.fo.flow.Marker;
import org.apache.fop.fo.pagination.PageSequence;
import org.apache.fop.fo.properties.Property;
import org.apache.fop.fo.properties.PropertyMaker;
import org.apache.xmlgraphics.util.QName;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;

public abstract class FObj extends FONode implements Constants {
   private static final PropertyMaker[] PROPERTY_LIST_TABLE = FOPropertyMapping.getGenericMappings();
   protected FONode firstChild;
   protected FONode lastChild;
   private List extensionAttachments;
   private Map foreignAttributes;
   private boolean isOutOfLineFODescendant;
   private Map markers;
   private int bidiLevel = -1;
   private String id;
   private String layer;
   private boolean forceKeepTogether;

   public FObj(FONode parent) {
      super(parent);
      if (parent != null && parent instanceof FObj) {
         if (((FObj)parent).getIsOutOfLineFODescendant()) {
            this.isOutOfLineFODescendant = true;
         } else {
            int foID = this.getNameId();
            if (foID == 15 || foID == 24 || foID == 25) {
               this.isOutOfLineFODescendant = true;
            }
         }
      }

   }

   public FONode clone(FONode parent, boolean removeChildren) throws FOPException {
      FObj fobj = (FObj)super.clone(parent, removeChildren);
      if (removeChildren) {
         fobj.firstChild = null;
      }

      return fobj;
   }

   public static PropertyMaker getPropertyMakerFor(int propId) {
      return PROPERTY_LIST_TABLE[propId];
   }

   public void processNode(String elementName, Locator locator, Attributes attlist, PropertyList pList) throws FOPException {
      this.setLocator(locator);
      pList.addAttributesToList(attlist);
      if (!this.inMarker() || "marker".equals(elementName)) {
         this.bind(pList);
      }

      this.warnOnUnknownProperties(attlist, elementName, pList);
   }

   private void warnOnUnknownProperties(Attributes attlist, String objName, PropertyList propertyList) throws FOPException {
      Map unknowns = propertyList.getUnknownPropertyValues();
      Iterator var5 = unknowns.entrySet().iterator();

      while(var5.hasNext()) {
         Map.Entry entry = (Map.Entry)var5.next();
         FOValidationEventProducer producer = FOValidationEventProducer.Provider.get(this.getUserAgent().getEventBroadcaster());
         producer.warnOnInvalidPropertyValue(this, objName, this.getAttributeNameForValue(attlist, (Property)entry.getValue(), propertyList), (String)entry.getKey(), (PropertyException)null, this.getLocator());
      }

   }

   private String getAttributeNameForValue(Attributes attList, Property value, PropertyList propertyList) throws FOPException {
      for(int i = 0; i < attList.getLength(); ++i) {
         String attributeName = attList.getQName(i);
         String attributeValue = attList.getValue(i);
         Property prop = propertyList.getPropertyForAttribute(attList, attributeName, attributeValue);
         if (prop != null && prop.equals(value)) {
            return attributeName;
         }
      }

      return "unknown";
   }

   protected PropertyList createPropertyList(PropertyList parent, FOEventHandler foEventHandler) throws FOPException {
      return this.getBuilderContext().getPropertyListMaker().make(this, parent);
   }

   public void bind(PropertyList pList) throws FOPException {
      this.id = pList.get(122).getString();
      this.layer = pList.get(290).getString();
   }

   public void startOfNode() throws FOPException {
      if (this.id != null) {
         this.checkId(this.id);
      }

      PageSequence pageSequence = this.getRoot().getLastPageSequence();
      if (pageSequence != null && pageSequence.hasChangeBars()) {
         this.startOfNodeChangeBarList = pageSequence.getClonedChangeBarList();
      }

   }

   public void endOfNode() throws FOPException {
      List endOfNodeChangeBarList = null;
      PageSequence pageSequence = this.getRoot().getLastPageSequence();
      if (pageSequence != null) {
         endOfNodeChangeBarList = pageSequence.getClonedChangeBarList();
      }

      if (this.startOfNodeChangeBarList != null && endOfNodeChangeBarList != null) {
         this.nodeChangeBarList = new LinkedList(endOfNodeChangeBarList);
         this.nodeChangeBarList.retainAll(this.startOfNodeChangeBarList);
         if (this.nodeChangeBarList.isEmpty()) {
            this.nodeChangeBarList = null;
         }

         this.startOfNodeChangeBarList = null;
      }

      super.endOfNode();
   }

   private void checkId(String id) throws ValidationException {
      if (!this.inMarker() && !id.equals("")) {
         Set idrefs = this.getBuilderContext().getIDReferences();
         if (!idrefs.contains(id)) {
            idrefs.add(id);
         } else {
            this.getFOValidationEventProducer().idNotUnique(this, this.getName(), id, true, this.locator);
         }
      }

   }

   boolean getIsOutOfLineFODescendant() {
      return this.isOutOfLineFODescendant;
   }

   protected void addChildNode(FONode child) throws FOPException {
      if (child.getNameId() == 44) {
         this.addMarker((Marker)child);
      } else {
         ExtensionAttachment attachment = child.getExtensionAttachment();
         if (attachment != null) {
            this.addExtensionAttachment(attachment);
         } else if (this.firstChild == null) {
            this.firstChild = child;
            this.lastChild = child;
         } else if (this.lastChild == null) {
            FONode prevChild;
            for(prevChild = this.firstChild; prevChild.siblings != null && prevChild.siblings[1] != null; prevChild = prevChild.siblings[1]) {
            }

            FONode.attachSiblings(prevChild, child);
         } else {
            FONode.attachSiblings(this.lastChild, child);
            this.lastChild = child;
         }
      }

   }

   protected static void addChildTo(FONode child, FONode parent) throws FOPException {
      parent.addChildNode(child);
   }

   public void removeChild(FONode child) {
      FONode nextChild = null;
      if (child.siblings != null) {
         nextChild = child.siblings[1];
      }

      if (child == this.firstChild) {
         this.firstChild = nextChild;
         if (this.firstChild != null) {
            this.firstChild.siblings[0] = null;
         }
      } else if (child.siblings != null) {
         FONode prevChild = child.siblings[0];
         prevChild.siblings[1] = nextChild;
         if (nextChild != null) {
            nextChild.siblings[0] = prevChild;
         }
      }

      if (child == this.lastChild) {
         if (child.siblings != null) {
            this.lastChild = this.siblings[0];
         } else {
            this.lastChild = null;
         }
      }

   }

   public FObj findNearestAncestorFObj() {
      FONode par;
      for(par = this.parent; par != null && !(par instanceof FObj); par = par.parent) {
      }

      return (FObj)par;
   }

   public boolean generatesReferenceAreas() {
      return false;
   }

   public FONode.FONodeIterator getChildNodes() {
      return this.hasChildren() ? new FObjIterator(this) : null;
   }

   public boolean hasChildren() {
      return this.firstChild != null;
   }

   public FONode.FONodeIterator getChildNodes(FONode childNode) {
      FONode.FONodeIterator it = this.getChildNodes();
      if (it == null) {
         return null;
      } else if (this.firstChild == childNode) {
         return it;
      } else {
         while(it.hasNext() && it.next().siblings[1] != childNode) {
         }

         return it.hasNext() ? it : null;
      }
   }

   void notifyChildRemoval(FONode node) {
   }

   protected void addMarker(Marker marker) {
      String mcname = marker.getMarkerClassName();
      if (this.firstChild != null) {
         FONode.FONodeIterator iter = this.getChildNodes();

         while(iter.hasNext()) {
            FONode node = iter.next();
            if (node instanceof FObj || node instanceof FOText && ((FOText)node).willCreateArea()) {
               this.getFOValidationEventProducer().markerNotInitialChild(this, this.getName(), mcname, this.locator);
               return;
            }

            if (node instanceof FOText) {
               iter.remove();
               this.notifyChildRemoval(node);
            }
         }
      }

      if (this.markers == null) {
         this.markers = new HashMap();
      }

      if (!this.markers.containsKey(mcname)) {
         this.markers.put(mcname, marker);
      } else {
         this.getFOValidationEventProducer().markerNotUniqueForSameParent(this, this.getName(), mcname, this.locator);
      }

   }

   public boolean hasMarkers() {
      return this.markers != null && !this.markers.isEmpty();
   }

   public Map getMarkers() {
      return this.markers;
   }

   protected String getContextInfoAlt() {
      StringBuilder sb = new StringBuilder();
      if (this.getLocalName() != null) {
         sb.append(this.getName());
         sb.append(", ");
      }

      if (this.hasId()) {
         sb.append("id=").append(this.getId());
         return sb.toString();
      } else {
         String s = this.gatherContextInfo();
         if (s != null) {
            sb.append("\"");
            if (s.length() < 32) {
               sb.append(s);
            } else {
               sb.append(s.substring(0, 32));
               sb.append("...");
            }

            sb.append("\"");
            return sb.toString();
         } else {
            return null;
         }
      }
   }

   protected String gatherContextInfo() {
      if (this.getLocator() != null) {
         return super.gatherContextInfo();
      } else {
         FONode.FONodeIterator iter = this.getChildNodes();
         if (iter == null) {
            return null;
         } else {
            StringBuilder sb = new StringBuilder();

            while(iter.hasNext()) {
               FONode node = iter.next();
               String s = node.gatherContextInfo();
               if (s != null) {
                  if (sb.length() > 0) {
                     sb.append(", ");
                  }

                  sb.append(s);
               }
            }

            return sb.length() > 0 ? sb.toString() : null;
         }
      }
   }

   protected boolean isBlockItem(String nsURI, String lName) {
      return "http://www.w3.org/1999/XSL/Format".equals(nsURI) && ("block".equals(lName) || "table".equals(lName) || "table-and-caption".equals(lName) || "block-container".equals(lName) || "list-block".equals(lName) || "float".equals(lName) || this.isNeutralItem(nsURI, lName));
   }

   protected boolean isInlineItem(String nsURI, String lName) {
      return "http://www.w3.org/1999/XSL/Format".equals(nsURI) && ("bidi-override".equals(lName) || "change-bar-begin".equals(lName) || "change-bar-end".equals(lName) || "character".equals(lName) || "external-graphic".equals(lName) || "instream-foreign-object".equals(lName) || "inline".equals(lName) || "inline-container".equals(lName) || "leader".equals(lName) || "page-number".equals(lName) || "page-number-citation".equals(lName) || "page-number-citation-last".equals(lName) || "basic-link".equals(lName) || "multi-toggle".equals(lName) && (this.getNameId() == 45 || this.findAncestor(45) > 0) || "footnote".equals(lName) && !this.isOutOfLineFODescendant || this.isNeutralItem(nsURI, lName));
   }

   protected boolean isBlockOrInlineItem(String nsURI, String lName) {
      return this.isBlockItem(nsURI, lName) || this.isInlineItem(nsURI, lName);
   }

   protected boolean isNeutralItem(String nsURI, String lName) {
      return "http://www.w3.org/1999/XSL/Format".equals(nsURI) && ("multi-switch".equals(lName) || "multi-properties".equals(lName) || "wrapper".equals(lName) || !this.isOutOfLineFODescendant && "float".equals(lName) || "retrieve-marker".equals(lName) || "retrieve-table-marker".equals(lName));
   }

   protected int findAncestor(int ancestorID) {
      int found = 1;

      for(FONode temp = this.getParent(); temp != null; temp = temp.getParent()) {
         if (temp.getNameId() == ancestorID) {
            return found;
         }

         ++found;
      }

      return -1;
   }

   public void clearChildNodes() {
      this.firstChild = null;
   }

   public String getId() {
      return this.id;
   }

   public boolean hasId() {
      return this.id != null && this.id.length() > 0;
   }

   public String getLayer() {
      return this.layer;
   }

   public boolean hasLayer() {
      return this.layer != null && this.layer.length() > 0;
   }

   public String getNamespaceURI() {
      return "http://www.w3.org/1999/XSL/Format";
   }

   public String getNormalNamespacePrefix() {
      return "fo";
   }

   public boolean isBidiRangeBlockItem() {
      String ns = this.getNamespaceURI();
      String ln = this.getLocalName();
      return !this.isNeutralItem(ns, ln) && this.isBlockItem(ns, ln);
   }

   public void setBidiLevel(int bidiLevel) {
      assert bidiLevel >= 0;

      if (this.bidiLevel < 0 || bidiLevel < this.bidiLevel) {
         this.bidiLevel = bidiLevel;
         if (this.parent != null && !this.isBidiPropagationBoundary()) {
            FObj foParent = (FObj)this.parent;
            int parentBidiLevel = foParent.getBidiLevel();
            if (parentBidiLevel < 0 || bidiLevel < parentBidiLevel) {
               foParent.setBidiLevel(bidiLevel);
            }
         }
      }

   }

   public int getBidiLevel() {
      return this.bidiLevel;
   }

   public int getBidiLevelRecursive() {
      for(FONode fn = this; fn != null; fn = ((FONode)fn).getParent()) {
         if (fn instanceof FObj) {
            int level = ((FObj)fn).getBidiLevel();
            if (level >= 0) {
               return level;
            }
         }

         if (this.isBidiInheritanceBoundary()) {
            break;
         }
      }

      return -1;
   }

   protected boolean isBidiBoundary(boolean propagate) {
      return false;
   }

   private boolean isBidiInheritanceBoundary() {
      return this.isBidiBoundary(false);
   }

   private boolean isBidiPropagationBoundary() {
      return this.isBidiBoundary(true);
   }

   void addExtensionAttachment(ExtensionAttachment attachment) {
      if (attachment == null) {
         throw new NullPointerException("Parameter attachment must not be null");
      } else {
         if (this.extensionAttachments == null) {
            this.extensionAttachments = new ArrayList();
         }

         if (log.isDebugEnabled()) {
            log.debug("ExtensionAttachment of category " + attachment.getCategory() + " added to " + this.getName() + ": " + attachment);
         }

         this.extensionAttachments.add(attachment);
      }
   }

   public List getExtensionAttachments() {
      return this.extensionAttachments == null ? Collections.EMPTY_LIST : this.extensionAttachments;
   }

   public boolean hasExtensionAttachments() {
      return this.extensionAttachments != null;
   }

   public void addForeignAttribute(QName attributeName, String value) {
      if (attributeName == null) {
         throw new NullPointerException("Parameter attributeName must not be null");
      } else {
         if (this.foreignAttributes == null) {
            this.foreignAttributes = new HashMap();
         }

         this.foreignAttributes.put(attributeName, value);
      }
   }

   public Map getForeignAttributes() {
      return this.foreignAttributes == null ? Collections.EMPTY_MAP : this.foreignAttributes;
   }

   public String toString() {
      return super.toString() + "[@id=" + this.id + "]";
   }

   public boolean isForceKeepTogether() {
      return this.forceKeepTogether;
   }

   public void setForceKeepTogether(boolean b) {
      this.forceKeepTogether = b;
   }

   public static class FObjIterator implements FONode.FONodeIterator {
      private static final int F_NONE_ALLOWED = 0;
      private static final int F_SET_ALLOWED = 1;
      private static final int F_REMOVE_ALLOWED = 2;
      private FONode currentNode;
      private final FObj parentNode;
      private int currentIndex;
      private int flags = 0;

      FObjIterator(FObj parent) {
         this.parentNode = parent;
         this.currentNode = parent.firstChild;
         this.currentIndex = 0;
         this.flags = 0;
      }

      public FObj parent() {
         return this.parentNode;
      }

      public FONode next() {
         if (this.currentNode == null) {
            throw new NoSuchElementException();
         } else {
            if (this.currentIndex != 0) {
               if (this.currentNode.siblings == null || this.currentNode.siblings[1] == null) {
                  throw new NoSuchElementException();
               }

               this.currentNode = this.currentNode.siblings[1];
            }

            ++this.currentIndex;
            this.flags |= 3;
            return this.currentNode;
         }
      }

      public FONode previous() {
         if (this.currentNode.siblings != null && this.currentNode.siblings[0] != null) {
            --this.currentIndex;
            this.currentNode = this.currentNode.siblings[0];
            this.flags |= 3;
            return this.currentNode;
         } else {
            throw new NoSuchElementException();
         }
      }

      public void set(FONode newNode) {
         if ((this.flags & 1) == 1) {
            if (this.currentNode == this.parentNode.firstChild) {
               this.parentNode.firstChild = newNode;
            } else {
               FONode.attachSiblings(this.currentNode.siblings[0], newNode);
            }

            if (this.currentNode.siblings != null && this.currentNode.siblings[1] != null) {
               FONode.attachSiblings(newNode, this.currentNode.siblings[1]);
            }

            if (this.currentNode == this.parentNode.lastChild) {
               this.parentNode.lastChild = newNode;
            }

         } else {
            throw new IllegalStateException();
         }
      }

      public void add(FONode newNode) {
         if (this.currentIndex == -1) {
            if (this.currentNode != null) {
               FONode.attachSiblings(newNode, this.currentNode);
            }

            this.parentNode.firstChild = newNode;
            this.currentIndex = 0;
            this.currentNode = newNode;
            if (this.parentNode.lastChild == null) {
               this.parentNode.lastChild = newNode;
            }
         } else {
            if (this.currentNode.siblings != null && this.currentNode.siblings[1] != null) {
               FONode.attachSiblings(newNode, this.currentNode.siblings[1]);
            }

            FONode.attachSiblings(this.currentNode, newNode);
            if (this.currentNode == this.parentNode.lastChild) {
               this.parentNode.lastChild = newNode;
            }
         }

         this.flags &= 0;
      }

      public boolean hasNext() {
         return this.currentNode != null && (this.currentIndex == 0 || this.currentNode.siblings != null && this.currentNode.siblings[1] != null);
      }

      public boolean hasPrevious() {
         return this.currentIndex != 0 || this.currentNode.siblings != null && this.currentNode.siblings[0] != null;
      }

      public int nextIndex() {
         return this.currentIndex + 1;
      }

      public int previousIndex() {
         return this.currentIndex - 1;
      }

      public void remove() {
         if ((this.flags & 2) != 2) {
            throw new IllegalStateException();
         } else {
            this.parentNode.removeChild(this.currentNode);
            if (this.currentIndex == 0) {
               this.currentNode = this.parentNode.firstChild;
            } else if (this.currentNode.siblings != null && this.currentNode.siblings[0] != null) {
               this.currentNode = this.currentNode.siblings[0];
               --this.currentIndex;
            } else {
               this.currentNode = null;
            }

            this.flags &= 0;
         }
      }

      public FONode last() {
         while(this.currentNode != null && this.currentNode.siblings != null && this.currentNode.siblings[1] != null) {
            this.currentNode = this.currentNode.siblings[1];
            ++this.currentIndex;
         }

         return this.currentNode;
      }

      public FONode first() {
         this.currentNode = this.parentNode.firstChild;
         this.currentIndex = 0;
         return this.currentNode;
      }
   }
}
