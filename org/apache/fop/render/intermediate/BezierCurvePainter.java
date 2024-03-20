package org.apache.fop.render.intermediate;

import java.io.IOException;

public interface BezierCurvePainter {
   void cubicBezierTo(int var1, int var2, int var3, int var4, int var5, int var6) throws IOException;
}
