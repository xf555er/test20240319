package org.apache.batik.extension.svg;

import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.DomExtension;
import org.apache.batik.dom.ExtensibleDOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class BatikDomExtension implements DomExtension, BatikExtConstants {
   public float getPriority() {
      return 1.0F;
   }

   public String getAuthor() {
      return "Thomas DeWeese";
   }

   public String getContactAddress() {
      return "deweese@apache.org";
   }

   public String getURL() {
      return "http://xml.apache.org/batik";
   }

   public String getDescription() {
      return "Example extension to standard SVG shape tags";
   }

   public void registerTags(ExtensibleDOMImplementation di) {
      di.registerCustomElementFactory("http://xml.apache.org/batik/ext", "regularPolygon", new BatikRegularPolygonElementFactory());
      di.registerCustomElementFactory("http://xml.apache.org/batik/ext", "star", new BatikStarElementFactory());
      di.registerCustomElementFactory("http://xml.apache.org/batik/ext", "histogramNormalization", new BatikHistogramNormalizationElementFactory());
      di.registerCustomElementFactory("http://xml.apache.org/batik/ext", "colorSwitch", new ColorSwitchElementFactory());
      di.registerCustomElementFactory("http://xml.apache.org/batik/ext", "flowText", new FlowTextElementFactory());
      di.registerCustomElementFactory("http://xml.apache.org/batik/ext", "flowDiv", new FlowDivElementFactory());
      di.registerCustomElementFactory("http://xml.apache.org/batik/ext", "flowPara", new FlowParaElementFactory());
      di.registerCustomElementFactory("http://xml.apache.org/batik/ext", "flowRegionBreak", new FlowRegionBreakElementFactory());
      di.registerCustomElementFactory("http://xml.apache.org/batik/ext", "flowRegion", new FlowRegionElementFactory());
      di.registerCustomElementFactory("http://xml.apache.org/batik/ext", "flowLine", new FlowLineElementFactory());
      di.registerCustomElementFactory("http://xml.apache.org/batik/ext", "flowSpan", new FlowSpanElementFactory());
   }

   protected static class FlowSpanElementFactory implements ExtensibleDOMImplementation.ElementFactory {
      public FlowSpanElementFactory() {
      }

      public Element create(String prefix, Document doc) {
         return new FlowSpanElement(prefix, (AbstractDocument)doc);
      }
   }

   protected static class FlowLineElementFactory implements ExtensibleDOMImplementation.ElementFactory {
      public FlowLineElementFactory() {
      }

      public Element create(String prefix, Document doc) {
         return new FlowLineElement(prefix, (AbstractDocument)doc);
      }
   }

   protected static class FlowRegionElementFactory implements ExtensibleDOMImplementation.ElementFactory {
      public FlowRegionElementFactory() {
      }

      public Element create(String prefix, Document doc) {
         return new FlowRegionElement(prefix, (AbstractDocument)doc);
      }
   }

   protected static class FlowRegionBreakElementFactory implements ExtensibleDOMImplementation.ElementFactory {
      public FlowRegionBreakElementFactory() {
      }

      public Element create(String prefix, Document doc) {
         return new FlowRegionBreakElement(prefix, (AbstractDocument)doc);
      }
   }

   protected static class FlowParaElementFactory implements ExtensibleDOMImplementation.ElementFactory {
      public FlowParaElementFactory() {
      }

      public Element create(String prefix, Document doc) {
         return new FlowParaElement(prefix, (AbstractDocument)doc);
      }
   }

   protected static class FlowDivElementFactory implements ExtensibleDOMImplementation.ElementFactory {
      public FlowDivElementFactory() {
      }

      public Element create(String prefix, Document doc) {
         return new FlowDivElement(prefix, (AbstractDocument)doc);
      }
   }

   protected static class FlowTextElementFactory implements ExtensibleDOMImplementation.ElementFactory {
      public FlowTextElementFactory() {
      }

      public Element create(String prefix, Document doc) {
         return new FlowTextElement(prefix, (AbstractDocument)doc);
      }
   }

   protected static class ColorSwitchElementFactory implements ExtensibleDOMImplementation.ElementFactory {
      public ColorSwitchElementFactory() {
      }

      public Element create(String prefix, Document doc) {
         return new ColorSwitchElement(prefix, (AbstractDocument)doc);
      }
   }

   protected static class BatikHistogramNormalizationElementFactory implements ExtensibleDOMImplementation.ElementFactory {
      public BatikHistogramNormalizationElementFactory() {
      }

      public Element create(String prefix, Document doc) {
         return new BatikHistogramNormalizationElement(prefix, (AbstractDocument)doc);
      }
   }

   protected static class BatikStarElementFactory implements ExtensibleDOMImplementation.ElementFactory {
      public BatikStarElementFactory() {
      }

      public Element create(String prefix, Document doc) {
         return new BatikStarElement(prefix, (AbstractDocument)doc);
      }
   }

   protected static class BatikRegularPolygonElementFactory implements ExtensibleDOMImplementation.ElementFactory {
      public BatikRegularPolygonElementFactory() {
      }

      public Element create(String prefix, Document doc) {
         return new BatikRegularPolygonElement(prefix, (AbstractDocument)doc);
      }
   }
}
