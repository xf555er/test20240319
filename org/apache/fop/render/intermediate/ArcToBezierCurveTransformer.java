package org.apache.fop.render.intermediate;

import java.io.IOException;

public class ArcToBezierCurveTransformer {
   private final BezierCurvePainter bezierCurvePainter;

   public ArcToBezierCurveTransformer(BezierCurvePainter bezierCurvePainter) {
      this.bezierCurvePainter = bezierCurvePainter;
   }

   public void arcTo(double startAngle, double endAngle, int cx, int cy, int width, int height) throws IOException {
      double etaStart = Math.atan(Math.tan(startAngle) * (double)width / (double)height) + this.quadrant(startAngle);
      double etaEnd = Math.atan(Math.tan(endAngle) * (double)width / (double)height) + this.quadrant(endAngle);
      double sinStart = Math.sin(etaStart);
      double cosStart = Math.cos(etaStart);
      double sinEnd = Math.sin(etaEnd);
      double cosEnd = Math.cos(etaEnd);
      double p0x = (double)cx + cosStart * (double)width;
      double p0y = (double)cy + sinStart * (double)height;
      double p3x = (double)cx + cosEnd * (double)width;
      double p3y = (double)cy + sinEnd * (double)height;
      double etaDiff = Math.abs(etaEnd - etaStart);
      double tan = Math.tan(etaDiff / 2.0);
      double alpha = Math.sin(etaDiff) * (Math.sqrt(4.0 + 3.0 * tan * tan) - 1.0) / 3.0;
      int order = etaEnd > etaStart ? 1 : -1;
      double p1x = p0x - alpha * sinStart * (double)width * (double)order;
      double p1y = p0y + alpha * cosStart * (double)height * (double)order;
      double p2x = p3x + alpha * sinEnd * (double)width * (double)order;
      double p2y = p3y - alpha * cosEnd * (double)height * (double)order;
      this.bezierCurvePainter.cubicBezierTo((int)p1x, (int)p1y, (int)p2x, (int)p2y, (int)p3x, (int)p3y);
   }

   private double quadrant(double angle) {
      if (angle <= Math.PI) {
         return angle <= 1.5707963267948966 ? 0.0 : Math.PI;
      } else {
         return angle > 4.71238898038469 ? 6.283185307179586 : Math.PI;
      }
   }
}
