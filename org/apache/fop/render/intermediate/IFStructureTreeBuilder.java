package org.apache.fop.render.intermediate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import org.apache.fop.accessibility.StructureTree2SAXEventAdapter;
import org.apache.fop.accessibility.StructureTreeElement;
import org.apache.fop.accessibility.StructureTreeEventHandler;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

final class IFStructureTreeBuilder implements StructureTreeEventHandler {
   private StructureTreeEventHandler delegate;
   private final List pageSequenceEventRecorders = new ArrayList();
   private SAXEventRecorder retrievedMarkersEventRecorder;
   private int idCounter;

   public void replayEventsForPageSequence(ContentHandler handler, int pageSequenceIndex) throws SAXException {
      ((SAXEventRecorder)this.pageSequenceEventRecorders.get(pageSequenceIndex)).replay(handler);
   }

   public void replayEventsForRetrievedMarkers(ContentHandler handler) throws SAXException {
      if (!this.retrievedMarkersEventRecorder.events.isEmpty()) {
         this.delegate = StructureTree2SAXEventAdapter.newInstance(handler);
         this.delegate.startPageSequence((Locale)null, (String)null);
         this.retrievedMarkersEventRecorder.replay(handler);
         this.delegate.endPageSequence();
         this.prepareRetrievedMarkersEventRecorder();
      }

   }

   public void startPageSequence(Locale locale, String role) {
      SAXEventRecorder eventRecorder = new SAXEventRecorder();
      this.pageSequenceEventRecorders.add(eventRecorder);
      this.delegate = StructureTree2SAXEventAdapter.newInstance(eventRecorder);
      this.delegate.startPageSequence(locale, role);
   }

   public void endPageSequence() {
      this.delegate.endPageSequence();
      this.prepareRetrievedMarkersEventRecorder();
   }

   private void prepareRetrievedMarkersEventRecorder() {
      SAXEventRecorder eventRecorder = new SAXEventRecorder();
      this.retrievedMarkersEventRecorder = eventRecorder;
      this.delegate = StructureTree2SAXEventAdapter.newInstance(eventRecorder);
   }

   public StructureTreeElement startNode(String name, Attributes attributes, StructureTreeElement parent) {
      if (parent != null) {
         attributes = this.addParentAttribute(new AttributesImpl((Attributes)attributes), parent);
      }

      this.delegate.startNode(name, (Attributes)attributes, (StructureTreeElement)null);
      return new IFStructureTreeElement();
   }

   private AttributesImpl addParentAttribute(AttributesImpl attributes, StructureTreeElement parent) {
      if (parent != null) {
         attributes.addAttribute("http://xmlgraphics.apache.org/fop/internal", "struct-ref", "foi:struct-ref", "CDATA", ((IFStructureTreeElement)parent).getId());
      }

      return attributes;
   }

   public void endNode(String name) {
      this.delegate.endNode(name);
   }

   public StructureTreeElement startImageNode(String name, Attributes attributes, StructureTreeElement parent) {
      String id = this.getNextID();
      AttributesImpl atts = this.addIDAttribute(attributes, id);
      this.addParentAttribute(atts, parent);
      this.delegate.startImageNode(name, atts, (StructureTreeElement)null);
      return new IFStructureTreeElement(id);
   }

   public StructureTreeElement startReferencedNode(String name, Attributes attributes, StructureTreeElement parent) {
      String id = this.getNextID();
      AttributesImpl atts = this.addIDAttribute(attributes, id);
      this.addParentAttribute(atts, parent);
      this.delegate.startReferencedNode(name, atts, (StructureTreeElement)null);
      return new IFStructureTreeElement(id);
   }

   private String getNextID() {
      return Integer.toHexString(this.idCounter++);
   }

   private AttributesImpl addIDAttribute(Attributes attributes, String id) {
      AttributesImpl atts = new AttributesImpl(attributes);
      atts.addAttribute("http://xmlgraphics.apache.org/fop/internal", "struct-id", "foi:struct-id", "CDATA", id);
      return atts;
   }

   static class SAXEventRecorder extends DefaultHandler {
      private final List events = new ArrayList();

      public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
         this.events.add(new StartElement(uri, localName, qName, attributes));
      }

      public void endElement(String uri, String localName, String qName) throws SAXException {
         this.events.add(new EndElement(uri, localName, qName));
      }

      public void startPrefixMapping(String prefix, String uri) throws SAXException {
         this.events.add(new StartPrefixMapping(prefix, uri));
      }

      public void endPrefixMapping(String prefix) throws SAXException {
         this.events.add(new EndPrefixMapping(prefix));
      }

      public void replay(ContentHandler handler) throws SAXException {
         Iterator var2 = this.events.iterator();

         while(var2.hasNext()) {
            Event e = (Event)var2.next();
            e.replay(handler);
         }

      }

      private static final class EndPrefixMapping extends Event {
         private final String prefix;

         private EndPrefixMapping(String prefix) {
            super(null);
            this.prefix = prefix;
         }

         void replay(ContentHandler handler) throws SAXException {
            handler.endPrefixMapping(this.prefix);
         }

         // $FF: synthetic method
         EndPrefixMapping(String x0, Object x1) {
            this(x0);
         }
      }

      private static final class StartPrefixMapping extends Event {
         private final String prefix;
         private final String uri;

         private StartPrefixMapping(String prefix, String uri) {
            super(null);
            this.prefix = prefix;
            this.uri = uri;
         }

         void replay(ContentHandler handler) throws SAXException {
            handler.startPrefixMapping(this.prefix, this.uri);
         }

         // $FF: synthetic method
         StartPrefixMapping(String x0, String x1, Object x2) {
            this(x0, x1);
         }
      }

      private static final class EndElement extends Element {
         private EndElement(String uri, String localName, String qName) {
            super(uri, localName, qName, null);
         }

         void replay(ContentHandler handler) throws SAXException {
            handler.endElement(this.uri, this.localName, this.qName);
         }

         // $FF: synthetic method
         EndElement(String x0, String x1, String x2, Object x3) {
            this(x0, x1, x2);
         }
      }

      private static final class StartElement extends Element {
         private final Attributes attributes;

         private StartElement(String uri, String localName, String qName, Attributes attributes) {
            super(uri, localName, qName, null);
            this.attributes = attributes;
         }

         void replay(ContentHandler handler) throws SAXException {
            handler.startElement(this.uri, this.localName, this.qName, this.attributes);
         }

         // $FF: synthetic method
         StartElement(String x0, String x1, String x2, Attributes x3, Object x4) {
            this(x0, x1, x2, x3);
         }
      }

      private abstract static class Element extends Event {
         protected final String uri;
         protected final String localName;
         protected final String qName;

         private Element(String uri, String localName, String qName) {
            super(null);
            this.uri = uri;
            this.localName = localName;
            this.qName = qName;
         }

         // $FF: synthetic method
         Element(String x0, String x1, String x2, Object x3) {
            this(x0, x1, x2);
         }
      }

      private abstract static class Event {
         private Event() {
         }

         abstract void replay(ContentHandler var1) throws SAXException;

         // $FF: synthetic method
         Event(Object x0) {
            this();
         }
      }
   }

   static final class IFStructureTreeElement implements StructureTreeElement {
      private final String id;

      IFStructureTreeElement() {
         this.id = null;
      }

      IFStructureTreeElement(String id) {
         this.id = id;
      }

      public String getId() {
         return this.id;
      }
   }
}
