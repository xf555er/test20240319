package org.apache.fop.pdf.xref;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.fop.pdf.PDFArray;
import org.apache.fop.pdf.PDFDictionary;
import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.pdf.PDFEncryption;
import org.apache.fop.pdf.PDFInfo;
import org.apache.fop.pdf.PDFRoot;
import org.apache.fop.pdf.PDFText;
import org.apache.fop.pdf.PDFWritable;

public class TrailerDictionary {
   private final PDFDictionary dictionary = new PDFDictionary();

   public TrailerDictionary(PDFDocument pdfDocument) {
      this.dictionary.setDocument(pdfDocument);
   }

   public TrailerDictionary setRoot(PDFRoot root) {
      this.dictionary.put("/Root", root);
      return this;
   }

   public TrailerDictionary setInfo(PDFInfo info) {
      this.dictionary.put("/Info", info);
      return this;
   }

   public TrailerDictionary setEncryption(PDFEncryption encryption) {
      this.dictionary.put("/Encrypt", encryption);
      return this;
   }

   public TrailerDictionary setFileID(byte[] originalFileID, byte[] updatedFileID) {
      class FileID implements PDFWritable {
         private final byte[] fileID;

         FileID(byte[] id) {
            this.fileID = id;
         }

         public void outputInline(OutputStream out, StringBuilder textBuffer) throws IOException {
            PDFDocument.flushTextBuffer(textBuffer, out);
            String hex = PDFText.toHex(this.fileID, true);
            byte[] encoded = hex.getBytes("US-ASCII");
            out.write(encoded);
         }
      }

      PDFArray fileID = new PDFArray(new Object[]{new FileID(originalFileID), new FileID(updatedFileID)});
      this.dictionary.put("/ID", fileID);
      return this;
   }

   public PDFDictionary getDictionary() {
      return this.dictionary;
   }
}
