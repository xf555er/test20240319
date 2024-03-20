package org.apache.fop.pdf.xref;

import java.io.IOException;
import java.io.OutputStream;

public abstract class CrossReferenceObject {
   protected final TrailerDictionary trailerDictionary;
   protected final long startxref;

   CrossReferenceObject(TrailerDictionary trailerDictionary, long startxref) {
      this.trailerDictionary = trailerDictionary;
      this.startxref = startxref;
   }

   public abstract void output(OutputStream var1) throws IOException;
}
