package org.apache.fop.pdf;

public abstract class PDFAction extends PDFObject {
   public abstract String getAction();

   protected String encodeScript(String text) {
      if (this.getDocument() != null && this.getDocumentSafely().isEncryptionActive()) {
         byte[] buf = PDFText.encode(text);
         byte[] enc = this.getDocument().getEncryption().encrypt(buf, this);
         return PDFText.toHex(enc, true);
      } else {
         return PDFText.escapeText(text, false);
      }
   }
}
