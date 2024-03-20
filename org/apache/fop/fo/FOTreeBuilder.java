package org.apache.fop.fo;

import java.io.OutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.accessibility.fo.FO2StructureTreeConverter;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FormattingResults;
import org.apache.fop.fo.pagination.Root;
import org.apache.fop.util.ContentHandlerFactory;
import org.apache.xmlgraphics.util.QName;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class FOTreeBuilder extends DefaultHandler {
   private static final Log LOG = LogFactory.getLog(FOTreeBuilder.class);
   protected ElementMappingRegistry elementMappingRegistry;
   protected Root rootFObj;
   protected MainFOHandler mainFOHandler;
   protected ContentHandler delegate;
   private FOTreeBuilderContext builderContext;
   private FOEventHandler foEventHandler;
   private Locator locator;
   private FOUserAgent userAgent;
   private boolean used;
   private boolean empty = true;
   private int depth;
   private boolean errorinstart;

   public FOTreeBuilder(String outputFormat, FOUserAgent foUserAgent, OutputStream stream) throws FOPException {
      this.userAgent = foUserAgent;
      this.elementMappingRegistry = this.userAgent.getElementMappingRegistry();
      this.foEventHandler = foUserAgent.getRendererFactory().createFOEventHandler(foUserAgent, outputFormat, stream);
      if (this.userAgent.isAccessibilityEnabled()) {
         this.foEventHandler = new FO2StructureTreeConverter(foUserAgent.getStructureTreeEventHandler(), this.foEventHandler);
      }

      this.builderContext = new FOTreeBuilderContext();
      this.builderContext.setPropertyListMaker(new PropertyListMaker() {
         public PropertyList make(FObj fobj, PropertyList parentPropertyList) {
            return new StaticPropertyList(fobj, parentPropertyList);
         }
      });
   }

   public void setDocumentLocator(Locator locator) {
      this.locator = locator;
   }

   protected Locator getEffectiveLocator() {
      return this.userAgent.isLocatorEnabled() ? this.locator : null;
   }

   public void characters(char[] data, int start, int length) throws SAXException {
      this.delegate.characters(data, start, length);
   }

   public void startDocument() throws SAXException {
      if (this.used) {
         throw new IllegalStateException("FOTreeBuilder (and the Fop class) cannot be reused. Please instantiate a new instance.");
      } else {
         this.used = true;
         this.empty = true;
         this.rootFObj = null;
         if (LOG.isDebugEnabled()) {
            LOG.debug("Building formatting object tree");
         }

         this.foEventHandler.startDocument();
         this.mainFOHandler = new MainFOHandler();
         this.mainFOHandler.startDocument();
         this.delegate = this.mainFOHandler;
      }
   }

   public void endDocument() throws SAXException {
      this.delegate.endDocument();
      if (this.rootFObj == null && this.empty) {
         FOValidationEventProducer eventProducer = FOValidationEventProducer.Provider.get(this.userAgent.getEventBroadcaster());
         eventProducer.emptyDocument(this);
      }

      this.rootFObj = null;
      if (LOG.isDebugEnabled()) {
         LOG.debug("Parsing of document complete");
      }

      this.foEventHandler.endDocument();
   }

   public void startElement(String namespaceURI, String localName, String rawName, Attributes attlist) throws SAXException {
      ++this.depth;
      this.errorinstart = false;

      try {
         this.delegate.startElement(namespaceURI, localName, rawName, attlist);
      } catch (SAXException var6) {
         this.errorinstart = true;
         throw var6;
      }
   }

   public void endElement(String uri, String localName, String rawName) throws SAXException {
      if (!this.errorinstart) {
         this.delegate.endElement(uri, localName, rawName);
         --this.depth;
         if (this.depth == 0 && this.delegate != this.mainFOHandler) {
            this.delegate.endDocument();
            this.delegate = this.mainFOHandler;
            this.delegate.endElement(uri, localName, rawName);
         }
      }

   }

   public void warning(SAXParseException e) {
      LOG.warn(e.getLocalizedMessage());
   }

   public void error(SAXParseException e) {
      LOG.error(e.toString());
   }

   public void fatalError(SAXParseException e) throws SAXException {
      LOG.error(e.toString());
      throw e;
   }

   public FOEventHandler getEventHandler() {
      return this.foEventHandler;
   }

   public FormattingResults getResults() {
      return this.getEventHandler().getResults();
   }

   private class MainFOHandler extends DefaultHandler {
      protected FONode currentFObj;
      protected PropertyList currentPropertyList;
      private int nestedMarkerDepth;

      private MainFOHandler() {
      }

      public void startElement(String namespaceURI, String localName, String rawName, Attributes attlist) throws SAXException {
         PropertyList propertyList = null;
         if (FOTreeBuilder.this.rootFObj == null) {
            FOTreeBuilder.this.empty = false;
            if (!namespaceURI.equals("http://www.w3.org/1999/XSL/Format") || !localName.equals("root")) {
               FOValidationEventProducer eventProducer = FOValidationEventProducer.Provider.get(FOTreeBuilder.this.userAgent.getEventBroadcaster());
               eventProducer.invalidFORoot(this, FONode.getNodeString(namespaceURI, localName), FOTreeBuilder.this.getEffectiveLocator());
            }
         } else if ((this.currentFObj.getNamespaceURI().equals("http://www.w3.org/1999/XSL/Format") || this.currentFObj.getNamespaceURI().equals("http://xmlgraphics.apache.org/fop/extensions") || this.currentFObj.getNamespaceURI().equals("http://xmlgraphics.apache.org/fop/extensions/pdf")) && !this.currentFObj.isChangeBarElement(namespaceURI, localName)) {
            this.currentFObj.validateChildNode(FOTreeBuilder.this.locator, namespaceURI, localName);
         }

         ElementMapping.Maker fobjMaker = this.findFOMaker(namespaceURI, localName);

         FONode foNode;
         try {
            foNode = fobjMaker.make(this.currentFObj);
            if (FOTreeBuilder.this.rootFObj == null) {
               FOTreeBuilder.this.rootFObj = (Root)foNode;
               FOTreeBuilder.this.rootFObj.setBuilderContext(FOTreeBuilder.this.builderContext);
               FOTreeBuilder.this.rootFObj.setFOEventHandler(FOTreeBuilder.this.foEventHandler);
            }

            propertyList = foNode.createPropertyList(this.currentPropertyList, FOTreeBuilder.this.foEventHandler);
            foNode.processNode(localName, FOTreeBuilder.this.getEffectiveLocator(), attlist, propertyList);
            if (foNode.getNameId() == 44) {
               if (FOTreeBuilder.this.builderContext.inMarker()) {
                  ++this.nestedMarkerDepth;
               } else {
                  FOTreeBuilder.this.builderContext.switchMarkerContext(true);
               }
            }

            if (foNode.getNameId() == 53) {
               FOTreeBuilder.this.builderContext.getXMLWhiteSpaceHandler().reset();
            }
         } catch (IllegalArgumentException var10) {
            throw new SAXException(var10);
         }

         ContentHandlerFactory chFactory = foNode.getContentHandlerFactory();
         if (chFactory != null) {
            ContentHandler subHandler = chFactory.createContentHandler();
            if (subHandler instanceof ContentHandlerFactory.ObjectSource && foNode instanceof ContentHandlerFactory.ObjectBuiltListener) {
               ((ContentHandlerFactory.ObjectSource)subHandler).setObjectBuiltListener((ContentHandlerFactory.ObjectBuiltListener)foNode);
            }

            subHandler.startDocument();
            subHandler.startElement(namespaceURI, localName, rawName, attlist);
            FOTreeBuilder.this.depth = 1;
            FOTreeBuilder.this.delegate = subHandler;
         }

         if (this.currentFObj != null) {
            this.currentFObj.addChildNode(foNode);
         }

         this.currentFObj = foNode;
         if (propertyList != null && !FOTreeBuilder.this.builderContext.inMarker()) {
            this.currentPropertyList = propertyList;
         }

         if (this.currentFObj.getNameId() != 10 && (!FOTreeBuilder.this.builderContext.inMarker() || this.currentFObj.getNameId() == 44)) {
            this.currentFObj.startOfNode();
         }

      }

      public void endElement(String uri, String localName, String rawName) throws SAXException {
         if (this.currentFObj == null) {
            throw new SAXException("endElement() called for " + rawName + " where there is no current element.");
         } else if (this.currentFObj.getLocalName().equals(localName) && this.currentFObj.getNamespaceURI().equals(uri)) {
            if (this.currentFObj.getNameId() != 10 && (!FOTreeBuilder.this.builderContext.inMarker() || this.currentFObj.getNameId() == 44)) {
               this.currentFObj.endOfNode();
            }

            if (this.currentPropertyList != null && this.currentPropertyList.getFObj() == this.currentFObj && !FOTreeBuilder.this.builderContext.inMarker()) {
               this.currentPropertyList = this.currentPropertyList.getParentPropertyList();
            }

            if (this.currentFObj.getNameId() == 44) {
               if (this.nestedMarkerDepth == 0) {
                  FOTreeBuilder.this.builderContext.switchMarkerContext(false);
               } else {
                  --this.nestedMarkerDepth;
               }
            }

            if (this.currentFObj.getParent() == null) {
               FOTreeBuilder.LOG.debug("endElement for top-level " + this.currentFObj.getName());
            }

            this.currentFObj = this.currentFObj.getParent();
         } else {
            throw new SAXException("Mismatch: " + this.currentFObj.getLocalName() + " (" + this.currentFObj.getNamespaceURI() + ") vs. " + localName + " (" + uri + ")");
         }
      }

      public void characters(char[] data, int start, int length) throws FOPException {
         if (this.currentFObj != null) {
            this.currentFObj.characters(data, start, length, this.currentPropertyList, FOTreeBuilder.this.getEffectiveLocator());
         }

      }

      public void endDocument() throws SAXException {
         this.currentFObj = null;
      }

      private ElementMapping.Maker findFOMaker(String namespaceURI, String localName) throws FOPException {
         ElementMapping.Maker maker = FOTreeBuilder.this.elementMappingRegistry.findFOMaker(namespaceURI, localName, FOTreeBuilder.this.locator);
         if (maker instanceof UnknownXMLObj.Maker) {
            FOValidationEventProducer eventProducer = FOValidationEventProducer.Provider.get(FOTreeBuilder.this.userAgent.getEventBroadcaster());
            String name = this.currentFObj != null ? this.currentFObj.getName() : "{" + namespaceURI + "}" + localName;
            eventProducer.unknownFormattingObject(this, name, new QName(namespaceURI, localName), FOTreeBuilder.this.getEffectiveLocator());
         }

         return maker;
      }

      // $FF: synthetic method
      MainFOHandler(Object x1) {
         this();
      }
   }
}
