package org.apache.fop.render.intermediate;

import java.awt.Color;
import java.awt.Point;
import java.io.IOException;
import org.apache.fop.traits.RuleStyle;

public interface GraphicsPainter {
   void drawBorderLine(int var1, int var2, int var3, int var4, boolean var5, boolean var6, int var7, Color var8) throws IOException;

   void drawLine(Point var1, Point var2, int var3, Color var4, RuleStyle var5) throws IOException;

   void moveTo(int var1, int var2) throws IOException;

   void lineTo(int var1, int var2) throws IOException;

   void arcTo(double var1, double var3, int var5, int var6, int var7, int var8) throws IOException;

   void rotateCoordinates(double var1) throws IOException;

   void translateCoordinates(int var1, int var2) throws IOException;

   void scaleCoordinates(float var1, float var2) throws IOException;

   void closePath() throws IOException;

   void clip() throws IOException;

   void saveGraphicsState() throws IOException;

   void restoreGraphicsState() throws IOException;
}
