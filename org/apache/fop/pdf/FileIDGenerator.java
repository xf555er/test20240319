package org.apache.fop.pdf;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

abstract class FileIDGenerator {
   abstract byte[] getOriginalFileID();

   abstract byte[] getUpdatedFileID();

   static FileIDGenerator getRandomFileIDGenerator() {
      return new RandomFileIDGenerator();
   }

   static FileIDGenerator getDigestFileIDGenerator(PDFDocument document) throws NoSuchAlgorithmException {
      return new DigestFileIDGenerator(document);
   }

   private static final class DigestFileIDGenerator extends FileIDGenerator {
      private byte[] fileID;
      private final PDFDocument document;
      private final MessageDigest digest;

      DigestFileIDGenerator(PDFDocument document) throws NoSuchAlgorithmException {
         this.document = document;
         this.digest = MessageDigest.getInstance("MD5");
      }

      byte[] getOriginalFileID() {
         if (this.fileID == null) {
            this.generateFileID();
         }

         return this.fileID;
      }

      byte[] getUpdatedFileID() {
         return this.getOriginalFileID();
      }

      private void generateFileID() {
         DateFormat df = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSS");
         this.digest.update(PDFDocument.encode(df.format(new Date())));
         this.digest.update(PDFDocument.encode(String.valueOf(this.document.getCurrentFileSize())));
         this.digest.update(this.document.getInfo().toPDF());
         this.fileID = this.digest.digest();
      }
   }

   private static final class RandomFileIDGenerator extends FileIDGenerator {
      private byte[] fileID;

      private RandomFileIDGenerator() {
         Random random = new Random();
         this.fileID = new byte[16];
         random.nextBytes(this.fileID);
      }

      byte[] getOriginalFileID() {
         return this.fileID;
      }

      byte[] getUpdatedFileID() {
         return this.fileID;
      }

      // $FF: synthetic method
      RandomFileIDGenerator(Object x0) {
         this();
      }
   }
}
