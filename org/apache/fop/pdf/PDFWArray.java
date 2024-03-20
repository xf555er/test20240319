package org.apache.fop.pdf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PDFWArray {
   private List entries = new ArrayList();

   public PDFWArray() {
   }

   public PDFWArray(int[] metrics) {
      this.addEntry(0, metrics);
   }

   public void addEntry(int start, int[] metrics) {
      this.entries.add(new Entry(start, metrics));
   }

   public void addEntry(int first, int last, int width) {
      this.entries.add(new int[]{first, last, width});
   }

   public void addEntry(int first, int last, int width, int posX, int posY) {
      this.entries.add(new int[]{first, last, width, posX, posY});
   }

   public byte[] toPDF() {
      return PDFDocument.encode(this.toPDFString());
   }

   public String toPDFString() {
      StringBuffer p = new StringBuffer();
      p.append("[ ");
      int len = this.entries.size();
      Iterator var3 = this.entries.iterator();

      while(true) {
         while(var3.hasNext()) {
            Object entry = var3.next();
            if (entry instanceof int[]) {
               int[] line = (int[])((int[])entry);
               int[] var6 = line;
               int var7 = line.length;

               for(int var8 = 0; var8 < var7; ++var8) {
                  int aLine = var6[var8];
                  p.append(aLine);
                  p.append(" ");
               }
            } else {
               ((Entry)entry).fillInPDF(p);
            }
         }

         p.append("]");
         return p.toString();
      }
   }

   private static class Entry {
      private int start;
      private int[] metrics;

      public Entry(int s, int[] m) {
         this.start = s;
         this.metrics = m;
      }

      public void fillInPDF(StringBuffer p) {
         p.append(this.start);
         p.append(" [");
         int[] var2 = this.metrics;
         int var3 = var2.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            int metric = var2[var4];
            p.append(metric);
            p.append(" ");
         }

         p.append("] ");
      }
   }
}
