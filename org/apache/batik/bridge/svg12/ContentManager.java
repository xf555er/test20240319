package org.apache.batik.bridge.svg12;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import javax.swing.event.EventListenerList;
import org.apache.batik.anim.dom.XBLEventSupport;
import org.apache.batik.anim.dom.XBLOMContentElement;
import org.apache.batik.anim.dom.XBLOMShadowTreeElement;
import org.apache.batik.dom.AbstractNode;
import org.apache.batik.dom.events.NodeEventTarget;
import org.apache.batik.dom.xbl.XBLManager;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.events.MutationEvent;

public class ContentManager {
   protected XBLOMShadowTreeElement shadowTree;
   protected Element boundElement;
   protected DefaultXBLManager xblManager;
   protected HashMap selectors = new HashMap();
   protected HashMap selectedNodes = new HashMap();
   protected LinkedList contentElementList = new LinkedList();
   protected Node removedNode;
   protected HashMap listeners = new HashMap();
   protected ContentElementDOMAttrModifiedEventListener contentElementDomAttrModifiedEventListener;
   protected DOMAttrModifiedEventListener domAttrModifiedEventListener;
   protected DOMNodeInsertedEventListener domNodeInsertedEventListener;
   protected DOMNodeRemovedEventListener domNodeRemovedEventListener;
   protected DOMSubtreeModifiedEventListener domSubtreeModifiedEventListener;
   protected ShadowTreeNodeInsertedListener shadowTreeNodeInsertedListener;
   protected ShadowTreeNodeRemovedListener shadowTreeNodeRemovedListener;
   protected ShadowTreeSubtreeModifiedListener shadowTreeSubtreeModifiedListener;

   public ContentManager(XBLOMShadowTreeElement s, XBLManager xm) {
      this.shadowTree = s;
      this.xblManager = (DefaultXBLManager)xm;
      this.xblManager.setContentManager(s, this);
      this.boundElement = this.xblManager.getXblBoundElement(s);
      this.contentElementDomAttrModifiedEventListener = new ContentElementDOMAttrModifiedEventListener();
      XBLEventSupport es = (XBLEventSupport)this.shadowTree.initializeEventSupport();
      this.shadowTreeNodeInsertedListener = new ShadowTreeNodeInsertedListener();
      this.shadowTreeNodeRemovedListener = new ShadowTreeNodeRemovedListener();
      this.shadowTreeSubtreeModifiedListener = new ShadowTreeSubtreeModifiedListener();
      es.addImplementationEventListenerNS("http://www.w3.org/2001/xml-events", "DOMNodeInserted", this.shadowTreeNodeInsertedListener, true);
      es.addImplementationEventListenerNS("http://www.w3.org/2001/xml-events", "DOMNodeRemoved", this.shadowTreeNodeRemovedListener, true);
      es.addImplementationEventListenerNS("http://www.w3.org/2001/xml-events", "DOMSubtreeModified", this.shadowTreeSubtreeModifiedListener, true);
      es = (XBLEventSupport)((AbstractNode)this.boundElement).initializeEventSupport();
      this.domAttrModifiedEventListener = new DOMAttrModifiedEventListener();
      this.domNodeInsertedEventListener = new DOMNodeInsertedEventListener();
      this.domNodeRemovedEventListener = new DOMNodeRemovedEventListener();
      this.domSubtreeModifiedEventListener = new DOMSubtreeModifiedEventListener();
      es.addImplementationEventListenerNS("http://www.w3.org/2001/xml-events", "DOMAttrModified", this.domAttrModifiedEventListener, true);
      es.addImplementationEventListenerNS("http://www.w3.org/2001/xml-events", "DOMNodeInserted", this.domNodeInsertedEventListener, true);
      es.addImplementationEventListenerNS("http://www.w3.org/2001/xml-events", "DOMNodeRemoved", this.domNodeRemovedEventListener, true);
      es.addImplementationEventListenerNS("http://www.w3.org/2001/xml-events", "DOMSubtreeModified", this.domSubtreeModifiedEventListener, false);
      this.update(true);
   }

   public void dispose() {
      this.xblManager.setContentManager(this.shadowTree, (ContentManager)null);
      Iterator i = this.selectedNodes.entrySet().iterator();

      while(i.hasNext()) {
         Map.Entry e = (Map.Entry)i.next();
         NodeList nl = (NodeList)e.getValue();

         for(int j = 0; j < nl.getLength(); ++j) {
            Node n = nl.item(j);
            this.xblManager.getRecord(n).contentElement = null;
         }
      }

      i = this.contentElementList.iterator();

      while(i.hasNext()) {
         NodeEventTarget n = (NodeEventTarget)i.next();
         n.removeEventListenerNS("http://www.w3.org/2001/xml-events", "DOMAttrModified", this.contentElementDomAttrModifiedEventListener, false);
      }

      this.contentElementList.clear();
      this.selectedNodes.clear();
      XBLEventSupport es = (XBLEventSupport)((AbstractNode)this.boundElement).getEventSupport();
      es.removeImplementationEventListenerNS("http://www.w3.org/2001/xml-events", "DOMAttrModified", this.domAttrModifiedEventListener, true);
      es.removeImplementationEventListenerNS("http://www.w3.org/2001/xml-events", "DOMNodeInserted", this.domNodeInsertedEventListener, true);
      es.removeImplementationEventListenerNS("http://www.w3.org/2001/xml-events", "DOMNodeRemoved", this.domNodeRemovedEventListener, true);
      es.removeImplementationEventListenerNS("http://www.w3.org/2001/xml-events", "DOMSubtreeModified", this.domSubtreeModifiedEventListener, false);
   }

   public NodeList getSelectedContent(XBLOMContentElement e) {
      return (NodeList)this.selectedNodes.get(e);
   }

   protected XBLOMContentElement getContentElement(Node n) {
      return this.xblManager.getXblContentElement(n);
   }

   public void addContentSelectionChangedListener(XBLOMContentElement e, ContentSelectionChangedListener l) {
      EventListenerList ll = (EventListenerList)this.listeners.get(e);
      if (ll == null) {
         ll = new EventListenerList();
         this.listeners.put(e, ll);
      }

      ll.add(ContentSelectionChangedListener.class, l);
   }

   public void removeContentSelectionChangedListener(XBLOMContentElement e, ContentSelectionChangedListener l) {
      EventListenerList ll = (EventListenerList)this.listeners.get(e);
      if (ll != null) {
         ll.remove(ContentSelectionChangedListener.class, l);
      }

   }

   protected void dispatchContentSelectionChangedEvent(XBLOMContentElement e) {
      this.xblManager.invalidateChildNodes(e.getXblParentNode());
      ContentSelectionChangedEvent evt = new ContentSelectionChangedEvent(e);
      EventListenerList ll = (EventListenerList)this.listeners.get(e);
      Object[] ls;
      int i;
      ContentSelectionChangedListener l;
      if (ll != null) {
         ls = ll.getListenerList();

         for(i = ls.length - 2; i >= 0; i -= 2) {
            l = (ContentSelectionChangedListener)ls[i + 1];
            l.contentSelectionChanged(evt);
         }
      }

      ls = this.xblManager.getContentSelectionChangedListeners();

      for(i = ls.length - 2; i >= 0; i -= 2) {
         l = (ContentSelectionChangedListener)ls[i + 1];
         l.contentSelectionChanged(evt);
      }

   }

   protected void update(boolean first) {
      HashSet previouslySelectedNodes = new HashSet();
      Iterator i = this.selectedNodes.entrySet().iterator();

      while(i.hasNext()) {
         Map.Entry e = (Map.Entry)i.next();
         NodeList nl = (NodeList)e.getValue();

         for(int j = 0; j < nl.getLength(); ++j) {
            Node n = nl.item(j);
            this.xblManager.getRecord(n).contentElement = null;
            previouslySelectedNodes.add(n);
         }
      }

      i = this.contentElementList.iterator();

      while(i.hasNext()) {
         NodeEventTarget n = (NodeEventTarget)i.next();
         n.removeEventListenerNS("http://www.w3.org/2001/xml-events", "DOMAttrModified", this.contentElementDomAttrModifiedEventListener, false);
      }

      this.contentElementList.clear();
      this.selectedNodes.clear();
      boolean updated = false;

      for(Node n = this.shadowTree.getFirstChild(); n != null; n = n.getNextSibling()) {
         if (this.update(first, n)) {
            updated = true;
         }
      }

      if (updated) {
         HashSet newlySelectedNodes = new HashSet();
         i = this.selectedNodes.entrySet().iterator();

         while(i.hasNext()) {
            Map.Entry e = (Map.Entry)i.next();
            NodeList nl = (NodeList)e.getValue();

            for(int j = 0; j < nl.getLength(); ++j) {
               Node n = nl.item(j);
               newlySelectedNodes.add(n);
            }
         }

         HashSet removed = new HashSet();
         removed.addAll(previouslySelectedNodes);
         removed.removeAll(newlySelectedNodes);
         HashSet added = new HashSet();
         added.addAll(newlySelectedNodes);
         added.removeAll(previouslySelectedNodes);
         if (!first) {
            this.xblManager.shadowTreeSelectedContentChanged(removed, added);
         }
      }

   }

   protected boolean update(boolean first, Node n) {
      boolean updated = false;

      for(Node m = n.getFirstChild(); m != null; m = m.getNextSibling()) {
         if (this.update(first, m)) {
            updated = true;
         }
      }

      if (n instanceof XBLOMContentElement) {
         this.contentElementList.add(n);
         XBLOMContentElement e = (XBLOMContentElement)n;
         e.addEventListenerNS("http://www.w3.org/2001/xml-events", "DOMAttrModified", this.contentElementDomAttrModifiedEventListener, false, (Object)null);
         AbstractContentSelector s = (AbstractContentSelector)this.selectors.get(n);
         boolean changed;
         if (s == null) {
            if (e.hasAttributeNS((String)null, "includes")) {
               String lang = this.getContentSelectorLanguage(e);
               String selector = e.getAttributeNS((String)null, "includes");
               s = AbstractContentSelector.createSelector(lang, this, e, this.boundElement, selector);
            } else {
               s = new DefaultContentSelector(this, e, this.boundElement);
            }

            this.selectors.put(n, s);
            changed = true;
         } else {
            changed = ((AbstractContentSelector)s).update();
         }

         NodeList selectedContent = ((AbstractContentSelector)s).getSelectedContent();
         this.selectedNodes.put(n, selectedContent);

         for(int i = 0; i < selectedContent.getLength(); ++i) {
            Node m = selectedContent.item(i);
            this.xblManager.getRecord(m).contentElement = e;
         }

         if (changed) {
            updated = true;
            this.dispatchContentSelectionChangedEvent(e);
         }
      }

      return updated;
   }

   protected String getContentSelectorLanguage(Element e) {
      String lang = e.getAttributeNS("http://xml.apache.org/batik/ext", "selectorLanguage");
      if (lang.length() != 0) {
         return lang;
      } else {
         lang = e.getOwnerDocument().getDocumentElement().getAttributeNS("http://xml.apache.org/batik/ext", "selectorLanguage");
         return lang.length() != 0 ? lang : null;
      }
   }

   protected class ShadowTreeSubtreeModifiedListener implements EventListener {
      public void handleEvent(Event evt) {
         if (ContentManager.this.removedNode != null) {
            ContentManager.this.removedNode = null;
            ContentManager.this.update(false);
         }

      }
   }

   protected class ShadowTreeNodeRemovedListener implements EventListener {
      public void handleEvent(Event evt) {
         EventTarget target = evt.getTarget();
         if (target instanceof XBLOMContentElement) {
            ContentManager.this.removedNode = (Node)evt.getTarget();
         }

      }
   }

   protected class ShadowTreeNodeInsertedListener implements EventListener {
      public void handleEvent(Event evt) {
         if (evt.getTarget() instanceof XBLOMContentElement) {
            ContentManager.this.update(false);
         }

      }
   }

   protected class DOMSubtreeModifiedEventListener implements EventListener {
      public void handleEvent(Event evt) {
         if (ContentManager.this.removedNode != null) {
            ContentManager.this.removedNode = null;
            ContentManager.this.update(false);
         }

      }
   }

   protected class DOMNodeRemovedEventListener implements EventListener {
      public void handleEvent(Event evt) {
         ContentManager.this.removedNode = (Node)evt.getTarget();
      }
   }

   protected class DOMNodeInsertedEventListener implements EventListener {
      public void handleEvent(Event evt) {
         ContentManager.this.update(false);
      }
   }

   protected class DOMAttrModifiedEventListener implements EventListener {
      public void handleEvent(Event evt) {
         if (evt.getTarget() != ContentManager.this.boundElement) {
            ContentManager.this.update(false);
         }

      }
   }

   protected class ContentElementDOMAttrModifiedEventListener implements EventListener {
      public void handleEvent(Event evt) {
         MutationEvent me = (MutationEvent)evt;
         Attr a = (Attr)me.getRelatedNode();
         Element e = (Element)evt.getTarget();
         if (e instanceof XBLOMContentElement) {
            String ans = a.getNamespaceURI();
            String aln = a.getLocalName();
            if (aln == null) {
               aln = a.getNodeName();
            }

            if (ans == null && "includes".equals(aln) || "http://xml.apache.org/batik/ext".equals(ans) && "selectorLanguage".equals(aln)) {
               ContentManager.this.selectors.remove(e);
               ContentManager.this.update(false);
            }
         }

      }
   }
}
