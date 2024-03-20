package org.apache.fop.pdf.xref;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import org.apache.fop.pdf.PDFDictionary;
import org.apache.fop.pdf.PDFDocument;

public class CrossReferenceTable extends CrossReferenceObject {
   private final List objectReferences;
   private final StringBuilder pdf = new StringBuilder(256);
   private int last;
   private int first;
   private int size;

   public CrossReferenceTable(TrailerDictionary trailerDictionary, long startxref, List location, int first, int last, int size) {
      super(trailerDictionary, startxref);
      this.objectReferences = location;
      this.first = first;
      this.last = last;
      this.size = size;
   }

   public void output(OutputStream stream) throws IOException {
      this.outputXref();
      this.writeTrailer(stream);
   }

   private void outputXref() throws IOException {
      if (this.first == 0) {
         this.pdf.append("xref\n0 ");
         this.pdf.append(this.last + 1);
         this.pdf.append("\n0000000000 65535 f \n");
      } else {
         this.pdf.append("xref\n" + (this.first + 1) + " ");
         this.pdf.append(this.last + "\n");
      }

      for(int i = this.first; i < this.first + this.last; ++i) {
         Long objectReference = (Long)this.objectReferences.get(i);

         assert objectReference != null;

         String padding = "0000000000";
         String s = String.valueOf(objectReference);
         if (s.length() > 10) {
            throw new IOException("PDF file too large. PDF 1.4 cannot grow beyond approx. 9.3GB.");
         }

         String loc = "0000000000".substring(s.length()) + s;
         this.pdf.append(loc).append(" 00000 n \n");
      }

   }

   private void writeTrailer(OutputStream stream) throws IOException {
      this.pdf.append("trailer\n");
      stream.write(PDFDocument.encode(this.pdf.toString()));
      PDFDictionary dictionary = this.trailerDictionary.getDictionary();
      dictionary.put("/Size", this.size + 1);
      dictionary.output(stream);
   }
}
