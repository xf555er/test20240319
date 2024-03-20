package org.apache.batik.transcoder;

import java.io.OutputStream;
import java.io.Writer;
import org.w3c.dom.Document;
import org.xml.sax.XMLFilter;

public class TranscoderOutput {
   protected XMLFilter xmlFilter;
   protected OutputStream ostream;
   protected Writer writer;
   protected Document document;
   protected String uri;

   public TranscoderOutput() {
   }

   public TranscoderOutput(XMLFilter xmlFilter) {
      this.xmlFilter = xmlFilter;
   }

   public TranscoderOutput(OutputStream ostream) {
      this.ostream = ostream;
   }

   public TranscoderOutput(Writer writer) {
      this.writer = writer;
   }

   public TranscoderOutput(Document document) {
      this.document = document;
   }

   public TranscoderOutput(String uri) {
      this.uri = uri;
   }

   public void setXMLFilter(XMLFilter xmlFilter) {
      this.xmlFilter = xmlFilter;
   }

   public XMLFilter getXMLFilter() {
      return this.xmlFilter;
   }

   public void setOutputStream(OutputStream ostream) {
      this.ostream = ostream;
   }

   public OutputStream getOutputStream() {
      return this.ostream;
   }

   public void setWriter(Writer writer) {
      this.writer = writer;
   }

   public Writer getWriter() {
      return this.writer;
   }

   public void setDocument(Document document) {
      this.document = document;
   }

   public Document getDocument() {
      return this.document;
   }

   public void setURI(String uri) {
      this.uri = uri;
   }

   public String getURI() {
      return this.uri;
   }
}
