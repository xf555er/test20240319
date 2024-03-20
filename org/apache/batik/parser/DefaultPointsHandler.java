package org.apache.batik.parser;

public class DefaultPointsHandler implements PointsHandler {
   public static final DefaultPointsHandler INSTANCE = new DefaultPointsHandler();

   protected DefaultPointsHandler() {
   }

   public void startPoints() throws ParseException {
   }

   public void point(float x, float y) throws ParseException {
   }

   public void endPoints() throws ParseException {
   }
}
