package org.apache.fop.pdf;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class PDFEmbeddedFiles extends PDFNameTreeNode {
   protected void writeDictionary(OutputStream out, StringBuilder textBuffer) throws IOException {
      this.sortNames();
      super.writeDictionary(out, textBuffer);
   }

   private void sortNames() {
      PDFArray names = this.getNames();
      SortedMap map = new TreeMap();
      int i = 0;
      int c = names.length();

      Object o;
      while(i < c) {
         Comparable key = (Comparable)names.get(i++);
         o = names.get(i++);
         map.put(key, o);
      }

      names.clear();
      Iterator var8 = map.entrySet().iterator();

      while(var8.hasNext()) {
         o = var8.next();
         Map.Entry entry = (Map.Entry)o;
         names.add(entry.getKey());
         names.add(entry.getValue());
      }

   }
}
