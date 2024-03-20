package org.apache.batik.util;

public abstract class AbstractParsedURLProtocolHandler implements ParsedURLProtocolHandler {
   protected String protocol;

   public AbstractParsedURLProtocolHandler(String protocol) {
      this.protocol = protocol;
   }

   public String getProtocolHandled() {
      return this.protocol;
   }
}
