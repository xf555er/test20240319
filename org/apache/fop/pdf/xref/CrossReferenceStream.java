package org.apache.fop.pdf.xref;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.fop.pdf.PDFArray;
import org.apache.fop.pdf.PDFDictionary;
import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.pdf.PDFFilterList;
import org.apache.fop.pdf.PDFName;
import org.apache.fop.pdf.PDFObjectNumber;
import org.apache.fop.pdf.PDFStream;

public class CrossReferenceStream extends CrossReferenceObject {
   private static final PDFName XREF = new PDFName("XRef");
   private final PDFDocument document;
   private final int objectNumber;
   private final List objectReferences;

   public CrossReferenceStream(PDFDocument document, int objectNumber, TrailerDictionary trailerDictionary, long startxref, List uncompressedObjectReferences, List compressedObjectReferences) {
      super(trailerDictionary, startxref);
      this.document = document;
      this.objectNumber = objectNumber;
      this.objectReferences = new ArrayList(uncompressedObjectReferences.size());
      Iterator var8 = uncompressedObjectReferences.iterator();

      while(var8.hasNext()) {
         Long offset = (Long)var8.next();
         this.objectReferences.add(offset == null ? null : new UncompressedObjectReference(offset));
      }

      var8 = compressedObjectReferences.iterator();

      while(var8.hasNext()) {
         CompressedObjectReference ref = (CompressedObjectReference)var8.next();

         while(ref.getObjectNumber().getNumber() > this.objectReferences.size()) {
            this.objectReferences.add((Object)null);
         }

         this.objectReferences.set(ref.getObjectNumber().getNumber() - 1, ref);
      }

   }

   public void output(OutputStream stream) throws IOException {
      this.populateDictionary();
      PDFStream helperStream = new PDFStream(this.trailerDictionary.getDictionary(), false) {
         protected void setupFilterList() {
            PDFFilterList filterList = this.getFilterList();

            assert !filterList.isInitialized();

            filterList.addDefaultFilters(CrossReferenceStream.this.document.getFilterMap(), this.getDefaultFilterName());
         }
      };
      helperStream.setObjectNumber(new PDFObjectNumber(this.objectNumber));
      helperStream.setDocument(this.document);
      ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
      DataOutputStream data = new DataOutputStream(byteArray);
      this.addFreeEntryForObject0(data);
      Iterator var5 = this.objectReferences.iterator();

      while(var5.hasNext()) {
         ObjectReference objectReference = (ObjectReference)var5.next();

         assert objectReference != null;

         objectReference.output(data);
      }

      (new UncompressedObjectReference(this.startxref)).output(data);
      data.close();
      helperStream.setData(byteArray.toByteArray());
      PDFDocument.outputIndirectObject(helperStream, stream);
   }

   private void populateDictionary() throws IOException {
      int objectCount = this.objectReferences.size() + 1;
      PDFDictionary dictionary = this.trailerDictionary.getDictionary();
      dictionary.put("/Type", XREF);
      dictionary.put("/Size", objectCount + 1);
      dictionary.put("/W", new PDFArray(new Object[]{1, 8, 2}));
   }

   private void addFreeEntryForObject0(DataOutputStream data) throws IOException {
      data.write(new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1});
   }
}
