package org.apache.fop.render.pdf;

import java.util.Map;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.pdf.PDFPage;
import org.apache.fop.pdf.PDFStructElem;
import org.apache.fop.render.AbstractRenderingContext;

public class PDFRenderingContext extends AbstractRenderingContext {
   private PDFContentGenerator generator;
   private FontInfo fontInfo;
   private PDFPage page;
   private PDFLogicalStructureHandler.MarkedContentInfo mci;
   private Map pageNumbers;
   private PDFLogicalStructureHandler pdfLogicalStructureHandler;
   private PDFStructElem currentSessionStructElem;

   public PDFRenderingContext(FOUserAgent userAgent, PDFContentGenerator generator, PDFPage page, FontInfo fontInfo) {
      super(userAgent);
      this.generator = generator;
      this.page = page;
      this.fontInfo = fontInfo;
   }

   public String getMimeType() {
      return "application/pdf";
   }

   public PDFContentGenerator getGenerator() {
      return this.generator;
   }

   public PDFPage getPage() {
      return this.page;
   }

   public FontInfo getFontInfo() {
      return this.fontInfo;
   }

   void setMarkedContentInfo(PDFLogicalStructureHandler.MarkedContentInfo mci) {
      this.mci = mci;
   }

   PDFLogicalStructureHandler.MarkedContentInfo getMarkedContentInfo() {
      return this.mci;
   }

   public Map getPageNumbers() {
      return this.pageNumbers;
   }

   public void setPageNumbers(Map pageNumbers) {
      this.pageNumbers = pageNumbers;
   }

   public PDFLogicalStructureHandler getPdfLogicalStructureHandler() {
      return this.pdfLogicalStructureHandler;
   }

   public void setPdfLogicalStructureHandler(PDFLogicalStructureHandler pdfLogicalStructureHandler) {
      this.pdfLogicalStructureHandler = pdfLogicalStructureHandler;
   }

   public PDFStructElem getCurrentSessionStructElem() {
      return this.currentSessionStructElem;
   }

   public void setCurrentSessionStructElem(PDFStructElem currentSessionStructElem) {
      this.currentSessionStructElem = currentSessionStructElem;
   }
}
