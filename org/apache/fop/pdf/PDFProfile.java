package org.apache.fop.pdf;

import java.text.MessageFormat;

public class PDFProfile {
   protected PDFAMode pdfAMode;
   protected PDFUAMode pdfUAMode;
   protected PDFXMode pdfXMode;
   protected PDFVTMode pdfVTMode;
   private PDFDocument doc;

   public PDFProfile(PDFDocument doc) {
      this.pdfAMode = PDFAMode.DISABLED;
      this.pdfUAMode = PDFUAMode.DISABLED;
      this.pdfXMode = PDFXMode.DISABLED;
      this.pdfVTMode = PDFVTMode.DISABLED;
      this.doc = doc;
   }

   protected void validateProfileCombination() {
      if (this.pdfAMode != PDFAMode.DISABLED && this.pdfAMode == PDFAMode.PDFA_1B && this.pdfXMode != PDFXMode.DISABLED && this.pdfXMode != PDFXMode.PDFX_3_2003 && this.pdfXMode != PDFXMode.PDFX_4) {
         throw new PDFConformanceException(this.pdfAMode + " and " + this.pdfXMode + " are not compatible!");
      } else if (this.pdfVTMode != PDFVTMode.DISABLED && this.pdfXMode != PDFXMode.PDFX_4) {
         throw new PDFConformanceException(this.pdfVTMode.name() + " requires " + PDFXMode.PDFX_4.getName() + " enabled");
      }
   }

   public PDFDocument getDocument() {
      return this.doc;
   }

   public PDFAMode getPDFAMode() {
      return this.pdfAMode;
   }

   public PDFUAMode getPDFUAMode() {
      return this.pdfUAMode;
   }

   public boolean isPDFAActive() {
      return this.getPDFAMode() != PDFAMode.DISABLED;
   }

   public void setPDFAMode(PDFAMode mode) {
      if (mode == null) {
         mode = PDFAMode.DISABLED;
      }

      this.pdfAMode = mode;
      this.validateProfileCombination();
   }

   public void setPDFUAMode(PDFUAMode mode) {
      if (mode == null) {
         mode = PDFUAMode.DISABLED;
      }

      this.pdfUAMode = mode;
      this.validateProfileCombination();
   }

   public PDFXMode getPDFXMode() {
      return this.pdfXMode;
   }

   public PDFVTMode getPDFVTMode() {
      return this.pdfVTMode;
   }

   public boolean isPDFXActive() {
      return this.getPDFXMode() != PDFXMode.DISABLED;
   }

   public boolean isPDFVTActive() {
      return this.getPDFVTMode() != PDFVTMode.DISABLED;
   }

   public void setPDFXMode(PDFXMode mode) {
      if (mode == null) {
         mode = PDFXMode.DISABLED;
      }

      this.pdfXMode = mode;
      this.validateProfileCombination();
   }

   public void setPDFVTMode(PDFVTMode mode) {
      if (mode == null) {
         mode = PDFVTMode.DISABLED;
      }

      this.pdfVTMode = mode;
      this.validateProfileCombination();
   }

   public String toString() {
      StringBuffer sb = new StringBuffer();
      if (this.isPDFAActive() && this.isPDFXActive()) {
         sb.append("[").append(this.getPDFAMode()).append(",").append(this.getPDFXMode()).append("]");
      } else if (this.isPDFAActive()) {
         sb.append(this.getPDFAMode());
      } else if (this.isPDFXActive()) {
         sb.append(this.getPDFXMode());
      } else if (this.getPDFUAMode().isEnabled()) {
         sb.append(this.getPDFUAMode());
      } else {
         sb.append(super.toString());
      }

      return sb.toString();
   }

   private String format(String pattern, Object[] args) {
      return MessageFormat.format(pattern, args);
   }

   private String format(String pattern, Object arg) {
      return this.format(pattern, new Object[]{arg});
   }

   public void verifyEncryptionAllowed() {
      String err = "{0} doesn't allow encrypted PDFs";
      if (this.isPDFAActive()) {
         throw new PDFConformanceException(this.format("{0} doesn't allow encrypted PDFs", (Object)this.getPDFAMode()));
      } else if (this.isPDFXActive()) {
         throw new PDFConformanceException(this.format("{0} doesn't allow encrypted PDFs", (Object)this.getPDFXMode()));
      }
   }

   public void verifyPSXObjectsAllowed() {
      String err = "PostScript XObjects are prohibited when {0} is active. Convert EPS graphics to another format.";
      if (this.isPDFAActive()) {
         throw new PDFConformanceException(this.format("PostScript XObjects are prohibited when {0} is active. Convert EPS graphics to another format.", (Object)this.getPDFAMode()));
      } else if (this.isPDFXActive()) {
         throw new PDFConformanceException(this.format("PostScript XObjects are prohibited when {0} is active. Convert EPS graphics to another format.", (Object)this.getPDFXMode()));
      }
   }

   public void verifyTransparencyAllowed(String context) {
      Object profile = this.isTransparencyAllowed();
      if (profile != null) {
         throw new TransparencyDisallowedException(profile, context);
      }
   }

   public Object isTransparencyAllowed() {
      if (this.pdfAMode.isPart1()) {
         return this.getPDFAMode();
      } else {
         return this.getPDFXMode() == PDFXMode.PDFX_3_2003 ? this.getPDFXMode() : null;
      }
   }

   public void verifyPDFVersion() {
      String err = "PDF version must be 1.4 for {0}";
      if (this.getPDFAMode().isPart1() && !Version.V1_4.equals(this.getDocument().getPDFVersion())) {
         throw new PDFConformanceException(this.format(err, (Object)this.getPDFAMode()));
      } else if (this.getPDFXMode() == PDFXMode.PDFX_3_2003 && !Version.V1_4.equals(this.getDocument().getPDFVersion())) {
         throw new PDFConformanceException(this.format(err, (Object)this.getPDFXMode()));
      }
   }

   public void verifyTaggedPDF() {
      if (this.getPDFAMode().isLevelA() || this.getPDFUAMode().isEnabled()) {
         String err = "{0} requires the {1} dictionary entry to be set";
         String mode = this.getPDFAMode().toString();
         if (this.getPDFUAMode().isEnabled()) {
            mode = this.getPDFUAMode().toString();
         }

         PDFDictionary markInfo = this.getDocument().getRoot().getMarkInfo();
         if (markInfo == null) {
            throw new PDFConformanceException(this.format("{0} requires that the accessibility option in the configuration file be enabled", (Object)mode));
         }

         if (!Boolean.TRUE.equals(markInfo.get("Marked"))) {
            throw new PDFConformanceException(this.format("{0} requires the {1} dictionary entry to be set", new Object[]{mode, "Marked"}));
         }

         if (this.getDocument().getRoot().getStructTreeRoot() == null) {
            throw new PDFConformanceException(this.format("{0} requires the {1} dictionary entry to be set", new Object[]{mode, "StructTreeRoot"}));
         }

         if (this.getDocument().getRoot().getLanguage() == null) {
            throw new PDFConformanceException(this.format("{0} requires the {1} dictionary entry to be set", new Object[]{mode, "Lang"}));
         }
      }

   }

   public boolean isIDEntryRequired() {
      return this.isPDFAActive() || this.isPDFXActive();
   }

   public boolean isFontEmbeddingRequired() {
      return this.isPDFAActive() || this.isPDFXActive() || this.getPDFUAMode().isEnabled();
   }

   public void verifyTitleAbsent() {
      String err = "{0} requires the title to be set.";
      if (this.getPDFUAMode().isEnabled()) {
         throw new PDFConformanceException(this.format("{0} requires the title to be set.", (Object)this.getPDFUAMode()));
      } else if (this.isPDFXActive()) {
         throw new PDFConformanceException(this.format("{0} requires the title to be set.", (Object)this.getPDFXMode()));
      }
   }

   public boolean isModDateRequired() {
      return this.getPDFXMode() != PDFXMode.DISABLED;
   }

   public boolean isTrappedEntryRequired() {
      return this.getPDFXMode() != PDFXMode.DISABLED;
   }

   public boolean isAnnotationAllowed() {
      return !this.isPDFXActive();
   }

   public void verifyAnnotAllowed() {
      if (!this.isAnnotationAllowed()) {
         String err = "{0} does not allow annotations inside the printable area.";
         throw new PDFConformanceException(this.format("{0} does not allow annotations inside the printable area.", (Object)this.getPDFXMode()));
      }
   }

   public void verifyActionAllowed() {
      if (this.isPDFXActive()) {
         String err = "{0} does not allow Actions.";
         throw new PDFConformanceException(this.format("{0} does not allow Actions.", (Object)this.getPDFXMode()));
      }
   }

   public void verifyEmbeddedFilesAllowed() {
      String err = "{0} does not allow embedded files.";
      if (this.isPDFAActive() && this.getPDFAMode().getPart() < 3) {
         throw new PDFConformanceException(this.format("{0} does not allow embedded files.", (Object)this.getPDFAMode()));
      } else if (this.isPDFXActive()) {
         throw new PDFConformanceException(this.format("{0} does not allow embedded files.", (Object)this.getPDFXMode()));
      }
   }
}
