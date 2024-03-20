package org.apache.fop.pdf;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.commons.io.output.CountingOutputStream;

public class PDFArray extends PDFObject {
   protected List values;

   public PDFArray(PDFObject parent) {
      super(parent);
      this.values = new ArrayList();
   }

   public PDFArray() {
      this((PDFObject)null);
   }

   public PDFArray(PDFObject parent, int[] values) {
      super(parent);
      this.values = new ArrayList();
      int[] var3 = values;
      int var4 = values.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         int value = var3[var5];
         this.values.add(value);
      }

   }

   public PDFArray(PDFObject parent, double[] values) {
      super(parent);
      this.values = new ArrayList();
      double[] var3 = values;
      int var4 = values.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         double value = var3[var5];
         this.values.add(value);
      }

   }

   public PDFArray(PDFObject parent, List values) {
      super(parent);
      this.values = new ArrayList();
      this.values.addAll(values);
   }

   public PDFArray(Object... elements) {
      this((PDFObject)null, (Object[])elements);
   }

   public PDFArray(List elements) {
      this((PDFObject)null, (List)elements);
   }

   public PDFArray(PDFObject parent, Object[] values) {
      super(parent);
      this.values = new ArrayList();
      Collections.addAll(this.values, values);
   }

   public boolean contains(Object obj) {
      return this.values.contains(obj);
   }

   public int length() {
      return this.values.size();
   }

   public void set(int index, Object obj) {
      this.values.set(index, obj);
   }

   public void set(int index, double value) {
      this.values.set(index, value);
   }

   public Object get(int index) {
      return this.values.get(index);
   }

   public void add(Object obj) {
      if (obj instanceof PDFObject) {
         PDFObject pdfObj = (PDFObject)obj;
         if (!pdfObj.hasObjectNumber()) {
            pdfObj.setParent(this);
         }
      }

      this.values.add(obj);
   }

   public void add(double value) {
      this.values.add(value);
   }

   public void clear() {
      this.values.clear();
   }

   public int output(OutputStream stream) throws IOException {
      CountingOutputStream cout = new CountingOutputStream(stream);
      StringBuilder textBuffer = new StringBuilder(64);
      textBuffer.append('[');

      for(int i = 0; i < this.values.size(); ++i) {
         if (i > 0) {
            textBuffer.append(' ');
         }

         Object obj = this.values.get(i);
         this.formatObject(obj, cout, textBuffer);
      }

      textBuffer.append(']');
      PDFDocument.flushTextBuffer(textBuffer, cout);
      return cout.getCount();
   }

   public void getChildren(Set children) {
      List contents = new ArrayList();
      Iterator var3 = this.values.iterator();

      while(var3.hasNext()) {
         Object c = var3.next();
         if (!(c instanceof PDFReference)) {
            contents.add(c);
         }
      }

      PDFDictionary.getChildren(contents, children);
   }
}
