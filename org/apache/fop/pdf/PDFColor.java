package org.apache.fop.pdf;

import java.awt.Color;
import java.awt.color.ColorSpace;
import java.util.ArrayList;
import java.util.List;
import org.apache.xmlgraphics.java2d.color.DeviceCMYKColorSpace;

public class PDFColor extends PDFPathPaint {
   private static double blackFactor = 2.0;
   private double red;
   private double green;
   private double blue;
   private double cyan;
   private double magenta;
   private double yellow;
   private double black;

   public PDFColor(double theRed, double theGreen, double theBlue) {
      this.red = -1.0;
      this.green = -1.0;
      this.blue = -1.0;
      this.cyan = -1.0;
      this.magenta = -1.0;
      this.yellow = -1.0;
      this.black = -1.0;
      this.colorSpace = new PDFDeviceColorSpace(2);
      this.red = theRed;
      this.green = theGreen;
      this.blue = theBlue;
   }

   public PDFColor(Color col) {
      this.red = -1.0;
      this.green = -1.0;
      this.blue = -1.0;
      this.cyan = -1.0;
      this.magenta = -1.0;
      this.yellow = -1.0;
      this.black = -1.0;
      ColorSpace cs = col.getColorSpace();
      float[] comps;
      if (cs != null && cs instanceof DeviceCMYKColorSpace) {
         this.colorSpace = new PDFDeviceColorSpace(3);
         comps = col.getColorComponents((float[])null);
         this.cyan = (double)comps[0];
         this.magenta = (double)comps[1];
         this.yellow = (double)comps[2];
         this.black = (double)comps[3];
      } else {
         this.colorSpace = new PDFDeviceColorSpace(2);
         comps = new float[3];
         comps = col.getColorComponents(comps);
         this.red = (double)comps[0];
         this.green = (double)comps[1];
         this.blue = (double)comps[2];
      }

   }

   public PDFColor(int theRed, int theGreen, int theBlue) {
      this((double)theRed / 255.0, (double)theGreen / 255.0, (double)theBlue / 255.0);
   }

   public PDFColor(double theCyan, double theMagenta, double theYellow, double theBlack) {
      this.red = -1.0;
      this.green = -1.0;
      this.blue = -1.0;
      this.cyan = -1.0;
      this.magenta = -1.0;
      this.yellow = -1.0;
      this.black = -1.0;
      this.colorSpace = new PDFDeviceColorSpace(3);
      this.cyan = theCyan;
      this.magenta = theMagenta;
      this.yellow = theYellow;
      this.black = theBlack;
   }

   public List getVector() {
      List theColorVector = new ArrayList();
      if (this.colorSpace.getColorSpace() == 2) {
         theColorVector.add(this.red);
         theColorVector.add(this.green);
         theColorVector.add(this.blue);
      } else if (this.colorSpace.getColorSpace() == 3) {
         theColorVector.add(this.cyan);
         theColorVector.add(this.magenta);
         theColorVector.add(this.yellow);
         theColorVector.add(this.black);
      } else {
         theColorVector.add(this.black);
      }

      return theColorVector;
   }

   public double red() {
      return this.red;
   }

   public double green() {
      return this.green;
   }

   public double blue() {
      return this.blue;
   }

   public int red255() {
      return (int)(this.red * 255.0);
   }

   public int green255() {
      return (int)(this.green * 255.0);
   }

   public int blue255() {
      return (int)(this.blue * 255.0);
   }

   public double cyan() {
      return this.cyan;
   }

   public double magenta() {
      return this.magenta;
   }

   public double yellow() {
      return this.yellow;
   }

   public double black() {
      return this.black;
   }

   public void setColorSpace(int theColorSpace) {
      int theOldColorSpace = this.colorSpace.getColorSpace();
      if (theOldColorSpace != theColorSpace) {
         if (theOldColorSpace == 2) {
            if (theColorSpace == 3) {
               this.convertRGBtoCMYK();
            } else {
               this.convertRGBtoGRAY();
            }
         } else if (theOldColorSpace == 3) {
            if (theColorSpace == 2) {
               this.convertCMYKtoRGB();
            } else {
               this.convertCMYKtoGRAY();
            }
         } else if (theColorSpace == 2) {
            this.convertGRAYtoRGB();
         } else {
            this.convertGRAYtoCMYK();
         }

         this.colorSpace.setColorSpace(theColorSpace);
      }

   }

   public String getColorSpaceOut(boolean fillNotStroke) {
      StringBuffer p = new StringBuffer("");
      if (this.colorSpace.getColorSpace() == 2) {
         boolean same = false;
         if (this.red == this.green && this.red == this.blue) {
            same = true;
         }

         if (fillNotStroke) {
            if (same) {
               p.append(PDFNumber.doubleOut(this.red) + " g\n");
            } else {
               p.append(PDFNumber.doubleOut(this.red) + " " + PDFNumber.doubleOut(this.green) + " " + PDFNumber.doubleOut(this.blue) + " rg\n");
            }
         } else if (same) {
            p.append(PDFNumber.doubleOut(this.red) + " G\n");
         } else {
            p.append(PDFNumber.doubleOut(this.red) + " " + PDFNumber.doubleOut(this.green) + " " + PDFNumber.doubleOut(this.blue) + " RG\n");
         }
      } else if (this.colorSpace.getColorSpace() == 3) {
         if (fillNotStroke) {
            p.append(PDFNumber.doubleOut(this.cyan) + " " + PDFNumber.doubleOut(this.magenta) + " " + PDFNumber.doubleOut(this.yellow) + " " + PDFNumber.doubleOut(this.black) + " k\n");
         } else {
            p.append(PDFNumber.doubleOut(this.cyan) + " " + PDFNumber.doubleOut(this.magenta) + " " + PDFNumber.doubleOut(this.yellow) + " " + PDFNumber.doubleOut(this.black) + " K\n");
         }
      } else if (fillNotStroke) {
         p.append(PDFNumber.doubleOut(this.black) + " g\n");
      } else {
         p.append(PDFNumber.doubleOut(this.black) + " G\n");
      }

      return p.toString();
   }

   protected void convertCMYKtoRGB() {
      this.red = 1.0 - this.cyan;
      this.green = 1.0 - this.green;
      this.blue = 1.0 - this.yellow;
      this.red += this.black / blackFactor;
      this.green += this.black / blackFactor;
      this.blue += this.black / blackFactor;
   }

   protected void convertRGBtoCMYK() {
      this.cyan = 1.0 - this.red;
      this.magenta = 1.0 - this.green;
      this.yellow = 1.0 - this.blue;
      this.black = 0.0;
   }

   protected void convertGRAYtoRGB() {
      this.red = 1.0 - this.black;
      this.green = 1.0 - this.black;
      this.blue = 1.0 - this.black;
   }

   protected void convertGRAYtoCMYK() {
      this.cyan = this.black;
      this.magenta = this.black;
      this.yellow = this.black;
   }

   protected void convertCMYKtoGRAY() {
      double tempDouble = 0.0;
      tempDouble = this.cyan;
      if (this.magenta < tempDouble) {
         tempDouble = this.magenta;
      }

      if (this.yellow < tempDouble) {
         tempDouble = this.yellow;
      }

      this.black = tempDouble / blackFactor;
   }

   protected void convertRGBtoGRAY() {
      double tempDouble = 0.0;
      tempDouble = this.red;
      if (this.green < tempDouble) {
         tempDouble = this.green;
      }

      if (this.blue < tempDouble) {
         tempDouble = this.blue;
      }

      this.black = 1.0 - tempDouble / blackFactor;
   }

   public byte[] toPDF() {
      return new byte[0];
   }

   protected boolean contentEquals(PDFObject obj) {
      if (!(obj instanceof PDFColor)) {
         return false;
      } else {
         PDFColor color = (PDFColor)obj;
         return color.red == this.red && color.green == this.green && color.blue == this.blue;
      }
   }
}
