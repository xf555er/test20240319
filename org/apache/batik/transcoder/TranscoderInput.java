package org.apache.batik.transcoder;

import java.io.InputStream;
import java.io.Reader;
import org.w3c.dom.Document;
import org.xml.sax.XMLReader;

public class TranscoderInput {
   protected XMLReader xmlReader;
   protected InputStream istream;
   protected Reader reader;
   protected Document document;
   protected String uri;

   public TranscoderInput() {
   }

   public TranscoderInput(XMLReader xmlReader) {
      this.xmlReader = xmlReader;
   }

   public TranscoderInput(InputStream istream) {
      this.istream = istream;
   }

   public TranscoderInput(Reader reader) {
      this.reader = reader;
   }

   public TranscoderInput(Document document) {
      this.document = document;
   }

   public TranscoderInput(String uri) {
      this.uri = uri;
   }

   public void setXMLReader(XMLReader xmlReader) {
      this.xmlReader = xmlReader;
   }

   public XMLReader getXMLReader() {
      return this.xmlReader;
   }

   public void setInputStream(InputStream istream) {
      this.istream = istream;
   }

   public InputStream getInputStream() {
      return this.istream;
   }

   public void setReader(Reader reader) {
      this.reader = reader;
   }

   public Reader getReader() {
      return this.reader;
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
