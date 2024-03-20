package org.apache.fop.pdf;

import java.util.ArrayList;
import java.util.List;

class ObjectStreamManager {
   private static final int OBJECT_STREAM_CAPACITY = 100;
   private final PDFDocument pdfDocument;
   private final List compressedObjectReferences;
   private int numObjectsInStream;
   private ObjectStream currentObjectStream;

   ObjectStreamManager(PDFDocument pdfDocument) {
      this.pdfDocument = pdfDocument;
      this.createObjectStream();
      this.compressedObjectReferences = new ArrayList();
   }

   void add(CompressedObject compressedObject) {
      if (this.numObjectsInStream++ == 100) {
         this.createObjectStream();
         this.numObjectsInStream = 1;
      }

      this.compressedObjectReferences.add(this.currentObjectStream.addObject(compressedObject));
   }

   private void createObjectStream() {
      this.currentObjectStream = this.currentObjectStream == null ? new ObjectStream() : new ObjectStream(this.currentObjectStream);
      this.pdfDocument.assignObjectNumber(this.currentObjectStream);
      this.pdfDocument.addTrailerObject(this.currentObjectStream);
   }

   List getCompressedObjectReferences() {
      return this.compressedObjectReferences;
   }
}
