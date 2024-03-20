package org.apache.fop.render.ps;

import java.awt.Color;
import java.awt.Point;
import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.render.intermediate.ArcToBezierCurveTransformer;
import org.apache.fop.render.intermediate.BezierCurvePainter;
import org.apache.fop.render.intermediate.BorderPainter;
import org.apache.fop.render.intermediate.GraphicsPainter;
import org.apache.fop.traits.RuleStyle;
import org.apache.fop.util.ColorUtil;
import org.apache.xmlgraphics.ps.PSGenerator;

public class PSGraphicsPainter implements GraphicsPainter, BezierCurvePainter {
   private static Log log = LogFactory.getLog(PSGraphicsPainter.class);
   private final PSGenerator generator;
   private final ArcToBezierCurveTransformer arcToBezierCurveTransformer;

   public PSGraphicsPainter(PSGenerator generator) {
      this.generator = generator;
      this.arcToBezierCurveTransformer = new ArcToBezierCurveTransformer(this);
   }

   public void drawBorderLine(int x1, int y1, int x2, int y2, boolean horz, boolean startOrBefore, int style, Color col) throws IOException {
      drawBorderLine(this.generator, toPoints(x1), toPoints(y1), toPoints(x2), toPoints(y2), horz, startOrBefore, style, col);
   }

   private static void drawLine(PSGenerator gen, float startx, float starty, float endx, float endy) throws IOException {
      gen.writeln(gen.formatDouble((double)startx) + " " + gen.formatDouble((double)starty) + " " + gen.mapCommand("moveto") + " " + gen.formatDouble((double)endx) + " " + gen.formatDouble((double)endy) + " " + gen.mapCommand("lineto") + " " + gen.mapCommand("stroke") + " " + gen.mapCommand("newpath"));
   }

   public static void drawBorderLine(PSGenerator gen, float x1, float y1, float x2, float y2, boolean horz, boolean startOrBefore, int style, Color col) throws IOException {
      float w = x2 - x1;
      float h = y2 - y1;
      if (!(w < 0.0F) && !(h < 0.0F)) {
         float unit;
         Color c;
         float ym;
         float ym;
         switch (style) {
            case 31:
               gen.useColor(col);
               if (horz) {
                  unit = BorderPainter.dashWidthCalculator(w, h);
                  if (unit != 0.0F) {
                     gen.useDash("[" + unit + " " + 0.5F * unit + "] 0");
                  }

                  gen.useLineCap(0);
                  gen.useLineWidth((double)h);
                  ym = y1 + h / 2.0F;
                  drawLine(gen, x1, ym, x2, ym);
               } else {
                  unit = BorderPainter.dashWidthCalculator(h, w);
                  if (unit != 0.0F) {
                     gen.useDash("[" + unit + " " + 0.5F * unit + "] 0");
                  }

                  gen.useLineCap(0);
                  gen.useLineWidth((double)w);
                  ym = x1 + w / 2.0F;
                  drawLine(gen, ym, y1, ym, y2);
               }
               break;
            case 36:
               gen.useColor(col);
               gen.useLineCap(1);
               int rep;
               if (horz) {
                  unit = Math.abs(2.0F * h);
                  rep = (int)(w / unit);
                  if (rep % 2 == 0) {
                     ++rep;
                  }

                  unit = w / (float)rep;
                  gen.useDash("[0 " + unit + "] 0");
                  gen.useLineWidth((double)h);
                  ym = y1 + h / 2.0F;
                  drawLine(gen, x1, ym, x2, ym);
               } else {
                  unit = Math.abs(2.0F * w);
                  rep = (int)(h / unit);
                  if (rep % 2 == 0) {
                     ++rep;
                  }

                  unit = h / (float)rep;
                  gen.useDash("[0 " + unit + "] 0");
                  gen.useLineWidth((double)w);
                  ym = x1 + w / 2.0F;
                  drawLine(gen, ym, y1, ym, y2);
               }
               break;
            case 37:
               gen.useColor(col);
               gen.useDash((String)null);
               if (horz) {
                  unit = h / 3.0F;
                  gen.useLineWidth((double)unit);
                  ym = y1 + unit / 2.0F;
                  ym = ym + unit + unit;
                  drawLine(gen, x1, ym, x2, ym);
                  drawLine(gen, x1, ym, x2, ym);
               } else {
                  unit = w / 3.0F;
                  gen.useLineWidth((double)unit);
                  ym = x1 + unit / 2.0F;
                  ym = ym + unit + unit;
                  drawLine(gen, ym, y1, ym, y2);
                  drawLine(gen, ym, y1, ym, y2);
               }
               break;
            case 55:
            case 119:
               unit = style == 55 ? 0.4F : -0.4F;
               gen.useDash((String)null);
               float h3;
               float ym1;
               Color lowercol;
               if (horz) {
                  c = ColorUtil.lightenColor(col, -unit);
                  lowercol = ColorUtil.lightenColor(col, unit);
                  h3 = h / 3.0F;
                  gen.useLineWidth((double)h3);
                  ym1 = y1 + h3 / 2.0F;
                  gen.useColor(c);
                  drawLine(gen, x1, ym1, x2, ym1);
                  gen.useColor(col);
                  drawLine(gen, x1, ym1 + h3, x2, ym1 + h3);
                  gen.useColor(lowercol);
                  drawLine(gen, x1, ym1 + h3 + h3, x2, ym1 + h3 + h3);
               } else {
                  c = ColorUtil.lightenColor(col, -unit);
                  lowercol = ColorUtil.lightenColor(col, unit);
                  h3 = w / 3.0F;
                  gen.useLineWidth((double)h3);
                  ym1 = x1 + h3 / 2.0F;
                  gen.useColor(c);
                  drawLine(gen, ym1, y1, ym1, y2);
                  gen.useColor(col);
                  drawLine(gen, ym1 + h3, y1, ym1 + h3, y2);
                  gen.useColor(lowercol);
                  drawLine(gen, ym1 + h3 + h3, y1, ym1 + h3 + h3, y2);
               }
            case 57:
               break;
            case 67:
            case 101:
               unit = style == 101 ? 0.4F : -0.4F;
               gen.useDash((String)null);
               if (horz) {
                  c = ColorUtil.lightenColor(col, (float)(startOrBefore ? 1 : -1) * unit);
                  gen.useLineWidth((double)h);
                  ym = y1 + h / 2.0F;
                  gen.useColor(c);
                  drawLine(gen, x1, ym, x2, ym);
               } else {
                  c = ColorUtil.lightenColor(col, (float)(startOrBefore ? 1 : -1) * unit);
                  gen.useLineWidth((double)w);
                  ym = x1 + w / 2.0F;
                  gen.useColor(c);
                  drawLine(gen, ym, y1, ym, y2);
               }
               break;
            default:
               gen.useColor(col);
               gen.useDash((String)null);
               gen.useLineCap(0);
               if (horz) {
                  gen.useLineWidth((double)h);
                  ym = y1 + h / 2.0F;
                  drawLine(gen, x1, ym, x2, ym);
               } else {
                  gen.useLineWidth((double)w);
                  ym = x1 + w / 2.0F;
                  drawLine(gen, ym, y1, ym, y2);
               }
         }

      } else {
         log.error("Negative extent received. Border won't be painted.");
      }
   }

   public void drawLine(Point start, Point end, int width, Color color, RuleStyle style) throws IOException {
      if (start.y != end.y) {
         throw new UnsupportedOperationException("Can only deal with horizontal lines right now");
      } else {
         this.saveGraphicsState();
         int half = width / 2;
         int starty = start.y - half;
         switch (style.getEnumValue()) {
            case 31:
            case 37:
            case 133:
               this.drawBorderLine(start.x, starty, end.x, starty + width, true, true, style.getEnumValue(), color);
               break;
            case 36:
               this.clipRect(start.x, starty, end.x - start.x, width);
               this.generator.concatMatrix(1.0, 0.0, 0.0, 1.0, (double)toPoints(half), 0.0);
               this.drawBorderLine(start.x, starty, end.x, starty + width, true, true, style.getEnumValue(), color);
               break;
            case 55:
            case 119:
               this.generator.useColor(ColorUtil.lightenColor(color, 0.6F));
               this.moveTo(start.x, starty);
               this.lineTo(end.x, starty);
               this.lineTo(end.x, starty + 2 * half);
               this.lineTo(start.x, starty + 2 * half);
               this.closePath();
               this.generator.write(" " + this.generator.mapCommand("fill"));
               this.generator.writeln(" " + this.generator.mapCommand("newpath"));
               this.generator.useColor(color);
               if (style == RuleStyle.GROOVE) {
                  this.moveTo(start.x, starty);
                  this.lineTo(end.x, starty);
                  this.lineTo(end.x, starty + half);
                  this.lineTo(start.x + half, starty + half);
                  this.lineTo(start.x, starty + 2 * half);
               } else {
                  this.moveTo(end.x, starty);
                  this.lineTo(end.x, starty + 2 * half);
                  this.lineTo(start.x, starty + 2 * half);
                  this.lineTo(start.x, starty + half);
                  this.lineTo(end.x - half, starty + half);
               }

               this.closePath();
               this.generator.write(" " + this.generator.mapCommand("fill"));
               this.generator.writeln(" " + this.generator.mapCommand("newpath"));
               break;
            default:
               throw new UnsupportedOperationException("rule style not supported");
         }

         this.restoreGraphicsState();
      }
   }

   private static float toPoints(int mpt) {
      return (float)mpt / 1000.0F;
   }

   public void moveTo(int x, int y) throws IOException {
      this.generator.writeln(this.generator.formatDouble((double)toPoints(x)) + " " + this.generator.formatDouble((double)toPoints(y)) + " " + this.generator.mapCommand("moveto"));
   }

   public void lineTo(int x, int y) throws IOException {
      this.generator.writeln(this.generator.formatDouble((double)toPoints(x)) + " " + this.generator.formatDouble((double)toPoints(y)) + " " + this.generator.mapCommand("lineto"));
   }

   public void arcTo(double startAngle, double endAngle, int cx, int cy, int width, int height) throws IOException {
      this.arcToBezierCurveTransformer.arcTo(startAngle, endAngle, cx, cy, width, height);
   }

   public void closePath() throws IOException {
      this.generator.writeln("cp");
   }

   private void clipRect(int x, int y, int width, int height) throws IOException {
      this.generator.defineRect((double)toPoints(x), (double)toPoints(y), (double)toPoints(width), (double)toPoints(height));
      this.clip();
   }

   public void clip() throws IOException {
      this.generator.writeln(this.generator.mapCommand("clip") + " " + this.generator.mapCommand("newpath"));
   }

   public void saveGraphicsState() throws IOException {
      this.generator.saveGraphicsState();
   }

   public void restoreGraphicsState() throws IOException {
      this.generator.restoreGraphicsState();
   }

   public void rotateCoordinates(double angle) throws IOException {
      StringBuffer sb = (new StringBuffer()).append(this.generator.formatDouble(angle * 180.0 / Math.PI)).append("  rotate ");
      this.generator.writeln(sb.toString());
   }

   public void translateCoordinates(int xTranslate, int yTranslate) throws IOException {
      StringBuffer sb = (new StringBuffer()).append(this.generator.formatDouble((double)toPoints(xTranslate))).append(" ").append(this.generator.formatDouble((double)toPoints(yTranslate))).append("  translate ");
      this.generator.writeln(sb.toString());
   }

   public void scaleCoordinates(float xScale, float yScale) throws IOException {
      StringBuffer sb = (new StringBuffer()).append(this.generator.formatDouble((double)xScale)).append(" ").append(this.generator.formatDouble((double)yScale)).append("  scale ");
      this.generator.writeln(sb.toString());
   }

   public void cubicBezierTo(int p1x, int p1y, int p2x, int p2y, int p3x, int p3y) throws IOException {
      StringBuffer sb = (new StringBuffer()).append(this.generator.formatDouble((double)toPoints(p1x))).append(" ").append(this.generator.formatDouble((double)toPoints(p1y))).append(" ").append(this.generator.formatDouble((double)toPoints(p2x))).append(" ").append(this.generator.formatDouble((double)toPoints(p2y))).append(" ").append(this.generator.formatDouble((double)toPoints(p3x))).append(" ").append(this.generator.formatDouble((double)toPoints(p3y))).append(" curveto ");
      this.generator.writeln(sb.toString());
   }
}
