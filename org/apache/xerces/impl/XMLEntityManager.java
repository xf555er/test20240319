package org.apache.xerces.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;
import org.apache.xerces.impl.io.ASCIIReader;
import org.apache.xerces.impl.io.Latin1Reader;
import org.apache.xerces.impl.io.UCSReader;
import org.apache.xerces.impl.io.UTF16Reader;
import org.apache.xerces.impl.io.UTF8Reader;
import org.apache.xerces.impl.validation.ValidationManager;
import org.apache.xerces.util.AugmentationsImpl;
import org.apache.xerces.util.EncodingMap;
import org.apache.xerces.util.HTTPInputSource;
import org.apache.xerces.util.SecurityManager;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.util.URI;
import org.apache.xerces.util.XMLChar;
import org.apache.xerces.util.XMLEntityDescriptionImpl;
import org.apache.xerces.util.XMLResourceIdentifierImpl;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLComponent;
import org.apache.xerces.xni.parser.XMLComponentManager;
import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLInputSource;

public class XMLEntityManager implements XMLComponent, XMLEntityResolver {
   public static final int DEFAULT_BUFFER_SIZE = 2048;
   public static final int DEFAULT_XMLDECL_BUFFER_SIZE = 64;
   public static final int DEFAULT_INTERNAL_BUFFER_SIZE = 512;
   protected static final String VALIDATION = "http://xml.org/sax/features/validation";
   protected static final String EXTERNAL_GENERAL_ENTITIES = "http://xml.org/sax/features/external-general-entities";
   protected static final String EXTERNAL_PARAMETER_ENTITIES = "http://xml.org/sax/features/external-parameter-entities";
   protected static final String ALLOW_JAVA_ENCODINGS = "http://apache.org/xml/features/allow-java-encodings";
   protected static final String WARN_ON_DUPLICATE_ENTITYDEF = "http://apache.org/xml/features/warn-on-duplicate-entitydef";
   protected static final String STANDARD_URI_CONFORMANT = "http://apache.org/xml/features/standard-uri-conformant";
   protected static final String PARSER_SETTINGS = "http://apache.org/xml/features/internal/parser-settings";
   protected static final String SYMBOL_TABLE = "http://apache.org/xml/properties/internal/symbol-table";
   protected static final String ERROR_REPORTER = "http://apache.org/xml/properties/internal/error-reporter";
   protected static final String ENTITY_RESOLVER = "http://apache.org/xml/properties/internal/entity-resolver";
   protected static final String VALIDATION_MANAGER = "http://apache.org/xml/properties/internal/validation-manager";
   protected static final String BUFFER_SIZE = "http://apache.org/xml/properties/input-buffer-size";
   protected static final String SECURITY_MANAGER = "http://apache.org/xml/properties/security-manager";
   private static final String[] RECOGNIZED_FEATURES = new String[]{"http://xml.org/sax/features/validation", "http://xml.org/sax/features/external-general-entities", "http://xml.org/sax/features/external-parameter-entities", "http://apache.org/xml/features/allow-java-encodings", "http://apache.org/xml/features/warn-on-duplicate-entitydef", "http://apache.org/xml/features/standard-uri-conformant"};
   private static final Boolean[] FEATURE_DEFAULTS;
   private static final String[] RECOGNIZED_PROPERTIES;
   private static final Object[] PROPERTY_DEFAULTS;
   private static final String XMLEntity;
   private static final String DTDEntity;
   private static final boolean DEBUG_BUFFER = false;
   private static final boolean DEBUG_ENTITIES = false;
   private static final boolean DEBUG_ENCODINGS = false;
   private static final boolean DEBUG_RESOLVER = false;
   protected boolean fValidation;
   protected boolean fExternalGeneralEntities;
   protected boolean fExternalParameterEntities;
   protected boolean fAllowJavaEncodings;
   protected boolean fWarnDuplicateEntityDef;
   protected boolean fStrictURI;
   protected SymbolTable fSymbolTable;
   protected XMLErrorReporter fErrorReporter;
   protected XMLEntityResolver fEntityResolver;
   protected ValidationManager fValidationManager;
   protected int fBufferSize;
   protected SecurityManager fSecurityManager;
   protected boolean fStandalone;
   protected boolean fHasPEReferences;
   protected boolean fInExternalSubset;
   protected XMLEntityHandler fEntityHandler;
   protected XMLEntityScanner fEntityScanner;
   protected XMLEntityScanner fXML10EntityScanner;
   protected XMLEntityScanner fXML11EntityScanner;
   protected int fEntityExpansionLimit;
   protected int fEntityExpansionCount;
   protected final Hashtable fEntities;
   protected final Stack fEntityStack;
   protected ScannedEntity fCurrentEntity;
   protected Hashtable fDeclaredEntities;
   private final XMLResourceIdentifierImpl fResourceIdentifier;
   private final Augmentations fEntityAugs;
   private final ByteBufferPool fSmallByteBufferPool;
   private final ByteBufferPool fLargeByteBufferPool;
   private byte[] fTempByteBuffer;
   private final CharacterBufferPool fCharacterBufferPool;
   protected Stack fReaderStack;
   private static String gUserDir;
   private static URI gUserDirURI;
   private static final boolean[] gNeedEscaping;
   private static final char[] gAfterEscaping1;
   private static final char[] gAfterEscaping2;
   private static final char[] gHexChs;
   private static PrivilegedAction GET_USER_DIR_SYSTEM_PROPERTY;

   public XMLEntityManager() {
      this((XMLEntityManager)null);
   }

   public XMLEntityManager(XMLEntityManager var1) {
      this.fExternalGeneralEntities = true;
      this.fExternalParameterEntities = true;
      this.fBufferSize = 2048;
      this.fSecurityManager = null;
      this.fInExternalSubset = false;
      this.fEntityExpansionLimit = 0;
      this.fEntityExpansionCount = 0;
      this.fEntities = new Hashtable();
      this.fEntityStack = new Stack();
      this.fResourceIdentifier = new XMLResourceIdentifierImpl();
      this.fEntityAugs = new AugmentationsImpl();
      this.fSmallByteBufferPool = new ByteBufferPool(this.fBufferSize);
      this.fLargeByteBufferPool = new ByteBufferPool(this.fBufferSize << 1);
      this.fTempByteBuffer = null;
      this.fCharacterBufferPool = new CharacterBufferPool(this.fBufferSize, 512);
      this.fReaderStack = new Stack();
      this.fDeclaredEntities = var1 != null ? var1.getDeclaredEntities() : null;
      this.setScannerVersion((short)1);
   }

   public void setStandalone(boolean var1) {
      this.fStandalone = var1;
   }

   public boolean isStandalone() {
      return this.fStandalone;
   }

   final void notifyHasPEReferences() {
      this.fHasPEReferences = true;
   }

   final boolean hasPEReferences() {
      return this.fHasPEReferences;
   }

   public void setEntityHandler(XMLEntityHandler var1) {
      this.fEntityHandler = var1;
   }

   public XMLResourceIdentifier getCurrentResourceIdentifier() {
      return this.fResourceIdentifier;
   }

   public ScannedEntity getCurrentEntity() {
      return this.fCurrentEntity;
   }

   public void addInternalEntity(String var1, String var2, int var3) {
      if (!this.fEntities.containsKey(var1)) {
         InternalEntity var4 = new InternalEntity(var1, var2, this.fInExternalSubset, var3);
         this.fEntities.put(var1, var4);
      } else if (this.fWarnDuplicateEntityDef) {
         this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_DUPLICATE_ENTITY_DEFINITION", new Object[]{var1}, (short)0);
      }

   }

   public void addInternalEntity(String var1, String var2) {
      this.addInternalEntity(var1, var2, 0);
   }

   public int getParamEntityRefCount(String var1) {
      if (var1 != null && var1.length() > 0 && var1.charAt(0) == '%') {
         Entity var2 = (Entity)this.fEntities.get(var1);
         if (var2 != null && !var2.isExternal()) {
            return ((InternalEntity)var2).paramEntityRefs;
         }
      }

      return 0;
   }

   public void addExternalEntity(String var1, String var2, String var3, String var4) throws IOException {
      if (!this.fEntities.containsKey(var1)) {
         if (var4 == null) {
            int var5 = this.fEntityStack.size();
            if (var5 == 0 && this.fCurrentEntity != null && this.fCurrentEntity.entityLocation != null) {
               var4 = this.fCurrentEntity.entityLocation.getExpandedSystemId();
            }

            for(int var6 = var5 - 1; var6 >= 0; --var6) {
               ScannedEntity var7 = (ScannedEntity)this.fEntityStack.elementAt(var6);
               if (var7.entityLocation != null && var7.entityLocation.getExpandedSystemId() != null) {
                  var4 = var7.entityLocation.getExpandedSystemId();
                  break;
               }
            }
         }

         ExternalEntity var8 = new ExternalEntity(var1, new XMLEntityDescriptionImpl(var1, var2, var3, var4, expandSystemId(var3, var4, false)), (String)null, this.fInExternalSubset);
         this.fEntities.put(var1, var8);
      } else if (this.fWarnDuplicateEntityDef) {
         this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_DUPLICATE_ENTITY_DEFINITION", new Object[]{var1}, (short)0);
      }

   }

   public boolean isExternalEntity(String var1) {
      Entity var2 = (Entity)this.fEntities.get(var1);
      return var2 == null ? false : var2.isExternal();
   }

   public boolean isEntityDeclInExternalSubset(String var1) {
      Entity var2 = (Entity)this.fEntities.get(var1);
      return var2 == null ? false : var2.isEntityDeclInExternalSubset();
   }

   public void addUnparsedEntity(String var1, String var2, String var3, String var4, String var5) {
      if (!this.fEntities.containsKey(var1)) {
         ExternalEntity var6 = new ExternalEntity(var1, new XMLEntityDescriptionImpl(var1, var2, var3, var4, (String)null), var5, this.fInExternalSubset);
         this.fEntities.put(var1, var6);
      } else if (this.fWarnDuplicateEntityDef) {
         this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_DUPLICATE_ENTITY_DEFINITION", new Object[]{var1}, (short)0);
      }

   }

   public boolean isUnparsedEntity(String var1) {
      Entity var2 = (Entity)this.fEntities.get(var1);
      return var2 == null ? false : var2.isUnparsed();
   }

   public boolean isDeclaredEntity(String var1) {
      Entity var2 = (Entity)this.fEntities.get(var1);
      return var2 != null;
   }

   public XMLInputSource resolveEntity(XMLResourceIdentifier var1) throws IOException, XNIException {
      if (var1 == null) {
         return null;
      } else {
         String var2 = var1.getPublicId();
         String var3 = var1.getLiteralSystemId();
         String var4 = var1.getBaseSystemId();
         String var5 = var1.getExpandedSystemId();
         boolean var6 = var5 == null;
         if (var4 == null && this.fCurrentEntity != null && this.fCurrentEntity.entityLocation != null) {
            var4 = this.fCurrentEntity.entityLocation.getExpandedSystemId();
            if (var4 != null) {
               var6 = true;
            }
         }

         XMLInputSource var7 = null;
         if (this.fEntityResolver != null) {
            if (var6) {
               var5 = expandSystemId(var3, var4, false);
            }

            var1.setBaseSystemId(var4);
            var1.setExpandedSystemId(var5);
            var7 = this.fEntityResolver.resolveEntity(var1);
         }

         if (var7 == null) {
            var7 = new XMLInputSource(var2, var3, var4);
         }

         return var7;
      }
   }

   public void startEntity(String var1, boolean var2) throws IOException, XNIException {
      Entity var3 = (Entity)this.fEntities.get(var1);
      if (var3 == null) {
         if (this.fEntityHandler != null) {
            Object var15 = null;
            this.fResourceIdentifier.clear();
            this.fEntityAugs.removeAllItems();
            this.fEntityAugs.putItem("ENTITY_SKIPPED", Boolean.TRUE);
            this.fEntityHandler.startEntity(var1, this.fResourceIdentifier, (String)var15, this.fEntityAugs);
            this.fEntityAugs.removeAllItems();
            this.fEntityAugs.putItem("ENTITY_SKIPPED", Boolean.TRUE);
            this.fEntityHandler.endEntity(var1, this.fEntityAugs);
         }

      } else {
         boolean var4 = var3.isExternal();
         StringBuffer var8;
         String var10;
         String var12;
         if (var4 && (this.fValidationManager == null || !this.fValidationManager.isCachedDTD())) {
            boolean var5 = var3.isUnparsed();
            boolean var6 = var1.startsWith("%");
            boolean var7 = !var6;
            if (var5 || var7 && !this.fExternalGeneralEntities || var6 && !this.fExternalParameterEntities) {
               if (this.fEntityHandler != null) {
                  this.fResourceIdentifier.clear();
                  var8 = null;
                  ExternalEntity var24 = (ExternalEntity)var3;
                  var10 = var24.entityLocation != null ? var24.entityLocation.getLiteralSystemId() : null;
                  String var25 = var24.entityLocation != null ? var24.entityLocation.getBaseSystemId() : null;
                  var12 = expandSystemId(var10, var25, false);
                  this.fResourceIdentifier.setValues(var24.entityLocation != null ? var24.entityLocation.getPublicId() : null, var10, var25, var12);
                  this.fEntityAugs.removeAllItems();
                  this.fEntityAugs.putItem("ENTITY_SKIPPED", Boolean.TRUE);
                  this.fEntityHandler.startEntity(var1, this.fResourceIdentifier, var8, this.fEntityAugs);
                  this.fEntityAugs.removeAllItems();
                  this.fEntityAugs.putItem("ENTITY_SKIPPED", Boolean.TRUE);
                  this.fEntityHandler.endEntity(var1, this.fEntityAugs);
               }

               return;
            }
         }

         int var16 = this.fEntityStack.size();

         Object var18;
         for(int var17 = var16; var17 >= 0; --var17) {
            var18 = var17 == var16 ? this.fCurrentEntity : (Entity)this.fEntityStack.elementAt(var17);
            if (((Entity)var18).name == var1) {
               var8 = new StringBuffer(var1);

               for(int var9 = var17 + 1; var9 < var16; ++var9) {
                  Entity var19 = (Entity)this.fEntityStack.elementAt(var9);
                  var8.append(" -> ");
                  var8.append(var19.name);
               }

               var8.append(" -> ");
               var8.append(this.fCurrentEntity.name);
               var8.append(" -> ");
               var8.append(var1);
               this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "RecursiveReference", new Object[]{var1, var8.toString()}, (short)2);
               if (this.fEntityHandler != null) {
                  this.fResourceIdentifier.clear();
                  var10 = null;
                  if (var4) {
                     ExternalEntity var11 = (ExternalEntity)var3;
                     var12 = var11.entityLocation != null ? var11.entityLocation.getLiteralSystemId() : null;
                     String var13 = var11.entityLocation != null ? var11.entityLocation.getBaseSystemId() : null;
                     String var14 = expandSystemId(var12, var13, false);
                     this.fResourceIdentifier.setValues(var11.entityLocation != null ? var11.entityLocation.getPublicId() : null, var12, var13, var14);
                  }

                  this.fEntityAugs.removeAllItems();
                  this.fEntityAugs.putItem("ENTITY_SKIPPED", Boolean.TRUE);
                  this.fEntityHandler.startEntity(var1, this.fResourceIdentifier, var10, this.fEntityAugs);
                  this.fEntityAugs.removeAllItems();
                  this.fEntityAugs.putItem("ENTITY_SKIPPED", Boolean.TRUE);
                  this.fEntityHandler.endEntity(var1, this.fEntityAugs);
               }

               return;
            }
         }

         var18 = null;
         XMLInputSource var20;
         if (var4) {
            ExternalEntity var21 = (ExternalEntity)var3;
            var20 = this.resolveEntity(var21.entityLocation);
         } else {
            InternalEntity var22 = (InternalEntity)var3;
            StringReader var23 = new StringReader(var22.text);
            var20 = new XMLInputSource((String)null, (String)null, (String)null, var23, (String)null);
         }

         this.startEntity(var1, var20, var2, var4);
      }
   }

   public void startDocumentEntity(XMLInputSource var1) throws IOException, XNIException {
      this.startEntity(XMLEntity, var1, false, true);
   }

   public void startDTDEntity(XMLInputSource var1) throws IOException, XNIException {
      this.startEntity(DTDEntity, var1, false, true);
   }

   public void startExternalSubset() {
      this.fInExternalSubset = true;
   }

   public void endExternalSubset() {
      this.fInExternalSubset = false;
   }

   public void startEntity(String var1, XMLInputSource var2, boolean var3, boolean var4) throws IOException, XNIException {
      String var5 = this.setupCurrentEntity(var1, var2, var3, var4);
      if (this.fSecurityManager != null) {
         this.fEntityExpansionCount += this.getParamEntityRefCount(var1);
         if (this.fEntityExpansionCount++ > this.fEntityExpansionLimit) {
            this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "EntityExpansionLimitExceeded", new Object[]{new Integer(this.fEntityExpansionLimit)}, (short)2);
            this.fEntityExpansionCount = 0;
         }
      }

      if (this.fEntityHandler != null) {
         this.fEntityHandler.startEntity(var1, this.fResourceIdentifier, var5, (Augmentations)null);
      }

   }

   public String setupCurrentEntity(String var1, XMLInputSource var2, boolean var3, boolean var4) throws IOException, XNIException {
      String var5 = var2.getPublicId();
      String var6 = var2.getSystemId();
      String var7 = var2.getBaseSystemId();
      String var8 = var2.getEncoding();
      boolean var9 = var8 != null;
      Boolean var10 = null;
      this.fTempByteBuffer = null;
      RewindableInputStream var11 = null;
      Reader var12 = var2.getCharacterStream();
      String var13 = expandSystemId(var6, var7, this.fStrictURI);
      if (var7 == null) {
         var7 = var13;
      }

      if (var12 == null) {
         InputStream var21 = var2.getByteStream();
         if (var21 == null) {
            URL var14 = new URL(var13);
            URLConnection var15 = var14.openConnection();
            if (!(var15 instanceof HttpURLConnection)) {
               var21 = var15.getInputStream();
            } else {
               boolean var16 = true;
               if (var2 instanceof HTTPInputSource) {
                  HttpURLConnection var17 = (HttpURLConnection)var15;
                  HTTPInputSource var18 = (HTTPInputSource)var2;
                  Iterator var19 = var18.getHTTPRequestProperties();

                  while(var19.hasNext()) {
                     Map.Entry var20 = (Map.Entry)var19.next();
                     var17.setRequestProperty((String)var20.getKey(), (String)var20.getValue());
                  }

                  var16 = var18.getFollowHTTPRedirects();
                  if (!var16) {
                     var17.setInstanceFollowRedirects(var16);
                  }
               }

               var21 = var15.getInputStream();
               if (var16) {
                  String var26 = var15.getURL().toString();
                  if (!var26.equals(var13)) {
                     var6 = var26;
                     var13 = var26;
                  }
               }
            }
         }

         RewindableInputStream var22 = new RewindableInputStream(var21);
         var11 = var22;
         int var25;
         if (var8 == null) {
            byte[] var23 = new byte[4];

            for(var25 = 0; var25 < 4; ++var25) {
               var23[var25] = (byte)var22.readAndBuffer();
            }

            if (var25 == 4) {
               EncodingInfo var27 = this.getEncodingInfo(var23, var25);
               var8 = var27.autoDetectedEncoding;
               String var29 = var27.readerEncoding;
               var10 = var27.isBigEndian;
               var22.reset();
               if (var27.hasBOM) {
                  if (var29 == "UTF-8") {
                     var22.skip(3L);
                  } else if (var29 == "UTF-16") {
                     var22.skip(2L);
                  }
               }

               var12 = this.createReader(var22, var29, var10);
            } else {
               var12 = this.createReader(var22, var8, var10);
            }
         } else {
            var8 = var8.toUpperCase(Locale.ENGLISH);
            int[] var24;
            if (var8.equals("UTF-8")) {
               var24 = new int[3];

               for(var25 = 0; var25 < 3; ++var25) {
                  var24[var25] = var22.readAndBuffer();
                  if (var24[var25] == -1) {
                     break;
                  }
               }

               if (var25 == 3) {
                  if (var24[0] != 239 || var24[1] != 187 || var24[2] != 191) {
                     var22.reset();
                  }
               } else {
                  var22.reset();
               }

               var12 = this.createReader(var22, "UTF-8", var10);
            } else if (var8.equals("UTF-16")) {
               var24 = new int[4];

               for(var25 = 0; var25 < 4; ++var25) {
                  var24[var25] = var22.readAndBuffer();
                  if (var24[var25] == -1) {
                     break;
                  }
               }

               var22.reset();
               if (var25 >= 2) {
                  int var28 = var24[0];
                  int var30 = var24[1];
                  if (var28 == 254 && var30 == 255) {
                     var10 = Boolean.TRUE;
                     var22.skip(2L);
                  } else if (var28 == 255 && var30 == 254) {
                     var10 = Boolean.FALSE;
                     var22.skip(2L);
                  } else if (var25 == 4) {
                     int var31 = var24[2];
                     int var32 = var24[3];
                     if (var28 == 0 && var30 == 60 && var31 == 0 && var32 == 63) {
                        var10 = Boolean.TRUE;
                     }

                     if (var28 == 60 && var30 == 0 && var31 == 63 && var32 == 0) {
                        var10 = Boolean.FALSE;
                     }
                  }
               }

               var12 = this.createReader(var22, "UTF-16", var10);
            } else if (var8.equals("ISO-10646-UCS-4")) {
               var24 = new int[4];

               for(var25 = 0; var25 < 4; ++var25) {
                  var24[var25] = var22.readAndBuffer();
                  if (var24[var25] == -1) {
                     break;
                  }
               }

               var22.reset();
               if (var25 == 4) {
                  if (var24[0] == 0 && var24[1] == 0 && var24[2] == 0 && var24[3] == 60) {
                     var10 = Boolean.TRUE;
                  } else if (var24[0] == 60 && var24[1] == 0 && var24[2] == 0 && var24[3] == 0) {
                     var10 = Boolean.FALSE;
                  }
               }

               var12 = this.createReader(var22, var8, var10);
            } else if (var8.equals("ISO-10646-UCS-2")) {
               var24 = new int[4];

               for(var25 = 0; var25 < 4; ++var25) {
                  var24[var25] = var22.readAndBuffer();
                  if (var24[var25] == -1) {
                     break;
                  }
               }

               var22.reset();
               if (var25 == 4) {
                  if (var24[0] == 0 && var24[1] == 60 && var24[2] == 0 && var24[3] == 63) {
                     var10 = Boolean.TRUE;
                  } else if (var24[0] == 60 && var24[1] == 0 && var24[2] == 63 && var24[3] == 0) {
                     var10 = Boolean.FALSE;
                  }
               }

               var12 = this.createReader(var22, var8, var10);
            } else {
               var12 = this.createReader(var22, var8, var10);
            }
         }
      }

      this.fReaderStack.push(var12);
      if (this.fCurrentEntity != null) {
         this.fEntityStack.push(this.fCurrentEntity);
      }

      this.fCurrentEntity = new ScannedEntity(var1, new XMLResourceIdentifierImpl(var5, var6, var7, var13), var11, var12, this.fTempByteBuffer, var8, var3, false, var4);
      this.fCurrentEntity.setEncodingExternallySpecified(var9);
      this.fEntityScanner.setCurrentEntity(this.fCurrentEntity);
      this.fResourceIdentifier.setValues(var5, var6, var7, var13);
      return var8;
   }

   public void setScannerVersion(short var1) {
      if (var1 == 1) {
         if (this.fXML10EntityScanner == null) {
            this.fXML10EntityScanner = new XMLEntityScanner();
         }

         this.fXML10EntityScanner.reset(this.fSymbolTable, this, this.fErrorReporter);
         this.fEntityScanner = this.fXML10EntityScanner;
         this.fEntityScanner.setCurrentEntity(this.fCurrentEntity);
      } else {
         if (this.fXML11EntityScanner == null) {
            this.fXML11EntityScanner = new XML11EntityScanner();
         }

         this.fXML11EntityScanner.reset(this.fSymbolTable, this, this.fErrorReporter);
         this.fEntityScanner = this.fXML11EntityScanner;
         this.fEntityScanner.setCurrentEntity(this.fCurrentEntity);
      }

   }

   public XMLEntityScanner getEntityScanner() {
      if (this.fEntityScanner == null) {
         if (this.fXML10EntityScanner == null) {
            this.fXML10EntityScanner = new XMLEntityScanner();
         }

         this.fXML10EntityScanner.reset(this.fSymbolTable, this, this.fErrorReporter);
         this.fEntityScanner = this.fXML10EntityScanner;
      }

      return this.fEntityScanner;
   }

   public void closeReaders() {
      for(int var1 = this.fReaderStack.size() - 1; var1 >= 0; --var1) {
         try {
            ((Reader)this.fReaderStack.pop()).close();
         } catch (IOException var3) {
         }
      }

   }

   public void reset(XMLComponentManager var1) throws XMLConfigurationException {
      boolean var2;
      try {
         var2 = var1.getFeature("http://apache.org/xml/features/internal/parser-settings");
      } catch (XMLConfigurationException var13) {
         var2 = true;
      }

      if (!var2) {
         this.reset();
      } else {
         try {
            this.fValidation = var1.getFeature("http://xml.org/sax/features/validation");
         } catch (XMLConfigurationException var12) {
            this.fValidation = false;
         }

         try {
            this.fExternalGeneralEntities = var1.getFeature("http://xml.org/sax/features/external-general-entities");
         } catch (XMLConfigurationException var11) {
            this.fExternalGeneralEntities = true;
         }

         try {
            this.fExternalParameterEntities = var1.getFeature("http://xml.org/sax/features/external-parameter-entities");
         } catch (XMLConfigurationException var10) {
            this.fExternalParameterEntities = true;
         }

         try {
            this.fAllowJavaEncodings = var1.getFeature("http://apache.org/xml/features/allow-java-encodings");
         } catch (XMLConfigurationException var9) {
            this.fAllowJavaEncodings = false;
         }

         try {
            this.fWarnDuplicateEntityDef = var1.getFeature("http://apache.org/xml/features/warn-on-duplicate-entitydef");
         } catch (XMLConfigurationException var8) {
            this.fWarnDuplicateEntityDef = false;
         }

         try {
            this.fStrictURI = var1.getFeature("http://apache.org/xml/features/standard-uri-conformant");
         } catch (XMLConfigurationException var7) {
            this.fStrictURI = false;
         }

         this.fSymbolTable = (SymbolTable)var1.getProperty("http://apache.org/xml/properties/internal/symbol-table");
         this.fErrorReporter = (XMLErrorReporter)var1.getProperty("http://apache.org/xml/properties/internal/error-reporter");

         try {
            this.fEntityResolver = (XMLEntityResolver)var1.getProperty("http://apache.org/xml/properties/internal/entity-resolver");
         } catch (XMLConfigurationException var6) {
            this.fEntityResolver = null;
         }

         try {
            this.fValidationManager = (ValidationManager)var1.getProperty("http://apache.org/xml/properties/internal/validation-manager");
         } catch (XMLConfigurationException var5) {
            this.fValidationManager = null;
         }

         try {
            this.fSecurityManager = (SecurityManager)var1.getProperty("http://apache.org/xml/properties/security-manager");
         } catch (XMLConfigurationException var4) {
            this.fSecurityManager = null;
         }

         this.reset();
      }
   }

   public void reset() {
      this.fEntityExpansionLimit = this.fSecurityManager != null ? this.fSecurityManager.getEntityExpansionLimit() : 0;
      this.fStandalone = false;
      this.fHasPEReferences = false;
      this.fEntities.clear();
      this.fEntityStack.removeAllElements();
      this.fEntityExpansionCount = 0;
      this.fCurrentEntity = null;
      if (this.fXML10EntityScanner != null) {
         this.fXML10EntityScanner.reset(this.fSymbolTable, this, this.fErrorReporter);
      }

      if (this.fXML11EntityScanner != null) {
         this.fXML11EntityScanner.reset(this.fSymbolTable, this, this.fErrorReporter);
      }

      if (this.fDeclaredEntities != null) {
         Iterator var1 = this.fDeclaredEntities.entrySet().iterator();

         while(var1.hasNext()) {
            Map.Entry var2 = (Map.Entry)var1.next();
            Object var3 = var2.getKey();
            Object var4 = var2.getValue();
            this.fEntities.put(var3, var4);
         }
      }

      this.fEntityHandler = null;
   }

   public String[] getRecognizedFeatures() {
      return (String[])RECOGNIZED_FEATURES.clone();
   }

   public void setFeature(String var1, boolean var2) throws XMLConfigurationException {
      if (var1.startsWith("http://apache.org/xml/features/")) {
         int var3 = var1.length() - "http://apache.org/xml/features/".length();
         if (var3 == "allow-java-encodings".length() && var1.endsWith("allow-java-encodings")) {
            this.fAllowJavaEncodings = var2;
         }
      }

   }

   public String[] getRecognizedProperties() {
      return (String[])RECOGNIZED_PROPERTIES.clone();
   }

   public void setProperty(String var1, Object var2) throws XMLConfigurationException {
      if (var1.startsWith("http://apache.org/xml/properties/")) {
         int var3 = var1.length() - "http://apache.org/xml/properties/".length();
         if (var3 == "internal/symbol-table".length() && var1.endsWith("internal/symbol-table")) {
            this.fSymbolTable = (SymbolTable)var2;
            return;
         }

         if (var3 == "internal/error-reporter".length() && var1.endsWith("internal/error-reporter")) {
            this.fErrorReporter = (XMLErrorReporter)var2;
            return;
         }

         if (var3 == "internal/entity-resolver".length() && var1.endsWith("internal/entity-resolver")) {
            this.fEntityResolver = (XMLEntityResolver)var2;
            return;
         }

         if (var3 == "input-buffer-size".length() && var1.endsWith("input-buffer-size")) {
            Integer var4 = (Integer)var2;
            if (var4 != null && var4 > 64) {
               this.fBufferSize = var4;
               this.fEntityScanner.setBufferSize(this.fBufferSize);
               this.fSmallByteBufferPool.setBufferSize(this.fBufferSize);
               this.fLargeByteBufferPool.setBufferSize(this.fBufferSize << 1);
               this.fCharacterBufferPool.setExternalBufferSize(this.fBufferSize);
            }
         }

         if (var3 == "security-manager".length() && var1.endsWith("security-manager")) {
            this.fSecurityManager = (SecurityManager)var2;
            this.fEntityExpansionLimit = this.fSecurityManager != null ? this.fSecurityManager.getEntityExpansionLimit() : 0;
         }
      }

   }

   public Boolean getFeatureDefault(String var1) {
      for(int var2 = 0; var2 < RECOGNIZED_FEATURES.length; ++var2) {
         if (RECOGNIZED_FEATURES[var2].equals(var1)) {
            return FEATURE_DEFAULTS[var2];
         }
      }

      return null;
   }

   public Object getPropertyDefault(String var1) {
      for(int var2 = 0; var2 < RECOGNIZED_PROPERTIES.length; ++var2) {
         if (RECOGNIZED_PROPERTIES[var2].equals(var1)) {
            return PROPERTY_DEFAULTS[var2];
         }
      }

      return null;
   }

   private static synchronized URI getUserDir() throws URI.MalformedURIException {
      String var0 = "";

      try {
         var0 = (String)AccessController.doPrivileged(GET_USER_DIR_SYSTEM_PROPERTY);
      } catch (SecurityException var10) {
      }

      if (var0.length() == 0) {
         return new URI("file", "", "", (String)null, (String)null);
      } else if (gUserDirURI != null && var0.equals(gUserDir)) {
         return gUserDirURI;
      } else {
         gUserDir = var0;
         char var1 = File.separatorChar;
         var0 = var0.replace(var1, '/');
         int var2 = var0.length();
         StringBuffer var4 = new StringBuffer(var2 * 3);
         int var3;
         if (var2 >= 2 && var0.charAt(1) == ':') {
            var3 = Character.toUpperCase(var0.charAt(0));
            if (var3 >= 65 && var3 <= 90) {
               var4.append('/');
            }
         }

         int var5;
         for(var5 = 0; var5 < var2; ++var5) {
            var3 = var0.charAt(var5);
            if (var3 >= 128) {
               break;
            }

            if (gNeedEscaping[var3]) {
               var4.append('%');
               var4.append(gAfterEscaping1[var3]);
               var4.append(gAfterEscaping2[var3]);
            } else {
               var4.append((char)var3);
            }
         }

         if (var5 < var2) {
            Object var6 = null;

            byte[] var11;
            try {
               var11 = var0.substring(var5).getBytes("UTF-8");
            } catch (UnsupportedEncodingException var9) {
               return new URI("file", "", var0, (String)null, (String)null);
            }

            var2 = var11.length;

            for(var5 = 0; var5 < var2; ++var5) {
               byte var7 = var11[var5];
               if (var7 < 0) {
                  var3 = var7 + 256;
                  var4.append('%');
                  var4.append(gHexChs[var3 >> 4]);
                  var4.append(gHexChs[var3 & 15]);
               } else if (gNeedEscaping[var7]) {
                  var4.append('%');
                  var4.append(gAfterEscaping1[var7]);
                  var4.append(gAfterEscaping2[var7]);
               } else {
                  var4.append((char)var7);
               }
            }
         }

         if (!var0.endsWith("/")) {
            var4.append('/');
         }

         gUserDirURI = new URI("file", "", var4.toString(), (String)null, (String)null);
         return gUserDirURI;
      }
   }

   public static void absolutizeAgainstUserDir(URI var0) throws URI.MalformedURIException {
      var0.absolutize(getUserDir());
   }

   public static String expandSystemId(String var0, String var1, boolean var2) throws URI.MalformedURIException {
      if (var0 == null) {
         return null;
      } else if (var2) {
         return expandSystemIdStrictOn(var0, var1);
      } else {
         try {
            return expandSystemIdStrictOff(var0, var1);
         } catch (URI.MalformedURIException var9) {
            if (var0.length() == 0) {
               return var0;
            } else {
               String var3 = fixURI(var0);
               URI var4 = null;
               URI var5 = null;

               try {
                  if (var1 != null && var1.length() != 0 && !var1.equals(var0)) {
                     try {
                        var4 = new URI(fixURI(var1).trim());
                     } catch (URI.MalformedURIException var7) {
                        if (var1.indexOf(58) != -1) {
                           var4 = new URI("file", "", fixURI(var1).trim(), (String)null, (String)null);
                        } else {
                           var4 = new URI(getUserDir(), fixURI(var1));
                        }
                     }
                  } else {
                     var4 = getUserDir();
                  }

                  var5 = new URI(var4, var3.trim());
               } catch (Exception var8) {
               }

               return var5 == null ? var0 : var5.toString();
            }
         }
      }
   }

   private static String expandSystemIdStrictOn(String var0, String var1) throws URI.MalformedURIException {
      URI var2 = new URI(var0, true);
      if (var2.isAbsoluteURI()) {
         return var0;
      } else {
         URI var3 = null;
         if (var1 != null && var1.length() != 0) {
            var3 = new URI(var1, true);
            if (!var3.isAbsoluteURI()) {
               var3.absolutize(getUserDir());
            }
         } else {
            var3 = getUserDir();
         }

         var2.absolutize(var3);
         return var2.toString();
      }
   }

   private static String expandSystemIdStrictOff(String var0, String var1) throws URI.MalformedURIException {
      URI var2 = new URI(var0, true);
      if (var2.isAbsoluteURI()) {
         if (var2.getScheme().length() > 1) {
            return var0;
         } else {
            throw new URI.MalformedURIException();
         }
      } else {
         URI var3 = null;
         if (var1 != null && var1.length() != 0) {
            var3 = new URI(var1, true);
            if (!var3.isAbsoluteURI()) {
               var3.absolutize(getUserDir());
            }
         } else {
            var3 = getUserDir();
         }

         var2.absolutize(var3);
         return var2.toString();
      }
   }

   public static OutputStream createOutputStream(String var0) throws IOException {
      String var1 = expandSystemId(var0, (String)null, true);
      URL var2 = new URL(var1 != null ? var1 : var0);
      Object var3 = null;
      String var4 = var2.getProtocol();
      String var5 = var2.getHost();
      if (var4.equals("file") && (var5 == null || var5.length() == 0 || var5.equals("localhost"))) {
         File var8 = new File(getPathWithoutEscapes(var2.getPath()));
         if (!var8.exists()) {
            File var9 = var8.getParentFile();
            if (var9 != null && !var9.exists()) {
               var9.mkdirs();
            }
         }

         var3 = new FileOutputStream(var8);
      } else {
         URLConnection var6 = var2.openConnection();
         var6.setDoInput(false);
         var6.setDoOutput(true);
         var6.setUseCaches(false);
         if (var6 instanceof HttpURLConnection) {
            HttpURLConnection var7 = (HttpURLConnection)var6;
            var7.setRequestMethod("PUT");
         }

         var3 = var6.getOutputStream();
      }

      return (OutputStream)var3;
   }

   private static String getPathWithoutEscapes(String var0) {
      if (var0 != null && var0.length() != 0 && var0.indexOf(37) != -1) {
         StringTokenizer var1 = new StringTokenizer(var0, "%");
         StringBuffer var2 = new StringBuffer(var0.length());
         int var3 = var1.countTokens();
         var2.append(var1.nextToken());

         for(int var4 = 1; var4 < var3; ++var4) {
            String var5 = var1.nextToken();
            var2.append((char)Integer.valueOf(var5.substring(0, 2), 16));
            var2.append(var5.substring(2));
         }

         return var2.toString();
      } else {
         return var0;
      }
   }

   void endEntity() throws XNIException {
      if (this.fEntityHandler != null) {
         this.fEntityHandler.endEntity(this.fCurrentEntity.name, (Augmentations)null);
      }

      try {
         this.fCurrentEntity.reader.close();
      } catch (IOException var2) {
      }

      if (!this.fReaderStack.isEmpty()) {
         this.fReaderStack.pop();
      }

      this.fCharacterBufferPool.returnBuffer(this.fCurrentEntity.fCharacterBuffer);
      if (this.fCurrentEntity.fByteBuffer != null) {
         if (this.fCurrentEntity.fByteBuffer.length == this.fBufferSize) {
            this.fSmallByteBufferPool.returnBuffer(this.fCurrentEntity.fByteBuffer);
         } else {
            this.fLargeByteBufferPool.returnBuffer(this.fCurrentEntity.fByteBuffer);
         }
      }

      this.fCurrentEntity = this.fEntityStack.size() > 0 ? (ScannedEntity)this.fEntityStack.pop() : null;
      this.fEntityScanner.setCurrentEntity(this.fCurrentEntity);
   }

   protected EncodingInfo getEncodingInfo(byte[] var1, int var2) {
      if (var2 < 2) {
         return XMLEntityManager.EncodingInfo.UTF_8;
      } else {
         int var3 = var1[0] & 255;
         int var4 = var1[1] & 255;
         if (var3 == 254 && var4 == 255) {
            return XMLEntityManager.EncodingInfo.UTF_16_BIG_ENDIAN_WITH_BOM;
         } else if (var3 == 255 && var4 == 254) {
            return XMLEntityManager.EncodingInfo.UTF_16_LITTLE_ENDIAN_WITH_BOM;
         } else if (var2 < 3) {
            return XMLEntityManager.EncodingInfo.UTF_8;
         } else {
            int var5 = var1[2] & 255;
            if (var3 == 239 && var4 == 187 && var5 == 191) {
               return XMLEntityManager.EncodingInfo.UTF_8_WITH_BOM;
            } else if (var2 < 4) {
               return XMLEntityManager.EncodingInfo.UTF_8;
            } else {
               int var6 = var1[3] & 255;
               if (var3 == 0 && var4 == 0 && var5 == 0 && var6 == 60) {
                  return XMLEntityManager.EncodingInfo.UCS_4_BIG_ENDIAN;
               } else if (var3 == 60 && var4 == 0 && var5 == 0 && var6 == 0) {
                  return XMLEntityManager.EncodingInfo.UCS_4_LITTLE_ENDIAN;
               } else if (var3 == 0 && var4 == 0 && var5 == 60 && var6 == 0) {
                  return XMLEntityManager.EncodingInfo.UCS_4_UNUSUAL_BYTE_ORDER;
               } else if (var3 == 0 && var4 == 60 && var5 == 0 && var6 == 0) {
                  return XMLEntityManager.EncodingInfo.UCS_4_UNUSUAL_BYTE_ORDER;
               } else if (var3 == 0 && var4 == 60 && var5 == 0 && var6 == 63) {
                  return XMLEntityManager.EncodingInfo.UTF_16_BIG_ENDIAN;
               } else if (var3 == 60 && var4 == 0 && var5 == 63 && var6 == 0) {
                  return XMLEntityManager.EncodingInfo.UTF_16_LITTLE_ENDIAN;
               } else {
                  return var3 == 76 && var4 == 111 && var5 == 167 && var6 == 148 ? XMLEntityManager.EncodingInfo.EBCDIC : XMLEntityManager.EncodingInfo.UTF_8;
               }
            }
         }
      }
   }

   protected Reader createReader(InputStream var1, String var2, Boolean var3) throws IOException {
      if (var2 != "UTF-8" && var2 != null) {
         if (var2 == "UTF-16" && var3 != null) {
            return this.createUTF16Reader(var1, var3);
         } else {
            String var4 = var2.toUpperCase(Locale.ENGLISH);
            if (var4.equals("UTF-8")) {
               return this.createUTF8Reader(var1);
            } else if (var4.equals("UTF-16BE")) {
               return this.createUTF16Reader(var1, true);
            } else if (var4.equals("UTF-16LE")) {
               return this.createUTF16Reader(var1, false);
            } else {
               boolean var5;
               if (var4.equals("ISO-10646-UCS-4")) {
                  if (var3 != null) {
                     var5 = var3;
                     if (var5) {
                        return new UCSReader(var1, (short)8);
                     }

                     return new UCSReader(var1, (short)4);
                  }

                  this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "EncodingByteOrderUnsupported", new Object[]{var2}, (short)2);
               }

               if (var4.equals("ISO-10646-UCS-2")) {
                  if (var3 != null) {
                     var5 = var3;
                     if (var5) {
                        return new UCSReader(var1, (short)2);
                     }

                     return new UCSReader(var1, (short)1);
                  }

                  this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "EncodingByteOrderUnsupported", new Object[]{var2}, (short)2);
               }

               var5 = XMLChar.isValidIANAEncoding(var2);
               boolean var6 = XMLChar.isValidJavaEncoding(var2);
               if (!var5 || this.fAllowJavaEncodings && !var6) {
                  this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "EncodingDeclInvalid", new Object[]{var2}, (short)2);
                  return this.createLatin1Reader(var1);
               } else {
                  String var7 = EncodingMap.getIANA2JavaMapping(var4);
                  if (var7 == null) {
                     if (!this.fAllowJavaEncodings) {
                        this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "EncodingDeclInvalid", new Object[]{var2}, (short)2);
                        return this.createLatin1Reader(var1);
                     }

                     var7 = var2;
                  } else {
                     if (var7.equals("ASCII")) {
                        return this.createASCIIReader(var1);
                     }

                     if (var7.equals("ISO8859_1")) {
                        return this.createLatin1Reader(var1);
                     }
                  }

                  return new InputStreamReader(var1, var7);
               }
            }
         }
      } else {
         return this.createUTF8Reader(var1);
      }
   }

   private Reader createUTF8Reader(InputStream var1) {
      if (this.fTempByteBuffer == null) {
         this.fTempByteBuffer = this.fSmallByteBufferPool.getBuffer();
      }

      return new UTF8Reader(var1, this.fTempByteBuffer, this.fErrorReporter.getMessageFormatter("http://www.w3.org/TR/1998/REC-xml-19980210"), this.fErrorReporter.getLocale());
   }

   private Reader createUTF16Reader(InputStream var1, boolean var2) {
      if (this.fTempByteBuffer == null) {
         this.fTempByteBuffer = this.fLargeByteBufferPool.getBuffer();
      } else if (this.fTempByteBuffer.length == this.fBufferSize) {
         this.fSmallByteBufferPool.returnBuffer(this.fTempByteBuffer);
         this.fTempByteBuffer = this.fLargeByteBufferPool.getBuffer();
      }

      return new UTF16Reader(var1, this.fTempByteBuffer, var2, this.fErrorReporter.getMessageFormatter("http://www.w3.org/TR/1998/REC-xml-19980210"), this.fErrorReporter.getLocale());
   }

   private Reader createASCIIReader(InputStream var1) {
      if (this.fTempByteBuffer == null) {
         this.fTempByteBuffer = this.fSmallByteBufferPool.getBuffer();
      }

      return new ASCIIReader(var1, this.fTempByteBuffer, this.fErrorReporter.getMessageFormatter("http://www.w3.org/TR/1998/REC-xml-19980210"), this.fErrorReporter.getLocale());
   }

   private Reader createLatin1Reader(InputStream var1) {
      if (this.fTempByteBuffer == null) {
         this.fTempByteBuffer = this.fSmallByteBufferPool.getBuffer();
      }

      return new Latin1Reader(var1, this.fTempByteBuffer);
   }

   protected static String fixURI(String var0) {
      var0 = var0.replace(File.separatorChar, '/');
      StringBuffer var1 = null;
      int var2;
      int var3;
      if (var0.length() >= 2) {
         var2 = var0.charAt(1);
         if (var2 == 58) {
            var3 = Character.toUpperCase(var0.charAt(0));
            if (var3 >= 65 && var3 <= 90) {
               var1 = new StringBuffer(var0.length() + 8);
               var1.append("file:///");
            }
         } else if (var2 == 47 && var0.charAt(0) == '/') {
            var1 = new StringBuffer(var0.length() + 5);
            var1.append("file:");
         }
      }

      var2 = var0.indexOf(32);
      if (var2 < 0) {
         if (var1 != null) {
            var1.append(var0);
            var0 = var1.toString();
         }
      } else {
         if (var1 == null) {
            var1 = new StringBuffer(var0.length());
         }

         for(var3 = 0; var3 < var2; ++var3) {
            var1.append(var0.charAt(var3));
         }

         var1.append("%20");

         for(int var4 = var2 + 1; var4 < var0.length(); ++var4) {
            if (var0.charAt(var4) == ' ') {
               var1.append("%20");
            } else {
               var1.append(var0.charAt(var4));
            }
         }

         var0 = var1.toString();
      }

      return var0;
   }

   Hashtable getDeclaredEntities() {
      return this.fEntities;
   }

   static final void print(ScannedEntity var0) {
   }

   static {
      FEATURE_DEFAULTS = new Boolean[]{null, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE};
      RECOGNIZED_PROPERTIES = new String[]{"http://apache.org/xml/properties/internal/symbol-table", "http://apache.org/xml/properties/internal/error-reporter", "http://apache.org/xml/properties/internal/entity-resolver", "http://apache.org/xml/properties/internal/validation-manager", "http://apache.org/xml/properties/input-buffer-size", "http://apache.org/xml/properties/security-manager"};
      PROPERTY_DEFAULTS = new Object[]{null, null, null, null, new Integer(2048), null};
      XMLEntity = "[xml]".intern();
      DTDEntity = "[dtd]".intern();
      gNeedEscaping = new boolean[128];
      gAfterEscaping1 = new char[128];
      gAfterEscaping2 = new char[128];
      gHexChs = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

      for(int var0 = 0; var0 <= 31; ++var0) {
         gNeedEscaping[var0] = true;
         gAfterEscaping1[var0] = gHexChs[var0 >> 4];
         gAfterEscaping2[var0] = gHexChs[var0 & 15];
      }

      gNeedEscaping[127] = true;
      gAfterEscaping1[127] = '7';
      gAfterEscaping2[127] = 'F';
      char[] var1 = new char[]{' ', '<', '>', '#', '%', '"', '{', '}', '|', '\\', '^', '~', '[', ']', '`'};
      int var2 = var1.length;

      for(int var4 = 0; var4 < var2; ++var4) {
         char var3 = var1[var4];
         gNeedEscaping[var3] = true;
         gAfterEscaping1[var3] = gHexChs[var3 >> 4];
         gAfterEscaping2[var3] = gHexChs[var3 & 15];
      }

      GET_USER_DIR_SYSTEM_PROPERTY = new PrivilegedAction() {
         public Object run() {
            return System.getProperty("user.dir");
         }
      };
   }

   protected final class RewindableInputStream extends InputStream {
      private InputStream fInputStream;
      private byte[] fData = new byte[64];
      private int fStartOffset;
      private int fEndOffset;
      private int fOffset;
      private int fLength;
      private int fMark;

      public RewindableInputStream(InputStream var2) {
         this.fInputStream = var2;
         this.fStartOffset = 0;
         this.fEndOffset = -1;
         this.fOffset = 0;
         this.fLength = 0;
         this.fMark = 0;
      }

      public void setStartOffset(int var1) {
         this.fStartOffset = var1;
      }

      public void rewind() {
         this.fOffset = this.fStartOffset;
      }

      public int readAndBuffer() throws IOException {
         if (this.fOffset == this.fData.length) {
            byte[] var1 = new byte[this.fOffset << 1];
            System.arraycopy(this.fData, 0, var1, 0, this.fOffset);
            this.fData = var1;
         }

         int var2 = this.fInputStream.read();
         if (var2 == -1) {
            this.fEndOffset = this.fOffset;
            return -1;
         } else {
            this.fData[this.fLength++] = (byte)var2;
            ++this.fOffset;
            return var2 & 255;
         }
      }

      public int read() throws IOException {
         if (this.fOffset < this.fLength) {
            return this.fData[this.fOffset++] & 255;
         } else if (this.fOffset == this.fEndOffset) {
            return -1;
         } else {
            return XMLEntityManager.this.fCurrentEntity.mayReadChunks ? this.fInputStream.read() : this.readAndBuffer();
         }
      }

      public int read(byte[] var1, int var2, int var3) throws IOException {
         int var4 = this.fLength - this.fOffset;
         if (var4 == 0) {
            if (this.fOffset == this.fEndOffset) {
               return -1;
            } else if (XMLEntityManager.this.fCurrentEntity.mayReadChunks) {
               return this.fInputStream.read(var1, var2, var3);
            } else {
               int var5 = this.readAndBuffer();
               if (var5 == -1) {
                  this.fEndOffset = this.fOffset;
                  return -1;
               } else {
                  var1[var2] = (byte)var5;
                  return 1;
               }
            }
         } else {
            if (var3 < var4) {
               if (var3 <= 0) {
                  return 0;
               }
            } else {
               var3 = var4;
            }

            if (var1 != null) {
               System.arraycopy(this.fData, this.fOffset, var1, var2, var3);
            }

            this.fOffset += var3;
            return var3;
         }
      }

      public long skip(long var1) throws IOException {
         if (var1 <= 0L) {
            return 0L;
         } else {
            int var3 = this.fLength - this.fOffset;
            if (var3 == 0) {
               return this.fOffset == this.fEndOffset ? 0L : this.fInputStream.skip(var1);
            } else if (var1 <= (long)var3) {
               this.fOffset = (int)((long)this.fOffset + var1);
               return var1;
            } else {
               this.fOffset += var3;
               if (this.fOffset == this.fEndOffset) {
                  return (long)var3;
               } else {
                  var1 -= (long)var3;
                  return this.fInputStream.skip(var1) + (long)var3;
               }
            }
         }
      }

      public int available() throws IOException {
         int var1 = this.fLength - this.fOffset;
         if (var1 == 0) {
            if (this.fOffset == this.fEndOffset) {
               return -1;
            } else {
               return XMLEntityManager.this.fCurrentEntity.mayReadChunks ? this.fInputStream.available() : 0;
            }
         } else {
            return var1;
         }
      }

      public void mark(int var1) {
         this.fMark = this.fOffset;
      }

      public void reset() {
         this.fOffset = this.fMark;
      }

      public boolean markSupported() {
         return true;
      }

      public void close() throws IOException {
         if (this.fInputStream != null) {
            this.fInputStream.close();
            this.fInputStream = null;
         }

      }
   }

   private static final class CharacterBufferPool {
      private static final int DEFAULT_POOL_SIZE = 3;
      private CharacterBuffer[] fInternalBufferPool;
      private CharacterBuffer[] fExternalBufferPool;
      private int fExternalBufferSize;
      private int fInternalBufferSize;
      private int fPoolSize;
      private int fInternalTop;
      private int fExternalTop;

      public CharacterBufferPool(int var1, int var2) {
         this(3, var1, var2);
      }

      public CharacterBufferPool(int var1, int var2, int var3) {
         this.fExternalBufferSize = var2;
         this.fInternalBufferSize = var3;
         this.fPoolSize = var1;
         this.init();
      }

      private void init() {
         this.fInternalBufferPool = new CharacterBuffer[this.fPoolSize];
         this.fExternalBufferPool = new CharacterBuffer[this.fPoolSize];
         this.fInternalTop = -1;
         this.fExternalTop = -1;
      }

      public CharacterBuffer getBuffer(boolean var1) {
         if (var1) {
            return this.fExternalTop > -1 ? this.fExternalBufferPool[this.fExternalTop--] : new CharacterBuffer(true, this.fExternalBufferSize);
         } else {
            return this.fInternalTop > -1 ? this.fInternalBufferPool[this.fInternalTop--] : new CharacterBuffer(false, this.fInternalBufferSize);
         }
      }

      public void returnBuffer(CharacterBuffer var1) {
         if (var1.isExternal) {
            if (this.fExternalTop < this.fExternalBufferPool.length - 1) {
               this.fExternalBufferPool[++this.fExternalTop] = var1;
            }
         } else if (this.fInternalTop < this.fInternalBufferPool.length - 1) {
            this.fInternalBufferPool[++this.fInternalTop] = var1;
         }

      }

      public void setExternalBufferSize(int var1) {
         this.fExternalBufferSize = var1;
         this.fExternalBufferPool = new CharacterBuffer[this.fPoolSize];
         this.fExternalTop = -1;
      }
   }

   private static final class CharacterBuffer {
      private final char[] ch;
      private final boolean isExternal;

      public CharacterBuffer(boolean var1, int var2) {
         this.isExternal = var1;
         this.ch = new char[var2];
      }
   }

   private static final class ByteBufferPool {
      private static final int DEFAULT_POOL_SIZE = 3;
      private int fPoolSize;
      private int fBufferSize;
      private byte[][] fByteBufferPool;
      private int fDepth;

      public ByteBufferPool(int var1) {
         this(3, var1);
      }

      public ByteBufferPool(int var1, int var2) {
         this.fPoolSize = var1;
         this.fBufferSize = var2;
         this.fByteBufferPool = new byte[this.fPoolSize][];
         this.fDepth = 0;
      }

      public byte[] getBuffer() {
         return this.fDepth > 0 ? this.fByteBufferPool[--this.fDepth] : new byte[this.fBufferSize];
      }

      public void returnBuffer(byte[] var1) {
         if (this.fDepth < this.fByteBufferPool.length) {
            this.fByteBufferPool[this.fDepth++] = var1;
         }

      }

      public void setBufferSize(int var1) {
         this.fBufferSize = var1;
         this.fByteBufferPool = new byte[this.fPoolSize][];
         this.fDepth = 0;
      }
   }

   private static class EncodingInfo {
      public static final EncodingInfo UTF_8 = new EncodingInfo("UTF-8", (Boolean)null, false);
      public static final EncodingInfo UTF_8_WITH_BOM = new EncodingInfo("UTF-8", (Boolean)null, true);
      public static final EncodingInfo UTF_16_BIG_ENDIAN;
      public static final EncodingInfo UTF_16_BIG_ENDIAN_WITH_BOM;
      public static final EncodingInfo UTF_16_LITTLE_ENDIAN;
      public static final EncodingInfo UTF_16_LITTLE_ENDIAN_WITH_BOM;
      public static final EncodingInfo UCS_4_BIG_ENDIAN;
      public static final EncodingInfo UCS_4_LITTLE_ENDIAN;
      public static final EncodingInfo UCS_4_UNUSUAL_BYTE_ORDER;
      public static final EncodingInfo EBCDIC;
      public final String autoDetectedEncoding;
      public final String readerEncoding;
      public final Boolean isBigEndian;
      public final boolean hasBOM;

      private EncodingInfo(String var1, Boolean var2, boolean var3) {
         this(var1, var1, var2, var3);
      }

      private EncodingInfo(String var1, String var2, Boolean var3, boolean var4) {
         this.autoDetectedEncoding = var1;
         this.readerEncoding = var2;
         this.isBigEndian = var3;
         this.hasBOM = var4;
      }

      static {
         UTF_16_BIG_ENDIAN = new EncodingInfo("UTF-16BE", "UTF-16", Boolean.TRUE, false);
         UTF_16_BIG_ENDIAN_WITH_BOM = new EncodingInfo("UTF-16BE", "UTF-16", Boolean.TRUE, true);
         UTF_16_LITTLE_ENDIAN = new EncodingInfo("UTF-16LE", "UTF-16", Boolean.FALSE, false);
         UTF_16_LITTLE_ENDIAN_WITH_BOM = new EncodingInfo("UTF-16LE", "UTF-16", Boolean.FALSE, true);
         UCS_4_BIG_ENDIAN = new EncodingInfo("ISO-10646-UCS-4", Boolean.TRUE, false);
         UCS_4_LITTLE_ENDIAN = new EncodingInfo("ISO-10646-UCS-4", Boolean.FALSE, false);
         UCS_4_UNUSUAL_BYTE_ORDER = new EncodingInfo("ISO-10646-UCS-4", (Boolean)null, false);
         EBCDIC = new EncodingInfo("CP037", (Boolean)null, false);
      }
   }

   public class ScannedEntity extends Entity {
      public InputStream stream;
      public Reader reader;
      public XMLResourceIdentifier entityLocation;
      public int lineNumber = 1;
      public int columnNumber = 1;
      public String encoding;
      boolean externallySpecifiedEncoding = false;
      public String xmlVersion = "1.0";
      public boolean literal;
      public boolean isExternal;
      public char[] ch = null;
      public int position;
      public int baseCharOffset;
      public int startPosition;
      public int count;
      public boolean mayReadChunks;
      private CharacterBuffer fCharacterBuffer;
      private byte[] fByteBuffer;

      public ScannedEntity(String var2, XMLResourceIdentifier var3, InputStream var4, Reader var5, byte[] var6, String var7, boolean var8, boolean var9, boolean var10) {
         super(var2, XMLEntityManager.this.fInExternalSubset);
         this.entityLocation = var3;
         this.stream = var4;
         this.reader = var5;
         this.encoding = var7;
         this.literal = var8;
         this.mayReadChunks = var9;
         this.isExternal = var10;
         this.fCharacterBuffer = XMLEntityManager.this.fCharacterBufferPool.getBuffer(var10);
         this.ch = this.fCharacterBuffer.ch;
         this.fByteBuffer = var6;
      }

      public final boolean isExternal() {
         return this.isExternal;
      }

      public final boolean isUnparsed() {
         return false;
      }

      public void setReader(InputStream var1, String var2, Boolean var3) throws IOException {
         XMLEntityManager.this.fTempByteBuffer = this.fByteBuffer;
         this.reader = XMLEntityManager.this.createReader(var1, var2, var3);
         this.fByteBuffer = XMLEntityManager.this.fTempByteBuffer;
      }

      public String getExpandedSystemId() {
         int var1 = XMLEntityManager.this.fEntityStack.size();

         for(int var2 = var1 - 1; var2 >= 0; --var2) {
            ScannedEntity var3 = (ScannedEntity)XMLEntityManager.this.fEntityStack.elementAt(var2);
            if (var3.entityLocation != null && var3.entityLocation.getExpandedSystemId() != null) {
               return var3.entityLocation.getExpandedSystemId();
            }
         }

         return null;
      }

      public String getLiteralSystemId() {
         int var1 = XMLEntityManager.this.fEntityStack.size();

         for(int var2 = var1 - 1; var2 >= 0; --var2) {
            ScannedEntity var3 = (ScannedEntity)XMLEntityManager.this.fEntityStack.elementAt(var2);
            if (var3.entityLocation != null && var3.entityLocation.getLiteralSystemId() != null) {
               return var3.entityLocation.getLiteralSystemId();
            }
         }

         return null;
      }

      public int getLineNumber() {
         int var1 = XMLEntityManager.this.fEntityStack.size();

         for(int var2 = var1 - 1; var2 >= 0; --var2) {
            ScannedEntity var3 = (ScannedEntity)XMLEntityManager.this.fEntityStack.elementAt(var2);
            if (var3.isExternal()) {
               return var3.lineNumber;
            }
         }

         return -1;
      }

      public int getColumnNumber() {
         int var1 = XMLEntityManager.this.fEntityStack.size();

         for(int var2 = var1 - 1; var2 >= 0; --var2) {
            ScannedEntity var3 = (ScannedEntity)XMLEntityManager.this.fEntityStack.elementAt(var2);
            if (var3.isExternal()) {
               return var3.columnNumber;
            }
         }

         return -1;
      }

      public int getCharacterOffset() {
         int var1 = XMLEntityManager.this.fEntityStack.size();

         for(int var2 = var1 - 1; var2 >= 0; --var2) {
            ScannedEntity var3 = (ScannedEntity)XMLEntityManager.this.fEntityStack.elementAt(var2);
            if (var3.isExternal()) {
               return var3.baseCharOffset + (var3.position - var3.startPosition);
            }
         }

         return -1;
      }

      public String getEncoding() {
         int var1 = XMLEntityManager.this.fEntityStack.size();

         for(int var2 = var1 - 1; var2 >= 0; --var2) {
            ScannedEntity var3 = (ScannedEntity)XMLEntityManager.this.fEntityStack.elementAt(var2);
            if (var3.isExternal()) {
               return var3.encoding;
            }
         }

         return null;
      }

      public String getXMLVersion() {
         int var1 = XMLEntityManager.this.fEntityStack.size();

         for(int var2 = var1 - 1; var2 >= 0; --var2) {
            ScannedEntity var3 = (ScannedEntity)XMLEntityManager.this.fEntityStack.elementAt(var2);
            if (var3.isExternal()) {
               return var3.xmlVersion;
            }
         }

         return null;
      }

      public boolean isEncodingExternallySpecified() {
         return this.externallySpecifiedEncoding;
      }

      public void setEncodingExternallySpecified(boolean var1) {
         this.externallySpecifiedEncoding = var1;
      }

      public String toString() {
         StringBuffer var1 = new StringBuffer();
         var1.append("name=\"").append(this.name).append('"');
         var1.append(",ch=");
         var1.append(this.ch);
         var1.append(",position=").append(this.position);
         var1.append(",count=").append(this.count);
         var1.append(",baseCharOffset=").append(this.baseCharOffset);
         var1.append(",startPosition=").append(this.startPosition);
         return var1.toString();
      }
   }

   protected static class ExternalEntity extends Entity {
      public XMLResourceIdentifier entityLocation;
      public String notation;

      public ExternalEntity() {
         this.clear();
      }

      public ExternalEntity(String var1, XMLResourceIdentifier var2, String var3, boolean var4) {
         super(var1, var4);
         this.entityLocation = var2;
         this.notation = var3;
      }

      public final boolean isExternal() {
         return true;
      }

      public final boolean isUnparsed() {
         return this.notation != null;
      }

      public void clear() {
         super.clear();
         this.entityLocation = null;
         this.notation = null;
      }

      public void setValues(Entity var1) {
         super.setValues(var1);
         this.entityLocation = null;
         this.notation = null;
      }

      public void setValues(ExternalEntity var1) {
         super.setValues(var1);
         this.entityLocation = var1.entityLocation;
         this.notation = var1.notation;
      }
   }

   protected static class InternalEntity extends Entity {
      public String text;
      public int paramEntityRefs;

      public InternalEntity() {
         this.clear();
      }

      public InternalEntity(String var1, String var2, boolean var3) {
         super(var1, var3);
         this.text = var2;
      }

      public InternalEntity(String var1, String var2, boolean var3, int var4) {
         this(var1, var2, var3);
         this.paramEntityRefs = var4;
      }

      public final boolean isExternal() {
         return false;
      }

      public final boolean isUnparsed() {
         return false;
      }

      public void clear() {
         super.clear();
         this.text = null;
      }

      public void setValues(Entity var1) {
         super.setValues(var1);
         this.text = null;
      }

      public void setValues(InternalEntity var1) {
         super.setValues(var1);
         this.text = var1.text;
      }
   }

   public abstract static class Entity {
      public String name;
      public boolean inExternalSubset;

      public Entity() {
         this.clear();
      }

      public Entity(String var1, boolean var2) {
         this.name = var1;
         this.inExternalSubset = var2;
      }

      public boolean isEntityDeclInExternalSubset() {
         return this.inExternalSubset;
      }

      public abstract boolean isExternal();

      public abstract boolean isUnparsed();

      public void clear() {
         this.name = null;
         this.inExternalSubset = false;
      }

      public void setValues(Entity var1) {
         this.name = var1.name;
         this.inExternalSubset = var1.inExternalSubset;
      }
   }
}
