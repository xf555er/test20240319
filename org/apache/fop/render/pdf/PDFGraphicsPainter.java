package org.apache.fop.render.pdf;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;
import org.apache.fop.render.intermediate.ArcToBezierCurveTransformer;
import org.apache.fop.render.intermediate.BezierCurvePainter;
import org.apache.fop.render.intermediate.BorderPainter;
import org.apache.fop.render.intermediate.GraphicsPainter;
import org.apache.fop.traits.RuleStyle;
import org.apache.fop.util.ColorUtil;

public class PDFGraphicsPainter implements GraphicsPainter, BezierCurvePainter {
   private final PDFContentGeneratorHelper generator;
   private final ArcToBezierCurveTransformer arcToBezierCurveTransformer;

   public PDFGraphicsPainter(PDFContentGenerator generator) {
      this.generator = new PDFContentGeneratorHelper(generator);
      this.arcToBezierCurveTransformer = new ArcToBezierCurveTransformer(this);
   }

   public void drawBorderLine(int x1, int y1, int x2, int y2, boolean horz, boolean startOrBefore, int style, Color col) {
      this.drawBorderLine2((float)x1 / 1000.0F, (float)y1 / 1000.0F, (float)x2 / 1000.0F, (float)y2 / 1000.0F, horz, startOrBefore, style, col);
   }

   private void drawBorderLine2(float x1, float y1, float x2, float y2, boolean horz, boolean startOrBefore, int style, Color col) {
      float w = x2 - x1;
      float h = y2 - y1;
      float colFactor;
      Color c;
      float ym;
      float ym;
      float unit;
      switch (style) {
         case 31:
            this.generator.setColor(col);
            if (horz) {
               unit = BorderPainter.dashWidthCalculator(w, h);
               if (unit != 0.0F) {
                  ym = y1 + h / 2.0F;
                  this.generator.setDashLine(unit, unit * 0.5F).setLineWidth(h).strokeLine(x1, ym, x2, ym);
               }
            } else {
               unit = BorderPainter.dashWidthCalculator(h, w);
               if (unit != 0.0F) {
                  ym = x1 + w / 2.0F;
                  this.generator.setDashLine(unit, unit * 0.5F).setLineWidth(w).strokeLine(ym, y1, ym, y2);
               }
            }
            break;
         case 36:
            this.generator.setColor(col).setRoundCap();
            int rep;
            if (horz) {
               unit = Math.abs(2.0F * h);
               rep = (int)(w / unit);
               if (rep % 2 == 0) {
                  ++rep;
               }

               unit = w / (float)rep;
               ym = y1 + h / 2.0F;
               this.generator.setDashLine(0.0F, unit).setLineWidth(h).strokeLine(x1, ym, x2, ym);
            } else {
               unit = Math.abs(2.0F * w);
               rep = (int)(h / unit);
               if (rep % 2 == 0) {
                  ++rep;
               }

               unit = h / (float)rep;
               ym = x1 + w / 2.0F;
               this.generator.setDashLine(0.0F, unit).setLineWidth(w).strokeLine(ym, y1, ym, y2);
            }
            break;
         case 37:
            this.generator.setColor(col).setSolidLine();
            if (horz) {
               unit = h / 3.0F;
               ym = y1 + unit / 2.0F;
               ym = ym + unit + unit;
               this.generator.setLineWidth(unit).strokeLine(x1, ym, x2, ym).strokeLine(x1, ym, x2, ym);
            } else {
               unit = w / 3.0F;
               ym = x1 + unit / 2.0F;
               ym = ym + unit + unit;
               this.generator.setLineWidth(unit).strokeLine(ym, y1, ym, y2).strokeLine(ym, y1, ym, y2);
            }
            break;
         case 55:
         case 119:
            colFactor = style == 55 ? 0.4F : -0.4F;
            this.generator.setSolidLine();
            float ym1;
            Color lowercol;
            if (horz) {
               c = ColorUtil.lightenColor(col, -colFactor);
               lowercol = ColorUtil.lightenColor(col, colFactor);
               ym = h / 3.0F;
               ym1 = y1 + ym / 2.0F;
               this.generator.setLineWidth(ym).setColor(c).strokeLine(x1, ym1, x2, ym1).setColor(col).strokeLine(x1, ym1 + ym, x2, ym1 + ym).setColor(lowercol).strokeLine(x1, ym1 + ym + ym, x2, ym1 + ym + ym);
            } else {
               c = ColorUtil.lightenColor(col, -colFactor);
               lowercol = ColorUtil.lightenColor(col, colFactor);
               ym = w / 3.0F;
               ym1 = x1 + ym / 2.0F;
               this.generator.setLineWidth(ym).setColor(c).strokeLine(ym1, y1, ym1, y2).setColor(col).strokeLine(ym1 + ym, y1, ym1 + ym, y2).setColor(lowercol).strokeLine(ym1 + ym + ym, y1, ym1 + ym + ym, y2);
            }
         case 57:
            break;
         case 67:
         case 101:
            colFactor = style == 101 ? 0.4F : -0.4F;
            this.generator.setSolidLine();
            if (horz) {
               c = ColorUtil.lightenColor(col, (float)(startOrBefore ? 1 : -1) * colFactor);
               ym = y1 + h / 2.0F;
               this.generator.setLineWidth(h).setColor(c).strokeLine(x1, ym, x2, ym);
            } else {
               c = ColorUtil.lightenColor(col, (float)(startOrBefore ? 1 : -1) * colFactor);
               ym = x1 + w / 2.0F;
               this.generator.setLineWidth(w).setColor(c).strokeLine(ym, y1, ym, y2);
            }
            break;
         default:
            this.generator.setColor(col).setSolidLine();
            if (horz) {
               ym = y1 + h / 2.0F;
               this.generator.setLineWidth(h).strokeLine(x1, ym, x2, ym);
            } else {
               ym = x1 + w / 2.0F;
               this.generator.setLineWidth(w).strokeLine(ym, y1, ym, y2);
            }
      }

   }

   public void drawLine(Point start, Point end, int width, Color color, RuleStyle style) {
      if (start.y != end.y) {
         throw new UnsupportedOperationException("Can only deal with horizontal lines right now");
      } else {
         this.saveGraphicsState();
         int half = width / 2;
         int starty = start.y - half;
         Rectangle boundingRect = new Rectangle(start.x, start.y - half, end.x - start.x, width);
         switch (style.getEnumValue()) {
            case 31:
            case 37:
            case 133:
               this.drawBorderLine(start.x, start.y - half, end.x, end.y + half, true, true, style.getEnumValue(), color);
               break;
            case 36:
               this.generator.clipRect(boundingRect).transformCoordinatesLine(1, 0, 0, 1, half, 0);
               this.drawBorderLine(start.x, start.y - half, end.x, end.y + half, true, true, style.getEnumValue(), color);
               break;
            case 55:
            case 119:
               this.generator.setFillColor(ColorUtil.lightenColor(color, 0.6F)).fillRect(start.x, start.y, end.x, starty + 2 * half).setFillColor(color).fillRidge(style, start.x, start.y, end.x, end.y, half);
               break;
            default:
               throw new UnsupportedOperationException("rule style not supported");
         }

         this.restoreGraphicsState();
      }
   }

   private static String format(int coordinate) {
      return format((float)coordinate / 1000.0F);
   }

   private static String format(float coordinate) {
      return PDFContentGenerator.format(coordinate);
   }

   public void moveTo(int x, int y) {
      this.generator.moveTo(x, y);
   }

   public void lineTo(int x, int y) {
      this.generator.lineTo(x, y);
   }

   public void arcTo(double startAngle, double endAngle, int cx, int cy, int width, int height) throws IOException {
      this.arcToBezierCurveTransformer.arcTo(startAngle, endAngle, cx, cy, width, height);
   }

   public void closePath() {
      this.generator.closePath();
   }

   public void clip() {
      this.generator.clip();
   }

   public void saveGraphicsState() {
      this.generator.saveGraphicsState();
   }

   public void restoreGraphicsState() {
      this.generator.restoreGraphicsState();
   }

   public void rotateCoordinates(double angle) throws IOException {
      float s = (float)Math.sin(angle);
      float c = (float)Math.cos(angle);
      this.generator.transformFloatCoordinates(c, s, -s, c, 0.0F, 0.0F);
   }

   public void translateCoordinates(int xTranslate, int yTranslate) throws IOException {
      this.generator.transformCoordinates(1000, 0, 0, 1000, xTranslate, yTranslate);
   }

   public void scaleCoordinates(float xScale, float yScale) throws IOException {
      this.generator.transformFloatCoordinates(xScale, 0.0F, 0.0F, yScale, 0.0F, 0.0F);
   }

   public void cubicBezierTo(int p1x, int p1y, int p2x, int p2y, int p3x, int p3y) {
      this.generator.cubicBezierTo(p1x, p1y, p2x, p2y, p3x, p3y);
   }

   private static class PDFContentGeneratorHelper {
      private final PDFContentGenerator generator;

      public PDFContentGeneratorHelper(PDFContentGenerator generator) {
         this.generator = generator;
      }

      public PDFContentGeneratorHelper moveTo(int x, int y) {
         return this.add("m", PDFGraphicsPainter.format(x), PDFGraphicsPainter.format(y));
      }

      public PDFContentGeneratorHelper lineTo(int x, int y) {
         return this.add("l", PDFGraphicsPainter.format(x), PDFGraphicsPainter.format(y));
      }

      public PDFContentGeneratorHelper cubicBezierTo(int p1x, int p1y, int p2x, int p2y, int p3x, int p3y) {
         return this.add("c", PDFGraphicsPainter.format(p1x), PDFGraphicsPainter.format(p1y), PDFGraphicsPainter.format(p2x), PDFGraphicsPainter.format(p2y), PDFGraphicsPainter.format(p3x), PDFGraphicsPainter.format(p3y));
      }

      public PDFContentGeneratorHelper closePath() {
         return this.add("h");
      }

      public PDFContentGeneratorHelper clip() {
         return this.addLine("W\nn");
      }

      public PDFContentGeneratorHelper clipRect(Rectangle rectangle) {
         this.generator.clipRect(rectangle);
         return this;
      }

      public PDFContentGeneratorHelper saveGraphicsState() {
         return this.addLine("q");
      }

      public PDFContentGeneratorHelper restoreGraphicsState() {
         return this.addLine("Q");
      }

      public PDFContentGeneratorHelper setSolidLine() {
         this.generator.add("[] 0 d ");
         return this;
      }

      public PDFContentGeneratorHelper setRoundCap() {
         return this.add("J", "1");
      }

      public PDFContentGeneratorHelper strokeLine(float xStart, float yStart, float xEnd, float yEnd) {
         this.add("m", xStart, yStart);
         return this.addLine("l S", xEnd, yEnd);
      }

      public PDFContentGeneratorHelper fillRect(int xStart, int yStart, int xEnd, int yEnd) {
         String xS = PDFGraphicsPainter.format(xStart);
         String xE = PDFGraphicsPainter.format(xEnd);
         String yS = PDFGraphicsPainter.format(yStart);
         String yE = PDFGraphicsPainter.format(yEnd);
         return this.addLine("m", xS, yS).addLine("l", xE, yS).addLine("l", xE, yE).addLine("l", xS, yE).addLine("h").addLine("f");
      }

      public PDFContentGeneratorHelper fillRidge(RuleStyle style, int xStart, int yStart, int xEnd, int yEnd, int half) {
         String xS = PDFGraphicsPainter.format(xStart);
         String xE = PDFGraphicsPainter.format(xEnd);
         String yS = PDFGraphicsPainter.format(yStart);
         if (style == RuleStyle.GROOVE) {
            this.addLine("m", xS, yS).addLine("l", xE, yS).addLine("l", xE, PDFGraphicsPainter.format(yStart + half)).addLine("l", PDFGraphicsPainter.format(xStart + half), PDFGraphicsPainter.format(yStart + half)).addLine("l", xS, PDFGraphicsPainter.format(yStart + 2 * half));
         } else {
            this.addLine("m", xE, yS).addLine("l", xE, PDFGraphicsPainter.format(yStart + 2 * half)).addLine("l", xS, PDFGraphicsPainter.format(yStart + 2 * half)).addLine("l", xS, PDFGraphicsPainter.format(yStart + half)).addLine("l", PDFGraphicsPainter.format(xEnd - half), PDFGraphicsPainter.format(yStart + half));
         }

         return this.addLine("h").addLine("f");
      }

      public PDFContentGeneratorHelper setLineWidth(float width) {
         return this.addLine("w", width);
      }

      public PDFContentGeneratorHelper setDashLine(float first, float... rest) {
         StringBuilder sb = new StringBuilder();
         sb.append("[").append(PDFGraphicsPainter.format(first));
         float[] var4 = rest;
         int var5 = rest.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            float unit = var4[var6];
            sb.append(" ").append(PDFGraphicsPainter.format(unit));
         }

         sb.append("] 0 d ");
         this.generator.add(sb.toString());
         return this;
      }

      public PDFContentGeneratorHelper setColor(Color col) {
         this.generator.setColor(col, false);
         return this;
      }

      public PDFContentGeneratorHelper setFillColor(Color col) {
         this.generator.setColor(col, true);
         return this;
      }

      public PDFContentGeneratorHelper transformFloatCoordinates(float a, float b, float c, float d, float e, float f) {
         return this.add("cm", a, b, c, d, e, f);
      }

      public PDFContentGeneratorHelper transformCoordinates(int a, int b, int c, int d, int e, int f) {
         return this.add("cm", PDFGraphicsPainter.format(a), PDFGraphicsPainter.format(b), PDFGraphicsPainter.format(c), PDFGraphicsPainter.format(d), PDFGraphicsPainter.format(e), PDFGraphicsPainter.format(f));
      }

      public PDFContentGeneratorHelper transformCoordinatesLine(int a, int b, int c, int d, int e, int f) {
         return this.addLine("cm", PDFGraphicsPainter.format(a), PDFGraphicsPainter.format(b), PDFGraphicsPainter.format(c), PDFGraphicsPainter.format(d), PDFGraphicsPainter.format(e), PDFGraphicsPainter.format(f));
      }

      public PDFContentGeneratorHelper add(String op) {
         assert op.equals(op.trim());

         this.generator.add(op + " ");
         return this;
      }

      private PDFContentGeneratorHelper add(String op, String... args) {
         this.add(this.createArgs(args), op);
         return this;
      }

      public PDFContentGeneratorHelper addLine(String op) {
         assert op.equals(op.trim());

         this.generator.add(op + "\n");
         return this;
      }

      public PDFContentGeneratorHelper addLine(String op, String... args) {
         this.addLine(this.createArgs(args), op);
         return this;
      }

      private PDFContentGeneratorHelper add(String op, float... args) {
         this.add(this.createArgs(args), op);
         return this;
      }

      public PDFContentGeneratorHelper addLine(String op, float... args) {
         this.addLine(this.createArgs(args), op);
         return this;
      }

      private StringBuilder createArgs(float... args) {
         StringBuilder sb = new StringBuilder();
         float[] var3 = args;
         int var4 = args.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            float arg = var3[var5];
            sb.append(PDFGraphicsPainter.format(arg)).append(" ");
         }

         return sb;
      }

      private StringBuilder createArgs(String... args) {
         StringBuilder sb = new StringBuilder();
         String[] var3 = args;
         int var4 = args.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            String arg = var3[var5];
            sb.append(arg).append(" ");
         }

         return sb;
      }

      private void add(StringBuilder args, String op) {
         assert op.equals(op.trim());

         this.generator.add(args.append(op).append(" ").toString());
      }

      private void addLine(StringBuilder args, String op) {
         assert op.equals(op.trim());

         this.generator.add(args.append(op).append("\n").toString());
      }
   }
}
