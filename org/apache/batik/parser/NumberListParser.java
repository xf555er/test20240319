package org.apache.batik.parser;

import java.io.IOException;

public class NumberListParser extends NumberParser {
   protected NumberListHandler numberListHandler;

   public NumberListParser() {
      this.numberListHandler = DefaultNumberListHandler.INSTANCE;
   }

   public void setNumberListHandler(NumberListHandler handler) {
      this.numberListHandler = handler;
   }

   public NumberListHandler getNumberListHandler() {
      return this.numberListHandler;
   }

   protected void doParse() throws ParseException, IOException {
      this.numberListHandler.startNumberList();
      this.current = this.reader.read();
      this.skipSpaces();

      try {
         do {
            this.numberListHandler.startNumber();
            float f = this.parseFloat();
            this.numberListHandler.numberValue(f);
            this.numberListHandler.endNumber();
            this.skipCommaSpaces();
         } while(this.current != -1);
      } catch (NumberFormatException var2) {
         this.reportUnexpectedCharacterError(this.current);
      }

      this.numberListHandler.endNumberList();
   }
}
