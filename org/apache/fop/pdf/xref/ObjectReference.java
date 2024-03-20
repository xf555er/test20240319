package org.apache.fop.pdf.xref;

import java.io.DataOutputStream;
import java.io.IOException;

interface ObjectReference {
   void output(DataOutputStream var1) throws IOException;
}
