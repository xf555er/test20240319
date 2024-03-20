package org.apache.batik.parser;

import java.io.IOException;

public class ClockParser extends TimingParser {
   protected ClockHandler clockHandler;
   protected boolean parseOffset;

   public ClockParser(boolean parseOffset) {
      super(false, false);
      this.parseOffset = parseOffset;
   }

   public void setClockHandler(ClockHandler handler) {
      this.clockHandler = handler;
   }

   public ClockHandler getClockHandler() {
      return this.clockHandler;
   }

   protected void doParse() throws ParseException, IOException {
      this.current = this.reader.read();
      float clockValue = this.parseOffset ? this.parseOffset() : this.parseClockValue();
      if (this.current != -1) {
         this.reportError("end.of.stream.expected", new Object[]{this.current});
      }

      if (this.clockHandler != null) {
         this.clockHandler.clockValue(clockValue);
      }

   }
}
