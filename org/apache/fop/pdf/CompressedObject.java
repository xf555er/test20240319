package org.apache.fop.pdf;

import java.io.IOException;
import java.io.OutputStream;

interface CompressedObject {
   PDFObjectNumber getObjectNumber();

   int output(OutputStream var1) throws IOException;
}
