package org.apache.fop.fo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.properties.CommonAbsolutePosition;
import org.apache.fop.fo.properties.CommonAural;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.CommonFont;
import org.apache.fop.fo.properties.CommonHyphenation;
import org.apache.fop.fo.properties.CommonMarginBlock;
import org.apache.fop.fo.properties.CommonMarginInline;
import org.apache.fop.fo.properties.CommonRelativePosition;
import org.apache.fop.fo.properties.CommonTextDecoration;
import org.apache.fop.fo.properties.Property;
import org.apache.fop.fo.properties.PropertyMaker;
import org.apache.xmlgraphics.util.QName;
import org.xml.sax.Attributes;

public abstract class PropertyList {
   private static boolean[] inheritableProperty = new boolean[295];
   protected PropertyList parentPropertyList;
   private FObj fobj;
   private static Log log;
   private final UnknownPropertyHandler unknownPropertyHandler = new UnknownPropertyHandler();

   public PropertyList(FObj fObjToAttach, PropertyList parentPropertyList) {
      this.fobj = fObjToAttach;
      this.parentPropertyList = parentPropertyList;
   }

   public FObj getFObj() {
      return this.fobj;
   }

   public FObj getParentFObj() {
      return this.parentPropertyList != null ? this.parentPropertyList.getFObj() : null;
   }

   public void validatePropertyValue(String propertyValue, Property output, Property property) {
      this.unknownPropertyHandler.validatePropertyValue(propertyValue, output, property);
   }

   public Map getUnknownPropertyValues() {
      return this.unknownPropertyHandler.getUnknownPropertyValues();
   }

   public PropertyList getParentPropertyList() {
      return this.parentPropertyList;
   }

   public Property getExplicitOrShorthand(int propId) throws PropertyException {
      Property p = this.getExplicit(propId);
      if (p == null) {
         p = this.getShorthand(propId);
      }

      return p;
   }

   public abstract Property getExplicit(int var1);

   public abstract void putExplicit(int var1, Property var2);

   public Property getInherited(int propId) throws PropertyException {
      return this.isInherited(propId) ? this.getFromParent(propId) : this.makeProperty(propId);
   }

   public Property get(int propId) throws PropertyException {
      return this.get(propId, true, true);
   }

   public Property get(int propId, boolean bTryInherit, boolean bTryDefault) throws PropertyException {
      PropertyMaker propertyMaker = findMaker(propId & 511);
      return propertyMaker != null ? propertyMaker.get(propId & -512, this, bTryInherit, bTryDefault) : null;
   }

   public Property getNearestSpecified(int propId) throws PropertyException {
      Property p = null;

      for(PropertyList pList = this.parentPropertyList; pList != null; pList = pList.parentPropertyList) {
         p = pList.getExplicit(propId);
         if (p != null) {
            return p;
         }
      }

      return this.makeProperty(propId);
   }

   public Property getFromParent(int propId) throws PropertyException {
      return this.parentPropertyList != null ? this.parentPropertyList.get(propId) : this.makeProperty(propId);
   }

   public int selectFromWritingMode(int lrtb, int rltb, int tbrl, int tblr) {
      int propID;
      try {
         switch (this.get(267).getEnum()) {
            case 79:
               propID = lrtb;
               break;
            case 121:
               propID = rltb;
               break;
            case 140:
               propID = tbrl;
               break;
            case 203:
               propID = tblr;
               break;
            default:
               propID = -1;
         }
      } catch (PropertyException var7) {
         propID = -1;
      }

      return propID;
   }

   private String addAttributeToList(Attributes attributes, String attributeName) throws ValidationException {
      String attributeValue = attributes.getValue(attributeName);
      if (attributeValue != null) {
         this.convertAttributeToProperty(attributes, attributeName, attributeValue);
      }

      return attributeValue;
   }

   public void addAttributesToList(Attributes attributes) throws ValidationException {
      this.addAttributeToList(attributes, "writing-mode");
      this.addAttributeToList(attributes, "column-number");
      this.addAttributeToList(attributes, "number-columns-spanned");
      String checkValue = this.addAttributeToList(attributes, "font");
      if (checkValue == null || "".equals(checkValue)) {
         this.addAttributeToList(attributes, "font-size");
      }

      FOUserAgent userAgent = this.getFObj().getUserAgent();

      for(int i = 0; i < attributes.getLength(); ++i) {
         String attributeNS = attributes.getURI(i);
         String attributeName = attributes.getQName(i);
         String attributeValue = attributes.getValue(i);
         if (attributeNS != null && attributeNS.length() != 0 && !"xml:lang".equals(attributeName) && !"xml:base".equals(attributeName)) {
            if (!userAgent.isNamespaceIgnored(attributeNS)) {
               ElementMapping mapping = userAgent.getElementMappingRegistry().getElementMapping(attributeNS);
               QName attr = new QName(attributeNS, attributeName);
               if (mapping != null) {
                  if (mapping.isAttributeProperty(attr) && mapping.getStandardPrefix() != null) {
                     this.convertAttributeToProperty(attributes, mapping.getStandardPrefix() + ":" + attr.getLocalName(), attributeValue);
                  } else {
                     this.getFObj().addForeignAttribute(attr, attributeValue);
                  }
               } else {
                  this.handleInvalidProperty(attr);
               }
            }
         } else {
            this.convertAttributeToProperty(attributes, attributeName, attributeValue);
         }
      }

   }

   protected boolean isValidPropertyName(String propertyName) {
      int propId = FOPropertyMapping.getPropertyId(findBasePropertyName(propertyName));
      int subpropId = FOPropertyMapping.getSubPropertyId(findSubPropertyName(propertyName));
      return propId != -1 && (subpropId != -1 || findSubPropertyName(propertyName) == null);
   }

   public Property getPropertyForAttribute(Attributes attributes, String attributeName, String attributeValue) throws FOPException {
      if (attributeValue == null) {
         return null;
      } else if (!attributeName.startsWith("xmlns:") && !"xmlns".equals(attributeName)) {
         String basePropertyName = findBasePropertyName(attributeName);
         String subPropertyName = findSubPropertyName(attributeName);
         int propId = FOPropertyMapping.getPropertyId(basePropertyName);
         int subpropId = FOPropertyMapping.getSubPropertyId(subPropertyName);
         return propId != -1 && (subpropId != -1 || subPropertyName == null) ? this.getExplicit(propId) : null;
      } else {
         return null;
      }
   }

   private void convertAttributeToProperty(Attributes attributes, String attributeName, String attributeValue) throws ValidationException {
      if (!attributeName.startsWith("xmlns:") && !"xmlns".equals(attributeName)) {
         if (attributeValue != null) {
            String basePropertyName = findBasePropertyName(attributeName);
            String subPropertyName = findSubPropertyName(attributeName);
            int propId = FOPropertyMapping.getPropertyId(basePropertyName);
            int subpropId = FOPropertyMapping.getSubPropertyId(subPropertyName);
            if (propId == -1 || subpropId == -1 && subPropertyName != null) {
               this.handleInvalidProperty(new QName((String)null, attributeName));
            }

            FObj parentFO = this.fobj.findNearestAncestorFObj();
            PropertyMaker propertyMaker = findMaker(propId);
            if (propertyMaker == null) {
               log.warn("No PropertyMaker registered for " + attributeName + ". Ignoring property.");
               return;
            }

            try {
               Property prop = null;
               if (subPropertyName == null) {
                  if (this.getExplicit(propId) != null) {
                     return;
                  }

                  prop = propertyMaker.make(this, attributeValue, parentFO);
               } else {
                  Property baseProperty = this.findBaseProperty(attributes, parentFO, propId, basePropertyName, propertyMaker);
                  prop = propertyMaker.make(baseProperty, subpropId, this, attributeValue, parentFO);
               }

               if (prop != null) {
                  this.putExplicit(propId, prop);
               }
            } catch (PropertyException var12) {
               this.fobj.getFOValidationEventProducer().invalidPropertyValue(this, this.fobj.getName(), attributeName, attributeValue, var12, this.fobj.locator);
            }
         }

      }
   }

   private Property findBaseProperty(Attributes attributes, FObj parentFO, int propId, String basePropertyName, PropertyMaker propertyMaker) throws PropertyException {
      Property baseProperty = this.getExplicit(propId);
      if (baseProperty != null) {
         return baseProperty;
      } else {
         String basePropertyValue = attributes.getValue(basePropertyName);
         if (basePropertyValue != null && propertyMaker != null) {
            baseProperty = propertyMaker.make(this, basePropertyValue, parentFO);
            return baseProperty;
         } else {
            return null;
         }
      }
   }

   protected void handleInvalidProperty(QName attr) throws ValidationException {
      if (!attr.getQName().startsWith("xmlns")) {
         this.fobj.getFOValidationEventProducer().invalidProperty(this, this.fobj.getName(), attr, true, this.fobj.locator);
      }

   }

   protected static String findBasePropertyName(String attributeName) {
      int separatorCharIndex = attributeName.indexOf(46);
      String basePropertyName = attributeName;
      if (separatorCharIndex > -1) {
         basePropertyName = attributeName.substring(0, separatorCharIndex);
      }

      return basePropertyName;
   }

   protected static String findSubPropertyName(String attributeName) {
      int separatorCharIndex = attributeName.indexOf(46);
      String subpropertyName = null;
      if (separatorCharIndex > -1) {
         subpropertyName = attributeName.substring(separatorCharIndex + 1);
      }

      return subpropertyName;
   }

   private Property getShorthand(int propId) throws PropertyException {
      PropertyMaker propertyMaker = findMaker(propId);
      return propertyMaker != null ? propertyMaker.getShorthand(this) : null;
   }

   private Property makeProperty(int propId) throws PropertyException {
      PropertyMaker propertyMaker = findMaker(propId);
      return propertyMaker != null ? propertyMaker.make(this) : null;
   }

   private boolean isInherited(int propId) {
      return inheritableProperty[propId];
   }

   private static PropertyMaker findMaker(int propId) {
      return propId >= 1 && propId <= 294 ? FObj.getPropertyMakerFor(propId) : null;
   }

   public CommonBorderPaddingBackground getBorderPaddingBackgroundProps() throws PropertyException {
      return CommonBorderPaddingBackground.getInstance(this);
   }

   public CommonHyphenation getHyphenationProps() throws PropertyException {
      return CommonHyphenation.getInstance(this);
   }

   public CommonMarginBlock getMarginBlockProps() throws PropertyException {
      return new CommonMarginBlock(this);
   }

   public CommonMarginInline getMarginInlineProps() throws PropertyException {
      return new CommonMarginInline(this);
   }

   public CommonAural getAuralProps() throws PropertyException {
      CommonAural props = new CommonAural(this);
      return props;
   }

   public CommonRelativePosition getRelativePositionProps() throws PropertyException {
      return new CommonRelativePosition(this);
   }

   public CommonAbsolutePosition getAbsolutePositionProps() throws PropertyException {
      return new CommonAbsolutePosition(this);
   }

   public CommonFont getFontProps() throws PropertyException {
      return CommonFont.getInstance(this);
   }

   public CommonTextDecoration getTextDecorationProps() throws PropertyException {
      return CommonTextDecoration.createFromPropertyList(this);
   }

   static {
      PropertyMaker maker = null;

      for(int prop = 1; prop <= 294; ++prop) {
         maker = findMaker(prop);
         inheritableProperty[prop] = maker != null && maker.isInherited();
      }

      log = LogFactory.getLog(PropertyList.class);
   }

   private static class UnknownPropertyHandler {
      private Map unknownPropertyValues;
      private Set knownProperties;

      private UnknownPropertyHandler() {
         this.unknownPropertyValues = new HashMap();
         this.knownProperties = new HashSet();
      }

      void validatePropertyValue(String propertyValue, Property output, Property property) {
         if (!this.knownProperties.contains(property) && output == null) {
            if (propertyValue != null) {
               this.unknownPropertyValues.put(propertyValue, property);
            }
         } else {
            this.knownProperties.add(property);
         }

      }

      Map getUnknownPropertyValues() {
         return this.unknownPropertyValues;
      }

      // $FF: synthetic method
      UnknownPropertyHandler(Object x0) {
         this();
      }
   }
}
