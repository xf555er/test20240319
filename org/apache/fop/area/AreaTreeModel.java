package org.apache.fop.area;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

public class AreaTreeModel {
   private List pageSequenceList = new ArrayList();
   private int currentPageIndex;
   protected PageSequence currentPageSequence;
   protected static final Log log = LogFactory.getLog(AreaTreeModel.class);

   public void startPageSequence(PageSequence pageSequence) {
      if (pageSequence == null) {
         throw new NullPointerException("pageSequence must not be null");
      } else {
         if (this.currentPageSequence != null) {
            this.currentPageIndex += this.currentPageSequence.getPageCount();
         }

         this.currentPageSequence = pageSequence;
         this.pageSequenceList.add(this.currentPageSequence);
      }
   }

   public void addPage(PageViewport page) {
      this.currentPageSequence.addPage(page);
      page.setPageIndex(this.currentPageIndex + this.currentPageSequence.getPageCount() - 1);
      page.setPageSequence(this.currentPageSequence);
   }

   public void handleOffDocumentItem(OffDocumentItem ext) {
   }

   public void endDocument() throws SAXException {
   }

   public PageSequence getCurrentPageSequence() {
      return this.currentPageSequence;
   }

   public int getPageSequenceCount() {
      return this.pageSequenceList.size();
   }

   public int getPageCount(int seq) {
      return ((PageSequence)this.pageSequenceList.get(seq - 1)).getPageCount();
   }

   public PageViewport getPage(int seq, int count) {
      return ((PageSequence)this.pageSequenceList.get(seq - 1)).getPage(count);
   }

   public void setDocumentLocale(Locale locale) {
   }
}
