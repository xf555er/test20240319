package org.apache.batik.parser;

import java.io.IOException;
import java.util.Calendar;

public class TimingSpecifierParser extends TimingParser {
   protected TimingSpecifierHandler timingSpecifierHandler;

   public TimingSpecifierParser(boolean useSVG11AccessKeys, boolean useSVG12AccessKeys) {
      super(useSVG11AccessKeys, useSVG12AccessKeys);
      this.timingSpecifierHandler = DefaultTimingSpecifierHandler.INSTANCE;
   }

   public void setTimingSpecifierHandler(TimingSpecifierHandler handler) {
      this.timingSpecifierHandler = handler;
   }

   public TimingSpecifierHandler getTimingSpecifierHandler() {
      return this.timingSpecifierHandler;
   }

   protected void doParse() throws ParseException, IOException {
      this.current = this.reader.read();
      Object[] spec = this.parseTimingSpecifier();
      this.skipSpaces();
      if (this.current != -1) {
         this.reportError("end.of.stream.expected", new Object[]{this.current});
      }

      this.handleTimingSpecifier(spec);
   }

   protected void handleTimingSpecifier(Object[] spec) {
      int type = (Integer)spec[0];
      switch (type) {
         case 0:
            this.timingSpecifierHandler.offset((Float)spec[1]);
            break;
         case 1:
            this.timingSpecifierHandler.syncbase((Float)spec[1], (String)spec[2], (String)spec[3]);
            break;
         case 2:
            this.timingSpecifierHandler.eventbase((Float)spec[1], (String)spec[2], (String)spec[3]);
            break;
         case 3:
            float offset = (Float)spec[1];
            String syncbaseID = (String)spec[2];
            if (spec[3] == null) {
               this.timingSpecifierHandler.repeat(offset, syncbaseID);
            } else {
               this.timingSpecifierHandler.repeat(offset, syncbaseID, (Integer)spec[3]);
            }
            break;
         case 4:
            this.timingSpecifierHandler.accesskey((Float)spec[1], (Character)spec[2]);
            break;
         case 5:
            this.timingSpecifierHandler.accessKeySVG12((Float)spec[1], (String)spec[2]);
            break;
         case 6:
            this.timingSpecifierHandler.mediaMarker((String)spec[1], (String)spec[2]);
            break;
         case 7:
            this.timingSpecifierHandler.wallclock((Calendar)spec[1]);
            break;
         case 8:
            this.timingSpecifierHandler.indefinite();
      }

   }
}
