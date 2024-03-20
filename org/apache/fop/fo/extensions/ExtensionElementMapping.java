package org.apache.fop.fo.extensions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.apache.fop.fo.ElementMapping;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.UnknownXMLObj;
import org.apache.fop.fo.extensions.destination.Destination;
import org.apache.xmlgraphics.util.QName;

public class ExtensionElementMapping extends ElementMapping {
   public static final String URI = "http://xmlgraphics.apache.org/fop/extensions";
   public static final String STANDARD_PREFIX = "fox";
   private static final Set PROPERTY_ATTRIBUTES = new HashSet();

   public ExtensionElementMapping() {
      this.namespaceURI = "http://xmlgraphics.apache.org/fop/extensions";
   }

   protected void initialize() {
      if (this.foObjs == null) {
         this.foObjs = new HashMap();
         this.foObjs.put("outline", new UnknownXMLObj.Maker("http://xmlgraphics.apache.org/fop/extensions"));
         this.foObjs.put("label", new UnknownXMLObj.Maker("http://xmlgraphics.apache.org/fop/extensions"));
         this.foObjs.put("destination", new DestinationMaker());
         this.foObjs.put("external-document", new ExternalDocumentMaker());
      }

   }

   public String getStandardPrefix() {
      return "fox";
   }

   public boolean isAttributeProperty(QName attributeName) {
      if (!"http://xmlgraphics.apache.org/fop/extensions".equals(attributeName.getNamespaceURI())) {
         throw new IllegalArgumentException("The namespace URIs don't match");
      } else {
         return PROPERTY_ATTRIBUTES.contains(attributeName.getLocalName());
      }
   }

   static {
      PROPERTY_ATTRIBUTES.add("block-progression-unit");
      PROPERTY_ATTRIBUTES.add("widow-content-limit");
      PROPERTY_ATTRIBUTES.add("orphan-content-limit");
      PROPERTY_ATTRIBUTES.add("internal-destination");
      PROPERTY_ATTRIBUTES.add("disable-column-balancing");
      PROPERTY_ATTRIBUTES.add("auto-toggle");
      PROPERTY_ATTRIBUTES.add("alt-text");
      PROPERTY_ATTRIBUTES.add("header");
      PROPERTY_ATTRIBUTES.add("abbreviation");
      PROPERTY_ATTRIBUTES.add("border-before-radius-start");
      PROPERTY_ATTRIBUTES.add("border-before-radius-end");
      PROPERTY_ATTRIBUTES.add("border-after-radius-start");
      PROPERTY_ATTRIBUTES.add("border-after-radius-end");
      PROPERTY_ATTRIBUTES.add("border-start-radius-before");
      PROPERTY_ATTRIBUTES.add("border-start-radius-after");
      PROPERTY_ATTRIBUTES.add("border-end-radius-before");
      PROPERTY_ATTRIBUTES.add("border-end-radius-after");
      PROPERTY_ATTRIBUTES.add("border-radius");
      PROPERTY_ATTRIBUTES.add("border-before-start-radius");
      PROPERTY_ATTRIBUTES.add("border-before-end-radius");
      PROPERTY_ATTRIBUTES.add("border-after-start-radius");
      PROPERTY_ATTRIBUTES.add("border-after-end-radius");
      PROPERTY_ATTRIBUTES.add("layer");
      PROPERTY_ATTRIBUTES.add("background-image-width");
      PROPERTY_ATTRIBUTES.add("background-image-height");
   }

   static class ExternalDocumentMaker extends ElementMapping.Maker {
      public FONode make(FONode parent) {
         return new ExternalDocument(parent);
      }
   }

   static class DestinationMaker extends ElementMapping.Maker {
      public FONode make(FONode parent) {
         return new Destination(parent);
      }
   }
}
