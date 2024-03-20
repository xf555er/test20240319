package org.apache.fop.pdf;

import java.io.IOException;
import java.io.OutputStream;

public interface PDFWritable {
   void outputInline(OutputStream var1, StringBuilder var2) throws IOException;
}
