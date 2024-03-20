package org.apache.fop.pdf;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.pdf.xref.CrossReferenceStream;
import org.apache.fop.pdf.xref.CrossReferenceTable;
import org.apache.fop.pdf.xref.TrailerDictionary;

public class PDFDocument {
   public static final String ENCODING = "ISO-8859-1";
   protected int objectcount;
   private Log log;
   protected long position;
   protected List indirectObjectOffsets;
   protected List structureTreeElements;
   protected List trailerObjects;
   protected List objects;
   private VersionController versionController;
   private PDFProfile pdfProfile;
   private PDFRoot root;
   private PDFOutline outlineRoot;
   private PDFPages pages;
   private PDFInfo info;
   private PDFResources resources;
   private PDFEncryption encryption;
   private PDFDeviceColorSpace colorspace;
   private int patternCount;
   private int shadingCount;
   private int xObjectCount;
   protected int gStateObjectCount;
   private Map xObjectsMap;
   private Map fontMap;
   private Map filterMap;
   private List gstates;
   private List functions;
   private List shadings;
   private List patterns;
   private List links;
   private List destinations;
   private List filespecs;
   private List gotoremotes;
   private List gotos;
   private List launches;
   protected List pageObjs;
   private List layers;
   private List navigators;
   private List navigatorActions;
   private PDFFactory factory;
   private FileIDGenerator fileIDGenerator;
   private boolean accessibilityEnabled;
   private boolean mergeFontsEnabled;
   private boolean linearizationEnabled;
   private boolean formXObjectEnabled;
   protected boolean outputStarted;

   public PDFDocument(String prod) {
      this(prod, (VersionController)null);
      this.versionController = VersionController.getDynamicVersionController(Version.V1_4, this);
   }

   public PDFDocument(String prod, VersionController versionController) {
      this.log = LogFactory.getLog("org.apache.fop.pdf");
      this.indirectObjectOffsets = new ArrayList();
      this.trailerObjects = new ArrayList();
      this.objects = new LinkedList();
      this.pdfProfile = new PDFProfile(this);
      this.colorspace = new PDFDeviceColorSpace(2);
      this.xObjectsMap = new HashMap();
      this.fontMap = new HashMap();
      this.filterMap = new HashMap();
      this.gstates = new ArrayList();
      this.functions = new ArrayList();
      this.shadings = new ArrayList();
      this.patterns = new ArrayList();
      this.links = new ArrayList();
      this.filespecs = new ArrayList();
      this.gotoremotes = new ArrayList();
      this.gotos = new ArrayList();
      this.launches = new ArrayList();
      this.pageObjs = new ArrayList();
      this.factory = new PDFFactory(this);
      this.pages = this.getFactory().makePages();
      this.root = this.getFactory().makeRoot(this.pages);
      this.resources = this.getFactory().makeResources();
      this.info = this.getFactory().makeInfo(prod);
      this.versionController = versionController;
   }

   public Version getPDFVersion() {
      return this.versionController.getPDFVersion();
   }

   public void setPDFVersion(Version version) {
      this.versionController.setPDFVersion(version);
   }

   public String getPDFVersionString() {
      return this.versionController.getPDFVersion().toString();
   }

   public PDFProfile getProfile() {
      return this.pdfProfile;
   }

   public PDFFactory getFactory() {
      return this.factory;
   }

   public static byte[] encode(String text) {
      try {
         return text.getBytes("ISO-8859-1");
      } catch (UnsupportedEncodingException var2) {
         return text.getBytes();
      }
   }

   public static void flushTextBuffer(StringBuilder textBuffer, OutputStream out) throws IOException {
      out.write(encode(textBuffer.toString()));
      textBuffer.setLength(0);
   }

   public void setProducer(String producer) {
      this.info.setProducer(producer);
   }

   public void setCreationDate(Date date) {
      this.info.setCreationDate(date);
   }

   public void setCreator(String creator) {
      this.info.setCreator(creator);
   }

   public void setFilterMap(Map map) {
      this.filterMap = map;
   }

   public Map getFilterMap() {
      return this.filterMap;
   }

   public PDFPages getPages() {
      return this.pages;
   }

   public PDFRoot getRoot() {
      return this.root;
   }

   public List getStructureTreeElements() {
      return this.structureTreeElements;
   }

   public PDFStructTreeRoot makeStructTreeRoot(PDFParentTree parentTree) {
      PDFStructTreeRoot structTreeRoot = new PDFStructTreeRoot(parentTree);
      this.assignObjectNumber(structTreeRoot);
      this.addTrailerObject(structTreeRoot);
      this.root.setStructTreeRoot(structTreeRoot);
      this.structureTreeElements = new ArrayList();
      return structTreeRoot;
   }

   public void registerStructureElement(PDFStructElem structElem) {
      this.assignObjectNumber(structElem);
      this.structureTreeElements.add(structElem);
   }

   public void registerStructureElement(PDFStructElem structElem, StandardStructureAttributes.Table.Scope scope) {
      this.registerStructureElement(structElem);
      this.versionController.addTableHeaderScopeAttribute(structElem, scope);
   }

   public PDFInfo getInfo() {
      return this.info;
   }

   public PDFObject registerObject(PDFObject obj) {
      this.assignObjectNumber(obj);
      this.addObject(obj);
      if (obj instanceof AbstractPDFStream) {
         ((AbstractPDFStream)obj).registerChildren();
      }

      return obj;
   }

   PDFObject registerTrailerObject(PDFObject obj) {
      this.assignObjectNumber(obj);
      this.addTrailerObject(obj);
      return obj;
   }

   public void assignObjectNumber(PDFObject obj) {
      if (this.outputStarted && this.isLinearizationEnabled()) {
         throw new IllegalStateException("Can't assign number after start of output");
      } else if (obj == null) {
         throw new NullPointerException("obj must not be null");
      } else if (obj.hasObjectNumber()) {
         throw new IllegalStateException("Error registering a PDFObject: PDFObject already has an object number");
      } else {
         PDFDocument currentParent = obj.getDocument();
         if (currentParent != null && currentParent != this) {
            throw new IllegalStateException("Error registering a PDFObject: PDFObject already has a parent PDFDocument");
         } else {
            obj.setObjectNumber(this);
            if (currentParent == null) {
               obj.setDocument(this);
            }

         }
      }
   }

   public void addObject(PDFObject obj) {
      if (obj == null) {
         throw new NullPointerException("obj must not be null");
      } else if (!obj.hasObjectNumber()) {
         throw new IllegalStateException("Error adding a PDFObject: PDFObject doesn't have an object number");
      } else {
         this.objects.add(obj);
         if (obj instanceof PDFFunction) {
            this.functions.add((PDFFunction)obj);
         }

         String patternName;
         if (obj instanceof PDFShading) {
            patternName = "Sh" + ++this.shadingCount;
            ((PDFShading)obj).setName(patternName);
            this.shadings.add((PDFShading)obj);
         }

         if (obj instanceof PDFPattern) {
            patternName = "Pa" + ++this.patternCount;
            ((PDFPattern)obj).setName(patternName);
            this.patterns.add((PDFPattern)obj);
         }

         if (obj instanceof PDFFont) {
            PDFFont font = (PDFFont)obj;
            this.fontMap.put(font.getName(), font);
         }

         if (obj instanceof PDFGState) {
            this.gstates.add((PDFGState)obj);
         }

         if (obj instanceof PDFPage) {
            this.pages.notifyKidRegistered((PDFPage)obj);
            this.pageObjs.add((PDFPage)obj);
         }

         if (obj instanceof PDFLaunch) {
            this.launches.add((PDFLaunch)obj);
         }

         if (obj instanceof PDFLink) {
            this.links.add((PDFLink)obj);
         }

         if (obj instanceof PDFFileSpec) {
            this.filespecs.add((PDFFileSpec)obj);
         }

         if (obj instanceof PDFGoToRemote) {
            this.gotoremotes.add((PDFGoToRemote)obj);
         }

         if (obj instanceof PDFLayer) {
            if (this.layers == null) {
               this.layers = new ArrayList();
            }

            this.layers.add((PDFLayer)obj);
         }

         if (obj instanceof PDFNavigator) {
            if (this.navigators == null) {
               this.navigators = new ArrayList();
            }

            this.navigators.add((PDFNavigator)obj);
         }

         if (obj instanceof PDFNavigatorAction) {
            if (this.navigatorActions == null) {
               this.navigatorActions = new ArrayList();
            }

            this.navigatorActions.add((PDFNavigatorAction)obj);
         }

      }
   }

   public void addTrailerObject(PDFObject obj) {
      this.trailerObjects.add(obj);
      if (obj instanceof PDFGoTo) {
         this.gotos.add((PDFGoTo)obj);
      }

   }

   public void applyEncryption(AbstractPDFStream stream) {
      if (this.isEncryptionActive()) {
         this.encryption.applyFilter(stream);
      }

   }

   public void setEncryption(PDFEncryptionParams params) {
      this.getProfile().verifyEncryptionAllowed();
      this.fileIDGenerator = FileIDGenerator.getRandomFileIDGenerator();
      this.encryption = PDFEncryptionManager.newInstance(params, this);
      if (this.encryption != null) {
         PDFObject pdfObject = (PDFObject)this.encryption;
         this.addTrailerObject(pdfObject);

         try {
            if (this.encryption.getPDFVersion().compareTo(this.versionController.getPDFVersion()) > 0) {
               this.versionController.setPDFVersion(this.encryption.getPDFVersion());
            }
         } catch (IllegalStateException var4) {
            this.log.warn("Configured encryption requires PDF version " + this.encryption.getPDFVersion() + " but version has been set to " + this.versionController.getPDFVersion() + ".");
            throw var4;
         }
      } else {
         this.log.warn("PDF encryption is unavailable. PDF will be generated without encryption.");
         if (params.getEncryptionLengthInBits() == 256) {
            this.log.warn("Make sure the JCE Unlimited Strength Jurisdiction Policy files are available.AES 256 encryption cannot be performed without them.");
         }
      }

   }

   public boolean isEncryptionActive() {
      return this.encryption != null;
   }

   public PDFEncryption getEncryption() {
      return this.encryption;
   }

   private Object findPDFObject(List list, PDFObject compare) {
      Iterator var3 = list.iterator();

      PDFObject obj;
      do {
         if (!var3.hasNext()) {
            return null;
         }

         obj = (PDFObject)var3.next();
      } while(!compare.contentEquals(obj));

      return obj;
   }

   protected PDFFunction findFunction(PDFFunction compare) {
      return (PDFFunction)this.findPDFObject(this.functions, compare);
   }

   protected PDFShading findShading(PDFShading compare) {
      return (PDFShading)this.findPDFObject(this.shadings, compare);
   }

   protected PDFPattern findPattern(PDFPattern compare) {
      return (PDFPattern)this.findPDFObject(this.patterns, compare);
   }

   protected PDFFont findFont(String fontname) {
      return (PDFFont)this.fontMap.get(fontname);
   }

   protected PDFDestination findDestination(PDFDestination compare) {
      int index = this.getDestinationList().indexOf(compare);
      return index >= 0 ? (PDFDestination)this.getDestinationList().get(index) : null;
   }

   protected PDFLink findLink(PDFLink compare) {
      return (PDFLink)this.findPDFObject(this.links, compare);
   }

   protected PDFFileSpec findFileSpec(PDFFileSpec compare) {
      return (PDFFileSpec)this.findPDFObject(this.filespecs, compare);
   }

   protected PDFGoToRemote findGoToRemote(PDFGoToRemote compare) {
      return (PDFGoToRemote)this.findPDFObject(this.gotoremotes, compare);
   }

   protected PDFGoTo findGoTo(PDFGoTo compare) {
      return (PDFGoTo)this.findPDFObject(this.gotos, compare);
   }

   protected PDFLaunch findLaunch(PDFLaunch compare) {
      return (PDFLaunch)this.findPDFObject(this.launches, compare);
   }

   protected PDFGState findGState(PDFGState wanted, PDFGState current) {
      Iterator var4 = this.gstates.iterator();

      PDFGState poss;
      PDFGState avail;
      do {
         if (!var4.hasNext()) {
            return null;
         }

         avail = (PDFGState)var4.next();
         poss = new PDFGState();
         poss.addValues(current);
         poss.addValues(avail);
      } while(!poss.equals(wanted));

      return avail;
   }

   public PDFDeviceColorSpace getPDFColorSpace() {
      return this.colorspace;
   }

   public int getColorSpace() {
      return this.getPDFColorSpace().getColorSpace();
   }

   public void setColorSpace(int theColorspace) {
      this.colorspace.setColorSpace(theColorspace);
   }

   public Map getFontMap() {
      return this.fontMap;
   }

   /** @deprecated */
   @Deprecated
   public PDFImageXObject getImage(String key) {
      return (PDFImageXObject)this.xObjectsMap.get(key);
   }

   public PDFXObject getXObject(String key) {
      return (PDFXObject)this.xObjectsMap.get(key);
   }

   public void addDestination(PDFDestination destination) {
      if (this.destinations == null) {
         this.destinations = new ArrayList();
      }

      this.destinations.add(destination);
   }

   public List getDestinationList() {
      return this.hasDestinations() ? this.destinations : Collections.emptyList();
   }

   public boolean hasDestinations() {
      return this.destinations != null && !this.destinations.isEmpty();
   }

   public PDFImageXObject addImage(PDFResourceContext res, PDFImage img) {
      String key = img.getKey();
      PDFImageXObject xObject = (PDFImageXObject)this.xObjectsMap.get(key);
      if (xObject != null) {
         if (res != null) {
            res.addXObject(xObject);
         }

         return xObject;
      } else {
         img.setup(this);
         xObject = new PDFImageXObject(++this.xObjectCount, img);
         this.registerObject(xObject);
         this.resources.addXObject(xObject);
         if (res != null) {
            res.addXObject(xObject);
         }

         this.xObjectsMap.put(key, xObject);
         return xObject;
      }
   }

   public PDFFormXObject addFormXObject(PDFResourceContext res, PDFStream cont, PDFReference formres, String key) {
      PDFFormXObject xObject = (PDFFormXObject)this.xObjectsMap.get(key);
      if (xObject != null) {
         if (res != null) {
            res.addXObject(xObject);
         }

         return xObject;
      } else {
         xObject = new PDFFormXObject(++this.xObjectCount, cont, formres);
         this.registerObject(xObject);
         this.resources.addXObject(xObject);
         if (res != null) {
            res.addXObject(xObject);
         }

         this.xObjectsMap.put(key, xObject);
         return xObject;
      }
   }

   public PDFOutline getOutlineRoot() {
      if (this.outlineRoot != null) {
         return this.outlineRoot;
      } else {
         this.outlineRoot = new PDFOutline((String)null, (PDFReference)null, true);
         this.assignObjectNumber(this.outlineRoot);
         this.addTrailerObject(this.outlineRoot);
         this.root.setRootOutline(this.outlineRoot);
         return this.outlineRoot;
      }
   }

   public PDFResources getResources() {
      return this.resources;
   }

   public void enableAccessibility(boolean enableAccessibility) {
      this.accessibilityEnabled = enableAccessibility;
   }

   public PDFReference resolveExtensionReference(String id) {
      Iterator var2;
      if (this.layers != null) {
         var2 = this.layers.iterator();

         while(var2.hasNext()) {
            PDFLayer layer = (PDFLayer)var2.next();
            if (layer.hasId(id)) {
               return layer.makeReference();
            }
         }
      }

      if (this.navigators != null) {
         var2 = this.navigators.iterator();

         while(var2.hasNext()) {
            PDFNavigator navigator = (PDFNavigator)var2.next();
            if (navigator.hasId(id)) {
               return navigator.makeReference();
            }
         }
      }

      if (this.navigatorActions != null) {
         var2 = this.navigatorActions.iterator();

         while(var2.hasNext()) {
            PDFNavigatorAction action = (PDFNavigatorAction)var2.next();
            if (action.hasId(id)) {
               return action.makeReference();
            }
         }
      }

      return null;
   }

   public void output(OutputStream stream) throws IOException {
      this.outputStarted = true;

      while(this.objects.size() > 0) {
         PDFObject object = (PDFObject)this.objects.remove(0);
         this.streamIndirectObject(object, stream);
      }

   }

   protected void writeTrailer(OutputStream stream, int first, int last, int size, long mainOffset, long startxref) throws IOException {
      TrailerOutputHelper trailerOutputHelper = this.mayCompressStructureTreeElements() ? new CompressedTrailerOutputHelper() : new UncompressedTrailerOutputHelper();
      if (this.structureTreeElements != null) {
         ((TrailerOutputHelper)trailerOutputHelper).outputStructureTreeElements(stream);
      }

      TrailerDictionary trailerDictionary = this.createTrailerDictionary(mainOffset != 0L);
      if (mainOffset != 0L) {
         trailerDictionary.getDictionary().put("Prev", mainOffset);
      }

      ((TrailerOutputHelper)trailerOutputHelper).outputCrossReferenceObject(stream, trailerDictionary, first, last, size);
      String trailer = "\nstartxref\n" + startxref + "\n%%EOF\n";
      stream.write(encode(trailer));
   }

   protected int streamIndirectObject(PDFObject o, OutputStream stream) throws IOException {
      this.outputStarted = true;
      this.recordObjectOffset(o);
      int len = outputIndirectObject(o, stream);
      this.position += (long)len;
      return len;
   }

   private void streamIndirectObjects(Collection objects, OutputStream stream) throws IOException {
      Iterator var3 = objects.iterator();

      while(var3.hasNext()) {
         PDFObject o = (PDFObject)var3.next();
         this.streamIndirectObject(o, stream);
      }

   }

   private void recordObjectOffset(PDFObject object) {
      int index = object.getObjectNumber().getNumber() - 1;

      while(this.indirectObjectOffsets.size() <= index) {
         this.indirectObjectOffsets.add((Object)null);
      }

      this.indirectObjectOffsets.set(index, this.position);
   }

   public static int outputIndirectObject(PDFObject object, OutputStream stream) throws IOException {
      if (!object.hasObjectNumber()) {
         throw new IllegalArgumentException("Not an indirect object");
      } else {
         byte[] obj = encode(object.getObjectID());
         stream.write(obj);
         int length = object.output(stream);
         byte[] endobj = encode("\nendobj\n");
         stream.write(endobj);
         return obj.length + length + endobj.length;
      }
   }

   public void outputHeader(OutputStream stream) throws IOException {
      this.position = 0L;
      this.getProfile().verifyPDFVersion();
      byte[] pdf = encode("%PDF-" + this.getPDFVersionString() + "\n");
      stream.write(pdf);
      this.position += (long)pdf.length;
      byte[] bin = new byte[]{37, -86, -85, -84, -83, 10};
      stream.write(bin);
      this.position += (long)bin.length;
   }

   public void outputTrailer(OutputStream stream) throws IOException {
      this.createDestinations();
      this.output(stream);
      this.outputTrailerObjectsAndXref(stream);
   }

   private void createDestinations() {
      if (this.hasDestinations()) {
         Collections.sort(this.destinations, new DestinationComparator());
         PDFDests dests = this.getFactory().makeDests(this.destinations);
         if (this.root.getNames() == null) {
            this.root.setNames(this.getFactory().makeNames());
         }

         this.root.getNames().setDests(dests);
      }

   }

   private void outputTrailerObjectsAndXref(OutputStream stream) throws IOException {
      TrailerOutputHelper trailerOutputHelper = this.mayCompressStructureTreeElements() ? new CompressedTrailerOutputHelper() : new UncompressedTrailerOutputHelper();
      if (this.structureTreeElements != null) {
         ((TrailerOutputHelper)trailerOutputHelper).outputStructureTreeElements(stream);
      }

      this.streamIndirectObjects(this.trailerObjects, stream);
      TrailerDictionary trailerDictionary = this.createTrailerDictionary(true);
      long startxref = ((TrailerOutputHelper)trailerOutputHelper).outputCrossReferenceObject(stream, trailerDictionary, 0, this.indirectObjectOffsets.size(), this.indirectObjectOffsets.size());
      String trailer = "\nstartxref\n" + startxref + "\n%%EOF\n";
      stream.write(encode(trailer));
   }

   private boolean mayCompressStructureTreeElements() {
      return this.accessibilityEnabled && this.versionController.getPDFVersion().compareTo(Version.V1_5) >= 0 && !this.isLinearizationEnabled();
   }

   private TrailerDictionary createTrailerDictionary(boolean addRoot) {
      FileIDGenerator gen = this.getFileIDGenerator();
      TrailerDictionary trailerDictionary = new TrailerDictionary(this);
      if (addRoot) {
         trailerDictionary.setRoot(this.root).setInfo(this.info);
      }

      trailerDictionary.setFileID(gen.getOriginalFileID(), gen.getUpdatedFileID());
      if (this.isEncryptionActive()) {
         trailerDictionary.setEncryption(this.encryption);
      }

      return trailerDictionary;
   }

   public boolean isMergeFontsEnabled() {
      return this.mergeFontsEnabled;
   }

   public void setMergeFontsEnabled(boolean mergeFontsEnabled) {
      this.mergeFontsEnabled = mergeFontsEnabled;
      if (mergeFontsEnabled) {
         this.getResources().createFontsAsObj();
      }

   }

   long getCurrentFileSize() {
      return this.position;
   }

   FileIDGenerator getFileIDGenerator() {
      if (this.fileIDGenerator == null) {
         try {
            this.fileIDGenerator = FileIDGenerator.getDigestFileIDGenerator(this);
         } catch (NoSuchAlgorithmException var2) {
            this.fileIDGenerator = FileIDGenerator.getRandomFileIDGenerator();
         }
      }

      return this.fileIDGenerator;
   }

   public boolean isLinearizationEnabled() {
      return this.linearizationEnabled;
   }

   public void setLinearizationEnabled(boolean b) {
      this.linearizationEnabled = b;
   }

   public boolean isFormXObjectEnabled() {
      return this.formXObjectEnabled;
   }

   public void setFormXObjectEnabled(boolean b) {
      this.formXObjectEnabled = b;
   }

   private class CompressedTrailerOutputHelper implements TrailerOutputHelper {
      private ObjectStreamManager structureTreeObjectStreams;

      private CompressedTrailerOutputHelper() {
      }

      public void outputStructureTreeElements(OutputStream stream) throws IOException {
         assert PDFDocument.this.structureTreeElements.size() > 0;

         this.structureTreeObjectStreams = new ObjectStreamManager(PDFDocument.this);
         Iterator var2 = PDFDocument.this.structureTreeElements.iterator();

         while(var2.hasNext()) {
            PDFStructElem structElem = (PDFStructElem)var2.next();
            this.structureTreeObjectStreams.add(structElem);
         }

      }

      public long outputCrossReferenceObject(OutputStream stream, TrailerDictionary trailerDictionary, int first, int last, int size) throws IOException {
         assert PDFDocument.this.objects.isEmpty();

         (new CrossReferenceStream(PDFDocument.this, ++PDFDocument.this.objectcount, trailerDictionary, PDFDocument.this.position, PDFDocument.this.indirectObjectOffsets, this.structureTreeObjectStreams.getCompressedObjectReferences())).output(stream);
         return PDFDocument.this.position;
      }

      // $FF: synthetic method
      CompressedTrailerOutputHelper(Object x1) {
         this();
      }
   }

   private class UncompressedTrailerOutputHelper implements TrailerOutputHelper {
      private UncompressedTrailerOutputHelper() {
      }

      public void outputStructureTreeElements(OutputStream stream) throws IOException {
         PDFDocument.this.streamIndirectObjects(PDFDocument.this.structureTreeElements, stream);
      }

      public long outputCrossReferenceObject(OutputStream stream, TrailerDictionary trailerDictionary, int first, int last, int size) throws IOException {
         (new CrossReferenceTable(trailerDictionary, PDFDocument.this.position, PDFDocument.this.indirectObjectOffsets, first, last, size)).output(stream);
         return PDFDocument.this.position;
      }

      // $FF: synthetic method
      UncompressedTrailerOutputHelper(Object x1) {
         this();
      }
   }

   private interface TrailerOutputHelper {
      void outputStructureTreeElements(OutputStream var1) throws IOException;

      long outputCrossReferenceObject(OutputStream var1, TrailerDictionary var2, int var3, int var4, int var5) throws IOException;
   }
}
