package org.apache.batik.parser;

import java.io.IOException;

public class PointsParser extends NumberParser {
   protected PointsHandler pointsHandler;
   protected boolean eRead;

   public PointsParser() {
      this.pointsHandler = DefaultPointsHandler.INSTANCE;
   }

   public void setPointsHandler(PointsHandler handler) {
      this.pointsHandler = handler;
   }

   public PointsHandler getPointsHandler() {
      return this.pointsHandler;
   }

   protected void doParse() throws ParseException, IOException {
      this.pointsHandler.startPoints();
      this.current = this.reader.read();
      this.skipSpaces();

      while(this.current != -1) {
         float x = this.parseFloat();
         this.skipCommaSpaces();
         float y = this.parseFloat();
         this.pointsHandler.point(x, y);
         this.skipCommaSpaces();
      }

      this.pointsHandler.endPoints();
   }
}
