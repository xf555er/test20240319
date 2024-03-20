package org.apache.fop.render.intermediate;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.accessibility.AccessibilityEventProducer;
import org.apache.fop.accessibility.StructureTreeElement;
import org.apache.fop.accessibility.StructureTreeEventHandler;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fo.ElementMapping;
import org.apache.fop.fo.ElementMappingRegistry;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.render.intermediate.extensions.DocumentNavigationHandler;
import org.apache.fop.traits.BorderProps;
import org.apache.fop.traits.RuleStyle;
import org.apache.fop.util.ColorUtil;
import org.apache.fop.util.ContentHandlerFactory;
import org.apache.fop.util.ContentHandlerFactoryRegistry;
import org.apache.fop.util.DOMBuilderContentHandlerFactory;
import org.apache.fop.util.DefaultErrorListener;
import org.apache.fop.util.LanguageTags;
import org.apache.fop.util.XMLConstants;
import org.apache.fop.util.XMLUtil;
import org.apache.xmlgraphics.util.QName;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

public class IFParser implements IFConstants {
   protected static final Log log = LogFactory.getLog(IFParser.class);
   private static SAXTransformerFactory tFactory = (SAXTransformerFactory)SAXTransformerFactory.newInstance();
   private static Set handledNamespaces = new HashSet();

   public void parse(Source src, IFDocumentHandler documentHandler, FOUserAgent userAgent) throws TransformerException, IFException {
      try {
         Transformer transformer = tFactory.newTransformer();
         transformer.setErrorListener(new DefaultErrorListener(log));
         SAXResult res = new SAXResult(this.getContentHandler(documentHandler, userAgent));
         transformer.transform(src, res);
      } catch (TransformerException var7) {
         Throwable cause = var7.getCause();
         if (cause instanceof SAXException) {
            SAXException se = (SAXException)cause;
            cause = se.getCause();
            if (cause instanceof IFException) {
               throw (IFException)cause;
            }
         } else if (cause instanceof IFException) {
            throw (IFException)cause;
         }

         throw var7;
      }
   }

   public ContentHandler getContentHandler(IFDocumentHandler documentHandler, FOUserAgent userAgent) {
      ElementMappingRegistry elementMappingRegistry = userAgent.getElementMappingRegistry();
      return new Handler(documentHandler, userAgent, elementMappingRegistry);
   }

   static {
      handledNamespaces.add("http://www.w3.org/2000/xmlns/");
      handledNamespaces.add("http://www.w3.org/XML/1998/namespace");
      handledNamespaces.add("http://xmlgraphics.apache.org/fop/intermediate");
      handledNamespaces.add("http://www.w3.org/1999/xlink");
   }

   private static class Handler extends DefaultHandler {
      private Map elementHandlers = new HashMap();
      private IFDocumentHandler documentHandler;
      private IFPainter painter;
      private FOUserAgent userAgent;
      private ElementMappingRegistry elementMappingRegistry;
      private Attributes lastAttributes;
      private StringBuffer content = new StringBuffer();
      private boolean ignoreCharacters = true;
      private int delegateDepth;
      private ContentHandler delegate;
      private boolean inForeignObject;
      private Document foreignObject;
      private ContentHandler navParser;
      private StructureTreeHandler structureTreeHandler;
      private Attributes pageSequenceAttributes;
      private Map structureTreeElements = new HashMap();
      private static final String[] SIDES = new String[]{"top", "bottom", "left", "right"};

      public Handler(IFDocumentHandler documentHandler, FOUserAgent userAgent, ElementMappingRegistry elementMappingRegistry) {
         this.documentHandler = documentHandler;
         this.userAgent = userAgent;
         this.elementMappingRegistry = elementMappingRegistry;
         this.elementHandlers.put("document", new DocumentHandler());
         this.elementHandlers.put("header", new DocumentHeaderHandler());
         this.elementHandlers.put("locale", new LocaleHandler());
         this.elementHandlers.put("trailer", new DocumentTrailerHandler());
         this.elementHandlers.put("page-sequence", new PageSequenceHandler());
         this.elementHandlers.put("page", new PageHandler());
         this.elementHandlers.put("page-header", new PageHeaderHandler());
         this.elementHandlers.put("content", new PageContentHandler());
         this.elementHandlers.put("page-trailer", new PageTrailerHandler());
         this.elementHandlers.put("viewport", new ViewportHandler());
         this.elementHandlers.put("g", new GroupHandler());
         this.elementHandlers.put("id", new IDHandler());
         this.elementHandlers.put("font", new FontHandler());
         this.elementHandlers.put("text", new TextHandler());
         this.elementHandlers.put("clip-rect", new ClipRectHandler());
         this.elementHandlers.put("rect", new RectHandler());
         this.elementHandlers.put("line", new LineHandler());
         this.elementHandlers.put("border-rect", new BorderRectHandler());
         this.elementHandlers.put("image", new ImageHandler());
      }

      private void establishForeignAttributes(Map foreignAttributes) {
         this.documentHandler.getContext().setForeignAttributes(foreignAttributes);
      }

      private void resetForeignAttributes() {
         this.documentHandler.getContext().resetForeignAttributes();
      }

      public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
         if (this.delegate != null) {
            ++this.delegateDepth;
            this.delegate.startElement(uri, localName, qName, attributes);
         } else {
            boolean handled = true;
            if ("http://xmlgraphics.apache.org/fop/intermediate".equals(uri)) {
               if (localName.equals("page-sequence") && this.userAgent.isAccessibilityEnabled()) {
                  this.pageSequenceAttributes = new AttributesImpl(attributes);
                  Locale language = getLanguage(attributes);
                  this.structureTreeHandler = new MainStructureTreeHandler(this.userAgent.getStructureTreeEventHandler(), language);
               } else if (localName.equals("structure-tree")) {
                  if (this.userAgent.isAccessibilityEnabled()) {
                     String type = attributes.getValue("type");
                     this.structureTreeHandler.startStructureTree(type);
                     this.delegate = this.structureTreeHandler;
                  } else {
                     this.delegate = new DefaultHandler();
                  }

                  ++this.delegateDepth;
                  this.delegate.startDocument();
                  this.delegate.startElement(uri, localName, qName, attributes);
               } else {
                  if (this.pageSequenceAttributes != null) {
                     AccessibilityEventProducer.Provider.get(this.userAgent.getEventBroadcaster()).noStructureTreeInXML(this);
                  }

                  handled = this.startIFElement(localName, attributes);
               }
            } else if ("http://xmlgraphics.apache.org/fop/intermediate/document-navigation".equals(uri)) {
               if (this.navParser == null) {
                  this.navParser = new DocumentNavigationHandler(this.documentHandler.getDocumentNavigationHandler(), this.structureTreeElements);
               }

               this.delegate = this.navParser;
               ++this.delegateDepth;
               this.delegate.startDocument();
               this.delegate.startElement(uri, localName, qName, attributes);
            } else {
               ContentHandlerFactoryRegistry registry = this.userAgent.getContentHandlerFactoryRegistry();
               ContentHandlerFactory factory = registry.getFactory(uri);
               if (factory == null) {
                  DOMImplementation domImplementation = this.elementMappingRegistry.getDOMImplementationForNamespace(uri);
                  if (domImplementation == null) {
                     domImplementation = ElementMapping.getDefaultDOMImplementation();
                  }

                  factory = new DOMBuilderContentHandlerFactory(uri, domImplementation);
               }

               this.delegate = ((ContentHandlerFactory)factory).createContentHandler();
               ++this.delegateDepth;
               this.delegate.startDocument();
               this.delegate.startElement(uri, localName, qName, attributes);
            }

            if (!handled) {
               if (uri == null || uri.length() == 0) {
                  throw new SAXException("Unhandled element " + localName + " in namespace: " + uri);
               }

               IFParser.log.warn("Unhandled element " + localName + " in namespace: " + uri);
            }
         }

      }

      private static Locale getLanguage(Attributes attributes) {
         String xmllang = attributes.getValue("http://www.w3.org/XML/1998/namespace", "lang");
         return xmllang == null ? null : LanguageTags.toLocale(xmllang);
      }

      private boolean startIFElement(String localName, Attributes attributes) throws SAXException {
         this.lastAttributes = new AttributesImpl(attributes);
         ElementHandler elementHandler = (ElementHandler)this.elementHandlers.get(localName);
         this.content.setLength(0);
         this.ignoreCharacters = true;
         if (elementHandler != null) {
            this.ignoreCharacters = elementHandler.ignoreCharacters();

            try {
               elementHandler.startElement(attributes);
            } catch (IFException var5) {
               this.handleIFException(var5);
            }

            return true;
         } else {
            return false;
         }
      }

      private void handleIFException(IFException ife) throws SAXException {
         Throwable cause = ife.getCause();
         if (cause instanceof SAXException) {
            throw (SAXException)cause;
         } else {
            throw new SAXException(ife);
         }
      }

      public void endElement(String uri, String localName, String qName) throws SAXException {
         if (this.delegate != null) {
            this.delegate.endElement(uri, localName, qName);
            --this.delegateDepth;
            if (this.delegateDepth == 0) {
               this.delegate.endDocument();
               if (this.delegate instanceof ContentHandlerFactory.ObjectSource) {
                  Object obj = ((ContentHandlerFactory.ObjectSource)this.delegate).getObject();
                  if (this.inForeignObject) {
                     this.foreignObject = (Document)obj;
                  } else {
                     this.handleExternallyGeneratedObject(obj);
                  }
               }

               this.delegate = null;
            }
         } else if ("http://xmlgraphics.apache.org/fop/intermediate".equals(uri)) {
            ElementHandler elementHandler = (ElementHandler)this.elementHandlers.get(localName);
            if (elementHandler != null) {
               try {
                  elementHandler.endElement();
               } catch (IFException var6) {
                  this.handleIFException(var6);
               }

               this.content.setLength(0);
            }

            this.ignoreCharacters = true;
         } else if (IFParser.log.isTraceEnabled()) {
            IFParser.log.trace("Ignoring " + localName + " in namespace: " + uri);
         }

      }

      protected void handleExternallyGeneratedObject(Object obj) throws SAXException {
         try {
            this.documentHandler.handleExtensionObject(obj);
         } catch (IFException var3) {
            this.handleIFException(var3);
         }

      }

      private Color getAttributeAsColor(Attributes attributes, String name) throws PropertyException {
         String s = attributes.getValue(name);
         return s == null ? null : ColorUtil.parseColorString(this.userAgent, s);
      }

      private static Map getForeignAttributes(Attributes atts) {
         Map foreignAttributes = null;
         int i = 0;

         for(int c = atts.getLength(); i < c; ++i) {
            String ns = atts.getURI(i);
            if (ns.length() > 0 && !IFParser.handledNamespaces.contains(ns)) {
               if (foreignAttributes == null) {
                  foreignAttributes = new HashMap();
               }

               QName qname = new QName(ns, atts.getQName(i));
               foreignAttributes.put(qname, atts.getValue(i));
            }
         }

         return foreignAttributes;
      }

      private void establishStructureTreeElement(Attributes attributes) {
         StructureTreeElement element = this.getStructureTreeElement(attributes);
         if (element != null) {
            this.documentHandler.getContext().setStructureTreeElement(element);
         }

      }

      private StructureTreeElement getStructureTreeElement(Attributes attributes) {
         String structRef = attributes.getValue("http://xmlgraphics.apache.org/fop/internal", "struct-ref");
         if (structRef != null && structRef.length() > 0) {
            assert this.structureTreeElements.containsKey(structRef);

            return (StructureTreeElement)this.structureTreeElements.get(structRef);
         } else {
            return null;
         }
      }

      private void resetStructureTreeElement() {
         this.documentHandler.getContext().resetStructureTreeElement();
      }

      public void characters(char[] ch, int start, int length) throws SAXException {
         if (this.delegate != null) {
            this.delegate.characters(ch, start, length);
         } else if (!this.ignoreCharacters) {
            this.content.append(ch, start, length);
         }

      }

      private class ImageHandler extends AbstractElementHandler {
         private ImageHandler() {
            super(null);
         }

         public void startElement(Attributes attributes) throws IFException {
            Handler.this.inForeignObject = true;
         }

         public void endElement() throws IFException {
            int x = Integer.parseInt(Handler.this.lastAttributes.getValue("x"));
            int y = Integer.parseInt(Handler.this.lastAttributes.getValue("y"));
            int width = Integer.parseInt(Handler.this.lastAttributes.getValue("width"));
            int height = Integer.parseInt(Handler.this.lastAttributes.getValue("height"));
            Map foreignAttributes = IFParser.Handler.getForeignAttributes(Handler.this.lastAttributes);
            Handler.this.establishForeignAttributes(foreignAttributes);
            Handler.this.establishStructureTreeElement(Handler.this.lastAttributes);
            if (Handler.this.foreignObject != null) {
               Handler.this.painter.drawImage(Handler.this.foreignObject, new Rectangle(x, y, width, height));
               Handler.this.foreignObject = null;
            } else {
               String uri = Handler.this.lastAttributes.getValue(XMLConstants.XLINK_HREF.getNamespaceURI(), XMLConstants.XLINK_HREF.getLocalName());
               if (uri == null) {
                  throw new IFException("xlink:href is missing on image", (Exception)null);
               }

               Handler.this.painter.drawImage(uri, new Rectangle(x, y, width, height));
            }

            Handler.this.resetStructureTreeElement();
            Handler.this.resetForeignAttributes();
            Handler.this.inForeignObject = false;
         }

         public boolean ignoreCharacters() {
            return false;
         }

         // $FF: synthetic method
         ImageHandler(Object x1) {
            this();
         }
      }

      private class BorderRectHandler extends AbstractElementHandler {
         private BorderRectHandler() {
            super(null);
         }

         public void startElement(Attributes attributes) throws IFException {
            int x = Integer.parseInt(attributes.getValue("x"));
            int y = Integer.parseInt(attributes.getValue("y"));
            int width = Integer.parseInt(attributes.getValue("width"));
            int height = Integer.parseInt(attributes.getValue("height"));
            BorderProps[] borders = new BorderProps[4];

            for(int i = 0; i < 4; ++i) {
               String b = attributes.getValue(IFParser.Handler.SIDES[i]);
               if (b != null) {
                  borders[i] = BorderProps.valueOf(Handler.this.userAgent, b);
               }
            }

            Color backgroundColor;
            try {
               backgroundColor = Handler.this.getAttributeAsColor(attributes, "inner-background-color");
            } catch (PropertyException var9) {
               throw new IFException("Error parsing the color attribute", var9);
            }

            Handler.this.painter.drawBorderRect(new Rectangle(x, y, width, height), borders[0], borders[1], borders[2], borders[3], backgroundColor);
         }

         // $FF: synthetic method
         BorderRectHandler(Object x1) {
            this();
         }
      }

      private class LineHandler extends AbstractElementHandler {
         private LineHandler() {
            super(null);
         }

         public void startElement(Attributes attributes) throws IFException {
            int x1 = Integer.parseInt(attributes.getValue("x1"));
            int y1 = Integer.parseInt(attributes.getValue("y1"));
            int x2 = Integer.parseInt(attributes.getValue("x2"));
            int y2 = Integer.parseInt(attributes.getValue("y2"));
            int width = Integer.parseInt(attributes.getValue("stroke-width"));

            Color color;
            try {
               color = Handler.this.getAttributeAsColor(attributes, "color");
            } catch (PropertyException var9) {
               throw new IFException("Error parsing the fill attribute", var9);
            }

            RuleStyle style = RuleStyle.valueOf(attributes.getValue("style"));
            Handler.this.painter.drawLine(new Point(x1, y1), new Point(x2, y2), width, color, style);
         }

         // $FF: synthetic method
         LineHandler(Object x1) {
            this();
         }
      }

      private class RectHandler extends AbstractElementHandler {
         private RectHandler() {
            super(null);
         }

         public void startElement(Attributes attributes) throws IFException {
            int x = Integer.parseInt(attributes.getValue("x"));
            int y = Integer.parseInt(attributes.getValue("y"));
            int width = Integer.parseInt(attributes.getValue("width"));
            int height = Integer.parseInt(attributes.getValue("height"));

            Color fillColor;
            try {
               fillColor = Handler.this.getAttributeAsColor(attributes, "fill");
            } catch (PropertyException var11) {
               throw new IFException("Error parsing the fill attribute", var11);
            }

            Rectangle rectangularArea = new Rectangle(x, y, width, height);
            BorderProps[] borders = new BorderProps[4];

            for(int i = 0; i < 4; ++i) {
               String b = attributes.getValue(IFParser.Handler.SIDES[i]);
               if (b != null) {
                  borders[i] = BorderProps.valueOf(Handler.this.userAgent, b);
               }
            }

            if (borders[0] != null || borders[1] != null || borders[2] != null || borders[3] != null) {
               Handler.this.painter.clipBackground(rectangularArea, borders[0], borders[1], borders[2], borders[3]);
            }

            Handler.this.painter.fillRect(rectangularArea, fillColor);
         }

         // $FF: synthetic method
         RectHandler(Object x1) {
            this();
         }
      }

      private class ClipRectHandler extends AbstractElementHandler {
         private ClipRectHandler() {
            super(null);
         }

         public void startElement(Attributes attributes) throws IFException {
            int x = Integer.parseInt(attributes.getValue("x"));
            int y = Integer.parseInt(attributes.getValue("y"));
            int width = Integer.parseInt(attributes.getValue("width"));
            int height = Integer.parseInt(attributes.getValue("height"));
            BorderProps[] borders = new BorderProps[4];

            for(int i = 0; i < 4; ++i) {
               String b = attributes.getValue(IFParser.Handler.SIDES[i]);
               if (b != null) {
                  borders[i] = BorderProps.valueOf(Handler.this.userAgent, b);
               }
            }

            if (borders[0] != null || borders[1] != null || borders[2] != null || borders[3] != null) {
               Handler.this.painter.clipBackground(new Rectangle(x, y, width, height), borders[0], borders[1], borders[2], borders[3]);
            }

            Handler.this.painter.clipRect(new Rectangle(x, y, width, height));
         }

         // $FF: synthetic method
         ClipRectHandler(Object x1) {
            this();
         }
      }

      private class TextHandler extends AbstractElementHandler {
         private TextHandler() {
            super(null);
         }

         public void endElement() throws IFException {
            int x = Integer.parseInt(Handler.this.lastAttributes.getValue("x"));
            int y = Integer.parseInt(Handler.this.lastAttributes.getValue("y"));
            String s = Handler.this.lastAttributes.getValue("letter-spacing");
            int letterSpacing = s != null ? Integer.parseInt(s) : 0;
            s = Handler.this.lastAttributes.getValue("word-spacing");
            int wordSpacing = s != null ? Integer.parseInt(s) : 0;
            int[] dx = XMLUtil.getAttributeAsIntArray(Handler.this.lastAttributes, "dx");
            int[][] dp = XMLUtil.getAttributeAsPositionAdjustments(Handler.this.lastAttributes, "dp");
            if (dp == null && dx != null) {
               dp = IFUtil.convertDXToDP(dx);
            }

            Handler.this.establishStructureTreeElement(Handler.this.lastAttributes);
            boolean isHyphenated = Boolean.valueOf(Handler.this.lastAttributes.getValue("hyphenated"));
            if (isHyphenated) {
               Handler.this.documentHandler.getContext().setHyphenated(isHyphenated);
            }

            boolean nextIsSpace = Boolean.valueOf(Handler.this.lastAttributes.getValue("next-is-space"));
            Handler.this.painter.drawText(x, y, letterSpacing, wordSpacing, dp, Handler.this.content.toString(), nextIsSpace);
            Handler.this.documentHandler.getContext().setHyphenated(false);
            Handler.this.resetStructureTreeElement();
         }

         public boolean ignoreCharacters() {
            return false;
         }

         // $FF: synthetic method
         TextHandler(Object x1) {
            this();
         }
      }

      private class FontHandler extends AbstractElementHandler {
         private FontHandler() {
            super(null);
         }

         public void startElement(Attributes attributes) throws IFException {
            String family = attributes.getValue("family");
            String style = attributes.getValue("style");
            Integer weight = XMLUtil.getAttributeAsInteger(attributes, "weight");
            String variant = attributes.getValue("variant");
            Integer size = XMLUtil.getAttributeAsInteger(attributes, "size");

            Color color;
            try {
               color = Handler.this.getAttributeAsColor(attributes, "color");
            } catch (PropertyException var9) {
               throw new IFException("Error parsing the color attribute", var9);
            }

            Handler.this.painter.setFont(family, style, weight, variant, size, color);
         }

         // $FF: synthetic method
         FontHandler(Object x1) {
            this();
         }
      }

      private class IDHandler extends AbstractElementHandler {
         private IDHandler() {
            super(null);
         }

         public void startElement(Attributes attributes) throws IFException, SAXException {
            String id = attributes.getValue("name");
            Handler.this.documentHandler.getContext().setID(id);
         }

         // $FF: synthetic method
         IDHandler(Object x1) {
            this();
         }
      }

      private class GroupHandler extends AbstractElementHandler {
         private GroupHandler() {
            super(null);
         }

         public void startElement(Attributes attributes) throws IFException {
            String transform = attributes.getValue("transform");
            AffineTransform[] transforms = AffineTransformArrayParser.createAffineTransform(transform);
            String layer = attributes.getValue("layer");
            Handler.this.painter.startGroup(transforms, layer);
         }

         public void endElement() throws IFException {
            Handler.this.painter.endGroup();
         }

         // $FF: synthetic method
         GroupHandler(Object x1) {
            this();
         }
      }

      private class ViewportHandler extends AbstractElementHandler {
         private ViewportHandler() {
            super(null);
         }

         public void startElement(Attributes attributes) throws IFException {
            String transform = attributes.getValue("transform");
            AffineTransform[] transforms = AffineTransformArrayParser.createAffineTransform(transform);
            int width = Integer.parseInt(attributes.getValue("width"));
            int height = Integer.parseInt(attributes.getValue("height"));
            Rectangle clipRect = XMLUtil.getAttributeAsRectangle(attributes, "clip-rect");
            Handler.this.painter.startViewport(transforms, new Dimension(width, height), clipRect);
            Handler.this.documentHandler.getContext().setRegionType(attributes.getValue("region-type"));
         }

         public void endElement() throws IFException {
            Handler.this.painter.endViewport();
         }

         // $FF: synthetic method
         ViewportHandler(Object x1) {
            this();
         }
      }

      private class PageTrailerHandler extends AbstractElementHandler {
         private PageTrailerHandler() {
            super(null);
         }

         public void startElement(Attributes attributes) throws IFException {
            Handler.this.documentHandler.startPageTrailer();
         }

         public void endElement() throws IFException {
            Handler.this.documentHandler.endPageTrailer();
         }

         // $FF: synthetic method
         PageTrailerHandler(Object x1) {
            this();
         }
      }

      private class PageContentHandler extends AbstractElementHandler {
         private PageContentHandler() {
            super(null);
         }

         public void startElement(Attributes attributes) throws IFException {
            Handler.this.painter = Handler.this.documentHandler.startPageContent();
         }

         public void endElement() throws IFException {
            Handler.this.painter = null;
            Handler.this.documentHandler.getContext().setID("");
            Handler.this.documentHandler.endPageContent();
         }

         // $FF: synthetic method
         PageContentHandler(Object x1) {
            this();
         }
      }

      private class PageHeaderHandler extends AbstractElementHandler {
         private PageHeaderHandler() {
            super(null);
         }

         public void startElement(Attributes attributes) throws IFException {
            Handler.this.documentHandler.startPageHeader();
            Handler.this.structureTreeHandler = Handler.this.new StructureTreeHandler(Handler.this.userAgent.getStructureTreeEventHandler());
         }

         public void endElement() throws IFException {
            Handler.this.documentHandler.endPageHeader();
         }

         // $FF: synthetic method
         PageHeaderHandler(Object x1) {
            this();
         }
      }

      private class PageHandler extends AbstractElementHandler {
         private PageHandler() {
            super(null);
         }

         public void startElement(Attributes attributes) throws IFException {
            int index = Integer.parseInt(attributes.getValue("index"));
            String name = attributes.getValue("name");
            String pageMasterName = attributes.getValue("page-master-name");
            int width = Integer.parseInt(attributes.getValue("width"));
            int height = Integer.parseInt(attributes.getValue("height"));
            Map foreignAttributes = IFParser.Handler.getForeignAttributes(Handler.this.lastAttributes);
            Handler.this.establishForeignAttributes(foreignAttributes);
            Handler.this.documentHandler.startPage(index, name, pageMasterName, new Dimension(width, height));
            Handler.this.documentHandler.getContext().setPageNumber(index + 1);
            Handler.this.resetForeignAttributes();
         }

         public void endElement() throws IFException {
            Handler.this.documentHandler.endPage();
         }

         // $FF: synthetic method
         PageHandler(Object x1) {
            this();
         }
      }

      private class PageSequenceHandler extends AbstractElementHandler {
         private PageSequenceHandler() {
            super(null);
         }

         public void startElement(Attributes attributes) throws IFException {
            String id = attributes.getValue("id");
            Locale language = IFParser.Handler.getLanguage(attributes);
            if (language != null) {
               Handler.this.documentHandler.getContext().setLanguage(language);
            }

            Map foreignAttributes = IFParser.Handler.getForeignAttributes(Handler.this.lastAttributes);
            Handler.this.establishForeignAttributes(foreignAttributes);
            Handler.this.documentHandler.startPageSequence(id);
            Handler.this.resetForeignAttributes();
         }

         public void endElement() throws IFException {
            Handler.this.documentHandler.endPageSequence();
            Handler.this.documentHandler.getContext().setLanguage((Locale)null);
         }

         // $FF: synthetic method
         PageSequenceHandler(Object x1) {
            this();
         }
      }

      private class DocumentTrailerHandler extends AbstractElementHandler {
         private DocumentTrailerHandler() {
            super(null);
         }

         public void startElement(Attributes attributes) throws IFException {
            Handler.this.documentHandler.startDocumentTrailer();
         }

         public void endElement() throws IFException {
            Handler.this.documentHandler.endDocumentTrailer();
         }

         // $FF: synthetic method
         DocumentTrailerHandler(Object x1) {
            this();
         }
      }

      private class LocaleHandler extends AbstractElementHandler {
         private LocaleHandler() {
            super(null);
         }

         public void startElement(Attributes attributes) throws IFException {
            Handler.this.documentHandler.setDocumentLocale(IFParser.Handler.getLanguage(attributes));
         }

         // $FF: synthetic method
         LocaleHandler(Object x1) {
            this();
         }
      }

      private class DocumentHeaderHandler extends AbstractElementHandler {
         private DocumentHeaderHandler() {
            super(null);
         }

         public void startElement(Attributes attributes) throws IFException {
            Handler.this.documentHandler.startDocumentHeader();
         }

         public void endElement() throws IFException {
            Handler.this.documentHandler.endDocumentHeader();
         }

         // $FF: synthetic method
         DocumentHeaderHandler(Object x1) {
            this();
         }
      }

      private class DocumentHandler extends AbstractElementHandler {
         private DocumentHandler() {
            super(null);
         }

         public void startElement(Attributes attributes) throws IFException {
            Handler.this.documentHandler.startDocument();
         }

         public void endElement() throws IFException {
            Handler.this.documentHandler.endDocument();
         }

         // $FF: synthetic method
         DocumentHandler(Object x1) {
            this();
         }
      }

      private abstract class AbstractElementHandler implements ElementHandler {
         private AbstractElementHandler() {
         }

         public void startElement(Attributes attributes) throws IFException, SAXException {
         }

         public void endElement() throws IFException {
         }

         public boolean ignoreCharacters() {
            return true;
         }

         // $FF: synthetic method
         AbstractElementHandler(Object x1) {
            this();
         }
      }

      private interface ElementHandler {
         void startElement(Attributes var1) throws IFException, SAXException;

         void endElement() throws IFException;

         boolean ignoreCharacters();
      }

      private class MainStructureTreeHandler extends StructureTreeHandler {
         private final Locale pageSequenceLanguage;

         MainStructureTreeHandler(StructureTreeEventHandler structureTreeEventHandler, Locale pageSequenceLanguage) throws SAXException {
            super(structureTreeEventHandler);
            this.pageSequenceLanguage = pageSequenceLanguage;
         }

         void startStructureTree(String type) {
            this.structureTreeEventHandler.startPageSequence(this.pageSequenceLanguage, type);
         }

         public void endDocument() throws SAXException {
            Handler.this.startIFElement("page-sequence", Handler.this.pageSequenceAttributes);
            Handler.this.pageSequenceAttributes = null;
         }
      }

      private class StructureTreeHandler extends DefaultHandler {
         protected final StructureTreeEventHandler structureTreeEventHandler;

         StructureTreeHandler(StructureTreeEventHandler structureTreeEventHandler) {
            this.structureTreeEventHandler = structureTreeEventHandler;
         }

         void startStructureTree(String type) {
         }

         public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (!"structure-tree".equals(localName)) {
               if (localName.equals("marked-content")) {
                  localName = "#PCDATA";
               }

               StructureTreeElement parent = Handler.this.getStructureTreeElement(attributes);
               String structID = attributes.getValue("http://xmlgraphics.apache.org/fop/internal", "struct-id");
               if (structID == null) {
                  this.structureTreeEventHandler.startNode(localName, attributes, parent);
               } else {
                  StructureTreeElement structureTreeElement;
                  if (!localName.equals("external-graphic") && !localName.equals("instream-foreign-object")) {
                     structureTreeElement = this.structureTreeEventHandler.startReferencedNode(localName, attributes, parent);
                     Handler.this.structureTreeElements.put(structID, structureTreeElement);
                  } else {
                     structureTreeElement = this.structureTreeEventHandler.startImageNode(localName, attributes, parent);
                     Handler.this.structureTreeElements.put(structID, structureTreeElement);
                  }
               }
            }

         }

         public void endElement(String uri, String localName, String arqNameg2) throws SAXException {
            if (!"structure-tree".equals(localName)) {
               this.structureTreeEventHandler.endNode(localName);
            }

         }
      }
   }
}
