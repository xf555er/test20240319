package org.apache.commons.io;

import java.nio.charset.Charset;
import java.util.Objects;

public enum StandardLineSeparator {
   CR("\r"),
   CRLF("\r\n"),
   LF("\n");

   private final String lineSeparator;

   private StandardLineSeparator(String lineSeparator) {
      this.lineSeparator = (String)Objects.requireNonNull(lineSeparator, "lineSeparator");
   }

   public byte[] getBytes(Charset charset) {
      return this.lineSeparator.getBytes(charset);
   }

   public String getString() {
      return this.lineSeparator;
   }
}
