package org.apache.fop.fo.properties;

import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.PropertyException;

public final class CommonAccessibility {
   private static final CommonAccessibility DEFAULT_INSTANCE = new CommonAccessibility((String)null, (String)null);
   private final String sourceDocument;
   private final String role;

   private CommonAccessibility(String sourceDocument, String role) {
      this.sourceDocument = sourceDocument;
      this.role = role;
   }

   public static CommonAccessibility getInstance(PropertyList propertyList) throws PropertyException {
      String sourceDocument = propertyList.get(221).getString();
      if ("none".equals(sourceDocument)) {
         sourceDocument = null;
      }

      String role = propertyList.get(212).getString();
      if ("none".equals(role)) {
         role = null;
      }

      return sourceDocument == null && role == null ? DEFAULT_INSTANCE : new CommonAccessibility(sourceDocument, role);
   }

   public String getSourceDocument() {
      return this.sourceDocument;
   }

   public String getRole() {
      return this.role;
   }
}
