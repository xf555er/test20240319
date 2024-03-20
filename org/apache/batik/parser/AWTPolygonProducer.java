package org.apache.batik.parser;

import java.awt.Shape;
import java.io.IOException;
import java.io.Reader;

public class AWTPolygonProducer extends AWTPolylineProducer {
   public static Shape createShape(Reader r, int wr) throws IOException, ParseException {
      PointsParser p = new PointsParser();
      AWTPolygonProducer ph = new AWTPolygonProducer();
      ph.setWindingRule(wr);
      p.setPointsHandler(ph);
      p.parse(r);
      return ph.getShape();
   }

   public void endPoints() throws ParseException {
      this.path.closePath();
   }
}
