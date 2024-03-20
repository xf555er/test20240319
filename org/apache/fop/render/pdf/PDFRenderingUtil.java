package org.apache.fop.render.pdf;

import java.awt.color.ICC_Profile;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.fo.extensions.xmp.XMPMetadata;
import org.apache.fop.pdf.PDFAMode;
import org.apache.fop.pdf.PDFArray;
import org.apache.fop.pdf.PDFConformanceException;
import org.apache.fop.pdf.PDFDeviceColorSpace;
import org.apache.fop.pdf.PDFDictionary;
import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.pdf.PDFEmbeddedFile;
import org.apache.fop.pdf.PDFEmbeddedFiles;
import org.apache.fop.pdf.PDFEncryptionManager;
import org.apache.fop.pdf.PDFEncryptionParams;
import org.apache.fop.pdf.PDFFileSpec;
import org.apache.fop.pdf.PDFICCBasedColorSpace;
import org.apache.fop.pdf.PDFICCStream;
import org.apache.fop.pdf.PDFInfo;
import org.apache.fop.pdf.PDFLayer;
import org.apache.fop.pdf.PDFMetadata;
import org.apache.fop.pdf.PDFName;
import org.apache.fop.pdf.PDFNames;
import org.apache.fop.pdf.PDFNavigator;
import org.apache.fop.pdf.PDFNull;
import org.apache.fop.pdf.PDFNumber;
import org.apache.fop.pdf.PDFOutputIntent;
import org.apache.fop.pdf.PDFPage;
import org.apache.fop.pdf.PDFPageLabels;
import org.apache.fop.pdf.PDFReference;
import org.apache.fop.pdf.PDFSetOCGStateAction;
import org.apache.fop.pdf.PDFTransitionAction;
import org.apache.fop.pdf.PDFXMode;
import org.apache.fop.pdf.Version;
import org.apache.fop.pdf.VersionController;
import org.apache.fop.render.pdf.extensions.PDFArrayExtension;
import org.apache.fop.render.pdf.extensions.PDFCollectionEntryExtension;
import org.apache.fop.render.pdf.extensions.PDFDictionaryAttachment;
import org.apache.fop.render.pdf.extensions.PDFDictionaryExtension;
import org.apache.fop.render.pdf.extensions.PDFDictionaryType;
import org.apache.fop.render.pdf.extensions.PDFEmbeddedFileAttachment;
import org.apache.fop.render.pdf.extensions.PDFObjectType;
import org.apache.fop.render.pdf.extensions.PDFPageExtension;
import org.apache.fop.render.pdf.extensions.PDFReferenceExtension;
import org.apache.xmlgraphics.java2d.color.profile.ColorProfileUtil;
import org.apache.xmlgraphics.util.DateFormatUtil;
import org.apache.xmlgraphics.xmp.Metadata;
import org.apache.xmlgraphics.xmp.schemas.DublinCoreSchema;
import org.apache.xmlgraphics.xmp.schemas.XMPBasicAdapter;
import org.apache.xmlgraphics.xmp.schemas.XMPBasicSchema;

class PDFRenderingUtil {
   private static Log log = LogFactory.getLog(PDFRenderingUtil.class);
   private FOUserAgent userAgent;
   private PDFDocument pdfDoc;
   private PDFRendererOptionsConfig rendererConfig;
   private PDFICCStream outputProfile;
   private PDFICCBasedColorSpace sRGBColorSpace;

   PDFRenderingUtil(FOUserAgent userAgent) {
      this.userAgent = userAgent;
      this.initialize();
   }

   private void initialize() {
      this.rendererConfig = PDFRendererOptionsConfig.DEFAULT.merge(createFromUserAgent(this.userAgent));
      if (this.rendererConfig.getPDFAMode().isLevelA()) {
         this.userAgent.getRendererOptions().put("accessibility", Boolean.TRUE);
      }

   }

   private static PDFRendererOptionsConfig createFromUserAgent(FOUserAgent userAgent) {
      Map properties = new EnumMap(PDFRendererOption.class);
      PDFRendererOption[] var2 = PDFRendererOption.values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         PDFRendererOption option = var2[var4];
         Object value = userAgent.getRendererOption(option);
         properties.put(option, option.parse(value));
      }

      PDFEncryptionParams encryptionConfig = (new EncryptionParamsBuilder()).createParams(userAgent);
      return new PDFRendererOptionsConfig(properties, encryptionConfig);
   }

   void mergeRendererOptionsConfig(PDFRendererOptionsConfig config) {
      this.rendererConfig = this.rendererConfig.merge(config);
   }

   private void updateInfo() {
      PDFInfo info = this.pdfDoc.getInfo();
      info.setCreator(this.userAgent.getCreator());
      info.setCreationDate(this.userAgent.getCreationDate());
      info.setAuthor(this.userAgent.getAuthor());
      info.setTitle(this.userAgent.getTitle());
      info.setSubject(this.userAgent.getSubject());
      info.setKeywords(this.userAgent.getKeywords());
   }

   private void updatePDFProfiles() {
      this.pdfDoc.getProfile().setPDFAMode(this.rendererConfig.getPDFAMode());
      this.pdfDoc.getProfile().setPDFUAMode(this.rendererConfig.getPDFUAMode());
      this.userAgent.setPdfUAEnabled(this.pdfDoc.getProfile().getPDFUAMode().isEnabled());
      this.pdfDoc.getProfile().setPDFXMode(this.rendererConfig.getPDFXMode());
      this.pdfDoc.getProfile().setPDFVTMode(this.rendererConfig.getPDFVTMode());
   }

   private void addsRGBColorSpace() throws IOException {
      if (this.rendererConfig.getDisableSRGBColorSpace()) {
         if (this.rendererConfig.getPDFAMode() != PDFAMode.DISABLED || this.rendererConfig.getPDFXMode() != PDFXMode.DISABLED || this.rendererConfig.getOutputProfileURI() != null) {
            throw new IllegalStateException("It is not possible to disable the sRGB color space if PDF/A or PDF/X functionality is enabled or an output profile is set!");
         }
      } else {
         if (this.sRGBColorSpace != null) {
            return;
         }

         this.sRGBColorSpace = PDFICCBasedColorSpace.setupsRGBAsDefaultRGBColorSpace(this.pdfDoc);
      }

   }

   private void addDefaultOutputProfile() throws IOException {
      if (this.outputProfile == null) {
         InputStream in = null;
         URI outputProfileUri = this.rendererConfig.getOutputProfileURI();
         if (outputProfileUri != null) {
            this.outputProfile = this.pdfDoc.getFactory().makePDFICCStream();
            in = this.userAgent.getResourceResolver().getResource(this.rendererConfig.getOutputProfileURI());

            ICC_Profile profile;
            try {
               profile = ColorProfileUtil.getICC_Profile((InputStream)in);
            } finally {
               IOUtils.closeQuietly((InputStream)in);
            }

            this.outputProfile.setColorSpace(profile, (PDFDeviceColorSpace)null);
         } else {
            this.outputProfile = this.sRGBColorSpace.getICCStream();
         }

      }
   }

   private void addPDFA1OutputIntent() throws IOException {
      this.addDefaultOutputProfile();
      String desc = ColorProfileUtil.getICCProfileDescription(this.outputProfile.getICCProfile());
      PDFOutputIntent outputIntent = this.pdfDoc.getFactory().makeOutputIntent();
      outputIntent.setSubtype("GTS_PDFA1");
      outputIntent.setDestOutputProfile(this.outputProfile);
      outputIntent.setOutputConditionIdentifier(desc);
      outputIntent.setInfo(outputIntent.getOutputConditionIdentifier());
      this.pdfDoc.getRoot().addOutputIntent(outputIntent);
   }

   private void addPDFXOutputIntent() throws IOException {
      this.addDefaultOutputProfile();
      String desc = ColorProfileUtil.getICCProfileDescription(this.outputProfile.getICCProfile());
      int deviceClass = this.outputProfile.getICCProfile().getProfileClass();
      if (deviceClass != 2) {
         throw new PDFConformanceException(this.pdfDoc.getProfile().getPDFXMode() + " requires that the DestOutputProfile be an Output Device Profile. " + desc + " does not match that requirement.");
      } else {
         PDFOutputIntent outputIntent = this.pdfDoc.getFactory().makeOutputIntent();
         outputIntent.setSubtype("GTS_PDFX");
         outputIntent.setDestOutputProfile(this.outputProfile);
         outputIntent.setOutputConditionIdentifier(desc);
         outputIntent.setInfo(outputIntent.getOutputConditionIdentifier());
         this.pdfDoc.getRoot().addOutputIntent(outputIntent);
      }
   }

   public void renderXMPMetadata(XMPMetadata metadata) {
      Metadata docXMP = metadata.getMetadata();
      Metadata fopXMP = PDFMetadata.createXMPFromPDFDocument(this.pdfDoc);
      List exclude = new ArrayList();
      if (this.pdfDoc.getProfile().getPDFAMode().isPart1()) {
         exclude.add(DublinCoreSchema.class);
      }

      fopXMP.mergeInto(docXMP, exclude);
      XMPBasicAdapter xmpBasic = XMPBasicSchema.getAdapter(docXMP);
      xmpBasic.setMetadataDate(new Date());
      PDFMetadata.updateInfoFromMetadata(docXMP, this.pdfDoc.getInfo());
      PDFMetadata pdfMetadata = this.pdfDoc.getFactory().makeMetadata(docXMP, metadata.isReadOnly());
      this.pdfDoc.getRoot().setMetadata(pdfMetadata);
   }

   public void generateDefaultXMPMetadata() {
      if (this.pdfDoc.getRoot().getMetadata() == null) {
         Metadata xmp = PDFMetadata.createXMPFromPDFDocument(this.pdfDoc);
         PDFMetadata pdfMetadata = this.pdfDoc.getFactory().makeMetadata(xmp, true);
         this.pdfDoc.getRoot().setMetadata(pdfMetadata);
      }

   }

   public void renderDictionaryExtension(PDFDictionaryAttachment attachment, PDFPage currentPage) {
      PDFDictionaryExtension extension = attachment.getExtension();
      PDFDictionaryType type = extension.getDictionaryType();
      if (type == PDFDictionaryType.Action) {
         this.addNavigatorAction(extension);
      } else if (type == PDFDictionaryType.Layer) {
         this.addLayer(extension);
      } else if (type == PDFDictionaryType.Navigator) {
         this.addNavigator(extension);
      } else {
         this.renderDictionaryExtension(extension, currentPage);
      }

   }

   public void addLayer(PDFDictionaryExtension extension) {
      assert extension.getDictionaryType() == PDFDictionaryType.Layer;

      String id = extension.getProperty("id");
      if (id != null && id.length() > 0) {
         PDFLayer layer = this.pdfDoc.getFactory().makeLayer(id);
         layer.setResolver(new PDFLayer.Resolver(layer, extension) {
            public void performResolution() {
               PDFDictionaryExtension extension = (PDFDictionaryExtension)this.getExtension();
               Object name = extension.findEntryValue("Name");
               Object intent = extension.findEntryValue("Intent");
               Object usage = PDFRenderingUtil.this.makeDictionary(extension.findEntryValue("Usage"));
               this.getLayer().populate(name, intent, usage);
            }
         });
      }

   }

   public void addNavigatorAction(PDFDictionaryExtension extension) {
      assert extension.getDictionaryType() == PDFDictionaryType.Action;

      String id = extension.getProperty("id");
      if (id != null && id.length() > 0) {
         String type = extension.getProperty("type");
         if (type != null) {
            if (type.equals("SetOCGState")) {
               PDFSetOCGStateAction action = this.pdfDoc.getFactory().makeSetOCGStateAction(id);
               action.setResolver(new PDFSetOCGStateAction.Resolver(action, extension) {
                  public void performResolution() {
                     PDFDictionaryExtension extension = (PDFDictionaryExtension)this.getExtension();
                     Object state = PDFRenderingUtil.this.makeArray(extension.findEntryValue("State"));
                     Object preserveRB = extension.findEntryValue("PreserveRB");
                     Object nextAction = PDFRenderingUtil.this.makeDictionaryOrArray(extension.findEntryValue("Next"));
                     this.getAction().populate(state, preserveRB, nextAction);
                  }
               });
            } else {
               if (!type.equals("Trans")) {
                  throw new UnsupportedOperationException();
               }

               PDFTransitionAction action = this.pdfDoc.getFactory().makeTransitionAction(id);
               action.setResolver(new PDFTransitionAction.Resolver(action, extension) {
                  public void performResolution() {
                     PDFDictionaryExtension extension = (PDFDictionaryExtension)this.getExtension();
                     Object transition = PDFRenderingUtil.this.makeDictionary(extension.findEntryValue("Trans"));
                     Object nextAction = PDFRenderingUtil.this.makeDictionaryOrArray(extension.findEntryValue("Next"));
                     this.getAction().populate(transition, nextAction);
                  }
               });
            }
         }
      }

   }

   public void addNavigator(PDFDictionaryExtension extension) {
      assert extension.getDictionaryType() == PDFDictionaryType.Navigator;

      String id = extension.getProperty("id");
      if (id != null && id.length() > 0) {
         PDFNavigator navigator = this.pdfDoc.getFactory().makeNavigator(id);
         navigator.setResolver(new PDFNavigator.Resolver(navigator, extension) {
            public void performResolution() {
               PDFDictionaryExtension extension = (PDFDictionaryExtension)this.getExtension();
               Object nextAction = PDFRenderingUtil.this.makeDictionary(extension.findEntryValue("NA"));
               Object next = PDFRenderingUtil.this.makeDictionary(extension.findEntryValue("Next"));
               Object prevAction = PDFRenderingUtil.this.makeDictionary(extension.findEntryValue("PA"));
               Object prev = PDFRenderingUtil.this.makeDictionary(extension.findEntryValue("Prev"));
               Object duration = extension.findEntryValue("Dur");
               this.getNavigator().populate(nextAction, next, prevAction, prev, duration);
            }
         });
      }

   }

   private Object makeArray(Object value) {
      if (value == null) {
         return null;
      } else if (value instanceof PDFReferenceExtension) {
         return this.resolveReference((PDFReferenceExtension)value);
      } else if (value instanceof List) {
         return this.populateArray(new PDFArray(), (List)value);
      } else {
         throw new IllegalArgumentException();
      }
   }

   private Object populateArray(PDFArray array, List entries) {
      Iterator var3 = entries.iterator();

      while(var3.hasNext()) {
         PDFCollectionEntryExtension entry = (PDFCollectionEntryExtension)var3.next();
         PDFObjectType type = entry.getType();
         if (type == PDFObjectType.Array) {
            array.add(this.makeArray(entry.getValue()));
         } else if (type == PDFObjectType.Boolean) {
            array.add(entry.getValueAsBoolean());
         } else if (type == PDFObjectType.Dictionary) {
            array.add(this.makeDictionary(entry.getValue()));
         } else if (type == PDFObjectType.Name) {
            array.add(new PDFName(entry.getValueAsString()));
         } else if (type == PDFObjectType.Number) {
            array.add(new PDFNumber(entry.getValueAsNumber()));
         } else if (type == PDFObjectType.Reference) {
            assert entry instanceof PDFReferenceExtension;

            array.add(this.resolveReference((PDFReferenceExtension)entry));
         } else if (type == PDFObjectType.String) {
            array.add(entry.getValue());
         }
      }

      return array;
   }

   private Object makeDictionary(Object value) {
      if (value == null) {
         return null;
      } else if (value instanceof PDFReferenceExtension) {
         return this.resolveReference((PDFReferenceExtension)value);
      } else if (value instanceof List) {
         return this.populateDictionary(new PDFDictionary(), (List)value);
      } else {
         throw new IllegalArgumentException();
      }
   }

   private Object populateDictionary(PDFDictionary dictionary, List entries) {
      Iterator var3 = entries.iterator();

      while(var3.hasNext()) {
         PDFCollectionEntryExtension entry = (PDFCollectionEntryExtension)var3.next();
         PDFObjectType type = entry.getType();
         String key = entry.getKey();
         if (type == PDFObjectType.Array) {
            dictionary.put(key, this.makeArray(entry.getValue()));
         } else if (type == PDFObjectType.Boolean) {
            dictionary.put(key, entry.getValueAsBoolean());
         } else if (type == PDFObjectType.Dictionary) {
            dictionary.put(key, this.makeDictionary(entry.getValue()));
         } else if (type == PDFObjectType.Name) {
            dictionary.put(key, new PDFName(entry.getValueAsString()));
         } else if (type == PDFObjectType.Number) {
            dictionary.put(key, new PDFNumber(entry.getValueAsNumber()));
         } else if (type == PDFObjectType.Reference) {
            assert entry instanceof PDFReferenceExtension;

            dictionary.put(key, this.resolveReference((PDFReferenceExtension)entry));
         } else if (type == PDFObjectType.String) {
            dictionary.put(key, entry.getValue());
         }
      }

      return dictionary;
   }

   private Object makeDictionaryOrArray(Object value) {
      if (value == null) {
         return null;
      } else if (value instanceof PDFReferenceExtension) {
         return this.resolveReference((PDFReferenceExtension)value);
      } else if (value instanceof List) {
         return this.hasKeyedEntry((List)value) ? this.populateDictionary(new PDFDictionary(), (List)value) : this.populateArray(new PDFArray(), (List)value);
      } else {
         throw new IllegalArgumentException();
      }
   }

   private boolean hasKeyedEntry(List entries) {
      Iterator var2 = entries.iterator();

      PDFCollectionEntryExtension entry;
      do {
         if (!var2.hasNext()) {
            return false;
         }

         entry = (PDFCollectionEntryExtension)var2.next();
      } while(entry.getKey() == null);

      return true;
   }

   public void renderDictionaryExtension(PDFDictionaryExtension extension, PDFPage currentPage) {
      PDFDictionaryType type = extension.getDictionaryType();
      if (type == PDFDictionaryType.Catalog) {
         this.augmentDictionary(this.pdfDoc.getRoot(), (PDFDictionaryExtension)extension);
      } else if (type == PDFDictionaryType.Page) {
         assert extension instanceof PDFPageExtension;

         if (((PDFPageExtension)extension).matchesPageNumber(currentPage.getPageIndex() + 1)) {
            this.augmentDictionary(currentPage, (PDFDictionaryExtension)extension);
         }
      } else if (type == PDFDictionaryType.Info) {
         PDFInfo info = this.pdfDoc.getInfo();
         Iterator var5 = extension.getEntries().iterator();

         while(var5.hasNext()) {
            PDFCollectionEntryExtension entry = (PDFCollectionEntryExtension)var5.next();
            info.put(entry.getKey(), entry.getValueAsString());
         }
      } else if (type == PDFDictionaryType.VT) {
         if (currentPage.get("DPart") != null) {
            this.augmentDictionary((PDFDictionary)currentPage.get("DPart"), extension);
         }
      } else {
         if (type != PDFDictionaryType.PagePiece) {
            throw new IllegalStateException();
         }

         String date = DateFormatUtil.formatPDFDate(new Date(), TimeZone.getDefault());
         if (currentPage.get("PieceInfo") == null) {
            currentPage.put("PieceInfo", new PDFDictionary());
            currentPage.put("LastModified", date);
         }

         PDFDictionary d = this.augmentDictionary((PDFDictionary)currentPage.get("PieceInfo"), extension);
         d.put("LastModified", date);
      }

   }

   private PDFDictionary augmentDictionary(PDFDictionary dictionary, PDFDictionaryExtension extension) {
      Iterator var3 = extension.getEntries().iterator();

      while(true) {
         while(var3.hasNext()) {
            PDFCollectionEntryExtension entry = (PDFCollectionEntryExtension)var3.next();
            if (entry instanceof PDFDictionaryExtension) {
               String[] keys = entry.getKey().split("/");

               for(int i = 0; i < keys.length; ++i) {
                  if (keys[i].isEmpty()) {
                     throw new IllegalStateException("pdf:dictionary key: " + entry.getKey() + " not valid");
                  }

                  if (i == keys.length - 1) {
                     dictionary.put(keys[i], this.augmentDictionary(new PDFDictionary(dictionary), (PDFDictionaryExtension)entry));
                  } else {
                     PDFDictionary d = new PDFDictionary();
                     dictionary.put(keys[i], d);
                     dictionary = d;
                  }
               }
            } else if (entry instanceof PDFArrayExtension) {
               dictionary.put(entry.getKey(), this.augmentArray(new PDFArray(dictionary), (PDFArrayExtension)entry));
            } else {
               this.augmentDictionary(dictionary, entry);
            }
         }

         return dictionary;
      }
   }

   private void augmentDictionary(PDFDictionary dictionary, PDFCollectionEntryExtension entry) {
      PDFObjectType type = entry.getType();
      String key = entry.getKey();
      if (type == PDFObjectType.Boolean) {
         dictionary.put(key, entry.getValueAsBoolean());
      } else if (type == PDFObjectType.Name) {
         dictionary.put(key, new PDFName(entry.getValueAsString()));
      } else if (type == PDFObjectType.Number) {
         dictionary.put(key, new PDFNumber(entry.getValueAsNumber()));
      } else if (type == PDFObjectType.Reference) {
         assert entry instanceof PDFReferenceExtension;

         dictionary.put(key, this.resolveReference((PDFReferenceExtension)entry));
      } else {
         if (type != PDFObjectType.String) {
            throw new IllegalStateException();
         }

         dictionary.put(key, entry.getValueAsString());
      }

   }

   private Object resolveReference(PDFReferenceExtension entry) {
      PDFReference reference = (PDFReference)entry.getResolvedReference();
      if (reference == null) {
         reference = this.pdfDoc.resolveExtensionReference(entry.getReferenceId());
         if (reference != null) {
            entry.setResolvedReference(reference);
         }

         return reference;
      } else {
         return PDFNull.INSTANCE;
      }
   }

   private PDFArray augmentArray(PDFArray array, PDFArrayExtension extension) {
      Iterator var3 = extension.getEntries().iterator();

      while(var3.hasNext()) {
         PDFCollectionEntryExtension entry = (PDFCollectionEntryExtension)var3.next();
         if (entry instanceof PDFDictionaryExtension) {
            array.add(this.augmentDictionary(new PDFDictionary(array), (PDFDictionaryExtension)entry));
         } else if (entry instanceof PDFArrayExtension) {
            array.add(this.augmentArray(new PDFArray(array), (PDFArrayExtension)entry));
         } else {
            this.augmentArray(array, entry);
         }
      }

      return array;
   }

   private void augmentArray(PDFArray array, PDFCollectionEntryExtension entry) {
      PDFObjectType type = entry.getType();
      if (type == PDFObjectType.Boolean) {
         array.add(entry.getValueAsBoolean());
      } else if (type == PDFObjectType.Name) {
         array.add(new PDFName(entry.getValueAsString()));
      } else if (type == PDFObjectType.Number) {
         array.add(new PDFNumber(entry.getValueAsNumber()));
      } else if (type == PDFObjectType.Reference) {
         assert entry instanceof PDFReferenceExtension;

         array.add(this.resolveReference((PDFReferenceExtension)entry));
      } else {
         if (type != PDFObjectType.String) {
            throw new IllegalStateException();
         }

         array.add(entry.getValueAsString());
      }

   }

   public PDFDocument setupPDFDocument(OutputStream out) throws IOException {
      if (this.pdfDoc != null) {
         throw new IllegalStateException("PDFDocument already set up");
      } else {
         String producer = this.userAgent.getProducer() != null ? this.userAgent.getProducer() : "";
         Version maxPDFVersion = this.rendererConfig.getPDFVersion();
         if (maxPDFVersion == null) {
            this.pdfDoc = new PDFDocument(producer);
         } else {
            VersionController controller = VersionController.getFixedVersionController(maxPDFVersion);
            this.pdfDoc = new PDFDocument(producer, controller);
         }

         this.updateInfo();
         this.updatePDFProfiles();
         this.pdfDoc.setFilterMap(this.rendererConfig.getFilterMap());
         this.pdfDoc.outputHeader(out);
         PDFEncryptionManager.setupPDFEncryption(this.rendererConfig.getEncryptionParameters(), this.pdfDoc);
         this.addsRGBColorSpace();
         if (this.rendererConfig.getOutputProfileURI() != null) {
            this.addDefaultOutputProfile();
         }

         PDFXMode pdfXMode = this.rendererConfig.getPDFXMode();
         if (pdfXMode != PDFXMode.DISABLED) {
            log.debug(pdfXMode + " is active.");
            log.warn("Note: " + pdfXMode + " support is work-in-progress and not fully implemented, yet!");
            this.addPDFXOutputIntent();
         }

         PDFAMode pdfAMode = this.rendererConfig.getPDFAMode();
         if (pdfAMode.isEnabled()) {
            log.debug("PDF/A is active. Conformance Level: " + pdfAMode);
            this.addPDFA1OutputIntent();
         }

         this.pdfDoc.enableAccessibility(this.userAgent.isAccessibilityEnabled());
         this.pdfDoc.setMergeFontsEnabled(this.rendererConfig.getMergeFontsEnabled());
         this.pdfDoc.setLinearizationEnabled(this.rendererConfig.getLinearizationEnabled());
         this.pdfDoc.setFormXObjectEnabled(this.rendererConfig.getFormXObjectEnabled());
         return this.pdfDoc;
      }
   }

   public void generatePageLabel(int pageIndex, String pageNumber) {
      PDFPageLabels pageLabels = this.pdfDoc.getRoot().getPageLabels();
      if (pageLabels == null) {
         pageLabels = this.pdfDoc.getFactory().makePageLabels();
         this.pdfDoc.getRoot().setPageLabels(pageLabels);
      }

      pageLabels.addPageLabel(pageIndex, pageNumber);
   }

   public void addEmbeddedFile(PDFEmbeddedFileAttachment embeddedFile) throws IOException {
      this.pdfDoc.getProfile().verifyEmbeddedFilesAllowed();
      PDFNames names = this.pdfDoc.getRoot().getNames();
      if (names == null) {
         names = this.pdfDoc.getFactory().makeNames();
         this.pdfDoc.getRoot().setNames(names);
      }

      PDFEmbeddedFile file = new PDFEmbeddedFile();
      this.pdfDoc.registerObject(file);

      URI srcURI;
      try {
         srcURI = InternalResourceResolver.cleanURI(embeddedFile.getSrc());
      } catch (URISyntaxException var14) {
         throw new RuntimeException(var14);
      }

      InputStream in = this.userAgent.getResourceResolver().getResource(srcURI);
      if (in == null) {
         throw new FileNotFoundException(embeddedFile.getSrc());
      } else {
         try {
            OutputStream out = file.getBufferOutputStream();
            IOUtils.copyLarge((InputStream)in, (OutputStream)out);
         } finally {
            IOUtils.closeQuietly((InputStream)in);
         }

         PDFDictionary dict = new PDFDictionary();
         dict.put("F", file);
         PDFFileSpec fileSpec = new PDFFileSpec(embeddedFile.getFilename(), embeddedFile.getUnicodeFilename());
         String filename = fileSpec.getFilename();
         this.pdfDoc.getRoot().addAF(fileSpec);
         fileSpec.setEmbeddedFile(dict);
         if (embeddedFile.getDesc() != null) {
            fileSpec.setDescription(embeddedFile.getDesc());
         }

         this.pdfDoc.registerObject(fileSpec);
         PDFEmbeddedFiles embeddedFiles = names.getEmbeddedFiles();
         if (embeddedFiles == null) {
            embeddedFiles = new PDFEmbeddedFiles();
            this.pdfDoc.assignObjectNumber(embeddedFiles);
            this.pdfDoc.addTrailerObject(embeddedFiles);
            names.setEmbeddedFiles(embeddedFiles);
         }

         PDFArray nameArray = embeddedFiles.getNames();
         if (nameArray == null) {
            nameArray = new PDFArray();
            embeddedFiles.setNames(nameArray);
         }

         nameArray.add(filename);
         nameArray.add(new PDFReference(fileSpec));
      }
   }

   private static final class EncryptionParamsBuilder {
      private PDFEncryptionParams params;

      private EncryptionParamsBuilder() {
      }

      private PDFEncryptionParams createParams(FOUserAgent userAgent) {
         this.params = (PDFEncryptionParams)userAgent.getRendererOptions().get("encryption-params");
         String userPassword = (String)userAgent.getRendererOption(PDFEncryptionOption.USER_PASSWORD);
         if (userPassword != null) {
            this.getEncryptionParams().setUserPassword(userPassword);
         }

         String ownerPassword = (String)userAgent.getRendererOption(PDFEncryptionOption.OWNER_PASSWORD);
         if (ownerPassword != null) {
            this.getEncryptionParams().setOwnerPassword(ownerPassword);
         }

         Object noPrint = userAgent.getRendererOption(PDFEncryptionOption.NO_PRINT);
         if (noPrint != null) {
            this.getEncryptionParams().setAllowPrint(!booleanValueOf(noPrint));
         }

         Object noCopyContent = userAgent.getRendererOption(PDFEncryptionOption.NO_COPY_CONTENT);
         if (noCopyContent != null) {
            this.getEncryptionParams().setAllowCopyContent(!booleanValueOf(noCopyContent));
         }

         Object noEditContent = userAgent.getRendererOption(PDFEncryptionOption.NO_EDIT_CONTENT);
         if (noEditContent != null) {
            this.getEncryptionParams().setAllowEditContent(!booleanValueOf(noEditContent));
         }

         Object noAnnotations = userAgent.getRendererOption(PDFEncryptionOption.NO_ANNOTATIONS);
         if (noAnnotations != null) {
            this.getEncryptionParams().setAllowEditAnnotations(!booleanValueOf(noAnnotations));
         }

         Object noFillInForms = userAgent.getRendererOption(PDFEncryptionOption.NO_FILLINFORMS);
         if (noFillInForms != null) {
            this.getEncryptionParams().setAllowFillInForms(!booleanValueOf(noFillInForms));
         }

         Object noAccessContent = userAgent.getRendererOption(PDFEncryptionOption.NO_ACCESSCONTENT);
         if (noAccessContent != null) {
            this.getEncryptionParams().setAllowAccessContent(!booleanValueOf(noAccessContent));
         }

         Object noAssembleDoc = userAgent.getRendererOption(PDFEncryptionOption.NO_ASSEMBLEDOC);
         if (noAssembleDoc != null) {
            this.getEncryptionParams().setAllowAssembleDocument(!booleanValueOf(noAssembleDoc));
         }

         Object noPrintHQ = userAgent.getRendererOption(PDFEncryptionOption.NO_PRINTHQ);
         if (noPrintHQ != null) {
            this.getEncryptionParams().setAllowPrintHq(!booleanValueOf(noPrintHQ));
         }

         return this.params;
      }

      private PDFEncryptionParams getEncryptionParams() {
         if (this.params == null) {
            this.params = new PDFEncryptionParams();
         }

         return this.params;
      }

      private static boolean booleanValueOf(Object obj) {
         if (obj instanceof Boolean) {
            return (Boolean)obj;
         } else if (obj instanceof String) {
            return Boolean.valueOf((String)obj);
         } else {
            throw new IllegalArgumentException("Boolean or \"true\" or \"false\" expected.");
         }
      }

      // $FF: synthetic method
      EncryptionParamsBuilder(Object x0) {
         this();
      }
   }
}
