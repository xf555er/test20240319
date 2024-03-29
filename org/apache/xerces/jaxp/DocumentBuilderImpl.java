package org.apache.xerces.jaxp;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.validation.Schema;
import org.apache.xerces.dom.DOMImplementationImpl;
import org.apache.xerces.dom.DOMMessageFormatter;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.impl.validation.ValidationManager;
import org.apache.xerces.impl.xs.XMLSchemaValidator;
import org.apache.xerces.jaxp.validation.XSGrammarPoolContainer;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.util.SecurityManager;
import org.apache.xerces.xni.XMLDocumentHandler;
import org.apache.xerces.xni.parser.XMLComponent;
import org.apache.xerces.xni.parser.XMLComponentManager;
import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.apache.xerces.xni.parser.XMLDocumentSource;
import org.apache.xerces.xni.parser.XMLParserConfiguration;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

public class DocumentBuilderImpl extends DocumentBuilder implements JAXPConstants {
   private static final String NAMESPACES_FEATURE = "http://xml.org/sax/features/namespaces";
   private static final String INCLUDE_IGNORABLE_WHITESPACE = "http://apache.org/xml/features/dom/include-ignorable-whitespace";
   private static final String CREATE_ENTITY_REF_NODES_FEATURE = "http://apache.org/xml/features/dom/create-entity-ref-nodes";
   private static final String INCLUDE_COMMENTS_FEATURE = "http://apache.org/xml/features/include-comments";
   private static final String CREATE_CDATA_NODES_FEATURE = "http://apache.org/xml/features/create-cdata-nodes";
   private static final String XINCLUDE_FEATURE = "http://apache.org/xml/features/xinclude";
   private static final String XMLSCHEMA_VALIDATION_FEATURE = "http://apache.org/xml/features/validation/schema";
   private static final String VALIDATION_FEATURE = "http://xml.org/sax/features/validation";
   private static final String SECURITY_MANAGER = "http://apache.org/xml/properties/security-manager";
   private final DOMParser domParser;
   private final Schema grammar;
   private final XMLComponent fSchemaValidator;
   private final XMLComponentManager fSchemaValidatorComponentManager;
   private final ValidationManager fSchemaValidationManager;
   private final UnparsedEntityHandler fUnparsedEntityHandler;
   private final ErrorHandler fInitErrorHandler;
   private final EntityResolver fInitEntityResolver;

   DocumentBuilderImpl(DocumentBuilderFactoryImpl var1, Hashtable var2, Hashtable var3) throws SAXNotRecognizedException, SAXNotSupportedException {
      this(var1, var2, var3, false);
   }

   DocumentBuilderImpl(DocumentBuilderFactoryImpl var1, Hashtable var2, Hashtable var3, boolean var4) throws SAXNotRecognizedException, SAXNotSupportedException {
      this.domParser = new DOMParser();
      if (var1.isValidating()) {
         this.fInitErrorHandler = new DefaultValidationErrorHandler();
         this.setErrorHandler(this.fInitErrorHandler);
      } else {
         this.fInitErrorHandler = this.domParser.getErrorHandler();
      }

      this.domParser.setFeature("http://xml.org/sax/features/validation", var1.isValidating());
      this.domParser.setFeature("http://xml.org/sax/features/namespaces", var1.isNamespaceAware());
      this.domParser.setFeature("http://apache.org/xml/features/dom/include-ignorable-whitespace", !var1.isIgnoringElementContentWhitespace());
      this.domParser.setFeature("http://apache.org/xml/features/dom/create-entity-ref-nodes", !var1.isExpandEntityReferences());
      this.domParser.setFeature("http://apache.org/xml/features/include-comments", !var1.isIgnoringComments());
      this.domParser.setFeature("http://apache.org/xml/features/create-cdata-nodes", !var1.isCoalescing());
      if (var1.isXIncludeAware()) {
         this.domParser.setFeature("http://apache.org/xml/features/xinclude", true);
      }

      if (var4) {
         this.domParser.setProperty("http://apache.org/xml/properties/security-manager", new SecurityManager());
      }

      this.grammar = var1.getSchema();
      if (this.grammar != null) {
         XMLParserConfiguration var5 = this.domParser.getXMLParserConfiguration();
         Object var6 = null;
         if (this.grammar instanceof XSGrammarPoolContainer) {
            var6 = new XMLSchemaValidator();
            this.fSchemaValidationManager = new ValidationManager();
            this.fUnparsedEntityHandler = new UnparsedEntityHandler(this.fSchemaValidationManager);
            var5.setDTDHandler(this.fUnparsedEntityHandler);
            this.fUnparsedEntityHandler.setDTDHandler(this.domParser);
            this.domParser.setDTDSource(this.fUnparsedEntityHandler);
            this.fSchemaValidatorComponentManager = new SchemaValidatorConfiguration(var5, (XSGrammarPoolContainer)this.grammar, this.fSchemaValidationManager);
         } else {
            var6 = new JAXPValidatorComponent(this.grammar.newValidatorHandler());
            this.fSchemaValidationManager = null;
            this.fUnparsedEntityHandler = null;
            this.fSchemaValidatorComponentManager = var5;
         }

         var5.addRecognizedFeatures(((XMLComponent)var6).getRecognizedFeatures());
         var5.addRecognizedProperties(((XMLComponent)var6).getRecognizedProperties());
         var5.setDocumentHandler((XMLDocumentHandler)var6);
         ((XMLDocumentSource)var6).setDocumentHandler(this.domParser);
         this.domParser.setDocumentSource((XMLDocumentSource)var6);
         this.fSchemaValidator = (XMLComponent)var6;
      } else {
         this.fSchemaValidationManager = null;
         this.fUnparsedEntityHandler = null;
         this.fSchemaValidatorComponentManager = null;
         this.fSchemaValidator = null;
      }

      this.setFeatures(var3);
      this.setDocumentBuilderFactoryAttributes(var2);
      this.fInitEntityResolver = this.domParser.getEntityResolver();
   }

   private void setFeatures(Hashtable var1) throws SAXNotSupportedException, SAXNotRecognizedException {
      if (var1 != null) {
         Iterator var2 = var1.entrySet().iterator();

         while(var2.hasNext()) {
            Map.Entry var3 = (Map.Entry)var2.next();
            String var4 = (String)var3.getKey();
            boolean var5 = (Boolean)var3.getValue();
            this.domParser.setFeature(var4, var5);
         }
      }

   }

   private void setDocumentBuilderFactoryAttributes(Hashtable var1) throws SAXNotSupportedException, SAXNotRecognizedException {
      if (var1 != null) {
         Iterator var2 = var1.entrySet().iterator();

         while(true) {
            String var4;
            Object var5;
            label45:
            do {
               while(true) {
                  while(true) {
                     while(var2.hasNext()) {
                        Map.Entry var3 = (Map.Entry)var2.next();
                        var4 = (String)var3.getKey();
                        var5 = var3.getValue();
                        if (!(var5 instanceof Boolean)) {
                           if (!"http://java.sun.com/xml/jaxp/properties/schemaLanguage".equals(var4)) {
                              if ("http://java.sun.com/xml/jaxp/properties/schemaSource".equals(var4)) {
                                 continue label45;
                              }

                              this.domParser.setProperty(var4, var5);
                           } else if ("http://www.w3.org/2001/XMLSchema".equals(var5) && this.isValidating()) {
                              this.domParser.setFeature("http://apache.org/xml/features/validation/schema", true);
                              this.domParser.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
                           }
                        } else {
                           this.domParser.setFeature(var4, (Boolean)var5);
                        }
                     }

                     return;
                  }
               }
            } while(!this.isValidating());

            String var6 = (String)var1.get("http://java.sun.com/xml/jaxp/properties/schemaLanguage");
            if (var6 == null || !"http://www.w3.org/2001/XMLSchema".equals(var6)) {
               throw new IllegalArgumentException(DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "jaxp-order-not-supported", new Object[]{"http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://java.sun.com/xml/jaxp/properties/schemaSource"}));
            }

            this.domParser.setProperty(var4, var5);
         }
      }
   }

   public Document newDocument() {
      return new DocumentImpl();
   }

   public DOMImplementation getDOMImplementation() {
      return DOMImplementationImpl.getDOMImplementation();
   }

   public Document parse(InputSource var1) throws SAXException, IOException {
      if (var1 == null) {
         throw new IllegalArgumentException(DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "jaxp-null-input-source", (Object[])null));
      } else {
         if (this.fSchemaValidator != null) {
            if (this.fSchemaValidationManager != null) {
               this.fSchemaValidationManager.reset();
               this.fUnparsedEntityHandler.reset();
            }

            this.resetSchemaValidator();
         }

         this.domParser.parse(var1);
         Document var2 = this.domParser.getDocument();
         this.domParser.dropDocumentReferences();
         return var2;
      }
   }

   public boolean isNamespaceAware() {
      try {
         return this.domParser.getFeature("http://xml.org/sax/features/namespaces");
      } catch (SAXException var2) {
         throw new IllegalStateException(var2.getMessage());
      }
   }

   public boolean isValidating() {
      try {
         return this.domParser.getFeature("http://xml.org/sax/features/validation");
      } catch (SAXException var2) {
         throw new IllegalStateException(var2.getMessage());
      }
   }

   public boolean isXIncludeAware() {
      try {
         return this.domParser.getFeature("http://apache.org/xml/features/xinclude");
      } catch (SAXException var2) {
         return false;
      }
   }

   public void setEntityResolver(EntityResolver var1) {
      this.domParser.setEntityResolver(var1);
   }

   public void setErrorHandler(ErrorHandler var1) {
      this.domParser.setErrorHandler(var1);
   }

   public Schema getSchema() {
      return this.grammar;
   }

   public void reset() {
      if (this.domParser.getErrorHandler() != this.fInitErrorHandler) {
         this.domParser.setErrorHandler(this.fInitErrorHandler);
      }

      if (this.domParser.getEntityResolver() != this.fInitEntityResolver) {
         this.domParser.setEntityResolver(this.fInitEntityResolver);
      }

   }

   DOMParser getDOMParser() {
      return this.domParser;
   }

   private void resetSchemaValidator() throws SAXException {
      try {
         this.fSchemaValidator.reset(this.fSchemaValidatorComponentManager);
      } catch (XMLConfigurationException var2) {
         throw new SAXException(var2);
      }
   }
}
