package org.apache.xerces.parsers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import org.apache.xerces.impl.XML11DTDScannerImpl;
import org.apache.xerces.impl.XML11DocumentScannerImpl;
import org.apache.xerces.impl.XML11NSDocumentScannerImpl;
import org.apache.xerces.impl.XMLDTDScannerImpl;
import org.apache.xerces.impl.XMLDocumentScannerImpl;
import org.apache.xerces.impl.XMLEntityHandler;
import org.apache.xerces.impl.XMLEntityManager;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.impl.XMLNSDocumentScannerImpl;
import org.apache.xerces.impl.XMLVersionDetector;
import org.apache.xerces.impl.dtd.XMLDTDValidatorFilter;
import org.apache.xerces.impl.dv.DTDDVFactory;
import org.apache.xerces.impl.msg.XMLMessageFormatter;
import org.apache.xerces.impl.validation.ValidationManager;
import org.apache.xerces.util.ParserConfigurationSettings;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.xni.XMLDTDContentModelHandler;
import org.apache.xerces.xni.XMLDTDHandler;
import org.apache.xerces.xni.XMLDocumentHandler;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.apache.xerces.xni.parser.XMLComponent;
import org.apache.xerces.xni.parser.XMLComponentManager;
import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.apache.xerces.xni.parser.XMLDTDScanner;
import org.apache.xerces.xni.parser.XMLDocumentScanner;
import org.apache.xerces.xni.parser.XMLDocumentSource;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLErrorHandler;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.parser.XMLPullParserConfiguration;

public class XML11NonValidatingConfiguration extends ParserConfigurationSettings implements XMLPullParserConfiguration, XML11Configurable {
   protected static final String XML11_DATATYPE_VALIDATOR_FACTORY = "org.apache.xerces.impl.dv.dtd.XML11DTDDVFactoryImpl";
   protected static final String VALIDATION = "http://xml.org/sax/features/validation";
   protected static final String NAMESPACES = "http://xml.org/sax/features/namespaces";
   protected static final String EXTERNAL_GENERAL_ENTITIES = "http://xml.org/sax/features/external-general-entities";
   protected static final String EXTERNAL_PARAMETER_ENTITIES = "http://xml.org/sax/features/external-parameter-entities";
   protected static final String CONTINUE_AFTER_FATAL_ERROR = "http://apache.org/xml/features/continue-after-fatal-error";
   protected static final String XML_STRING = "http://xml.org/sax/properties/xml-string";
   protected static final String SYMBOL_TABLE = "http://apache.org/xml/properties/internal/symbol-table";
   protected static final String ERROR_HANDLER = "http://apache.org/xml/properties/internal/error-handler";
   protected static final String ENTITY_RESOLVER = "http://apache.org/xml/properties/internal/entity-resolver";
   protected static final String ERROR_REPORTER = "http://apache.org/xml/properties/internal/error-reporter";
   protected static final String ENTITY_MANAGER = "http://apache.org/xml/properties/internal/entity-manager";
   protected static final String DOCUMENT_SCANNER = "http://apache.org/xml/properties/internal/document-scanner";
   protected static final String DTD_SCANNER = "http://apache.org/xml/properties/internal/dtd-scanner";
   protected static final String XMLGRAMMAR_POOL = "http://apache.org/xml/properties/internal/grammar-pool";
   protected static final String DTD_VALIDATOR = "http://apache.org/xml/properties/internal/validator/dtd";
   protected static final String NAMESPACE_BINDER = "http://apache.org/xml/properties/internal/namespace-binder";
   protected static final String DATATYPE_VALIDATOR_FACTORY = "http://apache.org/xml/properties/internal/datatype-validator-factory";
   protected static final String VALIDATION_MANAGER = "http://apache.org/xml/properties/internal/validation-manager";
   protected static final boolean PRINT_EXCEPTION_STACK_TRACE = false;
   protected SymbolTable fSymbolTable;
   protected XMLInputSource fInputSource;
   protected ValidationManager fValidationManager;
   protected XMLVersionDetector fVersionDetector;
   protected XMLLocator fLocator;
   protected Locale fLocale;
   protected ArrayList fComponents;
   protected ArrayList fXML11Components;
   protected ArrayList fCommonComponents;
   protected XMLDocumentHandler fDocumentHandler;
   protected XMLDTDHandler fDTDHandler;
   protected XMLDTDContentModelHandler fDTDContentModelHandler;
   protected XMLDocumentSource fLastComponent;
   protected boolean fParseInProgress;
   protected boolean fConfigUpdated;
   protected DTDDVFactory fDatatypeValidatorFactory;
   protected XMLNSDocumentScannerImpl fNamespaceScanner;
   protected XMLDocumentScannerImpl fNonNSScanner;
   protected XMLDTDScanner fDTDScanner;
   protected DTDDVFactory fXML11DatatypeFactory;
   protected XML11NSDocumentScannerImpl fXML11NSDocScanner;
   protected XML11DocumentScannerImpl fXML11DocScanner;
   protected XML11DTDScannerImpl fXML11DTDScanner;
   protected XMLGrammarPool fGrammarPool;
   protected XMLErrorReporter fErrorReporter;
   protected XMLEntityManager fEntityManager;
   protected XMLDocumentScanner fCurrentScanner;
   protected DTDDVFactory fCurrentDVFactory;
   protected XMLDTDScanner fCurrentDTDScanner;
   private boolean f11Initialized;

   public XML11NonValidatingConfiguration() {
      this((SymbolTable)null, (XMLGrammarPool)null, (XMLComponentManager)null);
   }

   public XML11NonValidatingConfiguration(SymbolTable var1) {
      this(var1, (XMLGrammarPool)null, (XMLComponentManager)null);
   }

   public XML11NonValidatingConfiguration(SymbolTable var1, XMLGrammarPool var2) {
      this(var1, var2, (XMLComponentManager)null);
   }

   public XML11NonValidatingConfiguration(SymbolTable var1, XMLGrammarPool var2, XMLComponentManager var3) {
      super(var3);
      this.fXML11Components = null;
      this.fCommonComponents = null;
      this.fParseInProgress = false;
      this.fConfigUpdated = false;
      this.fXML11DatatypeFactory = null;
      this.fXML11NSDocScanner = null;
      this.fXML11DocScanner = null;
      this.fXML11DTDScanner = null;
      this.f11Initialized = false;
      this.fComponents = new ArrayList();
      this.fXML11Components = new ArrayList();
      this.fCommonComponents = new ArrayList();
      this.fRecognizedFeatures = new ArrayList();
      this.fRecognizedProperties = new ArrayList();
      this.fFeatures = new HashMap();
      this.fProperties = new HashMap();
      String[] var4 = new String[]{"http://apache.org/xml/features/continue-after-fatal-error", "http://xml.org/sax/features/validation", "http://xml.org/sax/features/namespaces", "http://xml.org/sax/features/external-general-entities", "http://xml.org/sax/features/external-parameter-entities", "http://apache.org/xml/features/internal/parser-settings"};
      this.addRecognizedFeatures(var4);
      this.fFeatures.put("http://xml.org/sax/features/validation", Boolean.FALSE);
      this.fFeatures.put("http://xml.org/sax/features/namespaces", Boolean.TRUE);
      this.fFeatures.put("http://xml.org/sax/features/external-general-entities", Boolean.TRUE);
      this.fFeatures.put("http://xml.org/sax/features/external-parameter-entities", Boolean.TRUE);
      this.fFeatures.put("http://apache.org/xml/features/continue-after-fatal-error", Boolean.FALSE);
      this.fFeatures.put("http://apache.org/xml/features/internal/parser-settings", Boolean.TRUE);
      String[] var5 = new String[]{"http://xml.org/sax/properties/xml-string", "http://apache.org/xml/properties/internal/symbol-table", "http://apache.org/xml/properties/internal/error-handler", "http://apache.org/xml/properties/internal/entity-resolver", "http://apache.org/xml/properties/internal/error-reporter", "http://apache.org/xml/properties/internal/entity-manager", "http://apache.org/xml/properties/internal/document-scanner", "http://apache.org/xml/properties/internal/dtd-scanner", "http://apache.org/xml/properties/internal/validator/dtd", "http://apache.org/xml/properties/internal/datatype-validator-factory", "http://apache.org/xml/properties/internal/validation-manager", "http://xml.org/sax/properties/xml-string", "http://apache.org/xml/properties/internal/grammar-pool"};
      this.addRecognizedProperties(var5);
      if (var1 == null) {
         var1 = new SymbolTable();
      }

      this.fSymbolTable = var1;
      this.fProperties.put("http://apache.org/xml/properties/internal/symbol-table", this.fSymbolTable);
      this.fGrammarPool = var2;
      if (this.fGrammarPool != null) {
         this.fProperties.put("http://apache.org/xml/properties/internal/grammar-pool", this.fGrammarPool);
      }

      this.fEntityManager = new XMLEntityManager();
      this.fProperties.put("http://apache.org/xml/properties/internal/entity-manager", this.fEntityManager);
      this.addCommonComponent(this.fEntityManager);
      this.fErrorReporter = new XMLErrorReporter();
      this.fErrorReporter.setDocumentLocator(this.fEntityManager.getEntityScanner());
      this.fProperties.put("http://apache.org/xml/properties/internal/error-reporter", this.fErrorReporter);
      this.addCommonComponent(this.fErrorReporter);
      this.fNamespaceScanner = new XMLNSDocumentScannerImpl();
      this.fProperties.put("http://apache.org/xml/properties/internal/document-scanner", this.fNamespaceScanner);
      this.addComponent(this.fNamespaceScanner);
      this.fDTDScanner = new XMLDTDScannerImpl();
      this.fProperties.put("http://apache.org/xml/properties/internal/dtd-scanner", this.fDTDScanner);
      this.addComponent((XMLComponent)this.fDTDScanner);
      this.fDatatypeValidatorFactory = DTDDVFactory.getInstance();
      this.fProperties.put("http://apache.org/xml/properties/internal/datatype-validator-factory", this.fDatatypeValidatorFactory);
      this.fValidationManager = new ValidationManager();
      this.fProperties.put("http://apache.org/xml/properties/internal/validation-manager", this.fValidationManager);
      this.fVersionDetector = new XMLVersionDetector();
      if (this.fErrorReporter.getMessageFormatter("http://www.w3.org/TR/1998/REC-xml-19980210") == null) {
         XMLMessageFormatter var6 = new XMLMessageFormatter();
         this.fErrorReporter.putMessageFormatter("http://www.w3.org/TR/1998/REC-xml-19980210", var6);
         this.fErrorReporter.putMessageFormatter("http://www.w3.org/TR/1999/REC-xml-names-19990114", var6);
      }

      try {
         this.setLocale(Locale.getDefault());
      } catch (XNIException var7) {
      }

      this.fConfigUpdated = false;
   }

   public void setInputSource(XMLInputSource var1) throws XMLConfigurationException, IOException {
      this.fInputSource = var1;
   }

   public void setLocale(Locale var1) throws XNIException {
      this.fLocale = var1;
      this.fErrorReporter.setLocale(var1);
   }

   public void setDocumentHandler(XMLDocumentHandler var1) {
      this.fDocumentHandler = var1;
      if (this.fLastComponent != null) {
         this.fLastComponent.setDocumentHandler(this.fDocumentHandler);
         if (this.fDocumentHandler != null) {
            this.fDocumentHandler.setDocumentSource(this.fLastComponent);
         }
      }

   }

   public XMLDocumentHandler getDocumentHandler() {
      return this.fDocumentHandler;
   }

   public void setDTDHandler(XMLDTDHandler var1) {
      this.fDTDHandler = var1;
   }

   public XMLDTDHandler getDTDHandler() {
      return this.fDTDHandler;
   }

   public void setDTDContentModelHandler(XMLDTDContentModelHandler var1) {
      this.fDTDContentModelHandler = var1;
   }

   public XMLDTDContentModelHandler getDTDContentModelHandler() {
      return this.fDTDContentModelHandler;
   }

   public void setEntityResolver(XMLEntityResolver var1) {
      this.fProperties.put("http://apache.org/xml/properties/internal/entity-resolver", var1);
   }

   public XMLEntityResolver getEntityResolver() {
      return (XMLEntityResolver)this.fProperties.get("http://apache.org/xml/properties/internal/entity-resolver");
   }

   public void setErrorHandler(XMLErrorHandler var1) {
      this.fProperties.put("http://apache.org/xml/properties/internal/error-handler", var1);
   }

   public XMLErrorHandler getErrorHandler() {
      return (XMLErrorHandler)this.fProperties.get("http://apache.org/xml/properties/internal/error-handler");
   }

   public void cleanup() {
      this.fEntityManager.closeReaders();
   }

   public void parse(XMLInputSource var1) throws XNIException, IOException {
      if (this.fParseInProgress) {
         throw new XNIException("FWK005 parse may not be called while parsing.");
      } else {
         this.fParseInProgress = true;

         try {
            this.setInputSource(var1);
            this.parse(true);
         } catch (XNIException var13) {
            throw var13;
         } catch (IOException var14) {
            throw var14;
         } catch (RuntimeException var15) {
            throw var15;
         } catch (Exception var16) {
            throw new XNIException(var16);
         } finally {
            this.fParseInProgress = false;
            this.cleanup();
         }

      }
   }

   public boolean parse(boolean var1) throws XNIException, IOException {
      if (this.fInputSource != null) {
         try {
            this.fValidationManager.reset();
            this.fVersionDetector.reset(this);
            this.resetCommon();
            short var2 = this.fVersionDetector.determineDocVersion(this.fInputSource);
            if (var2 == 1) {
               this.configurePipeline();
               this.reset();
            } else {
               if (var2 != 2) {
                  return false;
               }

               this.initXML11Components();
               this.configureXML11Pipeline();
               this.resetXML11();
            }

            this.fConfigUpdated = false;
            this.fVersionDetector.startDocumentParsing((XMLEntityHandler)this.fCurrentScanner, var2);
            this.fInputSource = null;
         } catch (XNIException var10) {
            throw var10;
         } catch (IOException var11) {
            throw var11;
         } catch (RuntimeException var12) {
            throw var12;
         } catch (Exception var13) {
            throw new XNIException(var13);
         }
      }

      try {
         return this.fCurrentScanner.scanDocument(var1);
      } catch (XNIException var6) {
         throw var6;
      } catch (IOException var7) {
         throw var7;
      } catch (RuntimeException var8) {
         throw var8;
      } catch (Exception var9) {
         throw new XNIException(var9);
      }
   }

   public boolean getFeature(String var1) throws XMLConfigurationException {
      return var1.equals("http://apache.org/xml/features/internal/parser-settings") ? this.fConfigUpdated : super.getFeature(var1);
   }

   public void setFeature(String var1, boolean var2) throws XMLConfigurationException {
      this.fConfigUpdated = true;
      int var3 = this.fComponents.size();

      for(int var4 = 0; var4 < var3; ++var4) {
         XMLComponent var5 = (XMLComponent)this.fComponents.get(var4);
         var5.setFeature(var1, var2);
      }

      var3 = this.fCommonComponents.size();

      for(int var10 = 0; var10 < var3; ++var10) {
         XMLComponent var6 = (XMLComponent)this.fCommonComponents.get(var10);
         var6.setFeature(var1, var2);
      }

      var3 = this.fXML11Components.size();

      for(int var11 = 0; var11 < var3; ++var11) {
         XMLComponent var7 = (XMLComponent)this.fXML11Components.get(var11);

         try {
            var7.setFeature(var1, var2);
         } catch (Exception var9) {
         }
      }

      super.setFeature(var1, var2);
   }

   public void setProperty(String var1, Object var2) throws XMLConfigurationException {
      this.fConfigUpdated = true;
      int var3 = this.fComponents.size();

      for(int var4 = 0; var4 < var3; ++var4) {
         XMLComponent var5 = (XMLComponent)this.fComponents.get(var4);
         var5.setProperty(var1, var2);
      }

      var3 = this.fCommonComponents.size();

      for(int var10 = 0; var10 < var3; ++var10) {
         XMLComponent var6 = (XMLComponent)this.fCommonComponents.get(var10);
         var6.setProperty(var1, var2);
      }

      var3 = this.fXML11Components.size();

      for(int var11 = 0; var11 < var3; ++var11) {
         XMLComponent var7 = (XMLComponent)this.fXML11Components.get(var11);

         try {
            var7.setProperty(var1, var2);
         } catch (Exception var9) {
         }
      }

      super.setProperty(var1, var2);
   }

   public Locale getLocale() {
      return this.fLocale;
   }

   protected void reset() throws XNIException {
      int var1 = this.fComponents.size();

      for(int var2 = 0; var2 < var1; ++var2) {
         XMLComponent var3 = (XMLComponent)this.fComponents.get(var2);
         var3.reset(this);
      }

   }

   protected void resetCommon() throws XNIException {
      int var1 = this.fCommonComponents.size();

      for(int var2 = 0; var2 < var1; ++var2) {
         XMLComponent var3 = (XMLComponent)this.fCommonComponents.get(var2);
         var3.reset(this);
      }

   }

   protected void resetXML11() throws XNIException {
      int var1 = this.fXML11Components.size();

      for(int var2 = 0; var2 < var1; ++var2) {
         XMLComponent var3 = (XMLComponent)this.fXML11Components.get(var2);
         var3.reset(this);
      }

   }

   protected void configureXML11Pipeline() {
      if (this.fCurrentDVFactory != this.fXML11DatatypeFactory) {
         this.fCurrentDVFactory = this.fXML11DatatypeFactory;
         this.setProperty("http://apache.org/xml/properties/internal/datatype-validator-factory", this.fCurrentDVFactory);
      }

      if (this.fCurrentDTDScanner != this.fXML11DTDScanner) {
         this.fCurrentDTDScanner = this.fXML11DTDScanner;
         this.setProperty("http://apache.org/xml/properties/internal/dtd-scanner", this.fCurrentDTDScanner);
      }

      this.fXML11DTDScanner.setDTDHandler(this.fDTDHandler);
      this.fXML11DTDScanner.setDTDContentModelHandler(this.fDTDContentModelHandler);
      if (this.fFeatures.get("http://xml.org/sax/features/namespaces") == Boolean.TRUE) {
         if (this.fCurrentScanner != this.fXML11NSDocScanner) {
            this.fCurrentScanner = this.fXML11NSDocScanner;
            this.setProperty("http://apache.org/xml/properties/internal/document-scanner", this.fXML11NSDocScanner);
         }

         this.fXML11NSDocScanner.setDTDValidator((XMLDTDValidatorFilter)null);
         this.fXML11NSDocScanner.setDocumentHandler(this.fDocumentHandler);
         if (this.fDocumentHandler != null) {
            this.fDocumentHandler.setDocumentSource(this.fXML11NSDocScanner);
         }

         this.fLastComponent = this.fXML11NSDocScanner;
      } else {
         if (this.fXML11DocScanner == null) {
            this.fXML11DocScanner = new XML11DocumentScannerImpl();
            this.addXML11Component(this.fXML11DocScanner);
         }

         if (this.fCurrentScanner != this.fXML11DocScanner) {
            this.fCurrentScanner = this.fXML11DocScanner;
            this.setProperty("http://apache.org/xml/properties/internal/document-scanner", this.fXML11DocScanner);
         }

         this.fXML11DocScanner.setDocumentHandler(this.fDocumentHandler);
         if (this.fDocumentHandler != null) {
            this.fDocumentHandler.setDocumentSource(this.fXML11DocScanner);
         }

         this.fLastComponent = this.fXML11DocScanner;
      }

   }

   protected void configurePipeline() {
      if (this.fCurrentDVFactory != this.fDatatypeValidatorFactory) {
         this.fCurrentDVFactory = this.fDatatypeValidatorFactory;
         this.setProperty("http://apache.org/xml/properties/internal/datatype-validator-factory", this.fCurrentDVFactory);
      }

      if (this.fCurrentDTDScanner != this.fDTDScanner) {
         this.fCurrentDTDScanner = this.fDTDScanner;
         this.setProperty("http://apache.org/xml/properties/internal/dtd-scanner", this.fCurrentDTDScanner);
      }

      this.fDTDScanner.setDTDHandler(this.fDTDHandler);
      this.fDTDScanner.setDTDContentModelHandler(this.fDTDContentModelHandler);
      if (this.fFeatures.get("http://xml.org/sax/features/namespaces") == Boolean.TRUE) {
         if (this.fCurrentScanner != this.fNamespaceScanner) {
            this.fCurrentScanner = this.fNamespaceScanner;
            this.setProperty("http://apache.org/xml/properties/internal/document-scanner", this.fNamespaceScanner);
         }

         this.fNamespaceScanner.setDTDValidator((XMLDTDValidatorFilter)null);
         this.fNamespaceScanner.setDocumentHandler(this.fDocumentHandler);
         if (this.fDocumentHandler != null) {
            this.fDocumentHandler.setDocumentSource(this.fNamespaceScanner);
         }

         this.fLastComponent = this.fNamespaceScanner;
      } else {
         if (this.fNonNSScanner == null) {
            this.fNonNSScanner = new XMLDocumentScannerImpl();
            this.addComponent(this.fNonNSScanner);
         }

         if (this.fCurrentScanner != this.fNonNSScanner) {
            this.fCurrentScanner = this.fNonNSScanner;
            this.setProperty("http://apache.org/xml/properties/internal/document-scanner", this.fNonNSScanner);
         }

         this.fNonNSScanner.setDocumentHandler(this.fDocumentHandler);
         if (this.fDocumentHandler != null) {
            this.fDocumentHandler.setDocumentSource(this.fNonNSScanner);
         }

         this.fLastComponent = this.fNonNSScanner;
      }

   }

   protected void checkFeature(String var1) throws XMLConfigurationException {
      if (var1.startsWith("http://apache.org/xml/features/")) {
         int var2 = var1.length() - "http://apache.org/xml/features/".length();
         if (var2 == "validation/dynamic".length() && var1.endsWith("validation/dynamic")) {
            return;
         }

         byte var3;
         if (var2 == "validation/default-attribute-values".length() && var1.endsWith("validation/default-attribute-values")) {
            var3 = 1;
            throw new XMLConfigurationException(var3, var1);
         }

         if (var2 == "validation/validate-content-models".length() && var1.endsWith("validation/validate-content-models")) {
            var3 = 1;
            throw new XMLConfigurationException(var3, var1);
         }

         if (var2 == "nonvalidating/load-dtd-grammar".length() && var1.endsWith("nonvalidating/load-dtd-grammar")) {
            return;
         }

         if (var2 == "nonvalidating/load-external-dtd".length() && var1.endsWith("nonvalidating/load-external-dtd")) {
            return;
         }

         if (var2 == "validation/validate-datatypes".length() && var1.endsWith("validation/validate-datatypes")) {
            var3 = 1;
            throw new XMLConfigurationException(var3, var1);
         }

         if (var2 == "internal/parser-settings".length() && var1.endsWith("internal/parser-settings")) {
            var3 = 1;
            throw new XMLConfigurationException(var3, var1);
         }
      }

      super.checkFeature(var1);
   }

   protected void checkProperty(String var1) throws XMLConfigurationException {
      int var2;
      if (var1.startsWith("http://apache.org/xml/properties/")) {
         var2 = var1.length() - "http://apache.org/xml/properties/".length();
         if (var2 == "internal/dtd-scanner".length() && var1.endsWith("internal/dtd-scanner")) {
            return;
         }
      }

      if (var1.startsWith("http://java.sun.com/xml/jaxp/properties/")) {
         var2 = var1.length() - "http://java.sun.com/xml/jaxp/properties/".length();
         if (var2 == "schemaSource".length() && var1.endsWith("schemaSource")) {
            return;
         }
      }

      if (var1.startsWith("http://xml.org/sax/properties/")) {
         var2 = var1.length() - "http://xml.org/sax/properties/".length();
         if (var2 == "xml-string".length() && var1.endsWith("xml-string")) {
            byte var3 = 1;
            throw new XMLConfigurationException(var3, var1);
         }
      }

      super.checkProperty(var1);
   }

   protected void addComponent(XMLComponent var1) {
      if (!this.fComponents.contains(var1)) {
         this.fComponents.add(var1);
         this.addRecognizedParamsAndSetDefaults(var1);
      }
   }

   protected void addCommonComponent(XMLComponent var1) {
      if (!this.fCommonComponents.contains(var1)) {
         this.fCommonComponents.add(var1);
         this.addRecognizedParamsAndSetDefaults(var1);
      }
   }

   protected void addXML11Component(XMLComponent var1) {
      if (!this.fXML11Components.contains(var1)) {
         this.fXML11Components.add(var1);
         this.addRecognizedParamsAndSetDefaults(var1);
      }
   }

   protected void addRecognizedParamsAndSetDefaults(XMLComponent var1) {
      String[] var2 = var1.getRecognizedFeatures();
      this.addRecognizedFeatures(var2);
      String[] var3 = var1.getRecognizedProperties();
      this.addRecognizedProperties(var3);
      int var4;
      String var5;
      if (var2 != null) {
         for(var4 = 0; var4 < var2.length; ++var4) {
            var5 = var2[var4];
            Boolean var6 = var1.getFeatureDefault(var5);
            if (var6 != null && !this.fFeatures.containsKey(var5)) {
               this.fFeatures.put(var5, var6);
               this.fConfigUpdated = true;
            }
         }
      }

      if (var3 != null) {
         for(var4 = 0; var4 < var3.length; ++var4) {
            var5 = var3[var4];
            Object var7 = var1.getPropertyDefault(var5);
            if (var7 != null && !this.fProperties.containsKey(var5)) {
               this.fProperties.put(var5, var7);
               this.fConfigUpdated = true;
            }
         }
      }

   }

   private void initXML11Components() {
      if (!this.f11Initialized) {
         this.fXML11DatatypeFactory = DTDDVFactory.getInstance("org.apache.xerces.impl.dv.dtd.XML11DTDDVFactoryImpl");
         this.fXML11DTDScanner = new XML11DTDScannerImpl();
         this.addXML11Component(this.fXML11DTDScanner);
         this.fXML11NSDocScanner = new XML11NSDocumentScannerImpl();
         this.addXML11Component(this.fXML11NSDocScanner);
         this.f11Initialized = true;
      }

   }
}
