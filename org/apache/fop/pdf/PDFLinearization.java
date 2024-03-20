package org.apache.fop.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.output.CountingOutputStream;

public class PDFLinearization {
   private PDFDocument doc;
   private Map pageObjsMap = new HashMap();
   private PDFDictionary linearDict;
   private HintTable hintTable;

   public PDFLinearization(PDFDocument doc) {
      this.doc = doc;
   }

   private Set assignNumbers() throws IOException {
      Set page1Children = this.getPage1Children();
      Iterator var7;
      PDFObject o;
      if (!this.doc.pageObjs.isEmpty()) {
         for(int i = 1; i < this.doc.pageObjs.size(); ++i) {
            PDFPage page = (PDFPage)this.doc.pageObjs.get(i);
            Set children = (Set)this.pageObjsMap.get(page);
            Iterator var5 = children.iterator();

            while(var5.hasNext()) {
               PDFObject c = (PDFObject)var5.next();
               if (!page1Children.contains(c) && c.hasObjectNumber()) {
                  c.getObjectNumber().getNumber();
               }
            }
         }

         var7 = this.doc.objects.iterator();

         label66:
         while(true) {
            do {
               if (!var7.hasNext()) {
                  var7 = this.doc.objects.iterator();

                  while(var7.hasNext()) {
                     o = (PDFObject)var7.next();
                     if (!page1Children.contains(o)) {
                        o.getObjectNumber().getNumber();
                     }
                  }
                  break label66;
               }

               o = (PDFObject)var7.next();
               if (o instanceof PDFDests || o instanceof PDFOutline) {
                  Iterator var9 = this.getChildren(o).iterator();

                  while(var9.hasNext()) {
                     PDFObject c = (PDFObject)var9.next();
                     c.getObjectNumber().getNumber();
                  }
               }
            } while(!(o instanceof PDFInfo) && !(o instanceof PDFPageLabels));

            o.getObjectNumber().getNumber();
         }
      }

      this.linearDict = new LinearPDFDictionary(this.doc);
      var7 = page1Children.iterator();

      while(var7.hasNext()) {
         o = (PDFObject)var7.next();
         o.getObjectNumber().getNumber();
      }

      this.sort(this.doc.objects);
      return page1Children;
   }

   private void sort(List objects) {
      Collections.sort(objects, new Comparator() {
         public int compare(PDFObject o1, PDFObject o2) {
            return Integer.compare(o1.getObjectNumber().getNumber(), o2.getObjectNumber().getNumber());
         }
      });
   }

   private Set getChildren(PDFObject o) {
      Set children = new LinkedHashSet();
      children.add(o);
      o.getChildren(children);
      return children;
   }

   public void outputPages(OutputStream stream) throws IOException {
      Collections.sort(this.doc.pageObjs, new Comparator() {
         public int compare(PDFPage o1, PDFPage o2) {
            return Integer.compare(o1.pageIndex, o2.pageIndex);
         }
      });
      this.doc.objects.addAll(this.doc.trailerObjects);
      this.doc.trailerObjects = null;
      if (this.doc.getStructureTreeElements() != null) {
         this.doc.objects.addAll(this.doc.getStructureTreeElements());
         this.doc.structureTreeElements = null;
      }

      for(int i = 0; i < this.doc.objects.size() * 2; ++i) {
         this.doc.indirectObjectOffsets.add(0L);
      }

      Set page1Children = this.assignNumbers();
      this.doc.streamIndirectObject(this.linearDict, new ByteArrayOutputStream());
      Iterator var3 = page1Children.iterator();

      while(var3.hasNext()) {
         PDFObject o = (PDFObject)var3.next();
         this.doc.objects.remove(o);
      }

      int sizeOfRest = this.doc.objects.size();
      ByteArrayOutputStream fakeHeaderTrailerStream = new ByteArrayOutputStream();
      long topTrailer = this.doc.position;
      this.doc.writeTrailer(fakeHeaderTrailerStream, sizeOfRest, page1Children.size() + 1, page1Children.size() + sizeOfRest + 1, Long.MAX_VALUE, 0L);
      PDFDocument var10000 = this.doc;
      var10000.position += (long)fakeHeaderTrailerStream.size();
      ByteArrayOutputStream pageStream = new ByteArrayOutputStream();
      this.writeObjects(page1Children, pageStream, sizeOfRest + 1);
      long trailerOffset = this.doc.position;
      ByteArrayOutputStream footerTrailerStream = new ByteArrayOutputStream();
      this.doc.writeTrailer(footerTrailerStream, 0, sizeOfRest, sizeOfRest, 0L, topTrailer);
      var10000 = this.doc;
      var10000.position += (long)footerTrailerStream.size();
      this.linearDict.put("/L", this.doc.position);
      PDFDocument.outputIndirectObject(this.linearDict, stream);
      CountingOutputStream realTrailer = new CountingOutputStream(stream);
      this.doc.writeTrailer(realTrailer, sizeOfRest, page1Children.size() + 1, page1Children.size() + sizeOfRest + 1, trailerOffset, 0L);
      writePadding(fakeHeaderTrailerStream.size() - realTrailer.getCount(), stream);
      Iterator var12 = page1Children.iterator();

      while(var12.hasNext()) {
         PDFObject o = (PDFObject)var12.next();
         PDFDocument.outputIndirectObject(o, stream);
         if (o instanceof HintTable) {
            break;
         }
      }

      stream.write(pageStream.toByteArray());
      stream.write(footerTrailerStream.toByteArray());
   }

   private Set getPage1Children() throws IOException {
      Set page1Children = new LinkedHashSet();
      if (!this.doc.pageObjs.isEmpty()) {
         PDFPage page1 = (PDFPage)this.doc.pageObjs.get(0);
         page1Children.add(this.doc.getRoot());
         this.hintTable = new HintTable(this.doc);
         page1Children.add(this.hintTable);
         page1Children.add(page1);
         page1.getChildren(page1Children);
         this.doc.objects.remove(this.doc.getPages());
         this.doc.objects.add(0, this.doc.getPages());
         this.pageObjsMap.put(page1, page1Children);

         for(int i = 1; i < this.doc.pageObjs.size(); ++i) {
            PDFPage page = (PDFPage)this.doc.pageObjs.get(i);
            this.pageObjsMap.put(page, this.getChildren(page));
         }
      }

      return page1Children;
   }

   private static void writePadding(int padding, OutputStream stream) throws IOException {
      for(int i = 0; i < padding; ++i) {
         stream.write(" ".getBytes("UTF-8"));
      }

   }

   private void writeObjects(Set children1, OutputStream pageStream, int sizeOfRest) throws IOException {
      this.writePage1(children1, pageStream);
      this.linearDict.put("/E", this.doc.position);
      Iterator var4 = this.doc.pageObjs.iterator();

      while(var4.hasNext()) {
         PDFPage page = (PDFPage)var4.next();
         if (page.pageIndex != 0) {
            this.writePage(page, pageStream);
         }
      }

      while(!this.doc.objects.isEmpty()) {
         PDFObject o = (PDFObject)this.doc.objects.remove(0);
         if (o instanceof PDFOutline) {
            this.writeObjectGroup("/O", this.getChildren(o), pageStream);
         } else if (o instanceof PDFDests) {
            this.writeObjectGroup("/E", this.getChildren(o), pageStream);
         } else if (o instanceof PDFInfo) {
            this.writeObjectGroup("/I", this.getChildren(o), pageStream);
         } else if (o instanceof PDFPageLabels) {
            this.writeObjectGroup("/L", this.getChildren(o), pageStream);
         } else if (o instanceof PDFStructTreeRoot) {
            this.writeObjectGroup("/C", this.getChildren(o), pageStream);
         } else {
            this.doc.streamIndirectObject(o, pageStream);
         }
      }

      this.linearDict.put("/T", this.doc.position + 8L + (long)String.valueOf(sizeOfRest).length());
   }

   private void writeObjectGroup(String name, Set objects, OutputStream pageStream) throws IOException {
      List children = new ArrayList(objects);
      this.sort(children);
      int[] values = (int[])this.hintTable.hintGroups.get(name);
      values[0] = ((PDFObject)children.iterator().next()).getObjectNumber().getNumber();
      values[1] = (int)this.doc.position;
      values[2] = children.size();
      Iterator var6 = children.iterator();

      while(var6.hasNext()) {
         PDFObject o = (PDFObject)var6.next();
         values[3] += this.doc.streamIndirectObject(o, pageStream);
         this.doc.objects.remove(o);
      }

   }

   private void writePage1(Set children1, OutputStream pageStream) throws IOException {
      this.hintTable.pageStartPos = (int)this.doc.position;
      OutputStream stream = new ByteArrayOutputStream();
      Set sharedChildren = this.getSharedObjects();
      int page1Len = 0;
      int objCount = 0;
      int sharedCount = 0;
      Iterator var8 = children1.iterator();

      while(var8.hasNext()) {
         PDFObject o = (PDFObject)var8.next();
         if (o instanceof HintTable) {
            PDFArray a = (PDFArray)this.linearDict.get("/H");
            a.set(0, (double)this.doc.position);
            this.doc.streamIndirectObject(o, (OutputStream)stream);
            a.set(1, (double)this.doc.position - (Double)a.get(0));
            stream = pageStream;
         } else {
            int len = this.doc.streamIndirectObject(o, (OutputStream)stream);
            if (o instanceof PDFStream && (Integer)this.hintTable.contentStreamLengths.get(0) == 0) {
               this.hintTable.contentStreamLengths.set(0, len);
            }

            if (!(o instanceof PDFRoot)) {
               page1Len += len;
               ++objCount;
            }

            if (sharedChildren.contains(o)) {
               this.hintTable.sharedLengths.set(sharedCount, len);
               ++sharedCount;
            }
         }
      }

      this.hintTable.pageLengths.set(0, page1Len);
      this.hintTable.objCount.set(0, objCount);
   }

   private Set getSharedObjects() {
      Set pageSharedChildren = this.getChildren((PDFObject)this.doc.pageObjs.get(0));

      for(int i = 0; i < pageSharedChildren.size(); ++i) {
         this.hintTable.sharedLengths.add(0);
      }

      return pageSharedChildren;
   }

   private void writePage(PDFPage page, OutputStream pageStream) throws IOException {
      Set children = (Set)this.pageObjsMap.get(page);
      int pageLen = 0;
      int objCount = 0;
      Iterator var6 = children.iterator();

      while(var6.hasNext()) {
         PDFObject c = (PDFObject)var6.next();
         if (this.doc.objects.contains(c)) {
            int len = this.doc.streamIndirectObject(c, pageStream);
            if (c instanceof PDFStream) {
               this.hintTable.contentStreamLengths.set(page.pageIndex, len);
            }

            pageLen += len;
            this.doc.objects.remove(c);
            ++objCount;
         }
      }

      this.hintTable.pageLengths.set(page.pageIndex, pageLen);
      this.hintTable.objCount.set(page.pageIndex, objCount);
   }

   static class LinearPDFDictionary extends PDFDictionary {
      private int lastsize = -1;

      public LinearPDFDictionary(PDFDocument doc) {
         this.put("Linearized", 1);
         this.put("/L", 0);
         PDFArray larray = new PDFArray();
         larray.add(0.0);
         larray.add(0.0);
         this.put("/H", larray);
         doc.assignObjectNumber(this);
         this.getObjectNumber().getNumber();
         this.put("/O", this.getObjectNumber().getNumber() + 3);
         this.put("/E", 0);
         this.put("/N", doc.pageObjs.size());
         this.put("/T", 0);
      }

      public int output(OutputStream stream) throws IOException {
         int size = super.output(stream);
         int padding = this.lastsize - size + 32;
         if (this.lastsize == -1) {
            padding = 32;
            this.lastsize = size;
         }

         PDFLinearization.writePadding(padding, stream);
         return size + padding;
      }
   }

   static class HintTable extends PDFStream {
      private List pages;
      int pageStartPos;
      List sharedLengths = new ArrayList();
      List pageLengths = new ArrayList();
      List contentStreamLengths = new ArrayList();
      List objCount = new ArrayList();
      Map hintGroups = new HashMap();

      public HintTable(PDFDocument doc) {
         super(false);
         doc.assignObjectNumber(this);
         doc.addObject(this);
         this.pages = doc.pageObjs;

         for(int i = 0; i < this.pages.size(); ++i) {
            this.pageLengths.add(0);
            this.contentStreamLengths.add(0);
            this.objCount.add(0);
         }

         this.hintGroups.put("/C", new int[4]);
         this.hintGroups.put("/L", new int[4]);
         this.hintGroups.put("/I", new int[4]);
         this.hintGroups.put("/E", new int[4]);
         this.hintGroups.put("/O", new int[4]);
         this.hintGroups.put("/V", new int[4]);
      }

      public PDFFilterList getFilterList() {
         return new PDFFilterList(this.getDocument().isEncryptionActive());
      }

      protected void outputRawStreamData(OutputStream os) throws IOException {
         CountingOutputStream bos = new CountingOutputStream(os);
         this.writeULong(1, bos);
         this.writeULong(this.pageStartPos, bos);
         this.writeCard16(32, bos);
         this.writeULong(0, bos);
         this.writeCard16(32, bos);
         this.writeULong(0, bos);
         this.writeCard16(0, bos);
         this.writeULong(0, bos);
         this.writeCard16(32, bos);
         this.writeCard16(0, bos);
         this.writeCard16(0, bos);
         this.writeCard16(0, bos);
         this.writeCard16(4, bos);
         Iterator var3 = this.pages.iterator();

         PDFPage page;
         while(var3.hasNext()) {
            page = (PDFPage)var3.next();
            this.writeULong((Integer)this.objCount.get(page.pageIndex) - 1, bos);
         }

         var3 = this.pages.iterator();

         while(var3.hasNext()) {
            page = (PDFPage)var3.next();
            this.writeULong((Integer)this.pageLengths.get(page.pageIndex), bos);
         }

         var3 = this.pages.iterator();

         while(var3.hasNext()) {
            page = (PDFPage)var3.next();
            this.writeULong((Integer)this.contentStreamLengths.get(page.pageIndex), bos);
         }

         this.writeSharedTable(bos);
         var3 = this.hintGroups.entrySet().iterator();

         while(var3.hasNext()) {
            Map.Entry group = (Map.Entry)var3.next();
            this.put((String)group.getKey(), bos.getCount());
            int[] var5 = (int[])group.getValue();
            int var6 = var5.length;

            for(int var7 = 0; var7 < var6; ++var7) {
               int i = var5[var7];
               this.writeULong(i, bos);
            }

            if (((String)group.getKey()).equals("/C")) {
               this.writeULong(0, bos);
               this.writeCard16(0, bos);
            }
         }

      }

      private void writeSharedTable(CountingOutputStream bos) throws IOException {
         this.put("/S", bos.getCount());
         this.writeULong(0, bos);
         this.writeULong(0, bos);
         this.writeULong(this.sharedLengths.size(), bos);
         this.writeULong(this.sharedLengths.size(), bos);
         this.writeCard16(0, bos);
         this.writeULong(0, bos);
         this.writeCard16(32, bos);
         Iterator var2 = this.sharedLengths.iterator();

         while(var2.hasNext()) {
            int i = (Integer)var2.next();
            this.writeULong(i, bos);
         }

         this.writeULong(0, bos);
      }

      private void writeCard16(int s, OutputStream bos) throws IOException {
         byte b1 = (byte)(s >> 8 & 255);
         byte b2 = (byte)(s & 255);
         bos.write(b1);
         bos.write(b2);
      }

      private void writeULong(int s, OutputStream bos) throws IOException {
         byte b1 = (byte)(s >> 24 & 255);
         byte b2 = (byte)(s >> 16 & 255);
         byte b3 = (byte)(s >> 8 & 255);
         byte b4 = (byte)(s & 255);
         bos.write(b1);
         bos.write(b2);
         bos.write(b3);
         bos.write(b4);
      }
   }
}
