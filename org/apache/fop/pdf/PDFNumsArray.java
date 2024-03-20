package org.apache.fop.pdf;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.apache.commons.io.output.CountingOutputStream;

public class PDFNumsArray extends PDFObject {
   protected SortedMap map = new TreeMap();

   public PDFNumsArray(PDFObject parent) {
      super(parent);
   }

   public int length() {
      return this.map.size();
   }

   public void put(Integer key, Object obj) {
      this.map.put(key, obj);
   }

   public void put(int key, Object obj) {
      this.put(key, obj);
   }

   public Object get(Integer key) {
      return this.map.get(key);
   }

   public Object get(int key) {
      return this.get(key);
   }

   public int output(OutputStream stream) throws IOException {
      CountingOutputStream cout = new CountingOutputStream(stream);
      StringBuilder textBuffer = new StringBuilder(64);
      textBuffer.append('[');
      boolean first = true;
      Iterator var5 = this.map.entrySet().iterator();

      while(var5.hasNext()) {
         Map.Entry entry = (Map.Entry)var5.next();
         if (!first) {
            textBuffer.append(" ");
         }

         first = false;
         this.formatObject(entry.getKey(), cout, textBuffer);
         textBuffer.append(" ");
         this.formatObject(entry.getValue(), cout, textBuffer);
      }

      textBuffer.append(']');
      PDFDocument.flushTextBuffer(textBuffer, cout);
      return cout.getCount();
   }
}
