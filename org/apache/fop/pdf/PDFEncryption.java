package org.apache.fop.pdf;

public interface PDFEncryption {
   void applyFilter(AbstractPDFStream var1);

   byte[] encrypt(byte[] var1, PDFObject var2);

   String getTrailerEntry();

   Version getPDFVersion();
}
