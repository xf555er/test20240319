package org.apache.batik.transcoder.svg2svg;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.transcoder.AbstractTranscoder;
import org.apache.batik.transcoder.ErrorHandler;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.keys.BooleanKey;
import org.apache.batik.transcoder.keys.IntegerKey;
import org.apache.batik.transcoder.keys.StringKey;
import org.w3c.dom.Document;

public class SVGTranscoder extends AbstractTranscoder {
   public static final ErrorHandler DEFAULT_ERROR_HANDLER = new ErrorHandler() {
      public void error(TranscoderException ex) throws TranscoderException {
         throw ex;
      }

      public void fatalError(TranscoderException ex) throws TranscoderException {
         throw ex;
      }

      public void warning(TranscoderException ex) throws TranscoderException {
      }
   };
   public static final TranscodingHints.Key KEY_NEWLINE = new NewlineKey();
   public static final NewlineValue VALUE_NEWLINE_CR = new NewlineValue("\r");
   public static final NewlineValue VALUE_NEWLINE_CR_LF = new NewlineValue("\r\n");
   public static final NewlineValue VALUE_NEWLINE_LF = new NewlineValue("\n");
   public static final TranscodingHints.Key KEY_FORMAT = new BooleanKey();
   public static final Boolean VALUE_FORMAT_ON;
   public static final Boolean VALUE_FORMAT_OFF;
   public static final TranscodingHints.Key KEY_TABULATION_WIDTH;
   public static final TranscodingHints.Key KEY_DOCUMENT_WIDTH;
   public static final TranscodingHints.Key KEY_DOCTYPE;
   public static final DoctypeValue VALUE_DOCTYPE_CHANGE;
   public static final DoctypeValue VALUE_DOCTYPE_REMOVE;
   public static final DoctypeValue VALUE_DOCTYPE_KEEP_UNCHANGED;
   public static final TranscodingHints.Key KEY_PUBLIC_ID;
   public static final TranscodingHints.Key KEY_SYSTEM_ID;
   public static final TranscodingHints.Key KEY_XML_DECLARATION;

   public SVGTranscoder() {
      this.setErrorHandler(DEFAULT_ERROR_HANDLER);
   }

   public void transcode(TranscoderInput input, TranscoderOutput output) throws TranscoderException {
      Reader r = input.getReader();
      Writer w = output.getWriter();
      if (r == null) {
         Document d = input.getDocument();
         if (d == null) {
            throw new RuntimeException("Reader or Document expected");
         }

         StringWriter sw = new StringWriter(1024);

         try {
            DOMUtilities.writeDocument(d, sw);
         } catch (IOException var8) {
            throw new RuntimeException("IO:" + var8.getMessage());
         }

         r = new StringReader(sw.toString());
      }

      if (w == null) {
         throw new RuntimeException("Writer expected");
      } else {
         this.prettyPrint((Reader)r, w);
      }
   }

   protected void prettyPrint(Reader in, Writer out) throws TranscoderException {
      try {
         PrettyPrinter pp = new PrettyPrinter();
         NewlineValue nlv = (NewlineValue)this.hints.get(KEY_NEWLINE);
         if (nlv != null) {
            pp.setNewline(nlv.getValue());
         }

         Boolean b = (Boolean)this.hints.get(KEY_FORMAT);
         if (b != null) {
            pp.setFormat(b);
         }

         Integer i = (Integer)this.hints.get(KEY_TABULATION_WIDTH);
         if (i != null) {
            pp.setTabulationWidth(i);
         }

         i = (Integer)this.hints.get(KEY_DOCUMENT_WIDTH);
         if (i != null) {
            pp.setDocumentWidth(i);
         }

         DoctypeValue dtv = (DoctypeValue)this.hints.get(KEY_DOCTYPE);
         if (dtv != null) {
            pp.setDoctypeOption(dtv.getValue());
         }

         String s = (String)this.hints.get(KEY_PUBLIC_ID);
         if (s != null) {
            pp.setPublicId(s);
         }

         s = (String)this.hints.get(KEY_SYSTEM_ID);
         if (s != null) {
            pp.setSystemId(s);
         }

         s = (String)this.hints.get(KEY_XML_DECLARATION);
         if (s != null) {
            pp.setXMLDeclaration(s);
         }

         pp.print(in, out);
         out.flush();
      } catch (IOException var9) {
         this.getErrorHandler().fatalError(new TranscoderException(var9.getMessage()));
      }

   }

   static {
      VALUE_FORMAT_ON = Boolean.TRUE;
      VALUE_FORMAT_OFF = Boolean.FALSE;
      KEY_TABULATION_WIDTH = new IntegerKey();
      KEY_DOCUMENT_WIDTH = new IntegerKey();
      KEY_DOCTYPE = new DoctypeKey();
      VALUE_DOCTYPE_CHANGE = new DoctypeValue(0);
      VALUE_DOCTYPE_REMOVE = new DoctypeValue(1);
      VALUE_DOCTYPE_KEEP_UNCHANGED = new DoctypeValue(2);
      KEY_PUBLIC_ID = new StringKey();
      KEY_SYSTEM_ID = new StringKey();
      KEY_XML_DECLARATION = new StringKey();
   }

   protected static class DoctypeValue {
      final int value;

      protected DoctypeValue(int value) {
         this.value = value;
      }

      public int getValue() {
         return this.value;
      }
   }

   protected static class DoctypeKey extends TranscodingHints.Key {
      public boolean isCompatibleValue(Object v) {
         return v instanceof DoctypeValue;
      }
   }

   protected static class NewlineValue {
      protected final String value;

      protected NewlineValue(String val) {
         this.value = val;
      }

      public String getValue() {
         return this.value;
      }
   }

   protected static class NewlineKey extends TranscodingHints.Key {
      public boolean isCompatibleValue(Object v) {
         return v instanceof NewlineValue;
      }
   }
}
