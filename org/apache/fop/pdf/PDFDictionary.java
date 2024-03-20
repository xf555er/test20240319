package org.apache.fop.pdf;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.output.CountingOutputStream;

public class PDFDictionary extends PDFObject {
   private boolean visited;
   protected Map entries = new HashMap();
   protected List order = new ArrayList();

   public PDFDictionary() {
   }

   public PDFDictionary(PDFObject parent) {
      super(parent);
   }

   public void put(String name, Object value) {
      if (value instanceof PDFObject) {
         PDFObject pdfObj = (PDFObject)value;
         if (!pdfObj.hasObjectNumber()) {
            pdfObj.setParent(this);
         }
      }

      if (!this.entries.containsKey(name)) {
         this.order.add(name);
      }

      this.entries.put(name, value);
   }

   public void put(String name, int value) {
      if (!this.entries.containsKey(name)) {
         this.order.add(name);
      }

      this.entries.put(name, value);
   }

   public Object get(String name) {
      return this.entries.get(name);
   }

   public int output(OutputStream stream) throws IOException {
      CountingOutputStream cout = new CountingOutputStream(stream);
      StringBuilder textBuffer = new StringBuilder(64);
      this.writeDictionary(cout, textBuffer);
      PDFDocument.flushTextBuffer(textBuffer, cout);
      return cout.getCount();
   }

   protected void writeDictionary(OutputStream out, StringBuilder textBuffer) throws IOException {
      textBuffer.append("<<");
      boolean compact = this.order.size() <= 2;
      Iterator var4 = this.order.iterator();

      while(var4.hasNext()) {
         String key = (String)var4.next();
         if (compact) {
            textBuffer.append(' ');
         } else {
            textBuffer.append("\n  ");
         }

         textBuffer.append(PDFName.escapeName(key));
         textBuffer.append(' ');
         Object obj = this.entries.get(key);
         this.formatObject(obj, out, textBuffer);
      }

      if (compact) {
         textBuffer.append(' ');
      } else {
         textBuffer.append('\n');
      }

      textBuffer.append(">>");
   }

   public void getChildren(Set children) {
      if (!this.visited) {
         this.visited = true;
         Map childrenMap = new HashMap(this.entries);
         childrenMap.remove("Parent");
         getChildren(childrenMap.values(), children);
         this.visited = false;
      }

   }

   public static void getChildren(Collection values, Set children) {
      Iterator var2 = values.iterator();

      while(var2.hasNext()) {
         Object x = var2.next();
         if (x instanceof PDFReference) {
            x = ((PDFReference)x).getObject();
         }

         if (x instanceof PDFObject) {
            if (((PDFObject)x).hasObjectNumber()) {
               children.add((PDFObject)x);
            }

            ((PDFObject)x).getChildren(children);
         }
      }

   }

   public Set keySet() {
      return this.entries.keySet();
   }

   public boolean containsKey(String name) {
      return this.entries.containsKey(name);
   }

   public void remove(String name) {
      this.entries.remove(name);
   }
}
