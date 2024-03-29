package org.apache.fop.fo.extensions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.apache.fop.fo.ElementMapping;
import org.apache.xmlgraphics.util.QName;

public class InternalElementMapping extends ElementMapping {
   public static final String URI = "http://xmlgraphics.apache.org/fop/internal";
   public static final String STANDARD_PREFIX = "foi";
   public static final String STRUCT_ID = "struct-id";
   public static final String STRUCT_REF = "struct-ref";
   public static final String SCOPE = "scope";
   private static final Set PROPERTY_ATTRIBUTES = new HashSet();

   public InternalElementMapping() {
      this.namespaceURI = "http://xmlgraphics.apache.org/fop/internal";
   }

   protected void initialize() {
      if (this.foObjs == null) {
         this.foObjs = new HashMap();
      }

   }

   public String getStandardPrefix() {
      return "foi";
   }

   public boolean isAttributeProperty(QName attributeName) {
      if (!"http://xmlgraphics.apache.org/fop/internal".equals(attributeName.getNamespaceURI())) {
         throw new IllegalArgumentException("The namespace URIs don't match");
      } else {
         return PROPERTY_ATTRIBUTES.contains(attributeName.getLocalName());
      }
   }

   static {
      PROPERTY_ATTRIBUTES.add("struct-id");
      PROPERTY_ATTRIBUTES.add("struct-ref");
      PROPERTY_ATTRIBUTES.add("scope");
   }
}
