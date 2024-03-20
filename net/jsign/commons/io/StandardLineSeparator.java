package net.jsign.commons.io;

import java.util.Objects;

public enum StandardLineSeparator {
   CR("\r"),
   CRLF("\r\n"),
   LF("\n");

   private final String lineSeparator;

   private StandardLineSeparator(String lineSeparator) {
      this.lineSeparator = (String)Objects.requireNonNull(lineSeparator, "lineSeparator");
   }

   public String getString() {
      return this.lineSeparator;
   }
}
