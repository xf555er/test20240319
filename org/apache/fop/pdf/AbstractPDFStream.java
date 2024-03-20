package org.apache.fop.pdf;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.fop.util.CloseBlockerOutputStream;

public abstract class AbstractPDFStream extends PDFObject {
   private final PDFDictionary dictionary;
   private PDFFilterList filters;
   private boolean encodeOnTheFly;
   private PDFNumber refLength;

   protected AbstractPDFStream() {
      this(true);
   }

   protected AbstractPDFStream(PDFDictionary dictionary) {
      this(dictionary, true);
   }

   protected AbstractPDFStream(boolean encodeOnTheFly) {
      this(new PDFDictionary(), encodeOnTheFly);
   }

   protected AbstractPDFStream(PDFDictionary dictionary, boolean encodeOnTheFly) {
      this.refLength = new PDFNumber();
      this.dictionary = dictionary;
      dictionary.setParent(this);
      this.encodeOnTheFly = encodeOnTheFly;
   }

   protected final PDFDictionary getDictionary() {
      return this.dictionary;
   }

   public Object get(String key) {
      return this.dictionary.get(key);
   }

   public void put(String key, Object value) {
      this.dictionary.put(key, value);
   }

   protected void setupFilterList() {
      if (this.multipleFiltersAllowed() && !this.getFilterList().isInitialized()) {
         this.getFilterList().addDefaultFilters(this.getDocumentSafely().getFilterMap(), this.getDefaultFilterName());
      }

      this.prepareImplicitFilters();
      this.getDocument().applyEncryption(this);
   }

   protected String getDefaultFilterName() {
      return "default";
   }

   public PDFFilterList getFilterList() {
      if (this.filters == null) {
         if (this.getDocument() == null) {
            this.filters = new PDFFilterList();
         } else {
            this.filters = new PDFFilterList(this.getDocument().isEncryptionActive());
         }

         boolean hasFilterEntries = this.get("Filter") != null;
         if (hasFilterEntries) {
            this.filters.setDisableAllFilters(true);
         }
      }

      return this.filters;
   }

   protected abstract int getSizeHint() throws IOException;

   protected abstract void outputRawStreamData(OutputStream var1) throws IOException;

   protected int outputStreamData(StreamCache encodedStream, OutputStream out) throws IOException {
      int length = 0;
      byte[] p = encode("\nstream\n");
      out.write(p);
      length += p.length;
      encodedStream.outputContents(out);
      length += encodedStream.getSize();
      p = encode("\nendstream");
      out.write(p);
      length += p.length;
      return length;
   }

   protected StreamCache encodeStream() throws IOException {
      StreamCache encodedStream = StreamCacheFactory.getInstance().createStreamCache(this.getSizeHint());
      OutputStream filteredOutput = this.getFilterList().applyFilters(encodedStream.getOutputStream());
      this.outputRawStreamData(filteredOutput);
      filteredOutput.flush();
      filteredOutput.close();
      return encodedStream;
   }

   protected int encodeAndWriteStream(OutputStream out, PDFNumber refLength) throws IOException {
      int bytesWritten = 0;
      byte[] buf = encode("\nstream\n");
      out.write(buf);
      bytesWritten += buf.length;
      CloseBlockerOutputStream cbout = new CloseBlockerOutputStream(out);
      CountingOutputStream cout = new CountingOutputStream(cbout);
      OutputStream filteredOutput = this.getFilterList().applyFilters(cout);
      this.outputRawStreamData(filteredOutput);
      filteredOutput.close();
      refLength.setNumber(cout.getCount());
      bytesWritten += cout.getCount();
      buf = encode("\nendstream");
      out.write(buf);
      bytesWritten += buf.length;
      return bytesWritten;
   }

   public int output(OutputStream stream) throws IOException {
      this.setupFilterList();
      CountingOutputStream cout = new CountingOutputStream(stream);
      StringBuilder textBuffer = new StringBuilder(64);
      StreamCache encodedStream = null;
      Object lengthEntry;
      if (this.encodeOnTheFly) {
         if (!this.refLength.hasObjectNumber()) {
            this.registerChildren();
         }

         lengthEntry = this.refLength;
      } else {
         encodedStream = this.encodeStream();
         lengthEntry = encodedStream.getSize();
      }

      this.populateStreamDict(lengthEntry);
      this.dictionary.writeDictionary(cout, textBuffer);
      PDFDocument.flushTextBuffer(textBuffer, cout);
      if (encodedStream == null) {
         this.encodeAndWriteStream(cout, this.refLength);
      } else {
         this.outputStreamData(encodedStream, cout);
         encodedStream.clear();
      }

      PDFDocument.flushTextBuffer(textBuffer, cout);
      return cout.getCount();
   }

   public void setDocument(PDFDocument doc) {
      this.dictionary.setDocument(doc);
      super.setDocument(doc);
   }

   protected void populateStreamDict(Object lengthEntry) {
      this.put("Length", lengthEntry);
      if (!this.getFilterList().isDisableAllFilters()) {
         this.getFilterList().putFilterDictEntries(this.dictionary);
      }

   }

   protected void prepareImplicitFilters() {
   }

   protected boolean multipleFiltersAllowed() {
      return true;
   }

   public void getChildren(Set children) {
      this.dictionary.getChildren(children);
      if (this.encodeOnTheFly) {
         children.add(this.refLength);
      }

   }

   public void registerChildren() {
      if (this.encodeOnTheFly) {
         this.getDocument().registerObject(this.refLength);
      }

   }
}
