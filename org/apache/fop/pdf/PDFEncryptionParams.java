package org.apache.fop.pdf;

public class PDFEncryptionParams {
   private String userPassword = "";
   private String ownerPassword = "";
   private boolean allowPrint = true;
   private boolean allowCopyContent = true;
   private boolean allowEditContent = true;
   private boolean allowEditAnnotations = true;
   private boolean allowFillInForms = true;
   private boolean allowAccessContent = true;
   private boolean allowAssembleDocument = true;
   private boolean allowPrintHq = true;
   private boolean encryptMetadata = true;
   private int encryptionLengthInBits = 128;

   public PDFEncryptionParams(String userPassword, String ownerPassword, boolean allowPrint, boolean allowCopyContent, boolean allowEditContent, boolean allowEditAnnotations, boolean encryptMetadata) {
      this.setUserPassword(userPassword);
      this.setOwnerPassword(ownerPassword);
      this.setAllowPrint(allowPrint);
      this.setAllowCopyContent(allowCopyContent);
      this.setAllowEditContent(allowEditContent);
      this.setAllowEditAnnotations(allowEditAnnotations);
      this.encryptMetadata = encryptMetadata;
   }

   public PDFEncryptionParams() {
   }

   public PDFEncryptionParams(PDFEncryptionParams source) {
      this.setUserPassword(source.getUserPassword());
      this.setOwnerPassword(source.getOwnerPassword());
      this.setAllowPrint(source.isAllowPrint());
      this.setAllowCopyContent(source.isAllowCopyContent());
      this.setAllowEditContent(source.isAllowEditContent());
      this.setAllowEditAnnotations(source.isAllowEditAnnotations());
      this.setAllowAssembleDocument(source.isAllowAssembleDocument());
      this.setAllowAccessContent(source.isAllowAccessContent());
      this.setAllowFillInForms(source.isAllowFillInForms());
      this.setAllowPrintHq(source.isAllowPrintHq());
      this.setEncryptionLengthInBits(source.getEncryptionLengthInBits());
      this.encryptMetadata = source.encryptMetadata();
   }

   public boolean isAllowCopyContent() {
      return this.allowCopyContent;
   }

   public boolean isAllowEditAnnotations() {
      return this.allowEditAnnotations;
   }

   public boolean isAllowEditContent() {
      return this.allowEditContent;
   }

   public boolean isAllowPrint() {
      return this.allowPrint;
   }

   public boolean isAllowFillInForms() {
      return this.allowFillInForms;
   }

   public boolean isAllowAccessContent() {
      return this.allowAccessContent;
   }

   public boolean isAllowAssembleDocument() {
      return this.allowAssembleDocument;
   }

   public boolean isAllowPrintHq() {
      return this.allowPrintHq;
   }

   public boolean encryptMetadata() {
      return this.encryptMetadata;
   }

   public String getOwnerPassword() {
      return this.ownerPassword;
   }

   public String getUserPassword() {
      return this.userPassword;
   }

   public void setAllowCopyContent(boolean allowCopyContent) {
      this.allowCopyContent = allowCopyContent;
   }

   public void setAllowEditAnnotations(boolean allowEditAnnotations) {
      this.allowEditAnnotations = allowEditAnnotations;
   }

   public void setAllowEditContent(boolean allowEditContent) {
      this.allowEditContent = allowEditContent;
   }

   public void setAllowPrint(boolean allowPrint) {
      this.allowPrint = allowPrint;
   }

   public void setAllowFillInForms(boolean allowFillInForms) {
      this.allowFillInForms = allowFillInForms;
   }

   public void setAllowAccessContent(boolean allowAccessContent) {
      this.allowAccessContent = allowAccessContent;
   }

   public void setAllowAssembleDocument(boolean allowAssembleDocument) {
      this.allowAssembleDocument = allowAssembleDocument;
   }

   public void setAllowPrintHq(boolean allowPrintHq) {
      this.allowPrintHq = allowPrintHq;
   }

   public void setEncryptMetadata(boolean encryptMetadata) {
      this.encryptMetadata = encryptMetadata;
   }

   public void setOwnerPassword(String ownerPassword) {
      if (ownerPassword == null) {
         this.ownerPassword = "";
      } else {
         this.ownerPassword = ownerPassword;
      }

   }

   public void setUserPassword(String userPassword) {
      if (userPassword == null) {
         this.userPassword = "";
      } else {
         this.userPassword = userPassword;
      }

   }

   public int getEncryptionLengthInBits() {
      return this.encryptionLengthInBits;
   }

   public void setEncryptionLengthInBits(int encryptionLength) {
      this.encryptionLengthInBits = encryptionLength;
   }

   public String toString() {
      return "userPassword = " + this.userPassword + "\nownerPassword = " + this.ownerPassword + "\nallowPrint = " + this.allowPrint + "\nallowCopyContent = " + this.allowCopyContent + "\nallowEditContent = " + this.allowEditContent + "\nallowEditAnnotations = " + this.allowEditAnnotations + "\nallowFillInForms  = " + this.allowFillInForms + "\nallowAccessContent = " + this.allowAccessContent + "\nallowAssembleDocument = " + this.allowAssembleDocument + "\nallowPrintHq = " + this.allowPrintHq + "\nencryptMetadata = " + this.encryptMetadata;
   }
}
