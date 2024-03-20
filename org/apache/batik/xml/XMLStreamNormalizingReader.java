package org.apache.batik.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.io.Reader;
import org.apache.batik.util.io.StreamNormalizingReader;
import org.apache.batik.util.io.UTF16Decoder;

public class XMLStreamNormalizingReader extends StreamNormalizingReader {
   public XMLStreamNormalizingReader(InputStream is, String encod) throws IOException {
      PushbackInputStream pbis = new PushbackInputStream(is, 128);
      byte[] buf = new byte[4];
      int len = pbis.read(buf);
      if (len > 0) {
         pbis.unread(buf, 0, len);
      }

      if (len == 4) {
         Reader r;
         String enc;
         label49:
         switch (buf[0] & 255) {
            case 0:
               if (buf[1] == 60 && buf[2] == 0 && buf[3] == 63) {
                  this.charDecoder = new UTF16Decoder(pbis, true);
                  return;
               }
               break;
            case 60:
               switch (buf[1] & 255) {
                  case 0:
                     if (buf[2] == 63 && buf[3] == 0) {
                        this.charDecoder = new UTF16Decoder(pbis, false);
                        return;
                     }
                     break label49;
                  case 63:
                     if (buf[2] == 120 && buf[3] == 109) {
                        r = XMLUtilities.createXMLDeclarationReader(pbis, "UTF8");
                        enc = XMLUtilities.getXMLDeclarationEncoding(r, "UTF-8");
                        this.charDecoder = this.createCharDecoder(pbis, enc);
                        return;
                     }
                  default:
                     break label49;
               }
            case 76:
               if (buf[1] == 111 && (buf[2] & 255) == 167 && (buf[3] & 255) == 148) {
                  r = XMLUtilities.createXMLDeclarationReader(pbis, "CP037");
                  enc = XMLUtilities.getXMLDeclarationEncoding(r, "EBCDIC-CP-US");
                  this.charDecoder = this.createCharDecoder(pbis, enc);
                  return;
               }
               break;
            case 254:
               if ((buf[1] & 255) == 255) {
                  this.charDecoder = this.createCharDecoder(pbis, "UTF-16");
                  return;
               }
               break;
            case 255:
               if ((buf[1] & 255) == 254) {
                  this.charDecoder = this.createCharDecoder(pbis, "UTF-16");
                  return;
               }
         }
      }

      encod = encod == null ? "UTF-8" : encod;
      this.charDecoder = this.createCharDecoder(pbis, encod);
   }
}
