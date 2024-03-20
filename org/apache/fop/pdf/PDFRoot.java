package org.apache.fop.pdf;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;
import org.apache.fop.util.LanguageTags;

public class PDFRoot extends PDFDictionary {
   public static final int PAGEMODE_USENONE = 0;
   public static final int PAGEMODE_USEOUTLINES = 1;
   public static final int PAGEMODE_USETHUMBS = 2;
   public static final int PAGEMODE_FULLSCREEN = 3;
   private final PDFDocument document;
   private PDFDPartRoot dPartRoot;
   private PDFArray af;
   private static final PDFName[] PAGEMODE_NAMES = new PDFName[]{new PDFName("UseNone"), new PDFName("UseOutlines"), new PDFName("UseThumbs"), new PDFName("FullScreen")};

   public PDFRoot(PDFDocument document, PDFPages pages) {
      this.document = document;
      this.setObjectNumber(document);
      this.put("Type", new PDFName("Catalog"));
      this.setRootPages(pages);
      this.setLanguage("x-unknown");
   }

   public int output(OutputStream stream) throws IOException {
      if (this.document.getProfile().getPDFUAMode().isEnabled()) {
         PDFDictionary d = new PDFDictionary();
         d.put("DisplayDocTitle", true);
         this.put("ViewerPreferences", d);
      }

      this.getDocument().getProfile().verifyTaggedPDF();
      return super.output(stream);
   }

   public void setPageMode(int mode) {
      this.put("PageMode", PAGEMODE_NAMES[mode]);
   }

   public int getPageMode() {
      PDFName mode = (PDFName)this.get("PageMode");
      if (mode != null) {
         for(int i = 0; i < PAGEMODE_NAMES.length; ++i) {
            if (PAGEMODE_NAMES[i].equals(mode)) {
               return i;
            }
         }

         throw new IllegalStateException("Unknown /PageMode encountered: " + mode);
      } else {
         return 0;
      }
   }

   public void addPage(PDFPage page) {
      PDFPages pages = this.getRootPages();
      pages.addPage(page);
   }

   public void setRootPages(PDFPages pages) {
      this.put("Pages", pages.makeReference());
   }

   public PDFPages getRootPages() {
      PDFReference ref = (PDFReference)this.get("Pages");
      return ref != null ? (PDFPages)ref.getObject() : null;
   }

   public void setPageLabels(PDFPageLabels pageLabels) {
      this.put("PageLabels", pageLabels.makeReference());
   }

   public PDFPageLabels getPageLabels() {
      PDFReference ref = (PDFReference)this.get("PageLabels");
      return ref != null ? (PDFPageLabels)ref.getObject() : null;
   }

   public void setRootOutline(PDFOutline out) {
      this.put("Outlines", out.makeReference());
      PDFName mode = (PDFName)this.get("PageMode");
      if (mode == null) {
         this.setPageMode(1);
      }

   }

   public PDFOutline getRootOutline() {
      PDFReference ref = (PDFReference)this.get("Outlines");
      return ref != null ? (PDFOutline)ref.getObject() : null;
   }

   public void setNames(PDFNames names) {
      this.put("Names", names.makeReference());
   }

   public PDFNames getNames() {
      PDFReference ref = (PDFReference)this.get("Names");
      return ref != null ? (PDFNames)ref.getObject() : null;
   }

   public void setMetadata(PDFMetadata meta) {
      if (this.getDocumentSafely().getPDFVersion().compareTo(Version.V1_4) >= 0) {
         this.put("Metadata", meta.makeReference());
      }

   }

   public PDFMetadata getMetadata() {
      PDFReference ref = (PDFReference)this.get("Metadata");
      return ref != null ? (PDFMetadata)ref.getObject() : null;
   }

   public PDFArray getOutputIntents() {
      return (PDFArray)this.get("OutputIntents");
   }

   public void addOutputIntent(PDFOutputIntent outputIntent) {
      if (this.getDocumentSafely().getPDFVersion().compareTo(Version.V1_4) >= 0) {
         PDFArray outputIntents = this.getOutputIntents();
         if (outputIntents == null) {
            outputIntents = new PDFArray(this);
            this.put("OutputIntents", outputIntents);
         }

         outputIntents.add(outputIntent);
      }

   }

   void setVersion(Version version) {
      this.put("Version", new PDFName(version.toString()));
   }

   public String getLanguage() {
      return (String)this.get("Lang");
   }

   public void setLanguage(Locale locale) {
      if (locale == null) {
         throw new NullPointerException("locale must not be null");
      } else {
         this.setLanguage(LanguageTags.toLanguageTag(locale));
      }
   }

   private void setLanguage(String lang) {
      this.put("Lang", lang);
   }

   public void setStructTreeRoot(PDFStructTreeRoot structTreeRoot) {
      if (structTreeRoot == null) {
         throw new NullPointerException("structTreeRoot must not be null");
      } else {
         this.put("StructTreeRoot", structTreeRoot);
      }
   }

   public PDFStructTreeRoot getStructTreeRoot() {
      return (PDFStructTreeRoot)this.get("StructTreeRoot");
   }

   public void makeTagged() {
      PDFDictionary dict = new PDFDictionary();
      dict.put("Marked", Boolean.TRUE);
      this.put("MarkInfo", dict);
   }

   public PDFDictionary getMarkInfo() {
      return (PDFDictionary)this.get("MarkInfo");
   }

   public PDFDPartRoot getDPartRoot() {
      if (this.dPartRoot == null) {
         this.dPartRoot = this.getDocument().getFactory().makeDPartRoot();
         this.put("DPartRoot", this.dPartRoot.makeReference());
      }

      return this.dPartRoot;
   }

   public void addAF(PDFFileSpec fileSpec) {
      if (this.af == null) {
         this.af = new PDFArray();
         this.put("AF", this.af);
      }

      this.af.add(fileSpec);
      fileSpec.put("AFRelationship", new PDFName("Data"));
   }
}
