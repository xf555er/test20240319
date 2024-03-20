package org.apache.batik.ext.awt.image;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.CompositeContext;
import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferInt;
import java.awt.image.PackedColorModel;
import java.awt.image.Raster;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;

public class SVGComposite implements Composite {
   public static final SVGComposite OVER;
   public static final SVGComposite IN;
   public static final SVGComposite OUT;
   public static final SVGComposite ATOP;
   public static final SVGComposite XOR;
   public static final SVGComposite MULTIPLY;
   public static final SVGComposite SCREEN;
   public static final SVGComposite DARKEN;
   public static final SVGComposite LIGHTEN;
   CompositeRule rule;

   public CompositeRule getRule() {
      return this.rule;
   }

   public SVGComposite(CompositeRule rule) {
      this.rule = rule;
   }

   public boolean equals(Object o) {
      if (o instanceof SVGComposite) {
         SVGComposite svgc = (SVGComposite)o;
         return svgc.getRule() == this.getRule();
      } else if (o instanceof AlphaComposite) {
         AlphaComposite ac = (AlphaComposite)o;
         switch (this.getRule().getRule()) {
            case 1:
               return ac == AlphaComposite.SrcOver;
            case 2:
               return ac == AlphaComposite.SrcIn;
            case 3:
               return ac == AlphaComposite.SrcOut;
            default:
               return false;
         }
      } else {
         return false;
      }
   }

   public boolean is_INT_PACK(ColorModel cm) {
      if (!(cm instanceof PackedColorModel)) {
         return false;
      } else {
         PackedColorModel pcm = (PackedColorModel)cm;
         int[] masks = pcm.getMasks();
         if (masks.length != 4) {
            return false;
         } else if (masks[0] != 16711680) {
            return false;
         } else if (masks[1] != 65280) {
            return false;
         } else if (masks[2] != 255) {
            return false;
         } else {
            return masks[3] == -16777216;
         }
      }
   }

   public CompositeContext createContext(ColorModel srcCM, ColorModel dstCM, RenderingHints hints) {
      boolean use_int_pack = this.is_INT_PACK(srcCM) && this.is_INT_PACK(dstCM);
      switch (this.rule.getRule()) {
         case 1:
            if (!dstCM.hasAlpha()) {
               if (use_int_pack) {
                  return new OverCompositeContext_INT_PACK_NA(srcCM, dstCM);
               }

               return new OverCompositeContext_NA(srcCM, dstCM);
            } else if (!use_int_pack) {
               return new OverCompositeContext(srcCM, dstCM);
            } else {
               if (srcCM.isAlphaPremultiplied()) {
                  return new OverCompositeContext_INT_PACK(srcCM, dstCM);
               }

               return new OverCompositeContext_INT_PACK_UNPRE(srcCM, dstCM);
            }
         case 2:
            if (use_int_pack) {
               return new InCompositeContext_INT_PACK(srcCM, dstCM);
            }

            return new InCompositeContext(srcCM, dstCM);
         case 3:
            if (use_int_pack) {
               return new OutCompositeContext_INT_PACK(srcCM, dstCM);
            }

            return new OutCompositeContext(srcCM, dstCM);
         case 4:
            if (use_int_pack) {
               return new AtopCompositeContext_INT_PACK(srcCM, dstCM);
            }

            return new AtopCompositeContext(srcCM, dstCM);
         case 5:
            if (use_int_pack) {
               return new XorCompositeContext_INT_PACK(srcCM, dstCM);
            }

            return new XorCompositeContext(srcCM, dstCM);
         case 6:
            float[] coeff = this.rule.getCoefficients();
            if (use_int_pack) {
               return new ArithCompositeContext_INT_PACK_LUT(srcCM, dstCM, coeff[0], coeff[1], coeff[2], coeff[3]);
            }

            return new ArithCompositeContext(srcCM, dstCM, coeff[0], coeff[1], coeff[2], coeff[3]);
         case 7:
            if (use_int_pack) {
               return new MultiplyCompositeContext_INT_PACK(srcCM, dstCM);
            }

            return new MultiplyCompositeContext(srcCM, dstCM);
         case 8:
            if (use_int_pack) {
               return new ScreenCompositeContext_INT_PACK(srcCM, dstCM);
            }

            return new ScreenCompositeContext(srcCM, dstCM);
         case 9:
            if (use_int_pack) {
               return new DarkenCompositeContext_INT_PACK(srcCM, dstCM);
            }

            return new DarkenCompositeContext(srcCM, dstCM);
         case 10:
            if (use_int_pack) {
               return new LightenCompositeContext_INT_PACK(srcCM, dstCM);
            }

            return new LightenCompositeContext(srcCM, dstCM);
         default:
            throw new UnsupportedOperationException("Unknown composite rule requested.");
      }
   }

   static {
      OVER = new SVGComposite(CompositeRule.OVER);
      IN = new SVGComposite(CompositeRule.IN);
      OUT = new SVGComposite(CompositeRule.OUT);
      ATOP = new SVGComposite(CompositeRule.ATOP);
      XOR = new SVGComposite(CompositeRule.XOR);
      MULTIPLY = new SVGComposite(CompositeRule.MULTIPLY);
      SCREEN = new SVGComposite(CompositeRule.SCREEN);
      DARKEN = new SVGComposite(CompositeRule.DARKEN);
      LIGHTEN = new SVGComposite(CompositeRule.LIGHTEN);
   }

   public static class LightenCompositeContext_INT_PACK extends AlphaPreCompositeContext_INT_PACK {
      LightenCompositeContext_INT_PACK(ColorModel srcCM, ColorModel dstCM) {
         super(srcCM, dstCM);
      }

      public void precompose_INT_PACK(int width, int height, int[] srcPixels, int srcAdjust, int srcSp, int[] dstInPixels, int dstInAdjust, int dstInSp, int[] dstOutPixels, int dstOutAdjust, int dstOutSp) {
         int norm = 65793;
         int pt5 = 8388608;

         for(int y = 0; y < height; ++y) {
            int dstA;
            int dstR;
            int dstG;
            int dstB;
            for(int end = dstOutSp + width; dstOutSp < end; dstOutPixels[dstOutSp++] = dstA << 24 | dstR << 16 | dstG << 8 | dstB) {
               int srcP = srcPixels[srcSp++];
               int dstP = dstInPixels[dstInSp++];
               int srcV = srcP >>> 24;
               int dstV = dstP >>> 24;
               int srcM = (255 - dstV) * 65793;
               int dstM = (255 - srcV) * 65793;
               dstA = srcV + dstV - (srcV * dstV * 65793 + 8388608 >>> 24);
               srcV = srcP >> 16 & 255;
               dstV = dstP >> 16 & 255;
               dstR = (srcM * srcV + 8388608 >>> 24) + dstV;
               int tmp = (dstM * dstV + 8388608 >>> 24) + srcV;
               if (dstR < tmp) {
                  dstR = tmp;
               }

               srcV = srcP >> 8 & 255;
               dstV = dstP >> 8 & 255;
               dstG = (srcM * srcV + 8388608 >>> 24) + dstV;
               tmp = (dstM * dstV + 8388608 >>> 24) + srcV;
               if (dstG < tmp) {
                  dstG = tmp;
               }

               srcV = srcP & 255;
               dstV = dstP & 255;
               dstB = (srcM * srcV + 8388608 >>> 24) + dstV;
               tmp = (dstM * dstV + 8388608 >>> 24) + srcV;
               if (dstB < tmp) {
                  dstB = tmp;
               }

               dstA &= 255;
               dstR &= 255;
               dstG &= 255;
               dstB &= 255;
            }

            srcSp += srcAdjust;
            dstInSp += dstInAdjust;
            dstOutSp += dstOutAdjust;
         }

      }
   }

   public static class LightenCompositeContext extends AlphaPreCompositeContext {
      LightenCompositeContext(ColorModel srcCM, ColorModel dstCM) {
         super(srcCM, dstCM);
      }

      public void precompose(Raster src, Raster dstIn, WritableRaster dstOut) {
         int[] srcPix = null;
         int[] dstPix = null;
         int x = dstOut.getMinX();
         int w = dstOut.getWidth();
         int y0 = dstOut.getMinY();
         int y1 = y0 + dstOut.getHeight();
         int norm = 65793;
         int pt5 = 8388608;

         for(int y = y0; y < y1; ++y) {
            srcPix = src.getPixels(x, y, w, 1, srcPix);
            dstPix = dstIn.getPixels(x, y, w, 1, dstPix);
            int sp = 0;

            for(int end = w * 4; sp < end; ++sp) {
               int srcM = 255 - dstPix[sp + 3];
               int dstM = 255 - srcPix[sp + 3];
               int t1 = (srcM * srcPix[sp] * 65793 + 8388608 >>> 24) + dstPix[sp];
               int t2 = (dstM * dstPix[sp] * 65793 + 8388608 >>> 24) + srcPix[sp];
               if (t1 > t2) {
                  dstPix[sp] = t1;
               } else {
                  dstPix[sp] = t2;
               }

               ++sp;
               t1 = (srcM * srcPix[sp] * 65793 + 8388608 >>> 24) + dstPix[sp];
               t2 = (dstM * dstPix[sp] * 65793 + 8388608 >>> 24) + srcPix[sp];
               if (t1 > t2) {
                  dstPix[sp] = t1;
               } else {
                  dstPix[sp] = t2;
               }

               ++sp;
               t1 = (srcM * srcPix[sp] * 65793 + 8388608 >>> 24) + dstPix[sp];
               t2 = (dstM * dstPix[sp] * 65793 + 8388608 >>> 24) + srcPix[sp];
               if (t1 > t2) {
                  dstPix[sp] = t1;
               } else {
                  dstPix[sp] = t2;
               }

               ++sp;
               dstPix[sp] = srcPix[sp] + dstPix[sp] - (dstPix[sp] * srcPix[sp] * 65793 + 8388608 >>> 24);
            }

            dstOut.setPixels(x, y, w, 1, dstPix);
         }

      }
   }

   public static class DarkenCompositeContext_INT_PACK extends AlphaPreCompositeContext_INT_PACK {
      DarkenCompositeContext_INT_PACK(ColorModel srcCM, ColorModel dstCM) {
         super(srcCM, dstCM);
      }

      public void precompose_INT_PACK(int width, int height, int[] srcPixels, int srcAdjust, int srcSp, int[] dstInPixels, int dstInAdjust, int dstInSp, int[] dstOutPixels, int dstOutAdjust, int dstOutSp) {
         int norm = 65793;
         int pt5 = 8388608;

         for(int y = 0; y < height; ++y) {
            int dstA;
            int dstR;
            int dstG;
            int dstB;
            for(int end = dstOutSp + width; dstOutSp < end; dstOutPixels[dstOutSp++] = dstA << 24 | dstR << 16 | dstG << 8 | dstB) {
               int srcP = srcPixels[srcSp++];
               int dstP = dstInPixels[dstInSp++];
               int srcV = srcP >>> 24;
               int dstV = dstP >>> 24;
               int srcM = (255 - dstV) * 65793;
               int dstM = (255 - srcV) * 65793;
               dstA = srcV + dstV - (srcV * dstV * 65793 + 8388608 >>> 24);
               srcV = srcP >> 16 & 255;
               dstV = dstP >> 16 & 255;
               dstR = (srcM * srcV + 8388608 >>> 24) + dstV;
               int tmp = (dstM * dstV + 8388608 >>> 24) + srcV;
               if (dstR > tmp) {
                  dstR = tmp;
               }

               srcV = srcP >> 8 & 255;
               dstV = dstP >> 8 & 255;
               dstG = (srcM * srcV + 8388608 >>> 24) + dstV;
               tmp = (dstM * dstV + 8388608 >>> 24) + srcV;
               if (dstG > tmp) {
                  dstG = tmp;
               }

               srcV = srcP & 255;
               dstV = dstP & 255;
               dstB = (srcM * srcV + 8388608 >>> 24) + dstV;
               tmp = (dstM * dstV + 8388608 >>> 24) + srcV;
               if (dstB > tmp) {
                  dstB = tmp;
               }

               dstA &= 255;
               dstR &= 255;
               dstG &= 255;
               dstB &= 255;
            }

            srcSp += srcAdjust;
            dstInSp += dstInAdjust;
            dstOutSp += dstOutAdjust;
         }

      }
   }

   public static class DarkenCompositeContext extends AlphaPreCompositeContext {
      DarkenCompositeContext(ColorModel srcCM, ColorModel dstCM) {
         super(srcCM, dstCM);
      }

      public void precompose(Raster src, Raster dstIn, WritableRaster dstOut) {
         int[] srcPix = null;
         int[] dstPix = null;
         int x = dstOut.getMinX();
         int w = dstOut.getWidth();
         int y0 = dstOut.getMinY();
         int y1 = y0 + dstOut.getHeight();
         int norm = 65793;
         int pt5 = 8388608;

         for(int y = y0; y < y1; ++y) {
            srcPix = src.getPixels(x, y, w, 1, srcPix);
            dstPix = dstIn.getPixels(x, y, w, 1, dstPix);
            int sp = 0;

            for(int end = w * 4; sp < end; ++sp) {
               int srcM = 255 - dstPix[sp + 3];
               int dstM = 255 - srcPix[sp + 3];
               int t1 = (srcM * srcPix[sp] * 65793 + 8388608 >>> 24) + dstPix[sp];
               int t2 = (dstM * dstPix[sp] * 65793 + 8388608 >>> 24) + srcPix[sp];
               if (t1 > t2) {
                  dstPix[sp] = t2;
               } else {
                  dstPix[sp] = t1;
               }

               ++sp;
               t1 = (srcM * srcPix[sp] * 65793 + 8388608 >>> 24) + dstPix[sp];
               t2 = (dstM * dstPix[sp] * 65793 + 8388608 >>> 24) + srcPix[sp];
               if (t1 > t2) {
                  dstPix[sp] = t2;
               } else {
                  dstPix[sp] = t1;
               }

               ++sp;
               t1 = (srcM * srcPix[sp] * 65793 + 8388608 >>> 24) + dstPix[sp];
               t2 = (dstM * dstPix[sp] * 65793 + 8388608 >>> 24) + srcPix[sp];
               if (t1 > t2) {
                  dstPix[sp] = t2;
               } else {
                  dstPix[sp] = t1;
               }

               ++sp;
               dstPix[sp] = srcPix[sp] + dstPix[sp] - (dstPix[sp] * srcPix[sp] * 65793 + 8388608 >>> 24);
            }

            dstOut.setPixels(x, y, w, 1, dstPix);
         }

      }
   }

   public static class ScreenCompositeContext_INT_PACK extends AlphaPreCompositeContext_INT_PACK {
      ScreenCompositeContext_INT_PACK(ColorModel srcCM, ColorModel dstCM) {
         super(srcCM, dstCM);
      }

      public void precompose_INT_PACK(int width, int height, int[] srcPixels, int srcAdjust, int srcSp, int[] dstInPixels, int dstInAdjust, int dstInSp, int[] dstOutPixels, int dstOutAdjust, int dstOutSp) {
         int norm = 65793;
         int pt5 = 8388608;

         for(int y = 0; y < height; ++y) {
            int srcA;
            int srcR;
            int srcG;
            int srcB;
            int dstA;
            int dstR;
            int dstG;
            int dstB;
            for(int end = dstOutSp + width; dstOutSp < end; dstOutPixels[dstOutSp++] = srcR + dstR - (srcR * dstR * 65793 + 8388608 >>> 24) << 16 | srcG + dstG - (srcG * dstG * 65793 + 8388608 >>> 24) << 8 | srcB + dstB - (srcB * dstB * 65793 + 8388608 >>> 24) | srcA + dstA - (srcA * dstA * 65793 + 8388608 >>> 24) << 24) {
               int srcP = srcPixels[srcSp++];
               int dstP = dstInPixels[dstInSp++];
               srcA = srcP >>> 24;
               dstA = dstP >>> 24;
               srcR = srcP >> 16 & 255;
               dstR = dstP >> 16 & 255;
               srcG = srcP >> 8 & 255;
               dstG = dstP >> 8 & 255;
               srcB = srcP & 255;
               dstB = dstP & 255;
            }

            srcSp += srcAdjust;
            dstInSp += dstInAdjust;
            dstOutSp += dstOutAdjust;
         }

      }
   }

   public static class ScreenCompositeContext extends AlphaPreCompositeContext {
      ScreenCompositeContext(ColorModel srcCM, ColorModel dstCM) {
         super(srcCM, dstCM);
      }

      public void precompose(Raster src, Raster dstIn, WritableRaster dstOut) {
         int[] srcPix = null;
         int[] dstPix = null;
         int x = dstOut.getMinX();
         int w = dstOut.getWidth();
         int y0 = dstOut.getMinY();
         int y1 = y0 + dstOut.getHeight();
         int norm = 65793;
         int pt5 = 8388608;

         for(int y = y0; y < y1; ++y) {
            srcPix = src.getPixels(x, y, w, 1, srcPix);
            dstPix = dstIn.getPixels(x, y, w, 1, dstPix);
            int sp = 0;

            for(int end = w * 4; sp < end; ++sp) {
               int iSrcPix = srcPix[sp];
               int iDstPix = dstPix[sp];
               dstPix[sp] = iSrcPix + iDstPix - (iDstPix * iSrcPix * 65793 + 8388608 >>> 24);
               ++sp;
               iSrcPix = srcPix[sp];
               iDstPix = dstPix[sp];
               dstPix[sp] = iSrcPix + iDstPix - (iDstPix * iSrcPix * 65793 + 8388608 >>> 24);
               ++sp;
               iSrcPix = srcPix[sp];
               iDstPix = dstPix[sp];
               dstPix[sp] = iSrcPix + iDstPix - (iDstPix * iSrcPix * 65793 + 8388608 >>> 24);
               ++sp;
               iSrcPix = srcPix[sp];
               iDstPix = dstPix[sp];
               dstPix[sp] = iSrcPix + iDstPix - (iDstPix * iSrcPix * 65793 + 8388608 >>> 24);
            }

            dstOut.setPixels(x, y, w, 1, dstPix);
         }

      }
   }

   public static class MultiplyCompositeContext_INT_PACK extends AlphaPreCompositeContext_INT_PACK {
      MultiplyCompositeContext_INT_PACK(ColorModel srcCM, ColorModel dstCM) {
         super(srcCM, dstCM);
      }

      public void precompose_INT_PACK(int width, int height, int[] srcPixels, int srcAdjust, int srcSp, int[] dstInPixels, int dstInAdjust, int dstInSp, int[] dstOutPixels, int dstOutAdjust, int dstOutSp) {
         int norm = 65793;
         int pt5 = 8388608;

         for(int y = 0; y < height; ++y) {
            int srcA;
            int srcR;
            int srcG;
            int srcB;
            int srcM;
            int dstA;
            int dstR;
            int dstG;
            int dstB;
            int dstM;
            for(int end = dstOutSp + width; dstOutSp < end; dstOutPixels[dstOutSp++] = ((srcR * srcM + dstR * dstM + srcR * dstR) * 65793 + 8388608 & -16777216) >>> 8 | ((srcG * srcM + dstG * dstM + srcG * dstG) * 65793 + 8388608 & -16777216) >>> 16 | (srcB * srcM + dstB * dstM + srcB * dstB) * 65793 + 8388608 >>> 24 | srcA + dstA - (srcA * dstA * 65793 + 8388608 >>> 24) << 24) {
               int srcP = srcPixels[srcSp++];
               int dstP = dstInPixels[dstInSp++];
               srcA = srcP >>> 24;
               dstA = dstP >>> 24;
               srcR = srcP >> 16 & 255;
               dstR = dstP >> 16 & 255;
               srcG = srcP >> 8 & 255;
               dstG = dstP >> 8 & 255;
               srcB = srcP & 255;
               dstB = dstP & 255;
               srcM = 255 - dstA;
               dstM = 255 - srcA;
            }

            srcSp += srcAdjust;
            dstInSp += dstInAdjust;
            dstOutSp += dstOutAdjust;
         }

      }
   }

   public static class MultiplyCompositeContext extends AlphaPreCompositeContext {
      MultiplyCompositeContext(ColorModel srcCM, ColorModel dstCM) {
         super(srcCM, dstCM);
      }

      public void precompose(Raster src, Raster dstIn, WritableRaster dstOut) {
         int[] srcPix = null;
         int[] dstPix = null;
         int x = dstOut.getMinX();
         int w = dstOut.getWidth();
         int y0 = dstOut.getMinY();
         int y1 = y0 + dstOut.getHeight();
         int norm = 65793;
         int pt5 = 8388608;

         for(int y = y0; y < y1; ++y) {
            srcPix = src.getPixels(x, y, w, 1, srcPix);
            dstPix = dstIn.getPixels(x, y, w, 1, dstPix);
            int sp = 0;

            for(int end = w * 4; sp < end; ++sp) {
               int srcM = 255 - dstPix[sp + 3];
               int dstM = 255 - srcPix[sp + 3];
               dstPix[sp] = (srcPix[sp] * srcM + dstPix[sp] * dstM + srcPix[sp] * dstPix[sp]) * 65793 + 8388608 >>> 24;
               ++sp;
               dstPix[sp] = (srcPix[sp] * srcM + dstPix[sp] * dstM + srcPix[sp] * dstPix[sp]) * 65793 + 8388608 >>> 24;
               ++sp;
               dstPix[sp] = (srcPix[sp] * srcM + dstPix[sp] * dstM + srcPix[sp] * dstPix[sp]) * 65793 + 8388608 >>> 24;
               ++sp;
               dstPix[sp] = srcPix[sp] + dstPix[sp] - (dstPix[sp] * srcPix[sp] * 65793 + 8388608 >>> 24);
            }

            dstOut.setPixels(x, y, w, 1, dstPix);
         }

      }
   }

   public static class ArithCompositeContext_INT_PACK_LUT extends AlphaPreCompositeContext_INT_PACK {
      byte[] lut;

      ArithCompositeContext_INT_PACK_LUT(ColorModel srcCM, ColorModel dstCM, float k1, float k2, float k3, float k4) {
         super(srcCM, dstCM);
         k1 /= 255.0F;
         k4 = k4 * 255.0F + 0.5F;
         int sz = 65536;
         this.lut = new byte[sz];

         for(int i = 0; i < sz; ++i) {
            int val = (int)((float)((i >> 8) * (i & 255)) * k1 + (float)(i >> 8) * k2 + (float)(i & 255) * k3 + k4);
            if ((val & -256) != 0) {
               if ((val & Integer.MIN_VALUE) != 0) {
                  val = 0;
               } else {
                  val = 255;
               }
            }

            this.lut[i] = (byte)val;
         }

      }

      public void precompose_INT_PACK(int width, int height, int[] srcPixels, int srcAdjust, int srcSp, int[] dstInPixels, int dstInAdjust, int dstInSp, int[] dstOutPixels, int dstOutAdjust, int dstOutSp) {
         byte[] workTbl = this.lut;

         for(int y = 0; y < height; ++y) {
            int a;
            int r;
            int g;
            int b;
            for(int end = dstOutSp + width; dstOutSp < end; dstOutPixels[dstOutSp++] = a << 24 | r << 16 | g << 8 | b) {
               int srcP = srcPixels[srcSp++];
               int dstP = dstInPixels[dstInSp++];
               a = 255 & workTbl[srcP >> 16 & '\uff00' | dstP >>> 24];
               r = 255 & workTbl[srcP >> 8 & '\uff00' | dstP >> 16 & 255];
               g = 255 & workTbl[srcP & '\uff00' | dstP >> 8 & 255];
               b = 255 & workTbl[srcP << 8 & '\uff00' | dstP & 255];
               if (r > a) {
                  a = r;
               }

               if (g > a) {
                  a = g;
               }

               if (b > a) {
                  a = b;
               }
            }

            srcSp += srcAdjust;
            dstInSp += dstInAdjust;
            dstOutSp += dstOutAdjust;
         }

      }
   }

   public static class ArithCompositeContext_INT_PACK extends AlphaPreCompositeContext_INT_PACK {
      float k1;
      float k2;
      float k3;
      float k4;

      ArithCompositeContext_INT_PACK(ColorModel srcCM, ColorModel dstCM, float k1, float k2, float k3, float k4) {
         super(srcCM, dstCM);
         this.k1 = k1 / 255.0F;
         this.k2 = k2;
         this.k3 = k3;
         this.k4 = k4 * 255.0F + 0.5F;
      }

      public void precompose_INT_PACK(int width, int height, int[] srcPixels, int srcAdjust, int srcSp, int[] dstInPixels, int dstInAdjust, int dstInSp, int[] dstOutPixels, int dstOutAdjust, int dstOutSp) {
         for(int y = 0; y < height; ++y) {
            int a;
            int r;
            int g;
            int b;
            for(int end = dstOutSp + width; dstOutSp < end; dstOutPixels[dstOutSp++] = a << 24 | r << 16 | g << 8 | b) {
               int srcP = srcPixels[srcSp++];
               int dstP = dstInPixels[dstInSp++];
               a = (int)((float)((srcP >>> 24) * (dstP >>> 24)) * this.k1 + (float)(srcP >>> 24) * this.k2 + (float)(dstP >>> 24) * this.k3 + this.k4);
               if ((a & -256) != 0) {
                  if ((a & Integer.MIN_VALUE) != 0) {
                     a = 0;
                  } else {
                     a = 255;
                  }
               }

               r = (int)((float)((srcP >> 16 & 255) * (dstP >> 16 & 255)) * this.k1 + (float)(srcP >> 16 & 255) * this.k2 + (float)(dstP >> 16 & 255) * this.k3 + this.k4);
               if ((r & -256) != 0) {
                  if ((r & Integer.MIN_VALUE) != 0) {
                     r = 0;
                  } else {
                     r = 255;
                  }
               }

               if (a < r) {
                  a = r;
               }

               g = (int)((float)((srcP >> 8 & 255) * (dstP >> 8 & 255)) * this.k1 + (float)(srcP >> 8 & 255) * this.k2 + (float)(dstP >> 8 & 255) * this.k3 + this.k4);
               if ((g & -256) != 0) {
                  if ((g & Integer.MIN_VALUE) != 0) {
                     g = 0;
                  } else {
                     g = 255;
                  }
               }

               if (a < g) {
                  a = g;
               }

               b = (int)((float)((srcP & 255) * (dstP & 255)) * this.k1 + (float)(srcP & 255) * this.k2 + (float)(dstP & 255) * this.k3 + this.k4);
               if ((b & -256) != 0) {
                  if ((b & Integer.MIN_VALUE) != 0) {
                     b = 0;
                  } else {
                     b = 255;
                  }
               }

               if (a < b) {
                  a = b;
               }
            }

            srcSp += srcAdjust;
            dstInSp += dstInAdjust;
            dstOutSp += dstOutAdjust;
         }

      }
   }

   public static class ArithCompositeContext extends AlphaPreCompositeContext {
      float k1;
      float k2;
      float k3;
      float k4;

      ArithCompositeContext(ColorModel srcCM, ColorModel dstCM, float k1, float k2, float k3, float k4) {
         super(srcCM, dstCM);
         this.k1 = k1;
         this.k2 = k2;
         this.k3 = k3;
         this.k4 = k4;
      }

      public void precompose(Raster src, Raster dstIn, WritableRaster dstOut) {
         int[] srcPix = null;
         int[] dstPix = null;
         int x = dstOut.getMinX();
         int w = dstOut.getWidth();
         int bands = dstOut.getNumBands();
         int y0 = dstOut.getMinY();
         int y1 = y0 + dstOut.getHeight();
         float kk1 = this.k1 / 255.0F;
         float kk4 = this.k4 * 255.0F + 0.5F;

         for(int y = y0; y < y1; ++y) {
            srcPix = src.getPixels(x, y, w, 1, srcPix);
            dstPix = dstIn.getPixels(x, y, w, 1, dstPix);

            for(int i = 0; i < srcPix.length; ++i) {
               int max = 0;

               int val;
               for(int b = 1; b < bands; ++i) {
                  val = (int)(kk1 * (float)srcPix[i] * (float)dstPix[i] + this.k2 * (float)srcPix[i] + this.k3 * (float)dstPix[i] + kk4);
                  if ((val & -256) != 0) {
                     if ((val & Integer.MIN_VALUE) != 0) {
                        val = 0;
                     } else {
                        val = 255;
                     }
                  }

                  if (val > max) {
                     max = val;
                  }

                  dstPix[i] = val;
                  ++b;
               }

               val = (int)(kk1 * (float)srcPix[i] * (float)dstPix[i] + this.k2 * (float)srcPix[i] + this.k3 * (float)dstPix[i] + kk4);
               if ((val & -256) != 0) {
                  if ((val & Integer.MIN_VALUE) != 0) {
                     val = 0;
                  } else {
                     val = 255;
                  }
               }

               if (val > max) {
                  dstPix[i] = val;
               } else {
                  dstPix[i] = max;
               }
            }

            dstOut.setPixels(x, y, w, 1, dstPix);
         }

      }
   }

   public static class XorCompositeContext_INT_PACK extends AlphaPreCompositeContext_INT_PACK {
      XorCompositeContext_INT_PACK(ColorModel srcCM, ColorModel dstCM) {
         super(srcCM, dstCM);
      }

      public void precompose_INT_PACK(int width, int height, int[] srcPixels, int srcAdjust, int srcSp, int[] dstInPixels, int dstInAdjust, int dstInSp, int[] dstOutPixels, int dstOutAdjust, int dstOutSp) {
         int norm = 65793;
         int pt5 = 8388608;

         for(int y = 0; y < height; ++y) {
            int srcP;
            int srcM;
            int dstP;
            int dstM;
            for(int end = dstOutSp + width; dstOutSp < end; dstOutPixels[dstOutSp++] = (srcP >>> 24) * srcM + (dstP >>> 24) * dstM + 8388608 & -16777216 | ((srcP >> 16 & 255) * srcM + (dstP >> 16 & 255) * dstM + 8388608 & -16777216) >>> 8 | ((srcP >> 8 & 255) * srcM + (dstP >> 8 & 255) * dstM + 8388608 & -16777216) >>> 16 | (srcP & 255) * srcM + (dstP & 255) * dstM + 8388608 >>> 24) {
               srcP = srcPixels[srcSp++];
               dstP = dstInPixels[dstInSp++];
               srcM = (255 - (dstP >>> 24)) * 65793;
               dstM = (255 - (srcP >>> 24)) * 65793;
            }

            srcSp += srcAdjust;
            dstInSp += dstInAdjust;
            dstOutSp += dstOutAdjust;
         }

      }
   }

   public static class XorCompositeContext extends AlphaPreCompositeContext {
      XorCompositeContext(ColorModel srcCM, ColorModel dstCM) {
         super(srcCM, dstCM);
      }

      public void precompose(Raster src, Raster dstIn, WritableRaster dstOut) {
         int[] srcPix = null;
         int[] dstPix = null;
         int x = dstOut.getMinX();
         int w = dstOut.getWidth();
         int y0 = dstOut.getMinY();
         int y1 = y0 + dstOut.getHeight();
         int norm = 65793;
         int pt5 = 8388608;

         for(int y = y0; y < y1; ++y) {
            srcPix = src.getPixels(x, y, w, 1, srcPix);
            dstPix = dstIn.getPixels(x, y, w, 1, dstPix);
            int sp = 0;

            for(int end = w * 4; sp < end; ++sp) {
               int srcM = (255 - dstPix[sp + 3]) * 65793;
               int dstM = (255 - srcPix[sp + 3]) * 65793;
               dstPix[sp] = srcPix[sp] * srcM + dstPix[sp] * dstM + 8388608 >>> 24;
               ++sp;
               dstPix[sp] = srcPix[sp] * srcM + dstPix[sp] * dstM + 8388608 >>> 24;
               ++sp;
               dstPix[sp] = srcPix[sp] * srcM + dstPix[sp] * dstM + 8388608 >>> 24;
               ++sp;
               dstPix[sp] = srcPix[sp] * srcM + dstPix[sp] * dstM + 8388608 >>> 24;
            }

            dstOut.setPixels(x, y, w, 1, dstPix);
         }

      }
   }

   public static class AtopCompositeContext_INT_PACK extends AlphaPreCompositeContext_INT_PACK {
      AtopCompositeContext_INT_PACK(ColorModel srcCM, ColorModel dstCM) {
         super(srcCM, dstCM);
      }

      public void precompose_INT_PACK(int width, int height, int[] srcPixels, int srcAdjust, int srcSp, int[] dstInPixels, int dstInAdjust, int dstInSp, int[] dstOutPixels, int dstOutAdjust, int dstOutSp) {
         int norm = 65793;
         int pt5 = 8388608;

         for(int y = 0; y < height; ++y) {
            int srcP;
            int srcM;
            int dstP;
            int dstM;
            for(int end = dstOutSp + width; dstOutSp < end; dstOutPixels[dstOutSp++] = dstP & -16777216 | ((srcP >> 16 & 255) * srcM + (dstP >> 16 & 255) * dstM + 8388608 & -16777216) >>> 8 | ((srcP >> 8 & 255) * srcM + (dstP >> 8 & 255) * dstM + 8388608 & -16777216) >>> 16 | (srcP & 255) * srcM + (dstP & 255) * dstM + 8388608 >>> 24) {
               srcP = srcPixels[srcSp++];
               dstP = dstInPixels[dstInSp++];
               srcM = (dstP >>> 24) * 65793;
               dstM = (255 - (srcP >>> 24)) * 65793;
            }

            srcSp += srcAdjust;
            dstInSp += dstInAdjust;
            dstOutSp += dstOutAdjust;
         }

      }
   }

   public static class AtopCompositeContext extends AlphaPreCompositeContext {
      AtopCompositeContext(ColorModel srcCM, ColorModel dstCM) {
         super(srcCM, dstCM);
      }

      public void precompose(Raster src, Raster dstIn, WritableRaster dstOut) {
         int[] srcPix = null;
         int[] dstPix = null;
         int x = dstOut.getMinX();
         int w = dstOut.getWidth();
         int y0 = dstOut.getMinY();
         int y1 = y0 + dstOut.getHeight();
         int norm = 65793;
         int pt5 = 8388608;

         for(int y = y0; y < y1; ++y) {
            srcPix = src.getPixels(x, y, w, 1, srcPix);
            dstPix = dstIn.getPixels(x, y, w, 1, dstPix);
            int sp = 0;

            for(int end = w * 4; sp < end; sp += 2) {
               int srcM = dstPix[sp + 3] * 65793;
               int dstM = (255 - srcPix[sp + 3]) * 65793;
               dstPix[sp] = srcPix[sp] * srcM + dstPix[sp] * dstM + 8388608 >>> 24;
               ++sp;
               dstPix[sp] = srcPix[sp] * srcM + dstPix[sp] * dstM + 8388608 >>> 24;
               ++sp;
               dstPix[sp] = srcPix[sp] * srcM + dstPix[sp] * dstM + 8388608 >>> 24;
            }

            dstOut.setPixels(x, y, w, 1, dstPix);
         }

      }
   }

   public static class OutCompositeContext_INT_PACK extends AlphaPreCompositeContext_INT_PACK {
      OutCompositeContext_INT_PACK(ColorModel srcCM, ColorModel dstCM) {
         super(srcCM, dstCM);
      }

      public void precompose_INT_PACK(int width, int height, int[] srcPixels, int srcAdjust, int srcSp, int[] dstInPixels, int dstInAdjust, int dstInSp, int[] dstOutPixels, int dstOutAdjust, int dstOutSp) {
         int norm = 65793;
         int pt5 = 8388608;

         for(int y = 0; y < height; ++y) {
            int srcP;
            int srcM;
            for(int end = dstOutSp + width; dstOutSp < end; dstOutPixels[dstOutSp++] = (srcP >>> 24) * srcM + 8388608 & -16777216 | ((srcP >> 16 & 255) * srcM + 8388608 & -16777216) >>> 8 | ((srcP >> 8 & 255) * srcM + 8388608 & -16777216) >>> 16 | (srcP & 255) * srcM + 8388608 >>> 24) {
               srcM = (255 - (dstInPixels[dstInSp++] >>> 24)) * 65793;
               srcP = srcPixels[srcSp++];
            }

            srcSp += srcAdjust;
            dstInSp += dstInAdjust;
            dstOutSp += dstOutAdjust;
         }

      }
   }

   public static class OutCompositeContext extends AlphaPreCompositeContext {
      OutCompositeContext(ColorModel srcCM, ColorModel dstCM) {
         super(srcCM, dstCM);
      }

      public void precompose(Raster src, Raster dstIn, WritableRaster dstOut) {
         int[] srcPix = null;
         int[] dstPix = null;
         int x = dstOut.getMinX();
         int w = dstOut.getWidth();
         int y0 = dstOut.getMinY();
         int y1 = y0 + dstOut.getHeight();
         int norm = 65793;
         int pt5 = 8388608;

         for(int y = y0; y < y1; ++y) {
            srcPix = src.getPixels(x, y, w, 1, srcPix);
            dstPix = dstIn.getPixels(x, y, w, 1, dstPix);
            int sp = 0;

            for(int end = w * 4; sp < end; ++sp) {
               int srcM = (255 - dstPix[sp + 3]) * 65793;
               dstPix[sp] = srcPix[sp] * srcM + 8388608 >>> 24;
               ++sp;
               dstPix[sp] = srcPix[sp] * srcM + 8388608 >>> 24;
               ++sp;
               dstPix[sp] = srcPix[sp] * srcM + 8388608 >>> 24;
               ++sp;
               dstPix[sp] = srcPix[sp] * srcM + 8388608 >>> 24;
            }

            dstOut.setPixels(x, y, w, 1, dstPix);
         }

      }
   }

   public static class InCompositeContext_INT_PACK extends AlphaPreCompositeContext_INT_PACK {
      InCompositeContext_INT_PACK(ColorModel srcCM, ColorModel dstCM) {
         super(srcCM, dstCM);
      }

      public void precompose_INT_PACK(int width, int height, int[] srcPixels, int srcAdjust, int srcSp, int[] dstInPixels, int dstInAdjust, int dstInSp, int[] dstOutPixels, int dstOutAdjust, int dstOutSp) {
         int norm = 65793;
         int pt5 = 8388608;

         for(int y = 0; y < height; ++y) {
            int srcP;
            int srcM;
            for(int end = dstOutSp + width; dstOutSp < end; dstOutPixels[dstOutSp++] = (srcP >>> 24) * srcM + 8388608 & -16777216 | ((srcP >> 16 & 255) * srcM + 8388608 & -16777216) >>> 8 | ((srcP >> 8 & 255) * srcM + 8388608 & -16777216) >>> 16 | (srcP & 255) * srcM + 8388608 >>> 24) {
               srcM = (dstInPixels[dstInSp++] >>> 24) * 65793;
               srcP = srcPixels[srcSp++];
            }

            srcSp += srcAdjust;
            dstInSp += dstInAdjust;
            dstOutSp += dstOutAdjust;
         }

      }
   }

   public static class InCompositeContext extends AlphaPreCompositeContext {
      InCompositeContext(ColorModel srcCM, ColorModel dstCM) {
         super(srcCM, dstCM);
      }

      public void precompose(Raster src, Raster dstIn, WritableRaster dstOut) {
         int[] srcPix = null;
         int[] dstPix = null;
         int x = dstOut.getMinX();
         int w = dstOut.getWidth();
         int y0 = dstOut.getMinY();
         int y1 = y0 + dstOut.getHeight();
         int norm = 65793;
         int pt5 = 8388608;

         for(int y = y0; y < y1; ++y) {
            srcPix = src.getPixels(x, y, w, 1, srcPix);
            dstPix = dstIn.getPixels(x, y, w, 1, dstPix);
            int sp = 0;

            for(int end = w * 4; sp < end; ++sp) {
               int srcM = dstPix[sp + 3] * 65793;
               dstPix[sp] = srcPix[sp] * srcM + 8388608 >>> 24;
               ++sp;
               dstPix[sp] = srcPix[sp] * srcM + 8388608 >>> 24;
               ++sp;
               dstPix[sp] = srcPix[sp] * srcM + 8388608 >>> 24;
               ++sp;
               dstPix[sp] = srcPix[sp] * srcM + 8388608 >>> 24;
            }

            dstOut.setPixels(x, y, w, 1, dstPix);
         }

      }
   }

   public static class OverCompositeContext_INT_PACK_UNPRE extends AlphaPreCompositeContext_INT_PACK {
      OverCompositeContext_INT_PACK_UNPRE(ColorModel srcCM, ColorModel dstCM) {
         super(srcCM, dstCM);
         if (srcCM.isAlphaPremultiplied()) {
            throw new IllegalArgumentException("OverCompositeContext_INT_PACK_UNPRE is only forsources with unpremultiplied alpha");
         }
      }

      public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
         ColorModel dstPreCM = this.dstCM;
         if (!this.dstCM.isAlphaPremultiplied()) {
            dstPreCM = GraphicsUtil.coerceData((WritableRaster)dstIn, this.dstCM, true);
         }

         this.precompose(src, dstIn, dstOut);
         if (!this.dstCM.isAlphaPremultiplied()) {
            GraphicsUtil.coerceData(dstOut, dstPreCM, false);
            if (dstIn != dstOut) {
               GraphicsUtil.coerceData((WritableRaster)dstIn, dstPreCM, false);
            }
         }

      }

      public void precompose_INT_PACK(int width, int height, int[] srcPixels, int srcAdjust, int srcSp, int[] dstInPixels, int dstInAdjust, int dstInSp, int[] dstOutPixels, int dstOutAdjust, int dstOutSp) {
         int norm = 65793;
         int pt5 = 8388608;

         for(int y = 0; y < height; ++y) {
            int srcP;
            int srcM;
            int dstP;
            int dstM;
            for(int end = dstOutSp + width; dstOutSp < end; dstOutPixels[dstOutSp++] = (srcP & -16777216) + (dstP >>> 24) * dstM + 8388608 & -16777216 | ((srcP >> 16 & 255) * srcM + (dstP >> 16 & 255) * dstM + 8388608 & -16777216) >>> 8 | ((srcP >> 8 & 255) * srcM + (dstP >> 8 & 255) * dstM + 8388608 & -16777216) >>> 16 | (srcP & 255) * srcM + (dstP & 255) * dstM + 8388608 >>> 24) {
               srcP = srcPixels[srcSp++];
               dstP = dstInPixels[dstInSp++];
               srcM = (srcP >>> 24) * 65793;
               dstM = (255 - (srcP >>> 24)) * 65793;
            }

            srcSp += srcAdjust;
            dstInSp += dstInAdjust;
            dstOutSp += dstOutAdjust;
         }

      }
   }

   public static class OverCompositeContext_INT_PACK_NA extends AlphaPreCompositeContext_INT_PACK {
      OverCompositeContext_INT_PACK_NA(ColorModel srcCM, ColorModel dstCM) {
         super(srcCM, dstCM);
      }

      public void precompose_INT_PACK(int width, int height, int[] srcPixels, int srcAdjust, int srcSp, int[] dstInPixels, int dstInAdjust, int dstInSp, int[] dstOutPixels, int dstOutAdjust, int dstOutSp) {
         int norm = 65793;
         int pt5 = 8388608;

         for(int y = 0; y < height; ++y) {
            int srcP;
            int dstInP;
            int dstM;
            for(int end = dstOutSp + width; dstOutSp < end; dstOutPixels[dstOutSp++] = (srcP & 16711680) + (((dstInP >> 16 & 255) * dstM + 8388608 & -16777216) >>> 8) | (srcP & '\uff00') + (((dstInP >> 8 & 255) * dstM + 8388608 & -16777216) >>> 16) | (srcP & 255) + ((dstInP & 255) * dstM + 8388608 >>> 24)) {
               srcP = srcPixels[srcSp++];
               dstInP = dstInPixels[dstInSp++];
               dstM = (255 - (srcP >>> 24)) * 65793;
            }

            srcSp += srcAdjust;
            dstInSp += dstInAdjust;
            dstOutSp += dstOutAdjust;
         }

      }
   }

   public static class OverCompositeContext_INT_PACK extends AlphaPreCompositeContext_INT_PACK {
      OverCompositeContext_INT_PACK(ColorModel srcCM, ColorModel dstCM) {
         super(srcCM, dstCM);
      }

      public void precompose_INT_PACK(int width, int height, int[] srcPixels, int srcAdjust, int srcSp, int[] dstInPixels, int dstInAdjust, int dstInSp, int[] dstOutPixels, int dstOutAdjust, int dstOutSp) {
         int norm = 65793;
         int pt5 = 8388608;

         for(int y = 0; y < height; ++y) {
            int srcP;
            int dstInP;
            int dstM;
            for(int end = dstOutSp + width; dstOutSp < end; dstOutPixels[dstOutSp++] = (srcP & -16777216) + ((dstInP >>> 24) * dstM + 8388608 & -16777216) | (srcP & 16711680) + (((dstInP >> 16 & 255) * dstM + 8388608 & -16777216) >>> 8) | (srcP & '\uff00') + (((dstInP >> 8 & 255) * dstM + 8388608 & -16777216) >>> 16) | (srcP & 255) + ((dstInP & 255) * dstM + 8388608 >>> 24)) {
               srcP = srcPixels[srcSp++];
               dstInP = dstInPixels[dstInSp++];
               dstM = (255 - (srcP >>> 24)) * 65793;
            }

            srcSp += srcAdjust;
            dstInSp += dstInAdjust;
            dstOutSp += dstOutAdjust;
         }

      }
   }

   public static class OverCompositeContext_NA extends AlphaPreCompositeContext {
      OverCompositeContext_NA(ColorModel srcCM, ColorModel dstCM) {
         super(srcCM, dstCM);
      }

      public void precompose(Raster src, Raster dstIn, WritableRaster dstOut) {
         int[] srcPix = null;
         int[] dstPix = null;
         int x = dstOut.getMinX();
         int w = dstOut.getWidth();
         int y0 = dstOut.getMinY();
         int y1 = y0 + dstOut.getHeight();
         int norm = 65793;
         int pt5 = 8388608;

         for(int y = y0; y < y1; ++y) {
            srcPix = src.getPixels(x, y, w, 1, srcPix);
            dstPix = dstIn.getPixels(x, y, w, 1, dstPix);
            int srcSP = 0;
            int dstSP = 0;

            for(int end = w * 4; srcSP < end; ++dstSP) {
               int dstM = (255 - srcPix[srcSP + 3]) * 65793;
               dstPix[dstSP] = srcPix[srcSP] + (dstPix[dstSP] * dstM + 8388608 >>> 24);
               ++srcSP;
               ++dstSP;
               dstPix[dstSP] = srcPix[srcSP] + (dstPix[dstSP] * dstM + 8388608 >>> 24);
               ++srcSP;
               ++dstSP;
               dstPix[dstSP] = srcPix[srcSP] + (dstPix[dstSP] * dstM + 8388608 >>> 24);
               srcSP += 2;
            }

            dstOut.setPixels(x, y, w, 1, dstPix);
         }

      }
   }

   public static class OverCompositeContext extends AlphaPreCompositeContext {
      OverCompositeContext(ColorModel srcCM, ColorModel dstCM) {
         super(srcCM, dstCM);
      }

      public void precompose(Raster src, Raster dstIn, WritableRaster dstOut) {
         int[] srcPix = null;
         int[] dstPix = null;
         int x = dstOut.getMinX();
         int w = dstOut.getWidth();
         int y0 = dstOut.getMinY();
         int y1 = y0 + dstOut.getHeight();
         int norm = 65793;
         int pt5 = 8388608;

         for(int y = y0; y < y1; ++y) {
            srcPix = src.getPixels(x, y, w, 1, srcPix);
            dstPix = dstIn.getPixels(x, y, w, 1, dstPix);
            int sp = 0;

            for(int end = w * 4; sp < end; ++sp) {
               int dstM = (255 - srcPix[sp + 3]) * 65793;
               dstPix[sp] = srcPix[sp] + (dstPix[sp] * dstM + 8388608 >>> 24);
               ++sp;
               dstPix[sp] = srcPix[sp] + (dstPix[sp] * dstM + 8388608 >>> 24);
               ++sp;
               dstPix[sp] = srcPix[sp] + (dstPix[sp] * dstM + 8388608 >>> 24);
               ++sp;
               dstPix[sp] = srcPix[sp] + (dstPix[sp] * dstM + 8388608 >>> 24);
            }

            dstOut.setPixels(x, y, w, 1, dstPix);
         }

      }
   }

   public abstract static class AlphaPreCompositeContext_INT_PACK extends AlphaPreCompositeContext {
      AlphaPreCompositeContext_INT_PACK(ColorModel srcCM, ColorModel dstCM) {
         super(srcCM, dstCM);
      }

      protected abstract void precompose_INT_PACK(int var1, int var2, int[] var3, int var4, int var5, int[] var6, int var7, int var8, int[] var9, int var10, int var11);

      protected void precompose(Raster src, Raster dstIn, WritableRaster dstOut) {
         int x0 = dstOut.getMinX();
         int w = dstOut.getWidth();
         int y0 = dstOut.getMinY();
         int h = dstOut.getHeight();
         SinglePixelPackedSampleModel srcSPPSM = (SinglePixelPackedSampleModel)src.getSampleModel();
         int srcScanStride = srcSPPSM.getScanlineStride();
         DataBufferInt srcDB = (DataBufferInt)src.getDataBuffer();
         int[] srcPixels = srcDB.getBankData()[0];
         int srcBase = srcDB.getOffset() + srcSPPSM.getOffset(x0 - src.getSampleModelTranslateX(), y0 - src.getSampleModelTranslateY());
         SinglePixelPackedSampleModel dstInSPPSM = (SinglePixelPackedSampleModel)dstIn.getSampleModel();
         int dstInScanStride = dstInSPPSM.getScanlineStride();
         DataBufferInt dstInDB = (DataBufferInt)dstIn.getDataBuffer();
         int[] dstInPixels = dstInDB.getBankData()[0];
         int dstInBase = dstInDB.getOffset() + dstInSPPSM.getOffset(x0 - dstIn.getSampleModelTranslateX(), y0 - dstIn.getSampleModelTranslateY());
         SinglePixelPackedSampleModel dstOutSPPSM = (SinglePixelPackedSampleModel)dstOut.getSampleModel();
         int dstOutScanStride = dstOutSPPSM.getScanlineStride();
         DataBufferInt dstOutDB = (DataBufferInt)dstOut.getDataBuffer();
         int[] dstOutPixels = dstOutDB.getBankData()[0];
         int dstOutBase = dstOutDB.getOffset() + dstOutSPPSM.getOffset(x0 - dstOut.getSampleModelTranslateX(), y0 - dstOut.getSampleModelTranslateY());
         int srcAdjust = srcScanStride - w;
         int dstInAdjust = dstInScanStride - w;
         int dstOutAdjust = dstOutScanStride - w;
         this.precompose_INT_PACK(w, h, srcPixels, srcAdjust, srcBase, dstInPixels, dstInAdjust, dstInBase, dstOutPixels, dstOutAdjust, dstOutBase);
      }
   }

   public abstract static class AlphaPreCompositeContext implements CompositeContext {
      ColorModel srcCM;
      ColorModel dstCM;

      AlphaPreCompositeContext(ColorModel srcCM, ColorModel dstCM) {
         this.srcCM = srcCM;
         this.dstCM = dstCM;
      }

      public void dispose() {
         this.srcCM = null;
         this.dstCM = null;
      }

      protected abstract void precompose(Raster var1, Raster var2, WritableRaster var3);

      public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
         ColorModel srcPreCM = this.srcCM;
         if (!this.srcCM.isAlphaPremultiplied()) {
            srcPreCM = GraphicsUtil.coerceData((WritableRaster)src, this.srcCM, true);
         }

         ColorModel dstPreCM = this.dstCM;
         if (!this.dstCM.isAlphaPremultiplied()) {
            dstPreCM = GraphicsUtil.coerceData((WritableRaster)dstIn, this.dstCM, true);
         }

         this.precompose(src, dstIn, dstOut);
         if (!this.srcCM.isAlphaPremultiplied()) {
            GraphicsUtil.coerceData((WritableRaster)src, srcPreCM, false);
         }

         if (!this.dstCM.isAlphaPremultiplied()) {
            GraphicsUtil.coerceData(dstOut, dstPreCM, false);
            if (dstIn != dstOut) {
               GraphicsUtil.coerceData((WritableRaster)dstIn, dstPreCM, false);
            }
         }

      }
   }
}
