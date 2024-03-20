package org.apache.batik.anim.dom;

import java.net.URL;
import java.util.HashMap;
import org.apache.batik.css.engine.CSSContext;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.SVG12CSSEngine;
import org.apache.batik.css.engine.value.ShorthandManager;
import org.apache.batik.css.engine.value.ValueManager;
import org.apache.batik.css.parser.ExtendedParser;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.AbstractNode;
import org.apache.batik.dom.AbstractStylableDocument;
import org.apache.batik.dom.ExtensibleDOMImplementation;
import org.apache.batik.dom.GenericElement;
import org.apache.batik.dom.events.DocumentEventSupport;
import org.apache.batik.dom.events.EventSupport;
import org.apache.batik.dom.svg12.SVGOMWheelEvent;
import org.apache.batik.dom.svg12.XBLOMShadowTreeEvent;
import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.util.ParsedURL;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;

public class SVG12DOMImplementation extends SVGDOMImplementation {
   protected static HashMap svg12Factories;
   protected static HashMap xblFactories;
   protected static final DOMImplementation DOM_IMPLEMENTATION;

   public SVG12DOMImplementation() {
      this.factories = svg12Factories;
      this.registerFeature("CSS", "2.0");
      this.registerFeature("StyleSheets", "2.0");
      this.registerFeature("SVG", new String[]{"1.0", "1.1", "1.2"});
      this.registerFeature("SVGEvents", new String[]{"1.0", "1.1", "1.2"});
   }

   public CSSEngine createCSSEngine(AbstractStylableDocument doc, CSSContext ctx, ExtendedParser ep, ValueManager[] vms, ShorthandManager[] sms) {
      ParsedURL durl = ((SVGOMDocument)doc).getParsedURL();
      CSSEngine result = new SVG12CSSEngine(doc, durl, ep, vms, sms, ctx);
      URL url = this.getClass().getResource("resources/UserAgentStyleSheet.css");
      if (url != null) {
         ParsedURL purl = new ParsedURL(url);
         InputSource is = new InputSource(purl.toString());
         result.setUserAgentStyleSheet(result.parseStyleSheet(is, purl, "all"));
      }

      return result;
   }

   public Document createDocument(String namespaceURI, String qualifiedName, DocumentType doctype) throws DOMException {
      SVGOMDocument result = new SVG12OMDocument(doctype, this);
      result.setIsSVG12(true);
      if (qualifiedName != null) {
         result.appendChild(result.createElementNS(namespaceURI, qualifiedName));
      }

      return result;
   }

   public Element createElementNS(AbstractDocument document, String namespaceURI, String qualifiedName) {
      if (namespaceURI == null) {
         return new GenericElement(qualifiedName.intern(), document);
      } else {
         String name = DOMUtilities.getLocalName(qualifiedName);
         String prefix = DOMUtilities.getPrefix(qualifiedName);
         ExtensibleDOMImplementation.ElementFactory cef;
         if ("http://www.w3.org/2000/svg".equals(namespaceURI)) {
            cef = (ExtensibleDOMImplementation.ElementFactory)this.factories.get(name);
            if (cef != null) {
               return cef.create(prefix, document);
            }
         } else if ("http://www.w3.org/2004/xbl".equals(namespaceURI)) {
            cef = (ExtensibleDOMImplementation.ElementFactory)xblFactories.get(name);
            if (cef != null) {
               return cef.create(prefix, document);
            }
         }

         if (this.customFactories != null) {
            cef = (ExtensibleDOMImplementation.ElementFactory)this.customFactories.get(namespaceURI, name);
            if (cef != null) {
               return cef.create(prefix, document);
            }
         }

         return new BindableElement(prefix, document, namespaceURI, name);
      }
   }

   public DocumentEventSupport createDocumentEventSupport() {
      DocumentEventSupport result = super.createDocumentEventSupport();
      result.registerEventFactory("WheelEvent", new DocumentEventSupport.EventFactory() {
         public Event createEvent() {
            return new SVGOMWheelEvent();
         }
      });
      result.registerEventFactory("ShadowTreeEvent", new DocumentEventSupport.EventFactory() {
         public Event createEvent() {
            return new XBLOMShadowTreeEvent();
         }
      });
      return result;
   }

   public EventSupport createEventSupport(AbstractNode n) {
      return new XBLEventSupport(n);
   }

   public static DOMImplementation getDOMImplementation() {
      return DOM_IMPLEMENTATION;
   }

   static {
      svg12Factories = new HashMap(svg11Factories);
      svg12Factories.put("flowDiv", new FlowDivElementFactory());
      svg12Factories.put("flowLine", new FlowLineElementFactory());
      svg12Factories.put("flowPara", new FlowParaElementFactory());
      svg12Factories.put("flowRegionBreak", new FlowRegionBreakElementFactory());
      svg12Factories.put("flowRegion", new FlowRegionElementFactory());
      svg12Factories.put("flowRegionExclude", new FlowRegionExcludeElementFactory());
      svg12Factories.put("flowRoot", new FlowRootElementFactory());
      svg12Factories.put("flowSpan", new FlowSpanElementFactory());
      svg12Factories.put("handler", new HandlerElementFactory());
      svg12Factories.put("multiImage", new MultiImageElementFactory());
      svg12Factories.put("solidColor", new SolidColorElementFactory());
      svg12Factories.put("subImage", new SubImageElementFactory());
      svg12Factories.put("subImageRef", new SubImageRefElementFactory());
      xblFactories = new HashMap();
      xblFactories.put("xbl", new XBLXBLElementFactory());
      xblFactories.put("definition", new XBLDefinitionElementFactory());
      xblFactories.put("template", new XBLTemplateElementFactory());
      xblFactories.put("content", new XBLContentElementFactory());
      xblFactories.put("handlerGroup", new XBLHandlerGroupElementFactory());
      xblFactories.put("import", new XBLImportElementFactory());
      xblFactories.put("shadowTree", new XBLShadowTreeElementFactory());
      DOM_IMPLEMENTATION = new SVG12DOMImplementation();
   }

   protected static class XBLShadowTreeElementFactory implements ExtensibleDOMImplementation.ElementFactory {
      public XBLShadowTreeElementFactory() {
      }

      public Element create(String prefix, Document doc) {
         return new XBLOMShadowTreeElement(prefix, (AbstractDocument)doc);
      }
   }

   protected static class XBLImportElementFactory implements ExtensibleDOMImplementation.ElementFactory {
      public XBLImportElementFactory() {
      }

      public Element create(String prefix, Document doc) {
         return new XBLOMImportElement(prefix, (AbstractDocument)doc);
      }
   }

   protected static class XBLHandlerGroupElementFactory implements ExtensibleDOMImplementation.ElementFactory {
      public XBLHandlerGroupElementFactory() {
      }

      public Element create(String prefix, Document doc) {
         return new XBLOMHandlerGroupElement(prefix, (AbstractDocument)doc);
      }
   }

   protected static class XBLContentElementFactory implements ExtensibleDOMImplementation.ElementFactory {
      public XBLContentElementFactory() {
      }

      public Element create(String prefix, Document doc) {
         return new XBLOMContentElement(prefix, (AbstractDocument)doc);
      }
   }

   protected static class XBLTemplateElementFactory implements ExtensibleDOMImplementation.ElementFactory {
      public XBLTemplateElementFactory() {
      }

      public Element create(String prefix, Document doc) {
         return new XBLOMTemplateElement(prefix, (AbstractDocument)doc);
      }
   }

   protected static class XBLDefinitionElementFactory implements ExtensibleDOMImplementation.ElementFactory {
      public XBLDefinitionElementFactory() {
      }

      public Element create(String prefix, Document doc) {
         return new XBLOMDefinitionElement(prefix, (AbstractDocument)doc);
      }
   }

   protected static class XBLXBLElementFactory implements ExtensibleDOMImplementation.ElementFactory {
      public XBLXBLElementFactory() {
      }

      public Element create(String prefix, Document doc) {
         return new XBLOMXBLElement(prefix, (AbstractDocument)doc);
      }
   }

   protected static class SubImageRefElementFactory implements ExtensibleDOMImplementation.ElementFactory {
      public SubImageRefElementFactory() {
      }

      public Element create(String prefix, Document doc) {
         return new SVGOMSubImageRefElement(prefix, (AbstractDocument)doc);
      }
   }

   protected static class SubImageElementFactory implements ExtensibleDOMImplementation.ElementFactory {
      public SubImageElementFactory() {
      }

      public Element create(String prefix, Document doc) {
         return new SVGOMSubImageElement(prefix, (AbstractDocument)doc);
      }
   }

   protected static class SolidColorElementFactory implements ExtensibleDOMImplementation.ElementFactory {
      public SolidColorElementFactory() {
      }

      public Element create(String prefix, Document doc) {
         return new SVGOMSolidColorElement(prefix, (AbstractDocument)doc);
      }
   }

   protected static class MultiImageElementFactory implements ExtensibleDOMImplementation.ElementFactory {
      public MultiImageElementFactory() {
      }

      public Element create(String prefix, Document doc) {
         return new SVGOMMultiImageElement(prefix, (AbstractDocument)doc);
      }
   }

   protected static class HandlerElementFactory implements ExtensibleDOMImplementation.ElementFactory {
      public HandlerElementFactory() {
      }

      public Element create(String prefix, Document doc) {
         return new SVGOMHandlerElement(prefix, (AbstractDocument)doc);
      }
   }

   protected static class FlowSpanElementFactory implements ExtensibleDOMImplementation.ElementFactory {
      public FlowSpanElementFactory() {
      }

      public Element create(String prefix, Document doc) {
         return new SVGOMFlowSpanElement(prefix, (AbstractDocument)doc);
      }
   }

   protected static class FlowRootElementFactory implements ExtensibleDOMImplementation.ElementFactory {
      public FlowRootElementFactory() {
      }

      public Element create(String prefix, Document doc) {
         return new SVGOMFlowRootElement(prefix, (AbstractDocument)doc);
      }
   }

   protected static class FlowRegionExcludeElementFactory implements ExtensibleDOMImplementation.ElementFactory {
      public FlowRegionExcludeElementFactory() {
      }

      public Element create(String prefix, Document doc) {
         return new SVGOMFlowRegionExcludeElement(prefix, (AbstractDocument)doc);
      }
   }

   protected static class FlowRegionElementFactory implements ExtensibleDOMImplementation.ElementFactory {
      public FlowRegionElementFactory() {
      }

      public Element create(String prefix, Document doc) {
         return new SVGOMFlowRegionElement(prefix, (AbstractDocument)doc);
      }
   }

   protected static class FlowRegionBreakElementFactory implements ExtensibleDOMImplementation.ElementFactory {
      public FlowRegionBreakElementFactory() {
      }

      public Element create(String prefix, Document doc) {
         return new SVGOMFlowRegionBreakElement(prefix, (AbstractDocument)doc);
      }
   }

   protected static class FlowParaElementFactory implements ExtensibleDOMImplementation.ElementFactory {
      public FlowParaElementFactory() {
      }

      public Element create(String prefix, Document doc) {
         return new SVGOMFlowParaElement(prefix, (AbstractDocument)doc);
      }
   }

   protected static class FlowLineElementFactory implements ExtensibleDOMImplementation.ElementFactory {
      public FlowLineElementFactory() {
      }

      public Element create(String prefix, Document doc) {
         return new SVGOMFlowLineElement(prefix, (AbstractDocument)doc);
      }
   }

   protected static class FlowDivElementFactory implements ExtensibleDOMImplementation.ElementFactory {
      public FlowDivElementFactory() {
      }

      public Element create(String prefix, Document doc) {
         return new SVGOMFlowDivElement(prefix, (AbstractDocument)doc);
      }
   }
}
