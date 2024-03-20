package org.apache.fop.afp.parser;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MODCAParser {
   private static final Log LOG = LogFactory.getLog(MODCAParser.class);
   private static final int INTRODUCER_LENGTH = 8;
   public static final byte CARRIAGE_CONTROL_CHAR = 90;
   private DataInputStream din;

   public MODCAParser(InputStream in) {
      this.din = new DataInputStream(in);
   }

   public UnparsedStructuredField readNextStructuredField() throws IOException {
      do {
         if (this.din.available() == 0) {
            return null;
         }
      } while(this.din.readByte() != 90);

      byte[] introducerData = new byte[8];
      this.din.readFully(introducerData);
      UnparsedStructuredField.Introducer introducer = new UnparsedStructuredField.Introducer(introducerData);
      int dataLength = introducer.getLength() - 8;
      byte[] extData = null;
      if (introducer.isExtensionPresent()) {
         short extLength = false;
         short extLength = (short)(this.din.readByte() & 255);
         if (extLength > 0) {
            extData = new byte[extLength - 1];
            this.din.readFully(extData);
            dataLength -= extLength;
         }
      }

      byte[] data = new byte[dataLength];
      this.din.readFully(data);
      UnparsedStructuredField sf = new UnparsedStructuredField(introducer, data, extData);
      if (LOG.isTraceEnabled()) {
         LOG.trace(sf);
      }

      return sf;
   }
}
