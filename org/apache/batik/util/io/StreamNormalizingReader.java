package org.apache.batik.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import org.apache.batik.util.EncodingUtilities;

public class StreamNormalizingReader extends NormalizingReader {
   protected CharDecoder charDecoder;
   protected int nextChar;
   protected int line;
   protected int column;
   protected static final Map charDecoderFactories = new HashMap(11);

   public StreamNormalizingReader(InputStream is) throws IOException {
      this(is, (String)null);
   }

   public StreamNormalizingReader(InputStream is, String enc) throws IOException {
      this.nextChar = -1;
      this.line = 1;
      if (enc == null) {
         enc = "ISO-8859-1";
      }

      this.charDecoder = this.createCharDecoder(is, enc);
   }

   public StreamNormalizingReader(Reader r) throws IOException {
      this.nextChar = -1;
      this.line = 1;
      this.charDecoder = new GenericDecoder(r);
   }

   protected StreamNormalizingReader() {
      this.nextChar = -1;
      this.line = 1;
   }

   public int read() throws IOException {
      int result = this.nextChar;
      if (result != -1) {
         this.nextChar = -1;
         if (result == 13) {
            this.column = 0;
            ++this.line;
         } else {
            ++this.column;
         }

         return result;
      } else {
         result = this.charDecoder.readChar();
         switch (result) {
            case 10:
               this.column = 0;
               ++this.line;
            default:
               return result;
            case 13:
               this.column = 0;
               ++this.line;
               int c = this.charDecoder.readChar();
               if (c == 10) {
                  return 10;
               } else {
                  this.nextChar = c;
                  return 10;
               }
         }
      }
   }

   public int getLine() {
      return this.line;
   }

   public int getColumn() {
      return this.column;
   }

   public void close() throws IOException {
      this.charDecoder.dispose();
      this.charDecoder = null;
   }

   protected CharDecoder createCharDecoder(InputStream is, String enc) throws IOException {
      CharDecoderFactory cdf = (CharDecoderFactory)charDecoderFactories.get(enc.toUpperCase());
      if (cdf != null) {
         return cdf.createCharDecoder(is);
      } else {
         String e = EncodingUtilities.javaEncoding(enc);
         if (e == null) {
            e = enc;
         }

         return new GenericDecoder(is, e);
      }
   }

   static {
      CharDecoderFactory cdf = new ASCIIDecoderFactory();
      charDecoderFactories.put("ASCII", cdf);
      charDecoderFactories.put("US-ASCII", cdf);
      charDecoderFactories.put("ISO-8859-1", new ISO_8859_1DecoderFactory());
      charDecoderFactories.put("UTF-8", new UTF8DecoderFactory());
      charDecoderFactories.put("UTF-16", new UTF16DecoderFactory());
   }

   protected static class UTF16DecoderFactory implements CharDecoderFactory {
      public CharDecoder createCharDecoder(InputStream is) throws IOException {
         return new UTF16Decoder(is);
      }
   }

   protected static class UTF8DecoderFactory implements CharDecoderFactory {
      public CharDecoder createCharDecoder(InputStream is) throws IOException {
         return new UTF8Decoder(is);
      }
   }

   protected static class ISO_8859_1DecoderFactory implements CharDecoderFactory {
      public CharDecoder createCharDecoder(InputStream is) throws IOException {
         return new ISO_8859_1Decoder(is);
      }
   }

   protected static class ASCIIDecoderFactory implements CharDecoderFactory {
      public CharDecoder createCharDecoder(InputStream is) throws IOException {
         return new ASCIIDecoder(is);
      }
   }

   protected interface CharDecoderFactory {
      CharDecoder createCharDecoder(InputStream var1) throws IOException;
   }
}
