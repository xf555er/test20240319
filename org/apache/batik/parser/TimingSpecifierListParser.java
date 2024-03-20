package org.apache.batik.parser;

import java.io.IOException;

public class TimingSpecifierListParser extends TimingSpecifierParser {
   public TimingSpecifierListParser(boolean useSVG11AccessKeys, boolean useSVG12AccessKeys) {
      super(useSVG11AccessKeys, useSVG12AccessKeys);
      this.timingSpecifierHandler = DefaultTimingSpecifierListHandler.INSTANCE;
   }

   public void setTimingSpecifierListHandler(TimingSpecifierListHandler handler) {
      this.timingSpecifierHandler = handler;
   }

   public TimingSpecifierListHandler getTimingSpecifierListHandler() {
      return (TimingSpecifierListHandler)this.timingSpecifierHandler;
   }

   protected void doParse() throws ParseException, IOException {
      this.current = this.reader.read();
      ((TimingSpecifierListHandler)this.timingSpecifierHandler).startTimingSpecifierList();
      this.skipSpaces();
      if (this.current != -1) {
         while(true) {
            Object[] spec = this.parseTimingSpecifier();
            this.handleTimingSpecifier(spec);
            this.skipSpaces();
            if (this.current == -1) {
               break;
            }

            if (this.current == 59) {
               this.current = this.reader.read();
            } else {
               this.reportUnexpectedCharacterError(this.current);
            }
         }
      }

      this.skipSpaces();
      if (this.current != -1) {
         this.reportUnexpectedCharacterError(this.current);
      }

      ((TimingSpecifierListHandler)this.timingSpecifierHandler).endTimingSpecifierList();
   }
}
