package org.apache.fop.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.fop.pdf.xref.CompressedObjectReference;

public class ObjectStream extends PDFStream {
   private static final PDFName OBJ_STM = new PDFName("ObjStm");
   private List objects;
   private int firstObjectOffset;

   ObjectStream() {
      super(false);
      this.objects = new ArrayList();
   }

   ObjectStream(ObjectStream previous) {
      this();
      this.put("Extends", previous);
   }

   CompressedObjectReference addObject(CompressedObject obj) {
      if (obj == null) {
         throw new NullPointerException("obj must not be null");
      } else {
         CompressedObjectReference reference = new CompressedObjectReference(obj.getObjectNumber(), this.getObjectNumber(), this.objects.size());
         this.objects.add(obj);
         return reference;
      }
   }

   protected void outputRawStreamData(OutputStream out) throws IOException {
      int currentOffset = 0;
      StringBuilder offsetsPart = new StringBuilder();
      ByteArrayOutputStream streamContent = new ByteArrayOutputStream();

      CompressedObject object;
      for(Iterator var5 = this.objects.iterator(); var5.hasNext(); currentOffset += object.output(streamContent)) {
         object = (CompressedObject)var5.next();
         offsetsPart.append(object.getObjectNumber()).append(' ').append(currentOffset).append('\n');
      }

      byte[] offsets = PDFDocument.encode(offsetsPart.toString());
      this.firstObjectOffset = offsets.length;
      out.write(offsets);
      streamContent.writeTo(out);
   }

   protected void populateStreamDict(Object lengthEntry) {
      this.put("Type", OBJ_STM);
      this.put("N", this.objects.size());
      this.put("First", this.firstObjectOffset);
      super.populateStreamDict(lengthEntry);
   }
}
