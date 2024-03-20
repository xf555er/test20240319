package org.apache.fop.pdf;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class PDFObject implements PDFWritable {
   protected static final Log log = LogFactory.getLog(PDFObject.class.getName());
   private boolean hasObjNum;
   private PDFObjectNumber objNum = new PDFObjectNumber();
   private int generation;
   private PDFDocument document;
   private PDFObject parent;

   public PDFObjectNumber getObjectNumber() {
      if (!this.hasObjNum) {
         throw new IllegalStateException("Object has no number assigned: " + this.toString());
      } else {
         return this.objNum;
      }
   }

   public PDFObject() {
   }

   public PDFObject(PDFObject parent) {
      this.setParent(parent);
   }

   public boolean hasObjectNumber() {
      return this.hasObjNum;
   }

   public void setObjectNumber(PDFDocument document) {
      this.objNum.setDocument(document);
      this.hasObjNum = true;
      PDFDocument doc = this.getDocument();
      this.setParent((PDFObject)null);
      this.setDocument(doc);
      if (log.isTraceEnabled()) {
         log.trace("Assigning " + this + " object number " + this.objNum);
      }

   }

   public void setObjectNumber(PDFObjectNumber objectNumber) {
      this.objNum = objectNumber;
      this.hasObjNum = true;
   }

   public void setObjectNumber(int objectNumber) {
      this.objNum = new PDFObjectNumber(objectNumber);
      this.hasObjNum = true;
   }

   public int getGeneration() {
      return this.generation;
   }

   public final PDFDocument getDocument() {
      if (this.document != null) {
         return this.document;
      } else {
         return this.getParent() != null ? this.getParent().getDocument() : null;
      }
   }

   public final PDFDocument getDocumentSafely() {
      PDFDocument doc = this.getDocument();
      if (doc == null) {
         throw new IllegalStateException("Parent PDFDocument is unavailable on " + this.getClass().getName());
      } else {
         return doc;
      }
   }

   public void setDocument(PDFDocument doc) {
      this.document = doc;
   }

   public PDFObject getParent() {
      return this.parent;
   }

   public void setParent(PDFObject parent) {
      this.parent = parent;
   }

   public String getObjectID() {
      return this.getObjectNumber() + " " + this.getGeneration() + " obj\n";
   }

   public String referencePDF() {
      if (!this.hasObjectNumber()) {
         throw new IllegalArgumentException("Cannot reference this object. It doesn't have an object number");
      } else {
         return this.makeReference().toString();
      }
   }

   public PDFReference makeReference() {
      return new PDFReference(this);
   }

   public int output(OutputStream stream) throws IOException {
      byte[] pdf = this.toPDF();
      stream.write(pdf);
      return pdf.length;
   }

   public void outputInline(OutputStream out, StringBuilder textBuffer) throws IOException {
      if (this.hasObjectNumber()) {
         textBuffer.append(this.referencePDF());
      } else {
         PDFDocument.flushTextBuffer(textBuffer, out);
         this.output(out);
      }

   }

   protected byte[] toPDF() {
      return encode(this.toPDFString());
   }

   protected String toPDFString() {
      throw new UnsupportedOperationException("Not implemented. Use output(OutputStream) instead.");
   }

   public static final byte[] encode(String text) {
      return PDFDocument.encode(text);
   }

   protected byte[] encodeText(String text) {
      if (this.getDocumentSafely().isEncryptionActive()) {
         byte[] buf = PDFText.toUTF16(text);
         return PDFText.escapeByteArray(this.getDocument().getEncryption().encrypt(buf, this));
      } else {
         return encode(PDFText.escapeText(text, false));
      }
   }

   protected byte[] encodeString(String string) {
      return this.encodeText(string);
   }

   protected void encodeBinaryToHexString(byte[] data, OutputStream out) throws IOException {
      out.write(60);
      if (this.getDocumentSafely().isEncryptionActive()) {
         data = this.getDocument().getEncryption().encrypt(data, this);
      }

      String hex = PDFText.toHex(data, false);
      byte[] encoded = hex.getBytes("US-ASCII");
      out.write(encoded);
      out.write(62);
   }

   protected void formatObject(Object obj, OutputStream out, StringBuilder textBuffer) throws IOException {
      if (obj == null) {
         textBuffer.append("null");
      } else if (obj instanceof PDFWritable) {
         ((PDFWritable)obj).outputInline(out, textBuffer);
      } else if (obj instanceof Number) {
         if (!(obj instanceof Double) && !(obj instanceof Float)) {
            textBuffer.append(obj.toString());
         } else {
            textBuffer.append(PDFNumber.doubleOut(((Number)obj).doubleValue()));
         }
      } else if (obj instanceof Boolean) {
         textBuffer.append(obj.toString());
      } else if (obj instanceof byte[]) {
         PDFDocument.flushTextBuffer(textBuffer, out);
         this.encodeBinaryToHexString((byte[])((byte[])obj), out);
      } else {
         PDFDocument.flushTextBuffer(textBuffer, out);
         out.write(this.encodeText(obj.toString()));
      }

   }

   protected boolean contentEquals(PDFObject o) {
      return this.equals(o);
   }

   public void getChildren(Set children) {
   }
}
