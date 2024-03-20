package org.apache.fop.render.pdf.extensions;

import org.apache.fop.pdf.PDFText;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class PDFEmbeddedFileAttachment extends PDFExtensionAttachment {
   private static final long serialVersionUID = -1L;
   protected static final String ELEMENT = "embedded-file";
   private static final String ATT_NAME = "filename";
   private static final String ATT_SRC = "src";
   private static final String ATT_DESC = "description";
   private String filename;
   private String unicodeFilename;
   private String desc;
   private String src;

   public PDFEmbeddedFileAttachment() {
   }

   public PDFEmbeddedFileAttachment(String filename, String src, String desc) {
      this.setFilename(filename);
      this.src = src;
      this.desc = desc;
   }

   public String getFilename() {
      return this.filename;
   }

   public String getUnicodeFilename() {
      return this.unicodeFilename;
   }

   public void setFilename(String name) {
      if (!PDFText.toPDFString(name).equals(name)) {
         this.filename = "att" + name.hashCode();
      } else {
         this.filename = name;
      }

      this.unicodeFilename = name;
   }

   public String getDesc() {
      return this.desc;
   }

   public void setDesc(String desc) {
      this.desc = desc;
   }

   public String getSrc() {
      return this.src;
   }

   public void setSrc(String src) {
      this.src = src;
   }

   public String getCategory() {
      return "apache:fop:extensions:pdf";
   }

   public String toString() {
      return "PDFEmbeddedFile(name=" + this.getFilename() + ", " + this.getSrc() + ")";
   }

   protected String getElement() {
      return "embedded-file";
   }

   public void toSAX(ContentHandler handler) throws SAXException {
      AttributesImpl atts = new AttributesImpl();
      if (this.filename != null && this.filename.length() > 0) {
         atts.addAttribute("", "filename", "filename", "CDATA", this.filename);
      }

      if (this.src != null && this.src.length() > 0) {
         atts.addAttribute("", "src", "src", "CDATA", this.src);
      }

      if (this.desc != null && this.desc.length() > 0) {
         atts.addAttribute("", "description", "description", "CDATA", this.desc);
      }

      String element = this.getElement();
      handler.startElement("apache:fop:extensions:pdf", element, element, atts);
      handler.endElement("apache:fop:extensions:pdf", element, element);
   }
}
